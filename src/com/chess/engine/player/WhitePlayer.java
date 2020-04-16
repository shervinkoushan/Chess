package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;
import static com.chess.engine.pieces.Piece.PieceType.*;

public class WhitePlayer extends Player{
    public WhitePlayer(final Board board, final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board,whiteStandardLegalMoves,blackStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    public boolean isKingSideCastleCapable() {
        if(this.board.getTile(63).getPiece()!=null){
            return this.playerKing.isKingSideCastleCapable() &&
                    this.board.getTile(63).getPiece().getPieceType()==ROOK &&
                    this.board.getTile(63).getPiece().getPieceAlliance()==Alliance.WHITE &&
                    this.board.getTile(63).getPiece().isFirstMove();
        }
        return false;
    }

    @Override
    public boolean isQueenSideCastleCapable() {
        if(this.board.getTile(56).getPiece()!=null){
            return this.playerKing.isQueenSideCastleCapable() &&
                    this.board.getTile(56).getPiece().getPieceType()==ROOK &&
                    this.board.getTile(56).getPiece().getPieceAlliance()==Alliance.WHITE &&
                    this.board.getTile(56).getPiece().isFirstMove();
        }
        return false;
    }

    @Override
    public String toString(){
        return "White";
    }

}
