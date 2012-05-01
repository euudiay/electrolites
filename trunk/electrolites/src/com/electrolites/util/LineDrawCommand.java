package com.electrolites.util;

import com.electrolites.util.DPoint.PointType;
import com.electrolites.util.DPoint.Wave;

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
	
	public void defaultValues(DPoint dp) {
		if (dp.getType() == PointType.start || dp.getType() == PointType.end) {
            setWidth(1.f);
			setARGB(200, 180, 180, 240);
			// Debug offset dpoint
			if (dp.getWave() == Wave.Offset) {
				setARGB(200, 244, 10, 10);
			}
		}
		else if (dp.getType() == PointType.peak) {
            setWidth(2.f);
            if (dp.getWave() == Wave.QRS)
				setARGB(230, 255, 0, 255);
            else if (dp.getWave() == Wave.P)
				setARGB(230, 0, 255, 255);
			else if (dp.getWave() == Wave.T)
				setARGB(230, 255, 255, 0);
			else if (dp.getWave() == Wave.Offset) {
				setWidth(3.0f);
				setARGB(230, 10, 244, 10);
			}
		} else if (dp.getType() == PointType.s_peak) {
			setWidth(2.f);
			setARGB(255, 255, 0, 255);
		}
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
