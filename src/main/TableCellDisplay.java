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