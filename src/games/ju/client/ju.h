#ifndef __PHAIS_H
#define __PHAIS_H

#ifdef __cplusplus
extern "C" {
#endif
#include <math.h>
   /////////////////////////////////////////////////////////////////////

#define TRUE                  1
#define FALSE                 0

   // Limits of certain inputs
#define MAX_PLAYERS           2
#define MAX_ROUNDS            100
#define MIN_ROUND_VALUE       1
#define MAX_ROUND_VALUE       99
   /////////////////////////////////////////////////////////////////////


   /////////////////////////////////////////////////////////////////////
   // The following must be implemented by the client:

   /*
    *   This is called when your client connects to the server. You need to
    *   provide a name using setName.
    */

   void clientRegister();


   /*
    *   This is called when the game is about to begin. It tells you how many
    *   players there are in playerCount, the number of rounds, your player ID,
    *   and the value of each round. Your playerID will be [0,playerCount). You
    *   are not required to call anything in here.
    */
   void clientInit(int playerCount, int numRounds, int playerID, int *values);

   /*
    *   This is called for each player currently in the game once before your
    *   turn.  It tells you information about a player (playerID), in
    *   particular the cards they have left to play. The array cards is a
    *   boolean array, with TRUE in the ithCards spot if the card numbered i is
    *   still left to play.  Cards are between [0,numRounds) and playerID
    *   between [0,playerCount).
    */
   void clientCardsLeft(int playerID, int *cards);
   
   /*
    *   This is called before the start of your turn, telling you how many
    *   points each player has.  playerID is [0,playerCount).
    */
   void clientPoints(int playerID, int points);

   /*
    *   This is called before you take your turn, telling you the value of the
    *   round.
    */
   void clientRoundValue(int val);

   // As a note, clientRoundValue() is called BEFORE clientCardsLeft() which is
   // called BEFORE clientPoints()

   /*
    *   This is called when it is time for you to take your turn. While in the
    *   function, you must call playCard at most once. The move will be sent to
    *   the server automatically when you return.
    */
   void clientDoTurn();

   // The following are available to the client:

   /*
    *   This will send to the server what you want your name to be. It must
    *   only contain A-Z, a-z, 1-9 and _.
    */
   void setName(const char* name);
   
   /*
    *   This will send to the server what colour you want to be. The values
    *   must be [0,256).
    */
   void setColour(int r, int g, int b);

   /*
    *   Sends a move to the server. You specify the value of the card. You will
    *   be dropped if the card is invalid (that is, if you have already played
    *   it, or it is an invalid card). The value must be [0-numRounds).
    */
   void playCard(int cardVal);
   
   /*
    * Tells the client library that the bot is actually a human. This SHOULD
    * NOT be called in other cases, as if your bot infinite loops, they will
    * not be terminated by the client (meaning, you won't get feedback). This
    * does not change how quickly the server times you out. You should call
    * this function in clientRegister, as the first thing you do.
    */
   void imHuman();


   /////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
