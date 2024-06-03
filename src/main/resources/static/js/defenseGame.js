window.onload = function() {
    const config = {
        type: Phaser.AUTO,
        width: 800,
        height: 600,
        physics: {
            default: 'arcade',
            arcade: {
                debug: false // 디버그 모드 비활성화
            }
        },
        scene: {
            preload: preload,
            create: create,
            update: update
        }
    };

    const game = new Phaser.Game(config);
    let selectedTowerType = null;
    let towers = [];
    let cursorTower = null;
    let cancelMarker = null;
    const TOWER_ATTACK_RANGE = 150; // 타워 공격 범위 설정
    const FLAME_SPEED = 1000; // 불꽃 속도 설정
    const TOWER_RADIUS = 20; // 타워 설치 반경 (충돌 판정을 위한 값)

    function preload() {
        this.load.image('background', 'assets/defense/tiles/land_1.png');
        this.load.image('flameTower', 'assets/defense/towers/flameTowers_1.png');
        this.load.image('path', 'assets/defense/tiles/decor_6.png');
        this.load.image('flame_1', 'assets/defense/towers/flame_1.png');
        this.load.image('flame_2', 'assets/defense/towers/flame_2.png');
        this.load.image('flame_3', 'assets/defense/towers/flame_3.png');
        this.load.image('flame_4', 'assets/defense/towers/flame_4.png');
        this.load.image('flame_5', 'assets/defense/towers/flame_5.png');
        this.load.image('cancel', 'assets/defense/cancel.png'); // cancel 이미지 로드
        for (let i = 1; i <= 24; i++) {
            this.load.image(`enemy_walk_${i}`, `assets/defense/enemies/orc_enemy_walk_${i}.png`);
        }
    }

    function create() {
        const self = this;

        // 배경 설정
        self.add.tileSprite(400, 300, 800, 600, 'background');

        // 경로 설정
        const path = self.add.path(50, 50);
        path.lineTo(750, 50);
        path.lineTo(750, 550);
        path.lineTo(50, 550);
        path.lineTo(50, 50);

        const graphics = self.add.graphics();
        graphics.lineStyle(3, 0xffffff, 1);
        path.draw(graphics);

        // 애니메이션 생성
        const walkFrames = [];
        for (let i = 1; i <= 24; i++) {
            walkFrames.push({ key: `enemy_walk_${i}` });
        }

        self.anims.create({
            key: 'enemy_walk_anim',
            frames: walkFrames,
            frameRate: 10,
            repeat: -1
        });

        // 적 추가
        self.enemies = self.add.group();
        let enemyCount = 0;
        self.time.addEvent({
            delay: 1000,
            callback: function() {
                if (enemyCount < 10) {
                    const enemy = self.add.follower(path, 50, 50, 'enemy_walk_1').setScale(0.05);
                    enemy.health = 100;
                    self.enemies.add(enemy);
                    enemy.startFollow({
                        duration: 10000,
                        repeat: -1,
                        rotateToPath: true
                    });

                    enemy.play('enemy_walk_anim');

                    // 체력바 생성
                    const healthBar = self.add.graphics();
                    enemy.healthBar = healthBar;
                    updateHealthBar(enemy);

                    enemyCount++;
                }
            },
            callbackScope: self,
            loop: true
        });

        // 타워 설치 이벤트
        self.input.on('pointerdown', function(pointer) {
            if (selectedTowerType) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                // 원 내부인지 확인
                const distance = Phaser.Math.Distance.Between(400, 300, x, y);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                if (distance <= 200 && !isOccupied) {
                    const tower = self.add.sprite(x, y, selectedTowerType).setScale(0.2);
                    towers.push(tower);
                    selectedTowerType = null;
                    cursorTower.destroy();
                    cursorTower = null;
                    if (cancelMarker) {
                        cancelMarker.destroy();
                        cancelMarker = null;
                    }
                }
            }
        });

        self.input.on('pointermove', function(pointer) {
            if (cursorTower) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                cursorTower.x = x;
                cursorTower.y = y;

                // 원 내부인지 확인
                const distance = Phaser.Math.Distance.Between(400, 300, x, y);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                if (distance > 200 || isOccupied) {
                    if (!cancelMarker) {
                        cancelMarker = self.add.sprite(x, y, 'cancel').setScale(0.1);
                    }
                    cancelMarker.x = x;
                    cancelMarker.y = y;
                } else if (cancelMarker) {
                    cancelMarker.destroy();
                    cancelMarker = null;
                }
            }
        });

        // 타워 선택 UI
        createTowerSelectionUI(self);

        // 불꽃 공격 테스트
        self.time.addEvent({
            delay: 500, // 더 자주 공격
            callback: function() {
                towers.forEach(function(tower) {
                    self.enemies.getChildren().forEach(function(enemy) {
                        const distance = Phaser.Math.Distance.Between(tower.x, tower.y, enemy.x, enemy.y);
                        if (enemy.active && distance <= TOWER_ATTACK_RANGE) {
                            const flame = self.physics.add.sprite(tower.x, tower.y, 'flame_1').setScale(0.5);
                            if (!flame) {
                                console.error('Failed to create flame sprite');
                                return;
                            }
                            self.physics.moveToObject(flame, enemy, FLAME_SPEED);
                            self.physics.add.overlap(flame, enemy, function(flame, enemy) {
                                flame.destroy(); // 불꽃 제거
                                hitEnemy(self, flame, enemy);
                            }, null, self);
                        }
                    });
                });
            },
            callbackScope: self,
            loop: true
        });
    }

    function update() {
        // 적의 체력바 업데이트
        this.enemies.getChildren().forEach(function(enemy) {
            if (enemy.active) {
                updateHealthBar(enemy);
            }
        });
    }

    function createTowerSelectionUI(scene) {
        const flameTowerButton = scene.add.text(700, 50, 'Flame Tower', { fill: '#0f0' })
            .setInteractive()
            .on('pointerdown', () => {
                selectedTowerType = 'flameTower';
                if (cursorTower) cursorTower.destroy();
                cursorTower = scene.add.sprite(scene.input.activePointer.worldX, scene.input.activePointer.worldY, 'flameTower').setScale(0.2);
                cursorTower.setAlpha(0.5);
                if (cancelMarker) {
                    cancelMarker.destroy();
                    cancelMarker = null;
                }
            });

        // 추가적인 타워 타입이 있을 경우 버튼을 추가로 설정
        // 예:
        // const anotherTowerButton = scene.add.text(700, 100, 'Another Tower', { fill: '#0f0' })
        //     .setInteractive()
        //     .on('pointerdown', () => {
        //         selectedTowerType = 'another_tower';
        //         if (cursorTower) cursorTower.destroy();
        //         cursorTower = scene.add.sprite(scene.input.activePointer.worldX, scene.input.activePointer.worldY, 'anotherTower').setScale(0.2);
        //         cursorTower.setAlpha(0.5);
        //         if (cancelMarker) {
        //             cancelMarker.destroy();
        //             cancelMarker = null;
        //         }
        //     });
    }

    function updateHealthBar(enemy) {
        const x = enemy.x - 20;
        const y = enemy.y - 30;
        enemy.healthBar.clear();
        enemy.healthBar.fillStyle(0x00ff00, 1);
        enemy.healthBar.fillRect(x, y, 40 * (enemy.health / 100), 5);
    }

    function hitEnemy(scene, flame, enemy) {
        if (enemy.active) {
            enemy.health -= 20; // 적 체력 감소
            if (enemy.health <= 0) {
                enemy.healthBar.destroy(); // 체력바 제거
                enemy.destroy(); // 적 제거
            } else {
                // 체력바 업데이트
                updateHealthBar(enemy);
            }
        }
    }
};
