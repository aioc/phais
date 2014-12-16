package core.visualisation;

import java.awt.Graphics2D;
import java.util.List;

public interface FrameVisualisationHandler<S> {

	void generateBackground(int sWidth, int sHeight, Graphics2D g);
	
	void generateState(int sWidth, int sHeight, S state, Graphics2D g);
	
	void eventCreated(VisualGameEvent e);
	
	void animateEvents(List<VisualGameEvent> events);
	
	void eventEnded(VisualGameEvent e, S state);
	
}
