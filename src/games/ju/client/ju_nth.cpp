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

int bestMove;
int sortedRounds[MAX_ROUNDS];
int toPlay[MAX_ROUNDS];

void clientRegister(void) {
   setName("nth");
   setColour(255, 255, 255);
}
void clientInit(int playerCount, int numRounds, int playerID, int *values) {
   numPlayers = playerCount;
   amountRounds = numRounds;
   myID = playerID;
   for (int i = 0; i < amountRounds; i++) {
      roundValues[i] = values[i];
      sortedRounds[i] = i;
   }
   for (int i = 0; i < amountRounds; i++) {
      for (int j = i + 1; j < amountRounds; j++) {
         if (roundValues[sortedRounds[i]] > roundValues[sortedRounds[j]]) {
            int temp = sortedRounds[i];
            sortedRounds[i] = sortedRounds[j];
            sortedRounds[j] = temp;
         }
      }
   }
   for (int i = 0; i < amountRounds; i++) {
      toPlay[sortedRounds[i]] = i;
   }
   curRound = 0;
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

void printState() {
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

}

void clientDoTurn() {
   bestMove = toPlay[curRound];   
   playCard (bestMove);
   curRound++;
   // Done
}

