package com.electrolites.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.Intent;

import com.electrolites.data.DPoint;
import com.electrolites.util.DataParser;

public class FileParserService extends DataService {

	Random r;
	DataParser dp;
	
	// Muestras indexadas por no. de muestra
	protected ArrayList<Short> samples;
	// Puntos resultantes de la delineación, indexados por número de muestra
	protected HashMap<Integer, DPoint> dpoints;
	// Valores del ritmo cardíaco, indexados según el número de muestra anterior a su recepción
	protected HashMap<Integer, Short> hbrs;
	// Primera muestra dibujable
	protected Integer offset;
	
	public FileParserService() {
		super("FileParserService");
		
		dp = new DataParser();
		samples = new ArrayList<Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbrs = new HashMap<Integer, Short>();
		offset = new Integer(15000);
	}
	
	@Override
	public void startRunning(Intent intent) {
		System.out.println("Voy a leer data!");
	}
	
	@Override
	public void retrieveData(Intent intent) {
		
		synchronized(this) {
			data.loading = true;
			data.samples = new ArrayList<Short>();
			data.dpoints = new HashMap<Integer, DPoint>();
			data.offset = 0;
		}
		
		dp.loadBinaryFile(data.toLoad, samples, dpoints, hbrs, offset);
		
		synchronized(this) {
			data.samples.addAll(samples);
			data.dpoints.putAll(dpoints);
			data.offset = offset;
			data.loading = false;
		}
	}
}
