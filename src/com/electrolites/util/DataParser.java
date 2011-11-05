package com.electrolites.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.res.Resources;

import com.electrolites.data.DPoint;

public class DataParser {
	private FileConverter fc;		
	//private Data data;				// Instancia de Data
	private ArrayList<Byte> stream;	// Bytes le�dos
	private short[] samples;		// Valores de las muestras, indexadas por orden de llegada
	private int p1, p2;				// Punteros al �ltimo byte consumido y al �ltimo producido
	private int lastSample;			// �ltima muestra le�da (n�mero de orden)
	private float lastHBR;			// �ltimo resultado de ritmo card�aco le�do
	
	protected ArrayList<Short> dataSamples;
	protected HashMap<Integer, DPoint> dataDPoints;
	protected HashMap<Integer, Short> dataHBRs;
	protected int dataOffset;
	
	public DataParser() {
		//data = Data.getInstance();			// Accedemos a los datos de la aplicaci�n
		stream = new ArrayList<Byte>();		// Instanciamos el vector de datos raw
		
		// Inicialmente no tenemos datos
		p1 = 0;
		p2 = 0;
		lastSample = 0;
		lastHBR = 0;
		
		dataSamples = new ArrayList<Short>();
		dataDPoints = new HashMap<Integer, DPoint>();
		dataHBRs = new HashMap<Integer, Short>();
	}
	
	// Obtiene datos para la aplicaci�n de un archivo binario
	public void loadBinaryFile(String fname) {
		fc = new FileConverter();		// Instancia el conversor de archivos		
		stream = fc.readBinary(fname);	// Lee y almacena los datos del archivo
		
		// Inicializa los punteros de lectura y escritura
		p1 = 0;							
		p2 = stream.size() - 1;
		
		// Inicialmente no tenemos datos
		lastSample = 0;
		lastHBR = 0;
		
		// Procesa y guarda los datos en Data
		readStream();
	}
	
	// Obtiene datos para la aplicaci�n de un recurso interno
	public void loadResource(Resources resources, int id) {
		fc = new FileConverter();					// Instancia el conversor de archivos
		stream = fc.readResources(resources, id);	// Carga el recurso con identificador id
		
		// Inicializa los punteros de lectura y de escritura
		p1 = 0;
		p2 = stream.size() - 1;
		
		// Inicialmente no tenemos datos
		lastSample = 0;
		lastHBR = 0;

		// Procesa y guarda los datos en Data
		readStream();
	}
	
	public boolean hasNext() {
		return (p1 <= p2);
	} 
	
	public void nextField() {
		// Obtenemos el siguiente byte
		int new_byte = ((int) stream.get(p1)) & 0xff;
		// No adelantamos el puntero hasta ver si tenemos suficientes datos para leer
		int data_amount = p2 - p1;	// Cantidad de bytes por leer (sin contar el delimitador)
		
		switch (new_byte) {
			case 0xcc:					// Offset
				if (data_amount >= 4) 	// Tenemos que leer 4 bytes
					readOffset();		// Comprueba, adem�s, si se han perdido muestras
				break;
			case 0xfb:					// HBR
				if (data_amount >= 2)
					readHBR(); 			// Lee el ritmo card�aco actual y lo guarda en data
				break;
			case 0xda:					// Sample
				if (data_amount >= 2)
					readSample();		// Lee y almacena el valor de la muestra en data
				break;
			case 0xed:					// Point
				if (data_amount >= 5)
					readDPoint(); 	// Tratar puntos de delineaci�n
				break;
			default:
				System.err.println("Delimitador no reconocido: " + new_byte);
		}
	}
	
	// Procesa y guarda todos los datos que tiene disponibles
	public void readStream() {
		while (hasNext())
			nextField();
	}
	
	
	// Devuelve el n�mero de muestras que se ha saltado, -1 si error
	// Coloca el �ndice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public void readOffset() {
		// Antes el valor menos significativo
		int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;
		
		// Calculamos el n�mero de muestras que nos dice el offset
		int nSamples = byte3 + 256*(byte2 + 256*(byte1 + 256*byte0));
		
		// Si no coincide con la �ltima muestra le�da, hemos perdido muestras
		if (lastSample < nSamples && lastSample > 0) {
			// Rellenamos los huecos vac�os de las muestras en data
			for (int i = 0; i < nSamples - lastSample; i++)
				//data.samples.add(null);
				dataSamples.add(null);
			// Actualizamos cu�l fue la �ltima muestra (perdida)
			lastSample = nSamples;
		}
		else if (lastSample == 0) {
			lastSample = nSamples;
			//data.dataOffset = nSamples;
			dataOffset = nSamples;
		}
			
		
		System.err.println("Se han perdido " + (nSamples - lastSample) + " muestras");
		
		// Adelantamos el puntero de lectura 5 posiciones (delimitador + 4 bytes)
		p1 += 5;
	}
	
	public void readHBR() {
		int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;
		// Calcula el ritmo card�aco (60*250/X)
		lastHBR = 15000f / ((float) (byte0*255 + byte1));
		// Guarda en data el valor del ritmo card�aco en este momento (index�ndolo seg�n la �ltima muestra recibida)
		//data.hbr.put(lastSample, (short) lastHBR);
		dataHBRs.put(lastSample, (short) lastHBR);
		
		// Adelantamos el puntero de lectura 3 posiciones (delimitador + 3 bytes)
		p1 += 3;
	}
	
	public void readSample() {
		// Incrementamos la cantidad de muestras le�das
		lastSample++;	
		// Calculamos el valor de la muestra
		short sample = byteToShort(stream.get(p1+1), stream.get(p1+2));
		// A�adimos la muestra con su n�mero de orden a la tabla de muestras
		//data.samples.add(sample);
		dataSamples.add(sample);
		// Adelantamos el puntero de lectura 3 posiciones
		p1 += 3;
	}
	
	public void readDPoint() {
		DPoint dp = new DPoint();
		
		int byte0 = ((int) stream.get(p1+1)) & 0xff;
		
		// Comprobamos el tipo de punto
		dp.setType(dp.checkType(byte0));
		// Comprobamos la onda a la que se refiere
		dp.setWave(dp.checkWave(byte0));
		
		// Vemos a qu� muestra se refiere
		int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;
		int byte4 = ((int) stream.get(p1+5)) & 0xff;
		int sample = byte4 + 256*(byte3 + 256*(byte2 + 256*byte1));
		
		// A�adimos el punto a la tabla de puntos de data
		//data.dpoints.put(sample, dp);
		dataDPoints.put(sample, dp);
		
		// Adelantamos el puntero de lectura 6 posiciones (delimitador + 5 bytes)
		p1 += 6;
	}
	
	// Old!
	/*public void extractSamples() {
		samples = new short[4000];
		
		
		for (int i = 0; i < 4000; i++)
			if (data.samples.get(i) == null)
				samples[i] = 0;
			else
				samples[i] = data.samples.get(i);
	}*/
	
	// Convierte dos bytes dados en su short correspondiente (en complemento a 2)
	public short byteToShort(byte b1, byte b2) {
		// b1 m�s significativo que b2
		int i1 = (int) b1;
		int i2 = (int) b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
	}
	
	public short getLastHBR() { return (short) lastHBR; }
	
	// Old!
	public short[] getSamples() { return samples; }

	public ArrayList<Short> getDataSamples() {
		return dataSamples;
	}

	public HashMap<Integer, DPoint> getDataDPoints() {
		return dataDPoints;
	}

	public HashMap<Integer, Short> getDataHBRs() {
		return dataHBRs;
	}

	public int getDataOffset() {
		return dataOffset;
	}
}
