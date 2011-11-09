package com.electrolites.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.electrolites.services.BluetoothService;

// Actúa como servidor, se mantiene a la escucha y recibe datos
public class AcceptThread extends Thread {
	private final BluetoothServerSocket serverSocket;
	
	private BluetoothAdapter bA;
	private BluetoothService bS;	// Referencia al BluetoohService
	
	// Inicializa el serverSocket para recibir datos
	public AcceptThread(BluetoothAdapter bA, BluetoothService bS, UUID uuid) {
		this.bA = bA;
		this.bS = bS;
		BluetoothServerSocket tmp = null;

        try {
        	tmp = bA.listenUsingRfcommWithServiceRecord("BluetoothService", uuid);
        } catch (IOException e) {
            Log.e(BluetoothService.TAG, "Ha fallado la inicialización del ServerSocket", e);
        }
        
        serverSocket = tmp;
	}
	
	public void run() {
        if (BluetoothService.DEBUG) 
        	Log.d(BluetoothService.TAG, "AcceptThread " + this + " comienza su actividad.");

        BluetoothSocket socket = null;

        // Comprobamos que no estemos ya conectados
        while (bS.getState() != BluetoothService.STATE_CONNECTED) {
            try {
                socket = serverSocket.accept(); // Llamada bloqueante
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "Ha fallado el intento de conexión entrante (serverSocket.accept())", e);
                break;
            }

            // Si se ha aceptado la nueva conexión...
            if (socket != null) {
                synchronized (bS) {
                    switch (bS.getState()) {
		                case BluetoothService.STATE_LISTEN:
		                case BluetoothService.STATE_CONNECTING:
		                    // Situación normal, iniciamos la conexión
		                    bS.connected(socket, socket.getRemoteDevice());
		                    break;
		                case BluetoothService.STATE_NONE:
		                case BluetoothService.STATE_CONNECTED:
		                    // O no está preparado o ya ha sido conectado. Cerramos la conexión
		                    try {
		                        socket.close();
		                    } catch (IOException e) {
		                        Log.e(BluetoothService.TAG, "No se ha podido cerrar el socket.", e);
		                    }
		                    break;
                    }
                }
            }
        }
        
        if (BluetoothService.DEBUG) 
        	Log.i(BluetoothService.TAG, "AcceptThread " + this + " finaliza su actividad.");

    }
	
	public void cancel() {
        if (BluetoothService.DEBUG) 
        	Log.d(BluetoothService.TAG, "Cancelando la conexión: " + this);
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(BluetoothService.TAG, "Ha fallado el cierre del ServerSocket", e);
        }
    }
}
