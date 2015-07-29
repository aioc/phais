package com.ausinformatics.phais.server.runner;

import java.util.List;
import java.util.Map;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.spectators.Spectator;

public interface GameRunner{

    public void start(String name);
    
	/**
	 * Returns if the game is finished or not
	 * 
	 * @return if the game is finished or not
	 */
	public boolean isFinished();

	/**
	 * Returns the list of players that are in the game
	 * 
	 * @return the list of players
	 */
	public List<PersistentPlayer> getPlayers();

    public List<Spectator> getSpectators();

	/**
	 * Returns a mapping of players to scores. Scores may be negative. This
	 * method will only be called if isFinished() returns true
	 * 
	 * @return the mapping
	 */
	public Map<PersistentPlayer, Integer> getResults();
}
