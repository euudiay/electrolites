package org.microlites.data;

/** DataSourceThread abstract class.
 * A DataSourceThread provides data to other entities in the system.
 * Being a thread, it should start and continue running, as well as
 * handling to calls to cancel and stopSending methods.
 * Successfuly handling a stopSending call should make the DataSource
 * stop sending data towards any entity in the system, 
 * while a cancel call should stop any loop being executed,
 * e.g. the loop in the run method.
 * As a DataSourceThread often communicates with other entities which provide
 * raw data to it, the write method is provided so as to offer external-to-DataSourceThread
 * control over data transfering.
 */
public abstract class DataSourceThread extends Thread {
	/** Writes buffer into an external entity which DataSourceThread 
		is communicating with. (Optional)
	 	@param buffer Array of bytes to be written
	 */
	public abstract void write(byte[] buffer);
	/** Finalizes any loop currently executing so as to allow thread joining.*/
	public abstract void cancel();
	/** Finalizes data sending to any entity in the system.*/
	public abstract void stopSending();
}
