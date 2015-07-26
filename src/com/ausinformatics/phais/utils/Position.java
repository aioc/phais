package com.ausinformatics.phais.utils;

public class Position {

	public static int UP = 0;
	public static int RIGHT = 1;
	public static int DOWN = 2;
	public static int LEFT = 3;
	
	public int r, c;
	public static final int[] dx = { 0, 1, 0, -1 };
	public static final int[] dy = { -1, 0, 1, 0 };

	public Position(int r, int c) {
		this.r = r;
		this.c = c;
	}
	
	@Override
	public Position clone() {
		return new Position(r, c);
	}
	
	public boolean equals(Position p) {
		return p.r == r && p.c == c;
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
