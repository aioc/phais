package core.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.Config.Mode;
import core.interfaces.PersistentPlayer;

public class RoundRobinScheduler implements GameScheduler {
	
	private Set<PersistentPlayer> playersAvailable;
	private Map<PersistentPlayer, Set<PersistentPlayer>> matchesPlayed;

	//TODO: Support more than 2 players
	public RoundRobinScheduler() {
		matchesPlayed = new HashMap<>();
		playersAvailable = new HashSet<PersistentPlayer>();
	}

	@Override
	public void addPlayer(PersistentPlayer player) {
		synchronized (matchesPlayed) {
			if (!matchesPlayed.containsKey(player)) {
				matchesPlayed.put(player, new HashSet<PersistentPlayer>());
			}
		}
		synchronized (playersAvailable) {
			playersAvailable.add(player);
		}
	}

	@Override
	public void removePlayer(PersistentPlayer player) {
		synchronized (playersAvailable) {
			playersAvailable.remove(player);
		}
	}

	@Override
	public void scheduleGame(List<PersistentPlayer> players) {
		// Do nothing :p
	}

	@Override
	public boolean hasGame() {
		synchronized (playersAvailable) {
			for (PersistentPlayer p : playersAvailable) {
				for (PersistentPlayer p2 : playersAvailable) {
					if (p != p2) {
						synchronized (matchesPlayed) {
							if (!matchesPlayed.get(p).contains(p2)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<PersistentPlayer> getGame() {
		synchronized (playersAvailable) {
			for (PersistentPlayer p : playersAvailable) {
				for (PersistentPlayer p2 : playersAvailable) {
					if (p != p2) {
						synchronized (matchesPlayed) {
							if (!matchesPlayed.get(p).contains(p2)) {
								matchesPlayed.get(p).add(p2);
								matchesPlayed.get(p2).add(p);
								playersAvailable.remove(p);
								playersAvailable.remove(p2);
								return Arrays.asList(p, p2);
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public Mode getMode() {
		return Mode.ROUND_ROBIN;
	}

	@Override
	public int getNumPlayersPerGame() {
		return 2;
	}

	@Override
	public List<PersistentPlayer> removeWaitingPlayers() {
		List<PersistentPlayer> wait = new ArrayList<PersistentPlayer>();
		synchronized (playersAvailable) {
			for (PersistentPlayer p : playersAvailable) {
				wait.add(p);
			}
			playersAvailable.clear();
			synchronized (matchesPlayed) {
				matchesPlayed.clear();
			}
		}
		return wait;
	}
}
