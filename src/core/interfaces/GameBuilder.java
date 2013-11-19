package core.interfaces;

import java.util.List;

/**
 * This interface needs to be implemented by the game maker.
 * <p />
 * Used for the construction of new GameInstances
 */

public interface GameBuilder {

	/**
	 * Used to construct a game instance given a list of players to be playing
	 * in the game. If the creation of a GameInstance is only calling a
	 * constructor, then little needs to be done here. Otherwise, it would be
	 * appropriate here to:
	 * 
	 * <ul>
	 * <li />Record the creation of the new game,
	 * <li />Fetch extra information required to construct a new game,
	 * <li />Alter the players in any way (as a result of them being in a new
	 * game).
	 * </ul>
	 * 
	 * Note that it is expected that if the game is to be disregarded, that the
	 * players are in a state to immediately play another game, and so any
	 * cleaning up that must occur when, say, a disconnected player is detected
	 * and the game is rendered invalid, should occur here.
	 * 
	 * @param players
	 *            a List of players to be playing in the game. These players are
	 *            not guaranteed to be connected, and this should be handled in
	 *            this function.
	 * 
	 * @return a GameInstance with the given players. null if the game is to be
	 *         disregarded.
	 */
	public GameInstance createGameInstance(List<PersistentPlayer> players);

}
