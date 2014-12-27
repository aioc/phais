package com.ausinformatics.phais.core.commander.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.core.Director;

public interface Command {
	public void execute(Director reportTo, PrintStream out, String[] args);
	public String shortHelpString();
	public String detailedHelpString();
}
