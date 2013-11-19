package games.ju;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class GameRunner implements GameInstance {

	private JuGameState state;
	private int numRounds;
	private List<PersistentPlayer> players;
	private Map<PersistentPlayer, Integer> results;
	private Clip sweetBABMusic;

	private static int[] pointRewards = { 128, 64, 32, 16, 8, 4, 2, 1 };
	private int[] finalRanks;

	public GameRunner(List<PersistentPlayer> players, int numRounds, int initVals[]) {
		this.numRounds = numRounds;
		state = new JuGameState(players.size(), numRounds, initVals);
		this.players = players;
		results = new HashMap<PersistentPlayer, Integer>();
		finalRanks = new int[players.size()];
		this.sweetBABMusic = null;
	}

	private boolean isFinished(int playerIndex) {
		return results.containsKey(players.get(playerIndex));
	}

	private void killPlayer(int playerIndex) {
		finalRanks[playerIndex] = players.size() - results.size();
		results.put(players.get(playerIndex), pointRewards[finalRanks[playerIndex] - 1]);
	}

	@Override
	public void begin() {
		boolean HAVEBAB = false;
		for (int curRound = 0; curRound < numRounds && results.size() < players.size() - 1; curRound++) {
			long startTime = System.currentTimeMillis();
			List<Action> actions = new ArrayList<Action>();
			for (int curPlayer = 0; curPlayer < players.size(); curPlayer++) {
				if (!isFinished(curPlayer)) {
					if (players.get(curPlayer).getName().equals(BABBOT.THE_NAME)) {
						HAVEBAB = true;
						BABBOT b = (BABBOT) players.get(curPlayer);
						for (Action a: actions) {
							b.GIVEMETHECARD(a.getPlayer(), a.getUsed());
						}
						Action a = new Action(curPlayer, b.GETTHECARD());
						b.TAUNTPLAYER(players.get(0));
						// BAB MAKES NO MISTAKES. NO NEED TO VERIFY
						state.makeAction(a);
						actions.add(a);
						if (sweetBABMusic == null && GameFactory.SWEETBABMUSIC) sweetBABMusic = BABBOT.playSweetBABMusic();
					} else {
						ClientConnection connection = players.get(curPlayer).getConnection();
						connection.sendInfo("YOURMOVE");
						
						boolean playerDied = false;
						try {
							Action a = Action.getAction(curPlayer, connection);
							if (!state.isValidAction(a)) {
								playerDied = true;
								connection.sendInfo("ERROR Invalid action");
							} else {
								state.makeAction(a);
								actions.add(a);
							}
						} catch (DisconnectedException ex) {
							playerDied = true;
						} catch (BadProtocolException ex) {
							playerDied = true;
						}
						if (playerDied) {
							killPlayer(curPlayer);
						}
					}
				}
			}
			state.evaluateRound();
			for (Action a : actions) {
				for (PersistentPlayer p : players) {
					if (!p.getName().equals(BABBOT.THE_NAME)) {
						a.sendAction(p.getConnection());
					}
				}
			}
			int timeToWait = 500;
			if (HAVEBAB) {
				timeToWait = 2000;
			}
			while (System.currentTimeMillis() - startTime < timeToWait) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
			}
		}

		// Get each person, and their score
		int playerScores[] = new int[players.size() - results.size()];
		int j = 0;
		for (int i = 0; i < players.size(); i++) {
			if (!isFinished(i)) {
				playerScores[j] = i;
				j++;
			}
		}
		for (int i = 0; i < playerScores.length; i++) {
			for (j = i + 1; j < playerScores.length; j++) {
				if (state.getScore(playerScores[j]) > state.getScore(playerScores[i])) {
					int temp = playerScores[i];
					playerScores[i] = playerScores[j];
					playerScores[j] = temp;
				}
			}
		}
		for (int i = playerScores.length - 1; i >= 0; i--) {
			killPlayer(playerScores[i]);
		}
		for (int i = 0; i < players.size(); i++) {
			if (!players.get(i).getName().equals(BABBOT.THE_NAME)) {
				players.get(i).getConnection()
					.sendInfo("GAMEOVER Your place is " + finalRanks[i] + getOrdinal(finalRanks[i]));
			}
		}
		int finalSleep = 3000;
		if (HAVEBAB) {
			finalSleep = 4000;
		}
		try {
			Thread.sleep(finalSleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (sweetBABMusic != null) sweetBABMusic.stop();
	}

	private String getOrdinal(int rank) {
		if (rank % 10 == 1 && rank != 11) {
			return "st";
		} else if (rank % 10 == 2 && rank != 12) {
			return "nd";
		} else if (rank % 10 == 3 && rank != 13) {
			return "rd";
		} else {
			return "th";
		}
	}

	@Override
	public void getVisualisation(Graphics g, int width, int height) {
		int[] colours = new int[players.size()];

		for (int i = 0; i < players.size(); i++) {
			colours[i] = ((Player) players.get(i)).getColour();
		}
		JuGameState.WIDTH = width;
		JuGameState.HEIGHT = height;
		state.drawToGraphicsContext(g, colours);
		FontMetrics fm = g.getFontMetrics(g.getFont());
		
		Player player = (Player)players.get(0);
		g.setColor(new Color(player.getColour()));
		g.drawString(player.getName(), 10, 100);
		g.drawString(Integer.toString(state.getScore(0)), 10 + fm.stringWidth(player.getName()) / 3, 120);
		
		player = (Player)players.get(1);
		g.setColor(new Color(player.getColour()));

		if (player.ISBABNESS(player.getName())) {
			Font oldFont = g.getFont();
			g.setFont(oldFont.deriveFont(Font.BOLD).deriveFont(35.f));
			
			g.drawString(player.getName(), JuGameState.WIDTH - 215 - fm.stringWidth(player.getName()), 165);
			g.drawString(Integer.toString(state.getScore(1)), JuGameState.WIDTH - 205 - fm.stringWidth(player.getName()) / 4, 195);
			
			g.setFont(oldFont);
			BufferedImage img = null;
			try {
				img = ImageIO.read(this.getClass().getResource("/resources/bab.png"));
			} catch (IOException e) {
				// This ain't going to happen.
			}
			
			g.drawImage(img.getScaledInstance(400, -1, Image.SCALE_SMOOTH), JuGameState.WIDTH - 400, 200, null);
			float alpha = System.currentTimeMillis()%8000; alpha -= 4000f; if (alpha < 0) alpha = -1.0f*alpha;
			g.setColor(new Color(0.8f,0f,0f, alpha/8000f * 0.3f));
			g.fillRect(JuGameState.WIDTH - 400, 200, 400, 400);
		} else {
			g.drawString(player.getName(), JuGameState.WIDTH - fm.stringWidth(player.getName()) - 10, 100);
			g.drawString(Integer.toString(state.getScore(1)), JuGameState.WIDTH - 2*fm.stringWidth(player.getName()) / 3 - 10, 120);
		}
		
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return results;
	}
	
}
