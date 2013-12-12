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

// Limits of certain restrictions
#define MAX_BOARD_WIDTH       11
#define MAX_BOARD_HEIGHT      10
#define MAX_NAME_LENGTH       16

   /////////////////////////////////////////////////////////////////////
   /////////////////////////////////////////////////////////////////////

   // The following must be implemented by the client:

   /*
    *   This is called when your client connects to the server. You need to
    *   provide a name using setName.
    */
   void clientRegister();

   /*
    *   This is called when the game is about to begin. It tells you how big the board is
    *   and your playerID for the game (0 or 1).
    *   You are not required to call anything in here.
    */
   void clientInit(int boardHeight, int boardWidth, int playerID);

   
   // ******** This function will be called *BEFORE* players take their turns for the round ********

   /*
    *   This is called once for every move made. Tells you what move it was, by playerID
    *   and column the move was made.
    */
   void clientMoveMade(int pid, int column);


   /*
    *   This is called when it is time for you to take your turn. While in the
    *   function, you must call makeMove once.
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
    *   This will tell the server what column you wish to put the piece in.
    */
   void makeMove(int column);

   /////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
