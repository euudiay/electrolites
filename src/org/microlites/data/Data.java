package org.microlites.data;

import org.microlites.view.dynamic.DynamicViewThread;

import android.view.SurfaceHolder;

public class Data {
	// Singleton pattern
	protected static Data _instance = null;
	public static Data getInstance() {
		if (_instance == null)
			_instance = new Data();
		
		return _instance;
	}
	
	public Object mutex;
	public float drawBaseHeight = 0.5f;
	public float viewWidth = 1.5f;
	public DynamicViewThread dynamicThread = null;
	public SurfaceHolder currentViewHolder = null;
	public boolean pause = false;
	
	protected Data() {
		// TODO: Data initialization
		mutex = new Object();
	}
}
