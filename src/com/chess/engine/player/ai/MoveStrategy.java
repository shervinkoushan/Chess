package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public interface MoveStrategy {
    Move execute(Board board);

    void setStatus(Move bestMove, int lastValue, long executionTime);

    double getValue();

    String getTimeLapsed();

    Move getSuggestedMove();
}
