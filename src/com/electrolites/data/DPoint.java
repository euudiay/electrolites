package com.electrolites.data;

// Objeto que representa un punto resultante de la delineaci�n
public class DPoint {
	// Tipo de punto: ninguno, comienzo, pico, final o pico secundario
	public enum PointType { none, start, peak, end, s_peak };
	public enum Wave { none, QRS, P, T };
	
	private PointType type;
	private Wave wave;
	
	public DPoint() {
		type = PointType.none;
		wave = Wave.none;
	}
	
	public DPoint(PointType type, Wave wave) {
		this.type = type;
		this.wave = wave;
	}
	
	public PointType checkType(int i) {
		PointType type;
		
		switch (i & 0xf0) {
			case 1 << 4: type = PointType.start; break;
			case 2 << 4: type = PointType.peak; break;
			case 3 << 4: type = PointType.end; break;
			case 4 << 4: type = PointType.s_peak; break;
			default: type = PointType.none;
		}
		
		return type;
	}
	
	public Wave checkWave(int i) {
		Wave wave;
		
		switch (i & 0x0f) {
			case 1: wave = Wave.QRS; break;
			case 2: wave = Wave.P; break;
			case 3: wave = Wave.T; break;
			default: wave = Wave.none;
		}
		
		return wave;
	}
	
	public PointType getType() { return type; }
	
	public void setType(PointType type) { this.type = type; }
	
	public Wave getWave() { return wave; }
	
	public void setWave(Wave wave) { this.wave = wave; }
}
