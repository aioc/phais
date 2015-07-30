package com.ausinformatics.phais.server.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import com.ausinformatics.phais.common.Config.Mode;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public class RandomScheduler implements GameScheduler {
	// higher variance means more randomly chosen games
	private final int VARIANCE = 3;

	private int numGamesSpawned;
	private int numPlayersPerGame;
	private PriorityQueue<PrioritisedPlayer> players;
	private Queue<LinkedList<PersistentPlayer>> gamesQueued;

	public RandomScheduler(int numPlayersPerGame) {
		numGamesSpawned = 0;
		this.numPlayersPerGame = numPlayersPerGame;
		players = new PriorityQueue<PrioritisedPlayer>();
		gamesQueued = new LinkedList<LinkedList<PersistentPlayer>>();
	}

	@Override
	public void addPlayer(PersistentPlayer player) {
		synchronized (players) {
			// the further we are in, the lower the priorities of players become
			int priority = (new Random()).nextInt(VARIANCE) - numGamesSpawned;
			players.add(new PrioritisedPlayer(player, priority));
		}
	}

	@Override
	public void removePlayer(PersistentPlayer player) {
		synchronized (players) {
			players.remove(new PrioritisedPlayer(player, 0));
		}
	}

	@Override
	public void scheduleGame(List<PersistentPlayer> players) {
		gamesQueued.add(new LinkedList<PersistentPlayer>(players));
	}

	@Override
	public boolean hasGame() {
		return !gamesQueued.isEmpty() || players.size() >= numPlayersPerGame;
	}

	@Override
	public List<PersistentPlayer> getGame() {
		LinkedList<PersistentPlayer> ret = null;
		synchronized (players) {
			if (hasGame()) {
				if (gamesQueued.isEmpty()) {
					ret = new LinkedList<PersistentPlayer>();

					for (int i = 0; i < numPlayersPerGame; i++) {
						ret.add(players.remove().getPlayer());
					}
				} else {
					ret = gamesQueued.remove();
				}
		      numGamesSpawned++;
			}
		}
		return ret;
	}
	
	public Mode getMode() {
		return Mode.RANDOM;
	}

	@Override
	public int getNumPlayersPerGame() {
		return numPlayersPerGame;
	}

	@Override
	public List<PersistentPlayer> removeWaitingPlayers() {
		synchronized (players) {
			List<PersistentPlayer> ps = new ArrayList<>();
			while (!players.isEmpty()) {
				ps.add(players.remove().getPlayer());
			}
			return ps;
		}
	}
}