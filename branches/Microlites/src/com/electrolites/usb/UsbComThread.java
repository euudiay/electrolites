package com.electrolites.usb;

import java.nio.ByteBuffer;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.electrolites.data.Data;
import com.electrolites.util.FixedLinkedList;
import com.electrolites.util.RealTimeFriendlyDataParser;

public class UsbComThread extends Thread {
	public final static String TAG = "UsbComThread";
	
	private boolean stop;
	
	// Test land of awesomeness
	private RealTimeFriendlyDataParser dp;
	private Data data;
	private FixedLinkedList<Byte> stream;
	
	private UsbInterface interf = null;
	private UsbEndpoint endpointRead = null;
	private UsbEndpoint endpointWrite = null;
	private UsbDeviceConnection connection = null;
	
	private int bufferDataLength;
	
	public UsbComThread(UsbInterface interf, UsbEndpoint endpointIn, UsbEndpoint endpointOut, UsbDeviceConnection connection) {

		this.connection = connection;
		this.interf = interf;
		this.endpointRead = endpointIn;
		this.endpointWrite = endpointOut;
		
		//this.fd = fd;
	}
	
	@Override
	public void start() {
		bufferDataLength = endpointWrite.getMaxPacketSize();
		
		stream = new FixedLinkedList<Byte>(0);
		
		data = Data.getInstance();
		
		dp = new RealTimeFriendlyDataParser();
		dp.setData(data);
		dp.setStream(stream);
		
		super.start();
	}
	
	@Override
	public void run() {

		stop = false;

		//byte[] buffer = new byte[256];
		
		String str;
		
		bufferDataLength = endpointRead.getMaxPacketSize();
		ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
		UsbRequest requestQueued = null;
		UsbRequest request = new UsbRequest();
		
		byte startToken = (byte) 0xC0;
		
		request.initialize(connection, endpointRead);

		
		// Mandamos el token para empezar a recibir
		while (true) {
			if (! write(startToken)){
				Log.w(TAG, "Start token could not be delivered.");
				return;
			}
			
			/*try {
				if (input.read(buffer) == 1) {
					System.out.println(buffer[0]);
					if (buffer[0] == (byte) 0xc0) {
						System.out.println("ACK!");
						break;
					}
						
				}
			} catch (IOException e) {
				e.printStackTrace();
			}*/break;
		}
		
		int n = -1;
		// Nos ponemos a la escucha
		try{
			while (!stop) {
				for (int i = 0; i < 25; i++) {
					if (dp.hasNext())
						dp.step();
				}
				//Parte nueva para leer si eres host
				request.queue(buffer, bufferDataLength);
				requestQueued = connection.requestWait();
				if (request.equals(requestQueued))  {
					
					byte[] byteBuffer = new byte[bufferDataLength + 1];
					buffer.get(byteBuffer, 0, bufferDataLength);
			
					if (bufferDataLength > 0) {
						// Show data
						str = "";
						for (int i = 0; i < bufferDataLength; i++)
							str += (buffer.array()[i] & 0xff) + "_";
						str.substring(0, str.length()-1);
						System.out.println(str);
						
						// To dataparser!
						for (int i = 0; i < bufferDataLength; i++)
							stream.add(new Byte((byte) 0xda));
							//stream.add(new Byte(buffer.array()[i]));
					}
					buffer.clear();
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
		}catch (Exception ex){
			Log.w(TAG, "Algo a pasado durante la transmisión");
		}
		try	{
			 request.cancel();
			 request.close();
		}catch (Exception ex){
			Log.w(TAG, "Algo a pasado al cerrar la transmisión");
		}
			
	}
	
	private boolean write(byte data){
		bufferDataLength = endpointWrite.getMaxPacketSize();
		ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
		UsbRequest request = new UsbRequest();

		buffer.put(data);

		request.initialize(connection, endpointWrite);
		request.queue(buffer, bufferDataLength);
		try
		{
			if (request.equals(connection.requestWait())){
				return true;
			}
			return false;
		}
		catch (Exception ex){
		 // An exception has occured
			return false;
		}
	}

	
	
	/* El run para cuando es modo device
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
	}*/
	
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