package com.ausinformatics.phais.server.spectators;

import com.ausinformatics.phais.server.server.ClientConnection;

public interface SpectatorFactory {

    public Spectator makeSpectator(int myId, ClientConnection c);
    
}
