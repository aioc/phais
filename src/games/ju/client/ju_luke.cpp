#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "ju.h"

typedef struct _state *state;

struct _state {
   int myAvail;
   int theirAvail;
   int curRound;
};


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

struct _state curState;

int estimate (state s);
int findMove (state s, int depth);

void clientRegister(void) {
   setName("Obumd");
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
   state s = &curState;
   s->curRound = curRound;
   s->myAvail = 0;
   s->theirAvail = 0;
   for (int i = 0; i < amountRounds; i++) {
      if (cardsLeft[myID][i]) {
         s->myAvail |= (1 << i);
      }
      if (cardsLeft[1 - myID][i]) {
         s->theirAvail |= (1 << i);
      }
   }
   findMove (s, 1);
   playCard (bestMove);
   curRound++;
   // Done
}

int findMove (state s, int depth) {

   if (s->curRound == amountRounds) {
      return 0;
   }
   if (depth == 0) {
      return estimate(s);
   }
   int best = -100000000;
   int startV = toPlay[s->curRound] - 2;
   int endV = startV + 5;
   int exists = FALSE;
   for (int i = startV; i < endV && !exists; i++) {
      if (s->myAvail & (1 << i)) {
         exists = TRUE;
      }
   }
   if (!exists) {
      startV = 0;
      endV = amountRounds;
   }
   for (int i = startV; i < endV; i++) {
      if (s->myAvail & (1 << i)) {
         int total = 0;
         int enS = i - 2;
         int enE = i + 3;
         exists = FALSE;
         for (int j = enS; j < enE && !exists; j++) {
            if (s->theirAvail & (1 << j)) {
               exists = TRUE;
            }
         }
         if (!exists) {
            total += (amountRounds - s->curRound) * 3;
            enS = 0;
            enE = amountRounds;
         }
         int numC = 0;
         for (int j = enS; j < enE; j++) {
            if (s->theirAvail & (1 << j)) {
               s->myAvail ^= (1 << i);
               s->theirAvail ^= (1 << j);
               s->curRound++;
               total += findMove (s, depth - 1);
               s->myAvail ^= (1 << i);
               s->theirAvail ^= (1 << j);
               s->curRound--;
               if (i < j) {
                  total -= roundValues[s->curRound];
               } else if (j < i) {
                  total += roundValues[s->curRound];
               }
               numC++;
            }
         }
         total /= (numC);
         if (total > best) {
            best = total;
            bestMove = i;
         }
      }
   }
   return best;
}

int estimate (state s) {
   // Work out how many of the pairs of cards I win. 
   int iWin = 0;
   int total = 0;
   for (int i = 0; i < amountRounds; i++) {
      if (s->myAvail & (1 << i)) {
         for (int j = 0; j < amountRounds; j++) {
            if (s->theirAvail & (1 << j)) {
               if (i > j) {
                  iWin++;
               }
               total++;
            }
         }
      }
   }
   int avg = 0;
   for (int k = 0; k < 30; k++) {
      int totalS = 0;
      for (int i = s->curRound; i < amountRounds; i++) {
         if (rand () % total < iWin) {
            totalS += roundValues[i];
         }
      }
      avg += totalS;
   }
   return avg / 30;
}


