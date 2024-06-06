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
    let currency = 20; // 초기 화폐 양
    let currencyText;
    let backgroundMusic; // 전역 변수로 선언
    let towerAttackSound; // 전역 변수로 선언
    let enemy1DieSound; // 전역 변수로 선언
    let enemyCount = 0;
    const MAX_ENEMIES = 150;
    let towerUpgradeLevel = 0;
    let baseAttackPower = {
        '일반': 10,
        '레어': 15,
        '영웅': 20,
        '유물': 30,
        '전설': 40
    };
    let isDoubleSpeed = false;

    function preload() {
        this.load.audio('backgroundMusic', 'assets/audio/Main.mp3');
        this.load.audio('towerAttackSound', 'assets/audio/TowerAttack.mp3'); // 공격 소리 추가
        this.load.audio('enemy1DieSound', 'assets/audio/Enemy1Die.mp3'); // 적 죽음 소리 추가

        this.load.image('background', 'assets/defense/tiles/land_1.png');
        this.load.image('flameTower', 'assets/defense/towers/ccc.png');
        this.load.image('path', 'assets/defense/tiles/decor_6.png');
        this.load.image('flame_1', 'assets/defense/towers/flame_1.png');
        this.load.image('flame_2', 'assets/defense/towers/flame_2.png');
        this.load.image('cancel', 'assets/defense/cancel.png'); // cancel 이미지 로드
        this.load.image('menu', 'assets/defense/menu.png'); // 햄버거 메뉴 이미지 로드
        this.load.image('meso', 'assets/defense/meso.png');
        for (let i = 1; i <= 24; i++) {
            this.load.image(`enemy_walk_${i}`, `assets/defense/enemies/orc_enemy_walk_${i}.png`);
        }
    }

    function create() {
        const self = this;

        // 오디오 시스템 초기화
        self.sound.context.resume();

        // 배경 음악 재생
        backgroundMusic = self.sound.add('backgroundMusic');
        // backgroundMusic.play({ loop: true });

        // 공격 소리와 적 죽음 소리 로드
        towerAttackSound = self.sound.add('towerAttackSound');
        enemy1DieSound = self.sound.add('enemy1DieSound');

        // BGM 끄기/켜기 버튼 이벤트 설정
        document.getElementById('bgmToggle').addEventListener('click', function() {
            if (backgroundMusic.isPlaying) {
                backgroundMusic.pause();
            } else {
                self.sound.context.resume();
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
            towerAttackSound.setVolume(volume);
            enemy1DieSound.setVolume(volume);
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
                {key: 'flame_2'}
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
            if (selectedTowerGrade) {
                const x = pointer.worldX;
                const y = pointer.worldY;

                // 직사각형 내부인지 확인
                const isInsideRectangle = (x >= INSTALL_RECT.left && x <= INSTALL_RECT.right && y >= INSTALL_RECT.top && y <= INSTALL_RECT.bottom);
                const isOccupied = towers.some(tower => Phaser.Math.Distance.Between(tower.x, tower.y, x, y) <= TOWER_RADIUS);

                if (isInsideRectangle && !isOccupied) {
                    const tower = self.add.sprite(x, y, 'flameTower').setScale(0.05);
                    tower.grade = selectedTowerGrade; // 타워의 등급 설정
                    tower.attackPower = getTowerAttackPower(tower.grade); // 타워의 공격력 설정
                    tower.setInteractive();
                    towers.push(tower);
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
            const towerMenu = self.add.container(550, 100).setSize(200, 200).setInteractive();
            const background = self.add.rectangle(0, 0, 200, 200, 0x000000, 0.8).setOrigin(0);
            const towerPurchaseText = self.add.text(10, 10, '타워 구매', { fontSize: '24px', fill: '#FFF' }).setInteractive();
            const toggleSpeedText = self.add.text(10, 50, '게임2배속On/Off', { fontSize: '24px', fill: '#FFF' }).setInteractive(); // 추가된 부분
            const startGameText = self.add.text(10, 90, '게임시작', { fontSize: '24px', fill: '#FFF' }).setInteractive();

            towerPurchaseText.on('pointerdown', () => {
                // 기존 타워 구매 로직
                if (currency >= 10) {
                    selectedTowerGrade = getRandomTowerGrade();
                    if (cursorTower) cursorTower.destroy();
                    cursorTower = self.add.sprite(self.input.activePointer.worldX, self.input.activePointer.worldY, 'flameTower').setScale(0.05);
                    cursorTower.setAlpha(0.5);
                    towerMenu.destroy();
                    currency -= 10;
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
            });

            startGameText.on('pointerdown', () => {
                towerMenu.destroy();
                startRound(self);
            });

            towerMenu.add([background, towerPurchaseText, toggleSpeedText, startGameText]);
            self.add.existing(towerMenu);

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
                        const flame = self.physics.add.sprite(tower.x, tower.y, 'flame_1').setScale(0.02); // 불꽃 이미지 크기 조정
                        flame.attackPower = tower.attackPower; // 타워의 공격력을 불꽃에 추가
                        self.physics.moveToObject(flame, closestEnemy, FLAME_SPEED);

                        flame.play('flame_anim');
                        towerAttackSound.play(); // 공격 소리 재생

                        self.physics.add.overlap(flame, closestEnemy, function(flame, enemy) {
                            if (enemy.active) {
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
        flame.destroy(); // 불꽃 제거
        // 적 위치에 불꽃 폭발 효과 (애니메이션이 있는 경우)
        const explosion = scene.add.sprite(enemy.x, enemy.y, 'flame_1').setScale(0.01); // 폭발 이미지 크기 조정
        explosion.play('flame_anim');
        explosion.on('animationcomplete', () => {
            explosion.destroy();
        });

        if (enemy.active) {
            enemy.health -= flame.attackPower; // 타워의 공격력만큼 적 체력 감소
            console.log('Enemy health:', enemy.health);
            if (enemy.health <= 0) {
                enemy.healthBar.destroy(); // 체력바 제거
                enemy.destroy(); // 적 제거
                enemy1DieSound.play(); // 적 죽음 소리 재생
                currency += 1; // 화폐 1원 증가
                currencyText.setText(`: ${currency}`); // 텍스트 업데이트

                // 적 유닛 수 감소
                currentEnemyCount--;

                // 적 수 텍스트 업데이트
                enemyCountText.setText(`Enemies: ${currentEnemyCount}`);
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
        // 적 유닛 수가 최대치를 넘으면 게임 종료
        if (currentEnemyCount >= MAX_ENEMIES) {
            endGame(scene);
            return;
        }

        // 라운드에 따른 체력 설정
        let enemyHealth;
        if (round === 1) {
            enemyHealth = 50;
        } else if (round === 2) {
            enemyHealth = 75;
        } else if (round === 3) {
            enemyHealth = 120;
        } else if (round === 4) {
            enemyHealth = 170;
        } else if (round === 5) {
            enemyHealth = 200;
        } else if (round === 6) {
            enemyHealth = 300;
        } else if (round === 7) {
            enemyHealth = 400;
        } else if (round === 8) {
            enemyHealth = 500;
        } else if (round === 9) {
            enemyHealth = 800;
        } else {
            // 그 이후 라운드의 경우 필요시 추가
            enemyHealth = 100; // 임시값
        }

        const enemy = scene.add.follower(path, 50, 150, 'enemy_walk_1').setScale(0.05);
        enemy.health = enemyHealth; // 계산된 체력으로 설정
        enemy.maxHealth = enemyHealth; // 최대 체력도 동일하게 설정
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

    function showTowerDetails(scene, tower) {
        const detailsText = scene.add.text(tower.x, tower.y - 50, `등급: ${tower.grade}\n공격력: ${tower.attackPower}`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        });

        // 일정 시간 후 텍스트 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                detailsText.destroy();
            }
        });
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
        // 게임 종료 텍스트 추가
        const gameOverText = scene.add.text(400, 300, '게임 종료!', {
            fontSize: '64px',
            fill: '#FFF',
            backgroundColor: '#000'
        }).setOrigin(0.5).setName('gameOverText');

        // 게임 다시하기 버튼 추가
        const restartButton = scene.add.text(400, 400, '게임 다시하기', {
            fontSize: '32px',
            fill: '#FFF',
            backgroundColor: '#000'
        }).setOrigin(0.5).setInteractive();

        restartButton.on('pointerdown', () => {
            // 페이지를 다시 로드하여 게임을 초기화
            location.reload();
        });

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
    }

    function showTowerDetailsAndUpgradeButton(scene, tower) {
        const basePower = baseAttackPower[tower.grade];
        const detailsText = scene.add.text(tower.x, tower.y - 70, `등급: ${tower.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        });

        const upgradeCost = 10 + (towerUpgradeLevel * 2);
        const upgradeText = scene.add.text(tower.x, tower.y - 40, `${towerUpgradeLevel + 1}단계업그레이드(${upgradeCost}원)`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        }).setInteractive();

        const sellText = scene.add.text(tower.x, tower.y - 10, `판매하기(${getSellPrice(tower.grade)}원)`, {
            fontSize: '16px',
            fill: '#FFF',
            backgroundColor: '#000'
        }).setInteractive();

        // 업그레이드 버튼 클릭 이벤트 추가
        upgradeText.on('pointerdown', () => {
            upgradeTower(scene, tower, detailsText, upgradeText, sellText);
        });

        // 판매 버튼 클릭 이벤트 추가
        sellText.on('pointerdown', () => {
            sellTower(scene, tower, detailsText, upgradeText, sellText);
        });

        // 텍스트 객체를 타워에 저장
        tower.upgradeDetailsText = detailsText;
        tower.upgradeText = upgradeText;

        // 일정 시간 후 텍스트 제거
        scene.time.addEvent({
            delay: 2000,
            callback: () => {
                detailsText.destroy();
                upgradeText.destroy();
                sellText.destroy();
            }
        });
    }

    function upgradeTower(scene, tower, detailsText, upgradeText, sellText) {
        const upgradeCost = 10 + (towerUpgradeLevel * 2);

        if (currency >= upgradeCost) {
            currency -= upgradeCost; // 화폐 감소
            currencyText.setText(`: ${currency}`); // 화폐 텍스트 업데이트

            towerUpgradeLevel += 1; // 업그레이드 레벨 증가

            // 모든 타워의 공격력을 갱신
            towers.forEach(t => {
                t.attackPower = getTowerAttackPower(t.grade);
            });

            // 모든 타워의 텍스트를 갱신
            towers.forEach(t => {
                if (t.upgradeDetailsText && t.upgradeText) {
                    const basePower = baseAttackPower[t.grade];
                    t.upgradeDetailsText.setText(`등급: ${t.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})`);
                    t.upgradeText.setText(`${towerUpgradeLevel + 1}단계업그레이드(${10 + (towerUpgradeLevel * 2)}원)`);
                }
            });

            // 현재 클릭된 타워의 텍스트도 갱신
            const basePower = baseAttackPower[tower.grade];
            detailsText.setText(`등급: ${tower.grade}\n공격력: (${basePower} + ${towerUpgradeLevel * basePower})`);
            upgradeText.setText(`${towerUpgradeLevel + 1}단계업그레이드(${10 + (towerUpgradeLevel * 2)}원)`);
        } else {
            // 화폐가 부족할 때 경고 메시지 표시
            const warningText = scene.add.text(400, 300, '화폐가 부족합니다!', { fontSize: '32px', fill: '#FFF', backgroundColor: '#000' }).setOrigin(0.5);
            scene.time.addEvent({
                delay: 2000,
                callback: () => {
                    warningText.destroy();
                }
            });
        }
    }

    function sellTower(scene, tower, detailsText, upgradeText, sellText) {
        const sellPrice = getSellPrice(tower.grade);
        currency += sellPrice; // 화폐 증가
        currencyText.setText(`: ${currency}`); // 화폐 텍스트 업데이트
        towers = towers.filter(t => t !== tower); // 타워 목록에서 제거
        tower.destroy(); // 타워 제거
        detailsText.destroy(); // 상세 텍스트 제거
        upgradeText.destroy(); // 업그레이드 텍스트 제거
        sellText.destroy(); // 판매 텍스트 제거
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
