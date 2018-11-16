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
import markov.MarkovNode;
import tree.TreeNode;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 *
 */
public class frmExport {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmExport;
	JTextArea textArea;
	AmuaModel myModel;
	JLabel lblTargetLogo;
	JLabel lblTableFormat;
	JComboBox<String> comboTables;
	
	public frmExport(AmuaModel myModel){
		initialize();
		this.myModel=myModel;
		if(myModel.tables.size()>0){
			lblTableFormat.setEnabled(true);
			comboTables.setEnabled(true);
		}
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmExport = new JDialog();
			frmExport.setModalityType(ModalityType.APPLICATION_MODAL);
			frmExport.setTitle("Amua - Export Model");
			frmExport.setResizable(false);
			frmExport.setBounds(100, 100, 500, 300);
			frmExport.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmExport.getContentPane().setLayout(null);
			
			JLabel lblSelectAnExport = new JLabel("Select an export format:");
			lblSelectAnExport.setBounds(12, 23, 146, 16);
			frmExport.getContentPane().add(lblSelectAnExport);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 42, 188, 145);
			frmExport.getContentPane().add(scrollPane);
			
			final String descriptions[]=new String[5];
			descriptions[0]="C++ source files";
			//descriptions[1]="Creates an XML Spreadsheet that is compatible with Excel.  The model is converted to matrix form.";
			descriptions[1]="Java source files";
			descriptions[2]="Python 3 source files\n(requires NumPy library)";
			descriptions[3]="R source files";
			
			final String logos[]=new String[5];
			logos[0]="/images/cppIcon_48.png";
			//logos[1]="/images/excelIcon_48.png";
			logos[1]="/images/javaIcon_48.png";
			logos[2]="/images/pythonIcon_48.png";
			logos[3]="/images/rIcon_48.png";
			
			final JList<String> list = new JList<String>();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int selected=list.getSelectedIndex();
					textArea.setText(descriptions[selected]);
					lblTargetLogo.setIcon(new ImageIcon(frmExport.class.getResource(logos[selected])));
				}
			});
			list.setModel(new AbstractListModel<String>() {
				String[] values = new String[] {"C++", "Java", "Python", "R"};
				public int getSize() {
					return values.length;
				}
				public String getElementAt(int index) {
					return values[index];
				}
			});
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane.setViewportView(list);
			
			JLabel lblDescription = new JLabel("Description:");
			lblDescription.setBounds(232, 23, 86, 16);
			frmExport.getContentPane().add(lblDescription);
			
			JButton btnExport = new JButton("Export");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=list.getSelectedIndex();
					if(selected!=-1){
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						
						fc.setDialogTitle("Export Model");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmExport);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String dir=file.getAbsolutePath();
							
							int tableFormat=-1;
							if(comboTables.isEnabled()){tableFormat=comboTables.getSelectedIndex();}
							normalizeNodeNames();
							if(myModel.type==0){ //Decision tree
								if(selected==0){ //C++
									if(myModel.simType==0){new CppTreeCohort(dir,myModel,tableFormat);}
									else if(myModel.simType==1){new CppTreeMonteCarlo(dir,myModel,tableFormat);}
								}
								else if(selected==1){ //Java
									if(myModel.simType==0){new JavaTreeCohort(dir, myModel,tableFormat);}
									else if(myModel.simType==1){new JavaTreeMonteCarlo(dir, myModel, tableFormat);}
								}
								else if(selected==2){ //Python
									if(myModel.simType==0){new PythonTreeCohort(dir, myModel,tableFormat);}
									else if(myModel.simType==1){new PythonTreeMonteCarlo(dir, myModel, tableFormat);}
								}
								else if(selected==3){ //R
									if(myModel.simType==0){new RTreeCohort(dir, myModel,tableFormat);}
									else if(myModel.simType==1){new RTreeMonteCarlo(dir, myModel, tableFormat);}
								}
							}
							else if(myModel.type==1){ //Markov model
								if(selected==0){ //C++
									if(myModel.simType==0){new CppMarkovCohort(dir,myModel,tableFormat);}
									else if(myModel.simType==1){new CppMarkovMonteCarlo(dir,myModel,tableFormat);}
								}
								if(selected==1){ //Java
									if(myModel.simType==0){new JavaMarkovCohort(dir,myModel,tableFormat);}
									else if(myModel.simType==1){new JavaMarkovMonteCarlo(dir,myModel,tableFormat);}
								}
								else if(selected==2){ //Python
									if(myModel.simType==0){new PythonMarkovCohort(dir,myModel,tableFormat);}
									else if(myModel.simType==1){new PythonMarkovMonteCarlo(dir,myModel,tableFormat);}
								}
								else if(selected==3){ //R
									if(myModel.simType==0){new RMarkovCohort(dir,myModel,tableFormat);}
									else if(myModel.simType==1){new RMarkovMonteCarlo(dir,myModel,tableFormat);}
								}
							}
							
							JOptionPane.showMessageDialog(frmExport, "Exported!");
							frmExport.dispose();
						}

					}
				}
			});
			btnExport.setBounds(274, 232, 90, 28);
			frmExport.getContentPane().add(btnExport);
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExport.dispose();
				}
			});
			btnCancel.setBounds(381, 232, 90, 28);
			frmExport.getContentPane().add(btnCancel);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(232, 42, 256, 145);
			frmExport.getContentPane().add(scrollPane_1);
			
			textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			scrollPane_1.setViewportView(textArea);
			
			JLabel lblLogoAmua = new JLabel("New label");
			lblLogoAmua.setIcon(new ImageIcon(frmExport.class.getResource("/images/logo_48.png")));
			lblLogoAmua.setBounds(13, 199, 48, 48);
			frmExport.getContentPane().add(lblLogoAmua);
			
			lblTargetLogo = new JLabel("");
			lblTargetLogo.setBounds(147, 199, 48, 48);
			frmExport.getContentPane().add(lblTargetLogo);
			
			JLabel lblArrow = new JLabel("New label");
			lblArrow.setIcon(new ImageIcon(frmExport.class.getResource("/images/arrow_48.png")));
			lblArrow.setBounds(80, 199, 48, 48);
			frmExport.getContentPane().add(lblArrow);
			
			JLabel lblModelType = new JLabel("lbl");
			lblModelType.setIcon(new ImageIcon(frmExport.class.getResource("/images/modelTree_16.png")));
			lblModelType.setBounds(96, 195, 16, 16);
			frmExport.getContentPane().add(lblModelType);
			
			lblTableFormat = new JLabel("Table format:");
			lblTableFormat.setEnabled(false);
			lblTableFormat.setBounds(232, 195, 72, 16);
			frmExport.getContentPane().add(lblTableFormat);
			
			comboTables = new JComboBox<String>();
			comboTables.setEnabled(false);
			comboTables.setModel(new DefaultComboBoxModel<String>(new String[] {"In-line", "Separate Files"}));
			comboTables.setBounds(309, 190, 162, 26);
			frmExport.getContentPane().add(comboTables);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void normalizeNodeNames(){
		ArrayList<String> distinctNames= new ArrayList<String>();
		if(myModel.type==0){ //Decision Tree
			ArrayList<TreeNode> nodes=myModel.tree.nodes;
			int numNodes=nodes.size();
			for(int i=0; i<numNodes; i++){
				String name=checkName(nodes.get(i).name);
				//Ensure name is distinct		
				String testName=name;
				int index=distinctNames.indexOf(name);
				int c=1;
				while(index!=-1){
					testName=name+c;
					c++;
					index=distinctNames.indexOf(testName);
				}
				distinctNames.add(testName);
				nodes.get(i).nameExport=testName;
			}
		}
		else if(myModel.type==1){ //Markov
			ArrayList<MarkovNode> nodes=myModel.markov.nodes;
			int numNodes=nodes.size();
			for(int i=0; i<numNodes; i++){
				String name=checkName(nodes.get(i).name);
				//Ensure name is distinct		
				String testName=name;
				int index=distinctNames.indexOf(name);
				int c=1;
				while(index!=-1){
					testName=name+c;
					c++;
					index=distinctNames.indexOf(testName);
				}
				distinctNames.add(testName);
				nodes.get(i).nameExport=testName;
			}
		}
		
	}
		
	private String checkName(String name){
		if(name==null){name="node";}
		//Remove punctuation or special characters
		name=name.replaceAll(" ", ""); //Remove spaces
		name=name.replaceAll(",", ""); 
		name=name.replaceAll("\\.", "");
		name=name.replaceAll(";", "");
		name=name.replaceAll(":", "");
		name=name.replaceAll("|", "");
		name=name.replaceAll("\"", ""); //Quotation marks
		name=name.replaceAll("'", ""); //Apostrophe
		name=name.replaceAll("\\?", "");  
		name=name.replaceAll("!", "");  
		name=name.replaceAll("`", "");
		name=name.replaceAll("~", "");
		name=name.replaceAll("@", "");  
		name=name.replaceAll("#", "");  
		name=name.replaceAll("$", "");  
		name=name.replaceAll("%", "");  
		name=name.replaceAll("^", "");
		name=name.replaceAll("&", "");
		name=name.replaceAll("=", "");
		//Braces
		name=name.replaceAll("\\(", ""); name=name.replaceAll("\\)", "");
		name=name.replaceAll("\\[", ""); name=name.replaceAll("\\]", "");
		name=name.replaceAll("\\{", ""); name=name.replaceAll("\\}", "");
		name=name.replaceAll("<", ""); name=name.replaceAll(">", "");
		//Replace any operators with "_"
		name=name.replaceAll("-", "_minus");  //Remove -
		name=name.replaceAll("\\+", "_plus");  //Remove +
		name=name.replaceAll("/", "_fslash");  //Remove /
		name=name.replaceAll("\\\\", "_bslash");  //Remove \
		name=name.replaceAll("\\*", "_star");  //Remove *
					
		if(name.length()==0){name="node";}
		//Ensure name does not begin with a number
		String firstLetter=name.substring(0, 1);
		boolean isNumeric=true;
		try{
			Integer.parseInt(firstLetter);
		} catch(NumberFormatException nfe){
			isNumeric=false;
		}
		if(isNumeric){name="node"+name;}
		return(name);
	}
}
