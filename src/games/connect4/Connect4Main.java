package games.connect4;

import core.Config;
import core.Director;

public class Connect4Main {
	public static void main (String[] args) {
		System.out.println("Connect4 started");
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		config.numPlayersPerGame = 2;
		new Director(new PlayerFactory(), new GameFactory()).run(config);
	}
}
