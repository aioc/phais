package games.spies;

public enum Move {
	NOP('-', 0),
	UP('^', 1),
	RIGHT('>', 2),
	DOWN('v', 3),
	LEFT('<', 4),
	TURN_LEFT('(', 5),
	TURN_RIGHT(')', 6),
	SHOOT('S', 7),
	INVALID(' ', -1);
	
	private char c;
	private int v;
	
	private Move(char c, int v) {
		this.c = c;
		this.v = v;
	}
	
	public static Move findMove(char c) {
		for (Move m: Move.values()) {
			if (m.c == c) {
				return m;
			}
		}
		return INVALID;
	}
	
	public char getChar() {
		return c;
	}
	
	public boolean isApplyable() {
		return v >= UP.v && v <= TURN_RIGHT.v;
	}
	
	public boolean isMoveMove() {
		return v >= UP.v && v <= LEFT.v;
	}
	
	
	public GamePerson applyToPlayer(GamePerson p) {
		GamePerson gp = new GamePerson(p);
		if (v >= UP.v && v <= LEFT.v) {
			gp.position = gp.position.move(v - 1);
		} else if (this == TURN_LEFT) {
			gp.dir = gp.dir.turnLeft();
		} else if (this == TURN_RIGHT) {
			gp.dir = gp.dir.turnRight();
		}
		return gp;
	}
	
	
	
	
}