package com.electrolites.usb;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class UsbComThread extends Thread {
	private FileDescriptor fd;
	private FileInputStream input;
	private FileOutputStream output;
	
	public UsbComThread(FileDescriptor fd) {
		this.fd = fd;
	}
	
	public void start() {
		input = new FileInputStream(fd);
		output = new FileOutputStream(fd);
	}
	
	public void run() {    
		// To be done
	}
}
