#include <stdlib.h>
#include "rally.h"

char *get_name() {
	return "Alice";
}

char* get_moves() {
	static char moves[MOVES_PER_ROUND+1];
	int i;
	for (i = 0; i < MOVES_PER_ROUND; i++) {
		switch(random()%8) {
			case 0:
				moves[i] = MOVE_NORTH;
				break;
			case 1:
				moves[i] = MOVE_SOUTH;
				break;
			case 2:
				moves[i] = MOVE_EAST;
				break;
			case 3:
				moves[i] = MOVE_WEST;
				break;
			case 4:
				moves[i] = MOVE_TURN_LEFT;
				break;
			case 5:
				moves[i] = MOVE_TURN_RIGHT;
				break;
			case 6:
				moves[i] = MOVE_PUNCH;
				break;
			case 7:
				moves[i] = MOVE_SHOOT;
				break;
		}
	}
	return moves;
}

void get_ready() {
}

