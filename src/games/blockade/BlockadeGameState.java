package games.blockade;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import core.interfaces.PersistentPlayer;

public class BlockadeGameState {
	private static final int CLEAR = -1;
	
	public static int WIDTH = 1200;
	public static int HEIGHT = 900;
	public static int SQUAREWIDTH = 25;
	public static int DOTSIZE = 32;
	public static int OFFSET = WIDTH/2 - SQUAREWIDTH*13;
	
	private int[][] blockedOff;
	private int boardSize;
	private Position[] playerPos;
	private String[] playerNames;
	private boolean gameOver;
	private int winnerID;

	public BlockadeGameState(int numPlayers, int boardSize) {
		playerPos = new Position[numPlayers];
		playerNames = new String[numPlayers];
		this.boardSize = boardSize;
		blockedOff = new int[boardSize][boardSize];
		if (numPlayers == 2) {
			playerPos[0] = new Position(0, boardSize / 2);
			playerPos[1] = new Position(boardSize - 1, boardSize / 2);
		} else if (numPlayers == 4) {
			playerPos[0] = new Position(0, boardSize / 2);
			playerPos[1] = new Position(boardSize / 2, 0);
			playerPos[2] = new Position(boardSize - 1, boardSize / 2);
			playerPos[3] = new Position(boardSize / 2, boardSize - 1);
		}

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				blockedOff[i][j] = CLEAR;
			}
		}
		gameOver = false;
	}

	public boolean isValidAction(Action a) {
		if (a.getType() == Action.BLOCK) {
			Position p = a.getPos();
			if (p.r < 0 || p.c < 0 || p.r >= boardSize || p.c >= boardSize) {
				return false;
			}
			if (blockedOff[p.r][p.c] != CLEAR) {
				return false;
			}
			for (int i = 0; i < playerPos.length; i++) {
				if (playerPos[i].r == p.r && playerPos[i].c == p.c) {
					return false;
				}
			}
			// Now the hard part
			blockedOff[p.r][p.c] = a.getPlayer();
			for (int i = 0; i < playerPos.length; i++) {
				if (!canReachEnd(i)) {
					blockedOff[p.r][p.c] = CLEAR;
					return false;
				}
			}
			blockedOff[p.r][p.c] = CLEAR;
		} else if (a.getType() == Action.MOVE) {
			// Sanity checking
			if (a.getDir() < 0 || a.getDir() >= 4) {
				return false;
			}
			Position newPos = playerPos[a.getPlayer()].move(a.getDir());
			if (newPos.r < 0 || newPos.c < 0 || newPos.r >= boardSize || newPos.c >= boardSize) {
				return false;
			}

			// Check if collides with anything
			if (blockedOff[newPos.r][newPos.c] != CLEAR) {
				return false;
			}
		}
		return true;
	}

	private boolean canReachEnd(int player) {
		boolean seen[][] = new boolean[boardSize][boardSize];
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				seen[i][j] = false;
			}
		}
		Queue<Position> q = new LinkedList<Position>();
		q.add(playerPos[player]);
		seen[playerPos[player].r][playerPos[player].c] = true;
		while (q.size() > 0 && !endPos(player, q.peek())) {
			Position curP = q.poll();
			for (int i = 0; i < 4; i++) {
				Position newP = curP.move(i);
				if (newP.r >= 0 && newP.c >= 0 && newP.r < boardSize && newP.c < boardSize) {
					if (!seen[newP.r][newP.c] && blockedOff[newP.r][newP.c] == CLEAR) {
						q.add(newP);
						seen[newP.r][newP.c] = true;
					}
				}
			}
		}
		return q.size() > 0;
	}

	private boolean endPos(int player, Position p) {
		if (playerPos.length == 2) {
			if (player == 0) {
				return p.r == boardSize - 1;
			}
			if (player == 1) {
				return p.r == 0;
			}
		} else if (playerPos.length == 2) {
			if (player == 0) {
				return p.r == boardSize - 1;
			}
			if (player == 1) {
				return p.c == boardSize - 1;
			}
			if (player == 2) {
				return p.r == 0;
			}
			if (player == 3) {
				return p.c == 0;
			}
		}
		return false;
	}

	// Assume valid
	public void makeAction(Action a) {
		if (a.getType() == Action.BLOCK) {
			blockedOff[a.getPos().r][a.getPos().c] = a.getPlayer(); 
		} else if (a.getType() == Action.MOVE) {
			playerPos[a.getPlayer()] = playerPos[a.getPlayer()].move(a.getDir());
		}
	}

	public boolean hasPlayerFinished(int player) {
		return endPos(player, playerPos[player]);
	}
	
	public void setPlayerNames(List<PersistentPlayer> players) {
		int i = 0;
		for (PersistentPlayer p: players) {
			playerNames[i] = p.getName();		
			i++;
		}
	}
	
	public void setWinner (int pID) {
		gameOver = true;
		winnerID = pID;
	}

	public void drawIntoGraphicsContext(Graphics g, int[] colours) {
		g.setColor(Color.BLACK);
		g.fillRect(25, 25, WIDTH, HEIGHT);
		Font myFont = new Font("Serif", Font.BOLD, 24);
		g.setFont(myFont);
		FontMetrics fm = g.getFontMetrics(g.getFont());
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (blockedOff[i][j] == CLEAR) {
					g.setColor(Color.WHITE);
				} else {
					Color c = new Color(colours[blockedOff[i][j]]);
					c.darker().darker();
					g.setColor(c);
				}
				int x = OFFSET+50 + (SQUAREWIDTH + 1)*j;
				int y = OFFSET/2 + (SQUAREWIDTH + 1)*i;
				g.fillRect(x, y, SQUAREWIDTH, SQUAREWIDTH);
			}
		}
		for (int i = 0; i < playerPos.length; i++) {
			g.setColor(new Color(colours[i]));
			int x = OFFSET+50 - 1 + (SQUAREWIDTH + 1)*playerPos[i].c;
			int y = OFFSET/2 - 1 + (SQUAREWIDTH + 1)*playerPos[i].r;
			g.fillOval(x, y, SQUAREWIDTH+1, SQUAREWIDTH+1);
		}
		// Whatever. Hardcode.
		
		int x1 = 10;
		int y1 = 25 + HEIGHT/2 + 1;
		g.setColor(new Color(colours[0]));
		g.drawString(playerNames[0], x1, y1);
		drawName(playerNames[0], g, g.getFont(), x1, y1);
		
		int x2 = WIDTH - 1 - fm.stringWidth(playerNames[1]);
		int y2 = 25 + HEIGHT/2 + 1;
		g.setColor(new Color(colours[1]));
		g.drawString(playerNames[1], x2, y2);
		
		drawName(playerNames[1], g, g.getFont(), x2, y2);
		
		if (gameOver) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 25 + HEIGHT/2 - 4/4 * fm.getHeight() + 8, 50 * SQUAREWIDTH, fm.getHeight());
			g.setColor(new Color(colours[winnerID]));
			g.drawString ("PLAYER " + playerNames[winnerID] + " WON!", WIDTH/2 - fm.stringWidth(playerNames[winnerID])/2, 25 + HEIGHT/2+1);
		}
	}
	
	void drawName(String name, Graphics g, Font f, int x, int y) {
		return;
	}
	/*	Graphics2D g2 = (Graphics2D) g;

	    FontRenderContext frc = g2.getFontRenderContext();
	    TextLayout textTl = new TextLayout(name, f, frc);
	    AffineTransform saved = g2.getTransform();
		AffineTransform transform = new AffineTransform();
	    Shape outline = textTl.getOutline(null);
	    transform = g2.getTransform();
	    transform.translate(x, y);
	    g2.transform(transform);
	    g2.setColor(Color.WHITE);
	    g2.draw(outline);
	    g2.setClip(outline);
	    g2.transform(saved);
	}*/
}
