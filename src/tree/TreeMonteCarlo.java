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

import javax.swing.ProgressMonitor;

import base.AmuaModel;
import base.MicroStats;
import main.MersenneTwisterFast;
import main.Variable;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class TreeMonteCarlo{
	TreeNode root;
	DecisionTree tree;
	int numPeople;
	TreePerson person;
	int numDim;
	int numVars;
	Variable variables[];
	Numeric origVariableVals[];
	AmuaModel myModel;
	//MersenneTwisterFast generator;
	ProgressMonitor progress;
	MicroStats microStats[];
	String strategyNames[];
	
	//Constructor
	public TreeMonteCarlo(TreeNode root){
		this.root=root;
		this.tree=root.tree;
		this.myModel=root.myModel;
		
		//Individuals
		numPeople=myModel.cohortSize;
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Monte Carlo simulation", "", 0, 100);
		//Initialize random number generator
		myModel.generatorVar=new MersenneTwisterFast();
		
		numDim=root.numDimensions;
		numVars=myModel.variables.size();
		variables=new Variable[numVars];
		origVariableVals=new Numeric[numVars];
		for(int c=0; c<numVars; c++){ //get pointers
			variables[c]=myModel.variables.get(c);
			origVariableVals[c]=variables[c].value;
		}
		//Get evaluation tree - check for variables
		checkForVariables(root);
	}
	
	public void simulate(boolean display) throws NumericException, Exception{
		//Reset totals
		for(int s=0; s<tree.nodes.size(); s++){
			TreeNode curNode=tree.nodes.get(s);
			curNode.totalDenom=0;
			curNode.totalCosts=new double[numDim];
			curNode.totalPayoffs=new double[numDim];
		}
		
		evalChildProbs(root,true);
		
		//Simulate people
		boolean cancelled=false;
		progress.setMaximum(numPeople);
		person=new TreePerson();
		person.variableVals=new Numeric[numVars];
		person.costs=new double[numDim];
		person.payoffs=new double[numDim];
		
		long startTime=System.currentTimeMillis();
		
		int numStrat=root.numChildren;
		strategyNames=new String[numStrat];
		microStats=new MicroStats[numStrat];
		for(int s=0; s<numStrat; s++){
			strategyNames[s]=root.children[s].name;
			microStats[s]=new MicroStats(myModel, numPeople);
		}
		
		for(int p=0; p<numPeople; p++){
			//run all strategies
			for(int s=0; s<root.numChildren; s++){
				if(myModel.CRN){ //Common random numbers
					myModel.generatorVar.setSeed(myModel.crnSeed+p);
				}
				myModel.curGenerator=myModel.generatorVar;
				
				//initialize outcomes
				for(int d=0; d<numDim; d++){
					person.costs[d]=0;
					person.payoffs[d]=0;
				}
				
				//initialize variables
				for(int v=0; v<numVars; v++){
					person.variableVals[v]=Interpreter.evaluate(variables[v].expression, myModel,false);
					variables[v].value=person.variableVals[v];
				}
				
				//run model
				TreeNode child=root.children[s];
				traverseNode(child);
				
				//record results
				for(int d=0; d<numDim; d++){
					microStats[s].outcomes[d][p]=person.costs[d]+person.payoffs[d];
				}
				for(int v=0; v<numVars; v++){
					microStats[s].variables[v][p]=variables[v].value.getValue();
				}
			}
			
			//update progress
			if(display){
				progress.setProgress(p);
				//Update progress
				double prog=((p+1)/(numPeople*1.0))*100;
				long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
				remTime=(long) (remTime*(100-prog));
				remTime=remTime/1000;
				String seconds = Integer.toString((int)(remTime % 60));
				String minutes = Integer.toString((int)(remTime/60));
				if(seconds.length()<2){seconds="0"+seconds;}
				if(minutes.length()<2){minutes="0"+minutes;}
				progress.setProgress(p+1);
				progress.setNote("Time left: "+minutes+":"+seconds);
			}
			if(progress.isCanceled()){
				cancelled=true;
				p=numPeople;
			}
		}
		progress.close();
		
		//get EVs
		for(int c=0; c<root.numChildren; c++){
			calcEV(root.children[c]);
		}
				
		//repoint variable vals
		for(int c=0; c<numVars; c++){
			variables[c].value=origVariableVals[c];
		}
		
		//update display
		if(cancelled==false && display==true){
			for(int s=0; s<tree.nodes.size(); s++){
				TreeNode node=tree.nodes.get(s);
				if(node.type==1){ //chance
					String buildString="";
					for(int i=0; i<numDim-1; i++){
						buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
					}
					buildString+="("+myModel.dimInfo.dimSymbols[numDim-1]+") "+MathUtils.round(node.expectedValues[numDim-1],myModel.dimInfo.decimals[numDim-1]);
					node.textEV.setText(buildString);
					if(node.visible){
						node.textEV.setVisible(true);
					}
				}
				else if(node.type==2){ //terminal node
					node.textNumEnd.setText(node.totalDenom+"");
					if(node.visible){
						node.textNumEnd.setVisible(true);
					}
				}
			}
		}
	}
	
	/**
	 * Traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(TreeNode node) throws Exception{
		node.totalDenom++;
				
		//Update variables
		if(node.hasVarUpdates){
			myModel.unlockVars();
			//Perform variable updates
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].update(true);
			}
			//Update any dependent variables
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].variable.updateDependents(myModel);
			}
		}
		
		//Update costs
		if(node.hasCost){
			for(int d=0; d<numDim; d++){
				if(node.costHasVar[d]==false){ //use pre-calculated cost
					node.totalCosts[d]+=node.curCosts[d];
					person.costs[d]+=node.curCosts[d];
				}
				else{ //has variable, re-evaluate cost
					double curCost=Interpreter.evaluate(node.cost[d],myModel,false).getDouble();
					node.totalCosts[d]+=curCost;
					person.costs[d]+=curCost;
				}
			}
		}
		
		//Update payoffs
		if(node.type==2){ //terminal node
			for(int d=0; d<numDim; d++){
				if(node.payoffHasVar[d]==false){ //use pre-calculated payoff
					node.totalPayoffs[d]+=node.curPayoffs[d];
					person.payoffs[d]+=node.curPayoffs[d];
				}
				else{ //has variable, re-evaluate payoff
					double curPayoff=Interpreter.evaluate(node.payoff[d],myModel,false).getDouble();
					node.totalPayoffs[d]+=curPayoff;
					person.payoffs[d]+=curPayoff;
				}
			}
		}
		else if(node.type==1){ //sim chance node
			double rand=myModel.generatorVar.nextDouble();
			int k=0;
			if(node.childHasProbVar==true){ //re-evaluate child probs
				evalChildProbs(node,false);
			}
			while(rand>node.curChildProbs[k]){k++;}
			TreeNode curChild=node.children[k];
			traverseNode(curChild);
		}
		
	}
	
	/**
	 * Calculate expected values
	 */
	private void calcEV(TreeNode node){
		node.totalNet=new double[numDim];
		node.expectedValues=new double[numDim];
		if(node.numChildren==0){
			for(int i=0; i<numDim; i++){
				node.totalNet[i]=(node.totalPayoffs[i]+node.totalCosts[i]);
				node.expectedValues[i]=node.totalNet[i]/node.totalDenom;
			}
		}
		else{
			for(int i=0; i<numDim; i++){ //initialize costs
				node.totalNet[i]=node.totalCosts[i];
			}
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				calcEV(child);
				for(int i=0; i<numDim; i++){
					node.totalNet[i]+=child.totalNet[i];
				}
			}
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.totalNet[i]/node.totalDenom;
			}
		}
	}
	
	
	/**
	 * Re-evaluates child probs
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalChildProbs(TreeNode node, boolean recursive) throws NumericException, Exception{
		if(node.type==1){
			//Calculate probabilities for children
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				TreeNode curChild=node.children[c];
				if(curChild.prob.matches("C") || curChild.prob.matches("c")){ //Complementary
					curChild.curProb=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					curChild.curProb=Interpreter.evaluate(curChild.prob,myModel,false).getDouble();
					sumProb+=curChild.curProb;
				}
			}
			if(indexCompProb==-1){
				if(sumProb!=1.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
			}
			else{
				if(sumProb>1.0 || sumProb<0.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
				else{
					TreeNode curChild=node.children[indexCompProb];
					curChild.curProb=1.0-sumProb;
				}
			}

			//Get cum. child probs
			node.curChildProbs=new double[node.numChildren];
			TreeNode curChild=node.children[0];
			node.curChildProbs[0]=curChild.curProb;
			for(int c=1; c<node.numChildren; c++){
				curChild=node.children[c];
				node.curChildProbs[c]=node.curChildProbs[c-1]+curChild.curProb;
			}
		}
		
		if(recursive==true){ //move down tree
			for(int c=0; c<node.numChildren; c++){
				TreeNode curChild=node.children[c];
				evalChildProbs(curChild,recursive);
			}
		}
	}
	
	private void checkForVariables(TreeNode node){
		if(node.hasCost){ //cost
			node.costHasVar=new boolean[numDim];
			for(int d=0; d<numDim; d++){
				node.costHasVar[d]=myModel.textHasVariable(node.cost[d]);
			}
		}
		if(node.type==2){ //terminal, check payoff
			node.payoffHasVar=new boolean[numDim];
			for(int d=0; d<numDim; d++){
				node.payoffHasVar[d]=myModel.textHasVariable(node.payoff[d]);
			}
		}
		if(node.parentType!=0){
			node.probHasVar=myModel.textHasVariable(node.prob); //prob
			node.childHasProbVar=false;
		}
		
		//move down tree
		for(int c=0; c<node.numChildren; c++){
			TreeNode curChild=node.children[c];
			checkForVariables(curChild);
			if(curChild.probHasVar==true){node.childHasProbVar=true;}
		}
	}
		

}