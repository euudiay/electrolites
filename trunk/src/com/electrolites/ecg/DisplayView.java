package com.electrolites.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class DisplayView extends AnimationView {
	
	private Paint displayPaint;
	
	class DisplayThread extends AnimationThread {

		public DisplayThread(SurfaceHolder holder) {
			super(holder);
			
			displayPaint = new Paint();			
			displayPaint.setARGB(255, 0, 0, 255);
			displayPaint.setTextSize(36.f);
		}

		@Override
		public void onRender(Canvas canvas) {
			
			canvas.drawColor(Color.BLACK);
			displayPaint.setARGB(255, 0, 255, 0);
			canvas.drawText("NOT ARRITMIA DETECTED", 250, 30, displayPaint);		
			canvas.restore();
		}
		
	}

	public DisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		thread = new DisplayThread(holder);
	}
}