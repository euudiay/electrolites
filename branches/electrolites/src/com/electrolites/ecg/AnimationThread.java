package com.electrolites.ecg;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public abstract class AnimationThread extends Thread {
	private boolean _run;
	private long _lastTime;
	
	private int frameSamplesCollected = 0;
	private int frameSampleTime = 0;
	protected int fps = 0;
	protected int count = 0;
	
	private SurfaceHolder _surfaceHolder;

	public AnimationThread(SurfaceHolder holder) {
		_surfaceHolder = holder;
	}
	
	public int getFPS() { return fps; }
	
	@Override
	public void run() {
		while (_run) {
			Canvas c = null;
			try {
				c = _surfaceHolder.lockCanvas(null);
				synchronized (_surfaceHolder) {
					onUpdate();
					onRender(c);
				}
			} finally {
				if (c != null) {
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
	
	protected void onUpdate() {
		long now = System.currentTimeMillis();
		
		if (_lastTime != 0) {
			int time = (int) (now - _lastTime);
			frameSampleTime += time;
			frameSamplesCollected++;
			
			if (frameSamplesCollected == 10) {
				fps = (10000 / frameSampleTime);
				
				frameSampleTime = 0;
				frameSamplesCollected = 0;
			}
		}
		
		_lastTime = now;
	}
	
	public abstract void onRender(Canvas canvas);
	
	public void setRunning(boolean b) {
		_run = b;
	}	
}

