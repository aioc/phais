package com.ausinformatics.phais.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class VisualisationUtils {
    static public class Box {
        public final int top;
        public final int left;
        public final int right;
        public final int bottom;

        public final int width;
        public final int height;

        public Box(int t, int l, int r, int b) {
            top = t;
            left = l;
            right = r;
            bottom = b;
            width = r - l;
            height = b - t;
        }

        public void fill(Graphics2D g) {
            g.fillRect(left, top, width, height);
        }

        public String toString() {
            return "(" + left + " " + top + " " + right + " " + bottom + ")[" + width + " " + height + "]";
        }
    }
    
    static public class BoxFactory {

        private int width;
        private int height;

        public BoxFactory(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Box fromDimensions(int x, int y, int width, int height) {
            return new Box(y, x, x + width, y + height);
        }

        public Box fromPoints(int l, int t, int r, int b) {
            return new Box(t, l, width - r, height - b);
        }

        public Box fromMixedWidth(int l, int t, int width, int b) {
            return new Box(t, l, l + width, b);
        }

        public Box fromMixedHeight(int l, int t, int r, int height) {
            return new Box(t, l, r, t + height);
        }
    }

    static Font getLargestFittingFont(Font f, Box b, Graphics2D g, String s, int largestSize) {
        int minSize = 1;
        int maxSize = largestSize;
        while (minSize < maxSize) {
            int midSize = (minSize + maxSize) / 2;
            f = f.deriveFont(Font.PLAIN, midSize);
            FontMetrics fm = g.getFontMetrics(f);
            Rectangle2D fR = fm.getStringBounds(s, g);
            if (fR.getWidth() < b.width - 20 && fR.getHeight() < b.height) {
                minSize = midSize + 1;
            } else {
                maxSize = midSize - 1;
            }
        }
        return f.deriveFont(minSize);
    }

    static public void drawString(Graphics2D g, Box b, Font rootFont, String text, Color c) {
        Font fo = VisualisationUtils.getLargestFittingFont(rootFont, b, g, text, 180);
        g.setStroke(new BasicStroke(1));
        FontMetrics fm = g.getFontMetrics(fo);
        Rectangle2D fR = fm.getStringBounds(text, g);
        g.setFont(fo);
        g.setColor(c);
        g.drawString(text, b.left + (b.width - (int) fR.getWidth()) / 2,
                b.top + (b.height + (int) (0.5 * fR.getHeight())) / 2);
    }

    /* TODO (bgbn) this shouldn't need boardBoxes */
    static public Box tweenMovement(BoxFactory f, final Box[][] boardBoxes, Position from, Position to, double progress, int borderOffset) {
        int boxSize = boardBoxes[0][0].width;
        Box b1 = boardBoxes[from.r + 1][from.c + 1];
        Box b2 = boardBoxes[to.r + 1][to.c + 1];
        Box b3 = f.fromPoints(Math.min(b1.left, b2.left),
                Math.min(b1.top, b2.top),
                Math.max(b1.right, b2.right),
                Math.max(b1.bottom, b2.bottom));
        Box b = f.fromPoints(b3.left + borderOffset, b3.top + borderOffset,
                b3.right + borderOffset, b3.bottom + borderOffset);
        int amoDiff = (int) (progress * boxSize);
        if (from.c < to.c) {
            b = f.fromDimensions(b.left + amoDiff, b.top, b.width - amoDiff, b.height);
        } else if (from.c > to.c) {
            b = f.fromDimensions(b.left, b.top, b.width - amoDiff, b.height);
        } else if (from.r < to.r) {
            b = f.fromDimensions(b.left, b.top + amoDiff, b.width, b.height - amoDiff);
        } else {
            b = f.fromDimensions(b.left, b.top, b.width, b.height - amoDiff);
        }
        return b;
    }
}
