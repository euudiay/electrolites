package com.electrolites.util;


/* DPoint extended with index */

public class ExtendedDPoint{
	
	protected int index;
	protected DPoint dpoint;
	
	public ExtendedDPoint(int index, DPoint p) {
		this.index = index;
		this.dpoint = p;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public DPoint getDpoint() {
		return dpoint;
	}

	public void setDpoint(DPoint dpoint) {
		this.dpoint = dpoint;
	}
	
	@Override
	public String toString() {
		return "("+index+", "+dpoint+")";
	}
	
	public ExtendedDPoint clone() {
		DPoint p = new DPoint(dpoint.getType(), dpoint.getWave());
		return new ExtendedDPoint(index, p);
	}
}
