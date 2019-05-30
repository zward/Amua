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
import base.RunReport;
import main.Console;
import main.ConsoleTable;
import main.DimInfo;
import main.VariableUpdate;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;
import math.Token;


@XmlRootElement(name="MarkovTree")
/**
 * 
 * @author zward
 *
 */
public class MarkovTree{
	@XmlElement(name = "Node", type = MarkovNode.class)public ArrayList<MarkovNode> nodes; //Save ArrayList of nodes for direct access to children
	@XmlElement	public int maxCycles=1000;
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
	@XmlTransient ArrayList<MarkovNode> chains;
	@XmlTransient Exception threadError; //caught inside multithread and thrown outside
	
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
		root.curCosts=new double[myModel.dimInfo.dimNames.length][1];
		
		chains=new ArrayList<MarkovNode>();
		
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
		chains=new ArrayList<MarkovNode>();
		chains.add(chainRoot);
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
			curNode.curProb=new double[1];
			if(curNode.prob.matches("C") || curNode.prob.matches("c")){curNode.curProb[0]=-1;} //Complementary
			else{ //Evaluate text
				try{
					curNode.curProbTokens=Interpreter.parse(curNode.prob, myModel);
					curNode.curProb[0]=Interpreter.evaluateTokens(curNode.curProbTokens, 0, false).getDouble();
				}catch(Exception e){
					validProbs=false;
					curNode.highlightTextField(0, Color.YELLOW); //Prob
					errors.add("Node "+curNode.name+": Probability Error ("+curNode.prob+")");
				}
				if(curNode.curProb[0]<0 || curNode.curProb[0]>1 || Double.isNaN(curNode.curProb[0])){
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
			int numDim=myModel.dimInfo.dimNames.length;
			curNode.curCostTokens=new Token[numDim][];
			for(int c=0; c<numDim; c++){
				try{
					curNode.curCostTokens[c]=Interpreter.parse(curNode.cost[c], myModel);
					double testVal=Interpreter.evaluateTokens(curNode.curCostTokens[c], 0, false).getDouble();
					
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
			int numDim=myModel.dimInfo.dimNames.length;
			curNode.curRewardTokens=new Token[numDim][];
			for(int c=0; c<numDim; c++){
				try{
					curNode.curRewardTokens[c]=Interpreter.parse(curNode.rewards[c], myModel);
					double testVal=Interpreter.evaluateTokens(curNode.curRewardTokens[c], 0, false).getDouble();
					
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
			chains.add(curNode);
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
					if(child.curProb[0]==-1){numCompProb++;}
					else{sumProb+=child.curProb[0];}
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
							if(child.curProb[0]!=-1){child.highlightTextField(0, Color.YELLOW);} //Entered prob
						}
					}
					else{ //Calculate complementary prob
						for(int j=0; j<numChildren; j++){
							MarkovNode child=nodes.get(curNode.childIndices.get(j));
							if(child.curProb[0]==-1){child.curProb[0]=1.0-sumProb;}
						}
					}
				}
				else{ //2+ comp probs
					errors.add("Node "+curNode.name+": At most 1 probability can be complementary!");
					for(int j=0; j<numChildren; j++){
						MarkovNode child=nodes.get(curNode.childIndices.get(j));
						if(child.curProb[0]==-1){child.highlightTextField(0, Color.YELLOW);} //Comp. prob
					}
				}
			}
		}
		int numChildren=curNode.childIndices.size();
		curNode.curChildProbs=new double[numChildren][1];
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
				
				curNode.curTerminationTokens=Interpreter.parse(curNode.terminationCondition, myModel);
				Numeric check=Interpreter.evaluateTokens(curNode.curTerminationTokens, 0, false);
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
		if(node.curCosts==null){node.curCosts=new double[numDim][1];}
		if(node.hasCost){
			for(int c=0; c<numDim; c++){
				node.curCosts[c][0]=Interpreter.evaluateTokens(node.curCostTokens[c], 0, false).getDouble();
			}
		}
		
		if(node.type==0){ //Decision, choose best branch
			//Get EVs
			node.expectedValues=new double[numDim]; node.expectedValuesDis=new double[numDim];
			//Apply cost
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curCosts[i][0];
				node.expectedValuesDis[i]=node.curCosts[i][0];
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
					curChild.curProb[0]=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					curChild.curProb[0]=Interpreter.evaluateTokens(curChild.curProbTokens, 0, false).getDouble();
					sumProb+=curChild.curProb[0];
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
					curChild.curProb[0]=1.0-sumProb;
				}
			}
			
			//Get EVs
			node.expectedValues=new double[numDim]; node.expectedValuesDis=new double[numDim];
			//Apply cost
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curCosts[i][0];
				node.expectedValuesDis[i]=node.curCosts[i][0];
			}
			for(int c=0; c<node.numChildren; c++){
				MarkovNode child=node.children[c];
				getEVs(child,display);
				for(int d=0; d<numDim; d++){
					node.expectedValues[d]+=child.curProb[0]*child.expectedValues[d];
					node.expectedValuesDis[d]+=child.curProb[0]*child.expectedValuesDis[d];
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
	
	public void runModel(boolean display, RunReport runReport, boolean allChains) throws NumericException, Exception{
		//Run all chains and then get expected values
		MarkovNode root=nodes.get(0);
		long startTime=System.currentTimeMillis();
		
		if(myModel.simType==0){ //Cohort
			runCohort(runReport,display);
		}
		else if(myModel.simType==1){ //Monte Carlo
			//MarkovMonteCarlo microModel=new MarkovMonteCarlo(this, runReport); //orig
			//MarkovMonteCarloTEST microModel=new MarkovMonteCarloTEST(this, runReport); //flip loops
			MarkovMonteCarloPOOL microModel=new MarkovMonteCarloPOOL(this, runReport); //thread pool
			
			microModel.simulate(showTrace, display);
		}
		
		if(allChains==true){//Get EVs
			for(int c=0; c<root.numChildren; c++){
				getEVs(root.children[c],display);
			}
		}
		
		if(display==true && myModel.dimInfo.analysisType==0){
			for(int c=0; c<chains.size(); c++){
				displayChainResults(chains.get(c));
			}
		}
		
		long endTime=System.currentTimeMillis();
		runReport.runTime=endTime-startTime;
	}
	
	private void runCohort(final RunReport runReport, final boolean display) throws Exception{
		//multithread
		final int numChains=chains.size();
		final int numThreads=Math.min(myModel.numThreads,numChains);
		for(int v=0; v<myModel.variables.size(); v++){
			myModel.variables.get(v).value=new Numeric[numThreads];
		}
		int indexT=myModel.getInnateVariableIndex("t");
		myModel.innateVariables.get(indexT).value=new Numeric[numThreads];
		
		final int blockSize= numChains/numThreads;
		Thread[] threads = new Thread[numThreads];
		for(int n=0; n<numThreads; n++){
			final int finalN = n;
			threads[n] = new Thread() {
				public void run(){
					try{
						final int beginIndex = finalN * blockSize;
						final int endIndex = (finalN==numThreads-1) ? numChains :(finalN+1)*blockSize;
						for(int c=beginIndex; c<endIndex; c++){
							MarkovNode curChain=chains.get(c);
							MarkovCohort cohortModel=new MarkovCohort(curChain,finalN);
							cohortModel.simulate(display && showTrace);
							runReport.names.add(curChain.name);
							runReport.markovTraces.add(cohortModel.trace);
						}
					} catch(Exception e){
						threadError=e;
					}
				}
			};
			threads[n].start();
		}
		//Wait for threads to finish
		for(int n=0; n<numThreads; n++){
			try{
				threads[n].join();
			} catch (InterruptedException e){
				System.exit(-1);
			}
		}
		//Check for error
		if(threadError!=null){
			throw threadError;
		}
	}
	
	private void displayChainResults(MarkovNode curChain){
		String buildString="";
		if(discountRewards==false){
			for(int i=0; i<curChain.numDimensions-1; i++){
				buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(curChain.expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
			}
			buildString+="("+myModel.dimInfo.dimSymbols[curChain.numDimensions-1]+") "+MathUtils.round(curChain.expectedValues[curChain.numDimensions-1],myModel.dimInfo.decimals[curChain.numDimensions-1]);
		}
		else{
			for(int i=0; i<curChain.numDimensions-1; i++){
				buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(curChain.expectedValuesDis[i],myModel.dimInfo.decimals[i])+"; ";
			}
			buildString+="("+myModel.dimInfo.dimSymbols[curChain.numDimensions-1]+") "+MathUtils.round(curChain.expectedValuesDis[curChain.numDimensions-1],myModel.dimInfo.decimals[curChain.numDimensions-1]);
		}
		curChain.textEV.setText(buildString);
		if(curChain.visible){
			curChain.textEV.setVisible(true);
		}

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
	
	
	public void printModelResults(Console console, RunReport report){
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
		
		//subgroups
		for(int g=0; g<report.numSubgroups; g++){
			console.print("\nSubgroup Results: "+report.subgroupNames[g]+" (n="+report.subgroupSizes[g]+")\n");
			curTable=new ConsoleTable(console,colTypes);
			curTable.addRow(headers);
			for(int c=0; c<root.numChildren; c++){
				MarkovNode curNode=root.children[c];
				String row[]=new String[numDimensions+1];
				if(discountRewards){row=new String[numDimensions*2+1];}
				row[0]=curNode.name;
				if(curNode.expectedValuesGroup[g]!=null){
					for(int d=0; d<numDimensions; d++){
						row[d+1]=MathUtils.round(curNode.expectedValuesGroup[g][d], myModel.dimInfo.decimals[d])+"";
					}
					if(discountRewards){
						for(int d=0; d<numDimensions; d++){
							row[numDimensions+d+1]=MathUtils.round(curNode.expectedValuesDisGroup[g][d], myModel.dimInfo.decimals[d])+"";
						}
					}
				}
				curTable.addRow(row);
			}
			curTable.print();
		}
		
		console.newLine();
	}
	
	public void printChainResults(Console console, MarkovNode curChain, RunReport report){
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
		
		//subgroups
		for(int g=0; g<report.numSubgroups; g++){
			console.print("\nSubgroup Results: "+report.subgroupNames[g]+" (n="+report.subgroupSizes[g]+")\n");
			curTable=new ConsoleTable(console,colTypes);
			curTable.addRow(headers);
			row=new String[numDimensions+1];
			if(discountRewards){row=new String[numDimensions*2+1];}
			row[0]=curChain.name;
			if(curChain.expectedValuesGroup[g]!=null){
				for(int d=0; d<numDimensions; d++){
					row[d+1]=MathUtils.round(curChain.expectedValuesGroup[g][d], myModel.dimInfo.decimals[d])+"";
				}
				if(discountRewards){
					for(int d=0; d<numDimensions; d++){
						row[numDimensions+d+1]=MathUtils.round(curChain.expectedValuesDisGroup[g][d], myModel.dimInfo.decimals[d])+"";
					}
				}
			}
			curTable.addRow(row);
			curTable.print();
		}
		
		console.newLine();
	}
	
	public void displayCEAResults(RunReport report){
		//Display on tree
		MarkovNode root=nodes.get(0);
		for(int s=0; s<report.numStrat; s++){
			int origStrat=(int) report.table[s][0];
			if(origStrat!=-1){
				int index=root.childIndices.get(origStrat);
				MarkovNode child=nodes.get(index);
				String icer=""+report.table[s][4];
				if(icer.matches("---")){
					icer=(String)report.table[s][5];
				}
				if(report.dimInfo.analysisType==1){icer="ICER: "+icer;}
				else if(report.dimInfo.analysisType==2){icer="NMB: "+icer;}
				child.icer=icer;
				child.textICER.setText(icer);
				if(child.visible){
					child.textICER.setVisible(true);
				}
				child.textEV.setVisible(false);
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