package com.ausinformatics.phais.core.visualisation;

import java.util.Map;

import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

/**
 * This interface can be implemented by the game maker, when visualisation is
 * handled by another visualisation program
 * <p />
 * Used to represent the entirety of a game instance, including the game state,
 * the main game loop, victory and defeat conditions, etc.
 */

public interface GameHandler {

	/**
	 * This method will be called when the game is to begin. The main game loop
	 * should be executed while inside this method.
	 * <p />
	 * Returning from this function signifies the end of the game, and so the
	 * results of the games should be finalised before returning.
	 */
	public void begin();

	/**
	 * This method should return a map of PersistentPlayers to an integer score.
	 * There should be an entry for every player in the game.
	 * <p />
	 * This function is guaranteed to only be called <i>after</i> begin() has
	 * returned.
	 * 
	 * @return a map of players to their respective scores
	 */
	public Map<PersistentPlayer, Integer> getResults();
}
