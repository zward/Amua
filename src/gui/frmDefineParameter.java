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

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import base.AmuaModel;
import main.Parameter;
import main.StyledTextPane;
import main.Variable;
import math.Interpreter;
import math.Numeric;

/**
 *
 */
public class frmDefineParameter {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineParameter;
	AmuaModel myModel;
	private JTextField textName;
	private JTextPane paneValue;
	JTextArea textNotes;
	Parameter parameter;
	int paramNum;
	StyledTextPane paneExpression;
	
	public frmDefineParameter(AmuaModel myModel, int paramNum){
		this.myModel=myModel;
		this.paramNum=paramNum;
		initialize();
		if(paramNum==-1){parameter=new Parameter();}
		else{getParam();}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineParameter = new JDialog();
			frmDefineParameter.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineParameter.setTitle("Amua - Define Parameter");
			frmDefineParameter.setResizable(false);
			frmDefineParameter.setBounds(100, 100, 650, 350);
			frmDefineParameter.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineParameter.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					//Ensure name is valid and unique
					String testName=textName.getText();
					String testNotes=textNotes.getText();
					if(testName.length()==0){
						JOptionPane.showMessageDialog(frmDefineParameter, "Please enter a name!"); 
						proceed=false;
					}
					else if(Interpreter.isReservedString(testName)){
						JOptionPane.showMessageDialog(frmDefineParameter, testName+" is a reserved variable name!");
						proceed=false;
					}
					else{
						//Ensure name is unique
						int index=myModel.getParameterIndex(testName);
						if(index!=-1 && index!=paramNum){
							JOptionPane.showMessageDialog(frmDefineParameter, testName+" is already defined as a parameter!");
							proceed=false;
						}
						index=myModel.getVariableIndex(testName);
						if(index!=-1){
							JOptionPane.showMessageDialog(frmDefineParameter, testName+" is already defined as a variable!");
							proceed=false;
						}
						index=myModel.getTableIndex(testName);
						if(index!=-1){
							JOptionPane.showMessageDialog(frmDefineParameter, testName+" is already defined as a table!");
							proceed=false;
						}
						//Ensure name is valid
						for(int i=0; i<testName.length(); i++){
							if(Interpreter.isBreak(testName.charAt(i))){
								JOptionPane.showMessageDialog(frmDefineParameter, "Invalid character in name: "+testName.charAt(i));
								proceed=false;
							}
						}
						for(int d=0; d<myModel.dimInfo.dimSymbols.length; d++){
							if(testName.equals(myModel.dimInfo.dimSymbols[d])){
								JOptionPane.showMessageDialog(frmDefineParameter, testName+ " is a dimension label!");
								proceed=false;
							}
						}
					}

					if(proceed==true){
						//Evaluate expression
						String testExp=paneExpression.getText();
						if(isCircular(testExp,testName)){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineParameter, "Circular expression!");
						}
						else{
							String hasTime=checkTime(testExp);
							if(hasTime!=null){
								proceed=false;
								JOptionPane.showMessageDialog(frmDefineParameter, hasTime);
							}
						}
						
						Numeric testVal=null;
						try{
							testVal=Interpreter.evaluate(testExp, myModel,false);
						}catch(Exception e1){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineParameter, "Invalid expression!");
						}

						if(proceed==true){
							if(paramNum==-1){
								myModel.saveSnapshot("Add Parameter"); //Add to undo stack
								parameter.name=testName;
								parameter.expression=testExp;
								parameter.notes=testNotes;
								parameter.value=testVal;
								myModel.parameters.add(parameter);
								myModel.addParameter(parameter);
							}
							else{
								boolean changed=false;
								if(!parameter.name.matches(testName)){changed=true;}
								if(!parameter.expression.matches(testExp)){changed=true;}
								if(parameter.notes!=null && !parameter.notes.equals(testNotes)){changed=true;}
								
								if(changed){myModel.saveSnapshot("Edit Parameter");} //Add to undo stack
								parameter.name=testName;
								parameter.expression=testExp;
								parameter.notes=testNotes;
								parameter.value=testVal;
								myModel.editParameter(paramNum);
							}
							myModel.validateModelObjects(); //Update all model objects
							myModel.rescale(myModel.scale); //Re-validates textfields
							
							frmDefineParameter.dispose();
						}
					}
				}
			});
			btnSave.setBounds(436, 153, 90, 28);
			frmDefineParameter.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineParameter.dispose();
				}
			});
			btnCancel.setBounds(535, 153, 90, 28);
			frmDefineParameter.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Name:");
			lblName.setBounds(6, 13, 44, 16);
			frmDefineParameter.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(55, 7, 296, 28);
			frmDefineParameter.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblValue = new JLabel("Expected Value:");
			lblValue.setBounds(6, 118, 101, 16);
			frmDefineParameter.getContentPane().add(lblValue);
			
			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(6, 43, 71, 16);
			frmDefineParameter.getContentPane().add(lblExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(548, 71, 90, 28);
			frmDefineParameter.getContentPane().add(btnEvaluate);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(1, 61, 535, 50);
			frmDefineParameter.getContentPane().add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);
			
			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(6, 210, 55, 16);
			frmDefineParameter.getContentPane().add(lblNotes);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 226, 632, 83);
			frmDefineParameter.getContentPane().add(scrollPane_1);
			
			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);
			
			JScrollPane scrollPaneValue = new JScrollPane();
			scrollPaneValue.setBounds(6, 139, 418, 59);
			frmDefineParameter.getContentPane().add(scrollPaneValue);
			
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
				JOptionPane.showMessageDialog(frmDefineParameter, "Circular expression!");
			}
		}

		try{
			testVal=Interpreter.evaluate(testExp, myModel,false);
		}catch(Exception e1){
			update=false;
			JOptionPane.showMessageDialog(frmDefineParameter, "Invalid expression!");
		}
		if(update){paneValue.setText(testVal.toString()+"");}
	}

	private boolean isCircular(String text, String name){
		boolean circular=false;
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			int paramIndex=myModel.getParameterIndex(word);
			if(paramIndex!=-1){ //Check nested variable
				Parameter param=myModel.parameters.get(paramIndex);
				String checkName=param.name;
				if(checkName.matches(name)){circular=true;}
				else{
					boolean checkCirc=isCircular(param.expression,name);
					if(checkCirc==true){circular=true;}
				}
			}
			int varIndex=myModel.getVariableIndex(word);
			if(varIndex!=-1){ //Check nested variable
				Variable var=myModel.variables.get(varIndex);
				String checkName=var.name;
				if(checkName.matches(name)){circular=true;}
				else{
					boolean checkCirc=isCircular(var.expression,name);
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
	
	/**
	 * Checks expression to ensure it does not depend on t, trace, or a variable
	 * @param text Expression to check
	 * @return null: No dependencies; String: Error message
	 */
	private String checkTime(String text){
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			if(word.equals("t")){return("Parameters cannot vary over time! (t)");}
			else if(word.equals("trace")){return("Parameters cannot depend on the trace! (trace)");}
			else if(myModel.isVariable(word)){return("Parameters cannot depend on variables! ("+word+")");}
		
			if(index==len){len=0;} //End of word
			else{
				text=text.substring(index+1);
				len=text.length();
			}
		}
		return(null);
	}

	private void getParam(){
		parameter=myModel.parameters.get(paramNum);
		textName.setText(parameter.name);
		paneExpression.setText(parameter.expression);
		paneExpression.restyle();
		//textValue.setText(variable.EV.toString()+"");
		textNotes.setText(parameter.notes);
	}
}
