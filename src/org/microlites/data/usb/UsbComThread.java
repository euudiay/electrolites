package org.microlites.data.usb;

import java.nio.ByteBuffer;

import org.microlites.data.DataHolder;
import org.microlites.data.DataSourceThread;
import org.microlites.util.RealTimeDataParser;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

public class UsbComThread extends DataSourceThread {
	public final static String TAG = "UsbComThread";
	
	private boolean stop;
	
	// Test land of awesomeness
	private RealTimeDataParser parser;
	
	private UsbEndpoint endpointRead = null;
	private UsbEndpoint endpointWrite = null;
	private UsbDeviceConnection connection = null;
	
	private int bufferDataLength;
	
	public UsbComThread(UsbInterface interf, UsbEndpoint endpointIn, UsbEndpoint endpointOut, UsbDeviceConnection connection, DataHolder holder) {

		this.connection = connection;
		//teniendo la conexi??n y los endpoints la interfaz creo que no nos har??a falta guardarla aqui
		// this.interf = interf;
		this.endpointRead = endpointIn;
		this.endpointWrite = endpointOut;
		
		//Esto, o sobra aqu??, o nos guardamos los tama??os de los dos en vez de uno solo
		//Y quitamos el calculo del run y del write
		bufferDataLength = endpointWrite.getMaxPacketSize();
		
		parser = new RealTimeDataParser(holder);
	}
	
	@Override
	public void run() {

		stop = false;
		String str;
		
		bufferDataLength = endpointRead.getMaxPacketSize();
		ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
		UsbRequest request = new UsbRequest();
		
		byte startToken = (byte) 0xC0;
		
		request.initialize(connection, endpointRead);

		// Mandamos el token para empezar a recibir
		while (true) {
			//Tal y como est?? si falla se queda ciclando por lo que esto es un poco inutil
			//pero si cambiamos el writte esto es necesario as?? que lo mantengo
			if (!write(startToken)){
				Log.w(TAG, "Start token could not be delivered.");
				return;
			}
			break;
		}
		
		// Nos ponemos a la escucha
		try{
			while (!stop) {
				//Parte nueva para leer si eres host
				if (request.queue(buffer, bufferDataLength) && request.equals(connection.requestWait()))  {
					
					byte[] byteBuffer = new byte[bufferDataLength];
					buffer.get(byteBuffer, 0, bufferDataLength);
			
					if (bufferDataLength > 0) {
						// Show data
						str = "";
						for (int i = 0; i < bufferDataLength; i++)
							str += (buffer.array()[i] & 0xff) + "_";
						str.substring(0, str.length()-1);
						System.out.println(str);
						
						// To dataparser!
						for (int i = 0; i < bufferDataLength; i++)
						{
							//Apa??o para poder ver por pantalla algo coherente y comprobar si funcionaba bien
							parser.step((byte) 0xda);
							parser.step((byte) 0x00);
							parser.step(buffer.array()[i]);
						}
					}
					buffer.clear();
				}
			}
		}catch (Exception ex){
			Log.w(TAG, "Algo a pasado durante la transmisi???n");
		}
		try	{
			 request.cancel();
			 request.close();
		}catch (Exception ex){
			Log.w(TAG, "Algo a pasado al cerrar la transmisi???n");
		}
			
	}
	
	private boolean write(byte data){
		//Esta es la manera estandar de mandar datos asincronamente, aunque por supuesto hay otras
		bufferDataLength = endpointWrite.getMaxPacketSize();
		ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
		UsbRequest request = new UsbRequest();

		buffer.put(data);

		request.initialize(connection, endpointWrite);
		try
		{
			//En teor??a al hacerlo de esta manera si el request falla, deber??a devolver falso y nos evitariamos
			//hacer una llamada al Wait que se quedar??a bloqueada para siempre, pero en la pr??ctica, 
			//hay veces que el request devuelve true y a??n as?? el requestWait se queda bloqueado
			if (request.queue(buffer, bufferDataLength) && request.equals(connection.requestWait())){
				return true;
			}
			return false;
		}
		catch (Exception ex){
		 // An exception has occured
			return false;
		}
	}

	
	public void halt() { stop = true; }

	/** DataSourceThread implementation **/
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSending() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}
}	