package com.electrolites.util;

import java.util.ArrayList;

public class DataParser {
	private FileConverter fc;
	private ArrayList<Byte> data;
	private int pointer;
	private int lastSample;
	private int lastHBR;
	
	public DataParser() {
		fc = new FileConverter();
		data = new ArrayList<Byte>();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
	}
	
	public void loadBinaryFile(String fname) {
		fc.readBinary(fname);
		data = fc.getStream();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
	}
	
	// Devuelve el número de muestras que se ha saltado, -1 si error
	// Coloca el índice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public int goToNextOffset() {
		int i = pointer;
		boolean found = false;
		
		while (i < data.size() && !found) {
			found = (data.get(i) == 0xcc);
			i++;
		}
		
		if (found) {
			// Antes el valor menos significativo
			int byte0 = ((int) data.get(i)) & 0xff;
			int byte1 = ((int) data.get(i+1)) & 0xff;
			int byte2 = ((int) data.get(i+2)) & 0xff;
			int byte3 = ((int) data.get(i+3)) & 0xff;
			
			if (data.get(i) == 0xfb) {
				// Actualiza el HBR (primero el byte más significativo)
				byte0 = ((int) data.get(i+4)) & 0xff;
				byte1 = ((int) data.get(i+5)) & 0xff;
				lastHBR = 15000 / (byte0*255 + byte1);
			}
			
			pointer = i+6;
			
			int sample = byte0 + 256*(byte1 + 256*(byte2 + 256*byte3));
			return sample - lastSample;
		}
		else return -1;
	}
	
	public Sample getNextSample() {
		int i = pointer;
		boolean found = false;
		
		while (i < data.size() && !found) {
			found = (data.get(i) == 0xda);
			i++;
		}
		
		if (found) {
			pointer = i+2;
			lastSample++;
			return new Sample(data.get(i), data.get(i+1));
		}
			
		else
			return null;
	}
	
	public int getLastHBR() { return lastHBR; }
}
