package com.chess.engine.board;

public class Board {
    public Tile getTile(final int tileCoordinate){
        return Tile.createTile(tileCoordinate, null);
    }
}
