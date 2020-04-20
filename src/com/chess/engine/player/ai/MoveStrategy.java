package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.Player;

import javax.swing.*;
import java.util.List;

public abstract class MoveStrategy extends SwingWorker<Move,String>{
    public abstract Move execute(Board board);

    protected static void printMate(List<Move> principalVariation, Player firstPlayer){
        int mateInNumberOfMoves=-1;
        for(int i=0;i<principalVariation.size();i++){
            if(principalVariation.get(i).toString().contains("#")){
                mateInNumberOfMoves=i+1;
                break;
            }
        }

        if(mateInNumberOfMoves!=-1){
            StringBuilder s= new StringBuilder();
            if(firstPlayer.getAlliance().isWhite()){
                if(mateInNumberOfMoves%2==1){
                    s.append("White mates in ");
                }
                else{
                    s.append("Black mates in ");
                }
            }
            else{
                if(mateInNumberOfMoves%2==1){
                    s.append("Black mates in ");
                }
                else{
                    s.append("White mates in ");
                }

            }
            s.append(mateInNumberOfMoves).append(" move");
            if(mateInNumberOfMoves>1){
                s.append("s");
            }
            System.out.println(s);
        }
    }


    protected void printPrincipalVariation(final List<Move> principalVariation, int currentPly, boolean whiteToMove) {
        StringBuilder s= new StringBuilder();
        if(!whiteToMove && !principalVariation.get(0).isNullMove()){
            s.append((currentPly+1)/2).append("...");
        }
        for(int i=0;i<principalVariation.size();i++){
            Move move=principalVariation.get(i);
            if(move.isNullMove()){
                break;
            }
            if((currentPly+1)%2==1){
                s.append((currentPly+2)/2).append(".");
            }
            if(!move.isNullMove()){
                s.append(move).append(" ");
            }
            currentPly++;
        }
        System.out.println(s);
    }

    protected static String score(final Player currentPlayer,
                                          final int highestSeenValue,
                                          final int lowestSeenValue) {

        if(currentPlayer.getAlliance().isWhite()) {
            return "[score: " +highestSeenValue + "]";
        } else if(currentPlayer.getAlliance().isBlack()) {
            return "[score: " +lowestSeenValue+ "]";
        }
        throw new RuntimeException("Should not reach here");
    }

    protected static String calculateTimeTaken(final long start, final long end) {
        final long timeTaken = (end - start) / 1000000;
        return timeTaken + " ms";
    }
}
