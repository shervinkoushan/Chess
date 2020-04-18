package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Pawn extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATES={7,8,9,16};

    public Pawn(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.PAWN ,piecePosition, pieceAlliance, true);
    }

    public Pawn(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.PAWN ,piecePosition, pieceAlliance, isFirstMove);
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
                            BoardUtils.getColumn(this.piecePosition)==1 && this.pieceAlliance.isBlack())){
                        if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                            final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                            if(this.pieceAlliance != pieceOnCandidate.pieceAlliance){
                                if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                                    legalMoves.addAll(pawnPromotionList(new PawnAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate)));
                                }
                                else{
                                    legalMoves.add(new PawnAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                                }
                            }
                        }
                        else if(board.getEnPassantPawn()!=null){
                            if(board.getEnPassantPawn().getPiecePosition() == (this.piecePosition -this.pieceAlliance.getDirection())){
                                final Piece pieceOnCandidate=board.getEnPassantPawn();
                                if(this.pieceAlliance!=pieceOnCandidate.getPieceAlliance()){
                                    legalMoves.add(new PawnEnPassantAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                                }
                            }
                        }
                    }
                    break;
                case 8:
                    if (!board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                            legalMoves.addAll(pawnPromotionList(new PawnMove(board,this,candidateDestinationCoordinate)));
                        }
                        else{
                            legalMoves.add(new PawnMove(board,this,candidateDestinationCoordinate));
                        }
                    }
                    break;
                case 9:
                    if(!(BoardUtils.getColumn(this.piecePosition)==8 && this.pieceAlliance.isBlack() ||
                            BoardUtils.getColumn(this.piecePosition)==1 && this.pieceAlliance.isWhite())){
                        if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                            final Piece pieceOnCandidate=board.getTile(candidateDestinationCoordinate).getPiece();
                            if(this.pieceAlliance != pieceOnCandidate.pieceAlliance){
                                if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                                    legalMoves.addAll(pawnPromotionList(new PawnAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate)));
                                }
                                else{
                                    legalMoves.add(new PawnAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                                }
                            }
                        }
                        else if(board.getEnPassantPawn()!=null){
                            if(board.getEnPassantPawn().getPiecePosition() == (this.piecePosition + this.pieceAlliance.getDirection())){
                                final Piece pieceOnCandidate=board.getEnPassantPawn();
                                if(this.pieceAlliance!=pieceOnCandidate.getPieceAlliance()){
                                    legalMoves.add(new PawnEnPassantAttackMove(board,this,candidateDestinationCoordinate,pieceOnCandidate));
                                }
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
                            legalMoves.add(new PawnJump(board, this, candidateDestinationCoordinate));
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

    private Piece getQueenPromotionPiece(){
        return new Queen(this.piecePosition,this.pieceAlliance,false);
    }

    private Piece getRookPromotionPiece(){
        return new Rook(this.piecePosition,this.pieceAlliance,false);
    }

    private Piece getBishopPromotionPiece(){
        return new Bishop(this.piecePosition,this.pieceAlliance,false);
    }

    private Piece getKnightPromotionPiece(){
        return new Knight(this.piecePosition,this.pieceAlliance,false);
    }

    private List<Move> pawnPromotionList(Move decoratedMove){
        final List<Move> pawnPromotions=new ArrayList<>();
        pawnPromotions.add(new PawnPromotion(decoratedMove,getQueenPromotionPiece()));
        pawnPromotions.add(new PawnPromotion(decoratedMove,getRookPromotionPiece()));
        pawnPromotions.add(new PawnPromotion(decoratedMove,getBishopPromotionPiece()));
        pawnPromotions.add(new PawnPromotion(decoratedMove,getKnightPromotionPiece()));
        return pawnPromotions;
    }
}
