package com.ausinformatics.phais.server.runner;

import java.util.List;
import java.util.Map;

import com.ausinformatics.phais.common.events.SpectatorCommunicator;
import com.ausinformatics.phais.server.interfaces.GameInstance;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.spectators.Spectator;

/**
 * Runs a game, allowing you to query its state.
 */
public class StandardGameRunner implements GameRunner, Runnable {

    private Thread myThread;
    private GameInstance game;
    private boolean running;
    private SpectatorCommunicator sc;
    private List<PersistentPlayer> playersInGame;
    private List<Spectator> spectatorsInGame;

    public StandardGameRunner(GameInstance game, List<PersistentPlayer> players, List<Spectator> spectators,
            SpectatorCommunicator sc) {
        this.game = game;
        playersInGame = players;
        spectatorsInGame = spectators;
        this.sc = sc;
        running = true;
        myThread = new Thread(this);
    }

    @Override
    public void start(String name) {
        myThread.setName(name);
        myThread.start();
    }

    @Override
    public void run() {
        sc.startGame();
        game.begin();
        sc.endGame();
        running = false;
    }

    @Override
    public boolean isFinished() {
        return !running;
    }

    @Override
    public List<PersistentPlayer> getPlayers() {
        return playersInGame;
    }

    @Override
    public List<Spectator> getSpectators() {
        return spectatorsInGame;
    }

    @Override
    public Map<PersistentPlayer, Integer> getResults() {
        return game.getResults();
    }

    protected GameInstance getGame() {
        return game;
    }

}
