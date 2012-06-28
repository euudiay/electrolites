package org.microlites.data.generator;

import org.microlites.MicrolitesActivity;
import org.microlites.data.DataHolder;
import org.microlites.data.DataManager;


public class GeneratorManager implements DataManager {

	public final static String TAG = "GeneratorManager";
	
	private RandomGeneratorSourceThread thread;
	
	private DataHolder dataHolder;					// DataHolder Instance
	
	public GeneratorManager() {
		MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_GEN, 1, null);
	}
	
	public void start() {
		thread = new RandomGeneratorSourceThread(dataHolder);
		thread.start();
	}
	
	//@Override
	public void stop() {
		// Terminamos la actividad del thread de comunicación
		if (thread != null)
			thread.stop = true;
		
		MicrolitesActivity.instance.popView();
	}

	//@Override
	public void configure(DataHolder dataHolder) {
		this.dataHolder = dataHolder;
	}
	
	@Override
	public void back() {
		this.stop();
	}
}



