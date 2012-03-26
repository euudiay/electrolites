package org.microlites.view.still;

import org.microlites.data.StaticDataHolder;
import org.microlites.data.filereader.FileDataSourceThread;
import org.microlites.view.AnimationView;
import org.microlites.view.dynamic.DynamicViewThread;

import android.view.SurfaceHolder;

public class StaticViewThread extends DynamicViewThread 
							  implements StaticDataHolder {
	/* Scroll Handling Items */
	protected FileDataSourceThread dataSource;		// Data Source Thread
	
	public StaticViewThread(SurfaceHolder holder, AnimationView aview) {
		super(holder, aview);
	}
	
	public void setDataSource(FileDataSourceThread t) {
		this.dataSource = t;
	}
	
	/* StaticDataHolder implementation */
	@Override
	public void setSamplesArrays(int[] indexes, short[] amplitudes, 
								 int from, int to, int size) {
		s_start = from;
		s_end = to;
		s_size = size;
		s_index = indexes;
		s_amplitude = amplitudes;
	}
}
