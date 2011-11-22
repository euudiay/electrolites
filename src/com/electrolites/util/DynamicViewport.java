package com.electrolites.util;

import java.util.HashMap;
import java.util.Iterator;
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
	
	public DynamicViewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;

		actualData = Data.getInstance();
		
		samplesData = new LinkedList<SamplePoint>();
		
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
			
			if (actualData.dynamicData.samplesQueue.size() > 0) {				
				// Ask for one more sample, removing the older one if full queue
				if (samplesData.size() >= vaSamples) {
					int toRemove = samplesData.size() - vaSamples;
					for (int i = 0; i < toRemove; i++)
						samplesData.removeFirst();
				}
				
				if (!actualData.dynamicData.samplesQueue.isEmpty())
					samplesData.add(actualData.dynamicData.samplesQueue.remove());
				//System.out.println("Queue size = " + samplesData.size() + ", expected: " + vaSamples);
			}
		}
		
		float top = vpPxHeight*0.85f;
		float max = 12000f;
		vFactor = top/max;
	}
	
	public float[] getViewContents() {
		// Get a new sample and update drawing parameters
		updateParameters();
		
		float dpoints = vpPxWidth / ((float) vaSamples);
		
		if (dpoints <= 0) {
			System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			return null;
		}

		float[] results = new float[4+4*(samplesData.size()-1)];
		
		Iterator<SamplePoint> it = samplesData.iterator();
		
		for (int i = 0; i < samplesData.size()-1; i++) {
			if (!it.hasNext()) {
				System.err.println("No sample found at position " + i);
				return null;
			}
				
			if (i == 0) {
				results[i] = vpPxX;
				results[i+1] = baselinePxY - it.next().sample*vFactor;
				if (it.hasNext()) {
					results[i+2] = vpPxX + dpoints;
					results[i+3] = baselinePxY - it.next().sample*vFactor;
				}
			}
			else {
				// Duplicate last point
				results[4*i] = results[4*i-2];
				results[4*i+1] = results[4*i-1];
				results[4*i+2] = vpPxX + i*dpoints;
				results[4*i+3] = baselinePxY - it.next().sample*vFactor;
			}
		}
		
		return results;
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
