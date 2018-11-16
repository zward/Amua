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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import base.AmuaModel;
import filters.TXTFilter;
import math.Interpreter;
import math.Numeric;

/**
 *
 */
public class frmTestExpressions {

	public JFrame frmTestExpressions;
	AmuaModel myModel;
	
	private JTextField textFilepath;
	JTextArea textArea;
	
	public frmTestExpressions(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTestExpressions = new JFrame();
			frmTestExpressions.setResizable(false);
			frmTestExpressions.setTitle("Amua - Test Expressions");
			frmTestExpressions.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmTestExpressions.setBounds(100, 100, 636, 545);
			frmTestExpressions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmTestExpressions.getContentPane().setLayout(null);
			
			JLabel lblExpressionsFile = new JLabel("Expressions File:");
			lblExpressionsFile.setBounds(12, 13, 139, 16);
			frmTestExpressions.getContentPane().add(lblExpressionsFile);
			
			textFilepath = new JTextField();
			textFilepath.setBounds(116, 7, 401, 28);
			frmTestExpressions.getContentPane().add(textFilepath);
			textFilepath.setColumns(10);
			
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						JFileChooser fc=new JFileChooser();
						fc.resetChoosableFileFilters();
						fc.addChoosableFileFilter(new TXTFilter());
						fc.setAcceptAllFileFilterUsed(false);

						fc.setDialogTitle("Select Expression File");
						fc.setApproveButtonText("Select");

						int returnVal = fc.showOpenDialog(frmTestExpressions);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							textFilepath.setText(file.getAbsolutePath());
						}

					}catch(Exception e){
						JOptionPane.showMessageDialog(frmTestExpressions, e.toString());
					}
				}
			});
			btnBrowse.setBounds(528, 7, 90, 28);
			frmTestExpressions.getContentPane().add(btnBrowse);
			
			JButton btnTest = new JButton("Test!");
			btnTest.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try{
						String filepath=textFilepath.getText();
						textArea.append("Testing expression file: "+filepath+"\n");

						//Open file
						FileInputStream fstream = new FileInputStream(filepath);
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String strLine=br.readLine();
						int numExpr=0, numErr=0;
						while(strLine!=null){
							//get expression
							numExpr++;
							String curLine[]=strLine.split(";");
							Numeric curTest=Interpreter.evaluate(curLine[0],null,false);
							Numeric curAnswer=Interpreter.evaluate(curLine[1],null,false);
							
							if(!curTest.isEqual(curAnswer)){ //not equal
								numErr++;
								textArea.append("Error "+numErr+":\n");
								textArea.append("Expression: "+curLine[0]+" evaluates to: "+curTest.toString()+"\n");
								textArea.append("Answer: "+curLine[1]+" evaluates to: "+curAnswer.toString()+"\n");
								textArea.append("\n");
							}
							strLine=br.readLine();
						}
						
						textArea.append("Done!\n");
						textArea.append(numErr+" Failed, "+(numExpr-numErr)+" Passed\n");
						
						
						br.close();

					}catch(Exception e){
						textArea.append(e.toString()+"\n");
					}
				}
			});
			btnTest.setBounds(12, 41, 90, 28);
			frmTestExpressions.getContentPane().add(btnTest);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(16, 73, 602, 431);
			frmTestExpressions.getContentPane().add(scrollPane);
			
			textArea = new JTextArea();
			textArea.setEditable(false);
			scrollPane.setViewportView(textArea);
			
		
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
	
	
}
