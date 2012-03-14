package org.microlites.view;

import org.microlites.bluetooth.ConnectedThread;
import org.microlites.bluetooth.DataSourceThread;
import org.microlites.data.DataHolder;
import org.microlites.util.DynamicViewport;

import android.bluetooth.BluetoothSocket;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.SurfaceHolder;

public class FullDynamicThread extends AnimationThread 
										implements DataHolder {
	
	/* TODO: Decide if consumes input from ConnectedThread
	 * 		or it IS ConnectedThread 		 
	 */
	/* Data source */
	protected DataSourceThread dataSourceThread;	// Thread providing data
	
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
	
	/* Render items */
	protected AnimationView view;					// View that contains thread
	protected DynamicViewport dvport;				// Dynamic Viewport Info
	protected Paint textPaint;						// Text paint instance
	protected Paint ecgPaint;						// ECG Render paint instance
	protected Paint rectPaint;						// Rectangle paint instance
	
	protected int bgColor;							// Background color
	
	protected float samplePoints[];					// Samples (x1, y1, x2, y2)
	
	
	public FullDynamicThread(SurfaceHolder holder, AnimationView aview) {
		super(holder);
		
		view = aview;
		dvport = new DynamicViewport((int) (view.getWidth()*0.95), 
									 (int) (view.getHeight()*0.9), 3);
		dvport.setOnScreenPosition((view.getWidth() - dvport.vpPxWidth)/2,
								   (view.getHeight() - dvport.vpPxHeight)/2);
		
		/* Prepare renderers */
		// Black background
		bgColor = Color.rgb(0, 0, 0);
		
		// Green Text
		textPaint = new Paint();
		textPaint.setARGB(200, 100, 255, 100);
		textPaint.setStrokeWidth(2.f);
		textPaint.setTextAlign(Align.RIGHT);
		
		// ECG Paint will be configured while on use
		ecgPaint = new Paint();
		
		// Theme-gray Rectangles 
		rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(69, 69, 69));
		
		// Prepare data
		initData(); 
	}
	
	@Override
	protected void onUpdate() {
		super.onUpdate();
	}
	
	public void onRender(Canvas canvas) {
		if (canvas == null || dvport == null)
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
			
			// y axis
			textPaint.setARGB(230, 150, 150, 150);
			textPaint.setStrokeWidth(2.f);
			canvas.drawLine(left, top, left, bottom, textPaint);
			
			// Upper scale part
			int divisions = (int) Math.floor((dvport.baselinePxY - dvport.vpPxY) / (1000*dvport.vFactor));
			
			canvas.drawLine(left, dvport.baselinePxY, right+5, dvport.baselinePxY, textPaint);
			textPaint.setStrokeWidth(1.f);
			for (int i = 0; i <= divisions; i++) {
				canvas.drawLine(left, dvport.baselinePxY-i*1000*dvport.vFactor, right+5, dvport.baselinePxY-i*1000*dvport.vFactor, textPaint);
			}
			
			// Lower part
			divisions = (int) Math.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / (1000*dvport.vFactor));
			
			for (int i = 1; i <= divisions; i++) {
				canvas.drawLine(left, dvport.baselinePxY+i*1000*dvport.vFactor, right+5, dvport.baselinePxY+i*1000*dvport.vFactor, textPaint);
			}
		
		// Render samples
			ecgPaint.setColor(Color.rgb(59, 250, 59));
			ecgPaint.setAlpha((int) (255*0.9));
			ecgPaint.setStrokeWidth(2.f);
            
			float dpoints = dvport.vpPxWidth / ((float) s_size);
			if (dpoints <= 0) {
				System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			} else {
				int ammount = s_end + ((s_end < s_start) ? s_size : 0) - s_start;
				int ii = -1;
				for (int i = 0; i < ammount; i++) {
					ii = (s_start + i) % s_size;
					if (i == 0) {
						samplePoints[i] = dvport.vpPxX;
						samplePoints[i+1] = (dvport.baselinePxY - s_amplitude[ii]*dvport.vFactor);
					}
					else if (i == 1) {
						samplePoints[4*i-2] = (dvport.vpPxX + dpoints);
						samplePoints[4*i-1] = (dvport.baselinePxY - s_amplitude[ii]*dvport.vFactor);
					}
					else {
						// Duplicate last point
						samplePoints[4*i] = samplePoints[4*i-2];
						samplePoints[4*i+1] = samplePoints[4*i-1];
						samplePoints[4*i+2] = (dvport.vpPxX + i*dpoints);
						samplePoints[4*i+3] = (dvport.baselinePxY - s_amplitude[ii]*dvport.vFactor);
					}
				}
				
				canvas.drawLines(samplePoints, ecgPaint);
			}
		
		// Render dpoints
			// TODO: Render DPoints
			// DPoints arrays variables
			/*int ammount = dp_end + ((dp_end < dp_start) ? dp_size : 0) - dp_start;
			int ii = -1;
			
			// Samples arrays variables
			int sammount = s_end + ((s_end < s_start) ? s_size : 0) - s_start;
			int oldj = -1;
			int jj = -1;
			float sampleX = -1;

			for (int i = 0; i < ammount; i++) {
				System.out.println(oldj);
				ii = (dp_start + i) % dp_size;
				for (int j = 0; j < sammount; j++) {
					jj = (s_start + j) % s_size;
					if (s_index[jj] == dp_sample[ii]) {
						// Calculate sample x = vPxX + relativeIndex*dpoints
						sampleX = dvport.vpPxX + dpoints*(jj + (jj < s_start ? s_size : 0) - s_start);
						oldj = jj;
						break;
					}
				}
				canvas.drawLine(sampleX, dvport.vpPxY, sampleX, dvport.vpPxY+dvport.vpPxHeight, ecgPaint);
			}*/
		
		// Render frame
			canvas.drawRect(0, 0, view.getWidth(), dvport.vpPxY-1, rectPaint);
			canvas.drawRect(0, dvport.vpPxY+dvport.vpPxHeight+1, view.getWidth(), view.getHeight(), rectPaint);
			canvas.drawRect(0, 0, left, view.getHeight(), rectPaint);
			canvas.drawRect(right, 0,view. getWidth(), view.getHeight(), rectPaint);
			
			textPaint.setStrokeWidth(2.f);
			canvas.drawLine(left, top, right, top, textPaint);
			canvas.drawLine(left, top, left, bottom, textPaint);
			canvas.drawLine(left, bottom, right, bottom, textPaint);
			canvas.drawLine(right, top, right, bottom, textPaint);
		
		// Render text labels 
			divisions = (int) Math.floor((dvport.baselinePxY - dvport.vpPxY) / (1000*dvport.vFactor));
			
			canvas.drawText("0.0", left-2, dvport.baselinePxY, textPaint);
			for (int i = 0; i <= divisions; i++) {
				canvas.drawText("" + (float) i, left-2, dvport.baselinePxY-i*1000*dvport.vFactor, textPaint);
			}
			
			// Lower part
			divisions = (int) Math.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / (1000*dvport.vFactor));
			
			for (int i = 1; i <= divisions; i++) {
				canvas.drawText("" + (float) -i, left-2, dvport.baselinePxY+i*1000*dvport.vFactor, textPaint);
			}
		
		// Debug thingies
			textPaint.setTextAlign(Align.LEFT);
			canvas.drawText("" + dvport.areaOffset + " ~ " + dvport.lastOffset, left, top-10, textPaint);
			textPaint.setTextAlign(Align.RIGHT);
		
			canvas.drawText("FPS: " + fps, (left+right)/2, (top+bottom)/2, textPaint);
			
		// Aaaaand done!
			canvas.restore();
	}
	
	public void initData() {
		// Set queue sizes (TODO: Paramtrize size)
		s_size = dp_size = 768;
		
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
	
	public void setBluetoothSocket(BluetoothSocket socket) {
        dataSourceThread = new ConnectedThread(this, socket);
        dataSourceThread.start();
        System.out.println("FullDynamicThread: DataSourceThread created!");
    }
	
	public void write(byte[] buffer) {
        dataSourceThread.write(buffer);
    }
	
	public void cancel() {
		if (dataSourceThread != null)
			dataSourceThread.cancel();
    }
	
	@Override
	public void finish() {
		if (dataSourceThread != null) {
			boolean retry = true;
			dataSourceThread.stopSending();
			while (retry) {
				try {
					dataSourceThread.join();
					retry = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Finished DataSourceThread execution");
		}
		
		setRunning(false);
	}
}