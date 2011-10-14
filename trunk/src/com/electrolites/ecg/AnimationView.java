package com.electrolites.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Point;
import android.graphics.RectF;
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
		
		private Paint textPaint, linePaint, linePaint2;
		
		public AnimationThread(SurfaceHolder holder) {
			_surfaceHolder = holder;
			
			textPaint = new Paint();
			textPaint.setARGB(255, 255, 255, 255);
			textPaint.setTextSize(32.f);
			
			linePaint = new Paint();
			linePaint.setARGB(200, 100, 255, 100);
			linePaint.setStrokeWidth(2.f);
			
			linePaint2 = new Paint();
			linePaint2.setARGB(200, 255, 255, 255);
			linePaint2.setStrokeWidth(1.f);
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
			canvas.drawText("fps: " + fps, 100, 100, textPaint);
			
			//canvas.drawLine(0, getHeight()/2, getWidth()/4, getHeight()/2, linePaint);
			//canvas.drawLine(getWidth()/4, getHeight()/2, getWidth()/2, getHeight()/4, linePaint);
			//canvas.drawLine(getWidth()/2, getHeight()/4, 3*getWidth()/4, getHeight()/2, linePaint);
			//canvas.drawLine(3*getWidth()/4, getHeight()/2, getWidth(), getHeight()/2, linePaint);
			
			Path cosa = new Path();
			cosa.addArc(new RectF(0, 0, getWidth(), getHeight()), 90, 90);
			//canvas.drawPath(cosa, linePaint);
			
			float w = getWidth();
			float h = getHeight();
			
			float[] points = {
				0, h / 2,
				w / 16, h / 2,
				
				w / 16, h / 2,
				w / 8, 2*h / 5,
				
				w / 8, 2*h / 5,
				3*w / 16, h / 2,
				
				3*w / 16, h / 2,
				w / 4, h / 2,
				
				w / 4, h / 2,
				5*w / 16, 3*h / 5,
				
				5*w / 16, 3*h / 5,
				3*w / 8, h / 2,
				
				3*w / 8, h / 2,
				7*w / 16, h / 10,
				
				7*w / 16, h / 10,
				w / 2, 4*h / 5,
				
				w / 2, 4*h / 5,
				9*w / 16, h / 2,
				
				9*w / 16, h / 2,
				11*w / 16, h / 2,
				
				11*w / 16, h / 2,
				3*w / 4, 3*h / 10,
				
				3*w / 4, 3*h / 10,
				13*w / 16, h / 2,
				
				13*w / 16, h / 2,
				w, h /2
			};
			
			float[] points2 = {
					0, h / 2 - 1,
					w / 16, h / 2 - 1,
					
					w / 16, h / 2 - 1,
					w / 8, 2*h / 5 - 1,
					
					w / 8, 2*h / 5 - 1,
					3*w / 16, h / 2 - 1,
					
					3*w / 16, h / 2 - 1,
					w / 4, h / 2 - 1,
					
					w / 4, h / 2 - 1,
					5*w / 16, 3*h / 5 - 1,
					
					5*w / 16, 3*h / 5 - 1,
					3*w / 8, h / 2 - 1,
					
					3*w / 8, h / 2 - 1,
					7*w / 16, h / 10 - 1,
					
					7*w / 16, h / 10 - 1,
					w / 2, 4*h / 5 - 1,
					
					w / 2, 4*h / 5 - 1,
					9*w / 16, h / 2 - 1,
					
					9*w / 16, h / 2 - 1,
					11*w / 16, h / 2 - 1,
					
					11*w / 16, h / 2 - 1,
					3*w / 4, 3*h / 10 - 1,
					
					3*w / 4, 3*h / 10 - 1,
					13*w / 16, h / 2 - 1,
					
					13*w / 16, h / 2 - 1,
					w, h /2 - 1
				};
			
			canvas.drawLines(points, linePaint);
			canvas.drawLines(points2, linePaint2);
			canvas.drawTextOnPath("Pferv es un chico apuesto", cosa, 2.f, 2.f, linePaint);
			
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
