package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

public class MiniMax implements MoveStrategy{
    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private Move suggestedMove;
    private String timeLapsed="";
    private double value=0;
    private boolean executionFinished=false;

    public MiniMax(final int searchDepth, final BoardEvaluator boardEvaluator){
        this.boardEvaluator=boardEvaluator;
        this.searchDepth=searchDepth;
    }
    public MiniMax(final int searchDepth){
        this.boardEvaluator= new StandardBoardEvaluator();
        this.searchDepth=searchDepth;
    }

    @Override
    public Move execute(Board board) {
        this.executionFinished=false;
        final long startTime=System.currentTimeMillis();
        Move bestMove=null;
        int highestSeenValue=Integer.MIN_VALUE;
        int lowestSeenValue=Integer.MAX_VALUE;
        int currentValue;
        int lastValue=0;

        System.out.println(board.currentPlayer()+" THINKING with depth = "+this.searchDepth+". "+this.boardEvaluator);

        int numMoves=board.currentPlayer().getLegalMoves().size();

        long inLoopTime=System.currentTimeMillis();
        for(final Move move:board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition=board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()){
                currentValue=board.currentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getTransitionBoard(),this.searchDepth-1) :
                        max(moveTransition.getTransitionBoard(),this.searchDepth -1);

                if(board.currentPlayer().getAlliance().isWhite() && currentValue>= highestSeenValue){
                    highestSeenValue=currentValue;
                    lastValue=currentValue;
                    bestMove=move;
                    System.out.println("Best move is "+bestMove+" - value "+highestSeenValue +
                            "\t Computation took "+(System.currentTimeMillis()-inLoopTime)+" ms"+
                            "\t Total time: "+(System.currentTimeMillis()-startTime)+" ms");
                    setStatus(bestMove,highestSeenValue,System.currentTimeMillis()-startTime);
                    inLoopTime=System.currentTimeMillis();
                }
                else if(board.currentPlayer().getAlliance().isBlack() && currentValue<=lowestSeenValue){
                    lowestSeenValue=currentValue;
                    lastValue=currentValue;
                    bestMove=move;
                    System.out.println("Best move is "+bestMove+" - value "+lowestSeenValue +
                            "\t Computation took "+(System.currentTimeMillis()-inLoopTime)+" ms"+
                            "\t Total time: "+(System.currentTimeMillis()-startTime)+" ms");
                    setStatus(bestMove,lowestSeenValue,System.currentTimeMillis()-startTime);
                    inLoopTime=System.currentTimeMillis();
                }
            }
        }
        final long executionTime=System.currentTimeMillis()-startTime;
        System.out.println("Took "+executionTime + " milliseconds");
        setStatus(bestMove,lastValue,executionTime);
        this.executionFinished=true;
        return bestMove;
    }

    @Override
    public void setStatus(Move bestMove, int lastValue, long executionTime) {
        this.suggestedMove=bestMove;
        this.value=((double) lastValue) /100;
        double s=executionTime/1000;
        if(s>0){
            this.timeLapsed=executionTime/1000+"s, "+executionTime%1000 +"ms";
        }
        else{
            this.timeLapsed=executionTime%1000 +" ms";
        }
    }

    public double getValue(){
        return this.value;
    }

    public String getTimeLapsed(){
        return this.timeLapsed;
    }

    public Move getSuggestedMove(){
        return this.suggestedMove;
    }

    @Override
    public boolean getExecutionFinished() {
        return this.executionFinished;
    }

    @Override
    public long getNumBoardsEvaluated() {
        return 0;
    }

    @Override
    public String toString(){
        return "MiniMax";
    }

    private static boolean isEndGameScenario(Board board){
        return board.currentPlayer().isInCheckMate() || board.currentPlayer().isInStaleMate();
    }

    public int min(final Board board, final int depth){
        if(depth==0 || isEndGameScenario(board)){
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
        if(depth==0 || isEndGameScenario(board)){
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
        return highestSeenValue;
    }
}
