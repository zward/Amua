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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="node")
public class TreeNode{
	//Data
	/**
	 * Type of node.  0=Decision, 1=Chance, 2=Terminal
	 */
	@XmlElement public int type;
	@XmlElement public String name;
	@XmlElement int xPos,yPos;
	@XmlElement int width,height;
	@XmlElement int parentX,parentY;
	@XmlElement public int parentType;
	@XmlElement public ArrayList<Integer> childIndices;
	@XmlElement int level=-1;
	@XmlElement public String prob, cost[], payoff[];
	@XmlElement boolean hasCost=false;
	@XmlElement String notes;
	public int numDimensions=1;

	//Numeric data - parsed/calculated
	double curProb; //Current probability used to run the model - not saved
	double curCosts[], curPayoffs[];
	double expectedValues[];

	//Visual Attributes
	PanelTree panel;
	DecisionTree tree;
	boolean selected;
	JTextField textName;
	JLabel lblProb; TreeTextField textProb;
	JLabel lblCost; TreeTextField textCost;
	TreeTextField textPayoff;
	JTextField textEV;
	boolean visible=true;
	boolean collapsed=false;
	JLabel lblCollapsed;
	int curScale=100;
	public String nameExport; //Normalized name for export to another language
	Color textHighlights[];

	//Store original text on focus to see if it has change on lost focus
	String tempName, tempProb, tempCost[], tempPayoff[], tempNotes;

	Border defaultBorder=BorderFactory.createMatteBorder(1,3,1,1, Color.BLUE);

	//Constructor
	public TreeNode(int type1, TreeNode parent){
		panel=parent.panel;
		tree=parent.tree;
		type=type1;
		width=parent.width; height=parent.height;
		parentX=parent.xPos+parent.width; //Right
		parentY=parent.yPos+(parent.height/2); //Middle
		curScale=parent.curScale;
		xPos=parentX+scale(150); yPos=parent.yPos;
		selected=true;
		parentType=parent.type;
		childIndices=new ArrayList<Integer>();
		numDimensions=parent.numDimensions;
		cost=new String[numDimensions]; Arrays.fill(cost,"0");
		payoff=new String[numDimensions]; Arrays.fill(payoff,"0");
		expectedValues=new double[numDimensions];
		prob="0";
		level=parent.level+1;
		textHighlights=new Color[]{null,null,null};

		displayName();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){//Chance/Decision
			displayCost();
			displayEV();
		}
		else if(type==2){ //Terminal
			displayPayoff();
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
		textHighlights=new Color[]{null,null,null};
	}

	public TreeNode(){ //No argument constructor - initialize display fields with XML marshalling
		if(childIndices==null){childIndices=new ArrayList<Integer>();}
		textHighlights=new Color[]{null,null,null};
	}

	public int scale(int orig){
		double scale=curScale/100.0;
		int scaled=(int)(orig*scale);
		return(scaled);
	}

	private float scaleF(float orig){
		double scale=curScale/100.0;
		float scaled=(float) (orig*scale);
		return(scaled);
	}

	private void scaleFont(JComponent curComp){
		Font curFont=curComp.getFont();
		curComp.setFont(curFont.deriveFont(scaleF(curFont.getSize())));
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
						lblCost.setBounds(xPos-scale(150),(int)(yPos+2.5*(height/2)),scale(12),scale(28));
						textCost.setBounds(xPos-scale(140),(int)(yPos+2.5*(height/2)),scale(140),scale(28));
					}
					else{
						lblCost.setBounds(xPos-scale(150),yPos+(height/2),scale(12),scale(28));
						textCost.setBounds(xPos-scale(140),yPos+(height/2),scale(140),scale(28));
					}
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
					lblCost.setBounds(xPos-scale(150),(int)(yPos+2.5*(height/2)),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),(int)(yPos+2.5*(height/2)),scale(140),scale(28));
				}
				else{
					lblCost.setBounds(xPos-scale(150),yPos+(height/2),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),yPos+(height/2),scale(140),scale(28));
				}
				textEV.setBounds(xPos+scale(25),yPos-scale(5),scale(140),scale(28));
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
				if(tree.alignRight==false){
					g.drawLine(parentX, parentY, xPos-scale(150), yPos+(height/2));
					g.drawLine(xPos-scale(150), yPos+(height/2), xPos, yPos+(height/2));
					textName.setBounds(xPos-scale(150), yPos+(height/2)-scale(28), scale(150), scale(28));
				}
				else{ //Align right
					g.drawLine(parentX, parentY, parentX+scale(15), yPos+(height/2));
					g.drawLine(parentX+scale(15), yPos+(height/2), xPos, yPos+(height/2));
					textName.setBounds(parentX+scale(15), yPos+(height/2)-scale(28), scale(150), scale(28));
				}
				textPayoff.setBounds(xPos+scale(25),yPos+(height/2)-scale(14),scale(100),scale(28));
				if(parentType!=0){ //Not initial decision nodes
					if(tree.alignRight==false){
						lblProb.setBounds(xPos-scale(150), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(xPos-scale(140), yPos+(height/2),scale(140),scale(28));
					}
					else{ //Align right
						lblProb.setBounds(parentX+scale(15), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(parentX+scale(25), yPos+(height/2),scale(140),scale(28));
					}
				}
			}
			if(collapsed){
				lblCollapsed.setBounds(xPos+scale(25),yPos-scale(5),scale(15),scale(28));
			}
		}
	}

	public void setPanel(PanelTree panel){
		this.panel=panel;
		this.tree=panel.tree;
	}

	public void updateDisplay(){
		displayName();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){//Chance/Decision
			displayCost();
			displayEV();
		}
		else if(type==2){ //Terminal
			displayPayoff();
		}
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
		textName.setBackground(new Color(0,0,0,0));
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
						panel.saveSnapshot("Edit Name");//Add to undo stack
					}
					name=textName.getText();
					textName.setBorder(null);
				}
			}
		});
	}

	private void displayProb(){
		lblProb=new JLabel("p:");
		scaleFont(lblProb);
		//Default
		textProb=new TreeTextField(panel, this, 0);
		if(textHighlights[0]!=null){textProb.setBackground(textHighlights[0]);}
		else{textProb.setBackground(new Color(0,0,0,0));}
		textProb.setText(prob);
		textProb.validateEntry();
		Font curFont=textProb.getFont();
		if(prob.matches("C")){textProb.setFont(curFont.deriveFont(curFont.ITALIC));}
		else{textProb.setFont(curFont.deriveFont(curFont.PLAIN));}
		textProb.setBorder(null);
		scaleFont(textProb);
		textProb.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempProb=prob; //Get existing value on field entry
				panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
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
		scaleFont(lblCost);
		String buildString="";
		for(int i=0; i<numDimensions-1; i++){buildString+="("+tree.dimSymbols[i]+") "+cost[i]+"; ";}
		buildString+="("+tree.dimSymbols[numDimensions-1]+") "+cost[numDimensions-1];
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
				panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
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
		for(int i=0; i<numDimensions-1; i++){buildString+="("+tree.dimSymbols[i]+") "+payoff[i]+"; ";}
		buildString+="("+tree.dimSymbols[numDimensions-1]+") "+payoff[numDimensions-1];
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
				panel.mainForm.tabbedPaneBottom.setSelectedIndex(1);
				panel.paneFormula.setEditable(true);
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
				buildString+="("+tree.dimSymbols[i]+") "+tree.round(expectedValues[i])+"; ";
			}
			buildString+="("+tree.dimSymbols[numDimensions-1]+") "+tree.round(expectedValues[numDimensions-1]);
			textEV.setText(buildString);
		}
	}

	public void displayCollapsed(){
		lblCollapsed=new JLabel("+");
		lblCollapsed.setFont(new Font("SansSerif", Font.PLAIN, scale(20)));
		lblCollapsed.setForeground(Color.DARK_GRAY);
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
		node.hasCost=hasCost;
		node.collapsed=collapsed;
		node.curScale=curScale;
		//Pass by reference
		node.panel=panel;
		node.tree=tree;

		return(node);
	}


	public void highlightTextField(int fieldIndex, Color color){
		if(textHighlights==null){textHighlights=new Color[]{null,null,null};}
		textHighlights[fieldIndex]=color;
		JTextField curField=null;
		if(fieldIndex==0){curField=textProb;}
		else if(fieldIndex==1){curField=textCost;}
		else if(fieldIndex==2){curField=textPayoff;}
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
		}
		else{//Always hide cost
			showComponent(show,lblCost); showComponent(show,textCost);
		}
		showComponent(false,textEV); //Always hide EV
		if(show==false && collapsed==true){ //Overwrite collapsed
			collapsed=false;
			panel.remove(lblCollapsed);
		}
		if(nodes!=null){
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
			for(int i=0; i<numDimensions-1; i++){buildString+="("+tree.dimSymbols[i]+")"+cost[i]+"; ";}
			buildString+="("+tree.dimSymbols[numDimensions-1]+")"+cost[numDimensions-1];
			textCost.setText(buildString);
		}
	}

	private void showComponent(boolean show, JComponent curObject){
		if(curObject!=null){curObject.setVisible(show);}
	}

	public void setPlainFont(JTextField curField){
		Font curFont=curField.getFont();
		curField.setFont(curFont.deriveFont(curFont.PLAIN));
	}

	public void setItalicFont(JTextField curField){
		Font curFont=curField.getFont();
		curField.setFont(curFont.deriveFont(curFont.ITALIC));
	}

	public String[] parseDimensions(String text){
		String dims[]=new String[numDimensions];
		for(int i=0; i<numDimensions-1; i++){
			String curSymbol="("+tree.dimSymbols[i]+")";
			int index=text.indexOf(curSymbol);
			String curText=text.substring(index+curSymbol.length());
			index=curText.indexOf(";");
			curText=curText.substring(0, index);
			curText=curText.replaceAll(" ",""); //Remove any spaces
			dims[i]=curText;
		}
		//Get last dimension
		String curSymbol="("+tree.dimSymbols[numDimensions-1]+")";
		int index=text.indexOf(curSymbol);
		String curText=text.substring(index+curSymbol.length());
		curText=curText.replaceAll(" ",""); //Remove any spaces
		curText=curText.replaceAll(";",""); //Remove ; if present
		dims[numDimensions-1]=curText;

		return(dims);
	}

	public boolean validateText(String text){
		boolean valid=true;
		try{
			double test=panel.varHelper.evaluateExpression(text);
			if(Double.isNaN(test)){valid=false;}
		}catch(Exception e){
			valid=false;
		}
		return(valid);
	}

	public double evaluateExpression(String text){
		return(panel.varHelper.evaluateExpression(text));
	}

	class FocusGrabber implements Runnable {
		private JComponent component;

		public FocusGrabber(JComponent component) {
			this.component = component;
		}
		public void run() {
			component.grabFocus();
		}
	} 
}