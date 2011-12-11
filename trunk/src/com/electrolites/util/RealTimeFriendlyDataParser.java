package com.electrolites.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.text.format.Time;

import com.electrolites.data.DPoint;
import com.electrolites.data.DPoint.PointType;
import com.electrolites.data.DPoint.Wave;
import com.electrolites.data.Data;

public class RealTimeFriendlyDataParser {		
	private FixedLinkedList<Byte> stream;	// Bytes a tratar
	private int lastSample;					// �ltima muestra le�da
	
	enum Token {None, Sample, DPoint, Offset, HBR};
	
	byte currentByte;
	Token currentToken;
	int progress;
	byte[] storedBytes;
	
	private Data data;
	
	private FileOutputStream output;
	
	public RealTimeFriendlyDataParser() {
		lastSample = 0;
		
		currentToken = Token.None;
		progress = 0;
	
		output = null;
	}
	
	public void setData(Data data) {
		this.data = data;
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {	
			Time now = new Time();
			now.setToNow();

			File root = Environment.getExternalStorageDirectory();
			File path = new File(root, "/Download/");
			File dir = new File(path, "log-" + now.format("%d%m-%Y_%H-%M") + ".txt");

			try {
				output = new FileOutputStream(dir.getPath());
			} catch (IOException e) {
				e.printStackTrace();
				output = null;
			}
		}
	}
	
	public void step() {
		// Suponemos que hay siguiente token
		currentByte = stream.get();
		switch (currentToken) {
		case None:
			switch ((byte) (currentByte & 0xff)) {
			case (byte) 0xcc:
				// Start parsing an Offset
				currentToken = Token.Offset;
				break;
			case (byte) 0xda:
				// Start parsing a sample
				currentToken = Token.Sample;
				break;
			case (byte) 0xed:
				// Start parsing a delineation point
				currentToken = Token.DPoint;
				break;
			case (byte) 0xfb:
				currentToken = Token.HBR;
				break;
			default:
				currentToken = Token.None;
				System.out.println("Token unknown: " + ((byte) currentByte & 0xff));
			}
			// Reset parsing data
			progress = 0;
			storedBytes = new byte[10];
			break;
		case Offset:
			parseOffset();
			break;
		case Sample:
			parseSample();
			break;
		case DPoint:
			parseDPoint();
			break;
		case HBR:
			parseHBR();
			break;
		default:
			currentToken = Token.None;
			break;
		}
	}
	
	public void parseOffset() {
		storedBytes[progress] = currentByte; //(byte) (currentByte & 0xff);
		progress++;
		if (progress >= 5) {
			
			int first = storedBytes[0]<<24 &  0xFF000000;	//11111111000000000000000000000000
			int second = storedBytes[1]<<16 & 0x00FF0000;	//00000000111111110000000000000000
			int third = storedBytes[2]<<8  &  0x0000FF00;	//00000000000000001111111100000000
			int fourth = storedBytes[3]    &  0x000000FF;	//00000000000000000000000011111111
			
			//int offset = ((((storedBytes[0]*256)+storedBytes[1])*256)+storedBytes[2])*256+storedBytes[3];
			//int offset = (int)(storedBytes[0] << 24) | (storedBytes[1] << 16) | (storedBytes[2] << 8) | (storedBytes[3]);
			int offset = first | second | third | fourth;
			boolean wrong = false;
			// Add a debug offset dpoint
			DPoint p = new DPoint(PointType.start, Wave.Offset);
			ExtendedDPoint ep = new ExtendedDPoint(lastSample , p);

			if (offset != lastSample) {
				ep.getDpoint().setType(PointType.peak);
				wrong = true;
				System.err.println("WRONG OFFSET; READJUST");
			}
			
			lastSample = offset;
			
			synchronized (data.dynamicData.mutex) {
				if (wrong)
					data.dynamicData.discardUntil(offset-1);
				data.dynamicData.addDPoint(ep);
			}
			
			currentToken = Token.None;
			progress = 0;
		}
	}
	
	public void parseSample() {
		storedBytes[progress] = (byte) (currentByte & 0xff);
		progress++;
		if (progress >= 2) {
			short sample = byteToShort(storedBytes[0], storedBytes[1]);
			//long sample = byteToLong(storedBytes[0], storedBytes[1]);
			//long sample = (storedBytes[0] << 8) | storedBytes[1];
			synchronized (data.dynamicData.mutex) {
				data.dynamicData.addSample(new SamplePoint(lastSample, sample));
			}
			
			if (output != null) {
				try {
					output.write(0xda);
                    output.write(storedBytes, 0, 2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			lastSample++;
			currentToken = Token.None;
			progress = 0;			
		}
	}
	
	public void parseDPoint() {
		storedBytes[progress] = currentByte;//(byte) (currentByte & 0xff);
		progress++;
		if (progress >= 5) {
			DPoint p = new DPoint();
			p.setType(p.checkType(storedBytes[0]));
			p.setWave(p.checkWave(storedBytes[0]));
			
			int first = storedBytes[1]<<24 &  0xFF000000;	//11111111000000000000000000000000
			int second = storedBytes[2]<<16 & 0x00FF0000;	//00000000111111110000000000000000
			int third = storedBytes[3]<<8  &  0x0000FF00;	//00000000000000001111111100000000
			int fourth = storedBytes[4]    &  0x000000FF;	//00000000000000000000000011111111
			
			int index = first | second | third | fourth;
			
			ExtendedDPoint ep = new ExtendedDPoint(index, p);
			//int index = ((((storedBytes[1]*256)+storedBytes[2])*256) + storedBytes[3])*256+storedBytes[4];
				//(storedBytes[1] << 24) | (storedBytes[2] << 16) | (storedBytes[3] << 8) | (storedBytes[4]), p);//
			synchronized (data.dynamicData.mutex) {
				data.dynamicData.addDPoint(ep);
			}
			currentToken = Token.None;
			progress = 0;
		}
	}
	
	public void parseHBR() {
		System.out.println("AN HBR!");
		storedBytes[progress] = (byte) (currentByte & 0xff);
		progress++;
		if (progress >= 2) {
			int beatSamples = (storedBytes[0]*256 + storedBytes[1]);
			float hbr = 60*250/(float) beatSamples;
			synchronized(data.dynamicData.mutex) {
				hbr = Math.round(hbr*100)/100.f;
				System.out.println("HBR: " + hbr);
				data.dynamicData.setHBR(hbr);
			}
			currentToken = Token.None;
			progress = 0;
		}
	}
	
	public boolean hasNext() {
		return !stream.list.isEmpty();
	}
	
	// Convierte dos bytes dados en su short correspondiente (en complemento a 2)
	public short byteToShort(byte b1, byte b2) {
		int i1 = b1;
		int i2 = b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
	}
	
	public long byteToLong(byte b1, byte b2) {
		byte i1 = (byte) (b1 & 0xff);
		byte i2 = (byte) (b2 & 0xff);
		
		return (long) ((i1 * 256) + i2);
	}
	
	public void setStream(FixedLinkedList<Byte> stream) { 
		this.stream = stream;
	}

	public void clearStream() {
		stream = new FixedLinkedList<Byte>(0);
	}

	public void addToStream(byte b) {
		stream.add(b);
	}

	public FixedLinkedList<Byte> getStream() {
		return stream;
	}

	public void finish() {
		if (output != null)
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("-----------------------DPARSER OUT-------------------");
	}
}
