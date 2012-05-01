package com.electrolites.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Handler;

import com.electrolites.ecg.ElectrolitesActivity;
import com.electrolites.util.ExtendedDPoint;
import com.electrolites.util.FixedLinkedList;
import com.electrolites.util.SamplePoint;

public class Data {

	public static final short MODE_STOP = 0;
	public static final short MODE_STATIC = 1;
	public static final short MODE_DYNAMIC = 2;
	
	private static Data instance = null;
	public static Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		
		return instance;
	}
	
	// Posición X de la vista en secs
	public float vaSecX;
	// Altura base de renderizado (0~1)
	private float drawBaseHeight = 0.5f;
	// Escala de ancho: 0 (evitar) ~ whatever
	private float widthScale = 1;
	
	// Primera muestra dibujable
	public int offset;
	
	
	
	
	
	// Test area
	public Application app;
	public boolean autoScroll;
	public boolean loading;
	public Activity activity;
	public int bgColor;
	
	public short mode;
	
	public Handler handler;
	
	// Data container for dynamic visualization mode instance
	public DynamicData dynamicData;
	public StaticData staticData;
	
	public Data() {
		vaSecX = 0;
		drawBaseHeight = 0.5f;
		widthScale = 3;
		app = null;
		autoScroll = false;
		loading = false;
		
		bgColor = Color.rgb(0, 0, 0);
		
		
		//Data container for dynamic visualization mode
		dynamicData = new DynamicData();
		staticData = new StaticData();
		dynamicData.samplesQueueWidth = -1;
	}

	public float getDrawBaseHeight() {
		return drawBaseHeight;
	}

	public void setDrawBaseHeight(float drawBaseHeight) {
		this.drawBaseHeight = drawBaseHeight;
	}

	public float getWidthScale() {
		return widthScale;
	}

	public void setWidthScale(float widthScale) {
		this.widthScale = widthScale;
	}
	
	public short[] getSamplesArray() {
		/*Object samp[] = samples.toArray();
		short result[] = new short[samp.length];
		try {
			for (int i = 0; i < samp.length; i++) {
				if (samp[i] != null)
					result[i] = ((Short) samp[i]).shortValue();
				else
					result[i] = 0;
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}*/
		Object samp[] = staticData.samples.toArray();
		short result[] = new short[samp.length];
		for (int i = 0; i < samp.length; i++)
			if (samp[i] != null)
				result[i] = (short) ((SamplePoint) samp[i]).sample;
			else
				result[i] = 0;
		
		return result;
	}
	
	public class DynamicData {
		public Object mutex;
		public LinkedList<SamplePoint>samplesQueue;
		public FixedLinkedList<ExtendedDPoint> dpointsQueue;
		public int samplesQueueWidth;
		public int samplesQueueActualWidth;
		public float bufferWidth; 
		public float hbr;
		public boolean newHBR;
		
		//Nombre del dispositivo conectado
		public String connected;
		
		public boolean stop;

		public DynamicData() {
			mutex = new Object();
			samplesQueue = new LinkedList<SamplePoint>();
			dpointsQueue = new FixedLinkedList<ExtendedDPoint>(0);
			samplesQueueWidth = -1;
			samplesQueueActualWidth = -1;
			bufferWidth = 0.5f;
			hbr = 0;
			newHBR = false;
			
			connected = "FireFly-3781";
			
			stop = false;
		};
		
		public void setSamplesQueueWidth(int width) {
			samplesQueueWidth = width;
			samplesQueueActualWidth = samplesQueueWidth + (int) (width*(1 + bufferWidth));
		}
		
		public void addSample(SamplePoint sample) {
			if (samplesQueueActualWidth > 0) {
				// Update dpoints queue size
				dpointsQueue.capacity = samplesQueueActualWidth;
				if (samplesQueue.size() + 1 >= samplesQueueActualWidth) {	
					samplesQueue.remove();
				}
			}
			samplesQueue.add(sample);
		}
		
		public void addSamples(Collection<SamplePoint> list) {
			// If no width's specified, just add them all
			int newSamples = list.size();
			if (samplesQueueActualWidth < 0)
				samplesQueue.addAll(list);
			else {
				// Update dpoints queue size
				dpointsQueue.capacity = samplesQueueActualWidth;
				// Remove from head those that doesn't fit in
				if (samplesQueue.size() + newSamples >= samplesQueueActualWidth) {
					int toRemove = ((samplesQueue.size() + newSamples) - samplesQueueActualWidth);
					for (int i = 0; i < toRemove; i++) {
						if (samplesQueue.isEmpty())
							break;
						try {
							SamplePoint p = samplesQueue.removeFirst();
							p = null;
						} catch (NoSuchElementException e) {
							
						}
					}
				}
				// Add new ones at end
				samplesQueue.addAll(list);
				
			}
		}
			
		public SamplePoint getSample() {
			return samplesQueue.remove();
		}
		
		public void addDPoint(ExtendedDPoint p) {
			// Add only if in range
			if (samplesQueue.isEmpty())
				dpointsQueue.add(p);
			else {
				/*if (p.getIndex() >= samplesQueue.peek().id && 
						p.getIndex() <= samplesQueue.peekLast().id) {*/
					dpointsQueue.add(p);
				//}
			}
		}
		
		public void discardUntil(int sample) {
			/*SamplePoint p = new SamplePoint(-1, (short) -1);
			while (!samplesQueue.isEmpty()) {
				p = samplesQueue.remove();
				if (p == null)
					break;
				if (p.id < sample)
					p = null;
				else
					break;
			}
			// Re-insert the last one as it needs not to be removed
			samplesQueue.addFirst(p);*/
			samplesQueue.clear();
		}
		
		public void setHBR(float hbr) {
			newHBR = true;
			this.hbr = hbr;
			((ElectrolitesActivity) activity).updateHBR();
		}
		
		public float getHBR() {
			newHBR = false;
			return hbr;
		}
	}
	
	public class StaticData {
		// Muestras indexadas por no. de muestra
		public ArrayList<SamplePoint> samples = null;
		// Puntos resultantes de la delineaciï¿½n, indexados por no. de muestra
		public ArrayList<ExtendedDPoint> dpoints = null;
		// Valores del ritmo cardï¿½aco, indexados segï¿½n el nï¿½mero de muestra anterior a su recepciï¿½n
		public Map<Integer, Short> hbrs = null;
		
		//Nombre del archivo de log a cargar
		public String toLoad;
		
		public StaticData() {
			samples = new ArrayList<SamplePoint>();
			dpoints = new ArrayList<ExtendedDPoint>();
			hbrs = new HashMap<Integer, Short>();
			
			toLoad = "traza.txt";
		}
	}
}
