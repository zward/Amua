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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionListener;

import base.AmuaModel;
import document.WriteRMD;
import main.ScaledIcon;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;

/**
 *
 */
public class frmDocument {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmDocument;
	JTextArea textArea;
	AmuaModel myModel;
	JLabel lblTargetLogo;
	JButton btnDocument;
	
	public frmDocument(AmuaModel myModel){
		initialize();
		this.myModel=myModel;
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDocument = new JDialog();
			frmDocument.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDocument.class.getResource("/images/document_128.png")));
			frmDocument.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDocument.setTitle("Amua - Document Model");
			frmDocument.setResizable(false);
			frmDocument.setBounds(100, 100, 500, 300);
			frmDocument.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDocument.getContentPane().setLayout(null);
			
			JLabel lblSelectAnExport = new JLabel("Select a format:");
			lblSelectAnExport.setBounds(12, 23, 146, 16);
			frmDocument.getContentPane().add(lblSelectAnExport);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 42, 188, 145);
			frmDocument.getContentPane().add(scrollPane);
			
			final String descriptions[]=new String[1];
			descriptions[0]="Creates an R Markdown file (.Rmd) that documents the model.  This can in turn be output to different formats (HTML, PDF, Word, etc).";
			
			final String logos[]=new String[1];
			logos[0]="/images/rMarkdownIcon";
			
			final JList<String> list = new JList<String>();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int selected=list.getSelectedIndex();
					btnDocument.setEnabled(true);
					textArea.setText(descriptions[selected]);
					lblTargetLogo.setIcon(new ScaledIcon(logos[selected],48,48,48,true));
				}
			});
			list.setModel(new AbstractListModel<String>() {
				String[] values = new String[] {"R Markdown"};
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
			frmDocument.getContentPane().add(lblDescription);
			
			btnDocument = new JButton("Document");
			btnDocument.setEnabled(false);
			btnDocument.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=list.getSelectedIndex();
					if(selected!=-1){
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						
						fc.setDialogTitle("Select Documentation Folder");
						fc.setApproveButtonText("Document");

						int returnVal = fc.showDialog(frmDocument, "Select");
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String dir=file.getAbsolutePath()+File.separator+myModel.name+"_Doc";
							
							if(selected==0){ //R Markdown
								new WriteRMD(dir,myModel);
							}
							
							JOptionPane.showMessageDialog(frmDocument, "Documented!");
							frmDocument.dispose();
						}

					}
				}
			});
			btnDocument.setBounds(274, 232, 90, 28);
			frmDocument.getContentPane().add(btnDocument);
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDocument.dispose();
				}
			});
			btnCancel.setBounds(381, 232, 90, 28);
			frmDocument.getContentPane().add(btnCancel);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(232, 42, 256, 145);
			frmDocument.getContentPane().add(scrollPane_1);
			
			textArea = new JTextArea();
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			scrollPane_1.setViewportView(textArea);
			
			JLabel lblLogoAmua = new JLabel("New label");
			lblLogoAmua.setIcon(new ScaledIcon("/images/logo",48,48,48,true));
			lblLogoAmua.setBounds(13, 199, 48, 48);
			frmDocument.getContentPane().add(lblLogoAmua);
			
			lblTargetLogo = new JLabel("");
			lblTargetLogo.setBounds(147, 199, 48, 48);
			frmDocument.getContentPane().add(lblTargetLogo);
			
			JLabel lblArrow = new JLabel("New label");
			lblArrow.setIcon(new ScaledIcon("/images/arrow",48,48,48,true));
			lblArrow.setBounds(80, 199, 48, 48);
			frmDocument.getContentPane().add(lblArrow);
			
			JLabel lblModelType = new JLabel("lbl");
			lblModelType.setIcon(new ScaledIcon("/images/document",16,16,16,true));
			lblModelType.setBounds(96, 195, 16, 16);
			frmDocument.getContentPane().add(lblModelType);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	
}
