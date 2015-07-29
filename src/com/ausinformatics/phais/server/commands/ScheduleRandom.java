package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Config;
import com.ausinformatics.phais.server.Director;
import com.ausinformatics.phais.server.Config.Mode;

public class ScheduleRandom implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		boolean badArgs = false;
		if (args.length != 1) {
			badArgs = true;
		} else if (!args[0].matches("^[0-9]{1,3}$")) {
			// 999 players seems to be the limit of sanity
			badArgs = true;
		}

		int numPlayersPerGame = -1;
		try {
			numPlayersPerGame = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			badArgs = true;
			out.println("This should never happen");
		} catch (IndexOutOfBoundsException e) {
			// badArgs should already be true here
			if (!badArgs) {
				out.println("SOMETHING IS BROKEN, TELL KENNETH!");
			}
		}
		
		if (badArgs) {
			out.println("Usage: random #playersPerGame");
		} else {
			Config config = reportTo.getConfig();
			
			config.mode = Mode.RANDOM;
			config.numPlayersPerGame = numPlayersPerGame;
			
			reportTo.updateConfig(config);
		}
	}

	@Override
	public String shortHelpString() {
		return "Switches the game scheduler to create games with random groups of players.";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
