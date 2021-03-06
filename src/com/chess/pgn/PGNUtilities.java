package com.chess.pgn;

import com.chess.engine.board.Move;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.gui.Table.MoveLog;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chess.engine.board.Move.MoveFactory;

public class PGNUtilities {

    private static final Pattern PGN_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"(.*?)\"\\]$");
    private static final Pattern KING_SIDE_CASTLE = Pattern.compile("O-O#?\\+?");
    private static final Pattern QUEEN_SIDE_CASTLE = Pattern.compile("O-O-O#?\\+?");
    private static final Pattern PLAIN_PAWN_MOVE = Pattern.compile("^([a-h][0-8])(\\+)?(#)?$");
    private static final Pattern PAWN_ATTACK_MOVE = Pattern.compile("(^[a-h])(x)([a-h][0-8])(\\+)?(#)?$");
    private static final Pattern PLAIN_MAJOR_MOVE = Pattern.compile("^(B|N|R|Q|K)([a-h]|[1-8])?([a-h][0-8])(\\+)?(#)?$");
    private static final Pattern MAJOR_ATTACK_MOVE = Pattern.compile("^(B|N|R|Q|K)([a-h]|[1-8])?(x)([a-h][0-8])(\\+)?(#)?$");
    private static final Pattern PLAIN_PAWN_PROMOTION_MOVE = Pattern.compile("(.*?)=(.*?)");
    private static final Pattern ATTACK_PAWN_PROMOTION_MOVE = Pattern.compile("(.*?)x(.*?)=(.*?)");

    private PGNUtilities() {
        throw new RuntimeException("Not Instantiable!");
    }

    public static List<Move > persistPGNFile(final File pgnFile) throws IOException, ParsePGNException {

        int count = 0;
        int validCount = 0;
        Game game = null;
        String gameText="";

        try (final BufferedReader br = new BufferedReader(new FileReader(pgnFile))) {
            String line;
            PGNGameTags.TagsBuilder tagsBuilder = new PGNGameTags.TagsBuilder();
            StringBuilder gameTextBuilder = new StringBuilder();
            while((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (isTag(line)) {
                        final Matcher matcher = PGN_PATTERN.matcher(line);
                        if (matcher.find()) {
                            tagsBuilder.addTag(matcher.group(1), matcher.group(2));
                        }
                    }
                    else if (isEndOfGame(line)) {
                        final String[] ending = line.split(" ");
                        final String outcome = ending[ending.length - 1];
                        gameTextBuilder.append(line.replace(outcome, "")).append(" ");
                        gameText= gameTextBuilder.toString().trim();
                        if(!gameText.isEmpty()) {
                            game = GameFactory.createGame(tagsBuilder.build(), gameText, outcome);
                            count++;
                            //System.out.println("(" +(++count)+") Finished parsing " +game+ " count = " + (++count));
                            if(game.isValid()) {
                                validCount++;
                            }
                        }
                        gameTextBuilder = new StringBuilder();
                        tagsBuilder = new PGNGameTags.TagsBuilder();
                    }
                    else {
                        gameTextBuilder.append(line).append(" ");
                    }
                }
            }
            br.readLine();
        }
        System.out.println("Parsed " +count+ " games, valid = " +validCount+". Source: "+pgnFile+". ");
        if(game!=null){
            System.out.println("This is the last game in the file. Result: "+game.getWinner()+(game.getWinner()=="Tie" ? "" : " won"));
            System.out.println(game.toString());
        }
        return createMoveList(PGNUtilities.processMoveText(gameText));
    }

    public static void writeGameToPGNFile(final File pgnFile, final MoveLog moveLog,
                                          String whitePlayer, String blackPlayer) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(calculateEventString()).append("\n");
        builder.append(calculateSiteString()).append("\n");
        builder.append(calculateDateString()).append("\n");
        builder.append(calculateRoundString()).append("\n");
        builder.append(calculateWhiteString(whitePlayer)).append("\n");
        builder.append(calculateBlackString(blackPlayer)).append("\n");

        String result="1/2-1/2";
        int lastMove=moveLog.getMoves().size()-1;
        if(moveLog.getMoves().get(lastMove).toString().contains("#")){
            if(lastMove%2==0){
                result="1-0";
            }
            else{
                result="0-1";
            }
        }
        builder.append(calculateResultString(result)).append("\n");
        builder.append("\n");
        builder.append(calculatePlyCountString(moveLog)).append("\n");
        int counter=1;
        int halfstep=1;
        for(final Move move : moveLog.getMoves()) {
            halfstep++;
            if(halfstep==2){
                builder.append(counter+".");
                halfstep=0;
                counter++;
            }
            builder.append(move.toString()).append(" ");
        }
        builder.append(" "+result);
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pgnFile, true)))) {
            writer.write(builder.toString());
        }
    }

    private static String calculateResultString(String result) {
        return "[Result \""+result+"\"]";
    }

    private static String calculateBlackString(String blackPlayer) {
        return "[Black \""+blackPlayer+"\"]";
    }

    private static String calculateWhiteString(String whitePlayer) {
        return "[White \""+whitePlayer+"\"]";
    }

    private static String calculateRoundString() {
        return "[Round \"" +"1"+ "\"]";
    }

    private static String calculateSiteString() {
        return "[Site \""+"Your computer"+"\"]";
    }

    private static String calculateEventString() {
        return "[Event \"" +"Chess game"+ "\"]";
    }

    private static String calculateDateString() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return "[Date \"" + dateFormat.format(new Date()) + "\"]";
    }

    private static String calculatePlyCountString(final MoveLog moveLog) {
        return "[PlyCount \"" +moveLog.size() + "\"]";
    }

    public static List<String> processMoveText(final String gameText) throws ParsePGNException {
        return gameText.isEmpty() ? Collections.emptyList() : createMovesFromPGN(gameText);
    }

    private static List<String> createMovesFromPGN(final String pgnText) {
        if(!pgnText.startsWith("1.")) {
            return Collections.emptyList();
        }
        final List<String> sanitizedMoves = new LinkedList<>(Arrays.asList(
                removeParenthesis(pgnText).replaceAll(Pattern.quote("$") + "[0-9]+", "").replaceAll("[0-9]+\\s*\\.\\.\\.", "")
                        .split("\\s*[0-9]+" + Pattern.quote("."))));
        final List<String> processedData = removeEmptyText(sanitizedMoves);
        final String[] moveRows = processedData.toArray(new String[processedData.size()]);
        final ImmutableList.Builder<String> moves = new Builder<>();
        for(final String row : moveRows) {
            final String[] moveContent = removeWhiteSpace(row).split(" ");
            if(moveContent.length == 1) {
                moves.add(moveContent[0]);
            } else if(moveContent.length == 2) {
                moves.add(moveContent[0]);
                moves.add(moveContent[1]);
            } else {
                System.out.println("problem reading: " +pgnText+ " skipping!");
                return Collections.emptyList();
            }
        }
        return moves.build();
    }

    private static List<String> removeEmptyText(final List<String> moves) {
        final List<String> result = new ArrayList<>();
        for(final String moveText : moves) {
            if(!moveText.isEmpty()) {
                result.add(moveText);
            }
        }
        return result;
    }

    private static String removeWhiteSpace(final String row) {
        return row.trim().replaceAll("\\s+", " ");
    }

    private static boolean isTag(final String gameText) {
        return gameText.startsWith("[") && gameText.endsWith("]");
    }

    private static boolean isEndOfGame(final String gameText) {
        return gameText.endsWith("1-0") || gameText.endsWith("0-1") ||
               gameText.endsWith("1/2-1/2") || gameText.endsWith("*");
    }

    private static String removeParenthesis(final String gameText) {
        int parenthesisCounter=0;
        final StringBuilder builder = new StringBuilder();
        for (final char c : gameText.toCharArray()) {
            if (c == '(' || c == '{' ) {
                parenthesisCounter++;
            }
            if (c == ')' || c == '}' ) {
                parenthesisCounter--;
            }
            if (!(c == '(' || c == '{' || c == ')' || c == '}') && parenthesisCounter == 0) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static List<Move> createMoveList(List<String> stringList){
        Board board=Board.createStandardBoard();
        final List<Move> moveList = new ArrayList<>();
        for(String s: stringList){
            Move move=createMove(board,s);
            if(move!=null){
                moveList.add(move);
                board=board.currentPlayer().makeMove(move).getTransitionBoard();
            }
            else{
                break;
            }
        }
        return moveList;
    }

    public static Move createMove(final Board board,
                                  final String pgnText) {

        final Matcher kingSideCastleMatcher = KING_SIDE_CASTLE.matcher(pgnText);
        final Matcher queenSideCastleMatcher = QUEEN_SIDE_CASTLE.matcher(pgnText);
        final Matcher plainPawnMatcher = PLAIN_PAWN_MOVE.matcher(pgnText);
        final Matcher attackPawnMatcher = PAWN_ATTACK_MOVE.matcher(pgnText);
        final Matcher pawnPromotionMatcher = PLAIN_PAWN_PROMOTION_MOVE.matcher(pgnText);
        final Matcher attackPawnPromotionMatcher = ATTACK_PAWN_PROMOTION_MOVE.matcher(pgnText);
        final Matcher plainMajorMatcher = PLAIN_MAJOR_MOVE.matcher(pgnText);
        final Matcher attackMajorMatcher = MAJOR_ATTACK_MOVE.matcher(pgnText);

        int currentCoordinate;
        int destinationCoordinate;

        if(kingSideCastleMatcher.matches()) {
            return extractCastleMove(board, "O-O");
        } else if (queenSideCastleMatcher.matches()) {
            return extractCastleMove(board, "O-O-O");
        } else if(plainPawnMatcher.matches()) {
            final String destinationSquare = plainPawnMatcher.group(1);
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            currentCoordinate = deriveCurrentCoordinate(board, "P", destinationSquare, "");
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        } else if(attackPawnMatcher.matches()) {
            final String destinationSquare = attackPawnMatcher.group(3);
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            final String disambiguationFile = attackPawnMatcher.group(1) != null ? attackPawnMatcher.group(1) : "";
            currentCoordinate = deriveCurrentCoordinate(board, "P", destinationSquare, disambiguationFile);
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        } else if (attackPawnPromotionMatcher.matches()) {
            final String destinationSquare = attackPawnPromotionMatcher.group(2);
            final String disambiguationFile = attackPawnPromotionMatcher.group(1) != null ? attackPawnPromotionMatcher.group(1) : "";
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            currentCoordinate = deriveCurrentCoordinate(board, "P", destinationSquare, disambiguationFile);
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        } else if(pawnPromotionMatcher.find()) {
            final String destinationSquare = pawnPromotionMatcher.group(1);
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            currentCoordinate = deriveCurrentCoordinate(board, "P", destinationSquare, "");
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        } else if (plainMajorMatcher.find()) {
            final String destinationSquare = plainMajorMatcher.group(3);
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            final String disambiguationFile = plainMajorMatcher.group(2) != null ? plainMajorMatcher.group(2) : "";
            currentCoordinate = deriveCurrentCoordinate(board, plainMajorMatcher.group(1), destinationSquare, disambiguationFile);
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        } else if(attackMajorMatcher.find()) {
            final String destinationSquare = attackMajorMatcher.group(4);
            destinationCoordinate = BoardUtils.mapPosition(destinationSquare);
            final String disambiguationFile = attackMajorMatcher.group(2) != null ? attackMajorMatcher.group(2) : "";
            currentCoordinate = deriveCurrentCoordinate(board, attackMajorMatcher.group(1), destinationSquare, disambiguationFile);
            return MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        }

        return MoveFactory.getNullMove();

    }

    private static Move extractCastleMove(final Board board,
                                          final String castleMove) {
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            if (move.isCastlingMove() && move.toString().equals(castleMove)) {
                return move;
            }
        }
        return MoveFactory.getNullMove();
    }

    private static int deriveCurrentCoordinate(final Board board,
                                               final String movedPiece,
                                               final String destinationSquare,
                                               final String disambiguationFile) throws RuntimeException {
        final List<Move> currentCandidates = new ArrayList<>();
        final int destinationCoordinate =  BoardUtils.mapPosition(destinationSquare);
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            if (move.getDestinationCoordinate() == destinationCoordinate && move.getMovedPiece().toString().equals(movedPiece)) {
                currentCandidates.add(move);
            }
        }

        if(currentCandidates.size() == 0) {
            return -1;
        }

        return currentCandidates.size() == 1
                ? currentCandidates.iterator().next().getCurrentCoordinate()
                : extractFurther(currentCandidates, movedPiece, disambiguationFile);

    }

    private static int extractFurther(final List<Move> candidateMoves,
                                      final String movedPiece,
                                      final String disambiguationFile) {

        final List<Move> currentCandidates = new ArrayList<>();

        for(final Move move : candidateMoves) {
            if(move.getMovedPiece().getPieceType().toString().equals(movedPiece)) {
                currentCandidates.add(move);
            }
        }

        if(currentCandidates.size() == 1) {
            return currentCandidates.iterator().next().getCurrentCoordinate();
        }

        final List<Move> candidatesRefined = new ArrayList<>();

        for (final Move move : currentCandidates) {
            final String pos = BoardUtils.mapCoordinate(move.getCurrentCoordinate());
            if (pos.contains(disambiguationFile)) {
                candidatesRefined.add(move);
            }
        }

        if(candidatesRefined.size() == 1) {
            return candidatesRefined.iterator().next().getCurrentCoordinate();
        }

        return -1;

    }

}
