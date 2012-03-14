package com.electrolites.services;

import java.util.Random;

import android.content.Intent;

import com.electrolites.util.SamplePoint;

public class RandomGeneratorService extends DataService {

	Random r;
	
	public RandomGeneratorService() {
		super("RandomGeneratorService");
		r = new Random();
	}
	
	@Override
	public void startRunning(Intent intent) {
	}
	
	@Override
	public void retrieveData(Intent intent) {
		short base = 0;
		short value;
		for (int i = 0; i < 250; i++) {
			synchronized (data.dynamicData.mutex) {
				value = (short) (r.nextInt(100)-50);
				base += value;
				data.dynamicData.samplesQueue.add(new SamplePoint(0, base));
			}
		}
	}
}
