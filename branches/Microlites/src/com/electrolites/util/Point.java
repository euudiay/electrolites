package com.electrolites.util;

public class Point {
	private float x;
	private float y;
	
	public Point(float x, float y) {
		this.setX(x);
		this.setY(y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return "(" + x + " , " + y + ")";
	}
}
