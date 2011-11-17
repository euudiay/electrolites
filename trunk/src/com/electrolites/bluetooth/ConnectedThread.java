package com.electrolites.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.electrolites.services.BluetoothService;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

// Actúa como cliente, una vez conectado el socket envía datos (o los recibe)
public class ConnectedThread extends Thread {
	private final BluetoothSocket socket;
    private final InputStream input;
    private final OutputStream output;
    
    private BluetoothService bS;

    public ConnectedThread(BluetoothService bS, BluetoothSocket socket) {
        Log.d(BluetoothService.TAG, "ConnectedThread creado.");
        this.bS = bS;
        this.socket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Obtenemos los streams de entrada y salida
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(BluetoothService.TAG, "No se han podido obtener los streams del socket.", e);
        }

        input = tmpIn;
        output = tmpOut;
    }
    
    // Recepción de datos
    @Override
	public void run() {
        Log.i(BluetoothService.TAG, "ConnectedThread comienza su actividad.");
        byte[] buffer = new byte[1024];
        int bytes;

        // Se mantiene a la escucha mientras esté conectado
        while (true) {
            try {
                // Leemos del InputStream
            	//buffer = new byte[1024];
                bytes = input.read(buffer);
                
                // Hacemos cosas con los datos obtenidos, como guardarlos en data
                bS.read(bytes, buffer);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "La conexión se ha perdido.", e);
                bS.connectionLost();
                break;
            }
        }
    }
    
    // Envío de datos al dispositivo
    public void write(byte[] buffer) {
        try {
            output.write(buffer);
        } catch (IOException e) {
            Log.e(BluetoothService.TAG, "No se ha podido realizar el envío.", e);
        }
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(BluetoothService.TAG, "Ha fallado el cierre del socket.", e);
        }
    }

}
