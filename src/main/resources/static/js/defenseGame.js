window.onload = function() {
    const config = {
        type: Phaser.AUTO,
        width: 800,
        height: 600,
        physics: {
            default: 'arcade',
            arcade: {
                debug: false
            }
        },
        scene: {
            preload: preload,
            create: create,
            update: update
        }
    };

    const game = new Phaser.Game(config);

    function preload() {
        this.load.image('background', 'assets/defense/tiles/land_1.png');
        this.load.image('archer_tower', 'assets/defense/towers/towers_1.png');
        this.load.image('path', 'assets/defense/tiles/decor_6.png');
        this.load.image('flame_1', 'assets/defense/towers/flame_1.png');
        this.load.image('flame_2', 'assets/defense/towers/flame_2.png');
        this.load.image('flame_3', 'assets/defense/towers/flame_3.png');
        this.load.image('flame_4', 'assets/defense/towers/flame_4.png');
        this.load.image('flame_5', 'assets/defense/towers/flame_5.png');
        for (let i = 1; i <= 24; i++) {
            this.load.image(`enemy_walk_${i}`, `assets/defense/enemies/orc_enemy_walk_${i}.png`);
        }
    }

    function create() {
        const self = this;

        // 배경 설정
        self.add.tileSprite(400, 300, 800, 600, 'background');

        // 타워 설정
        const archerTower = self.add.sprite(400, 300, 'archer_tower').setScale(0.2);
        archerTower.setInteractive();
        self.input.setDraggable(archerTower);

        // 경로 설정
        const radius = 200;
        const centerX = 400;
        const centerY = 300;
        const pathImageCount = 32;

        for (let i = 0; i < pathImageCount; i++) {
            const angle = (2 * Math.PI / pathImageCount) * i;
            const x = centerX + radius * Math.cos(angle);
            const y = centerY + radius * Math.sin(angle);
            self.add.image(x, y, 'path').setScale(0.5).setOrigin(0.5, 0.5);
        }

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

        self.enemies = self.physics.add.group();

        // 적 추가
        let enemyCount = 0;
        self.time.addEvent({
            delay: 1000,
            callback: function() {
                if (enemyCount < 10) {
                    const enemy = self.physics.add.sprite(centerX, centerY - radius, 'enemy_walk_1').setScale(0.05);
                    enemy.health = 100;
                    self.enemies.add(enemy);
                    enemyCount++;
                    enemy.play('enemy_walk_anim');

                    const healthBar = self.add.graphics();
                    healthBar.fillStyle(0x00ff00, 1);
                    healthBar.fillRect(enemy.x - 20, enemy.y - 30, 40, 5);
                    enemy.healthBar = healthBar;

                    self.tweens.add({
                        targets: enemy,
                        angle: 360,
                        duration: 10000,
                        repeat: -1,
                        ease: 'Linear',
                        onUpdate: function(tween) {
                            const angle = Phaser.Math.DegToRad(tween.getValue());
                            enemy.x = centerX + radius * Math.cos(angle - Math.PI / 2);
                            enemy.y = centerY + radius * Math.sin(angle - Math.PI / 2);

                            enemy.healthBar.clear();
                            enemy.healthBar.fillStyle(0x00ff00, 1);
                            enemy.healthBar.fillRect(enemy.x - 20, enemy.y - 30, 40 * (enemy.health / 100), 5);
                        }
                    });
                }
            },
            callbackScope: self,
            loop: true
        });

        // 불꽃 공격 테스트
        self.time.addEvent({
            delay: 2000,
            callback: function() {
                self.enemies.getChildren().forEach(function(enemy) {
                    if (enemy.active) {
                        const flame = self.physics.add.sprite(archerTower.x, archerTower.y, 'flame_1').setScale(0.5);
                        if (!flame) {
                            console.error('Failed to create flame sprite');
                            return;
                        }
                        self.physics.moveToObject(flame, enemy, 300);
                        self.physics.add.overlap(flame, enemy, function(flame, enemy) {
                            flame.destroy(); // 불꽃 제거
                            hitEnemy(self, archerTower, enemy);
                        }, null, self);
                    }
                });
            },
            callbackScope: self,
            loop: true
        });
    }

    function update() {
        // 게임 업데이트 로직 (필요한 경우 추가)
    }

    function hitEnemy(scene, tower, enemy) {
        if (enemy.active) {
            enemy.health -= 20; // 적 체력 감소
            if (enemy.health <= 0) {
                enemy.healthBar.destroy(); // 체력바 제거
                enemy.destroy(); // 적 제거
            } else {
                // 체력바 업데이트
                enemy.healthBar.clear();
                enemy.healthBar.fillStyle(0x00ff00, 1);
                enemy.healthBar.fillRect(enemy.x - 20, enemy.y - 30, 40 * (enemy.health / 100), 5);
            }
        }
    }
};
