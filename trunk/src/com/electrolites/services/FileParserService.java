package com.electrolites.services;

import java.util.Random;

import com.electrolites.util.DataParser;

import android.content.Intent;

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
		}
		
		dp.loadBinaryFile(d.toLoad, samples, dpoints, hbrs, offset);
		
		try {
			synchronized(this) {
				wait(1000);
				d.samples.addAll(samples);
				d.dpoints.putAll(dpoints);
				d.offset = offset;
				d.loading = false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
