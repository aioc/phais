package com.ausinformatics.phais.core.commander.commands;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import com.ausinformatics.phais.core.Director;
import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

public class ScheduleGame implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {

		List<PersistentPlayer> players = new LinkedList<PersistentPlayer>();

		for (String name : args) {
			PersistentPlayer toAdd = reportTo.getPlayerFromName(name);
			if (toAdd == null) {
				out.println(name + " is not a connected player");
			} else {
				players.add(toAdd);
			}
		}

		if (players.size() != args.length) {
			out.println((args.length - players.size()) + " players not found, try again.");
		} else {
			out.println("Adding game to queue");
			reportTo.addGameToQueue(players);
		}
	}

	@Override
	public String shortHelpString() {
		return "Add a game to the queue of games to be spawned";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
