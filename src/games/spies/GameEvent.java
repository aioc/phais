package games.spies;

import java.util.List;

import core.interfaces.PersistentPlayer;

public interface GameEvent {
	
	public String getRepresentation(List<PersistentPlayer> players);
}
