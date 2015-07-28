package com.ausinformatics.phais.server.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

	private ServerSocket socket;
	private boolean running;
	private int timeout;
	private ClientRegister registar;

	public Server(int port, int defaultTimeout, ClientRegister registar) throws IOException {
		timeout = defaultTimeout;
		this.registar = registar;
		socket = new ServerSocket(port);
		running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Socket connection = socket.accept();
				new NewConnectionHandler(connection, timeout, registar).start();
				Thread.sleep(10);
			} catch (Exception e) {
				try {
					socket.close();
				} catch (Exception e1) {}
				running = false;
			}
		}
		System.out.println("Server listener exiting...");
	}

	public void kill() {
		running = false;
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
