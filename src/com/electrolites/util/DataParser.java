package com.electrolites.util;

import java.util.ArrayList;

public class DataParser {
	enum Code {none, offset, hbr, sample, point };
	
	private FileConverter fc;		
	private ArrayList<Byte> data;
	private int pointer;			// Apunta al siguiente byte al �ltimo le�do (en data)
	private int lastSample;			// �ltima muestra le�da (n�mero de orden)
	private int lastHBR;			// �ltimo resultado de ritmo card�aco le�do
	private Code lastCode;			// Tipo del �ltimo byte le�do
	
	public DataParser() {
		fc = new FileConverter();
		data = new ArrayList<Byte>();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
	}
	
	public void loadBinaryFile(String fname) {
		fc.readBinary(fname);
		data = fc.getStream();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
	}
	
	// Devuelve el n�mero de muestras que se ha saltado, -1 si error
	// Coloca el �ndice en el siguiente byte a los 5 del offset
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
				// Actualiza el HBR (primero el byte m�s significativo)
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
	
	// Avanza una posici�n en data y devuelve el tipo del byte
	public Code getNextByte() {
		int new_byte = ((int) data.get(pointer)) & 0xff;
		
		switch (new_byte) {
		case 0xcc:
			lastCode = Code.offset;
			break;
		case 0xfb:
			lastCode = Code.hbr;
			break;
		case 0xda:
			lastCode = Code.sample;
			break;
		case 0xed:
			lastCode = Code.point;
			break;
		}
		
		pointer++;
		
		return lastCode;
	}
	
	public int getLastHBR() { return lastHBR; }
}
