package games.spies;

import core.server.ClientConnection;
import core.server.DisconnectedException;

public class Action {

	private Move moves[];

	private Action(Move moves[]) {
		this.moves = moves;
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
			String s = tokens[1];
			Move moves[] = new Move[s.length()];
			for (int i = 0; i < s.length(); i++) {
				moves[i] = Move.findMove(s.charAt(i));
				if (moves[i] == Move.INVALID) {
					throw new BadProtocolException("Getting action: Invalid move character (got " + inputString + ")");
				}
			}
			finalA = new Action(moves);
		}
		return finalA;
	}
	
	public Move getMove(int index) {
		if (index < 0 || index >= moves.length) {
			return Move.INVALID;
		}
		return moves[index];
	}

	public int getAmountMoves() {
		return moves.length;
	}
	
	public int getAmountMovingMoves() {
		int count = 0;
		for (int i = 0; i < moves.length; i++) {
			if (moves[i] != Move.NOP) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < moves.length; i++) {
			s += moves[i].getChar();
		}
		return s;
	}
	
	public static Action noAction(int length) {
		Move m[] = new Move[length];
		for (int i = 0; i < length; i++) {
			m[i] = Move.NOP;
		}
		return new Action(m);
	}
	
}
