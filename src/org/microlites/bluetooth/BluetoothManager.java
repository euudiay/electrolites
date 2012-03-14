package org.microlites.bluetooth;

import java.util.ArrayList;
import java.util.UUID;

import org.microlites.view.FullDynamicThread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothManager {
	public static final String TAG = "BluetoothService";
	public static final boolean DEBUG = true;
	
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public final static int BT_DISABLED = 0;
	public final static int BT_ENABLED = 1;
	public final static int BT_UNAVAILABLE = -1;
	  
	public static final int STATE_NONE = 0;       // Estado inicial
    public static final int STATE_LISTEN = 1;     // A la espera de nuevas conexiones
    public static final int STATE_CONNECTING = 2; // Iniciando una conexi�n al exterior
    public static final int STATE_CONNECTED = 3;  // Conectado a un dispositivo
    
    private final BluetoothAdapter bA;
    
    private ArrayList<BluetoothDevice> bondedDevices;
    private ArrayList<BluetoothDevice> newDevicesArrayAdapter;
	
	private ConnectThread connectT;
	private FullDynamicThread connectedT;
	
	private int state;			// Estado del servicio
	private int adapterState;	// Estado del adaptador bluetooth
	
	public BluetoothManager() {
		bA = BluetoothAdapter.getDefaultAdapter();
		if (bA == null) {
			System.err.println("No hay adaptador bluetooth.");
			adapterState = BT_UNAVAILABLE;
		} else
			if (!bA.isEnabled()) {
				adapterState = BT_DISABLED;
				System.err.println("El adaptador bluetooth se encuentra desactivado.");
			}
			else {
				adapterState = BT_ENABLED;
		        // Register for broadcasts when a device is discovered
			}
	}

	public void startRunning(String deviceName, FullDynamicThread thread) {
		if (DEBUG) 
			Log.d(TAG, "BluetoothService.start()");
		
		connectedT = thread;

        // Cancelamos cualquier intento de conexi�n pendiente
        if (connectT != null) {
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier conexi�n en ejecuci�n
        if (connectedT != null) {
        	connectedT.cancel(); 
        }

        setState(STATE_LISTEN);
        
     // Si estamos realizando descubrimiento, paramos
        if (bA.isDiscovering()) {
        	bA.cancelDiscovery();
        }
		
        // Preparamos para el descubrimiento de dispositivos, por si fuese necesario
		newDevicesArrayAdapter = new ArrayList<BluetoothDevice>();
        
		// Recuperamos el dispositivo al que queremos conectarnos
		BluetoothDevice bD = getBluetoothDevice(deviceName);

		bA.cancelDiscovery();
		
		if (bD != null) {
			connect(bD);	// Conectamos con el dispositivo
		} else
			System.err.println("No se ha encontrado o no se ha podido establecer conexi�n con el dispositivo:" + deviceName);
	}
	
	public void stopRunning() {
		if (connectT != null) {
			connectT.cancel();
			try {
				connectT.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (connectedT != null) {
			connectedT.cancel();
			connectedT.finish();
			try {
				connectedT.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void connect(BluetoothDevice bD) {
		if (DEBUG)
			Log.d(TAG, "Iniciando conexión a: " + bD);

        // Cancelamos cualquier intento pendiente de conexión
        if (state == STATE_CONNECTING) {
            if (connectT != null) {
            	connectT.cancel(); 
            	connectT = null;
            }
        }

        // Cancelamos cualquier conexi�n actual
        if (connectedT != null) {
        	connectedT.cancel(); 
        	// connectedT = null;
        }

        // Iniciamos el thread para conectar con el dispositivo 
        connectT = new ConnectThread(bA, this, bD, uuid);
        connectT.start();
        setState(STATE_CONNECTING);
	}
	
	// Indica que el intento de conexión ha fallado
	public void connectionFailed() {
		Log.e(TAG, "El intento de conexión ha fallado. Reintentando...");
        setState(STATE_NONE);
	}
	
	public void connectionLost() {
		Log.e(TAG, "Se ha perdido la conexión.");
        setState(STATE_NONE);
	}
	
	private int getBondedDevices() {
		if (adapterState == BT_ENABLED) {
			bondedDevices = new ArrayList<BluetoothDevice>(bA.getBondedDevices());
			return bondedDevices.size();
		} else
			return 0;
	}
	
	public BluetoothDevice getDeviceFromDiscovery(String deviceName) {
		boolean found = false;
		int j = 0;
		BluetoothDevice bD = null;

        // Pedimos al BluetoothAdapter que busque nuevos dispositivos.
        bA.startDiscovery();

        // Posiblemente haya que esperar un tiempo para que el discovery mande sus intents
        
		while (!found && j < newDevicesArrayAdapter.size()) {
			bD = newDevicesArrayAdapter.get(j);
			found = bD.getName().equals(deviceName);
			j++;
		}
		
		if (found)
			return bD;
		else
			return null;

	}
	
	private BluetoothDevice getBluetoothDevice(String deviceName) {
		// De momento s�lo buscamos entre los dispositivos sincronizados
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
			
			// Si lo hemos encontrado entre los sincronizados, hemos terminado
			if (found) {
				return bD;
			}
			// Si no, intentamos descubrirlo
			else
				return getDeviceFromDiscovery(deviceName);
		}
		// Si no hay dispositivos sincronizados, intentamos buscarlo
		else
			return getDeviceFromDiscovery(deviceName);
	}
	
	private synchronized void setState(int state) {
        if (DEBUG) Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        // Give the new state to the Handler so the UI Activity can update
        // handler.obtainMessage(ElectrolitesActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
	
	public int getState() { return state; }
	
	public int getAdapterState() { return adapterState; }
	
	public void setConnectThread(ConnectThread connectT) {
		this.connectT = connectT;
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {
		if (DEBUG) 
			Log.d(TAG, "Socket conectado.");

        // Cancelamos el thread que ha completado el proceso de conexi�n
        if (connectT != null) { 
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier thread que está llevando a cabo una conexión
        if (connectedT != null) {
        	connectedT.cancel(); 
        	// connectedT = null;
        }
        
        // Iniciamos el thread que maneja la conexión
        // connectedT = new FullDynamicThread(this, socket);
        connectedT.setBluetoothSocket(socket);
        // connectedT.start()

        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
      
		// Hay que mandar un token al Shimmer para que este comience su transmisi�n
		byte[] startToken = { (byte) 0xc0 };
		write(startToken);
	}
	
	private void write(byte[] bytes) {
		// Creamos un objeto temporal para realizar la escritura
		FullDynamicThread r;
        // Sincronizamos una copia del connectedT
        synchronized (this) {
            if (state != STATE_CONNECTED) 
            	return;
            r = connectedT;
        }
        // Realizamos la escritura de manera as�ncrona
        r.write(bytes);
	}
}
