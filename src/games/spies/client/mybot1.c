#include <stdio.h>
#include <string.h>
#include "player.h"

char *get_name() {
	return "brbBot";
}

char* get_moves() {
	static char s[6] = ">>>>>";
	printf("row %d, column %d\n", players[player_number].r, players[player_number].c);
	if (players[player_number].r == 0) {
		if (players[player_number].c == 0)
			strcpy(s, "(vSSS");
		else {
			strcpy(s, "<SSSS");
			if (players[player_number].dir != SOUTH)
				s[2] = '(';
		}
	} else if (players[player_number].c == 0) {
		if (players[player_number].r == board_size-1)
			strcpy(s, "(>SSS");
		else {
			strcpy(s, "vSSSS");
			if (players[player_number].dir != EAST)
				s[2] = '(';
		}
	} else if (players[player_number].r == board_size-1) {
		if (players[player_number].c == board_size-1)
			strcpy(s, "(^SSS");
		else {
			strcpy(s, ">SSSS");
			if (players[player_number].dir != NORTH)
				s[2] = '(';
		}
	} else if (players[player_number].c == board_size-1) {
		if (players[player_number].r == 0)
			strcpy(s, "(<SSS");
		else {
			strcpy(s, "^SSSS");
			if (players[player_number].dir != WEST)
				s[2] = '(';
		}
	}
	/* printf("Bot %d at %d, %d\n", player_number, players[player_number].r, players[player_number].c); */
	return s;
}

void get_ready() {
}

