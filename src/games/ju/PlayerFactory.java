package games.ju;

import core.interfaces.PersistentPlayer;
import core.interfaces.PlayerBuilder;
import core.server.ClientConnection;

public class PlayerFactory implements PlayerBuilder {

	@Override
	public PersistentPlayer createPlayer(int ID, ClientConnection client) {
		Player ret = new Player(ID, client);
		ret.generateNewName();
		
		return ret;
	}

}
