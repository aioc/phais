package games.connect4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import core.interfaces.PersistentPlayer;

public class GameVisualiser {
	private static final int BORDER_SIZE = 10;

	private List<PersistentPlayer> players;
	private GameState state;

	public GameVisualiser(List<PersistentPlayer> pl, GameState state) {
		players = pl;
		this.state = state;
	}

	public void visualise(Graphics2D g, int sWidth, int sHeight) {
		Rectangle paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight
				- (2 * BORDER_SIZE));
		if (paintBox.isEmpty()) {
			return;
		}
		g.setColor(Color.BLACK);
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		Rectangle titleBox = new Rectangle(paintBox.x, paintBox.y, paintBox.width, paintBox.height / 10);
		Rectangle boardBox = new Rectangle(paintBox.x, titleBox.y + titleBox.height, paintBox.width, paintBox.height
				- titleBox.height);
		int sizeWidth = boardBox.width / state.getWidth();
		int sizeHeight = boardBox.height / state.getHeight();
		int cellBorderSize = Math.min(sizeWidth / 20, sizeHeight / 20);
		Color colours[] = { Color.RED, Color.BLUE };
		for (int i = 0; i < state.getHeight(); i++) {
			for (int j = 0; j < state.getWidth(); j++) {
				Rectangle cellBox = new Rectangle(boardBox.x + j * sizeWidth, boardBox.y + i * sizeHeight, sizeWidth
						- cellBorderSize, sizeHeight - cellBorderSize);
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(cellBox.x, cellBox.y, cellBox.width, cellBox.height);
				if (state.getInCell(state.getHeight() - i - 1, j) != GameState.NO_PLAYER) {
					g.setColor(colours[state.getInCell(state.getHeight() - i - 1, j)]);
					g.setStroke(new BasicStroke(cellBorderSize));
					boolean drawFull = false;
					if (state.isGameOver()) {
						for (int k = 0; k < state.getWinningPoints().length && !drawFull; k++) {
							if (state.getWinningPoints()[k].x == j && state.getWinningPoints()[k].y == state.getHeight() - i - 1) {
								drawFull = true;
							}
						}
					}
					if (drawFull) {
						g.fillOval(cellBox.x + cellBorderSize, cellBox.y + cellBorderSize, cellBox.width - 2
								* cellBorderSize, cellBox.height - 2 * cellBorderSize);
					} else {
						g.drawOval(cellBox.x + cellBorderSize, cellBox.y + cellBorderSize, cellBox.width - 2
								* cellBorderSize, cellBox.height - 2 * cellBorderSize);
					}
				}
			}
		}
		String title = players.get(0).getName() + " vs " + players.get(1).getName();
		Font f = getLargestFittingFont(getFont(), titleBox, g, title, 72);
		g.setFont(f);
		g.setColor(colours[0]);
		FontMetrics fm = g.getFontMetrics(f);
		g.drawString(players.get(0).getName(), titleBox.x, titleBox.y + fm.getHeight());
		g.setColor(Color.WHITE);
		g.drawString(" vs ", titleBox.x + (int) fm.getStringBounds(players.get(0).getName(), g).getWidth(), titleBox.y
				+ fm.getHeight());
		g.setColor(colours[1]);
		g.drawString(players.get(1).getName(),
				titleBox.x + (int) fm.getStringBounds(players.get(0).getName() + " vs ", g).getWidth(),
				titleBox.y + fm.getHeight());
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

	private Font getFont() {
		return ResourceManager.getFont();
	}

}
