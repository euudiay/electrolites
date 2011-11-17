package com.electrolites.services;

import android.content.Intent;

public class DummyService extends DataService {
	//Data d;
	
	public DummyService() {
		super("DummyService");
		System.out.println("Dummy says hiya there!!");
//		d = Data.getInstance();
	}

	@Override
	protected void startRunning(Intent intent) {
		// Work!
		for (int i = 0; i < 5; i++) {
			synchronized (this) {
				try {
					System.out.println("NO VA!");
					d.setWidthScale(d.getWidthScale()*2);
					wait(2000);
				} catch (Exception pferv) {
					pferv.printStackTrace();
				}
			}
		}
	}
}