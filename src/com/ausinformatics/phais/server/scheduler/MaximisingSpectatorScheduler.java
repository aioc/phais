package com.ausinformatics.phais.server.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.spectators.Spectator;

public class MaximisingSpectatorScheduler implements SpectatorScheduler {

    private Set<Spectator> availableSpectators;
    
    public MaximisingSpectatorScheduler() {
        availableSpectators = new HashSet<>();
    }
    
    @Override
    public synchronized void addSpectator(Spectator spectator) {
        availableSpectators.add(spectator);
    }

    @Override
    public synchronized void removeSpectator(Spectator spectator) {
        availableSpectators.remove(spectator);
    }

    @Override
    public synchronized List<Spectator> addToGame(List<PersistentPlayer> players) {
        Set<Spectator> remaining = new HashSet<>();
        List<Spectator> scheduled = new ArrayList<>();
        Set<Integer> usedGid = new HashSet<>();
        for (Spectator s : availableSpectators) {
            if (usedGid.contains(s.getGroupId())) {
                remaining.add(s);
            } else  {
                if (s.shouldAddToGame(players, scheduled)) {
                    scheduled.add(s);
                    usedGid.add(s.getGroupId());
                    s.addedToGame();
                } else {
                    remaining.add(s);
                }
            }
        }
        availableSpectators = remaining;
        return scheduled;
    }

}
