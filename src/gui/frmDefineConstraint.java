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
import main.Constraint;
import main.StyledTextPane;
import math.Interpreter;
import math.Numeric;
import java.awt.Toolkit;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;

/**
 *
 */
public class frmDefineConstraint {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineConstraint;
	AmuaModel myModel;
	private JTextField textName;
	private JTextPane paneValue;
	JTextArea textNotes;
	Constraint constraint;
	int constNum;
	StyledTextPane paneExpression;
	
	public frmDefineConstraint(AmuaModel myModel, int constNum){
		this.myModel=myModel;
		this.constNum=constNum;
		initialize();
		if(constNum==-1){constraint=new Constraint();}
		else{getConst();}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineConstraint = new JDialog();
			frmDefineConstraint.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineConstraint.class.getResource("/images/constraint.png")));
			frmDefineConstraint.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineConstraint.setTitle("Amua - Define Constraint");
			frmDefineConstraint.setResizable(false);
			frmDefineConstraint.setBounds(100, 100, 650, 350);
			frmDefineConstraint.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineConstraint.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					String testName=textName.getText();
					String testNotes=textNotes.getText();

					if(proceed==true){
						//Evaluate expression
						String testExp=paneExpression.getText();
						proceed=evaluate();
					
						if(proceed==true){
							if(constNum==-1){
								myModel.saveSnapshot("Add Constraint"); //Add to undo stack
								constraint.name=testName;
								constraint.expression=testExp;
								constraint.notes=testNotes;
								//constraint.value=testVal;
								myModel.constraints.add(constraint);
								myModel.addConstraint(constraint);
							}
							else{
								boolean changed=false;
								if(!constraint.name.matches(testName)){changed=true;}
								if(!constraint.expression.matches(testExp)){changed=true;}
								if(constraint.notes!=null && !constraint.notes.equals(testNotes)){changed=true;}
								
								if(changed){myModel.saveSnapshot("Edit Constraint");} //Add to undo stack
								constraint.name=testName;
								constraint.expression=testExp;
								constraint.notes=testNotes;
								//constraint.value=testVal;
								myModel.editConstraint(constNum);
							}
							constraint.valid=true;
							//myModel.validateParamsVars(); //Update all parameters/variables
							//myModel.rescale(myModel.scale); //Re-validates textfields
							
							frmDefineConstraint.dispose();
						}
					}
				}
			});
			btnSave.setBounds(436, 153, 90, 28);
			frmDefineConstraint.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineConstraint.dispose();
				}
			});
			btnCancel.setBounds(535, 153, 90, 28);
			frmDefineConstraint.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Name:");
			lblName.setBounds(6, 13, 44, 16);
			frmDefineConstraint.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(55, 7, 296, 28);
			frmDefineConstraint.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblValue = new JLabel("Expected Value:");
			lblValue.setBounds(6, 118, 101, 16);
			frmDefineConstraint.getContentPane().add(lblValue);
			
			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(35, 43, 71, 16);
			frmDefineConstraint.getContentPane().add(lblExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(548, 71, 90, 28);
			frmDefineConstraint.getContentPane().add(btnEvaluate);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(1, 61, 535, 50);
			frmDefineConstraint.getContentPane().add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);
			
			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(6, 210, 55, 16);
			frmDefineConstraint.getContentPane().add(lblNotes);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 226, 632, 83);
			frmDefineConstraint.getContentPane().add(scrollPane_1);
			
			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);
			
			JScrollPane scrollPaneValue = new JScrollPane();
			scrollPaneValue.setBounds(6, 139, 418, 59);
			frmDefineConstraint.getContentPane().add(scrollPaneValue);
			
			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);
			
			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			toolBar.setBounds(1, 40, 48, 24);
			frmDefineConstraint.getContentPane().add(toolBar);
			
			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,false);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText("Build Expression");
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ImageIcon(frmDefineConstraint.class.getResource("/images/formula.png")));
			toolBar.add(btnFx);
			
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

	private boolean evaluate(){
		boolean valid=true;
		String testExp=paneExpression.getText();
		paneValue.setText("");
		String allExp[]=testExp.split(";");
		int numExp=allExp.length;
		for(int i=0; i<numExp; i++){
			Numeric testVal=null;
			boolean update=true;

			try{
				testVal=Interpreter.evaluate(allExp[i], myModel,false);
			}catch(Exception e1){
				update=false;
				paneValue.setText(e1.toString());
				//JOptionPane.showMessageDialog(frmDefineConstraint, "Invalid expression: "+allExp[i]);
				valid=false;
			}

			if(testVal.isBoolean()==false){
				update=false;
				paneValue.setText("Error: Not a boolean expression: "+allExp[i]);
				//JOptionPane.showMessageDialog(frmDefineConstraint, "Not a boolean expression: "+allExp[i]);
				valid=false;
			}

			if(update){paneValue.setText(paneValue.getText()+testVal.toString()+"\n");}
		}
		return(valid);
	}

	
	private void getConst(){
		constraint=myModel.constraints.get(constNum);
		textName.setText(constraint.name);
		paneExpression.setText(constraint.expression);
		paneExpression.restyle();
		textNotes.setText(constraint.notes);
	}
}
