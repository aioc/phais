package games.blockade;

import core.server.ClientConnection;
import core.server.DisconnectedException;

/*
 * Protocol bits: 	ACTION (BLOCK int int|MOVE int) (Client -> Server)
 * 					ACTION int (BLOCK int int|MOVE int) (Server -> Client)
 */

public class Action {

	public static int MOVE = 0;
	public static int BLOCK = 1;

	private int type;
	private int dir;
	private int player;
	private Position p;

	private Action(int player, int dir) {
		type = MOVE;
		this.player = player;
		this.dir = dir;
	}

	private Action(int player, Position p) {
		type = BLOCK;
		this.player = player;
		this.p = p;
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
			if (tokens[1].equals("BLOCK")) {
				if (tokens.length != 4) {
					throw new BadProtocolException("Getting action: Not enough arguments for blocking  (got "
							+ inputString + ")");
				}
				int row, col;
				try {
					row = Integer.parseInt(tokens[2]);
					col = Integer.parseInt(tokens[3]);
				} catch (NumberFormatException e) {
					throw new BadProtocolException("Getting move: Bad numbers (got " + inputString + ")");
				}
				finalA = new Action(playerNum, new Position(row, col));
			} else if (tokens[1].equals("MOVE")) {
				if (tokens.length != 3) {
					throw new BadProtocolException("Getting action: Not enough arguments for moving  (got "
							+ inputString + ")");
				}
				int dir;
				try {
					dir = Integer.parseInt(tokens[2]);
				} catch (NumberFormatException e) {
					throw new BadProtocolException("Getting move: Bad numbers (got " + inputString + ")");
				}
				finalA = new Action(playerNum, dir);
			} else {
				throw new BadProtocolException("Getting action: Invalid action identifier (got " + inputString + ")");
			}
		}
		return finalA;
	}

	public void sendAction(ClientConnection c) {
		c.sendInfo(toString());
	}

	public int getType() {
		return type;
	}

	public int getPlayer() {
		return player;
	}

	public int getDir() {
		return dir;
	}

	public Position getPos() {
		return p;
	}

	@Override
	public String toString() {
		String toSend = "ACTION " + player;
		if (type == BLOCK) {
			toSend += " BLOCK " + p.r + " " + p.c;
		} else {
			toSend += " MOVE " + dir;
		}

		return toSend;
	}

}
