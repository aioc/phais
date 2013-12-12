package games.connect4;

import core.server.ClientConnection;
import core.server.DisconnectedException;

public class Action {

	private int col;

	private Action(int col) {
		this.col = col;
	}

	public static Action getAction(ClientConnection c) throws BadProtocolException,
			DisconnectedException {
		String inputString;
		inputString = c.getStrInput();

		String[] tokens = inputString.split("\\s");
		Action finalA;
		if (tokens.length < 2) {
			throw new BadProtocolException("Getting action: Not enough arguments (got " + inputString + ")");
		} else if (tokens.length > 2) {
			throw new BadProtocolException("Getting action: Too many arguments (got " + inputString + ")");
		} else if (!tokens[0].equals("ACTION")) {
			throw new BadProtocolException("Getting action: Invalid identifier (got " + inputString + ")");
		} else {
			int numCol;
			try {
				numCol = Integer.parseInt(tokens[1]);
			} catch (Exception e) {
				throw new BadProtocolException("Getting action: Bad column input (got " + inputString + ")");
			}
			finalA = new Action(numCol);
		}
		return finalA;
	}
	
	public int getColumn() {
		return col;
	}
	
	@Override
	public String toString() {
		return "" + col;
	}
	
	
}
