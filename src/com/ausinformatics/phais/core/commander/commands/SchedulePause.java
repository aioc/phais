package com.ausinformatics.phais.core.commander.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.core.Config;
import com.ausinformatics.phais.core.Director;
import com.ausinformatics.phais.core.Config.Mode;

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
