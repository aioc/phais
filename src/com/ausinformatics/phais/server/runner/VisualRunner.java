package com.ausinformatics.phais.server.runner;

import java.util.List;

import com.ausinformatics.phais.server.interfaces.GameInstance;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

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
