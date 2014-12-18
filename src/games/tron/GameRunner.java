package games.tron;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class GameRunner implements GameInstance {
	private TronGameState state;
	private List<PersistentPlayer> players;
	private Map<PersistentPlayer, Integer> results;
	
	private int[] pointRewards = {128, 64, 32, 16, 8, 4, 2, 1};
	private int[] finalRanks;
	
	public GameRunner(List<PersistentPlayer> players, int boardSize) {
		state = new TronGameState(players.size(), boardSize);
		this.players = players;
		results = new HashMap<PersistentPlayer, Integer>();
		finalRanks = new int[players.size()];
	}
	
	private boolean stillAlive(int playerIndex) {
		return !results.containsKey(players.get(playerIndex));
	}
	
	@Override
	public void begin() {
		int playerToMove = 0;
		while (results.size() != players.size() - 1) {
			if (stillAlive(playerToMove)) {
				ClientConnection connection = players.get(playerToMove).getConnection();
				// the player is not dead yet (ie. not in results map)
				
				// send over the positions of all other players
				Position[] positions = state.getPlayerPositions(playerToMove);
				for (int i = 0; i < positions.length; i++) {
					int player;
					if (i == 0) {
						player = playerToMove;
					} else if (i > playerToMove) {
						player = i;
					} else {
						player = i - 1;
					}
					
					if (stillAlive(player)) {
						String toSend = "LOCATION " + i + " " + positions[i].r + " " + positions[i].c;
						connection.sendInfo(toSend);
					}
				}
				
				// request move
				connection.sendInfo("YOURMOVE");
				
				boolean playerDied = false;
				
				try {
					String inputString = connection.getStrInput();
					String[] tokens = inputString.split("\\s");
					// dump bad protocol messages everywhere
					if (tokens.length != 3) {
						playerDied = true;
					} else if (!tokens[0].equals("MOVE")) {
						playerDied = true;
					} else {
						int dr = Integer.parseInt(tokens[1]);
						int dc = Integer.parseInt(tokens[2]);
						
						if (!state.takeNextMove(playerToMove, dr, dc)) {
							playerDied = true;
						}
					}
				} catch (DisconnectedException e) {
					playerDied = true;
				}
				
				if (playerDied) {
					// minus 1 to account for this guy dying
					finalRanks[playerToMove] = (players.size() - results.size());
					results.put(players.get(playerToMove), pointRewards[finalRanks[playerToMove] - 1]);
				}
			}
			
			playerToMove = (playerToMove + 1) % players.size();
		}
		
		for (int i = 0; i < players.size(); i++) {
			// TODO check that this only ever corresponds to a single player
			if (stillAlive(i)) {
				finalRanks[i] = 1;
				results.put(players.get(i), pointRewards[0]);
			}
			players.get(i).getConnection().sendInfo("GAMEOVER Your place is " + finalRanks[i]);
		}
	}

	@Override
	public void getVisualisation(Graphics g, int width, int height) {
		int[] colours = new int[players.size()];
		
		for (int i = 0; i < players.size(); i++) {
			colours[i] = ((Player)players.get(i)).getColour();
		}
		state.drawIntoGraphicsContext(g, colours);
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return results;
	}

	@Override
	public void handleWindowResize(int w, int h) {
	}

	@Override
	public void windowClosed() {
	}
}
