#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "spies.h"
#include <ctime>
#define me players[myID]

FILE *flog=fopen("log.txt", "w");
void offensive();

struct Player {
    int pid;
    int health;
    int dir;
    int row;
    int col;
    char *previousMoves;
};

static int myHealth;
static int myID;
// bit i set in a tile means you don't want to move there in move i
static int danger[MAX_BOARD_SIZE+5][MAX_BOARD_SIZE+5];
static Player players[MAX_NUM_PLAYERS];
static int numTurns = 0;
static int bs;
static int np;
static int dx[] = {-1, 0, 1, 0, 0};
static int dy[] = {0, 1, 0, -1, 0};
static char m[] = "^>v<-";

void clientRegister() {
    setName("lolnic");
    setColour(0, 0, 0);
}

/*
 *   This is called when the game is about to begin. It tells you the number
 *   of players in the game, how big the board is, the starting health of
 *   all players, and your playerID for the game (0 <= playerID < numPlayers).
 *   You are not required to call anything in here.
 */
void clientInit(int numPlayers, int boardSize, int startingHealth, int playerID) {
    bs = boardSize;
    np = numPlayers;
    myID = playerID;
    numTurns = 0;
}

void clearThings() {
    for (int i = 0; i < bs; i++) {
        for (int j = 0; j < bs; j++) {
            danger[i][j] = 0;
        }
    }
}


// ******** This function will be called *BEFORE* players take their turns for the round ********

/*
 *   This is called once for *each* player in the game, telling you their
 *   state at the start of the round.  pid will contain their playerID,
 *   health will contain the number of hitpoints they have remaining, and r
 *   and c will be their row and column respectively on the grid
 *   (0 <= r < boardSize, 0 <= c < boardSize). previousMoves will string
 *   containing the moves this player made during the previous round
 *   (len(previousMoves) == startingHealth). Note that previousMoves may be
 *   modified after you return from this function, so if you wish to use its
 *   value later on, you should make a copy.
 *   If pid is your playerID, this will be your state.
 */
void clientPlayerState(int pid, int health, int dir, int r, int c, char *previousMoves) {
    players[pid].pid = pid;
    players[pid].health = health;
    players[pid].dir = dir;
    players[pid].row = r;
    players[pid].col = c;
    players[pid].previousMoves = new char[health];
    for (int i = 0; i < health; i++) players[pid].previousMoves[i] = previousMoves[i];
}

int turnLeft(int dir) {
    if (dir == 0) return 3;
    return dir-1;
}

int turnRight(int dir) {
    if (dir == 3) return 0;
    return dir+1;
}

void markDanger(char moves[], int player) {
    Player tmp = players[player];
    for (int i = 0; i < tmp.health; i++) {
        switch (moves[i]) {
            case MOVE_UP:
                tmp.row--;
                break;
            case MOVE_RIGHT:
                tmp.col++;
                break;
            case MOVE_DOWN:
                tmp.row++;
                break;
            case MOVE_LEFT:
                tmp.col--;
                break;
            case MOVE_TURN_LEFT:
                tmp.dir = turnLeft(tmp.dir);
                break;
            case MOVE_TURN_RIGHT:
                tmp.dir = turnRight(tmp.dir);
                break;
            case MOVE_SHOOT:
                int r = tmp.row;
                int c = tmp.col;
                while (0 <= r && r < bs && 0 <= c && c < bs) {
                    danger[r][c] |= (1<<i);
                    r += dx[tmp.dir];
                    c += dy[tmp.dir];
                }
                break;
        }
    }
}

char allmoveset[6];
void allMoves(int upto) {
    if (upto == 5) {
        for (int i = 0; i < np; i++) {
            if (i == myID) continue;
            markDanger(allmoveset, i);
        }
        return;
    }
    char lol[] = "^<>v()S-";
    for (int i = 0; i < strlen(lol); i++) {
        allmoveset[upto] = lol[i];
        allMoves(upto+1);
    }
}

int findGoodMove(char move[], int upto, int row, int col) {
    char bestmove[6];
    int lowestRisk = 1000000;
    if (upto == me.health) {
        int risk = 0;
        for (int i = me.health; i < 5; i++) {
            if (danger[row][col] & (1<<i)) risk++;
        }
        return risk;
    }
    for (int i = 0; i < 5; i++) {
        if (row+dx[i] < 0 || row+dx[i] >= bs || col+dy[i] < 0 || col+dy[i] >= bs)
            continue;
        int r = danger[row+dx[i]][col+dy[i]] & (1<<upto);
        r = !!r;
        move[upto] = m[i];
        r += findGoodMove(move, upto+1, row+dx[i], col+dy[i]);
        if (r < lowestRisk) {
            lowestRisk = r;
            strcpy(bestmove, move);
        }
    }
    strcpy(move, bestmove);
    return lowestRisk;
}

/*
 *   This is called when it is time for you to take your turn. While in the
 *   function, you must call makeMoves once. If you make multiple calls to
 *   makeMove, only the final call will be considered.
 */
void defensive(bool seriously=false) {
    char moves[MAX_STARTING_HEALTH+1];
    allMoves(0);
    moves[me.health] = 0;
    if (danger[me.row][me.col] == 0) {
        if (me.health == 5) {
            if (seriously)
                makeMoves("-----");
            else
                offensive();
        }
        else {
            makeMoves("-----");
        }
    }
    else if (findGoodMove(moves, 0, me.row, me.col) < 5)
        makeMoves(moves);
    else {
        if (seriously)
            makeMoves("-----");
        else
            offensive();
    }
}

int abs(int x) {
    if (x<0)return -x;
    return x;
}

void offensive() {
    // pick the closest guy, shoot him where he is (he might not move in the right direction to dodge)
    int gotor = me.row;
    int gotoc = me.col;
    int gotodir = me.dir;
    int best = 10000;
    for (int i = 0; i < np; i++) {
        if (i == myID) continue;
        if (players[i].health == 0) continue;
        int horiz = abs(players[i].col - me.col);
        if (me.dir == LEFT || me.dir == RIGHT) horiz++;
        if (me.row > players[i].row && me.dir == DOWN) horiz += 2;
        if (me.row < players[i].row && me.dir == UP) horiz += 2;
        int vert = abs(players[i].row - me.row);
        if (me.dir == UP || me.dir == DOWN) vert++;
        if (me.col < players[i].col && me.dir == RIGHT) vert += 2;
        if (me.col > players[i].col && me.dir == LEFT) vert += 2;
        
        if (vert < best) {
            best = vert;
            gotor = players[i].row;
            gotoc = me.col;
            if (me.col < players[i].col) gotodir = RIGHT;
            else gotodir = LEFT;
        }
        if (horiz < best) {
            best = horiz;
            gotor = me.row;
            gotoc = players[i].col;
            if (me.row < players[i].row) gotodir = DOWN;
            else gotodir = UP;
        }
    }
    char *moves = new char[6];
    moves[5] = 0;
    fprintf(flog, "%d\n", best+1);
    fflush(flog);
    int col = me.col;
    int row = me.row;
    int dir = me.dir;
    int upto = 0;
    while (row > gotor && upto < me.health) {
        moves[upto++] = MOVE_UP;
        row--;
    }
    while (row < gotor && upto < me.health) {
        moves[upto++] = MOVE_DOWN;
        row++;
    }
    while (col > gotoc && upto < me.health) {
        moves[upto++] = MOVE_LEFT;
        col--;
    }
    while (col < gotoc && upto < me.health) {
        moves[upto++] = MOVE_RIGHT;
        col++;
    }
    if (turnLeft(me.dir) == gotodir && upto < me.health) {
        moves[upto++] = MOVE_TURN_LEFT;
    }
    else if (turnLeft(turnLeft(me.dir)) == gotodir && upto+1 < me.health) {
        moves[upto++] = MOVE_TURN_LEFT;
        moves[upto++] = MOVE_TURN_LEFT;
    }
    else if (turnRight(me.dir) == gotodir && upto < me.health) {
        moves[upto++] = MOVE_TURN_RIGHT;
    }
    while (upto < me.health) {
        moves[upto++] = MOVE_SHOOT;
    }
    fprintf(flog, "%s\n", moves);
    fflush(flog);
    makeMoves(moves);
}

void clientDoTurn() {
    numTurns++;
    if (numTurns < 10 || rand() % (50/numTurns+1) == 0) offensive();
    else defensive();
    clearThings();
}
