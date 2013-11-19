package games.tron;

import java.util.List;
import java.util.Random;

import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.DisconnectedException;

public class GameFactory implements GameBuilder {

	private int boardSize = 25;
	@Override
	public GameInstance createGameInstance(List<PersistentPlayer> players) {
		int randKey = new Random().nextInt();
		
		for (PersistentPlayer p : players) {
			String toSend = "NEWGAME " + players.size() + " " + boardSize + " " + randKey;
			p.getConnection().sendInfo(toSend);
			
			try {
				while (true) {
					String inputString = p.getConnection().getStrInput();
					String[] tokens = inputString.split("\\s");
					if (tokens.length != 2) {
						continue;
					} else if (!tokens[0].equals("READY")) {
						continue;
					}
					
					try {
						if (Integer.parseInt(tokens[1]) == randKey) {
							break;
						}
					} catch (NumberFormatException nfe) {
						continue;
					}
				}
			} catch (DisconnectedException de) {
				p.getConnection().disconnect();
			}
		}
		return new GameRunner(players, boardSize);
	}

}
