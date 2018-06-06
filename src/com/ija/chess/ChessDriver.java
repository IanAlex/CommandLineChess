package com.ija.chess;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Scanner;

public class ChessDriver {
	
	
	private Map<Point, ChessPiece> board = new HashMap<Point, ChessPiece>();
	private Map<ChessPiece, Point> capturedPiecesMap = new HashMap<ChessPiece, Point>();
	private ChessPieceColor currentColor;
	
	private static final String QUIT = "Quit";
	private static final String EXECMOVE = "EXECMOVE";
	private static final String SHOWCAPTURED = "SHOWCAPTURED";
	private static final String CASTLELEFTROOK = "CASTLELEFTROOK";
	private static final String CASTLERIGHTROOK = "CASTLERIGHTROOK";
	
	
	public Map<Point, ChessPiece> getBoard() {
		return this.board;
	}
	
	public void setBoard(Map<Point, ChessPiece> board) {
		this.board = board;
	}
	
	public Map<ChessPiece, Point> getCapturedPiecesMap() {
		return this.capturedPiecesMap;
	}
	
	public void setCapturedPiecesMap(Map<ChessPiece, Point> capturedPiecesMap) {
		this.capturedPiecesMap = capturedPiecesMap;
	}
	
	
	public ChessPieceColor getCurrentColor()  {
		return this.currentColor;
	}
	
	public void setCurrentColor(ChessPieceColor currentColor) {
		this.currentColor = currentColor;
	}
	
	
	private static ChessDriver gameInit() {
	    // sets up a new game (ChessDriver) object and initializes the
		// values of currentColor, the board map, captured pieces map  
		
		ChessDriver game = new ChessDriver();
		game.setCurrentColor(ChessPieceColor.WHITE);
		game.getBoard().clear();
		game.getBoard().putAll(ChessUtil.initBoard());
		game.getCapturedPiecesMap().clear();
		game.getCapturedPiecesMap().putAll(new HashMap<ChessPiece, Point>());
		game.setCapturedPiecesMap(new HashMap<ChessPiece, Point>());
		return game;
	}
	
	private static String menuChoices(Scanner scanner, ChessMove mover) {
	    // generates menu choices dynamically.  For instance if are captured pieces then
		// viewing them is an option.  Similarly if King can be castled then that is an option.
		int choice = -1;
		int cnt = 1;
		String retVal = null;
		boolean validFlag = false;
		Map <Integer, String> menuChoiceMap = new HashMap<Integer, String>();
		while (!validFlag) {
			System.out.println("Please Choose from the following options (choose the number)");
			System.out.println(cnt + " - Execute move");
			menuChoiceMap.put(cnt++, EXECMOVE);
			if (!mover.getCapturedPiecesMap().isEmpty()) {
				System.out.println(cnt + " - Show Captured Pieces");
				menuChoiceMap.put(cnt++, SHOWCAPTURED);
			}
			if (mover.canKingCastleWithLeftRook()) {
				System.out.println(cnt + " - castle " + mover.getCurrentColor() + " King with Left Rook");
				menuChoiceMap.put(cnt++, CASTLELEFTROOK);
			}
			if (mover.canKingCastleWithRightRook()) {
				System.out.println(cnt + " - castle " + mover.getCurrentColor() + " King with Right Rook");
				menuChoiceMap.put(cnt++, CASTLERIGHTROOK);
			}
			System.out.println(cnt + " - Quit game");
			menuChoiceMap.put(cnt, QUIT);
		
			if (scanner.hasNextInt()) { 
				choice = scanner.nextInt();
				if (choice >= 1 && choice <= cnt) {
					validFlag = true;
					retVal = menuChoiceMap.get(choice);
				}
			}
			if (!validFlag) {
				System.out.println("Invalid choice.  Try again.");
				System.out.println("");
			}
		}
		return retVal;
	}
	
	public static void main(String[] args) {
	    // sets up the game an iteratively queries the user through the menu and 
		// executes logic in accordance with the menu item chosen.
		Scanner scanner = new Scanner(System.in);
		ChessDriver game = null;
		ChessMove mover = null;		
		while (true) {
			System.out.print("Do you wish to start a Chess Game? (Y/N)");
		    String gameStartResponse = scanner.next();
		    if (gameStartResponse != null && ( gameStartResponse.equalsIgnoreCase("y") || gameStartResponse.equalsIgnoreCase("n")) ) {
		    	if (gameStartResponse.equalsIgnoreCase("y")) {
		    		game = gameInit();
		    		mover = new ChessMove(game.getBoard(), game.getCapturedPiecesMap(), game.getCurrentColor());
		    		System.out.println("");
		    		break;
		    	}
		    	else {
		    		System.out.println("Did not start game.  Goodbye");
		    		scanner.close();
		    		System.exit(0);
		    	}
		    }
		    else {
		    	System.out.println("Invalid Response");
		    	System.out.println("");
		    }
		}
		while (true) {
			Point kingLocation = null;
			for (Map.Entry<Point,ChessPiece> mapEntry : game.getBoard().entrySet()) {
				if (mapEntry.getValue()!= null && mapEntry.getValue().getType().equals(ChessPieceType.KING)  && mapEntry.getValue().getColor().equals(game.getCurrentColor())) {
					kingLocation = mapEntry.getKey();
					break;
				}
			}
			if (mover.kingCheckMated(kingLocation)) {
				ChessUtil.showBoard(game.getBoard());
				System.out.println(game.getCurrentColor() + " is checkmated. " + 
			                          ((game.getCurrentColor().equals(ChessPieceColor.WHITE)) ? ChessPieceColor.BLACK : ChessPieceColor.WHITE) +
			                           " wins");
				System.out.println("Game over.");
				while (true) {
					System.out.print("Start new game? (Y/N");
					String newgameStartResponse = scanner.next();
				    if (newgameStartResponse != null && ( newgameStartResponse.equalsIgnoreCase("y") || newgameStartResponse.equalsIgnoreCase("n")) ) {
				    	if (newgameStartResponse.equalsIgnoreCase("y")) {
				    		game = gameInit();
				    		mover = new ChessMove(game.getBoard(), game.getCapturedPiecesMap(), game.getCurrentColor());
				    		System.out.println("");
				    		break;
				    	}
				    	else {
				    		System.out.println("Did not start new game.  Goodbye");
				    		scanner.close();
				    		System.exit(0);
				    	}
				    }
				    else {
				    	System.out.println("Invalid Response");
				    	System.out.println("");
				    }
				}
			}
			boolean kingUnderCheckFlag = mover.kingInCheck(kingLocation);
			String menuChoice = menuChoices(scanner, mover);
			if (menuChoice.equals(QUIT)) {
				System.out.println("You quit the game.  See you next time.  Goodbye.");
				scanner.close();
				System.exit(0);
			}
			else if (menuChoice.equals(EXECMOVE)) {
				System.out.println("Current Player: " + game.getCurrentColor());
			    if (kingUnderCheckFlag) {
					System.out.println("WARNING: " + game.getCurrentColor() +" King is currently uncer check");
				}
			    ChessUtil.showBoard(game.getBoard());
				Point current = null;
				Point target = null;
				while (true) {					
					System.out.print(game.getCurrentColor() + ": Move piece FROM x,y (where x and y are integers between 0 and 7");
					current = ChessUtil.parseInputCoord(scanner.next());
					if (current != null ) {
						break;
						}
					System.out.println("Invalid coordinate.  Try again.");
					System.out.println("");
				}
				while (true) {
					System.out.print(game.getCurrentColor() + ": Move piece TO x,y (where x and y are integers between 0 and 7");
					target = ChessUtil.parseInputCoord(scanner.next());
					if (target != null ) {
						break;
						}
					System.out.println("Invalid coordinate.  Try again.");
					System.out.println("");
				}
				
				mover.executeMove(current, target);
				if (mover.getErrorList().isEmpty()) {
					game.setCurrentColor((game.getCurrentColor().equals(ChessPieceColor.WHITE)) ? ChessPieceColor.BLACK : ChessPieceColor.WHITE);
					mover.setCurrentColor(game.getCurrentColor());
					
					System.out.println("Move executed sucdessfully");
					System.out.println("");
					}
					else {
						System.out.println("Invalid move for rollowing reason(s):");
						for (String errorStr : mover.getErrorList()) {
							System.out.println(errorStr);
						}					
				}
			}
			else if (menuChoice.equals(SHOWCAPTURED)) {
				ChessUtil.showCapturedPieces(mover.getCapturedPiecesMap());
			}
			else if (menuChoice.equals(CASTLELEFTROOK)) {
				int yCoord = (game.getCurrentColor().equals(ChessPieceColor.WHITE) ? 0 : 7);
				Point current = new Point(4, yCoord);
				Point target = new Point(2, yCoord);
				mover.executeMove(current, target);
				if (mover.getErrorList().isEmpty()) {
					game.setCurrentColor((game.getCurrentColor().equals(ChessPieceColor.WHITE)) ? ChessPieceColor.BLACK : ChessPieceColor.WHITE);
					mover.setCurrentColor(game.getCurrentColor());
					System.out.println("Move executed sucdessfully");
					System.out.println("");
					}
					else {
						System.out.println("Invalid move for rollowing reason(s):");
						for (String errorStr : mover.getErrorList()) {
							System.out.println(errorStr);
						}					
				}
			}
			else if (menuChoice.equals(CASTLERIGHTROOK)) {
				int yCoord = (game.getCurrentColor().equals(ChessPieceColor.WHITE) ? 0 : 7);
				Point current = new Point(4, yCoord);
				Point target = new Point(6, yCoord);
				mover.executeMove(current, target);
				if (mover.getErrorList().isEmpty()) {
					game.setCurrentColor((game.getCurrentColor().equals(ChessPieceColor.WHITE)) ? ChessPieceColor.BLACK : ChessPieceColor.WHITE);
					mover.setCurrentColor(game.getCurrentColor());
					System.out.println("Move executed sucdessfully");
					System.out.println("");
					}
					else {
						System.out.println("Invalid move for rollowing reason(s):");
						for (String errorStr : mover.getErrorList()) {
							System.out.println(errorStr);
						}					
				}
			}
		}
		
	}
	
	
	
	

}
