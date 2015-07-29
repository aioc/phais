package com.ausinformatics.phais.server.runner;

import java.util.List;

import com.ausinformatics.phais.common.events.SpectatorCommunicator;
import com.ausinformatics.phais.server.interfaces.GameInstance;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.spectators.Spectator;

public class RunnerFactory {

    public StandardGameRunner getStandardRunner(GameInstance game, List<PersistentPlayer> players,
            List<Spectator> spectators, SpectatorCommunicator sc) {

        StandardGameRunner newS = new StandardGameRunner(game, players, spectators, sc);
        return newS;
    }

}
