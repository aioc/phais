package games.connect4;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class GameRunner implements GameInstance {

	private GameState state;
	private List<PersistentPlayer> players;
	private Map<PersistentPlayer, Integer> results;
	private GameVisualiser visualiser;

	private int[] finalRanks;

	public GameRunner(List<PersistentPlayer> players, int boardHeight, int boardWidth) {
		this.players = players;
		results = new HashMap<PersistentPlayer, Integer>();
		finalRanks = new int[players.size()];
		state = new GameState(boardHeight, boardWidth);
		visualiser = new GameVisualiser(players, state);
	}

	private boolean isFinished(int playerIndex) {
		return results.containsKey(players.get(playerIndex));
	}

	private void killPlayer(Integer toKill) {
		List<Integer> l = new ArrayList<Integer>();
		l.add(toKill);
		killPlayers(l);
	}

	private void killPlayers(List<Integer> toKill) {
		for (Integer i : toKill) {
			finalRanks[i] = players.size() - results.size();
		}
		for (Integer i : toKill) {
			results.put(players.get(i), getReward(finalRanks[i] - 1));
		}
	}

	@Override
	public void begin() {
		int curPlayer = 0;
		while (results.size() < players.size() - 1 && !state.isGameOver()) {
			PersistentPlayer p = players.get(curPlayer);
			ClientConnection connection = p.getConnection();
			connection.sendInfo("YOURMOVE");
			boolean playerDied = false;
			try {
				Action a = Action.getAction(connection);
				// Some verification
				if (!state.isValidAction(curPlayer, a)) {
					playerDied = true;
					connection.sendInfo("BADPROT Invalid action " + a.toString());
				} else {
					state.makeMove(curPlayer, a);
					for (int i = 0; i < 2; i++) {
						players.get(i).getConnection().sendInfo("MOVE " + curPlayer + " " + a.toString());
					}
				}
			} catch (DisconnectedException ex) {
				playerDied = true;
			} catch (BadProtocolException ex) {
				connection.sendInfo("BADPROT Invalid action. " + ex.getExtraInfo());
				playerDied = true;
			}
			if (playerDied) {
				System.out.println ("Invalid move");
				killPlayer(curPlayer);
			}
			curPlayer = (curPlayer + 1) % 2;
			try {
				Thread.sleep(30);
			} catch (Exception e) {

			}
		}
		if (state.isGameOver()) {
			if (state.getWinner() == GameState.DRAW) {
				List<Integer> stillAlive = new ArrayList<Integer>();
				if (!isFinished(0)) {
					stillAlive.add(0);
				}
				if (!isFinished(1)) {
					stillAlive.add(1);
				}
				killPlayers(stillAlive);
			}
			if (results.size() == 0) {
				killPlayer(1 - state.getWinner());
			}
			if (results.size() == 1) {
				killPlayer(state.getWinner());
			}
		} else {
			if (!isFinished(0)) {
				killPlayer(0);
			}
			if (!isFinished(1)) {
				killPlayer(1);
			}
		}
		for (int i = 0; i < players.size(); i++) {
			players.get(i).getConnection().sendInfo("GAMEOVER Your place is " + finalRanks[i]);
			if (finalRanks[i] == 1) {
				// state.setWinner(i);
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
		visualiser.visualise((Graphics2D) g, width, height);
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return results;
	}

	public int getReward(int pos) {
		return 1 << (players.size() - pos - 1);
	}

}
