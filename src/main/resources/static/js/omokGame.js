document.addEventListener('DOMContentLoaded', function () {
    console.log('omokGame.js has been loaded successfully');
    const gameContainer = document.getElementById('game-container');

    // Clear any existing content to prevent duplication
    gameContainer.innerHTML = '';

    // Create main game elements
    const gameBoard = document.createElement('div');
    gameBoard.id = 'game-board';

    const controls = document.createElement('div');
    controls.id = 'controls';

    const buttons = document.createElement('div');
    buttons.id = 'buttons';

    const startButton = document.createElement('button');
    startButton.id = 'start-game-btn';
    startButton.innerText = 'GameStart';

    const placeStoneButton = document.createElement('button');
    placeStoneButton.id = 'place-stone-btn';
    placeStoneButton.innerText = '돌생성';
    placeStoneButton.disabled = true;

    buttons.appendChild(startButton);
    buttons.appendChild(placeStoneButton);

    const chat = document.createElement('div');
    chat.id = 'chat';

    controls.appendChild(buttons);
    controls.appendChild(chat);

    gameContainer.appendChild(gameBoard);
    gameContainer.appendChild(controls);

    // Initialize game board
    const size = 14; // 14줄 유지
    let cells = [];
    let selectedCell = null;

    for (let i = 0; i < size; i++) {
        cells[i] = [];
        for (let j = 0; j < size; j++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            cell.dataset.row = i;
            cell.dataset.col = j;
            cell.style.gridRow = `${i + 1}`;
            cell.style.gridColumn = `${j + 1}`;
            cell.addEventListener('click', onCellClick);
            cells[i].push(cell);
            gameBoard.appendChild(cell);
        }
    }

    // Add star points
    const starPoints = [
        { top: 2, left: 2 },
        { top: 2, left: 12 },
        { top: 12, left: 2 },
        { top: 12, left: 12 },
        { top: 7, left: 7 }
    ];

    starPoints.forEach(point => {
        const star = document.createElement('div');
        star.classList.add('star-point');
        star.style.top = `calc(${point.top * 60}px - 30px)`;
        star.style.left = `calc(${point.left * 60}px - 30px)`;
        gameBoard.appendChild(star);
    });

    function onCellClick(event) {
        if (selectedCell) {
            selectedCell.classList.remove('red-point');
        }

        const rect = event.target.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        const row = parseInt(event.target.dataset.row, 10);
        const col = parseInt(event.target.dataset.col, 10);

        let closestRow = row;
        let closestCol = col;

        if (x < 30) {
            closestCol = col;
        } else {
            closestCol = col + 1;
        }

        if (y < 30) {
            closestRow = row;
        } else {
            closestRow = row + 1;
        }

        if (closestRow < 0) closestRow = 0;
        if (closestCol < 0) closestCol = 0;
        if (closestRow >= size) closestRow = size - 1;
        if (closestCol >= size) closestCol = size - 1;

        selectedCell = cells[closestRow][closestCol];
        selectedCell.classList.add('red-point');
        placeStoneButton.disabled = false;
    }

    placeStoneButton.addEventListener('click', function () {
        if (selectedCell) {
            const row = selectedCell.dataset.row;
            const col = selectedCell.dataset.col;
            if (!selectedCell.classList.contains('black') && !selectedCell.classList.contains('white')) {
                placeStone(row, col, 'black');
                selectedCell.classList.remove('red-point');
                selectedCell = null;
                placeStoneButton.disabled = true;

                setTimeout(() => {
                    const aiMove = getAIMove();
                    placeStone(aiMove.row, aiMove.col, 'white');
                }, 500);
            }
        }
    });

    function placeStone(row, col, color) {
        const cell = cells[row][col];
        if (!cell.classList.contains('black') && !cell.classList.contains('white')) {
            cell.classList.add(color);
        }
    }

    function getAIMove() {
        let row, col;
        do {
            row = Math.floor(Math.random() * size);
            col = Math.floor(Math.random() * size);
        } while (cells[row][col].classList.contains('black') || cells[row][col].classList.contains('white'));
        return { row, col };
    }

    startButton.addEventListener('click', function () {
        console.log('Game started!');
        placeStoneButton.disabled = false; // 게임 시작 시 돌 생성 버튼 활성화
        // 추가로 초기화 작업을 여기서 수행할 수 있습니다.
    });
});
