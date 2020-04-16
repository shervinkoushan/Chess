package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;

import javax.print.DocFlavor;

import static com.chess.engine.board.Move.*;

public class MiniMax implements MoveStrategy{
    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private long boardsEvaluated;
    private int movesTillWhiteMates=Integer.MIN_VALUE;
    private int movesTillBlackMates=Integer.MIN_VALUE;
    private int possibleMovesTillWhiteMates=Integer.MAX_VALUE;
    private int possibleMovesTillBlackMates=Integer.MAX_VALUE;


    public MiniMax(final int searchDepth, final BoardEvaluator boardEvaluator){
        this.boardEvaluator=boardEvaluator;
        this.searchDepth=searchDepth;
    }
    public MiniMax(final int searchDepth){
        this.boardEvaluator= new StandardBoardEvaluator();
        this.searchDepth=searchDepth;
        this.boardsEvaluated=0;
    }

    @Override
    public Move execute(Board board) {
        final long startTime=System.currentTimeMillis();
        final Player currentPlayer=board.currentPlayer();
        Move bestMove= MoveFactory.getNullMove();
        int highestSeenValue=Integer.MIN_VALUE;
        int lowestSeenValue=Integer.MAX_VALUE;
        int currentValue;
        int lastValue=0;
        System.out.println(board.currentPlayer()+" THINKING with depth = "+this.searchDepth+". "+this.boardEvaluator);
        int moveCounter=1;
        int numMoves=board.currentPlayer().getLegalMoves().size();

        for(final Move move:currentPlayer.getLegalMoves()){
            final MoveTransition moveTransition=board.currentPlayer().makeMove(move);
            final String s;
            if(moveTransition.getMoveStatus().isDone()){
                final long candidateMoveStartTime = System.nanoTime();
                currentValue=currentPlayer.getAlliance().isWhite() ?
                        min(moveTransition.getTransitionBoard(),this.searchDepth-1) :
                        max(moveTransition.getTransitionBoard(),this.searchDepth -1);

                if(currentPlayer.getAlliance().isWhite() && currentValue>= highestSeenValue){
                    highestSeenValue=currentValue;
                    lastValue=currentValue;
                    bestMove=move;
                    if(moveTransition.getTransitionBoard().blackPlayer().isInCheckMate()) {
                        break;
                    }
                }
                else if(currentPlayer.getAlliance().isBlack() && currentValue<=lowestSeenValue){
                    lowestSeenValue=currentValue;
                    lastValue=currentValue;
                    bestMove=move;
                    if(moveTransition.getTransitionBoard().whitePlayer().isInCheckMate()) {
                        break;
                    }
                }
                s = toString() + " [" +this.searchDepth+ "], m: (" +moveCounter+ "/" +numMoves+ ") " + move + ", best:  " + bestMove + " " +
                        score(currentPlayer, highestSeenValue, lowestSeenValue) + ", t: " +calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
            }
            else {
                s =toString() + " [" +this.searchDepth + "]" + ", m: (" +moveCounter+ "/" +numMoves+ ") " + move + " is illegal! best: " +bestMove;
            }
            System.out.println(s);
            moveCounter++;
        }
        final long executionTime=System.currentTimeMillis()-startTime;
        System.out.printf("%s SELECTS %s - [boards evaluated: %d, time taken: %d ms, rate: %.1f] - EVALUATION: %s\n", currentPlayer,
                bestMove, this.boardsEvaluated, executionTime, (1000 * ((double)this.boardsEvaluated/executionTime)),
                evaluation(this.movesTillWhiteMates,this.movesTillBlackMates,lastValue));
        return bestMove;
    }

    private static String evaluation(int movesTillWhiteMates, int movesTillBlackMates, int value){
        if(movesTillWhiteMates!=Integer.MIN_VALUE && value>20000){
            return "White mates in "+movesTillWhiteMates+" moves";
        }
        else if(movesTillBlackMates!= Integer.MIN_VALUE && value<20000){
            return "Black mates in "+movesTillBlackMates+" moves";
        }
        else{
            return ""+(((double) value) /100);
        }
    }


    private static String score(final Player currentPlayer,
                                final int highestSeenValue,
                                final int lowestSeenValue) {

        if(currentPlayer.getAlliance().isWhite()) {
            return "[score: " +highestSeenValue + "]";
        } else if(currentPlayer.getAlliance().isBlack()) {
            return "[score: " +lowestSeenValue+ "]";
        }
        throw new RuntimeException("Should not reach here");
    }

    private static String calculateTimeTaken(final long start, final long end) {
        final long timeTaken = (end - start) / 1000000;
        return timeTaken + " ms";
    }

    @Override
    public String toString(){
        return "MiniMax";
    }

    public int min(final Board board, final int depth){
        if(depth==0 || BoardUtils.isEndGame(board)){
            int currentDepth = this.searchDepth-depth;
            if(board.currentPlayer().isInCheckMate() && currentDepth<this.movesTillWhiteMates){
                this.movesTillWhiteMates=currentDepth;
            }
            this.boardsEvaluated++;
            return this.boardEvaluator.evaluate(board,depth);
        }
        int lowestSeenValue=Integer.MAX_VALUE;
        for(final Move move:board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, final int depth){
        if(depth==0 || BoardUtils.isEndGame(board)){
            int currentDepth = this.searchDepth-depth;
            if(board.currentPlayer().isInCheckMate() && currentDepth<this.possibleMovesTillBlackMates){
                this.possibleMovesTillBlackMates=currentDepth;
            }
            this.boardsEvaluated++;
            return this.boardEvaluator.evaluate(board,depth);
        }
        int highestSeenValue=Integer.MIN_VALUE;
        for(final Move move:board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }
        if(this.possibleMovesTillBlackMates>this.movesTillBlackMates && highestSeenValue<-20000){
            this.movesTillBlackMates=this.possibleMovesTillBlackMates;
        }
        return highestSeenValue;
    }
}
