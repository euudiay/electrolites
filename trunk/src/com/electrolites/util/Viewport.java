package com.electrolites.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.electrolites.data.DPoint;
import com.electrolites.data.Data;

public class Viewport {
	// Posici�n, ancho y alto del viewport en pixels
	public int vpPxX;
	public int vpPxY;
	public int vpPxWidth;
	public int vpPxHeight;

	// Posici�n X de la viewarea en segundos
	public float vaSecX;
	// Segundos comprendidos en la viewarea
	public float vaSeconds;
	// Muestras por segundo
	public float samplesPerSecond;
	
	// Base de dibujo horizontal
	public float baselinePxY;
	
	// Datos (temporal)
	// public float[] data;
	public short[] data;
	public int dataStart;
	public int dataEnd;
	
	// Data de verdad
	public Data actualData;
	
	// Testing area
	public float vFactor;
	
	public Viewport(int width, int height) {
		vpPxWidth = width;
		vpPxHeight = height;
		
		dataEnd = 0;
		dataStart = 0;
		actualData = Data.getInstance();
		
		samplesPerSecond = 250f;
		vaSeconds = 2f;
		vaSecX = actualData.vaSecX;
		
		updateParameters();
	}
	
	public Viewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;
		vaSeconds = seconds;
		
		data = null;
		dataEnd = 0;
		dataStart = 0;
		actualData = Data.getInstance();
		
		samplesPerSecond = 250f;
		vaSecX = actualData.vaSecX;
		
		updateParameters();
	}
	
	public void setRenderData(short[] data, int dataStart, int dataEnd) {
		this.data = data;
		this.dataStart = dataStart;
		this.dataEnd = dataEnd;
	}
	
	public void setOnScreenPosition(int pxX, int pxY) {
		vpPxX = pxX;
		vpPxY = pxY;
		baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
	}
	
	public void updateParameters() {
		vaSeconds = Math.max(0.1f, actualData.getWidhtScale());
		baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
		
		float top = vpPxHeight*0.85f;
		float max = 8000f;
		vFactor = top/max;
	}
	
	public float[] getViewContents() {
		
		// Obtener nuevos parametros
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
		int start = Math.round(vaSecX);
		// Buscar �ltimo punto
		int end = start + Math.round(npoints);
		// Construir la lista de puntos a devolver
		float points[] = new float[(end-start-2)*4+4];
		
		int index = 0;
		
		while (index < end-start-1 && start+index+1 < data.length) {
			// Devolver array de puntos a pintar
			// X, Y
			if (index == 0) {
				points[index] = vpPxX;
				points[index+1] = baselinePxY - data[start]*vFactor;
				points[index+2] = vpPxX+dpoints;
				points[index+3] = baselinePxY - data[start+1]*vFactor;
			}
			else {
				// Si no es el primer punto, duplicar el anterior
				points[4*index] = points[4*index-2];
				points[4*index+1] = points[4*index-1];
				points[4*index+2] = vpPxX + index*dpoints;
				points[4*index+3] = baselinePxY - data[start+index+1]*vFactor;
			}
			index++;
		}

		return points;
	}
	
	public Map<Float, ExtendedDPoint> getViewDPoints() {
		
		// Obtener nuevos parametros
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
		int start = Math.round(vaSecX);
		// Buscar �ltimo punto
		int end = start + Math.round(npoints);
				
		HashMap<Float, ExtendedDPoint> map = new HashMap<Float, ExtendedDPoint>();
		
		Iterator<Map.Entry<Integer, DPoint>> it = actualData.dpoints.entrySet().iterator();
		Map.Entry<Integer, DPoint> entry;
		boolean done = false;
		
		while (it.hasNext() && !done) {
			entry = (Map.Entry<Integer, DPoint>) it.next();
			
			if (entry.getKey().intValue()-actualData.dataOffset < start || entry.getKey().intValue()-actualData.dataOffset >= end)
				continue;
			
			map.put(vpPxX + (entry.getKey()-start-actualData.dataOffset)*dpoints, new ExtendedDPoint(entry.getKey().intValue()-actualData.dataOffset, entry.getValue()));
		}
		
		return map;
	}
	
	public boolean move(float secDeltaX) {
		// Comprobaci�n de l�mites
		if (secDeltaX > 0) {
			if (vaSecX + secDeltaX >= samplesPerSecond*(dataEnd - dataStart) - vaSeconds) {
				vaSecX = samplesPerSecond*(dataEnd - dataStart) - vaSeconds;
				return false;
			} else {
				vaSecX += secDeltaX;
			}
		}
		else if (secDeltaX < 0) {
			if (vaSecX + secDeltaX < 0) {
				vaSecX = 0;
				return false;
			} else {
				vaSecX += secDeltaX;
			}
		}
		
		actualData.vaSecX = vaSecX;
		
		return true;
	}
}
