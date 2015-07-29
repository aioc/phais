package com.ausinformatics.phais.spectator.interfaces;

import java.awt.Graphics2D;
import java.util.List;

import com.ausinformatics.phais.common.events.VisualGameEvent;

public interface FrameVisualisationHandler<S> {

    public S createInitial(VisualGameEvent firstEvent);
    
	public void generateBackground(S state, int sWidth, int sHeight, Graphics2D g);
	
	public void generateState(S state, int sWidth, int sHeight, Graphics2D g);
	
	public void eventCreated(VisualGameEvent e, S state);
	
	public void animateEvents(S state, List<VisualGameEvent> events, int sWidth, int sHeight, Graphics2D g);
	
	public void eventEnded(VisualGameEvent e, S state);
	
}
