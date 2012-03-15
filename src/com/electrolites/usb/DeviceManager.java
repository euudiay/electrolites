package com.electrolites.usb;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;


public class DeviceManager {

	public final static String TAG = "DeviceManager";
	public static final String ACTION_USB_PERMISSION = "com.electrolites.ecg.USB_PERMISSION";
	
	private Activity activity;	// Referencia a la actividad que la invoca
	private Context context;
	private BroadcastReceiver usbReceiver;
	
	private UsbManager usbManager;
	
	private UsbInterface interf = null;
	private UsbEndpoint endpointRead = null;
	private UsbEndpoint endpointWrite = null;
	private UsbDeviceConnection connection = null;

	private boolean forceClaim = true;
	
	private UsbComThread thread;	// Hilo que se encargará de la comunicación con el accesorio
	
	public DeviceManager(Activity activity) {
		this.activity = activity;
		context = activity.getApplicationContext();
		usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
	}
	
	public void start(BroadcastReceiver usbReceiver) {
		this.usbReceiver = usbReceiver;
		
		PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        activity.registerReceiver(usbReceiver, filter);
        
        UsbDevice device = null;
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();
        }
        
        if (device != null)
        	usbManager.requestPermission(device, permissionIntent);
        else 
        	Toast.makeText(context, "No devices attached.", Toast.LENGTH_SHORT).show();
	}
	
	public void stop() {
		// Terminamos la actividad del thread de comunicación
		if (thread != null)
			thread.halt();
		
		// Cerramos el descriptor de archivo del accesorio
		if (connection != null){
			connection.close();
			connection.releaseInterface(interf);
		}
		
		activity.unregisterReceiver(usbReceiver);
	}
	
	public void openDevice(UsbDevice device) {
        if (device.getInterfaceCount() != 1) {
        	Log.w(TAG, "could not find interface.");
            return;
        }
		interf = device.getInterface(0);
		
		if (interf.getEndpointCount() < 2) {
			Log.w(TAG, "could not find the endpoints.");
	        return;
	    }
		
		UsbEndpoint endpoint1 = interf.getEndpoint(0);
		UsbEndpoint endpoint2 = interf.getEndpoint(1);
			
		if (endpoint1.getType() == UsbConstants.USB_ENDPOINT_XFER_INT){
			if (endpoint1.getDirection() == UsbConstants.USB_DIR_IN){
				endpointRead = endpoint1;
			}
			else if (endpoint1.getDirection() == UsbConstants.USB_DIR_OUT){
				endpointWrite = endpoint1;
			}
		}
		if (endpoint2.getType() == UsbConstants.USB_ENDPOINT_XFER_INT){
			if (endpoint2.getDirection() == UsbConstants.USB_DIR_IN){
				endpointRead = endpoint2;
			}
			else if (endpoint2.getDirection() == UsbConstants.USB_DIR_OUT){
				endpointWrite = endpoint2;
			}
		}
		if ((endpointRead == null) || (endpointWrite == null)){
			Log.w(TAG, "endpoint is not interrupt type");
			return;
		}
		
		
		connection = usbManager.openDevice(device); 
		//connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT); //do in another thread
		
	    if (connection != null) {		    

			connection.claimInterface(interf, forceClaim);
	        thread = new UsbComThread(interf, endpointRead, endpointWrite, connection);
	        thread.start();
	    }
	}
}



