package com.ausinformatics.phais.core.server;

import java.io.IOException;
import java.net.Socket;

public class Client implements ClientConnection {

	private int timeoutTime;
	private SocketTransport output;
	private InputPoller input;
	private boolean connected;

	private final static long PING_TIMEOUT = 3000;

	public Client(int timeout, Socket socket) {
		timeoutTime = timeout;
		connected = true;
		try {
			output = new SocketTransport(socket);
			input = new InputPoller(output);
			new Thread(input).start();
		} catch (Exception e) {
			disconnect();
		}
	}

	@Override
	public void sendInfo(String s) {
		output.write(s);
	}

	@Override
	public void sendInfo(int i) {
		sendInfo(((Integer) i).toString());
	}

	@Override
	public String getStrInput() throws DisconnectedException {
		return getStrInput(false);
	}

	public String getStrInput(boolean ignorePings) throws DisconnectedException {
		// We need to check for two things. First, that they don't time out.
		// Second, that they haven't disconnected.
		if (!isConnected()) {
			drop();
		}
		long curTime = System.currentTimeMillis();
		long lastPingTime = System.currentTimeMillis();
		String in = null;
		while (in == null) {
			if (System.currentTimeMillis() - curTime > timeoutTime) {
				sendInfo("ERROR Client took too long to respond");
				drop();
			}
			if (System.currentTimeMillis() - lastPingTime > PING_TIMEOUT) {
				sendInfo("ERROR Ping timeout");
				drop();
			}
			try {
				if (input.errorOccured()) {
					drop();
				}
				if (input.inputAvailable()) {
					in = input.getInput();
					lastPingTime = System.currentTimeMillis();
					if (in.startsWith("PING") && ignorePings) {
						in = null;
					}
				} else {
					Thread.sleep(5);
				}
			} catch (Exception e) {
				e.printStackTrace();
				drop();
			}
		}
		return in;
	}

	@Override
	public int getIntInput() throws DisconnectedException {
		return Integer.parseInt(getStrInput());
	}

	private void drop() throws DisconnectedException {
		disconnect();
		throw new DisconnectedException(this);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void disconnect() {
		try {
			connected = false;
			input.stop();
			output.close();
		} catch (IOException e) {}
	}

	@Override
	public boolean checkConnected() {
		boolean con = true;
		try {
			getStrInput(false);
		} catch (DisconnectedException de) {
			con = false;
		}
		return con;
	}

}