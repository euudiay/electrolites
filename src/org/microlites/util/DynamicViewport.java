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
		}
		
		float top = vpPxHeight*0.85f;
		float max = 12000f;
		vFactor = top/max;
	}
	
	/*public float[] getViewContents() {
		// Get a new sample and update drawing parameters
		updateParameters();
		
		float dpoints = vpPxWidth / ((float) vaSamples);
		
		if (dpoints <= 0) {
			System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			return null;
		}
		
		// Full data clone 
		samplesData = new LinkedList<SamplePoint>();
		int w;
		synchronized(actualData.dynamicData) {
			// Shallow copy!
			//w = actualData.dynamicData.samplesQueueActualWidth - actualData.dynamicData.samplesQueueWidth;
			Object[] temp = actualData.dynamicData.samplesQueue.toArray();
			SamplePoint p;
			//int len = Math.min((int) (actualData.dynamicData.samplesQueueWidth/* *(1+actualData.dynamicData.bufferWidth) * /),
			//				temp.length);
			w = temp.length;
			int beginAt = 0;//Math.min(temp.length, actualData.dynamicData.samplesQueueWidth);
			int len = Math.min(temp.length, actualData.dynamicData.samplesQueueWidth);
			//int len = temp.length;
			for (int i = beginAt; i < len; i++) {
				p = (SamplePoint) temp[i];
				if (p != null) {
					samplesData.add(p.clone());
				}
			}
		}
		if (!samplesData.isEmpty()) {
			areaOffset = samplesData.peek().id;
			lastOffset = samplesData.peekLast().id;
		}

		float[] results = new float[4+4*(samplesData.size()-1)];
		
		Iterator<SamplePoint> it = samplesData.iterator();
		SamplePoint p;
		samplesIndex = new HashMap<Integer, Float>();
		
		for (int i = 0; i < samplesData.size(); i++) {
			if (!it.hasNext()) {
				System.err.println("No sample found at position " + i);
				return null;
			}
			
			// Index sample
			p = it.next();
			//if (p.id % 256 >= 128)
				//samplesIndex.put(p.id, vpPxX + i*dpoints + 8);
			//else
				samplesIndex.put(p.id, vpPxX + i*dpoints);
			
			if (i == 0) {
				results[i] = vpPxX;
				results[i+1] = baselinePxY - p.sample*vFactor;
			}
			else if (i == 1) {
				results[4*i-2] = vpPxX + dpoints;
				results[4*i-1] = baselinePxY - p.sample*vFactor;
			}
			else {
				// Duplicate last point
				results[4*i] = results[4*i-2];
				results[4*i+1] = results[4*i-1];
				results[4*i+2] = vpPxX + i*dpoints;
				results[4*i+3] = baselinePxY - p.sample*vFactor;
			}
			
			//if (i == samplesData.size()-w-1)
			//	lastOffset = p.id;
		}
		
		return results;
	}*/
	
	/*public LinkedList<LineDrawCommand> getViewDPoints() {
		// Full data clone 
		pointsData = new LinkedList<LineDrawCommand>();
		synchronized(actualData.dynamicData) {
			int len = actualData.dynamicData.dpointsQueue.size();
			for (int i = 0; i < len; i++) {
				ExtendedDPoint ep = actualData.dynamicData.dpointsQueue.list.get(i).clone();
				if (ep.getIndex() < areaOffset || ep.getIndex() >= lastOffset)
					continue;
					
				
				LineDrawCommand com = new LineDrawCommand();
				com.defaultValues(ep.getDpoint());
				
				/*DPoint p = ep.getDpoint();
				
				if (p.getType() == PointType.start || p.getType() == PointType.end) {
		            com.setWidth(1.f);
					com.setARGB(200, 180, 180, 240);
					// Debug offset dpoint
					if (p.getWave() == Wave.Offset) {
						com.setARGB(200, 244, 10, 10);
					}
				}
				else if (p.getType() == PointType.peak) {
		            com.setWidth(2.f);
		            if (p.getWave() == Wave.QRS)
						com.setARGB(230, 255, 0, 255);
		            else if (p.getWave() == Wave.P)
						com.setARGB(230, 0, 255, 255);
					else if (p.getWave() == Wave.T)
						com.setARGB(230, 255, 255, 0);
					else if (p.getWave() == Wave.Offset) {
						com.setWidth(3.0f);
						com.setARGB(230, 10, 244, 10);
					}
				}
				else continue;* /
				
				float x;
				int index;
				if (ep != null && samplesIndex != null) {
					index = ep.getIndex();
					try {
						x = samplesIndex.get(index);
					} catch (NullPointerException e) {
						continue;
					}
				}
				else {
					x = 0;
					System.out.println("Sampes Index or ep null in getViewDPoints()");
				}
				//float x = vpPxX + (samplesIndex[i] - areaOffset)*dpoints;
				
				if (baselinePxY > verticalThreshold*vpPxHeight)
					com.setPoints(x, vpPxY, x, baselinePxY-samplesData.get(ep.getIndex()-areaOffset).sample*vFactor-10*vFactor);
				else
					com.setPoints(x, vpPxY+vpPxHeight, x, baselinePxY-samplesData.get(ep.getIndex()-areaOffset).sample*vFactor+10*vFactor);
				
				//com.setPoints(x, vpPxY, x, baselinePxY+1);
				
				if (ep.getDpoint().getWave() == Wave.Offset)
					com.setPoints(x, vpPxY, x, vpPxY+vpPxHeight);
				
				pointsData.add(com);
			}
		}
		
		return pointsData;
	}*/
}
