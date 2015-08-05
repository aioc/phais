package com.ausinformatics.phais.server.server;

import java.io.IOException;
import java.net.Socket;

public class NewConnectionHandler implements Runnable {
	private Socket socket;
	private int timeout;
	private ClientRegister registar;

	public NewConnectionHandler(Socket socket, int timeout,
			ClientRegister registar) {
		this.socket = socket;
		this.timeout = timeout;
		this.registar = registar;
	}

	public void start() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		ClientConnection newPlayer = HACK();
		if (newPlayer != null)
			registar.registerPlayer(newPlayer);
	}

	private ClientConnection HACK() {
		try {
			socket.setSoTimeout(1000); // Initial read.
			int tag = socket.getInputStream().read();
			socket.setSoTimeout(0);

			switch (tag) {
			case 'C':
				return new ProtobufClientConnection(socket, timeout);
			case 'V':
				return new TextClientConnection(timeout, socket);
			default:
				return null;
			}
		} catch (IOException e) {
			System.out.println("piece of shit timed out");
			return null;
		}
	}
}
