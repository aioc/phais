package com.ausinformatics.phais.core.runner;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.ausinformatics.phais.core.interfaces.GameInstance;

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
        
        theFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GameVisualisation.this.handleClosing();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        preTime = System.currentTimeMillis();
        super.paintComponent(g);
        // TODO (bgbn) make createCompatibleImage a library method somewhere
        // since we use it in a few places.
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice().
            getDefaultConfiguration();
        BufferedImage backBuffer = gfx_config.createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
        Graphics gg = backBuffer.getGraphics();

        game.getVisualisation(gg, getWidth(), getHeight());

        while (System.currentTimeMillis() - preTime < MSPF) {
            try {
                Thread.sleep(2);
            } catch (Exception e) {}
        }

        if (!toClose) {
            g.drawImage(backBuffer, 0, 0, null);
            repaint();
        }

        gg.dispose();
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
        repaint();
    }
    
    public void handleClosing() {
        game.windowClosed();
        close();
    }

    public void close() {
        toClose = true;
        Timer t= new Timer();
        t.schedule(new HideTask(), 10000);
    }
    
    private class HideTask extends TimerTask {

        @Override
        public void run() {
            if (toClose) {
                theFrame.setVisible(false);
            }
        }
        
    }

}
