package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;
import static com.chess.engine.pieces.Piece.PieceType.*;

public class BlackPlayer extends Player{
    public BlackPlayer(final Board board, final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board,blackStandardLegalMoves,whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public boolean isKingSideCastleCapable() {
        if(this.board.getTile(7).getPiece()!=null){
            return this.playerKing.isKingSideCastleCapable() &&
                    this.board.getTile(7).getPiece().getPieceType()==ROOK &&
                    this.board.getTile(7).getPiece().getPieceAlliance()==Alliance.BLACK &&
                    this.board.getTile(7).getPiece().isFirstMove();
        }
        return false;
    }

    @Override
    public boolean isQueenSideCastleCapable() {
        if(this.board.getTile(0).getPiece()!=null){
            return this.playerKing.isQueenSideCastleCapable() &&
                    this.board.getTile(0).getPiece().getPieceType()==ROOK &&
                    this.board.getTile(0).getPiece().getPieceAlliance()==Alliance.BLACK &&
                    this.board.getTile(0).getPiece().isFirstMove();
        }
      return false;
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    public String toString(){
        return "Black";
    }
}

