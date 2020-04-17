package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.*;
import com.chess.pgn.FenUtilities;
import com.google.common.collect.Lists;

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

import static javax.swing.SwingUtilities.*;

public class Table extends Observable {
    private final JFrame gameFrame=new JFrame("JChess");
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final LogPanel logPanel;
    private final AnalyzePanel analyzePanel;
    private Board chessBoard=Board.createStandardBoard();
    private BoardDirection boardDirection=BoardDirection.NORMAL;
    private final MoveLog moveLog=new MoveLog();
    private final MoveLog secondaryMoveLog=new MoveLog();
    private final GameSetup gameSetup;
    private Move computerMove;
    private Move engineMove;
    private List<String> gameHistory=new ArrayList<>();
    private List<String> secondaryGameHistory=new ArrayList<>();

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;

    private boolean highlightLegals=true;
    private boolean engineOutput=true;
    private boolean highlightEngineMoves=true;
    private boolean engineStop=true;

    private final static Dimension OUTER_FRAME_DIMENSION=new Dimension(800,740);
    private final static Dimension BOARD_PANEL_DIMENSION=new Dimension(400,350);
    private final static Dimension TILE_PANEL_DIMENSION=new Dimension(10,10);
    private final static Dimension LOG_PANEL_DIMENSION =new Dimension(400,30);
    private final static Dimension ANALYZE_PANEL_DIMENSION =new Dimension(400,100);
    private final static String defaultPieceImagePath="art/pieces/plain/";
    private final static String startingFEN="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final Color LIGHT_TILE_COLOR = Color.decode("#EDD0A7");
    private final Color DARK_TILE_COLOR = Color.decode("#7D5E49");

    private static final Table INSTANCE=new Table();

    private Table(){
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setLocation(400,50);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gameFrame.setResizable(false);
        final JMenuBar tableMenuBar=createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.gameHistory.add(startingFEN);
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup=new GameSetup(this.gameFrame,true);
        this.boardPanel = new BoardPanel();
        this.analyzePanel = new AnalyzePanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.logPanel = new LogPanel();
        this.gameFrame.add(this.takenPiecesPanel,BorderLayout.WEST);
        this.gameFrame.add(this.gameHistoryPanel,BorderLayout.EAST);
        this.gameFrame.add(this.boardPanel,BorderLayout.CENTER);
        this.gameFrame.add(this.logPanel,BorderLayout.SOUTH);
        this.gameFrame.add(this.analyzePanel,BorderLayout.NORTH);
        this.gameFrame.setVisible(true);
    }

    public static Table get(){
        return INSTANCE;
    }

    public void show() {
        invokeLater(new Runnable() {
            public void run() {
                Table.get().getMoveLog().clear();
                Table.get().getSecondaryMoveLog().clear();
                Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
            }
        });
    }

    private GameSetup getGameSetup(){
        return this.gameSetup;
    }

    private void setGameHistory(List<String> list){
        this.gameHistory=list;
    }

    private List<String> getGameHistory(){
        return this.gameHistory;
    }

    private void setSecondaryGameHistory(List<String> list){
        this.secondaryGameHistory=list;
    }

    private List<String> getSecondaryGameHistory(){
        return this.secondaryGameHistory;
    }

    private void setupUpdate(final GameSetup gameSetup){
        setChanged();
        notifyObservers(gameSetup);
    }

    private Board getGameBoard(){
        return this.chessBoard;
    }

    private static class AIThinkTank extends SwingWorker<Move,String>{
        private AIThinkTank(){
        }

        @Override
        protected Move doInBackground() throws Exception{
            final MoveStrategy engine;
            if(Table.get().getGameBoard().currentPlayer().getAlliance().isWhite()){
                if(Table.get().getGameSetup().getWhiteEngine().equals("Alpha Beta")){
                    engine=new StockAlphaBeta(Table.get().getGameSetup().getWhiteSearchDepth(),Table.get().getGameSetup().getWhiteBoardEvaluator());
                }
                else{
                    engine=new MiniMax(Table.get().getGameSetup().getWhiteSearchDepth(),Table.get().getGameSetup().getWhiteBoardEvaluator());
                }
            }
            else{
                if(Table.get().getGameSetup().getBlackEngine().equals("Alpha Beta")){
                    engine=new StockAlphaBeta(Table.get().getGameSetup().getBlackSearchDepth(),Table.get().getGameSetup().getBlackBoardEvaluator());
                }
                else{
                    engine=new MiniMax(Table.get().getGameSetup().getBlackSearchDepth(),Table.get().getGameSetup().getBlackBoardEvaluator());
                }
            }

            final Move bestMove=engine.execute(Table.get().getGameBoard());

            return bestMove;
        }

        @Override
        public void done(){
            try {
                final Move bestMove=get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(),Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        Table.get().getGameHistory().add(FenUtilities.createFENFromGame(Table.get().getGameBoard()));
        Table.get().getSecondaryGameHistory().clear();
        Table.get().getSecondaryMoveLog().clear();
        Table.get().updateEngineMove(null);
        setChanged();
        notifyObservers(playerType);
    }

    private LogPanel getLogPanel(){
        return this.logPanel;
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

    private MoveLog getMoveLog(){
        return this.moveLog;
    }

    private MoveLog getSecondaryMoveLog(){
        return this.secondaryMoveLog;
    }

    public void updateComputerMove(final Move move) {
        this.computerMove=move;
    }

    public void updateEngineMove(final Move move) {
        this.engineMove=move;
    }

    public void updateGameBoard(final Board board) {
        this.chessBoard=board;
    }

    private static class TableGameAIWatcher implements Observer{

        @Override
        public void update(Observable o, Object arg) {
            if(Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                    !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
                    !Table.get().getGameBoard().currentPlayer().isInStaleMate()  &&
                    !Table.get().getStopped()){
                //create an AI thread
                //execute AI work
                final AIThinkTank thinkTank=new AIThinkTank();
                thinkTank.execute();
            }
            if(Table.get().getGameBoard().currentPlayer().isInCheckMate()){
                System.out.println("Game over, "+Table.get().getGameBoard().currentPlayer()+" is in checkmate");
            }
            if(Table.get().getGameBoard().currentPlayer().isInStaleMate()){
                System.out.println("Game over, "+Table.get().getGameBoard().currentPlayer()+" is in stalemate");
            }
        }
    }

    private boolean getStopped() {
        return this.engineStop;
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar= new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu=new JMenu("File");

        final JMenuItem restartMenuItem = new JMenuItem("Restart");
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().updateGameBoard(Board.createStandardBoard());
                Table.get().getGameHistory().clear();
                Table.get().getGameHistory().add(startingFEN);
                Table.get().getSecondaryGameHistory().clear();
                Table.get().getLogPanel().stopButton.setText("Start");
                engineStop=true;
                invokeLater(new Runnable() {
                    public void run() {
                        Table.get().getMoveLog().clear();
                        Table.get().getSecondaryMoveLog().clear();
                        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
                        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                    }
                });
            }
        });
        fileMenu.add(restartMenuItem);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferencesMenu(){
        final JMenu preferencesMenu=new JMenu("Preferences");
        final JMenuItem flipBoardMenuitem=new JMenuItem("Flip Board");
        flipBoardMenuitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection=boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });
        preferencesMenu.add(flipBoardMenuitem);
        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox=new JCheckBoxMenuItem("Highlight legal moves", true);
        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegals=legalMoveHighlighterCheckbox.isSelected();
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem engineHighlighterCheckbox=new JCheckBoxMenuItem("Highlight engine moves", true);
        engineHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightEngineMoves=engineHighlighterCheckbox.isSelected();
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());

                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        final JCheckBoxMenuItem engineCheckbox=new JCheckBoxMenuItem("View engine output", true);
        engineCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engineOutput=engineCheckbox.isSelected();
                Table.get().getAnalyzePanel().setOutPutStream();
                preferencesMenu.setPopupMenuVisible(true);
                preferencesMenu.setSelected(true);
            }
        });

        preferencesMenu.add(legalMoveHighlighterCheckbox);
        preferencesMenu.add(engineHighlighterCheckbox);
        preferencesMenu.add(engineCheckbox);

        preferencesMenu.addSeparator();
        final JMenuItem closeMenu=new JMenuItem("Close");
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

    private JMenu createOptionsMenu(){
        final JMenu optionsMenu=new JMenu("Options");
        final JMenuItem setupGameMenuItem=new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);
        return optionsMenu;
    }

    public enum BoardDirection{
        NORMAL{
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED{
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse (final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    public static class MoveLog{
        private final List<Move> moves;

        MoveLog(){
            this.moves=new ArrayList<>();
        }

        public List<Move> getMoves(){
            return this.moves;
        }

        public void addMove(final Move move){
            this.moves.add(move);
        }

        public int size(){
            return this.moves.size();
        }

        public void clear(){
            this.moves.clear();
        }

        public Move removeMove(int index){
            return this.moves.remove(index);
        }

        public boolean removeMove(final Move move){
            return this.moves.remove(move);
        }
    }

    enum PlayerType{
        HUMAN,
        COMPUTER
    }

    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;

        BoardPanel(){
            super(new GridLayout(8,8));
            this.boardTiles=new ArrayList<>();

            for(int i =0;i< BoardUtils.NUM_TILES;i++){
                final TilePanel tilePanel=new TilePanel(this,i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board){
            removeAll();
            for(final TilePanel tilePanel: boardDirection.traverse(boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    private class TilePanel extends JPanel{
        private final int tileId;

        TilePanel(final BoardPanel boardPanel, final int tileId){
            super(new GridBagLayout());
            this.tileId=tileId;

            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {

                    if(isRightMouseButton(e)){
                        sourceTile=null;
                        destinationTile=null;
                        humanMovedPiece=null;
                    }
                    else if(isLeftMouseButton(e)){
                        if(sourceTile==null){
                            //first click
                            sourceTile=chessBoard.getTile(tileId);
                            humanMovedPiece=sourceTile.getPiece();
                            if(humanMovedPiece==null){
                                sourceTile=null;
                            }
                        }
                        else{
                            //second click, make the move
                            destinationTile=chessBoard.getTile(tileId);
                            final Move move=Move.MoveFactory.createMove(chessBoard,sourceTile.getTileCoordinate(),destinationTile.getTileCoordinate());
                            final MoveTransition transition=chessBoard.currentPlayer().makeMove(move);
                            if(transition.getMoveStatus().isDone()){
                                chessBoard=transition.getTransitionBoard();
                                moveLog.addMove(move);
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            }
                            sourceTile=null;
                            destinationTile=null;
                            humanMovedPiece=null;
                        }
                    }
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gameHistoryPanel.redo(chessBoard,moveLog);
                            takenPiecesPanel.redo(moveLog);
                            boardPanel.drawBoard(chessBoard);
                        }
                    });
                }

                @Override
                public void mousePressed(final MouseEvent e) { }

                @Override
                public void mouseReleased(final MouseEvent e) { }

                @Override
                public void mouseEntered(final MouseEvent e) { }

                @Override
                public void mouseExited(final MouseEvent e) { }
            });

            validate();
        }

        public void drawTile(final Board board){
            assignTileColor();
            assignTilePieceIcon(board);
            highlightTileBorder(chessBoard);
            highlightLegalMoves(chessBoard);
            highlightAIMove();
            validate();
            repaint();
        }

        private void highlightTileBorder(final Board board) {
            if(humanMovedPiece != null &&
                    humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance() &&
                    humanMovedPiece.getPiecePosition() == this.tileId) {
                setBorder(BorderFactory.createLineBorder(Color.cyan));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }

        private void highlightAIMove() {
            if(highlightEngineMoves){
                if(engineMove!=null){
                    if(this.tileId == engineMove.getCurrentCoordinate()) {
                        setBackground(Color.cyan);
                    } else if(this.tileId == engineMove.getDestinationCoordinate()) {
                        setBackground(Color.blue);
                    }
                }

                if(!engineStop && computerMove != null) {
                    if(this.tileId == computerMove.getCurrentCoordinate()) {
                        setBackground(Color.pink);
                    } else if(this.tileId == computerMove.getDestinationCoordinate()) {
                        setBackground(Color.red);
                    }
                }
            }
        }

        private void highlightLegalMoves(final Board board){
            if(highlightLegals){
                for(final Move move: pieceLegalMoves(board)){
                    MoveTransition transition = board.currentPlayer().makeMove(move);
                    if(!transition.getMoveStatus().isDone()){
                        continue;
                    }
                    else if(move.getDestinationCoordinate()==this.tileId){
                        try{
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(Board board) {
            if(humanMovedPiece!=null && humanMovedPiece.getPieceAlliance()==board.currentPlayer().getAlliance()){
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTilePieceIcon(final Board board){
            this.removeAll();
            if(board.getTile(this.tileId).isTileOccupied()){
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(defaultPieceImagePath+
                                    board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0,1)+
                                    board.getTile(this.tileId).getPiece().toString()+".png"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void assignTileColor() {
            if(BoardUtils.getWhitePositions().contains(this.tileId)){
                setBackground(LIGHT_TILE_COLOR);
            }
            else{
                setBackground(DARK_TILE_COLOR);
            }
        }

    }

    private class LogPanel extends JPanel{
        final JButton backButton = new JButton("<");
        final JButton stopButton = new JButton("Start");
        final JButton forwardButton = new JButton(">");
        final JButton openFEN = new JButton("Load FEN");
        final JButton copyFEN = new JButton("Copy FEN");
        JTextField txtInput = new JTextField(startingFEN);

        LogPanel(){
            add(copyFEN);
            add(backButton);
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    engineStop=true;
                    stopButton.setText("Start");

                    if(Table.get().getMoveLog().size()>0){
                        int index=Table.get().getMoveLog().size()-1;
                        Table.get().getSecondaryMoveLog().addMove(Table.get().getMoveLog().getMoves().get(index));
                        Table.get().getMoveLog().removeMove(index);
                    }

                    List<String> pastBoards=Table.get().getGameHistory();
                    if(pastBoards.size()>1){
                        Table.get().getSecondaryGameHistory().add(pastBoards.get(pastBoards.size()-1));
                        pastBoards.remove(pastBoards.size()-1);
                        setGameHistory(pastBoards);
                        Table.get().updateGameBoard(FenUtilities.createGameFromFEN(pastBoards.get(pastBoards.size()-1)));
                    }
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        gameHistoryPanel.redo(chessBoard,moveLog);
                        takenPiecesPanel.redo(moveLog);
                        boardPanel.drawBoard(chessBoard);
                    }
                });}
            });

            add(stopButton);

            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (engineStop && Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                            !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
                            !Table.get().getGameBoard().currentPlayer().isInStaleMate()){
                        engineStop=false;
                        stopButton.setText("Stop");
                        final AIThinkTank thinkTank=new AIThinkTank();
                        thinkTank.execute();
                    }
                    else{
                        engineStop=true;
                        stopButton.setText("Start");
                    }
                    validate();
                }
            });

            add(forwardButton);
            forwardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    engineStop=true;
                    stopButton.setText("Start");

                    if(Table.get().getSecondaryMoveLog().size()>0){
                        int index=Table.get().getSecondaryMoveLog().size()-1;
                        Table.get().getMoveLog().addMove(Table.get().getSecondaryMoveLog().getMoves().get(index));
                        Table.get().getSecondaryMoveLog().removeMove(index);
                    }

                    List<String> pastBoards=Table.get().getGameHistory();
                    if(Table.get().getSecondaryGameHistory().size()>0){
                        int index=Table.get().getSecondaryGameHistory().size()-1;
                        pastBoards.add(Table.get().getSecondaryGameHistory().get(index));
                        Table.get().getSecondaryGameHistory().remove(index);
                        Table.get().setGameHistory(pastBoards);
                        Table.get().updateGameBoard(FenUtilities.createGameFromFEN(pastBoards.get(pastBoards.size()-1)));
                    }
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gameHistoryPanel.redo(chessBoard,moveLog);
                            takenPiecesPanel.redo(moveLog);
                            boardPanel.drawBoard(chessBoard);
                        }
                    });}});

            openFEN.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Table.get().getMoveLog().clear();
                    Table.get().getSecondaryMoveLog().clear();
                    Table.get().getGameHistory().clear();
                    Table.get().getSecondaryGameHistory().clear();
                    Table.get().updateGameBoard(FenUtilities.createGameFromFEN(txtInput.getText()));
                    Table.get().getGameHistory().add(txtInput.getText());
                    engineStop=true;
                    stopButton.setText("Start");

                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                            Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                            Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                        }
                    });}
            });

            copyFEN.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String myString = FenUtilities.createFENFromGame(Table.get().getGameBoard());
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
    }

    private class AnalyzePanel extends JPanel{
        final JButton analyzeBtn=new JButton("Analyze");
        JSpinner depthSpinner;
        String[] boardEvaluatorChoices = {"Standard", "Modified"};
        String[] aiChoices={"Alpha Beta","Minimax"};
        final JComboBox<String> cb = new JComboBox<String>(boardEvaluatorChoices);
        final JComboBox<String> cb2 = new JComboBox<String>(aiChoices);
        BoardEvaluator boardEvaluator=new StandardBoardEvaluator();
        String chosenEngine="Alpha Beta";
        MoveStrategy engine=new StockAlphaBeta(4,boardEvaluator);
        JTextArea textArea = new JTextArea(3, 10);
        TextAreaOutputStream taOutputStream = new TextAreaOutputStream(textArea);
        String output="";
        ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
        JScrollPane p=new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        AnalyzePanel(){
            super(new BorderLayout());

            this.depthSpinner = addLabeledSpinner(this, "Search depth:", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
            add(depthSpinner,BorderLayout.EAST);

            cb2.setSelectedIndex(0);
            cb2.setVisible(true);
            add(cb2,BorderLayout.WEST);

            cb2.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JComboBox cb2 = (JComboBox) e.getSource();
                    chosenEngine = (String) cb2.getSelectedItem();
                }
            });

            cb.setSelectedIndex(0);
            cb.setVisible(true);
            add(cb,BorderLayout.CENTER);

            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String chosenEvaluator = (String) cb.getSelectedItem();
                    switch(chosenEvaluator){
                        case "Standard":
                            boardEvaluator = new StandardBoardEvaluator();
                            break;
                        case "Modified":
                            boardEvaluator = new ModifiedBoardEvaluator();
                            break;
                    }
                }
            });

            add(analyzeBtn,BorderLayout.NORTH);
            analyzeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    switch(chosenEngine){
                        case "Alpha Beta":
                            engine = new StockAlphaBeta((Integer) depthSpinner.getValue(),boardEvaluator);
                            break;
                        case "Minimax":
                            engine = new MiniMax((Integer) depthSpinner.getValue(),boardEvaluator);
                            break;
                    }
                    Engine e1=new Engine(engine);
                    e1.execute();
                }
                });

            add(p,BorderLayout.SOUTH);
            setOutPutStream();
            setPreferredSize(ANALYZE_PANEL_DIMENSION);
            validate();
        }

        public void setOutPutStream(){
            if(engineOutput){
                textArea.setText(output+newConsole.toString().trim());
                System.setOut(new PrintStream(taOutputStream));
            }
            else{
                output=textArea.getText();

                System.setOut(new PrintStream(newConsole));
                textArea.setText("");
            }
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

    private static class Engine extends SwingWorker<Move,String> {
        final MoveStrategy engine;
        public Engine(MoveStrategy engine) {
            this.engine=engine;
        }

        @Override
        protected Move doInBackground() throws Exception {
            return engine.execute(Table.get().getGameBoard());
        }

        @Override
        public void done(){
            try {
                Table.get().updateEngineMove(get());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
