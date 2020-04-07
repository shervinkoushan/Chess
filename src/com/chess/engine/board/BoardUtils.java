package com.chess.engine.board;

public class BoardUtils {
    private BoardUtils(){
        throw new RuntimeException("You cannot instantiate me!");
    }

    public static boolean isValidTileCoordinate(int coordinate)
    {
        return coordinate >=0 && coordinate <64;
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
}
