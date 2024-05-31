window.onload = function() {
    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);

    const config = {
        type: Phaser.AUTO,
        width: isMobile ? window.innerWidth : 800,
        height: isMobile ? window.innerHeight : 600,
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
        scale: {
            mode: isMobile ? Phaser.Scale.RESIZE : Phaser.Scale.FIT,
            autoCenter: Phaser.Scale.CENTER_BOTH
        }
    };

    let game = new Phaser.Game(config);
    let tower;
    let enemies;
    let cursors;
    let gameOver = false;
    let gameOverText;
    let restartButton;
    let timerText;
    let startTime;
    let enemySpawnTimer;
    let enemySpeed = 100;
    let spawnInterval = 1000;
    let touchControls;

    function preload () {
        // 이미지를 사용하지 않고, 그래픽스로 그리기 때문에 이 부분은 비워둡니다.
    }

    function create () {
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

        enemySpawnTimer = this.time.addEvent({
            delay: spawnInterval,
            callback: addEnemy,
            callbackScope: this,
            loop: true
        });

        cursors = this.input.keyboard.createCursorKeys();

        // 터치 입력 지원
        touchControls = this.input.addPointer(1);

        // 충돌 감지
        this.physics.add.overlap(tower, enemies, hitEnemy, null, this);

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
        });

        // 게임 시작 시간 기록
        startTime = this.time.now;
    }

    function update () {
        if (gameOver) {
            return;
        }

        // 타이머 업데이트
        const elapsed = Math.floor((this.time.now - startTime) / 1000);
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
    }

    function addEnemy () {
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

        const enemy = enemies.create(x, y, 'enemy');
        this.physics.moveToObject(enemy, tower, enemySpeed);
    }

    function hitEnemy (tower, enemy) {
        this.physics.pause();
        tower.setTint(0xff0000);
        gameOver = true;
        const survivedTime = Math.floor((this.time.now - startTime) / 1000);
        gameOverText.setText('Game Over\n생존시간: ' + survivedTime + ' 초').setVisible(true);
        restartButton.setVisible(true);
    }
};
