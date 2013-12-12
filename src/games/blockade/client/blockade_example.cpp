#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "blockade.h"
#define NOT_BLOCKED        -1

int boardSize;
int myID;
int isBlocked[MAX_SIZE][MAX_SIZE];
int playerPos[2][2];

int seen[MAX_SIZE][MAX_SIZE];

int dx[] = {0, 1, 0, -1};
int dy[] = {-1, 0, 1, 0};

void clientRegister(void) {
   setName("Dumbo2000");
   setColour(128, 128, 128);
   srand(time(NULL));
}

void clientInit(int boardS, int playerID) {
   int i, j;
   boardSize = boardS;
   myID = playerID;
   for (i = 0; i < boardS; i++) {
      for (j = 0; j < boardS; j++) {
         isBlocked[i][j] = NOT_BLOCKED;
      }
   }
   // Don't need to reset playerPos, that will be reset for us
}

void clientPlayerPosition(int pid, int x, int y) {
   // Y first, and X next
   playerPos[pid][0] = y;
   playerPos[pid][1] = x;
}

void clientSquareIsBlocked(int pid, int x, int y) {
   isBlocked[y][x] = pid;
}  

int invalidDir (int dir) {
   int newY = playerPos[myID][0] + dy[dir];
   int newX = playerPos[myID][1] + dx[dir];
   if (newX < 0 || newY < 0 || newX >= boardSize || newY >= boardSize) {
      return TRUE;
   }
   if (isBlocked[newY][newX] != NOT_BLOCKED) {
      return TRUE;
   }
   return FALSE;
}

int invalidBlock (int x, int y) {
   int i, j;
   for (i = 0; i < boardSize; i++) {
      for (j = 0; j < boardSize; j++) {
         seen[i][j] = FALSE;
      }
   }
   
}


void clientDoTurn() {
   // Make a random move, or block a random square
   int toDo = rand() % 5; // 0-3 = move, 4 = block
   if (toDo == 4) {
      int x = rand() % boardSize;
      int y = rand() % boardSize;
      while (invalidBlock (x, y)) {
         x = rand() % boardSize;
         y = rand() % boardSize;
      }
      blockSquare (x, y);
   } else { // We are moving
      int dir = toDo;
      while (invalidDir (dir)) {
         dir = rand() % 4;
      }
      movePosition (dir);
   }
   /*
   if (myID == 0) {
      movePosition (DOWN);
   } else {
      movePosition (UP);
   }*/
   // Done
}

