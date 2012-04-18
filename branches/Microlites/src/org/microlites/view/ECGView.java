package org.microlites.view;

import org.microlites.MicrolitesActivity;
import org.microlites.data.Data;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class ECGView extends AnimationView {
	
	MicrolitesActivity currentActivity;
	public byte notifyAboutCreation;
	
	public ECGView(Context context, AttributeSet attrs, MicrolitesActivity act) {
		super(context, attrs);
		thread = null;
		currentActivity = act;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Data.getInstance().currentViewHolder = holder;
		Data.getInstance().currentViewThread.onSurfaceChange(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Data.getInstance().currentViewHolder = holder;
		currentActivity.initVisualization(notifyAboutCreation, 2, this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		if (thread != null) {
			thread.setRunning(false);
			thread.finish();
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void handleScroll(float distX, float distY, float x, float y) {
		if (thread != null)
			thread.handleScroll(distX, distY, x, y);
	}
	
	public AnimationThread getThread() {
		return thread;
	}
	
	public void setThread(AnimationThread t) {
		thread = t;
		try {
			if (!thread.isAlive()) {
				thread.setRunning(true);
				thread.start();
			}
		} catch (Exception e) {
		}
	}
};
