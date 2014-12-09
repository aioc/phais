#include "rally.h"
#include "assert.h"

#define NAME "PatBot"


extern int board_size;
extern int number_of_players;
extern player *players;

char *get_name() {
	return NAME;
}

//return our health
int gethealth() {

	int i;
	for (i=0; i<=MAX_PLAYERS; i++) {
		if (!strcmp(players[i].name, NAME)) {
			return players[i].health;
		}
	}

	// should never get to here
	assert(0);
	return -1;
}

// return 1 if i represents me, otherwise 0
int isme(int i) {
	if (!strcmp(players[i].name, NAME)) {
		return 1;
	} else {
		return 0;
	}
}

//return the row we're in
int getrow() {

	int i;
	for (i=0; i<=MAX_PLAYERS; i++) {
//		printf("getrow: looking at player %s, i=%d, n=%d\n", players[i].name, i, number_of_players);
		if (!strcmp(players[i].name, NAME)) {
			return players[i].r;
		}
	}

	// should never get to here
	assert(0);
	return -1;
}

// returns the column we're in
int getcol() {

	int i;
	for (i=0; i<=MAX_PLAYERS; i++) {
//		printf("getrow: looking at player %s, i=%d, n=%d\n", players[i].name, i, number_of_players);
		if (!strcmp(players[i].name, NAME)) {
			return players[i].c;
		}
	}

	// should never get to here
	assert(0);
	return -1;
}

// returns the direction we're facing
direction getdir() {

	int i;
	for (i=0; i<=MAX_PLAYERS; i++) {
//		printf("getrow: looking at player %s, i=%d, n=%d\n", players[i].name, i, number_of_players);
		if (!strcmp(players[i].name, NAME)) {
			return (direction) players[i].dir;
		}
	}

	// should never get to here
	assert(0);
	return (direction)-1;
}

int isplayer(int i) {
	if ((strlen(players[i].name) > 0) && (players[i].health > 0)) {
		return 1;
	} else {
		return 0;
	}
}

char* get_moves() {
	// Return a string of MOVES_PER_ROUND characters specifying the sequence of
	// moves
	int i, j;

	// if current row != top move to the top
	if (getrow() != 0) {
		if (getdir() == WEST) {		// we're facing west
			return ")SSSS";			// turn north and fire!
		} else if (getdir() == SOUTH) {	// we're facing south
			return "))SSS";			// turn north and fire!
		} else if (getdir() == EAST) {
			return "(SSSS";			// turn south and fire
		} else {					// we're facing north
			return "^^^^^";
		}
	}

	// we're on the top row. is anyone else on the top row?
	for (i=0; i<MAX_PLAYERS; i++) {
		if (isme(i)) {			// don't try and fire at ourself
			continue;
		}
		if (isplayer(i)) {
			if (players[i].r == 0) {	// yes
				printf("Firing at %s in top row\n", players[i].name);
				if (players[i].c < getcol()) {	// they're west of us
					if (getdir() == WEST) {		// we're facing west
						return "SSSSS";			// fire!
					} else if (getdir() == SOUTH) {	// we're facing south
						return ")SSSS";			// turn west and fire!
					} else if (getdir() == EAST) {
						return "))SSS";			// turn west and fire!
					} else {
						return "(SSSS";			// turn west and fire
					}
				} else {						// they're east of us
					if (getdir() == WEST) {		// we're facing west
						return "((SSS";			// turn east and fire!
					} else if (getdir() == SOUTH) {	// we're facing south
						return "(SSSS";			// turn east and fire!
					} else if (getdir() == EAST) {
						return "SSSSS";			// fire!
					} else {
						return ")SSSS"; 		// turn east and fire
					}
				}
			}
		}
	}
	
	// noone else is alive in the top row. make sure we're facing south
	printf("noone in top row. Facing south\n");
	if (getdir() == WEST) {		// we're facing west
		return "(SSSS";			// turn south and fire!
	} else if (getdir() == EAST) {
		return ")SSSS";			// turn south and fire
	} else if (getdir() == NORTH) {					// we're facing north
		return "((SSS";			// turn south and fire
	}

	printf("...done, dir=%d\n", getdir());
	fflush(stdout);
	
	// find the person in the closest column to us
	direction bestdir = WEST;
	int bestdistance = board_size;
	int bestplayer;
	for (j=0; j<=MAX_PLAYERS; j++) {
		if (isme(j)) {			// don't try and fire at ourself
			continue;
		}
		if (isplayer(j)) {
			int diff;
			diff = players[j].c - getcol();
			if (diff < 0) {
				diff = -diff;
				if (diff < bestdistance) {
					bestdistance = diff;
					bestdir = WEST;
					bestplayer = j;
				}
			} else if (diff > 0) {
				if (diff < bestdistance) {
					bestdistance = diff;
					bestdir = EAST;
					bestplayer = j;
				}
			} else { 			// someone in our column!
				return "SSSSS";
			}
		}
	}

	// if we get to here:
	// 	- we're facing south
	// 	- there is noone in our row or column
	// 	- we're in the top row
	// 	- we have two variables specifying the best distance and direction to move
	assert(getdir() == SOUTH);
	assert(getrow() == 0);

	printf("Moving towards %s\n", players[bestplayer].name);

	// move to the column where this person is and fire
	static char retval[6];
	for (i=0; i<5; i++) {
		if (bestdistance > 0) {
			if (bestdir == WEST) {
				retval[i] = '<';
			} else {
				retval[i] = '>';
			}
			bestdistance--;
		} else {
			retval[i] = 'S';
		}
	}

	return retval;
}

void get_ready() {
	// Perform any initalisation here
	printf("woo!\n");
}

