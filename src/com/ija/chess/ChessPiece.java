package com.ija.chess;

import java.awt.Point;

public class ChessPiece {
	
	ChessPieceType type;
	ChessPieceColor color;
	int numberOfMoves = 0;
	Point capturedAt = null;
	
	
	boolean underCheck = false;  // aplies only for King
	
	public ChessPiece(ChessPieceType type, ChessPieceColor color) {
		this.type = type;
		this.color = color;
	}
	
	public ChessPieceType getType() {
		return type;
	}
	
	public ChessPieceColor getColor() {
		return color;
	}
	
	public boolean isUnderCheck() {
		return underCheck;
	}
	
	public void setUnderCheck(boolean underCheck) {
		this.underCheck = underCheck;
	}
	
	public Point getCapturedAt() {
		return capturedAt;
	}
	
	public void setCapturedAt(Point capturedAt) {
		this.capturedAt = capturedAt;
	}
	
	public int getNumberOfMoves() {
		return numberOfMoves;
	}
	
	public void setNumberOfMoves(int numberOfMoves) {
		this.numberOfMoves = numberOfMoves;
	}
	
	

}
