package core.server;

/**
 * Acts as an interface through which to communicate with the client.
 */
public interface ClientConnection {

	/**
	 * Sends a string to the client
	 * 
	 * @param s
	 *            the string to send
	 */
	public void sendInfo(String s);

	/**
	 * Sends an integer to the client
	 * 
	 * @param i
	 *            the integer to send
	 */
	public void sendInfo(int i);

	/**
	 * Receives string input from the client
	 * 
	 * @return the string received
	 * @throws DisconnectedException
	 *             if the client is disconnected. The game maker should handle
	 *             this as appropriate to their game
	 */
	public String getStrInput() throws DisconnectedException;

	/**
	 * Receives integer input from the client
	 * 
	 * @return the integer received
	 * @throws DisconnectedException
	 *             if the client is disconnected. The game maker should handle
	 *             this as appropriate to their game
	 */
	public int getIntInput() throws DisconnectedException;

	/**
	 * Disconnects the client
	 */
	public void disconnect();

	/**
	 * Returns true iff the client is still connected
	 * 
	 * @return true iff the client is still connected
	 */
	public boolean isConnected();

	/**
	 * Forces a recheck to see if they are connected. Will return true iff they
	 * are still connected
	 * 
	 * @return true iff they are stilled connected after checking
	 */
	public boolean checkConnected();

}
