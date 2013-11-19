package games.ju;

import core.server.ClientConnection;
import core.server.DisconnectedException;

/*
 * Protocol bits: 	ACTION n
 * 					ACTION player n
 */

public class Action {

	private int used;
	private int player;

	public Action(int player, int used) {
		this.player = player;
		this.used = used;
	}

	public static Action getAction(int playerNum, ClientConnection c) throws BadProtocolException,
			DisconnectedException {
		String inputString;
		inputString = c.getStrInput();

		String[] tokens = inputString.split("\\s");
		Action finalA;
		if (tokens.length < 2) {
			throw new BadProtocolException("Getting action: Not enough arguments (got " + inputString + ")");
		} else if (!tokens[0].equals("ACTION")) {
			throw new BadProtocolException("Getting action: Invalid identifier (got " + inputString + ")");
		} else {
			int want;
			try {
				want = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				throw new BadProtocolException("Getting move: Bad number (got " + inputString + ")");
			}
			finalA = new Action(playerNum, want);
		}
		return finalA;
	}

	public void sendAction(ClientConnection c) {
		String toSend = "USED " + player + " " + used;
		c.sendInfo(toSend);
	}

	public int getUsed() {
		return used;
	}

	public int getPlayer() {
		return player;
	}

}
