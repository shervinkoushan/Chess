package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.pieces.Piece.PieceType.*;

public abstract class Player {
    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(final Board board, final Collection<Move> legalMoves, final Collection <Move> opponentMoves){
        this.board=board;
        this.playerKing=establishKing();
        this.legalMoves=ImmutableList.copyOf(Iterables.concat(legalMoves,calculateKingCastles(legalMoves,opponentMoves)));
        this.isInCheck=!Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(),opponentMoves).isEmpty();
    }

    protected static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for(final Move move: moves){
            if(piecePosition==move.getDestinationCoordinate()){
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    private King establishKing() {
        for(final Piece piece: getActivePieces()){
            if(piece.getPieceType()== KING){
                return (King) piece;
            }
        }
        throw new RuntimeException("Should not reach here, not a valid board");
    }

    public boolean isMoveLegal(final Move move){
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck(){
        return this.isInCheck;
    }

    public boolean isInCheckMate(){
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate(){
        return !this.isInCheck && !hasEscapeMoves();
    }

    protected boolean hasEscapeMoves() {
        for (final Move move : this.legalMoves){
            final MoveTransition transition= makeMove(move);
            if(transition.getMoveStatus().isDone()){
                return true;
            }
        }
        return false;
    }

    public boolean isCastled(){
        return false;
    }

    public MoveTransition makeMove(final Move move){
        if(!isMoveLegal(move)){
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        final Board transitionBoard=move.execute();
        final Collection<Move> kingAttacks=calculateAttacksOnTile(transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
                transitionBoard.currentPlayer().getLegalMoves());

        if(!kingAttacks.isEmpty()){
            return new MoveTransition(this.board,move,MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }

        return new MoveTransition(transitionBoard,move,MoveStatus.DONE);
    }

    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    public King getPlayerKing() {
        return this.playerKing;
    }

    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();

    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        final int offset=this.getAlliance().isWhite() ? 56 : 0;

        if(this.playerKing.isFirstMove() && this.playerKing.getPiecePosition() == (4+offset) && !this.isInCheck()){
            if(!this.board.getTile(5+offset).isTileOccupied() && !this.board.getTile(6+offset).isTileOccupied()){
                final Tile rookTile=this.board.getTile(7+offset);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()){
                    if(Player.calculateAttacksOnTile(5+offset,opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(6+offset,opponentLegals).isEmpty() &&
                            rookTile.getPiece().getPieceType()== ROOK){
                        kingCastles.add(new Move.KingSideCastleMove(this.board,this.playerKing,6+offset,
                                (Rook)rookTile.getPiece(),rookTile.getTileCoordinate(),5+offset));
                    }
                }
            }

            if(!this.board.getTile(1+offset).isTileOccupied()
                    && !this.board.getTile(2+offset).isTileOccupied()
                    && !this.board.getTile(3+offset).isTileOccupied()){
                final Tile rookTile=this.board.getTile(0+offset);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()){
                    if(Player.calculateAttacksOnTile(2+offset,opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(3+offset,opponentLegals).isEmpty() &&
                            rookTile.getPiece().getPieceType()== ROOK){
                        kingCastles.add(new Move.QueenSideCastleMove(this.board,this.playerKing,2+offset,
                                (Rook)rookTile.getPiece(),rookTile.getTileCoordinate(),3+offset));
                    }
                }
            }
        }

        return ImmutableList.copyOf(kingCastles);
    }
}
