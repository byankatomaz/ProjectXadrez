package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece {
    private ColorT colorT;
    private int moveCount;

    public ChessPiece(Board board, ColorT colorT) {
        super(board);
        this.colorT = colorT;
    }

    public ColorT getColor() {
        return colorT;
    }

    public int getMoveCount(){ return moveCount; }

    public void increaseMoveCount(){
        moveCount++;
    }

    public void decreaseMoveCount(){
        moveCount--;
    }

    public ChessPosition getChessPosition(){
        return ChessPosition.fromPosition(position);
    }

    protected boolean isThereOpponentPiece(Position position){
        ChessPiece p = (ChessPiece) getBoard().piece(position);
        return p != null && p.getColor() != colorT;
    }
}
