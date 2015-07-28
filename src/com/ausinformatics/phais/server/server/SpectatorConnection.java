package com.ausinformatics.phais.server.server;

public interface SpectatorConnection {
	
	public void sendInfo(String s);
	
	public boolean isConnected();
}
