package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Director;

public class Kill implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		reportTo.kill();
	}

	@Override
	public String shortHelpString() {
		return "Kills the server";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
