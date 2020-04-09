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
    private final static int[] CANDIDATE_MOVE_COORDINATES={-9,-8,-7,-1,1,7,8,9};
    private static final Set<Integer> BISHOP_COORDINATES = new HashSet<Integer>(Arrays.asList(-9,-7,7,9));

    public Queen(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.QUEEN ,piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset:CANDIDATE_MOVE_COORDINATES){
            int candidateDestinationCoordinate=this.piecePosition;
            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                if(BoardUtils.getColumn(candidateDestinationCoordinate)==1 && currentCandidateOffset==-1
                        || BoardUtils.getColumn(candidateDestinationCoordinate)==8 && currentCandidateOffset==1){
                    break;
                }
                candidateDestinationCoordinate+=currentCandidateOffset;
                if(BISHOP_COORDINATES.contains(currentCandidateOffset) &&
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
                        if(this.pieceAlliance!=pieceAtDestination.pieceAlliance){
                            legalMoves.add(new AttackMove(board,this,candidateDestinationCoordinate,pieceAtDestination));
                        }
                        break;
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Queen movePiece(final Move move) {
        return new Queen(move.getDestinationCoordinate(),move.getMovedPiece().getPieceAlliance());
    }
}
