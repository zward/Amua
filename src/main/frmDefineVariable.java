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
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;

/**
 *
 */
public class frmDefineVariable {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineVariable;
	PanelTree myPanel;
	private JTextField textName;
	private JTextField textValue;
	JTextArea textNotes;
	TreeVariable variable;
	int varNum;
	StyledTextPane paneExpression;
	VarHelper varHelper;
	
	/**
	 *  Default Constructor
	 */
	public frmDefineVariable(PanelTree myPanel,int varNum) {
		this.myPanel=myPanel;
		this.varHelper=myPanel.varHelper;
		this.varNum=varNum;
		initialize();
		if(varNum==-1){variable=new TreeVariable();}
		else{getVar();}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineVariable = new JDialog();
			frmDefineVariable.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineVariable.setTitle("Amua - Define Variable");
			frmDefineVariable.setResizable(false);
			frmDefineVariable.setBounds(100, 100, 450, 258);
			frmDefineVariable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineVariable.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					//Ensure name is valid and unique
					String testName=textName.getText();
					String testNotes=textNotes.getText();
					if(testName.length()==0){
						JOptionPane.showMessageDialog(frmDefineVariable, "Please enter a name!"); 
						proceed=false;
					}
					else if(varHelper.isReserved(testName)){
						JOptionPane.showMessageDialog(frmDefineVariable, testName+" is a reserved variable name!");
						proceed=false;
					}
					else{
						//Ensure name is unique
						int index=varHelper.getVariableIndex(testName);
						if(index!=-1 && index!=varNum){
							JOptionPane.showMessageDialog(frmDefineVariable, testName+" is already defined!");
							proceed=false;
						}
						//Ensure name is valid
						for(int i=0; i<varHelper.breaks.length; i++){
							if(testName.contains(varHelper.breaks[i])){
								JOptionPane.showMessageDialog(frmDefineVariable, "Invalid character in name: "+varHelper.breaks[i]);
								proceed=false;
							}
						}
						for(int d=0; d<myPanel.tree.dimSymbols.length; d++){
							if(testName.equals(myPanel.tree.dimSymbols[d])){
								JOptionPane.showMessageDialog(frmDefineVariable, testName+ " is a dimension label!");
								proceed=false;
							}
						}
					}

					if(proceed==true){
						//Evaluate expression
						String testExp=paneExpression.getText();
						if(isCircular(testExp,testName)){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineVariable, "Circular expression!");
						}

						double testVal=0;
						try{
							testVal=varHelper.evaluateExpression(testExp);
						}catch(Exception e1){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineVariable, "Invalid expression!");
						}

						if(proceed==true){
							if(varNum==-1){
								myPanel.saveSnapshot("Add Variable");//Add to undo stack
								variable.name=testName;
								variable.expression=testExp;
								variable.value=testVal;
								variable.notes=testNotes;
								myPanel.tree.variables.add(variable);
								myPanel.addVariable(variable);
							}
							else{
								boolean changed=false;
								if(!variable.name.matches(testName)){changed=true;}
								if(!variable.expression.matches(testExp)){changed=true;}
								if(variable.notes!=null && !variable.notes.equals(testNotes)){changed=true;}

								if(changed){myPanel.saveSnapshot("Edit Variable");}//Add to undo stack
								variable.name=testName;
								variable.expression=testExp;
								variable.value=testVal;
								variable.notes=testNotes;
								myPanel.editVariable(varNum);
							}
							myPanel.tree.evalAllVars(); //Update all variables
							myPanel.rescale(myPanel.tree.scale); //Re-validates textfields
							frmDefineVariable.dispose();
						}
					}
				}
			});
			btnSave.setBounds(249, 139, 90, 28);
			frmDefineVariable.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineVariable.dispose();
				}
			});
			btnCancel.setBounds(348, 139, 90, 28);
			frmDefineVariable.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Name:");
			lblName.setBounds(6, 13, 44, 16);
			frmDefineVariable.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(55, 7, 296, 28);
			frmDefineVariable.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblValue = new JLabel("Expected Value:");
			lblValue.setBounds(6, 118, 101, 16);
			frmDefineVariable.getContentPane().add(lblValue);

			textValue = new JTextField();
			textValue.setEditable(false);
			textValue.setBounds(96, 112, 131, 28);
			frmDefineVariable.getContentPane().add(textValue);
			textValue.setColumns(10);

			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(6, 43, 71, 16);
			frmDefineVariable.getContentPane().add(lblExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(348, 71, 90, 28);
			frmDefineVariable.getContentPane().add(btnEvaluate);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(1, 61, 346, 50);
			frmDefineVariable.getContentPane().add(scrollPane);

			paneExpression=new StyledTextPane(varHelper);
			scrollPane.setViewportView(paneExpression);
			
			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(6, 151, 55, 16);
			frmDefineVariable.getContentPane().add(lblNotes);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 169, 432, 50);
			frmDefineVariable.getContentPane().add(scrollPane_1);
			
			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);
			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluate();
					}
				}
			});

		} catch (Exception ex){
			ex.printStackTrace();
			myPanel.errorLog.recordError(ex);
		}
	}

	private void evaluate(){
		String testExp=paneExpression.getText();
		double testVal=0;
		boolean update=true;
		String varName=textName.getText();
		if(varName!=null){
			if(isCircular(testExp,varName)){
				update=false;
				JOptionPane.showMessageDialog(frmDefineVariable, "Circular expression!");
			}
		}

		try{
			testVal=varHelper.evaluateExpression(testExp);
		}catch(Exception e1){
			update=false;
			JOptionPane.showMessageDialog(frmDefineVariable, "Invalid expression!");
		}
		if(update){textValue.setText(myPanel.tree.round(testVal)+"");}
	}

	private boolean isCircular(String text, String varName){
		boolean circular=false;
		int len=text.length();
		while(len>0){
			int index=varHelper.getNextIndex(text);
			String word=text.substring(0, index);
			int varIndex=varHelper.getVariableIndex(word);
			if(varIndex!=-1){ //Check nested variable
				TreeVariable var=varHelper.variables.get(varIndex);
				String checkName=var.name;
				if(checkName.matches(varName)){circular=true;}
				else{
					boolean checkCirc=isCircular(var.expression,varName);
					if(checkCirc==true){circular=true;}
				}
			}
			if(index==len){len=0;} //End of word
			else{
				text=text.substring(index+1);
				len=text.length();
			}
		}
		return(circular);
	}

	private void getVar(){
		variable=myPanel.tree.variables.get(varNum);
		textName.setText(variable.name);
		paneExpression.setText(variable.expression);
		paneExpression.restyle();
		textValue.setText(myPanel.tree.round(variable.value)+"");
		textNotes.setText(variable.notes);
	}
}
