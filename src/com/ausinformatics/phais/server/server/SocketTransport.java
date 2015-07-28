package com.ausinformatics.phais.server.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketTransport {

	private Socket socket;
	private PrintWriter out;
	private Scanner in;

	public SocketTransport(Socket socket) throws IOException {
		this.socket = socket;
		this.socket.setTcpNoDelay(true);
		this.out = new PrintWriter(socket.getOutputStream());
		this.in = new Scanner(socket.getInputStream());
	}

	public void write(String s) {
		//out.write(s + "\n");
		out.print(s + "\n");
		out.flush();
	}

	public String read() throws IOException {
		return in.nextLine();
	}

	public void close() throws IOException {
		out.close();
		in.close();
		socket.close();
	}

}
