package com.ausinformatics.phais.common.events;

import java.util.List;

import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.server.spectators.Spectator;

public class NetworkEventSender implements EventReceiver, SpectatorCommunicator {

    private List<Spectator> spectators;
    private EventManager manager;

    public NetworkEventSender(List<Spectator> s, EventManager manager) {
        spectators = s;
        this.manager = manager;
    }
    
    @Override
    public void startGame() {
        for (Spectator s : spectators) {
            s.getConnection().sendInfo("BEGIN");
        }
    }

    @Override
    public void giveEvents(List<VisualGameEvent> events) {
        StringBuilder sb = new StringBuilder();
        sb.append(events.size());
        for (VisualGameEvent e : events) {
            sb.append(" ");
            sb.append(manager.toData(e));
        }
        for (Spectator s : spectators) {
            s.getConnection().sendInfo(sb.toString());
        }
    }
    
    @Override
    public void endGame() {
        for (Spectator s : spectators) {
            s.getConnection().sendInfo("END");
        }
    }

}
