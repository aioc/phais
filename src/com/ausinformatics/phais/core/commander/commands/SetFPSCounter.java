package com.ausinformatics.phais.core.commander.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.core.Director;
import com.ausinformatics.phais.core.runner.GameVisualisation;

public class SetFPSCounter implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
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
