package com.ausinformatics.phais.common.commander;

import java.io.PrintStream;

import com.ausinformatics.phais.server.Director;

public interface Command {
	public void execute(Director reportTo, PrintStream out, String[] args);
	public String shortHelpString();
	public String detailedHelpString();
}
