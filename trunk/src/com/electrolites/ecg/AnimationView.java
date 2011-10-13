package com.electrolites.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;

public class AnimationView extends SurfaceView implements SurfaceHolder.Callback {

	class AnimationThread extends Thread {
		private boolean _run;
		private long _lastTime;
		
		private int frameSamplesCollected = 0;
		private int frameSampleTime = 0;
		private int fps = 0;
		
		private SurfaceHolder _surfaceHolder;
		
		private Paint textPaint, linePaint;
		
		public AnimationThread(SurfaceHolder holder) {
			_surfaceHolder = holder;
			
			textPaint = new Paint();
			textPaint.setARGB(255, 255, 255, 255);
			textPaint.setTextSize(32.f);
			
			linePaint = new Paint();
			linePaint.setARGB(100, 255, 100, 200);
			linePaint.setStrokeWidth(3.f);
		}
		
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
		
		private void onUpdate() {
			long now = System.currentTimeMillis();
			
			if (_lastTime != 0) {
				int time = (int) (now - _lastTime);
				frameSampleTime += time;
				frameSamplesCollected++;
				
				if (frameSamplesCollected == 10) {
					fps = (int) (10000 / frameSampleTime);
					
					frameSampleTime = 0;
					frameSamplesCollected = 0;
				}
			}
			
			_lastTime = now;
		}
		
		private void onRender(Canvas canvas) {
			canvas.drawColor(Color.BLACK);
			canvas.drawText("fps: " + fps, 0, 0, textPaint);
			
			canvas.drawLine(0, getHeight()/2, getWidth()/4, getHeight()/2, linePaint);
			canvas.drawLine(getWidth()/4, getHeight()/2, getWidth()/2, getHeight()/4, linePaint);
			canvas.drawLine(getWidth()/2, getHeight()/4, 3*getWidth()/4, getHeight()/2, linePaint);
			canvas.drawLine(3*getWidth()/4, getHeight()/2, getWidth(), getHeight()/2, linePaint);
			
			canvas.restore();
		}
		
		private void setRunning(boolean b) {
			_run = b;
		}
	}

	private AnimationThread thread;
	
	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		thread = new AnimationThread(holder);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {}
		}
	}
}
