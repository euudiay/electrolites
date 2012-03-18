package org.microlites.data;

public interface DataHolder {
	// DP types
	public static final short DP_TYPE_START = 1;
	public static final short DP_TYPE_PEAK 	= 2;
	public static final short DP_TYPE_END	= 3;
	public static final short DP_TYPE_SPEAK	= 4;
	
	// Wave types
	public static final short WAVE_QRS		= 1;
	public static final short WAVE_P		= 2;
	public static final short WAVE_T		= 3;
	public static final short WAVE_OFFSET	= 4;
	
	public abstract void initData();
	public abstract void addSample(int index, short sample);
	public abstract void addDPoint(int sample, short type, short wave);
	public abstract void handleOffset(int offset);
}
