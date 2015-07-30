package com.ausinformatics.phais.spectator.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.spectator.visualisation.GameVisualisation;

public class SetFPSCounter implements Command {

	@Override
	public void execute(PrintStream out, String[] args) {
	    if (args.length < 1) {
	        System.out.println("Needs either on or off.");
	        return;
	    }
		if (args[0].equals("on")) {
			GameVisualisation.CALCFPS = true;
		} else {
			GameVisualisation.CALCFPS = false;
		}
	}

	@Override
	public String shortHelpString() {
		return "Sets whether the game should show a FPS counter.";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}
}
