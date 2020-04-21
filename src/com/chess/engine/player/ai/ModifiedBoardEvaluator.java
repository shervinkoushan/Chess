package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import java.util.Map;

import static com.chess.engine.pieces.Piece.PieceType.*;

public final class ModifiedBoardEvaluator implements BoardEvaluator {
    private static final int CHECK_BONUS=50;
    private static final int CHECKMATE_BONUS=10000;
    private static final int DEPTH_BONUS=10;
    private static final int CASTLE_BONUS=60;
    private static final int MOBILITY_BONUS=30;
    private static final double PAWN_ADVANCED_BONUS = 1.5;
    private static final int ATTACK_BONUS = 10;
    private static final int CAN_CASTLE_BONUS =5;

    private static final Map<Piece.PieceType,Integer> pieceValues=Map.of(PAWN, 100, KNIGHT,300, BISHOP, 300,ROOK,500,QUEEN,900,KING, 10000);

    @Override
    public String toString(){
        return "Modified Board Evaluator";
    }

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board,board.whitePlayer(),depth)-
                scorePlayer(board,board.blackPlayer(),depth);
    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        /*if(player.getOpponent().isInStaleMate() || player.isInStaleMate()){
            return 0;
        }*/
        return pieceValue(player) + mobility(player) + check(player) + checkMate(player,depth) +
                castled(player)+pawnAdvancement(player)+attacking(player)+canCastle(player);
    }

    private int canCastle(Player player) {
        if(player.isCastled()){
            return CAN_CASTLE_BONUS*2;
        }
        else{
            return (player.isKingSideCastleCapable() ? CAN_CASTLE_BONUS : 0) +
                    (player.isQueenSideCastleCapable() ? CAN_CASTLE_BONUS : 0);
        }
    }

    private int attacking(Player player) {
        int attackSum=0;
        for(final Move move:player.getLegalMoves()){
            if(move.isAttack()){
                attackSum+=ATTACK_BONUS;
            }
        }
        return attackSum;
    }


    private int pawnAdvancement(Player player) {
        int pawnReward=0;
        for(final Piece piece : player.getActivePieces()){
            if(piece.getPieceType()==PAWN){
                pawnReward+=player.getOpponent().getAlliance().getDirection()*BoardUtils.getRow(piece.getPiecePosition())*PAWN_ADVANCED_BONUS;
            }
        }
        return pawnReward;
    }

    private static int mobility(Player player){
        return player.getLegalMoves().size()*MOBILITY_BONUS;
    }

    private static int check(Player player){
        return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
    }

    private static int checkMate(Player player, int depth){
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS*depthBonus(depth) : 0;
    }

    private static int depthBonus(int depth){
        return DEPTH_BONUS * (depth+1);
    }

    private static int castled(Player player){
        return player.isCastled() ? CASTLE_BONUS : 0;
    }

    private static int pieceValue(final Player player){
        int pieceValueScore=0;
        for(final Piece piece : player.getActivePieces()){
            for (Map.Entry<Piece.PieceType, Integer> entry :pieceValues.entrySet()) {
                if (entry.getKey().equals(piece.getPieceType())) {
                    pieceValueScore+=entry.getValue();
                }
            }
        }
        return pieceValueScore;
    }
}
