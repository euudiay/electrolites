package com.electrolites.services;

import java.util.Random;

import android.content.Intent;

public class FileParserService extends DataService {

	Random r;
	
	public FileParserService() {
		super("FileParserService");
		System.out.println("Arrancho serivce!");
		r = new Random();
	}
	
	@Override
	public void startRunning(Intent intent) {
		System.out.println("Voy a leer data!");
	}
	
	public void retrieveData(Intent intent) {
		for (int i = 0; i < 20; i++) {
			synchronized (this) {
				try {
					for (int j = 0; j < 20; j++) {
						//System.out.println("HAGO DATAR!!");
						short value = (short) (r.nextInt(2000)-1000);
						samples.add(new Short(value));
					}
					wait(1000);
				} catch (InterruptedException e) {
					System.err.println("He sido interrumpido!");
				}
			}
		}
	}
}
