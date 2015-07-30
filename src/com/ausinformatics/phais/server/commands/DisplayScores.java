package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;
import java.util.Map;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Director;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public class DisplayScores implements Command {

    private Director d;
    
    public DisplayScores(Director d) {
        this.d = d;
    }   
    
	@Override
	public void execute(PrintStream out, String[] args) {
		Map<PersistentPlayer, Integer> scores = d.getScores();
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
