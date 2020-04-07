package com.chess.engine.board;

public class Move {
    public int coordinate;
    public Move(Tile tile){
        this.coordinate=tile.tileCoordinate;
    }
}
