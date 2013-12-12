package core.commander.commands;

import java.io.PrintStream;

import core.Director;
import core.interfaces.PersistentPlayer;

public class KickPlayers implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		if (args.length != 0) {
			for (String name : args) {
				PersistentPlayer toKick = reportTo.getPlayerFromName(name);
				if (toKick == null) {
					out.println("Error: " + name + " is not a connected player.");
				} else {
					reportTo.deregisterPlayer(toKick);
					out.println("Kicked " + name);
				}
			}
		} else {
			out.println("Usage: kick PLAYERS...");
		}
	}

	@Override
	public String shortHelpString() {
		return "Kicks the listed players";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}
}