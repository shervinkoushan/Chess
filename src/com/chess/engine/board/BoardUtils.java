package com.chess.engine.board;

public class BoardUtils {
    private BoardUtils(){
        throw new RuntimeException("You cannot instantiate me!");
    }

    public static final int NUM_TILES=64;
    public static final int NUM_TILES_PER_ROW=8;

    public static boolean isValidTileCoordinate(final int coordinate)
    {
        return coordinate >=0 && coordinate <NUM_TILES;
    }

    public static String mapCoordinate(int coordinate){
        String letters[]={"a","b","c","d","e","f","g","h"};
        String position=letters[coordinate%8]+(8-coordinate/8);
        return position;
    }

    public static int mapPosition(String position){
        int coordinate=0;
        String letters[]={"a","b","c","d","e","f","g","h"};
        for(int i=0;i<letters.length;i++){
            if(Character.toString(position.charAt(0)).equals(letters[i])){
                coordinate+=i;
                break;
            }
        }
        coordinate+=(8-Character.getNumericValue(position.charAt(1)))*8;
        return coordinate;
    }

    public static boolean isWhite(final int coordinate){
        return coordinate%2 == 0;
    }
}
