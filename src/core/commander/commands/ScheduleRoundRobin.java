package core.commander.commands;

import java.io.PrintStream;

import core.Config;
import core.Config.Mode;
import core.Director;

public class ScheduleRoundRobin implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
			Config config = reportTo.getConfig();
			
			config.mode = Mode.ROUND_ROBIN;
			config.numPlayersPerGame = 2;
			
			reportTo.updateConfig(config);
	}

	@Override
	public String shortHelpString() {
		return "Switches the game scheduler to do a round robin.";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
