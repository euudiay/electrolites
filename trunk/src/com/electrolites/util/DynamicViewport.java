package com.electrolites.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.electrolites.data.Data;

public class DynamicViewport {
	// Posici�n, ancho y alto del viewport en pixels
	public int vpPxX;
	public int vpPxY;
	public int vpPxWidth;
	public int vpPxHeight;

	// Muestras comprendidas en la viewarea
	public int vaSamples;
	// Muestras por segundo
	public float samplesPerSecond;
	
	// Base de dibujo horizontal
	public float baselinePxY;
	
	// Data de verdad
	public Data actualData;
	
	// Samples Data
	public LinkedList<SamplePoint> samplesData;
	
	// Testing area
	public float vFactor;
	
	public DynamicViewport(int width, int height) {
		vpPxWidth = width;
		vpPxHeight = height;

		actualData = Data.getInstance();
		
		samplesPerSecond = 250f;
		
		updateParameters();
	}
	
	public DynamicViewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;

		actualData = Data.getInstance();
		
		samplesPerSecond = 250f;
		
		updateParameters();
	}
	
	public void setOnScreenPosition(int pxX, int pxY) {
		vpPxX = pxX;
		vpPxY = pxY;
		baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
	}
	
	public void updateParameters() {
		synchronized (this) {
			actualData.dynamicData.samplesQueueWidth = (int) (samplesPerSecond * Math.max(0.1f, actualData.getWidthScale()));
			vaSamples = actualData.dynamicData.samplesQueueWidth;
			baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
			
			// Ask for one more sample, removing the older one if full queue
			if (samplesData.size() >= vaSamples)
				for (int i = 0; i < vaSamples - samplesData.size(); i++)
					samplesData.remove();
			
			samplesData.add(actualData.dynamicData.samplesQueue.remove());
		}
		
		float top = vpPxHeight*0.85f;
		float max = 8000f;
		vFactor = top/max;
	}
	
	public float[] getViewContents() {
		return null;
	}
	
	public Map<Float, ExtendedDPoint> getViewDPoints() {
		
		/*// Obtener nuevos parametros
		updateParameters();
		
		// Calcular cantidad de puntos que caben
		float npoints = vaSeconds*samplesPerSecond;
		// Calcular densidad de puntos
		float dpoints = vpPxWidth / npoints;
		// Si la densidad es < 0 es que se quieren mostrar 
		// m�s puntos de los que caben (aglutinar o...)
		if (dpoints < 0)
			return null;
		// Buscar primer punto
		// Por ahora, redonder y coger el que sea (mejorar esto)
		int start = Math.round(vaSecX*samplesPerSecond);
		// Buscar �ltimo punto
		int end = Math.min(start + Math.round(npoints), dataEnd);*/
				
		HashMap<Float, ExtendedDPoint> map = new HashMap<Float, ExtendedDPoint>();
		
		/*Iterator<Map.Entry<Integer, DPoint>> it = actualData.dpoints.entrySet().iterator();
		Map.Entry<Integer, DPoint> entry;
		boolean done = false;
		
		while (it.hasNext() && !done) {
			entry = it.next();
			
			if (entry.getKey().intValue()-actualData.offset < start || entry.getKey().intValue()-actualData.offset >= end)
				continue;
			
			map.put(vpPxX + (entry.getKey()-start-actualData.offset)*dpoints, new ExtendedDPoint(entry.getKey().intValue()-actualData.offset, entry.getValue()));
		}*/
		
		return map;
	}
}
