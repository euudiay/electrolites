package com.electrolites.services;

import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
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
	
	public final static int BT_DISABLED = 0;
	public final static int BT_ENABLED = 1;
	public final static int BT_UNAVAILABLE = -1;

	public static final int STATE_NONE = 0;       // Estado inicial
    public static final int STATE_LISTEN = 1;     // A la espera de nuevas conexiones
    public static final int STATE_CONNECTING = 2; // Iniciando una conexión al exterior
    public static final int STATE_CONNECTED = 3;  // Conectado a un dispositivo
    
    private final BluetoothAdapter bA;
    
    private ArrayList<BluetoothDevice> bondedDevices;
	
	private AcceptThread acceptT;
	private ConnectThread connectT;
	private ConnectedThread connectedT;
	
	private int state;			// Estado del servicio
	private int adapterState;	// Estado del adaptador bluetooth
	
	public BluetoothService() {
		super("BluetoothService");
		
		state = STATE_NONE;
		
		bA = BluetoothAdapter.getDefaultAdapter();
		if (bA == null) {
			System.err.println("No hay adaptador bluetooth.");
			adapterState = BT_UNAVAILABLE;
		} else
			if (!bA.isEnabled()) {
				adapterState = BT_DISABLED;
				System.err.println("El adaptador bluetooth se encuentra desactivado.");
			}
			else
				adapterState = BT_ENABLED;
	}
	
	protected void startRunning(Intent intent) {
		// Al activar el servicio, el intent que lo hace deberá llevar el nombre 
		// del dispositivo al que queremos conectarnos (que previamente deberá 
		// estar sincronizado). Al menos de momento, más tarde implementaremos el
		// descubrimiento de dispositivos.
		String deviceName = intent.getStringExtra("deviceName");
		BluetoothDevice bD = getBluetoothDevice(deviceName);
		
		if (bD != null) {
			start();		// Inicializamos el thread de aceptación
			connect(bD);	// Conectamos con el dispositivo
			acceptT.run();	// Nos ponemos a la escucha
		} else
			System.err.println("No se ha encontrado o no se ha podido establecer conexión con el dispositivo:" + deviceName);
	}

	protected void stopRunning(Intent intent) {
	}
	
	protected void retrieveData(Intent intent) {
	}
	
	protected void getData(Intent intent) {
	}
	
	private synchronized void start() {
		// TODO Auto-generated method stub
		
	}
	
	private synchronized void connect(BluetoothDevice bD) {
		// TODO Auto-generated method stub
		
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {
		// TODO Auto-generated method stub
		
	}
	
	private void write(byte[] bytes) {
		// TODO Auto-generated method stub
		
	}
	
	public void read(int bytes, byte[] buffer) {
		// TODO Auto-generated method stub
		
	}
	
	private synchronized void stop() {
		// To be done
	}
	
	public void connectionFailed() {
		// To be done
	}
	
	public void connectionLost() {
		// TODO Auto-generated method stub
		
	}
	
	private int getBondedDevices() {
		if (adapterState == BT_ENABLED) {
			bondedDevices = new ArrayList<BluetoothDevice>(bA.getBondedDevices());
			return bondedDevices.size();
		} else
			return 0;
	}
	
	private BluetoothDevice getBluetoothDevice(String deviceName) {
		// De momento sólo buscamos entre los dispositivos sincronizados
		int n;
		if ((n = getBondedDevices()) > 0) {
			boolean found = false;
			int i = 0;
			BluetoothDevice bD = null;
			while (!found && i < n) {
				bD = bondedDevices.get(i);
				found = bD.getName().equals(deviceName);
				i++;
			}
			
			if (found)
				return bD;
			else
				return null;
		} else
			return null;
	}
	
	public int getState() { return state; }
	
	public int getAdapterState() { return adapterState; }
	
	public void setConnectThread(ConnectThread connectT) {
		this.connectT = connectT;
	}
}
