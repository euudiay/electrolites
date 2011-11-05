package com.electrolites.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;

public class Data {
	private static Data instance = null;
	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		
		return instance;
	}
	
	// Posici�n X de la vista en secs
	public float vaSecX;
	// Altura base de renderizado (0~1)
	private float drawBaseHeight = 0.5f;
	// Escala de ancho: 0 (evitar) ~ whatever
	private float WidhtScale = 1;
	// Muestras indexadas por no. de muestra
	public ArrayList<Short> samples = null;
	// Puntos resultantes de la delineaci�n, indexados por no. de muestra
	public Map<Integer, DPoint> dpoints = null;
	// Valores del ritmo card�aco, indexados seg�n el n�mero de muestra anterior a su recepci�n
	public Map<Integer, Short> hbr = null;
	// Primera muestra dibujable
	public int dataOffset;
	
	// Test area
	public Application app;
	public boolean autoScroll;
	public boolean loading;
	public Activity activity;
	
	public Data() {
		vaSecX = 0;
		drawBaseHeight = 0.5f;
		WidhtScale = 3;
		samples = new ArrayList<Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbr = new HashMap<Integer, Short>();
		app = null;
		autoScroll = false;
		loading = false;
	}



	public float getDrawBaseHeight() {
		return drawBaseHeight;
	}



	public void setDrawBaseHeight(float drawBaseHeight) {
		this.drawBaseHeight = drawBaseHeight;
	}



	public float getWidhtScale() {
		return WidhtScale;
	}



	public void setWidhtScale(float widhtScale) {
		WidhtScale = widhtScale;
	}
	
	
	public short[] getSamplesArray() {
		
		Object samp[] = samples.toArray();
		short result[] = new short[samp.length];
		for (int i = 0; i < samp.length; i++)
			result[i] = ((Short) samp[i]).shortValue();
		
		return result;
	}
}
