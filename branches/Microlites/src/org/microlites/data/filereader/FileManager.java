package org.microlites.data.filereader;

import java.io.File;

import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.data.DataManager;
import org.microlites.view.still.StaticViewThread;

import android.os.Environment;

public class FileManager implements DataManager {
	
	FileDataSourceThread dataSource;			// File Data Source
	File path;									// Path to log file
	
	public FileManager() {
		
	}
	
	@Override
	public void configure(DataHolder dataHolder) {
		File root = Environment.getExternalStorageDirectory();
		//File path = new File(root, "/Download/raw-1903-2012_13-19.txt");
		path = new File(root, "/Download/a.log");
	}

	@Override
	public void start() {
		dataSource = new FileDataSourceThread(null, path.getAbsolutePath());
		dataSource.start();
		((StaticViewThread) Data.getInstance().currentViewThread).setDataSource(dataSource);
	}

	@Override
	public void stop() {
		boolean retry = true;
		while (retry) {
			try {
				dataSource.finish();
				dataSource.join();
				retry = false;
			} catch (InterruptedException e) {
				
			}
		}
	}
}
