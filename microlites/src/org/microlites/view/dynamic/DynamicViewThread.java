package org.microlites.view.dynamic;

import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.util.DynamicViewport;
import org.microlites.view.AnimationThread;
import org.microlites.view.AnimationView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.SurfaceHolder;

public class DynamicViewThread extends AnimationThread 
										implements DataHolder {
	
	/* Data items */
	// Samples
	protected int s_start, s_end;					// Circular queue pointers
	public int s_size;								// Circular queue max size
	public int s_index[];							// Sample index
	public short s_amplitude[];						// Sample amplitude
	
	// DPoints
	protected int dp_start, dp_end;					// Circular queue pointers
	public int dp_size;								// Circular queue max size
	public short dp_type[];							// DPoint types
	public short dp_wave[];							// DPoint waves
	public int dp_sample[];							// DPoint samples
	
	// HBR
	public float hbr_current;						// Current HBR in BPM
	
	/* Render items */
	protected AnimationView view;					// View that contains thread
	protected DynamicViewport dvport;				// Dynamic Viewport Info
	protected Paint textPaint;						// Text paint instance
	protected Paint ecgPaint;						// ECG Render paint instance
	protected Paint rectPaint;						// Rectangle paint instance
	
	protected int samplesColor;						// Samples color
	protected int bgColor;							// Background color
	protected int dpGray;							// DPoint render gray
	
	protected int qrsColor;
	protected int pColor;
	protected int tColor;
	protected int offsetColor;
	
	protected float samplePoints[];					// Samples (x1, y1, x2, y2)
	protected float divisionsPoints[];				// Scale divisions (x1, y1, x2, y2)
	protected static final int DIVISIONS_MAX = 25;
	
	public DynamicViewThread(SurfaceHolder holder, AnimationView aview) {
		super(holder);
		
		view = aview;
		dvport = new DynamicViewport((int) (view.getWidth()*0.95), 
									 (int) (view.getHeight()*0.9), 3);
		dvport.setOnScreenPosition((view.getWidth() - dvport.vpPxWidth)/2,
								   (view.getHeight() - dvport.vpPxHeight)/2);
		
		/* Prepare renderers */
		// Black background
		// Samples color
		samplesColor = Color.rgb(250, 59, 59);
		// Black background
		bgColor = Color.rgb(30, 30, 31);//239, 239, 239);
		//dpGray = Color.rgb(255-180, 255-180, 255-140);
		dpGray = Color.rgb(80, 90, 90);
		// DPoints colors
		qrsColor = Color.MAGENTA;
		pColor = Color.CYAN;
		tColor = Color.YELLOW;
		offsetColor = Color.RED;
		
		// Green Text
		textPaint = new Paint();
		textPaint.setColor(Color.rgb(100, 255, 100));
		textPaint.setStrokeWidth(2.f);
		textPaint.setTextAlign(Align.RIGHT);
		textPaint.setAntiAlias(true);
		
		// ECG Paint will be configured while on use
		ecgPaint = new Paint();
		//ecgPaint.setAntiAlias(true);
		
		// Theme-gray Rectangles 
		rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(69, 69, 69));
		
		// Prepare data
		initData(); 
	}
	
	public void onRender(Canvas canvas) {
		if (canvas == null || dvport == null || Data.getInstance().pause)
			return;
		
			dvport.updateParameters();

		/*** Calculate border positions ***/
			int left = dvport.vpPxX;
			int right = dvport.vpPxX + dvport.vpPxWidth;
			int top = dvport.vpPxY;
			int bottom = dvport.vpPxY + dvport.vpPxHeight;
		
		/*** Clear canvas ***/
			canvas.drawColor(bgColor);
		
		/*** Render axis and scales ***/
			
			textPaint.setColor(dpGray);
			textPaint.setStrokeWidth(2.f);
			// Render y axis
			canvas.drawLine(left, top, left, bottom, textPaint);
			// Render x axis
			canvas.drawLine(left, dvport.baselinePxY, right+5, dvport.baselinePxY, textPaint);
			
			// Upper scale part
			float delta = (dvport.max > 40000 ? 2*dvport.max/40000f : 1)*1000*dvport.vFactor;
			int divisions = (int) android.util.FloatMath.floor((dvport.baselinePxY - dvport.vpPxY) / delta);
			
			textPaint.setStrokeWidth(1.f);
			int counter = 0;
			for (int i = 0; i <= divisions; i++) {
				divisionsPoints[counter++] = left;
				divisionsPoints[counter++] = dvport.baselinePxY-i*delta;
				divisionsPoints[counter++] = right+5;
				divisionsPoints[counter++] = dvport.baselinePxY-i*delta;
			}
			
			// Lower part
			divisions = (int) android.util.FloatMath.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / delta);
			
			for (int i = 1; i <= divisions; i++) {
				divisionsPoints[counter++] = left;
				divisionsPoints[counter++] = dvport.baselinePxY+i*delta;
				divisionsPoints[counter++] = right+5;
				divisionsPoints[counter++] = dvport.baselinePxY+i*delta;				
			}
			
			canvas.drawLines(divisionsPoints, 0, counter, textPaint);
		
		// Render samples
			ecgPaint.setColor(samplesColor);
			ecgPaint.setStrokeWidth(3.f);
            
			// int actualSamplePoints = samplePoints.length;
			
			float dpoints = dvport.vpPxWidth / ((float) s_size);
			if (dpoints <= 0) {
				System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			} else {
				int ammount = s_end + ((s_end < s_start) ? s_size : 0) - s_start;
				int ii = -1;

				boolean zeroProcessing = false;
				// int nz = 0;
				int i = 0;
				int ai = 0; // Actual samplePoints array index 
				short iipa; // Sample amplitude to add in current iteration
				while (i < ammount-1) {
					
					// System.err.println(i + ", " + ai);
					
					ii = (s_start + i) % s_size;
					iipa = s_amplitude[(ii+1)%s_size];
					if (i == 0) {
						samplePoints[ai] = dvport.vpPxX;
						samplePoints[ai+1] = (dvport.baselinePxY - s_amplitude[ii]*dvport.vFactor);
						samplePoints[ai+2] = dvport.vpPxX + dpoints;
						samplePoints[ai+3] = (dvport.baselinePxY - iipa*dvport.vFactor);
						// At this point zeroProcessing can't be true
						// so we just activate the flag and add the point as usual
						if (iipa == 0) {
							// nz = 0;
							// System.out.print("Start ZeroProcessing [");
							zeroProcessing = true;
							// Activate zero processing
							// and reserve the slot for the other 0point
							// ai++;
						}
						// Point stored
						ai++;
					} 
					else {
						if (zeroProcessing) {
							if (iipa == 0) {
								// nz++;
								// Another 0, skip it
							} else {
								// System.out.println(nz + "]/nStop ZeroProcessing");
								// Non zero sample, zeroProcessing ended
								zeroProcessing = false;
								
								ai++;
								
								// Add pair 0point (has a reserved slot)
								samplePoints[4*(ai-1)] = samplePoints[4*(ai-1)-2];
								samplePoints[4*(ai-1)+1] = (dvport.baselinePxY);
								samplePoints[4*(ai-1)+2] = (dvport.vpPxX + i*dpoints);
								samplePoints[4*(ai-1)+3] = (dvport.baselinePxY);
								
								samplePoints[4*ai] = samplePoints[4*ai-2];
								samplePoints[4*ai+1] = samplePoints[4*ai-1];
								samplePoints[4*ai+2] = (dvport.vpPxX + (i+1)*dpoints);
								samplePoints[4*ai+3] = (dvport.baselinePxY - iipa*dvport.vFactor);
								
								// Point stored
								// ai++;
							}
						} else {
							// Not zero processing
							// Store point
							samplePoints[4*ai] = samplePoints[4*ai-2];
							samplePoints[4*ai+1] = samplePoints[4*ai-1];
							samplePoints[4*ai+2] = (dvport.vpPxX + (i+1)*dpoints);
							samplePoints[4*ai+3] = (dvport.baselinePxY - iipa*dvport.vFactor);
							
							if (iipa == 0) {
								// nz = 0;
								// System.out.print("Start ZeroProcessing [");
								// A zero, start zero processing
								zeroProcessing = true;
								// Reserve slot for the other 0point
								// ai++;
							}
							
							ai++;
						}
					}
					
					i++;
				}
				
				if (zeroProcessing) {
					// An endopoint 0 is missing
					// Add pair 0point (has a reserved slot)
					// System.out.println("]\nUnfinished ZeroProcessing");
					samplePoints[4*(ai)] = samplePoints[4*(ai)-2];
					samplePoints[4*(ai)+1] = samplePoints[4*ai-1];
					samplePoints[4*(ai)+2] = (dvport.vpPxX + i*dpoints);
					samplePoints[4*(ai)+3] = (dvport.baselinePxY);
				}
				
				canvas.drawLines(samplePoints, 0, 4*ai+4, ecgPaint);
			}
		
		// Render dpoints
			// DPoints arrays variables
			int ammount = dp_end + ((dp_end < dp_start) ? dp_size : 0) - dp_start;
			int ii = -1;
			
			float sampleX = -1;
			int indexDistance;
			int sampleIndex;

			for (int i = 0; i < ammount; i++) {
				ii = (dp_start + i) % dp_size;
				if (dp_sample[ii] < 0 || dp_sample[ii] < s_index[s_start]) {
					dp_sample[ii] = -1;
					continue;
				}
				
				indexDistance = dp_sample[ii] - s_index[s_start];
				sampleIndex = (s_start + indexDistance) % s_size;
				sampleX = dvport.vpPxX + dpoints*(sampleIndex + (sampleIndex < s_start ? s_size : 0) - s_start);
				
				if (dp_type[ii] == DP_TYPE_START || dp_type[ii] == DP_TYPE_END) {
					ecgPaint.setStrokeWidth(1);
					if (dp_wave[ii] == WAVE_OFFSET)
						ecgPaint.setColor(offsetColor);
					else 
						ecgPaint.setColor(dpGray);
				} else {
					if (dp_wave[ii] == WAVE_QRS)
						ecgPaint.setColor(qrsColor);
					else if (dp_wave[ii] == WAVE_P)
						ecgPaint.setColor(pColor);
					else if (dp_wave[ii] == WAVE_T)
						ecgPaint.setColor(tColor);
					ecgPaint.setStrokeWidth(2);
				}
				
				if (dp_wave[ii] == WAVE_OFFSET)
					canvas.drawLine(sampleX, dvport.vpPxY, sampleX, dvport.vpPxY+dvport.vpPxHeight, ecgPaint);
				else
					canvas.drawLine(sampleX, dvport.vpPxY, sampleX, dvport.baselinePxY - s_amplitude[sampleIndex]*dvport.vFactor, ecgPaint);

			}
		
		// Render frame
			rectPaint.setColor(bgColor);
			canvas.drawRect(0, 0, view.getWidth(), dvport.vpPxY-1, rectPaint);
			canvas.drawRect(0, dvport.vpPxY+dvport.vpPxHeight+1, view.getWidth(), view.getHeight(), rectPaint);
			canvas.drawRect(0, 0, left, view.getHeight(), rectPaint);
			canvas.drawRect(right, 0,view. getWidth(), view.getHeight(), rectPaint);
			
			/*textPaint.setStrokeWidth(2.f);
			canvas.drawLine(left, top, right, top, textPaint);
			canvas.drawLine(left, top, left, bottom, textPaint);
			canvas.drawLine(left, bottom, right, bottom, textPaint);
			canvas.drawLine(right, top, right, bottom, textPaint);*/
		
		// Render text labels 
			divisions = (int) android.util.FloatMath.floor((dvport.baselinePxY - dvport.vpPxY) / delta);
			
			canvas.drawText("0.0", left-2, dvport.baselinePxY, textPaint);
			for (int i = 0; i <= divisions; i++) {
				canvas.drawText("" + (float) i, left-2, dvport.baselinePxY-i*delta, textPaint);
			}
			
			// Lower part
			divisions = (int) android.util.FloatMath.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / delta);
			
			for (int i = 1; i <= divisions; i++) {
				canvas.drawText("" + (float) -i, left-2, dvport.baselinePxY+i*delta, textPaint);
			}
			
		// Render HBR Label
			textPaint.setTextAlign(Align.RIGHT);
			textPaint.setColor(Color.RED);
			canvas.drawText("HBR: " + hbr_current, right, bottom + 16, textPaint);
			textPaint.setTextAlign(Align.CENTER);
			canvas.drawText("No se ha detectado arritmia", left+right/2, bottom+16, textPaint);
			textPaint.setColor(dpGray);
		
		// Debug thingies
			textPaint.setTextAlign(Align.RIGHT);
			canvas.drawText("FPS: " + fps, (left+right)/2, (top+bottom)/2, textPaint);
			
		// Aaaaand done!
			canvas.restore();
	}
	
	public void initData() {
		// Set queue sizes (TODO: Paramtrize size)
		float rsize = Data.getInstance().viewWidth*250; // TODO: Parametrize sps
		s_size = (int) rsize;
		dp_size = (int) (rsize / 6);
		
		// Reset indexes
		s_start = s_end = 0;
		dp_start = dp_end = 0;
		
		// Initialize queues
		s_index = new int[s_size];
		s_amplitude = new short[s_size];
		dp_sample = new int[dp_size];
		dp_type = new short[dp_size];
		dp_wave = new short[dp_size];
		
		// Initialize point array
		samplePoints = new float[s_size*4];
		// Initialize divisions array
		divisionsPoints = new float[DIVISIONS_MAX*2*4];
	}
	
	public void addSample(int index, short sample) {
		// Store (index, sample)
		s_index[s_end] = index;
		s_amplitude[s_end] = sample;
		// Update pointers
		s_end = (s_end + 1) % s_size;
		// If end has surpassed start, discard one
		if (s_end == s_start)
			s_start = (s_start + 1) % s_size;
	}
	
	public void addDPoint(int sample, short type, short wave) {
		// Store (index, type, wave)
		dp_sample[dp_end] = sample;
		dp_type[dp_end] = type;
		dp_wave[dp_end] = wave;
		// Update pointers
		dp_end = (dp_end + 1) % dp_size;
		// If end has surpassed start, discard one
		if (dp_end == dp_start)
			dp_start = (dp_start + 1) % dp_size;
	}
	
	public void handleOffset(int offset) {
		dp_start = dp_end = 0;
		s_start = s_end = 0;
	}
	
	public void handleHBR(float hbr) {
		hbr_current = hbr;
	}

	@Override
	public void finish() {
		setRunning(false);
	}
	
	public void onSurfaceChange(int width, int height) {
		dvport = new DynamicViewport((int) (width*0.95), 
									 (int) (height*0.9), 3);
		dvport.setOnScreenPosition((width - dvport.vpPxWidth)/2,
								   (height - dvport.vpPxHeight)/2);
	}
	
	@Override
	public void saveYourData(Bundle out) {
		/*
		 * 	// Samples
			protected int s_start, s_end;					// Circular queue pointers
			public int s_size;								// Circular queue max size
			public int s_index[];							// Sample index
			public short s_amplitude[];						// Sample amplitude
			
			// DPoints
			protected int dp_start, dp_end;					// Circular queue pointers
			public int dp_size;								// Circular queue max size
			public short dp_type[];							// DPoint types
			public short dp_wave[];							// DPoint waves
			public int dp_sample[];							// DPoint samples
		 */
		out.putIntArray("s_index", s_index);
		out.putShortArray("s_amplitude", s_amplitude);
		out.putInt("s_start", s_start);
		out.putInt("s_end", s_end);
		out.putInt("s_size", s_size);
	}
	
	@Override
	public void restoreYourData(Bundle in) {
		s_index = in.getIntArray("s_index"); 
		s_amplitude = in.getShortArray("s_amplitude");
		s_start = in.getInt("s_start");
		s_end = in.getInt("s_end");
		s_size = in.getInt("s_size");
	}
	
	@Override
	public void handleScroll(float distX, float distY, float x, float y) {
		Data.getInstance().drawBaseHeight -= distY*0.002;
	}
}