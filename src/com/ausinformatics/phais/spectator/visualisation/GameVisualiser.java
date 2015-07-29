package com.ausinformatics.phais.spectator.visualisation;

import java.awt.Graphics2D;

public interface GameVisualiser {

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
    public void getVisualisation(Graphics2D g, int width, int height);

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
}
