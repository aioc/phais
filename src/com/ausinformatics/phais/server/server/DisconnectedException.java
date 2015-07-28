package com.ausinformatics.phais.server.server;

public class DisconnectedException extends Exception {

	/**
    * 
    */
	private static final long serialVersionUID = 6469578443490765167L;

	private ClientConnection personGone;

	public DisconnectedException(ClientConnection p) {
		personGone = p;
		p.disconnect();
	}

	public ClientConnection getPlayerGone() {
		return personGone;
	}

}
