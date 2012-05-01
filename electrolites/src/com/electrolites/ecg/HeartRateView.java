package com.electrolites.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class HeartRateView extends AnimationView {
	
	private Paint ratePaint;
	private Paint unitsPaint;
	
	private int bpm = 60;
	
	class HeartRateThread extends AnimationThread {

		public HeartRateThread(SurfaceHolder holder) {
			super(holder);
			
			ratePaint = new Paint();
			unitsPaint = new Paint();
			
			ratePaint.setARGB(255, 255, 0, 0);
			ratePaint.setTextSize(32.f);
			
			unitsPaint.setARGB(255, 255, 255, 255);
			unitsPaint.setTextSize(16.f);
		}

		@Override
		public void onRender(Canvas canvas) {
			if (Math.random() > 0.95)
				bpm = (int) (Math.random() * 80 + 60);
			
			if (bpm > 120) {
				canvas.drawColor(Color.RED);
				ratePaint.setARGB(255, 0, 0, 0);
				canvas.drawText("ALERT!", 10, 35, ratePaint);
			} else {
				canvas.drawColor(Color.BLACK);
				ratePaint.setARGB(255, 255, 0, 0);
				canvas.drawText(Integer.toString(bpm), 10, 35, ratePaint);
				canvas.drawText("bpm", 60, 35, unitsPaint);
			}
			
			canvas.restore();
		}
		
	}

	public HeartRateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		thread = new HeartRateThread(holder);
	}
}
