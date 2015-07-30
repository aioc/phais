package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;
import java.util.Collection;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Director;
import com.ausinformatics.phais.server.interfaces.PersistentPlayer;

public class ListPlayers implements Command {

    private Director d;
    
    public ListPlayers(Director d) {
        this.d = d;
    }   
    
    
	@Override
	public void execute(PrintStream out, String[] args) {
		Collection<PersistentPlayer> players = d.getPlayers();
		if (players.size() == 0) {
			out.println("No players connected.");
		} else {
			out.println("Connected:");
			for (PersistentPlayer p : players) {
				System.out.println(p.getID() + ": " + p.getName());
			}
		}
	}

	@Override
	public String shortHelpString() {
		return "Lists players currently connected to server";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
