package com.electrolites.usb;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class UsbComThread extends Thread {
	public final static String TAG = "UsbComThread";
	
	private FileDescriptor fd;
	private FileInputStream input;
	private FileOutputStream output;
	
	private boolean stop;
	
	public UsbComThread(FileDescriptor fd) {
		this.fd = fd;
	}
	
	public void start() {
		input = new FileInputStream(fd);
		output = new FileOutputStream(fd);
	}
	
	public void run() {
		stop = false;
		byte[] startToken = { (byte) 0xc0 };
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Mandamos el token para empezar a recibir
		try {
			output.write(startToken);
		} catch (IOException e) {
			Log.w(TAG, "Start token could not be delivered.");
		}
		// Nos ponemos a la escucha
		while (!stop) {
			try {
				bytes = input.read(buffer);
				
				/* // Para cuando exista UsbParserService, si es que finalmente es un service
				synchronized (UsbParserService.stream) {
					for (int i = 0; i < bytes; i++)
						UsbParserService.stream.add(buffer[i]);
				}
				*/
			} catch (IOException e) {
				Log.w(TAG, "Cannot read from usbAccessory.");
				break;
			}
		}
	}
	
	public void halt() { stop = true; }
}
