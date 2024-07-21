document.addEventListener('DOMContentLoaded', function () {
    console.log('omokGame.js has been loaded successfully');

    const gameContainer = document.getElementById('game-container');
    gameContainer.innerHTML = '';
    const gameBoard = document.createElement('div');
    const controls = document.createElement('div');
    const buttons = document.createElement('div');
    const startButton = document.createElement('button');
    const placeStoneButton = document.createElement('button');
    const chat = document.createElement('div');
    const preGameSettings = document.createElement('div');
    const size = 15;  // size를 15로 수정
    const cells = [];
    let selectedCell = null;
    let playerFirst = true;
    let firstMove = true; // 첫 번째 수를 위한 변수 추가
    const modal = document.getElementById('options-modal');
    const startBtn = document.getElementById('start-btn');
    const selectedOptionText = document.getElementById('selected-option');

    // Clear any existing content to prevent duplication
    gameContainer.innerHTML = '';

    // Create main game elements
    gameBoard.id = 'game-board';

    controls.id = 'controls';

    buttons.id = 'buttons';

    startButton.id = 'start-game-btn';
    startButton.innerText = 'GameStart';

    placeStoneButton.id = 'place-stone-btn';
    placeStoneButton.innerText = '돌생성';
    placeStoneButton.disabled = true;

    buttons.appendChild(startButton);
    buttons.appendChild(placeStoneButton);

    chat.id = 'chat';

    controls.appendChild(buttons);
    controls.appendChild(chat);

    gameContainer.appendChild(gameBoard);
    gameContainer.appendChild(controls);

    // Add pre-game settings
    preGameSettings.id = 'pre-game-settings';
    preGameSettings.style.display = 'none';
    preGameSettings.innerHTML = `
        <p>Who goes first?</p>
        <button id="player-first-btn">Player First</button>
        <button id="ai-first-btn">AI First</button>
    `;
    gameContainer.appendChild(preGameSettings);

    // Initialize game board
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
        { top: 3, left: 3 },
        { top: 3, left: 11 },
        { top: 11, left: 3 },
        { top: 11, left: 11 },
        { top: 7, left: 7 }
    ];

    starPoints.forEach(point => {
        const star = document.createElement('div');
        star.classList.add('star-point');
        star.style.top = `calc(${point.top * 40}px - 20px)`;
        star.style.left = `calc(${point.left * 40}px - 20px)`;
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

        if (x < 20) {
            closestCol = col;
        } else {
            closestCol = col + 1;
        }

        if (y < 20) {
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

    async function getGeminiMove(board, firstMove, playerFirst, playerMove = null) {
        const data = {
            boardState: board,
            firstMove: firstMove,
            playerFirst: playerFirst,
            playerMove: playerMove
        };

        try {
            console.log('Sending board state to server:', JSON.stringify(data));
            const response = await fetch('/api/gemini/move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(`API request failed with status ${response.status}`);
            }
            const responseData = await response.json();
            console.log('Received data from server:', responseData);
            return responseData;
        } catch (error) {
            console.error('Error fetching move from Gemini API:', error);
            return null;
        }
    }

    function getBoardState() {
        return cells.map(row => row.map(cell => {
            if (cell.classList.contains('black')) return 'black';
            if (cell.classList.contains('white')) return 'white';
            return 'empty';
        }));
    }

    placeStoneButton.addEventListener('click', async function () {
        if (selectedCell) {
            const row = selectedCell.dataset.row;
            const col = selectedCell.dataset.col;
            if (!selectedCell.classList.contains('black') && !selectedCell.classList.contains('white')) {
                placeStone(row, col, playerFirst ? 'black' : 'white');
                selectedCell.classList.remove('red-point');
                selectedCell = null;
                placeStoneButton.disabled = true;

                const boardState = getBoardState();
                const playerMove = { row: parseInt(row, 10), col: parseInt(col, 10) };
                const response = await getGeminiMove(boardState, firstMove, playerFirst, playerMove);
                const aiMove = response;
                if (aiMove) {
                    placeStone(aiMove.row, aiMove.col, playerFirst ? 'white' : 'black');
                } else {
                    console.error('Failed to get AI move');
                }
                firstMove = false; // 첫 번째 수 이후에는 false로 설정
            }
        }
    });

    function placeStone(row, col, color) {
        console.log(`Placing stone at row: ${row}, col: ${col}, color: ${color}`);
        const cell = cells[row][col];
        if (!cell.classList.contains('black') && !cell.classList.contains('white')) {
            cell.classList.add(color);
        } else {
            console.log(`Cell at row: ${row}, col: ${col} is already occupied.`);
        }
    }

    startButton.addEventListener('click', function () {
        modal.style.display = 'block';
    });

    document.getElementById('first-btn').addEventListener('click', function () {
        playerFirst = true;
        startBtn.disabled = false; // 옵션 선택 시 게임 시작 버튼 활성화
        selectedOptionText.innerText = '선공을 선택하셨습니다.';
        document.getElementById('first-btn').classList.add('selected');
        document.getElementById('second-btn').classList.remove('selected');
    });

    document.getElementById('second-btn').addEventListener('click', function () {
        playerFirst = false;
        startBtn.disabled = false; // 옵션 선택 시 게임 시작 버튼 활성화
        selectedOptionText.innerText = '후공을 선택하셨습니다.';
        document.getElementById('second-btn').classList.add('selected');
        document.getElementById('first-btn').classList.remove('selected');
    });

    startBtn.addEventListener('click', function () {
        modal.style.display = 'none';
        initializeGame();
    });

    function initializeGame() {
        console.log('Game started! Player first:', playerFirst);

        firstMove = true; // 게임 시작 시 첫 번째 수를 true로 설정

        if (!playerFirst) {
            // AI가 선공인 경우 AI의 첫 번째 수를 요청
            const boardState = getBoardState();
            getGeminiMove(boardState, true, playerFirst).then(response => {
                const aiMove = response;
                if (aiMove) {
                    placeStone(aiMove.row, aiMove.col, 'black');
                } else {
                    console.error('Failed to get AI move');
                }
                firstMove = false; // AI가 첫 번째 수를 둔 후에는 false로 설정
            });
        }
        placeStoneButton.disabled = !playerFirst;
    }
});
