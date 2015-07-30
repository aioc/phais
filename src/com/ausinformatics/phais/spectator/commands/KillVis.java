package com.ausinformatics.phais.spectator.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.spectator.VisualiserDirector;

public class KillVis<S> implements Command {

    private VisualiserDirector<S> d;
    
    public KillVis(VisualiserDirector<S> d) {
        this.d = d;
    }   
    
    
	@Override
	public void execute(PrintStream out, String[] args) {
		d.stop();
	}

	@Override
	public String shortHelpString() {
		return "Kills the visualiser";
	}

	@Override
	public String detailedHelpString() {
		// TODO Auto-generated method stub
		return null;
	}

}
