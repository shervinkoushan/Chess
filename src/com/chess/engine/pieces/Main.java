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
        Queen queen = new Queen(BoardUtils.mapPosition("e4"),Alliance.WHITE);
        Bishop bishop = new Bishop(BoardUtils.mapPosition("d5"),Alliance.WHITE);
        Rook rook = new Rook(BoardUtils.mapPosition("d4"),Alliance.WHITE);
        Collection <Move> moves =queen.calculateLegalMoves(board);
        for(Move move:moves){
            //System.out.println(BoardUtils.mapCoordinate(move.destinationCoordinate));
        }

    }

}
