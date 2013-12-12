package games.connect4;

import java.awt.Point;


public class GameState {

	public static final int NO_PLAYER = -1;
	
	public static final int DRAW = 2;
	

	public static final int dx[] = {0, 0, 1, 1, 1, -1, -1, -1};
	public static final int dy[] = {1, -1, 1, 0, -1, 1, 0, -1};
	private int boardWidth, boardHeight;
	private int board[][];
	private boolean gameOver;
	private int winner;
	private int firstSpace[];
	private int totalMoves;
	private Point winningPoints[];
	

	public GameState(int boardHeight, int boardWidth) {
		board = new int[boardHeight][boardWidth];
		firstSpace = new int[boardWidth];
		for (int i = 0; i < boardHeight; i++) {
			for (int j = 0; j < boardWidth; j++) {
				board[i][j] = NO_PLAYER;
			}
		}
		for (int i = 0; i < boardWidth; i++) {
			firstSpace[i] = 0;
		}
		this.boardHeight = boardHeight;
		this.boardWidth = boardWidth;
		gameOver = false;
		winner = -1;
		totalMoves = 0;
		winningPoints = new Point[0];
	}
	
	private boolean wonDir(int r, int c, int dir, int lookingFor) {
		for (int i = 0; i < 4; i++) {
			if (r < 0 || c < 0 || r >= boardHeight || c >= boardWidth) {
				return false;
			}
			if (board[r][c] != lookingFor) {
				return false;
			}
			r += dy[dir];
			c += dx[dir];
		}
		
		return true;
	}
	
	public void makeMove(int playerID, Action a) {
		int col = a.getColumn();
		board[firstSpace[col]][col] = playerID;
		for (int i = 0; i < 8; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					if (wonDir(firstSpace[col] + j, col + k, i, playerID)) {
						winningPoints = new Point[4];
						for (int l = 0; l < 4; l++) {
							winningPoints[l] = new Point(col + k + dx[i] * l, firstSpace[col] + j + dy[i] * l);
						}
						gameOver = true;
						winner = playerID;
						return;
					}
				}
			}
			
		}
		firstSpace[col]++;
		totalMoves++;
		if (totalMoves == boardHeight * boardWidth) {
			gameOver = true;
			winner = DRAW;
		}
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
	
	public int getWinner() {
		return winner;
	}

	public boolean isValidAction(int playerID, Action a) {
		int col = a.getColumn();
		if (col < 0 || col >= boardWidth) {
			return false;
		}
		if (firstSpace[col] >= boardHeight) {
			return false;
		}
		return true;
	}
	
	public int getWidth() {
		return boardWidth;
	}
	
	public int getHeight() {
		return boardHeight;
	}
	
	public int getInCell(int r, int c) {
		return board[r][c];
	}
	
	public Point[] getWinningPoints() {
		return winningPoints;
	}

}
