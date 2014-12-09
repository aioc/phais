package games.spies;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;

import core.interfaces.PersistentPlayer;

public class GameVisualiser {
	private static final int DEFAULT_FRAMES_PER_STATE = 5;
	private static final int SPECIAL_FRAMES_PER_STATE = 15;
	private static final int BORDER_SIZE = 10;
	private static final int SQUARE_BORDER_DIVSOR = 20;

	// Contains all drawing-relevant information at a given turn in the game.
	private static class GameStateInfo extends Pair<List<GamePerson>, List<GameEvent>> {
		GameStateInfo(List<GamePerson> gamePersons, List<GameEvent> gameEvents) {
			super(gamePersons, gameEvents);
		}
	}

	private int boardSize;
	private int maxHealth;
	private boolean isVisualising;
	private List<PersistentPlayer> players;
	private GameStateInfo previousState;
	private GameStateInfo currentState;
	private Integer framesPerThisState;
	private Queue<GameStateInfo> states;
	private List<String> gameEvents;
	private int curFrame; // INV: 0 <= curFrame < framesPerThisState
	private boolean waitingOnNextState; // queue is empty *and* we've rendered all frames already
	private int curRound;
	private Clip music;

	// Render-specific helpers
	BufferedImage prerenderedBackground;
	private int sizeBoard;
	private int sizeSquare;
	private int borderSquareSize;
	private Rectangle boardBox; // The bounding box of the game board.
	private Rectangle paintBox; // The portion of the screen inside the outer border.

	public GameVisualiser(List<PersistentPlayer> pl, int boardSize, int maxHealth) {
		players = pl;
		this.boardSize = boardSize;
		this.maxHealth = maxHealth;
		states = new LinkedList<GameStateInfo>();
		gameEvents = new ArrayList<String>();
		isVisualising = false;
		waitingOnNextState = true;
		framesPerThisState = null;
		curFrame = 0;
		curRound = 0;
		previousState = currentState = null;
		music = null;
	}

	private int getFramesPerThisState() {
		if (framesPerThisState != null) return framesPerThisState;
		framesPerThisState = DEFAULT_FRAMES_PER_STATE;
		PersistentPlayer lastPlayer = players.get(players.size()-1);
		if (Player.isMINARWIN(lastPlayer) && previousState != null) {
			GamePerson gP = previousState.getL().get(players.size()-1);
			boolean awesome = false;
			for (int i = 0; i < players.size(); i++) {
				if (i == players.size()-1) continue;
				GamePerson gP2 = currentState.getL().get(i);
				if (gP2.lastMove != Move.SHOOT) continue;
				if (gP2.dir == Direction.UP && gP2.position.c == gP.position.c && gP2.position.r > gP.position.r) awesome = true;
				if (gP2.dir == Direction.DOWN && gP2.position.c == gP.position.c && gP2.position.r < gP.position.r) awesome = true;
				if (gP2.dir == Direction.RIGHT && gP2.position.r == gP.position.r && gP2.position.c < gP.position.c) awesome = true;
				if (gP2.dir == Direction.LEFT && gP2.position.r == gP.position.r && gP2.position.c > gP.position.c) awesome = true;
			}
			if (gP.lastMove == Move.SHOOT) awesome = true;
			if (awesome) framesPerThisState = SPECIAL_FRAMES_PER_STATE;
		}
		return framesPerThisState;
	}

	private int laserWidth(int frameNum, int framesPerThisState) {
		double x = ((double) frameNum) / framesPerThisState;
		double sigmoid = 1/(1 + Math.exp(-12*(0.5-x)));
		double adj_width = sigmoid * 25 + 5;
		return (int) adj_width;
	}

	public void addStateToVisualise(GamePerson[] newPeople, List<GameEvent> events) {
		List<GamePerson> newL = new ArrayList<GamePerson>();
		for (GamePerson p : newPeople) {
			newL.add(new GamePerson(p));
		}
		GameStateInfo newState = new GameStateInfo(newL, events);
		if (waitingOnNextState) {
			startRenderingState(newState);
		} else {
			states.add(newState);
		}
	}

	/**
	 * Does all the bookkeeping required so that the next render shows a new state.
	 * Pushes game events to the queue, resets curFrame, increments curRound.
	 */
	private void startRenderingState(GameStateInfo newState) {
		previousState = currentState;
		currentState = newState;
		curFrame = 0;
		curRound++;
		framesPerThisState = null;
		addEvents(currentState.getR());
		waitingOnNextState = false;
	}

	public void handleWindowResize(int width, int height) {
		prepareBackground(width, height);
	}

	/**
	 * Recalculates all 'constants' dependent on window size (e.g. board width in pixels);
	 * redraws the pre-drawn background.
	 */
	private void prepareBackground(int sWidth, int sHeight) {
		paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight
				- (2 * BORDER_SIZE));
		sizeBoard = Math.min(paintBox.height - (2 * BORDER_SIZE), (3 * (paintBox.width - (2 * BORDER_SIZE))) / 5);
		if (sizeBoard < 0) {
			sizeBoard = 0;
		}
		boardBox = new Rectangle(paintBox.x + BORDER_SIZE, paintBox.y + BORDER_SIZE, sizeBoard, sizeBoard);
		sizeSquare = sizeBoard / boardSize;
		borderSquareSize = (sizeSquare / SQUARE_BORDER_DIVSOR) + 1;
		// Draw background
		prerenderedBackground = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) prerenderedBackground.getGraphics();
		g.setColor(Color.RED.darker().darker());
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		g.setColor(Color.WHITE);
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				g.fillRect(
					boardBox.x + (j * sizeSquare),
					boardBox.y + (i * sizeSquare),
					sizeSquare - borderSquareSize,
					sizeSquare - borderSquareSize);
			}
		}
	}
	
	/**
	 * Renders a single frame of animation; updates the frame counter.
	 */
	public void visualise(Graphics2D g, int sWidth, int sHeight) {
		if (!isVisualising) {
			isVisualising = true;
			// Set up the background and bounding box sizes for the first time.
			prepareBackground(sWidth, sHeight);
		}
		if (currentState == null) {
			return;
		}
		List<GamePerson> curPeople = currentState.getL();
		List<GamePerson> prevPeople = (previousState == null) ? currentState.getL() : previousState.getL();
		if (paintBox.isEmpty()) {
			return;
		}
		// Draw back
		g.drawImage(prerenderedBackground, 0, 0, null);
		Rectangle textBox = new Rectangle(boardBox.x + boardBox.width + BORDER_SIZE, paintBox.y + BORDER_SIZE,
				paintBox.width - boardBox.width - 2 * BORDER_SIZE, paintBox.height - 2 * BORDER_SIZE);
		g.setColor(Color.RED.darker());
		g.fillRect(textBox.x, textBox.y, textBox.width, textBox.height);
		drawStatBoxes(g, textBox, curPeople);
		// Draw dead players
		for (int i = 0; i < players.size(); i++) {
			if (curPeople.get(i).health == 0) {
				drawPerson(g,
					boardBox,
					sizeSquare,
					borderSquareSize,
					curPeople.get(i),
					prevPeople.get(i),
					new Color(((Player)
							players.get(i)).getColour()),
					Player.isMINARWIN(players.get(i)));
			}
		}
		// Draw lasers
		for (int i = 0; i < players.size(); i++) {
			if (curPeople.get(i).lastMove == Move.SHOOT) {
				drawLaser(g, boardBox, sizeSquare, borderSquareSize, curPeople.get(i),
						new Color(((Player) players.get(i)).getColour()),
						Player.isMINARWIN(players.get(i)));
			}
		}
		// Draw living players
		for (int i = 0; i < players.size(); i++) {
			if (curPeople.get(i).health != 0) {
				drawPerson(g,
					boardBox,
					sizeSquare,
					borderSquareSize,
					curPeople.get(i),
					prevPeople.get(i),
					new Color(((Player)
							players.get(i)).getColour()),
					Player.isMINARWIN(players.get(i)));
			}
		}
		/*
		 * BEGIN HACKING CODE
		 */
		if (players.get(players.size() - 1).getConnection() instanceof MINARWIN) {
			if (music == null) {
				music = MINARWIN.getMusic();
				new Thread(
			            new Runnable() {
			                public void run() {
			                	music.start();
			                }
			            }
			        ).start();
			}
 			if (false) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(this.getClass().getResource(
							"/resources/evgeny.png"));
				} catch (IOException e) {
					// This ain't going to happen.
				}
 			}
		}
		/*
		 * END HACKING CODE
		 */
		curFrame++;
		if (curFrame == getFramesPerThisState()) {
			if (states.size() > 0) {
				startRenderingState(states.remove());
			} else {
				curFrame--;
				waitingOnNextState = true;
			}
		}
		if (curFrame >= getFramesPerThisState() && states.size() > 1) {
			curFrame = 0;
			curRound++;
		}
	}

	private void drawRectangle(Graphics2D g, Rectangle r) {
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	/**
	 * Draw player names, health, shots, etc.
	 */
	private void drawStatBoxes(Graphics2D g, Rectangle textBox, List<GamePerson> curPeople) {
		// Divide into 3 areas: Top (title + turn stuff),
		// Middle (player score cards)
		// Bot (player events)
		Rectangle topBox = new Rectangle(textBox.x, textBox.y, textBox.width, textBox.height / 12);
		Rectangle middleBox = new Rectangle(textBox.x, topBox.y + topBox.height, textBox.width,
				(4 * textBox.height) / 6);
		Rectangle botBox = new Rectangle(textBox.x, middleBox.y + middleBox.height, textBox.width,
				(3 * textBox.height) / 12);
		Font myFont = getFont();
		drawTitle(g, topBox, myFont);
		// Next, work out each player box and draw them.
		int squareSize = (int) Math.ceil(Math.sqrt(players.size()));
		int squareWidth = middleBox.width / squareSize;
		int squareHeight = middleBox.height / squareSize;
		int strokeSize = (Math.min(squareWidth, squareHeight) / 80) + 1;
		squareWidth -= 2 * strokeSize;
		squareHeight -= 2 * strokeSize;
		// Now, loop through all players assigning them a box and drawing them
		for (int i = 0; i < players.size(); i++) {
			int x = i % squareSize;
			int y = i / squareSize;
			Rectangle squarePos = new Rectangle(middleBox.x + (x * (squareWidth + 2 * strokeSize)) + strokeSize,
					middleBox.y + (y * (squareHeight + 2 * strokeSize)) + strokeSize, squareWidth, squareHeight);
			drawPlayerBox(g, myFont, squarePos, (Player) players.get(i), curPeople.get(i), strokeSize);
		}
		drawEvents(g, myFont, botBox);
	}

	private void drawTitle(Graphics2D g, Rectangle titleBox, Font f) {
		String toDraw1 = "Round " + ((curRound / 5) + 1);
		String toDraw2 = ":" + (curRound%5 + 1);
		String toDraw = toDraw1+toDraw2;
		// Make it as big as possible yet still fit.
		f = getLargestFittingFont(f, titleBox, g, toDraw, 256);
		FontMetrics fm = g.getFontMetrics(f);
		g.setFont(f);
		g.setColor(Color.WHITE);
		g.drawString(toDraw1, titleBox.x, titleBox.y + fm.getHeight());
		g.setColor(new Color(50,50,50));
		g.drawString(toDraw2, titleBox.x + ((int)fm.getStringBounds(toDraw1, g).getWidth()), titleBox.y + fm.getHeight());
	}

	private void drawPlayerBox(Graphics2D g, Font f, Rectangle playerBox, Player p, GamePerson gP, int strokeSize) {
		boolean isMINARWIN = Player.isMINARWIN(p);
		Color pColour = isMINARWIN ? Color.WHITE : new Color(p.getColour());
		Color nameColour = isMINARWIN ? Color.RED : Color.WHITE;
		Color textColour = Color.WHITE;
		g.setColor(pColour);
		g.setStroke(new BasicStroke(strokeSize));
		drawRectangle(g, playerBox);
		g.setStroke(new BasicStroke(1));
		// Write their name so that it's either 24 big, or just fits.
		Rectangle nameBox = new Rectangle(playerBox.x, playerBox.y, playerBox.width, playerBox.height / 5);
		f = getLargestFittingFont(f, nameBox, g, p.getName(), 24);
		FontMetrics fm = g.getFontMetrics(f);
		Rectangle2D fR = fm.getStringBounds(p.getName(), g);
		g.fillRect(nameBox.x, nameBox.y, nameBox.width, nameBox.height);
		g.setFont(f);
		g.setColor(nameColour);
		g.drawString(p.getName(), nameBox.x, (nameBox.y + nameBox.height) - (8 * (int) (nameBox.height - fR.getHeight())) / 10);
		// Draw the interior of the box
		Rectangle playerBoxInterior = new Rectangle(playerBox.x + strokeSize, playerBox.y + strokeSize, playerBox.width - 2*strokeSize, playerBox.height - 2*strokeSize);
		// Now, we draw their hearts such that they fill the space (kinda)
		int heartSize = Math.min((playerBoxInterior.width / maxHealth), playerBoxInterior.height / 5);
		int heartGap = heartSize / 10;
		heartSize -= heartGap;
		Image heart = getHeartImage(heartSize);
		Point curHeartP = new Point(playerBoxInterior.x + heartGap, nameBox.y + nameBox.height + heartGap);
		for (int i = 0; i < gP.health; i++) {
			g.drawImage(heart, curHeartP.x, curHeartP.y, null);
			curHeartP.x += heartSize + heartGap;
		}
		// Next, draw some stats
		Rectangle curStatWindow = new Rectangle(playerBoxInterior.x, playerBoxInterior.y + (2 * playerBoxInterior.height) / 5, playerBoxInterior.width, playerBoxInterior.height / 5);
		g.setColor(textColour);
		String movesString = "Moves: " + gP.stats.movesMade;
		String shotsString = "Shots: " + gP.stats.shotsFired;
		String killsString = "Kills: " + gP.stats.killsDone;
		g.setFont(getLargestFittingFont(f, curStatWindow, g, movesString, 24));
		g.drawString(movesString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
		curStatWindow.y += curStatWindow.height;
		g.setFont(getLargestFittingFont(f, curStatWindow, g, shotsString, 24));
		g.drawString(shotsString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
		curStatWindow.y += curStatWindow.height;
		g.setFont(getLargestFittingFont(f, curStatWindow, g, killsString, 24));
		g.drawString(killsString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
	}
	
	private void drawEvents(Graphics2D g, Font f, Rectangle eventBox) {
		// Draw the most recent 5.
		int start = gameEvents.size() - 5;
		if (start < 0) {
			start = 0;
		}
		Point drawPoint = new Point(eventBox.x, eventBox.y);
		Rectangle fitBox = new Rectangle(eventBox.x, eventBox.y, (9 * eventBox.width) / 10, eventBox.height / 5);
		float fontSize = 24f;
		for (int i = 0; i < 5 && i < gameEvents.size(); i++) {
			String text = gameEvents.get(start + i);
			f = getLargestFittingFont(f, fitBox, g, text, 20);
			fontSize = Math.min(fontSize, f.getSize2D());
		}
		g.setFont(f.deriveFont(fontSize));
		for (int i = 0; i < 5 && i < gameEvents.size(); i++) {
			String text = gameEvents.get(start + i);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D textRec = fm.getStringBounds(text, g);
			g.drawString(text, drawPoint.x, drawPoint.y + (int) textRec.getHeight());
			drawPoint.y += (int) textRec.getHeight() + 1;
		}
	}
 
	private Font getLargestFittingFont(Font f, Rectangle r, Graphics2D g, String s, int largestSize) {
		int minSize = 1;
		int maxSize = largestSize;
		while (minSize < maxSize) {
			int midSize = (minSize + maxSize + 1) / 2;
			f = f.deriveFont(Font.PLAIN, midSize);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D fR = fm.getStringBounds(s, g);
			if (fR.getWidth() < r.width && fR.getHeight() < r.height) {
				minSize = midSize;
			} else {
				maxSize = midSize - 1;
			}
		}
		return f.deriveFont(minSize);
	}

	private void drawPerson(
		Graphics2D g, Rectangle boardBox, int squareSize, int borderSquareSize, GamePerson gp,
			GamePerson prevGp, Color c, boolean isMINARWIN) {
		// Draw them onto the board!
		Position p = gp.position;
		Position prePos = gp.position;
		if (gp.lastMove == Move.UP) {
			prePos = Direction.DOWN.applyDirection(p);
		} else if (gp.lastMove == Move.RIGHT) {
			prePos = Direction.LEFT.applyDirection(p);
		} else if (gp.lastMove == Move.DOWN) {
			prePos = Direction.UP.applyDirection(p);
		} else if (gp.lastMove == Move.LEFT) {
			prePos = Direction.RIGHT.applyDirection(p);
		}
		Direction d = gp.dir;
		Direction prevD = prevGp.dir;
		int personSize = squareSize - borderSquareSize;
		Rectangle person = new Rectangle(
			boardBox.x + ((prePos.c * squareSize + ((p.c - prePos.c) * squareSize * curFrame) / getFramesPerThisState())),
			boardBox.y + ((prePos.r * squareSize + ((p.r - prePos.r) * squareSize * curFrame) / getFramesPerThisState())),
			personSize,
			personSize);
		// DRAW BODY
		// Draw empty background
		g.setColor(Color.WHITE);
		g.fillOval(person.x, person.y, person.width, person.height);
		// Set clip and draw interior
		g.setClip(person.x + ((maxHealth - gp.health) * person.width) / maxHealth, person.y, person.width,
				person.height);
		g.setColor(isMINARWIN ? Color.WHITE : c);
		g.fillOval(person.x, person.y, person.width, person.height);
		g.setClip(null);
		// Draw the outline
		g.setColor(isMINARWIN ? Color.RED : c);
		int strokeWidth = (person.width / 20) + 1;
		g.setStroke(new BasicStroke(strokeWidth));
		if (gp.health == 0) {
			g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0));
		}
		g.drawOval(person.x + (strokeWidth / 2), person.y + (strokeWidth / 2), person.width - (strokeWidth / 2) - 2,
				person.height - (strokeWidth / 2) - 2);
		if (isMINARWIN) {
			drawStar(g, new Point((int) person.x + personSize/2, (int) person.y + personSize/2), personSize);
		}

		// DRAW EYE
		if (gp.health != 0) {
			int eyeRadius = personSize / 8;
			double prevAngle = prevD.toAngle();
			double curAngle = d.toAngle();
			while (curAngle - prevAngle > Math.PI) { prevAngle += Math.PI * 2; }
			while (prevAngle - curAngle > Math.PI) { curAngle += Math.PI * 2; }
			double eyeAngle = ((curAngle * curFrame) + (prevAngle * (getFramesPerThisState() - curFrame))) / getFramesPerThisState();
			Point eyeCentre = new Point(
					(int) (person.x + personSize/2 + (Math.cos(eyeAngle) * personSize) / 4),
					(int) (person.y + personSize/2 + (Math.sin(eyeAngle) * personSize) / 4));
			g.setColor(Color.WHITE.darker());
			g.fillOval(eyeCentre.x - eyeRadius, eyeCentre.y - eyeRadius, eyeRadius * 2, eyeRadius * 2);
			int pupilRadius = eyeRadius / 2;
			g.setColor(Color.BLACK);
			g.fillOval(eyeCentre.x - pupilRadius, eyeCentre.y - pupilRadius, pupilRadius * 2, pupilRadius * 2);
		}
	}

	private void drawLaser(Graphics2D g, Rectangle boardBox, int squareSize, int borderSquareSize, GamePerson gp,
			Color c, boolean isMINARWIN) {
		Rectangle laser = new Rectangle();
		Direction d = gp.dir;
		int laserWidth = (squareSize / laserWidth(curFrame, getFramesPerThisState())) + 1;
		int remainingWidth = (squareSize - laserWidth) / 2;
		if (d == Direction.UP || d == Direction.DOWN) {
			laser.width = laserWidth;
			laser.x = squareSize * (gp.position.c) + remainingWidth;
			if (d == Direction.UP) {
				laser.height = squareSize * (gp.position.r) + squareSize / 2;
				laser.y = 0;
			} else { // d == Direction.DOWN
				laser.height = squareSize * (boardSize - gp.position.r - 1) + squareSize / 2;
				laser.y = (squareSize * boardSize) - laser.height;
			}
		} else {
			laser.height = laserWidth;
			laser.y = squareSize * (gp.position.r) + remainingWidth;
			if (d == Direction.LEFT) {
				laser.width = squareSize * (gp.position.c) + squareSize / 2;
				laser.x = 0;
			} else { // d == Direction.LEFT
				laser.width = squareSize * (boardSize - gp.position.c - 1) + squareSize / 2;
				laser.x = (squareSize * boardSize) - laser.width;
			}
		}
		g.setColor(isMINARWIN ? Color.RED : c);
		g.fillRect(laser.x + boardBox.x, laser.y + boardBox.y, laser.width, laser.height);
	}
	
	private void addEvents(List<GameEvent> events) {
		for (GameEvent e : events) {
			gameEvents.add((gameEvents.size() + 1) + ": " + e.getRepresentation(players));
		}
	}

	public boolean stillHasStatesQueued() {
		return states.size() != 0;
	}

	public boolean hasVisualised() {
		return isVisualising;
	}

	private Font getFont() {
		return ResourceManager.getFont();
	}

	private BufferedImage getHeartImage(int size) {
		BufferedImage finalBi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) finalBi.getGraphics();
		//g.drawImage(ResourceManager.getImage(), 0, 0, size - 1, size - 1, 0, 0, 15, 15, null);
		double x1Points[] = {0.4, 0, 0.3, 0.5, 0.8, 1.0, 1.0};
		double y1Points[] = {1.0, 0.3, 0, 0.3, 0.0, 0.1, 0.4};
		int x[] = new int[x1Points.length];
		int y[] = new int[y1Points.length];
		for (int i = 0; i < x1Points.length; i++) {
			        x[i] = (int) (x1Points[i] * size);
			        y[i] = (int) (y1Points[i] * size);
		};
		g.setColor(Color.WHITE);
		g.fillPolygon(x, y, x.length);
		g.dispose();
		return finalBi;
	}

	private void drawStar(Graphics2D g, Point centre, int personSize) {
		int x[] = new int[10];
		int y[] = new int[10];
		for (int i = 0; i < x.length; i++) {
			double rad = (i % 2 == 0) ? ((double) personSize/4) : ((double) personSize/8);
			double angle = i * 2 * Math.PI / 10 - Math.PI/2;
			x[i] = (int) (centre.x + Math.cos(angle) * rad);
			y[i] = (int) (centre.y + Math.sin(angle) * rad);
		}
		g.setColor(Color.RED);
		g.fillPolygon(x, y, x.length);
	}

}
