package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.player.Player;
import com.chess.engine.player.ai.BoardEvaluator;
import com.chess.engine.player.ai.ModifiedBoardEvaluator;
import com.chess.engine.player.ai.StandardBoardEvaluator;
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

    private BoardEvaluator whiteBoardEvaluator=new StandardBoardEvaluator();
    private BoardEvaluator blackBoardEvaluator=new StandardBoardEvaluator();


    private static final String HUMAN_TEXT = "Human";
    private static final String COMPUTER_TEXT = "Computer";

    GameSetup(final JFrame frame,
              final boolean modal) {
        super(frame, modal);
        final JPanel myPanel = new JPanel();
        //myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.PAGE_AXIS));
        myPanel.setLayout(new GridLayout(0,1));
        final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
        final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
        whiteHumanButton.setActionCommand(HUMAN_TEXT);
        final ButtonGroup whiteGroup = new ButtonGroup();
        whiteGroup.add(whiteHumanButton);
        whiteGroup.add(whiteComputerButton);
        whiteHumanButton.setSelected(true);

        getContentPane().add(myPanel);
        myPanel.add(new JLabel("White"));
        myPanel.add(whiteHumanButton);
        myPanel.add(whiteComputerButton);

        myPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        myPanel.add(new JLabel("White board evaluator"));
        String[] choices = {"Standard", "Modified"};
        final JComboBox<String> cb = new JComboBox<String>(choices);
        cb.setSelectedIndex(0);
        cb.setVisible(true);
        myPanel.add(cb);

        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String chosenEvaluator = (String) cb.getSelectedItem();
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

        this.whiteSearchDepthSpinner = addLabeledSpinner(myPanel, "White Search Depth", new SpinnerNumberModel(4, 0, Integer.MAX_VALUE, 1));


        final ButtonGroup blackGroup = new ButtonGroup();
        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackHumanButton.setSelected(true);

        myPanel.add(new JLabel("Black"));
        myPanel.add(blackHumanButton);
        myPanel.add(blackComputerButton);

        myPanel.add(new JLabel("Black board evaluator"));
        final JComboBox<String> cb2 = new JComboBox<String>(choices);
        cb2.setSelectedIndex(0);
        cb2.setVisible(true);
        myPanel.add(cb2);

        cb2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox cb2 = (JComboBox) e.getSource();
                String chosenEvaluator = (String) cb2.getSelectedItem();
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

        this.blackSearchDepthSpinner = addLabeledSpinner(myPanel, "Black Search Depth", new SpinnerNumberModel(4, 0, Integer.MAX_VALUE, 1));

        final JButton cancelButton = new JButton("Cancel");
        final JButton okButton = new JButton("OK");

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

        myPanel.add(cancelButton);
        myPanel.add(okButton);

        setLocationRelativeTo(frame);
        pack();
        setVisible(false);
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
}