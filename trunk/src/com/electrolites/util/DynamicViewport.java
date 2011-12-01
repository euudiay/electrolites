package com.electrolites.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.electrolites.data.DPoint;
import com.electrolites.data.DPoint.PointType;
import com.electrolites.data.DPoint.Wave;
import com.electrolites.data.Data;

public class DynamicViewport {
	// Posici�n, ancho y alto del viewport en pixels
	public int vpPxX;
	public int vpPxY;
	public int vpPxWidth;
	public int vpPxHeight;

	// Muestras comprendidas en la viewarea
	public int vaSamples;
	// Muestras por segundo
	public float samplesPerSecond;
	
	// Base de dibujo horizontal
	public float baselinePxY;
	
	// Data de verdad
	public Data actualData;
	
	// Samples Data
	public LinkedList<SamplePoint> samplesData;
	public LinkedList<LineDrawCommand> pointsData;
	protected HashMap<Integer, Float> samplesIndex;
	
	// Testing area
	public float vFactor;
	public int areaOffset, lastOffset;
	public float verticalThreshold;
	
	public DynamicViewport(int width, int height, float seconds) {
		vpPxWidth = width;
		vpPxHeight = height;

		actualData = Data.getInstance();
		
		samplesData = new LinkedList<SamplePoint>();
		
		samplesPerSecond = 250f;
		
		areaOffset = 0;
		lastOffset = 0;
		
		verticalThreshold = 0.4f;
		
		updateParameters();
	}
	
	public void setOnScreenPosition(int pxX, int pxY) {
		vpPxX = pxX;
		vpPxY = pxY;
		baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
	}
	
	public void updateParameters() {
		synchronized (actualData.dynamicData) {
			actualData.dynamicData.setSamplesQueueWidth((int) (samplesPerSecond * Math.max(0.1f, actualData.getWidthScale())));
			vaSamples = actualData.dynamicData.samplesQueueWidth;
			baselinePxY = vpPxY + vpPxHeight*actualData.getDrawBaseHeight();
		}
		
		float top = vpPxHeight*0.85f;
		float max = 12000f;
		vFactor = top/max;
	}
	
	public float[] getViewContents() {
		
		// Get a new sample and update drawing parameters
		updateParameters();
		
		float dpoints = vpPxWidth / ((float) vaSamples);
		
		if (dpoints <= 0) {
			System.err.println("No usable quantity of samples found. Please note, this error should not have popped.");
			return null;
		}
		
		// Full data clone 
		samplesData = new LinkedList<SamplePoint>();
		synchronized(actualData.dynamicData) {
			// Shallow copy!
			Object[] temp = actualData.dynamicData.samplesQueue.toArray();
			SamplePoint p;
			/*int len = Math.min((int) (actualData.dynamicData.samplesQueueWidth/* *(1+actualData.dynamicData.bufferWidth)),
							temp.length);*/
			int len = Math.min(actualData.dynamicData.samplesQueueWidth, temp.length);
			for (int i = 0; i < len; i++) {
				p = (SamplePoint) temp[i];
				samplesData.add(p.clone());
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
		for (int i = 0; i < samplesData.size()-1; i++) {
			if (!it.hasNext()) {
				System.err.println("No sample found at position " + i);
				return null;
			}
			
			// Index sample
			p = it.next();
			samplesIndex.put(p.id, vpPxX + i*dpoints);
			
			if (i == 0) {
				results[i] = vpPxX;
				results[i+1] = baselinePxY - p.sample*vFactor;
				if (it.hasNext()) {
					p = it.next();
					samplesIndex.put(p.id, vpPxX + i*dpoints);
					results[i+2] = vpPxX + dpoints;
					results[i+3] = baselinePxY - p.sample*vFactor;
				}
			}
			else {
				// Duplicate last point
				results[4*i] = results[4*i-2];
				results[4*i+1] = results[4*i-1];
				results[4*i+2] = vpPxX + i*dpoints;
				results[4*i+3] = baselinePxY - p.sample*vFactor;
			}
		}
		
		return results;
	}
	
	public LinkedList<LineDrawCommand> getViewDPoints() {
		// Full data clone 
		pointsData = new LinkedList<LineDrawCommand>();
		synchronized(actualData.dynamicData) {
			int len = actualData.dynamicData.dpointsQueue.size();
			for (int i = 0; i < len; i++) {
				ExtendedDPoint ep = actualData.dynamicData.dpointsQueue.list.get(i).clone();
				if (ep.getIndex() < areaOffset || ep.getIndex() >= lastOffset)
					continue;
				
				LineDrawCommand com = new LineDrawCommand();
				
				DPoint p = ep.getDpoint();
				if (p.getType() == PointType.start || p.getType() == PointType.end) {
		            com.setWidth(1.f);
					com.setARGB(200, 180, 180, 240);
				}
				else if (p.getType() == PointType.peak) {
		            com.setWidth(2.f);
		            if (p.getWave() == Wave.QRS)
						com.setARGB(230, 255, 0, 255);
		            else if (p.getWave() == Wave.P)
						com.setARGB(230, 0, 255, 255);
					else if (p.getWave() == Wave.T)
						com.setARGB(230, 255, 255, 0);
				}
				else continue;
				
				float x;
				if (ep != null && samplesIndex != null)
					x = samplesIndex.get(ep.getIndex());
				else {
					x = 0;
					System.out.println("Sampes Index or ep null in getViewDPoints()");
				}
				//float x = vpPxX + (samplesIndex[i] - areaOffset)*dpoints;
				
				/*if (baselinePxY > verticalThreshold*vpPxHeight)
					com.setPoints(x, vpPxY, x, baselinePxY-samplesData.get(ep.getIndex()-areaOffset).sample*vFactor-10*vFactor);
				else
					com.setPoints(x, vpPxY+vpPxHeight, x, baselinePxY-samplesData.get(ep.getIndex()-areaOffset).sample*vFactor+10*vFactor);*/
				
				com.setPoints(x, vpPxY, x, baselinePxY+1);
				
				pointsData.add(com);
			}
		}
		
		return pointsData;
	}
}
