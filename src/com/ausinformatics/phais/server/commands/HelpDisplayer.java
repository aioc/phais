package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Director;

public class HelpDisplayer implements Command {
	Map<String, Command> commands;
	Map<String, Command> listableCommands;

	public HelpDisplayer(Map<String, Command> commands) {
		this.commands = commands;
	}

	/**
	 * Modifies commands so that no two Strings refer to the same
	 * Command.
	 */
	private void juggle() {
		Map<Command, String> rev = new HashMap<Command, String>();
		
		for (Entry<String, Command> e : commands.entrySet()) {
			String name = e.getKey();
			Command command = e.getValue();
			if (rev.containsKey(command)) {
				String newString = rev.get(command) + "/" + name;
				rev.put(command, newString);
			} else {
				rev.put(command, name);
			}
		}

		listableCommands = new HashMap<String, Command>();

		for (Entry<Command, String> e : rev.entrySet()) {
			listableCommands.put(e.getValue(), e.getKey());
		}
	}

	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		if (listableCommands == null) {
			juggle();
		}
		if (args.length == 0) {
			out.println("Available commands\n");
			for (Entry<String, Command> e : listableCommands.entrySet()) {
				out.println(e.getKey() + ": " + e.getValue().shortHelpString());
			}
		} else {
			for (String arg : args) {
				arg = arg.toUpperCase();
				if (commands.containsKey(arg)) {
					if (commands.get(arg).detailedHelpString() != null) {
						out.println("Detailed information about command " + arg + ":");
						out.println(commands.get(arg).detailedHelpString());
					} else {
						out.println("No detailed information about command " + arg + " available");
					}
				} else {
					out.println("No command matches " + arg);
				}
			}
		}
	}

	@Override
	public String shortHelpString() {
		return "Displays this listing of available commands";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}
}
