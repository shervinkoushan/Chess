package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.gui.Table.MoveLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class GameHistoryPanel extends JPanel {
    private final JTable table;
    private final DataModel model;
    private final JScrollPane scrollPane;
    private static final Dimension HISTORY_PANEL_DIMENSION=new Dimension(150,400);

    GameHistoryPanel(){
        this.setLayout(new BorderLayout());
        this.model=new DataModel();
        table=new JTable(model);
        table.setRowHeight(15);
        table.setCellSelectionEnabled(true);
        this.scrollPane=new JScrollPane(table);
        this.scrollPane.setColumnHeaderView(table.getTableHeader());
        this.scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        this.add(scrollPane,BorderLayout.CENTER);
        this.setVisible(true);
    }

    void redo(final Board board, final MoveLog moveLog, final int currentPly){
        int currentRow=0;
        this.model.clear();
        for(final Move move: moveLog.getMoves()){
            this.model.setValueAt(""+currentRow+1,currentRow,0);
            final String moveText=move.toString();
            if(move.getMovedPiece().getPieceAlliance().isWhite()){
                this.model.setValueAt(moveText,currentRow,1);
            }
            else if(move.getMovedPiece().getPieceAlliance().isBlack()){
                this.model.setValueAt(moveText,currentRow,2);
                currentRow++;
            }
        }

        if(moveLog.getMoves().size()>0){
            final Move lastMove=moveLog.getMoves().get(moveLog.size()-1);
            final String moveText=lastMove.toString();
            if(lastMove.getMovedPiece().getPieceAlliance().isWhite()){
                this.model.setValueAt(moveText,currentRow,1);
                //table.changeSelection(currentRow,1,false,false);
            }
            else if(lastMove.getMovedPiece().getPieceAlliance().isBlack()){
                this.model.setValueAt(moveText,currentRow-1,2);
                //table.changeSelection(currentRow-1,2,false,false);
            }
            table.changeSelection((currentPly-1)/2,2-(currentPly)%2,false,false);
        }

        final JScrollBar vertical =scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

        System.out.println(currentPly);
        repaint();
        validate();
    }

    private static class DataModel extends DefaultTableModel {
        private final java.util.List<Row> values;
        private static final String[] NAMES = {"Move","White", "Black"};

        DataModel(){
            this.values=new ArrayList<>();
        }

        public void clear(){
            this.values.clear();
            setRowCount(0);
        }

        @Override
        public int getRowCount() {
            if(this.values==null){
                return 0;
            }
            return this.values.size();
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;
        }

        @Override
        public Object getValueAt(final int row, final int column){
            final Row currentRow=this.values.get(row);
            if(column==0){
                return currentRow.getRow();
            }
            if(column==1){
                return currentRow.getWhiteMove();
            }
            else if(column==2){
                return currentRow.getBlackMove();
            }
            return null;
        }

        @Override
        public void setValueAt(final Object aValue, final int row, final int column){
            final Row currentRow;
            if(this.values.size()<=row){
                currentRow=new Row();
                this.values.add(currentRow);
            }
            else{
                currentRow=this.values.get(row);
            }
            if(column==0){
                currentRow.setCurrentRow(row+1);
                fireTableRowsInserted(row,row);
            }
            if(column==1){
                currentRow.setWhiteMove((String) aValue);
                fireTableRowsInserted(row,row);
            }
            else if (column==2){
                currentRow.setBlackMove((String) aValue);
                fireTableCellUpdated(row,column);
            }
        }

        @Override
        public Class<?> getColumnClass(final int column){
            return Move.class;
        }

        @Override
        public String getColumnName(final int column){
            return NAMES[column];
        }
    }

    private static class Row{
        private int currentRow;
        private String whiteMove;
        private String blackMove;

        Row(){
        }

        public String getWhiteMove(){
            return this.whiteMove;
        }

        public String getBlackMove(){
            return this.blackMove;
        }

        public String getRow(){
            return (String) ""+this.currentRow;
        }

        public void setCurrentRow(final int row){
            this.currentRow=row;
        }

        public void setWhiteMove(final String move){
            this.whiteMove=move;
        }

        public void setBlackMove(final String move){
            this.blackMove=move;
        }

    }
}
