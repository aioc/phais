package com.ausinformatics.phais.server.spectators;

import java.util.List;
import java.util.Random;

import com.ausinformatics.phais.server.interfaces.PersistentPlayer;
import com.ausinformatics.phais.server.server.ClientConnection;
import com.ausinformatics.phais.server.server.DisconnectedException;

public class StandardSpectator implements Spectator {

    private int myId;
    private ClientConnection c;
    private int gId;
    private String name;
    private String watchName;
    
    private boolean ready;

    public StandardSpectator(int myId, ClientConnection c) {
        this.myId = myId;
        this.c = c;
        name = "";
        watchName = "";
        ready = true;
    }

    public void configure() {
        c.sendInfo("DETAILS");
        try {
            String inputString = c.getStrInput();
            String[] tokens = inputString.split("\\s");
            // Response should be DETAILS gID name watch?
            if (tokens.length < 3) {
                c.sendInfo("ERROR Not enough tokens");
                c.disconnect();
                return;
            } else  if (tokens.length > 4) {
                c.sendInfo("ERROR Too many enough tokens");
                c.disconnect();
                return;
            } else if (!tokens[0].equals("DETAILS")) {
                c.sendInfo("ERROR Need details string");
                c.disconnect();
                return;
            } else {
                try {
                    gId = Integer.parseInt(tokens[1]);
                } catch (NumberFormatException e) {
                    c.sendInfo("ERROR First parameter must be an integer");
                    c.disconnect();
                    return;
                }
                if (tokens[2].length() > 16) {
                    c.sendInfo("ERROR Your name is too long");
                    c.disconnect();
                    return;
                }
                name = tokens[2];
                if (tokens.length == 4) {
                    watchName = tokens[3];
                }
            }
        } catch (DisconnectedException e) {
            name = "DisconnectedPlayer " + new Random().nextInt(1000);
        }
    }

    @Override
    public int getId() {
        return myId;
    }

    @Override
    public int getGroupId() {
        return gId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean shouldAddToGame(List<PersistentPlayer> players, List<Spectator> spectators) {
        // It will handle groups. So, we just check if it contains the name we want.
        if (!ready) {
            if (!c.getAsync().equals("READY")) {
                return false;
            }
            ready = true;
        }
        if (watchName.equals("")) {
            return true;
        }
        for (PersistentPlayer p : players) {
            if (p.getName().equals(watchName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ClientConnection getConnection() {
        return c;
    }

    @Override
    public void addedToGame() {
        ready = false;
    }

}
