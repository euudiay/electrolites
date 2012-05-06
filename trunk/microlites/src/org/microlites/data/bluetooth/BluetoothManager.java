package org.microlites.data.bluetooth;

import java.util.ArrayList;
import java.util.UUID;

import org.microlites.MicrolitesActivity;
import org.microlites.R;
import org.microlites.data.Data;
import org.microlites.data.DataHolder;
import org.microlites.data.DataManager;
import org.microlites.data.DataSourceThread;
import org.microlites.util.ColorPickerDialog;
import org.microlites.util.ColorPickerDialog.OnColorChangedListener;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class BluetoothManager implements DataManager {
	
	public static final String TAG = "BluetoothService";		// Logging TAG
	public static final boolean DEBUG = true;					// Debug Mode On/Off
	
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	// System BT Service State Constants
	public final static int BT_DISABLED = 0;					// Disabled
	public final static int BT_ENABLED = 1;						// Enabled
	public final static int BT_UNAVAILABLE = -1;				// Unavailable
	  
	// Bluetooth Manager State Constants
	public static final int STATE_NONE = 0;       				// No connection requested
    public static final int STATE_LISTEN = 1;     				// Awaiting connections
    public static final int STATE_CONNECTING = 2; 				// Trying to connect to a device
    public static final int STATE_CONNECTED = 3;  				// Connected to a device
    
    private final BluetoothAdapter bA;							// BT Adapter reference
    private BluetoothDevice bD;									// BT Device refence
    
    private ArrayList<BluetoothDevice> bondedDevices;			// Collection of bonded bt devices
    private ArrayList<BluetoothDevice> newDevicesArrayAdapter;	// Collection of new bt devices	
	
	private ConnectThread connectT;								// Connection handling thread 
	private ConnectedThread connectedT;							// Thread run after successful connection
	private DataHolder dataHolder;								// Received data holder reference
	
	private int state;											// Bluetooth Service State
	private int adapterState;									// Bluetooth Adapter State
	
	private String deviceName;									// Connect to this device
	
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
		
		connectT = null;
		connectedT = null;
		dataHolder = null;
		
		deviceName = "FireFly-3781";
		
		buildMenu();
	}

	public void startRunning(String deviceName, DataHolder thread) {
		if (DEBUG) 
			Log.d(TAG, "BluetoothService.start()");
		
		dataHolder = thread;

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
        
        // TODO: Control bA == null
        
     // Si estamos realizando descubrimiento, paramos
        if (bA.isDiscovering()) {
        	bA.cancelDiscovery();
        }
		
        // Preparamos para el descubrimiento de dispositivos, por si fuese necesario
		newDevicesArrayAdapter = new ArrayList<BluetoothDevice>();
        
		// Recuperamos el dispositivo al que queremos conectarnos
		bD = getBluetoothDevice(deviceName);

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
        	connectedT = null;
        }

        // Iniciamos el thread para conectar con el dispositivo 
        connectT = new ConnectThread(bA, this, bD, uuid);
        connectT.start();
        setState(STATE_CONNECTING);
	}
	
	// Indica que el intento de conexión ha fallado
	public void connectionFailed() {
		Log.e(TAG, "El intento de conexión ha fallado.");
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
        connectedT = new ConnectedThread(dataHolder, socket);
        connectedT.start();

        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
      
		// Hay que mandar un token al Shimmer para que este comience su transmisi�n
		byte[] startToken = { (byte) 0xc0 };
		write(startToken);
	}
	
	private void write(byte[] bytes) {
		// Creamos un objeto temporal para realizar la escritura
		DataSourceThread r;
        // Sincronizamos una copia del connectedT
        synchronized (this) {
            if (state != STATE_CONNECTED) 
            	return;
            r = connectedT;
        }
        // Realizamos la escritura de manera as�ncrona
        r.write(bytes);
	}

	//@Override
	public void configure(DataHolder dataHolder) {
		this.dataHolder = dataHolder;
	}

	//@Override
	public void start() {
		startRunning(deviceName, this.dataHolder);
	}

	//@Override
	public void stop() {
		stopRunning();
		MicrolitesActivity.instance.popView();
	}
	
	/** Builds Bluetooth Configuration Menu and pushes it into the View Stack
	*/ 
	private void buildMenu() {
		MicrolitesActivity act = MicrolitesActivity.instance; 
		LayoutInflater li = act.getLayoutInflater();
		View view = li.inflate(R.layout.btconfiglayout, null);
		
		/*LinearLayout l = (LinearLayout) view.findViewById(R.id.viewOptionsPanel);
		l.addView(li.inflate(R.layout.viewconfiglayout, null));
		
		Spinner sp = (Spinner) l.findViewById(R.id.themeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            act.getApplicationContext(), R.array.colorThemes, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    sp.setAdapter(adapter);*/
		
		Button vs = (Button) view.findViewById(R.id.btSettingsViewConfig);
		vs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(MicrolitesActivity.instance);

				dialog.setContentView(R.layout.viewconfiglayout);
				dialog.setTitle("Configuración de Vista");
				
				Spinner sp = (Spinner) dialog.findViewById(R.id.themeSpinner);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
			            MicrolitesActivity.instance.getApplicationContext(), R.array.colorThemes, android.R.layout.simple_spinner_item);
			    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			    sp.setAdapter(adapter);
			    
			    final Button c1 = (Button) dialog.findViewById(R.id.buttonC1);
			    c1.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						ColorPickerDialog p = new ColorPickerDialog(MicrolitesActivity.instance, new OnColorChangedListener() {
							public void colorChanged(int color) {
								Data.getInstance().color1 = color;
								c1.setBackgroundColor(color);
							}
						}, Data.getInstance().color1);
						
						p.show();
					}
				});
			    
			    final Button c2 = (Button) dialog.findViewById(R.id.buttonC2);
			    c2.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						ColorPickerDialog p = new ColorPickerDialog(MicrolitesActivity.instance, new OnColorChangedListener() {
							public void colorChanged(int color) {
								Data.getInstance().color2 = color;
								c2.setBackgroundColor(color);
							}
						}, Data.getInstance().color2);
						
						p.show();
					}
				});
			    
			    Button b = (Button) dialog.findViewById(R.id.button1);
			    b.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
				dialog.setOwnerActivity(MicrolitesActivity.instance);
				
				dialog.show();
			}
		});
		
		Button b = (Button) view.findViewById(R.id.btSettingsStart);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = MicrolitesActivity.instance.getCurrentView();
				TextView tv = (TextView) view.findViewById(R.id.btDeviceNameTextfield);
				CharSequence cs = tv.getText(); 
				deviceName = cs.toString();
				// TODO: Check name here
				MicrolitesActivity.instance.initVisualization(MicrolitesActivity.MODE_BLUETOOTH, 1, null);
			}
		});
		
		act.pushView(view);
	}
}