package com.ausinformatics.phais.core.interfaces;

import com.ausinformatics.phais.core.server.ClientConnection;

/**
 * This interface needs to be implemented by the game maker.
 * <p />
 * Stores all data about a player that persists between games. In particular,
 * this player corresponds to a connected client, and is different to a player
 * within a game instance, which is something that the game maker should
 * implement separately (if necessary).
 */

public interface PersistentPlayer {

	/**
	 * Returns the ID of the persistent player. This should never change and
	 * should be supplied when the PersistentPlayer was constructed in
	 * PlayerBuilder.
	 * 
	 * @return the ID of the persistent player.
	 */
	public int getID();

	/**
	 * Returns the name of the persistent player.
	 * 
	 * @return the name of the persistent player.
	 */
	public String getName();

	/**
	 * Called when the existing name is invalid (if, for example, it conflicts
	 * with an existing persistent player). After this function returns,
	 * getName() should return a different string to the one returned before
	 * this function was called.
	 */
	public void generateNewName();

	/**
	 * TODO make this not the game maker's responsibility to implement
	 * Returns the ClientConnection corresponding to this player.
	 * 
	 * @return the ClientConnection corresponding to this player.
	 */
	public ClientConnection getConnection();

}
