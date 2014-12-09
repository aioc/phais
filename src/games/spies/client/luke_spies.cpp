#include <cstdio>
#include <cstdlib>
#include "spies.h"

#define NUM_MOVES_SIMULATE          (5)
#define STILL_BENEFIT               (1024)

struct player {
    int pid;
    int h;
    int d;
    int r;
    int c;
    char preMoves[MAX_STARTING_HEALTH];
};

static int nP;
static int bS;
static int sH;
static int myID;

static struct player players[MAX_NUM_PLAYERS];
static int numAlivePlayers;
static int numShots[MAX_STARTING_HEALTH][MAX_BOARD_SIZE][MAX_BOARD_SIZE];

static int preMove[MAX_STARTING_HEALTH][MAX_BOARD_SIZE][MAX_BOARD_SIZE];
static int bestPath[MAX_STARTING_HEALTH][MAX_BOARD_SIZE][MAX_BOARD_SIZE];

static int dx[] = {0, 1, 0, -1};
static int dy[] = {-1, 0, 1, 0};

void clientRegister() {
    setName("Skynet");
    setColour(255, 190, 28);
}


void clientPlayerState(int pid, int health, int dir, int r, int c, char *previousMoves) {
    players[pid].pid = pid;
    players[pid].h = health;
    players[pid].d = dir;
    players[pid].r = r;
    players[pid].c = c;
    for (int i = 0; i < sH; i++) {
        players[pid].preMoves[i] = previousMoves[i];
    }
    if (players[pid].h > 0) {
        numAlivePlayers++;
    }
}

static void resetState() {
    numAlivePlayers = 0;
    for (int i = 0; i < sH; i++) {
        for (int j = 0; j < bS; j++) {
            for (int k = 0; k < bS; k++) {
                numShots[i][j][k] = 0;
                bestPath[i][j][k] = -1;
            }
        }
    }
}

static void fireGrid(int m, int r, int c, int d) {
    while (r >= 0 && c >= 0 && r < bS && c < bS) {
        numShots[m][r][c] += sH - m;
        r += dy[d];
        c += dx[d];
    }
}

static int absV(int n) {
    return n < 0 ? -n : n;
}

static void simulateMoves (int pid, int moves) {
    struct player p = players[pid];
    for (int i = 0; i < NUM_MOVES_SIMULATE; i++) {
        int moveID = (moves & (7 << (i * 3))) >> (i * 3);
        if (moveID < 4) {
            // It's a move move. Change position
            p.r += dy[moveID];
            p.c += dx[moveID];
            if (p.r < 0) {
                p.r == 0;
            }
            if (p.c < 0) {
                p.c = 0;
            }
            if (p.r >= bS) {
                p.r = bS;
            }
            if (p.c >= bS) {
                p.c = bS;
            }
        } else if (moveID == 4) {
            // Turn left
            p.d = (p.d + 3) % 4;
        } else if (moveID == 5) {
            // Turn right
            p.d == (p.d + 1) % 4;
        } else if (moveID == 6) {
            // Shoot
            fireGrid(i, p.r, p.c, p.d);
        }
    }
}

static void calculateBoard() {
    // Essentially, for each player loop over all possible moves they can make, up to 5
    // This is 8^5 == 65000.
    // If they fire, mark it in the array
    for (int i = 0; i < nP; i++) {
        // If they aren't alive, ignore them
        if (players[i].h == 0 || i == myID) {
            continue;
        }
        // Loop over every possible move set of 5
        for (int j = 0; j < (1 << (3 * NUM_MOVES_SIMULATE)); j++) {
            simulateMoves(i, j);
        }
    }
}

static int findBest (int r, int c, int turn) {
    if (turn == players[myID].h) {
        return 0;
    }
    if (bestPath[turn][r][c] != -1) {
        return bestPath[turn][r][c];
    }
    // Try all 4 directions, plus being still
    int best = findBest(r, c, turn + 1) + numShots[turn][r][c];
    int bestMove = 4;
    for (int i = 0; i < 4; i++) {
        int newR = r + dy[i];
        int newC = c + dx[i];
        if (newR >= 0 && newC >= 0 && newR < bS && newC < bS) {
            int res = findBest(newR, newC, turn + 1) + numShots[turn][newR][newC];
            if (res < best) {
                best = res;
                bestMove = i;
            }
        }
    }
    bestPath[turn][r][c] = best;
    preMove[turn][r][c] = bestMove;
    return best;
};

static int findClosestWall(int r, int c) {
    if (r < bS / 2) {
        if (c < bS / 2) {
            if (r < c) {
                return UP;
            } else {
                return LEFT;
            }
        } else {
            if (r < bS - c - 1) {
                return UP;
            } else {
                return RIGHT;
            }
        }
    } else {
        if (c < bS / 2) {
            if (bS - r - 1 < c) {
                return DOWN;
            } else {
                return LEFT;
            }
        } else {
            if (bS - r - 1 < bS - c - 1) {
                return DOWN;
            } else {
                return RIGHT;
            }
        }
    }
}

static void turnFaceDir(int *moveAt, char *moves, int d, int wantD) {
    int dif = absV(d - wantD);
    if (dif == 1) {
        if (wantD < d) {
            moves[(*moveAt)++] = MOVE_TURN_LEFT;
        } else {      
            moves[(*moveAt)++] = MOVE_TURN_RIGHT;
        }
    } else if (dif == 2) {
        moves[(*moveAt)++] = MOVE_TURN_LEFT;
        moves[(*moveAt)++] = MOVE_TURN_LEFT;
    } else if (dif == 3) {
        if (wantD < d) {
            moves[(*moveAt)++] = MOVE_TURN_RIGHT;
        } else {      
            moves[(*moveAt)++] = MOVE_TURN_LEFT;
        }
    }
}

static void make1v1Move(char *moves) {
    struct player me = players[myID];
    struct player them;
    int curMoveAt = 0;
    for (int i = 0; i < nP; i++) {
        if (i != myID && players[i].h > 0) {
            them = players[i];
            break;
        }
    }
    int xDif = me.c - them.c;
    int yDif = me.r - them.r;
    if (xDif == 0) {
        // Turn into the right direction
        if (yDif < 0) {
            // Need to face up
        }
    }
}

void clientDoTurn() {
    char moves[MAX_STARTING_HEALTH + 5];
    for (int i = 0; i < MAX_STARTING_HEALTH; i++) {
        moves[i] = MOVE_NOTHING;
    }
    if (players[myID].h == 0) {
        // damn it we're dead
        makeMoves(moves);
        resetState();
        return;
    }
    if (numAlivePlayers >= 2) {
        // Survive!
        calculateBoard();
        // Now, we get to find the best path...
        int be = findBest(players[myID].r, players[myID].c, 0);
        printf ("Best == %d\n", be);
        if (players[myID].h < sH) {
            // Also calculate standing still cost (gaining back health)
            int stillCost = -STILL_BENEFIT;
            for (int i = 0; i < MAX_STARTING_HEALTH; i++) {
                stillCost += numShots[i][players[myID].r][players[myID].c];
            }
            printf ("Still == %d\n", stillCost);
            if (stillCost < be) {
                makeMoves(moves);
                resetState();
                return;
            }
        }
        // Now just make those moves
        char moveStr[] = "^>v<";
        int r = players[myID].r;
        int c = players[myID].c;
        for (int i = 0; i < players[myID].h; i++) {
            if (preMove[i][r][c] < 4) {
                moves[i] = moveStr[preMove[i][r][c]];
            } else {
                // Work out what we want. Can either turn, or shoot.
                int closWal = findClosestWall(players[myID].r, players[myID].c);
                printf ("Closest wall: %d\n", closWal);
                int moveMade = 0;
                turnFaceDir(&moveMade, &(moves[i]), players[myID].d, (closWal + 2) % 4);
                if (moveMade == 0) {
                    moves[i] = MOVE_SHOOT;
                } else {
                    if (moves[i] == MOVE_TURN_LEFT) {
                        players[myID].d = (players[myID].d + 3) % 4;
                    } else {
                        players[myID].d = (players[myID].d + 1) % 4;
                    }
                }
            }
            int m = preMove[i][r][c];
            if (m < 4) {
                r += dy[m];
                c += dx[m];
            }
        }
    } else {
        // 1v1 mode engaged! Time to win this :D
        make1v1Move(moves);
    }
    makeMoves(moves);
    resetState();
}


void clientInit(int numPlayers, int boardSize, int startingHealth, int playerID) {
    myID = playerID;
    nP = numPlayers;
    bS = boardSize;
    sH = startingHealth;
    resetState();
}