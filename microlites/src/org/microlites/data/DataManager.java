package org.microlites.data;

/** DataManager interface.
 * 	A DataManager handles initialization of a DataSourceThread and comunication
 * 	between it and a DataHolder which is passed to it.
 */
public interface DataManager {
	// The manager stores its DataHolder reference and prepare whatever it needs
	void configure(DataHolder dataHolder);
	// The manager initializes a DataSource, notifying the DataHolder about it
	void start();
	// The manager halts the execution of DataSource 
	void stop();
	// The manager handles a back button press
	void back();
}
