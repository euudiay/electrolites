package com.electrolites.services;

import java.util.Random;

import android.content.Intent;

public class RandomGeneratorService extends DataService {

	Random r;
	
	public RandomGeneratorService() {
		super("RandomGeneratorService");
		r = new Random();
	}
	
	@Override
	public void startRunning(Intent intent) {
	}
	
	public void retrieveData(Intent intent) {
		short base = 0;
		short value;
		for (int i = 0; i < 5*250; i++) {
			synchronized (this) {
				try {
					value = (short) (r.nextInt(100)-50);
					base += value;
					d.samples.add(new Short(base));
					wait(4);
				} catch (InterruptedException e) {
					System.err.println("He sido interrumpido!");
				}
			}
		}
	}
}
