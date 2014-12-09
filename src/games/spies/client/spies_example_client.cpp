#include <cstdio>
#include <cstdlib>
#include "spies.h"

static int myHealth;
static int myID;

void clientRegister() {
    setName("Dumbo-tutor");
}

/*
 *   This is called when the game is about to begin. It tells you the number
 *   of players in the game, how big the board is, the starting health of
 *   all players, and your playerID for the game (0 <= playerID < numPlayers).
 *   You are not required to call anything in here.
 */
void clientInit(int numPlayers, int boardSize, int startingHealth, int playerID) {
    myHealth = startingHealth;
    myID = playerID;
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
    if (pid == myID) {
        myHealth = health;
    }
}


/*
 *   This is called when it is time for you to take your turn. While in the
 *   function, you must call makeMoves once. If you make multiple calls to
 *   makeMove, only the final call will be considered.
 */
void clientDoTurn() {
    char moves[MAX_STARTING_HEALTH];
    char pos_moves[] = "^<>v()S-";
    for (int i = 0; i < MAX_STARTING_HEALTH; i++) {
        moves[i] = MOVE_NOTHING;
    }
    for (int i = 0; i < myHealth; i++) {
        moves[i] = pos_moves[rand() % 8];
    }
    makeMoves(moves);
}


