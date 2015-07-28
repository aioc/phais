package com.ausinformatics.phais.server.interfaces;

import java.awt.Graphics;
import java.util.Map;

/**
 * This interface needs to be implemented by the game maker.
 * <p />
 * Used to represent the entirety of a game instance, including the game state,
 * the main game loop, victory and defeat conditions, etc.
 */

public interface GameInstance {

	/**
	 * This method will be called when the game is to begin. The main game loop
	 * should be executed while inside this method.
	 * <p />
	 * Returning from this function signifies the end of the game, and so the
	 * results of the games should be finalised before returning.
	 */
	public void begin();

	/**
	 * This method should return an Image visualisation of the game. If there is
	 * nothing to display (or visualisation of the game has not been
	 * implemented), this should return null.
	 * @param g The java.awt.Graphics context to draw into 
	 * @param width The width of the drawing context
	 * @param height The height of the drawing context
	 * 
	 * @return an Image representing the game state; <code>null</code> if there is nothing to
	 *         display (or if unimplemented)
	 */
	public void getVisualisation(Graphics g, int width, int height);

	/**
	 * This method should do any bookkeeping required to handle a window
	 * resize event.
	 * @param width The width of the drawing context.
	 * @param height The height of the drawing context.
	 */
	public void handleWindowResize(int width, int height);
	
	/**
	 * This gets called whenever the visualising window gets closed
	 */
	public void windowClosed();

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
