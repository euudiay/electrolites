package com.electrolites.ecg;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;

import com.electrolites.util.*;
import com.electrolites.data.*;

public class AnimationView extends SurfaceView implements SurfaceHolder.Callback {

	class AnimationThread extends Thread {
		private boolean _run;
		private long _lastTime;
		
		private int frameSamplesCollected = 0;
		private int frameSampleTime = 0;
		private int fps = 0;
		private int count = 0;
		
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

			Path cosa = new Path();
			cosa.addArc(new RectF(0, 0, getWidth(), getHeight()), 90, 90);
			//canvas.drawPath(cosa, linePaint);
			
			data.drawBaseHeight += -3*(int) Math.random() + (int) Math.random()*3;
			
			float w = getWidth();
			float h = getHeight();

			float points[] = buildEcg(-h/10, h/10, -h/10*4, h/10*2, -h/10*1.5f);
			canvas.drawLines(points, 0, count,  linePaint);
			if (count < 48)
				count += 4;
			else 
				count = 0;
			canvas.drawTextOnPath("Pferv es un chico apuesto", cosa, 2.f, 2.f, linePaint);
			
			canvas.restore();
		}
		
		private float[] buildEcg(float p, float q, float r, float s, float t) {
			
			float baseHeight = getHeight()/2 + data.drawBaseHeight;
			float xI = 0 , xF = getWidth();
			float pX, rX, qX, sX, tX;
			float pI, pF, qI, sF, tI, tF;
			
			pX = getWidth()/4*0.95f;
			qX = getWidth()/2*0.90f;
			rX = getWidth()/2;
			sX = getWidth()/2*1.1f;
			tX = 3*getWidth()/4*1.05f;
			
			pI = pX*0.95f;
			pF = pX*1.05f;
			qI = qX*0.95f;
			sF = sX*1.05f;
			tI = tX*0.95f;
			tF = tX*1.05f;
			
			ArrayList<Point> ecg = new ArrayList<Point>();
			ecg.add(new Point(xI, baseHeight));
			ecg.add(new Point(pI, baseHeight));
			ecg.add(new Point(pX, baseHeight+p));
			ecg.add(new Point(pF, baseHeight));
			ecg.add(new Point(qI, baseHeight));
			ecg.add(new Point(qX, baseHeight+q));
			ecg.add(new Point(rX, baseHeight+r));
			ecg.add(new Point(sX, baseHeight+s));
			ecg.add(new Point(sF, baseHeight));
			ecg.add(new Point(tI, baseHeight));
			ecg.add(new Point(tX, baseHeight+t));
			ecg.add(new Point(tF, baseHeight));
			ecg.add(new Point(xF, baseHeight));
			
			float list[] = new float[48];
			list[0] = ecg.get(0).getX();
			list[1] = ecg.get(0).getY();
			for (int i = 1; i < ecg.size()-1; i++) {
				list[i*4-2] = ecg.get(i).getX();
				list[i*4-1] = ecg.get(i).getY();
				list[i*4] = ecg.get(i).getX();
				list[i*4+1] = ecg.get(i).getY();
			}
			list[46] = ecg.get(ecg.size()-1).getX();
			list[47] = ecg.get(ecg.size()-1).getY();
			
			return list;
		}
		
		private void setRunning(boolean b) {
			_run = b;
		}
	}

	private Data data;
	
	private AnimationThread thread;
	
	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		data = Data.getInstance();
		
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
