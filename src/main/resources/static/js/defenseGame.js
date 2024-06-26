window.onload = function() {
    const config = {
        type: Phaser.AUTO,
        width: 800,
        height: 600,
        scale: {
            mode: Phaser.Scale.FIT,
            autoCenter: Phaser.Scale.CENTER_BOTH,
            width: 800,
            height: 600,
        },
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
        },
        audio: {
            disableWebAudio: false // Web Audio API를 사용하도록 설정
        }
    };

    const game = new Phaser.Game(config);
    let selectedTowerGrade = null;
    let towers = [];
    let cursorTower = null;
    let cancelMarker = null;
    const FLAME_SPEED = 500; // 불꽃 속도 설정
    const TOWER_RADIUS = 30; // 타워 설치 반경 (충돌 판정을 위한 값)
    const INSTALL_RECT = {
        left: 50,
        right: 750,
        top: 150,
        bottom: 550
    };

    let round = 1;
    let maxRounds = 9;
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
    let currency = 30; // 초기 화폐 양
    let currencyText;
    let backgroundMusic; // 전역 변수로 선언
    let enemy1AttackSound; // 전역 변수로 선언
    let enemy1DieSound; // 전역 변수로 선언
    let boss1DieSound;
    let enemyCount = 0;
    let bossSpawned = false;
    const MAX_ENEMIES = 150;
    let towerUpgradeLevel = 0;
    let baseAttackPower = {
        '일반': 10,
        '레어': 15,
        '영웅': 20,
        '유물': 30,
        '전설': 40
    };
    let towerAttackRange = {
        '일반': 130,
        '레어': 130,
        '영웅': 150,
        '유물': 170,
        '전설': 200
    };
    let isDoubleSpeed = false;

    function preload() {
        this.load.audio('backgroundMusic', 'assets/audio/backgroundMusic.mp3');
        this.load.audio('enemy1AttackSound', 'assets/audio/enemy1Attack.mp3'); // 적1 맞는 소리
        this.load.audio('enemy1DieSound', 'assets/audio/enemy1Die.mp3'); // 적 죽음 소리 추가
        this.load.audio('boss1DieSound', 'assets/audio/boss1Die.mp3');

        this.load.image('background', 'assets/defense/tiles/land_1.png');
        this.load.image('flameTower', 'assets/defense/towers/ccc.png');
        this.load.image('path', 'assets/defense/tiles/decor_6.png');
        this.load.image('soil', 'assets/defense/tiles/soil.png');
        this.load.image('flame_1', 'assets/defense/towers/flame_1.png');
        this.load.image('flame_2', 'assets/defense/towers/flame_2.png');
        this.load.image('cancel', 'assets/defense/cancel.png'); // cancel 이미지 로드
        this.load.image('menu', 'assets/defense/menu.png'); // 햄버거 메뉴 이미지 로드
        this.load.image('meso', 'assets/defense/meso.png');
        this.load.image('boss', 'assets/defense/boss/boss.png');
        for (let i = 1; i <= 24; i++) {
            this.load.image(`enemy_walk_${i}`, `assets/defense/enemies/orc_enemy_walk_${i}.png`);
        }
    }

    function create() {
        const self = this;

        // 오디오 시스템 초기화
        this.sound.context.resume();

        // 배경 음악 재생
        backgroundMusic = this.sound.add('backgroundMusic');
        backgroundMusic.play({ loop: true });

        // 적 맞는 소리와 적 죽음 소리 로드
        enemy1AttackSound = this.sound.add('enemy1AttackSound');
        enemy1DieSound = this.sound.add('enemy1DieSound');
        boss1DieSound = this.sound.add('boss1DieSound');

        // BGM 끄기/켜기 버튼 이벤트 설정
        document.getElementById('bgmToggle').addEventListener('click', () => {
            if (backgroundMusic.isPlaying) {
                backgroundMusic.pause();
            } else {
                this.sound.context.resume();
                backgroundMusic.play({ loop: true });
            }
        });

        // 볼륨 조절 슬라이더 이벤트 설정
        document.getElementById('volumeControl').addEventListener('input', function() {
            const volume = this.value / 100;
            backgroundMusic.setVolume(volume);
        });

        //효과음 조절
        document.getElementById('sfxVolumeControl').addEventListener('input', function() {
            const volume = this.value / 100;
            enemy1AttackSound.setVolume(volume);
            enemy1DieSound.setVolume(volume);
        });

        // 배경 설정
        this.add.tileSprite(400, 300, 800, 600, 'background');


        const soilPositions = [];
        const soilSprites = [];

        // 직사각형 내부에 soil 이미지를 그리드 형식으로 배치 (테두리 제외)
        for (let x = INSTALL_RECT.left + 50; x < INSTALL_RECT.right; x += 50) {
            for (let y = INSTALL_RECT.top + 50; y < INSTALL_RECT.bottom; y += 50) {
                const soilSprite = this.add.image(x, y, 'soil').setScale(0.05);
                soilPositions.push({ x: x, y: y, sprite: soilSprite });
                soilSprites.push(soilSprite);
            }
        }

        // 경로 설정
        path = this.add.path(50, 150); // 전역 변수 path에 할당
        path.lineTo(750, 150);
        path.lineTo(750, 550);
        path.lineTo(50, 550);
        path.lineTo(50, 150);

        const graphics = this.add.graphics();
        graphics.lineStyle(3, 0xffffff, 1);
        path.draw(graphics);

        // 중앙 위치 계산
        const centerX = config.width / 2;
        const centerY = 50;

        // 화폐 이미지 및 텍스트 추가 (화면 중앙에 위치)
        const mesoIcon = this.add.sprite(centerX, centerY, 'meso').setScale(0.01);
        currencyText = this.add.text(centerX + 30, centerY - 16, `: ${currency}`, { fontSize: '32px', fill: '#FFF' });

        // 애니메이션 생성
        const walkFrames = [];
        for (let i = 1; i <= 24; i++) {
            walkFrames.push({key: `enemy_walk_${i}`});
        }

        this.anims.create({
            key: 'flame_anim',
            frames: [
                {key: 'flame_1'},
                {key: 'flame_2'}
            ],
            frameRate: 10,
            repeat: 0
        });

        this.anims.create({
            key: 'enemy_walk_anim',
            frames: walkFrames,
            frameRate: 10,
            repeat: -1
        });

        // 적 그룹 정의
        this.enemies = this.physics.add.group();
        this.flames = this.physics.add.group(); // 불꽃 그룹 추가

        // 라운드, 시간, 적 수 표시 텍스트 생성
        roundText = this.add.text(16, 16, `Round: ${round}`, { fontSize: '32px', fill: '#FFF' });
        timeText = this.add.text(16, 48, `Time: ${remainingTime}`, { fontSize: '32px', fill: '#FFF' });
        enemyCountText = this.add.text(16, 80, `Enemies: ${currentEnemyCount}`, { fontSize: '32px', fill: '#FFF' });

        // 타워 설치 이벤트
        this.input.on('pointerdown', function(pointer) {
            if (selectedTowerGrade) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                // 직사각형 내부에 있는지 확인하고 테두리는 제외
                const isInsideRectangle = (x > INSTALL_RECT.left && x < INSTALL_RECT.right && y > INSTALL_RECT.top && y < INSTALL_RECT.bottom);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                const validSoilIndex = soilPositions.findIndex(pos => Phaser.Math.Distance.Between(pos.x, pos.y, x, y) <= TOWER_RADIUS);

                if (isInsideRectangle && !isOccupied && validSoilIndex !== -1) {
                    const tower = self.add.sprite(x, y, 'flameTower').setScale(0.05);
                    tower.grade = selectedTowerGrade; // 타워의 등급 설정
                    tower.attackPower = getTowerAttackPower(tower.grade); // 타워의 공격력 설정
                    tower.range = towerAttackRange[tower.grade]; // 타워의 사거리 설정
                    tower.setInteractive();
                    towers.push(tower);
                    showTowerGradeNotification(self, selectedTowerGrade);  // 타워 등급 알림 표시
                    selectedTowerGrade = null;
                    cursorTower.destroy();
                    cursorTower = null;
                    if (cancelMarker) {
                        cancelMarker.destroy();
                        cancelMarker = null;
                    }

                    // 타워 클릭 이벤트 추가
                    tower.on('pointerdown', () => {
                        showTowerDetailsAndUpgradeButton(self, tower);
                    });
                }
            }
        });


        this.input.on('pointermove', function(pointer) {
            if (cursorTower) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                cursorTower.x = x;
                cursorTower.y = y;

                // 직사각형 내부인지 확인하고 테두리는 제외
                const isInsideRectangle = (x > INSTALL_RECT.left && x < INSTALL_RECT.right && y > INSTALL_RECT.top && y < INSTALL_RECT.bottom);
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
        const menuButton = this.add.sprite(750, 50, 'menu').setInteractive().setScale(1);

        menuButton.on('pointerdown', () => {
            const towerMenu = this.add.container(550, 100).setSize(250, 200).setInteractive();
            const background = this.add.rectangle(0, 0, 250, 200, 0x000000, 0.7).setOrigin(0, 0);

            const style = { fontSize: '20px', fill: '#FFF', fontFamily: 'Arial', align: 'left' };

            const gameDescriptionText = this.add.text(10, 10, '게임설명', style).setInteractive();
            const towerPurchaseText = this.add.text(10, 50, '랜덤 타워 구매 (15원)', style).setInteractive();
            const toggleSpeedText = this.add.text(10, 90, '게임 2배속 On/Off', style).setInteractive();
            const startGameText = this.add.text(10, 130, '게임시작', style).setInteractive();

            gameDescriptionText.on('pointerdown', () => {
                // 게임 설명 표시
                const descriptionBackground = this.add.rectangle(400, 300, 500, 300, 0x000000, 0.7).setOrigin(0.5); // 배경을 더 크게 설정
                const descriptionText = this.add.text(400, 300, '김치 랜덤 디펜스 \n 플레이어는 메소를 사용하여 타워를 구매하고 모든 적을 섬멸해야합니다.\n지금은 10단계가 최종보스 \n\n타워 등급과 출현 확률:\n- 일반: 51%\n- 레어: 33%\n- 영웅: 10%\n- 유물: 5%\n- 전설: 1%\n\n각 타워는 등급에 따라 다른 공격력과 사거리를 가집니다. 전략적으로 타워를 배치하여 적을 물리치세요.\n\n*게임 배속은 존재하는 적에게는 적용되지 않습니다.\n5초뒤 사라짐', {
                    fontSize: '16px',
                    fill: '#FFF',
                    fontFamily: 'Arial',
                    align: 'center',
                    wordWrap: { width: 480, useAdvancedWrap: true }
                }).setOrigin(0.5);

                // 설명 텍스트가 더 앞에 나오도록 설정
                descriptionBackground.setDepth(11);
                descriptionText.setDepth(12);

                // 일정 시간 후 설명 제거
                this.time.addEvent({
                    delay: 10000, // 10초 동안 설명 표시
                    callback: () => {
                        descriptionBackground.destroy();
                        descriptionText.destroy();
                    }
                });
            });

            towerPurchaseText.on('pointerdown', () => {
                if (currency >= 15) {
                    selectedTowerGrade = getRandomTowerGrade();
                    if (cursorTower) cursorTower.destroy();
                    cursorTower = self.add.sprite(self.input.activePointer.worldX, self.input.activePointer.worldY, 'flameTower').setScale(0.05);
                    cursorTower.setAlpha(0.5);
                    towerMenu.destroy();
                    currency -= 15;
                    currencyText.setText(`: ${currency}`);

                } else {
                    const warningText = self.add.text(400, 300, '화폐가 부족합니다!', { fontSize: '32px', fill: '#FFF', backgroundColor: '#000' }).setOrigin(0.5);
                    self.time.addEvent({
                        delay: 2000,
                        callback: () => {
                            warningText.destroy();
                        }
                    });
                }
            });

            toggleSpeedText.on('pointerdown', () => {
                isDoubleSpeed = !isDoubleSpeed;
                self.time.timeScale = isDoubleSpeed ? 2 : 1;

                self.enemies.getChildren().forEach(enemy => {
                    const baseDuration = enemy.texture.key === 'boss' ? 30000 : 19750;
                    const newDuration = baseDuration / (isDoubleSpeed ? 2 : 1);

                    if (enemy.pathFollower) {
                        enemy.pathFollower.tween.stop();
                        enemy.startFollow({
                            duration: newDuration,
                            repeat: -1,
                            rotateToPath: true
                        });
                    }
                });
            });

            startGameText.on('pointerdown', () => {
                towerMenu.destroy();
                startRound(self);
            });

            towerMenu.add([background, gameDescriptionText, towerPurchaseText, toggleSpeedText, startGameText]);
            self.add.existing(towerMenu);

            towerMenu.setDepth(10);

            const closeMenu = function(pointer) {
                if (!towerMenu.getBounds().contains(pointer.worldX, pointer.worldY)) {
                    towerMenu.destroy();
                    self.input.off('pointerdown', closeMenu);
                }
            };

            setTimeout(() => {
                self.input.on('pointerdown', closeMenu);
            }, 100);
        });


        // 타워 공격 로직
        this.time.addEvent({
            delay: 500, // 공격 주기
            callback: () => {
                towers.forEach(tower => {
                    let closestEnemy = null;
                    let minDistance = tower.range; // 각 타워의 사거리 사용

                    this.enemies.getChildren().forEach(enemy => {
                        const distance = Phaser.Math.Distance.Between(tower.x, tower.y, enemy.x, enemy.y);
                        if (enemy.active && distance <= minDistance) {
                            closestEnemy = enemy;
                            minDistance = distance;
                        }
                    });

                    if (closestEnemy) {
                        createFlame(this, tower, closestEnemy);
                    }
                });
            },
            callbackScope: this,
            loop: true
        });

        this.physics.add.overlap(this.flames, this.enemies, (flame, enemy) => {
            hitEnemy(this, flame, enemy);
        }, null, this);
    }

    function update() {

        // 불꽃 업데이트
        updateFlames(this);

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

    function createFlame(scene, tower, target) {
        const flame = scene.physics.add.sprite(tower.x, tower.y, 'flame_1').setScale(0.02);
        flame.attackPower = tower.attackPower;
        flame.target = target; // 목표 적 설정
        scene.flames.add(flame);
        flame.play('flame_anim');
    }

    function updateFlames(scene) {
        scene.flames.getChildren().forEach(flame => {
            if (flame.target && flame.target.active) {
                scene.physics.moveToObject(flame, flame.target, FLAME_SPEED);

                // 적과의 거리 계산
                const distance = Phaser.Math.Distance.Between(flame.x, flame.y, flame.target.x, flame.target.y);
                if (distance < 10) { // 거리가 작으면 충돌로 간주
                    hitEnemy(scene, flame, flame.target);
                }
            } else {
                flame.destroy();
            }
        });
    }

    function hitEnemy(scene, flame, enemy) {
        if (!flame.active || !enemy.active) return;

        flame.destroy(); // 불꽃 제거
        enemy1AttackSound.play();

        // 적 위치에 불꽃 폭발 효과 (애니메이션이 있는 경우)
        const explosion = scene.add.sprite(enemy.x, enemy.y, 'flame_1').setScale(0.01); // 폭발 이미지 크기 조정
        explosion.play('flame_anim');
        explosion.on('animationcomplete', () => {
            explosion.destroy();
        });

        enemy.health -= flame.attackPower; // 타워의 공격력만큼 적 체력 감소

        if (enemy.health <= 0) {
            if (round === 10 && enemy.texture.key === 'boss') {
                boss1DieSound.play();
                displayGameClear(scene);
            }
            enemy.destroy(); // 적 제거
            enemy1DieSound.play(); // 적 죽음 소리 재생
            currency += 1; // 화폐 1원 증가
            currencyText.setText(`: ${currency}`); // 텍스트 업데이트

            // 적 유닛 수 감소
            currentEnemyCount--;

            // 적 수 텍스트 업데이트
            enemyCountText.setText(`Enemies: ${currentEnemyCount}`);
        }
    }

    function displayGameClear(scene) {

        const gameClearContainer = scene.add.container(400, 300).setSize(600, 300);
        const gameClearBackground = scene.add.rectangle(0, 0, 500, 200, 0x000000, 0.8).setOrigin(0.5);

        const gameClearText = scene.add.text(0, -50, '게임 클리어!', {
            fontSize: '48px',
            fill: '#FFF',
            fontFamily: 'Arial',
        }).setOrigin(0.5);

        const restartButton = scene.add.text(0, 50, '게임 다시하기', {
            fontSize: '24px',
            fill: '#FFF',
            backgroundColor: '#00ff00',
            padding: { left: 20, right: 20, top: 10, bottom: 10 },
            borderRadius: 5
        }).setOrigin(0.5).setInteractive();

        restartButton.on('pointerdown', () => {
            // 페이지를 다시 로드하여 게임을 초기화
            location.reload();
        });

        gameClearContainer.add([gameClearBackground, gameClearText, restartButton]);
        gameClearContainer.setDepth(100);

        // 모든 타이머 이벤트 제거
        scene.time.removeAllEvents();

        // 점수 저장
        saveGameData(scene, 10); // 10라운드 클리어 시점 저장
    }

    function showTowerGradeNotification(scene, grade) {
        const notificationContainer = scene.add.container(scene.cameras.main.centerX, scene.cameras.main.centerY).setSize(300, 100);
        const notificationBackground = scene.add.rectangle(0, 0, 300, 100, 0x000000, 0.8).setOrigin(0.5);

        const notificationText = scene.add.text(0, 0, `${grade} 등급이 생성되었습니다.`, {
            fontSize: '24px',
            fill: '#FFF',
            fontFamily: 'Arial',
            align: 'center'
        }).setOrigin(0.5);

        notificationContainer.add([notificationBackground, notificationText]);
        notificationContainer.setDepth(100);

        // 2초 후 알림 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                notificationContainer.destroy();
            }
        });
    }



    function startRound(scene) {

        if (round < 10) {
            // 첫 번째 적을 즉시 생성
            spawnEnemy(scene, path);

            enemySpawnEvent = scene.time.addEvent({
                delay: enemySpawnInterval,
                callback: () => {
                    spawnEnemy(scene, path);
                },
                callbackScope: scene,
                loop: true
            });

            roundTimerEvent = scene.time.delayedCall(roundTime, () => {
                endRound(scene);
            }, [], scene);
        } else if (round === 10) {
            // 10라운드 보스 한 마리만 생성
            spawnEnemy(scene, path);

            roundTimerEvent = scene.time.delayedCall(roundTime, () => {
                endRound(scene);
            }, [], scene);
        }
    }

    function spawnEnemy(scene, path) {
        // 적 유닛 수가 최대치를 넘으면 게임 종료
        if (currentEnemyCount >= MAX_ENEMIES) {
            endGame(scene);
            return;
        }

        let enemyHealth;
        let enemy;

        if (round < 10) {
            // 라운드에 따른 체력 설정
            if (round === 1) {
                enemyHealth = 70;
            } else if (round === 2) {
                enemyHealth = 600;
            } else if (round === 3) {
                enemyHealth = 1500;
            } else if (round === 4) {
                enemyHealth = 3300;
            } else if (round === 5) {
                enemyHealth = 5500;
            } else if (round === 6) {
                enemyHealth = 7500;
            } else if (round === 7) {
                enemyHealth = 9000;
            } else if (round === 8) {
                enemyHealth = 11000;
            } else if (round === 9) {
                enemyHealth = 13000;
            } else {
                enemyHealth = 100; // 임시값
            }

            enemy = scene.add.follower(path, 50, 150, 'enemy_walk_1').setScale(0.05);
        } else if (!bossSpawned) {
            // 10라운드 보스 생성
            enemyHealth = 300000;
            enemy = scene.add.follower(path, 50, 150, 'boss').setScale(0.1);
            bossSpawned = true; // 보스가 생성되었음을 기록
        } else {
            return; // 이미 보스가 생성된 경우 함수를 종료
        }

        enemy.health = enemyHealth; // 계산된 체력으로 설정
        enemy.maxHealth = enemyHealth; // 최대 체력도 동일하게 설정
        scene.physics.add.existing(enemy);  // 적에 물리 속성 추가
        enemy.body.setCircle(enemy.displayWidth / 2);  // 원형 충돌 박스 설정
        scene.enemies.add(enemy);

        const baseDuration = round < 10 ? 19750 : 30000;
        const newDuration = baseDuration / (isDoubleSpeed ? 2 : 1);

        enemy.startFollow({
            duration: newDuration,
            repeat: -1,
            rotateToPath: true
        });

        if (round < 10) {
            enemy.play('enemy_walk_anim');
        }

        // 적 클릭 이벤트 추가
        enemy.setInteractive();
        enemy.on('pointerdown', () => {
            showEnemyHealth(scene, enemy);
        });

        // 적 유닛 수 증가
        currentEnemyCount++;

        // 적 수 텍스트 업데이트
        enemyCountText.setText(`Enemies: ${currentEnemyCount}`);
    }

    function showEnemyHealth(scene, enemy) {
        const healthText = scene.add.text(enemy.x, enemy.y - 50, `체력: ${enemy.health}/${enemy.maxHealth}`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        }).setOrigin(0.5);

        // 일정 시간 후 텍스트 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                healthText.destroy();
            }
        });
    }

    function endRound(scene) {
        if (round === 10) {
            // 보스가 살아 있으면 게임 오버
            const boss = scene.enemies.getChildren().find(enemy => enemy.texture.key === 'boss' && enemy.active);
            if (boss) {
                endGame(scene);
                return;
            }
        }

        if (enemySpawnEvent) {
            enemySpawnEvent.remove(false);
        }

        if (round < 10) {
            round++;
            remainingTime = roundTime / 1000; // 새로운 라운드를 위해 시간 초기화
            roundText.setText(`Round: ${round}`);
            console.log(`Round ${round} 시작!`);
            startRound(scene);
        } else if (round === 10) {
            console.log('보스 라운드 시작!');
            round++;
            remainingTime = roundTime / 1000;
            roundText.setText(`Round: ${round}`);
            startRound(scene);
        } else {
            console.log('게임 종료!');
        }
    }

    function getRandomTowerGrade() {
        const randomValue = Math.random() * 100;
        if (randomValue < 1) {
            return '전설';
        } else if (randomValue < 6) {
            return '유물';
        } else if (randomValue < 16) {
            return '영웅';
        } else if (randomValue < 49) {
            return '레어';
        } else {
            return '일반';
        }
    }

    function getTowerAttackPower(grade) {
        return baseAttackPower[grade] + (towerUpgradeLevel * baseAttackPower[grade]);
    }

    function endGame(scene) {

        const gameOverContainer = scene.add.container(400, 300).setSize(600, 300);
        const gameOverBackground = scene.add.rectangle(0, 0, 500, 200, 0x000000, 0.8).setOrigin(0.5);

        const gameOverText = scene.add.text(0, -50, '게임 종료!', {
            fontSize: '48px',
            fill: '#FFF',
            fontFamily: 'Arial',
        }).setOrigin(0.5);

        const restartButton = scene.add.text(0, 50, '게임 다시하기', {
            fontSize: '24px',
            fill: '#FFF',
            backgroundColor: '#ff0000',
            padding: { left: 20, right: 20, top: 10, bottom: 10 },
            borderRadius: 5
        }).setOrigin(0.5).setInteractive();

        restartButton.on('pointerdown', () => {
            // 페이지를 다시 로드하여 게임을 초기화
            location.reload();
        });

        gameOverContainer.add([gameOverBackground, gameOverText, restartButton]);
        gameOverContainer.setDepth(100);

        // 모든 타이머 이벤트 제거
        scene.time.removeAllEvents();

        // 적 유닛의 추가 생성 중단
        if (enemySpawnEvent) {
            enemySpawnEvent.remove(false);
        }

        // 라운드 타이머 이벤트 중단
        if (roundTimerEvent) {
            roundTimerEvent.remove(false);
        }

        saveGameData(scene, round - 1);

    }

    function saveGameData(scene, score) {
        const gameData = {
            gameName: "김치 타워 디펜스",
            kakaoId: 9999, // 여기에 적절한 kakaoId를 설정하세요
            score: score // 전달된 점수 저장
        };

        fetch('/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(gameData)
        }).then(response => {
            if (response.ok) {
                return response.text().then(text => {
                    if (text) {
                        return JSON.parse(text);
                    }
                    return {}; // 비어 있는 응답 처리
                });
            }
            throw new Error('Network response was not ok ' + response.statusText);
        }).then(data => {
            console.log('Success:', data);
            updateGameScores(data);
        }).catch(error => {
            console.error('Error:', error);
        });
    }

    function updateGameScores(games) {
        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = '';

        games.forEach((game, index) => {
            const row = document.createElement('tr');

            const rankCell = document.createElement('td');
            rankCell.textContent = index + 1;
            row.appendChild(rankCell);

            const nameCell = document.createElement('td');
            nameCell.textContent = game.gameName;
            row.appendChild(nameCell);

            const nicknameCell = document.createElement('td');
            nicknameCell.textContent = game.changeNickname;
            row.appendChild(nicknameCell);

            const scoreCell = document.createElement('td');
            scoreCell.textContent = game.score;
            row.appendChild(scoreCell);

            const dateCell = document.createElement('td');
            const createdDate = new Date(game.createdDate);
            const formattedDate = `${createdDate.getFullYear()}년 ${createdDate.getMonth() + 1}월 ${createdDate.getDate()}일`;
            dateCell.textContent = formattedDate;
            row.appendChild(dateCell);

            tbody.appendChild(row);
        });
    }


    function showTowerDetailsAndUpgradeButton(scene, tower) {
        const basePower = baseAttackPower[tower.grade];
        const style = { fontSize: '16px', fill: '#FFF', fontFamily: 'Arial', align: 'left' };

        // 배경 컨테이너 설정
        const detailsContainer = scene.add.container(tower.x, tower.y - 120);
        const background = scene.add.rectangle(0, 0, 250, 150, 0x000000, 0.7).setOrigin(0, 0);

        const detailsText = scene.add.text(10, 10, `등급: ${tower.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})\n사거리: ${tower.range}`, style);

        const upgradeCost = 20 + (towerUpgradeLevel * 2);
        const upgradeText = scene.add.text(10, 70, `${towerUpgradeLevel + 1}단계업그레이드(${upgradeCost}원)`, style).setInteractive();

        const sellText = scene.add.text(10, 100, `판매하기(${getSellPrice(tower.grade)}원)`, style).setInteractive();

        const moveText = scene.add.text(10, 130, `이동`, style).setInteractive();

        // 사거리 범위 표시
        const rangeCircle = scene.add.graphics();
        rangeCircle.lineStyle(2, 0xff0000, 1);
        rangeCircle.strokeCircle(tower.x, tower.y, tower.range);

        // 업그레이드 버튼 클릭 이벤트 추가
        upgradeText.on('pointerdown', () => {
            upgradeTower(scene, tower, detailsText, upgradeText, sellText, moveText, rangeCircle);
        });

        // 판매 버튼 클릭 이벤트 추가
        sellText.on('pointerdown', () => {
            sellTower(scene, tower, detailsText, upgradeText, sellText, moveText, rangeCircle);
        });

        // 이동 버튼 클릭 이벤트 추가
        moveText.on('pointerdown', () => {
            moveTower(scene, tower, detailsContainer, rangeCircle);
        });

        // 배경과 텍스트 추가
        detailsContainer.add([background, detailsText, upgradeText, sellText, moveText]);
        scene.add.existing(detailsContainer);

        // 일정 시간 후 텍스트와 사거리 범위 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                if (detailsContainer) detailsContainer.destroy();
                if (rangeCircle) rangeCircle.destroy();
            }
        });
    }

    function moveTower(scene, tower, detailsContainer, rangeCircle) {
        // UI 제거
        if (detailsContainer) detailsContainer.destroy();
        if (rangeCircle) rangeCircle.destroy();

        // 타워 이동 설정
        const originalX = tower.x;
        const originalY = tower.y;
        let cursorTower = tower;
        cursorTower.setAlpha(0.5);
        cursorTower.disableInteractive(); // 이동 중에는 클릭 비활성화

        const cancelMarker = scene.add.sprite(cursorTower.x, cursorTower.y, 'cancel').setScale(0.1);

        const pointerMoveHandler = function(pointer) {
            const x = pointer.worldX;
            const y = pointer.worldY;
            cursorTower.x = x;
            cursorTower.y = y;

            const isInsideRectangle = (x >= INSTALL_RECT.left && x <= INSTALL_RECT.right && y >= INSTALL_RECT.top && y <= INSTALL_RECT.bottom);
            const isOccupied = towers.some(t => t !== cursorTower && Phaser.Math.Distance.Between(t.x, t.y, x, y) <= TOWER_RADIUS);

            if (!isInsideRectangle || isOccupied) {
                cancelMarker.x = x;
                cancelMarker.y = y;
            } else {
                cancelMarker.x = -100; // 화면 밖으로 이동
            }
        };

        scene.input.on('pointermove', pointerMoveHandler);

        scene.input.once('pointerdown', function(pointer) {
            const x = pointer.worldX;
            const y = pointer.worldY;
            const isInsideRectangle = (x >= INSTALL_RECT.left && x <= INSTALL_RECT.right && y >= INSTALL_RECT.top && y <= INSTALL_RECT.bottom);
            const isOccupied = towers.some(t => t !== cursorTower && Phaser.Math.Distance.Between(t.x, t.y, x, y) <= TOWER_RADIUS);

            if (isInsideRectangle && !isOccupied) {
                cursorTower.setAlpha(1);
                cursorTower.x = x;
                cursorTower.y = y;
            } else {
                cursorTower.x = originalX;
                cursorTower.y = originalY;
                cursorTower.setAlpha(1);
            }

            cursorTower.disableInteractive();
            cancelMarker.destroy();
            scene.input.off('pointermove', pointerMoveHandler);

            // 다시 클릭 가능하도록 인터랙티브 설정
            scene.time.delayedCall(100, () => {
                cursorTower.setInteractive();
                cursorTower.on('pointerdown', () => {
                    showTowerDetailsAndUpgradeButton(scene, cursorTower);
                });
            });
        });
    }

    function upgradeTower(scene, tower, detailsText, upgradeText, sellText, moveText, rangeCircle) {
        const baseUpgradeCost = 20;
        const upgradeCost = baseUpgradeCost + (towerUpgradeLevel * 2);

        if (currency >= upgradeCost) {
            currency -= upgradeCost;
            currencyText.setText(`: ${currency}`);
            towerUpgradeLevel += 1;

            towers.forEach(t => {
                t.attackPower = getTowerAttackPower(t.grade);
            });

            towers.forEach(t => {
                if (t.upgradeDetailsText && t.upgradeText) {
                    const basePower = baseAttackPower[t.grade];
                    t.upgradeDetailsText.setText(`등급: ${t.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})\n사거리: ${t.range}`);
                    t.upgradeText.setText(`${towerUpgradeLevel + 1}단계업그레이드(${baseUpgradeCost + (towerUpgradeLevel * 2)}원)`);
                }
            });

            const basePower = baseAttackPower[tower.grade];
            detailsText.setText(`등급: ${tower.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})\n사거리: ${tower.range}`);
            upgradeText.setText(`${towerUpgradeLevel + 1}단계업그레이드(${baseUpgradeCost + (towerUpgradeLevel * 2)}원)`);

            rangeCircle.clear();
            rangeCircle.lineStyle(2, 0xff0000, 1);
            rangeCircle.strokeCircle(tower.x, tower.y, tower.range);
        } else {
            const warningText = scene.add.text(400, 300, '화폐가 부족합니다!', { fontSize: '32px', fill: '#FFF', backgroundColor: '#000' }).setOrigin(0.5);
            scene.time.addEvent({
                delay: 2000,
                callback: () => {
                    warningText.destroy();
                }
            });
        }
    }

    function sellTower(scene, tower, detailsText, upgradeText, sellText, moveText, rangeCircle) {
        const sellPrice = getSellPrice(tower.grade);
        currency += sellPrice;
        currencyText.setText(`: ${currency}`);
        towers = towers.filter(t => t !== tower);
        tower.destroy();
        detailsText.destroy();
        upgradeText.destroy();
        sellText.destroy();
        rangeCircle.destroy();
    }

    function getSellPrice(grade) {
        switch (grade) {
            case '전설':
                return 50;
            case '유물':
                return 20;
            case '영웅':
                return 10;
            case '레어':
                return 5;
            case '일반':
            default:
                return 3;
        }
    }
}