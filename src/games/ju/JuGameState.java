package games.ju;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class JuGameState {

	private static final int NOT_PLAYED = -1;
	public static int WIDTH = 1024;
	public static int HEIGHT = 700;
	public static int CARDWIDTH = 50;
	public static int DOTSIZE = 35;
	private int numPlayers;
	private int numRounds;
	private boolean hasUsed[][];
	private int values[];
	private int curRound;
	private int playerScores[];
	private int curPlayed[];
	private int playerHistory[][];

	public JuGameState(int numPlayers, int numRounds, int initVals[]) {
		this.numPlayers = numPlayers;
		this.numRounds = numRounds;
		curRound = 0;
		hasUsed = new boolean[numPlayers][numRounds];
		values = new int[numRounds];
		playerHistory = new int[numPlayers][numRounds];
		for (int i = 0; i < numRounds; i++) {
			values[i] = initVals[i];
		}
		playerScores = new int[numPlayers];
		curPlayed = new int[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			curPlayed[i] = NOT_PLAYED;
		}
	}

	public boolean isValidAction(Action a) {
		int num = a.getUsed();
		int pla = a.getPlayer();
		if (num < 0 || num >= numRounds) {
			return false;
		}
		try {
			if (hasUsed[pla][num]) {
				return false;
			}
		} catch (Exception e) {
			System.out.println ("Stuff " + pla + " " + num + " " + numRounds + " ");
			return false;
		}
		return true;
	}

	// Assume valid
	public void makeAction(Action a) {
		curPlayed[a.getPlayer()] = a.getUsed();
		hasUsed[a.getPlayer()][a.getUsed()] = true;
	}

	public void evaluateRound() {
		List<Integer> winningPlayers = new ArrayList<Integer>();
		int curMax = 0;
		for (int i = 0; i < numPlayers; i++) {
			if (curPlayed[i] > curMax) {
				winningPlayers.clear();
				curMax = curPlayed[i];
			}
			if (curPlayed[i] == curMax) {
				winningPlayers.add(i);
			}
			playerHistory[i][curRound] = curPlayed[i];
		}
		if (winningPlayers.size() > 0) {
			int amoGive = values[curRound] / winningPlayers.size();
			for (Integer i: winningPlayers) {
				playerScores[i] += amoGive;
			}
		}
		curRound++;
		for (int i = 0; i < numPlayers; i++) {
			curPlayed[i] = NOT_PLAYED;
		}
	}

	public int getScore(int player) {
		return playerScores[player];
	}

	public void drawToGraphicsContext(Graphics g, int[] colours) {
		CARDWIDTH = WIDTH / 35;
		DOTSIZE = HEIGHT / 20;
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int nCards = numRounds-curRound;
		for (int i = 0; i < numPlayers; i++) {
			int x = 0;
			for (int j = 0; j < numRounds; j++) {
				if (!hasUsed[i][j]) {
					drawCard(g, j, x*CARDWIDTH + (WIDTH/2)-(nCards*CARDWIDTH-1)/2, i*CARDWIDTH,
							CARDWIDTH-1,CARDWIDTH-1);
					x++;
				}
			}
		}
		int sum = 0;
		for (int i = 0; i < numRounds; i++) {
			int minY = numPlayers*CARDWIDTH*2 + DOTSIZE * (1+i);
			if (i < curRound) {
				if (numPlayers == 2) {
					int play[] = {playerHistory[0][i], playerHistory[1][i]};
					if (play[0] == play[1]) {
						g.setColor(getScaledColor(play[0]));
						g.fillRect(WIDTH/2 - DOTSIZE * 2, minY + 1, DOTSIZE*4, DOTSIZE - 1);
					} else {
						int w[] = {DOTSIZE * 2, DOTSIZE * 2};
						if (play[0] < play[1]) {
							w[0] = DOTSIZE-1;
							w[1] = DOTSIZE * 5 / 2;
						} else {
							w[1] = DOTSIZE-1;
							w[0] = DOTSIZE * 5 / 2;
						}
						g.setColor(getScaledColor(play[0]));
						g.fillRect(WIDTH/2 - DOTSIZE*2, minY + 1, w[0],DOTSIZE - 1);
						g.setColor(getScaledColor(play[1]));
						g.fillRect(WIDTH/2 + DOTSIZE*2 - w[1], minY + 1, w[1],DOTSIZE - 1);
					}
					g.setColor(getScaledColor(play[0]));
					g.drawString(Integer.toString(playerHistory[0][i] + 1), WIDTH/2 - DOTSIZE*2 - 20, minY + 3*DOTSIZE/4 - 1);
					g.setColor(getScaledColor(play[1]));
					g.drawString(Integer.toString(playerHistory[1][i] + 1), WIDTH/2 + DOTSIZE*2 + 10, minY + 3*DOTSIZE/4 - 1);
				}
				g.setColor(Color.BLACK);
			} else {
				g.setColor(Color.WHITE);
			}

			sum += values[i];
			String s = Integer.toString(values[i]);
			g.drawString(s, WIDTH/2 - fm.stringWidth(s)/2, minY + 3*DOTSIZE/4 - 1);
		}
		int lWidth = 0;
		int rWidth = 0;
		g.setColor(Color.WHITE);
		g.fillRect(0, HEIGHT - 50, WIDTH, 10);
		for (int i = 0; i < numRounds; i++) {
			int width = (int)(WIDTH*(double)((double)values[i] / (double)sum));
			if (playerHistory[0][i] < playerHistory[1][i]) {
				// Right hand player won this round.
				g.setColor(getScaledColor(playerHistory[1][i]));
				g.fillRect(WIDTH - rWidth - width, HEIGHT - 50, width, 10);
				rWidth += width;
			} else if (playerHistory[0][i] > playerHistory[1][i]) {
				// Left hand player won this round
				g.setColor(getScaledColor(playerHistory[0][i]));
				g.fillRect(lWidth, HEIGHT - 50, width, 10);
				lWidth += width;
			} else if (i < curRound) {
				// Tie
				g.setColor(getScaledColor(playerHistory[0][i]));
				g.fillRect(lWidth, HEIGHT - 50, width / 2, 10);
				g.fillRect(WIDTH - rWidth - width/2, HEIGHT - 50, width / 2, 10);
				lWidth += width / 2;
				rWidth += width / 2;
			}
		}
		g.setColor(Color.BLACK);
		g.fillRect(WIDTH/2, HEIGHT - 50, 1, 10);
		return;
	}
	private void drawCard(Graphics g, int cardId, int x, int y, int w, int h) {
		// draw card bg
		g.setColor(getScaledColor(cardId));
		g.fillRect(x,y,w,h);
		// draw text
		g.setColor(Color.BLACK);
		String tempString = Integer.toString(cardId+1);
		g.drawString(tempString, x, y+h);
	}
	private Color getScaledColor(int cardId) {
		float extent = ((float)cardId)/(numRounds-1);
		float v[][] = {
				// r, g, b, location
				{0.3F,0.7F,0.3F,0F},
				{0.85F,0.85F,0,2/5F},
				{1,0,0,1F}
		};
		for (int i = 0; ; i++) {
			if (v[i+1][3] >= extent) {
				extent = (extent - v[i][3])/(v[i+1][3]-v[i][3]);
				return new Color(
						v[i+1][0]*extent + v[i][0]*(1F-extent),
						v[i+1][1]*extent + v[i][1]*(1F-extent),
						v[i+1][2]*extent + v[i][2]*(1F-extent)
						);
			}
		}
	}
}
