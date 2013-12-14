#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "ju.h"

int numPlayers;
int amountRounds;
int myID;
int roundValues[MAX_ROUNDS];
int cardsLeft[MAX_PLAYERS][MAX_ROUNDS];
int pointsOn[MAX_PLAYERS];
int curRound;
int curValueForRound;

void clientRegister(void) {
   imHuman();
   char buffer[100];
   printf ("Please enter your name: ");
   scanf ("%s", &buffer[0]);
   setName(buffer);
   printf ("Please enter your colour (r g b): ");
   int r, g, b;
   scanf ("%d %d %d", &r, &g, &b);
   setColour(r, g, b);
}
void clientInit(int playerCount, int numRounds, int playerID, int *values) {
   numPlayers = playerCount;
   amountRounds = numRounds;
   myID = playerID;
   for (int i = 0; i < amountRounds; i++) {
      roundValues[i] = values[i];
   }
   curRound = 0;
   printf ("A game has started with %d players, %d rounds.\n", numPlayers, amountRounds);
   printf ("The point values of each round are:\n\t\t");
   for (int i = 0; i < amountRounds; i++) {
      printf ("%.2d ", roundValues[i]);
   }
   printf ("\n");
}

void clientCardsLeft(int pid, int *cards) {
   for (int i = 0; i < amountRounds; i++) {
      cardsLeft[pid][i] = cards[i];
   }
}

void clientRoundValue(int val) {
   curValueForRound = val;
}

void clientPoints(int pid, int points) {
   pointsOn[pid] = points;
}

void clientDoTurn() {
   // Print out relevent information
   printf ("Round %d\n", curRound + 1);
   for (int i = 0; i < curRound; i++) {
      printf ("   ");
   }
   printf (" v\n");
   for (int i = 0; i < amountRounds; i++) {
      printf ("%.2d ", roundValues[i]);
   }
   printf ("\n");
   printf ("These are the cards left:\n");
   printf ("Your  hand (%5d):", pointsOn[myID]);
   for (int i = 0; i < amountRounds; i++) {
      if (cardsLeft[myID][i]) {
         printf (" %.2d", i + 1);
      }
   }
   printf ("\n");
   int seenMe = FALSE;
   for (int i = 0; i < numPlayers; i++) {
      if (i != myID) {
         int theirID = i + 1;
         if (seenMe) {
            theirID--;
         }
         printf ("Opponent %d (%5d):", theirID, pointsOn[i]);
         for (int j = 0; j < amountRounds; j++) {
            if (cardsLeft[i][j]) {
               printf (" %.2d", j + 1);
            }
         }
         printf ("\n");
      } else {
         seenMe = TRUE;
      }
   }
   int val = -1;
   while (val < 1 || val > amountRounds || !cardsLeft[myID][val - 1]) {
      printf ("Please enter the card value you wish to play: ");
      scanf ("%d", &val);
   }
   playCard (val - 1);
   curRound++;
   // Done
}

