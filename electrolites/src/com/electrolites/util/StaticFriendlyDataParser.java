package com.electrolites.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.electrolites.util.DPoint.PointType;
import com.electrolites.util.DPoint.Wave;

public class StaticFriendlyDataParser {
	public final static boolean DEBUG = true;
	public final static String TAG = "StaticFriendlyDataParser";
	
	private ArrayList<Byte> stream;
	private ArrayList<SamplePoint> samples;
	private ArrayList<ExtendedDPoint> dpoints;
	private HashMap<Integer, Short> hbrs;
	private int offset;
	
	private int p1, p2;
	private int expected_bytes;
	private int lastSample;		
	private float lastHBR;		
	
	public StaticFriendlyDataParser() {
		samples = new ArrayList<SamplePoint>();
		dpoints = new ArrayList<ExtendedDPoint>();
		hbrs = new HashMap<Integer, Short>();
		
		p1 = 0;
		p2 = 0;
		expected_bytes = 0;
		lastSample = 0;
		lastHBR = 0;
	}
	
	public void parseStream() {
		if (stream != null) {
			// Inicializa los punteros de lectura y escritura
			p1 = 0;							
			p2 = stream.size() - 1;
			expected_bytes = 0;
			
			// Inicialmente no tenemos datos
			lastSample = 0;
			lastHBR = 0;
			
			// Procesa y guarda los datos en Data
			//readStreamStatic(samples, dpoints, hbrs, offset);
			int bytes = 0;
			while(hasNext())
				if ((bytes = nextField()) != 0 && DEBUG)
					Log.d(TAG, bytes + " bytes remaining.");
						
		}
	}
	
	private boolean hasNext() {
		return (p1 <= p2 && p2 - p1 >= expected_bytes);
	}
	
	public int nextField() {
		// Obtenemos el siguiente byte
		int new_byte = ((int) stream.get(p1)) & 0xff;
		// No adelantamos el puntero hasta ver si tenemos suficientes datos para leer
		int data_amount = p2 - p1;	// Cantidad de bytes por leer (sin contar el delimitador)
		
		switch (new_byte) {
			case 0xcc:					// Offset
				expected_bytes = 4;
				if (data_amount >= 4) 	// Tenemos que leer 4 bytes
					readOffset();		// Comprueba, ademÔøΩs, si se han perdido muestras
				break;
			case 0xfb:					// HBR
				expected_bytes = 2;
				if (data_amount >= 2)
					readHBR(); 			// Lee el ritmo cardÔøΩaco actual y lo guarda en data
				break;
			case 0xda:					// Sample
				expected_bytes = 2;
				if (data_amount >= 2)
					readSample();		// Lee y almacena el valor de la muestra en data
				break;
			case 0xed:					// Point
				expected_bytes = 5;
				if (data_amount >= 5)
					readDPoint(); 	// Tratar puntos de delineaciÔøΩn
				break;
			default:
				System.err.println("Unknown byte: " + new_byte);
				p1++;
		}
		
		return 0; // Si todo ha ido bien, no devuelve nada
	}
	
	// Devuelve el nÔøΩmero de muestras que se ha saltado, -1 si error
	// Coloca el ÔøΩndice en el siguiente byte a los 5 del offset
	// Actualiza el HBR con el nuevo valor
	public void readOffset() {
		int first = stream.get(p1+1)  << 24 & 0xFF000000;	//11111111000000000000000000000000
		int second = stream.get(p1+2) << 16 & 0x00FF0000;	//00000000111111110000000000000000
		int third = stream.get(p1+3)  << 8  & 0x0000FF00;	//00000000000000001111111100000000
		int fourth = stream.get(p1+4)       & 0x000000FF;	//00000000000000000000000011111111
		
		// Calculamos el nÔøΩmero de muestras que nos dice el offset
		int nSamples = first | second | third | fourth;
		
		// Add a debug offset dpoint
		if (DEBUG) {
			DPoint p = new DPoint(PointType.start, Wave.Offset);
			//ExtendedDPoint ep = new ExtendedDPoint(nSamples , p);
			ExtendedDPoint ep = new ExtendedDPoint(lastSample , p);
			dpoints.add(ep);
		}
		
		// Si no coincide con la ÔøΩltima muestra leÔøΩda, hemos perdido muestras
		if (lastSample < nSamples && lastSample > 0) {
			// Rellenamos los huecos vacÔøΩos de las muestras en data
			for (int i = 0; i < nSamples - lastSample; i++)
					samples.add(null);
			System.err.println((nSamples - lastSample) + " samples were lost.");
			// Actualizamos cuÔøΩl fue la ÔøΩltima muestra (perdida)
			lastSample = nSamples;
		}
		else if (lastSample == 0) {
			lastSample = nSamples;
			offset = nSamples;
		}
		
		// Adelantamos el puntero de lectura 5 posiciones (delimitador + 4 bytes)
		p1 += 5;
		expected_bytes = 0;
	}
		
	public void readHBR() {
		int first = stream.get(p1+1) << 8 &  0x0000FF00;	//00000000000000001111111100000000
		int second = stream.get(p1+2)     &  0x000000FF;	//00000000000000000000000011111111
	
		int beatSamples = first | second;
		
		// Calcula el ritmo cardÔøΩaco (60*250/X)
		lastHBR = 15000f / beatSamples;
		// Guarda en data el valor del ritmo cardÔøΩaco en este momento (indexÔøΩndolo segÔøΩn la ÔøΩltima muestra recibida)
		hbrs.put(lastSample, (short) lastHBR);
		
		// Adelantamos el puntero de lectura 3 posiciones (delimitador + 3 bytes)
		p1 += 3;
		expected_bytes = 0;
	}
	
	public void readSample() {	
		// Calculamos el valor de la muestra
		short sample = byteToShort(stream.get(p1+1), stream.get(p1+2));
		// AÔøΩadimos la muestra con su nÔøΩmero de orden a la tabla de muestras
		samples.add(new SamplePoint(lastSample, sample));
		// Incrementamos la cantidad de muestras leÔøΩdas
		lastSample++;
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
		
		int first = stream.get(p1+2)  << 24 & 0xFF000000;	//11111111000000000000000000000000
		int second = stream.get(p1+3) << 16 & 0x00FF0000;	//00000000111111110000000000000000
		int third = stream.get(p1+4)  << 8  & 0x0000FF00;	//00000000000000001111111100000000
		int fourth = stream.get(p1+5)       & 0x000000FF;	//00000000000000000000000011111111
		
		// Calculamos el nÔøΩmero de muestras que nos dice el offset
		int nSamples = first | second | third | fourth;
			
		// Añadimos el punto a la tabla de puntos de data
		dpoints.add(new ExtendedDPoint(nSamples, dp));
		
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

	public void setStream(ArrayList<Byte> stream) {
		this.stream = stream;
	}

	public ArrayList<SamplePoint> getSamples() {
		return samples;
	}

	public ArrayList<ExtendedDPoint> getDPoints() {
		return dpoints;
	}

	public HashMap<Integer, Short> getHBRs() {
		return hbrs;
	}

	public int getOffset() {
		return offset;
	}
}
