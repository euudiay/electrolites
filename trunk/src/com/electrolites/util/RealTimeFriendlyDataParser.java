package com.electrolites.util;

import java.util.Formatter;

import com.electrolites.data.DPoint;
import com.electrolites.data.Data;

public class RealTimeFriendlyDataParser {		
	private FixedLinkedList<Byte> stream;	// Bytes a tratar
	private int lastSample;					// Última muestra leída
	
	enum Token {None, Sample, DPoint, Offset, HBR};
	
	byte currentByte;
	Token currentToken;
	int progress;
	byte[] storedBytes;
	
	private Data data;
	
	public RealTimeFriendlyDataParser() {
		lastSample = 0;
		
		currentToken = Token.None;
		progress = 0;
	}
	
	public void setData(Data data) {
		this.data = data;
	}
	
	public void step() {
		// Suponemos que hay siguiente token
		currentByte = stream.get();
		switch (currentToken) {
		case None:
			switch (currentByte & 0xff) {
			case 0xcc:
				// Start parsing an Offset
				currentToken = Token.Offset;
				break;
			case 0xda:
				// Start parsing a sample
				currentToken = Token.Sample;
				break;
			case 0xed:
				// Start parsing a delineation point
				currentToken = Token.DPoint;
				break;
			case 0xfb:
				currentToken = Token.HBR;
				break;
			default:
				currentToken = Token.None;
				System.out.println("Token unknown: " + (currentByte&0xff));
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
		storedBytes[progress] = (byte) (currentByte & 0xff);
		progress++;
		if (progress >= 5) {
			int offset = ((((storedBytes[0]*256)+storedBytes[1])*256)+storedBytes[2])*256+storedBytes[3];
			lastSample = offset;
			currentToken = Token.None;
			progress = 0;
		}
	}
	
	public void parseSample() {
		storedBytes[progress] =  (byte) (currentByte & 0xff);
		progress++;
		if (progress >= 2) {
			short sample = byteToShort(storedBytes[0], storedBytes[1]);
			synchronized (data.dynamicData.mutex) {
				data.dynamicData.addSample(new SamplePoint(lastSample, sample));
			}
			lastSample++;
			currentToken = Token.None;
			progress = 0;			
		}
	}
	
	public void parseDPoint() {
		storedBytes[progress] = (byte) (currentByte & 0xff);
		progress++;
		if (progress >= 5) {
			DPoint p = new DPoint();
			p.setType(p.checkType(storedBytes[0]));
			p.setWave(p.checkWave(storedBytes[0]));
			ExtendedDPoint ep = new ExtendedDPoint(
				((((storedBytes[1]*256)+storedBytes[2])*256) + storedBytes[3])*256+storedBytes[4] , p);
			synchronized (data.dynamicData.mutex) {
				data.dynamicData.addDPoint(ep);
			}
			currentToken = Token.None;
			progress = 0;
		}
	}
	
	public void parseHBR() {
		System.out.println("AN HBR!");
		storedBytes[progress] = currentByte;
		progress++;
		if (progress >= 2) {
			int beatSamples = ((storedBytes[0]&0xff)*256 + (storedBytes[1]&0xff));
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
		// b1 más significativo que b2
		int i1 = b1;
		int i2 = b2;
		i1 &= 0xff;
		i2 &= 0xff;
		
		return (short) (i1*256 + i2);
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
}
