package com.ausinformatics.phais.core.commander.commands;

import java.io.PrintStream;
import java.util.Map;

import com.ausinformatics.phais.core.Director;
import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

public class DisplayScores implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		Map<PersistentPlayer, Integer> scores = reportTo.getScores();
		out.println("Scores:");
		for (PersistentPlayer p : scores.keySet()) {
			out.println(p.getName() + ": " + scores.get(p));
		}
	}

	@Override
	public String shortHelpString() {
		return "Displays a table of the players' scores.";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
