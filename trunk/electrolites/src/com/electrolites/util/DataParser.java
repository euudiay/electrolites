package com.electrolites.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.res.Resources;

import com.electrolites.data.Data;
import com.electrolites.util.DPoint.PointType;
import com.electrolites.util.DPoint.Wave;

public class DataParser {
	private FileConverter fc;		
	
	private ArrayList<Byte> stream;	// Bytes leÔøΩdos
	private int p1, p2;				// Punteros al ÔøΩltimo byte consumido y al ÔøΩltimo producido
	private int expected_bytes;		// Bytes que se espera que vengan
	private int lastSample;			// ÔøΩltima muestra leÔøΩda (nÔøΩmero de orden)
	private float lastHBR;			// ÔøΩltimo resultado de ritmo cardÔøΩaco leÔøΩdo
	
	// Referencias a las estructuras de DataService
	private ArrayList<Short> dataSamplesStatic;
	private ArrayList<SamplePoint> dataSamplesDynamic;
	private HashMap<Integer, DPoint> dataDPoints;
	private HashMap<Integer, Short> dataHBRs;
	private Integer dataOffset;
	
	private Data data;
	
	public DataParser() {
		stream = new ArrayList<Byte>();		// Instanciamos el vector de datos raw
		
		// Inicialmente no tenemos datos
		p1 = 0;
		p2 = 0;
		expected_bytes = 0;
		lastSample = 0;
		lastHBR = 0;
	}
	
	public void setData(Data data) {
		this.data = data;
	}
	
	// Obtiene datos para la aplicaciÔøΩn de un archivo binario
	public void loadBinaryFile(String fname, ArrayList<Short> samples, 
			HashMap<Integer, DPoint> dpoints, HashMap <Integer, Short> hbrs, Integer offset) {
		fc = new FileConverter();		// Instancia el conversor de archivos		
		stream = fc.readBinary(fname);	// Lee y almacena los datos del archivo
		
		// Inicializa los punteros de lectura y escritura
		p1 = 0;							
		p2 = stream.size() - 1;
		expected_bytes = 0;
		
		// Inicialmente no tenemos datos
		lastSample = 0;
		lastHBR = 0;
		
		// Procesa y guarda los datos en Data
		readStreamStatic(samples, dpoints, hbrs, offset);
	}
	
	// Obtiene datos para la aplicaciÔøΩn de un recurso interno
	public void loadResource(Resources resources, int id, ArrayList<Short> samples, 
			HashMap<Integer, DPoint> dpoints, HashMap <Integer, Short> hbrs, Integer offset) {
		fc = new FileConverter();					// Instancia el conversor de archivos
		stream = fc.readResources(resources, id);	// Carga el recurso con identificador id
		
		// Inicializa los punteros de lectura y de escritura
		p1 = 0;
		p2 = stream.size() - 1;
		expected_bytes = 0;
		
		// Inicialmente no tenemos datos
		lastSample = 0;
		lastHBR = 0;

		// Procesa y guarda los datos en Data
		readStreamStatic(samples, dpoints, hbrs, offset);
	}
	
	public boolean hasNext() {
		return (p1 <= p2 && p2-p1 >= expected_bytes);
	} 
	
	// Devuelve el nÔøΩmero de bytes que nos han sobrado (forman parte del siguiente flujo de bytes)
	public int nextField(boolean staticMode) {
		// Obtenemos el siguiente byte
		int new_byte = ((int) stream.get(p1)) & 0xff;
		// No adelantamos el puntero hasta ver si tenemos suficientes datos para leer
		int data_amount = p2 - p1;	// Cantidad de bytes por leer (sin contar el delimitador)
		
		switch (new_byte) {
			case 0xcc:					// Offset
				expected_bytes = 4;
				if (data_amount >= 4) 	// Tenemos que leer 4 bytes
					readOffset(staticMode);		// Comprueba, ademÔøΩs, si se han perdido muestras
				break;
			case 0xfb:					// HBR
				expected_bytes = 2;
				if (data_amount >= 2)
					readHBR(); 			// Lee el ritmo cardÔøΩaco actual y lo guarda en data
				break;
			case 0xda:					// Sample
				expected_bytes = 2;
				if (data_amount >= 2)
					readSample(staticMode);		// Lee y almacena el valor de la muestra en data
				break;
			case 0xed:					// Point
				expected_bytes = 5;
				if (data_amount >= 5)
					readDPoint(); 	// Tratar puntos de delineaciÔøΩn
				break;
			default:
				System.err.println("Delimitador no reconocido: " + new_byte);
				p1++;
		}
		
		return 0; // Si todo ha ido bien, no devuelve nada
	}
	
	// Procesa y guarda todos los datos que tiene disponibles
	public int readStreamDynamic(ArrayList<SamplePoint> samples, HashMap<Integer, DPoint> dpoints, 
			HashMap <Integer, Short> hbrs, Integer offset) {
		// Guardamos las referencias
		//samples = new ArrayList<SamplePoint>();
		dataSamplesDynamic = samples;
		dataDPoints = dpoints;
		dataHBRs = hbrs;
		dataOffset = offset;
		
		/*for (int i = 0; i < 24; i++)
			dataSamplesDynamic.add(new SamplePoint((short) i, (short)(new Random().nextInt())));
		return 0;*/
		
		//p1 = 0;
		p2 = stream.size() -1;// Esto de momento
		int bytes = 0;
		while (hasNext())
			if ((bytes = nextField(false)) != 0)
				System.out.println("Faltan " + bytes + " por parsear."); // Quedan cosas por leer
		//stream = new ArrayList<Byte>();
		System.out.println("Tama√±o de array en dataParser: " + stream.size());
		return bytes;
	}
	
	// Procesa y guarda todos los datos que tiene disponibles
	public int readStreamStatic(ArrayList<Short> samples, HashMap<Integer, DPoint> dpoints, 
			HashMap <Integer, Short> hbrs, Integer offset) {
		// Guardamos las referencias
		dataSamplesStatic = samples;
		dataDPoints = dpoints;
		dataHBRs = hbrs;
		dataOffset = offset;
		
		p1 = 0;
		p2 = stream.size() -1;// Esto de momento
		int bytes = 0;
		while (hasNext())
			if ((bytes = nextField(true)) != 0)
				System.out.println("Faltan " + bytes + " por parsear."); // Quedan cosas por leer
		return bytes;
	}
	
	
	// Devuelve el nÔøΩmero de muestras que se ha saltado, -1 si error
	// Coloca el ÔøΩndice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public void readOffset(boolean staticData) {
		// Antes el valor menos significativo
		/*int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;*/
		
		int first = stream.get(p1+1)  << 24 & 0xFF000000;	//11111111000000000000000000000000
		int second = stream.get(p1+2) << 16 & 0x00FF0000;	//00000000111111110000000000000000
		int third = stream.get(p1+3)  << 8  & 0x0000FF00;	//00000000000000001111111100000000
		int fourth = stream.get(p1+4)       & 0x000000FF;	//00000000000000000000000011111111
		
		// Calculamos el nÔøΩmero de muestras que nos dice el offset
		//int nSamples = byte3 + 256*(byte2 + 256*(byte1 + 256*byte0));
		int nSamples = first | second | third | fourth;
		
		// Add a debug offset dpoint
		DPoint p = new DPoint(PointType.start, Wave.Offset);
		//ExtendedDPoint ep = new ExtendedDPoint(lastSample , p);
		dataDPoints.put(nSamples, p);
		
		// Si no coincide con la ÔøΩltima muestra leÔøΩda, hemos perdido muestras
		if (staticData && lastSample < nSamples && lastSample > 0) {
			// Rellenamos los huecos vacÔøΩos de las muestras en data
			for (int i = 0; i < nSamples - lastSample; i++)
					dataSamplesStatic.add(null);
			System.err.println("Se han perdido " + (nSamples - lastSample) + " muestras");
			// Actualizamos cuÔøΩl fue la ÔøΩltima muestra (perdida)
			lastSample = nSamples;
		}
		else if (lastSample == 0) {
			lastSample = nSamples;
			//data.dataOffset = nSamples;
			dataOffset = nSamples;
		}
		
		// Adelantamos el puntero de lectura 5 posiciones (delimitador + 4 bytes)
		p1 += 5;
		expected_bytes = 0;
	}
	
	public void readHBR() {
		/*int byte0 = ((int) stream.get(p1+1)) & 0xff;
		int byte1 = ((int) stream.get(p1+2)) & 0xff;*/
		
		int first = stream.get(p1+1) << 8 &  0x0000FF00;	//00000000000000001111111100000000
		int second = stream.get(p1+2)     &  0x000000FF;	//00000000000000000000000011111111
	
		int beatSamples = first | second;
		
		// Calcula el ritmo cardÔøΩaco (60*250/X)
		//lastHBR = 15000f / (byte0*255 + byte1);
		lastHBR = 15000f / beatSamples;
		// Guarda en data el valor del ritmo cardÔøΩaco en este momento (indexÔøΩndolo segÔøΩn la ÔøΩltima muestra recibida)
		//data.hbr.put(lastSample, (short) lastHBR);
		dataHBRs.put(lastSample, (short) lastHBR);
		
		// Adelantamos el puntero de lectura 3 posiciones (delimitador + 3 bytes)
		p1 += 3;
		expected_bytes = 0;
	}
	
	public void readSample(boolean staticMode) {
		// Incrementamos la cantidad de muestras leÔøΩdas
		lastSample++;	
		// Calculamos el valor de la muestra
		short sample = byteToShort(stream.get(p1+1), stream.get(p1+2));
		// AÔøΩadimos la muestra con su nÔøΩmero de orden a la tabla de muestras
		if (staticMode)
			dataSamplesStatic.add(sample);
		else /*
			dataSamplesDynamic.add(new SamplePoint(lastSample, sample));
			*/
			synchronized (data.dynamicData) {
				Collection<SamplePoint> c = new ArrayList<SamplePoint>();
				c.add(new SamplePoint(lastSample, sample));
				data.dynamicData.addSamples(c);
			}
		// Adelantamos el puntero de lectura 3 posiciones
		p1 += 3;
		expected_bytes = 0;
	}
	
	public void readDPoint() {
		DPoint dp = new DPoint();
		
		int byte0 = ((int) stream.get(p1+1)) & 0xff;
		
		// Comprobamos el tipo de punto
		dp.setType(dp.checkType(byte0));
		// Comprobamos la onda a la que se refiere
		dp.setWave(dp.checkWave(byte0));
		
		// Vemos a quÔøΩ muestra se refiere
		/*int byte1 = ((int) stream.get(p1+2)) & 0xff;
		int byte2 = ((int) stream.get(p1+3)) & 0xff;
		int byte3 = ((int) stream.get(p1+4)) & 0xff;
		int byte4 = ((int) stream.get(p1+5)) & 0xff;
		int sample = byte4 + 256*(byte3 + 256*(byte2 + 256*byte1));
		
		// A�adimos el punto a la tabla de puntos de data
		//data.dpoints.put(sample, dp);
		dataDPoints.put(sample, dp);*/
		
		int first = stream.get(p1+2)  << 24 & 0xFF000000;	//11111111000000000000000000000000
		int second = stream.get(p1+3) << 16 & 0x00FF0000;	//00000000111111110000000000000000
		int third = stream.get(p1+4)  << 8  & 0x0000FF00;	//00000000000000001111111100000000
		int fourth = stream.get(p1+5)       & 0x000000FF;	//00000000000000000000000011111111
		
		// Calculamos el nÔøΩmero de muestras que nos dice el offset
		int nSamples = first | second | third | fourth;
			
		// AÔøΩadimos el punto a la tabla de puntos de data
		//data.dpoints.put(sample, dp);
		dataDPoints.put(nSamples, dp);
		
		// Adelantamos el puntero de lectura 6 posiciones (delimitador + 5 bytes)
		p1 += 6;
		expected_bytes = 0;
	}
	
	// Convierte dos bytes dados en su short correspondiente (en complemento a 2)
	public short byteToShort(byte b1, byte b2) {
		// b1 mÔøΩs significativo que b2
		int i1 = b1;
		int i2 = b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
	}
	
	public short getLastHBR() { return (short) lastHBR; }

	public ArrayList<SamplePoint> getDataSamples() {
		return dataSamplesDynamic;
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
	
	public void setStream(ArrayList<Byte> stream) { 
		this.stream = stream;
	}
	
	public void setP2(int p2) { this.p2 = p2; }

	public void clearDataSamples() {
		dataSamplesStatic.clear();
		dataSamplesDynamic.clear();
	}

	public void clearDataDPoints() {
		dataDPoints.clear();
	}

	public void clearDataHBRs() {
		dataHBRs.clear();
	}

	public void setDataOffset(int i) {
		dataOffset = i;
	}

	public void clearStream() {
		stream.clear();
	}

	public void addToStream(byte b) {
		stream.add(b);
	}

	public ArrayList<Byte> getStream() {
		return stream;
	}

	public int getP2() {
		return p2;
	}
}
