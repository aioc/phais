#include <stdio.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>
#include <signal.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include "ju.h"

// Globals
static const char* SERVER = "127.0.0.1";
static const int PORT = 12317;
static const char* VERSION = "v1.0";
static const int END_TURN_ID = 0;
static int app_argc;
static char** app_argv;


static int colour_r, colour_g, colour_b;
static char names[17];
static int isHuman;
static int pingSock;

// Client state
static const int STATE_INTERNAL = 0;
static const int STATE_REGISTER = 1;
static const int STATE_MAKEMOVE = 2;

static int state = STATE_INTERNAL;


// client functions. Should not need changing

// Server functions. These should be changed to match the protocol


// Game functions


static void ensure_state(int s, const char* fn) {
	if (s != state) {
		fprintf(stderr, "Cannot call function %s at this time (%d != %d)\n", fn, s, state);
		raise(SIGUSR1);
	}
}

///////////////////////////////////
//
//  COMMS
//

// 0 = sucess, 1 = connection is dead
static int client_rx_line(int sock, char* buf, int maxlen) {
	for (int i=0; i < maxlen-1; i++) {
		if (read(sock, buf+i, 1) <= 0) {
			// EOF condition
			return 1;
		}

		if ('\r' == buf[i]) {
			buf[i] = '\0';
		}

		if ('\n' == buf[i]) {
			buf[i] = '\0';
			//printf("Rx(\"%s\")\n", buf);
			return 0;
		}
	}
	buf[maxlen-1] = '\0';
	//printf("Rx(\"%s\")\n", buf);
	return 0;
}

// 0 = success, 1 = connection is dead
static int client_printf(int sock, const char* fmt, ...) {
	char buf[1000];

	va_list ap;
	va_start(ap, fmt);

	vsnprintf(buf, 999, fmt, ap);

	va_end(ap);


	int buflen = strlen(buf);

	int result = write(sock, buf, buflen);

	if (result != buflen) {
		printf("WRITE ERROR OH NOES\n");
		return 1;
	}

	/*
	   const char* errors = "";
	   if (buflen >= 2) {
	   if (buf[buflen-2] != '\r' || buf[buflen-1] != '\n') {
	   errors = " (Improper line termination?)";
	   }
	   buf[buflen-2] = '\0';
	   } else {
	   errors = " (Short line???"")";
	   }
	   printf("Tx(\"%s\"); returns %d%s\n\n", buf, result, errors);
	 */
	return 0;
}

//////////////////////////
//
//  NAME HANDLER
//


static inline int invalid_letter(char l) {
	if (l >= '0' && l <= '9') return 0;
	if (l >= 'a' && l <= 'z') return 0;
	if (l >= 'A' && l <= 'Z') return 0;
	return 1;
}

static void valcpy(char* dest, const char* src) {
	int i=0;

	if (0 == src) {
		dest[0] = '\0';
	}

	while (src[i]) {

		if (i >= 16) break;

		if (invalid_letter(src[i])) {
			dest[i] = '_';
		} else {
			dest[i] = src[i];
		}

		++i;
	}

	dest[i] = '\0';
}

void setName(const char* name) { // CLIENT
	ensure_state(STATE_REGISTER, "setName");
	valcpy(names, name);
}

void imHuman() {
	isHuman = TRUE;
}


static inline int clamp(int v, int low, int high) {
	if (v < low) return low;
	if (v > high) return high;
	return v;
}

void setColour(int r, int g, int b) {
	ensure_state(STATE_REGISTER, "set_colour");

	colour_r = clamp(r, 0, 255);
	colour_g = clamp(g, 0, 255);
	colour_b = clamp(b, 0, 255);
}

static void handle_name(int sock, char* args) {
	colour_r = rand () % 256;
	colour_g = rand () % 256;
	colour_b = rand () % 256;
	names[0] = '\0';
	isHuman = FALSE;
	state = STATE_REGISTER;
	alarm(1);
	clientRegister();
	alarm(0);
	state = STATE_INTERNAL;

	if ('\0' == names[0]) {
		char tbuf[80];
		sprintf(tbuf, "anon%d", rand() % 10000);
		valcpy(names, tbuf);
	}
	client_printf(sock, "NAME %s %d %d %d\r\n", names, colour_r, colour_g, colour_b);
}


//////////////////////////////
//
//  NEWGAME HANDLER
//

static int num_got;
static int *round_values;

static int num_rounds;
static int my_ID;
static int player_count;

static int cur_round;
static int** cards_left;
static int* points;
static int* card_last_played;

static int thekey;

static void handle_newgame(int sock, const char* args) {
	int res = sscanf(args, "%d %d %d %d", &player_count, &num_rounds, &my_ID, &thekey);

	if (res != 4) {
		printf("CLIENT LIBRARY INTERNAL ERROR: "
				"Incorrect arguments on NEWGAME.\n");
	}

	round_values = (int*) calloc (num_rounds, sizeof (int));
	cards_left = (int**) calloc (player_count, sizeof (int*));
	for (int i = 0; i < player_count; i++) {
		cards_left[i] = (int*) calloc (num_rounds, sizeof (int));
		for (int j = 0; j < num_rounds; j++) {
			cards_left[i][j] = TRUE;
		}
	}
	points = (int*) calloc (player_count, sizeof (int));
	card_last_played = (int*) calloc (player_count, sizeof (int));
	num_got = 0;
	cur_round = -1; // Starts out at -1. When we want a move, previous round is handled then incremented
}

static void handle_round(int sock, const char* args) {
	int res = sscanf(args, "%d", &round_values[num_got]);
	if (res != 1) {
		printf("CLIENT LIBRARY INTERNAL ERROR: "
				"Incorrect arguments on ROUND.\n");
	}
	num_got++;
	if (num_got == num_rounds) {
		alarm(1);
		state = STATE_INTERNAL;
		clientInit(player_count, num_rounds, my_ID, round_values);
		alarm(0);
		client_printf(sock, "READY %d\r\n", thekey);
	}
}

// MOVES

static int move_made;
static int value_used;

void playCard(int cardVal) {
	ensure_state(STATE_MAKEMOVE, "move");
	move_made = TRUE;
	value_used = cardVal;
}


static void handle_yourmove(int sock, const char* args) {
	// Check if a round just finished
	if (cur_round >= 0) {
		// Update stuff
		int best_card = 0;
		int num_with_card = 0;
		for (int i = 0; i < player_count; i++) {
			if (card_last_played[i] > best_card) {
				best_card = card_last_played[i];
				num_with_card = 1;
			} else if (card_last_played[i] == best_card) {
				num_with_card++;
			}
		}
		if (num_with_card > 0) {
			int points_worth = round_values[cur_round] / num_with_card;
			for (int i = 0; i < player_count; i++) {
				if (card_last_played[i] == best_card) {
					points[i] += points_worth;
				}
			}
		}
	}
	for (int i = 0; i < player_count; i++) {
		card_last_played[i] = -1;
	}
	cur_round++;

	// tell the client the information got
	move_made = FALSE;
	alarm(1);
	state = STATE_INTERNAL;
	clientRoundValue(round_values[cur_round]);
	for (int i = 0; i < player_count; i++) {
		alarm(1);
		clientCardsLeft(i, cards_left[i]);
	}
	for (int i = 0; i < player_count; i++) {
		alarm(1);
		clientPoints(i, points[i]);
	}
	alarm(1);
	state = STATE_MAKEMOVE;
	clientDoTurn();
	state = STATE_INTERNAL;
	alarm(0);

	if (!move_made) {
		printf("WARNING: Client did not make a move\n");
		raise(SIGUSR1);
	}
	client_printf(sock, "ACTION %d\r\n", value_used);
}

static void handle_used(int sock, const char* args) {
	int player, value;
	int res = sscanf(args, "%d %d", &player, &value);
	if (res != 2) {
		printf("CLIENT LIBRARY INTERNAL ERROR: "
				"Incorrect arguments on USED.\n");
	}
	cards_left[player][value] = FALSE;
	card_last_played[player] = value;
}

static void clear_info() {
	free(round_values);
	for (int i = 0; i < player_count; i++) {
		free(cards_left[i]);
	}
	free(cards_left);
	free(points);
	free(card_last_played);
}

static void handle_gameover(int sock, char* args) {
	clear_info();
	printf("Game over: %s!\n", args);
}


static void handle_error(int sock, char* args) {
	clear_info();
	printf("ERROR: %s\n", args);
}

static void handle_taunt(int sock, char* args) {
   fprintf (stderr, "%s\n", args);
}

//////////////////////////////
//
//  PROTOCOL
//



static void isolate_verb(char* buf, char** args) {
	char* i = buf;
	while (*i && *i != ' ') ++i;

	if ('\0' == *i) {
		// There were no spaces, so no arguments
		*args = i;
	} else {
		// This is a space, arguments follow
		*args = i+1;
	}

	*i = '\0';
}

static int main_loop(int sock) {
	char buf[1000];
	for (;;) {
		//printf("Rx: \"%s\"\n", buf);

		int result = client_rx_line(sock, buf, 999);
		if (result) break;

		char* args;

		//  printf("CMD: \"%s\"\n", buf);

		isolate_verb(buf, &args);

		//printf("ISO: \"%s\"(\"%s\")\n\n", buf, args);

		if (0 == strcasecmp(buf, "NAME")) {
			handle_name(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "NEWGAME")) {
			handle_newgame(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "ROUND")) {
			handle_round(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "YOURMOVE")) {
			handle_yourmove(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "USED")) {
			handle_used(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "GAMEOVER")) {
			handle_gameover(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "ERROR")) {
			handle_error(sock, args);
			continue;
		}

      if (0 == strcasecmp(buf, "TAUNT")) {
         handle_taunt(sock, args);
         continue;
      }

		clear_info();
		printf("Server error: unknown verb \"%s\"\n", buf);
		return 1;

	}
	return 1;
}

////////////////////////////
//
//  CONNECTING
//

static int connect_to_server() {
	int sock;
	int result;
	// Create socket
	sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
	int one = 1;
	setsockopt (sock, IPPROTO_TCP, TCP_NODELAY, &one, sizeof (one));
	if (-1 == sock) {
		perror("Error calling socket()");
		exit(-1);
	}

	char* ip = getenv("SERVERIP");
	int port = PORT;

	int c;
	while ( (c = getopt(app_argc, app_argv, "s:p:")) != -1) {
		if (c == 's') {
			ip = optarg;
		} else if (c == 'p') {
			port = atoi(optarg);
		}
	}

	// Initialise remote address
	struct sockaddr_in ServAddr;
	memset(&ServAddr, 0, sizeof(ServAddr));
	ServAddr.sin_family = AF_INET;
	ServAddr.sin_port = htons(port);
	if (ip) {
		printf("ip = \"%s\"\n", ip);
		result = inet_pton(AF_INET, ip, &ServAddr.sin_addr);
	} else {
		printf("ip = \"%s\"\n", SERVER);
		result = inet_pton(AF_INET, SERVER, &ServAddr.sin_addr);
	}
	if (result < 0) {
		perror("Invalid address family");
		close(sock);
		exit(-1);
	} else if (0 == result) {
		perror("Invalid address");
		close(sock);
		exit(-1);
	}
	// Connect
	if (-1 == connect(sock, (const struct sockaddr*)&ServAddr,
				sizeof(struct sockaddr_in))) {
		perror("Error calling connect()");
		close(sock);
		return -1;
	}

	return sock;
}

///////////////////////////////////////////////
//
//  SEGFAULT CATCHING
//

static void segfaulthandler(int v) {
	printf("Segmentation fault");
	printf(" -- restarting client\n\n\n");
	sleep(1);
	execvp(app_argv[0], app_argv);
	exit(-1);
}

static void siguserhandler(int v) {
	printf("Invalid client operation");
	printf(" -- restarting client\n\n\n");
	sleep(1);
	execvp(app_argv[0], app_argv);
	exit(-1);
}

static void sigalrmhandler(int v) {
	if (!isHuman) {
		printf("\n\n\n\n\nTIME LIMIT EXCEEDED!!!!!\n\n");
		printf(" -- restarting client\n\n\n\n\n");

		execvp(app_argv[0], app_argv);
		exit(-1);
	} else {
		client_printf(pingSock, "PING\r\n");
		alarm(1);
	}
}

static void prepare_signal_handler(int signum, void(*handler)(int)) {
	struct sigaction param;
	memset(&param, 0, sizeof(param));
	param.sa_handler = handler;
	//  param.sa_sigaction = NULL;
	sigemptyset(&(param.sa_mask));
	param.sa_flags = SA_NODEFER | SA_RESTART;
	//param.sa_restorer = NULL;
	sigaction(signum, &param, NULL);
}

////////////////////////////////////////
//
//  MAIN
//

int main(int argc, char** argv) {
	printf("\n\nPHAIS client library %s\n\n", VERSION);

	// Set up crash recovery
	app_argc = argc;
	app_argv = argv;
	prepare_signal_handler(SIGSEGV, &segfaulthandler);
	prepare_signal_handler(SIGUSR1, &siguserhandler);
	prepare_signal_handler(SIGALRM, &sigalrmhandler);
	// Initialise random 
	srand(time(0));
	for (;;) {
		int sock;
		printf("Connecting to server...\n");
		// Connect to server
		for (;;) {
			sock = connect_to_server();
			if (-1 != sock) {
				break;
			}
			printf("   ...failed to connect.\n");
			sleep(1);
			printf("   Retrying...\n");
		}
		printf("Connected!\n");
		prepare_signal_handler(SIGALRM, &sigalrmhandler);
		// Do whatever
		pingSock = sock;
		main_loop(sock);
		// Exit gracefully, for some reason
		printf("Disconnecting\n");
		shutdown(sock, SHUT_RDWR);
		close(sock);
	}
	return 0;
}



