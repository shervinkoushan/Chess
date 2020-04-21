package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.*;

import static com.chess.engine.board.BoardUtils.mvvlva;
import static com.chess.engine.board.Move.MoveFactory;

public class StockAlphaBeta extends MoveStrategy {
    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private long boardsEvaluated;
    private long executionTime;
    private int quiescenceCount;
    private static final int MAX_QUIESCENCE = 5000;
    private int counter=0;
    private boolean firstLook=true;
    private boolean findVariation;
    private List<Move> principalVariation=new ArrayList<>();
    private Player firstPlayer;
    private int currentPly;

    @Override
    protected Move doInBackground() throws Exception {
        return null;
    }

    private enum MoveSorter {

        STANDARD {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(mvvlva(move2), mvvlva(move1))
                        .result()).immutableSortedCopy(moves);
            }
        },
        EXPENSIVE {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.kingThreat(move1), BoardUtils.kingThreat(move2))
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(mvvlva(move2), mvvlva(move1))
                        .result()).immutableSortedCopy(moves);
            }
        };

        abstract  Collection<Move> sort(Collection<Move> moves);
    }

    public StockAlphaBeta(final int searchDepth,final BoardEvaluator boardEvaluator, final boolean findVariation, final int currentPly) {
        this.evaluator = boardEvaluator;
        this.searchDepth = searchDepth;
        this.boardsEvaluated = 0;
        this.quiescenceCount = 0;
        this.findVariation=findVariation;
        this.currentPly=currentPly;
    }

    @Override
    public String toString() {
        return "Alpha Beta";
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        Move bestMove = MoveFactory.getNullMove();
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        int lastValue=0;
        if(firstLook){
            System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth+". "+this.evaluator+". Move "+(currentPly+2)/2);
            firstPlayer=currentPlayer;
        }
        int moveCounter = 1;
        int numMoves = board.currentPlayer().getLegalMoves().size();
        Collection<Move> sortedMoves = MoveSorter.EXPENSIVE.sort((board.currentPlayer().getLegalMoves()));
        for (final Move move : sortedMoves) {
            if(!isCancelled()){
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                this.quiescenceCount = 0;
                final String s;
                if (moveTransition.getMoveStatus().isDone()) {
                    final long candidateMoveStartTime = System.nanoTime();
                    currentValue = currentPlayer.getAlliance().isWhite() ?
                            min(moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue) :
                            max(moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
                    if (currentPlayer.getAlliance().isWhite() && currentValue > highestSeenValue) {
                        highestSeenValue = currentValue;
                        lastValue=currentValue;
                        bestMove = move;
                        if(moveTransition.getTransitionBoard().blackPlayer().isInCheckMate()) {
                            break;
                        }
                    }
                    else if (currentPlayer.getAlliance().isBlack() && currentValue < lowestSeenValue) {
                        lowestSeenValue = currentValue;
                        lastValue=currentValue;
                        bestMove = move;
                        if(moveTransition.getTransitionBoard().whitePlayer().isInCheckMate()) {
                            break;
                        }
                    }

                    final String quiescenceInfo = " " + score(currentPlayer, highestSeenValue, lowestSeenValue) + " q: " +this.quiescenceCount;
                    s = toString() + " [" +this.searchDepth+ "], m: (" +moveCounter+ "/" +numMoves+ ") " + move + ", best:  " + bestMove

                            + quiescenceInfo + ", t: " +calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
                } else {
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
                this.executionTime = System.currentTimeMillis() - startTime;
                System.out.printf("%s SELECTS %s - [boards evaluated: %d, time taken: %d ms, rate: %.1f] - EVALUATION: %.2f\n", board.currentPlayer(),
                        bestMove, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)),((double) lastValue) /100);
            }
        }
        else{
            System.out.println("Alpha Beta stopped");
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
                printMate(principalVariation,firstPlayer);
                printPrincipalVariation(principalVariation,currentPly,firstPlayer.getAlliance().isWhite());
            }
        }

        return bestMove;
    }

    private int max(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        int currentHighest = highest;
        if(!isCancelled()){
            if (depth == 0 || BoardUtils.isEndGame(board)) {
                this.boardsEvaluated++;
                return this.evaluator.evaluate(board, depth);
            }

            for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    Board toBoard = moveTransition.getTransitionBoard();
                    currentHighest = Math.max(currentHighest, min(toBoard,
                            calculateQuiescenceDepth(toBoard, depth), currentHighest, lowest));
                    if (currentHighest >= lowest) {
                        return lowest;
                    }
                }
            }
        }
        return currentHighest;
    }

    private int min(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        int currentLowest = lowest;
        if(!isCancelled()){
            if (depth == 0 || BoardUtils.isEndGame(board)) {
                this.boardsEvaluated++;
                return this.evaluator.evaluate(board, depth);
            }

            for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    Board toBoard = moveTransition.getTransitionBoard();
                    currentLowest = Math.min(currentLowest, max(toBoard,
                            calculateQuiescenceDepth(toBoard, depth), highest, currentLowest));
                    if (currentLowest <= highest) {
                        return highest;
                    }
                }
            }
        }
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final Board toBoard,
                                         final int depth) {
        if(depth == 1 && this.quiescenceCount < MAX_QUIESCENCE) {
            int activityMeasure = 0;
            if (toBoard.currentPlayer().isInCheck()) {
                activityMeasure += 1;
            }
            for(final Move move: BoardUtils.lastNMoves(toBoard, 2)) {
                if(move.isAttack()) {
                    activityMeasure += 1;
                }
            }
            if(activityMeasure >= 2) {
                this.quiescenceCount++;
                return 1;
            }
        }
        return depth - 1;
    }
}