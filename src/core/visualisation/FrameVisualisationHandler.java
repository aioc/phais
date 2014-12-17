package core.visualisation;

import java.awt.Graphics2D;
import java.util.List;

public interface FrameVisualisationHandler<S> {

	void generateBackground(S state, int sWidth, int sHeight, Graphics2D g);
	
	void generateState(S state, int sWidth, int sHeight, Graphics2D g);
	
	void eventCreated(VisualGameEvent e);
	
	void animateEvents(S state, List<VisualGameEvent> events, int sWidth, int sHeight, Graphics2D g);
	
	void eventEnded(VisualGameEvent e, S state);
	
}
