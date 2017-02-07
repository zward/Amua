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

import export.*;
import filters.CppFilter;
import filters.JavaFilter;
import filters.PyFilter;
import filters.RFilter;
import filters.XMLFilter;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

/**
 *
 */
public class frmExport {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmExport;
	JTextArea textArea;
	PanelTree myPanel;
	JLabel lblTargetLogo;
	
	/**
	 *  Default Constructor
	 */
	public frmExport(PanelTree myPanel) {
		initialize();
		this.myPanel=myPanel;
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
			descriptions[0]="C++ source file";
			descriptions[1]="Creates an XML Spreadsheet that is compatible with Excel.  The model is converted to matrix form.";
			descriptions[2]="Java source file";
			descriptions[3]="Python 3.0 source file";
			descriptions[4]="R source file";
			
			final String logos[]=new String[5];
			logos[0]="/images/cppIcon_48.png";
			logos[1]="/images/excelIcon_48.png";
			logos[2]="/images/javaIcon_48.png";
			logos[3]="/images/pythonIcon_48.png";
			logos[4]="/images/rIcon_48.png";
			
			final JList list = new JList();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int selected=list.getSelectedIndex();
					textArea.setText(descriptions[selected]);
					lblTargetLogo.setIcon(new ImageIcon(frmExport.class.getResource(logos[selected])));
				}
			});
			list.setModel(new AbstractListModel() {
				String[] values = new String[] {"C++", "Excel", "Java", "Python", "R"};
				public int getSize() {
					return values.length;
				}
				public Object getElementAt(int index) {
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
						JFileChooser fc=new JFileChooser(myPanel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						if(selected==0){fc.setFileFilter(new CppFilter());}
						else if(selected==1){fc.setFileFilter(new XMLFilter());}
						else if(selected==2){fc.setFileFilter(new JavaFilter());}
						else if(selected==3){fc.setFileFilter(new PyFilter());}
						else if(selected==4){fc.setFileFilter(new RFilter());}
						
						fc.setDialogTitle("Export Model");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmExport);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							
							normalizeNodeNames();
							if(selected==0){new CppTree(path,myPanel);}
							else if(selected==1){new ExcelTree(path, myPanel);}
							else if(selected==2){new JavaTree(path, myPanel);}
							else if(selected==3){new PythonTree(path, myPanel);}
							else if(selected==4){new RTree(path, myPanel);}
							
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
			
			
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void normalizeNodeNames(){
		ArrayList<String> distinctNames= new ArrayList<String>();
		ArrayList<TreeNode> nodes=myPanel.tree.nodes;
		int numNodes=nodes.size();
		for(int i=0; i<numNodes; i++){
			String name=nodes.get(i).name;
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
			name=name.replaceAll("-", "_");  //Remove -
			name=name.replaceAll("\\+", "_");  //Remove +
			name=name.replaceAll("/", "_");  //Remove /
			name=name.replaceAll("\\\\", "_");  //Remove \
			name=name.replaceAll("\\*", "_");  //Remove *
						
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
