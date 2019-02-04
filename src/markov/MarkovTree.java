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
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import main.CEAHelper;
import main.Console;
import main.ConsoleTable;
import main.DimInfo;
import main.VariableUpdate;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;
import tree.TreeNode;


@XmlRootElement(name="MarkovTree")
/**
 * 
 * @author zward
 *
 */
public class MarkovTree{
	@XmlElement(name = "Node", type = MarkovNode.class)public ArrayList<MarkovNode> nodes; //Save ArrayList of nodes for direct access to children
	@XmlElement	public int maxCycles=10000;
	@XmlElement public int stateDecimals=4;
	@XmlElement public boolean halfCycleCorrection;
	@XmlElement public boolean discountRewards;
	@XmlElement public double discountRates[];
	@XmlElement public int discountStartCycle=0;
	@XmlElement public boolean showTrace=true;
		
	@XmlTransient public boolean showEV=false;
	@XmlTransient boolean validProbs;
	@XmlTransient ArrayList<String> errors;
	@XmlTransient public AmuaModel myModel;
	
	//Constructor
	/**
	 * Default constructor
	 * @param createRoot  If true adds a decision node as the root.  If false, leaves the nodes empty
	 */
	public MarkovTree(boolean createRoot){
		nodes=new ArrayList<MarkovNode>();
		if(createRoot==true){
			nodes.add(new MarkovNode(175,175,20,20,1,this));
		}
	}

	/** 
	 * No argument constructor for XML unmarshalling.  Not explicitly called anywhere else in the code.
	 */
	public MarkovTree(){

	}

	public MarkovTree copySubtree(MarkovNode origNode){
		MarkovTree subTree=new MarkovTree(false);
		MarkovNode copyNode=origNode.copy();
		subTree.nodes.add(copyNode); //Add subtree root
		copyChildren(subTree,origNode,copyNode);
		return(subTree);
	}

	private void copyChildren(MarkovTree subTree, MarkovNode origNode, MarkovNode copyNode){
		int numChildren=origNode.childIndices.size();
		for(int i=0; i<numChildren; i++){
			int size=subTree.nodes.size();
			int childIndex=origNode.childIndices.get(i);
			MarkovNode origChild=nodes.get(childIndex);
			MarkovNode copyChild=origChild.copy();
			subTree.nodes.add(copyChild); //Copy child
			copyNode.childIndices.add(size);
			copyChildren(subTree,origChild,copyChild);
		}
	}

	public MarkovTree snapshot(){ //Return copy of this tree
		MarkovTree copy=new MarkovTree(false);
		copy.maxCycles=maxCycles;
		copy.stateDecimals=stateDecimals;
		copy.halfCycleCorrection=halfCycleCorrection;
		copy.discountRewards=discountRewards;
		if(discountRates!=null){
			copy.discountRates=new double[discountRates.length];
			for(int d=0; d<discountRates.length; d++){
				copy.discountRates[d]=discountRates[d];
			}
		}
		copy.discountStartCycle=discountStartCycle;
		
		for(int i=0; i<nodes.size(); i++){
			MarkovNode copyNode=nodes.get(i).copy();
			for(int j=0; j<nodes.get(i).childIndices.size(); j++){
				copyNode.childIndices.add(nodes.get(i).childIndices.get(j));
			}
			copy.nodes.add(copyNode);
		}
		return(copy);
	}

	/**
	 * Parse text entries and ensure tree inputs are plausible.
	 * @return ArrayList of error messages
	 */
	public ArrayList<String> parseTree(){
		myModel.validateModelObjects(); 
		
		errors=new ArrayList<String>();
		//Initialize root
		MarkovNode root=nodes.get(0);
		root.curCosts=new double[myModel.dimInfo.dimNames.length];
		
		//Parse tree inputs and variables
		validProbs=true;
		int numChildren=root.childIndices.size(); //Exclude root node
		root.numChildren=numChildren;
		root.children=new MarkovNode[numChildren];
		for(int i=0; i<numChildren; i++){
			int childIndex=root.childIndices.get(i);
			MarkovNode child=nodes.get(childIndex);
			root.children[i]=child; //get pointer to child
			parseNode(child);
		}
		
		//Ensure probabilities sum to 1.0
		if(validProbs){
			for(int i=0; i<numChildren; i++){
				int childIndex=root.childIndices.get(i);
				MarkovNode child=nodes.get(childIndex);
				checkProbs(child);
			}
			//check termination conditions
			for(int i=1; i<nodes.size(); i++){
				checkTerminationCondition(nodes.get(i));
			}
		}

		return(errors);
	}
	
	public ArrayList<String> parseChain(MarkovNode chainRoot){
		myModel.validateModelObjects();
		errors=new ArrayList<String>();
		//Parse chain inputs and variables
		validProbs=true;
		parseNode(chainRoot);
		//Ensure probabilities sum to 1.0
		if(validProbs){
			checkProbs(chainRoot);
			checkTerminationCondition(chainRoot);
		}
		return(errors);
	}

	private void parseNode(MarkovNode curNode){
		if(curNode.parentType!=0){ //Validate probability
			curNode.highlightTextField(0,null); //Prob
			if(curNode.prob.matches("C") || curNode.prob.matches("c")){curNode.curProb=-1;} //Complementary
			else{ //Evaluate text
				try{
					curNode.curProb=Interpreter.evaluate(curNode.prob, myModel,false).getDouble();
				}catch(Exception e){
					validProbs=false;
					curNode.highlightTextField(0, Color.YELLOW); //Prob
					errors.add("Node "+curNode.name+": Probability Error ("+curNode.prob+")");
				}
				if(curNode.curProb<0 || curNode.curProb>1 || Double.isNaN(curNode.curProb)){
					validProbs=false;
					curNode.highlightTextField(0, Color.YELLOW); //Prob
					errors.add("Node "+curNode.name+": Probability Error ("+curNode.prob+")");
				}
			}
		}
		
		if(curNode.type!=4){ //Not transition, ensure it has children
			if(curNode.childIndices.size()==0){
				errors.add("Node "+curNode.name+": Branches must end in a state transition");
			}
		}
		else{ //Transition, validate next state
			curNode.comboTransition.setBackground(new Color(0,0,0,0));
			curNode.comboTransition.setBorder(null);
			int index=curNode.chain.stateNames.indexOf(curNode.transition);
			if(index==-1){
				curNode.comboTransition.setBackground(Color.YELLOW);
				curNode.comboTransition.setBorder(null);
				errors.add("Node "+curNode.name+": State Transition not found ("+curNode.transition+")");
			}
		}
		
		if(curNode.type!=2){ //Not state, validate cost
			curNode.highlightTextField(1,null); //Cost
			for(int c=0; c<myModel.dimInfo.dimNames.length; c++){
				try{
					double testVal=Interpreter.evaluate(curNode.cost[c],myModel,false).getDouble();
					if(Double.isNaN(testVal)){
						curNode.highlightTextField(1, Color.YELLOW); //Cost
						errors.add("Node "+curNode.name+": Cost Error ("+curNode.cost[c]+")");
					}
				}catch(Exception e){
					curNode.highlightTextField(1, Color.YELLOW); //Cost
					errors.add("Node "+curNode.name+": Cost Error ("+curNode.cost[c]+")");
				}
			}
		}
		else{ //State, validate rewards
			curNode.highlightTextField(3, null); //rewards
			for(int c=0; c<myModel.dimInfo.dimNames.length; c++){
				try{
					double testVal=Interpreter.evaluate(curNode.rewards[c],myModel,false).getDouble();
					if(Double.isNaN(testVal)){
						curNode.highlightTextField(3, Color.YELLOW); //rewards
						errors.add("Node "+curNode.name+": Rewards Error ("+curNode.rewards[c]+")");
					}
				}catch(Exception e){
					curNode.highlightTextField(3, Color.YELLOW); //rewards
					errors.add("Node "+curNode.name+": Rewards Error ("+curNode.rewards[c]+")");
				}
			}
		}
		
		if(curNode.type==1){ //Chain, check name
			curNode.highlightTextField(5, null);
			if(curNode.name==null || curNode.name.isEmpty()){
				curNode.highlightTextField(5, Color.YELLOW);
				errors.add("Markov Chain Error: Chain not named");
			}
		}
		
		//Variable updates
		curNode.highlightTextField(4,null); 
		if(curNode.hasVarUpdates){
			String updates[]=null;
			int curU=-1;
			try{
				updates=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				curNode.curVariableUpdates=new VariableUpdate[numUpdates];
				for(int u=0; u<updates.length; u++){
					curU=u; //update for error catching
					curNode.curVariableUpdates[u]=new VariableUpdate(updates[u],myModel);
					double testVal=curNode.curVariableUpdates[u].testVal.getDouble();
					if(Double.isNaN(testVal)){
						curNode.highlightTextField(4, Color.YELLOW); //Variable updates
						errors.add("Node "+curNode.name+": Variable Update Error ("+updates[u]+")");
					}

				}
			}catch(Exception e){
				curNode.highlightTextField(4, Color.YELLOW); //Variable updates
				if(updates==null){errors.add("Node "+curNode.name+": Variable Update Error - Null entry");}
				else{errors.add("Node "+curNode.name+": Variable Update Error ("+updates[curU]+")");}
			}
		}
		
		curNode.numChildren=curNode.childIndices.size();
		curNode.children=new MarkovNode[curNode.numChildren];
		for(int c=0; c<curNode.numChildren; c++){
			int childIndex=curNode.childIndices.get(c);
			MarkovNode child=nodes.get(childIndex);
			curNode.children[c]=child; //get pointer to child
			parseNode(child);
		}
	}
	
	private void checkProbs(MarkovNode curNode){
		if(curNode.type!=0){ //Not decision node
			int numChildren=curNode.childIndices.size();
			if(numChildren>0){
				double sumProb=0;
				int numCompProb=0;
				for(int j=0; j<numChildren; j++){
					MarkovNode child=nodes.get(curNode.childIndices.get(j));
					if(child.curProb==-1){numCompProb++;}
					else{sumProb+=child.curProb;}
				}
				if(numCompProb==0){
					if(Math.abs(1.0-sumProb)>MathUtils.tolerance){
						errors.add("Node "+curNode.name+": Probabilities sum to "+sumProb+"!");
						for(int j=0; j<numChildren; j++){
							MarkovNode child=nodes.get(curNode.childIndices.get(j));
							child.highlightTextField(0, Color.YELLOW); //Prob
						}
					}
				}
				else if(numCompProb==1){
					if(sumProb<0 || sumProb>(1.0+MathUtils.tolerance)){
						errors.add("Node "+curNode.name+": Entered probabilities sum to "+sumProb+"!");
						for(int j=0; j<numChildren; j++){
							MarkovNode child=nodes.get(curNode.childIndices.get(j));
							if(child.curProb!=-1){child.highlightTextField(0, Color.YELLOW);} //Entered prob
						}
					}
					else{ //Calculate complementary prob
						for(int j=0; j<numChildren; j++){
							MarkovNode child=nodes.get(curNode.childIndices.get(j));
							if(child.curProb==-1){child.curProb=1.0-sumProb;}
						}
					}
				}
				else{ //2+ comp probs
					errors.add("Node "+curNode.name+": At most 1 probability can be complementary!");
					for(int j=0; j<numChildren; j++){
						MarkovNode child=nodes.get(curNode.childIndices.get(j));
						if(child.curProb==-1){child.highlightTextField(0, Color.YELLOW);} //Comp. prob
					}
				}
			}
		}
		int numChildren=curNode.childIndices.size();
		curNode.curChildProbs=new double[numChildren];
		for(int c=0; c<numChildren; c++){
			int childIndex=curNode.childIndices.get(c);
			MarkovNode child=nodes.get(childIndex);
			checkProbs(child);
		}
	}
	
	private void checkTerminationCondition(MarkovNode curNode){
		if(curNode.type==1){ //Chain, check termination condition
			try{
				//Initialize trace
				myModel.traceMarkov=new MarkovTrace(curNode);
				myModel.traceMarkov.setT0(curNode);
				
				Numeric check=Interpreter.evaluate(curNode.terminationCondition,myModel,false);
				if(check==null){ //if not parseable
					//curNode.lblTermination.setBackground(Color.YELLOW);
					errors.add("Node "+curNode.name+": Termination Condition Error ("+curNode.terminationCondition+")");
				}
				else if(check.getBool()==true){ //if true at t=0, model will never start
					//curNode.lblTermination.setBackground(Color.YELLOW);
					errors.add("Node "+curNode.name+": Termination Condition true at t=0 ("+curNode.terminationCondition+")");
				}
			
			}catch(Exception e){
				//curNode.lblTermination.setBackground(Color.YELLOW);
				errors.add("Node "+curNode.name+": Termination Condition Error ("+curNode.terminationCondition+")");
			}
		}
	}
	
	/**
	 * Run all Markov chains
	 * @param display
	 * @throws Exception 
	 * @throws NumericException 
	 */
	
	private void getEVs(MarkovNode node, boolean display) throws NumericException, Exception{
		int numDim=myModel.dimInfo.dimNames.length;
		
		//Update costs
		if(node.curCosts==null){node.curCosts=new double[numDim];}
		if(node.hasCost){
			for(int c=0; c<numDim; c++){
				node.curCosts[c]=Interpreter.evaluate(node.cost[c],myModel,false).getDouble();
			}
		}
		
		if(node.type==0){ //Decision, choose best branch
			//Get EVs
			node.expectedValues=new double[numDim]; node.expectedValuesDis=new double[numDim];
			//Apply cost
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curCosts[i];
				node.expectedValuesDis[i]=node.curCosts[i];
			}
			//Get children EVs
			int disIndex=0;
			if(discountRewards==true){disIndex=1;}
			double childEVs[][][]=new double[node.numChildren][numDim][2];
			for(int c=0; c<node.numChildren; c++){
				MarkovNode child=node.children[c];
				getEVs(child,display);
				for(int i=0; i<numDim; i++){
					childEVs[c][i][0]=child.expectedValues[i];
					childEVs[c][i][1]=child.expectedValuesDis[i];
				}
			}
			//Make decision
			if(myModel.dimInfo.analysisType==0){ //EV
				int obj=myModel.dimInfo.objective;
				int objDim=myModel.dimInfo.objectiveDim;
				double bestEV=childEVs[0][objDim][disIndex];
				int bestChild=0;
				for(int c=1; c<node.numChildren; c++){
					if(obj==0){ //Maximize
						if(childEVs[c][objDim][disIndex]>bestEV){
							bestEV=childEVs[c][objDim][disIndex];	bestChild=c;
						}
					}
					else{ //Minimize
						if(childEVs[c][objDim][disIndex]<bestEV){
							bestEV=childEVs[c][objDim][disIndex];	bestChild=c;
						}
					}
				}
				//choose best EV
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=childEVs[bestChild][i][0];
					node.expectedValuesDis[i]+=childEVs[bestChild][i][1];
				}
			}
			else if(myModel.dimInfo.analysisType>0){ //CEA or BCA
				int dimCost=myModel.dimInfo.costDim;
				int dimBenefit=myModel.dimInfo.effectDim;
				double wtp=myModel.dimInfo.WTP;
				double bestNMB=(wtp*childEVs[0][dimBenefit][disIndex])-childEVs[0][dimCost][disIndex];
				int bestChild=0;
				for(int c=1; c<node.numChildren; c++){
					double curNMB=(wtp*childEVs[c][dimBenefit][disIndex])-childEVs[c][dimCost][disIndex];
					if(curNMB>bestNMB){
						bestNMB=curNMB; bestChild=c;
					}
				}
				//choose best NMB
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=childEVs[bestChild][i][0];
					node.expectedValuesDis[i]+=childEVs[bestChild][i][1];
				}
			}
		}
		else if(node.type==3){ //Chance node, get EV of children
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
				if(sumProb!=1.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
			}
			else{
				if(sumProb>1.0 || sumProb<0.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
				else{
					MarkovNode curChild=node.children[indexCompProb];
					curChild.curProb=1.0-sumProb;
				}
			}
			
			//Get EVs
			node.expectedValues=new double[numDim]; node.expectedValuesDis=new double[numDim];
			//Apply cost
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curCosts[i];
				node.expectedValuesDis[i]=node.curCosts[i];
			}
			for(int c=0; c<node.numChildren; c++){
				MarkovNode child=node.children[c];
				getEVs(child,display);
				for(int d=0; d<numDim; d++){
					node.expectedValues[d]+=child.curProb*child.expectedValues[d];
					node.expectedValuesDis[d]+=child.curProb*child.expectedValuesDis[d];
				}
			}
		}
		
		if(display==true){
			String buildString="";
			if(discountRewards==false){
				for(int i=0; i<node.numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[node.numDimensions-1]+") "+MathUtils.round(node.expectedValues[node.numDimensions-1],myModel.dimInfo.decimals[node.numDimensions-1]);
			}
			else{
				for(int i=0; i<node.numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValuesDis[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[node.numDimensions-1]+") "+MathUtils.round(node.expectedValuesDis[node.numDimensions-1],myModel.dimInfo.decimals[node.numDimensions-1]);
			}
			node.textEV.setText(buildString);
			if(node.visible){
				node.textEV.setVisible(true);
			}
		}
	}
	
	/**
	 * Run selected Markov chain
	 * @param node  Root
	 * @param display
	 * @return 
	 * @throws Exception 
	 * @throws NumericException 
	 */
	public ArrayList<MarkovTrace> runModel(boolean display) throws NumericException, Exception{
		ArrayList<MarkovTrace> traces=new ArrayList<MarkovTrace>();
		MarkovNode root=nodes.get(0);
		//Run all chains and then get expected values
		for(int n=0; n<nodes.size(); n++){
			MarkovNode curNode=nodes.get(n);
			if(curNode.type==1){
				traces.add(runMarkovChain(curNode,display));
			}
		}

		//Get EVs
		for(int c=0; c<root.numChildren; c++){
			getEVs(root.children[c],display);
		}

		return(traces);
	}
	
	public MarkovTrace runMarkovChain(MarkovNode node, boolean display) throws NumericException, Exception{
		MarkovTrace trace=null;
		if(myModel.simType==0){ //Markov
			MarkovCohort cohortModel=new MarkovCohort(node);
			cohortModel.simulate(display && showTrace);
			trace=cohortModel.trace;
		}
		else if(myModel.simType==1){ //Monte Carlo
			MarkovMonteCarlo microModel=new MarkovMonteCarlo(node);
			microModel.simulate(display && showTrace);
			trace=microModel.trace;
		}
		
		if(display==true && myModel.dimInfo.analysisType==0){
			String buildString="";
			if(discountRewards==false){
				for(int i=0; i<node.numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[node.numDimensions-1]+") "+MathUtils.round(node.expectedValues[node.numDimensions-1],myModel.dimInfo.decimals[node.numDimensions-1]);
			}
			else{
				for(int i=0; i<node.numDimensions-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValuesDis[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[node.numDimensions-1]+") "+MathUtils.round(node.expectedValuesDis[node.numDimensions-1],myModel.dimInfo.decimals[node.numDimensions-1]);
			}
			node.textEV.setText(buildString);
			if(node.visible){
				node.textEV.setVisible(true);
			}
		}
		
		return(trace);
	}
	
	public int getParentIndex(int childIndex){
		int parentIndex=-1;
		int i=0;
		while(parentIndex==-1){
			if(nodes.get(i).childIndices.contains(childIndex)){parentIndex=i;}
			i++;
		}
		return(parentIndex);
	}
	
	public void updateMarkovChain(MarkovNode parent){
		if(parent.type==1){ //chain
			parent.chain=parent;
		}
		int numChildren=parent.childIndices.size();
		for(int i=0; i<numChildren; i++){
			MarkovNode child=nodes.get(parent.childIndices.get(i));
			child.chain=parent.chain;
			updateMarkovChain(child);
		}
	}

	public void displayModelResults(Console console){
		//Display output for each strategy
		DimInfo dimInfo=myModel.dimInfo;
		int numDimensions=dimInfo.dimSymbols.length;
		console.print("\n");
		boolean colTypes[]=new boolean[numDimensions+1]; //is column number (true), or text (false)
		if(discountRewards){colTypes=new boolean[numDimensions*2+1];}
		colTypes[0]=false;
		for(int d=1; d<colTypes.length; d++){colTypes[d]=true;}
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[numDimensions+1];
		if(discountRewards){headers=new String[numDimensions*2+1];}
		headers[0]="Strategy";
		for(int d=0; d<numDimensions; d++){headers[d+1]=dimInfo.dimNames[d];}
		if(discountRewards){
			for(int d=0; d<numDimensions; d++){headers[numDimensions+d+1]=dimInfo.dimNames[d]+" (Dis)";}
		}
		curTable.addRow(headers);
		//strategy results
		MarkovNode root=nodes.get(0);
		for(int c=0; c<root.numChildren; c++){
			MarkovNode curNode=root.children[c];
			String row[]=new String[numDimensions+1];
			if(discountRewards){row=new String[numDimensions*2+1];}
			row[0]=curNode.name;
			if(curNode.expectedValues!=null){
				for(int d=0; d<numDimensions; d++){
					row[d+1]=MathUtils.round(curNode.expectedValues[d],myModel.dimInfo.decimals[d])+"";
				}
				if(discountRewards){
					for(int d=0; d<numDimensions; d++){
						row[numDimensions+d+1]=MathUtils.round(curNode.expectedValuesDis[d],myModel.dimInfo.decimals[d])+"";
					}
				}
			}
			curTable.addRow(row);
		}
		
		curTable.print();
		console.newLine();
	}
	
	public void displayChainResults(Console console, MarkovNode curChain){
		DimInfo dimInfo=myModel.dimInfo;
		int numDimensions=dimInfo.dimSymbols.length;
		console.print("\n");
		boolean colTypes[]=new boolean[numDimensions+1]; //is column number (true), or text (false)
		if(discountRewards){colTypes=new boolean[numDimensions*2+1];}
		colTypes[0]=false;
		for(int d=1; d<colTypes.length; d++){colTypes[d]=true;}
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[numDimensions+1];
		if(discountRewards){headers=new String[numDimensions*2+1];}
		headers[0]="Chain";
		for(int d=0; d<numDimensions; d++){headers[d+1]=dimInfo.dimNames[d];}
		if(discountRewards){
			for(int d=0; d<numDimensions; d++){headers[numDimensions+d+1]=dimInfo.dimNames[d]+" (Dis)";}
		}
		curTable.addRow(headers);
		//chain results
		String row[]=new String[numDimensions+1];
		if(discountRewards){row=new String[numDimensions*2+1];}
		row[0]=curChain.name;
		if(curChain.expectedValues!=null){
			for(int d=0; d<numDimensions; d++){
				row[d+1]=MathUtils.round(curChain.expectedValues[d],myModel.dimInfo.decimals[d])+"";
			}
			if(discountRewards){
				for(int d=0; d<numDimensions; d++){
					row[numDimensions+d+1]=MathUtils.round(curChain.expectedValuesDis[d],myModel.dimInfo.decimals[d])+"";
				}
			}
		}
		curTable.addRow(row);
		curTable.print();
		console.newLine();
	}
	
	public void runCEA(Console console){
		DimInfo dimInfo=myModel.dimInfo;
		Object table[][]=null;
		int numStrat=0;

		if(dimInfo.analysisType==1){ //CEA
			table=new CEAHelper().calculateICERs(myModel);
			
			//Round results
			numStrat=table.length;
			for(int s=0; s<numStrat; s++){
				table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.costDim]); //Cost
				table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.effectDim]); //Effect
				double icer=(double) table[s][4];
				if(Double.isNaN(icer)){
					table[s][4]="---";
				}
				else{
					table[s][4]=MathUtils.round(icer,dimInfo.decimals[dimInfo.costDim]);
				}
			}
			
			//Print results
			console.print("\nCEA Results:\n");
			boolean colTypes[]=new boolean[]{false,true,true,true,false}; //is column number (true), or text (false)
			ConsoleTable curTable=new ConsoleTable(console,colTypes);
			String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.costDim],dimInfo.dimNames[dimInfo.effectDim],"ICER","Notes"};
			curTable.addRow(headers);
			for(int s=0; s<numStrat; s++){
				String row[]=new String[]{table[s][1]+"",table[s][2]+"",table[s][3]+"",table[s][4]+"",table[s][5]+""};
				curTable.addRow(row);
			}
			curTable.print();
			console.newLine();
		}
		else if(dimInfo.analysisType==2){ //BCA
			table=new CEAHelper().calculateNMB(myModel);
			
			//Round results
			numStrat=table.length;
			for(int s=0; s<numStrat; s++){
				table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.effectDim]); //Benefit
				table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.costDim]); //Cost
				table[s][4]=MathUtils.round((double)table[s][4],dimInfo.decimals[dimInfo.costDim]); //NMB
			}
			
			console.print("\nBCA Results:\n");
			boolean colTypes[]=new boolean[]{false,true,true,true};
			ConsoleTable curTable=new ConsoleTable(console,colTypes);
			String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.effectDim],dimInfo.dimNames[dimInfo.costDim],"NMB"};
			curTable.addRow(headers);
			for(int s=0; s<numStrat; s++){
				String row[]=new String[]{table[s][1]+"",table[s][2]+"",table[s][3]+"",table[s][4]+""};
				curTable.addRow(row);
			}
			curTable.print();
			console.newLine();
		}
		
		//Display on tree
		MarkovNode root=nodes.get(0);
		for(int s=0; s<numStrat; s++){
			int origStrat=(int) table[s][0];
			if(origStrat!=-1){
				int index=root.childIndices.get(origStrat);
				MarkovNode child=nodes.get(index);
				String icer=""+table[s][4];
				if(icer.matches("---")){
					icer=(String)table[s][5];
				}
				if(dimInfo.analysisType==1){icer="ICER: "+icer;}
				else if(dimInfo.analysisType==2){icer="NMB: "+icer;}
				child.icer=icer;
				child.textICER.setText(icer);
				if(child.visible){
					child.textICER.setVisible(true);
				}
			}
		}
	}

		
	public void updateDimensions(int numDimensions){
		int numNodes=nodes.size();
		for(int i=0; i<numNodes; i++){
			MarkovNode curNode=nodes.get(i);
			curNode.numDimensions=numDimensions;
			curNode.cost=Arrays.copyOf(curNode.cost, numDimensions);
			if(curNode.type==2){ //state
				curNode.rewards=Arrays.copyOf(curNode.rewards, numDimensions);
			}
			else if(curNode.type==1){ //chain
				if(curNode.expectedValues==null){curNode.expectedValues=new double[numDimensions];}
				else{curNode.expectedValues=Arrays.copyOf(curNode.expectedValues, numDimensions);}
				if(curNode.expectedValuesDis==null){curNode.expectedValuesDis=new double[numDimensions];}
				else{curNode.expectedValuesDis=Arrays.copyOf(curNode.expectedValuesDis, numDimensions);}
			}
			for(int d=0; d<numDimensions; d++){
				if(curNode.cost[d]==null){curNode.cost[d]="0";}
				if(curNode.type==2){
					if(curNode.rewards[d]==null){curNode.rewards[d]="0";}
				}
			}
		}
	}

}