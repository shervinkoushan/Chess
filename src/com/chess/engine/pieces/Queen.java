package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static com.chess.engine.board.Move.*;

public class Queen extends Piece {
    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES={-9,-8,-7,-1,1,7,8,9};
    private static final Set<Integer> BISHOP_COORDINATES = new HashSet<Integer>(Arrays.asList(-9,-7,7,9));

    Queen(int piecePosition, Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int candidateCoordinateOffset:CANDIDATE_MOVE_VECTOR_COORDINATES){
            int candidateDestinationCoordinate=this.piecePosition;
            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                if(candidateDestinationCoordinate%8==0 && candidateCoordinateOffset==-1
                        || candidateDestinationCoordinate%8==7 && candidateCoordinateOffset==1){
                    break;
                }
                candidateDestinationCoordinate+=candidateCoordinateOffset;
                if(BISHOP_COORDINATES.contains(candidateCoordinateOffset) &&
                        !BoardUtils.sameColor(this.piecePosition,candidateDestinationCoordinate)){
                    break;
                }
                if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                    final Tile candidateDestinationTile= board.getTile(candidateDestinationCoordinate);
                    if(!candidateDestinationTile.isTileOccupied()){
                        legalMoves.add(new MajorMove(board,this,candidateDestinationCoordinate));
                    }
                    else{
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAlliance=pieceAtDestination.getPieceAlliance();
                        if(this.pieceAlliance!=pieceAlliance){
                            legalMoves.add(new AttackMove(board,this,candidateDestinationCoordinate,pieceAtDestination));
                        }
                        break;
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }
}
