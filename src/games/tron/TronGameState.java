package games.tron;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class TronGameState {
	private static int UNTOUCHED = -1;
	private int[][] touched;
	private int boardSize;
	// player row
	private int[] pr;
	// player col
	private int[] pc;
	
	public TronGameState(int numPlayers, int boardSize) {
		this.boardSize = boardSize;
		touched = new int[boardSize][boardSize];
		pr = new int[numPlayers];
		pc = new int[numPlayers];
		
		//TODO place the players properly
		pr[0] = 0;
		pc[0] = 0;
		pr[1] = boardSize - 1;
		pc[1] = boardSize - 1;
		
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				touched[i][j] = UNTOUCHED;
			}
		}
		
		for (int i = 0; i < numPlayers; i++) {
			touched[pr[i]][pc[i]] = i;
		}

	}
	
	private boolean inBounds(int coord) {
		return coord >= 0 && coord < touched.length;
	}
	
	private boolean isValidMove(int dr, int dc) {
		if (dr != 0 && dc != 0) {
			return false;
		}
		if ((dr + dc) != 1 && (dr + dc) != -1) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param i index of player making the move
	 * @param dr delta r
	 * @param dc delta c
	 * @return true if player doesn't die from making the move
	 */
	public boolean takeNextMove(int i, int dr, int dc) {
		if (!isValidMove(dr, dc)) {
			return false;
		}
		
		pr[i] += dr;
		pc[i] += dc;
		
		if (!inBounds(pr[i]) || !inBounds(pc[i])) {
			return false;
		}
		
		if (touched[pr[i]][pc[i]] != UNTOUCHED) {
			return false;
		}
		
		touched[pr[i]][pc[i]] = i;
		
		return true;
	}
	
	public Position[] getPlayerPositions(int pointOfView) {
		int numPlayers = pc.length;
		ArrayList<Position> ret = new ArrayList<Position>();
		
		ret.add(new Position(pr[pointOfView], pc[pointOfView]));
		
		for (int i = 0; i < numPlayers; i++) {
			if (i != pointOfView) {
				ret.add(new Position(pr[i], pc[i]));
			}
		}
		
		return ret.toArray(new Position[0]);
	}
	
	public void drawIntoGraphicsContext(Graphics g, int[] colours) {
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (touched[i][j] == UNTOUCHED) {
					g.setColor(new Color(0));
				} else {
					g.setColor(new Color(colours[touched[i][j]]));
				}
				g.drawRect(i, j, 1, 1);
			}
		}
	}
}
