package games.blockade;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class GameRunner implements GameInstance {

	private BlockadeGameState state;
	private List<PersistentPlayer> players;
	private Map<PersistentPlayer, Integer> results;
	private int numFinished;

	private static int[] pointRewards = { 128, 64, 32, 16, 8, 4, 2, 1 };
	private int[] finalRanks;

	public GameRunner(List<PersistentPlayer> players, int boardSize) {
		state = new BlockadeGameState(players.size(), boardSize);
		state.setPlayerNames(players);
		this.players = players;
		results = new HashMap<PersistentPlayer, Integer>();
		finalRanks = new int[players.size()];
		numFinished = 0;
	}

	private boolean isFinished(int playerIndex) {
		return results.containsKey(players.get(playerIndex));
	}

	private void playerFinished(int playerIndex) {
		numFinished++;
		finalRanks[playerIndex] = numFinished;
		results.put(players.get(playerIndex), pointRewards[finalRanks[playerIndex] - 1]);
	}

	private void killPlayer(int playerIndex) {
		finalRanks[playerIndex] = players.size() - (results.size() - numFinished);
		results.put(players.get(playerIndex), pointRewards[finalRanks[playerIndex] - 1]);
	}

	@Override
	public void begin() {
		int playerToMove = 0;
		while (results.size() < players.size() - 1) {
			if (!isFinished(playerToMove)) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ClientConnection connection = players.get(playerToMove).getConnection();
				// request move
				connection.sendInfo("YOURMOVE");

				boolean playerDied = false;
				try {
					Action a = Action.getAction(playerToMove, connection);
					if (!state.isValidAction(a)) {
						playerDied = true;
						connection.sendInfo("BADPROT Invalid action " + a.toString());
					} else {
						state.makeAction(a);
						// Then send the move to everyone
						for (PersistentPlayer p : players) {
							a.sendAction(p.getConnection());
						}
						if (state.hasPlayerFinished(playerToMove)) {
							playerFinished(playerToMove);
						}
					}
					
					
				} catch (DisconnectedException ex) {
					playerDied = true;
				} catch (BadProtocolException ex) {
					playerDied = true;
				}
				if (playerDied) {
					killPlayer(playerToMove);
				}
			}
			playerToMove = (playerToMove + 1) % players.size();
		}

		for (int i = 0; i < players.size(); i++) {
			if (!isFinished(i)) {
				killPlayer(i);
			}
			players.get(i).getConnection().sendInfo("GAMEOVER Your place is " + finalRanks[i]);
			if (finalRanks[i] == 1) {
				state.setWinner(i);
			}
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void getVisualisation(Graphics g, int width, int height) {
		int[] colours = new int[players.size()];

		for (int i = 0; i < players.size(); i++) {
			colours[i] = ((Player) players.get(i)).getColour();
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
