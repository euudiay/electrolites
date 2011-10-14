package com.electrolites.data;

public class Data {
	private static Data instance = null;
	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		
		return instance;
	}
	
	public int drawBaseHeight = 0;
	
	public Data() {
		drawBaseHeight = 0;
	}
}
