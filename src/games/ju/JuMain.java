package games.ju;

import core.Config;
import core.Director;
import core.commander.EmptyGameCommandHandler;

public class JuMain {
	public static void main (String[] args) {
		System.out.println("Ju started");
		Config config = new Config();
		config.parseArgs(args);
		new Director(new PlayerFactory(), new GameFactory(), new EmptyGameCommandHandler()).run(config);
	}
}
