package core.visualisation;

import java.awt.Graphics;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;

public class EventBasedFrameVisualiser<S> implements GameInstance {
	
	private GameHandler h;
	private FrameVisualisationHandler<S> v;
	
	public EventBasedFrameVisualiser(GameHandler h, FrameVisualisationHandler<S> v) {
		this.h = h;
		this.v = v;
	}
	
	@Override
	public void begin() {
		h.begin();
	}

	@Override
	public void getVisualisation(Graphics g, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleWindowResize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return h.getResults();
	}
	
	public boolean finishedVisualising() {
		return false;
	}

	public boolean isVisualising() {
		return true;
	}
}
