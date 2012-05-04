package org.microlites.data.filereader;

import org.microlites.data.DataHolder;
import org.microlites.util.StaticDataParser;

import android.util.Log;

public class FileDataSourceThread extends Thread implements DataHolder {
	
	/* File reading Items */
	String filename;						// File to read
	FileConverter fileConverter;			// File converter utility
	byte[] stream;							// Raw data stream
	int stream_size;						// Actual data stream size
	StaticDataParser parser;				// Data Parser
	
	/* Data Holding Items*/
	// Samples
	protected int s_current;				// Circular queue pointers
	public int s_size;						// Circular queue max size
	public int s_index[];					// Sample index
	public short s_amplitude[];				// Sample amplitude
	
	public boolean loading;					// Loading flag
	public boolean started;					// Load Started flag
	
	// DPoints
	protected int dp_current;				// Circular queue pointers
	public int dp_size;						// Circular queue max size
	public short dp_type[];					// DPoint types
	public short dp_wave[];					// DPoint waves
	public int dp_sample[];					// DPoint samples
	
	/* Scroll handling Items */
	public int s_viewstart;					// View to start in this array index
	public int s_viewend;					// View to end in this array index
	public int s_viewsize;					// View size in array positions
	
	public int dp_viewstart;				// View to start in this array index
	public int dp_viewend;					// View to end in this array index
	public int dp_viewsize;					// View size in array positions
	
	public float hspeed;					// Horizontal scrolling speed
	public float hacc;						// Horizontal scrolling deceleration
	public boolean forcedMovement;			// Should not apply deceleration
	
	public boolean running;					// Running flag 
	
	public FileDataSourceThread(DataHolder d, String pathToFile) {
		loading = false;
		started = false;
		running = false;
		
		filename = pathToFile;
		
		hspeed = 0;
		hacc = 0.2f;
		forcedMovement = false;
	}

	public void setViewSamplesSize(int size) {
		s_viewsize = size;
		s_viewend = Math.min(s_viewstart + size, s_size); 
	}
	
	public void handleScroll(float distX) {
		boolean forced;
		if (Math.abs(distX) < 20)
			forced = true;
		else
			forced = false;
		handleScroll(distX, forced);
	}
	
	public void handleScroll(float distX, boolean forced) {
		forcedMovement = forced;
		hspeed = distX*0.1f;
	}
	
	public void finish() {
		running = false;
	}
	
	@Override
	public void run() {
		if (!started) {
			started = true;
			loading = true;
			
			fileConverter = new FileConverter();
			int[] size = new int[1];
			stream = fileConverter.readBinaryFriendly(filename, size);
			int length = size[0];
			
			// The stream will be parsed and stored in the arrays
			initData();
			
			// Init Parser
			parser = new StaticDataParser(this);
			// Parse full stream
			for (int i = 0; i < length; i++) {
				parser.step(stream[i]);
			}
			
			s_size = s_current;
			dp_size = dp_current;
			
			loading = false;
			
			s_viewstart = 0;
			if (s_viewsize > 0)
				s_viewend = Math.min(s_viewstart + s_viewsize, s_size);
			else
				s_viewend = 0;
			
			dp_viewstart = 0;
			dp_viewend = dp_size-1;
			
			Log.d("FileDataSource", "File parsing finished");
			Log.d("FileDataSource", "Samples: " + s_size + "; DPoints: " + dp_size);
			
			running = true;
		}
		
		while (running) {
			//s_viewstart += (int) android.util.FloatMath.floor(hspeed);
			s_viewstart += (int) Math.round(hspeed);
			s_viewstart = Math.max(0, Math.min(s_viewstart, s_size-s_viewsize-1));
			s_viewend = s_viewstart + s_viewsize;
			
			if (s_viewstart == 0 || s_viewend >= s_size-1)
				hspeed = 0;
			
			if (!forcedMovement) {
				if (Math.abs(hspeed) <= hacc)
					hspeed = 0;
				else if (hspeed > 0)
					hspeed -= hacc;
				else if (hspeed < 0)
					hspeed += hacc;
			} else {
				forcedMovement = false;
			}
			
			try {
				sleep(4);
			} catch (InterruptedException e) {
				System.out.println("PHYSICS ENGINE FAIL!");
			}
		}
	}
	
	/* DataHolder Implementation for storing parsing result*/
	//@Override
	public void initData() {
		// Arrays are initialized to arbitrary sizes
		s_size = stream.length/2;
		s_index = new int[s_size];
		s_amplitude = new short[s_size];
		s_current = 0;
		
		dp_size = stream.length/4;
		dp_type = new short[dp_size];
		dp_wave = new short[dp_size];
		dp_sample = new int[dp_size];
		dp_current = 0;
	}


	//@Override
	public void addSample(int index, short sample) {
		s_index[s_current] = index;
		s_amplitude[s_current] = sample;
		s_current++;
		// If array is full, resize
		if (s_current >= s_size) {
			int[] new_index = new int[s_size*2];
			for (int i = 0; i < s_size; i++)
				new_index[i] = s_index[i];
			s_index = new_index;
			
			short[] new_amplitude = new short[s_size*2];
			for (int i = 0; i < s_size; i++)
				new_amplitude[i] = s_amplitude[i];
			s_amplitude = new_amplitude;
			
			s_size *= 2;
		}
	}


	//@Override
	public void addDPoint(int sample, short type, short wave) {
		dp_sample[dp_current] = sample;
		dp_type[dp_current] = type;
		dp_wave[dp_current] = wave;
		dp_current++;
		// If array is full, resize
		if (dp_current >= dp_size) {
			int[] new_sample = new int[dp_size*2];
			for (int i = 0; i < dp_size; i++)
				new_sample[i] = dp_sample[i];
			dp_sample = new_sample;
			
			short new_type[] = new short[dp_size*2];
			for (int i = 0; i < dp_size; i++)
				new_type[i] = dp_type[i];
			dp_type = new_type;
			
			short new_wave[] = new short[dp_size*2];
			for (int i = 0; i < dp_size; i++)
				new_wave[i] = dp_wave[i];
			dp_wave = new_wave;
			
			dp_size *= 2;
		}
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
