package com.ausinformatics.phais.common.commander;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.ausinformatics.phais.server.commands.HelpDisplayer;

public class ShellCommander implements Commander, Runnable {
	private PrintStream out;
	private Map<String, Command> commands;
	private boolean shouldRun;

	public ShellCommander(Map<String, Command> gameCommands) {
		out = System.out;
		commands = new HashMap<String, Command>();
		shouldRun = true;
		fillCommands(gameCommands);
	}

	private void fillCommands(Map<String, Command> gameCommands) {
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
		while (shouldRun) {
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
				commands.get(command).execute(out, args);
			} else {
				out.println(command + ": command not found");
			}
		}
		in.close();
		// out.println("Commander exiting...");
	}

    @Override
    public void start() {
        shouldRun = true;
        Thread running = new Thread(this);
        running.setName("Command listener");
        running.start();
    }

    @Override
    public void stop() {
        shouldRun = false;
        try {
            System.in.close();
        } catch (IOException e) {
        }
    }

}
