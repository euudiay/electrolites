package com.electrolites.data;

// Objeto que representa un punto resultante de la delineación
public class DPoint {
	// Tipo de punto: ninguno, comienzo, pico, final o pico secundario
	enum PointType { none, start, peak, end, s_peak };
	enum Wave { none, QRS, P, T };
	
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
	
	public PointType getType() { return type; }
	
	public Wave getWave() { return wave; }
}
