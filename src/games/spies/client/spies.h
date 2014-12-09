#ifndef __PHAIS_H
#define __PHAIS_H

#ifdef __cplusplus
extern "C" {
#endif
#include <math.h>
   /////////////////////////////////////////////////////////////////////

// Constants used
#define TRUE                  1
#define FALSE                 0

#define UP                    0
#define RIGHT                 1
#define DOWN                  2
#define LEFT                  3

#define MOVE_UP               '^'
#define MOVE_RIGHT            '>'
#define MOVE_DOWN             'v'
#define MOVE_LEFT             '<'

#define MOVE_SHOOT            'S'
#define MOVE_TURN_LEFT        '('
#define MOVE_TURN_RIGHT       ')'
#define MOVE_NOTHING          '-'

// Limits of certain restrictions
#define MAX_STARTING_HEALTH   10
#define MAX_NUM_PLAYERS       28
#define MAX_BOARD_SIZE        20
#define MAX_NAME_LENGTH       16

   /////////////////////////////////////////////////////////////////////
   /////////////////////////////////////////////////////////////////////

   // The following must be implemented by the client:

   /*
    *   This is called when your client connects to the server. You need to
    *   provide a name using setName and a colour with setColour.
    */
   void clientRegister();

   /*
    *   This is called when the game is about to begin. It tells you the number
    *   of players in the game, how big the board is, the starting health of
    *   all players, and your playerID for the game (0 <= playerID < numPlayers).
    *   You are not required to call anything in here.
    */
   void clientInit(int numPlayers, int boardSize, int startingHealth, int playerID);

   
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
    *   You are not required to call anything in here.
    */
   void clientPlayerState(int pid, int health, int dir, int r, int c, char *previousMoves);


   /*
    *   This is called when it is time for you to take your turn. While in the
    *   function, you must call makeMoves once. If you make multiple calls to
    *   makeMove, only the final call will be considered.
    */
   void clientDoTurn();

   
   /////////////////////////////////////////////////////////////////////
   // The following are available to the client:

   /*
    *   This will send to the server what you want your name to be. It must
    *   only contain A-Z, a-z, 1-9 and _. The length of the name should be at
    *   most 16 characters (not including the null terminating byte).
    */
   void setName(const char* name);
   
   /*
    *   This will send to the server what colour you want to be. The values
    *   must be 0-255.
    */
   void setColour(int r, int g, int b);

   /*   
    *   This will tell the server that you want to make some sequence of moves.
    *   You should pass in a string with a length of _at least_ startingHealth.
    *   Any characters after the startingHealth-th character will be ignored.
    *   Each of these characters should be one of MOVE_UP, MOVE_RIGHT,
    *   MOVE_DOWN, MOVE_LEFT, MOVE_SHOOT, MOVE_TURN_LEFT, MOVE_TURN_RIGHT or
    *   MOVE_NOTHING. These should correspond to the sequence of moves that you
    *   wish to make.
    *   The number of moves that are not MOVE_NOTHING should be no greater than
    *   the health of your player.
    */
   void makeMoves(char *moves);

   /////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
