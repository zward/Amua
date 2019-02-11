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

import javax.swing.ProgressMonitor;

import base.AmuaModel;
import gui.frmTrace;
import main.MersenneTwisterFast;
import main.Variable;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class MarkovMonteCarlo{
	MarkovNode chainRoot;
	MarkovTree markovTree;
	int numStates;
	MarkovNode states[];
	int numPeople;
	MarkovPerson people[];
	double curPrev[], newPrev[];
	int numDim;
	double cycleRewards[],cumRewards[];
	double cycleRewardsDis[],cumRewardsDis[];
	int numVariables;
	Variable variables[];
	Numeric origVariableVals[];
	double cycleVariables[]; int cycleVariablesDenom[];
	MarkovTrace trace;
	Variable curT;
	AmuaModel myModel;
	MersenneTwisterFast generator;
	ProgressMonitor progress;
	
	//Constructor
	public MarkovMonteCarlo(MarkovNode chainRoot){
		this.chainRoot=chainRoot;
		this.markovTree=chainRoot.tree;
		this.myModel=chainRoot.myModel;
		//Get Markov States
		numStates=chainRoot.stateNames.size();
		states=new MarkovNode[numStates];
		for(int s=0; s<numStates; s++){ //get pointers
			int index=chainRoot.childIndices.get(s);
			states[s]=markovTree.nodes.get(index);
		}
		//Individuals
		numPeople=myModel.cohortSize;
		people=new MarkovPerson[numPeople];
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Monte Carlo simulation", "", 0, 100);
		//Initialize random number generator
		generator=new MersenneTwisterFast();
		if(myModel.CRN){ //Common random numbers
			generator.setSeed(myModel.crnSeed);
		}
		myModel.generatorVar=generator;
		
		//Prev/rewards
		curPrev=new double[numStates]; newPrev=new double[numStates];
		numDim=chainRoot.numDimensions;
		cycleRewards=new double[numDim]; cycleRewardsDis=new double[numDim];
		cumRewards=new double[numDim]; cumRewardsDis=new double[numDim];
		numVariables=myModel.variables.size();
		cycleVariables=new double[numVariables]; cycleVariablesDenom=new int[numVariables];
		variables=new Variable[numVariables];
		origVariableVals=new Numeric[numVariables];
		for(int c=0; c<numVariables; c++){ //get pointers
			variables[c]=myModel.variables.get(c);
			origVariableVals[c]=variables[c].value;
		}
		trace=new MarkovTrace(chainRoot);
		myModel.traceMarkov=trace;
		//Get state indices for all transition nodes
		chainRoot.transFrom=-1;
		getTransitionIndex(chainRoot);
		//Get evaluation tree - check for variables
		checkForVariables(chainRoot);
	}
	
	public void simulate(boolean showTrace) throws NumericException, Exception{
		//Get innate variable 't'
		int indexT=myModel.getInnateVariableIndex("t");
		curT=myModel.innateVariables.get(indexT);
		curT.value=new Numeric(0);
		
		//Initialize state prevalence (assume static probs)
		double initPrev[]=new double[numStates];
		initPrev[0]=states[0].curProb;
		curPrev[0]=0; newPrev[0]=0;
		for(int s=1; s<numStates; s++){
			initPrev[s]=initPrev[s-1]+states[s].curProb;
			curPrev[s]=0; newPrev[s]=0;
		}
		
		//Initialize people
		for(int p=0; p<numPeople; p++){
			people[p]=new MarkovPerson();
			//initialize variables
			people[p].variableVals=new Numeric[numVariables];
			for(int c=0; c<numVariables; c++){
				people[p].variableVals[c]=Interpreter.evaluate(variables[c].expression, myModel,false);
			}
			//chain root variable updates
			if(chainRoot.hasVarUpdates){
				//re-point variables
				for(int c=0; c<numVariables; c++){
					variables[c].value=people[p].variableVals[c];
				}
				myModel.unlockVars();
				//Perform variable updates
				for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
					chainRoot.curVariableUpdates[u].update(true);
				}
				//Update any dependent variables
				for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
					chainRoot.curVariableUpdates[u].variable.updateDependents(myModel);
				}
			}
			//assign starting state
			if(chainRoot.childHasProbVariables){
				evalChildProbs(chainRoot,false);
				initPrev=new double[numStates];
				initPrev[0]=states[0].curProb;
				for(int s=1; s<numStates; s++){
					initPrev[s]=initPrev[s-1]+states[s].curProb;
				}
			}
			
			double rand=generator.nextDouble();
			int k=0;
			while(rand>initPrev[k]){k++;}
			people[p].curState=k;
			curPrev[k]++; newPrev[k]++;
		}
		
		
		//Simulate cycles
		int t=0;
		
		boolean terminate=false;
		boolean cancelled=false;
		progress.setMaximum(markovTree.maxCycles);
		
		while(terminate==false && t<markovTree.maxCycles){
			progress.setProgress(t);
			progress.setNote("t = "+t);
			
			//Update expressions for costs/rewards
			evalCosts(chainRoot);
			for(int s=0; s<numStates; s++){
				states[s].curRewards=new double[numDim];
				for(int d=0; d<numDim; d++){
					double curReward=Interpreter.evaluate(states[s].rewards[d],myModel,false).getDouble();
					states[s].curRewards[d]=curReward;
				}
			}
			
			//Update probs
			evalChildProbs(chainRoot,true);
			
			//Update each person
			for(int p=0; p<numPeople; p++){ 
				//re-point variables
				for(int c=0; c<numVariables; c++){
					variables[c].value=people[p].variableVals[c];
				}
				
				//chain root variable updates
				if(t>0 && chainRoot.hasVarUpdates){
					myModel.unlockVars();
					//Perform variable updates
					for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
						chainRoot.curVariableUpdates[u].update(true);
					}
					//Update any dependent variables
					for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
						chainRoot.curVariableUpdates[u].variable.updateDependents(myModel);
					}
				}
				
				
				int curState=people[p].curState;
				//rewards
				for(int d=0; d<numDim; d++){ //Update state rewards
					if(states[curState].rewardHasVariables[d]==false){ //use pre-calculated reward
						cycleRewards[d]+=states[curState].curRewards[d];
					}
					else{ //has variable, re-evaluate reward
						double curReward=Interpreter.evaluate(states[curState].rewards[d],myModel,false).getDouble();
						cycleRewards[d]+=curReward;
					}
					
				}
				//state transition
				traverseNode(states[curState],people[p]);
				//update variables
				for(int c=0; c<numVariables; c++){
					cycleVariables[c]+=variables[c].value.getDouble();
					cycleVariablesDenom[c]++;
				}
			}
			
			updateTrace(t);
			terminate=checkTerminationCondition(); //check condition
			if(terminate && markovTree.halfCycleCorrection==true){
				trace.updateHalfCycle();
				//adjust cum rewards
				for(int d=0; d<numDim; d++){
					cumRewards[d]=trace.cumRewards[d].get(t);
					if(markovTree.discountRewards){
						cumRewardsDis[d]=trace.cumRewardsDis[d].get(t);
					}
				}
			}

			t++; //next cycle
			curT.value.setInt(t);
			
			if(progress.isCanceled()){
				cancelled=true;
				terminate=true;
			}
		}
		progress.close();

		//Get chain EVs
		if(cancelled==false){
			chainRoot.expectedValues=new double[numDim];
			chainRoot.expectedValuesDis=new double[numDim];
			for(int d=0; d<numDim; d++){
				chainRoot.expectedValues[d]=cumRewards[d];
				chainRoot.expectedValuesDis[d]=cumRewardsDis[d];
			}
		}

		//Reset variable 't'
		curT.value.setInt(0);

		//repoint variable vals
		for(int c=0; c<numVariables; c++){
			variables[c].value=origVariableVals[c];
		}
		
		if(showTrace){//Show trace
			frmTrace window=new frmTrace(trace,chainRoot.panel.errorLog);
			window.frmTrace.setVisible(true);
		}
	}
	
	private boolean checkTerminationCondition(){
		boolean terminate=false;
		try{
			Numeric check=Interpreter.evaluate(chainRoot.terminationCondition, myModel,false);
			if(check.getBool()){ //termination condition true
				terminate=true;
			}
		}catch(Exception e){
			e.printStackTrace();
			chainRoot.panel.errorLog.recordError(e);
			curT.value.setInt(0);
		}
		return(terminate);
	}
	
	/**
	 * Recursively traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(MarkovNode node, MarkovPerson curPerson) throws Exception{
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
				if(node.costHasVariables[d]==false){ //use pre-calculated cost
					cycleRewards[d]+=node.curCosts[d];
				}
				else{ //has variable, re-evaluate cost
					double curCost=Interpreter.evaluate(node.cost[d],myModel,false).getDouble();
					cycleRewards[d]+=curCost;
				}
			}
		}
		
		
		if(node.type==4){ //Transition node, end of branch
			newPrev[node.transFrom]--; //from state
			newPrev[node.transTo]++; //next state
			curPerson.curState=node.transTo;
		}
		else{ //sim chance node
			double rand=generator.nextDouble();
			int k=0;
			if(node.childHasProbVariables==true){ //re-evaluate child probs
				evalChildProbs(node,false);
			}
			while(rand>node.curChildProbs[k]){k++;}
			MarkovNode curChild=node.children[k];
			traverseNode(curChild,curPerson);
		}
		
	}
	
	/**
	 * Re-evaluates costs each cycle
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalCosts(MarkovNode node) throws NumericException, Exception{
		if(node.hasCost){
			node.curCosts=new double[numDim];
			for(int d=0; d<numDim; d++){
				double curCost=Interpreter.evaluate(node.cost[d],myModel,false).getDouble();
				node.curCosts[d]=curCost;
			}
		}
		for(int c=0; c<node.numChildren; c++){
			evalCosts(node.children[c]);
		}
		
	}
	
	/**
	 * Re-evaluates child probs
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalChildProbs(MarkovNode node, boolean recursive) throws NumericException, Exception{
		if(node.type!=4){ //not transition node
			//Calculate probabilities for children
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
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
				if(Math.abs(1.0-sumProb)>MathUtils.tolerance){ //throw error
					throw new Exception("Error: Probabilities sum to "+sumProb+" ("+node.name+")");
				}
			}
			else{
				if(sumProb>1.0 || sumProb<0.0){ //throw error
					throw new Exception("Error: Probabilities sum to "+sumProb+" ("+node.name+")");
				}
				else{
					MarkovNode curChild=node.children[indexCompProb];
					curChild.curProb=1.0-sumProb;
				}
			}

			//Get cum. child probs
			MarkovNode curChild=node.children[0];
			node.curChildProbs[0]=curChild.curProb;
			for(int c=1; c<node.numChildren; c++){
				curChild=node.children[c];
				node.curChildProbs[c]=node.curChildProbs[c-1]+curChild.curProb;
			}

			if(recursive==true){ //move down tree
				for(int c=0; c<node.numChildren; c++){
					curChild=node.children[c];
					evalChildProbs(curChild,recursive);
				}
			}
		}
	}

	private void updateTrace(int t){
		trace.cycles.add(t);
		//Update prev
		for(int s=0; s<numStates; s++){
			trace.prev[s].add(curPrev[s]); //prev at beginning of cycle
			curPrev[s]=newPrev[s];
		}
		//Check for half-cycle correction - first and last cycle
		if(t==0 && markovTree.halfCycleCorrection==true){
			for(int d=0; d<numDim; d++){
				cycleRewards[d]*=0.5; //half-cycle correction
			}
		}
		//Update rewards
		for(int d=0; d<numDim; d++){
			cumRewards[d]+=cycleRewards[d];
			trace.cycleRewards[d].add(cycleRewards[d]);
			trace.cumRewards[d].add(cumRewards[d]);
			if(markovTree.discountRewards){
				double discountRate=markovTree.discountRates[d]/100.0;
				int disCycle=t;
				if(t<markovTree.discountStartCycle){disCycle=0;} //don't discount yet
				else{disCycle=(t-markovTree.discountStartCycle)+1;}
				double discountFactor=1.0/Math.pow(1+discountRate, disCycle);
				
				cycleRewardsDis[d]=cycleRewards[d]*discountFactor;
				cumRewardsDis[d]+=cycleRewardsDis[d];
				trace.cycleRewardsDis[d].add(cycleRewardsDis[d]);
				trace.cumRewardsDis[d].add(cumRewardsDis[d]);
			}
			//reset
			cycleRewards[d]=0; 
			cycleRewardsDis[d]=0;
		}
		//Update variables
		for(int c=0; c<numVariables; c++){
			double mean=cycleVariables[c]/(cycleVariablesDenom[c]*1.0);
			trace.cycleVariables[c].add(mean);
			cycleVariables[c]=0; cycleVariablesDenom[c]=0;
		}
		trace.updateTable(t);
	}
	
	private void getTransitionIndex(MarkovNode node){
		if(node.type==4){ //get transition to
			String nextState=(String) node.comboTransition.getSelectedItem();
			node.transTo=getStateIndex(nextState);
		}
		else{
			if(node.type==2){ //state, get transition from
				node.transFrom=getStateIndex(node.name);
			}
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
				curChild.transFrom=node.transFrom; //pass to child
				getTransitionIndex(curChild);
			}
		}
	}
	
	private int getStateIndex(String name){
		int index=-1;
		boolean found=false;
		while(found==false){
			index++;
			if(states[index].name.equals(name)){
				found=true;
			}
		}
		return(index);
	}
	
	private void checkForVariables(MarkovNode node){
		if(node.hasCost){ //cost
			node.costHasVariables=new boolean[numDim];
			for(int d=0; d<numDim; d++){
				node.costHasVariables[d]=myModel.textHasVariable(node.cost[d]);
			}
		}
		if(node.type==2){ //state, check rewards
			node.rewardHasVariables=new boolean[numDim];
			for(int d=0; d<numDim; d++){
				node.rewardHasVariables[d]=myModel.textHasVariable(node.rewards[d]);
			}
		}
		
		node.probHasVariables=myModel.textHasVariable(node.prob); //prob
		node.childHasProbVariables=false;
		
		//move down tree
		for(int c=0; c<node.numChildren; c++){
			MarkovNode curChild=node.children[c];
			checkForVariables(curChild);
			if(curChild.probHasVariables==true){node.childHasProbVariables=true;}
		}
	}
		

}