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
import core.interfaces.GameCommandHandler;

public class ShellCommander implements Commander {
	private Director reportTo;
	private GameCommandHandler gameCommands;
	private PrintStream out;
	private Map<String, Command> commands;

	public ShellCommander(Director reportTo, GameCommandHandler gameCommands) {
		this.reportTo = reportTo;
		this.gameCommands = gameCommands;
		out = System.out;
		commands = new HashMap<String, Command>();
		fillCommands();
	}

	private void fillCommands() {
		commands.put("RANDOM", new ScheduleRandom());
		commands.put("LS", new ListPlayers());
		commands.put("LIST", commands.get("LS"));
		commands.put("KICK", new KickPlayers());
		commands.put("PLAY", new ScheduleGame());
		commands.put("VIS", new SetVisualiser());
		commands.put("SCORES", new DisplayScores());
		commands.put("QUIT", new Kill());
		//TODO: Add round command for scheduling a round robin

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
			} else if (!gameCommands.handleCommand(command, args)) {
				out.println(command + ": command not found");
			}
		}
		in.close();
		// out.println("Commander exiting...");
	}

}
