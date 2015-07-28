package com.ausinformatics.phais.server.commander.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.server.Config;
import com.ausinformatics.phais.server.Director;

public class SetVisualiser implements Command {

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		boolean badArgs = false;
		if (args.length != 1) {
			badArgs = true;
		}
		
		if (badArgs) {
			out.println("Usage: vis [on|off]");
		} else {
			Config config = reportTo.getConfig();
			
			// TODO move the messages to director, so the correspond to the recognition of changes
			//     ie. the message gets sent when the action actually occurs
			if (args[0].equals("on")) {
				config.visualise = true;
				out.println("Visualiser enabled");
			} else if (args[0].equals("off")) {
				config.visualise = false;
				out.println("Visualiser disabled");
			}
			
			reportTo.updateConfig(config);
		}
	}

	@Override
	public String shortHelpString() {
		return "Sets the visualiser to be on or off";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
