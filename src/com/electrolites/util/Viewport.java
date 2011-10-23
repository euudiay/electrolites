package com.electrolites.util;

import android.util.Log;

public class Viewport {
	// Posición, ancho y alto del viewport en pixels
	public int vpPxX;
	public int vpPxY;
	public int vpPxWidth;
	public int vpPxHeight;

	// Posición X de la viewarea en segundos
	public float vaSecX;
	// Segundos comprendidos en la viewarea
	public float vaSeconds;
	// Muestras por segundo
	public float samplesPerSecond;
	
	// Base de dibujo horizontal
	public float baselinePxY;
	
	// Datos (temporal)
	public float[] data;
	public int dataStart;
	public int dataEnd;
	
	public Viewport(int width, int height) {
		vpPxWidth = width;
		vpPxHeight = height;
		
		samplesPerSecond = 250f;
		vaSeconds = 2f;
		vaSecX = 0f;
		baselinePxY = vpPxY + vpPxHeight/2;
		
		data = new float[5000];
		for (int i = 0; i < 5000; i++) {
			if (Math.random()*2 > 0.9)
				data[i] = -1*(float) Math.random()*100;
			else
				data[i] = (float) Math.random()*100;
		}
		dataEnd = 5000;
		dataStart = 0;
	}
	
	public Viewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;
		vaSeconds = seconds;
		
		samplesPerSecond = 250f;
		vaSecX = 0f;
		baselinePxY = vpPxY + vpPxHeight/2;
		
		data = new float[5000];
		for (int i = 0; i < 5000; i++) {
			if (Math.random()*2 > 0.9)
				data[i] = -1*(float) Math.random()*100;
			else
				data[i] = (float) Math.random()*100;
		}
		dataEnd = 5000;
		dataStart = 0;
	}
	
	public void setOnScreenPosition(int pxX, int pxY) {
		vpPxX = pxX;
		vpPxY = pxY;
		baselinePxY = pxY + vpPxHeight/2;
	}
	
	public float[] getViewContents() {
		// Calcular cantidad de puntos que caben
		float npoints = vaSeconds*samplesPerSecond;
		// Calcular densidad de puntos
		float dpoints = vpPxWidth / npoints;
		// Si la densidad es < 0 es que se quieren mostrar 
		// más puntos de los que caben (aglutinar o...)
		if (dpoints < 0)
			return null;
		// Buscar primer punto
		// Por ahora, redonder y coger el que sea (mejorar esto)
		int start = Math.round(vaSecX);
		// Buscar último punto
		int end = start + Math.round(npoints);
		// Construir la lista de puntos a devolver
		float points[] = new float[(end-start-2)*4+4];
		
		for (int i = 0; i < end-start-1; i+=1) {
			// Devolver array de puntos a pintar
			// X, Y
			if (i == 0) {
				points[i] = vpPxX;
				points[i+1] = baselinePxY + data[start];
				points[i+2] = vpPxX+dpoints;
				points[i+3] = baselinePxY + data[start+1];
			}
			else {
				// Si no es el primer punto, duplicar el anterior
				points[4*i] = points[4*i-2];
				points[4*i+1] = points[4*i-1];
				points[4*i+2] = vpPxX + i*dpoints;
				points[4*i+3] = baselinePxY + data[start+i+1];
			}
		}

		return points;
	}
	
	public boolean move(float secDeltaX) {
		// Comprobación de límites
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
		
		return true;
	}
}