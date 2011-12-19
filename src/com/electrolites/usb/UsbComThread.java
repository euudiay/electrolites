package com.electrolites.usb;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

import com.electrolites.data.Data;
import com.electrolites.util.FixedLinkedList;
import com.electrolites.util.RealTimeFriendlyDataParser;

public class UsbComThread extends Thread {
	public final static String TAG = "UsbComThread";
	
	private FileDescriptor fd;
	private FileInputStream input;
	private FileOutputStream output;
	
	private boolean stop;
	
	// Test land of awesomeness
	private RealTimeFriendlyDataParser dp;
	private Data data;
	private FixedLinkedList<Byte> stream;
	
	public UsbComThread(FileDescriptor fd) {
		this.fd = fd;
	}
	
	public void start() {
		input = new FileInputStream(fd);
		output = new FileOutputStream(fd);
		
		stream = new FixedLinkedList<Byte>(0);
		
		data = Data.getInstance();
		
		dp = new RealTimeFriendlyDataParser();
		dp.setData(data);
		dp.setStream(stream);
		
		super.start();
	}
	
	public void run() {
		stop = false;
		byte startToken = (byte) 0xC0;
		byte[] buffer = new byte[256];
		
		String str;
		
		// Mandamos el token para empezar a recibir
		while (true) {
			try {
				output.write(startToken);
			} catch (IOException e) {
				Log.w(TAG, "Start token could not be delivered.");
				return;
			}
			
			try {
				if (input.read(buffer) == 1) {
					System.out.println(buffer[0]);
					if (buffer[0] == (byte) 0xc0) {
						System.out.println("ACK!");
						break;
					}
						
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int n = -1;
		// Nos ponemos a la escucha
		while (!stop) {
			for (int i = 0; i < 25; i++) {
				if (dp.hasNext())
					dp.step();
			}
			
			
			byte[] buf = new byte[10];
			try {
				n = input.read(buf);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			if (n > 0) {
				// Show data
				str = "";
				for (int i = 0; i < n; i++)
					str += (buf[i] & 0xff) + "_";
				str.substring(0, str.length()-1);
				System.out.println(str);
				
				// To dataparser!
				for (int i = 0; i < n; i++)
					stream.add(new Byte(buf[i]));
			}
			
			// Stop if we have to
			synchronized (data.dynamicData.mutex) {
				if (data.dynamicData.stop)
					break;
			}
			try {
				// Take it easy, dude
				sleep(10);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	public void halt() { stop = true; }
}

/* Old, deprectated run method **
	public void runOld() {
		stop = false;
		byte[] startToken = { (byte) 0xc0 };
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Mandamos el token para empezar a recibir
		while (true) {
			try {
				output.write(startToken);
			} catch (IOException e) {
				Log.w(TAG, "Start token could not be delivered.");
				return;
			}
			
			try {
				if (input.read(buffer) == 1) {
					System.out.println(buffer[0]);
					if (buffer[0] == (byte) 0xc0) {
						System.out.println("ACK!");
						break;
					}
						
				}
			} catch (IOException e) {
				
			}
		}
		
		int n = -1;
		// Nos ponemos a la escucha
		while (!stop) {
			byte[] buf = new byte[2];
			try {
				n = input.read(buf);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			if (n >= 2) {
				System.out.println(buf[0] + "--" + buf[1]);
				if ((buf[0] & 0xff) == 0xA1)
					if ((buf[1] & 0xff) == 0x0A)
						System.out.println("Got token!");
			}
			
			try {
				sleep(10);
			} catch (InterruptedException e) {
				
			}
		}
	}
*/