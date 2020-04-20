package com.tests.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.*;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.ModifiedBoardEvaluator;
import com.chess.engine.player.ai.MoveStrategy;
import org.junit.jupiter.api.Test;

import static com.chess.engine.board.BoardUtils.mapCoordinate;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test
    public void initialBoard() {

        final Board board = Board.createStandardBoard();
        assertEquals(board.currentPlayer().getLegalMoves().size(), 20);
        assertEquals(board.currentPlayer().getOpponent().getLegalMoves().size(), 20);
        assertFalse(board.currentPlayer().isInCheck());
        assertFalse(board.currentPlayer().isInCheckMate());
        assertFalse(board.currentPlayer().isCastled());
/*        assertTrue(board.currentPlayer().isKingSideCastleCapable());
        assertTrue(board.currentPlayer().isQueenSideCastleCapable());*/
        assertEquals(board.currentPlayer(), board.whitePlayer());
        assertEquals(board.currentPlayer().getOpponent(), board.blackPlayer());
        assertFalse(board.currentPlayer().getOpponent().isInCheck());
        assertFalse(board.currentPlayer().getOpponent().isInCheckMate());
        assertFalse(board.currentPlayer().getOpponent().isCastled());
    }

    @Test
    public void testAlgebraicNotation() {
        assertEquals(mapCoordinate(0), "a8");
        assertEquals(mapCoordinate(1), "b8");
        assertEquals(mapCoordinate(2), "c8");
        assertEquals(mapCoordinate(3), "d8");
        assertEquals(mapCoordinate(4), "e8");
        assertEquals(mapCoordinate(5), "f8");
        assertEquals(mapCoordinate(6), "g8");
        assertEquals(mapCoordinate(7), "h8");
    }

    @Test
    public void mem() {
        final Runtime runtime = Runtime.getRuntime();
        long start, end;
        runtime.gc();
        start = runtime.freeMemory();
        Board board = Board.createStandardBoard();
        end =  runtime.freeMemory();
        System.out.println("That took " + (start-end) + " bytes.");

    }

    @Test
    public void testFoolsMate() {
        final Board board = Board.createStandardBoard();

        final MoveTransition t1 = board.currentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.mapPosition("f2"), BoardUtils.mapPosition("f3")));
        assertTrue(t1.getMoveStatus().isDone());

    }

    @Test
    public void testPieceValues() {
        final Board board = Board.createStandardBoard();
        ModifiedBoardEvaluator modifiedEvaluator=new ModifiedBoardEvaluator();
        //assertEquals(modifiedEvaluator.pieceValue(board.whitePlayer()),800+10000+900+2*500+700+600);
    }
}
