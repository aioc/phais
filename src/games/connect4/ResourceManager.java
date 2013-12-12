package games.connect4;

import java.awt.Font;

public class ResourceManager {

	private static boolean gotFont = false;
	private static Font myFont;
	
	
	public static Font getFont() {
		if (gotFont) {
			return myFont;
		}
		try {
			myFont = Font.createFont(Font.TRUETYPE_FONT, ResourceManager.class.getResourceAsStream("emulogic.ttf"));
			gotFont = true;
		} catch (Exception e) {
			myFont = new Font("Serif", Font.BOLD, 24);
		}
		return myFont;
	}
	
}
