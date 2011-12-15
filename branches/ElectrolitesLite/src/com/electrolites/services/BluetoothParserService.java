package com.electrolites.services;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.electrolites.bluetooth.ConnectThread;
import com.electrolites.bluetooth.ConnectedThread;
import com.electrolites.data.DPoint;
import com.electrolites.ecg.ElectrolitesActivity;
import com.electrolites.util.FixedLinkedList;
import com.electrolites.util.RealTimeFriendlyDataParser;
import com.electrolites.util.SamplePoint;

public class BluetoothParserService extends DataService {
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
	private ConnectedThread connectedT;
	
	private int state;			// Estado del servicio
	private int adapterState;	// Estado del adaptador bluetooth
	
	private Handler handler;
	
	private RealTimeFriendlyDataParser dp;
	
	public static FixedLinkedList<Byte> stream;
	
	// Muestras indexadas por no. de muestra
	protected ArrayList<SamplePoint> samples;
	// Puntos resultantes de la delineaci�n, indexados por n�mero de muestra
	protected HashMap<Integer, DPoint> dpoints;
	// Valores del ritmo card�aco, indexados seg�n el n�mero de muestra anterior a su recepci�n
	protected HashMap<Integer, Short> hbrs;
	// Primera muestra dibujable
	protected int offset;
	
	
	public BluetoothParserService() {
		super("BPARSERSERVIUCE!");
		
		samples = new ArrayList<SamplePoint>();
		dpoints = new HashMap<Integer, DPoint>();
		hbrs = new HashMap<Integer, Short>();
		
		stream = new FixedLinkedList<Byte>(0);
		stream.add((byte) 5);
		
		handler = data.handler;
		dp = new RealTimeFriendlyDataParser();
		dp.setData(data);
		dp.setStream(stream);
		
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
		start();		
		
		// Si estamos realizando descubrimiento, paramos
        if (bA.isDiscovering()) {
        	bA.cancelDiscovery();
        }
		
        // Preparamos para el descubrimiento de dispositivos, por si fuese necesario
		newDevicesArrayAdapter = new ArrayList<BluetoothDevice>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        
		// Recuperamos el dispositivo al que queremos conectarnos
		String deviceName = intent.getStringExtra("deviceName");
		BluetoothDevice bD = getBluetoothDevice(deviceName);

		bA.cancelDiscovery();
		
		if (bD != null) {
			connect(bD);	// Conectamos con el dispositivo
		} else
			System.err.println("No se ha encontrado o no se ha podido establecer conexi�n con el dispositivo:" + deviceName);
	}

	@Override
	protected void stopRunning(Intent intent) {
		//stop();
		// Puede que haga falta hacer m�s cosas
	}
	
	@Override
	protected void retrieveData(Intent intent) {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(receiver, filter);
		
		while (true) {
			synchronized(BluetoothParserService.stream) {
				for (int i = 0; i < 25; i++) {
					if (BluetoothParserService.stream.size() > 0) {
						dp.step();
					}
				}
			}
			try {
				synchronized (data.dynamicData.mutex) {
					if (data.dynamicData.stop) 
						break;
				}
				synchronized (this) {
					wait(4);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dp.finish();
		stop();
	}
	
    private synchronized void setState(int state) {
        if (DEBUG) Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(ElectrolitesActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
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

        setState(STATE_LISTEN);
	}
	
	private synchronized void connect(BluetoothDevice bD) {
		if (BluetoothService.DEBUG) 
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

        // Cancelamos el thread que ha completado el proceso de conexi�n
        if (connectT != null) { 
        	connectT.cancel(); 
        	connectT = null;
        }

        // Cancelamos cualquier thread que está llevando a cabo una conexión
        if (connectedT != null) {
        	connectedT.cancel(); 
        	connectedT = null;
        }
        
        // Iniciamos el thread que maneja la conexión
        connectedT = new ConnectedThread(this, socket);
        connectedT.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(ElectrolitesActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.DEVICE_NAME, remoteDevice.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);
        
        setState(STATE_CONNECTED);
      
		// Hay que mandar un token al Shimmer para que este comience su transmisi�n
		byte[] startToken = { (byte) 0xc0 };
		write(startToken);
		
		Intent i = new Intent(getApplication(), BluetoothParserService.class);
		i.setAction(DataService.RETRIEVE_DATA);
		getApplication().startService(i);
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
	/*public void read(int bytes, byte[] buffer) {
		if (bytes > 0) {
			//if (DEBUG)
			//	Log.d(TAG, "Datos recibidos" + buffer);
			
			//Toast.makeText(getApplication(), "Llegaron datos: " + b, Toast.LENGTH_SHORT).show();
			for (int i = 0; i < bytes; i++) {
				//synchronized (this) {
					// A�adimos el byte al vector de datos le�dos
					//dp.addToStream(buffer[i]);
					//dp.setP2(dp.getP2()+1);
					stream.add(buffer[i]);
			//	}
			}
		}
		
		// Deja que DataParser haga su tarea y mande sus resultados a data
		parseData();
	}*/
	
	/*private void parseData() {
		samples = new ArrayList<SamplePoint>();
		// Lee el flujo de bytes y parsea
		dp.readStreamDynamic(samples, dpoints, hbrs, offset);
		
		// Manda los resultados a data y vac�a las estructuras de DataService
		/*synchronized (data.dynamicData) {
			data.dynamicData.addSamples(samples);
		}* /
		//samples = new ArrayList<SamplePoint>();
		//data.dpoints.putAll(dpoints);
		dpoints = new HashMap<Integer, DPoint>();
		//data.hbrs.putAll(hbrs);
		hbrs = new HashMap<Integer, Short>();
		
		if (offset != -1) {
			synchronized (this) {
				data.offset = offset;
			}
			offset = -1;
		}
	}*/
	
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

        setState(STATE_NONE);
	}
	
	// Indica que el intento de conexión ha fallado
	public void connectionFailed() {
		Log.e(BluetoothService.TAG, "El intento de conexión ha fallado. Reintentando...");
		// Deberíamos avisar a la activity, no?
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(ElectrolitesActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.TOAST, "El intento de conexión ha fallado.");
        msg.setData(bundle);
        handler.sendMessage(msg);
		// Nos volvemos a poner a la escucha
		//this.start();
        //Cerramos el servicio para poder ejecutarlo de nuevo
        setState(STATE_NONE);
        stopSelf();
	}
	
	public void connectionLost() {
		Log.e(BluetoothService.TAG, "Se ha perdido la conexión. Reiniciando...");
		// Deberíamos avisar a la activity, no?
        Message msg = handler.obtainMessage(ElectrolitesActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ElectrolitesActivity.TOAST, "Se ha perdido la conexión");
        msg.setData(bundle);
        handler.sendMessage(msg);
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
	
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
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
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(receiver);
    }
	
	public int getState() { return state; }
	
	public int getAdapterState() { return adapterState; }
	
	public void setConnectThread(ConnectThread connectT) {
		this.connectT = connectT;
	}
}
