package com.electrolites.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;

import com.electrolites.data.DPoint;
import com.electrolites.data.Data;
import com.electrolites.util.DataParser;

public class DataService extends IntentService {
	public static String START_RUNNING = "start!";
	public static String STOP_RUNNING = "stop!";
	public static String RETRIEVE_DATA = "catchEmAll!";
	public static String GET_DATA = "gimmeGimme!";
	
	Data d;
	
	// Muestras indexadas por no. de muestra
	protected ArrayList<Short> samples;
	// Puntos resultantes de la delineación, indexados por número de muestra
	protected HashMap<Integer, DPoint> dpoints;
	// Valores del ritmo cardíaco, indexados según el número de muestra anterior a su recepción
	protected HashMap<Integer, Short> hbrs;
	// Primera muestra dibujable
	protected int offset;
	
	public DataService(String name) {
		super(name);
		System.out.println("DataService (parent) arrancando");
		d = Data.getInstance();
		samples = new ArrayList<Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbrs = new HashMap<Integer, Short>();
		offset = -1;
	}
	
	@Override
	public void onCreate() {
	    // TODO Auto-generated method stub
	    super.onCreate();
	    // pushBackground();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Work!
		if (intent.getAction().equals(START_RUNNING))
			startRunning(intent);
		else if (intent.getAction().equals(STOP_RUNNING))
			stopRunning(intent);
		else if (intent.getAction().equals(RETRIEVE_DATA))
			retrieveData(intent);
		else if (intent.getAction().equals(GET_DATA))
			getData(intent);
	}
	
	protected void startRunning(Intent intent) {
	}
	
	protected void stopRunning(Intent intent) {
	}
	
	protected void retrieveData(Intent intent) {
	}
	
	protected void getData(Intent intent) {
		d.samples.addAll(samples);
		samples = new ArrayList<Short>();
		d.dpoints.putAll(dpoints);
		dpoints = new HashMap<Integer, DPoint>();
		d.hbrs.putAll(hbrs);
		hbrs = new HashMap<Integer, Short>();
		
		if (offset != -1) {
			d.offset = offset;
			offset = -1;
		}
		
	}
}