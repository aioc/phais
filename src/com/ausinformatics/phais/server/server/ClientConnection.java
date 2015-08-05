package com.ausinformatics.phais.server.server;

import com.google.protobuf.Message;

/**
 * Acts as an interface through which to communicate with the client.
 */
public interface ClientConnection {

    /**
     * Sends a protobuf to the client
     *
     * @param message
     *            The message to send
     */
    public void sendMessage(Message message);

    /**
     * Reads a protobuf from the client
     *
     * @param builder
     *            The builder for underlying message class
     */
    public void recvMessage(Message.Builder builder) throws DisconnectedException;

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
     * Sends an error to the client and disconnect
     *
     * @param s
     *            Send an error to the client
     */
    public void sendFatal(String s);

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

    public String getAsync();

}
