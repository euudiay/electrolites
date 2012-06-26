package org.microlites.data;

/** DataHolder interface.
 * A DataHolder instance manages processed ECG data.
 * Certain DataHolder instance might, for instance, store data for visualization
 * while other could save it into a log file.
 * The way the data is stored and managed depends on the implementation.  
 */
public interface DataHolder {
	/** Delineation Point Type Constants */
	/** Delineation Point Start Type Constant */
	public static final short DP_TYPE_START = 1;
	/** Delineation Point Peak Type Constant */
	public static final short DP_TYPE_PEAK 	= 2;
	/** Delineation Point End Type Constant */
	public static final short DP_TYPE_END	= 3;
	/** Delineation Point Secondary Peak Type Constant */
	public static final short DP_TYPE_SPEAK	= 4;
	
	/** Delineation Point Wave Type Constants */
	/** Delineation Point QRS Wave Constant */
	public static final short WAVE_QRS		= 1;
	/** Delineation Point P Wave Constant */
	public static final short WAVE_P		= 2;
	/** Delineation Point T Wave Constant */
	public static final short WAVE_T		= 3;
	/** Delineation Point Offset Constant */
	public static final short WAVE_OFFSET	= 4;
	
	/** Performs any initialization needed by the DataHolder. */
	public abstract void initData();
	
	/** Handles data for a new Sample 
	 * 	@param index New sample's index 
	 * 	@param sample New sample's amplitude
	*/
	public abstract void addSample(int index, short sample);
	
	/**
	 * Handles data for a new Delineation Point
	 * @param sample Sample index to which the DPoint is related
	 * @param type Type of the DPoint
	 * @param wave Wave to which DPoint corresponds
	 */
	public abstract void addDPoint(int sample, short type, short wave);
	
	/**
	 * Handles data for a new Offset Marker, if it differs for own offset
	 * counter a problem has arised and should be handled appropiately
	 * @param offset Expected Offset
	 */
	public abstract void handleOffset(int offset);
	
	/**
	 * Handles data for a new Heart Beat Rate (HBR) value
	 * @param hbr Heart Beat Rate in beats-per-minute (bpm)
	 */
	public abstract void handleHBR(float hbr);
}
