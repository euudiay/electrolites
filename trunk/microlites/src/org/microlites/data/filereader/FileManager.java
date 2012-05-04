package org.microlites.data.filereader;

import java.io.File;
import java.io.FilenameFilter;

import org.microlites.MicrolitesActivity;
import org.microlites.R;
import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.data.DataManager;
import org.microlites.view.still.StaticViewThread;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FileManager implements DataManager {
	
	FileDataSourceThread dataSource;			// File Data Source
	public File path;							// Path to log file
	
	public FileManager() {
		path = null;

		// MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_FILELOG, 1, null);
		buildView();
	}
	
	//@Override
	public void configure(DataHolder dataHolder) {
	}

	//@Override
	public void start() {
		dataSource = new FileDataSourceThread(null, path.getAbsolutePath());
		dataSource.start();
		((StaticViewThread) Data.getInstance().currentViewThread).setDataSource(dataSource);
	}

	//@Override
	public void stop() {
		if (dataSource != null) {
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
		
		MicrolitesActivity.instance.popView();
	}
	
	public void buildView() {
		MicrolitesActivity act = MicrolitesActivity.instance; 
		LayoutInflater li = act.getLayoutInflater();
		View view = li.inflate(R.layout.logchooserlayout, null);
		ListView lv = (ListView) view.findViewById(R.id.listView);
		
		File root = Environment.getExternalStorageDirectory();
		File path = new File(root, "/Download/");
		
		File[] files = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return (!filename.contains("log-") &&
						(filename.endsWith(".log") || filename.endsWith(".txt")));
			}
		});
		
		String[] fnames = new String[files.length];
		String[] sizes = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fnames[i] = files[i].getName();
			sizes[i] = "" + (files[i].length() / 1024) + "KB";
		}
		
		lv.setAdapter(new LogArrayAdapter(act.getApplicationContext(), fnames, sizes));
		lv.setFocusable(true);
		lv.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
				File root = Environment.getExternalStorageDirectory();
				((FileManager) MicrolitesActivity.instance.getCurrentManager()).path = new File(root, "/Download/"+parent.getItemAtPosition(position));
				MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_FILELOG, 1, null);
			}
		});
		
		act.pushView(view);
	}
}
