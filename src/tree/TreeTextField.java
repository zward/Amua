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

import java.awt.Color;
import base.ModelTextField;

public class TreeTextField extends ModelTextField{
	TreeNode node;
	String checkPayoffs[];
	
	//Constructor
	public TreeTextField(PanelTree panel1, TreeNode node, int type){
		super(panel1,node,type);
		this.node=node;
	}

	public void validateEntry(){
		if(fieldType==0){validateProb();}
		else if(fieldType==1){validateCost();}
		else if(fieldType==2){validatePayoff();}
		else if(fieldType==3){validateVarUpdates();}
	}

	public void updateHistory(){
		if(fieldType==0){updateProbHistory();}
		else if(fieldType==1){updateCostHistory();}
		else if(fieldType==2){updatePayoffHistory();}
		else if(fieldType==3){updateVarUpdateHistory();}
		
		panel.paneFormula.setText("");
		panel.paneFormula.setEditable(false);
		panel.curFocus=null;
		this.setBorder(null);
	}
		
	private void updateProbHistory(){
		if(!node.tempProb.matches("0") && !node.tempProb.equals(checkProb)){ //Prob was changed
			myModel.saveSnapshot("Edit Probability");//Add to undo stack
		}
		node.prob=checkProb; //Update current value
	}
	
	private void updateCostHistory(){
		boolean changed=false;
		for(int i=0; i<node.numDimensions; i++){
			if(!node.tempCost[i].equals(checkCosts[i])){changed=true;}
		}
		if(changed==true){ //At least one cost was changed
			myModel.saveSnapshot("Edit Cost");//Add to undo stack
		}
		node.cost=checkCosts; //Update current values
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
			myModel.saveSnapshot("Edit Payoff");//Add to undo stack
		}
		node.payoff=checkPayoffs; //Update current values
	}
	
	private void updateVarUpdateHistory(){
		if(node.tempVarUpdates!=null && !node.tempVarUpdates.equals(checkVarUpdates)){ //Counters changed
			panel.saveSnapshot("Edit Counter Updates"); //add to undo stack
		}
		//Update current value
		node.varUpdates=checkVarUpdates;
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