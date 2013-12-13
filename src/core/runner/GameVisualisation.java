package core.runner;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;

import core.interfaces.GameInstance;

public class GameVisualisation extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6362354475690588421L;
	private static final String DEFAULT_TITLE = "";
	private static final int DEFAULT_WIDTH = 1280;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int MSPF = 30;

	private GameInstance game;
	private JFrame theFrame;
	private boolean toClose;
	private long preTime;

	public GameVisualisation() {
		String title = DEFAULT_TITLE;
		try {
			title = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {}
		theFrame = new JFrame(title);
		setBackground(Color.BLACK);
		theFrame.setBounds(10, 10, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		theFrame.setExtendedState(Frame.NORMAL);
		theFrame.setUndecorated(false);
		theFrame.getContentPane().add(this);
		theFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		toClose = false;
		theFrame.getRootPane().addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				GameVisualisation.this.handleWindowResize();
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		preTime = System.currentTimeMillis();
		super.paintComponent(g);
		game.getVisualisation(g, getWidth(), getHeight());
		while (System.currentTimeMillis() - preTime < MSPF) {
			try {
				Thread.sleep(2);
			} catch (Exception e) {}
		}
		repaint();

	}

	private void handleWindowResize() {
		game.handleWindowResize(getWidth(), getHeight());
	}

	// TODO make the visualiser toggleable
	public void show(GameInstance game) {
		this.game = game;
		if (!theFrame.isVisible()) {
			theFrame.setVisible(true);
		}
		toClose = false;
	}

	public void close() {
		toClose = true;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (toClose) {
			theFrame.dispose();
		}
	}

}
