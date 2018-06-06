package com.ija.chess;

public enum ChessPieceColor {
	
	WHITE ("White"),
	BLACK ("Black");
	
	private final String color;
	
	private ChessPieceColor(String s) {
		color = s;
	}
	
	public String getColor() {
		return color;
	}
	
	public String toString() {
		return this.color;
	}

}
