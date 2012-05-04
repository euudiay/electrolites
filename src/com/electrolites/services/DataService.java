package com.electrolites.services;

import android.app.IntentService;
import android.content.Intent;

import com.electrolites.data.Data;

public class DataService extends IntentService {
	public static String START_RUNNING = "start!";
	public static String STOP_RUNNING = "stop!";
	public static String RETRIEVE_DATA = "catchEmAll!";
	public static String GET_DATA = "gimmeGimme!";
	
	Data data;
	
	public DataService(String name) {
		super(name);
		System.out.println("DataService (parent) arrancando");
		data = Data.getInstance();
	}
	
	@Override
	public void onCreate() {
	    super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(START_RUNNING))
			startRunning(intent);
		else if (intent.getAction().equals(STOP_RUNNING))
			stopRunning(intent);
		else if (intent.getAction().equals(RETRIEVE_DATA))
			retrieveData(intent);
	}
	
	protected void startRunning(Intent intent) {
	}
	
	protected void stopRunning(Intent intent) {
	}
	
	protected void retrieveData(Intent intent) {
	}
}