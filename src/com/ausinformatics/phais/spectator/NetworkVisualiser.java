package com.ausinformatics.phais.spectator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ausinformatics.phais.common.events.EventReceiver;
import com.ausinformatics.phais.common.events.VisualGameEvent;
import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.server.server.SocketTransport;
import com.ausinformatics.phais.spectator.visualisation.GameVisualisation;
import com.ausinformatics.phais.spectator.visualisation.GameVisualiser;

public class NetworkVisualiser {

    private EventManager em;
    private EventReceiver er;
    private GameVisualiser vis;

    public NetworkVisualiser(EventManager eventManager, EventReceiver eventReceiver, GameVisualiser visualiser) {
        em = eventManager;
        er = eventReceiver;
        vis = visualiser;
    }

    public void start(SocketTransport st, GameVisualisation gv) throws IOException {
        String line = st.read();
        if (!line.equals("BEGIN")) {
            System.out.println("Did not get BEGIN... something may be wrong: " + line);
            return;
        }
        gv.show(vis);
        while (!(line = st.read()).equals("END")) {
            int amount;
            try {
                amount = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Could not read amount of events: " + line);
                return;
            }
            List<VisualGameEvent> events = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                events.add(em.fromData(st.read()));
            }
            er.giveEvents(events);
        }
        System.out.println("Finsihed getting events...");
        try {
            while (!vis.finishedVisualising()) {
                Thread.sleep(10);
            }
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Finsihed game!");
        st.write("READY");
    }

}
