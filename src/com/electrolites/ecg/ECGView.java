package com.electrolites.ecg;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Button;

import com.electrolites.data.DPoint;
import com.electrolites.data.DPoint.PointType;
import com.electrolites.data.DPoint.Wave;
import com.electrolites.data.Data;
import com.electrolites.util.ExtendedDPoint;
import com.electrolites.util.Viewport;

public class ECGView extends AnimationView {

	private class ECGThreadStatic extends AnimationThread {
		
		private Intent intent;
		
		private Paint linePaint, rectPaint, ecgPaint;
		
		private int bgColor;

		public ECGThreadStatic(SurfaceHolder holder) {
			super(holder);
			
			//bgColor = Color.rgb(89, 89, 89);
			bgColor = Color.rgb(0, 0, 0);
			
			linePaint = new Paint();
			linePaint.setARGB(200, 100, 255, 100);
			linePaint.setStrokeWidth(2.f);
			linePaint.setTextAlign(Align.RIGHT);
			
			ecgPaint = new Paint();
			
			rectPaint = new Paint();
			rectPaint.setColor(Color.rgb(69, 69, 69));
		}
		
		@Override
		public void onRender(Canvas canvas) {
			
			//synchronized (this) {
			if (data == null || vport == null)
				return;
			
				vport.data = data.getSamplesArray();
				if (data.autoScroll)
					vport.moveToEnd();
			//}
			
			vport.dataStart = 0;
			vport.dataEnd = vport.data.length;
			
			
			boolean loading = data.loading;
			
			canvas.drawColor(data.bgColor);
            //canvas.drawText("fps: " + fps, 100, 100, textPaint);
			
			int left = vport.vpPxX-5, right = vport.vpPxX+vport.vpPxWidth+5, top = vport.vpPxY-1, bottom = vport.vpPxY+vport.vpPxHeight+1;

			if (loading) {
				// Ultimate Cutresy!
				canvas.drawRect(new Rect(0, 0, getWidth(), vport.vpPxY-1), rectPaint);
				canvas.drawRect(new Rect(0, vport.vpPxY+vport.vpPxHeight+1, getWidth(), getHeight()), rectPaint);
				canvas.drawRect(new Rect(0, 0, left, getHeight()), rectPaint);
				canvas.drawRect(new Rect(right, 0, getWidth(), getHeight()), rectPaint);
				
				linePaint.setStrokeWidth(2.f);
				canvas.drawLine(left, top, right, top, linePaint);
				canvas.drawLine(left, top, left, bottom, linePaint);
				canvas.drawLine(left, bottom, right, bottom, linePaint);
				canvas.drawLine(right, top, right, bottom, linePaint);
				
				Align a = linePaint.getTextAlign();
				float s = linePaint.getTextSize();
				linePaint.setTextAlign(Align.CENTER);
				linePaint.setTextSize(48);
				canvas.drawText("Loading...", vport.vpPxX+vport.vpPxWidth/2, vport.vpPxY+vport.vpPxHeight/2, linePaint);
				linePaint.setTextAlign(a);
				linePaint.setTextSize(s);
				
			} else {
				// Render Axis and Data
				linePaint.setARGB(230, 150, 150, 150);
				linePaint.setStrokeWidth(2.f);
				canvas.drawLine(left, vport.vpPxY, left, vport.vpPxY+vport.vpPxHeight, linePaint);
				
				// Render Axis Scales
				// Upper part
				int divisions = (int) Math.floor((vport.baselinePxY - vport.vpPxY) / (1000*vport.vFactor));
				
				//canvas.drawText("0.0", left-2, vport.baselinePxY, linePaint);
				canvas.drawLine(left, vport.baselinePxY, vport.vpPxX+5+vport.vpPxWidth, vport.baselinePxY, linePaint);
				linePaint.setStrokeWidth(1.f);
				for (int i = 0; i <= divisions; i++) {
				//	canvas.drawText("" + (float) i, left-2, vport.baselinePxY-i*1000*vport.vFactor, linePaint);
					canvas.drawLine(left, vport.baselinePxY-i*1000*vport.vFactor, vport.vpPxX+5+vport.vpPxWidth, vport.baselinePxY-i*1000*vport.vFactor, linePaint);
				}
				
				// Lower part
				divisions = (int) Math.floor((vport.vpPxY+vport.vpPxHeight- vport.baselinePxY) / (1000*vport.vFactor));
				
				for (int i = 1; i <= divisions; i++) {
				//	canvas.drawText("" + (float) -i, left-2, vport.baselinePxY+i*1000*vport.vFactor, linePaint);
					canvas.drawLine(left, vport.baselinePxY+i*1000*vport.vFactor, vport.vpPxX+5+vport.vpPxWidth, vport.baselinePxY+i*1000*vport.vFactor, linePaint);
				}
				
				
				// Render samples
				ecgPaint.setColor(Color.GREEN);
				ecgPaint.setAlpha((int) (255*0.9));
				ecgPaint.setStrokeWidth(2.f);
	            float points[] = vport.getViewContents();
	            int toDraw = points.length;
	            canvas.drawLines(points, 0, toDraw,  ecgPaint);
	            
				// Render delineation results
				Map<Float, ExtendedDPoint> specials = vport.getViewDPoints();
				for (Map.Entry<Float, ExtendedDPoint> ent : specials.entrySet()) {
					renderDPoint(canvas, ent.getKey().floatValue(), ent.getValue(), points);
				}
	            
				// Ultimate Cutresy!
				canvas.drawRect(new Rect(0, 0, getWidth(), vport.vpPxY-1), rectPaint);
				canvas.drawRect(new Rect(0, vport.vpPxY+vport.vpPxHeight+1, getWidth(), getHeight()), rectPaint);
				canvas.drawRect(new Rect(0, 0, left, getHeight()), rectPaint);
				canvas.drawRect(new Rect(right, 0, getWidth(), getHeight()), rectPaint);
				
				linePaint.setStrokeWidth(2.f);
				canvas.drawLine(left, top, right, top, linePaint);
				canvas.drawLine(left, top, left, bottom, linePaint);
				canvas.drawLine(left, bottom, right, bottom, linePaint);
				canvas.drawLine(right, top, right, bottom, linePaint);
				
				divisions = (int) Math.floor((vport.baselinePxY - vport.vpPxY) / (1000*vport.vFactor));
				
				canvas.drawText("0.0", left-2, vport.baselinePxY, linePaint);
				for (int i = 0; i <= divisions; i++) {
					canvas.drawText("" + (float) i, left-2, vport.baselinePxY-i*1000*vport.vFactor, linePaint);
				}
				
				// Lower part
				divisions = (int) Math.floor((vport.vpPxY+vport.vpPxHeight- vport.baselinePxY) / (1000*vport.vFactor));
				
				for (int i = 1; i <= divisions; i++) {
					canvas.drawText("" + (float) -i, left-2, vport.baselinePxY+i*1000*vport.vFactor, linePaint);
				}
				
				linePaint.setTextAlign(Align.LEFT);
				canvas.drawText(vport.vaSecX + " - " + (vport.vaSecX+vport.vaSeconds), left, top-10, linePaint);
				linePaint.setTextAlign(Align.RIGHT);
			}
			
            canvas.restore();
			
		}
		
		protected void renderDPoint(Canvas canvas, float x, ExtendedDPoint edp, float[] points) {
			DPoint p = edp.getDpoint();
			
			if (edp.getIndex() < 0 || edp.getIndex() >= data.samples.size())
				return;
			
			if (p.getType() == PointType.start || p.getType() == PointType.end) {
	            ecgPaint.setStrokeWidth(1.f);
				ecgPaint.setARGB(200, 180, 180, 240);
			}
			else if (p.getType() == PointType.peak) {
	            ecgPaint.setStrokeWidth(2.f);
	            if (p.getWave() == Wave.QRS)
					ecgPaint.setARGB(230, 240, 240, 240);
	            else if (p.getWave() == Wave.P)
					ecgPaint.setARGB(230, 240, 110, 110);
				else if (p.getWave() == Wave.T)
					ecgPaint.setARGB(230, 110, 240, 110);
			}
			else
				return;

			if (vport.baselinePxY > 0.3*getHeight())
				canvas.drawLine(x, vport.vpPxY, x, vport.baselinePxY-data.samples.get(edp.getIndex())*vport.vFactor-10*vport.vFactor, ecgPaint);
			else
				canvas.drawLine(x, vport.vpPxY+vport.vpPxHeight, x, vport.baselinePxY-data.samples.get(edp.getIndex())*vport.vFactor+10*vport.vFactor, ecgPaint);
		}
	}

	private class ECGThreadDynamic extends AnimationThread {
		
		private Paint linePaint, rectPaint, ecgPaint;
		
		private int bgColor;

		public ECGThreadDynamic(SurfaceHolder holder) {
			super(holder);
			
			//bgColor = Color.rgb(89, 89, 89);
			bgColor = Color.rgb(0, 0, 0);
			
			linePaint = new Paint();
			linePaint.setARGB(200, 100, 255, 100);
			linePaint.setStrokeWidth(2.f);
			linePaint.setTextAlign(Align.RIGHT);
			
			ecgPaint = new Paint();
			
			rectPaint = new Paint();
			rectPaint.setColor(Color.rgb(69, 69, 69));
		}
		
		@Override
		public void onRender(Canvas canvas) {
			
			if (canvas == null)
				return;
			canvas.drawColor(Color.rgb(255, 10, 10));
			Align a = linePaint.getTextAlign();
			float s = linePaint.getTextSize();
			linePaint.setTextAlign(Align.CENTER);
			linePaint.setTextSize(48);
			canvas.drawText("Loading...", vport.vpPxX+vport.vpPxWidth/2, vport.vpPxY+vport.vpPxHeight/2, linePaint);
			linePaint.setTextAlign(a);
			linePaint.setTextSize(s);
			
            canvas.restore();
			
		}
	}

	
	protected Viewport vport;
	
	private Data data;
	
	public ECGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		data = Data.getInstance();
		
		/*if (thread == null) {
			thread = new ECGThread(holder);
			thread.setDaemon(true);
		}*/
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		int w = (int) (getWidth()*0.95);
		int h = (int) (getHeight()*0.9);
		vport = new Viewport(w, h, 3.0f);
		vport.setOnScreenPosition(30, 30);
		vport.data = data.getSamplesArray();
		vport.dataStart = 0;
		vport.dataEnd = vport.data.length;
	
		try {
			if (!thread.isAlive()) {
				thread.setRunning(true);
				thread.start();
			}
		} catch (Exception e) {
			/*thread = new ECGThread(getHolder());
			thread.setRunning(true);
			thread.start();*/
		}
	}

	@Override
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
	
	@Override
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
			
			if (data.mode == Data.MODE_STATIC) {
				vport.move(-1*(event.getX() - holdStartX)/vport.vpPxWidth*vport.vaSeconds*0.5f);
				holdStartX = event.getX();
			}

			data.setDrawBaseHeight(data.getDrawBaseHeight()+(event.getY() - holdStartY)/vport.vpPxHeight);
			holdStartY = event.getY();
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!holding) return true;
			
			holding = false;
			holdEndX = event.getX();
			holdEndY = event.getY();
		}
		return true;
	}
	
	public void reset() {
		if (thread != null) {
			thread.setRunning(false);
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
					System.err.println("SE MORTO");
				} catch (InterruptedException e) {
				}
			}
		}

		if (data.mode == Data.MODE_STATIC)
			thread = new ECGThreadStatic(getHolder());
		else if (data.mode == Data.MODE_DYNAMIC)
			thread = new ECGThreadDynamic(getHolder());
		thread.setRunning(true);
		thread.start();
	}
};
