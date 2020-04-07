package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Knight;

import java.util.Collection;

public class Main {

    public static void main(String[] args){
        Board board=new Board();
        Knight knight=new Knight((BoardUtils.mapPosition("h7")), Alliance.BLACK);
        Collection<Move> moves =knight.calculateLegalMoves(board);

        for(Move move:moves){
            System.out.println(BoardUtils.mapCoordinate(move.coordinate));
        }
    }

}
