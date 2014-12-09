#include "rally.h"
#include "stdlib.h"
#include "string.h"

#define min(a,b) ((a) < (b) ? (a) : (b))
#define abs(a) ((a) > 0 ? (a) : -(a))

char *get_name() {
	return "meeeeooow";
	return "lol just kidding im not a cat";
	return "get it? lol... cat?";
	return "lol lolcat";
}

char* get_moves() {
	int closest, closestid;
	int i;
	closest = board_size + 20;
	static char ret[16];

	for (i = 0; i < number_of_players; i++) {
		if (i == player_number) {continue;}
		int tmp = min(abs(players[player_number].r-players[i].r),abs(players[player_number].c-players[i].c));
		if (tmp < closest) {
			closest = tmp;
			closestid = i;
		}
	}
	printf("New call with %d %d\n",players[player_number].r-players[closestid].r,players[player_number].c-players[closestid].c);
	if (5 <= closest) {
		return "RRRRR"; //meeeeeeooooooow
	} else if (3 <= closest && closest <= 4) {
		if (rand() % 2 == 0) {
			return "RRRRR"; //meeeeeeeeoooooooooowwww
		} else {
			return "<v>^S";
		}
	} else if (closest == 2) {
		if (rand() % 3 == 0) {return "RRRRR";}
		return "<v>^S";
	} else {
		//work out which direction the other guy is in
		int dir;
		int j;
		
		if (rand() % 5 == 0) {return "RRRRR";}

		if (abs(players[player_number].r-players[closestid].r) > abs(players[player_number].c-players[closestid].c)) {
			dir = players[player_number].r > players[closestid].r ? NORTH : SOUTH;
		} else {
			dir = players[player_number].c > players[closestid].c ? WEST : EAST;
		}
		
		//work out which way we need to turn
		for (j = 0; j < 5; j++) {ret[j] = 'S';}
		j = 0;	
		
		printf("At %d, want %d\n",players[player_number].dir,dir);
		
		int pleaseturn = (dir + 3*players[player_number].dir) % 4;
		fflush(stdout);
		if (pleaseturn == 1) {
			ret[0] = ')';
			j = 1;
		} else if (pleaseturn == 3) {
			ret[0] = '(';
			j = 1;
		} else if (pleaseturn == 2) {
			ret[0] = ret[1] = '(';
			j = 2;
		} 
		//spam the attack button

		int distance = abs(players[player_number].r-players[i].r) + abs(players[player_number].c-players[i].c);
		if (distance > 2) {
			for (; j < 5; j++) {ret[j] = 'S';}
		} else {
			for (; j < 5; j++) {ret[j] = 'P';}
		}
		return ret;
	}
}

void get_ready() {
}


