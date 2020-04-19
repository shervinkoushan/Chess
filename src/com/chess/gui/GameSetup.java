package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.player.Player;
import com.chess.engine.player.ai.*;
import com.chess.gui.Table.PlayerType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class GameSetup extends JDialog {
    private PlayerType whitePlayerType;
    private PlayerType blackPlayerType;
    private JSpinner whiteSearchDepthSpinner;
    private JSpinner blackSearchDepthSpinner;
    private String[] engineChoices = {"Alpha Beta", "Minimax"};
    private String[] evaluatorChoices = {"Standard", "Modified"};
    private final JPanel myPanel = new JPanel();
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton okButton = new JButton("OK");

    private final ButtonGroup whiteGroup = new ButtonGroup();
    private final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
    private final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
    private final JComboBox<String> cbWhiteEngine = new JComboBox<String>(engineChoices);
    final JComboBox<String> cbWhiteEvaluator = new JComboBox<String>(evaluatorChoices);

    private final ButtonGroup blackGroup = new ButtonGroup();
    private final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
    private final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
    private final JComboBox<String> cbBlackEngine = new JComboBox<String>(engineChoices);
    private final JComboBox<String> cbBlackEvaluator = new JComboBox<String>(evaluatorChoices);

    private BoardEvaluator whiteBoardEvaluator=new StandardBoardEvaluator();
    private BoardEvaluator blackBoardEvaluator=new StandardBoardEvaluator();

    private String whiteEngine="Alpha Beta";
    private String blackEngine="Alpha Beta";

    private static final String HUMAN_TEXT = "Human";
    private static final String COMPUTER_TEXT = "Computer";

    GameSetup(final JFrame frame, final boolean modal) {
        super(frame, modal);
        myPanel.setLayout(new GridLayout(0,1));
        myPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(myPanel);

        whiteHumanButton.setActionCommand(HUMAN_TEXT);
        whiteGroup.add(whiteHumanButton);
        whiteGroup.add(whiteComputerButton);
        whiteHumanButton.setSelected(true);

        cbWhiteEngine.setSelectedIndex(0);
        cbWhiteEngine.setVisible(true);
        cbWhiteEngine.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cbWhiteEngine = (JComboBox) e.getSource();
                whiteEngine = (String) cbWhiteEngine.getSelectedItem();
            }
        });

        cbWhiteEvaluator.setSelectedIndex(0);
        cbWhiteEvaluator.setVisible(true);
        cbWhiteEvaluator.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cbWhiteEvaluator = (JComboBox) e.getSource();
                String chosenEvaluator = (String) cbWhiteEvaluator.getSelectedItem();
                switch(chosenEvaluator){
                    case "Standard":
                        whiteBoardEvaluator = new StandardBoardEvaluator();
                        break;
                    case "Modified":
                        whiteBoardEvaluator = new ModifiedBoardEvaluator();
                        break;
                }
            }
        });

        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackHumanButton.setSelected(true);

        cbBlackEngine.setSelectedIndex(0);
        cbBlackEngine.setVisible(true);
        cbBlackEngine.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cbBlackEngine = (JComboBox) e.getSource();
                blackEngine = (String) cbBlackEngine.getSelectedItem();
            }
        });

        cbBlackEvaluator.setSelectedIndex(0);
        cbBlackEvaluator.setVisible(true);
        cbBlackEvaluator.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cbBlackEvaluator = (JComboBox) e.getSource();
                String chosenEvaluator = (String) cbBlackEvaluator.getSelectedItem();
                switch(chosenEvaluator){
                    case "Standard":
                        blackBoardEvaluator = new StandardBoardEvaluator();
                        break;
                    case "Modified":
                        blackBoardEvaluator = new ModifiedBoardEvaluator();
                        break;
                }
            }
        });

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whitePlayerType = whiteComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
                blackPlayerType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
                GameSetup.this.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Cancel");
                GameSetup.this.setVisible(false);
            }
        });

        setPreferredSize(new Dimension(this.getWidth(),this.getHeight()+610));
        setLocation(220,150);
        redo(false);
        pack();
        setVisible(false);
        }

     public void redo(boolean flipped){
        myPanel.removeAll();
        if(flipped){
            drawWhite();
            drawBlack();
        }
        else{
            drawBlack();
            drawWhite();
        }

         myPanel.add(cancelButton);
         myPanel.add(okButton);
     }

    private void drawBlack() {
        myPanel.add(new JLabel("Black"));
        myPanel.add(blackHumanButton);
        myPanel.add(blackComputerButton);
        myPanel.add(new JLabel("Black engine"));
        myPanel.add(cbBlackEngine);
        myPanel.add(new JLabel("Black board evaluator"));
        myPanel.add(cbBlackEvaluator);
        this.blackSearchDepthSpinner = addLabeledSpinner(myPanel, "Black Search Depth", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
    }

    private void drawWhite() {
        myPanel.add(new JLabel("White"));
        myPanel.add(whiteHumanButton);
        myPanel.add(whiteComputerButton);
        myPanel.add(new JLabel("White engine"));
        myPanel.add(cbWhiteEngine);
        myPanel.add(new JLabel("White board evaluator"));
        myPanel.add(cbWhiteEvaluator);
        this.whiteSearchDepthSpinner = addLabeledSpinner(myPanel, "White Search Depth", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
    }


    void promptUser() {
        setVisible(true);
        repaint();
    }

    boolean isAIPlayer(final Player player) {
        if(player.getAlliance() == Alliance.WHITE) {
            return getWhitePlayerType() == PlayerType.COMPUTER;
        }
        return getBlackPlayerType() == PlayerType.COMPUTER;
    }

    PlayerType getWhitePlayerType() {
        return this.whitePlayerType;
    }

    PlayerType getBlackPlayerType() {
        return this.blackPlayerType;
    }

    private static JSpinner addLabeledSpinner(final Container c,
                                              final String label,
                                              final SpinnerModel model) {
        final JLabel l = new JLabel(label);
        c.add(l);
        final JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);
        return spinner;
    }

    int getWhiteSearchDepth() {
        return (Integer)this.whiteSearchDepthSpinner.getValue();
    }

    int getBlackSearchDepth() {
        return (Integer)this.blackSearchDepthSpinner.getValue();
    }

    BoardEvaluator getWhiteBoardEvaluator(){
        return this.whiteBoardEvaluator;
    }

    BoardEvaluator getBlackBoardEvaluator(){
        return this.blackBoardEvaluator;
    }

    String getWhiteEngine(){
        return this.whiteEngine;
    }

    String getBlackEngine(){
        return this.blackEngine;
    }
}