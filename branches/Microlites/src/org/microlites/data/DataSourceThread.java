package org.microlites.data;

public abstract class DataSourceThread extends Thread {
	public abstract void write(byte[] buffer);
	public abstract void cancel();
	public abstract void stopSending();
}
