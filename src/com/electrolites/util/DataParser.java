package com.electrolites.util;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.res.Resources;

public class DataParser {
	enum Code {none, offset, hbr, sample, point };
	
	private FileConverter fc;		
	private ArrayList<Byte> data;	// Bytes leídos
	private short[] samples;		// Valores de las muestras, indexadas por orden de llegada
	private int pointer;			// Apunta al siguiente byte al ï¿½ltimo leï¿½do (en data)
	private int lastSample;			// ï¿½ltima muestra leï¿½da (nï¿½mero de orden)
	private int lastHBR;			// ï¿½ltimo resultado de ritmo cardï¿½aco leï¿½do
	private Code lastCode;			// Tipo del ï¿½ltimo byte leï¿½do
	private Resources resources;
	
	public DataParser(Resources resources) {
		this.resources = resources;
		fc = new FileConverter(resources);
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
	
	public void loadResource() {
		fc.readResources();
		data = fc.getStream();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
	}
	
	// Devuelve el nï¿½mero de muestras que se ha saltado, -1 si error
	// Coloca el ï¿½ndice en el siguiente byte a los 5 del offset
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
				// Actualiza el HBR (primero el byte mï¿½s significativo)
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
	
	// Filtra los datos de las muestras de los demás datos y los guarda en el vector samples
	public void extractSamples() {
		Iterator<Byte> it = data.iterator();
	
		samples = new short[5000];
		
		byte a, b;
		int i = 0;
		while (it.hasNext()) {
			if ((it.next() & 0xff) == 0xda) {
				// Supongamos que esto funciona siempre
				a = it.next();
				b = it.next();
				samples[i] = byteToShort(a, b);
				i++;
			}
		}
	}
	
	// Convierte dos bytes dados en su short correspondiente (en complemento a 2)
	public short byteToShort(byte b1, byte b2) {
		// b1 más significativo que b2
		int i1 = (int) b1;
		int i2 = (int) b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
	}
	
	// Avanza una posiciï¿½n en data y devuelve el tipo del byte
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
	
	public short[] getSamples() { return samples; }
}
