/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017 Zachary J. Ward
 *
 * This file is part of Amua. Amua is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Amua is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amua.  If not, see <http://www.gnu.org/licenses/>.
 */

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