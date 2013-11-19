package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.commander.Commander;
import core.commander.ShellCommander;
import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.interfaces.PlayerBuilder;
import core.runner.GameRunner;
import core.runner.RunnerFactory;
import core.scheduler.GameScheduler;
import core.scheduler.RandomScheduler;
import core.scorer.ScoreKeeper;
import core.scorer.StandardScoreKeeper;
import core.server.ClientConnection;
import core.server.Server;

// The game maker should create one of these, with the specified things.

public class Director {

	private GameBuilder gBuilder;
	private PlayerBuilder pBuilder;
	private Integer curID;
	private Server server;
	private GameScheduler scheduler;
	private Commander commander;
	private ScoreKeeper scoreKeeper;
	private RunnerFactory runnerGetter;
	private Map<String, PersistentPlayer> playerMap;
	private Set<GameRunner> runningGames;
	private Map<GameRunner, Thread> runningGameThreads;

	private Config config;
	
	private boolean running;

	public Director(PlayerBuilder pBuilder, GameBuilder gBuilder) {
		this.pBuilder = pBuilder;
		this.gBuilder = gBuilder;
		runnerGetter = new RunnerFactory();
		playerMap = new HashMap<String, PersistentPlayer>();
		runningGames = new HashSet<GameRunner>();
		runningGameThreads = new HashMap<GameRunner, Thread>();
		curID = 0;
		running = true;
	}

	public boolean isRunning() {
		return running;
	}
	
	public void registerPlayer(ClientConnection p) {
		int copiedID;
		synchronized (curID) {
			copiedID = curID;
			curID++;
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
		scheduler.removePlayer(player);
		playerMap.remove(player.getName());
		scoreKeeper.deregisterPlayer(player);
		player.getConnection().disconnect();
	}

	public PersistentPlayer getPlayerFromName(String name) {
		return playerMap.get(name);
	}

	public Map<PersistentPlayer, Integer> getScores() {
		Map<PersistentPlayer, Integer> ret = new HashMap<PersistentPlayer, Integer>();
		for (PersistentPlayer p : playerMap.values()) {
			ret.put(p, scoreKeeper.getScore(p));
		}
		return ret;
	}

	public void addGameToQueue(List<PersistentPlayer> players) {
		synchronized (scheduler) {
			scheduler.scheduleGame(players);
		}
	}

	private void reschedulePlayers(List<PersistentPlayer> players) {
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
		commander = new ShellCommander(this);
		scoreKeeper = new StandardScoreKeeper();

		// We now create the threads we need for various things
		new Thread(server).start();
		new Thread(commander).start();

		while (running) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println("This should never happen.");
				e.printStackTrace();
			}
			// finish all running games
			for (GameRunner g : runningGames.toArray(new GameRunner[0])) {
				if (g.isFinished()) {
					runningGames.remove(g);
					runningGameThreads.remove(g);

					reschedulePlayers(g.getPlayers());
					
					scoreKeeper.submitGame(g.getResults());
				}
			}

			// put together games, run them (in parallel if desired)
			while (runningGames.size() < config.maxParallelGames && scheduler.hasGame()) {
				List<PersistentPlayer> players = scheduler.getGame();
				boolean incompleteGame = false;
				for (PersistentPlayer p : players) {
					if (!p.getConnection().isConnected()) {
						deregisterPlayer(p);
						incompleteGame = true;
					}
				}
				if (!incompleteGame) {
					GameInstance toSpawn = gBuilder.createGameInstance(players);
					if (toSpawn == null) {
						reschedulePlayers(players);
					} else {
						GameRunner newGameInstance;
						if (config.visualise) {
							newGameInstance = runnerGetter.getVisualRunner(toSpawn, players);
						} else {
							newGameInstance = runnerGetter.getStandardRunner(toSpawn, players);
						}
						
						runningGames.add(newGameInstance);
						
						Thread newThread = new Thread(newGameInstance);
						runningGameThreads.put(newGameInstance, newThread);
						
						newThread.start();
					}
				}
			}
		}

		cleanUp();
		
		System.out.println("Director exiting...");
	}

	@SuppressWarnings("deprecation")
	private void cleanUp() {
		server.kill();
		for (PersistentPlayer p : playerMap.values()) {
			p.getConnection().disconnect();
		}
		for (Thread t : runningGameThreads.values()) {
			// Any objects that would be corrupted by t.stop() wouldn't matter,
			// since we are stopping everything. This might not be true if games
			// and players are held in some other object outside of Director,
			// but for the purposes of PHAIS, this should never happen.
			
			// If things break, consider looking here.
			t.stop();
		}
	}
	
	public Config getConfig() {
		return config;
	}
	
	public void updateConfig(Config config) {
		this.config = config;
		
		// check that everything is as expected
		if (scheduler.getMode() != config.mode) {
			// TODO swap out the schedulers
		} else if (scheduler.getNumPlayersPerGame() != config.numPlayersPerGame) {
			// TODO note to self: also check that the number of players per game hasn't gone out of sync
		}
	}
	
	public Collection<PersistentPlayer> getPlayers() {
		return playerMap.values();
	}

	public void kill() {
		running = false;
	}
}
