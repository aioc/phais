package core.commander.commands;

import java.io.PrintStream;
import java.util.Collection;

import core.Director;
import core.interfaces.PersistentPlayer;

public class ListPlayers implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		Collection<PersistentPlayer> players = reportTo.getPlayers();
		if (players.size() == 0) {
			out.println("No players connected.");
		} else {
			out.println("Connected:");
			for (PersistentPlayer p : players) {
				System.out.println(p.getID() + ": " + p.getName());
			}
		}
	}

	@Override
	public String shortHelpString() {
		return "Lists players currently connected to server";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
