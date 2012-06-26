package org.microlites.data;

/** 
 * A StaticDataHolder is a specialization of a DataHolder with ability
 * to handle batch data sending from other entity.
 */
public interface StaticDataHolder extends DataHolder {
	/**
	 * Handles data for a batch of samples.
	 * @param indexes Samples indexes array
	 * @param amplitudes Samples amplitudes list
	 * @param from First index of arrays containing usable data
	 * @param to Last index of arrays containing usable data
	 * @param size Size of both arrays
	 */
	void setSamplesArrays(int indexes[], short amplitudes[], 
						  int from, int to, int size);
}
