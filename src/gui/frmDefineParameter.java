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
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import base.AmuaModel;
import main.Parameter;
import main.ScaledIcon;
import main.StyledTextPane;
import main.Variable;
import math.Interpreter;
import math.Numeric;
import java.awt.Toolkit;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
			frmDefineParameter.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineParameter.class.getResource("/images/parameter_128.png")));
			frmDefineParameter.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineParameter.setTitle("Amua - "+myModel.language.base.getString("title.define_parameter")); //Define Parameter
			frmDefineParameter.setResizable(false);
			frmDefineParameter.setBounds(100, 100, 650, 350);
			frmDefineParameter.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{30, 19, 0, 242, 76, 92, 0};
			gridBagLayout.rowHeights = new int[]{0, 19, 69, 16, 70, 16, 83, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			frmDefineParameter.getContentPane().setLayout(gridBagLayout);

			JLabel lblName = new JLabel(myModel.language.base.getString("object.name")+":");
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.gridwidth = 2;
			gbc_lblName.insets = new Insets(5, 5, 5, 5);
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			frmDefineParameter.getContentPane().add(lblName, gbc_lblName);

			textName = new JTextField();
			GridBagConstraints gbc_textName = new GridBagConstraints();
			gbc_textName.gridwidth = 2;
			gbc_textName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textName.insets = new Insets(5, 0, 5, 5);
			gbc_textName.gridx = 2;
			gbc_textName.gridy = 0;
			frmDefineParameter.getContentPane().add(textName, gbc_textName);
			textName.setColumns(10);

			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.SOUTHWEST;
			gbc_toolBar.insets = new Insets(0, 0, 0, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 1;
			frmDefineParameter.getContentPane().add(toolBar, gbc_toolBar);

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
			GridBagConstraints gbc_lblExpression = new GridBagConstraints();
			gbc_lblExpression.gridwidth = 2;
			gbc_lblExpression.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblExpression.insets = new Insets(0, 0, 5, 5);
			gbc_lblExpression.gridx = 1;
			gbc_lblExpression.gridy = 1;
			frmDefineParameter.getContentPane().add(lblExpression, gbc_lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
			gbc_scrollPane.gridwidth = 5;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			frmDefineParameter.getContentPane().add(scrollPane, gbc_scrollPane);

			paneExpression=new StyledTextPane(myModel, myModel.language);
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

			JButton btnEvaluate = new JButton(myModel.language.base.getString("button.evaluate")); //Evaluate
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			GridBagConstraints gbc_btnEvaluate = new GridBagConstraints();
			gbc_btnEvaluate.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnEvaluate.insets = new Insets(0, 0, 5, 5);
			gbc_btnEvaluate.gridx = 5;
			gbc_btnEvaluate.gridy = 2;
			frmDefineParameter.getContentPane().add(btnEvaluate, gbc_btnEvaluate);

			JLabel lblValue = new JLabel(myModel.language.math.getString("sum.expected_value")+":");
			GridBagConstraints gbc_lblValue = new GridBagConstraints();
			gbc_lblValue.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblValue.insets = new Insets(0, 5, 0, 5);
			gbc_lblValue.gridwidth = 3;
			gbc_lblValue.gridx = 0;
			gbc_lblValue.gridy = 3;
			frmDefineParameter.getContentPane().add(lblValue, gbc_lblValue);

			JScrollPane scrollPaneValue = new JScrollPane();
			GridBagConstraints gbc_scrollPaneValue = new GridBagConstraints();
			gbc_scrollPaneValue.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneValue.insets = new Insets(5, 5, 5, 5);
			gbc_scrollPaneValue.gridwidth = 4;
			gbc_scrollPaneValue.gridx = 0;
			gbc_scrollPaneValue.gridy = 4;
			frmDefineParameter.getContentPane().add(scrollPaneValue, gbc_scrollPaneValue);

			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineParameter.dispose();
				}
			});

			JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					//Ensure name is valid and unique
					String testName=textName.getText();
					String testNotes=textNotes.getText();
					if(testName.length()==0){
						JOptionPane.showMessageDialog(frmDefineParameter, myModel.language.message.getString("err.please_enter_name"));  //Please enter a name!
						proceed=false;
					}
					else if(Interpreter.isReservedString(testName)){
						//[name] is a reserved variable name!
						String msg=MessageFormat.format(myModel.language.message.getString("err.reserved_name"), testName);
						JOptionPane.showMessageDialog(frmDefineParameter, msg);
						proceed=false;
					}
					else{
						//Ensure name is unique
						int index=myModel.getParameterIndex(testName);
						if(index!=-1 && index!=paramNum){
							//[name] is already defined as a parameter!
							String msg=MessageFormat.format(myModel.language.message.getString("err.defined_parameter"), testName);
							JOptionPane.showMessageDialog(frmDefineParameter, msg);
							proceed=false;
						}
						index=myModel.getVariableIndex(testName);
						if(index!=-1){
							//[name] is already defined as a variable!
							String msg=MessageFormat.format(myModel.language.message.getString("err.defined_variable"), testName);
							JOptionPane.showMessageDialog(frmDefineParameter, msg);
							proceed=false;
						}
						index=myModel.getTableIndex(testName);
						if(index!=-1){
							//[name] is already defined as a table!
							String msg=MessageFormat.format(myModel.language.message.getString("err.defined_table"), testName);
							JOptionPane.showMessageDialog(frmDefineParameter, msg);
							proceed=false;
						}
						//Ensure name is valid
						for(int i=0; i<testName.length(); i++){
							if(Interpreter.isBreak(testName.charAt(i))){
								//Invalid character in name
								JOptionPane.showMessageDialog(frmDefineParameter, myModel.language.message.getString("err.invalid_character")+": "+testName.charAt(i));
								proceed=false;
							}
						}
						for(int d=0; d<myModel.dimInfo.dimSymbols.length; d++){
							if(testName.equals(myModel.dimInfo.dimSymbols[d])){
								//[name] is a dimension label!
								String msg=MessageFormat.format(myModel.language.message.getString("err.dim_label"), testName);
								JOptionPane.showMessageDialog(frmDefineParameter, msg);
								proceed=false;
							}
						}
					}

					if(proceed==true){
						//Evaluate expression
						String testExp=paneExpression.getText();
						if(isCircular(testExp,testName)){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineParameter, myModel.language.message.getString("err.circular_expression")); //Circular expression!
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
							testVal=Interpreter.evaluate(testExp, myModel,false,myModel.language);
						}catch(Exception e1){
							proceed=false;
							JOptionPane.showMessageDialog(frmDefineParameter, myModel.language.message.getString("err.invalid_expression")); //Invalid expression!
						}

						if(proceed==true){
							if(paramNum==-1){
								myModel.saveSnapshot(myModel.language.base.getString("button.add_parameter")); //Add to undo stack (Add Parameter)
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
								if(parameter.expression==null){changed=true;}
								else if(!parameter.expression.matches(testExp)){changed=true;}
								if(parameter.notes!=null && !parameter.notes.equals(testNotes)){changed=true;}

								if(changed){myModel.saveSnapshot(myModel.language.base.getString("button.edit_parameter"));} //Add to undo stack (Edit Parameter)
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
			GridBagConstraints gbc_btnSave = new GridBagConstraints();
			gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnSave.insets = new Insets(0, 0, 5, 5);
			gbc_btnSave.gridx = 4;
			gbc_btnSave.gridy = 4;
			frmDefineParameter.getContentPane().add(btnSave, gbc_btnSave);
			GridBagConstraints gbc_btnCancel = new GridBagConstraints();
			gbc_btnCancel.anchor = GridBagConstraints.WEST;
			gbc_btnCancel.insets = new Insets(0, 0, 5, 0);
			gbc_btnCancel.gridx = 5;
			gbc_btnCancel.gridy = 4;
			frmDefineParameter.getContentPane().add(btnCancel, gbc_btnCancel);

			JLabel lblNotes = new JLabel(myModel.language.base.getString("menu.notes")+":");
			GridBagConstraints gbc_lblNotes = new GridBagConstraints();
			gbc_lblNotes.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblNotes.insets = new Insets(0, 5, 0, 0);
			gbc_lblNotes.gridwidth = 3;
			gbc_lblNotes.gridx = 0;
			gbc_lblNotes.gridy = 5;
			frmDefineParameter.getContentPane().add(lblNotes, gbc_lblNotes);

			JScrollPane scrollPane_1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.insets = new Insets(5, 5, 5, 5);
			gbc_scrollPane_1.gridwidth = 6;
			gbc_scrollPane_1.gridx = 0;
			gbc_scrollPane_1.gridy = 6;
			frmDefineParameter.getContentPane().add(scrollPane_1, gbc_scrollPane_1);

			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

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
				JOptionPane.showMessageDialog(frmDefineParameter, myModel.language.message.getString("err.circular_expression")); //Circular expression!
			}
		}

		try{
			testVal=Interpreter.evaluate(testExp, myModel,false,myModel.language);
		}catch(Exception e1){
			update=false;
			//JOptionPane.showMessageDialog(frmDefineParameter, "Invalid expression!");
			paneValue.setText(e1.toString());
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
			if(word.equals("t")){return(myModel.language.message.getString("err.params_vary_time")+" (t)");} //Parameters cannot vary over time!
			else if(word.equals("trace")){return(myModel.language.message.getString("err.params_depend_trace")+" (trace)");} //Parameters cannot depend on the trace!
			else if(myModel.isVariable(word)){return(myModel.language.message.getString("err.params_depend_vars")+" ("+word+")");} //Parameters cannot depend on variables!

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
