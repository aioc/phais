package com.ausinformatics.phais.server.scorer;

import java.util.Map;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public interface ScoreKeeper {

	/*
	 * Register a player to count in the score keeper
	 * 
	 * @param player the player to register
	 */
	void registerPlayer(PersistentPlayer player);

	/*
	 * Deregister a player so that it no longer counts in the score keeper
	 * 
	 * @param player the player to deregister
	 */
	void deregisterPlayer(PersistentPlayer player);

	/*
	 * Submit the results of a game
	 * 
	 * @param result a map of players to the scores that the players received.
	 * Should only have maps of players that were directly involved in the game,
	 * and whose presence in the game was necessary.
	 */
	void submitGame(Map<PersistentPlayer, Integer> result);

	/*
	 * Get the score for a given player
	 */
	int getScore(PersistentPlayer player);
}
