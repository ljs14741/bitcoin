window.onload = function() {
    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);

    const config = {
        type: Phaser.AUTO,
        width: isMobile ? window.innerWidth : 800,
        height: isMobile ? window.innerHeight : 600,
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
    const TOWER_RADIUS = 30; // 타워 설치 반경 (충돌 판정을 위한 값)
    const INSTALL_RECT = {
        left: 50,
        right: 750,
        top: 150,
        bottom: 550
    };

    let round = 1;
    let maxRounds = 3;
    let roundTime = 120000; // 2분 (120,000 밀리초)
    let enemySpawnInterval = 1000; // 1초 (1,000 밀리초)
    let roundTimerEvent;
    let enemySpawnEvent;
    let path; // 전역 변수로 선언
    let roundText;
    let timeText;
    let enemyCountText;
    let remainingTime = roundTime / 1000; // 초기 시간(초)
    let currentEnemyCount = 0;
    let currency = 100; // 초기 화폐 양
    let currencyText;
    let backgroundMusic; // 전역 변수로 선언
    let towerAttackSound; // 전역 변수로 선언
    let enemy1DieSound; // 전역 변수로 선언

    function preload() {
        this.load.audio('backgroundMusic', 'assets/audio/Main.mp3');
        this.load.audio('towerAttackSound', 'assets/audio/TowerAttack.mp3'); // 공격 소리 추가
        this.load.audio('enemy1DieSound', 'assets/audio/Enemy1Die.mp3'); // 적 죽음 소리 추가

        this.load.image('background', 'assets/defense/tiles/land_1.png');
        this.load.image('flameTower', 'assets/defense/towers/flameTowers_1.png');
        this.load.image('path', 'assets/defense/tiles/decor_6.png');
        this.load.image('flame_1', 'assets/defense/towers/flame_1.png');
        this.load.image('flame_2', 'assets/defense/towers/flame_2.png');
        this.load.image('flame_3', 'assets/defense/towers/flame_3.png');
        this.load.image('flame_4', 'assets/defense/towers/flame_4.png');
        this.load.image('flame_5', 'assets/defense/towers/flame_5.png');
        this.load.image('cancel', 'assets/defense/cancel.png'); // cancel 이미지 로드
        this.load.image('menu', 'assets/defense/menu.png'); // 햄버거 메뉴 이미지 로드
        this.load.image('meso', 'assets/defense/meso.png');
        for (let i = 1; i <= 24; i++) {
            this.load.image(`enemy_walk_${i}`, `assets/defense/enemies/orc_enemy_walk_${i}.png`);
        }
    }

    function create() {
        const self = this;

        // 배경 음악 재생
        backgroundMusic = self.sound.add('backgroundMusic');
        backgroundMusic.play({ loop: true });

        // 공격 소리와 적 죽음 소리 로드
        towerAttackSound = self.sound.add('towerAttackSound');
        enemy1DieSound = self.sound.add('enemy1DieSound');

        // BGM 끄기/켜기 버튼 이벤트 설정
        document.getElementById('bgmToggle').addEventListener('click', function() {
            if (backgroundMusic.isPlaying) {
                backgroundMusic.pause();
            } else {
                backgroundMusic.resume();
            }
        });

        // 배경 설정
        self.add.tileSprite(400, 300, 800, 600, 'background');

        // 경로 설정
        path = self.add.path(50, 150); // 전역 변수 path에 할당
        path.lineTo(750, 150);
        path.lineTo(750, 550);
        path.lineTo(50, 550);
        path.lineTo(50, 150);

        const graphics = self.add.graphics();
        graphics.lineStyle(3, 0xffffff, 1);
        path.draw(graphics);

        // 중앙 위치 계산
        const centerX = config.width / 2;
        const centerY = 50;

        // 화폐 이미지 및 텍스트 추가 (화면 중앙에 위치)
        const mesoIcon = self.add.sprite(centerX, centerY, 'meso').setScale(0.01);
        currencyText = self.add.text(centerX + 30, centerY - 16, `: ${currency}`, { fontSize: '32px', fill: '#FFF' });

        // 애니메이션 생성
        const walkFrames = [];
        for (let i = 1; i <= 24; i++) {
            walkFrames.push({key: `enemy_walk_${i}`});
        }

        self.anims.create({
            key: 'flame_anim',
            frames: [
                {key: 'flame_1'},
                {key: 'flame_2'},
                {key: 'flame_3'},
                {key: 'flame_4'},
                {key: 'flame_5'}
            ],
            frameRate: 10,
            repeat: 0
        });

        self.anims.create({
            key: 'enemy_walk_anim',
            frames: walkFrames,
            frameRate: 10,
            repeat: -1
        });

        // 적 그룹 정의
        self.enemies = self.physics.add.group();

        // 라운드, 시간, 적 수 표시 텍스트 생성
        roundText = self.add.text(16, 16, `Round: ${round}`, { fontSize: '32px', fill: '#FFF' });
        timeText = self.add.text(16, 48, `Time: ${remainingTime}`, { fontSize: '32px', fill: '#FFF' });
        enemyCountText = self.add.text(16, 80, `Enemies: ${currentEnemyCount}`, { fontSize: '32px', fill: '#FFF' });

        // 타워 설치 이벤트
        self.input.on('pointerdown', function (pointer) {
            if (selectedTowerType) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                // 직사각형 내부인지 확인
                const isInsideRectangle = (x >= INSTALL_RECT.left && x <= INSTALL_RECT.right && y >= INSTALL_RECT.top && y <= INSTALL_RECT.bottom);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                if (isInsideRectangle && !isOccupied) {
                    const tower = self.add.sprite(x, y, selectedTowerType).setScale(0.2);
                    tower.attackPower = 50; // 타워의 공격력 설정
                    tower.setInteractive();
                    towers.push(tower);
                    selectedTowerType = null;
                    cursorTower.destroy();
                    cursorTower = null;
                    if (cancelMarker) {
                        cancelMarker.destroy();
                        cancelMarker = null;
                    }

                    // 타워 클릭 이벤트 추가
                    tower.on('pointerdown', () => {
                        showTowerAttackPower(self, tower);
                    });
                }
            }
        });

        self.input.on('pointermove', function (pointer) {
            if (cursorTower) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                cursorTower.x = x;
                cursorTower.y = y;

                // 직사각형 내부인지 확인
                const isInsideRectangle = (x >= INSTALL_RECT.left && x <= INSTALL_RECT.right && y >= INSTALL_RECT.top && y <= INSTALL_RECT.bottom);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                if (!isInsideRectangle || isOccupied) {
                    if (!cancelMarker) {
                        cancelMarker = self.add.sprite(x, y, 'cancel').setScale(0.1); // cancel 이미지 크기 조정
                    }
                    cancelMarker.x = x;
                    cancelMarker.y = y;
                } else if (cancelMarker) {
                    cancelMarker.destroy();
                    cancelMarker = null;
                }
            }
        });

        // 햄버거 메뉴 설정
        const menuButton = self.add.sprite(750, 50, 'menu').setInteractive().setScale(0.5);

        menuButton.on('pointerdown', () => {
            console.log('햄버거 메뉴 클릭됨'); // 클릭 이벤트가 감지되었는지 확인

            const towerMenu = self.add.container(550, 100).setSize(200, 200).setInteractive();
            console.log('타워 메뉴 컨테이너 생성됨'); // 메뉴 컨테이너 생성 로그

            const background = self.add.rectangle(0, 0, 200, 200, 0x000000, 0.8).setOrigin(0);
            console.log('배경 생성됨'); // 배경 생성 로그

            const towerPurchaseText = self.add.text(10, 10, '타워 구매', { fontSize: '24px', fill: '#FFF' }).setInteractive();
            console.log('타워 구매 텍스트 생성됨'); // 텍스트 생성 로그

            const flameTowerText = self.add.text(10, 50, 'FlameTower', { fontSize: '24px', fill: '#FFF' }).setInteractive();
            flameTowerText.setVisible(false);
            console.log('FlameTower 텍스트 생성됨'); // 텍스트 생성 로그

            const startGameText = self.add.text(10, 90, '게임시작', { fontSize: '24px', fill: '#FFF' }).setInteractive();
            console.log('게임시작 텍스트 생성됨'); // 텍스트 생성 로그

            towerPurchaseText.on('pointerdown', () => {
                flameTowerText.setVisible(true);
                startGameText.setVisible(false); // "타워 구매"를 클릭했을 때 "게임 시작" 숨기기
            });

            flameTowerText.on('pointerdown', () => {
                selectedTowerType = 'flameTower';
                if (cursorTower) cursorTower.destroy();
                cursorTower = self.add.sprite(self.input.activePointer.worldX, self.input.activePointer.worldY, 'flameTower').setScale(0.2);
                cursorTower.setAlpha(0.5);
                towerMenu.destroy();
            });

            startGameText.on('pointerdown', () => {
                towerMenu.destroy();
                startRound(self);
            });

            towerMenu.add([background, towerPurchaseText, flameTowerText, startGameText]);
            self.add.existing(towerMenu);
            console.log('타워 메뉴 추가됨'); // 메뉴 추가 로그

            // 메뉴를 닫는 이벤트 등록
            const closeMenu = function(pointer) {
                if (!towerMenu.getBounds().contains(pointer.worldX, pointer.worldY)) {
                    towerMenu.destroy();
                    startGameText.setVisible(true); // 메뉴가 닫힐 때 "게임 시작" 다시 보이기
                    self.input.off('pointerdown', closeMenu); // 이벤트 핸들러 제거
                    console.log('타워 메뉴 닫힘'); // 메뉴 닫힘 로그
                }
            };

            // 메뉴가 생성된 후 약간의 지연 시간을 두고 메뉴 닫힘 이벤트를 등록
            setTimeout(() => {
                self.input.on('pointerdown', closeMenu);
            }, 100); // 100 밀리초 지연
        });

        // 타워 공격 로직
        self.time.addEvent({
            delay: 500, // 더 자주 공격
            callback: function() {
                towers.forEach(function(tower) {
                    let closestEnemy = null;
                    let minDistance = TOWER_ATTACK_RANGE;

                    self.enemies.getChildren().forEach(function(enemy) {
                        const distance = Phaser.Math.Distance.Between(tower.x, tower.y, enemy.x, enemy.y);
                        if (enemy.active && distance <= minDistance) {
                            closestEnemy = enemy;
                            minDistance = distance;
                        }
                    });

                    if (closestEnemy) {
                        const flame = self.physics.add.sprite(tower.x, tower.y, 'flame_1').setScale(0.5);
                        self.physics.moveToObject(flame, closestEnemy, FLAME_SPEED);

                        flame.play('flame_anim');
                        towerAttackSound.play(); // 공격 소리 재생

                        self.physics.add.overlap(flame, closestEnemy, function(flame, enemy) {
                            if (enemy.active) {
                                console.log('Hit detected');
                                hitEnemy(self, flame, enemy);
                            }
                        }, null, self);
                    }
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

        // 남은 시간 업데이트
        if (roundTimerEvent) {
            const elapsed = roundTimerEvent.getElapsedSeconds();
            remainingTime = Math.max(0, (roundTime / 1000) - elapsed).toFixed(0);
            timeText.setText(`Time: ${remainingTime}`);
        }

        // 적 수 업데이트
        currentEnemyCount = this.enemies.countActive(true);
        enemyCountText.setText(`Enemies: ${currentEnemyCount}`);
    }

    function createTowerSelectionUI(scene) {
        // 기존 코드에서는 사용되지 않음
    }

    function updateHealthBar(enemy) {
        const x = enemy.x - 20;
        const y = enemy.y - 30;
        enemy.healthBar.clear();
        enemy.healthBar.fillStyle(0x00ff00, 1);
        enemy.healthBar.fillRect(x, y, 40 * (enemy.health / 100), 5);
    }

    function hitEnemy(scene, flame, enemy) {
        console.log('HitEnemy called');
        flame.destroy(); // 불꽃 제거
        // 적 위치에 불꽃 폭발 효과 (애니메이션이 있는 경우)
        const explosion = scene.add.sprite(enemy.x, enemy.y, 'flame_1').setScale(0.5);
        explosion.play('flame_anim');
        explosion.on('animationcomplete', () => {
            explosion.destroy();
        });

        if (enemy.active) {
            enemy.health -= 20; // 적 체력 감소
            console.log('Enemy health:', enemy.health);
            if (enemy.health <= 0) {
                enemy.healthBar.destroy(); // 체력바 제거
                enemy.destroy(); // 적 제거
                enemy1DieSound.play(); // 적 죽음 소리 재생
                currency += 1; // 화폐 1원 증가
                currencyText.setText(`: ${currency}`); // 텍스트 업데이트
            } else {
                // 체력바 업데이트
                updateHealthBar(enemy);
            }
        }
    }

    function startRound(scene) {
        let enemyCount = 0;

        // 첫 번째 적을 즉시 생성
        spawnEnemy(scene, path);

        enemySpawnEvent = scene.time.addEvent({
            delay: enemySpawnInterval,
            callback: function () {
                spawnEnemy(scene, path);
            },
            callbackScope: scene,
            loop: true
        });

        roundTimerEvent = scene.time.delayedCall(roundTime, () => {
            endRound(scene);
        }, [], scene);
    }

    function spawnEnemy(scene, path) {
        const enemy = scene.add.follower(path, 50, 150, 'enemy_walk_1').setScale(0.05);
        enemy.health = 100;
        scene.physics.add.existing(enemy);  // 적에 물리 속성 추가
        enemy.body.setCircle(enemy.displayWidth / 2);  // 원형 충돌 박스 설정
        scene.enemies.add(enemy);
        enemy.startFollow({
            duration: 19750, // 이동속도 20000 -> 20초
            repeat: -1,
            rotateToPath: true
        });

        enemy.play('enemy_walk_anim');

        // 체력바 생성
        const healthBar = scene.add.graphics();
        enemy.healthBar = healthBar;
        updateHealthBar(enemy);
    }

    function endRound(scene) {
        enemySpawnEvent.remove(false);
        if (round < maxRounds) {
            round++;
            remainingTime = roundTime / 1000; // 새로운 라운드를 위해 시간 초기화
            roundText.setText(`Round: ${round}`);
            console.log(`Round ${round} 시작!`);
            startRound(scene);
        } else {
            console.log('게임 종료!');
        }
    }

    function showTowerAttackPower(scene, tower) {
        const attackPowerText = scene.add.text(tower.x, tower.y - 50, `공격력: ${tower.attackPower}`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        });

        // 일정 시간 후 텍스트 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                attackPowerText.destroy();
            }
        });
    }
}
