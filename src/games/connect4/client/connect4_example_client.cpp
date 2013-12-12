#include <cstdio>
#include <cstdlib>
#include "connect4.h"

#define NO_ONE          (-1)

static int board[MAX_BOARD_HEIGHT][MAX_BOARD_WIDTH];
static int cellUpTo[MAX_BOARD_WIDTH];

static int bH, bW;

void clientRegister() {
    setName("Dumbo");
}

void clientInit(int boardHeight, int boardWidth, int playerID) {
    for (int i = 0; i < boardHeight; i++) {
        for (int j = 0; j < boardWidth; j++) {
            board[i][j] = NO_ONE;
        }
    }
    for (int i = 0; i < boardWidth; i++) {
        cellUpTo[i] = 0;
    }
    bH = boardHeight;
    bW = boardWidth;
}

void clientMoveMade(int pid, int column) {
    board[cellUpTo[column]][column] = pid;
    cellUpTo[column]++;
}

void clientDoTurn() {
    // Find random column which isn't full
    int c = rand() % bW;
    while (cellUpTo[c] == bH) {
        c = rand() % bW;
    }
    makeMove(c);
}



