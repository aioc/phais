package core.runner;

import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;

/**
 * Runs a game, allowing you to query its state.
 */
public class StandardGameRunner implements GameRunner {
	private GameInstance game;
	private boolean running;
	private boolean restartable;
	private List<PersistentPlayer> playersInGame;

	public StandardGameRunner(GameInstance game, List<PersistentPlayer> players) {
		this.game = game;
		playersInGame = players;
		running = true;
		restartable = false;
	}

	@Override
	public void run() {
		game.begin();
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
	public Map<PersistentPlayer, Integer> getResults() {
		restartable = true;
		return game.getResults();
	}

	@Override
	public boolean canRestart() {
		return restartable;
	}
	
	protected GameInstance getGame() {
		return game;
	}

	@Override
	public void restart(GameInstance game, List<PersistentPlayer> players) {
		if (!restartable) {
			System.out.println ("ERROR tried to restart game that had not finished");
		}
		running = true;
		restartable = false;
		this.game = game;
		this.playersInGame = players;
	}
}
