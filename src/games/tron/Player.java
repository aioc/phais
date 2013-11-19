package games.tron;

import java.util.Random;

import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class Player implements PersistentPlayer {

	private int ID;
	private String name;
	private ClientConnection connection;
	private int colour;
	
	public Player(int ID, ClientConnection connection) {
		this.ID = ID;
		this.connection = connection;
	}
	
	@Override
	public int getID() {
		return ID;
	}

	@Override
	public String getName() {
		if (name == null) {
			generateNewName();
		}
		return name;
	}

	@Override
	public void generateNewName() {
		if (name == null) {
			connection.sendInfo("NAME");
			
			try {
				String inputString = connection.getStrInput();
				String[] tokens = inputString.split("\\s");
				if (tokens.length < 5) {
					connection.disconnect();
				} else if (!tokens[0].equals("NAME")) {
					connection.disconnect();
				} else {
					name = tokens[1];
					//TODO handle colours being too similar to others
					//TODO bounds checking
					int multiplier = 1 << 8;
					int r = Integer.parseInt(tokens[2]);
					int g = Integer.parseInt(tokens[3]);
					int b = Integer.parseInt(tokens[4]);
					colour = r;
					colour *= multiplier;
					colour += g;
					colour *= multiplier;
					colour += b;
				}
			} catch (DisconnectedException e) {
				name = "DisconnectedPlayer " + new Random().nextInt(1000);
				e.printStackTrace();
				// TODO print out something useful
			}
		} else {
			name += new Random().nextInt(1000);
		}
	}

	@Override
	public ClientConnection getConnection() {
		return connection;
	}

	public int getColour() {
		return colour;
	}

}
