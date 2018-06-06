package com.ija.chess;

import java.awt.Point;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

// this is a utility class for returning representations of chess pieces 
// and board coordinates

public final class ChessUtil {
	
	public static int begBound = 0;
	public static int endBound = 7;
	
	public static String longFormatChessPiece(ChessPiece piece) {
		//long name for chesspiece Color & PieceType
		String retVal = null;
		if (piece != null && piece.getType() != null & piece.getColor() != null) {
			retVal = piece.getColor() + " " + piece.getType();
		}
		return retVal;
	}
	
	public static String shortFormatChessPiece(ChessPiece piece) {
		String retVal = null;
		//abbreviated pieces (e.g. White Queen is W-Q, etc.
		//since King and Knight both beting with K use W-Kn for White Knight, W-Ki for White King
		if (piece != null && piece.getType() != null & piece.getColor() != null) {
			if (piece.getType().equals(ChessPieceType.KNIGHT) || piece.getType().equals(ChessPieceType.KING)) {
				retVal = piece.getColor().toString().substring(0, 1) + piece.getType().toString().substring(0, 2);
			}
			else {
				retVal = piece.getColor().toString().substring(0, 1) + "-" + piece.getType().toString().substring(0, 1);
			}
		}
		return retVal;
	}
	
	public static Point parseInputCoord(String inputCoord) {
		// ensure that user inputed coordinates on chess board are in 
		// for x,y.  Anything else leads to an error.
		Point retVal = null;
		if (inputCoord != null && inputCoord.length() == 3 && inputCoord.charAt(1) == ',') {
			boolean err = false;
			int xCoord = -1;
			int yCoord = -1;
			try {
				 xCoord = Integer.parseInt(inputCoord.substring(0,1));
			}catch (NumberFormatException ex) {
		    	err = true;
		    } finally {	}
			if (!err) {
			  try {	
				yCoord = Integer.parseInt(inputCoord.substring(2));
			  } finally {	}
		    }
			if (xCoord >=0 && yCoord >= 0) {
				retVal = new Point(xCoord, yCoord);
			}			
		}
		return retVal;
	}
	
	public static String formatPoint(Point point) {
		//format a point for use readability. In form x,y
		String retVal = null;
		if (point != null) {
			retVal = point.x + "," + point.y;
		}
		return retVal;		
	}
	
	public static void showCapturedPieces(Map<ChessPiece,Point> capturedPiecesMap) {
		//display list of captured pieces by color
		StringBuilder whiteCaptured = new StringBuilder("");
		StringBuilder blackCaptured = new StringBuilder("");
		for (Map.Entry<ChessPiece,Point> mapEntry : capturedPiecesMap.entrySet()) {
			if (mapEntry.getKey().getColor().equals(ChessPieceColor.WHITE)) {
				whiteCaptured.append(((whiteCaptured.length() > 0)? ";" :""));
				whiteCaptured.append(mapEntry.getKey().getType() + " at (" + formatPoint(mapEntry.getValue())+ ")");
			}
			else {
				blackCaptured.append(((blackCaptured.length() > 0)? ";" :""));
				blackCaptured.append(mapEntry.getKey().getType() + " at (" + formatPoint(mapEntry.getValue()) + ")");
			}
		}
		if (whiteCaptured.length() > 0) {
			System.out.println("White pieces captured (by Black)");
			System.out.println(whiteCaptured.toString());
		}
		else {
			System.out.println("No White pieces captured by Black");
		}
		System.out.println("");
		if (blackCaptured.length() > 0) {
			System.out.println("Black pieces captured (by White)");
			System.out.println(blackCaptured.toString());
		}
		else {
			System.out.println("No Black pieces captured by White");
		}
		System.out.println("");
	}
	
	public static void showBoard(Map<Point, ChessPiece> board) {
		// displays the current state of the chess board after latest moves
		System.out.println("CURRENT CHESS BOARD");
		System.out.println("+++++++++++++++++++");
		System.out.println("");
		System.out.println("");
		StringBuilder boardRow = new StringBuilder("  ");
		for (int i = begBound; i <= endBound; i++) {
			boardRow.append(" " + i + " |");
		}
		System.out.println(boardRow.toString());
		System.out.println("##################################");
		for (int j = endBound; j >= begBound; j--) {
			boardRow = new StringBuilder("");
			boardRow.append(j + "#");
			for (int i = begBound; i <= endBound; i++) {
				ChessPiece piece = board.get(new Point(i,j));
				if (piece != null) {
					boardRow.append(shortFormatChessPiece(piece) + "|");
				}
				else {
					boardRow.append("   |");
				}
			}
			boardRow.append(j);
			System.out.println(boardRow.toString());			
		} 
		System.out.println("##################################");
		boardRow = new StringBuilder(" #");
		for (int i = begBound; i <= endBound; i++) {
			boardRow.append(" " + i + " |");
		}
		System.out.println(boardRow.toString());
		System.out.println("");		
	}
	
	public static Map<Point, ChessPiece> initBoard() {
		// initializes the chess board at the starting position of the game
		Map<Point, ChessPiece> board = new HashMap<Point, ChessPiece>();
		int yCoord = begBound;
		while (yCoord == begBound || yCoord == endBound) {
			board.put(new Point(0,yCoord), new ChessPiece(ChessPieceType.ROOK, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(1,yCoord), new ChessPiece(ChessPieceType.KNIGHT, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(2,yCoord), new ChessPiece(ChessPieceType.BISHOP, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(3,yCoord), new ChessPiece(ChessPieceType.QUEEN, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(4,yCoord), new ChessPiece(ChessPieceType.KING, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(5,yCoord), new ChessPiece(ChessPieceType.BISHOP, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(6,yCoord), new ChessPiece(ChessPieceType.KNIGHT, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			board.put(new Point(7,yCoord), new ChessPiece(ChessPieceType.ROOK, (yCoord == begBound ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			yCoord = (yCoord == begBound ? endBound : -99);		
		}
		yCoord = begBound + 1;
		while (yCoord == (begBound + 1) || yCoord == (endBound -1) ) {				
			for (int i = begBound; i <= endBound; i++) {
				board.put(new Point(i,yCoord), new ChessPiece(ChessPieceType.PAWN, (yCoord == (begBound + 1) ? ChessPieceColor.WHITE : ChessPieceColor.BLACK)) );
			}
			yCoord = (yCoord == (begBound + 1) ? (endBound - 1) : -99);
		}
		return board;
	}

}
