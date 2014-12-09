#ifndef _RALLY_H_
#define _RALLY_H_

#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <signal.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#include "player.h"

#define MOVE_NORTH MOVE_UP
#define MOVE_SOUTH MOVE_DOWN
#define MOVE_WEST MOVE_LEFT
#define MOVE_EAST MOVE_RIGHT
#define MOVE_PUNCH 'P'

char *get_name();
char* get_moves();
void get_ready();


#ifdef __cplusplus
}
#endif
#endif /* _RALLY_H_ */
