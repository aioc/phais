package games.tron;

import core.Config;
import core.Director;
import core.commander.EmptyGameCommandHandler;

public class Main {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		new Director(new PlayerFactory(), new GameFactory(), new EmptyGameCommandHandler()).run(config);
	}

}
