package com.electrolites.ecg;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.electrolites.data.Data;
import com.electrolites.util.Point;

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
            canvas.drawText("fps: " + fps, 100, 100, textPaint);
            
            data.drawBaseHeight += -3*(int) Math.random() + (int) Math.random()*3;
            
            float w = getWidth();
            float h = getHeight();

            float points[] = buildEcg(-h/10, h/10, -h/10*4, h/10*2, -h/10*1.5f);
            canvas.drawLines(points, 0, count,  linePaint);
            if (count < 48)
                    count += 4;
            else 
                    count = 0;
            
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
		
	}

	private Data data;
	
	public ECGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		data = Data.getInstance();
		
		thread = new ECGThread(holder);
	}
	
}
