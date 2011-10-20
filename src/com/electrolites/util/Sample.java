package com.electrolites.util;

public class Sample {
	private byte a;
	private byte b;
	private int value;
	
	public Sample(byte a, byte b) {
		this.a = a;
		this.b = b;
		// Suponiendo que sea así como lo manda el shimmer
		value = ((int) a) * 255 + ((int) b);
	}
	
	public int getValue() { return value; }
}
