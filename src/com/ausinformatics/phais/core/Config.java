package com.ausinformatics.phais.core;

import java.util.HashMap;
import java.util.Map;

import com.ausinformatics.phais.core.commander.commands.Command;

public class Config {

	public enum Mode {
		RANDOM, ROUND_ROBIN, PAUSE
	}

	public int numPlayersPerGame = 2;
	public Mode mode = Mode.RANDOM;
	public int port = 12317;
	public boolean verbose = false;
	public int timeout = 2000;
	public int maxParallelGames = 2;
	public boolean visualise = true;
	public Map<String, Command> gameCommands = new HashMap<String, Command>();

	public void parseArgs(String[] args) {
		// TODO parse command line arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-p")) {
				i++;
				try {
					port = Integer.parseInt(args[i]);
				} catch (Exception e) {
					System.out.println ("Invalid port");
				}
			} else if (args[i].equals("-t")) {
				i++;
				try {
					timeout = Integer.parseInt(args[i]);
				} catch (Exception e) {
					System.out.println ("Invalid timeout");
				}
			} else if (args[i].equals("-g")) {
				i++;
				try {
					maxParallelGames = Integer.parseInt(args[i]);
				} catch (Exception e) {
					System.out.println ("Invalid parallelGames");
				}
			} else {
				System.out.println ("No idea what " + args[i]);
			}
		}
	}
}
