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

#include "blockade.h"

static int dx[] = {0, 1, 0, -1};
static int dy[] = {-1, 0, 1, 0};

// Globals
static const char* SERVER = "127.0.0.1";
static const int PORT = 12317;
static const char* VERSION = "v1.0";
static const int END_TURN_ID = 0;
static int app_argc;
static char** app_argv;


static int colour_r, colour_g, colour_b;
static char names[17];
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
	int i = 0;

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

static int my_ID;
static int player_count;

static int boardsize;
static int* playerPosX;
static int* playerPosY;

static int thekey;

static void handle_newgame(int sock, const char* args) {
	int res = sscanf(args, "%d %d %d %d", &player_count, &boardsize, &my_ID, &thekey);

	if (res != 4) {
		printf("CLIENT LIBRARY INTERNAL ERROR: "
				"Incorrect arguments on NEWGAME.\n");
	}
   playerPosX = (int*) malloc (sizeof (int) * player_count);
   playerPosY = (int*) malloc (sizeof (int) * player_count);
   playerPosX[0] = boardsize / 2;
   playerPosY[0] = 0;
   playerPosX[1] = boardsize / 2;
   playerPosY[1] = boardsize - 1;   
   state = STATE_INTERNAL;
    alarm(1);
    //clientInit(player_count, boardsize, my_ID);
    clientInit(boardsize, my_ID);
    alarm(0);
    alarm(1);
    clientPlayerPosition(0, playerPosX[0], playerPosY[0]);
    alarm(0);
    alarm(1);
    clientPlayerPosition(1, playerPosX[1], playerPosY[1]);
    alarm(0);
    client_printf(sock, "READY %d\r\n", thekey);
}

static void handle_action(int sock, const char* args) {
    char buf[100];
    int playerid;
    int res = sscanf(args, "%d %s ", &playerid, buf);
    int x, y;
    if (res != 2) {
        printf("CLIENT LIBRARY INTERNAL ERROR: "
                "Incorrect arguments on ACTION.\n");
    }
    if (0 == strcasecmp(buf, "BLOCK")) {
        res = sscanf(args, "%d %s %d %d", &playerid, buf, &y, &x);
        if (res != 4) {
            printf("CLIENT LIBRARY INTERNAL ERROR: "
                    "Incorrect arguments on ACTION BLOCK.\n");
        }
        alarm(1);
        clientSquareIsBlocked(playerid, x, y);
        alarm(0);
    }
    else if (0 == strcasecmp(buf, "MOVE")) {
        int dir;
        res = sscanf(args, "%d %s %d", &playerid, buf, &dir);
        if (res != 3) {
            printf("CLIENT LIBRARY INTERNAL ERROR: "
                    "Incorrect arguments on ACTION MOVE.\n");
        }
        playerPosX[playerid] += dx[dir];
        playerPosY[playerid] += dy[dir];
        clientPlayerPosition(playerid, playerPosX[playerid], playerPosY[playerid]);
    }
}

// MOVES

typedef enum {
    NONE,
    BLOCK,
    MOVE
} move;

static move move_made;
static int blocked_x;
static int blocked_y;
static int direction;

void movePosition(int dir) {
    move_made = MOVE;
    direction = dir;
}

void blockSquare(int x, int y) {
    move_made = BLOCK;
    blocked_x = x;
    blocked_y = y;
}

static void handle_yourmove(int sock, const char* args) {
	// tell the client the information got
	move_made = NONE;
	state = STATE_MAKEMOVE;
	alarm(1);
	clientDoTurn();
	alarm(0);
	state = STATE_INTERNAL;

	if (move_made == NONE) {
		printf("WARNING: Client did not make a move\n");
		raise(SIGUSR1);
	} else if (move_made == BLOCK) {
        client_printf(sock, "ACTION BLOCK %d %d\r\n", blocked_y, blocked_x);
    } else if (move_made == MOVE) {
        client_printf(sock, "ACTION MOVE %d\r\n", direction);
    }
}

static void clear_info() {
	if (playerPosX != NULL) {
		free (playerPosX);
      playerPosX = NULL;
	}
	if (playerPosY != NULL) {
		free (playerPosY);
      playerPosY = NULL;
	}
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

		if (0 == strcasecmp(buf, "ACTION")) {
			handle_action(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "YOURMOVE")) {
			handle_yourmove(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "GAMEOVER")) {
			handle_gameover(sock, args);
			continue;
		}

		if (0 == strcasecmp(buf, "ERROR") || 0 == strcasecmp(buf, "BADPROT")) {
			handle_error(sock, args);
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
	printf("\n\n\n\n\nTIME LIMIT EXCEEDED!!!!!\n\n");
	printf(" -- restarting client\n\n\n\n\n");

	execvp(app_argv[0], app_argv);
	exit(-1);
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
