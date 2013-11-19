package core.server;

import java.net.Socket;

import core.Director;

public class NewConnectionHandler implements Runnable {
	private Socket socket;
	private int timeout;
	private Director director;
	public NewConnectionHandler(Socket socket, int timeout, Director director) {
		this.socket = socket;
		this.timeout = timeout;
		this.director = director;
	}
	
	@Override
	public void run() {
		Client newPlayer = new Client(timeout, socket);
		director.registerPlayer(newPlayer);
	}
}
