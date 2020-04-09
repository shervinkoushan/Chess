package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Pawn extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATES={7,8,9,16};

    public Pawn(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.PAWN ,piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset:CANDIDATE_MOVE_COORDINATES){
            final int candidateDestinationCoordinate=this.piecePosition+(this.pieceAlliance.getDirection()*currentCandidateOffset);

            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                continue;
            }

            switch(currentCandidateOffset){
                case 7:
                    if(!(BoardUtils.getColumn(this.piecePosition)==8 && this.pieceAlliance.isWhite() ||
                            BoardUtils.getColumn(this.piecePosition)==1 && this.pieceAlliance.isBlack() )){
                        if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                            final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                            if(this.pieceAlliance != pieceOnCandidate.pieceAlliance){
                                legalMoves.add(new AttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                            }
                        }
                    }
                    break;
                case 8:
                    if (!board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                        legalMoves.add(new MajorMove(board,this,candidateDestinationCoordinate));
                    }
                    break;
                case 9:
                    if(!(BoardUtils.getColumn(this.piecePosition)==8 && this.pieceAlliance.isBlack() ||
                            BoardUtils.getColumn(this.piecePosition)==1 && this.pieceAlliance.isWhite() )){
                        if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                            final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                            if(this.pieceAlliance != pieceOnCandidate.pieceAlliance){
                                legalMoves.add(new AttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                            }
                        }
                    }
                    break;
                case 16:
                    if(BoardUtils.getRow(this.piecePosition)==7 && this.pieceAlliance.isBlack() ||
                            BoardUtils.getRow(this.piecePosition)==2 && this.pieceAlliance.isWhite()) {
                        final int behindCandidateDestinationCoordinate = this.piecePosition + this.pieceAlliance.getDirection() * 8;
                        if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                                !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                            legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
                    break;
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Pawn movePiece(final Move move) {
        return new Pawn(move.getDestinationCoordinate(),move.getMovedPiece().getPieceAlliance());
    }
}
