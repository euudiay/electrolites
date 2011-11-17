package com.electrolites.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Handler;

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
	private float widthScale = 1;
	// Muestras indexadas por no. de muestra
	public ArrayList<Short> samples = null;
	// Puntos resultantes de la delineaci�n, indexados por no. de muestra
	public Map<Integer, DPoint> dpoints = null;
	// Valores del ritmo card�aco, indexados seg�n el n�mero de muestra anterior a su recepci�n
	public Map<Integer, Short> hbrs = null;
	// Primera muestra dibujable
	public int offset;
	
	//Nombre del archivo de log a cargar
	public String toLoad;
	
	//Nombre del dispositivo conectado
	public String connected;
	
	// Test area
	public Application app;
	public boolean autoScroll;
	public boolean loading;
	public Activity activity;
	public int bgColor;
	
	public Handler handler;
	
	public Data() {
		vaSecX = 0;
		drawBaseHeight = 0.5f;
		widthScale = 3;
		samples = new ArrayList<Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbrs = new HashMap<Integer, Short>();
		app = null;
		autoScroll = false;
		loading = false;
		toLoad = "traza.txt";
		bgColor = Color.rgb(0, 0, 0);
		connected = "FireFly-3781";
	}

	public float getDrawBaseHeight() {
		return drawBaseHeight;
	}

	public void setDrawBaseHeight(float drawBaseHeight) {
		this.drawBaseHeight = drawBaseHeight;
	}

	public float getWidthScale() {
		return widthScale;
	}

	public void setWidthScale(float widthScale) {
		this.widthScale = widthScale;
	}
	
	public short[] getSamplesArray() {
		Object samp[] = samples.toArray();
		short result[] = new short[samp.length];
		try {
			for (int i = 0; i < samp.length; i++) {
				if (samp[i] != null)
					result[i] = ((Short) samp[i]).shortValue();
				else
					result[i] = 0;
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
