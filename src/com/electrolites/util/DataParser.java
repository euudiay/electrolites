package com.electrolites.util;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.res.Resources;

import com.electrolites.data.Data;

public class DataParser {
	enum Code {none, offset, hbr, sample, point };
	
	private FileConverter fc;		
	private Data data;				// Instancia de Data
	private ArrayList<Byte> stream;	// Bytes leídos
	private short[] samples;		// Valores de las muestras, indexadas por orden de llegada
	private int pointer;			// Apunta al siguiente byte al ï¿½ltimo leï¿½do (en data)
	private int lastSample;			// ï¿½ltima muestra leï¿½da (nï¿½mero de orden)
	private int lastHBR;			// ï¿½ltimo resultado de ritmo cardï¿½aco leï¿½do
	private Code lastCode;			// Tipo del ï¿½ltimo byte leï¿½do
	
	private int bytesLeft;			// Bytes que quedan por leer del campo actual
	
	public DataParser() {
		fc = new FileConverter();
		data = Data.getInstance();
		stream = new ArrayList<Byte>();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
		
		bytesLeft = 0;
	}
	
	public void loadBinaryFile(String fname) {
		fc.readBinary(fname);
		stream = fc.getStream();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
		
		bytesLeft = 0;
	}
	
	public void loadResource(Resources resources) {
		fc.readResources(resources);
		stream = fc.getStream();
		pointer = 0;
		lastSample = 0;
		lastHBR = 0;
		lastCode = Code.none;
		
		bytesLeft = 0;
	}
	
	// Devuelve el nï¿½mero de muestras que se ha saltado, -1 si error
	// Coloca el ï¿½ndice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public int goToNextOffset() {
		int i = pointer;
		boolean found = false;
		
		while (i < stream.size() && !found) {
			found = (stream.get(i) == 0xcc);
			i++;
		}
		
		if (found) {
			// Antes el valor menos significativo
			int byte0 = ((int) stream.get(i)) & 0xff;
			int byte1 = ((int) stream.get(i+1)) & 0xff;
			int byte2 = ((int) stream.get(i+2)) & 0xff;
			int byte3 = ((int) stream.get(i+3)) & 0xff;
			
			if (stream.get(i) == 0xfb) {
				// Actualiza el HBR (primero el byte mï¿½s significativo)
				byte0 = ((int) stream.get(i+4)) & 0xff;
				byte1 = ((int) stream.get(i+5)) & 0xff;
				lastHBR = 15000 / (byte0*255 + byte1);
			}
			
			pointer = i+6;
			
			int sample = byte0 + 256*(byte1 + 256*(byte2 + 256*byte3));
			return sample - lastSample;
		}
		else return -1;
	}
	
	public Short getNextSample() {
		int i = pointer;
		boolean found = false;
		Short sample = 0;
		
		// A partir de la posición del puntero, buscamos la siguiente muestra
		while (i < stream.size() && !found) {
			found = (stream.get(i) == 0xda);
			i++;
		}
		
		// Si la hemos encontrado...
		if (found) {
			pointer = i+2;	// Avanzamos el puntero dos posiciones
			lastSample++;	// Incrementamos la cantidad de muestras leídas
			
			// Calculamos el valor de la muestra
			sample = byteToShort(stream.get(i), stream.get(i+1));
			// Añadimos la muestra con su número de orden a la tabla de muestras
			data.samples.put(lastSample, sample);
			
			return sample;
		}
			
		else
			return null;
	}
	
	// De momento sólo para leer de archivo
	// Filtra los datos de las muestras de los demás datos y los guarda en el vector samples
	public void extractSamples() {
		Iterator<Byte> it = stream.iterator();
	
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
		// Obtenemos el siguiente byte
		int new_byte = ((int) stream.get(pointer)) & 0xff;
		pointer++;	// Adelantamos el puntero una posición
		
		if (bytesLeft == 0) {
			// Ya se han leído todos los bytes del campo anterior
			switch (new_byte) {
				case 0xcc:
					lastCode = Code.offset;
					bytesLeft = 4;
					break;
				case 0xfb:
					lastCode = Code.hbr;
					bytesLeft = 2;
					break;
				case 0xda:
					lastCode = Code.sample;
					bytesLeft = 2;
					break;
				case 0xed:
					lastCode = Code.point;
					bytesLeft = 5;
					break;
			}
		}
		else
			bytesLeft--;	// Hemos leído un byte más de los que faltaban por leer
				
		return lastCode;
	}
	
	public int getLastHBR() { return lastHBR; }
	
	public short[] getSamples() { return samples; }
}
