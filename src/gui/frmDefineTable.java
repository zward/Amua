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

package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import base.AmuaModel;
import filters.CSVFilter;
import lang.Language;
import main.ErrorLog;
import main.ScaledIcon;
import main.Table;
import math.Interpreter;
import math.MathUtils;

/**
 *
 */
public class frmDefineTable {

	public JDialog frmDefineTable;
	AmuaModel myModel;
	JTextArea textNotes;
	Table table;
	int tableNum;
	private JTable viewTable;
	DefaultTableModel model;
	private JTextField textName;
	JComboBox<String> comboType, comboLookupMethod;
	JLabel lblMethod;
	private JTextField textEV;
	private JTextField textNumRows;
	private JTextField textNumCols;
	JFileChooser fc;
	ErrorLog errorLog;
	double tempTable[][];
	String tempHeaders[];
	JLabel lblExpectedValue;
	JButton btnEvaluate;
	private interpolateTable tableInterpolation;
	
	public frmDefineTable(AmuaModel myModel,int tableNum, JFileChooser fc, ErrorLog errorLog){
		this.myModel=myModel;
		this.tableNum=tableNum;
		this.fc=fc;
		this.errorLog=errorLog;
		initialize();
		if(tableNum==-1){table=new Table();}
		else{getTable();}
	}


	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineTable = new JDialog();
			frmDefineTable.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineTable.class.getResource("/images/table_128.png")));
			frmDefineTable.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineTable.setTitle("Amua - "+myModel.language.base.getString("title.define_table")); //Define Table
			frmDefineTable.setBounds(100, 100, 925, 600);
			frmDefineTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{75, 123, 84, 134, 100, 110, 285, 0, 0};
			gridBagLayout.rowHeights = new int[]{28, 28, 334, 22, 65, 28, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			frmDefineTable.getContentPane().setLayout(gridBagLayout);

			JLabel lblName = new JLabel(myModel.language.base.getString("object.name")+":"); //Name
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.insets = new Insets(0, 0, 5, 5);
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			frmDefineTable.getContentPane().add(lblName, gbc_lblName);

			textName = new JTextField();
			GridBagConstraints gbc_textName = new GridBagConstraints();
			gbc_textName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textName.gridwidth = 3;
			gbc_textName.insets = new Insets(0, 0, 5, 5);
			gbc_textName.gridx = 1;
			gbc_textName.gridy = 0;
			frmDefineTable.getContentPane().add(textName, gbc_textName);
			textName.setColumns(10);

			JLabel lblType = new JLabel(myModel.language.base.getString("table.table_type")+":"); //Table Type
			GridBagConstraints gbc_lblType = new GridBagConstraints();
			gbc_lblType.anchor = GridBagConstraints.EAST;
			gbc_lblType.insets = new Insets(0, 0, 5, 5);
			gbc_lblType.gridx = 4;
			gbc_lblType.gridy = 0;
			frmDefineTable.getContentPane().add(lblType, gbc_lblType);

			comboType = new JComboBox<String>();
			lblExpectedValue = new JLabel(myModel.language.math.getString("sum.expected_value")+":"); //Expected Value
			btnEvaluate = new JButton(myModel.language.base.getString("button.evaluate")); //Evaluate
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						int numRows=model.getRowCount();
						int selected=viewTable.getSelectedColumn();
						if(selected<1){selected=1;}
						double mean=0;
						double sumProb=0;
						for(int r=0; r<numRows; r++){
							double prob=Double.parseDouble((String)viewTable.getValueAt(r,0));
							sumProb+=prob;
							double val=Double.parseDouble((String)viewTable.getValueAt(r,selected));
							mean+=prob*val;
						}
						if(Math.abs(sumProb-1.0)>MathUtils.tolerance){
							//Probabilities sum to [value]!
							String msg=MessageFormat.format(myModel.language.message.getString("err.prob_sum"), sumProb);
							JOptionPane.showMessageDialog(frmDefineTable, msg); 
						}
						else{
							textEV.setText(mean+"");
						}
					}catch(Exception er){
						JOptionPane.showMessageDialog(frmDefineTable,myModel.language.message.getString("error")+": "+er.getMessage());
					}
				}
			});
			comboType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=comboType.getSelectedIndex();
					if(selected==0){ //Lookup
						int numCols=model.getColumnCount();
						if(numCols==0){
							model.addColumn(myModel.language.base.getString("table.index")); //Index
							model.addColumn(myModel.language.analysis.getString("result.value")); //Value
						}
						else if(numCols==1){
							model.setColumnIdentifiers(new String[]{myModel.language.base.getString("table.index")}); //Index
							model.addColumn(myModel.language.analysis.getString("result.value")); //Value
						}
						else{
							String colNames[]=new String[numCols];
							for(int c=0; c<numCols; c++){
								colNames[c]=model.getColumnName(c);
							}
							colNames[0]=myModel.language.base.getString("table.index"); //Index
							model.setColumnIdentifiers(colNames);
						}
						textNumCols.setText(model.getColumnCount()+"");
												
						lblMethod.setEnabled(true);
						comboLookupMethod.setEnabled(true);
						lblExpectedValue.setVisible(false);
						textEV.setVisible(false);
						btnEvaluate.setVisible(false);
					}
					else if(selected==1){ //Distribution
						int numCols=model.getColumnCount();
						if(numCols==0){
							model.addColumn(myModel.language.math.getString("prob.probability")); //Probability
							model.addColumn(myModel.language.analysis.getString("result.value")); //Value
						}
						else if(numCols==1){
							model.setColumnIdentifiers(new String[]{myModel.language.math.getString("prob.probability")}); //Probability
							model.addColumn(myModel.language.analysis.getString("result.value")); //Value
						}
						else{
							String colNames[]=new String[numCols];
							for(int c=0; c<numCols; c++){
								colNames[c]=model.getColumnName(c);
							}
							colNames[0]=myModel.language.math.getString("prob.probability"); //Probability
							model.setColumnIdentifiers(colNames);
						}
						textNumCols.setText(model.getColumnCount()+"");
						
						lblMethod.setEnabled(false);
						comboLookupMethod.setEnabled(false);
						lblExpectedValue.setVisible(true);
						textEV.setVisible(true);
						btnEvaluate.setVisible(true);
					}
					else if(selected==2){ //Matrix
						int numCols=model.getColumnCount();
						String colNames[]=new String[numCols];
						for(int c=0; c<numCols; c++){
							colNames[c]=c+"";
						}
						model.setColumnIdentifiers(colNames);
						textNumCols.setText(model.getColumnCount()+"");
						
						lblMethod.setEnabled(false);
						comboLookupMethod.setEnabled(false);
						lblExpectedValue.setVisible(false);
						textEV.setVisible(false);
						btnEvaluate.setVisible(false);
					}
				}
			});
			//comboType.setModel(new DefaultComboBoxModel<String>(new String[] {"Lookup", "Distribution","Matrix"}));
			String types[]=new String[3];
			types[0]=myModel.language.base.getString("table.lookup"); //Lookup
			types[1]=myModel.language.base.getString("table.distribution"); //Distribution
			types[2]=myModel.language.base.getString("table.matrix"); //Matrix
			comboType.setModel(new DefaultComboBoxModel<String>(types));
			
			GridBagConstraints gbc_comboType = new GridBagConstraints();
			gbc_comboType.insets = new Insets(0, 0, 5, 5);
			gbc_comboType.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboType.gridx = 5;
			gbc_comboType.gridy = 0;
			frmDefineTable.getContentPane().add(comboType, gbc_comboType);

			final JScrollPane scrollPane_2 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
			gbc_scrollPane_2.gridheight = 2;
			gbc_scrollPane_2.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_2.gridx = 6;
			gbc_scrollPane_2.gridy = 0;
			frmDefineTable.getContentPane().add(scrollPane_2, gbc_scrollPane_2);

			tableInterpolation = new interpolateTable();
			tableInterpolation.language=myModel.language;
			scrollPane_2.setViewportView(tableInterpolation);
			tableInterpolation.setRowSelectionAllowed(false);
			tableInterpolation.setShowVerticalLines(true);
			tableInterpolation.setModel(new DefaultTableModel(
					new Object[][] {
						{myModel.language.base.getString("table.interpolation_method"), myModel.language.base.getString("table.linear")}, //Interpolation Method, Linear
						{myModel.language.base.getString("table.boundary_condition"), myModel.language.base.getString("table.natural")}, //Boundary Condition, Natural
						{myModel.language.base.getString("table.extrapolate"), myModel.language.base.getString("button.no")}, //Extrapolate, No
					},
					new String[] {
							"", ""
					}
					) {
				boolean[] columnEditables = new boolean[] {
						false, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
			tableInterpolation.getColumnModel().getColumn(0).setResizable(false);
			tableInterpolation.getColumnModel().getColumn(0).setPreferredWidth(124);
			tableInterpolation.getColumnModel().getColumn(1).setPreferredWidth(150);
			tableInterpolation.getTableHeader().setReorderingAllowed(false);
			tableInterpolation.setEnabled(false);
			
			tableInterpolation.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			    @Override
			    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			        if(tableInterpolation.enabled[row]){setForeground(tableInterpolation.getForeground());}
			        else{setForeground(new Color(220,220,220));}
			        return this;
			    }   
			});


			JToolBar toolBar = new JToolBar();
			toolBar.setRollover(true);
			toolBar.setFloatable(false);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_toolBar.gridwidth = 4;
			gbc_toolBar.insets = new Insets(0, 0, 5, 5);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 1;
			frmDefineTable.getContentPane().add(toolBar, gbc_toolBar);

			JButton btnImport = new JButton(myModel.language.base.getString("menu.import")); //Import
			btnImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						fc.setDialogTitle(myModel.language.base.getString("title.import_table")); //Import Table
						fc.setApproveButtonText(myModel.language.base.getString("menu.import")); //Import
						fc.setFileFilter(new CSVFilter(myModel.language));

						int returnVal = fc.showOpenDialog(frmDefineTable);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							FileInputStream fstream = new FileInputStream(file);
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));
							String strLine;
							model.setRowCount(0);
							strLine=br.readLine(); //Headers
							//check for BOM
							String BOM = "\uFEFF";
							strLine=strLine.replace(BOM,"");
							
							String data[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
							int colNum=data.length;
							model.setColumnCount(0);
							for(int c=0; c<colNum; c++){
								model.addColumn(data[c]); //Column headers
							}
							if(comboType.getSelectedIndex()==2){refreshMatrixColNames();}
							strLine=br.readLine(); //First line
							while(strLine!=null){
								data=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
								model.addRow(data);
								strLine=br.readLine();
							}
							br.close();

							textNumRows.setText(model.getRowCount()+"");
							textNumCols.setText(model.getColumnCount()+"");
						}

					}catch(Exception er){
						JOptionPane.showMessageDialog(frmDefineTable,er.getMessage());
						errorLog.recordError(er);
					}
				}
			});
			btnImport.setToolTipText(myModel.language.base.getString("title.import_table")); //Import Table
			btnImport.setIcon(new ScaledIcon("/images/import",16,16,16,true));
			toolBar.add(btnImport);

			JButton btnPasteTable = new JButton("");
			btnPasteTable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
						String strData=(String) clip.getData(DataFlavor.stringFlavor);
						String rows[]=strData.split("\n");
						String headers[]=rows[0].split("\t");
						int numCols=headers.length;
						int numRows=rows.length;
						model.setColumnCount(0);
						for(int c=0; c<numCols; c++){model.addColumn(headers[c]);}
						if(comboType.getSelectedIndex()==2){refreshMatrixColNames();}
						model.setRowCount(rows.length-1);
						for(int r=1; r<numRows; r++){
							String curRow[]=rows[r].split("\t");
							for(int c=0; c<numCols; c++){
								model.setValueAt(curRow[c], r-1, c);
							}
						}
						textNumRows.setText(model.getRowCount()+"");
						textNumCols.setText(model.getColumnCount()+"");
					}catch(Exception er){
						JOptionPane.showMessageDialog(frmDefineTable,er.getMessage());
						errorLog.recordError(er);
					}
				}
			});
			btnPasteTable.setIcon(new ScaledIcon("/images/paste",16,16,16,true));
			btnPasteTable.setToolTipText(myModel.language.base.getString("title.paste_table")); //Paste Table
			toolBar.add(btnPasteTable);
			
			JButton btnExport = new JButton(myModel.language.base.getString("menu.export")); //Export
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						fc.setDialogTitle(myModel.language.base.getString("title.export_table")); //Export Table
						fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export
						fc.setFileFilter(new CSVFilter(myModel.language));

						int returnVal = fc.showSaveDialog(frmDefineTable);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							int numCols=model.getColumnCount();
							int numRows=model.getRowCount();
							//Headers
							for(int i=0; i<numCols-1; i++){
								out.write(model.getColumnName(i)+",");
							}
							out.write(model.getColumnName(numCols-1));
							out.newLine();
							//Data
							for(int i=0; i<numRows; i++){
								for(int j=0; j<numCols-1; j++){
									out.write(model.getValueAt(i, j)+",");
								}
								out.write(model.getValueAt(i, numCols-1)+"");
								out.newLine();
							}
							out.close();
							
							JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("info.exported")); //Exported!
						}

					}catch(Exception er){
						JOptionPane.showMessageDialog(frmDefineTable,er.getMessage());
						errorLog.recordError(er);
					}
				}
			});
			btnExport.setToolTipText(myModel.language.base.getString("title.export_table")); //Export Table
			btnExport.setIcon(new ScaledIcon("/images/export",16,16,16,true));
			toolBar.add(btnExport);

			JSeparator separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
			toolBar.add(separator);

			JLabel lblRows = new JLabel(" "+myModel.language.base.getString("table.rows")+":"); //Rows
			toolBar.add(lblRows);

			JButton btnAddRow = new JButton("");
			btnAddRow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=viewTable.getSelectedRow();
					if(selected==-1){model.addRow(new Object[]{null});} //Add row at end
					else{model.insertRow(selected+1, new Object[]{null});} //Add row after selected
					viewTable.clearSelection();
					textNumRows.setText(model.getRowCount()+"");
				}
			});
			btnAddRow.setToolTipText(myModel.language.base.getString("table.add_row")); //Add Row
			btnAddRow.setIcon(new ScaledIcon("/images/add",16,16,16,true));
			toolBar.add(btnAddRow);

			JButton btnRemoveRow = new JButton("");
			btnRemoveRow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(viewTable.getRowCount()>1){
						int selected=viewTable.getSelectedRow();
						if(selected==-1){selected=viewTable.getRowCount()-1;} //Select last row
						model.removeRow(selected);
						viewTable.clearSelection();
						textNumRows.setText(model.getRowCount()+"");
					}
				}
			});
			btnRemoveRow.setToolTipText(myModel.language.base.getString("table.remove_row")); //Remove Row
			btnRemoveRow.setIcon(new ScaledIcon("/images/remove",16,16,16,true));
			toolBar.add(btnRemoveRow);

			textNumRows = new JTextField();
			textNumRows.setBackground(SystemColor.info);
			textNumRows.setEditable(false);
			textNumRows.setHorizontalAlignment(SwingConstants.CENTER);
			textNumRows.setText("1");
			toolBar.add(textNumRows);
			textNumRows.setColumns(2);

			JSeparator separator_1 = new JSeparator();
			separator_1.setOrientation(SwingConstants.VERTICAL);
			toolBar.add(separator_1);

			JLabel lblCols = new JLabel(" "+myModel.language.base.getString("table.cols")+":"); //Cols
			toolBar.add(lblCols);

			JButton btnBtnaddcol = new JButton("");
			btnBtnaddcol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int tableType=comboType.getSelectedIndex();
					int selected=viewTable.getSelectedColumn();
					String name=null;
					if(tableType<2){ //Lookup or distribution
						name = JOptionPane.showInputDialog(frmDefineTable, myModel.language.base.getString("table.column_name")+":"); //Column name
					}
					else{ //matrix
						name=model.getColumnCount()+"";
					}
					if(name!=null && !name.isEmpty()){
						if(selected==-1){model.addColumn(name);} //Add column at end
						else{ //Insert column
							int numRows=model.getRowCount();
							int numCols=model.getColumnCount();
							String colNames[]=getColumnNames(model);
							Object data[][]=getTableData(model);
							model.setColumnCount(selected+1); //Trim cols to right
							model.addColumn(name); //Insert new column
							for(int c=selected+1; c<numCols; c++){ //Append original data
								model.addColumn(colNames[c]);
								for(int r=0; r<numRows; r++){
									model.setValueAt(data[r][c], r, c+1);
								}
							}
							if(tableType==2){refreshMatrixColNames();}
						}
						viewTable.clearSelection();
						textNumCols.setText(model.getColumnCount()+"");
					}
				}
			});
			btnBtnaddcol.setIcon(new ScaledIcon("/images/add",16,16,16,true));
			btnBtnaddcol.setToolTipText(myModel.language.base.getString("table.add_column")); //Add Column
			toolBar.add(btnBtnaddcol);

			JButton btnRemoveColumn = new JButton("");
			btnRemoveColumn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int tableType=comboType.getSelectedIndex();
					if((tableType<2 && model.getColumnCount()>2) || (tableType==2 && model.getColumnCount()>1)){
						int numCols=model.getColumnCount();
						int selected=viewTable.getSelectedColumn();
						if(selected==-1){model.setColumnCount(numCols-1);} //Remove last column
						else{
							int numRows=model.getRowCount();
							String colNames[]=getColumnNames(model);
							Object data[][]=getTableData(model);
							model.setColumnCount(selected); //Clear cols to the right
							for(int c=selected+1; c<numCols; c++){ //Append cols
								model.addColumn(colNames[c]);
								for(int r=0; r<numRows; r++){
									model.setValueAt(data[r][c], r, c-1);
								}
							}
							if(tableType==2){refreshMatrixColNames();}
						}
						viewTable.clearSelection();
						textNumCols.setText(numCols-1+"");
					}
				}
			});
			btnRemoveColumn.setIcon(new ScaledIcon("/images/remove",16,16,16,true));
			btnRemoveColumn.setToolTipText(myModel.language.base.getString("table.remove_column")); //Remove Column
			toolBar.add(btnRemoveColumn);
			
			JButton btnEditColName = new JButton("");
			btnEditColName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=viewTable.getSelectedColumn();
					if(selected==-1) {
						JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("err.select_cell")); //Please select a cell in the table!
					}
					else{
						String curName=model.getColumnName(selected);
						String name = JOptionPane.showInputDialog(frmDefineTable, myModel.language.base.getString("table.column_name")+":",curName); //Column name
						if(name!=null && !name.isEmpty()){
							int numCols=model.getColumnCount();
							String colNames[]=new String[numCols];
							for(int c=0; c<numCols; c++){
								colNames[c]=model.getColumnName(c);
							}
							colNames[selected]=name;
							model.setColumnIdentifiers(colNames);
						}
					}
				}
			});
			btnEditColName.setToolTipText(myModel.language.base.getString("table.edit_column_name")); //Edit Column Name
			btnEditColName.setIcon(new ScaledIcon("/images/edit",16,16,16,true));
			toolBar.add(btnEditColName);

			textNumCols = new JTextField();
			textNumCols.setBackground(SystemColor.info);
			textNumCols.setHorizontalAlignment(SwingConstants.CENTER);
			textNumCols.setText("2");
			textNumCols.setEditable(false);
			toolBar.add(textNumCols);
			textNumCols.setColumns(2);

			lblMethod = new JLabel(myModel.language.base.getString("table.lookup_method")+":"); //Lookup Method
			GridBagConstraints gbc_lblMethod = new GridBagConstraints();
			gbc_lblMethod.anchor = GridBagConstraints.EAST;
			gbc_lblMethod.insets = new Insets(0, 0, 5, 5);
			gbc_lblMethod.gridx = 4;
			gbc_lblMethod.gridy = 1;
			frmDefineTable.getContentPane().add(lblMethod, gbc_lblMethod);
			
			comboLookupMethod = new JComboBox<String>();
			comboLookupMethod.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(comboLookupMethod.getSelectedIndex()==1){ //Interpolate
						tableInterpolation.setEnabled(true);
					}
					else{
						tableInterpolation.setEnabled(false);
					}
				}
			});

			//comboLookupMethod.setModel(new DefaultComboBoxModel(new String[] {"Exact", "Interpolate", "Truncate"}));
			String methods[]=new String[3];
			methods[0]=myModel.language.base.getString("table.exact"); //Exact
			methods[1]=myModel.language.base.getString("table.interpolate"); //Interpolate
			methods[2]=myModel.language.base.getString("table.truncate"); //Truncate
			comboLookupMethod.setModel(new DefaultComboBoxModel(methods));
			
			GridBagConstraints gbc_comboLookupMethod = new GridBagConstraints();
			gbc_comboLookupMethod.insets = new Insets(0, 0, 5, 5);
			gbc_comboLookupMethod.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboLookupMethod.gridx = 5;
			gbc_comboLookupMethod.gridy = 1;
			frmDefineTable.getContentPane().add(comboLookupMethod, gbc_comboLookupMethod);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane.gridwidth = 8;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			frmDefineTable.getContentPane().add(scrollPane, gbc_scrollPane);

			model=new DefaultTableModel(new Object[][] {{null, null},},
					new String[] {myModel.language.base.getString("table.index"), myModel.language.analysis.getString("result.value")}); //Index, Value

			viewTable = new JTable();
			viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			viewTable.setShowVerticalLines(true);
			viewTable.setRowSelectionAllowed(false);
			viewTable.getTableHeader().setReorderingAllowed(false);
			viewTable.setModel(model);
			scrollPane.setViewportView(viewTable);

			JLabel lblNotes = new JLabel(myModel.language.base.getString("menu.notes")+":"); //Notes
			GridBagConstraints gbc_lblNotes = new GridBagConstraints();
			gbc_lblNotes.anchor = GridBagConstraints.SOUTHEAST;
			gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
			gbc_lblNotes.gridx = 0;
			gbc_lblNotes.gridy = 3;
			frmDefineTable.getContentPane().add(lblNotes, gbc_lblNotes);

			lblExpectedValue.setVisible(false);
			GridBagConstraints gbc_lblExpectedValue = new GridBagConstraints();
			gbc_lblExpectedValue.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblExpectedValue.insets = new Insets(0, 0, 5, 5);
			gbc_lblExpectedValue.gridx = 2;
			gbc_lblExpectedValue.gridy = 3;
			frmDefineTable.getContentPane().add(lblExpectedValue, gbc_lblExpectedValue);

			textEV = new JTextField();
			textEV.setVisible(false);
			textEV.setEditable(false);
			GridBagConstraints gbc_textEV = new GridBagConstraints();
			gbc_textEV.anchor = GridBagConstraints.NORTH;
			gbc_textEV.insets = new Insets(0, 0, 5, 5);
			gbc_textEV.fill = GridBagConstraints.HORIZONTAL;
			gbc_textEV.gridx = 3;
			gbc_textEV.gridy = 3;
			frmDefineTable.getContentPane().add(textEV, gbc_textEV);
			textEV.setColumns(10);

			btnEvaluate.setVisible(false);
			GridBagConstraints gbc_btnEvaluate = new GridBagConstraints();
			gbc_btnEvaluate.insets = new Insets(0, 0, 5, 5);
			gbc_btnEvaluate.gridx = 4;
			gbc_btnEvaluate.gridy = 3;
			frmDefineTable.getContentPane().add(btnEvaluate, gbc_btnEvaluate);

			JScrollPane scrollPane_1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane_1.gridwidth = 8;
			gbc_scrollPane_1.gridx = 0;
			gbc_scrollPane_1.gridy = 4;
			frmDefineTable.getContentPane().add(scrollPane_1, gbc_scrollPane_1);

			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

			JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						boolean proceed=true;
						//Ensure name is valid and unique
						String testName=textName.getText();
						String strTypes[]=new String[] {"Lookup","Distribution","Matrix"};
						String testType=strTypes[comboType.getSelectedIndex()];
						String strMethods[]=new String[] {"Exact","Interpolate","Truncate"};
						String testLookupMethod=strMethods[comboLookupMethod.getSelectedIndex()];
						//tableInterpolation
						String testInterpolate="";
						String strVal=(String) tableInterpolation.getValueAt(0, 1);
						if(strVal.equals(myModel.language.base.getString("table.linear"))) {testInterpolate="Linear";}
						else if(strVal.equals(myModel.language.base.getString("table.cubic_splines"))) {testInterpolate="Cubic Splines";}

						String testBoundary="";
						strVal=(String) tableInterpolation.getValueAt(1, 1);
						if(strVal.equals(myModel.language.base.getString("table.natural"))) {testBoundary="Natural";}
						else if(strVal.equals(myModel.language.base.getString("table.clamped"))) {testBoundary="Clamped";}
						else if(strVal.equals(myModel.language.base.getString("table.not_a_knot"))) {testBoundary="Not-a-knot";}
						else if(strVal.equals(myModel.language.base.getString("table.periodic"))) {testBoundary="Periodic";}

						String testExtrapolate="";
						strVal=(String) tableInterpolation.getValueAt(2, 1);
						if(strVal.equals(myModel.language.base.getString("button.no"))) {testExtrapolate="No";}
						else if(strVal.equals(myModel.language.base.getString("button.yes"))) {testExtrapolate="Yes";}
						else if(strVal.equals(myModel.language.base.getString("table.left_only"))) {testExtrapolate="Left only";}
						else if(strVal.equals(myModel.language.base.getString("table.right_only"))) {testExtrapolate="Right only";}

						String testNotes=textNotes.getText();
						if(testName.length()==0){
							JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("err.please_enter_name")); //Please enter a name!
							proceed=false;
						}
						else if(Interpreter.isReservedString(testName)){
							//[name] is a reserved variable name!
							String msg=MessageFormat.format(myModel.language.message.getString("err.reserved_name"), testName);
							JOptionPane.showMessageDialog(frmDefineTable, msg);
							proceed=false;
						}
						else{
							//Ensure name is unique
							int index=myModel.getTableIndex(testName);
							if(index!=-1 && index!=tableNum){
								//[name] is already defined as a table!
								String msg=MessageFormat.format(myModel.language.message.getString("err.defined_table"), testName);
								JOptionPane.showMessageDialog(frmDefineTable, msg);
								proceed=false;
							}
							index=myModel.getParameterIndex(testName);
							if(index!=-1){
								//[name] is already defined as a parameter!
								String msg=MessageFormat.format(myModel.language.message.getString("err.defined_parameter"), testName);
								JOptionPane.showMessageDialog(frmDefineTable, msg);
								proceed=false;
							}
							index=myModel.getVariableIndex(testName);
							if(index!=-1){
								//[name] is already defined as a variable!
								String msg=MessageFormat.format(myModel.language.message.getString("err.defined_variable"), testName);
								JOptionPane.showMessageDialog(frmDefineTable, msg);
								proceed=false;
							}

							//Ensure name is valid
							for(int i=0; i<testName.length(); i++){
								if(Interpreter.isBreak(testName.charAt(i))){
									//Invalid character in name
									JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("err.invalid_character")+": "+testName.charAt(i));
									proceed=false;
								}
							}

							for(int d=0; d<myModel.dimInfo.dimSymbols.length; d++){
								if(testName.equals(myModel.dimInfo.dimSymbols[d])){
									//[name] is a dimension label!
									String msg=MessageFormat.format(myModel.language.message.getString("err.dim_label"), testName);
									JOptionPane.showMessageDialog(frmDefineTable, msg);
									proceed=false;
								}
							}
						}

						if(proceed==true){
							if(comboType.getSelectedIndex()==0){ //Lookup
								proceed=checkTable(); //Check table values
								if(proceed==true){//Ensure index is ascending
									for(int r=1; r<tempTable.length; r++){
										double index0=tempTable[r-1][0];
										double index1=tempTable[r][0];
										if(index1<=index0){
											//Error: Index is not strictly ascending!
											JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("err.index_ascending")+" ("+index1+"<="+index0+")");
											proceed=false;
											r=tempTable.length; //Quit loop
										}
									}
								}
								if(comboLookupMethod.getSelectedIndex()==1){ //Interpolate
									if(viewTable.getRowCount()<2){
										//Error: At least 2 points are needed for interpolation!
										JOptionPane.showMessageDialog(frmDefineTable, myModel.language.message.getString("err.interpolate_points"));
										proceed=false;
									}
								}
							}
							else if(comboType.getSelectedIndex()==1){ //Distribution
								proceed=checkTable(); //Check table values
								if(proceed==true){ //Ensure probabilities sum to 1.0
									double sum=0;
									for(int r=0; r<tempTable.length; r++){
										sum+=tempTable[r][0];
									}
									if(Math.abs(sum-1.0)>MathUtils.tolerance){
										//Probabilities sum to [value]!
										String msg=MessageFormat.format(myModel.language.message.getString("err.prob_sum"), sum);
										JOptionPane.showMessageDialog(frmDefineTable, msg);
										proceed=false;
									}
								}
							}
							else if(comboType.getSelectedIndex()==2){ //Matrix
								proceed=checkTable();
							}

							if(proceed==true){
								if(tableNum==-1){
									myModel.saveSnapshot(myModel.language.base.getString("button.add_table")); //Add to undo stack (Add Table)
									table.name=testName;
									table.type=testType;
									table.lookupMethod=testLookupMethod;
									parseTable();
									table.notes=testNotes;
									table.myModel=myModel;
									myModel.tables.add(table);
									myModel.addTable(table);
								}
								else{
									boolean changed=false;
									if(!table.name.equals(testName)){changed=true;}
									if(!table.type.matches(testType)){changed=true;}
									if(!table.lookupMethod.matches(testLookupMethod)){changed=true;}
									if(table.interpolate!=null){
										if(!table.interpolate.matches(testInterpolate)){changed=true;}
										if(table.boundary!=null && !table.boundary.matches(testBoundary)){changed=true;}
										if(!table.extrapolate.matches(testExtrapolate)){changed=true;}
									}
									if(matchTable()==false){changed=true;}
									if(table.notes!=null && !table.notes.equals(testNotes)){changed=true;}

									if(changed){myModel.saveSnapshot(myModel.language.base.getString("button.edit_table"));} //Add to undo stack (Edit Table)
									table.name=testName;
									table.type=testType;
									table.lookupMethod=testLookupMethod;
									parseTable();
									table.notes=testNotes;
									myModel.editTable(tableNum);
								}
								myModel.validateModelObjects(); //Update all model objects
								myModel.rescale(myModel.scale); //Re-validates textfields

								frmDefineTable.dispose();
							}
						}
					}catch(Exception e1) {
						e1.printStackTrace();
						myModel.errorLog.recordError(e1);
						JOptionPane.showMessageDialog(frmDefineTable, e1.toString());
					}
				}
			});
			GridBagConstraints gbc_btnSave = new GridBagConstraints();
			gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnSave.insets = new Insets(0, 0, 0, 5);
			gbc_btnSave.anchor = GridBagConstraints.NORTH;
			gbc_btnSave.gridx = 4;
			gbc_btnSave.gridy = 5;
			frmDefineTable.getContentPane().add(btnSave, gbc_btnSave);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineTable.dispose();
				}
			});
			GridBagConstraints gbc_btnCancel = new GridBagConstraints();
			gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
			gbc_btnCancel.gridx = 5;
			gbc_btnCancel.gridy = 5;
			frmDefineTable.getContentPane().add(btnCancel, gbc_btnCancel);

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private boolean checkTable(){
		boolean valid=true;
		int numRows=viewTable.getRowCount();
		int numCols=viewTable.getColumnCount();
		tempHeaders=new String[numCols];
		tempTable=new double[numRows][numCols];
		for(int c=0; c<numCols; c++){
			tempHeaders[c]=viewTable.getColumnName(c);
			for(int r=0; r<numRows; r++){
				try{
					tempTable[r][c]=Double.parseDouble((String)viewTable.getValueAt(r, c));
				}catch(Exception e){
					valid=false;
					//Invalid entry: [value] (Row: [r] Col: [c])
					String msg=MessageFormat.format(myModel.language.message.getString("err.invalid_entry"), viewTable.getValueAt(r,c), r, c);
					JOptionPane.showMessageDialog(frmDefineTable, msg);
					c=numCols; //Quit loop
					r=numRows;
				}
			}
		}
		return(valid);
	}

	private boolean matchTable(){
		boolean match=true;
		int numRows=viewTable.getRowCount();
		int numCols=viewTable.getColumnCount();
		if(table.numRows!=numRows || table.numCols!=numCols){match=false;} //Different dimensions
		else{
			for(int c=0; c<numCols; c++){
				if(!tempHeaders[c].equals(table.headers[c])){
					match=false;
					c=numCols; //Quit loop
				}
				if(match==true){
					for(int r=0; r<numRows; r++){
						if(tempTable[r][c]!=table.data[r][c]){
							match=false;
							c=numCols; //Quit loop
							r=numRows;
						}
					}
				}
			}
		}
		return(match);
	}

	private void parseTable(){
		if(table.lookupMethod.matches("Interpolate")){
			//Method
			String strVal=(String) tableInterpolation.getValueAt(0, 1);
			if(strVal.equals(myModel.language.base.getString("table.linear"))) {table.interpolate="Linear";}
			else if(strVal.equals(myModel.language.base.getString("table.cubic_splines"))) {table.interpolate="Cubic Splines";}
			//Extrapolate
			strVal=(String) tableInterpolation.getValueAt(2, 1);
			if(strVal.equals(myModel.language.base.getString("button.no"))) {table.extrapolate="No";}
			else if(strVal.equals(myModel.language.base.getString("button.yes"))) {table.extrapolate="Yes";}
			else if(strVal.equals(myModel.language.base.getString("table.left_only"))) {table.extrapolate="Left only";}
			else if(strVal.equals(myModel.language.base.getString("table.right_only"))) {table.extrapolate="Right only";}
			if(table.interpolate.matches("Cubic Splines")){
				//Boundary condition
				strVal=(String) tableInterpolation.getValueAt(1, 1);
				if(strVal.equals(myModel.language.base.getString("table.natural"))) {table.boundary="Natural";}
				else if(strVal.equals(myModel.language.base.getString("table.clamped"))) {table.boundary="Clamped";}
				else if(strVal.equals(myModel.language.base.getString("table.not_a_knot"))) {table.boundary="Not-a-knot";}
				else if(strVal.equals(myModel.language.base.getString("table.periodic"))) {table.boundary="Periodic";}
			}
			else{
				table.boundary=null;
			}
		}
		else{
			table.interpolate=null;
			table.boundary=null;
			table.extrapolate=null;
		}
		
		table.numRows=viewTable.getRowCount();
		table.numCols=viewTable.getColumnCount();
		table.headers=new String[table.numCols];
		table.data=new double[table.numRows][table.numCols];
		for(int c=0; c<table.numCols; c++){
			table.headers[c]=viewTable.getColumnName(c);
			//strip \r or \n characters
			table.headers[c]=table.headers[c].replaceAll("\\r", "");
			table.headers[c]=table.headers[c].replaceAll("\\n", "");
			
			for(int r=0; r<table.numRows; r++){
				table.data[r][c]=Double.parseDouble((String)viewTable.getValueAt(r, c));
			}
		}
		if(table.interpolate!=null && table.interpolate.matches("Cubic Splines")){
			table.constructSplines();
		}
	}

	private void getTable(){
		table=myModel.tables.get(tableNum);
		textName.setText(table.name);
		//Type
		if(table.type.matches("Lookup")) {comboType.setSelectedIndex(0);}
		else if(table.type.matches("Distribution")) {comboType.setSelectedIndex(1);}
		else if(table.type.matches("Matrix")) {comboType.setSelectedIndex(2);}
		//Lookup Method
		if(table.lookupMethod.matches("Exact")) {comboLookupMethod.setSelectedIndex(0);}
		else if(table.lookupMethod.matches("Interpolate")) {comboLookupMethod.setSelectedIndex(1);}
		else if(table.lookupMethod.matches("Truncate")) {comboLookupMethod.setSelectedIndex(2);}
		
		if(table.lookupMethod.matches("Interpolate")){
			//Method
			String strVal="";
			if(table.interpolate.matches("Linear")) {strVal=myModel.language.base.getString("table.linear");}
			else if(table.interpolate.matches("Cubic Splines")) {strVal=myModel.language.base.getString("table.cubic_splines");}
			tableInterpolation.setValueAt(strVal, 0, 1);	
			//Extrapolate
			strVal="";
			if(table.extrapolate.matches("No")) {strVal=myModel.language.base.getString("button.no");}
			else if(table.extrapolate.matches("Yes")) {strVal=myModel.language.base.getString("button.yes");}
			else if(table.extrapolate.matches("Left only")) {strVal=myModel.language.base.getString("table.left_only");}
			else if(table.extrapolate.matches("Right only")) {strVal=myModel.language.base.getString("table.right_only");}
			tableInterpolation.setValueAt(strVal, 2, 1);	
			//Boundary condition
			if(table.interpolate.matches("Cubic Splines")){
				strVal="";
				if(table.boundary.matches("Natural")){strVal=myModel.language.base.getString("table.natural");}
				else if(table.boundary.matches("Clamped")){strVal=myModel.language.base.getString("table.clamped");}
				else if(table.boundary.matches("Not-a-knot")){strVal=myModel.language.base.getString("table.not_a_knot");}
				else if(table.boundary.matches("Periodic")){strVal=myModel.language.base.getString("table.periodic");}
				tableInterpolation.setValueAt(strVal, 1, 1);
				tableInterpolation.enabled[1]=true;
			}
		}
		textNumRows.setText(table.numRows+"");
		textNumCols.setText(table.numCols+"");
		model.setColumnCount(0);
		for(int c=0; c<table.numCols; c++){
			model.addColumn(table.headers[c]);
		}
		model.setRowCount(table.numRows);
		for(int r=0; r<table.numRows; r++){
			for(int c=0; c<table.numCols; c++){
				model.setValueAt(table.data[r][c]+"", r, c);
			}
		}
		textNotes.setText(table.notes);
		if(table.type.matches("Distribution")){
			lblMethod.setEnabled(false);
			comboLookupMethod.setEnabled(false);
			lblExpectedValue.setVisible(true);
			btnEvaluate.setVisible(true);
			textEV.setVisible(true);
			textEV.setText(table.calcEV(1)+"");
		}
	}

	private String[] getColumnNames(DefaultTableModel model){
		int numCols=model.getColumnCount();
		String names[]=new String[numCols];
		for(int c=0; c<numCols; c++){
			names[c]=model.getColumnName(c);
		}
		return(names);
	}

	private Object[][] getTableData(DefaultTableModel model){
		int numRows=model.getRowCount();
		int numCols=model.getColumnCount();
		Object[][] data=new Object[numRows][numCols];
		for(int r=0; r<numRows; r++){
			for(int c=0; c<numCols; c++){
				data[r][c]=model.getValueAt(r,c);
			}
		}
		return(data);
	}
	
	private void refreshMatrixColNames(){
		Vector<Integer> newColNames=new Vector<Integer>();
		for(int c=0; c<model.getColumnCount(); c++){newColNames.add(c);}
		model.setColumnIdentifiers(newColNames);
	}
}

class interpolateTable extends JTable{
	public boolean enabled[]=new boolean[]{false,false,false};
	boolean isCubic=false;
	interpolateTable thisTable=this;
	Language language;
	
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(int i=0; i<3; i++){this.enabled[i]=enabled;}
		if(isCubic==false){this.enabled[1]=false;}
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column){
		if(column==1){
			if(row==0){ //Interpolation method
				final JComboBox<String> comboInterpolate = new JComboBox<String>();
				comboInterpolate.addItem(language.base.getString("table.linear")); //Linear
				comboInterpolate.addItem(language.base.getString("table.cubic_splines")); //Cubic Splines
				comboInterpolate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int selected=comboInterpolate.getSelectedIndex();
						if(selected==0){ //Linear
							enabled[1]=false;
							isCubic=false;
							thisTable.repaint();
						}
						else if(selected==1){ //Cubic Splines
							enabled[1]=true;
							isCubic=true;
							thisTable.repaint();
						}
					}
				});
				return(new DefaultCellEditor(comboInterpolate));
			}
			else if(row==1){ //Boundary conditions
				JComboBox<String> comboBoundary = new JComboBox<String>();
				comboBoundary.addItem(language.base.getString("table.natural")); //Natural
				comboBoundary.addItem(language.base.getString("table.clamped")); //Clamped
				comboBoundary.addItem(language.base.getString("table.not_a_knot")); //Not-a-knot
				comboBoundary.addItem(language.base.getString("table.periodic")); //Periodic
				comboBoundary.setEnabled(enabled[1]);
				return(new DefaultCellEditor(comboBoundary));
			}
			else if(row==2){ //Extrapolate
				JComboBox<String> comboExtrapolate= new JComboBox<String>();
				comboExtrapolate.addItem(language.base.getString("button.no")); //No
				comboExtrapolate.addItem(language.base.getString("button.yes")); //Yes
				comboExtrapolate.addItem(language.base.getString("table.left_only")); //Left only
				comboExtrapolate.addItem(language.base.getString("table.right_only")); //Right only
				return(new DefaultCellEditor(comboExtrapolate));
			}
		}
		return(null);
	}
}
