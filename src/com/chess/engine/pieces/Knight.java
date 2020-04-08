package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;


public class Knight extends Piece{

    private final static int[] CANDIDATE_MOVE_COORDINATES={-17,-15,-10,-6,6,10,15,17};

    public Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }


    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES){
            final int candidateDestinationCoordinate=this.piecePosition+currentCandidateOffset;
            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){

                int columnOfPossiblePosition = BoardUtils.getColumn(candidateDestinationCoordinate);
                int columnOfCurrentPosition = BoardUtils.getColumn(this.piecePosition);

                if (Math.abs(columnOfCurrentPosition - columnOfPossiblePosition)>2){
                    continue;
                }

                final Tile candidateDestinationTile= board.getTile(candidateDestinationCoordinate);
                if(!candidateDestinationTile.isTileOccupied()){
                    legalMoves.add(new MajorMove(board,this,candidateDestinationCoordinate));
                }
                else{
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    if(this.pieceAlliance!=pieceAtDestination.pieceAlliance){
                        legalMoves.add(new AttackMove(board,this,candidateDestinationCoordinate,pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }
}
