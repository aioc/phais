#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <stdlib.h>
#include <assert.h>
#include <setjmp.h>

#include "rally.h"
#include "spies.h"

extern int move_fd;

extern int board_size;
extern player *players;
extern int player_number;
static char *name;

void clientRegister() {
	players = NULL;
	player_number = -1;
	name = get_name();
	setName(name);
}

void clientInit(int numPlayers, int boardSize, int startingHealth, int playerID) {
	number_of_players = numPlayers;
	player_number = playerID;
	board_size = boardSize;
	if (players != NULL) {
		free(players);
	}
	players = (player*)calloc(sizeof(player), MAX_CLIENTS);
	int i;
	for (i = 0; i < numPlayers; i++) {
		sprintf(players[i].name, "player%d", i);
	}
	sprintf(players[playerID].name, "%s", name);
	get_ready();
}

void clientPlayerState(int pid, int health, int dir, int r, int c, char *previousMoves) {
	players[pid].r = r;
	players[pid].c = c;
	players[pid].dir = dir;
	players[pid].health = health;
}

void clientDoTurn() {
	char moves[MAX_STARTING_HEALTH];
	strcpy(moves, get_moves());
	// Remove extraneous moves by covering with NOPS
	int i;
	int notNops = 0;
	for (i = 0; moves[i] != '\0'; i++) {
		if (moves[i] == 'R' || moves[i] == 'P') {
			moves[i] = '-';
		}
		if (moves[i] != MOVE_NOTHING) {
			notNops++;
			if (notNops > players[player_number].health) {
				moves[i] = MOVE_NOTHING;
			}
		}
	}
	makeMoves(moves);
}

