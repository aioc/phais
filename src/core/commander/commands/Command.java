package core.commander.commands;

import java.io.PrintStream;

import core.Director;

public interface Command {
	void execute(Director reportTo, PrintStream out, String[] args);
	String shortHelpString();
	String detailedHelpString();
}
