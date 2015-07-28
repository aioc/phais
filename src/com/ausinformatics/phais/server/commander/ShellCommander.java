package com.ausinformatics.phais.server.commander;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.ausinformatics.phais.server.Director;
import com.ausinformatics.phais.server.commander.commands.Command;
import com.ausinformatics.phais.server.commander.commands.DisplayScores;
import com.ausinformatics.phais.server.commander.commands.HelpDisplayer;
import com.ausinformatics.phais.server.commander.commands.KickPlayers;
import com.ausinformatics.phais.server.commander.commands.Kill;
import com.ausinformatics.phais.server.commander.commands.ListPlayers;
import com.ausinformatics.phais.server.commander.commands.ScheduleGame;
import com.ausinformatics.phais.server.commander.commands.SchedulePause;
import com.ausinformatics.phais.server.commander.commands.ScheduleRandom;
import com.ausinformatics.phais.server.commander.commands.ScheduleRoundRobin;
import com.ausinformatics.phais.server.commander.commands.SetVisualiser;

public class ShellCommander implements Commander {
	private Director reportTo;
	private PrintStream out;
	private Map<String, Command> commands;

	public ShellCommander(Director reportTo, Map<String, Command> gameCommands) {
		this.reportTo = reportTo;
		out = System.out;
		commands = new HashMap<String, Command>();
		fillCommands(gameCommands);
	}

	private void fillCommands(Map<String, Command> gameCommands) {
		commands.put("RANDOM", new ScheduleRandom());
		commands.put("ROUNDROBIN", new ScheduleRoundRobin());
		commands.put("PAUSE", new SchedulePause());
		commands.put("LS", new ListPlayers());
		commands.put("LIST", commands.get("LS"));
		commands.put("KICK", new KickPlayers());
		commands.put("PLAY", new ScheduleGame());
		commands.put("VIS", new SetVisualiser());
		commands.put("SCORES", new DisplayScores());
		commands.put("QUIT", new Kill());
		//TODO: Add round command for scheduling a round robin
		
		for (String s : gameCommands.keySet()) {
			commands.put(s, gameCommands.get(s));
		}
		commands.put("HELP", new HelpDisplayer(commands));
		commands.put("?", commands.get("HELP"));
	}

	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		out.println("PHAIS shell commander. Type \"help\" or \"?\" for command list");
		while (reportTo.isRunning()) {
			out.print("$ ");
			String rawInput;
			try {
				rawInput = in.nextLine();
			} catch (Exception e) {
				rawInput = "QUIT";	
			}
			String[] inputTokens = rawInput.split("\\s+");

			String command = inputTokens[0].toUpperCase();
			String[] args = Arrays.copyOfRange(inputTokens, 1, inputTokens.length);

			// TODO make LIST and SCORES give a breakdown when given a specific
			// username
			if (commands.containsKey(command)) {
				commands.get(command).execute(reportTo, out, args);
			} else {
				out.println(command + ": command not found");
			}
		}
		in.close();
		// out.println("Commander exiting...");
	}

}
