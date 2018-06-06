package com.ija.chess;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.awt.Point;


public class ChessMove {
	
	private Map<Point, ChessPiece> board;
	private Map<ChessPiece, Point> capturedPiecesMap;
	private ChessPieceColor currentColor;
	//private Map<Point, ChessPiece> modifiedBoard;
	
	private List<String> errorList = new ArrayList<String>();
	private Set<Map.Entry<Point,ChessPiece>> piecesCheckingKingSet = new HashSet<Map.Entry<Point,ChessPiece>>();
	private boolean doKingCastleLeftRook = false;
	private boolean doKingCastleRightRook = false;
	
	// Formatted tring constants for error messages
	private static final String noPieceToMove = "Cannot move from %s since there is no chess piece at that location";
	private static final String wrongColorMove = "Cannot move %s at %s since this is an opposing piece (i.e. wrong color)";	
	private static final String wrongMovePieceInTheWay = "Cannot move %s from %s to %s since there are piece(s) in the way of path";
	private static final String wrongCaptureTarget = "Cannot move %s from %s to %s since piece %s already there is same color";	
	private static final String wrongTargetOutOfBounds = "Cannot move a piece to (%s) since this target coordinate is not on the chess board";	
	private static final String wrongMovePathBishop = "Cannot move %s from %s to %s since this is not diagonal path";
	private static final String wrongMovePathRook = "Cannot move %s from %s to %s since this is not vertical/horizontal path";
	private static final String wrongMovePathKnight = "Cannot move %s from %s to %s since this is not valid path for knight";
	private static final String wrongMovePathQueen = "Cannot move %s from %s to %s since this is not vertical/horizontal/diagonal path";
	private static final String wrongMovePathPawn = "Cannot move %s from %s to %s since this violates Pawn rule: %s";
	private static final String wrongMovePathKing = "Cannot move %s from %s to %s since this is not a one-shift vertical/horizontal/diagonal  path";
	private static final String wrongMoveCastleKing = "Cannot move %s from %s to %s since this castle attempt violates rule: %s";
	private static final String wrongPutsKingInCheck = "Cannot move %s from %s to %s since this puts your King in check";
	private static final String wrongNotClearPath = "Cannot move %s from %s to %s since this is not a clear path (pieces are in the way)";
	private static final String updownRulePawn = "up-one WHITE/down-one BLACK permitted if no other pieces at target; up-two WHITE/down-two BLACK permitted if first move and no other pieces at target";
	private static final String diagRulePawn = "(diag-up-left/diag-up-right)WHITE Or (diag-down-left/diag-down-right)BLACK permitted if target still on the board boundary and there is an opposing piece at target (capture it)";
	private static final String castleRuleLeftRook = "King and left rook must not have previously moved and there are no pieces blocking castle path";
	private static final String castleRuleRightRook = "King and right rook must not have previously moved and there are no pieces blocking castle path";
	private static final String wrongMoveKingUnderCheck = "Cannot move %s from %s to %s since this puts your King under check (or King was already under check and this move does not remedy it).";
	
	
	
	public ChessMove(Map board, Map capturedPiecesMap, ChessPieceColor currentColor) {

		this.board = board;
		this.capturedPiecesMap = capturedPiecesMap;
		this.currentColor = currentColor;
	}
		
	public ChessPieceColor getCurrentColor() {
		return this.currentColor;
	}
	
	public void setCurrentColor(ChessPieceColor currentColor) {
		this.currentColor = currentColor;
	}
	
	public List<String> getErrorList() {
		return this.errorList;
	}
	
	public Map<Point, ChessPiece> getBoard() {
		return this.board;
	}
	
	public Map<ChessPiece, Point> getCapturedPiecesMap() {
		return this.capturedPiecesMap;
	}
	
	public void clearErrors() {
		errorList.clear();
	}
	
	private boolean validateMove(Point current, Point target) {
		//validate in accordance with exisiting rules. Return 
		// true in accordance to rules being satisfied.
		clearErrors();
		return (movePieceExists(current) &&
								moveColorValid(current, currentColor) &&
									boardBoundaryHonored(target, true) &&
										movePathValid(current, target, true) &&
											movePathClear(current, target, board, true) &&
												moveCanLandOnTarget(current, target, true) &&
												!movePutsKingInCheck(current, target, true));	
	}
	
	public boolean executeMove(Point current, Point target) {
		// validate the move according to rules methods and 
		// if that clears then (1) if a target piece was captured add it to map
		// AND (2) set move-from board location to null value and target board location
		// to the piece that was moved to it
		// ALSO: logic for execute king-castling (after validation).
		// ALSO: logic for promotion of pawn (to Queen in all cases)
		boolean retVal = false;
		if (validateMove(current, target)) {
			ChessPiece currentPiece = board.get(current);
			ChessPiece targetPiece = board.get(target);
			int yCoord = (currentColor.getColor().equals(ChessPieceColor.WHITE.toString()) ? 0 : 7 );
			System.out.println("ycoord =" + yCoord);
			if (targetPiece != null) {
				capturedPiecesMap.put(targetPiece, target);
			}
			if (doKingCastleRightRook) {
				doKingCastleRightRook = false;
				executeMove(new Point(7,yCoord), new Point(5,yCoord));
				// move king to target
				board.put(target, currentPiece);
				// clear king original spot
				board.put(current, null);
			}
			else if (doKingCastleLeftRook) {
				doKingCastleLeftRook = false;
				executeMove(new Point(0,yCoord), new Point(3,yCoord));
				// move king to target
				board.put(target, currentPiece);
				// clear king original spot
				board.put(current, null);
			}
			else {
				// if Pawn gets makes it to the other end, assume promotion to QUEEN.
				if (currentPiece.getType().equals(ChessPieceType.PAWN) && target.y == yCoord) {
					ChessPiece promotedPiece = new ChessPiece(ChessPieceType.QUEEN, currentColor);
					board.put(target, promotedPiece);
				}
				else {
					board.put(target, board.get(current));	
				}
				board.put(current, null);	
			}
		}
		return retVal;
	}
	
	private boolean movePathValid(Point current, Point target, boolean recordErrors) {
		// make sure that the piece type in question can move to a new x,y pair on the 
		// board in accordance to the move roles.
		// This is done based on the piece type (e.g. pawn, king, bishop, etc) of the piece being moved
		ChessPiece pieceToMove = board.get(current);
		ChessPieceType type = pieceToMove.getType();
		int dx = Math.abs(target.x - current.x);
		int dy = Math.abs(target.y - current.y);
		boolean validSoFar = false;
		switch (type) {
		   case BISHOP:
			   //check for diagonal move
		    	//if (dx == 0.0 || dy == 0.0 || Math.abs(dx) == Math.abs(dy) ) {
			    if (dx == dy) {
		    		validSoFar = true;
		    	}
		    	else if (recordErrors){
		    		errorList.add(String.format(wrongMovePathBishop, 
		    			ChessUtil.longFormatChessPiece(pieceToMove),
		    			ChessUtil.formatPoint(current),
		    			ChessUtil.formatPoint(target)));
		    	}
		    	break;
		   case ROOK:
			   // check for vertical or horizontal move
			   if (current.x == target.x || current.y == target.y) {
				   validSoFar = true;
			   }
			   else if (recordErrors) {
				   errorList.add(String.format(wrongMovePathRook, 
		    			ChessUtil.longFormatChessPiece(pieceToMove),
		    			ChessUtil.formatPoint(current),
		    			ChessUtil.formatPoint(target)));
			   }
			   break;
		   case KNIGHT:
			   //check for following:
			   // (1) move left/right 1 (along X-axis) AND move up/down 2 (along Y-axis)
			   // (2) move left/right 2 (along X-axis) AND move up/down 1 (along Y-axis)
			   			   
			   if ((dx == 1 && dy == 2) || (dx == 2 && dy == 1 )) {
				   validSoFar = true;
			   }
			   else if (recordErrors) {
				   errorList.add(String.format(wrongMovePathKnight, 
		    			ChessUtil.longFormatChessPiece(pieceToMove),
		    			ChessUtil.formatPoint(current),
		    			ChessUtil.formatPoint(target)));
			   }
			   break;
		   case QUEEN:
			   // can move (1) horizontally (along X-axis) (2) vertically (along y-axis) OR
			   // (3) diagonally
			   if (current.x == target.x || current.y == target.y || dx == dy) {
				   validSoFar = true;
			   }
			   else if (recordErrors){				   
				   errorList.add(String.format(wrongMovePathQueen, 
		    			ChessUtil.longFormatChessPiece(pieceToMove),
		    			ChessUtil.formatPoint(current),
		    			ChessUtil.formatPoint(target)));
			   }
			   break;
		   case PAWN:
			   //can move as follows:
			   // (1) up-one WHITE/ down-one BLACK if there is no other piece at target
			   // (2) up-two WHITE/ down-two BLACK if it hasn't made a move and there is no other piece at target
			   // (3) diag-up-left WHITE/ diag-down-left BLACK if still on the board boundary and there is an opposing piece at target (capture it)
			   // (4) diag-up-right WHITE/ diag-down-right BLACK if still on the board boundary and there is an opposing piece at target (capture it)
			   if (target.x == current.x) {  //up-one
				   if (((target.y - current.y) == (currentColor.equals(ChessPieceColor.WHITE) ? 1 : -1))) {
					  validSoFar = true;
				   }
				   else if (((target.y - current.y) == (currentColor.equals(ChessPieceColor.WHITE) ? 2 : -2))  && pieceToMove.numberOfMoves == 0 ) { //up-two if no other moves were made
					  validSoFar = true;
				   }
				   if (validSoFar && board.get(target)== null) { //make sure there's no other pieces in the way
					   validSoFar = true;
				   }
			   }
			   // diagonal-up WHITE/diagonal-down BLACK move if there is opposing color piece to capture
			   else if (((target.y - current.y) == (currentColor.equals(ChessPieceColor.WHITE) ? 1 : -1)) && dx == 1 ) {
				   if (board.get(target) != null && !board.get(target).getColor().equals(currentColor)) {
					   validSoFar = true;
				   }
			   }
			   if ((!validSoFar) && recordErrors) {
				   errorList.add(String.format(wrongMovePathPawn, 
			    			ChessUtil.longFormatChessPiece(pieceToMove),
			    			ChessUtil.formatPoint(current),
			    			ChessUtil.formatPoint(target),
			    			diagRulePawn));
			   }
			   
			   break;
		   case KING:
			   //allow single vertical, horizontal or diagonal shift
			   if (dx <= 1 && dy <=1) {
				   validSoFar = true;
			   }
			   //test for allowing King & Rook to castle.
			   //King must be at original position (X=4) and have not moved at all
			   // it can castle with left rook (X=0) or right rook (X-7) provided
			   // (1) there is clear space between them and the rook in question has
			   // not moved.
			   
			   if (pieceToMove.getNumberOfMoves() == 0 && current.x == 4) {
			      // castle King with left rook provided that neither piece has moved and there
			       // is clear space between
				  if (target.x == 2) {	 
					       
					 ChessPiece pieceLeftRookPos = board.get(new Point(0, current.y));
					 if (pieceLeftRookPos.numberOfMoves == 0 && pieceLeftRookPos.getType().equals(ChessPieceType.ROOK) &&
							 board.get(new Point(1, current.y)) == null &&
							   board.get(new Point(2, current.y)) == null &&
								 board.get(new Point(3, current.y)) == null)
					 {
						 validSoFar = true;
						 doKingCastleLeftRook = true;
					 }
					 else if (recordErrors){
						 errorList.add(String.format(wrongMoveCastleKing, 
					    			ChessUtil.longFormatChessPiece(pieceToMove),
					    			ChessUtil.formatPoint(current),
					    			ChessUtil.formatPoint(target),
					    			castleRuleLeftRook));
					 }
					 
				  }
				  
				   // castle King with right rook provided that neither piece has moved and there
				   // is clear space between
				  else if (target.x == 6)  {
					 ChessPiece pieceRightRookPos = board.get(new Point(7, current.y));
					 if (pieceRightRookPos.numberOfMoves == 0 && pieceRightRookPos.getType().equals(ChessPieceType.ROOK) && 
							 board.get(new Point(5, current.y)) == null &&
							   board.get(new Point(6, current.y)) == null )
					 {
						 validSoFar = true;
						 doKingCastleRightRook = true;
					 }
					 else if (recordErrors){
						 errorList.add(String.format(wrongMoveCastleKing, 
					    			ChessUtil.longFormatChessPiece(pieceToMove),
					    			ChessUtil.formatPoint(current),
					    			ChessUtil.formatPoint(target),
					    			castleRuleRightRook));
					 }
					 
				  }				 
			   }
			   
			   else if ((!validSoFar) && recordErrors) {
				// if not a one-shift move nor a castling move 
				   errorList.add(String.format(wrongMovePathKing, 
			    			ChessUtil.longFormatChessPiece(pieceToMove),
			    			ChessUtil.formatPoint(current),
			    			ChessUtil.formatPoint(target)));
			   }
			   break;
		   default:
			   break;
			
		}
		return validSoFar;
	}
	
	private boolean moveColorValid(Point current, ChessPieceColor currentColor) {
		// If its White(Black) turn to move and try to move a Black(White) piece then 
		// rule is violated
	    ChessPiece curPiece = board.get(current);
	    boolean validFlag =  curPiece.getColor().name().equals(currentColor.name());
	    if (!validFlag) {
	    	errorList.add(String.format(wrongColorMove, 
	    			ChessUtil.longFormatChessPiece(curPiece),
	    			ChessUtil.formatPoint(current)));
	    }
	    return validFlag;
	}
	
	private boolean movePieceExists(Point current) {
		// ensure that there is a piece on the board at the coordinate FROM where 
		// the move is taking place.
		boolean existFlag = board.get(current) != null;
		if (!existFlag) {
			errorList.add(String.format(noPieceToMove,
	    			ChessUtil.formatPoint(current)));
		}
		return existFlag;
	}

	
	private boolean movePathClear(Point current, Point target, Map<Point,ChessPiece> board,  boolean recordError) {
		// ensure that there are no blocking the move path from the move-from
		// coordinate to the move-to coordinates
		// If piece is Knight then test passes, since it can jump over;
		if (board.get(current).getType().equals(ChessPieceType.KNIGHT)) {
			return true;
		}
		// moving vertically (on an X coordinate).  If there's a piece in the way, then not clear
		// PLEASE NOTE: the end-point is NOT considered since this is accounted for in moveCanCapturePiece(Point current, Point target) method
		boolean validFlag = true;
		if (current.x == target.x && current.y != target.y) {

			int beg =  Math.min(current.y, target.y) + 1;
			int end =  Math.max(current.y, target.y) - 1;
			while (beg <= end) {

				if (board.get(new Point(current.x, end--)) != null) {
					validFlag = false;
					break;
				}
			}
		}
		//moving horizontally on a Y coordinate. If there's a piece in the way, then not clear
		// PLEASE NOTE: the end-point is NOT considered since this is accounted for in moveCanCapturePiece(Point current, Point target) method
		else if (current.x != target.x && current.y == target.y) {

			int beg =  Math.min(current.x, target.x) + 1;
			int end =  Math.max(current.x, target.x) - 1;

			while (beg <= end) {
				if (board.get(new Point(beg++, current.y)) != null) {
					validFlag = false;
					break;
				}
			}
		}
        // moving diagonally; if a piece is in the way, then not clear
		// PLEASE NOTE: the end-point is NOT considered since this is accounted for in moveCanCapturePiece(Point current, Point target) method
		else if (Math.abs(current.x - target.x) == Math.abs(current.y - target.y)) {

			int dx = (current.x >= target.x) ? -1 : 1;
			int dy = (current.y >= target.y) ? -1 : 1;
			int begX = current.x + dx;
			int begY =  current.y + dy;

			while ((dx == -1 && begX > (target.x)) || (dx == 1 && begX < (target.x))){
				if (board.get(new Point(begX, begY)) != null) {			
					validFlag = false;
				}
				begX += dx;
				begY += dy;
			}
			
		}
		if (!validFlag && recordError) {
			errorList.add(String.format(wrongMovePieceInTheWay, 
	    			ChessUtil.longFormatChessPiece(board.get(current)),
	    			ChessUtil.formatPoint(current),
	    			ChessUtil.formatPoint(target)));
		}

		return validFlag;
	}

	
	private boolean moveCanLandOnTarget(Point current, Point target, boolean recordError) {
		// allows move to target spot on chess board if there is no piece there or the piece on 
		// target spot is opposite color (i.e. can capture)
		boolean validFlag = (board.get(target) == null || !board.get(current).getColor().equals(board.get(target).getColor()));
		if (!validFlag && recordError) {
			errorList.add(String.format(wrongCaptureTarget, 
	    			ChessUtil.longFormatChessPiece(board.get(current)),
	    			ChessUtil.formatPoint(current),
	    			ChessUtil.formatPoint(target),
	    			ChessUtil.longFormatChessPiece(board.get(target))));
		}
		return validFlag;
	}

		
	public boolean kingInCheck(Point kingLocation) {
		// check and record (in a set) the opponent pieces (different color to King) to see if they have a path to capture the king
		// if there is at least one such opponent piece then return true;
		boolean inCheckFlag = false;
		piecesCheckingKingSet.clear();
		for (Map.Entry<Point,ChessPiece> mapEntry : board.entrySet()) {
			ChessPiece  piece = mapEntry.getValue();
			if (piece != null && !piece.getColor().equals(currentColor)) {
				if (movePathClear(mapEntry.getKey(), kingLocation, board, false) && movePathValid(mapEntry.getKey(), kingLocation, false)) {
					piecesCheckingKingSet.add(mapEntry);
					inCheckFlag = true;;
				}				
			}
		}
		return inCheckFlag;
	}
	
	private boolean kingInCheck(Point kingLocation, Map<Point, ChessPiece> modBoard) {
		// check the opponent pieces (different color to King) to see if they have a path to capture the king
		// this is an overridden version of the method to see if the king is in check after a proposed move
		// (which changes the board) and return true if it is.
		boolean inCheckFlag = false;
		for (Map.Entry<Point,ChessPiece> mapEntry : modBoard.entrySet()) {
			ChessPiece  piece = mapEntry.getValue();
			if (piece != null && !piece.getColor().equals(currentColor)) {
				if (movePathClear(mapEntry.getKey(), kingLocation, modBoard, false) && movePathValid(mapEntry.getKey(), kingLocation, false)) {
					inCheckFlag = true;
				}				
			}
		}
		return inCheckFlag;
	}
	
	private boolean checkIfPathToBlock(Point kingLocation) {
		// this method checks to see if any other pieces of the checked King's color can be moved
		// in order to cancel king-under-check condition
		for (Map.Entry<Point,ChessPiece> checkPiecemapEntry : piecesCheckingKingSet) {
			List<Point> blockingPoints = findPointsOnPath(checkPiecemapEntry, kingLocation);
			for (Map.Entry<Point, ChessPiece> othermapEntry :board.entrySet()) {
				if (othermapEntry != null && othermapEntry.getValue() != null && othermapEntry.getValue().getColor().equals(currentColor)) {
				   for (Point blockPoint: blockingPoints) {
					   if (movePathValid(othermapEntry.getKey(), blockPoint, false) &&
							movePathClear(othermapEntry.getKey(), blockPoint, board, false) && 
							   moveCanLandOnTarget(othermapEntry.getKey(), blockPoint, false) &&
							    !movePutsKingInCheck(othermapEntry.getKey(), blockPoint, false)) 
					   {
					     return true;
					   }
				   }
				}
			}
		}
		return false;
	}
	
	List<Point> findPointsOnPath(Map.Entry<Point, ChessPiece> checkingPieceEntry, Point target) {
		// find the points on the path between a checking piece and the king under check and return
		// as a list.   This list is used to determine if there are pieces that can block the checking piece
		// by moving to those points.
		List<Point> retVal = new ArrayList<Point>();
		retVal.add(checkingPieceEntry.getKey());
		int dx = Math.abs(target.x - checkingPieceEntry.getKey().x);
		int dy = Math.abs(target.y - checkingPieceEntry.getKey().y);
		ChessPieceType checkingType = checkingPieceEntry.getValue().getType();
		if (checkingType.equals(ChessPieceType.ROOK) || checkingType.equals(ChessPieceType.QUEEN) ) {
			if (dx == 0 && dy > 0) {
				//king is on an upward vertical path from checking piece
				if ((target.y - checkingPieceEntry.getKey().y) > 0) {
					for (int begY = (checkingPieceEntry.getKey().y + 1); begY < target.y; begY++ ) {
						retVal.add(new Point(target.x, begY));
					}
				}
				//king is on an downward vertical path from checking piece
				else {
					for (int begY = (target.y - 1); begY >=  (checkingPieceEntry.getKey().y + 1); begY--  ) {
						retVal.add(new Point(target.x, begY));
					}
				}
			} 
			//horizontal path to king
			else if (dx > 0 && dy == 0) {
				//king is on an horizontal path to right of checking piece
				if ((target.x - checkingPieceEntry.getKey().x) > 0) {
					for (int begX = (checkingPieceEntry.getKey().x + 1); begX < target.x; begX++ ) {
						retVal.add(new Point(begX, target.y));
					}
				}
				//king is on an horizontal path to left of checking piece
				else {
					for (int begX = (target.x - 1); begX >=  (checkingPieceEntry.getKey().x + 1); begX--  ) {
						retVal.add(new Point(begX, target.y));
					}
				}
			}
		}
		else if(checkingType.equals(ChessPieceType.BISHOP) || checkingType.equals(ChessPieceType.QUEEN)) {
			// diagonal path to king
			if (dx == dy) {
				int begX = checkingPieceEntry.getKey().x + 1;
				int begY = checkingPieceEntry.getKey().y + 1;
				// king is on an upward rightward diagonal path from checking piece
				if (target.x - checkingPieceEntry.getKey().x > 0 && target.y - checkingPieceEntry.getKey().y > 0) {
					while (begX < target.x && begY < target.y) {
						retVal.add(new Point(begX++, begY++));
					}
				}
				// king is on a downward rightward diagonal path from checking piece
				else if (target.x - checkingPieceEntry.getKey().x > 0 && target.y - checkingPieceEntry.getKey().y < 0) {
					while (begX < target.x && begY > target.y) {
						retVal.add(new Point(begX++, begY--));
					}
				}
				// king is on an upward leftward diagonal path from checking piece
				else if (target.x - checkingPieceEntry.getKey().x < 0 && target.y - checkingPieceEntry.getKey().y > 0) {
					while (begX > target.x && begY < target.y) {
						retVal.add(new Point(begX--, begY++));
					}
				}
				// king is on downward leftward diagonal path from checking piece
				else if (target.x - checkingPieceEntry.getKey().x < 0 && target.y - checkingPieceEntry.getKey().y < 0) {
					while (begX > target.x && begY > target.y) {
						retVal.add(new Point(begX--, begY--));
					}
				}
			}
		}
		return retVal;
	}
	
	private boolean movePutsKingInCheck(Point current, Point target, boolean recordErrors) {
		// produce a "new" modified board based on move, get the King's location on this board
		// and determine if the move has put the King under check by calling KingInCheck()
		Map<Point, ChessPiece> modifiedBoard = new HashMap<Point, ChessPiece>();
		modifiedBoard.putAll(board);
		ChessPiece pieceToMove = board.get(current);
		modifiedBoard.put(target, pieceToMove);
		modifiedBoard.put(current, null);		
		Point kingLocation = null;
		for (Map.Entry<Point,ChessPiece> mapEntry : modifiedBoard.entrySet()) {
			if (mapEntry.getValue() != null && mapEntry.getValue().getType().equals( ChessPieceType.KING)  && mapEntry.getValue().getColor().equals(currentColor)) {
				kingLocation = mapEntry.getKey();
				break;
			}
		}
		boolean kingInCheckFlag = kingInCheck(kingLocation, modifiedBoard);
		if (kingInCheckFlag && recordErrors) {
			errorList.add(String.format(wrongMoveKingUnderCheck, 
	    			ChessUtil.longFormatChessPiece(pieceToMove),
	    			ChessUtil.formatPoint(current),
	    			ChessUtil.formatPoint(target)));
		}
		return kingInCheckFlag;
		
	}
	

	
	public boolean kingCheckMated(Point kingLocation) {
		// if king is currently under check and cannot make ANY of the otherwise-valid moves without
	    // remaining under check then this is checkmate.
		boolean checkMateFlag = true;
		if (kingInCheck(kingLocation)) {
			Point moveRightOne = new Point(kingLocation.x + 1, kingLocation.y);
			Point moveLeftOne = new Point(kingLocation.x - 1, kingLocation.y);
			Point moveUpOne = new Point(kingLocation.x, kingLocation.y + 1);
			Point moveDownOne = new Point(kingLocation.x, kingLocation.y - 1);
			Point moveDiagLeftUp = new Point(kingLocation.x - 1, kingLocation.y + 1);
			Point moveDiagLeftDown = new Point(kingLocation.x - 1, kingLocation.y - 1);
			Point moveDiagRightUp = new Point(kingLocation.x + 1, kingLocation.y + 1);
			Point moveDiagRightDown = new Point(kingLocation.x + 1, kingLocation.y - 1);
			
			if ( (boardBoundaryHonored(moveRightOne, false) && moveCanLandOnTarget(kingLocation,moveRightOne,false) && !kingInCheck(moveRightOne)) ||
				 (boardBoundaryHonored(moveLeftOne, false) && moveCanLandOnTarget(kingLocation,moveLeftOne,false) && !kingInCheck(moveLeftOne)) ||
				  (boardBoundaryHonored(moveUpOne, false) && moveCanLandOnTarget(kingLocation,moveUpOne,false) && !kingInCheck(moveUpOne)) ||
				   (boardBoundaryHonored(moveDownOne, false) && moveCanLandOnTarget(kingLocation,moveDownOne,false) && !kingInCheck(moveDownOne)) ||
				    (boardBoundaryHonored(moveDiagLeftUp, false) && moveCanLandOnTarget(kingLocation,moveDiagLeftUp,false) && !kingInCheck(moveDiagLeftUp)) ||
				     (boardBoundaryHonored(moveDiagLeftDown, false) && moveCanLandOnTarget(kingLocation,moveDiagLeftDown,false) && !kingInCheck(moveDiagLeftDown)) ||
				      (boardBoundaryHonored(moveDiagRightUp, false) && moveCanLandOnTarget(kingLocation,moveDiagRightUp,false) && !kingInCheck(moveDiagRightUp)) ||
				       (boardBoundaryHonored(moveDiagRightDown, false) && moveCanLandOnTarget(kingLocation,moveDiagRightDown,false) && !kingInCheck(moveDiagRightDown)) )
			{
				checkMateFlag = false;
			} 	
		}
		else {
			checkMateFlag = false;
		}
		
		if (checkMateFlag) {
			checkMateFlag = !checkIfPathToBlock(kingLocation);
		}
		
		return checkMateFlag;
	}
	
	public boolean canKingCastleWithLeftRook() {
		ChessPiece king;
		ChessPiece rook;
		int yCoord = (currentColor.equals(ChessPieceColor.WHITE) ? 0 : 7);
		king = board.get(new Point(4, yCoord));
		rook = board.get(new Point(0, yCoord));
		return (king != null && rook != null && king.getNumberOfMoves()== 0 && rook.getNumberOfMoves() == 0 && 
				board.get(new Point(1, yCoord )) == null && board.get(new Point(2, yCoord )) == null && board.get(new Point(3, yCoord )) == null);
		
		
	}
	
	public boolean canKingCastleWithRightRook() {
		
		ChessPiece king;
		ChessPiece rook;
		int yCoord = (currentColor.equals(ChessPieceColor.WHITE) ? 0 : 7);
		king = board.get(new Point(4, yCoord));
		rook = board.get(new Point(7, yCoord));
		return (king != null && rook != null && king.getNumberOfMoves()== 0 && rook.getNumberOfMoves() == 0 && 
				board.get(new Point(5, yCoord )) == null && board.get(new Point(6, yCoord )) == null); 
		
	}
	
	private boolean boardBoundaryHonored(Point target, boolean recordError) {
		boolean boundaryHonoredFlag;
		boundaryHonoredFlag = (target != null && target.x >= 0 && target.x <= 7 && target.y >= 0 && target.y <= 7 );
		if (!boundaryHonoredFlag && recordError) {
			errorList.add(String.format(wrongTargetOutOfBounds,
	    			ChessUtil.formatPoint(target)));
		}
		return boundaryHonoredFlag;
	}

}
