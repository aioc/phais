package com.ausinformatics.phais.core.scorer;

import java.util.ArrayList;
import java.util.Map;

import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

public class StandardScoreKeeper implements ScoreKeeper {
	private ArrayList<Integer> scores;
	// delta[i][j] = amounts of points that player 'i' has gained
	// from games involving player 'j'
	private ArrayList<ArrayList<Integer>> delta;

	public StandardScoreKeeper() {
		scores = new ArrayList<Integer>();
		delta = new ArrayList<ArrayList<Integer>>();
	}

	@Override
	public void registerPlayer(PersistentPlayer player) {
		while (scores.size() <= player.getID()) {
			scores.add(0);
		}
		while (delta.size() <= player.getID()) {
			delta.add(new ArrayList<Integer>());
		}

		for (ArrayList<Integer> deltas : delta) {
			while (deltas.size() <= player.getID()) {
				deltas.add(0);
			}
		}
	}

	@Override
	public void deregisterPlayer(PersistentPlayer player) {
		for (int i = 0; i < scores.size(); i++) {
			int newScore = scores.get(i) - delta.get(i).get(player.getID());
			scores.set(i, newScore);
			delta.get(i).set(player.getID(), 0);
		}
	}

	@Override
	public void submitGame(Map<PersistentPlayer, Integer> results) {
		for (PersistentPlayer p : results.keySet()) {
			if (p.getConnection().isConnected()) {
				int newScore = scores.get(p.getID()) + results.get(p);
				scores.set(p.getID(), newScore);

				for (PersistentPlayer p2 : results.keySet()) {
					int newDelta = delta.get(p.getID()).get(p2.getID()) + results.get(p);
					delta.get(p.getID()).set(p2.getID(), newDelta);
				}
			}
		}
	}

	@Override
	public int getScore(PersistentPlayer player) {
		return scores.get(player.getID());
	}
}
