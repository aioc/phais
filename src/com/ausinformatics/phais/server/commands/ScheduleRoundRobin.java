package com.ausinformatics.phais.server.commands;

import java.io.PrintStream;

import com.ausinformatics.phais.common.Config;
import com.ausinformatics.phais.common.Config.Mode;
import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.server.Director;

public class ScheduleRoundRobin implements Command {

    private Director d;

    public ScheduleRoundRobin(Director d) {
        this.d = d;
    }

    @Override
    public void execute(PrintStream out, String[] args) {
        Config config = d.getConfig();

        config.mode = Mode.ROUND_ROBIN;
        config.numPlayersPerGame = 2;

        d.updateConfig(config);
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
