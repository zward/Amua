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
import java.text.MessageFormat;
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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
			frmDefineSubgroup.setTitle("Amua - "+myModel.language.base.getString("title.define_subgroup")); //Define Subgroup
			frmDefineSubgroup.setFont(myModel.language.font);
			frmDefineSubgroup.setResizable(false);
			frmDefineSubgroup.setBounds(100, 100, 415, 218);
			frmDefineSubgroup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{24, 51, 118, 193, 0};
			gridBagLayout.rowHeights = new int[]{28, 28, 79, 28, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			frmDefineSubgroup.getContentPane().setLayout(gridBagLayout);

			JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.setFont(myModel.language.font);
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					String testName=textName.getText();

					//Check if name is unique
					int index=names.indexOf(testName);
					if(index!=-1){ //in list
						if(subgroupNum==-1){
							proceed=false;
							//testName+" is already defined as a subgroup!
							String msg = MessageFormat.format(myModel.language.message.getString("err.defined_subgroup"), testName);
							JOptionPane.showMessageDialog(frmDefineSubgroup, msg);
						}
						else if(subgroupNum!=-1 && index!=subgroupNum){
							proceed=false;
							//testName+" is already defined as a subgroup!
							String msg = MessageFormat.format(myModel.language.message.getString("err.defined_subgroup"), testName);
							JOptionPane.showMessageDialog(frmDefineSubgroup, msg);
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

			JLabel lblName = new JLabel(myModel.language.base.getString("object.name")+":");
			lblName.setFont(myModel.language.font);
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.gridwidth = 2;
			gbc_lblName.insets = new Insets(5, 5, 0, 5);
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			frmDefineSubgroup.getContentPane().add(lblName, gbc_lblName);

			textName = new JTextField();
			textName.setFont(myModel.language.font);
			GridBagConstraints gbc_textName = new GridBagConstraints();
			gbc_textName.anchor = GridBagConstraints.NORTH;
			gbc_textName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textName.insets = new Insets(5, 5, 0, 5);
			gbc_textName.gridwidth = 2;
			gbc_textName.gridx = 2;
			gbc_textName.gridy = 0;
			frmDefineSubgroup.getContentPane().add(textName, gbc_textName);
			textName.setColumns(10);

			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.NORTH;
			gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_toolBar.insets = new Insets(0, 0, 0, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 1;
			frmDefineSubgroup.getContentPane().add(toolBar, gbc_toolBar);

			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,false);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText(myModel.language.base.getString("button.build_expression")); //Build Expression
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ScaledIcon("/images/formula",24,24,24,true));
			toolBar.add(btnFx);

			JLabel lblExpression = new JLabel(myModel.language.base.getString("object.expression")+":");
			lblExpression.setFont(myModel.language.font);
			GridBagConstraints gbc_lblExpression = new GridBagConstraints();
			gbc_lblExpression.gridwidth = 3;
			gbc_lblExpression.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblExpression.insets = new Insets(0, 0, 5, 5);
			gbc_lblExpression.gridx = 1;
			gbc_lblExpression.gridy = 1;
			frmDefineSubgroup.getContentPane().add(lblExpression, gbc_lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 5, 5, 0);
			gbc_scrollPane.gridwidth = 4;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			frmDefineSubgroup.getContentPane().add(scrollPane, gbc_scrollPane);

			paneExpression=new StyledTextPane(myModel, myModel.language);
			//paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			paneExpression.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			scrollPane.setViewportView(paneExpression);
			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluate();
					}
				}
			});
			GridBagConstraints gbc_btnSave = new GridBagConstraints();
			gbc_btnSave.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnSave.insets = new Insets(0, 0, 0, 5);
			gbc_btnSave.gridx = 2;
			gbc_btnSave.gridy = 3;
			frmDefineSubgroup.getContentPane().add(btnSave, gbc_btnSave);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.setFont(myModel.language.font);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineSubgroup.dispose();
				}
			});
			GridBagConstraints gbc_btnCancel = new GridBagConstraints();
			gbc_btnCancel.anchor = GridBagConstraints.NORTHWEST;
			gbc_btnCancel.gridx = 3;
			gbc_btnCancel.gridy = 3;
			frmDefineSubgroup.getContentPane().add(btnCancel, gbc_btnCancel);

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
			testVal=Interpreter.evaluate(testExp, myModel,false,myModel.language);
		}catch(Exception e1){
			JOptionPane.showMessageDialog(frmDefineSubgroup, e1.toString());
			valid=false;
		}

		if(testVal==null || testVal.isBoolean()==false){
			JOptionPane.showMessageDialog(frmDefineSubgroup, myModel.language.message.getString("err.not_boolean")+": "+testExp); //Not a boolean expression
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
