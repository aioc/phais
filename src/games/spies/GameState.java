package games.spies;

import java.util.ArrayList;
import java.util.List;

public class GameState {

	private int boardSize;
	private int maxHealth;
	private int numPlayers;
	private GamePerson[] allPlayers;
	private List<Integer> killStart;
	
	private GameVisualiser visualReport;

	public GameState(int numPlayers, int boardSize, int maxHealth, GameVisualiser reportTo) {
		this.numPlayers = numPlayers;
		this.boardSize = boardSize;
		this.maxHealth = maxHealth;
		this.visualReport = reportTo;
		allPlayers = new GamePerson[numPlayers];
		killStart = new ArrayList<Integer>();
		// For now, distribute in spiral fashion
		Direction facing = Direction.DOWN;
		int curColumn = (boardSize - 1) / 2;
		int curRow = (boardSize - 1) / 2;
		for (int i = 0; i < numPlayers; i++) {
			Position p = new Position();
			if (facing == Direction.DOWN || facing == Direction.UP) {
				if (facing == Direction.DOWN) {
					p.r = 0;
				} else { // facing == Direction.UP
					p.r = boardSize - 1;
				}
				p.c = curColumn;
				curColumn += ((i / 2) + 1) * (1 - (i % 4));
			} else { // facing == Direction.LEFT || facing == Direction.RIGHT
				if (facing == Direction.LEFT) {
					p.c = boardSize - 1;
				} else { // facing == Direction.RIGHT
					p.c = 0;
				}
				p.r = curRow;
				curRow += ((i / 2) + 1) * (1 - ((i % 4) - 1));
			}
			allPlayers[i] = new GamePerson(p, facing, maxHealth);
			facing = facing.turnRight();
		}
	}

	public void setPlayersAction(int playerID, Action a) {
		allPlayers[playerID].action = a;
	}

	public List<List<Integer>> implementMoves() {
		List<List<Integer>> allDead = new ArrayList<List<Integer>>();
		allDead.add(killStart);
		// And, kill them all!
		for (Integer i : killStart) {
			allPlayers[i].health = 0;
		}
		killStart = new ArrayList<Integer>();
		// Now, simulate all moves simulatenously
		for (int i = 0; i < maxHealth; i++) {
			List<GameEvent> events = new ArrayList<GameEvent>();
			for (int j = 0; j < numPlayers; j++) {
				allPlayers[j].lastMove = allPlayers[j].action.getMove(i);
				if (allPlayers[j].health > 0) {
					if (allPlayers[j].action.getMove(i) != Move.SHOOT) {
						// Do non-shooting first
						GamePerson newP = allPlayers[j].action.getMove(i).applyToPlayer(allPlayers[j]);
						if (newP.position.c < 0 || newP.position.c >= boardSize || newP.position.r < 0
								|| newP.position.r >= boardSize) {
							newP = allPlayers[j];
							allPlayers[j].lastMove = Move.NOP;
						}
						if (allPlayers[j].action.getMove(i).isMoveMove()) {
							newP.stats.movesMade++;
						}
						allPlayers[j] = newP;
					}
				}
			}
			int newHealths[] = new int[numPlayers];
			for (int j = 0; j < numPlayers; j++) {
				newHealths[j] = allPlayers[j].health;
			}
			for (int j = 0; j < numPlayers; j++) {
				if (allPlayers[j].health > 0) {
					if (allPlayers[j].action.getMove(i) == Move.SHOOT) {
						allPlayers[j].stats.shotsFired++;
						// Now FIRE
						Position p = allPlayers[j].position;
						for (int k = 0; k < numPlayers; k++) {
							if (k == j) {
								continue;
							}
							Position pk = allPlayers[k].position;
							if (allPlayers[k].health > 0) {
								if (allPlayers[j].dir == Direction.DOWN) {
									if (pk.c == p.c && pk.r >= p.r) {
										if (newHealths[k] > 0) {
											newHealths[k]--;
											if (newHealths[k] == 0) {
												allPlayers[j].stats.killsDone++;
												events.add(new KilledGameEvent(j, k));
											}
										}
									}
								} else if (allPlayers[j].dir == Direction.LEFT) {
									if (pk.r == p.r && pk.c <= p.c) {
										if (newHealths[k] > 0) {
											newHealths[k]--;
											if (newHealths[k] == 0) {
												allPlayers[j].stats.killsDone++;
												events.add(new KilledGameEvent(j, k));
											}
										}
									}
								} else if (allPlayers[j].dir == Direction.UP) {
									if (pk.c == p.c && pk.r <= p.r) {
										if (newHealths[k] > 0) {
											newHealths[k]--;
											if (newHealths[k] == 0) {
												allPlayers[j].stats.killsDone++;
												events.add(new KilledGameEvent(j, k));
											}
										}
									}
								} else { // allPlayers[j].dir == Direction.RIGHT
									if (pk.r == p.r && pk.c >= p.c) {
										if (newHealths[k] > 0) {
											newHealths[k]--;
											if (newHealths[k] == 0) {
												allPlayers[j].stats.killsDone++;
												events.add(new KilledGameEvent(j, k));
											}
										}
									}
								}
							}
						}
					}
				}
			}
			// Done!
			List<Integer> diedRound = new ArrayList<Integer>();
			for (int j = 0; j < numPlayers; j++) {
				if (allPlayers[j].health > 0 && newHealths[j] == 0) {
					diedRound.add(j);
				}
				allPlayers[j].health = newHealths[j];
			}
			allDead.add(diedRound);
			if (i == maxHealth - 1) {
				for (int j = 0; j < numPlayers; j++) {
					if (allPlayers[j].action.getAmountMovingMoves() == 0) {
						if (allPlayers[j].health > 0 && allPlayers[j].health < maxHealth) {
							allPlayers[j].health++;
							events.add(new HealGameEvent(j));
						}
					}
				}
			}
			visualReport.addStateToVisualise(allPlayers, events);
		}
		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i].preAction = allPlayers[i].action;
			allPlayers[i].action = Action.noAction(maxHealth);
		}
		return allDead;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getBoardSize() {
		return boardSize;
	}
	
	public int getNumberPlayers() {
		return allPlayers.length;
	}

	public GamePerson getPerson(int playerID) {
		return allPlayers[playerID];
	}

	public void killPlayer(int playerID) {
		killStart.add(playerID);
	}

	public boolean isValidAction(int playerID, Action a) {
		if (a.getAmountMoves() != maxHealth) {
			return false;
		}
		if (a.getAmountMovingMoves() > allPlayers[playerID].health) {
			return false;
		}
		return true;
	}

}
