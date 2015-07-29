package com.ausinformatics.phais.server.scheduler;

import java.util.List;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.spectators.Spectator;

public interface SpectatorScheduler {

    public void addSpectator(Spectator spectator);
    
    public void removeSpectator(Spectator spectator);
    
    public List<Spectator> addToGame(List<PersistentPlayer> players);
    
}
