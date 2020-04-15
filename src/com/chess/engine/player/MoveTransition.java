package com.chess.engine.player;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public class MoveTransition {
    private final Move move;
    private final MoveStatus moveStatus;
    private final Board transitionBoard;

    public MoveTransition(final Board transitionBoard,
                          final Move move,
                          final MoveStatus moveStatus){
        this.transitionBoard=transitionBoard;
        this.move=move;
        this.moveStatus=moveStatus;
    }

    public MoveStatus getMoveStatus(){
        return this.moveStatus;
    }

    public Board getTransitionBoard(){
        return this.transitionBoard;
    }

}
