package com.ausinformatics.phais.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ausinformatics.phais.common.Config;
import com.ausinformatics.phais.common.Config.Mode;
import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.common.commander.Commander;
import com.ausinformatics.phais.common.commander.ShellCommander;
import com.ausinformatics.phais.common.events.NetworkEventSender;
import com.ausinformatics.phais.server.commands.DisplayScores;
import com.ausinformatics.phais.server.commands.KickPlayers;
import com.ausinformatics.phais.server.commands.Kill;
import com.ausinformatics.phais.server.commands.ListPlayers;
import com.ausinformatics.phais.server.commands.ScheduleGame;
import com.ausinformatics.phais.server.commands.SchedulePause;
import com.ausinformatics.phais.server.commands.ScheduleRandom;
import com.ausinformatics.phais.server.commands.ScheduleRoundRobin;
import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.server.interfaces.GameBuilder;
import com.ausinformatics.phais.server.interfaces.GameInstance;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.interfaces.PlayerBuilder;
import com.ausinformatics.phais.server.runner.GameRunner;
import com.ausinformatics.phais.server.runner.RunnerFactory;
import com.ausinformatics.phais.server.scheduler.GameScheduler;
import com.ausinformatics.phais.server.scheduler.MaximisingSpectatorScheduler;
import com.ausinformatics.phais.server.scheduler.PauseScheduler;
import com.ausinformatics.phais.server.scheduler.RandomScheduler;
import com.ausinformatics.phais.server.scheduler.RoundRobinScheduler;
import com.ausinformatics.phais.server.scheduler.SpectatorScheduler;
import com.ausinformatics.phais.server.scorer.ScoreKeeper;
import com.ausinformatics.phais.server.scorer.StandardScoreKeeper;
import com.ausinformatics.phais.server.server.ClientConnection;
import com.ausinformatics.phais.server.server.ClientRegister;
import com.ausinformatics.phais.server.server.DisconnectedException;
import com.ausinformatics.phais.server.server.Server;
import com.ausinformatics.phais.server.spectators.Spectator;
import com.ausinformatics.phais.server.spectators.SpectatorFactory;
import com.ausinformatics.phais.server.spectators.StandardSpecatorFactory;

// The game maker should create one of these, with the specified things.

public class Director implements ClientRegister {

    private GameBuilder gBuilder;
    private PlayerBuilder pBuilder;
    
    private EventManager eventManager;
    
    private SpectatorFactory sFactory;

    private Integer curGameID;
    private Server server;
    private GameScheduler scheduler;
    private SpectatorScheduler spectatorScheduler;
    private Commander commander;
    private ScoreKeeper scoreKeeper;
    private RunnerFactory runnerGetter;
    private Map<String, PersistentPlayer> playerMap;
    private Set<GameRunner> runningGames;

    private Config config;

    private boolean running;

    public Director(PlayerBuilder pBuilder, GameBuilder gBuilder, EventManager eventManger) {
        this.pBuilder = pBuilder;
        this.gBuilder = gBuilder;
        this.eventManager = eventManger;
        runnerGetter = new RunnerFactory();
        playerMap = new HashMap<String, PersistentPlayer>();
        runningGames = new HashSet<GameRunner>();
        curGameID = 0;
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void registerPlayer(ClientConnection p) {
        int copiedID;
        synchronized (curGameID) {
            copiedID = curGameID;
            curGameID++;
        }
        // Check if they are a player or spectator.
        try {
            String token = p.getStrInput();
            if (token.equals("spectator")) {
                Spectator s = sFactory.makeSpectator(copiedID, p);
                if (!s.getConnection().isConnected()) {
                    return;
                }
                synchronized (spectatorScheduler) {
                    spectatorScheduler.addSpectator(s);
                }
                System.out.println("Spectator " + s.getName() + " added");
                return;
            }
        } catch (DisconnectedException e) {
            return;
        }

        PersistentPlayer newPlayer = pBuilder.createPlayer(copiedID, p);
        if (!newPlayer.getConnection().isConnected()) {
            return;
        }
        synchronized (playerMap) {
            while (playerMap.containsKey(newPlayer.getName())) {
                newPlayer.generateNewName();
            }
            playerMap.put(newPlayer.getName(), newPlayer);
        }
        synchronized (scheduler) {
            scheduler.addPlayer(newPlayer);
        }
        synchronized (scoreKeeper) {
            scoreKeeper.registerPlayer(newPlayer);
        }
        System.out.println("Player " + newPlayer.getName() + " added");
    }

    public void deregisterPlayer(PersistentPlayer player) {
        // FLUSH THEM FROM EXISTENCE
        // TODO make sure this doesn't interfere with games currently running
        synchronized (scheduler) {
            scheduler.removePlayer(player);
        }
        synchronized (playerMap) {
            playerMap.remove(player.getName());
        }
        synchronized (scheduler) {
            scoreKeeper.deregisterPlayer(player);
        }
        player.getConnection().disconnect();
    }

    public PersistentPlayer getPlayerFromName(String name) {
        synchronized (playerMap) {
            return playerMap.get(name);
        }
    }

    public Map<PersistentPlayer, Integer> getScores() {
        Map<PersistentPlayer, Integer> ret = new HashMap<PersistentPlayer, Integer>();
        synchronized (playerMap) {
            for (PersistentPlayer p : playerMap.values()) {
                ret.put(p, scoreKeeper.getScore(p));
            }
        }
        return ret;
    }

    public void addGameToQueue(List<PersistentPlayer> players) {
        synchronized (scheduler) {
            scheduler.scheduleGame(players);
        }
    }

    private void reschedulePlayers(List<PersistentPlayer> players) {
        synchronized (scheduler) {
            for (PersistentPlayer p : players) {
                // do stuff
                if (!p.getConnection().isConnected()) {
                    // player has disconnected
                    deregisterPlayer(p);
                } else {
                    scheduler.addPlayer(p);
                }
            }
        }
    }
    
    private void rescheduleSpectators(List<Spectator> spectators) {
        synchronized (spectatorScheduler) {
            for (Spectator s : spectators) {
                if (!s.getConnection().isConnected()) {
                    spectatorScheduler.removeSpectator(s);
                } else {
                    spectatorScheduler.addSpectator(s);
                }
            }
        }
    }
    
    private Map<String, Command> fillCommands(Map<String, Command> gameCommands) {
        Map<String, Command> commands = new HashMap<>();
        commands.put("RANDOM", new ScheduleRandom(this));
        commands.put("ROUNDROBIN", new ScheduleRoundRobin(this));
        commands.put("PAUSE", new SchedulePause(this));
        commands.put("LS", new ListPlayers(this));
        commands.put("LIST", commands.get("LS"));
        commands.put("KICK", new KickPlayers(this));
        commands.put("PLAY", new ScheduleGame(this));
        commands.put("SCORES", new DisplayScores(this));
        commands.put("QUIT", new Kill(this));
        for (String s : gameCommands.keySet()) {
            commands.put(s, gameCommands.get(s));
        }
        return commands;
    }

    public void run(Config config) {
        // First, create everything we need
        this.config = config;
        try {
            server = new Server(config.port, config.timeout, this);
        } catch (Exception e) {
            System.out.println("Could not open server connection: " + e.getMessage());
            System.exit(1);
        }
        scheduler = new RandomScheduler(config.numPlayersPerGame);
        
        commander = new ShellCommander(fillCommands(config.gameCommands));
        scoreKeeper = new StandardScoreKeeper();
        spectatorScheduler = new MaximisingSpectatorScheduler();
        sFactory = new StandardSpecatorFactory();

        // We now create the threads we need for various things
        Thread t = new Thread(server);
        t.setName("Server listener");
        t.start();
        commander.start();

        while (running) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("This should never happen.");
                e.printStackTrace();
            }
            // finish all running games
            synchronized (runningGames) {
                for (GameRunner g : runningGames.toArray(new GameRunner[0])) {
                    if (g.isFinished()) {
                        runningGames.remove(g);

                        reschedulePlayers(g.getPlayers());
                        rescheduleSpectators(g.getSpectators());

                        synchronized (scoreKeeper) {
                            scoreKeeper.submitGame(g.getResults());
                        }
                    }
                }
            }

            // put together games, run them (in parallel if desired)
            while (runningGames.size() < config.maxParallelGames && scheduler.hasGame()) {
                List<PersistentPlayer> players = null;
                synchronized (scheduler) {
                    players = scheduler.getGame();
                }
                boolean incompleteGame = false;
                String gameName = "Game between";
                for (PersistentPlayer p : players) {
                    if (!p.getConnection().isConnected()) {
                        incompleteGame = true;
                    }
                    gameName += " " + p.getName();
                }
                List<Spectator> spectators = null;
                synchronized (spectatorScheduler) {
                    spectators = spectatorScheduler.addToGame(players);
                }
                for (Spectator s : spectators) {
                    if (!s.getConnection().isConnected()) {
                        incompleteGame = true;
                    }
                }
                if (!incompleteGame) {
                    NetworkEventSender nes = new NetworkEventSender(spectators, eventManager);
                    GameInstance toSpawn = gBuilder.createGameInstance(players, nes);
                    if (toSpawn == null) {
                        reschedulePlayers(players);
                        rescheduleSpectators(spectators);
                    } else {
                        GameRunner newGameInstance;
                        newGameInstance = runnerGetter.getStandardRunner(toSpawn, players, spectators, nes);

                        runningGames.add(newGameInstance);
                        newGameInstance.start(gameName);
                    }
                } else {
                    reschedulePlayers(players);
                    rescheduleSpectators(spectators);
                }
            }
        }

        cleanUp();

        System.out.println("Director exiting...");
        System.exit(0);
    }

    private void cleanUp() {
        commander.stop();
        server.kill();
        for (PersistentPlayer p : playerMap.values()) {
            p.getConnection().disconnect();
        }

    }

    public Config getConfig() {
        return config;
    }

    public void updateConfig(Config config) {
        this.config = config;

        // check that everything is as expected

        GameScheduler newSch = null;
        boolean scheChanged = false;

        if (scheduler.getMode() != config.mode || scheduler.getNumPlayersPerGame() != config.numPlayersPerGame) {
            if (config.mode == Mode.RANDOM) {
                newSch = new RandomScheduler(config.numPlayersPerGame);
            } else if (config.mode == Mode.ROUND_ROBIN) {
                newSch = new RoundRobinScheduler();
            } else if (config.mode == Mode.PAUSE) {
                newSch = new PauseScheduler();
            }
            scheChanged = true;
        }
        if (scheChanged) {
            // TODO swap out the schedulers
            GameScheduler oldSch = scheduler;
            // Switch out now
            synchronized (scheduler) {
                scheduler = newSch;
            }
            // Add all the players into it
            List<PersistentPlayer> waiting = oldSch.removeWaitingPlayers();
            for (PersistentPlayer p : waiting) {
                scheduler.addPlayer(p);
            }
        }
    }

    public Collection<PersistentPlayer> getPlayers() {
        return playerMap.values();
    }

    public void kill() {
        running = false;
    }
}
