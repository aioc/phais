package core.runner;

import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;

public interface GameRunner extends Runnable {

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

	/**
	 * Returns a mapping of players to scores. Scores may be negative. This
	 * method will only be called if isFinished() returns true
	 * 
	 * @return the mapping
	 */
	public Map<PersistentPlayer, Integer> getResults();
	
	/**
	 * Returns whether you can restart the game with this runner
	 * @return if you can restart the game
	 */
	public boolean canRestart();
	
	/**
	 * Restarts the runner with a new game
	 * @param game the game that is to be run
	 * @param players the players in the game
	 */
	public void restart(GameInstance game, List<PersistentPlayer> players);

}
