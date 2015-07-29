package com.ausinformatics.phais.server.spectators;

import java.util.List;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.server.ClientConnection;

public interface Spectator {

    public int getId();

    public int getGroupId();

    public String getName();

    public boolean shouldAddToGame(List<PersistentPlayer> players, List<Spectator> spectators);

    public ClientConnection getConnection();
}
