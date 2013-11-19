package core.commander.commands;

import java.io.PrintStream;

import core.Director;

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
