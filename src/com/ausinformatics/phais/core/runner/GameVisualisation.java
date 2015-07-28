package com.ausinformatics.phais.core.runner;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.ausinformatics.phais.core.interfaces.GameInstance;

public class GameVisualisation extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 6362354475690588421L;
    private static final String DEFAULT_TITLE = "";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int FPS = 1000;
    public static boolean CALCFPS = false;

    private GameInstance game;
    private JFrame theFrame;
    private boolean toClose;
    private Queue<Long> frames;
    private Timer gameDisplayer;

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
        
        gameDisplayer = new Timer(1000 / FPS, this);
        
        frames = new ArrayDeque<>();
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TODO (bgbn) make createCompatibleImage a library method somewhere
        // since we use it in a few places.
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice().
            getDefaultConfiguration();
        BufferedImage backBuffer = gfx_config.createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
        Graphics gg = backBuffer.getGraphics();

        game.getVisualisation(gg, getWidth(), getHeight());
        if (CALCFPS) {
            frameRendered();
            gg.setColor(Color.WHITE);
            gg.drawString("FPS: " + getFPS(), 10, 40);
        }
        gg.dispose();

        if (!toClose) {
            g.drawImage(backBuffer, 0, 0, null);
        }

    }
    
    private synchronized void frameRendered() {
        long t = System.currentTimeMillis();
        frames.add(t);
        while (t - frames.peek() > 1000) {
            frames.poll();
        }
    }
    
    private synchronized int getFPS() {
        return frames.size();
    }

    private synchronized void handleWindowResize() {
        game.handleWindowResize(getWidth(), getHeight());
    }

    // TODO make the visualiser toggleable
    public synchronized void show(GameInstance game) {
        this.game = game;
        if (!theFrame.isVisible()) {
            theFrame.setVisible(true);
        }
        toClose = false;
        gameDisplayer.start();
    }
    
    public synchronized void handleClosing() {
        game.windowClosed();
        close();
    }

    public synchronized void close() {
        toClose = true;
        gameDisplayer.stop();
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        repaint();
    }

}
