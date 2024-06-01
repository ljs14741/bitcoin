window.onload = function() {
    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);

    const config = {
        type: Phaser.AUTO,
        width: 800,
        height: 600,
        backgroundColor: '#000000',
        physics: {
            default: 'arcade',
            arcade: {
                gravity: { y: 0 }
            }
        },
        scene: {
            preload: preload,
            create: create,
            update: update
        },
    };

    let game = new Phaser.Game(config);
    let tower;
    let enemies;
    let powerUps;
    let cursors;
    let gameOver = false;
    let gameOverText;
    let restartButton;
    let timerText;
    let startTime;
    let pauseStartTime = 0;
    let totalPausedTime = 0;
    let enemySpawnTimer;
    let powerUpSpawnTimer;
    let enemySpeed = 100;
    let spawnInterval = 1000;
    let touchControls;

    function preload () {
        // 음악
        this.load.audio('backgroundMusic', 'assets/audio/Ztar Warz.mp3');
    }

    function create () {
        // 배경 음악 재생
        if (!backgroundMusic) {
            backgroundMusic = this.sound.add('backgroundMusic', { loop: true });
            backgroundMusic.play();
        }

        // 타워를 그래픽스로 그리기
        const towerGraphics = this.make.graphics({ x: 0, y: 0, add: false });
        towerGraphics.fillStyle(0xFFFF00, 1);
        towerGraphics.fillCircle(5, 5, 5); // 반지름을 5로 줄임
        towerGraphics.generateTexture('tower', 10, 10); // 지름이 10인 텍스처 생성
        tower = this.physics.add.sprite(this.cameras.main.width / 2, this.cameras.main.height / 2, 'tower');

        // 적을 그래픽스로 그리기
        const enemyGraphics = this.make.graphics({ x: 0, y: 0, add: false });
        enemyGraphics.fillStyle(0xFF0000, 1);
        enemyGraphics.fillCircle(3.75, 3.75, 3.75); // 반지름을 3.75로 줄임
        enemyGraphics.generateTexture('enemy', 7.5, 7.5); // 지름이 7.5인 텍스처 생성
        enemies = this.physics.add.group();

        // 파워업 아이템 그래픽
        const powerUpGraphics = this.make.graphics({ x: 0, y: 0, add: false });
        powerUpGraphics.fillStyle(0x00FF00, 1);
        powerUpGraphics.fillCircle(5, 5, 5); // 반지름 5로 줄임
        powerUpGraphics.generateTexture('powerUp', 10, 10); // 지름이 10인 텍스처 생성
        powerUps = this.physics.add.group();

        enemySpawnTimer = this.time.addEvent({
            delay: spawnInterval,
            callback: addEnemy,
            callbackScope: this,
            loop: true
        });

        powerUpSpawnTimer = this.time.addEvent({
            delay: 10000,
            callback: addPowerUp,
            callbackScope: this,
            loop: true
        });

        cursors = this.input.keyboard.createCursorKeys();

        // 터치 입력 지원
        touchControls = this.input.addPointer(1);

        // 충돌 감지
        this.physics.add.overlap(tower, enemies, hitEnemy, null, this);
        this.physics.add.overlap(tower, powerUps, collectPowerUp, null, this);

        // 타이머 텍스트
        timerText = this.add.text(16, 16, 'Time: 0', { fontSize: '32px', fill: '#FFF' });

        // 게임 오버 텍스트
        gameOverText = this.add.text(this.cameras.main.width / 2 - 100, this.cameras.main.height / 2 - 50, 'Game Over', { fontSize: '32px', fill: '#FFF' }).setVisible(false);

        // 다시하기 버튼 생성
        restartButton = this.add.text(this.cameras.main.width / 2 - 50, this.cameras.main.height / 2, 'Restart', { fontSize: '32px', fill: '#FFF' }).setInteractive().setVisible(false);
        restartButton.on('pointerdown', () => {
            this.scene.restart();
            gameOver = false;
            startTime = this.time.now;
            enemySpeed = 100;
            spawnInterval = 1000;
            totalPausedTime = 0;
        });

        // 게임 시작 시간 기록
        startTime = this.time.now;

        document.addEventListener('visibilitychange', handleVisibilityChange);
    }

    function update () {
        if (gameOver) {
            return;
        }

        // 타이머 업데이트
        const elapsed = Math.floor((this.time.now - startTime - totalPausedTime) / 1000);
        timerText.setText('Time: ' + elapsed);

        // 난이도 증가
        if (elapsed > 0 && elapsed % 10 === 0 && enemySpawnTimer.delay > 200) {
            enemySpawnTimer.remove(false);
            spawnInterval -= 100;  // 적 생성 주기를 줄임
            enemySpawnTimer = this.time.addEvent({
                delay: spawnInterval,
                callback: addEnemy,
                callbackScope: this,
                loop: true
            });
            enemySpeed += 20; // 적 속도 증가
        }

        // 키보드 입력 처리
        if (cursors.left.isDown) {
            tower.x -= 3;
        }
        else if (cursors.right.isDown) {
            tower.x += 3;
        }

        if (cursors.up.isDown) {
            tower.y -= 3;
        }
        else if (cursors.down.isDown) {
            tower.y += 3;
        }

        // 터치 입력 처리
        if (touchControls.isDown) {
            const touchX = touchControls.x;
            const touchY = touchControls.y;

            if (touchX < tower.x) {
                tower.x -= 3;
            } else if (touchX > tower.x) {
                tower.x += 3;
            }

            if (touchY < tower.y) {
                tower.y -= 3;
            } else if (touchY > tower.y) {
                tower.y += 3;
            }
        }

        // 화면 경계를 벗어나지 않도록 제한
        if (tower.x < 0) {
            tower.x = 0;
        } else if (tower.x > this.cameras.main.width) {
            tower.x = this.cameras.main.width;
        }

        if (tower.y < 0) {
            tower.y = 0;
        } else if (tower.y > this.cameras.main.height) {
            tower.y = this.cameras.main.height;
        }
    }

    function addEnemy () {
        const elapsed = Math.floor((this.time.now - startTime - totalPausedTime) / 1000);

        // 적이 화면 밖에서 생성되도록 함
        const position = Phaser.Math.Between(0, 3);
        let x, y;

        switch (position) {
            case 0: // 상단
                x = Phaser.Math.Between(0, this.cameras.main.width);
                y = -20;
                break;
            case 1: // 하단
                x = Phaser.Math.Between(0, this.cameras.main.width);
                y = this.cameras.main.height + 20;
                break;
            case 2: // 좌측
                x = -20;
                y = Phaser.Math.Between(0, this.cameras.main.height);
                break;
            case 3: // 우측
                x = this.cameras.main.width + 20;
                y = Phaser.Math.Between(0, this.cameras.main.height);
                break;
        }

        let enemy;
        if (elapsed < 10) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed);
        } else if (elapsed < 20) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.1);
        } else if (elapsed < 30) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.2);
        } else if (elapsed < 40) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.3);
        } else if (elapsed < 50) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.4);
        } else if (elapsed < 60) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.5);
        } else if (elapsed < 70) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.6);
        } else if (elapsed < 80) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.7);
        } else if (elapsed < 90) {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.8);
        } else {
            enemy = enemies.create(x, y, 'enemy');
            this.physics.moveToObject(enemy, tower, enemySpeed * 1.9);
        }
    }

    function addPowerUp () {
        const x = Phaser.Math.Between(0, this.cameras.main.width);
        const y = Phaser.Math.Between(0, this.cameras.main.height);
        const powerUp = powerUps.create(x, y, 'powerUp');

        powerUp.setCollideWorldBounds(true);
        powerUp.setBounce(1);
        powerUp.setVelocity(Phaser.Math.Between(-100, 100), Phaser.Math.Between(-100, 100));
    }

    function collectPowerUp(tower, powerUp) {
        powerUp.destroy();
        activatePowerUp.call(this); // 여기서 this 바인딩
    }

    function activatePowerUp() {
        tower.setTint(0x00ff00); // 무적 상태 표시
        this.time.addEvent({
            delay: 5000,
            callback: () => {
                tower.clearTint(); // 무적 상태 해제
            }
        });
    }

    function hitEnemy (tower, enemy) {
        if (tower.tintTopLeft === 0x00ff00) {
            enemy.destroy();
            return;
        }
        this.physics.pause();
        tower.setTint(0xff0000);
        gameOver = true;
        const survivedTime = Math.floor((this.time.now - startTime - totalPausedTime) / 1000);
        gameOverText.setText('Game Over\n생존시간: ' + survivedTime + ' 초').setVisible(true);
        restartButton.setVisible(true);

        // 게임 데이터 서버에 전송
        const gameData = {
            gameName: "총알 피하기",
            kakaoId: 9999,
            score: survivedTime
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

    function handleVisibilityChange() {
        if (document.hidden) {
            pauseStartTime = performance.now();
        } else {
            const now = performance.now();
            totalPausedTime += now - pauseStartTime;
        }
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
};
