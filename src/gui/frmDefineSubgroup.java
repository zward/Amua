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

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import base.AmuaModel;
import main.ScaledIcon;
import main.StyledTextPane;
import math.Interpreter;
import math.Numeric;

/**
 *
 */
public class frmDefineSubgroup {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineSubgroup;
	AmuaModel myModel;
	private JTextField textName;
	int subgroupNum;
	ArrayList<String> names, defs;
	StyledTextPane paneExpression;
	frmProperties form;
	
	public frmDefineSubgroup(AmuaModel myModel, int subgroupNum, frmProperties form){
		this.myModel=myModel;
		this.subgroupNum=subgroupNum;
		this.form=form;
		this.names=form.subgroupNames;
		this.defs=form.subgroupDefs;
		initialize();
		if(subgroupNum!=-1){getSubgroup();}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineSubgroup = new JDialog();
			frmDefineSubgroup.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineSubgroup.setTitle("Amua - Define Subgroup");
			frmDefineSubgroup.setResizable(false);
			frmDefineSubgroup.setBounds(100, 100, 415, 218);
			frmDefineSubgroup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineSubgroup.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					String testName=textName.getText();
					
					//Check if name is unique
					int index=names.indexOf(testName);
					if(index!=-1){ //in list
						if(subgroupNum==-1){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineSubgroup, testName+" is already defined as a subgroup!");
						}
						else if(subgroupNum!=-1 && index!=subgroupNum){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineSubgroup, testName+" is already defined as a subgroup!");
						}
					}
					
					if(proceed==true){
						//Evaluate expression
						String testExp=paneExpression.getText();
						proceed=evaluate();
					
						if(proceed==true){
							if(subgroupNum==-1){ //add new
								names.add(testName);
								defs.add(testExp);
							}
							else{ //edit
								names.set(subgroupNum, testName);
								defs.set(subgroupNum, testExp);
							}
							form.refreshSubgroupTable();
							
							frmDefineSubgroup.dispose();
						}
					}
				}
			});
			btnSave.setBounds(109, 142, 90, 28);
			frmDefineSubgroup.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineSubgroup.dispose();
				}
			});
			btnCancel.setBounds(208, 142, 90, 28);
			frmDefineSubgroup.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Name:");
			lblName.setBounds(6, 13, 44, 16);
			frmDefineSubgroup.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(55, 7, 296, 28);
			frmDefineSubgroup.getContentPane().add(textName);
			textName.setColumns(10);
			
			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(35, 43, 71, 16);
			frmDefineSubgroup.getContentPane().add(lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(1, 61, 400, 79);
			frmDefineSubgroup.getContentPane().add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);
			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluate();
					}
				}
			});
			
			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			toolBar.setBounds(1, 38, 48, 24);
			frmDefineSubgroup.getContentPane().add(toolBar);
			
			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,false);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText("Build Expression");
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ScaledIcon("/images/formula",24,24,24,true));
			toolBar.add(btnFx);

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private boolean evaluate(){
		boolean valid=true;
		String testExp=paneExpression.getText();
		Numeric testVal=null;
		
		try{
			testVal=Interpreter.evaluate(testExp, myModel,false);
		}catch(Exception e1){
			JOptionPane.showMessageDialog(frmDefineSubgroup, e1.toString());
			valid=false;
		}

		if(testVal==null || testVal.isBoolean()==false){
			JOptionPane.showMessageDialog(frmDefineSubgroup, "Not a boolean expression: "+testExp);
			valid=false;
		}

		return(valid);
	}

	
	private void getSubgroup(){
		textName.setText(names.get(subgroupNum));
		paneExpression.setText(defs.get(subgroupNum));
		paneExpression.restyle();
	}
}
