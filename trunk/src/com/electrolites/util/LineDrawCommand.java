package com.electrolites.util;

public class LineDrawCommand {
	float width;
	short A, R, G, B;
	float x1, y1, x2, y2;
	
	public LineDrawCommand() {
		width = 1.f;
		A = R = G = B = 255;
		x1 = y1 = x2 = y2 = 0;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}
	
	public void setARGB(int A, int R, int G, int B) {
		this.A = (short) A;
		this.R = (short) R;
		this.G = (short) G;
		this.B = (short) B;
	}
	
	public void setPoints(float x1, float y1, float x2, float y2) {
		this.x1 = x1; this.x2 = x2;
		this.y1 = y1; this.y2 = y2;
	}

	public short getA() {
		return A;
	}

	public void setA(short a) {
		A = a;
	}

	public short getR() {
		return R;
	}

	public void setR(short r) {
		R = r;
	}

	public short getG() {
		return G;
	}

	public void setG(short g) {
		G = g;
	}

	public short getB() {
		return B;
	}

	public void setB(short b) {
		B = b;
	}

	public float getX1() {
		return x1;
	}

	public void setX1(float x1) {
		this.x1 = x1;
	}

	public float getY1() {
		return y1;
	}

	public void setY1(float y1) {
		this.y1 = y1;
	}

	public float getX2() {
		return x2;
	}

	public void setX2(float x2) {
		this.x2 = x2;
	}

	public float getY2() {
		return y2;
	}

	public void setY2(float y2) {
		this.y2 = y2;
	}
}
