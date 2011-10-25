package com.electrolites.ecg;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.electrolites.data.Data;
import com.electrolites.util.Point;
import com.electrolites.util.Viewport;

public class ECGView extends AnimationView {

	private class ECGThread extends AnimationThread {
		
		private Paint textPaint, linePaint, linePaint2;

		public ECGThread(SurfaceHolder holder) {
			super(holder);
			
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

		@Override
		public void onRender(Canvas canvas) {
			canvas.drawColor(Color.BLACK);
            //canvas.drawText("fps: " + fps, 100, 100, textPaint);

			linePaint.setColor(Color.WHITE);
			linePaint.setStrokeWidth(1.f);
			canvas.drawLine(vport.vpPxX-5, vport.vpPxY, vport.vpPxX-5, vport.vpPxY+vport.vpPxHeight, linePaint);
			canvas.drawLine(vport.vpPxX-5, vport.baselinePxY, vport.vpPxX-5+vport.vpPxWidth, vport.baselinePxY, linePaint);
			
			linePaint.setColor(Color.GREEN);
			linePaint.setAlpha((int) (255*0.9));
			linePaint.setStrokeWidth(2.f);
            float points[] = vport.getViewContents();
            int toDraw = points.length;
            canvas.drawLines(points, 0, toDraw,  linePaint);
            
            canvas.restore();
			
		}
	}

	protected Viewport vport;
	
	private Data data;
	
	public ECGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		data = Data.getInstance();
		
		thread = new ECGThread(holder);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		int w = (int) (getWidth()*0.9);
		int h = (int) (getHeight()*0.9);
		vport = new Viewport(w, h, 0.5f);
		vport.setOnScreenPosition(getWidth()-10-w, getHeight()-10-h);
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
	
	boolean holding;
	float holdStartX;
	float holdStartY;
	float holdEndX;
	float holdEndY;
	
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (holding) {
				holding = false; return true;
			}
			
			holding = true;
			holdStartX = event.getX();
			holdStartY = event.getY();
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!holding) return true;
			
			vport.move(-1*(event.getX() - holdStartX)/480);
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!holding) return true;
			
			holding = false;
			holdEndX = event.getX();
			holdEndY = event.getY();
		}
		//vport.move(0.5f);
		return true;
	}
}
