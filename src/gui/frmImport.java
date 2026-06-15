/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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

package gui;

import javax.swing.JFrame;
import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;
import base.AmuaModel;
import main.Constraint;
import main.Parameter;
import main.ScaledIcon;
import main.Table;
import main.Variable;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JCheckBox;

/**
 *
 */
public class frmImport {

	/**
	 * JFrame for form
	 */
	public JDialog frmImport;
	AmuaModel myModel, donorModel;
	DefaultTableModel modelParams, modelVars, modelTables, modelConstraints;
	private JTable tableParams, tableVars, tableTables, tableConstraints;

	public frmImport(AmuaModel myModel, AmuaModel donorModel){
		this.myModel=myModel;
		this.donorModel=donorModel;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmImport = new JDialog();
			frmImport.setIconImage(Toolkit.getDefaultToolkit().getImage(frmImport.class.getResource("/images/import_128.png")));
			frmImport.setModalityType(ModalityType.APPLICATION_MODAL);
			
			
			//Import Model Objects from [name]
			String title = MessageFormat.format(myModel.language.base.getString("title.import_model_objects_from"), donorModel.name);
			frmImport.setTitle("Amua - "+title);
			frmImport.setFont(myModel.language.font);
			frmImport.setResizable(false);
			frmImport.setBounds(100, 100, 600, 500);
			frmImport.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmImport.getContentPane().setLayout(null);

			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setFont(myModel.language.font);
			tabbedPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			tabbedPane.setBounds(6, 6, 576, 420);
			frmImport.getContentPane().add(tabbedPane);

			//parameters
			JScrollPane scrollPaneParams = new JScrollPane();
			JLabel lblParams=new JLabel(myModel.language.base.getString("object.parameters")); //Parameters
			lblParams.setFont(myModel.language.font);
			lblParams.setIcon(new ScaledIcon("/images/parameter",16,16,16,true));
			lblParams.setHorizontalTextPosition(SwingConstants.RIGHT);
			tabbedPane.addTab(myModel.language.base.getString("object.parameters"), null, scrollPaneParams, null); //Parameters
			tabbedPane.setTabComponentAt(0, lblParams);

			modelParams=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.base.getString("object.name"), myModel.language.base.getString("object.expression")} //Name, Expression
					) {
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			if(donorModel.parameters!=null) {
				for(int p=0; p<donorModel.parameters.size(); p++) {
					modelParams.addRow(new Object[] {null});
					Parameter curParam=donorModel.parameters.get(p);
					modelParams.setValueAt(curParam.name, p, 0);
					modelParams.setValueAt(curParam.expression, p, 1);
				}
			}

			tableParams = new JTable();
			tableParams.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tableParams.setShowVerticalLines(true);
			tableParams.getTableHeader().setReorderingAllowed(false);
			tableParams.getTableHeader().setFont(myModel.language.font);
			tableParams.setFont(myModel.language.font);
			tableParams.setAutoCreateRowSorter(true);
			tableParams.setModel(modelParams);
			scrollPaneParams.setViewportView(tableParams);

			//variables
			JScrollPane scrollPaneVars = new JScrollPane();
			JLabel lblVars=new JLabel(myModel.language.base.getString("object.variables")); //Variables
			lblVars.setFont(myModel.language.font);
			lblVars.setIcon(new ScaledIcon("/images/variable",16,16,16,true));
			lblVars.setHorizontalTextPosition(SwingConstants.RIGHT);
			tabbedPane.addTab(myModel.language.base.getString("object.variables"), null, scrollPaneVars, null);
			tabbedPane.setTabComponentAt(1, lblVars);

			modelVars=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.base.getString("object.name"), myModel.language.base.getString("object.expression")} //Name, Expression
					) {
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			if(donorModel.variables!=null) {
				for(int v=0; v<donorModel.variables.size(); v++) {
					modelVars.addRow(new Object[] {null});
					Variable curVar=donorModel.variables.get(v);
					modelVars.setValueAt(curVar.name, v, 0);
					modelVars.setValueAt(curVar.expression, v, 1);
				}
			}

			tableVars = new JTable();
			tableVars.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tableVars.setShowVerticalLines(true);
			tableVars.getTableHeader().setReorderingAllowed(false);
			tableVars.getTableHeader().setFont(myModel.language.font);
			tableVars.setFont(myModel.language.font);
			tableVars.setAutoCreateRowSorter(true);
			tableVars.setModel(modelVars);
			scrollPaneVars.setViewportView(tableVars);

			//tables
			JScrollPane scrollPaneTables = new JScrollPane();
			JLabel lblTables=new JLabel(myModel.language.base.getString("object.tables")); //Tables
			lblTables.setFont(myModel.language.font);
			lblTables.setIcon(new ScaledIcon("/images/table",16,16,16,true));
			lblTables.setHorizontalTextPosition(SwingConstants.RIGHT);
			tabbedPane.addTab(myModel.language.base.getString("object.tables"), null, scrollPaneTables, null); //Tables
			tabbedPane.setTabComponentAt(2, lblTables);

			modelTables=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.base.getString("object.name"),myModel.language.base.getString("object.type"),myModel.language.base.getString("object.size")} //Name, Type, Size
					) {
				boolean[] columnEditables = new boolean[] {false, false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			if(donorModel.tables!=null) {
				for(int t=0; t<donorModel.tables.size(); t++) {
					modelTables.addRow(new Object[] {null});
					Table curTable=donorModel.tables.get(t);
					modelTables.setValueAt(curTable.name, t, 0);
					modelTables.setValueAt(curTable.type, t, 1);
					modelTables.setValueAt(curTable.numRows+" x "+curTable.numCols, t, 2);
				}
			}
			
			tableTables = new JTable();
			tableTables.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tableTables.setShowVerticalLines(true);
			tableTables.getTableHeader().setReorderingAllowed(false);
			tableTables.getTableHeader().setFont(myModel.language.font);
			tableTables.setFont(myModel.language.font);
			tableTables.setAutoCreateRowSorter(true);
			tableTables.setModel(modelTables);
			scrollPaneTables.setViewportView(tableTables);


			//constraints
			JScrollPane scrollPaneConstraints = new JScrollPane();
			JLabel lblConstraints=new JLabel(myModel.language.base.getString("object.constraints")); //Constraints
			lblConstraints.setFont(myModel.language.font);
			lblConstraints.setIcon(new ScaledIcon("/images/constraint",16,16,16,true));
			lblConstraints.setHorizontalTextPosition(SwingConstants.RIGHT);
			tabbedPane.addTab(myModel.language.base.getString("object.constraints"), null, scrollPaneConstraints, null); //Constraints
			tabbedPane.setTabComponentAt(3, lblConstraints);

			modelConstraints=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.base.getString("object.name"), myModel.language.base.getString("object.expression")} //Name, Expression
					) {
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			if(donorModel.constraints!=null) {
				for(int c=0; c<donorModel.constraints.size(); c++) {
					modelConstraints.addRow(new Object[] {null});
					Constraint curConstraint=donorModel.constraints.get(c);
					modelConstraints.setValueAt(curConstraint.name, c, 0);
					modelConstraints.setValueAt(curConstraint.expression, c, 1);
				}
			}

			tableConstraints = new JTable();
			tableConstraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tableConstraints.setShowVerticalLines(true);
			tableConstraints.getTableHeader().setReorderingAllowed(false);
			tableConstraints.getTableHeader().setFont(myModel.language.font);
			tableConstraints.setFont(myModel.language.font);
			tableConstraints.setAutoCreateRowSorter(true);
			tableConstraints.setModel(modelConstraints);
			scrollPaneConstraints.setViewportView(tableConstraints);


			JButton btnClearSelection = new JButton(myModel.language.base.getString("button.clear_selection")); //Clear Selection
			btnClearSelection.setFont(myModel.language.font);
			btnClearSelection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index=tabbedPane.getSelectedIndex();
					if(index==0) { //parameters
						tableParams.clearSelection();
					}
					else if(index==1) { //variables
						tableVars.clearSelection();
					}
					else if(index==2) { //tables
						tableTables.clearSelection();
					}
					else if(index==3) { //constraints
						tableConstraints.clearSelection();
					}
				}
			});
			btnClearSelection.setBounds(6, 432, 138, 28);
			frmImport.getContentPane().add(btnClearSelection);
			
			JCheckBox chckbxOverwrite = new JCheckBox(myModel.language.base.getString("object.overwrite_current")); //Overwrite current objects
			chckbxOverwrite.setFont(myModel.language.font);
			chckbxOverwrite.setBounds(187, 437, 186, 18);
			frmImport.getContentPane().add(chckbxOverwrite);

			JButton btnImport = new JButton(myModel.language.base.getString("menu.import")); //Import
			btnImport.setFont(myModel.language.font);
			btnImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					boolean overwrite=chckbxOverwrite.isSelected();
					myModel.saveSnapshot(myModel.language.base.getString("title.import_objects")); //Add to undo stack (Import Objects)
					
					//parameters
					int indexParams[]=tableParams.getSelectedRows();
					for(int p=0; p<indexParams.length; p++) {
						int dataRow=tableParams.convertRowIndexToModel(indexParams[p]);
						Parameter curParam=donorModel.parameters.get(dataRow);
						if(myModel.isParameter(curParam.name)) { //parameter already exists
							if(overwrite==true) {
								//remove old parameter
								int paramNum=myModel.getParameterIndex(curParam.name);
								myModel.parameters.remove(paramNum);
								myModel.mainForm.modelParameters.removeRow(paramNum);
								//add new parameter
								myModel.parameters.add(curParam.copy());
								myModel.mainForm.modelParameters.addRow(new Object[]{curParam.name,curParam.expression});
							}
						}
						else { //new parameter
							myModel.parameters.add(curParam.copy());
							myModel.mainForm.modelParameters.addRow(new Object[]{curParam.name,curParam.expression});
						}
					}
										
					//variables
					int indexVars[]=tableVars.getSelectedRows();
					for(int v=0; v<indexVars.length; v++) {
						int dataRow=tableVars.convertRowIndexToModel(indexVars[v]);
						Variable curVar=donorModel.variables.get(dataRow);
						if(myModel.isVariable(curVar.name)) { //variable already exists
							if(overwrite==true) {
								//remove old variable
								int varNum=myModel.getVariableIndex(curVar.name);
								myModel.variables.remove(varNum);
								myModel.mainForm.modelVariables.removeRow(varNum);
								//add new variable
								myModel.variables.add(curVar.copy());
								myModel.mainForm.modelVariables.addRow(new Object[]{curVar.name,curVar.expression});
							}
						}
						else { //new variable
							myModel.variables.add(curVar.copy());
							myModel.mainForm.modelVariables.addRow(new Object[]{curVar.name,curVar.expression});
						}
					}
					
					//tables
					int indexTables[]=tableTables.getSelectedRows();
					for(int t=0; t<indexTables.length; t++) {
						int dataRow=tableTables.convertRowIndexToModel(indexTables[t]);
						Table curTable=donorModel.tables.get(dataRow);
						if(myModel.isTable(curTable.name)) { //table already exists
							if(overwrite==true) {
								//remove old table
								int tableNum=myModel.getTableIndex(curTable.name);
								myModel.tables.remove(tableNum);
								myModel.mainForm.modelTables.removeRow(tableNum);
								//add new table
								myModel.tables.add(curTable.copy());
								myModel.mainForm.modelTables.addRow(new Object[]{curTable.name,curTable.type,curTable.numRows+" x "+curTable.numCols});
							}
						}
						else { //new table
							myModel.tables.add(curTable.copy());
							myModel.mainForm.modelTables.addRow(new Object[]{curTable.name,curTable.type,curTable.numRows+" x "+curTable.numCols});
						}
					}
					
					//constraints
					int indexConstraints[]=tableConstraints.getSelectedRows();
					for(int c=0; c<indexConstraints.length; c++) {
						int dataRow=tableConstraints.convertRowIndexToModel(indexConstraints[c]);
						Constraint curConst=donorModel.constraints.get(dataRow);
						//add constraint
						myModel.constraints.add(curConst.copy());
						myModel.mainForm.modelConstraints.addRow(new Object[]{curConst.name,curConst.expression});
					}
					
					
					myModel.validateModelObjects(); //Update all model objects
					myModel.rescale(myModel.scale); //Re-validates textfields
					
					frmImport.dispose();

				}
			});
			btnImport.setBounds(385, 432, 90, 28);
			frmImport.getContentPane().add(btnImport);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.setFont(myModel.language.font);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmImport.dispose();
				}
			});
			btnCancel.setBounds(492, 432, 90, 28);
			frmImport.getContentPane().add(btnCancel);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
