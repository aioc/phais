package games.spies;

public class Statistics {

	public int movesMade;
	public int shotsFired;
	public int killsDone;

	public Statistics() {
		movesMade = 0;
		shotsFired = 0;
		killsDone = 0;
	}

	public Statistics(Statistics s) {
		movesMade = s.movesMade;
		shotsFired = s.shotsFired;
		killsDone = s.killsDone;
	}

}
