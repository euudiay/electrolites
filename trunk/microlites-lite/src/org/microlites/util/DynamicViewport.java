package org.microlites.util;

import org.microlites.data.Data;

public class DynamicViewport {
	public int vpPxX;				// Onscreen x position
	public int vpPxY;				// Onscreen y position
	public int vpPxWidth;			// Viewport widht
	public int vpPxHeight;			// Viewport height

	public int vaSamples;			// Samples in view
	public float samplesPerSecond;	// Samples per second
	
	public float baselinePxY;		// Horizontal draw baseline
	
	public float vFactor;			// Vertical scale factor
	public int areaOffset;			// TODO: Document these 
	public int lastOffset;			// TODO: Document these
	public float verticalThreshold;	// TODO: Document these
	public float max;				// Max Wave Height (used to calc vFactor)
	
	public Data data;				// Application data holder
	
	public DynamicViewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;

		samplesPerSecond = 250f;
		
		areaOffset = 0;
		lastOffset = 0;
		
		verticalThreshold = 0.4f;
		
		data = Data.getInstance();
		
		updateParameters();
	}
	
	public void setOnScreenPosition(int pxX, int pxY) {
		vpPxX = pxX;
		vpPxY = pxY;
		baselinePxY = vpPxY + vpPxHeight*data.drawBaseHeight;
	}
	
	public void updateParameters() {
		synchronized (data.mutex) {
			baselinePxY = vpPxY + vpPxHeight*data.drawBaseHeight;
			max = data.yScaleTopValue;
		}
		
		float top = vpPxHeight*0.85f;
		vFactor = top/max;
	}
}
