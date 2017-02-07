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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;

public class TreeTextField extends JTextField{
	TreeTextField field;
	TreeNode node;
	PanelTree panel;
	int fieldType; //0=Prob; 1=Cost, 2=Payoff
	String checkProb, checkCosts[], checkPayoffs[];

	//Constructor
	public TreeTextField(PanelTree panel, TreeNode node, int type){
		field=this;
		this.panel=panel;
		this.node=node;
		fieldType=type;
		//this.setToolTipText("");
	}

	public void validateEntry(){
		if(fieldType==0){validateProb();}
		else if(fieldType==1){validateCost();}
		else if(fieldType==2){validatePayoff();}
	}

	public void updateHistory(){
		if(fieldType==0){updateProbHistory();}
		else if(fieldType==1){updateCostHistory();}
		else if(fieldType==2){updatePayoffHistory();}
	}
	
	private void validateProb(){
		checkProb=this.getText();
		Font curFont=this.getFont();
		this.setFont(curFont.deriveFont(curFont.PLAIN));
		this.setForeground(Color.BLACK);
		boolean valid=true;
		if(checkProb.length()==0){ //No entry, default to 0
			checkProb="0";
			this.setText("0");
		}
		else if(checkProb.matches("C") || checkProb.matches("c")){ //Complementary
			this.setText("C");
			this.setFont(curFont.deriveFont(curFont.ITALIC));
		}
		else{ //Validate expression
			if(node.validateText(checkProb)==false){valid=false;}
			else{
				double testProb=node.evaluateExpression(checkProb);
				if(testProb<0 || testProb>1){valid=false;}
			}
		}
		if(valid==false){this.setForeground(Color.RED);}
	}	
		
	private void updateProbHistory(){
		if(!node.tempProb.matches("0") && !node.tempProb.equals(checkProb)){ //Prob was changed
			panel.saveSnapshot("Edit Probability");//Add to undo stack
		}
		//Update current value
		node.prob=checkProb;
		panel.paneFormula.setText("");
		panel.paneFormula.setEditable(false);
		panel.curFocus=null;
		this.setBorder(null);
	}

	private void validateCost(){
		boolean valid=true;
		this.setForeground(Color.BLACK);
		try{
			checkCosts=node.parseDimensions(this.getText());
			int i=0;
			while(valid==true && i<node.numDimensions){
				if(node.validateText(checkCosts[i])==false){valid=false;}
				i++;
			}
		}catch(Exception ex){valid=false;}
		if(valid==false){this.setForeground(Color.RED);}
	}
	
	private void updateCostHistory(){
		boolean changed=false;
		for(int i=0; i<node.numDimensions; i++){
			if(!node.tempCost[i].equals(checkCosts[i])){changed=true;}
		}
		if(changed==true){ //At least one cost was changed
			panel.saveSnapshot("Edit Cost");//Add to undo stack
		}
		node.cost=checkCosts; //Update current values
		panel.paneFormula.setText("");
		panel.paneFormula.setEditable(false);
		panel.curFocus=null;
		this.setBorder(null);
	}

	private void validatePayoff(){
		boolean valid=true;
		this.setForeground(Color.BLACK);
		try{
			checkPayoffs=node.parseDimensions(this.getText());
			int i=0;
			while(valid==true && i<node.numDimensions){
				if(node.validateText(checkPayoffs[i])==false){valid=false;}
				i++;
			}
		}catch(Exception ex){valid=false;}
		if(valid==false){this.setForeground(Color.RED);}
	}
	
	private void updatePayoffHistory(){
		boolean changed=false;
		for(int i=0; i<node.numDimensions; i++){
			if(!node.tempPayoff[i].equals(checkPayoffs[i])){changed=true;}
		}
		if(changed==true){ //At least one payoff was changed
			panel.saveSnapshot("Edit Payoff");//Add to undo stack
		}
		node.payoff=checkPayoffs; //Update current values
		panel.paneFormula.setText("");
		panel.paneFormula.setEditable(false);
		panel.curFocus=null;
		this.setBorder(null);
	}

	//Set up tool-tips
	/*@Override
	public String getToolTipText(MouseEvent event){
		String tip=null;
		if(fieldType==0){tip=node.curProb+"";}
		else if(fieldType==1){validateCost();}
		else if(fieldType==2){validatePayoff();}
		return(tip);
	}*/
}