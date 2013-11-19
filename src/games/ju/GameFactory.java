package games.ju;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.DisconnectedException;

public class GameFactory implements GameBuilder {

	public static final int NUM_ROUNDS = 13;
	public static int BABCHANCE        = 0;
	public static boolean SWEETBABMUSIC = false;
	private long lastTime = 0;
	private static final long WAITTIME  = 1000 * 60 * 4;

	@Override
	public GameInstance createGameInstance(List<PersistentPlayer> players) {
		int randKey = new Random().nextInt();
		
		int[] values = getValues();
		boolean haveTheBab = false;
		BABBOT THEBAB = null;
		if (BABCHANCE > 0 && new Random().nextInt(BABCHANCE) == 0 && System.currentTimeMillis() - lastTime > WAITTIME) {
			List<PersistentPlayer> newPlayers = new ArrayList<PersistentPlayer>();
			newPlayers.add(players.get(0));
			haveTheBab = true;
			THEBAB = new BABBOT(players.get(1).getID(), players.get(1).getConnection());
			newPlayers.add(THEBAB);
			players = newPlayers;
			lastTime = System.currentTimeMillis();
		}
		for (int i = 0; i < players.size(); i++) {
			PersistentPlayer p = players.get(i);
			if (!p.getName().equals(BABBOT.THE_NAME)) {
				String toSend = "NEWGAME " + players.size() + " " + NUM_ROUNDS + " " + i + " " + randKey;
				p.getConnection().sendInfo(toSend);

				for (int v : values) {
					p.getConnection().sendInfo("ROUND " + v);
				}

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
					for (int j = 0; j < i; j++) {
						players.get(j).getConnection().sendInfo("GAMEOVER Opponent disconnected");
					}
					return null;
				}
			}
		}
		GameRunner gr = new GameRunner(players, NUM_ROUNDS, values);
		if (haveTheBab) {
			THEBAB.GIVEMETHEROUNDS(values);
		}
		return gr;
	}

	private int[] getValues() {
		int values[] = new int[NUM_ROUNDS];
		for (int i = 0; i < NUM_ROUNDS; i++) {
			values[i] = new Random().nextInt(50) + 1;
		}
		return values;
	}

}
