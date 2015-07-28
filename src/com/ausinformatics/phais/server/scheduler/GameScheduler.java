package com.ausinformatics.phais.server.scheduler;

import java.util.List;

import com.ausinformatics.phais.server.Config.Mode;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

/**
 * Handles the scheduling of new games
 */

public interface GameScheduler {

	/**
	 * Adds a player to be returned in a game
	 * 
	 * @param player the player to be added
	 */
	void addPlayer(PersistentPlayer player);

	/**
	 * Removes players from being schedulable
	 * 
	 * @param player the player to be removed
	 */
	void removePlayer(PersistentPlayer player);

	/**
	 * Returns true if there are games waiting
	 * 
	 * @return true if there are games waiting
	 */
	boolean hasGame();

	/**
	 * Returns the next scheduled game
	 * 
	 * @return a List of players in the game. If no game is available, returns
	 * null.
	 */
	List<PersistentPlayer> getGame();

	/**
	 * Schedules a game to be returned on next call of getGame()
	 * 
	 * @param players the players to be involved in the game
	 */
	void scheduleGame(List<PersistentPlayer> players);

	/**
	 * Returns the type of scheduler this is
	 * 
	 * @return the type of scheduler this is
	 * 
	 * @see Mode
	 */
	Mode getMode();
	
	/**
	 * Returns the number of players to be scheduled per game
	 * 
	 * @return the number of players to be scheduled per game
	 */
	int getNumPlayersPerGame();
	
	/**
	 * Returns all players currently in the scheduler, and removes them
	 * 
	 * @return the players waiting to be scheduled
	 */
	List<PersistentPlayer> removeWaitingPlayers();
}
