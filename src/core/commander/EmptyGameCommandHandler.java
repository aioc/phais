package core.commander;

import core.interfaces.GameCommandHandler;

public class EmptyGameCommandHandler implements GameCommandHandler {

	@Override
	public boolean handleCommand(String command, String[] args) {
		// Do not handle anything.
		return false;
	}

}
