package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class MiniMax extends MoveStrategy {
    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private long boardsEvaluated;
    private int counter=0;
    private List<Move> principalVariation=new ArrayList<>();
    private boolean firstLook=true;
    private boolean findVariation;

    public MiniMax(final int searchDepth, final BoardEvaluator boardEvaluator){
        this.boardEvaluator=boardEvaluator;
        this.searchDepth=searchDepth;
        this.findVariation=false;
    }
    public MiniMax(final int searchDepth, final BoardEvaluator boardEvaluator, final boolean findVariation){
        this.boardEvaluator=boardEvaluator;
        this.searchDepth=searchDepth;
        this.findVariation=findVariation;
    }
    public MiniMax(final int searchDepth){
        this.boardEvaluator= new StandardBoardEvaluator();
        this.searchDepth=searchDepth;
        this.boardsEvaluated=0;
        this.findVariation=false;
    }

    @Override
    protected Move doInBackground() throws Exception {
        return null;
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
        if(firstLook){
            System.out.println(board.currentPlayer()+" THINKING with depth = "+this.searchDepth+". "+this.boardEvaluator);
        }
        int moveCounter=1;
        int numMoves=board.currentPlayer().getLegalMoves().size();

        for(final Move move:currentPlayer.getLegalMoves()){
            if(!isCancelled()){
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
                if(firstLook){
                    System.out.println(s);
                }
                moveCounter++;
            }
        }
        if(!isCancelled()){
            if(firstLook){
                final long executionTime=System.currentTimeMillis()-startTime;
                System.out.printf("%s SELECTS %s - [boards evaluated: %d, time taken: %d ms, rate: %.1f] - EVALUATION: %.2f\n", currentPlayer,
                        bestMove, this.boardsEvaluated, executionTime, (1000 * ((double)this.boardsEvaluated/executionTime)),
                        ((double) lastValue) /100);
            }
        }
        else{
            System.out.println("MiniMax stopped");
        }
        counter++;
        if(firstLook){
            firstLook=false;
        }
        principalVariation.add(bestMove);
        if(findVariation){
            if(counter<this.searchDepth){
                execute(board.currentPlayer().makeMove(bestMove).getTransitionBoard());
            }
            else{
                printPrincipalVariation();
            }
        }
        return bestMove;
    }

    private void printPrincipalVariation() {
        StringBuilder s= new StringBuilder();
        for(Move move:principalVariation){
            if(!move.isNullMove()){
                s.append(move).append(" ");
            }
        }
        System.out.println(s);
    }


    public int min(final Board board, final int depth){
        int lowestSeenValue=Integer.MAX_VALUE;
        if(!isCancelled()){
            if(depth==0 || BoardUtils.isEndGame(board)){
                this.boardsEvaluated++;
                return this.boardEvaluator.evaluate(board,depth);
            }

            for(final Move move:board.currentPlayer().getLegalMoves()) {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
                    if (currentValue <= lowestSeenValue) {
                        lowestSeenValue = currentValue;
                    }
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, final int depth){
        int highestSeenValue=Integer.MIN_VALUE;
        if(!isCancelled()){
            if(depth==0 || BoardUtils.isEndGame(board)){
                this.boardsEvaluated++;
                return this.boardEvaluator.evaluate(board,depth);
            }

            for(final Move move:board.currentPlayer().getLegalMoves()) {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
                    if (currentValue >= highestSeenValue) {
                        highestSeenValue = currentValue;
                    }
                }
            }
        }
        return highestSeenValue;
    }

    private static String evaluation(int movesTillWhiteMates, int movesTillBlackMates, int value){
        if(movesTillWhiteMates!=Integer.MAX_VALUE && value>20000){
            return "White mates in "+movesTillWhiteMates+" moves";
        }
        else if(movesTillBlackMates!= 0 && value<-20000){
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
}
