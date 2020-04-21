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
    private final String[] engineChoices = {"Alpha Beta", "Minimax"};
    private final String[] evaluatorChoices = {"Standard", "Modified"};
    private final JPanel myPanel = new JPanel();
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton okButton = new JButton("OK");

    private final ButtonGroup whiteGroup = new ButtonGroup();
    private final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
    private final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
    private final JComboBox<String> cbWhiteEngine = new JComboBox<String>(engineChoices);
    private final JComboBox<String> cbWhiteEvaluator = new JComboBox<String>(evaluatorChoices);
    private final JCheckBoxMenuItem viewWhitePVCheckBox= new JCheckBoxMenuItem("Display best variation", false);

    private final ButtonGroup blackGroup = new ButtonGroup();
    private final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
    private final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
    private final JComboBox<String> cbBlackEngine = new JComboBox<String>(engineChoices);
    private final JComboBox<String> cbBlackEvaluator = new JComboBox<String>(evaluatorChoices);
    private final JCheckBoxMenuItem viewBlackPVCheckBox = new JCheckBoxMenuItem("Display best variation", false);

    private BoardEvaluator whiteBoardEvaluator=new StandardBoardEvaluator();
    private BoardEvaluator blackBoardEvaluator=new StandardBoardEvaluator();

    private String whiteEngine="Alpha Beta";
    private String blackEngine="Alpha Beta";

    private boolean displayBlackEngine=false;
    private boolean displayWhiteEngine=false;
    private boolean flipped=false;
    private boolean viewBlackVariation=false;
    private boolean viewWhiteVariation=false;

    private static final String HUMAN_TEXT = "Human";
    private static final String COMPUTER_TEXT = "Computer";

    GameSetup(final JFrame frame, final boolean modal) {
        super(frame, modal);
        myPanel.setLayout(new GridLayout(0,1));
        myPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(myPanel);

        whiteComputerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayWhiteEngine=true;
                redo(flipped);
            }
        });
        whiteHumanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayWhiteEngine=false;
                redo(flipped);
            }
        });
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

        viewWhitePVCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewWhiteVariation=viewWhitePVCheckBox.isSelected();
            }
            });

        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackHumanButton.setSelected(true);
        blackComputerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBlackEngine=true;
                redo(flipped);
            }
        });
        blackHumanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBlackEngine=false;
                redo(flipped);
            }
        });

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

        viewBlackPVCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewBlackVariation=viewBlackPVCheckBox.isSelected();
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
                GameSetup.this.setVisible(false);
            }
        });

        setLocation(215,128);
        redo(false);
        setVisible(false);
        }

     public void redo(boolean flipped){
        this.flipped=flipped;
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
        pack();
     }

    private void drawBlack() {
        myPanel.add(new JLabel("Black"));
        myPanel.add(blackHumanButton);
        myPanel.add(blackComputerButton);
        if(displayBlackEngine){
            myPanel.add(new JLabel("Black Engine"));
            myPanel.add(cbBlackEngine);
            myPanel.add(new JLabel("Black Board evaluator"));
            myPanel.add(cbBlackEvaluator);
            this.blackSearchDepthSpinner = addLabeledSpinner(myPanel, "Black Search Depth", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
            myPanel.add(viewBlackPVCheckBox);
        }
    }

    private void drawWhite() {
        myPanel.add(new JLabel("White"));
        myPanel.add(whiteHumanButton);
        myPanel.add(whiteComputerButton);
        if(displayWhiteEngine){
            myPanel.add(new JLabel("White Engine"));
            myPanel.add(cbWhiteEngine);
            myPanel.add(new JLabel("White Board evaluator"));
            myPanel.add(cbWhiteEvaluator);
            this.whiteSearchDepthSpinner = addLabeledSpinner(myPanel, "White Search Depth", new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
            myPanel.add(viewWhitePVCheckBox);
        }
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

    boolean isViewBlackVariation(){
        return this.viewBlackVariation;
    }

    boolean isViewWhiteVariation(){
        return this.viewWhiteVariation;
    }
}