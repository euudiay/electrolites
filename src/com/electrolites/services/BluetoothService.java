package com.electrolites.services;

import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import com.electrolites.bluetooth.AcceptThread;
import com.electrolites.bluetooth.ConnectThread;
import com.electrolites.bluetooth.ConnectedThread;

public class BluetoothService extends DataService {
	public static final String TAG = "BluetoothService";
	public static final boolean DEBUG = true;
	
	private static final UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	public static final int STATE_NONE = 0;       // Estado inicial
    public static final int STATE_LISTEN = 1;     // A la espera de nuevas conexiones
    public static final int STATE_CONNECTING = 2; // Iniciando una conexión al exterior
    public static final int STATE_CONNECTED = 3;  // Conectado a un dispositivo
	
	private AcceptThread acceptT;
	private ConnectThread connectT;
	private ConnectedThread connectedT;
	
	private int state;
	
	public BluetoothService() {
		super("BluetoothService");
	}
	
	protected void startRunning(Intent intent) {
	}
	
	protected void stopRunning(Intent intent) {
	}
	
	protected void retrieveData(Intent intent) {
	}
	
	protected void getData(Intent intent) {
	}
	
	public void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {
		// TODO Auto-generated method stub
		
	}
	
	public void connectionFailed() {
		// To be done
	}
	
	public void connectionLost() {
		// TODO Auto-generated method stub
		
	}
	
	public void manageReceivedData(int bytes, byte[] buffer) {
		// TODO Auto-generated method stub
		
	}
	
	public int getState() { return state; }
	
	public void setConnectThread(ConnectThread connectT) {
		this.connectT = connectT;
	}
}
