package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.player.MoveTransition;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static com.chess.engine.board.Move.*;

public class King extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATES={-9,-8,-7,-1,1,7,8,9};
    private static final Set<Integer> EDGE_COORDINATES = new HashSet<Integer>(Arrays.asList(-9,-1,7));
    private final boolean queenSideCastleCapable;
    private final boolean kingSideCastleCapable;
    private final boolean isCastled;

    public King(final int piecePosition,
                final Alliance alliance,
                final boolean kingSideCastleCapable,
                final boolean queenSideCastleCapable) {
        super(PieceType.KING, piecePosition, alliance, true);
        this.isCastled = false;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
    }

    public King(final int piecePosition,
                final Alliance alliance,
                final boolean isFirstMove,
                final boolean isCastled,
                final boolean kingSideCastleCapable,
                final boolean queenSideCastleCapable) {
        super(PieceType.KING, piecePosition, alliance, isFirstMove);
        this.isCastled = isCastled;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
    }



    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset:CANDIDATE_MOVE_COORDINATES){
            final int candidateDestinationCoordinate=this.piecePosition+currentCandidateOffset;

            if(BoardUtils.getColumn(this.piecePosition)==1 && EDGE_COORDINATES.contains(currentCandidateOffset) ||
                    BoardUtils.getColumn(this.piecePosition)==8 && EDGE_COORDINATES.contains(-currentCandidateOffset) ){
                continue;
            }

            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                final Tile candidateDestinationTile= board.getTile(candidateDestinationCoordinate);
                if(!candidateDestinationTile.isTileOccupied()){
                    legalMoves.add(new MajorMove(board,this,candidateDestinationCoordinate));
                }
                else{
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    if(this.pieceAlliance!=pieceAtDestination.pieceAlliance){
                        legalMoves.add(new MajorAttackMove(board,this,candidateDestinationCoordinate,pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public King movePiece(final Move move) {
        return new King(move.getDestinationCoordinate(),move.getMovedPiece().getPieceAlliance(),false,move.isCastlingMove(),false,false);
    }

    public boolean isKingSideCastleCapable() {
        return this.kingSideCastleCapable;
    }

    public boolean isQueenSideCastleCapable() {
        return this.queenSideCastleCapable;
    }
}
