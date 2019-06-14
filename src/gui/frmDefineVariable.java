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

import java.awt.Font;
import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import base.AmuaModel;
import main.Parameter;
import main.StyledTextPane;
import main.Variable;
import math.Interpreter;
import math.Numeric;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Toolkit;

/**
 *
 */
public class frmDefineVariable {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineVariable;
	AmuaModel myModel;
	private JTextField textName;
	private JTextPane paneValue;
	JTextArea textNotes;
	Variable variable;
	int varNum;
	StyledTextPane paneExpression;
	
	public frmDefineVariable(AmuaModel myModel, int varNum){
		this.myModel=myModel;
		this.varNum=varNum;
		initialize();
		if(varNum==-1){
			variable=new Variable();
			paneExpression.setText("0"); //initialize to 0
		}
		else{getVar();}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineVariable = new JDialog();
			frmDefineVariable.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineVariable.class.getResource("/images/variable.png")));
			frmDefineVariable.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineVariable.setTitle("Amua - Define Variable");
			frmDefineVariable.setResizable(false);
			frmDefineVariable.setBounds(100, 100, 650, 350);
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
					else if(Interpreter.isReservedString(testName)){
						JOptionPane.showMessageDialog(frmDefineVariable, testName+" is a reserved variable name!");
						proceed=false;
					}
					else{
						//Ensure name is unique
						int index=myModel.getVariableIndex(testName);
						if(index!=-1 && index!=varNum){
							JOptionPane.showMessageDialog(frmDefineVariable, testName+" is already defined as a variable!");
							proceed=false;
						}
						index=myModel.getParameterIndex(testName);
						if(index!=-1){
							JOptionPane.showMessageDialog(frmDefineVariable, testName+" is already defined as a parameter!");
							proceed=false;
						}
						index=myModel.getTableIndex(testName);
						if(index!=-1){
							JOptionPane.showMessageDialog(frmDefineVariable, testName+" is already defined as a table!");
							proceed=false;
						}
						//Ensure name is valid
						for(int i=0; i<testName.length(); i++){
							if(Interpreter.isBreak(testName.charAt(i))){
								JOptionPane.showMessageDialog(frmDefineVariable, "Invalid character in name: "+testName.charAt(i));
								proceed=false;
							}
						}
						for(int d=0; d<myModel.dimInfo.dimSymbols.length; d++){
							if(testName.equals(myModel.dimInfo.dimSymbols[d])){
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

						Numeric testVal=null;
						try{
							testVal=Interpreter.evaluate(testExp, myModel,false);
						}catch(Exception e1){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineVariable, "Invalid expression!");
						}

						if(proceed==true){
							if(varNum==-1){
								myModel.saveSnapshot("Add Variable"); //Add to undo stack
								variable.name=testName;
								variable.expression=testExp;
								variable.notes=testNotes;
								variable.value[0]=testVal;
								myModel.variables.add(variable);
								myModel.addVariable(variable);
							}
							else{
								boolean changed=false;
								if(!variable.name.matches(testName)){changed=true;}
								if(variable.expression==null){changed=true;}
								else if(!variable.expression.matches(testExp)){changed=true;}
								if(variable.notes!=null && !variable.notes.equals(testNotes)){changed=true;}
								
								if(changed){myModel.saveSnapshot("Edit Variable");} //Add to undo stack
								variable.name=testName;
								variable.expression=testExp;
								variable.notes=testNotes;
								variable.value[0]=testVal;
								myModel.editVariable(varNum);
							}
							myModel.validateModelObjects(); //Update all model objects
							myModel.rescale(myModel.scale); //Re-validates textfields
							
							frmDefineVariable.dispose();
						}
					}
				}
			});
			btnSave.setBounds(436, 153, 90, 28);
			frmDefineVariable.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineVariable.dispose();
				}
			});
			btnCancel.setBounds(535, 153, 90, 28);
			frmDefineVariable.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Name:");
			lblName.setBounds(6, 13, 44, 16);
			frmDefineVariable.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(55, 7, 296, 28);
			frmDefineVariable.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblValue = new JLabel("Expected Value:");
			lblValue.setBounds(6, 118, 150, 16);
			frmDefineVariable.getContentPane().add(lblValue);
			
			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(35, 43, 150, 16);
			frmDefineVariable.getContentPane().add(lblExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(548, 71, 90, 28);
			frmDefineVariable.getContentPane().add(btnEvaluate);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(1, 61, 535, 50);
			frmDefineVariable.getContentPane().add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);
			
			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(6, 210, 55, 16);
			frmDefineVariable.getContentPane().add(lblNotes);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 226, 632, 83);
			frmDefineVariable.getContentPane().add(scrollPane_1);
			
			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

			JScrollPane scrollPaneValue = new JScrollPane();
			scrollPaneValue.setBounds(6, 139, 418, 59);
			frmDefineVariable.getContentPane().add(scrollPaneValue);

			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);

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
			toolBar.setBounds(1, 40, 48, 24);
			frmDefineVariable.getContentPane().add(toolBar);
			
			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,false);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText("Build Expression");
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ImageIcon(frmDefineVariable.class.getResource("/images/formula.png")));
			toolBar.add(btnFx);

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void evaluate(){
		String testExp=paneExpression.getText();
		Numeric testVal=null;
		boolean update=true;
		String varName=textName.getText();
		if(varName!=null){
			if(isCircular(testExp,varName)){
				update=false;
				JOptionPane.showMessageDialog(frmDefineVariable, "Circular expression!");
			}
		}

		try{
			testVal=Interpreter.evaluate(testExp, myModel,false);
		}catch(Exception e1){
			update=false;
			paneValue.setText(e1.toString());
			//JOptionPane.showMessageDialog(frmDefineVariable, "Invalid expression!");
		}
		if(update){paneValue.setText(testVal.toString()+"");}
	}

	private boolean isCircular(String text, String varName){
		boolean circular=false;
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			int varIndex=myModel.getVariableIndex(word);
			if(varIndex!=-1){ //Check nested variable
				Variable var=myModel.variables.get(varIndex);
				String checkName=var.name;
				if(checkName.matches(varName)){circular=true;}
				else{
					boolean checkCirc=isCircular(var.expression,varName);
					if(checkCirc==true){circular=true;}
				}
			}
			int paramIndex=myModel.getParameterIndex(word);
			if(paramIndex!=-1){ //Check nested parameter
				Parameter param=myModel.parameters.get(paramIndex);
				String checkName=param.name;
				if(checkName.matches(varName)){circular=true;}
				else{
					boolean checkCirc=isCircular(param.expression,varName);
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
		variable=myModel.variables.get(varNum);
		textName.setText(variable.name);
		paneExpression.setText(variable.expression);
		paneExpression.restyle();
		//textValue.setText(variable.EV.toString()+"");
		textNotes.setText(variable.notes);
	}
}
