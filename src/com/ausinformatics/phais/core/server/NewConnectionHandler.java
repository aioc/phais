package com.ausinformatics.phais.core.server;

import java.net.Socket;

public class NewConnectionHandler implements Runnable {
	private Socket socket;
	private int timeout;
	private ClientRegister registar;
	public NewConnectionHandler(Socket socket, int timeout, ClientRegister registar) {
		this.socket = socket;
		this.timeout = timeout;
		this.registar = registar;
	}
	
	@Override
	public void run() {
		Client newPlayer = new Client(timeout, socket);
		registar.registerPlayer(newPlayer);
	}
}
