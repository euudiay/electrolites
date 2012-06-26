package org.microlites.data.generator;

import org.microlites.data.DataHolder;
import org.microlites.data.DataSourceThread;

public class RandomGeneratorSourceThread extends DataSourceThread {
	public final static String TAG = "RandomGeneratorSourceThread";
	
	public boolean stop;
	
	// Test land of awesomeness
	private DataHolder holder;
	
	private final static int COUNTER = 125;
	
	public RandomGeneratorSourceThread(DataHolder holder) {
		this.holder = holder;
	}
	
	@Override
	public void run() {
		int counter = (int) (COUNTER/2.f + Math.random()*COUNTER);
		boolean zeroTime = false;
		int index = 0;

		while (!stop) {
			// Do things
			if (--counter > 0) {
				if (zeroTime) {
					holder.addSample(index++, (short) 0);
				} else {
					holder.addSample(index++, (short) (Math.random()*2000));
				}
			} else {
				zeroTime = !zeroTime;
				counter = (int) (COUNTER/2.f + Math.random()*COUNTER);
			}
			
			try {
				sleep(16);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	public void halt() { stop = true; }

	/** DataSourceThread implementation **/
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSending() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}
}	