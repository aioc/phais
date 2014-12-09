package games.spies;

public enum Direction {
	UP(0),
	RIGHT(1),
	DOWN(2),
	LEFT(3);
	
	private int d;
	
	private Direction(int d) {
		this.d = d;
	}
	
	private static Direction fromValue(int v) {
		return Direction.values()[v];
	}
	
	public Direction turnLeft() {
		return fromValue((d + 3) % 4);
	}
	
	public Direction turnRight() {
		return fromValue((d + 1) % 4);
	}
	
	public Position applyDirection(Position p) {
		return p.move(d);
	}
	
	public String toString() {
		return d + "";
	}

	public double toAngle() {
		return (3+d) * Math.PI / 2;
	}
}
