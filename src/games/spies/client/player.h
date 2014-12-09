#ifndef _PLAYER_H_
#define _PLAYER_H_

#include "spies.h"
#ifdef __cplusplus
extern "C" {
#endif

#define MOVES_PER_ROUND		5

#define MAX_CLIENTS 25
#define MAX_PLAYERS 20

#define STARTING_HEALTH		5

typedef enum {
	NORTH = 0,
	EAST,
	SOUTH,
	WEST,
} direction;

typedef struct {
	int r, c, dir, health;
	char name[15];
} player;

int player_number;
int number_of_players;
int board_size;
int board[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
player *players;

#ifdef __cplusplus
}
#endif

#endif /* _PLAYER_H_ */
