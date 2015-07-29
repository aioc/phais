package com.ausinformatics.phais.server.spectators;

import com.ausinformatics.phais.server.server.ClientConnection;

public class StandardSpecatorFactory implements SpectatorFactory {

    @Override
    public Spectator makeSpectator(int myId, ClientConnection c) {
        StandardSpectator s = new StandardSpectator(myId, c);
        s.configure();
        return s;
    }

}
