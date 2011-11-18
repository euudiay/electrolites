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
	
	public FileParserService() {
		super("FileParserService");
		
		dp = new DataParser();
	}
	
	@Override
	public void startRunning(Intent intent) {
		System.out.println("Voy a leer data!");
	}
	
	@Override
	public void retrieveData(Intent intent) {
		
		synchronized(this) {
			d.loading = true;
			d.samples = new ArrayList<Short>();
			d.dpoints = new HashMap<Integer, DPoint>();
			d.offset = 0;
		}
		
		dp.loadBinaryFile(d.toLoad, samples, dpoints, hbrs, offset);
		
		synchronized(this) {
			d.samples.addAll(samples);
			d.dpoints.putAll(dpoints);
			d.offset = offset;
			d.loading = false;
		}
	}
}
