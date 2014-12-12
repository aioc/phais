package games.spies;

import core.Config;
import core.Director;
import core.commander.EmptyGameCommandHandler;

public class SpiesMain {
	public static void main (String[] args) {
		System.out.println("Spies started");
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		config.numPlayersPerGame = 2;
		new Director(new PlayerFactory(), new GameFactory(), new EmptyGameCommandHandler()).run(config);
	}
}
