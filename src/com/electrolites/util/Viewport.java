package com.electrolites.util;

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
	
	// Datos (temporal)
	public float[] data;
	
	public Viewport(int width, int height) {
		vpPxWidth = width;
		vpPxHeight = height;
		
		samplesPerSecond = 250f;
		vaSeconds = 2f;
		vaSecX = 0f;
	}
	
	public Viewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;
		vaSeconds = seconds;
		
		samplesPerSecond = 250f;
		vaSecX = 0f;
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
		float contents[] = new float[end-start];
		for (int i = 0; i < start-end; i++) {
			contents[i] = data[start+i];
		}
		return contents;
	}
}