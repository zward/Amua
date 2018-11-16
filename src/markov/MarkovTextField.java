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

import java.awt.Color;
import base.ModelTextField;

public class MarkovTextField extends ModelTextField{
	MarkovNode node;
	String checkTermination;
	String checkRewards[];
		
	//Constructor
	public MarkovTextField(PanelMarkov panel1, MarkovNode node, int type){
		super(panel1,node,type);
		this.node=node;
	}

	public void validateEntry(){
		if(fieldType==0){validateProb();}
		else if(fieldType==1){validateCost();}
		else if(fieldType==2){validateTermination();}
		else if(fieldType==3){validateRewards();}
		else if(fieldType==4){validateVarUpdates();}
	}
	
	private void validateTermination(){
		checkTermination=this.getText();
		//Font curFont=this.getFont();
		//this.setFont(curFont.deriveFont(curFont.PLAIN));
		this.setForeground(Color.BLACK);
		boolean valid=true;
		if(checkTermination.length()==0){ //No entry, set to default
			checkTermination="[termination]";
			this.setText("[termination]");
			valid=false;
		}
		/*else{ //Validate expression
			if(node.validateText(checkTermination)==false){valid=false;}
			/*else{
				double testValue=node.evaluateExpression(checkTermination);
				if(testValue==1){valid=false;} //terminates at t=0
			}*/
		//}
		if(valid==false){this.setForeground(Color.RED);}

	}
	
	protected void validateRewards(){
		boolean valid=true;
		this.setForeground(Color.BLACK);
		try{
			checkRewards=node.parseDimensions(this.getText());
			int i=0;
			while(valid==true && i<node.numDimensions){
				if(node.validateText(checkRewards[i])==false){valid=false;}
				i++;
			}
		}catch(Exception ex){valid=false;}
		if(valid==false){this.setForeground(Color.RED);}
	}
	
	public void updateHistory(){
		if(fieldType==0){updateProbHistory();}
		else if(fieldType==1){updateCostHistory();}
		else if(fieldType==2){updateTerminationHistory();}
		else if(fieldType==3){updateRewardsHistory();}
		else if(fieldType==4){updateVarUpdatesHistory();}
		
		panel.paneFormula.setText("");
		panel.paneFormula.setEditable(false);
		panel.curFocus=null;
		this.setBorder(null);
	}
			
	private void updateProbHistory(){
		if(!node.tempProb.matches("0") && !node.tempProb.equals(checkProb)){ //Prob was changed
			panel.saveSnapshot("Edit Probability");//Add to undo stack
		}
		//Update current value
		node.prob=checkProb;
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
	}
	
	private void updateTerminationHistory(){
		if(!node.tempTermination.equals(checkTermination)){ //Condition was changed
			panel.saveSnapshot("Edit Termination");//Add to undo stack
		}
		//Update current value
		node.terminationCondition=checkTermination;
	}
	
	private void updateRewardsHistory(){
		boolean changed=false;
		for(int i=0; i<node.numDimensions; i++){
			if(!node.tempRewards[i].equals(checkRewards[i])){changed=true;}
		}
		if(changed==true){ //At least one cost was changed
			panel.saveSnapshot("Edit Rewards");//Add to undo stack
		}
		node.rewards=checkRewards; //Update current values
	}
	
	private void updateVarUpdatesHistory(){
		if(node.tempVarUpdates!=null && !node.tempVarUpdates.equals(checkVarUpdates)){ //Var updates changed
			panel.saveSnapshot("Edit Variable Updates"); //add to undo stack
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