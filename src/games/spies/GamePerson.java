package games.spies;

/**
 * Represents the current state of a particular player.
 */
public class GamePerson {
	
	public Position position;
	public Direction dir;
	public int health;
	public Action action;
	public Action preAction;
	public Move lastMove;
	public Statistics stats;
	
	public GamePerson(Position p, Direction dir, int health) {
		position = p;
		this.dir = dir;
		this.health = health;
		lastMove = Move.NOP;
		action = Action.noAction(health);
		preAction = action;
		stats = new Statistics();
	}
	
	public GamePerson(GamePerson p) {
		position = p.position;
		dir = p.dir;
		health = p.health;
		action = p.action;
		preAction = p.preAction;
		lastMove = p.lastMove;
		stats = new Statistics(p.stats);
	}
	
	public String toString() {
		return health + " " + dir + " " + position + " " + preAction;
	}
	
	public void fromGamePerson(GamePerson p) {
		position = p.position;
		dir = p.dir;
		health = p.health;
		action = p.action;
		preAction = p.preAction;
		lastMove = p.lastMove;
		stats = new Statistics(p.stats);
	}
	
	
	
}
