package org.microlites.data.usb;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.microlites.data.DataHolder;
import org.microlites.data.DataSourceThread;
import org.microlites.util.RealTimeDataParser;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.util.Log;

public class UsbComThread extends DataSourceThread {
	public final static String TAG = "UsbComThread";
	
	public boolean stop;
	
	// Test land of awesomeness
	private RealTimeDataParser parser;
	
	private UsbEndpoint endpointRead = null;
	private UsbEndpoint endpointWrite = null;
	private UsbDeviceConnection connection = null;
	
	private int bufferDataLength;
	
	public Handler handler;
	
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
		
		bufferDataLength = endpointRead.getMaxPacketSize()*2;
		ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
		UsbRequest request = new UsbRequest();
		
		byte startToken = (byte) 0xC0;
		
		request.initialize(connection, endpointRead);

		// Mandamos el token para empezar a recibir
		/*while (true) {
			//Tal y como est?? si falla se queda ciclando por lo que esto es un poco inutil
			//pero si cambiamos el writte esto es necesario as?? que lo mantengo
			if (!write(startToken)){
				Log.w(TAG, "Start token could not be delivered.");
				return;
			}
			break;
		}*/
		
		byte seqNum = 0x0;
		int maxSeqNum = 256;
		
		// Nos ponemos a la escucha
		try{
			byte[] byteBuffer = new byte[bufferDataLength];
			short actualBytes = -1;
			//char currentChar = 0x40;
			while (!stop) {
				//Parte nueva para leer si eres host
				if (request.queue(buffer, bufferDataLength) && request.equals(connection.requestWait())) {
					try {
						buffer.get(byteBuffer, 0, bufferDataLength);
					} catch (BufferUnderflowException e) {
						e.printStackTrace();
					}//
			
					if (bufferDataLength > 2) {
						
						actualBytes = byteBuffer[1];
						
						// Received data
						str = "";
						for (int i = 0; i < actualBytes + 2; i++)
							str += Integer.toString((buffer.array()[i] & 0xff), 16) + " ";
						str.substring(0, str.length()-1);
						System.out.println(str);
						
						// Correct package?
						/*if (byteBuffer[0] != 0x63) {
							System.err.println("Incorrect usb data package received. Expected 63 got " + buffer.array()[0]);
							buffer.clear();
							continue;
						}*/
						// Number of bytes received?
						
						// Checks here
						
						// To dataparser!
						int start = 2;
						if (byteBuffer[1] == 62) {
							start = 3;
							if (seqNum+1 != byteBuffer[2]) {
								Log.e("SEQ", "Wrong Seq. Num: Got " + Integer.toString(byteBuffer[2], 16) + " expected " + Integer.toString((seqNum+1), 16));
								seqNum = byteBuffer[2];
							} else 
								seqNum = (byte) ((seqNum + 1) % maxSeqNum);
						}
						for (int i = start; i < actualBytes + 2; i++)
						{
							/*if (actualBytes == 29 && i == 30) {
								//System.out.println("OHPORELAMORDEDIOSHEMOSRECIBIDOELTOKEN29DELQUEHABLANLASSAGRADASESCRITURAS");
								System.out.println("SeqNum: " + byteBuffer[i]);
							//	System.out.println("OHPORELAMORDEDIOSHEMOSRECIBIDOELTOKEN29DELQUEHABLANLASSAGRADASESCRITURAS");
							} else {*/
								/*currentChar++;
								if (currentChar > 0x5A)
									currentChar = 0x40;
								
								System.out.println(currentChar);
								
								if (currentChar != byteBuffer[i]) {
									System.err.println("Got " + (char) byteBuffer[i] + "("+byteBuffer[i]+") expected " + currentChar + "("+(int)currentChar+")");
									currentChar = (char) byteBuffer[i];
								}
							//}*/
								parser.step(byteBuffer[i]);
						}
					} else {
						System.out.println("Recepción de paquete USB no finalizada, pero continuamos");
					}
					buffer.clear();
				}
			}
			
		}catch (Exception ex){
			Log.w(TAG, "Algo ha pasado durante la transmisión");
			ex.printStackTrace();
		} finally {
			System.out.println("Cerrando conexión USB...");
			try	{
				request.cancel();
				request.close();
				System.out.println("Cerrada!");
			} catch (Exception ex){
				Log.w(TAG, "Algo ha pasado al cerrar la transmisión");
				ex.printStackTrace();
			}
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
		} finally {
			request.cancel();
			request.close();
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