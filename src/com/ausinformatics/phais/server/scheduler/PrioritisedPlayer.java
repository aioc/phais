package com.ausinformatics.phais.server.scheduler;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public class PrioritisedPlayer implements Comparable<PrioritisedPlayer> {
	private PersistentPlayer player;
	private int priority;

	public PrioritisedPlayer(PersistentPlayer player, int priority) {
		this.player = player;
		this.priority = priority;
	}

	public PersistentPlayer getPlayer() {
		return player;
	}

	@Override
	public int compareTo(PrioritisedPlayer p) {
		// higher priority means higher priority
		return p.priority - this.priority;
	}

	public boolean equals(Object o) {
		if (!(o instanceof PrioritisedPlayer)) {
			return false;
		}
		return ((PrioritisedPlayer) o).player.equals(this.player);
	}
}
