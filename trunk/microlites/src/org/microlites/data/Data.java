package org.microlites.data;

import org.microlites.view.AnimationThread;

import android.view.SurfaceHolder;

public class Data {
	// Singleton pattern
	protected static Data _instance = null;
	public static Data getInstance() {
		if (_instance == null)
			_instance = new Data();
		
		return _instance;
	}
	
	/* Temporary storage */
	// TODO: Clean and organize this
	public Object mutex;
	public float drawBaseHeight = 0.5f;
	public float viewWidth = 1.5f;
	// public DynamicViewThread dynamicThread = null;
	// public StaticViewThread staticThread = null;
	public AnimationThread currentViewThread = null;
	public SurfaceHolder currentViewHolder = null;
	public boolean pause = false;
	public float yScaleFactor = 12000.0f;
	
	public boolean generateZeros = false;
	public short generateHeight = 2000;
	
	protected Data() {
		// TODO: Data initialization
		mutex = new Object();
	}
}
