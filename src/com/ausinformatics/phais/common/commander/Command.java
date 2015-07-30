package com.ausinformatics.phais.common.commander;

import java.io.PrintStream;

public interface Command {
	public void execute(PrintStream out, String[] args);
	public String shortHelpString();
	public String detailedHelpString();
}
