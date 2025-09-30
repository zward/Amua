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

import javax.swing.JFrame;
import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import base.AmuaModel;
import export_Cpp.CppMarkovCohort;
import export_Cpp.CppMarkovMonteCarlo;
import export_Cpp.CppTreeCohort;
import export_Cpp.CppTreeMonteCarlo;
import export_Java.JavaMarkovCohort;
import export_Java.JavaMarkovMonteCarlo;
import export_Java.JavaTreeCohort;
import export_Java.JavaTreeMonteCarlo;
import export_Python.PythonMarkovCohort;
import export_Python.PythonMarkovMonteCarlo;
import export_Python.PythonTreeCohort;
import export_Python.PythonTreeMonteCarlo;
import export_R.RMarkovCohort;
import export_R.RMarkovMonteCarlo;
import export_R.RTreeCohort;
import export_R.RTreeMonteCarlo;
import filters.AmuaModelFilter;
import filters.CSVFilter;
import main.Constraint;
import main.Parameter;
import main.ScaledIcon;
import main.Variable;
import markov.MarkovNode;
import math.Interpreter;
import tree.TreeNode;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Cursor;
import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmImport2 {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmImport2;
	AmuaModel myModel;
	JTextArea textDescription;
	
	private JTextField textFilepath;
	JButton btnImport;
	
	
	public frmImport2(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmImport2 = new JDialog();
			frmImport2.setIconImage(Toolkit.getDefaultToolkit().getImage(frmImport2.class.getResource("/images/import_128.png")));
			frmImport2.setModalityType(ModalityType.APPLICATION_MODAL);
			frmImport2.setTitle("Amua - "+myModel.language.base.getString("title.import_objects")); //Import Objects
			frmImport2.setResizable(false);
			frmImport2.setBounds(100, 100, 582, 328);
			frmImport2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmImport2.getContentPane().setLayout(null);
			
			JLabel lblSelectAnExport = new JLabel(myModel.language.message.getString("ask.select_source")+":"); //Select source
			lblSelectAnExport.setBounds(12, 23, 146, 16);
			frmImport2.getContentPane().add(lblSelectAnExport);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 42, 226, 145);
			frmImport2.getContentPane().add(scrollPane);
			
			textDescription = new JTextArea();
			textDescription.setEditable(false);
			textDescription.setLineWrap(true);
			textDescription.setBounds(243, 41, 327, 146);
			frmImport2.getContentPane().add(textDescription);
						
			final JList<String> list = new JList<String>();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					//updates description
					int index=list.getSelectedIndex();
					if(index==0) { //Amua model
						textDescription.setText(myModel.language.message.getString("info.imports_objects_model")); //Imports model objects from an existing Amua model.
					}
					else if(index==1) {
						//Imports model objects from a .csv file.\n\nExpected columns are:\n| Name | Expression | Notes (optional) |\n\nThe first row should contain the column names.\nSubsequent rows should define model objects."
						textDescription.setText(myModel.language.message.getString("info.imports_objects_csv"));
					}
				}
			});
			list.setModel(new AbstractListModel() {
				String[] values = new String[] {myModel.language.base.getString("file.amua"), myModel.language.base.getString("file.csv")}; //Amua model, CSV
				public int getSize() {
					return values.length;
				}
				public Object getElementAt(int index) {
					return values[index];
				}
			});
			list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane.setViewportView(list);
			
			JButton btnBrowse = new JButton(myModel.language.base.getString("button.browse")); //Browse
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						int selected=list.getSelectedIndex();
						if(selected!=-1){

							if(selected==0) { //Amua model
								JFileChooser fc=null;
								fc=new JFileChooser(myModel.filepath);
								fc.setFileFilter(new AmuaModelFilter(myModel.language));
								fc.setAcceptAllFileFilterUsed(false);

								fc.setDialogTitle(myModel.language.base.getString("title.import_objects")); //Import Objects

								int returnVal = fc.showDialog(frmImport2, myModel.language.base.getString("menu.import")); //Import
								if (returnVal == JFileChooser.APPROVE_OPTION) {
									File file = fc.getSelectedFile();
									String path=file.getAbsolutePath();
									if(path.equals(myModel.filepath)) {
										JOptionPane.showMessageDialog(frmImport2, myModel.language.message.getString("err.select_different_model")); //Please select a different model!
									}
									else { //open donor model
										frmImport2.setCursor(new Cursor(Cursor.WAIT_CURSOR));

										JAXBContext context = JAXBContext.newInstance(AmuaModel.class);
										Unmarshaller un = context.createUnmarshaller();
										AmuaModel donorModel = (AmuaModel) un.unmarshal(new File(path));

										frmImport2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

										frmImport window=new frmImport(myModel,donorModel);
										window.frmImport.setVisible(true);
										
										frmImport2.dispose();
									}
								}
							}
							else if(selected==1) { //CSV
								JFileChooser fc=null;
								fc=new JFileChooser(myModel.filepath);
								fc.setFileFilter(new CSVFilter(myModel.language));
								fc.setAcceptAllFileFilterUsed(false);

								fc.setDialogTitle(myModel.language.base.getString("title.import_objects")); //Import Objects
								int returnVal = fc.showOpenDialog(frmImport2);
								if (returnVal == JFileChooser.APPROVE_OPTION) {
									File file = fc.getSelectedFile();
									textFilepath.setText(file.getAbsolutePath());
								
									btnImport.setEnabled(true);
								}
							}
						}
					}catch(Exception e1){
						frmImport2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(frmImport2, myModel.language.message.getString("error")+": "+e1.getMessage()); //Error
						myModel.errorLog.recordError(e1);
					}
				}
			});
			btnBrowse.setBounds(12, 187, 90, 28);
			frmImport2.getContentPane().add(btnBrowse);
			
			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmImport2.dispose();
				}
			});
			btnCancel.setBounds(146, 187, 90, 28);
			frmImport2.getContentPane().add(btnCancel);
			
			JLabel lblNewLabel = new JLabel(myModel.language.base.getString("file.filepath")+":"); //Filepath
			lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			lblNewLabel.setBounds(6, 233, 96, 16);
			frmImport2.getContentPane().add(lblNewLabel);
			
			textFilepath = new JTextField();
			textFilepath.setBounds(103, 227, 461, 28);
			frmImport2.getContentPane().add(textFilepath);
			textFilepath.setColumns(10);
			
			JLabel lblNewLabel_1 = new JLabel(myModel.language.base.getString("object.object_type")+":"); //Object Type
			lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
			lblNewLabel_1.setBounds(6, 261, 96, 16);
			frmImport2.getContentPane().add(lblNewLabel_1);
			
			JComboBox comboBox = new JComboBox();
			comboBox.setModel(new DefaultComboBoxModel(new String[] {
					myModel.language.base.getString("object.parameters"), //Parameters
					myModel.language.base.getString("object.variables"), //Variables
					myModel.language.base.getString("object.constraints")})); //Constraints
			comboBox.setBounds(103, 256, 132, 26);
			frmImport2.getContentPane().add(comboBox);
			
			JCheckBox chckbxOverwrite = new JCheckBox(myModel.language.base.getString("object.overwrite_current")); //Overwrite current objects
			chckbxOverwrite.setBounds(238, 260, 216, 18);
			frmImport2.getContentPane().add(chckbxOverwrite);
			
			btnImport = new JButton(myModel.language.base.getString("button.import_csv")); //Import CSV
			btnImport.setEnabled(false);
			btnImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
					
						boolean overwrite=chckbxOverwrite.isSelected();
						int type=comboBox.getSelectedIndex();
						myModel.saveSnapshot(myModel.language.base.getString("title.import_objects")); //Add to undo stack (Import Objects)

						//read CSV
						String path=textFilepath.getText();
						FileInputStream fstream = new FileInputStream(path);
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String strLine;
						strLine=br.readLine(); //Headers
						strLine=br.readLine(); //First line
						while(strLine!=null){
							String data[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
							if(type==0) { //parameters
								//define parameter
								Parameter curParam=new Parameter();
								curParam.name=data[0].replaceAll("\"", ""); //strip quotes
								curParam.expression=data[1].replaceAll("\"", ""); //strip quotes
								if(data.length>2) {
									curParam.notes=data[2].replaceAll("\"", ""); //strip quotes
								}
								
								//add to model
								if(myModel.isParameter(curParam.name)) { //parameter already exists
									if(overwrite==true) {
										//remove old parameter
										int paramNum=myModel.getParameterIndex(curParam.name);
										myModel.parameters.remove(paramNum);
										myModel.mainForm.modelParameters.removeRow(paramNum);
										//add new parameter
										if(validateName(curParam.name)) {
											myModel.parameters.add(curParam.copy());
											myModel.mainForm.modelParameters.addRow(new Object[]{curParam.name,curParam.expression});
										}
									}
								}
								else { //new parameter
									if(validateName(curParam.name)) {
										myModel.parameters.add(curParam.copy());
										myModel.mainForm.modelParameters.addRow(new Object[]{curParam.name,curParam.expression});
									}
								}
							}
							else if(type==1) { //variables
								//define variable
								Variable curVar=new Variable();
								curVar.name=data[0].replaceAll("\"", ""); //strip quotes
								curVar.expression=data[1].replaceAll("\"", ""); //strip quotes
								if(data.length>2) {
									curVar.notes=data[2].replaceAll("\"", ""); //strip quotes
								}
								
								//add to model
								if(myModel.isVariable(curVar.name)) { //variable already exists
									if(overwrite==true) {
										//remove old variable
										int varNum=myModel.getVariableIndex(curVar.name);
										myModel.variables.remove(varNum);
										myModel.mainForm.modelVariables.removeRow(varNum);
										//add new variable
										if(validateName(curVar.name)) {
											myModel.variables.add(curVar.copy());
											myModel.mainForm.modelVariables.addRow(new Object[]{curVar.name,curVar.expression});
										}
									}
								}
								else { //new variable
									if(validateName(curVar.name)) {
										myModel.variables.add(curVar.copy());
										myModel.mainForm.modelVariables.addRow(new Object[]{curVar.name,curVar.expression});
									}
								}
							}
							else if(type==2) { //constraints
								//define constraint
								Constraint curConst=new Constraint();
								curConst.name=data[0].replaceAll("\"", ""); //strip quotes
								curConst.expression=data[1].replaceAll("\"", ""); //strip quotes
								if(data.length>2) {
									curConst.notes=data[2].replaceAll("\"", ""); //strip quotes
								}
								
								//add constraint
								myModel.constraints.add(curConst.copy());
								myModel.mainForm.modelConstraints.addRow(new Object[]{curConst.name,curConst.expression});
							}
							
							strLine=br.readLine(); //next line
						}
						br.close();

						myModel.validateModelObjects(); //Update all model objects
						myModel.rescale(myModel.scale); //Re-validates textfields
						
						frmImport2.dispose();
						
					} catch(Exception e1) {
						frmImport2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(frmImport2, myModel.language.message.getString("error")+": "+e1.getMessage()); //Error
						myModel.errorLog.recordError(e1);
					}
					
				}
			});
			btnImport.setBounds(454, 255, 90, 28);
			frmImport2.getContentPane().add(btnImport);
			
			JLabel lblDescription = new JLabel(myModel.language.base.getString("title.description")+":"); //Description
			lblDescription.setBounds(243, 23, 90, 16);
			frmImport2.getContentPane().add(lblDescription);
			
						
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private boolean validateName(String testName) {
		boolean valid=true;
		if(testName.length()==0){
			JOptionPane.showMessageDialog(frmImport2, myModel.language.message.getString("err.empty_name")); //Error: Empty name!
			valid=false;
		}
		else if(Interpreter.isReservedString(testName)){
			//[name] is a reserved variable name!
			String msg=MessageFormat.format(myModel.language.message.getString("err.reserved_name"), testName);
			JOptionPane.showMessageDialog(frmImport2, msg);
			valid=false;
		}
		else{
			//Ensure name is unique
			int index=myModel.getTableIndex(testName);
			if(index!=-1){
				//[name] is already defined as a table!
				String msg=MessageFormat.format(myModel.language.message.getString("err.defined_table"), testName);
				JOptionPane.showMessageDialog(frmImport2, msg);
				valid=false;
			}
			index=myModel.getParameterIndex(testName);
			if(index!=-1){
				//[name] is already defined as a parameter!
				String msg=MessageFormat.format(myModel.language.message.getString("err.defined_parameter"), testName);
				JOptionPane.showMessageDialog(frmImport2, msg);
				valid=false;
			}
			index=myModel.getVariableIndex(testName);
			if(index!=-1){
				//[name] is already defined as a variable!
				String msg=MessageFormat.format(myModel.language.message.getString("err.defined_variable"), testName);
				JOptionPane.showMessageDialog(frmImport2, msg);
				valid=false;
			}
			
			//Ensure name is valid
			for(int i=0; i<testName.length(); i++){
				if(Interpreter.isBreak(testName.charAt(i))){
					//Invalid character in name
					JOptionPane.showMessageDialog(frmImport2, myModel.language.message.getString("err.invalid_character")+": "+testName.charAt(i));
					valid=false;
				}
			}
								
			for(int d=0; d<myModel.dimInfo.dimSymbols.length; d++){
				if(testName.equals(myModel.dimInfo.dimSymbols[d])){
					//[name] is a dimension label!
					String msg=MessageFormat.format(myModel.language.message.getString("err.dim_label"), testName);
					JOptionPane.showMessageDialog(frmImport2, msg);
					valid=false;
				}
			}
		}
		
		return(valid);
		
	}
}
