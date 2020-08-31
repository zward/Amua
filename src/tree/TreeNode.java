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

package tree;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.ModelNode;
import main.VariableUpdate;
import markov.MarkovNode;
import math.MathUtils;
import math.Token;

@XmlRootElement(name="node")
public class TreeNode extends ModelNode{
	//Model-specific data
	@XmlElement public String prob, cost[], payoff[];
		
	//Numeric data - parsed/calculated
	@XmlTransient int numChildren;
	@XmlTransient TreeNode children[];
	@XmlTransient Token curProbTokens[]; //[token]
	@XmlTransient Token curCostTokens[][], curPayoffTokens[][]; //[dim][token]
	@XmlTransient double curProb[]; //[thread] //Current probability used to run the model - not saved
	@XmlTransient double curCosts[], curPayoffs[];
	@XmlTransient public double expectedValues[], expectedValuesGroup[][];
	@XmlTransient public VariableUpdate curVariableUpdates[];
	//Monte Carlo
	@XmlTransient boolean probHasVar, childHasProbVar;
	@XmlTransient boolean costHasVar[];
	@XmlTransient boolean payoffHasVar[];
	@XmlTransient public double curChildProbs[][]; //[thread][child]
	@XmlTransient double totalDenom, totalCosts[], totalPayoffs[], totalNet[];
	@XmlTransient double totalDenomGroup[],	totalCostsGroup[][], totalPayoffsGroup[][], totalNetGroup[][]; //subgroups
	//multi-threaded
	@XmlTransient int numThreads, numDim, numSubgroups;
	@XmlTransient double nTotalDenom[], nTotalCosts[][], nTotalPayoffs[][];
	@XmlTransient double nTotalDenomGroup[][], nTotalCostsGroup[][][], nTotalPayoffsGroup[][][];
	
	//Visual Attributes
	@XmlTransient PanelTree panel;
	@XmlTransient public DecisionTree tree;
	@XmlTransient JLabel lblProb; TreeTextField textProb;
	@XmlTransient JLabel lblCost; TreeTextField textCost;
	@XmlTransient JLabel lblVarUpdates; TreeTextField textVarUpdates;
	@XmlTransient TreeTextField textPayoff;
	@XmlTransient JTextField textEV, textICER, textNumEnd;
	@XmlTransient String icer;
	
	//Store original text on focus to see if it has change on lost focus
	@XmlTransient String tempName, tempProb, tempCost[], tempPayoff[], tempNotes, tempVarUpdates;

	//Constructor
	public TreeNode(int type1, TreeNode parent){
		myModel=parent.myModel;
		panel=parent.panel;
		tree=parent.tree;
		type=type1;
		width=parent.width; height=parent.height;
		parentX=parent.xPos+parent.width; //Right
		parentY=parent.yPos+(parent.height/2); //Middle
		curScale=parent.curScale;
		xPos=parentX+scale(150); 
		xPos+=5; //move right a bit
		yPos=parent.yPos;
		int numSiblings=parent.childIndices.size();
		if(numSiblings>0) {//place below other children
			int maxChildY=parent.yPos;
			for(int c=0; c<numSiblings; c++) {
				TreeNode child=tree.nodes.get(parent.childIndices.get(c));
				maxChildY=Math.max(maxChildY, child.yPos);
			}
			yPos=maxChildY+scale(60);
		}
				
		selected=true;
		parentType=parent.type;
		childIndices=new ArrayList<Integer>();
		numDimensions=parent.numDimensions;
		cost=new String[numDimensions]; Arrays.fill(cost,"0");
		payoff=new String[numDimensions]; Arrays.fill(payoff,"0");
		expectedValues=new double[numDimensions];
		prob="0";
		level=parent.level+1;
		textHighlights=new Color[]{null,null,null,null,null};

		displayName();
		displayVarUpdates();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){//Chance/Decision
			displayCost();
			displayEV();
		}
		else if(type==2){ //Terminal
			displayPayoff();
			displayNumEnd();
		}
		if(level==1){
			displayICER();
		}
	}

	//Overloaded constructor for root
	public TreeNode(int x, int y, int width1, int height1, int numDims, DecisionTree tree){
		this.tree=tree;
		type=0;
		xPos=x;	yPos=y;
		width=width1; height=height1;
		childIndices=new ArrayList<Integer>();
		numDimensions=numDims;
		cost=new String[numDimensions]; Arrays.fill(cost,"0");
		payoff=new String[numDimensions]; Arrays.fill(payoff,"0");
		expectedValues=new double[numDimensions];
		level=0;
		name="Root";
		textHighlights=new Color[]{null,null,null,null,null};
	}

	public TreeNode(){ //No argument constructor - initialize display fields with XML marshalling
		if(childIndices==null){childIndices=new ArrayList<Integer>();}
		textHighlights=new Color[]{null,null,null,null,null};
	}

	public void paintComponent(Graphics g){
		if(visible){
			if(type==0){ //Decision 
				g.setColor(Color.RED); //Fill color
				if(collapsed){g.setColor(Color.LIGHT_GRAY);}
				g.fillRect(xPos,yPos,width,height);
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2f));
				g.drawRect(xPos,yPos,width,height);
				if(level!=0){
					//Draw line to parent
					g.drawLine(parentX, parentY, xPos-scale(150), yPos+(height/2));
					g.drawLine(xPos-scale(150), yPos+(height/2), xPos, yPos+(height/2));
					//Add attributes
					textName.setBounds(xPos-scale(150), yPos+(height/2)-scale(28), scale(150), scale(28));
					if(parentType!=0){ //Not decisions
						lblProb.setBounds(xPos-scale(150), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(xPos-scale(140), yPos+(height/2),scale(140),scale(28));
						lblCost.setBounds(xPos-scale(150),(int)(yPos+3*(height/2)),scale(12),scale(28));
						textCost.setBounds(xPos-scale(140),(int)(yPos+3*(height/2)),scale(140),scale(28));
						if(hasCost){
							lblVarUpdates.setBounds(xPos-scale(150), (int)(yPos+5*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+5*(height/2)),scale(140),scale(28));
						}
						else{
							lblVarUpdates.setBounds(xPos-scale(150), (int)(yPos+3*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+3*(height/2)),scale(140),scale(28));
						}
					}
					else{
						lblCost.setBounds(xPos-scale(150),yPos+(height/2),scale(12),scale(28));
						textCost.setBounds(xPos-scale(140),yPos+(height/2),scale(140),scale(28));
						if(hasCost){
							lblVarUpdates.setBounds(xPos-scale(150),(int)(yPos+3*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+3*(height/2)),scale(140),scale(28));
						}
						else{
							lblVarUpdates.setBounds(xPos-scale(150),(int)(yPos+(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+(height/2)),scale(140),scale(28));
						}
					}
					//textEV.setBounds(xPos+scale(25),yPos-scale(5),scale(140),scale(28));
					int width=Math.max((int)(textEV.getText().length()*6),140);
					textEV.setBounds(xPos+scale(25),yPos-scale(5),scale(width),scale(28));
				}
			}
			else if(type==1){ //Chance
				g.setColor(Color.GREEN); //Fill color
				if(collapsed){g.setColor(Color.LIGHT_GRAY);}
				g.fillOval(xPos, yPos, width, height);
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2f));
				g.drawOval(xPos,yPos,width,height);  
				//Draw line to parent
				g.drawLine(parentX, parentY, xPos-scale(150), yPos+(height/2));
				g.drawLine(xPos-scale(150), yPos+(height/2), xPos, yPos+(height/2));
				//Add attributes
				textName.setBounds(xPos-scale(150), yPos+(height/2)-scale(28), scale(150), scale(28));
				if(parentType!=0){ //Not decisions
					lblProb.setBounds(xPos-scale(150), yPos+(height/2),scale(12),scale(28));
					textProb.setBounds(xPos-scale(140), yPos+(height/2),scale(140),scale(28));
					lblCost.setBounds(xPos-scale(150),(int)(yPos+3*(height/2)),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),(int)(yPos+3*(height/2)),scale(140),scale(28));
					if(hasCost){
						lblVarUpdates.setBounds(xPos-scale(150), (int)(yPos+5*(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+5*(height/2)),scale(140),scale(28));
					}
					else{
						lblVarUpdates.setBounds(xPos-scale(150), (int)(yPos+3*(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+3*(height/2)),scale(140),scale(28));
					}
				}
				else{
					lblCost.setBounds(xPos-scale(150),yPos+(height/2),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),yPos+(height/2),scale(140),scale(28));
					if(hasCost){
						lblVarUpdates.setBounds(xPos-scale(150),(int)(yPos+3*(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+3*(height/2)),scale(140),scale(28));
					}
					else{
						lblVarUpdates.setBounds(xPos-scale(150),(int)(yPos+(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+(height/2)),scale(140),scale(28));
					}
				}
				//textEV.setBounds(xPos+scale(25),yPos-scale(5),scale(140),scale(28));
				int width=Math.max((int)(textEV.getText().length()*6),140);
				textEV.setBounds(xPos+scale(25),yPos-scale(5),scale(width),scale(28));
			}
			else if(type==2){ //Terminal
				Polygon triangle = new Polygon();
				triangle.addPoint(xPos, yPos+(height/2));
				triangle.addPoint(xPos+width,yPos+height);
				triangle.addPoint(xPos+width,yPos);
				g.setColor(Color.BLUE); //Fill color
				g.fillPolygon(triangle);
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2f));
				g.drawPolygon(triangle);
				if(myModel.alignRight==false){
					g.drawLine(parentX, parentY, xPos-scale(150), yPos+(height/2));
					g.drawLine(xPos-scale(150), yPos+(height/2), xPos, yPos+(height/2));
					textName.setBounds(xPos-scale(150), yPos+(height/2)-scale(28), scale(150), scale(28));
				}
				else{ //Align right
					g.drawLine(parentX, parentY, parentX+scale(15), yPos+(height/2));
					g.drawLine(parentX+scale(15), yPos+(height/2), xPos, yPos+(height/2));
					textName.setBounds(parentX+scale(15), yPos+(height/2)-scale(28), scale(150), scale(28));
				}
				int width=Math.max((int)(textPayoff.getText().length()*6),50);
				textPayoff.setBounds(xPos+scale(25),yPos+(height/2)-scale(14),scale(width),scale(28));
				if(parentType!=0){ //Not initial decision nodes
					if(myModel.alignRight==false){
						lblProb.setBounds(xPos-scale(150), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(xPos-scale(140), yPos+(height/2),scale(140),scale(28));
						lblVarUpdates.setBounds(xPos-scale(150), (int)(yPos+3*(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(xPos-scale(138),(int)(yPos+3*(height/2)),scale(140),scale(28));
					}
					else{ //Align right
						lblProb.setBounds(parentX+scale(15), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(parentX+scale(25), yPos+(height/2),scale(140),scale(28));
						lblVarUpdates.setBounds(parentX+scale(15), (int)(yPos+3*(height/2)),scale(15),scale(28));
						textVarUpdates.setBounds(parentX+scale(25),(int)(yPos+3*(height/2)),scale(140),scale(28));
					}
				}
				textNumEnd.setBounds(xPos+scale(25+width),yPos+(height/2)-scale(14),scale(100),scale(28));
			}
			if(level==1){ //Strategy
				int x=textName.getBounds().x;
				int y=textName.getBounds().y;
				textICER.setBounds(x, y-scale(20), scale(150), scale(28));
			}
			if(collapsed){
				lblCollapsed.setBounds(xPos+scale(25),yPos-scale(5),scale(15),scale(28));
			}
		}
	}

	public void setPanel(PanelTree panel){
		this.panel=panel;
		this.tree=panel.tree;
		this.myModel=panel.myModel;
	}

	public void updateDisplay(){
		displayName();
		displayVarUpdates();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){//Chance/Decision
			displayCost();
			displayEV();
		}
		else if(type==2){ //Terminal
			displayPayoff();
			displayNumEnd();
		}
		if(level==1){displayICER();}
		if(collapsed){displayCollapsed();}
		showNode(visible,null);
	}

	public void move(int xShift, int yShift, ArrayList<TreeNode> nodes){
		//Current node
		xPos+=xShift;
		yPos+=yShift;
		for(int i=0; i<childIndices.size(); i++){
			int index=childIndices.get(i);
			TreeNode child=nodes.get(index);
			child.parentX+=xShift;
			child.parentY+=yShift;
			child.move(xShift, yShift, nodes);
		}
	}

	private void displayName(){
		textName=new JTextField("Name");
		if(name!=null){textName.setText(name);}
		textName.setBorder(null);
		textName.setHorizontalAlignment(JTextField.CENTER);
		if(textHighlights[4]!=null){textName.setBackground(textHighlights[0]);}
		else{textName.setBackground(new Color(0,0,0,0));}
		scaleFont(textName);
		textName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempName=name; //Get existing name on field entry
				textName.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary()) {
					if(tempName!=null && !tempName.equals(textName.getText())){ //Text was changed
						myModel.saveSnapshot("Edit Name");//Add to undo stack
					}
					name=textName.getText();
					textName.setBorder(null);
				}
			}
		});
		textName.addKeyListener(new KeyAdapter(){ //Save text on Enter
			@Override
			public void keyPressed(KeyEvent e){
				int key=e.getKeyCode();
				if(key==KeyEvent.VK_ENTER){
					panel.requestFocusInWindow();
				}
			}
		});
	}

	private void displayProb(){
		lblProb=new JLabel("p:");
		lblProb.setForeground(Color.GRAY);
		scaleFont(lblProb);
		//Default
		textProb=new TreeTextField(panel, this, 0);
		if(textHighlights[0]!=null){textProb.setBackground(textHighlights[0]);}
		else{textProb.setBackground(new Color(0,0,0,0));}
		textProb.setText(prob);
		textProb.validateEntry();
		Font curFont=textProb.getFont();
		if(prob!=null && prob.matches("C")){textProb.setFont(curFont.deriveFont(curFont.ITALIC));}
		else{textProb.setFont(curFont.deriveFont(curFont.PLAIN));}
		textProb.setBorder(null);
		scaleFont(textProb);
		textProb.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempProb=prob; //Get existing value on field entry
				//panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
				panel.mainForm.btnFx.setEnabled(true); panel.mainForm.updateCurFx=false;
				panel.paneFormula.setText(textProb.getText());
				panel.curFocus=textProb;
				textProb.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textProb.validateEntry();
					textProb.updateHistory();
				}
			}
		});
		textProb.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textProb.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textProb.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}

	public void displayCost(){
		lblCost=new JLabel("c:");
		lblCost.setVisible(hasCost);
		lblCost.setForeground(Color.GRAY);
		scaleFont(lblCost);
		String buildString="";
		for(int i=0; i<numDimensions-1; i++){buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+cost[i]+"; ";}
		buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+cost[numDimensions-1];
		textCost=new TreeTextField(panel, this, 1);
		if(textHighlights[1]!=null){textCost.setBackground(textHighlights[1]);}
		else{textCost.setBackground(new Color(0,0,0,0));}
		textCost.setText(buildString);
		textCost.validateEntry();
		textCost.setVisible(hasCost);
		textCost.setBorder(null);
		//textCost.setForeground(Color.RED);
		scaleFont(textCost);
		textCost.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempCost=new String[numDimensions];
				for(int i=0; i<numDimensions; i++){tempCost[i]=cost[i];} //Get existing values on field entry
				//panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
				panel.mainForm.btnFx.setEnabled(true); panel.mainForm.updateCurFx=false;
				panel.paneFormula.setText(textCost.getText());
				panel.curFocus=textCost;
				textCost.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textCost.validateEntry();
					textCost.updateHistory();
				}
			}
		});
		textCost.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textCost.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textCost.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}

	public void displayPayoff(){
		String buildString="";
		for(int i=0; i<numDimensions-1; i++){buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+payoff[i]+"; ";}
		buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+payoff[numDimensions-1];
		textPayoff=new TreeTextField(panel, this, 2);
		if(textHighlights[2]!=null){textPayoff.setBackground(textHighlights[2]);}
		else{textPayoff.setBackground(new Color(0,0,0,0));}
		textPayoff.setText(buildString);
		textPayoff.validateEntry();
		textPayoff.setBorder(null);
		scaleFont(textPayoff);
		textPayoff.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempPayoff=new String[numDimensions];
				for(int i=0; i<numDimensions; i++){tempPayoff[i]=payoff[i];} //Get existing values on field entry
				//panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
				panel.mainForm.btnFx.setEnabled(true); panel.mainForm.updateCurFx=false;
				panel.paneFormula.setText(textPayoff.getText());
				panel.curFocus=textPayoff;
				textPayoff.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textPayoff.validateEntry();
					textPayoff.updateHistory();
				}
			}
		});
		textPayoff.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textPayoff.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textPayoff.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}

	public void displayVarUpdates(){
		lblVarUpdates=new JLabel("V:");
		lblVarUpdates.setVisible(hasVarUpdates);
		lblVarUpdates.setForeground(Color.GRAY);
		scaleFont(lblVarUpdates);
		textVarUpdates=new TreeTextField(panel, this, 3);
		if(textHighlights[3]!=null){textVarUpdates.setBackground(textHighlights[3]);}
		else{textVarUpdates.setBackground(new Color(0,0,0,0));}
		textVarUpdates.setText(varUpdates);
		textVarUpdates.validateEntry();
		textVarUpdates.setVisible(hasVarUpdates);
		textVarUpdates.setBorder(null);
		scaleFont(textVarUpdates);
		textVarUpdates.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempVarUpdates=varUpdates; //Get existing value on field entry
				panel.paneFormula.setEditable(true);
				panel.mainForm.btnFx.setEnabled(true); panel.mainForm.updateCurFx=true;
				panel.paneFormula.setText(textVarUpdates.getText());
				panel.curFocus=textVarUpdates;
				textVarUpdates.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textVarUpdates.validateEntry();
					textVarUpdates.updateHistory();
				}
			}
		});
		textVarUpdates.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textVarUpdates.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textVarUpdates.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}
	
	public void displayEV(){
		textEV=new JTextField();
		textEV.setBackground(new Color(0,0,0,0));
		textEV.setBorder(null);
		textEV.setEditable(false);
		textEV.setVisible(tree.showEV);
		scaleFont(textEV);
		textEV.setFont(new Font(textEV.getFont().getFontName(), Font.BOLD, textEV.getFont().getSize()));
		if(tree.showEV){
			String buildString="";
			for(int i=0; i<numDimensions-1; i++){
				buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
			}
			buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+MathUtils.round(expectedValues[numDimensions-1],myModel.dimInfo.decimals[numDimensions-1]);
			textEV.setText(buildString);
		}
	}
	
	public void displayNumEnd(){
		textNumEnd=new JTextField();
		textNumEnd.setBackground(new Color(0,0,0,0));
		textNumEnd.setHorizontalAlignment(JTextField.LEFT);
		textNumEnd.setBorder(null);
		textNumEnd.setEditable(false);
		textNumEnd.setVisible(tree.showEV);
		scaleFont(textNumEnd);
		textNumEnd.setFont(new Font(textNumEnd.getFont().getFontName(), Font.BOLD, textNumEnd.getFont().getSize()));
		textNumEnd.setForeground(new Color(0,0,139));
		if(tree.showEV){
			textNumEnd.setText(Math.round(totalDenom*100)/100.0+"");
		}
	}
	
	public void displayICER(){
		textICER=new JTextField();
		textICER.setBackground(new Color(0,0,0,0));
		textICER.setHorizontalAlignment(JTextField.CENTER);
		textICER.setBorder(null);
		textICER.setEditable(false);
		boolean showICER=(tree.showEV && myModel.dimInfo.analysisType>0);
		textICER.setVisible(showICER);
		scaleFont(textICER);
		textICER.setFont(new Font(textICER.getFont().getFontName(), Font.BOLD, textICER.getFont().getSize()));
		if(showICER){
			textICER.setText(icer);
		}
	}

	/**
	 * Creates a deep copy of current node attributes except child indices
	 * @return New object that is a copy of the current node
	 */
	public TreeNode copy(){
		TreeNode node=new TreeNode();
		//Copy data
		node.type=type;
		node.name=name; 
		node.xPos=xPos; node.yPos=yPos;
		node.width=width; node.height=height;
		node.parentX=parentX; node.parentY=parentY;
		node.parentType=parentType;
		node.level=level;
		node.prob=prob;
		node.numDimensions=numDimensions;
		node.varUpdates=varUpdates;
		node.cost=new String[numDimensions];
		node.payoff=new String[numDimensions];
		node.expectedValues=new double[numDimensions];
		for(int i=0; i<numDimensions; i++){
			node.cost[i]=cost[i];
			node.payoff[i]=payoff[i];
		}
		node.notes=notes;
		//Copy display variables
		node.selected=selected;
		node.visible=visible;
		node.hasVarUpdates=hasVarUpdates;
		node.hasCost=hasCost;
		node.collapsed=collapsed;
		node.curScale=curScale;
		//Pass by reference
		node.panel=panel;
		node.tree=tree;
		node.myModel=myModel;

		return(node);
	}


	public void highlightTextField(int fieldIndex, Color color){
		if(textHighlights==null){textHighlights=new Color[]{null,null,null,null,null};}
		textHighlights[fieldIndex]=color;
		JTextField curField=null;
		if(fieldIndex==0){curField=textProb;}
		else if(fieldIndex==1){curField=textCost;}
		else if(fieldIndex==2){curField=textPayoff;}
		else if(fieldIndex==3){curField=textVarUpdates;}
		else if(fieldIndex==4){curField=textName;}
		if(curField!=null){
			if(color==null){curField.setBackground(new Color(0,0,0,0));}
			else{curField.setBackground(color);}
		}
	}

	public void showNode(boolean show, ArrayList<TreeNode> nodes){
		visible=show;
		showComponent(show,textName);
		showComponent(show,textPayoff);
		showComponent(show,lblProb); showComponent(show,textProb);
		if(visible){
			if(hasCost){showComponent(show,lblCost); showComponent(show,textCost);}
			if(hasVarUpdates){showComponent(show,lblVarUpdates); showComponent(show,textVarUpdates);}
		}
		else{//Always hide cost
			showComponent(show,lblCost); showComponent(show,textCost);
			showComponent(false,lblVarUpdates); showComponent(false,textVarUpdates);
		}
		showComponent(visible && tree.showEV,textEV); 
		showComponent(visible && tree.showEV,textNumEnd);
		showComponent(false,textICER); //Hide ICER
		if(collapsed==true){
			if(show==false){panel.remove(lblCollapsed);} //remove collapsed label
			else{panel.add(lblCollapsed);} //add collapsed label
		}
		
		if(nodes!=null){ //apply to children
			if(collapsed==true){show=false;}
			for(int i=0; i<childIndices.size(); i++){
				int index=childIndices.get(i);
				TreeNode child=nodes.get(index);
				child.showNode(show, nodes);
			}
		}
	}

	public void addRemoveCost(){
		hasCost=!hasCost;
		showComponent(hasCost,lblCost); showComponent(hasCost,textCost);
		if(hasCost==false){ //Remove all costs
			Arrays.fill(cost, "0");
		}
		else{ //Update
			String buildString="";
			for(int i=0; i<numDimensions-1; i++){buildString+="("+myModel.dimInfo.dimSymbols[i]+")"+cost[i]+"; ";}
			buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+")"+cost[numDimensions-1];
			textCost.setText(buildString);
		}
	}
	
	public void addRemoveVarUpdates(){
		hasVarUpdates=!hasVarUpdates;
		showComponent(hasVarUpdates,lblVarUpdates); showComponent(hasVarUpdates,textVarUpdates);
		if(hasVarUpdates==false){ //Remove updates
			varUpdates=null;
		}
		else{ //Update
			textVarUpdates.setText(varUpdates);
		}
	}
	
	public void setThreads(int numThreads, int numDim, int numSubgroups){
		this.numThreads=numThreads;
		this.numDim=numDim;
		this.numSubgroups=numSubgroups;
		curProb=new double[numThreads];
		if(type==1){curChildProbs=new double[numThreads][numChildren];}
		nTotalDenom=new double[numThreads];
		nTotalDenomGroup=new double[numThreads][numSubgroups];
		nTotalCosts=new double[numThreads][numDim];
		nTotalCostsGroup=new double[numThreads][numSubgroups][numDim];
		nTotalPayoffs=new double[numThreads][numDim];
		nTotalPayoffsGroup=new double[numThreads][numSubgroups][numDim];
	}
	
	public void sumThreads(){
		totalDenom=0;
		totalDenomGroup=new double[numSubgroups];
		totalCosts=new double[numDim];
		totalCostsGroup=new double[numSubgroups][numDim];
		totalPayoffs=new double[numDim];
		totalPayoffsGroup=new double[numSubgroups][numDim];
		for(int n=0; n<numThreads; n++){
			totalDenom+=nTotalDenom[n];
			for(int d=0; d<numDim; d++){
				totalCosts[d]+=nTotalCosts[n][d];
				totalPayoffs[d]+=nTotalPayoffs[n][d];
			}
			for(int g=0; g<numSubgroups; g++){
				totalDenomGroup[g]+=nTotalDenomGroup[n][g];
				for(int d=0; d<numDim; d++){
					totalCostsGroup[g][d]+=nTotalCostsGroup[n][g][d];
					totalPayoffsGroup[g][d]+=nTotalPayoffsGroup[n][g][d];
				}
			}
			
		}
	}
}