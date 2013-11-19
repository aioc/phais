package games.blockade;

public class Position {
	public int r, c;
	
	public static final int[] dx = {0, 1, 0, -1};
	public static final int[] dy = {-1, 0, 1, 0};
	
	public Position(int r, int c) {
		this.r = r;
		this.c = c;
	}
	
	public Position move(int dir) {
		return new Position(r + dy[dir], c + dx[dir]);
	}
	
	public String toString() {
		return r + " " + c;
	}
}
