package org.microlites.data.filereader;

import java.io.File;
import java.io.FilenameFilter;

import org.microlites.MicrolitesActivity;
import org.microlites.R;
import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.data.DataManager;
import org.microlites.view.still.StaticViewThread;

import android.app.Dialog;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class FileManager implements DataManager {
	
	FileDataSourceThread dataSource;			// File Data Source
	public File path;							// Path to log file
	
	public final float BIG_FILE_SIZE = 1000f;	// File size to be considered large  
	
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
					dataSource.running = false;
					dataSource.stop[0] = true;
					dataSource.finish();
					dataSource.join();
					retry = false;
				} catch (InterruptedException e) {
					
				}
				
				dataSource = null;
				
				MicrolitesActivity.instance.setViewControlButtons(false);
			}
		} else {
			MicrolitesActivity.instance.endCurrentManagerOperation();
		}
		
		MicrolitesActivity.instance.popView();
		//
	}
	
	public void buildView() {
		MicrolitesActivity act = MicrolitesActivity.instance; 
		LayoutInflater li = act.getLayoutInflater();
		View view = li.inflate(R.layout.logchooserlayout, null);
		ListView lv = (ListView) view.findViewById(R.id.listView);
		
		File root = Environment.getExternalStorageDirectory();
		File path = new File(root, "/Download/");
		
		File[] files = path.listFiles(new FilenameFilter() {
			// @Override
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
			// @Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {			
				File root = Environment.getExternalStorageDirectory();
				File file = new File(root, "/Download/"+parent.getItemAtPosition(position));
				((FileManager) MicrolitesActivity.instance.getCurrentManager()).path = file;
				
				if (file.length() / 1024 > BIG_FILE_SIZE)
					showConfirmDialog();
				else
					MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_FILELOG, 1, null);
			}
		});
		
		act.pushView(view);
	}
	
	private void showConfirmDialog() {
		// Store a reference to the activity instance
		MicrolitesActivity act = MicrolitesActivity.instance;
		
		// Display Bluetooth Settings dialog
		final Dialog dialog = new Dialog(act);
		
		dialog.setTitle(R.string.logLoadTitle);
		dialog.setContentView(R.layout.confirmlogloadinglayout);
		dialog.setOwnerActivity(act);
		dialog.show();
		
		// Set button listener
		Button b = (Button) dialog.findViewById(R.id.logLoadStart);
		b.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				dialog.cancel();
				MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_FILELOG, 1, null);
			}
		});
		
		b = (Button) dialog.findViewById(R.id.logLoadCancel);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((FileManager) MicrolitesActivity.instance.getCurrentManager()).path = null;
				dialog.cancel();
			}
		});
	}

	public void back() {
		if (this.dataSource == null) {
			MicrolitesActivity.instance.popView();
		}
		else
			this.stop();
	}
}
