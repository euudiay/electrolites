package com.electrolites.data;

import java.util.HashMap;
import java.util.Map;

public class Data {
	private static Data instance = null;
	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		
		return instance;
	}
	
	public int drawBaseHeight = 0;
	// Muestras indexadas por no. de muestra
	public Map<Integer, Short> samples = null;
	// Puntos resultantes de la delineación, indexados por no. de muestra
	public Map<Integer, DPoint> dpoints = null;
	// Valores del ritmo cardíaco, indexados según el número de muestra anterior a su recepción
	public Map<Integer, Short> hbr = null;
	
	
	public Data() {
		drawBaseHeight = 0;
		samples = new HashMap<Integer, Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbr = new HashMap<Integer, Short>();
	}
}
