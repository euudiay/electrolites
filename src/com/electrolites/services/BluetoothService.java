package com.electrolites.services;

import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.electrolites.bluetooth.AcceptThread;
import com.electrolites.bluetooth.ConnectThread;
import com.electrolites.bluetooth.ConnectedThread;
import com.electrolites.ecg.ElectrolitesActivity;

public class BluetoothService extends DataService {
	public static final String TAG = "BluetoothService";
	public static final boolean DEBUG = true;
	
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public final static int BT_DISABLED = 0;
	public final static int BT_ENABLED = 1;
	public final static int BT_UNAVAILABLE = -1;
	  
	public static final int STATE_NONE = 0;       // Estado inicial
    public static final int STATE_LISTEN = 1;     // A la espera de nuevas conexiones
    public static final int STATE_CONNECTING = 2; // Iniciando una conexiï¿½n al exterior
    public static final int STATE_CONNECTED = 3;  // Conectado a un dispositivo
    
    private final BluetoothAdapter bA;
    
    private ArrayList<BluetoothDevice> bondedDevices;
    private ArrayList<BluetoothDevice> newDevicesArrayAdapter;
	
	private AcceptThread acceptT;
	private ConnectThread connectT;
	private ConnectedThread connectedT;
	
	private int state;			// Estado del servicio
	private int adapterState;	// Estado del adaptador bluetooth
	
	private Handler mHandler;
	
	public BluetoothService() {
		super("BluetoothService");
		
		mHandler = d.mHandler;
		
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
	
	@Override
	protected void startRunning(Intent intent) {
		// Al activar el servicio, el intent que lo hace deberï¿½ llevar el nombre 
		// del dispositivo al que queremos conectarnos (que previamente deberï¿½ 
		// estar sincronizado). Al menos de momento, mï¿½s tarde implementaremos el
		// descubrimiento de dispositivos.
		
		//Creamos el thread de aceptación, por si alguien intenta conectarse con nosotros
		start();		
		
		newDevicesArrayAdapter = new ArrayList<BluetoothDevice>();
		
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // If we're already discovering, stop it
        if (bA.isDiscovering()) {
        	bA.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bA.startDiscovery();
        
		String deviceName = intent.getStringExtra("deviceName");
		BluetoothDevice bD = getBluetoothDevice(deviceName);

		bA.cancelDiscovery();
		
		if (bD != null) {
			connect(bD);	// Conectamos con el dispositivo
		} else
			System.err.println("No se ha encontrado o no se ha podido establecer conexiï¿½n con el dispositivo:" + deviceName);
	}

	@Override
	protected void stopRunning(Intent intent) {
		stop();
		// Puede que haga falta hacer mï¿½s cosas
	}
	
	@Override
	protected void retrieveData(Intent intent) {
		if (state == STATE_CONNECTED) {
			// Hay que mandar cosas al Shimmer para que este comience su transmisiï¿½n
			byte[] startToken = { (byte) 0xc0 };
			write(startToken);
			// Y ahora hay que recuperar y tratar todo lo que nos llegue
		}
	}
	
	@Override
	protected void getData(Intent intent) {
	}
	
    private synchronized void setState(int state) {
        if (DEBUG) Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(ElectrolitesActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
	
	private synchronized void start() {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "BluetoothService.start()");

        // Cancelamos cualquier intento de conexiï¿½n pendiente
        if (connectT != null) {
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier conexiï¿½n en ejecuciï¿½n
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        setState(STATE_LISTEN);

        // Ponemos el ServerSocket a la escucha
        if (acceptT == null) {
        	acceptT = new AcceptThread(bA, this, uuid);
        	acceptT.start();
        }
	}
	
	private synchronized void connect(BluetoothDevice bD) {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "Iniciando conexiï¿½n a: " + bD);

        // Cancelamos cualquier intento pendiente de conexiï¿½n
        if (state == STATE_CONNECTING) {
            if (connectT != null) {
            	connectT.cancel(); 
            	connectT = null;
            }
        }

        // Cancelamos cualquier conexiï¿½n actual
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        // Iniciamos el thread para conectar con el dispositivo
        connectT = new ConnectThread(bA, this, bD, uuid);
        connectT.start();
        setState(STATE_CONNECTING);
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {
		if (BluetoothService.DEBUG) 
			Log.d(TAG, "Socket conectado.");

        // Cancelamos el thread que ha completado el proceso de conexiï¿½n
        if (connectT != null) { 
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier thread que estï¿½ llevando a cabo una conexiï¿½n
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }

        // Cancelamos el thread de aceptaciï¿½n (de momento sï¿½lo queremos un dispositivo conectado)
        if (acceptT != null) {
            acceptT.cancel();
            acceptT = null;
        }

        // Iniciamos el thread que maneja la conexiï¿½n
        connectedT = new ConnectedThread(this, socket);
        connectedT.start();

     // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(ElectrolitesActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.DEVICE_NAME, remoteDevice.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        
        setState(STATE_CONNECTED);
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
        // Realizamos la escritura de manera asï¿½ncrona
        r.write(bytes);
	}
	
	// Hacemos cosas con lo que nos ha llegado
	public void read(int bytes, byte[] buffer) {
		if (bytes > 0) {
			if (DEBUG)
				Log.d(TAG, "Datos recibidos" + buffer);
			short b = buffer[0];
			//Toast.makeText(getApplication(), "Llegaron datos: " + b, Toast.LENGTH_SHORT).show();
			synchronized (this) {
				// Y hacemos mï¿½s cosas, como guardarlas en Data
				d.samples.add(b);
			}
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

        setState(STATE_NONE);
	}
	
	// Indica que el intento de conexión ha fallado
	public void connectionFailed() {
		Log.e(BluetoothService.TAG, "El intento de conexión ha fallado. Reintentando...");
		// Deberï¿½amos avisar a la activity, no?
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(ElectrolitesActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.TOAST, "El intento de conexión ha fallado");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
		// Nos volvemos a poner a la escucha
		//this.start();
        //Cerramos el servicio para poder ejecutarlo de nuevo
        setState(STATE_NONE);
        stopSelf();
	}
	
	public void connectionLost() {
		Log.e(BluetoothService.TAG, "Se ha perdido la conexiï¿½n. Reiniciando...");
		// Deberï¿½amos avisar a la activity, no?
        Message msg = mHandler.obtainMessage(ElectrolitesActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.TOAST, "Se ha perdido la conexión");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
		// Nos volvemos a poner a la escucha
		//this.start();
        //Cerramos el servicio para poder ejecutarlo de nuevo
        setState(STATE_NONE);
        stopSelf();
	}
	
	private int getBondedDevices() {
		if (adapterState == BT_ENABLED) {
			bondedDevices = new ArrayList<BluetoothDevice>(bA.getBondedDevices());
			return bondedDevices.size();
		} else
			return 0;
	}
	
	private BluetoothDevice getBluetoothDevice(String deviceName) {
		// De momento sï¿½lo buscamos entre los dispositivos sincronizados
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
			{
				return bD;
			}
			else{
				boolean found1 = false;
				int j = 0;
				bD = null;
				while (!found1 && j < newDevicesArrayAdapter.size()) {
					bD = newDevicesArrayAdapter.get(j);
					found1 = bD.getName().equals(deviceName);
					j++;
				}
				if (found1)
					return bD;
				else
					return null;
			}
		}
		else
			return null;
	}
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesArrayAdapter.add(device);
                }
            }
        }
    };
	
	public int getState() { return state; }
	
	public int getAdapterState() { return adapterState; }
	
	public void setConnectThread(ConnectThread connectT) {
		this.connectT = connectT;
	}
}
