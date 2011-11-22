package com.electrolites.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Handler;

import com.electrolites.util.SamplePoint;

public class Data {

	public static final short MODE_STOP = 0;
	public static final short MODE_STATIC = 1;
	public static final short MODE_DYNAMIC = 2;
	
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
	
	public short mode;
	
	public Handler handler;
	
	// Data container for dynamic visualization mode
	public class DynamicData {
		public LinkedList<SamplePoint>samplesQueue;
		public int samplesQueueWidth;
		
		public DynamicData() {
			samplesQueue = new LinkedList<SamplePoint>();
			samplesQueueWidth = -1;
		};
		
		public void addSamples(Collection<SamplePoint> list) {
			// If no width's specified, just add them all
			if (samplesQueueWidth < 0)
				samplesQueue.addAll(list);
			else {
				// Remove from head those that doesn't fit in
				if (samplesQueue.size() + list.size() >= samplesQueueWidth) {
					int toRemove = ((samplesQueue.size() + list.size()) - samplesQueueWidth);
					for (int i = 0; i < toRemove; i++) {
						if (samplesQueue.isEmpty())
							break;
						try {
							SamplePoint p = samplesQueue.removeFirst();
							p = null;
						} catch (NoSuchElementException e) {
							
						}
					}
				}
				// Add new ones at end
				samplesQueue.addAll(list);
			}
		}
			
		public SamplePoint getSample() {
			return samplesQueue.remove();
		}
	};
	
	// Data container for dynamic visualization mode instance
	public DynamicData dynamicData;
	
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
		
		//Data container for dynamic visualization mode
		dynamicData = new DynamicData();
		dynamicData.samplesQueueWidth = -1;
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
