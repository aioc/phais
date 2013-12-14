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
   setName("Dumbo2000");
   setColour(128, 128, 128);
}
void clientInit(int playerCount, int numRounds, int playerID, int *values) {
   numPlayers = playerCount;
   amountRounds = numRounds;
   myID = playerID;
   for (int i = 0; i < amountRounds; i++) {
      roundValues[i] = values[i];
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


void clientDoTurn() {
   int roundsLeft = amountRounds - curRound;
   int cardToFind = rand() % roundsLeft;
   // We are going to find a random card, by counting down until cardToFind is zero
   int cardToPlay = 0;
   for (int i = 0; i < amountRounds; i++) {
      if (cardsLeft[myID][i]) {
         if (cardToFind == 0) {
            cardToPlay = i;
         }
         cardToFind--; // One less to find
      }
   }
   playCard (cardToPlay);
   curRound++;
   // Done
}

