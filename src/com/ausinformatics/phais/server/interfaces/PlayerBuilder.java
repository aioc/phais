package com.ausinformatics.phais.server.interfaces;

import com.ausinformatics.phais.server.server.ClientConnection;

/**
 * This interface needs to be implemented by the game maker.
 * <p />
 * Used to construct a player from their client connection.
 */

public interface PlayerBuilder {

	/**
	 * Used to construct a PersistentPlayer, given the expected ID of the
	 * player, as well as the ClientConnection through which the client
	 * connected.
	 * <p />
	 * If there are any procedures that should occur when a player is created
	 * for the first time, they should execute here. In particular, the name of
	 * the player should be determined here (through whatever protocol is deemed
	 * appropriate by the game maker). The returned PersistentPlayer should have
	 * its name initialised, and should be ready to be placed into a game at any
	 * time.
	 * <p />
	 * This would be an appropriate place to assign colours to the players.
	 * 
	 * @param ID
	 *            the expected ID of the player. When getID is called on the
	 *            returned player, it should always return this ID.
	 * @param client
	 *            the connection through which the client has connected.
	 * @return a persistent player corresponding to the client given, with the
	 *         given ID
	 */
	public PersistentPlayer createPlayer(int ID, ClientConnection client);

}
