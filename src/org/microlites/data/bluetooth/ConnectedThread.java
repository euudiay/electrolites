package org.microlites.data.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.microlites.data.DataHolder;
import org.microlites.data.DataSourceThread;
import org.microlites.util.RealTimeDataParser;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectedThread extends DataSourceThread {
	/* Bluetooth reception */
	public boolean startReceiving = false;			// Reception control flag
	public BluetoothSocket socket; 					// BT reception stream
	private OutputStream output;					// BT transmission stream
	
	/* Input */
	private InputStream stream;						// Input stream to be parsed
	
	byte[] readBuffer = new byte[512];				// Temporal input buffer
	int readCount;									// bytes read to buffer

	/* Parser */
	private RealTimeDataParser parser;		// Raw data parser
	
	public ConnectedThread(DataHolder d, BluetoothSocket s) {
		Log.d("ConnectedThread", "ConnectedThread creado.");
		// Store refrence to socket
		socket = s;
		// Create parser
		parser = new RealTimeDataParser(d);

        // Get input and output streams
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("DynamicThread", "No se han podido obtener los streams del socket.", e);
        }

        stream = tmpIn;
        output = tmpOut;

		// Begin
		startReceiving = true;
	}

	public void run() {
		while (startReceiving) {
			// Double-check if we are ready
			if (socket == null) {
				startReceiving = false;
				return;
			}
			
			// Read from input into readBuffer
			try {
				readCount = stream.read(readBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				startReceiving = false;
				return;
			}
		
			// Parse data read
			for (int i = 0; i < readCount; i++) {
				parser.step(readBuffer[i]);
			}
		}
	}
	
	public void write(byte[] buffer) {
        try {
            output.write(buffer);
        } catch (IOException e) {
            Log.e("ConnectedThread", "No se ha podido realizar el envío.", e);
        }
    }
	
	public void cancel() {
		// Stop loop
		startReceiving = false;
		
		// Finish parser execution and close log files
		parser.finish();
		
		// Close socket if available
		if (socket == null)
			return;
		
		stream = null;
		output = null;
		
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("ConnectedThread", "Ha fallado el cierre del socket.", e);
        }
        
        socket = null;
    }
		
	
	public void stopSending() {
		cancel();
	}
}
