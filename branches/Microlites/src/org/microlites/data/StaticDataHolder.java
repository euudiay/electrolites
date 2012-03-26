package org.microlites.data;

public interface StaticDataHolder extends DataHolder {
	void setSamplesArrays(int indexes[], short amplitudes[], 
						  int from, int to, int size);
}
