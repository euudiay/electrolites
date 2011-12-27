package com.electrolites.util;


public class PositionedDPoint {
	
	protected float pos;
	protected ExtendedDPoint edpoint;
	
	public PositionedDPoint(float pos, ExtendedDPoint p) {
		this.pos = pos;
		this.edpoint = p;
	}

	public float getPosition() {
		return pos;
	}

	public void setIndex(int pos) {
		this.pos = pos;
	}

	public ExtendedDPoint getExtendedDpoint() {
		return edpoint;
	}

	public void setDpoint(ExtendedDPoint dpoint) {
		this.edpoint = dpoint;
	}
	
	@Override
	public String toString() {
		return "("+pos+", "+edpoint+")";
	}
	
	public PositionedDPoint clone() {
		ExtendedDPoint p = new ExtendedDPoint(edpoint.getIndex(),
				new DPoint(edpoint.getDpoint().getType(), edpoint.getDpoint().getWave()));
		return new PositionedDPoint(pos, p);
	}
}
