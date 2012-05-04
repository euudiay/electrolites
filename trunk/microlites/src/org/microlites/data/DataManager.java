package org.microlites.data;

public interface DataManager {
	// The manager stores its DataHolder reference and prepare whatever it needs
	void configure(DataHolder dataHolder);
	// The manager initializes a DataSource, notifying the DataHolder about it
	void start();
	// The manager halts the execution of DataSource 
	void stop();
}
