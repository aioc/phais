package core.scheduler;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import core.Config.Mode;
import core.interfaces.PersistentPlayer;

public class RoundRobinScheduler implements GameScheduler {
	private int numPlayersPerGame;

	private Deque<LinkedList<PersistentPlayer>> gamesQueued;
	private Queue<LinkedList<PersistentPlayer>> gamesManuallyQueued;
	private Set<PersistentPlayer> playersAvailable;
	private Set<PersistentPlayer> playersEncountered;

	public RoundRobinScheduler(int numPlayersPerGame) {
		this.numPlayersPerGame = numPlayersPerGame;
		gamesQueued = new LinkedList<LinkedList<PersistentPlayer>>();
		gamesManuallyQueued = new LinkedList<LinkedList<PersistentPlayer>>();
		playersAvailable = new HashSet<PersistentPlayer>();
	}

	private LinkedList<LinkedList<PersistentPlayer>> recurse(int ply, Set<PersistentPlayer> used) {
		LinkedList<LinkedList<PersistentPlayer>> ret = new LinkedList<LinkedList<PersistentPlayer>>();

		if (ply < numPlayersPerGame) {
			for (PersistentPlayer p : playersEncountered) {
				if (!used.contains(p.getID())) {
					used.add(p);

					for (LinkedList<PersistentPlayer> toAdd : recurse(ply + 1, used)) {
						// append myself to the front
						toAdd.addFirst(p);
						ret.add(toAdd);
					}

					used.remove(p);
				}
			}
		} else {
			ret.add(new LinkedList<PersistentPlayer>());
		}
		return ret;
	}

	private LinkedList<LinkedList<PersistentPlayer>> getAllPermutations() {
		LinkedList<LinkedList<PersistentPlayer>> ret = new LinkedList<LinkedList<PersistentPlayer>>();

		ret.addAll(recurse(0, new HashSet<PersistentPlayer>()));

		return ret;
	}

	@Override
	public void addPlayer(PersistentPlayer player) {
		if (!playersEncountered.contains(player)) {
			for (List<PersistentPlayer> permutation : getAllPermutations()) {
				// insert new player at all points
				for (int i = 0; i <= permutation.size(); i++) {
					List<PersistentPlayer> toAdd = new LinkedList<PersistentPlayer>();

					int pos = 0;

					for (PersistentPlayer p : permutation) {
						if (pos == i) {
							toAdd.add(player);
						}

						toAdd.add(p);

						pos++;
					}

					// need to special case out adding at the end
					if (i == permutation.size()) {
						toAdd.add(player);
					}
				}
			}
			playersEncountered.add(player);
		}
		playersAvailable.add(player);
	}

	@Override
	public void removePlayer(PersistentPlayer player) {
		synchronized (playersAvailable) {
			playersAvailable.remove(player);
		}
	}

	@Override
	public void scheduleGame(List<PersistentPlayer> players) {
		gamesManuallyQueued.add(new LinkedList<PersistentPlayer>(players));
	}

	@Override
	public boolean hasGame() {
		// TODO somehow disregard games that aren't playable (because available
		// player isn't there)
		return !gamesManuallyQueued.isEmpty() || !gamesQueued.isEmpty();
	}

	@Override
	public List<PersistentPlayer> getGame() {
		List<PersistentPlayer> ret = null;
		synchronized (gamesQueued) {
			if (hasGame()) {
				if (gamesManuallyQueued.isEmpty()) {
					int numGamesAvail = gamesQueued.size();

					boolean goodGameFound = false;

					for (int i = 0; i < numGamesAvail && !goodGameFound; i++) {
						LinkedList<PersistentPlayer> top = gamesQueued.poll();

						goodGameFound = true;

						for (PersistentPlayer p : top) {
							if (!playersAvailable.contains(p)) {
								goodGameFound = false;
							}
						}
						if (goodGameFound) {
							ret = top;
						} else {
							gamesQueued.add(top);
						}
					}
				} else {
					ret = gamesManuallyQueued.remove();
				}
			}
		}

		return ret;
	}
	
	public Mode getMode() {
		return Mode.ROUND_ROBIN;
	}

	@Override
	public int getNumPlayersPerGame() {
		return numPlayersPerGame;
	}
}
