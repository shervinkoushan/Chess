package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

import static com.chess.engine.pieces.Piece.PieceType.KING;

public class Board {
    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;
    private final Pawn enPassantPawn;
    private final Move transitionMove;

    private Board(final Builder builder){
        this.gameBoard=createGameBoard(builder);
        this.whitePieces=calculateActivePieces(this.gameBoard,Alliance.WHITE);
        this.blackPieces=calculateActivePieces(this.gameBoard,Alliance.BLACK);
        this.enPassantPawn=builder.enPassantPawn;

        final Collection <Move> whiteStandardLegalMoves=calculateLegalMoves(this.whitePieces);
        final Collection <Move> blackStandardLegalMoves=calculateLegalMoves(this.blackPieces);

        this.whitePlayer= new WhitePlayer(this,whiteStandardLegalMoves,blackStandardLegalMoves);
        this.blackPlayer= new BlackPlayer(this,whiteStandardLegalMoves,blackStandardLegalMoves);
        this.currentPlayer=builder.nextMoveMaker.choosePlayer(this.whitePlayer,this.blackPlayer);
        this.transitionMove = builder.transitionMove != null ? builder.transitionMove : Move.MoveFactory.getNullMove();
    }

    public Move getTransitionMove() {
        return this.transitionMove;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for(int i=0; i<BoardUtils.NUM_TILES;i++){
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s",tileText));
            if((i+1)%BoardUtils.NUM_TILES_PER_ROW == 0){
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private Collection<Move> calculateLegalMoves(Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final Piece piece:pieces){
            final Collection<Move> possibleLegalMoves = piece.calculateLegalMoves(this);
            legalMoves.addAll(possibleLegalMoves);
        }

        return ImmutableList.copyOf(legalMoves);
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, final Alliance alliance){
        final List<Piece> pieces = new ArrayList<>();
        for(final Tile tile: gameBoard){
            if(tile.isTileOccupied()){
                final Piece piece=tile.getPiece();
                if(piece.getPieceAlliance()==alliance){
                    pieces.add(piece);
                }
            }
        }
        return ImmutableList.copyOf(pieces);
    }

    public boolean insufficientMaterial(){
        final List<Piece> pieces = new ArrayList<>();
        pieces.addAll(calculateActivePieces(this.gameBoard,Alliance.WHITE));
        pieces.addAll(calculateActivePieces(this.gameBoard,Alliance.BLACK));
        for(final Piece piece: pieces){
            if(piece.getPieceType()!= KING){
                return false;
            }
        }
        return true;
    }

    public Tile getTile(final int tileCoordinate){
        return gameBoard.get(tileCoordinate);
    }

    private static List<Tile> createGameBoard(final Builder builder){
        final Tile[] tiles=new Tile[BoardUtils.NUM_TILES];
        for(int i=0; i<BoardUtils.NUM_TILES;i++){
            tiles[i]=Tile.createTile(i, builder.boardConfig.get(i));
        }
        return ImmutableList.copyOf(tiles);
    }

    public Pawn getEnPassantPawn(){
        return this.enPassantPawn;
    }

    public Player blackPlayer(){
        return this.blackPlayer;
    }

    public Player whitePlayer(){
        return this.whitePlayer;
    }

    public Player currentPlayer() {
        return this.currentPlayer;
    }

    public static Board createStandardBoard(){
        final Builder builder=new Builder();

        builder.setPiece(new Rook(0,Alliance.BLACK));
        builder.setPiece(new Rook(7,Alliance.BLACK));
        builder.setPiece(new Knight(1,Alliance.BLACK));
        builder.setPiece(new Knight(6,Alliance.BLACK));
        builder.setPiece(new Bishop(2,Alliance.BLACK));
        builder.setPiece(new Bishop(5,Alliance.BLACK));
        builder.setPiece(new Queen(3,Alliance.BLACK));
        builder.setPiece(new King(4,Alliance.BLACK,true,true));
        for(int i=8;i<16;i++){
            builder.setPiece(new Pawn(i,Alliance.BLACK));
        }

        builder.setPiece(new Rook(56,Alliance.WHITE));
        builder.setPiece(new Rook(63,Alliance.WHITE));
        builder.setPiece(new Knight(57,Alliance.WHITE));
        builder.setPiece(new Knight(62,Alliance.WHITE));
        builder.setPiece(new Bishop(58,Alliance.WHITE));
        builder.setPiece(new Bishop(61,Alliance.WHITE));
        builder.setPiece(new Queen(59,Alliance.WHITE));
        builder.setPiece(new King(60,Alliance.WHITE,true,true));
        for(int i=48;i<56;i++){
            builder.setPiece(new Pawn(i,Alliance.WHITE));
        }

        builder.setMoveMaker(Alliance.WHITE);
        return builder.build();
    }

    public Collection<Piece> getBlackPieces() {
        return this.blackPieces;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    public Iterable<Move> getAllLegalMoves() {
        return Iterables.unmodifiableIterable(Iterables.concat(this.whitePlayer.getLegalMoves(),this.blackPlayer.getLegalMoves()));
    }


    public static class Builder{
        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;
        Move transitionMove;

        public Builder(){
            this.boardConfig=new HashMap<>();
        }

        public Builder setPiece(final Piece piece){
            this.boardConfig.put(piece.getPiecePosition(),piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker){
            this.nextMoveMaker=nextMoveMaker;
            return this;
        }

        public Builder setEnPassantPawn(final Pawn enPassantPawn) {
            this.enPassantPawn=enPassantPawn;
            return this;
        }

        public Board build(){
            return new Board(this);
        }

    }
}
