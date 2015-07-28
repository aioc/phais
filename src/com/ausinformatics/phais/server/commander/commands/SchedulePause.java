package com.ausinformatics.phais.server.commander.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.server.Config;
import com.ausinformatics.phais.server.Director;
import com.ausinformatics.phais.server.Config.Mode;

public class SchedulePause implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
			Config config = reportTo.getConfig();
			config.mode = Mode.PAUSE;
			reportTo.updateConfig(config);
		
	}

	@Override
	public String shortHelpString() {
		return "Pauses games being created";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
