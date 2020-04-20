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
    private Player firstPlayer;

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
            firstPlayer=currentPlayer;
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
                printMate(principalVariation, firstPlayer);
                printPrincipalVariation(principalVariation);
            }
        }
        return bestMove;
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

    @Override
    public String toString(){
        return "MiniMax";
    }
}
