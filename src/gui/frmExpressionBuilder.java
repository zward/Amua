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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

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
import java.util.HashMap;
import java.util.Map;

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
			frmExpressionBuilder.setTitle("Amua - "+myModel.language.base.getString("button.build_expression")); //Build Expression
			frmExpressionBuilder.setFont(myModel.language.font);
			frmExpressionBuilder.setResizable(false);
			frmExpressionBuilder.setBounds(100, 100, 1000, 600);
			frmExpressionBuilder.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmExpressionBuilder.getContentPane().setLayout(null);
			
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 29, 200, 318);
			frmExpressionBuilder.getContentPane().add(scrollPane);
			
			listType = new JList<String>();
			//listType.setFont(new Font("SansSerif", Font.PLAIN, 12));
			listType.setFont(myModel.language.font);
			listType.setSelectionBackground(new Color(57, 105, 138)); //grey-blue
			listType.setSelectionForeground(Color.WHITE);
			
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
			modelType.addElement(myModel.language.base.getString("object.model_objects")); //Model Objects (header)
			modelType.addElement(myModel.language.base.getString("object.parameter")); //Parameter
			modelType.addElement(myModel.language.base.getString("object.table")); //Table
			modelType.addElement(myModel.language.base.getString("object.variable")); //Variable
			if(myModel.type==1){
				modelType.addElement(myModel.language.base.getString("markov.markov")); //Markov
			}
			modelType.addElement("");
			modelType.addElement(myModel.language.math.getString("fx.functions")); //Functions (header)
			modelType.addElement(myModel.language.math.getString("const.constant")); //Constant
			modelType.addElement(myModel.language.base.getString("table.distribution")); //Distribution
			modelType.addElement(myModel.language.math.getString("fx.function")); //Function
			modelType.addElement(myModel.language.math.getString("mat.matrix_function")); //Matrix Function
			modelType.addElement("");
			modelType.addElement(myModel.language.math.getString("op.operators")); //Operators (header)
			modelType.addElement(myModel.language.math.getString("op.logical_operator")); //Logical Operator
			modelType.addElement(myModel.language.math.getString("op.operator")); //Operator
			if(updateOperation){
				modelType.addElement(myModel.language.math.getString("op.update_operator")); //Update Operator
			}
			
			int header1=5;
			int header2=11;
			if(myModel.type==1) {
				header1++; header2++;
			}
			final int fxIndex=header1;
			final int opIndex=header2;
			
			listType.setModel(modelType);
			listType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane.setViewportView(listType);
			
			//set fonts
			final Font headerFont = myModel.language.font.deriveFont(Font.BOLD | Font.ITALIC);
			final Font normalFont = myModel.language.font.deriveFont(Font.PLAIN);
			listType.setCellRenderer(new DefaultListCellRenderer() {
			    @Override
			    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			        label.setFont(normalFont);

			        // Section headers: bold + italic
			        if (index==0 || index==fxIndex || index==opIndex) {
			            label.setFont(headerFont);
			        }
			        
			        return label;
			    }
			});
			
			
			JLabel lblDescription = new JLabel(myModel.language.base.getString("title.description")); //Description 
			lblDescription.setFont(myModel.language.font);
			lblDescription.setBounds(421, 10, 86, 16);
			frmExpressionBuilder.getContentPane().add(lblDescription);
			
			JButton btnInsert = new JButton(myModel.language.base.getString("button.update_expression")); //Update Expression
			btnInsert.setFont(myModel.language.font);
			btnInsert.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						String text=textPaneExpression.getText();
						//int curPos=targetPane.getCaretPosition();
						//targetPane.getDocument().insertString(curPos, text, null);
						targetPane.setText(text);
						frmExpressionBuilder.dispose();
					}catch(Exception e1){
						JOptionPane.showMessageDialog(frmExpressionBuilder, e1.toString());
						myModel.errorLog.recordError(e1);
					}
				}
			});
			btnInsert.setBounds(623, 510, 161, 28);
			frmExpressionBuilder.getContentPane().add(btnInsert);
			
			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel 
			btnCancel.setFont(myModel.language.font);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder.dispose();
				}
			});
			btnCancel.setBounds(796, 510, 90, 28);
			frmExpressionBuilder.getContentPane().add(btnCancel);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(415, 29, 573, 318);
			frmExpressionBuilder.getContentPane().add(scrollPane_1);
			
			textPaneDescription = new JTextPane();
			textPaneDescription.setFont(myModel.language.font);
			StyleConstants.setFontFamily(textPaneDescription.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE), myModel.language.font.getFamily());
			textPaneDescription.setContentType("text/html");
			textPaneDescription.setEditable(false);
			scrollPane_1.setViewportView(textPaneDescription);
			
			JLabel lblExpression = new JLabel(myModel.language.base.getString("object.expression")+":"); //Expression
			lblExpression.setFont(myModel.language.font);
			lblExpression.setBounds(12, 368, 90, 16);
			frmExpressionBuilder.getContentPane().add(lblExpression);
			
			JScrollPane scrollPane_2 = new JScrollPane();
			scrollPane_2.setBounds(12, 391, 874, 80);
			frmExpressionBuilder.getContentPane().add(scrollPane_2);
			
			textPaneExpression = new StyledTextPane(myModel, myModel.language);
			//textPaneExpression.setFont(new Font("Consolas", Font.PLAIN, 15));
			textPaneExpression.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			textPaneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluate();
					}
				}
			});
			scrollPane_2.setViewportView(textPaneExpression);
			textPaneExpression.setText(targetPane.getText());
			
			JLabel lblType = new JLabel(myModel.language.base.getString("object.elements")); //Elements
			lblType.setFont(myModel.language.font);
			lblType.setBounds(12, 10, 55, 16);
			frmExpressionBuilder.getContentPane().add(lblType);
			
			JLabel lblValues = new JLabel(myModel.language.analysis.getString("result.values")); //Values
			lblValues.setFont(myModel.language.font);
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
			//listValue.setFont(new Font("Consolas", Font.PLAIN, 15));
			//listValue.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			listValue.setSelectionBackground(new Color(57, 105, 138)); //grey-blue
			listValue.setSelectionForeground(Color.WHITE);
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
			
			JButton btnInsertValue = new JButton(myModel.language.base.getString("button.insert_value")); //Insert Value
			btnInsertValue.setFont(myModel.language.font);
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
			btnInsertValue.setBounds(252, 351, 115, 28);
			frmExpressionBuilder.getContentPane().add(btnInsertValue);
			
			JButton btnEvaluate = new JButton(myModel.language.base.getString("button.evaluate")); //Evaluate
			btnEvaluate.setFont(myModel.language.font);
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate();
				}
			});
			btnEvaluate.setBounds(898, 407, 90, 28);
			frmExpressionBuilder.getContentPane().add(btnEvaluate);
			
			JLabel lblExpectedValue = new JLabel(myModel.language.base.getString("object.expected_value")+":"); //Expected Value
			lblExpectedValue.setFont(myModel.language.font);
			lblExpectedValue.setBounds(12, 474, 104, 16);
			frmExpressionBuilder.getContentPane().add(lblExpectedValue);
			
			JScrollPane scrollPane_4 = new JScrollPane();
			scrollPane_4.setBounds(12, 492, 610, 60);
			frmExpressionBuilder.getContentPane().add(scrollPane_4);
			
			textPaneValue = new JTextPane();
			textPaneValue.setFont(myModel.language.font);
			StyleConstants.setFontFamily(textPaneValue.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE), myModel.language.font.getFamily());
			textPaneValue.setEditable(false);
			scrollPane_4.setViewportView(textPaneValue);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void selectType(String type){
		modelValue.clear();
	
		//set fonts
		final Font headerFont = myModel.language.fontCode.deriveFont(Font.BOLD | Font.ITALIC, 15);
		final Font normalFont = new Font("Consolas", Font.PLAIN, 15); //English only
		
		if(type.equals(myModel.language.base.getString("object.parameter"))) { //Parameter
			listValue.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			listValue.setCellRenderer(new DefaultListCellRenderer());
		    for(int p=0; p<myModel.parameters.size(); p++){
		        modelValue.addElement(myModel.parameters.get(p).name);
		    }
		}
		else if(type.equals(myModel.language.base.getString("object.table"))) { //Table
			listValue.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			listValue.setCellRenderer(new DefaultListCellRenderer());
			for(int p=0; p<myModel.tables.size(); p++){
				modelValue.addElement(myModel.tables.get(p).name);
			}
		}
		else if(type.equals(myModel.language.base.getString("object.variable"))) { //Variable
			listValue.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			listValue.setCellRenderer(new DefaultListCellRenderer());
			for(int p=0; p<myModel.variables.size(); p++){
				modelValue.addElement(myModel.variables.get(p).name);
			}
		}
		else if(type.equals(myModel.language.base.getString("markov.markov"))) { //Markov
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("t");
			modelValue.addElement("trace");
		}
		else if(type.equals(myModel.language.math.getString("const.constant"))) { //Constant
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("e");
			modelValue.addElement("inf");
			modelValue.addElement("pi");
		}
		else if(type.equals(myModel.language.base.getString("table.distribution"))) { //Distribution
			listValue.setCellRenderer(new DefaultListCellRenderer() {
			    @Override
			    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			        label.setFont(normalFont);
			        // Section headers: bold + italic
			        if (index==0 || index==11 || index==32) {
			            label.setFont(headerFont);
			        }
			        return label;
			    }
			});
			
			modelValue.addElement(myModel.language.dist.getString("gen.discrete")); //Discrete (0)
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
			modelValue.addElement(myModel.language.dist.getString("gen.continuous")); //Continuous (11)
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
			modelValue.addElement(myModel.language.dist.getString("gen.multivariate")); //Multivariate (32)
			modelValue.addElement("Dir");
			modelValue.addElement("MvNorm");
			modelValue.addElement("Multi");
		}
		else if(type.equals(myModel.language.math.getString("fx.function"))) { //Function
			listValue.setCellRenderer(new DefaultListCellRenderer() {
			    @Override
			    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			        label.setFont(normalFont);
			        // Section headers: bold + italic
			        if (index==0 || index==10 || index==16 || index==21 || index==29 || index==32 || index==37 || index==41 || index==49) {
			            label.setFont(headerFont);
			        }
			        return label;
			    }
			});
			
			modelValue.addElement(myModel.language.math.getString("fx.bounding")); //Bounding (0)
			modelValue.addElement("abs");
			modelValue.addElement("bound");
			modelValue.addElement("ceil");
			modelValue.addElement("floor");
			modelValue.addElement("max");
			modelValue.addElement("min");
			modelValue.addElement("round");
			modelValue.addElement("signum");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.combinatorial")); //Combinatorial (10)
			modelValue.addElement("choose");
			modelValue.addElement("fact");
			modelValue.addElement("gamma");
			modelValue.addElement("logGamma");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.error")); //Error (16)
			modelValue.addElement("erf");
			modelValue.addElement("invErf");
			modelValue.addElement("probit");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.logarithmic")); //Logarithmic (21)
			modelValue.addElement("exp");
			modelValue.addElement("log");
			modelValue.addElement("logb");
			modelValue.addElement("log10");
			modelValue.addElement("logit");
			modelValue.addElement("logistic");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.logical")); //Logical (29)
			modelValue.addElement("if");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.prob_rate")); //Probability/Rate (32)
			modelValue.addElement("probRescale");
			modelValue.addElement("probToRate");
			modelValue.addElement("rateToProb");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.roots")); //Roots (37)
			modelValue.addElement("cbrt");
			modelValue.addElement("sqrt");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.summary")); //Summary (41)
			modelValue.addElement("mean");
			modelValue.addElement("product");
			modelValue.addElement("quantile");
			modelValue.addElement("sd");
			modelValue.addElement("sum");
			modelValue.addElement("var");
			
			modelValue.addElement("");
			modelValue.addElement(myModel.language.math.getString("fx.trigonometric")); //Trigonometric (49)
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
		}
		else if(type.equals(myModel.language.math.getString("mat.matrix_function"))) { //Matrix Function
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("chol");
			modelValue.addElement("det");
			modelValue.addElement("diag");
			modelValue.addElement("iden");
			modelValue.addElement("interpolate");
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
		}
		else if(type.equals(myModel.language.math.getString("op.logical_operator"))) { //Logical Operator
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("==");
			modelValue.addElement("!=");
			modelValue.addElement("<");
			modelValue.addElement(">");
			modelValue.addElement("<=");
			modelValue.addElement(">=");
			modelValue.addElement("&");
			modelValue.addElement("|");
			modelValue.addElement("^|");
		}
		else if(type.equals(myModel.language.math.getString("op.operator"))) { //Operator
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("+");
			modelValue.addElement("-");
			modelValue.addElement("*");
			modelValue.addElement("/");
			modelValue.addElement("^");
			modelValue.addElement("%");
		}
		else if(type.equals(myModel.language.math.getString("op.update_operator"))) { //Update Operator
			listValue.setFont(new Font("Consolas", Font.PLAIN, 15)); //English only
			listValue.setCellRenderer(new DefaultListCellRenderer());
			modelValue.addElement("=");
			modelValue.addElement("++");
			modelValue.addElement("--");
			modelValue.addElement("+=");
			modelValue.addElement("-=");
			modelValue.addElement("*=");
			modelValue.addElement("/=");
		}
	}
		
	private String getDescription(){
		String selectedType=listType.getSelectedValue();
		String selectedValue=listValue.getSelectedValue();
		
		
		if(selectedType.equals(myModel.language.base.getString("object.parameter"))) { //Parameter
			return(myModel.getParamDescription(selectedValue));
		}
		else if(selectedType.equals(myModel.language.base.getString("object.table"))) { //Table
			return(myModel.getTableDescription(selectedValue));
		}
		else if(selectedType.equals(myModel.language.base.getString("object.variable"))) { //Variable
			return(myModel.getVarDescription(selectedValue));
		}
		else if(selectedType.equals(myModel.language.base.getString("markov.markov"))) { //Markov
			String des="";
			switch(selectedValue){
			case "t":
				des="<html><b>"+myModel.language.base.getString("markov.time_cycle_index")+"</b><br>"; //Time (cycle index)
				des+=MathUtils.consoleFont("<b><i>t</i></b>")+": "+myModel.language.base.getString("markov.t_desc")+"<br>"; //Returns the current cycle index, starting from t=0
				des+="</html>";
				return(des);
			case "trace":
				des="<html><b>"+myModel.language.base.getString("markov.markov_trace")+"</b><br>"; //Markov Trace
				String strCycle=myModel.language.base.getString("markov.cycle").toLowerCase(myModel.language.locale);
				String strColumn=myModel.language.base.getString("table.column").toLowerCase(myModel.language.locale);
				strCycle="cycle"; //hard-code
				strColumn="column"; //hard-code
				
				des+=MathUtils.consoleFont("<b><i>trace</i></b>["+strCycle+","+strColumn+"]")+": "+myModel.language.base.getString("markov.trace_desc")+"<br>"; //Returns the column trace value for the specified cycle
				des+="<br><i>"+myModel.language.math.getString("fx.arguments")+"</i><br>"; //Arguments
				des+=MathUtils.consoleFont(strCycle)+": "+myModel.language.base.getString("markov.arg_cycle")+"<br>"; //Cycle index, integer ≥0
				des+=MathUtils.consoleFont(strColumn)+": "+myModel.language.base.getString("markov.arg_column")+"<br>"; //Column index (integer ≥0), or column name as a string
				des+="</html>";
				return(des);
			} //end switch
			return(""); //fell through
		}
		else if(selectedType.equals(myModel.language.math.getString("const.constant"))) { //Constant
			return(Constants.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.base.getString("table.distribution"))) { //Distribution
			return(Distributions.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.math.getString("fx.function"))) { //Function
			return(Functions.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.math.getString("mat.matrix_function"))) { //Matrix Function
			return(MatrixFunctions.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.math.getString("op.logical_operator"))) { //Logical Operator
			return(Operators.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.math.getString("op.operator"))) { //Operator
			return(Operators.getDescription(selectedValue, myModel.language));
		}
		else if(selectedType.equals(myModel.language.math.getString("op.update_operator"))) { //Update Operator
			String des="";
			switch(selectedValue){
			case "=":
				des="<html><b>"+myModel.language.math.getString("up.assignment")+"</b><br>"; //Assignment
				des+=MathUtils.consoleFont("a = b")+": "+myModel.language.math.getString("up.assign_desc")+"<br><br>"; //Updates the value of a to equal b
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var = 3 + 3")+" "+myModel.language.math.getString("up.results_in")+" "+MathUtils.consoleFont("var = 6")+"<br>"; //...results in...
				des+="</html>";
				return(des);
			case "++":
				des="<html><b>"+myModel.language.math.getString("up.increment")+"</b><br>"; //Increment by 1
				des+=MathUtils.consoleFont("a++")+": "+myModel.language.math.getString("up.increment_desc")+"<br><br>"; //Increments the value of a by 1
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var++")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var + 1")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			case "--":
				des="<html><b>"+myModel.language.math.getString("up.decrement")+"</b><br>"; //Decrement by 1
				des+=MathUtils.consoleFont("a--")+": "+myModel.language.math.getString("up.decrement_desc")+"<br><br>"; //Decrements the value of a by 1
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var--")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var - 1")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			case "+=":
				des="<html><b>"+myModel.language.math.getString("up.increment_gen")+"</b><br>"; //Increment
				des+=MathUtils.consoleFont("a += b")+": "+myModel.language.math.getString("up.increment_gen_desc")+"<br><br>"; //Increments the value of a by b
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var += b")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var + b")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			case "-=":
				des="<html><b>"+myModel.language.math.getString("up.decrement_gen")+"</b><br>"; //Decrement
				des+=MathUtils.consoleFont("a -= b")+": "+myModel.language.math.getString("up.decrement_gen_desc")+"<br><br>"; //Decrements the value of a by b
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var -= b")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var - b")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			case "*=":
				des="<html><b>"+myModel.language.math.getString("up.mult")+"</b><br>"; //Multiplication Assignment
				des+=MathUtils.consoleFont("a *= b")+": "+myModel.language.math.getString("up.mult_desc")+"<br><br>"; //Multiplies the value of a by b
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var *= b")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var * b")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			case "/=":
				des="<html><b>"+myModel.language.math.getString("up.div")+"</b><br>"; //Division Assignment
				des+=MathUtils.consoleFont("a /= b")+": "+myModel.language.math.getString("up.div_desc")+"<br><br>"; //Divides the value of a by b
				des+="<i>"+myModel.language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("var /= b")+" "+myModel.language.math.getString("up.results_in_same")+" "+MathUtils.consoleFont("var = var / b")+"<br>"; //results in the same update as
				des+="</html>";
				return(des);
			}//end switch
			return(""); //fell through
		}
		return(""); //fell through
	}
	
	private String insertValue(){
		String selectedType=listType.getSelectedValue();
		String selectedValue=listValue.getSelectedValue();
		
		if(selectedType.equals(myModel.language.base.getString("object.parameter"))) { //Parameter
			return(selectedValue);
		}
		else if(selectedType.equals(myModel.language.base.getString("object.table"))) { //Table
			Table curTable=myModel.tables.get(myModel.getTableIndex(selectedValue));
			if(curTable.type.matches("Lookup")){
				String strIndex=myModel.language.base.getString("table.index").toLowerCase(myModel.language.locale);
				String strColumn=myModel.language.base.getString("table.column").toLowerCase(myModel.language.locale);
				return(selectedValue+"["+strIndex+","+strColumn+"]");
			}
			else if(curTable.type.matches("Distribution")){
				String strColumn=myModel.language.base.getString("table.column").toLowerCase(myModel.language.locale);
				return(selectedValue+"("+strColumn+",~)");
			}
			else if(curTable.type.matches("Matrix")){
				String strRow=myModel.language.base.getString("table.row").toLowerCase(myModel.language.locale);
				String strColumn=myModel.language.base.getString("table.column").toLowerCase(myModel.language.locale);
				return(selectedValue+"["+strRow+","+strColumn+"]");
			}
			return(myModel.getTableDescription(selectedValue));
		}
		else if(selectedType.equals(myModel.language.base.getString("object.variable"))) { //Variable
			return(selectedValue);
		}
		else if(selectedType.equals(myModel.language.base.getString("markov.markov"))) { //Markov
			if(selectedValue.equals("t")) { return("t");}
			else if(selectedValue.equals("trace")) {return("trace[cycle,column]");}
			else {return("");} //fell through
		}
		else if(selectedType.equals(myModel.language.math.getString("const.constant"))) { //Constant
			return(selectedValue);
		}
		else if(selectedType.equals(myModel.language.base.getString("table.distribution"))) { //Distribution
			return(Distributions.getDefaultParams(selectedValue));
		}
		else if(selectedType.equals(myModel.language.math.getString("fx.function"))) { //Function
			return(Functions.getDefaultArgs(selectedValue));
		}
		else if(selectedType.equals(myModel.language.math.getString("mat.matrix_function"))) { //Matrix Function
			return(MatrixFunctions.getDefaultArgs(selectedValue));
		}
		else if(selectedType.equals(myModel.language.math.getString("op.logical_operator"))) { //Logical Operator
			return(selectedValue);
		}
		else if(selectedType.equals(myModel.language.math.getString("op.operator"))) { //Operator
			return(selectedValue);
		}
		else if(selectedType.equals(myModel.language.math.getString("op.update_operator"))) { //Update Operator
			return(selectedValue);
		}
	
		return(""); //fell through
	}
	
	private void evaluate(){
		String testExp=textPaneExpression.getText();
		Numeric testVal=null;
		if(updateOperation==false){
			boolean updatePane=true;
			try{
				testVal=Interpreter.evaluate(testExp, myModel,false,myModel.language);
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
