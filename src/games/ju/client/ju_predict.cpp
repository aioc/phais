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
int totalDiff;

void theyPlayed(int val);
int predict();

void clientRegister(void) {
   setName("Telepath");
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
   totalDiff = 0;
}

void clientCardsLeft(int pid, int *cards) {
   for (int i = 0; i < amountRounds; i++) {
      if (curRound > 0) {
         if (cards[i] != cardsLeft[pid][i] && pid != myID) {
            theyPlayed(i);
         }
      }
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
   int theirMove = predict();
   bestMove = -1;
   for (int i = theirMove + 1; i < toPlay[curRound] + 3 && i < amountRounds; i++) {
      if (cardsLeft[myID][i]) {
         bestMove = i;
         break;
      }
   }
   if (cardsLeft[myID][theirMove] && bestMove < 0) {
      bestMove = theirMove;
   }
   if (bestMove < 0) {
      for (int i = 0; i < amountRounds; i++) {
         if (cardsLeft[myID][i]) {
            bestMove = i;
            break;
         }
      }
   }
   playCard (bestMove);
   curRound++;
   // Done
}



void theyPlayed(int val) {
   int should = toPlay[curRound - 1];
   totalDiff += (val - should) / 2;
}

int predict() {
   return toPlay[curRound] + totalDiff;
}




