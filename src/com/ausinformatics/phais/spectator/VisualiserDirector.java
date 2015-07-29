package com.ausinformatics.phais.spectator;

import java.io.IOException;
import java.net.Socket;

import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.server.server.SocketTransport;
import com.ausinformatics.phais.spectator.interfaces.FrameVisualisationHandler;
import com.ausinformatics.phais.spectator.interfaces.FrameVisualiserFactory;
import com.ausinformatics.phais.spectator.visualisation.EventBasedFrameVisualiser;

public class VisualiserDirector<S> {

    private EventManager em;
    private FrameVisualiserFactory<S> factory;
    
    public VisualiserDirector(EventManager em, FrameVisualiserFactory<S> factory) {
        this.em = em;
        this.factory = factory;
    }
    
    public void runForever(String address, int port, int gid) {
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
                st.write("DETAILS " + gid + " 1.0VIS");
            } else {
                System.out.println("Got bad connection string: " + ds);
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not read: " + e.getMessage());
            return;
        }
        while (true) {
            FrameVisualisationHandler<S> handler = factory.createHandler();
            EventBasedFrameVisualiser<S> vis = new EventBasedFrameVisualiser<>(handler);
            NetworkVisualiser nv = new NetworkVisualiser(em, vis, vis);
            try {
                nv.start(st);
            } catch (IOException e) {
                System.out.println("Error while processing game: " + e.getMessage());
            }
        }
    }
    
}
