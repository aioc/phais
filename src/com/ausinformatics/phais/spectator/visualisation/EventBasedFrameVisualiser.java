package com.ausinformatics.phais.spectator.visualisation;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.ausinformatics.phais.common.events.EventReceiver;
import com.ausinformatics.phais.common.events.VisualGameEvent;
import com.ausinformatics.phais.common.events.events.EndGameEvent;
import com.ausinformatics.phais.common.events.events.EndTurnEvent;
import com.ausinformatics.phais.spectator.interfaces.FrameVisualisationHandler;

public class EventBasedFrameVisualiser<S> implements GameVisualiser, EventReceiver {

	private FrameVisualisationHandler<S> v;

	private S curState;

	private List<VisualGameEvent> curEvents;
	private Queue<VisualGameEvent> queuedEvents;

	private Image backImg;
	private Image stateImg;

	private boolean wasVisualising;
	private boolean endGameEventSeen;

	private int curTurn;
	private int markingTurn;
	private boolean shouldRedrawState;

	public EventBasedFrameVisualiser(FrameVisualisationHandler<S> v) {
		this.v = v;
		curState = null; // This will be set by the first state.
		curEvents = new ArrayList<VisualGameEvent>();
		queuedEvents = new ArrayDeque<VisualGameEvent>();
		stateImg = null;
		backImg = null;
		wasVisualising = false;
		endGameEventSeen = false;
		curTurn = 0;
		markingTurn = 0;
		shouldRedrawState = false;
	}

	@Override
	public synchronized void getVisualisation(Graphics2D g, int width, int height) {
	    if (curState == null) {
	        return;
	    }
		// We first paint the background
		if (backImg == null) {
			handleWindowResize(width, height);
		}
		g.drawImage(backImg, 0, 0, width, height, null);

		// Secondly, paint the state
		if (stateImg == null) {
			redrawState(width, height);
		}
		g.drawImage(stateImg, 0, 0, width, height, null);

		// Finally, paint the events ontop
		v.animateEvents(curState, curEvents, width, height, (Graphics2D) g);
		// Now fix up events
		List<VisualGameEvent> newEvents = new ArrayList<>();
		for (VisualGameEvent e : curEvents) {
			e.curFrame++;
			if (e.curFrame == e.totalFrames) {
				v.eventEnded(e, curState);
				shouldRedrawState = true;
				if (e instanceof EndTurnEvent) {
					curTurn++;
				}
			} else {
				newEvents.add(e);
			}
		}
		curEvents = newEvents;
		moveTurnsToCur();
		if (shouldRedrawState) {
			redrawState(width, height);
		}
		wasVisualising = true;
	}

	@Override
	public synchronized void handleWindowResize(int width, int height) {
	    if (curState == null) {
	        return;
	    }
	    backImg = null;
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice().
            getDefaultConfiguration();
        BufferedImage newBackImg = gfx_config.createCompatibleImage(width, height, Transparency.OPAQUE);
		Graphics2D g = newBackImg.createGraphics();
		v.generateBackground(curState, width, height, g);
		g.dispose();
		redrawState(width, height);
		backImg = newBackImg;
	}

	@Override
	public synchronized void windowClosed() {
		wasVisualising = false;
	}

	private void redrawState(int width, int height) {
	    stateImg = null;
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice().
            getDefaultConfiguration();
        BufferedImage newStateImg = gfx_config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = newStateImg.createGraphics();
		v.generateState(curState, width, height, g);
		g.dispose();
		stateImg = newStateImg;
	}

	@Override
	public synchronized void giveEvents(List<VisualGameEvent> events) {
	    if (curState == null) {
	        VisualGameEvent firstEvent = events.get(0);
	        curState = v.createInitial(firstEvent);
	    }
		for (VisualGameEvent e : events) {
			if (e instanceof EndGameEvent) {
				endGameEventSeen = true;
			}
			e.turn = markingTurn;
			queuedEvents.add(e);
			if (e instanceof EndTurnEvent) {
				markingTurn++;
			}
		}
		moveTurnsToCur();
	}

	private void moveTurnsToCur() {
		while (queuedEvents.size() > 0 && queuedEvents.peek().turn <= curTurn) {
			VisualGameEvent ev = queuedEvents.poll();
			v.eventCreated(ev, curState);
			ev.curFrame = 0;
			curEvents.add(ev);
			shouldRedrawState = true;
		}
	}

	public synchronized boolean finishedVisualising() {
		return curEvents.size() == 0 && endGameEventSeen;
	}

	public synchronized boolean isVisualising() {
		return wasVisualising;
	}
}
