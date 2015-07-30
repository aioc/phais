package com.ausinformatics.phais.utils;

import java.io.Serializable;

public class Position implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 2766491281630412203L;
    
    public final static int UP = 0;
	public final static int RIGHT = 1;
	public final static int DOWN = 2;
	public final static int LEFT = 3;
	public final static int NO_MOVE = 4;
	
	public final int r, c;
	public final static int[] dx = { 0, 1, 0, -1, 0 };
	public final static int[] dy = { -1, 0, 1, 0, 0 };

	public Position(int r, int c) {
		this.r = r;
		this.c = c;
	}
	
	@Override
	public Position clone() {
		return new Position(r, c);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Position) {
			return ((Position)o).r == r && ((Position)o).c == c;
		} else {
			return false;
		}
	}
	
	public Position move(int dir) {
		return new Position(r + dy[dir], c + dx[dir]);
	}

	public Position moveN(int dir, int amount) {
		return new Position(r + dy[dir] * amount, c + dx[dir] * amount);
	}
	
	public String toString() {
		return r + " " + c;
	}
}
