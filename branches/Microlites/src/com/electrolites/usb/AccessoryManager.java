package com.electrolites.usb;

import java.io.FileDescriptor;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;


public class AccessoryManager {
	public static final String ACTION_USB_PERMISSION = "com.electrolites.ecg.USB_PERMISSION";
	
	private Activity activity;	// Referencia a la actividad que la invoca
	private Context context;
	private BroadcastReceiver usbReceiver;
	
	private UsbManager usbManager;
	
	private ParcelFileDescriptor pfd = null;
	
	private UsbComThread thread;	// Hilo que se encargará de la comunicación con el accesorio
	
	public AccessoryManager(Activity activity) {
		this.activity = activity;
		context = activity.getApplicationContext();
		usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
	}
	
	public void start(BroadcastReceiver usbReceiver) {
		this.usbReceiver = usbReceiver;
		
		PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        activity.registerReceiver(usbReceiver, filter);
        
        UsbAccessory accessory = null;
        UsbAccessory[] accessoryList = usbManager.getAccessoryList();
        
        if (accessoryList == null) {
        	Toast.makeText(context, "No accessories attached.", Toast.LENGTH_SHORT).show();
        	usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        }
        else
        	accessory = accessoryList[0];
        
        if (accessory != null)
        	usbManager.requestPermission(accessory, permissionIntent);
        else 
        	Toast.makeText(context, "No accessories attached.", Toast.LENGTH_SHORT).show();
	}
	
	public void stop() {
		// Terminamos la actividad del thread de comunicación
		if (thread != null)
			thread.halt();
		
		// Cerramos el descriptor de archivo del accesorio
		if (pfd != null) {
			try {
				pfd.close();
			} catch (IOException e) {
				Toast.makeText(context, "Accessory cannot be closed.", Toast.LENGTH_SHORT);
			}
		}
		
		activity.unregisterReceiver(usbReceiver);
	}
	
	public void openAccessory(UsbAccessory accessory) {
	    pfd= usbManager.openAccessory(accessory);
	    if (pfd != null) {
	        FileDescriptor fd = pfd.getFileDescriptor();
	        
	        thread = new UsbComThread(fd);
	        thread.start();
	    }
	}
}