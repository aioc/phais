package core.commander;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import core.Director;
import core.commander.commands.Command;
import core.commander.commands.DisplayScores;
import core.commander.commands.HelpDisplayer;
import core.commander.commands.KickPlayers;
import core.commander.commands.Kill;
import core.commander.commands.ListPlayers;
import core.commander.commands.ScheduleGame;
import core.commander.commands.ScheduleRandom;
import core.commander.commands.SetVisualiser;

public class ShellCommander implements Commander {
	private Director reportTo;
	private PrintStream out;
	private Map<String, Command> commands;

	public ShellCommander(Director reportTo) {
		this.reportTo = reportTo;
		out = System.out;
		commands = new HashMap<String, Command>();
		fillCommands();
	}

	private void fillCommands() {
		commands.put("RANDOM", new ScheduleRandom());
		commands.put("LS", new ListPlayers());
		commands.put("LIST", commands.get("LS"));
		commands.put("KICK", new KickPlayers());
		commands.put("RANDOM", new ScheduleRandom());
		commands.put("PLAY", new ScheduleGame());
		commands.put("VIS", new SetVisualiser());
		commands.put("SCORES", new DisplayScores());
		commands.put("QUIT", new Kill());

		commands.put("HELP", new HelpDisplayer(commands));
		commands.put("?", commands.get("HELP"));
	}

	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		out.println("PHAIS shell commander. Type \"help\" or \"?\" for command list");
		while (reportTo.isRunning()) {
			out.print("$ ");
			String rawInput = in.nextLine();
			String[] inputTokens = rawInput.split("\\s+");

			String command = inputTokens[0].toUpperCase();
			String[] args = Arrays.copyOfRange(inputTokens, 1, inputTokens.length);

			// TODO make LIST and SCORES give a breakdown when given a specific
			// username
			if (commands.containsKey(command)) {
				commands.get(command).execute(reportTo, out, args);
			} else if (command.equals("ROUND")) {
				// change mode so that round robin begins with 2*args[0] games
				// per pairing are played
			} else if (command.equals("SIZE")) {
				// ultimately allow for changing size of game boards, etc.
				/*
				 * } else if (reportTo.isGameCommand(command)) {{
				 * reportTo.passThrough(command, args);
				 */
			} else{
				out.println(command + ": command not found");
			}
		}
		// out.println("Commander exiting...");
	}

}
