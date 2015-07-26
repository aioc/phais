package com.ausinformatics.phais.core.visualisation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.ausinformatics.phais.core.interfaces.GameInstance;
import com.ausinformatics.phais.core.interfaces.PersistentPlayer;

public class EventBasedFrameVisualiser<S> implements GameInstance {

	private GameHandler h;
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

	public EventBasedFrameVisualiser(GameHandler h, FrameVisualisationHandler<S> v, S initialState) {
		this.h = h;
		this.v = v;
		curState = initialState;
		curEvents = new ArrayList<VisualGameEvent>();
		queuedEvents = new ArrayDeque<VisualGameEvent>();
		stateImg = null;
		backImg = null;
		wasVisualising = false;
		endGameEventSeen = false;
		curTurn = 0;
		markingTurn = 0;
	}

	@Override
	public void begin() {
		h.begin();
	}

	@Override
	public synchronized void getVisualisation(Graphics g, int width, int height) {
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
		boolean stateChanged = false;
		for (VisualGameEvent e : curEvents) {
			e.curFrame++;
			if (e.curFrame == e.totalFrames) {
				v.eventEnded(e, curState);
				stateChanged = true;
				if (e instanceof EndTurnEvent) {
					curTurn++;
				}
			} else {
				newEvents.add(e);
			}
		}
		curEvents = newEvents;
		moveTurnsToCur();
		if (stateChanged) {
			redrawState(width, height);
		}
		wasVisualising = true;
	}

	@Override
	public synchronized void handleWindowResize(int width, int height) {
		BufferedImage newBackImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
		BufferedImage newStateImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < newStateImg.getHeight(); i++) {
			for (int j = 0; j < newStateImg.getWidth(); j++) {
				newStateImg.setRGB(j, i, 0x00000000);
			}
		}
		Graphics2D g = newStateImg.createGraphics();
		v.generateState(curState, width, height, g);
		g.dispose();
		stateImg = newStateImg;
	}

	public synchronized void giveEvents(List<VisualGameEvent> events) {
		for (VisualGameEvent e : events) {
			if (e instanceof EndGameEvent) {
				endGameEventSeen = true;
				return;
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
			v.eventCreated(ev);
			ev.curFrame = 0;
			curEvents.add(ev);
		}
	}

	public synchronized S getCurState() {
		return curState;
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return h.getResults();
	}

	public synchronized boolean finishedVisualising() {
		return curEvents.size() == 0 && endGameEventSeen;
	}

	public synchronized boolean isVisualising() {
		return wasVisualising;
	}

}
