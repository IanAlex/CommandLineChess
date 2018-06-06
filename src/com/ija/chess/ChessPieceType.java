package com.ija.chess;

public enum ChessPieceType {
	
	KING ("King"),
	QUEEN ("Queen"),
	ROOK ("Rook"),
	BISHOP ("Bishop"),
	KNIGHT ("Knight"),
	PAWN ("Pawn");
	
	private final String type;
	
	private ChessPieceType(String s) {
		type = s;
	}
	
	public String getType() {
		return type;
	}
	
		
	public String toString() {
		return this.type;
	}
	
	

}
