package com.ausinformatics.phais.spectator;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.ausinformatics.phais.common.commander.Command;
import com.ausinformatics.phais.common.commander.Commander;
import com.ausinformatics.phais.common.commander.ShellCommander;
import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.server.server.SocketTransport;
import com.ausinformatics.phais.spectator.commands.KillVis;
import com.ausinformatics.phais.spectator.commands.SetFPSCounter;
import com.ausinformatics.phais.spectator.interfaces.FrameVisualisationHandler;
import com.ausinformatics.phais.spectator.interfaces.FrameVisualiserFactory;
import com.ausinformatics.phais.spectator.visualisation.EventBasedFrameVisualiser;
import com.ausinformatics.phais.spectator.visualisation.GameVisualisation;

public class VisualiserDirector<S> {

    private EventManager em;
    private FrameVisualiserFactory<S> factory;
    NetworkVisualiser nv;
    private boolean shouldRun;
    
    public VisualiserDirector(EventManager em, FrameVisualiserFactory<S> factory) {
        this.em = em;
        this.factory = factory;
    }
    
    private Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("SETFPS", new SetFPSCounter());
        commands.put("QUIT", new KillVis<>(this));
        return commands;
    }
    
    public void stop() {
        shouldRun = false;
        nv.stop();
    }
    
    public void runForever(String address, int port, int gid, String name) {
        shouldRun = true;
        // Start a commander.
        
        SocketTransport st;
        try {
            st = new SocketTransport(new Socket(address, port));
        } catch (IOException e) {
            System.out.println("Error connecting: " + e.getMessage());
            return;
        }
        st.write("spectator");
        try {
            String ds = st.read();
            if (ds.equals("DETAILS")) {
                st.write("DETAILS " + gid + " 1.0VIS " + name);
            } else {
                System.out.println("Got bad connection string: " + ds);
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not read: " + e.getMessage());
            return;
        }
        Commander c = new ShellCommander(getCommands());
        c.start();
        GameVisualisation gv = new GameVisualisation();
        while (shouldRun) {
            FrameVisualisationHandler<S> handler = factory.createHandler();
            EventBasedFrameVisualiser<S> vis = new EventBasedFrameVisualiser<>(handler);
            nv = new NetworkVisualiser(em, vis, vis);
            try {
                nv.start(st, gv);
            } catch (Exception e) {
                System.out.println("Error while processing game: " + e.getMessage());
                e.printStackTrace();
                System.out.println("The server probably went down.");
                shouldRun = false;
            }
        }
        c.stop();
        System.exit(0);
    }
    
}
