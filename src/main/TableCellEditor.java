package main;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import base.AmuaModel;

public class TableCellEditor extends DefaultCellEditor {
	//protected JScrollPane scrollpane;
	protected StyledTextPane textPane;

	public TableCellEditor(AmuaModel myModel) {
		super(new JCheckBox());
		//scrollpane = new JScrollPane();
		textPane = new StyledTextPane(myModel); 
		textPane.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLUE));
		
		//textPane.setLineWrap(true);
		//textarea.setWrapStyleWord(true);
		//textarea.setBorder(new TitledBorder("This is a JTextArea"));
		//scrollpane.getViewport().add(textPane);
		//scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		textPane.setText((String) value);
		return(textPane);
		//return(scrollpane);
	}

	public Object getCellEditorValue() {
		return textPane.getText();
	}
}