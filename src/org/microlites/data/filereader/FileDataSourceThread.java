package org.microlites.data.filereader;

import java.util.ArrayList;

import org.microlites.data.DataHolder;
import org.microlites.util.StaticDataParser;

public class FileDataSourceThread extends Thread implements DataHolder {
	
	/* File reading Items */
	String filename;						// File to read
	FileConverter fileConverter;			// File converter utility
	ArrayList<Byte> stream;					// Raw data stream
	StaticDataParser parser;				// Data Parser
	
	/* Data Holding Items*/
	// Samples
	protected int s_current;				// Circular queue pointers
	public int s_size;						// Circular queue max size
	public int s_index[];					// Sample index
	public short s_amplitude[];				// Sample amplitude
	
	public boolean loading;					// Loading flag
	
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
	public int dp_viewsize;					// View size in array positions
	
	public float hspeed;					// Horizontal scrolling speed
	public float hacc;						// Horizontal scrolling deceleration
	
	public FileDataSourceThread(DataHolder d, String pathToFile) {
		loading = true;
		
		filename = pathToFile;
		fileConverter = new FileConverter();
		stream = fileConverter.readBinary(pathToFile);
		
		// The stream will be parsed and stored in the arrays
		initData();
		
		// Init Parser
		parser = new StaticDataParser(this);
		// Parse full stream
		for (Byte b : stream) {
			parser.step(b.byteValue());
		}
		
		loading = false;
		/*for (int i = 0; i < s_current; i++)
			System.out.println(s_amplitude[i]+", ");*/
	}


	public void setViewSamplesSize(int size) {
		s_viewsize = size;
		s_viewend = Math.min(s_viewstart + size, s_size); 
	}
	
	/* DataHolder Implementation for storing parsing result*/
	@Override
	public void initData() {
		// Arrays are initialized to arbitrary sizes
		s_size = stream.size()/2;
		s_index = new int[s_size];
		s_amplitude = new short[s_size];
		s_current = 0;
		
		dp_size = stream.size()/4;
		dp_type = new short[dp_size];
		dp_wave = new short[dp_size];
		dp_sample = new int[dp_size];
		dp_current = 0;
	}


	@Override
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


	@Override
	public void addDPoint(int sample, short type, short wave) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleOffset(int offset) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleHBR(float hbr) {
		// TODO Auto-generated method stub
		
	}
}
