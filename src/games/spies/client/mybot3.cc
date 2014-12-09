#include "rally.h"

char *get_name() {
	return "Elisa";
}

char* get_moves() {
	static int foo = 0;
	if (!foo) {
		foo = 1;
		return "^^^^v";
	}
	return "S(S(-";
}

void get_ready() {
}

