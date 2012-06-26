package org.microlites.data.generator;

import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.data.DataSourceThread;

public class InputGeneratorSourceThread extends DataSourceThread {
	public final static String TAG = "GeneratorSourceThread";
	
	public boolean stop;
	
	// Test land of awesomeness
	private DataHolder holder;
	
	public InputGeneratorSourceThread(DataHolder holder) {
		this.holder = holder;
	}
	
	@Override
	public void run() {
		int index = 0;
		Data d = Data.getInstance();
		
		while (!stop) {
			if (d.generateZeros) {
				holder.addSample(index++, (short) 0);
			} else {
				holder.addSample(index++, (short) d.generateHeight);
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