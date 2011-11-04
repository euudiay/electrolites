package com.electrolites.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import android.R;
import android.content.res.Resources;
import android.os.Environment;

public class FileConverter {
	private ArrayList<Byte> stream;
	
	public FileConverter() {
		stream = new ArrayList<Byte>();
	}
	
	// Lee un archivo de texto y devuelve un vector de datos sin procesar (bytes)
	public ArrayList<Byte> readTxt(String fname) {
		stream.clear();
		
		try {
			Scanner sc = new Scanner(new File(fname));
			
			while(sc.hasNext()) {
				if (sc.hasNextInt()) {
					int aux = sc.nextInt();
					stream.add((byte) aux);
				}
				else
					sc.next();
			}
			
			sc.close();
		} catch (FileNotFoundException e) {
			System.err.println("Archivo no encontrado: " + fname);
			e.printStackTrace();
		}
		
		return stream;
	}
	
	// Lee un archivo binario y devuelve sus datos en un ArrayList de bytes
	public ArrayList<Byte> readBinary(String fname) {
		stream.clear();
		
		File root = Environment.getExternalStorageDirectory();
		File path = new File(root, "/Download/");
		path.mkdirs();
		
		try {
			FileInputStream s = new FileInputStream(new File(path, fname));
			
			int aux = 0;
			while ((aux = s.read()) >= 0)
				stream.add((byte) aux);
			
			s.close();
		}
		catch (FileNotFoundException e) {
			System.err.println("Archivo no encontrado: " + fname);
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return stream;
	}
	
	// Lee el recurso de identificador id y devuelve sus datos en forma de array de bytes
	public ArrayList<Byte> readResources(Resources resources, int id) {
		InputStream input = resources.openRawResource(id);
		stream.clear();
		
		int r = 0;
		try {
			while ((r = input.read()) >= 0) {
				stream.add((byte) r);
			}
			input.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return stream;
	}
	
	// Escribe el vector de datos en un archivo de texto
	public void writeTxt(String fname) {
		if (!stream.isEmpty()) {
			try {
				FileWriter writer = new FileWriter(new File(fname));
				
				Iterator<Byte> it = stream.iterator();
				while (it.hasNext()) {
					Byte b = it.next();
					int i = (int) (b & 0xff);
					writer.write(i + "\n");
				}
				
				writer.close();
			}
			catch (FileNotFoundException e) {
				System.err.println("Ruta no v�lida: " + fname);
				e.printStackTrace();
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		else System.err.println("No hay nada que guardar!");
	}
	
	// Vuelca el contenido del vector de datos en un archivo binario
	public void writeBinary(String fname) {
		if (!stream.isEmpty()) {
			try {
				FileOutputStream s = new FileOutputStream(new File(fname));
				DataOutputStream d = new DataOutputStream(s);
				
				Iterator<Byte> it = stream.iterator();
				while (it.hasNext())
					d.writeByte(it.next());
				
				s.close();
			} catch (FileNotFoundException e) {
				System.err.println("Ruta no v�lida: " + fname);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		else System.err.println("No hay nada que guardar!");
	}
	
	public ArrayList<Byte> getStream() { return stream; }
	
	/*
	public static void main(String args[]) {
		FileConverter f = new FileConverter();
		
		f.readTxt("traza.txt");
		f.writeTxt("traza2.txt");
		f.writeBinary("traza3.txt");
		f.readBinary("traza3.txt");
		f.writeTxt("traza4.txt");
		
		System.exit(0);
	}
	*/
}