package org.microlites.view.still;

import org.microlites.data.Data;
import org.microlites.data.StaticDataHolder;
import org.microlites.data.filereader.FileDataSourceThread;
import org.microlites.util.DynamicViewport;
import org.microlites.view.AnimationThread;
import org.microlites.view.AnimationView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.SurfaceHolder;

public class StaticViewThread extends AnimationThread 
							  implements StaticDataHolder {
	/* Scroll Handling Items */
	protected FileDataSourceThread dataSource;		// Data Source Thread
	public int s_start, s_end;						// Samples array pointers
	public int s_size;								// Samples array size
	public int dp_start, dp_end;					// TODO: Document these
	public int dp_size;								// TODO: Document these
	
	/* Render items */
	protected AnimationView view;					// View that contains thread
	protected DynamicViewport dvport;				// Dynamic Viewport Info
	protected Paint textPaint;						// Text paint instance
	protected Paint ecgPaint;						// ECG Render paint instance
	protected Paint rectPaint;						// Rectangle paint instance
	
	protected int bgColor;							// Background color
	protected int dpGray;							// DPoint render gray
	
	protected float samplePoints[];					// Samples (x1, y1, x2, y2)
	
	public StaticViewThread(SurfaceHolder holder, AnimationView aview) {
		super(holder);
		
		view = aview;
		dvport = new DynamicViewport((int) (view.getWidth()*0.95), 
									 (int) (view.getHeight()*0.9), 3);
		dvport.setOnScreenPosition((view.getWidth() - dvport.vpPxWidth)/2,
								   (view.getHeight() - dvport.vpPxHeight)/2);
		
		/* Prepare renderers */
		// Black background
		bgColor = Color.rgb(0, 0, 0);
		dpGray = Color.rgb(180, 180, 140);
		
		// Green Text
		textPaint = new Paint();
		textPaint.setARGB(200, 100, 255, 100);
		textPaint.setStrokeWidth(2.f);
		textPaint.setTextAlign(Align.RIGHT);
		textPaint.setAntiAlias(true);
		
		// ECG Paint will be configured while on use
		ecgPaint = new Paint();
		ecgPaint.setAntiAlias(true);
		
		// Theme-gray Rectangles 
		rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(69, 69, 69));
		
		// Prepare data
		initData(); 
	}
	
	public void setDataSource(FileDataSourceThread t) {
		this.dataSource = t;
		t.setViewSamplesSize(this.s_size);
	}
	
	@Override
	public void onSurfaceChange(int width, int height) {
		dvport = new DynamicViewport((int) (width*0.95), 
									 (int) (height*0.9), 3);
		dvport.setOnScreenPosition((width - dvport.vpPxWidth)/2,
								   (height - dvport.vpPxHeight)/2);
	}
	
	@Override
	public void handleScroll(float distX, float distY) {
		Data.getInstance().drawBaseHeight -= distY*0.002;
		dataSource.handleScroll(distX);
	}
	
	@Override
	public void finish() {
		setRunning(false);
	}
	
	@Override
	public void onRender(Canvas canvas) {
		if (canvas == null || dvport == null || Data.getInstance().pause)
			return;
		
		/*** Update viewport dimensions ***/
			dvport.updateParameters();
		
		/*** Calculate border positions ***/
			int left = dvport.vpPxX;
			int right = dvport.vpPxX + dvport.vpPxWidth;
			int top = dvport.vpPxY;
			int bottom = dvport.vpPxY + dvport.vpPxHeight;
	
		/*** Clear canvas ***/
			canvas.drawColor(bgColor);
		
		/*** If still loading, show just a message */
		if (dataSource == null || dataSource.loading) {
			textPaint.setColor(Color.GREEN);
			textPaint.setStrokeWidth(2.f);
			textPaint.setTextAlign(Align.CENTER);
			canvas.drawText("Cargando...", (left+right)/2, (top+bottom)/2, textPaint);
			return;
		}
			
		/*** Fetch data from DataSource ***/
			int[] s_index = dataSource.s_index;
			short[] s_amplitude = dataSource.s_amplitude;
			s_start = dataSource.s_viewstart;
			s_end = dataSource.s_viewend;
			
			int[] dp_sample = dataSource.dp_sample;
			short[] dp_type = dataSource.dp_type;
			short[] dp_wave = dataSource.dp_wave;
			dp_start = dataSource.dp_viewstart;
			dp_end = dataSource.dp_viewend;
			
		/*** Render axis and scales ***/
			
			// y axis
			textPaint.setColor(dpGray);
			textPaint.setStrokeWidth(2.f);
			canvas.drawLine(left, top, left, bottom, textPaint);
			
			// Upper scale part
			int divisions = (int) android.util.FloatMath.floor((dvport.baselinePxY - dvport.vpPxY) / (1000*dvport.vFactor));
			
			canvas.drawLine(left, dvport.baselinePxY, right+5, dvport.baselinePxY, textPaint);
			textPaint.setStrokeWidth(1.f);
			for (int i = 0; i <= divisions; i++) {
				canvas.drawLine(left, dvport.baselinePxY-i*1000*dvport.vFactor, right+5, dvport.baselinePxY-i*1000*dvport.vFactor, textPaint);
			}
			
			// Lower part
			divisions = (int) android.util.FloatMath.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / (1000*dvport.vFactor));
			
			for (int i = 1; i <= divisions; i++) {
				canvas.drawLine(left, dvport.baselinePxY+i*1000*dvport.vFactor, right+5, dvport.baselinePxY+i*1000*dvport.vFactor, textPaint);
			}
		
		// Render samples
			ecgPaint.setColor(Color.rgb(59, 250, 59));
			ecgPaint.setAlpha((int) (255*0.9));
			ecgPaint.setStrokeWidth(2.5f);
            
			float dpoints = dvport.vpPxWidth / ((float) s_size);
			if (dpoints <= 0) {
				System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			} else {
				int ammount = s_end - s_start;
				int ii = -1;
				for (int i = 0; i < ammount; i++) {
					ii = (s_start + i);
					if (i == 0) {
						samplePoints[i] = dvport.vpPxX;
						samplePoints[i+1] = (dvport.baselinePxY - s_amplitude[ii]*dvport.vFactor);
						samplePoints[i+2] = dvport.vpPxX + dpoints;
						samplePoints[i+3] = (dvport.baselinePxY - s_amplitude[ii+1]*dvport.vFactor);
					} 
					else {
						samplePoints[4*i] = samplePoints[4*i-2];
						samplePoints[4*i+1] = samplePoints[4*i-1];
						samplePoints[4*i+2] = (dvport.vpPxX + (i+1)*dpoints);
						samplePoints[4*i+3] = (dvport.baselinePxY - s_amplitude[ii+1]*dvport.vFactor);
					}
				}
				
				canvas.drawLines(samplePoints, ecgPaint);
			}
		
		// Render dpoints
			// DPoints arrays variables
			int ammount = dp_end - dp_start;
			int ii = -1;
			
			float sampleX = -1;
			int indexDistance;
			int sampleIndex;

			for (int i = 0; i < ammount; i++) {
				ii = (dp_start + i);
				if (dp_sample[ii] < 0 || dp_sample[ii] < s_index[s_start])
					continue; 
				else if (dp_sample[ii] > s_index[s_end]) {
					break;
				}
				
				indexDistance = dp_sample[ii] - s_index[s_start];
				sampleIndex = (s_start + indexDistance) % s_size;
				sampleX = dvport.vpPxX + dpoints*(sampleIndex - s_start);
				
				if (dp_type[ii] == DP_TYPE_START || dp_type[ii] == DP_TYPE_END) {
					if (dp_wave[ii] == WAVE_OFFSET)
						ecgPaint.setColor(Color.RED);
					else 
						ecgPaint.setColor(dpGray);
				} else {
					if (dp_wave[ii] == WAVE_QRS)
						ecgPaint.setColor(Color.MAGENTA);
					else if (dp_wave[ii] == WAVE_P)
						ecgPaint.setColor(Color.CYAN);
					else if (dp_wave[ii] == WAVE_T)
						ecgPaint.setColor(Color.YELLOW);
				}
				ecgPaint.setStrokeWidth(1);
				
				
				canvas.drawLine(sampleX, dvport.vpPxY, sampleX, dvport.vpPxY+dvport.vpPxHeight, ecgPaint);
			}
		
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
			divisions = (int) android.util.FloatMath.floor((dvport.baselinePxY - dvport.vpPxY) / (1000*dvport.vFactor));
			
			canvas.drawText("0.0", left-2, dvport.baselinePxY, textPaint);
			for (int i = 0; i <= divisions; i++) {
				canvas.drawText("" + (float) i, left-2, dvport.baselinePxY-i*1000*dvport.vFactor, textPaint);
			}
			
			// Lower part
			divisions = (int) android.util.FloatMath.floor((dvport.vpPxY+dvport.vpPxHeight- dvport.baselinePxY) / (1000*dvport.vFactor));
			
			for (int i = 1; i <= divisions; i++) {
				canvas.drawText("" + (float) -i, left-2, dvport.baselinePxY+i*1000*dvport.vFactor, textPaint);
			}
			
		// Render HBR Label
			// textPaint.setTextAlign(Align.RIGHT);
			// textPaint.setColor(Color.RED);
			// canvas.drawText("HBR: " + hbr_current, right, bottom + 16, textPaint);
			// textPaint.setTextAlign(Align.CENTER);
			// canvas.drawText("No se ha detectado arritmia", left+right/2, bottom+16, textPaint);
			textPaint.setTextAlign(Align.LEFT);
			canvas.drawText("hsp: " + dataSource.hspeed, left+right/2, bottom+16, textPaint);
			textPaint.setColor(dpGray);
		
		// Debug thingies
			textPaint.setTextAlign(Align.RIGHT);
			canvas.drawText("FPS: " + fps, (left+right)/2, (top+bottom)/2, textPaint);
			
		// Aaaaand done!
			canvas.restore();
	}
	
	/* StaticDataHolder implementation */
	//@Override
	public void setSamplesArrays(int[] indexes, short[] amplitudes, 
								 int from, int to, int size) {
		/*s_start = from;
		s_end = to;
		s_size = size;
		s_index = indexes;
		s_amplitude = amplitudes;*/
	}
	
	/* DataHolder implementation */
	public void initData() {
		// Set queue sizes (TODO: Paramtrize size)
		float rsize = Data.getInstance().viewWidth*250; // TODO: Parametrize sps
		s_size = (int) rsize;
		// dp_size = (int) (rsize / 6);
		
		// Reset indexes
		s_start = s_end = 0;
		// dp_start = dp_end = 0;
		
		// Initialize queues
		// s_index = new int[s_size];
		// s_amplitude = new short[s_size];
		// dp_sample = new int[dp_size];
		// dp_type = new short[dp_size];
		// dp_wave = new short[dp_size];
		
		// Initialize point array
		samplePoints = new float[s_size*4];
	}

	//@Override
	public void addSample(int index, short sample) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void addDPoint(int sample, short type, short wave) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void handleOffset(int offset) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void handleHBR(float hbr) {
		// TODO Auto-generated method stub
		
	}
}
