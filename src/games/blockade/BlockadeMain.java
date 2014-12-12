package games.blockade;

import core.Config;
import core.Director;
import core.commander.EmptyGameCommandHandler;

public class BlockadeMain {
	public static void main (String[] args) {
		System.out.println("Blockade started");
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		new Director(new PlayerFactory(), new GameFactory(), new EmptyGameCommandHandler()).run(config);
	}
}
