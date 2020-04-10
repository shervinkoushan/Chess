package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;

public abstract class Piece {
    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    Piece (final PieceType piecetype, final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove){
        this.pieceType=piecetype;
        this.pieceAlliance=pieceAlliance;
        this.piecePosition=piecePosition;
        this.isFirstMove=isFirstMove;
        this.cachedHashCode=computeHashCode();
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result=31*result+pieceAlliance.hashCode();
        result=31*result+piecePosition;
        result=31*result + (isFirstMove ? 1:0);
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if(this==other){
            return true;
        }
        if(!(other instanceof Piece)){
            return false;
        }
        final Piece otherPiece=(Piece) other;
        return piecePosition==otherPiece.getPiecePosition() && pieceType==otherPiece.getPieceType() &&
                pieceAlliance==otherPiece.getPieceAlliance() && isFirstMove == otherPiece.isFirstMove();
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public Alliance getPieceAlliance(){
        return this.pieceAlliance;
    }

    public int getPiecePosition(){
        return this.piecePosition;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public abstract Piece movePiece(Move move);

    public PieceType getPieceType(){
        return this.pieceType;
    }

    @Override
    public String toString() {
        return pieceType.toString();
    }

    public int getPieceValue() {
        return this.pieceType.getPieceValue();
    }

    public enum PieceType{
        PAWN("P",100),
        KNIGHT("N",300),
        BISHOP("B",300),
        ROOK("R",500),
        QUEEN("Q",900),
        KING("K",10000);

        private String pieceName;
        private int pieceValue;

        PieceType(final String pieceName,final int pieceValue){
            this.pieceName=pieceName;
            this.pieceValue=pieceValue;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        public int getPieceValue() {
            return this.pieceValue;
        }
    }
}
