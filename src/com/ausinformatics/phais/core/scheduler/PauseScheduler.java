package com.ausinformatics.phais.core.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ausinformatics.phais.core.Config.Mode;
import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

public class PauseScheduler implements GameScheduler {

	private Set<PersistentPlayer> players;
	
	public PauseScheduler() {
		players = new HashSet<>();
	}
	
	@Override
	public void addPlayer(PersistentPlayer player) {
		synchronized (players) {
			players.add(player);
		}
	}

	@Override
	public void removePlayer(PersistentPlayer player) {
		synchronized (players) {
			players.remove(player);
		}
	}

	@Override
	public boolean hasGame() {
		return false;
	}

	@Override
	public List<PersistentPlayer> getGame() {
		return null;
	}

	@Override
	public void scheduleGame(List<PersistentPlayer> players) {
		
	}

	@Override
	public Mode getMode() {
		return Mode.PAUSE;
	}

	@Override
	public int getNumPlayersPerGame() {
		return 0;
	}

	@Override
	public List<PersistentPlayer> removeWaitingPlayers() {
		synchronized (players) {
			List<PersistentPlayer> wait = new ArrayList<PersistentPlayer>();
			wait.addAll(players);
			players.clear();
			return wait;
		}
	}

}
