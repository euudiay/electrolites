package com.electrolites.services;

import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

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
    public static final int STATE_CONNECTING = 2; // Iniciando una conexi�n al exterior
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
			else {
				adapterState = BT_ENABLED;
			}
	}
	
	protected void startRunning(Intent intent) {
		// Al activar el servicio, el intent que lo hace deber� llevar el nombre 
		// del dispositivo al que queremos conectarnos (que previamente deber� 
		// estar sincronizado). Al menos de momento, m�s tarde implementaremos el
		// descubrimiento de dispositivos.
		String deviceName = intent.getStringExtra("deviceName");
		BluetoothDevice bD = getBluetoothDevice(deviceName);
		
		if (bD != null) {
			start();		// Inicializamos el thread de aceptaci�n
			connect(bD);	// Conectamos con el dispositivo
			acceptT.run();	// Nos ponemos a la escucha
		} else
			System.err.println("No se ha encontrado o no se ha podido establecer conexi�n con el dispositivo:" + deviceName);
	}

	protected void stopRunning(Intent intent) {
		stop();
		// Puede que haga falta hacer m�s cosas
	}
	
	protected void retrieveData(Intent intent) {
		if (state == STATE_CONNECTED) {
			// Hay que mandar cosas al Shimmer para que este comience su transmisi�n
			byte[] startToken = { (byte) 0xc0 };
			write(startToken);
			// Y ahora hay que recuperar y tratar todo lo que nos llegue
		}
	}
	
	protected void getData(Intent intent) {
	}
	
	private synchronized void start() {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "BluetoothService.start()");

        // Cancelamos cualquier intento de conexi�n pendiente
        if (connectT != null) {
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier conexi�n en ejecuci�n
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        state = STATE_LISTEN;

        // Ponemos el ServerSocket a la escucha
        if (acceptT == null) {
        	acceptT = new AcceptThread(bA, this, uuid);
        	acceptT.start();
        }
	}
	
	private synchronized void connect(BluetoothDevice bD) {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "Iniciando conexi�n a: " + bD);

        // Cancelamos cualquier intento pendiente de conexi�n
        if (state == STATE_CONNECTING) {
            if (connectT != null) {
            	connectT.cancel(); 
            	connectT = null;
            }
        }

        // Cancelamos cualquier conexi�n actual
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        // Iniciamos el thread para conectar con el dispositivo
        connectT = new ConnectThread(bA, this, bD, uuid);
        connectT.start();
        state = STATE_CONNECTING;
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "Socket conectado.");

        // Cancelamos el thread que ha completado el proceso de conexi�n
        if (connectT != null) { 
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier thread que est� llevando a cabo una conexi�n
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        // Cancelamos el thread de aceptaci�n (de momento s�lo queremos un dispositivo conectado)
        if (acceptT != null) {
            acceptT.cancel();
            acceptT = null;
        }

        // Iniciamos el thread que maneja la conexi�n
        connectedT = new ConnectedThread(this, socket);
        connectedT.start();

        state = STATE_CONNECTED;
	}
	
	private void write(byte[] bytes) {
		// Creamos un objeto temporal para realizar la escritura
        ConnectedThread r;
        // Sincronizamos una copia del connectedT
        synchronized (this) {
            if (state != STATE_CONNECTED) 
            	return;
            r = connectedT;
        }
        // Realizamos la escritura de manera as�ncrona
        r.write(bytes);
	}
	
	// Hacemos cosas con lo que nos ha llegado
	public void read(int bytes, byte[] buffer) {
		if (bytes > 0) {
			if (DEBUG)
				Log.d(TAG, "Datos recibidos" + buffer);
			short b = buffer[0];
			Toast.makeText(getApplication(), "Llegaron datos: " + buffer, Toast.LENGTH_SHORT).show();
			/*synchronized (this) {
				// Y hacemos m�s cosas, como guardarlas en Data
				d.samples.add(b);
			}*/
		}
	}
	
	private synchronized void stop() {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "BluetoothService.stop()");

        if (connectT != null) {
            connectT.cancel();
            connectT = null;
        }

        if (connectedT != null) {
            connectedT.cancel();
            connectedT = null;
        }

        if (acceptT != null) {
        	acceptT.cancel();
        	acceptT = null;
        }

        state = STATE_NONE;
	}
	
	// Indica que el intento de conexi�n ha fallado
	public void connectionFailed() {
		Log.e(BluetoothService.TAG, "El intento de conexi�n ha fallado. Reintentando...");
		// Deber�amos avisar a la activity, no?
		// Nos volvemos a poner a la escucha
		this.start();
	}
	
	public void connectionLost() {
		Log.e(BluetoothService.TAG, "Se ha perdido la conexi�n. Reiniciando...");
		// Deber�amos avisar a la activity, no?
		// Nos volvemos a poner a la escucha
		this.start();
	}
	
	private int getBondedDevices() {
		if (adapterState == BT_ENABLED) {
			bondedDevices = new ArrayList<BluetoothDevice>(bA.getBondedDevices());
			return bondedDevices.size();
		} else
			return 0;
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
