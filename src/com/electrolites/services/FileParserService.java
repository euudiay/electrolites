package com.electrolites.services;

import java.util.Random;

import com.electrolites.util.DataParser;

import android.content.Intent;

public class FileParserService extends DataService {

	Random r;
	
	public FileParserService() {
		super("FileParserService");
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
		dp.loadBinaryFile(d.toLoad);
		try {
			synchronized(this) {
				wait(1000);
				d.samples.addAll(dp.getDataSamples());
				d.dpoints.putAll(dp.getDataDPoints());
				d.dataOffset = dp.getDataOffset();
				d.loading = false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
