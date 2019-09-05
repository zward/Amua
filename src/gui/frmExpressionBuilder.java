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
import javax.swing.event.ListSelectionListener;

import base.AmuaModel;
import main.StyledTextPane;
import main.Table;
import math.Constants;
import math.Distributions;
import math.Functions;
import math.Interpreter;
import math.MathUtils;
import math.MatrixFunctions;
import math.Numeric;
import math.Operators;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.DefaultListModel;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 */
public class frmExpressionBuilder {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmExpressionBuilder;
	
	StyledTextPane textPaneExpression, targetPane;
	JList<String> listType;
	JList<String> listValue;
	DefaultListModel<String> modelValue;
	JTextPane textPaneDescription, textPaneValue;
	AmuaModel myModel;
	boolean updateOperation;
	
	boolean updateDescription=true;
	
	public frmExpressionBuilder(AmuaModel myModel, StyledTextPane targetPane, boolean updateOperation){
		this.myModel=myModel;
		this.targetPane=targetPane;
		this.updateOperation=updateOperation;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmExpressionBuilder = new JDialog();
			frmExpressionBuilder.setIconImage(Toolkit.getDefaultToolkit().getImage(frmExpressionBuilder.class.getResource("/images/formula_128.png")));
			frmExpressionBuilder.setModalityType(ModalityType.APPLICATION_MODAL);
			frmExpressionBuilder.setTitle("Amua - Build Expression");
			frmExpressionBuilder.setResizable(false);
			frmExpressionBuilder.setBounds(100, 100, 1000, 581);
			frmExpressionBuilder.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmExpressionBuilder.getContentPane().setLayout(null);
			
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 29, 200, 318);
			frmExpressionBuilder.getContentPane().add(scrollPane);
			
			listType = new JList<String>();
			listType.setFont(new Font("SansSerif", Font.PLAIN, 12));
			listType.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					updateDescription=false;
					listValue.clearSelection();
					String selectedType=listType.getSelectedValue();
					selectType(selectedType);
					textPaneDescription.setText(""); //clear
					updateDescription=true;
				}
			});
			
			DefaultListModel<String> modelType= new DefaultListModel<String>();
			modelType.addElement("<html><b><i>Model Objects</i></b></html>");
			modelType.addElement("Parameter");
			modelType.addElement("Table");
			modelType.addElement("Variable");
			if(myModel.type==1){
				modelType.addElement("Markov");
			}
			modelType.addElement("");
			modelType.addElement("<html><b><i>Functions</i></b></html>");
			modelType.addElement("Constant");
			modelType.addElement("Distribution");
			modelType.addElement("Function");
			modelType.addElement("Matrix Function");
			modelType.addElement("");
			modelType.addElement("<html><b><i>Operators</i></b></html>");
			modelType.addElement("Logical Operator");
			modelType.addElement("Operator");
			if(updateOperation){
				modelType.addElement("Update Operator");
			}
			
			listType.setModel(modelType);
			listType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane.setViewportView(listType);
			
			JLabel lblDescription = new JLabel("Description");
			lblDescription.setBounds(421, 10, 86, 16);
			frmExpressionBuilder.getContentPane().add(lblDescription);
			
			JButton btnInsert = new JButton("Insert Expression");
			btnInsert.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						int curPos=targetPane.getCaretPosition();
						String text=textPaneExpression.getText();
						targetPane.getDocument().insertString(curPos, text, null);
						frmExpressionBuilder.dispose();
					}catch(Exception e1){
						JOptionPane.showMessageDialog(frmExpressionBuilder, e1.toString());
						myModel.errorLog.recordError(e1);
					}
				}
			});
			btnInsert.setBounds(644, 499, 135, 28);
			frmExpressionBuilder.getContentPane().add(btnInsert);
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder.dispose();
				}
			});
			btnCancel.setBounds(796, 499, 90, 28);
			frmExpressionBuilder.getContentPane().add(btnCancel);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(415, 29, 573, 318);
			frmExpressionBuilder.getContentPane().add(scrollPane_1);
			
			textPaneDescription = new JTextPane();
			textPaneDescription.setContentType("text/html");
			textPaneDescription.setEditable(false);
			scrollPane_1.setViewportView(textPaneDescription);
			
			JLabel lblExpression = new JLabel("Expression:");
			lblExpression.setBounds(12, 368, 90, 16);
			frmExpressionBuilder.getContentPane().add(lblExpression);
			
			JScrollPane scrollPane_2 = new JScrollPane();
			scrollPane_2.setBounds(12, 391, 874, 60);
			frmExpressionBuilder.getContentPane().add(scrollPane_2);
			
			textPaneExpression = new StyledTextPane(myModel);
			textPaneExpression.setFont(new Font("Consolas", Font.PLAIN, 15));
			textPaneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluate();
					}
				}
			});
			scrollPane_2.setViewportView(textPaneExpression);
			
			JLabel lblType = new JLabel("Elements");
			lblType.setBounds(12, 10, 55, 16);
			frmExpressionBuilder.getContentPane().add(lblType);
			
			JLabel lblValues = new JLabel("Values");
			lblValues.setBounds(220, 10, 55, 16);
			frmExpressionBuilder.getContentPane().add(lblValues);
			
			JScrollPane scrollPane_3 = new JScrollPane();
			scrollPane_3.setBounds(214, 29, 200, 318);
			frmExpressionBuilder.getContentPane().add(scrollPane_3);
			
			modelValue=new DefaultListModel<String>();
			
			listValue = new JList<String>();
			listValue.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(arg0.getButton()==MouseEvent.BUTTON1 && arg0.getClickCount()==2){ //double-click
						try{
							int curPos=textPaneExpression.getCaretPosition();
							String text=insertValue();
							textPaneExpression.getDocument().insertString(curPos, text, null);
						} catch(Exception e1){
							JOptionPane.showMessageDialog(frmExpressionBuilder, e1.toString());
							myModel.errorLog.recordError(e1);
						}
					}
				}
			});
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15));
			listValue.setModel(modelValue);
			listValue.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if(updateDescription){
						String description=getDescription();
						textPaneDescription.setText(description);
					}
				}
			});
			listValue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane_3.setViewportView(listValue);
			
			JButton btnInsertValue = new JButton("Insert Value");
			btnInsertValue.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						int curPos=textPaneExpression.getCaretPosition();
						String text=insertValue();
						textPaneExpression.getDocument().insertString(curPos, text, null);
					} catch(Exception e1){
						JOptionPane.showMessageDialog(frmExpressionBuilder, e1.toString());
						myModel.errorLog.recordError(e1);
					}
				}
			});
			btnInsertValue.setBounds(263, 351, 104, 28);
			frmExpressionBuilder.getContentPane().add(btnInsertValue);
			
			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(898, 407, 90, 28);
			frmExpressionBuilder.getContentPane().add(btnEvaluate);
			
			JLabel lblExpectedValue = new JLabel("Expected Value:");
			lblExpectedValue.setBounds(12, 463, 104, 16);
			frmExpressionBuilder.getContentPane().add(lblExpectedValue);
			
			JScrollPane scrollPane_4 = new JScrollPane();
			scrollPane_4.setBounds(12, 481, 610, 60);
			frmExpressionBuilder.getContentPane().add(scrollPane_4);
			
			textPaneValue = new JTextPane();
			textPaneValue.setEditable(false);
			scrollPane_4.setViewportView(textPaneValue);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void selectType(String type){
		modelValue.clear();
		switch(type){
		case "Parameter":{
			for(int p=0; p<myModel.parameters.size(); p++){
				modelValue.addElement(myModel.parameters.get(p).name);
			}
			break;
		}
		case "Table":{
			for(int p=0; p<myModel.tables.size(); p++){
				modelValue.addElement(myModel.tables.get(p).name);
			}
			break;
		}
		case "Variable":{
			for(int p=0; p<myModel.variables.size(); p++){
				modelValue.addElement(myModel.variables.get(p).name);
			}
			break;
		}
		case "Markov":{
			modelValue.addElement("t");
			modelValue.addElement("trace");
			break;
		}
		case "Constant":{
			modelValue.addElement("e");
			modelValue.addElement("inf");
			modelValue.addElement("pi");
			break;
		}
		case "Distribution":{
			modelValue.addElement("<html><b><i>Discrete</i></b></html>");
			modelValue.addElement("Bern");
			modelValue.addElement("Bin");
			modelValue.addElement("Cat");
			modelValue.addElement("DUnif");
			modelValue.addElement("Geom");
			modelValue.addElement("HGeom");
			modelValue.addElement("NBin");
			modelValue.addElement("Pois");
			modelValue.addElement("Zipf");
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Continuous</i></b></html>");
			modelValue.addElement("Beta");
			modelValue.addElement("Cauchy");
			modelValue.addElement("ChiSq");
			modelValue.addElement("Expo");
			modelValue.addElement("Gamma");
			modelValue.addElement("Gumbel");
			modelValue.addElement("HalfCauchy");
			modelValue.addElement("HalfNorm");
			modelValue.addElement("Laplace");
			modelValue.addElement("Logistic");
			modelValue.addElement("LogNorm");
			modelValue.addElement("Norm");
			modelValue.addElement("Pareto");
			modelValue.addElement("PERT");
			modelValue.addElement("StudentT");
			modelValue.addElement("Tri");
			modelValue.addElement("TruncNorm");
			modelValue.addElement("Unif");
			modelValue.addElement("Weibull");
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Multivariate</i></b></html>");
			modelValue.addElement("Dir");
			modelValue.addElement("MvNorm");
			modelValue.addElement("Multi");
			break;
		}
		case "Function":{
			modelValue.addElement("<html><b><i>Bounding</i></b></html>");
			modelValue.addElement("abs");
			modelValue.addElement("bound");
			modelValue.addElement("ceil");
			modelValue.addElement("floor");
			modelValue.addElement("max");
			modelValue.addElement("min");
			modelValue.addElement("round");
			modelValue.addElement("signum");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Combinatorial</i></b></html>");
			modelValue.addElement("choose");
			modelValue.addElement("fact");
			modelValue.addElement("gamma");
			modelValue.addElement("logGamma");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Error</i></b></html>");
			modelValue.addElement("erf");
			modelValue.addElement("invErf");
			modelValue.addElement("probit");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Logarithmic</i></b></html>");
			modelValue.addElement("exp");
			modelValue.addElement("log");
			modelValue.addElement("logb");
			modelValue.addElement("log10");
			modelValue.addElement("logit");
			modelValue.addElement("logistic");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Logical</i></b></html>");
			modelValue.addElement("if");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Probability/Rate</i></b></html>");
			modelValue.addElement("probRescale");
			modelValue.addElement("probToRate");
			modelValue.addElement("rateToProb");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Roots</i></b></html>");
			modelValue.addElement("cbrt");
			modelValue.addElement("sqrt");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Summary</i></b></html>");
			modelValue.addElement("mean");
			modelValue.addElement("product");
			modelValue.addElement("quantile");
			modelValue.addElement("sd");
			modelValue.addElement("sum");
			modelValue.addElement("var");
			
			modelValue.addElement("");
			modelValue.addElement("<html><b><i>Trigonometric</i></b></html>");
			modelValue.addElement("acos");
			modelValue.addElement("asin");
			modelValue.addElement("atan");
			modelValue.addElement("cos");
			modelValue.addElement("cosh");
			modelValue.addElement("hypot");
			modelValue.addElement("sin");
			modelValue.addElement("sinh");
			modelValue.addElement("tan");
			modelValue.addElement("tanh");
			
			break;
		}
		case "Matrix Function":{
			modelValue.addElement("chol");
			modelValue.addElement("det");
			modelValue.addElement("diag");
			modelValue.addElement("iden");
			modelValue.addElement("inv");
			modelValue.addElement("ncol");
			modelValue.addElement("norm");
			modelValue.addElement("nrow");
			modelValue.addElement("renorm");
			modelValue.addElement("rep");
			modelValue.addElement("seq");
			modelValue.addElement("softmax");
			modelValue.addElement("stack");
			modelValue.addElement("tp");
			modelValue.addElement("tr");
			
			break;
		}
		case "Logical Operator":{
			modelValue.addElement("==");
			modelValue.addElement("!=");
			modelValue.addElement("<");
			modelValue.addElement(">");
			modelValue.addElement("<=");
			modelValue.addElement(">=");
			modelValue.addElement("&");
			modelValue.addElement("|");
			modelValue.addElement("^|");
			break;
		}
		case "Operator":{
			modelValue.addElement("+");
			modelValue.addElement("-");
			modelValue.addElement("*");
			modelValue.addElement("/");
			modelValue.addElement("^");
			modelValue.addElement("%");
			break;
		}
		case "Update Operator":{
			modelValue.addElement("=");
			modelValue.addElement("++");
			modelValue.addElement("--");
			modelValue.addElement("+=");
			modelValue.addElement("-=");
			modelValue.addElement("*=");
			modelValue.addElement("/=");
			break;
		}
		
		} //end switch
	}
	
	private String getDescription(){
		String selectedType=listType.getSelectedValue();
		String selectedValue=listValue.getSelectedValue();
		
		switch(selectedType){
		case "Parameter": 	return(myModel.getParamDescription(selectedValue));
		case "Table": 	return(myModel.getTableDescription(selectedValue));
		case "Variable": 	return(myModel.getVarDescription(selectedValue));
		case "Markov":{
			String des="";
			switch(selectedValue){
			case "t":
				des="<html><b>Time (cycle index)</b><br>";
				des+=MathUtils.consoleFont("<b><i>t</i></b>")+": Returns the current cycle index, starting from "+MathUtils.consoleFont("t=0")+"<br>";
				des+="</html>";
				return(des);
			case "trace":
				des="<html><b>Markov Trace</b><br>";
				des+=MathUtils.consoleFont("<b><i>trace</i></b>[cycle,column]")+": Returns the "+MathUtils.consoleFont("column")+" trace value for the specified "+MathUtils.consoleFont("cycle")+"<br>";
				des+="<br><i>Arguments</i><br>";
				des+=MathUtils.consoleFont("cycle")+": Cycle index, integer "+MathUtils.consoleFont("≥0")+"<br>";
				des+=MathUtils.consoleFont("column")+": Column index (integer "+MathUtils.consoleFont("≥0")+"), or column name as a "+MathUtils.consoleFont("string")+"<br>";
				des+="</html>";
				return(des);
			} //end switch
			return(""); //fell through
		}
		case "Constant": 	return(Constants.getDescription(selectedValue));
		case "Distribution": return(Distributions.getDescription(selectedValue));
		case "Function": return(Functions.getDescription(selectedValue));
		case "Matrix Function": return(MatrixFunctions.getDescription(selectedValue));
		case "Logical Operator": return(Operators.getDescription(selectedValue));
		case "Operator": return(Operators.getDescription(selectedValue));
		case "Update Operator":{
			String des="";
			switch(selectedValue){
			case "=":
				des="<html><b>Assignment</b><br>";
				des+=MathUtils.consoleFont("a = b")+": Updates the value of "+MathUtils.consoleFont("a")+" to equal "+MathUtils.consoleFont("b")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var = 3 + 3")+" results in "+MathUtils.consoleFont("var = 6")+"<br>";
				des+="</html>";
				return(des);
			case "++":
				des="<html><b>Increment by 1</b><br>";
				des+=MathUtils.consoleFont("a++")+": Increments the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("1")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var++")+" results in the same update as "+MathUtils.consoleFont("var = var + 1")+"<br>";
				des+="</html>";
				return(des);
			case "--":
				des="<html><b>Decrement by 1</b><br>";
				des+=MathUtils.consoleFont("a--")+": Decrements the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("1")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var--")+" results in the same update as "+MathUtils.consoleFont("var = var - 1")+"<br>";
				des+="</html>";
				return(des);
			case "+=":
				des="<html><b>Increment</b><br>";
				des+=MathUtils.consoleFont("a += b")+": Increments the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("b")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var += b")+" results in the same update as "+MathUtils.consoleFont("var = var + b")+"<br>";
				des+="</html>";
				return(des);
			case "-=":
				des="<html><b>Decrement</b><br>";
				des+=MathUtils.consoleFont("a -= b")+": Decrements the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("b")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var -= b")+" results in the same update as "+MathUtils.consoleFont("var = var - b")+"<br>";
				des+="</html>";
				return(des);
			case "*=":
				des="<html><b>Multiplication Assignment</b><br>";
				des+=MathUtils.consoleFont("a *= b")+": Multiplies the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("b")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var *= b")+" results in the same update as "+MathUtils.consoleFont("var = var * b")+"<br>";
				des+="</html>";
				return(des);
			case "/=":
				des="<html><b>Division Assignment</b><br>";
				des+=MathUtils.consoleFont("a /= b")+": Divides the value of "+MathUtils.consoleFont("a")+" by "+MathUtils.consoleFont("b")+"<br><br>";
				des+="<i>Example</i><br>"+MathUtils.consoleFont("var /= b")+" results in the same update as "+MathUtils.consoleFont("var = var / b")+"<br>";
				des+="</html>";
				return(des);
			}//end switch
			return(""); //fell through
		}
		
		} //end switch
		return("");
	}
	
	private String insertValue(){
		String selectedType=listType.getSelectedValue();
		String selectedValue=listValue.getSelectedValue();
		
		switch(selectedType){
		case "Parameter": 	return(selectedValue);
		case "Table":{ 	
			Table curTable=myModel.tables.get(myModel.getTableIndex(selectedValue));
			if(curTable.type.matches("Lookup")){
				return(selectedValue+"[index,column]");
			}
			else if(curTable.type.matches("Distribution")){
				return(selectedValue+"(column,~)");
			}
			else if(curTable.type.matches("Matrix")){
				return(selectedValue+"[row,column]");
			}
			return(myModel.getTableDescription(selectedValue));
		}
		case "Variable": 	return(selectedValue);
		case "Markov":{
			switch(selectedValue){
			case "t": return("t");
			case "trace": return("trace[cycle,column]");
			} //end switch
			return(""); //fell through
		}
		case "Constant": 	return(selectedValue);
		case "Distribution": return(Distributions.getDefaultParams(selectedValue));
		case "Function": return(Functions.getDefaultArgs(selectedValue));
		case "Matrix Function": return(MatrixFunctions.getDefaultArgs(selectedValue));
		case "Logical Operator": return(selectedValue);
		case "Operator": return(selectedValue);
		case "Update Operator": return(selectedValue);
		
		} //end switch
		return("");
	}
	
	private void evaluate(){
		String testExp=textPaneExpression.getText();
		Numeric testVal=null;
		if(updateOperation==false){
			boolean updatePane=true;
			try{
				testVal=Interpreter.evaluate(testExp, myModel,false);
			}catch(Exception e1){
				updatePane=false;
				textPaneValue.setText(e1.toString());
			}
			if(updatePane){textPaneValue.setText(testVal.toString()+"");}
		}
		else{ //update operation
			
		}
	}

}
