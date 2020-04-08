package com.chess.engine.board;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BoardUtils {
    private BoardUtils(){
        throw new RuntimeException("You cannot instantiate me!");
    }

    public static final int NUM_TILES=64;
    public static final int NUM_TILES_PER_ROW=8;
    private static final Set<Integer> WHITE_POSITIONS =
            new HashSet<Integer>(Arrays.asList(0,2,4,6,9,11,13,15,16,18,20,22,25,27,29,31,32,34,36,38,41,43,45,47,48,50,52,54,57,59,61,63));

    public static  Set<Integer>  getWhitePositions(){
        Set<Integer> WHITE_POSITIONS2= new HashSet<Integer>(Arrays.asList());
        int i=0;
        int count=0;
        boolean oddRow=false;
        while(i<NUM_TILES){
            WHITE_POSITIONS2.add(i);
            count++;
            if(count==4){
                count=0;
                if(oddRow){
                    i++;
                    oddRow=false;
                }
                else{
                    i+=3;
                    oddRow=true;
                }
            }
            else{
                i+=2;
            }
        }
        return WHITE_POSITIONS2;
    }

    public static int getRow(int coordinate){
        return 8-coordinate/8;
    }

    public static int getColumn(int coordinate){
        return (coordinate%8)+1;
    }

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
        return WHITE_POSITIONS.contains(coordinate);
    }


    public static boolean sameColor(final int coordinate, final int coordinate2){
        return isWhite(coordinate) == isWhite(coordinate2);
    }
}
