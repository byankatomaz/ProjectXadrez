package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {
    private Board board;
    private int turn;
    private ColorT currentPlayer;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();


    public ChessMatch(){
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = ColorT.WHITE;
        initialSetup();
    }

    public int ChessMatchNumber(){
        board = new Board(8, 8);
        return board.getRows() * board.getColumns();
    }

    public int getTurn() {
        return turn;
    }

    public ColorT getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate(){
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable(){
        return enPassantVulnerable;
    }
    public ChessPiece getPromoted(){
        return promoted;
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition){

        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);

        return board.piece(position).possibleMoves();
    }

    public ChessPiece perfomeChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) throws IllegalAccessException {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);

        Piece capturedPiece = MakeMove(source, target);

        if (testCheck(currentPlayer)){
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece)board.piece(target);

        //specialmove promotion
        promoted = null;

        if (movedPiece instanceof Pawn){
            if ((movedPiece.getColor() == ColorT.WHITE && target.getRow() == 0) || (movedPiece.getColor() == ColorT.BLACK && target.getRow() == 7)){

                promoted = (ChessPiece)board.piece(target);
                promoted = replcaPromotedPiece("Q");

            }
        }

        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        //specialmove enPassant
        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)){
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replcaPromotedPiece(String type){
        if (promoted == null){
            throw new IllegalStateException("There is no piece tobe promoted");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")){
            throw new InvalidParameterException("Invalid type for promotion");
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;

    }

    private ChessPiece newPiece(String type, ColorT colorT){
        if (type.equals("B")) return new Bishop(board, colorT);
        if (type.equals("N")) return new Knight(board, colorT);
        if (type.equals("R")) return new Rook(board, colorT);
        return new Queen(board, colorT);

    }

    private Piece MakeMove(Position source, Position target){
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null){
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // #specialmove castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);

            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);

            rook.increaseMoveCount();
        }

        // #specialmove castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);

            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);

            rook.increaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn){
            if (source.getColumn() != target.getColumn() && capturedPiece == null){
                Position pawnPosition;

                if (p.getColor() == ColorT.WHITE){
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }

                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // #specialmove castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);

            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);

            rook.decreaseMoveCount();
        }

        // #specialmove castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);

            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);

            rook.decreaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn){
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable){
                ChessPiece pawn = (ChessPiece)board.removePiece(target);
                Position pawnPosition;

                if (p.getColor() == ColorT.WHITE){
                    pawnPosition = new Position(3, target.getColumn());
                } else {
                    pawnPosition = new Position(4, target.getColumn());
                }

                board.placePiece(pawn, pawnPosition);

            }
        }
    }

    private void validateSourcePosition(Position position){

        if (!board.thereIsAPiece(position)){
            throw new ChessException("There is no piece on source position");
        }

        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("The chosen piece is not your");
        }

        if (!board.piece(position).isThereAnyPossibleMove()){
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }

    private void validateTargetPosition(Position source, Position target){
        if (!board.piece(source).possibleMove(target)){
            throw new ChessException("The chosen piece can't move to target position");
        }
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == ColorT.WHITE) ? ColorT.BLACK : ColorT.WHITE;
    }

    private ColorT opponent(ColorT colorT){
        return (colorT == ColorT.WHITE) ? ColorT.BLACK : ColorT.WHITE;
    }

    private ChessPiece king(ColorT colorT) throws IllegalAccessException {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == colorT).collect(Collectors.toList());

        for (Piece p : list){
            if (p instanceof King){
                return (ChessPiece)p;
            }
        }
        throw new IllegalAccessException("There is no " + colorT + " king on the board");
    }

    private boolean testCheck(ColorT colorT) throws IllegalAccessException {
        Position kingPosition = king(colorT).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(colorT)).collect(Collectors.toList());

        for (Piece p : opponentPieces){
            boolean[][] mat = p.possibleMoves();

            if (mat[kingPosition.getRow()][kingPosition.getColumn()]){
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(ColorT colorT) throws IllegalAccessException {
        if (!testCheck(colorT)){
            return false;
        }

        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == colorT).collect(Collectors.toList());

        for (Piece p : list){
            boolean[][] mat = p.possibleMoves();

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);

                        Piece capturedPiece = MakeMove(source, target);

                        boolean testCheck = testCheck(colorT);

                        undoMove(source, target, capturedPiece);

                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;

    }

    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }



    private void initialSetup(){
        placeNewPiece('a', 1, new Rook(board, ColorT.WHITE));
        placeNewPiece('b', 1, new Knight(board, ColorT.WHITE));
        placeNewPiece('c', 1, new Bishop(board, ColorT.WHITE));
        placeNewPiece('d', 1, new King(board, ColorT.WHITE, this));
        placeNewPiece('e', 1, new Queen(board, ColorT.WHITE));
        placeNewPiece('f', 1, new Bishop(board, ColorT.WHITE));
        placeNewPiece('g', 1, new Knight(board, ColorT.WHITE));
        placeNewPiece('h', 1, new Rook(board, ColorT.WHITE));
        placeNewPiece('a', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, ColorT.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, ColorT.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, ColorT.BLACK));
        placeNewPiece('b', 8, new Knight(board, ColorT.BLACK));
        placeNewPiece('c', 8, new Bishop(board, ColorT.BLACK));
        placeNewPiece('d', 8, new King(board, ColorT.BLACK, this));
        placeNewPiece('e', 8, new Queen(board, ColorT.BLACK));
        placeNewPiece('f', 8, new Bishop(board, ColorT.BLACK));
        placeNewPiece('g', 8, new Knight(board, ColorT.BLACK));
        placeNewPiece('h', 8, new Rook(board, ColorT.BLACK));
        placeNewPiece('a', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, ColorT.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, ColorT.BLACK, this));

    }
}
