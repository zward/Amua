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
import main.ScaledIcon;
import main.StyledTextPane;
import math.Interpreter;
import math.Numeric;
import java.awt.Toolkit;
import javax.swing.JToolBar;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
			frmDefineConstraint.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineConstraint.class.getResource("/images/constraint_128.png")));
			frmDefineConstraint.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineConstraint.setTitle("Amua - "+myModel.language.base.getString("title.define_constraint")); //Define Constraint
			frmDefineConstraint.setFont(myModel.language.font);
			frmDefineConstraint.setResizable(false);
			frmDefineConstraint.setBounds(100, 100, 650, 350);
			frmDefineConstraint.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineConstraint.getContentPane().setLayout(null);
			
			JPanel panel = new JPanel();
			panel.setBounds(6, 6, 632, 306);
			frmDefineConstraint.getContentPane().add(panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{30, 19, 0, 242, 76, 92, 0};
			gbl_panel.rowHeights = new int[]{0, 29, 69, 0, 70, 0, 0, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);

			JLabel lblName = new JLabel(myModel.language.base.getString("object.name")+":");
			lblName.setFont(myModel.language.font);
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.gridwidth = 2;
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.insets = new Insets(0, 0, 5, 5);
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			panel.add(lblName, gbc_lblName);

			textName = new JTextField();
			textName.setFont(myModel.language.font);
			GridBagConstraints gbc_textName = new GridBagConstraints();
			gbc_textName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textName.gridwidth = 2;
			gbc_textName.insets = new Insets(0, 0, 5, 5);
			gbc_textName.gridx = 2;
			gbc_textName.gridy = 0;
			panel.add(textName, gbc_textName);
			textName.setColumns(10);

			JToolBar toolBar = new JToolBar();
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.SOUTHWEST;
			//gbc_toolBar.insets = new Insets(0, 0, 5, 5);
			gbc_toolBar.insets = new Insets(0, 0, 0, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 1;
			panel.add(toolBar, gbc_toolBar);
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);

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
			gbc_lblExpression.gridwidth = 2;
			gbc_lblExpression.insets = new Insets(0, 0, 5, 5);
			gbc_lblExpression.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblExpression.gridx = 1;
			gbc_lblExpression.gridy = 1;
			panel.add(lblExpression, gbc_lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 5;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			panel.add(scrollPane, gbc_scrollPane);

			paneExpression=new StyledTextPane(myModel, myModel.language);
			//paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			paneExpression.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			scrollPane.setViewportView(paneExpression);

			JButton btnEvaluate = new JButton(myModel.language.base.getString("button.evaluate")); //Evaluate
			btnEvaluate.setFont(myModel.language.font);
			GridBagConstraints gbc_btnEvaluate = new GridBagConstraints();
			gbc_btnEvaluate.insets = new Insets(0, 0, 5, 0);
			gbc_btnEvaluate.gridx = 5;
			gbc_btnEvaluate.gridy = 2;
			panel.add(btnEvaluate, gbc_btnEvaluate);

			JLabel lblValue = new JLabel(myModel.language.math.getString("sum.expected_value")+":");
			lblValue.setFont(myModel.language.font);
			GridBagConstraints gbc_lblValue = new GridBagConstraints();
			gbc_lblValue.gridwidth = 3;
			gbc_lblValue.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblValue.insets = new Insets(0, 0, 5, 5);
			gbc_lblValue.gridx = 0;
			gbc_lblValue.gridy = 3;
			panel.add(lblValue, gbc_lblValue);

			JScrollPane scrollPaneValue = new JScrollPane();
			GridBagConstraints gbc_scrollPaneValue = new GridBagConstraints();
			gbc_scrollPaneValue.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneValue.gridwidth = 4;
			gbc_scrollPaneValue.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPaneValue.gridx = 0;
			gbc_scrollPaneValue.gridy = 4;
			panel.add(scrollPaneValue, gbc_scrollPaneValue);

			paneValue = new JTextPane();
			paneValue.setFont(myModel.language.font);
			StyleConstants.setFontFamily(paneValue.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE), myModel.language.font.getFamily());
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);

			JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.setFont(myModel.language.font);
			GridBagConstraints gbc_btnSave = new GridBagConstraints();
			gbc_btnSave.insets = new Insets(0, 0, 5, 5);
			gbc_btnSave.gridx = 4;
			gbc_btnSave.gridy = 4;
			panel.add(btnSave, gbc_btnSave);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.setFont(myModel.language.font);
			GridBagConstraints gbc_btnCancel = new GridBagConstraints();
			gbc_btnCancel.insets = new Insets(0, 0, 5, 0);
			gbc_btnCancel.gridx = 5;
			gbc_btnCancel.gridy = 4;
			panel.add(btnCancel, gbc_btnCancel);

			JLabel lblNotes = new JLabel(myModel.language.base.getString("menu.notes")+":");
			lblNotes.setFont(myModel.language.font);
			GridBagConstraints gbc_lblNotes = new GridBagConstraints();
			gbc_lblNotes.gridwidth = 3;
			gbc_lblNotes.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
			gbc_lblNotes.gridx = 0;
			gbc_lblNotes.gridy = 5;
			panel.add(lblNotes, gbc_lblNotes);

			JScrollPane scrollPane_1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.gridwidth = 6;
			gbc_scrollPane_1.gridx = 0;
			gbc_scrollPane_1.gridy = 6;
			panel.add(scrollPane_1, gbc_scrollPane_1);

			textNotes = new JTextArea();
			textNotes.setFont(myModel.language.font);
			scrollPane_1.setViewportView(textNotes);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineConstraint.dispose();
				}
			});
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
								myModel.saveSnapshot(myModel.language.base.getString("button.add_constraint")); //Add to undo stack (Add Constraint)
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

								if(changed){myModel.saveSnapshot(myModel.language.base.getString("button.edit_constraint"));} //Add to undo stack (Edit Constraint)
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
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});

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
				testVal=Interpreter.evaluate(allExp[i], myModel,false,myModel.language);
			}catch(Exception e1){
				update=false;
				paneValue.setText(e1.toString());
				//JOptionPane.showMessageDialog(frmDefineConstraint, "Invalid expression: "+allExp[i]);
				valid=false;
			}

			if(testVal.isBoolean()==false){
				update=false;
				paneValue.setText(myModel.language.message.getString("err.not_boolean")+": "+allExp[i]); //Error: Not a boolean expression
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
