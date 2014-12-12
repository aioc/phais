package games.tron;

import core.Config;
import core.Director;

public class TronMain {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		new Director(new PlayerFactory(), new GameFactory()).run(config);
	}
	
}
