package com.ausinformatics.phais.server.runner;

import java.util.ArrayList;
import java.util.List;

import com.ausinformatics.phais.server.interfaces.GameInstance;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public class RunnerFactory {
	
	private List<VisualRunner> visualGames;
	private List<StandardGameRunner> standardGames;
	
	public RunnerFactory() {
		visualGames = new ArrayList<VisualRunner>();
		standardGames = new ArrayList<StandardGameRunner>();
	}
	
	public VisualRunner getVisualRunner(GameInstance game, List<PersistentPlayer> players) {
		for (VisualRunner v: visualGames) {
			if (v.canRestart()) {
				v.restart(game, players);
				return v;
			}
		}
		VisualRunner newV = new VisualRunner(game, players);
		visualGames.add(newV);
		return newV;
	}
	
	public StandardGameRunner getStandardRunner(GameInstance game, List<PersistentPlayer> players) {
		for (StandardGameRunner s: standardGames) {
			if (s.canRestart()) {
				s.restart(game, players);
				return s;
			}
		}
		StandardGameRunner newS = new StandardGameRunner(game, players);
		standardGames.add(newS);
		return newS;
	}
	
	
}
