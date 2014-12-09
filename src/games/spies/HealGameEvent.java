package games.spies;

import java.util.List;

import core.interfaces.PersistentPlayer;

public class HealGameEvent implements GameEvent {

	private int healer;

	public HealGameEvent(int healer) {
		this.healer = healer;
	}

	@Override
	public String getRepresentation(List<PersistentPlayer> players) {
		return players.get(healer).getName() + " healed!";
	}

}
