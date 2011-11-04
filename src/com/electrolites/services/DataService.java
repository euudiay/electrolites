package com.electrolites.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.electrolites.data.DPoint;
import com.electrolites.data.Data;

public class DataService extends IntentService {
	public static String START_RUNNING = "start!";
	public static String STOP_RUNNING = "stop!";
	public static String RETRIEVE_DATA = "catchEmAll!";
	public static String GET_DATA = "gimmeGimme!";
	
	Data d;
	
	// Muestras indexadas por no. de muestra
	public ArrayList<Short> samples = null;
	// Puntos resultantes de la delineaci�n, indexados por no. de muestra
	public Map<Integer, DPoint> dpoints = null;
	// Valores del ritmo card�aco, indexados seg�n el n�mero de muestra anterior a su recepci�n
	public Map<Integer, Short> hbr = null;
	// Primera muestra dibujable
	public int dataOffset;
	
	public DataService(String name) {
		super(name);
		System.out.println("DataService (parent) arrancando");
		d = Data.getInstance();
		samples = new ArrayList<Short>();
		dpoints = new HashMap<Integer, DPoint>();
		hbr = new HashMap<Integer, Short>();
		dataOffset = -1;
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
		d.hbr.putAll(hbr);
		hbr = new HashMap<Integer, Short>();
		if (dataOffset != -1) {
			d.dataOffset = dataOffset;
			dataOffset = -1;
		}
		
	}
}