package core.runner;

import java.util.List;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;

/*
 * Runs a game, allowing you to query its state. Also handles visualisation.
 */
public class VisualRunner extends StandardGameRunner implements GameRunner {

	private GameVisualisation visualisation;
	
	
	public VisualRunner(GameInstance game, List<PersistentPlayer> players) {
		super(game, players);
		visualisation = new GameVisualisation();
	}

	@Override
	public void run() {
		visualisation.show(getGame());
		super.run();
		visualisation.close();
	}
}
