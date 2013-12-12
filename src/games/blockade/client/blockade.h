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

// Limits of certain restrictions
#define NUM_PLAYERS           2
#define MAX_SIZE              50


   /////////////////////////////////////////////////////////////////////


   /////////////////////////////////////////////////////////////////////
   // The following must be implemented by the client:

   /*
    *   This is called when your client connects to the server. You need to provide
    *   a name using setName and a colour with setColour.
    */

   void clientRegister();


   /*
    *   This is called when the game is about to begin. It tells you how big the board is,
    *   and your playerID. Your playerID will be 0 or 1.
    *   You are not required to call anything in here.
    */
   void clientInit(int boardSize, int playerID);

   // ******** These functions will be called *BEFORE* you take a turn ********
   /*
    *   This is called once for *each* player in the game, telling you their position.
    *   pid will contain their playerID, and x and y will be their coordinates on the
    *   grid (0 <= x < boardSize, 0 <= y < boardSize).
    *   If pid is your playerID, this will be your position, else it will be your
    *   opponenet.
    */
   void clientPlayerPosition(int pid, int x, int y);
   
   /*
    *   This is called whenever a square becomes blocked (by a player blocking it). pid
    *   is the playerID of the player blocking it (0 or 1), and x and y will be its coordinates
    *   on the grid (0 <= x < boardSize, 0 <= y < boardSize).
    */
   void clientSquareIsBlocked(int pid, int x, int y);

   /*   
    *   As a note, clientPlayerPosition is called *BEFORE* clientSquareIsBlocked, and both
    *   are called before your turn.
    */

   /*
    *   This is called when it is time for you to take your turn. While in the function, you must
    *   call either movePosition or blockSquare once. If you call multiple ones of these, only
    *   the last will be sent to the server.
    */
   void clientDoTurn();

   
   /////////////////////////////////////////////////////////////////////
   // The following are available to the client:

   /*
    *   This will send to the server what you want your name to be. It must only contain
    *   A-Z, a-z, 1-9 and _.
    */
   void setName(const char* name);
   
   /*
    *   This will send to the server what colour you want to be. The values must be 0-255.
    */
   void setColour(int r, int g, int b);

   /*   
    *   This will tell the server that you want to move in a certain direction. dir should be one
    *   of UP, RIGHT, DOWN or LEFT
    */
   void movePosition (int dir);
   
   /*
    *   This will tell the server that you want to block a certain square. x and y must be on the
    *   grid (0 <= x < boardSize, 0 <= y < boardSize).
    */
   void blockSquare (int x, int y);
   


   /////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
