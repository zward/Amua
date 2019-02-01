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

package markov;
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
import java.util.Comparator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.ModelNode;
import main.VariableUpdate;
import math.MathUtils;

@XmlRootElement(name="node")
public class MarkovNode extends ModelNode{
	//Model-specific data
	@XmlElement public String prob, cost[], transition, rewards[];
	@XmlElement public String terminationCondition;
	//chain elements
	@XmlElement public ArrayList<String> stateNames;
		
	//Cur structure
	@XmlTransient public int numChildren;
	@XmlTransient public MarkovNode children[];
	//Numeric data - parsed/calculated
	@XmlTransient double curProb; //Current probability used to run the model - not saved
	@XmlTransient double curCosts[];
	@XmlTransient int transFrom, transTo; //Index of cur state and next state
	@XmlTransient public double expectedValues[], expectedValuesDis[]; //For each chain
	@XmlTransient double curRewards[];
	@XmlTransient double curChildProbs[]; //cumulative
	@XmlTransient public VariableUpdate curVariableUpdates[];
	@XmlTransient boolean probHasVariables, childHasProbVariables;
	@XmlTransient boolean costHasVariables[];
	@XmlTransient boolean rewardHasVariables[];
	
	//Visual Attributes
	@XmlTransient PanelMarkov panel;
	@XmlTransient MarkovTree tree;
	@XmlTransient public MarkovNode chain;
	@XmlTransient JLabel lblProb; MarkovTextField textProb;
	@XmlTransient JLabel lblCost; MarkovTextField textCost;
	@XmlTransient JLabel lblVarUpdates; MarkovTextField textVarUpdates;
	@XmlTransient JLabel lblRewards; MarkovTextField textRewards;
	@XmlTransient JComboBox<String> comboTransition;
	@XmlTransient MarkovTextField textTermination;
	@XmlTransient
	public JTextField textEV;
	@XmlTransient
	JTextField textICER;
	@XmlTransient String icer;
	
	//Store original text on focus to see if it has change on lost focus
	@XmlTransient String tempName, tempProb, tempCost[], tempTransition, tempNotes, tempTermination, tempRewards[], tempVarUpdates;

	//Constructor
	public MarkovNode(int type1, MarkovNode parent){
		myModel=parent.myModel;
		numDimensions=myModel.dimInfo.dimNames.length;
		panel=parent.panel;
		tree=parent.tree;
		chain=parent.chain; //pass pointer
		type=type1;
		width=parent.width; height=parent.height;
		if(parent.type==2){ //Parent is Markov state
			width/=6;
			height/=2;
		}
		if(type==1){ //Markov chain
			chain=this;
			width*=1.5;
			height*=1.5;
			stateNames=new ArrayList<String>();
			terminationCondition="[termination]";
		}
		else if(type==2){ //Markov state
			width*=4; //inherits width 1.5 and height 1.5 from parent Markov chain
			height*=(4.0/3.0);
			rewards=new String[numDimensions]; Arrays.fill(rewards,"0");
			//initialize state name
			if(name==null){
				String testName="State 1";
				int num=1;
				while(chain.stateNames.contains(testName)){
					num++;
					testName="State "+num;
				}
				name=testName;
				chain.stateNames.add(name);
			}
		}
		parentX=parent.xPos+parent.width; //Right
		parentY=parent.yPos+(parent.height/2); //Middle
		curScale=parent.curScale;
		xPos=parentX+scale(150); yPos=parent.yPos;
		if(type==2){
			xPos=parentX+scale(40);
		}
		selected=true;
		parentType=parent.type;
		childIndices=new ArrayList<Integer>();
		cost=new String[numDimensions]; Arrays.fill(cost,"0");
		if(type==1){ //chain
			expectedValues=new double[numDimensions];
			expectedValuesDis=new double[numDimensions];
		}
		prob="0";
		level=parent.level+1;
		textHighlights=new Color[]{null,null,null,null,null,null};

		displayName();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){//Not state
			displayCost();
		}
		if(type==1){ //Chain
			displayTermination();
			displayEV();
			displayVarUpdates();
		}
		else{ //not Chain
			displayVarUpdates();
		}
		
		if(type==2){ //State
			displayRewards();
		}
		if(type==4){ //Transition
			displayTransition();
		}
		if(level==1){
			displayICER();
		}
	}

	//Overloaded constructor for root
	public MarkovNode(int x, int y, int width1, int height1, int numDims, MarkovTree tree){
		this.tree=tree;
		type=0;
		xPos=x;	yPos=y;
		width=width1; height=height1;
		childIndices=new ArrayList<Integer>();
		numDimensions=numDims;
		cost=new String[numDimensions]; Arrays.fill(cost,"0");
		level=0;
		name="Root";
		textHighlights=new Color[]{null,null,null,null,null,null};
	}

	public MarkovNode(){ //No argument constructor - initialize display fields with XML marshalling
		if(childIndices==null){childIndices=new ArrayList<Integer>();}
		textHighlights=new Color[]{null,null,null,null,null,null};
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
				}
			}
			else if(type==1){ //Chain
				Graphics2D g2=(Graphics2D) g;
				//draw outline
				g2.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				g2.drawRoundRect(xPos, yPos, width, height, (int)(width/3), (int)(height/3));
				//transitions
				g2.setStroke(new BasicStroke(1f));
				g2.setColor(Color.BLACK);
				g2.drawArc(xPos+(int)(width*0.25), yPos+(int)(height*0.225), (int)(width*0.5), (int)(height), 0, 180);
				g2.drawArc(xPos+(int)(width*0.25), yPos+(int)(height*0.575), (int)(width*0.5), (int)(height*0.3), 180, 180);
				//fill
				g2.setColor(Color.ORANGE);
				if(collapsed){g2.setColor(Color.LIGHT_GRAY);}
				g2.fillOval(xPos+(int)(width*0.3),yPos+(int)(height*0.1),(int)(width/2.5),height/4); //top circle
				g2.fillOval(xPos+(int)(width*0.05), yPos+(int)(height*0.6), (int)(width/2.5), height/4); //bottom-left circle
				g2.fillOval(xPos+(int)(width*0.6)-(int)(width*0.05), yPos+(int)(height*0.6), (int)(width/2.5), height/4); //bottom-right circle
				//outlines
				g2.setColor(Color.BLACK);
				g2.drawOval(xPos+(int)(width*0.3),yPos+(int)(height*0.1),(int)(width/2.5),height/4); //top circle
				g2.drawOval(xPos+(int)(width*0.05), yPos+(int)(height*0.6), (int)(width/2.5), height/4); //bottom-left circle
				g2.drawOval(xPos+(int)(width*0.6)-(int)(width*0.05), yPos+(int)(height*0.6), (int)(width/2.5), height/4); //bottom-right circle
				
				//Draw line to parent
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				g2.setStroke(new BasicStroke(2f));
				g.drawLine(parentX, parentY, xPos-scale(150), yPos+(height/2));
				g.drawLine(xPos-scale(150), yPos+(height/2), xPos, yPos+(height/2));
				//Add attributes
				textName.setBounds(xPos-scale(150), yPos+(height/2)-scale(28), scale(150), scale(28));
				textTermination.setBounds(xPos+width-scale(100),yPos+height,scale(100),scale(28));
				
				if(parentType!=0){ //Not decisions
					lblProb.setBounds(xPos-scale(150), yPos+(height/2),scale(12),scale(28));
					textProb.setBounds(xPos-scale(140), yPos+(height/2),scale(140),scale(28));
					lblCost.setBounds(xPos-scale(150),(int)(yPos+2.5*(height/2)),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),(int)(yPos+2.5*(height/2)),scale(140),scale(28));
					lblVarUpdates.setBounds(xPos+width-scale(112), (int)(yPos+5*(height/2)),scale(15),scale(28));
					textVarUpdates.setBounds(xPos+width-scale(100),(int)(yPos+5*(height/2)),scale(100),scale(28));
				}
				else{
					lblCost.setBounds(xPos-scale(150),yPos+(height/2),scale(12),scale(28));
					textCost.setBounds(xPos-scale(140),yPos+(height/2),scale(140),scale(28));
					lblVarUpdates.setBounds(xPos+width-scale(112),(int)(yPos+3.5*(height/2)),scale(15),scale(28));
					textVarUpdates.setBounds(xPos+width-scale(100),(int)(yPos+3.5*(height/2)),scale(100),scale(28));
				}
				textEV.setBounds(xPos-scale(150),yPos+(height/2)-scale(48),scale(150),scale(28));
			}
			else if(type==2){ //State
				g.setColor(Color.ORANGE);
				if(collapsed){g.setColor(Color.LIGHT_GRAY);}
				g.fillOval(xPos, yPos, width, height);
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2f));
				g.drawOval(xPos,yPos,width,height);  
				//Draw line to parent
				g.drawLine(parentX, parentY, xPos-scale(40), yPos+(height/2));
				g.drawLine(xPos-scale(40), yPos+(height/2), xPos, yPos+(height/2));
				//Add attributes
				textName.setBounds(xPos, yPos+(height/2)-scale(8), width, scale(16));
				lblProb.setBounds(xPos-scale(40), yPos+(height/2),scale(12),scale(28));
				textProb.setBounds(xPos-scale(30), yPos+(height/2),scale(40),scale(28));
				lblRewards.setBounds(xPos-scale(43), yPos+height,scale(15),scale(28));
				textRewards.setBounds(xPos-scale(30), yPos+height,scale(80),scale(28));
				lblVarUpdates.setBounds(xPos-scale(43),(int)(yPos+1.5*height),scale(15),scale(28));
				textVarUpdates.setBounds(xPos-scale(30),(int)(yPos+1.5*height),scale(140),scale(28));
			}
			else if(type==3){ //Chance
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
			}
			else if(type==4){ //Transition
				Polygon arrow = new Polygon();
				double midH=yPos+height/2.0;
				arrow.addPoint(xPos, (int) (midH+0.15*height));
				arrow.addPoint((int) (xPos+width*0.55), (int) (midH+0.15*height));
				arrow.addPoint((int) (xPos+width*0.55), (int) (midH+0.30*height));
				arrow.addPoint(xPos+width, (int) midH); //point
				arrow.addPoint((int) (xPos+width*0.55), (int) (midH-0.30*height));
				arrow.addPoint((int) (xPos+width*0.55), (int) (midH-0.15*height));
				arrow.addPoint(xPos, (int) (midH-0.15*height));
				g.setColor(Color.BLUE); //Fill color
				g.fillPolygon(arrow);
				g.setColor(Color.BLACK);
				if(selected==true){g.setColor(Color.RED);}
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2f));
				g.drawPolygon(arrow);
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
				int width=75;
				if(comboTransition.getSelectedIndex()!=-1){
					width=Math.max((int)(comboTransition.getSelectedItem().toString().length()*10),100);
				}
				comboTransition.setBounds(xPos+scale(25),yPos+(height/2)-scale(14),scale(width),scale(28));
				if(parentType!=0){ //Not initial decision nodes
					if(myModel.alignRight==false){
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
					else{ //Align right
						lblProb.setBounds(parentX+scale(15), yPos+(height/2),scale(12),scale(28));
						textProb.setBounds(parentX+scale(25), yPos+(height/2),scale(140),scale(28));
						lblCost.setBounds(parentX+scale(15),(int)(yPos+3*(height/2)),scale(12),scale(28));
						textCost.setBounds(parentX+scale(25),(int)(yPos+3*(height/2)),scale(140),scale(28));
						if(hasCost){
							lblVarUpdates.setBounds(parentX+scale(15), (int)(yPos+5*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(parentX+scale(25),(int)(yPos+5*(height/2)),scale(140),scale(28));
						}
						else{
							lblVarUpdates.setBounds(parentX+scale(15), (int)(yPos+3*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(parentX+scale(25),(int)(yPos+3*(height/2)),scale(140),scale(28));
						}
					}
				}
				else{
					if(myModel.alignRight==false){
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
					else{
						lblCost.setBounds(parentX+scale(15),yPos+(height/2),scale(12),scale(28));
						textCost.setBounds(parentX+scale(25),yPos+(height/2),scale(140),scale(28));
						if(hasCost){
							lblVarUpdates.setBounds(parentX+scale(15),(int)(yPos+3*(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(parentX+scale(25),(int)(yPos+3*(height/2)),scale(140),scale(28));
						}
						else{
							lblVarUpdates.setBounds(parentX+scale(15),(int)(yPos+(height/2)),scale(15),scale(28));
							textVarUpdates.setBounds(parentX+scale(25),(int)(yPos+(height/2)),scale(140),scale(28));
						}
					}
				}
			}
			if(level==1){ //Strategy
				int x=textName.getBounds().x;
				int y=textName.getBounds().y;
				textICER.setBounds(x, y-scale(20), scale(150), scale(28));
			}
			if(collapsed){
				lblCollapsed.setBounds(xPos+width+scale(5),yPos+height/2-scale(14),scale(15),scale(28));
			}
		}
	}

	public void setPanel(PanelMarkov panel){
		this.panel=panel;
		this.tree=panel.tree;
		this.myModel=panel.myModel;
	}

	public void updateDisplay(){
		displayName();
		if(parentType!=0){ //Not decision
			displayProb();
		}
		if(type!=2){ //Not state
			displayCost();
		}
		if(type==1){ //Chain
			displayTermination();
			displayEV();
			displayVarUpdates();
		}
		else{ //not Chain
			displayVarUpdates();
		}
		if(type==2){ //State
			displayRewards();
		}
		if(type==4){ //Transition
			displayTransition();
		}
		if(level==1){displayICER();}
		if(collapsed){displayCollapsed();}
		showNode(visible,null);
	}

	public void move(int xShift, int yShift, ArrayList<MarkovNode> nodes){
		//Current node
		xPos+=xShift;
		yPos+=yShift;
		for(int i=0; i<childIndices.size(); i++){
			int index=childIndices.get(i);
			MarkovNode child=nodes.get(index);
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
		if(textHighlights[5]!=null){textName.setBackground(textHighlights[5]);}
		else{textName.setBackground(new Color(0,0,0,0));}
		if(type==2){ //State
			Font curFont=textName.getFont();
			textName.setFont(curFont.deriveFont(curFont.BOLD));
		}
		
		scaleFont(textName);
		textName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempName=name; //Get existing name on field entry
				textName.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary()) {
					String checkName=textName.getText();
					updateName(checkName);
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
	
	public void updateTermination(String checkCondition){
		if(!checkCondition.equals(terminationCondition)){ //condition was changed
			if(tempTermination!=null){
				panel.saveSnapshot("Edit Termination Condition");//Add to undo stack
			}
			terminationCondition=checkCondition;
			textTermination.setText(terminationCondition);
			
			if(panel.modelProperties.getRowCount()>0){ //visible
				panel.modelProperties.setValueAt(terminationCondition, 1, 1);
			}
		}
	}
	
	public void updateName(String checkName){
		if(tempName==null){ //first edit
			if(type==2){ //Validate state name
				if(chain.stateNames.contains(checkName)){ //duplicate name
					JOptionPane.showMessageDialog(panel, "Duplicate state name!");
					name=tempName;
				}
				else{ //unique
					chain.stateNames.add(checkName);
					name=checkName;
				}
			}
			else{ //not State, update name
				name=checkName;
			}
		}
		else if(tempName!=null && !tempName.equals(textName.getText())){ //Text was changed
			if(type==2){ //Validate state name
				textName.setForeground(Color.BLACK);
				if(chain.stateNames.contains(checkName)){ //duplicate name
					JOptionPane.showMessageDialog(panel, "Duplicate state name!");
					name=tempName;
				}
				else{ //unique
					panel.saveSnapshot("Edit Name");//Add to undo stack
					int prevIndex=chain.stateNames.indexOf(tempName);
					if(prevIndex!=-1){chain.stateNames.remove(prevIndex);} //remove previous name
					chain.stateNames.add(checkName);
					name=checkName;
				}
			}
			else{ //not State, update name
				panel.saveSnapshot("Edit Name");//Add to undo stack
				name=checkName;
			}
		}
		
		textName.setText(name);
		if(type==1 || type==2){ //Markov node, update properties tab
			if(panel.modelProperties.getRowCount()>0){ //visible
				panel.modelProperties.setValueAt(name, 0, 1);
			}
		}
	}

	private void displayProb(){
		lblProb=new JLabel("p:");
		lblProb.setForeground(Color.GRAY);
		scaleFont(lblProb);
		//Default
		textProb=new MarkovTextField(panel, this, 0);
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
				panel.paneFormula.setEditable(true);
				panel.paneFormula.setText(textProb.getText());
				panel.curFocus=textProb;
				textProb.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textProb.validateEntry();
					textProb.updateHistory();
					if(type==2 && panel.modelProperties.getRowCount()>0){ //visible
						panel.modelProperties.setValueAt(prob, 1, 1);
					}
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
	
	public void displayTermination(){
		textTermination=new MarkovTextField(panel,this,2);
		textTermination.setHorizontalAlignment(SwingConstants.RIGHT);
		if(textHighlights[2]!=null){textTermination.setBackground(textHighlights[2]);}
		else{textTermination.setBackground(new Color(0,0,0,0));}
		textTermination.setText(terminationCondition);
		textTermination.validateEntry();
		Font curFont=textTermination.getFont();
		textTermination.setFont(curFont.deriveFont(curFont.ITALIC));
		textTermination.setBorder(null);
		scaleFont(textTermination);
		textTermination.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempTermination=terminationCondition; //Get existing value on field entry
				panel.paneFormula.setEditable(true);
				panel.paneFormula.setText(textTermination.getText());
				panel.curFocus=textTermination;
				textTermination.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textTermination.validateEntry();
					textTermination.updateHistory();
					if(type==1 && panel.modelProperties.getRowCount()>0){ //visible
						panel.modelProperties.setValueAt(terminationCondition, 1, 1);
					}
				}
			}
		});
		textTermination.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textTermination.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textTermination.getText());}
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
		textCost=new MarkovTextField(panel, this, 1);
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
	
	public void displayVarUpdates(){
		lblVarUpdates=new JLabel("V:");
		lblVarUpdates.setVisible(hasVarUpdates);
		lblVarUpdates.setForeground(Color.GRAY);
		scaleFont(lblVarUpdates);
		textVarUpdates=new MarkovTextField(panel, this, 4);
		if(textHighlights[4]!=null){textVarUpdates.setBackground(textHighlights[4]);}
		else{textVarUpdates.setBackground(new Color(0,0,0,0));}
		if(type==1){ //chain
			textVarUpdates.setHorizontalAlignment(SwingConstants.RIGHT);
		}
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

	public void displayRewards(){
		lblRewards=new JLabel("R:");
		lblRewards.setForeground(Color.GRAY);
		scaleFont(lblRewards);
		String buildString="";
		for(int i=0; i<numDimensions-1; i++){buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+rewards[i]+"; ";}
		buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+rewards[numDimensions-1];
		textRewards=new MarkovTextField(panel, this, 3);
		if(textHighlights[3]!=null){textRewards.setBackground(textHighlights[3]);}
		else{textRewards.setBackground(new Color(0,0,0,0));}
		textRewards.setText(buildString);
		textRewards.validateEntry();
		textRewards.setBorder(null);
		scaleFont(textRewards);
		textRewards.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempRewards=new String[numDimensions];
				for(int i=0; i<numDimensions; i++){tempRewards[i]=rewards[i];} //Get existing values on field entry
				panel.paneFormula.setEditable(true);
				panel.paneFormula.setText(textRewards.getText());
				panel.curFocus=textRewards;
				textRewards.setBorder(defaultBorder);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary() && e.getOppositeComponent()!=panel.paneFormula) { //Validate
					textRewards.validateEntry();
					textRewards.updateHistory();
				}
			}
		});
		textRewards.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textRewards.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(panel.formulaBarFocus==false){panel.paneFormula.setText(textRewards.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}

	public void updateStateNameOrder(){
		//get position of states
		numChildren=childIndices.size();
		Object childY[][]=new Object[numChildren][2]; //[y pos][cur position in child indices]
		for(int c=0; c<numChildren; c++){
			MarkovNode curChild=tree.nodes.get(childIndices.get(c));
			childY[c][0]=curChild.yPos;
			childY[c][1]=curChild.name;
		}
		Arrays.sort(childY, new Comparator<Object[]>(){
			@Override
			public int compare(final Object[] row1, final Object[] row2){
				Integer y1 = (Integer) row1[0];
				Integer y2 = (Integer) row2[0];
				return y1.compareTo(y2);
			}
		});
		if(stateNames==null){stateNames=new ArrayList<String>();}
		stateNames.clear();
		for(int c=0; c<numChildren; c++){ //update order
			stateNames.add((String) childY[c][1]);
		}
	}
	
	
	public void displayTransition(){
		comboTransition=new JComboBox<String>();
		comboTransition.setBackground(new Color(0,0,0,0));
		comboTransition.setBorder(null);
		Font curFont=comboTransition.getFont();
		comboTransition.setFont(curFont.deriveFont(curFont.BOLD));
		scaleFont(comboTransition);
	
		DefaultComboBoxModel modelTransition=new DefaultComboBoxModel();
		if(chain.stateNames!=null){modelTransition=new DefaultComboBoxModel(chain.stateNames.toArray());}
		comboTransition.setModel(modelTransition);
		comboTransition.setSelectedItem(transition);
		
		comboTransition.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				tempTransition=transition; //get existing value on field entry
				comboTransition.setBorder(defaultBorder);
				DefaultComboBoxModel modelTransition=new DefaultComboBoxModel();
				chain.updateStateNameOrder();
				if(chain.stateNames!=null){modelTransition=new DefaultComboBoxModel(chain.stateNames.toArray());}
				comboTransition.setModel(modelTransition);
				comboTransition.setSelectedItem(tempTransition);
			}
			public void focusLost(FocusEvent e){
				if (!e.isTemporary()) { //Validate
					transition=(String)comboTransition.getSelectedItem();
					if(tempTransition!=null && !tempTransition.equals(transition)){ //Combo was changed
						panel.saveSnapshot("Edit Transition");//Add to undo stack
					}
					comboTransition.setBorder(null);
				}
			}
		});
		
	}

	public void displayEV(){
		textEV=new JTextField();
		textEV.setHorizontalAlignment(JTextField.CENTER);
		textEV.setBackground(new Color(0,0,0,0));
		textEV.setBorder(null);
		textEV.setEditable(false);
		textEV.setVisible(tree.showEV);
		scaleFont(textEV);
		textEV.setFont(new Font(textEV.getFont().getFontName(), Font.BOLD, textEV.getFont().getSize()));
		if(tree.showEV && myModel.dimInfo.analysisType==0 && expectedValues!=null){
			if(tree.discountRewards==false){
				String buildString="";
				for(int i=0; i<numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+MathUtils.round(expectedValues[numDimensions-1],myModel.dimInfo.decimals[numDimensions-1]);
				textEV.setText(buildString);
			}
			else{ //discounted
				String buildString="";
				for(int i=0; i<numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(expectedValuesDis[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[numDimensions-1]+") "+MathUtils.round(expectedValuesDis[numDimensions-1],myModel.dimInfo.decimals[numDimensions-1]);
				textEV.setText(buildString);
			}
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
	public MarkovNode copy(){
		MarkovNode node=new MarkovNode();
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
		node.transition=transition;
		node.terminationCondition=terminationCondition;
		if(type==1){ //chain
			node.expectedValues=new double[numDimensions];
		}
		for(int i=0; i<numDimensions; i++){
			node.cost[i]=cost[i];
		}
		if(type==2){ //state
			node.rewards=new String[numDimensions];
			for(int i=0; i<numDimensions; i++){
				node.rewards[i]=rewards[i];
			}
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
		//chain-specific objects
		if(type==1){ //is chain
			node.chain=node; //point to self
			node.stateNames=new ArrayList<String>();
			for(int i=0; i<stateNames.size(); i++){
				node.stateNames.add(stateNames.get(i));
			}
		}
		else{
			node.chain=chain; //copy by reference, will be updated when pasted
			//node.stateNames=chain.stateNames;
		}
		
		return(node);
	}


	public void highlightTextField(int fieldIndex, Color color){
		if(textHighlights==null){textHighlights=new Color[]{null,null,null,null,null,null};}
		textHighlights[fieldIndex]=color;
		JTextField curField=null;
		if(fieldIndex==0){curField=textProb;}
		else if(fieldIndex==1){curField=textCost;}
		else if(fieldIndex==2){curField=textTermination;}
		else if(fieldIndex==3){curField=textRewards;}
		else if(fieldIndex==4){curField=textVarUpdates;}
		else if(fieldIndex==5){curField=textName;}
		if(curField!=null){
			if(color==null){curField.setBackground(new Color(0,0,0,0));}
			else{curField.setBackground(color);}
		}
	}

	public void showNode(boolean show, ArrayList<MarkovNode> nodes){
		visible=show;
		showComponent(show,textName);
		showComponent(show,comboTransition);
		showComponent(show,lblProb); showComponent(show,textProb);
		if(visible){
			if(hasCost){showComponent(show,lblCost); showComponent(show,textCost);}
			if(hasVarUpdates){showComponent(show,lblVarUpdates); showComponent(show,textVarUpdates);}
		}
		else{//Hide cost and variable updates
			showComponent(false,lblCost); showComponent(false,textCost);
			showComponent(false,lblVarUpdates); showComponent(false,textVarUpdates);
		}
		showComponent(show,textTermination);
		showComponent(show,lblRewards); showComponent(show,textRewards);
		showComponent(false,textEV); //Always hide EV
		showComponent(false,textICER); //Hide ICER
		if(collapsed==true){
			if(show==false){panel.remove(lblCollapsed);} //remove collapsed label
			else{panel.add(lblCollapsed);} //add collapsed label
		}
		if(nodes!=null){ //apply to children
			if(collapsed==true){show=false;}
			for(int i=0; i<childIndices.size(); i++){
				int index=childIndices.get(i);
				MarkovNode child=nodes.get(index);
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
}