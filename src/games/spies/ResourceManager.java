package games.spies;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class ResourceManager {

	private static boolean gotFont = false;
	private static boolean gotImage = false;
	private static Font myFont;
	private static Image myImage;
	
	
	public static Font getFont() {
		if (gotFont) {
			return myFont;
		}
		try {
			myFont = Font.createFont(Font.TRUETYPE_FONT, ResourceManager.class.getResourceAsStream("red_october_regular.ttf"));
			gotFont = true;
		} catch (Exception e) {
			myFont = new Font("Serif", Font.BOLD, 24);
		}
		return myFont;
	}
	
	public static Image getImage() {
		if (gotImage) {
			return myImage;
		}
		try {
			myImage = ImageIO.read(GameVisualiser.class.getResourceAsStream("heart.png"));
		} catch (Exception e) {
			myImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics g = myImage.getGraphics();
			g.setColor(Color.RED);
			g.fillRect(0, 0, 16, 16);
			g.dispose();
		}
		return myImage;
	}
	
}
