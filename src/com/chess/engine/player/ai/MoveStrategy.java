package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import javax.swing.*;

public abstract class MoveStrategy extends SwingWorker<Move,String>{
    public abstract Move execute(Board board);
}
