package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;

public abstract class Piece {
    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;

    Piece (final PieceType piecetype, final int piecePosition, final Alliance pieceAlliance){
        this.pieceType=piecetype;
        this.pieceAlliance=pieceAlliance;
        this.piecePosition=piecePosition;
    }

    public Alliance getPieceAlliance(){
        return this.pieceAlliance;
    }

    public int getPiecePosition(){
        return this.piecePosition;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public PieceType getPieceType(){
        return this.pieceType;
    }

    @Override
    public String toString() {
        return pieceType.toString();
    }

    public enum PieceType{
        PAWN("P"),
        KNIGHT("N"),
        BISHOP("B"),
        ROOK("R"),
        QUEEN("Q"),
        KING("K");

        private String pieceName;
        PieceType(final String pieceName){
            this.pieceName=pieceName;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }
    }
}
