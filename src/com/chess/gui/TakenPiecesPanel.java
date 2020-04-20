package com.chess.gui;

import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.gui.Table.MoveLog;
import com.google.common.primitives.Ints;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TakenPiecesPanel extends JPanel {
    private final JPanel northPanel;
    private final JPanel southPanel;

    private static final EtchedBorder PANEL_BORDER=new EtchedBorder(EtchedBorder.RAISED);
    private static final Dimension TAKEN_PIECES_DIMENSION=new Dimension(70,80);
    private static final Color PANEL_COLOR=Color.decode("0xFDF5E6");
    private static final int SCALING_FACTOR =3;

    public TakenPiecesPanel(){
        super(new BorderLayout());
        setBackground(PANEL_COLOR);
        setBorder(PANEL_BORDER);
        this.northPanel=new JPanel(new GridLayout(8,2));
        this.southPanel=new JPanel(new GridLayout(8,2));
        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);
        add(this.northPanel,BorderLayout.NORTH);
        add(this.southPanel,BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog movelog, int ply, boolean flipped){
        southPanel.removeAll();
        northPanel.removeAll();

        final List<Piece> whiteTakenPieces=new ArrayList<>();
        final List<Piece> blackTakenPieces=new ArrayList<>();

        for(int i=0; i<ply;i++){
            if(i<movelog.getMoves().size()){
                final Move move=movelog.getMoves().get(i);
                if(move.isAttack()){
                    final Piece takenPiece=move.getAttackedPiece();
                    if(takenPiece.getPieceAlliance().isWhite()){
                        whiteTakenPieces.add(takenPiece);
                    }
                    else{
                        blackTakenPieces.add(takenPiece);
                    }
                }
            }
            else{
                break;
            }
        }

        Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Ints.compare(o1.getPieceValue(),o2.getPieceValue());
            }
        });

        Collections.sort(blackTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Ints.compare(o1.getPieceValue(),o2.getPieceValue());
            }
        });

        for(final Piece takenPiece:whiteTakenPieces){
            try{
                final BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/art/pieces/plain/"+
                        takenPiece.getPieceAlliance().toString().substring(0,1)+takenPiece.toString()+".png"));
                final ImageIcon ic = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(ic.getImage().getScaledInstance(
                        ic.getIconWidth()/SCALING_FACTOR, ic.getIconWidth()/SCALING_FACTOR, Image.SCALE_SMOOTH)));
                if (flipped) {
                    this.southPanel.add(imageLabel);
                } else {
                    this.northPanel.add(imageLabel);
                }
            }
            catch (final IOException e){
                e.printStackTrace();
            }
        }

        for(final Piece takenPiece:blackTakenPieces){
            try{
                final BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/art/pieces/plain/"+
                        takenPiece.getPieceAlliance().toString().substring(0,1)+takenPiece.toString()+".png"));
                final ImageIcon ic = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(ic.getImage().getScaledInstance(
                        ic.getIconWidth()/SCALING_FACTOR, ic.getIconWidth()/SCALING_FACTOR, Image.SCALE_SMOOTH)));
                if(flipped){
                    this.northPanel.add(imageLabel);
                }
                else{
                    this.southPanel.add(imageLabel);
                }
            }
            catch (final IOException e){
                e.printStackTrace();
            }
        }

        repaint();
        validate();
    }
}
