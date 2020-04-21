package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.*;
import com.chess.pgn.FenUtilities;
import com.chess.pgn.ParsePGNException;
import com.google.common.collect.Lists;

import static com.chess.engine.pieces.Piece.PieceType.*;
import static com.chess.pgn.PGNUtilities.persistPGNFile;
import static com.chess.pgn.PGNUtilities.writeGameToPGNFile;

import javax.swing.filechooser.FileFilter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.IOException;

import static javax.swing.SwingUtilities.*;

public class Table extends Observable {
    private final JFrame gameFrame = new MyFrame("Chess");
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final LogPanel logPanel;
    private final AnalyzePanel analyzePanel;
    private Board chessBoard = Board.createStandardBoard();
    private BoardDirection boardDirection = BoardDirection.NORMAL;
    private final MoveLog moveLog = new MoveLog();
    private final GameSetup gameSetup;
    private Move computerMove;
    private Move engineMove;
    private List<String> gameHistory = new ArrayList<>();
    private Engine analyzeEngine;
    private AIThinkTank aiThinkTank=new AIThinkTank();
    private JMenu prefMenu=createPreferencesMenu();

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private int currentPly = 0;

    private boolean findVariation=true;
    private boolean soundMuted=false;
    private boolean highlightLegals = true;
    private boolean engineOutput = true;
    private boolean highlightEngineMoves = true;
    private boolean engineStop = true;
    private boolean gameEngineIsRunning=false;
    private boolean analyzeEngineStop = true;
    private boolean textHasFocus=false;
    private boolean highlightNormalMoves=false;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(810, 740);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
    private final static Dimension LOG_PANEL_DIMENSION = new Dimension(400, 30);
    private final static Dimension ANALYZE_PANEL_DIMENSION = new Dimension(400, 100);
    private final static String defaultPieceImagePath = "/art/pieces/plain/";
    private final static String startingFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private Board startingBoard=Board.createStandardBoard();

    private final Color LIGHT_TILE_COLOR = Color.decode("#EDD0A7");
    private final Color DARK_TILE_COLOR = Color.decode("#7D5E49");

    private static final Table INSTANCE = new Table();

    private Table() {
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setLocation(400, 50);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gameFrame.setResizable(false);
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.gameHistory.add(startingFEN);
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.boardPanel = new BoardPanel();
        this.analyzePanel = new AnalyzePanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.logPanel = new LogPanel();
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.logPanel, BorderLayout.SOUTH);
        this.gameFrame.add(this.analyzePanel, BorderLayout.NORTH);
        this.gameFrame.setVisible(true);

        this.gameHistoryPanel.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                prefMenu.setPopupMenuVisible(false);
                prefMenu.setSelected(false);
                int row = gameHistoryPanel.getTable().rowAtPoint(evt.getPoint());
                int col = gameHistoryPanel.getTable().columnAtPoint(evt.getPoint());
                if (row >= 0 && col > 0) {
                    setPly(2*row+col);
                    updateGUI();
                }
            }
        });
    }

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        invokeLater(new Runnable() {
            public void run() {
                moveLog.clear();
                gameHistoryPanel.redo(chessBoard, moveLog, currentPly);
                boardPanel.drawBoard(chessBoard);
            }
        });
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private void setupUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private AIThinkTank getAIThinkTank(){
        return this.aiThinkTank;
    }

    private void setAIThinkTank(AIThinkTank aiThinkTank){
        this.aiThinkTank=aiThinkTank;
    }

    private void setGameEngineIsRunning(final boolean isRunning){
        this.gameEngineIsRunning=isRunning;
    }

    private BoardDirection getBoardDirection() {
        return this.boardDirection;
    }

    private void setEngineStop(boolean stop) {
        this.engineStop=stop;
        if(stop){
            logPanel.setStopTxt("Start");
            if(gameEngineIsRunning){
                gameEngineIsRunning=false;
                //aiThinkTank.gameEngine.cancel(true);
                Table.get().getAIThinkTank().getGameEngine().cancel(true);
            }
        }
        else{
            logPanel.setStopTxt("Stop");
        }
    }

    private void addPly() {
        this.currentPly++;
    }

    private void subtractPly() {
        if (this.currentPly > 0) {
            this.currentPly--;
        }
    }

    private void resetPly() {
        this.currentPly = 0;
    }

    private void setPly(int ply) {
        if (ply >= 0 && ply <= this.moveLog.size()) {
            this.currentPly = ply;
        }
    }

    private int getCurrentPly() {
        return this.currentPly;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private AnalyzePanel getAnalyzePanel() {
        return this.analyzePanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private boolean getStopped() {
        return this.engineStop;
    }

    private boolean isAnalyzeEngineStop(){
        return this.analyzeEngineStop;
    }

    private void setAnalyzeEngineStop(boolean stop){
        this.analyzeEngineStop=stop;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    public void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    public void updateEngineMove(final Move move) {
        this.engineMove = move;
    }

    public void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    public void updateGameHistory() {
        this.gameHistory.clear();
        Board transitionBoard = startingBoard;
        this.gameHistory.add(FenUtilities.createFENFromGame(transitionBoard));
        for (int i = 0; i < moveLog.size(); i++) {
            transitionBoard = transitionBoard.currentPlayer().makeMove(moveLog.getMoves().get(i)).getTransitionBoard();
            this.gameHistory.add(FenUtilities.createFENFromGame(transitionBoard));
        }
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {
        private MoveStrategy gameEngine;

        private AIThinkTank() {
        }

        public MoveStrategy getGameEngine(){
            return this.gameEngine;
        }

        @Override
        protected Move doInBackground() throws Exception {
            final MoveStrategy engine;
            final int ply=Table.get().getCurrentPly();
            if (Table.get().getGameBoard().currentPlayer().getAlliance().isWhite()) {
                if (Table.get().getGameSetup().getWhiteEngine().equals("Alpha Beta")) {
                    gameEngine = new StockAlphaBeta(Table.get().getGameSetup().getWhiteSearchDepth(),
                            Table.get().getGameSetup().getWhiteBoardEvaluator(), Table.get().getGameSetup().isViewWhiteVariation(),ply);
                } else {
                    gameEngine = new MiniMax(Table.get().getGameSetup().getWhiteSearchDepth(),
                            Table.get().getGameSetup().getWhiteBoardEvaluator(),Table.get().getGameSetup().isViewWhiteVariation(),ply);
                }
            } else {
                if (Table.get().getGameSetup().getBlackEngine().equals("Alpha Beta")) {
                    gameEngine = new StockAlphaBeta(Table.get().getGameSetup().getBlackSearchDepth(),
                            Table.get().getGameSetup().getBlackBoardEvaluator(), Table.get().getGameSetup().isViewBlackVariation(),ply);
                } else {
                    gameEngine = new MiniMax(Table.get().getGameSetup().getBlackSearchDepth(),
                            Table.get().getGameSetup().getBlackBoardEvaluator(), Table.get().getGameSetup().isViewBlackVariation(),ply);
                }
            }

            Table.get().setGameEngineIsRunning(true);
            final Move bestMove = gameEngine.execute(Table.get().getGameBoard());

            return bestMove;
        }

        @Override
        public void done() {
            if(!Table.get().getStopped()){
                try {
                    final Move bestMove = get();
                    Table.get().updateComputerMove(bestMove);
                    Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                    Table.get().getMoveLog().addMove(bestMove);
                    Table.get().moveMadeUpdate(PlayerType.COMPUTER);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Table.get().setGameEngineIsRunning(false);
        }
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        addPly();
        updateGameHistory();
        takenPiecesPanel.redo(moveLog, currentPly, boardDirection.isFlipped());
        gameHistoryPanel.redo(chessBoard, moveLog, currentPly);
        boardPanel.drawBoard(chessBoard);
        updateEngineMove(null);
        setChanged();
        notifyObservers(playerType);
        playSound();
        if(isGameOver()){
            printGameOver();
            setEngineStop(true);
        }
    }

    private static class TableGameAIWatcher implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                    !Table.get().isGameOver() && !Table.get().getStopped()) {
                //create an AI thread
                Table.get().setAIThinkTank(new AIThinkTank());
                //execute AI work
                Table.get().getAIThinkTank().execute();
            }
        }
    }

    private boolean isThreeFold() {
        for (final String position : gameHistory) {
            if (Collections.frequency(gameHistory, position) == 3) {
                //System.out.println(gameHistory);
                //return true;
            }
        }
        return false;
    }

    private void printGameOver(){
        if (chessBoard.currentPlayer().isInCheckMate()) {
            System.out.println("Game over, " + chessBoard.currentPlayer() + " is in checkmate");
        } else if (chessBoard.currentPlayer().isInStaleMate()) {
            System.out.println("Game over, " + chessBoard.currentPlayer() + " is in stalemate");
        } else if (chessBoard.insufficientMaterial()){
            System.out.println("Game over, insufficient material");
        }
        else if (isThreeFold()) {
            System.out.println("Game over, threefold repetition occurred in move "+(Table.get().getCurrentPly()+1)/2);
        }
    }

    private boolean isGameOver(){
        return chessBoard.currentPlayer().isInCheckMate() ||
                chessBoard.currentPlayer().isInStaleMate() ||
                chessBoard.insufficientMaterial() || isThreeFold();
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(prefMenu);
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem openPGN = new JMenuItem("Load PGN File", KeyEvent.VK_O);
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setEngineStop(true);
                JFileChooser chooser = new JFileChooser();
                int option = chooser.showOpenDialog(gameFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    loadPGNFile(chooser.getSelectedFile());
                }
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem saveToPGN = new JMenuItem("Save Game", KeyEvent.VK_S);
        saveToPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setEngineStop(true);
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return ".pgn";
                    }

                    @Override
                    public boolean accept(final File file) {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith("pgn");
                    }
                });
                final int option = chooser.showSaveDialog(gameFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    savePGNFile(chooser.getSelectedFile());
                }
            }
        });
        fileMenu.add(saveToPGN);

        final JMenuItem restartMenuItem = new JMenuItem("Restart");
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startingBoard=Board.createStandardBoard();
                humanMovedPiece = null;
                engineMove = null;
                computerMove=null;
                setEngineStop(true);
                analyzePanel.clearOutput();
                resetPly();
                moveLog.clear();
                chessBoard = Board.createStandardBoard();
                gameHistory.clear();
                gameHistory.add(startingFEN);
                invokeLater(new Runnable() {
                    public void run() {
                        gameHistoryPanel.redo(chessBoard, moveLog, currentPly);
                        takenPiecesPanel.redo(moveLog, currentPly, boardDirection.isFlipped());
                        boardPanel.drawBoard(chessBoard);
                    }
                });
            }
        });
        fileMenu.add(restartMenuItem);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameFrame.dispose();
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private static void loadPGNFile(final File pgnFile) {
        try {
            Table.get().resetPly();
            Table.get().getMoveLog().setLog(persistPGNFile(pgnFile));
            Table.get().updateGameHistory();
            Table.get().updateGameBoard(Board.createStandardBoard());
            Table.get().getAnalyzePanel().setOutPutStream();
            Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog(), 0);
            Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog(), Table.get().getCurrentPly(), Table.get().getBoardDirection().isFlipped());
            Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
        } catch (final IOException | ParsePGNException e) {
            e.printStackTrace();
        }
    }

    private static void savePGNFile(final File pgnFile) {
        try {
            writeGameToPGNFile(pgnFile, Table.get().getMoveLog(),
                    Table.get().getGameSetup().getWhitePlayerType() == PlayerType.COMPUTER ? Table.get().getGameSetup().getWhiteEngine() : "Human",
                    Table.get().getGameSetup().getBlackPlayerType() == PlayerType.COMPUTER ? Table.get().getGameSetup().getBlackEngine() : "Human");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                gameSetup.redo(boardDirection.isFlipped());
                takenPiecesPanel.redo(moveLog, currentPly, boardDirection.isFlipped());
                boardPanel.drawBoard(chessBoard);
                preferencesMenu.setPopupMenuVisible(false);
                preferencesMenu.setSelected(false);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);
        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight legal moves", true);
        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegals = legalMoveHighlighterCheckbox.isSelected();
                boardPanel.drawBoard(chessBoard);
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem engineHighlighterCheckbox = new JCheckBoxMenuItem("Highlight engine moves", true);
        engineHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightEngineMoves = engineHighlighterCheckbox.isSelected();
                boardPanel.drawBoard(chessBoard);
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem normalHighlighterCheckbox = new JCheckBoxMenuItem("Highlight normal moves", false);
        normalHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightNormalMoves = normalHighlighterCheckbox.isSelected();
                boardPanel.drawBoard(chessBoard);
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem engineCheckbox = new JCheckBoxMenuItem("View engine output", true);
        engineCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engineOutput = engineCheckbox.isSelected();
                analyzePanel.setOutPutStream();
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem soundCheckbox = new JCheckBoxMenuItem("Mute sounds", false);
        soundCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                soundMuted = soundCheckbox.isSelected();
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem variationCheckbox = new JCheckBoxMenuItem("Find best variation when analyzing", true);
        variationCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findVariation = variationCheckbox.isSelected();
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        preferencesMenu.add(legalMoveHighlighterCheckbox);
        preferencesMenu.add(normalHighlighterCheckbox);
        preferencesMenu.add(engineHighlighterCheckbox);
        preferencesMenu.add(engineCheckbox);
        preferencesMenu.add(variationCheckbox);
        preferencesMenu.add(soundCheckbox);

        preferencesMenu.addSeparator();
        final JMenuItem closeMenu = new JMenuItem("Close");
        closeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferencesMenu.setPopupMenuVisible(false);
                preferencesMenu.setSelected(false);
            }
        });
        preferencesMenu.add(closeMenu);

        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");
        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEngineStop(true);
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);
        return optionsMenu;
    }

    public enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }

            @Override
            boolean isFlipped() {
                return false;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }

            @Override
            boolean isFlipped() {
                return true;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);

        abstract BoardDirection opposite();

        abstract boolean isFlipped();
    }

    public static class MoveLog {
        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public void setLog(List<Move> moveList) {
            this.moves.clear();
            this.moves.addAll(moveList);
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            if (this.moves.size() > Table.get().getCurrentPly()) {
                List<Move> sub = new ArrayList<>();
                sub.addAll(this.moves.subList(0, Table.get().getCurrentPly()));
                this.moves.clear();
                this.moves.addAll(sub);
            }
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        public void clear() {
            this.moves.clear();
        }
    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }

    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();

            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    private class TilePanel extends JPanel {
        private final int tileId;
        private Image pieceImage;
        private Image dotImage;

        TilePanel(final BoardPanel boardPanel, final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;

            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    prefMenu.setPopupMenuVisible(false);
                    prefMenu.setSelected(false);
                    boolean chooseNewPiece=false;
                    if(chessBoard.getTile(tileId).getPiece()!=null) {
                        if (chessBoard.getTile(tileId).getPiece().getPieceAlliance() == chessBoard.currentPlayer().getAlliance()) {
                            chooseNewPiece = true;
                        }
                    }

                    if (isRightMouseButton(e) || (gameSetup.isAIPlayer(chessBoard.currentPlayer()) && !engineStop) || isGameOver() || !analyzeEngineStop) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    }
                    else if (isLeftMouseButton(e)) {
                        if (sourceTile == null || chooseNewPiece) {
                            sourceTile = chessBoard.getTile(tileId);
                            if(!(humanMovedPiece ==sourceTile.getPiece())){
                                humanMovedPiece = sourceTile.getPiece();
                            }
                            else{
                                humanMovedPiece=null;
                            }
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        }
                        else {
                            //second click, make the move
                            destinationTile = chessBoard.getTile(tileId);
                            Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
                            if (move.isPawnPromotion()) {
                                Object[] possibilities = {"Queen", "Rook", "Bishop", "Knight"};
                                String promotionPiece = (String) JOptionPane.showInputDialog(
                                        gameFrame,
                                        "Choose promotion piece",
                                        "Choose wisely",
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        possibilities,
                                        "Queen");
                                move = Move.MoveFactory.createPawnPromotionMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate(), promotionPiece);
                            }
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getTransitionBoard();
                                moveLog.addMove(move);
                                moveMadeUpdate(PlayerType.HUMAN);
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;
                        }
                    }
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            boardPanel.drawBoard(chessBoard);
                        }
                    });
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                }

                @Override
                public void mouseReleased(final MouseEvent e) {
                }

                @Override
                public void mouseEntered(final MouseEvent e) {
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                }
            });

            validate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (pieceImage != null) {
                g.drawImage(pieceImage,1, 0, this);
            }
            if (dotImage != null) {
                g.drawImage(dotImage, 28, 27, this);
            }
        }

        public void drawTile(final Board board) {
            pieceImage = null;
            dotImage = null;
            assignTileColor();
            assignTilePieceIcon(board);
            highlightTileBorder(chessBoard);
            highlightLegalMoves(chessBoard);
            highlightMoves();
            validate();
            repaint();
        }

        private void highlightTileBorder(final Board board) {
            if (humanMovedPiece != null &&
                    humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance() &&
                    humanMovedPiece.getPiecePosition() == this.tileId) {
                setBorder(BorderFactory.createLineBorder(Color.orange));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }

        private void highlightMoves() {
            if(highlightNormalMoves && (chessBoard.currentPlayer().getAlliance().isWhite() && gameSetup.getBlackPlayerType()==PlayerType.HUMAN
                    || chessBoard.currentPlayer().getAlliance().isBlack() && gameSetup.getWhitePlayerType()==PlayerType.HUMAN || computerMove==null || engineStop))
            {
                if(moveLog.size()>currentPly-1 && moveLog.size()>0 && currentPly>0){
                    Move lastMove=moveLog.getMoves().get(currentPly-1);
                    if (this.tileId == lastMove.getCurrentCoordinate()) {
                        setBackground(Color.orange);
                    } else if (this.tileId == lastMove.getDestinationCoordinate()) {
                        setBackground(Color.yellow);
                    }
                }
            }
            if (highlightEngineMoves) {
                if (engineMove != null) {
                    if (this.tileId == engineMove.getCurrentCoordinate()) {
                        setBackground(Color.cyan);
                    } else if (this.tileId == engineMove.getDestinationCoordinate()) {
                        setBackground(Color.blue);
                    }
                }

                if (!engineStop && computerMove != null) {
                    if (this.tileId == computerMove.getCurrentCoordinate()) {
                        setBackground(Color.pink);
                    } else if (this.tileId == computerMove.getDestinationCoordinate()) {
                        setBackground(Color.red);
                    }
                }
            }
        }

        private void highlightLegalMoves(final Board board) {
            if (highlightLegals) {
                for (final Move move : pieceLegalMoves(board)) {
                    MoveTransition transition = board.currentPlayer().makeMove(move);
                    if (!transition.getMoveStatus().isDone()) {
                        continue;
                    } else if (move.getDestinationCoordinate() == this.tileId) {
                        paintLegal();
                    }
                }
                if (humanMovedPiece != null) {
                    if (board.currentPlayer().getAlliance() == Alliance.WHITE && humanMovedPiece.getPieceType() == KING) {
                        if (board.currentPlayer().canCastleKingSideRightNow() && this.tileId == 62) {
                            paintLegal();
                        }
                        if (board.currentPlayer().canCastleQueenSideRightNow() && this.tileId == 58) {
                            paintLegal();
                        }
                    } else if (humanMovedPiece.getPieceType() == KING) {
                        if (board.currentPlayer().canCastleKingSideRightNow() && this.tileId == 6) {
                            paintLegal();
                        }
                        if (board.currentPlayer().canCastleQueenSideRightNow() && this.tileId == 2) {
                            paintLegal();
                        }
                    }
                }
            }
        }

        private void paintLegal() {
            try {
                dotImage = ImageIO.read(getClass().getResourceAsStream("/art/misc/green_dot.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Collection<Move> pieceLegalMoves(Board board) {
            if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                try {
                    final BufferedImage image =
                            ImageIO.read(getClass().getResourceAsStream(defaultPieceImagePath +
                                    board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0, 1) +
                                    board.getTile(this.tileId).getPiece().toString() + ".png"));
                    pieceImage = image.getScaledInstance(image.getWidth() - 8, image.getHeight() - 8, Image.SCALE_SMOOTH);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void assignTileColor() {
            if (BoardUtils.getWhitePositions().contains(this.tileId)) {
                setBackground(LIGHT_TILE_COLOR);
            } else {
                setBackground(DARK_TILE_COLOR);
            }
        }

    }

    private class LogPanel extends JPanel {
        final JButton backButton = new JButton("<");
        final JButton stopButton = new JButton("Start");
        final JButton forwardButton = new JButton(">");
        final JButton openFEN = new JButton("Load FEN");
        final JButton copyFEN = new JButton("Copy FEN");
        JTextField txtInput = new JTextField(startingFEN);

        LogPanel() {
            txtInput.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            prefMenu.setPopupMenuVisible(false);
                            prefMenu.setSelected(false);
                            txtInput.selectAll();
                            textHasFocus=true;
                        }
                    });
                }
                public void focusLost(java.awt.event.FocusEvent evt){
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            textHasFocus=false;
                        }
                    });
                }
            });

            add(copyFEN);
            add(backButton);
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setEngineStop(true);
                    goBack();
                }
            });

            add(stopButton);

            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(engineStop) {
                        if(gameSetup.isAIPlayer(chessBoard.currentPlayer()) && !isGameOver() && analyzeEngineStop){
                            setEngineStop(false);
                            Table.get().setAIThinkTank(new AIThinkTank());
                            Table.get().getAIThinkTank().execute();
                        }
                    }
                    else{
                        setEngineStop(true);
                    }
                    validate();
                }
            });

            add(forwardButton);
            forwardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setEngineStop(true);
                    goForward();
                }
            });

            openFEN.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (FenUtilities.createGameFromFEN(txtInput.getText()) != null) {
                        humanMovedPiece = null;
                        engineMove = null;
                        computerMove=null;
                        resetPly();
                        moveLog.clear();
                        gameHistory.clear();
                        startingBoard=FenUtilities.createGameFromFEN(txtInput.getText());
                        updateGameBoard(startingBoard);
                        gameHistory.add(txtInput.getText());
                    }
                    engineStop = true;
                    stopButton.setText("Start");

                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gameHistoryPanel.redo(chessBoard, moveLog, currentPly);
                            takenPiecesPanel.redo(moveLog, currentPly, boardDirection.isFlipped());
                            boardPanel.drawBoard(chessBoard);
                        }
                    });
                }
            });

            copyFEN.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String myString = FenUtilities.createFENFromGame(chessBoard);
                    StringSelection stringSelection = new StringSelection(myString);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            });

            add(openFEN);
            add(txtInput);
            setPreferredSize(LOG_PANEL_DIMENSION);
            validate();
        }

        public void setStopTxt(final String text){
            this.stopButton.setText(text);
        }
    }

    private class AnalyzePanel extends JPanel {
        final JButton analyzeBtn = new JButton("Start Analyzing");
        JSpinner depthSpinner;
        String[] boardEvaluatorChoices = {"Standard", "Modified"};
        String[] aiChoices = {"Alpha Beta", "Minimax"};
        final JComboBox<String> cb = new JComboBox<String>(boardEvaluatorChoices);
        final JComboBox<String> cb2 = new JComboBox<String>(aiChoices);
        BoardEvaluator boardEvaluator = new StandardBoardEvaluator();
        String chosenEngine = "Alpha Beta";
        MoveStrategy engine = new StockAlphaBeta(4, boardEvaluator, true, currentPly);
        JTextArea textArea = new JTextArea(3, 10);
        TextAreaOutputStream taOutputStream = new TextAreaOutputStream(textArea);
        String output = "";
        ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
        JScrollPane p = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        public void setAnalyzeBtn(String text){
            analyzeBtn.setText(text);
        }

        AnalyzePanel() {
            super(new BorderLayout());

            this.depthSpinner = addLabeledSpinner(this, "Search depth:", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
            add(depthSpinner, BorderLayout.EAST);

            cb2.setSelectedIndex(0);
            cb2.setVisible(true);
            add(cb2, BorderLayout.WEST);

            cb2.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JComboBox cb2 = (JComboBox) e.getSource();
                    chosenEngine = (String) cb2.getSelectedItem();
                }
            });

            cb.setSelectedIndex(0);
            cb.setVisible(true);
            add(cb, BorderLayout.CENTER);

            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String chosenEvaluator = (String) cb.getSelectedItem();
                    switch (chosenEvaluator) {
                        case "Standard":
                            boardEvaluator = new StandardBoardEvaluator();
                            break;
                        case "Modified":
                            boardEvaluator = new ModifiedBoardEvaluator();
                            break;
                    }
                }
            });

            add(analyzeBtn, BorderLayout.NORTH);
            analyzeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!analyzeEngineStop){
                        analyzeEngineStop=true;
                        analyzeBtn.setText("Start Analyzing");
                        analyzeEngine.getEngine().cancel(true);
                    }
                    else if(engineStop){
                        analyzeEngineStop=false;
                        analyzeBtn.setText("Stop Analyzing");
                        switch (chosenEngine) {
                            case "Alpha Beta":
                                engine = new StockAlphaBeta((Integer) depthSpinner.getValue(), boardEvaluator,findVariation,currentPly);
                                break;
                            case "Minimax":
                                engine = new MiniMax((Integer) depthSpinner.getValue(), boardEvaluator,findVariation,currentPly);
                                break;
                        }
                        analyzeEngine = new Engine(engine);
                        analyzeEngine.execute();
                    }
                }
            });

            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            add(p, BorderLayout.SOUTH);
            setOutPutStream();
            setPreferredSize(ANALYZE_PANEL_DIMENSION);
            validate();
        }

        public void setOutPutStream() {
            if (engineOutput) {
                textArea.setText(output + newConsole.toString().trim());
                System.setOut(new PrintStream(taOutputStream));
            } else {
                output = textArea.getText();

                System.setOut(new PrintStream(newConsole));
                textArea.setText("");
            }
            validate();
        }

        public void clearOutput() {
            textArea.setText("");
        }

        private JSpinner addLabeledSpinner(final Container c,
                                           final String label,
                                           final SpinnerModel model) {
            final JLabel l = new JLabel(label);
            c.add(l);
            final JSpinner spinner = new JSpinner(model);
            l.setLabelFor(spinner);
            c.add(spinner);
            return spinner;
        }
    }

    private static class Engine extends SwingWorker<Move, String> {
        final MoveStrategy engine;

        public Engine(MoveStrategy engine) {
            this.engine = engine;
        }

        public MoveStrategy getEngine(){
            return this.engine;
        }

        @Override
        protected Move doInBackground() throws Exception {
            return engine.execute(Table.get().getGameBoard());
        }

        @Override
        public void done() {
            if(!Table.get().isAnalyzeEngineStop()){
                try {
                    SoundUtils.playAnalysisDoneSound(Table.get().soundMuted);
                    Table.get().updateEngineMove(get());
                    Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Table.get().setAnalyzeEngineStop(true);
            Table.get().getAnalyzePanel().setAnalyzeBtn("Start Analyzing");
        }
    }
    private class MyFrame extends JFrame {
        private class MyDispatcher implements KeyEventDispatcher {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && !textHasFocus) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        setEngineStop(true);
                        goBack();
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
                        setEngineStop(true);
                        goForward();
                    }
                }
                return false;
            }
        }

        public MyFrame(final String s) {
            super(s);
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new MyDispatcher());
        }
    }

    private void goBack(){
        subtractPly();
        updateGUI();
    }

    private void goForward(){
        if (currentPly < moveLog.size()) {
            addPly();
        }
        updateGUI();
    }

    private void updateGUI(){
        engineMove=null;
        humanMovedPiece=null;
        computerMove=null;
        updateGameHistory();
        updateGameBoard(FenUtilities.createGameFromFEN(gameHistory.get(currentPly)));
        playSound();

        invokeLater(new Runnable() {
            @Override
            public void run() {
                gameHistoryPanel.redo(chessBoard, moveLog, currentPly);
                takenPiecesPanel.redo(moveLog, currentPly, boardDirection.isFlipped());
                boardPanel.drawBoard(chessBoard);
            }
        });
    }

    private void playSound(){
        if(currentPly>0 && !soundMuted){
            if(isGameOver()){
                SoundUtils.playGameOverSound();
            }
            else if(moveLog.getMoves().get(currentPly-1).isAttack()){
                SoundUtils.playCaptureSound();
            }
            else{
                SoundUtils.playMoveSound();
            }
        }
    }
}