package com.electrolites.util;

public class SamplePoint {
	public int id;
	public long sample;
	
	public SamplePoint(int id, long value) {
		this.id = id;
		this.sample = value;
	}
	
	public SamplePoint clone() {
		return new SamplePoint(id, sample);
	}
}
