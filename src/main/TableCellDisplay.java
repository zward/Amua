package main;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import base.AmuaModel;

public class TableCellDisplay extends JScrollPane implements TableCellRenderer{
	StyledTextPane textPane;

	public TableCellDisplay(AmuaModel myModel) {
		textPane = new StyledTextPane(myModel);
		getViewport().add(textPane);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		/*if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
			textPane.setForeground(table.getSelectionForeground());
			textPane.setBackground(table.getSelectionBackground());
		} 
		else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
			textPane.setForeground(table.getForeground());
			textPane.setBackground(table.getBackground());
		}*/
		setBackground(table.getBackground());
		textPane.setBackground(table.getBackground());
		textPane.setText((String) value);
		textPane.restyle();
		//textPane.setCaretPosition(0);
		//return this;
		return(textPane);
	}
}