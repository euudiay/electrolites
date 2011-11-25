package com.electrolites.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.res.Resources;

import com.electrolites.data.DPoint;
import com.electrolites.data.Data;

public class RealTimeFriendlyDataParser {
	private FileConverter fc;		
	
	private FixedLinkedList<Byte> stream;	// Bytes le�dos
	//private int p1, p2;				// Punteros al �ltimo byte consumido y al �ltimo producido
	private int expected_bytes;		// Bytes que se espera que vengan
	private int lastSample;			// �ltima muestra le�da (n�mero de orden)
	
	enum Token {None, Sample};
	
	byte currentByte;
	Token currentToken;
	int progress;
	byte[] storedBytes;
	
	private Data data;
	
	public RealTimeFriendlyDataParser() {
		//stream = new FixedLinkedList<Byte>(0);		// Instanciamos el vector de datos raw
		
		// Inicialmente no tenemos datos
		//p1 = 0;
		//p2 = 0;
		expected_bytes = 0;
		lastSample = 0;
		
		currentToken = Token.None;
		progress = 0;
	}
	
	public void setData(Data data) {
		this.data = data;
	}
	
	public void step() {
		// Suponemos que hay siguiente token
		currentByte = stream.get();
		switch (currentToken) {
		case None:
			switch (currentByte & 0xff) {
			case 0xda:
				currentToken = Token.Sample;
				break;
			default:
				currentToken = Token.None;
			}
			progress = 0;
			storedBytes = new byte[10];
			break;
		case Sample:
			parseSample();
			break;
		default:
			currentToken = Token.None;
			break;
		}
	}
	
	public void parseSample() {
		storedBytes[progress] = currentByte;
		progress++;
		if (progress == 2) {
			short sample = byteToShort(storedBytes[0], storedBytes[1]);
			synchronized (data.dynamicData) {
				data.dynamicData.addSample(new SamplePoint(lastSample, sample));
			currentToken = Token.None;
			progress = 0;			
			}
		}
	}
	
	public boolean hasNext() {
		//return (p1 <= p2 && p2-p1 >= expected_bytes);
		return !stream.list.isEmpty();
	} 
	
	// Devuelve el n�mero de bytes que nos han sobrado (forman parte del siguiente flujo de bytes)
	public int nextField(boolean staticMode) {
		// Obtenemos el siguiente byte
		//int new_byte = ((int) stream.get(p1)) & 0xff;
		int new_byte = ((int) stream.get()) & 0xff;
		// No adelantamos el puntero hasta ver si tenemos suficientes datos para leer
		//int data_amount = p2 - p1;	// Cantidad de bytes por leer (sin contar el delimitador)
		int data_amount = stream.size();
		
		switch (new_byte) {
			case 0xcc:					// Offset
				expected_bytes = 4;
				if (data_amount >= 4) 	// Tenemos que leer 4 bytes
					readOffset(staticMode);		// Comprueba, adem�s, si se han perdido muestras
				break;
			case 0xfb:					// HBR
				expected_bytes = 2;
				if (data_amount >= 2)
					readHBR(); 			// Lee el ritmo card�aco actual y lo guarda en data
				break;
			case 0xda:					// Sample
				expected_bytes = 2;
				if (data_amount >= 2)
					readSample(staticMode);		// Lee y almacena el valor de la muestra en data
				break;
			case 0xed:					// Point
				expected_bytes = 5;
				if (data_amount >= 5)
					readDPoint(); 	// Tratar puntos de delineaci�n
				break;
			default:
				System.err.println("Delimitador no reconocido: " + new_byte);
				//p1++;
		}
		
		return 0; // Si todo ha ido bien, no devuelve nada
	}
	
	// Devuelve el n�mero de muestras que se ha saltado, -1 si error
	// Coloca el �ndice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public void readOffset(boolean staticData) {
		// Antes el valor menos significativo
		/*int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;*/
		int byte0 = ((int) stream.get()) & 0xff;
		int byte1 = ((int) stream.get()) & 0xff;
		int byte2 = ((int) stream.get()) & 0xff;
		int byte3 = ((int) stream.get()) & 0xff;
		
		// Calculamos el n�mero de muestras que nos dice el offset
		int nSamples = byte3 + 256*(byte2 + 256*(byte1 + 256*byte0));
		
		if (lastSample == 0) {
			lastSample = nSamples;
			//data.dataOffset = nSamples;
			//dataOffset = nSamples;
		}
		
		// Adelantamos el puntero de lectura 5 posiciones (delimitador + 4 bytes)
		//p1 += 5;
		expected_bytes = 0;
	}
	
	public void readHBR() {
		/*int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;*/
		int byte0 = ((int) stream.get()) & 0xff;
		int byte1 = ((int) stream.get()) & 0xff;
		// Calcula el ritmo card�aco (60*250/X)
		//lastHBR = 15000f / (byte0*255 + byte1);
		// Guarda en data el valor del ritmo card�aco en este momento (index�ndolo seg�n la �ltima muestra recibida)
		//data.hbr.put(lastSample, (short) lastHBR);
		//dataHBRs.put(lastSample, (short) lastHBR);
		
		// Adelantamos el puntero de lectura 3 posiciones (delimitador + 3 bytes)
		//p1 += 3;
		expected_bytes = 0;
	}
	
	public void readSample(boolean staticMode) {
		// Incrementamos la cantidad de muestras le�das
		lastSample++;	
		// Calculamos el valor de la muestra
		//short sample = byteToShort(stream.get(p1+1), stream.get(p1+2));
		short sample = byteToShort(stream.get(), stream.get());
		// A�adimos la muestra con su n�mero de orden a la tabla de muestras
		//dataSamplesDynamic.add(new SamplePoint(lastSample, sample));
		synchronized (data.dynamicData) {
			data.dynamicData.addSample(new SamplePoint(lastSample, sample));
		}
		// Adelantamos el puntero de lectura 3 posiciones
		//p1 += 3;
		expected_bytes = 0;
	}
	
	public void readDPoint() {
		DPoint dp = new DPoint();
		
		//int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte0 = ((int) stream.get()) & 0xff;
		
		// Comprobamos el tipo de punto
		dp.setType(dp.checkType(byte0));
		// Comprobamos la onda a la que se refiere
		dp.setWave(dp.checkWave(byte0));
		
		// Vemos a qu� muestra se refiere
		/*int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;
		int byte4 = ((int) stream.get(p1+5)) & 0xff;*/
		int byte1 = ((int) stream.get()) & 0xff;
		int byte2 = ((int) stream.get()) & 0xff;
		int byte3 = ((int) stream.get()) & 0xff;
		int byte4 = ((int) stream.get()) & 0xff;
		
		int sample = byte4 + 256*(byte3 + 256*(byte2 + 256*byte1));
		
		// A�adimos el punto a la tabla de puntos de data
		//data.dpoints.put(sample, dp);
		//dataDPoints.put(sample, dp);
		
		// Adelantamos el puntero de lectura 6 posiciones (delimitador + 5 bytes)
		//p1 += 6;
		expected_bytes = 0;
	}
	
	// Convierte dos bytes dados en su short correspondiente (en complemento a 2)
	public short byteToShort(byte b1, byte b2) {
		// b1 m�s significativo que b2
		int i1 = b1;
		int i2 = b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
	}
	
	public void setStream(FixedLinkedList<Byte> stream) { 
		this.stream = stream;
	}

	public void clearStream() {
		stream = new FixedLinkedList<Byte>(0);
	}

	public void addToStream(byte b) {
		stream.add(b);
	}

	public FixedLinkedList<Byte> getStream() {
		return stream;
	}
}
