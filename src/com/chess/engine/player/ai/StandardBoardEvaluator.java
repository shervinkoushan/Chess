package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

public final class StandardBoardEvaluator implements BoardEvaluator {
    private static final int CHECK_BONUS=50;
    private static final int CHECKMATE_BONUS=10000;
    private static final int DEPTH_BONUS=10;
    private static final int CASTLE_BONUS=60;
    private static final int MOBILITY_BONUS=30;

    @Override
    public String toString(){
        return "Standard Board Evaluator";
    }

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board,board.whitePlayer(),depth)-
                scorePlayer(board,board.blackPlayer(),depth);
    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        /*if(player.isInStaleMate() || player.getOpponent().isInStaleMate() || board.insufficientMaterial()){
            return 0;
        }*/
        return pieceValue(player) + mobility(player) + check(player) + checkMate(player,depth) + castled(player);
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
            pieceValueScore+=piece.getPieceValue();
        }
        return pieceValueScore;
    }
}
