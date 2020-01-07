/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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
	int numPeople, numStrat;
	TreePerson people[];
	int numDim;
	int numVars;
	Variable variables[];
	Numeric origVariableVals[];
	AmuaModel myModel;
	//MersenneTwisterFast generator;
	long startTime, endTime;
	ProgressMonitor progress;
	double maxProg;
	MicroStats microStats[];
	MicroStats microStatsGroup[][];
	String strategyNames[];
	int numSubgroups;
	int subgroupSize[];
	boolean cancelled;
	Exception threadError; //caught inside multithread and thrown outside
	
	//Constructor
	public TreeMonteCarlo(TreeNode root){
		this.root=root;
		this.tree=root.tree;
		this.myModel=root.myModel;
		
		threadError=null;
		
		//Individuals
		numPeople=myModel.cohortSize;
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Monte Carlo simulation", "", 0, 100);
		
		numDim=root.numDimensions;
		numVars=myModel.variables.size();
		variables=new Variable[numVars];
		origVariableVals=new Numeric[numVars];
		for(int c=0; c<numVars; c++){ //get pointers
			variables[c]=myModel.variables.get(c);
			origVariableVals[c]=variables[c].value[0];
		}
		//Get evaluation tree - check for variables
		checkForVariables(root);
	}
	
	public void simulate(final boolean display) throws NumericException, Exception{
		numStrat=root.numChildren;
		strategyNames=new String[numStrat];
		if(myModel.displayIndResults){
			microStats=new MicroStats[numStrat];
		}
		numSubgroups=0;
		if(myModel.reportSubgroups){
			numSubgroups=myModel.subgroupNames.size();
			if(myModel.displayIndResults){
				microStatsGroup=new MicroStats[numSubgroups][numStrat];
			}
		}
				
		cancelled=false;
		maxProg=(numPeople+numPeople*numStrat)*1.0; //initialization + simulation
		progress.setMaximum((int) maxProg); 
		startTime=System.currentTimeMillis();
		
		//multi-thread
		final int numThreads=myModel.numThreads;
		for(int s=0; s<tree.nodes.size(); s++){
			TreeNode curNode=tree.nodes.get(s);
			curNode.setThreads(numThreads,numDim,numSubgroups);
		}
		
		//Initialize random number generator
		myModel.generatorVar=new MersenneTwisterFast[numThreads];
		myModel.curGenerator=new MersenneTwisterFast[numThreads];
		for(int n=0; n<numThreads; n++){
			myModel.generatorVar[n]=new MersenneTwisterFast();
			if(myModel.CRN){ //seend RNG
				myModel.generatorVar[n].setSeed(myModel.crnSeed+555+n); //initialization seed
			}
			myModel.curGenerator[n]=myModel.generatorVar[n];
		}

		//initialize probs
		for(int n=0; n<numThreads; n++){
			evalChildProbs(root,true,n);
		}
		
		for(int v=0; v<numVars; v++){
			variables[v].value=new Numeric[numThreads];
			variables[v].locked=new boolean[numThreads];
		}
		
		if(myModel.reportSubgroups){
			myModel.parseSubgroups();
		}
		
		//initialize people
		people=new TreePerson[numPeople];
		
		final int blockSize= numPeople/numThreads;
		Thread[] threads = new Thread[numThreads];
		for(int n=0; n<numThreads; n++){
			final int finalN = n;
			threads[n] = new Thread() {
				public void run(){
					try{
						int threadProg=0;
						final int beginIndex = finalN * blockSize;
						final int endIndex = (finalN==numThreads-1) ? numPeople :(finalN+1)*blockSize;
						for(int p=beginIndex; p<endIndex; p++){
							people[p]=new TreePerson();
							people[p].initVariableVals=new Numeric[numVars];
							people[p].variableVals=new Numeric[numVars];
							people[p].costs=new double[numDim];
							people[p].payoffs=new double[numDim];
							//initialize independent variables
							myModel.unlockVarsAll(finalN);
							for(int v=0; v<numVars; v++){
								if(variables[v].independent){
									variables[v].locked[finalN]=true;
									people[p].initVariableVals[v]=Interpreter.evaluateTokens(variables[v].parsedTokens, finalN, true);
									variables[v].value[finalN]=people[p].initVariableVals[v];
								}
							}
							//Update any dependent variables
							for(int v=0; v<numVars; v++){
								if(variables[v].independent==true){
									variables[v].updateDependents(myModel,finalN);
									people[p].initVariableVals[v]=variables[v].value[finalN];
								}
							}

							//get subgroup
							if(myModel.reportSubgroups){
								people[p].inSubgroup=new boolean[numSubgroups];
								for(int g=0; g<numSubgroups; g++){
									Numeric curVal=Interpreter.evaluateTokens(myModel.subgroupTokens[g], finalN, false);
									people[p].inSubgroup[g]=curVal.getBool();
								}
							}
							
							if(finalN==0 && display){ //update progress from thread 0
								threadProg++;
								updateProgress(threadProg*numThreads);
								if(progress.isCanceled()){
									cancelled=true;	p=numPeople;
								}
							}
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
		
		//Get subgroup indices
		subgroupSize=new int[numSubgroups];
		int subgroupCounters[]=new int[numSubgroups];
		for(int p=0; p<numPeople; p++){
			people[p].subgroupIndex=new int[numSubgroups];
			for(int g=0; g<numSubgroups; g++){
				if(people[p].inSubgroup[g]){
					subgroupSize[g]++;
					people[p].subgroupIndex[g]=subgroupCounters[g];
					subgroupCounters[g]++;
				}
			}
		}
		
		//Simulate each strategy
		for(int s=0; s<numStrat; s++){
			for(int n=0; n<numThreads; n++){
				if(myModel.CRN){ //Common random numbers
					myModel.generatorVar[n].setSeed(myModel.crnSeed+n);
				}
				myModel.curGenerator[n]=myModel.generatorVar[n];
			}
			
			strategyNames[s]=root.children[s].name;
			
			//Initialize individual summaries
			if(myModel.displayIndResults){
				microStats[s]=new MicroStats(myModel, numPeople);
				if(myModel.reportSubgroups){
					for(int g=0; g<numSubgroups; g++){
						microStatsGroup[g][s]=new MicroStats(myModel,subgroupSize[g]);
					}
				}
			}

			//Simulate people - multithread
			final int finalS=s;
			final TreeNode strategy=root.children[s];
			for(int n=0; n<numThreads; n++){
				final int finalN = n;
				threads[n] = new Thread() {
					public void run(){
						try{
							final int beginIndex = finalN * blockSize;
							final int endIndex = (finalN==numThreads-1) ? numPeople :(finalN+1)*blockSize;
							int threadProg=0;
							for(int p=beginIndex; p<endIndex; p++){
								TreePerson curPerson=people[p];
								for(int v=0; v<numVars; v++){ //get person-specific variables
									curPerson.variableVals[v]=curPerson.initVariableVals[v].copy();
									variables[v].value[finalN]=curPerson.variableVals[v];
								}
								for(int d=0; d<numDim; d++){ //reset outcomes
									curPerson.costs[d]=0; 
									curPerson.payoffs[d]=0;
								}

								//traverse tree
								traverseNode(strategy,curPerson,finalN);

								//record overall individual results
								if(myModel.displayIndResults){
									for(int d=0; d<numDim; d++){microStats[finalS].outcomes[d][p]=curPerson.costs[d]+curPerson.payoffs[d];}
									for(int v=0; v<numVars; v++){microStats[finalS].variables[v][p]=curPerson.variableVals[v].getValue();}

									//record subgroup results
									if(myModel.reportSubgroups){
										for(int g=0; g<numSubgroups; g++){
											if(curPerson.inSubgroup[g]){
												int z=curPerson.subgroupIndex[g]; //cur index in subgroup
												for(int d=0; d<numDim; d++){microStatsGroup[g][finalS].outcomes[d][z]=microStats[finalS].outcomes[d][p];}
												for(int v=0; v<numVars; v++){microStatsGroup[g][finalS].variables[v][z]=microStats[finalS].variables[v][p];}
											}
										}
									}
								}

								if(finalN==0 && display){ //update progress from thread 0
									threadProg++;
									updateProgress(numPeople+(finalS*numPeople)+(threadProg*numThreads));
									if(progress.isCanceled()){
										cancelled=true;	p=numPeople;
									}
								}
							} //end simulate loop
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
			
			if(cancelled){s=numStrat;}

		} //end strategy loop

		progress.close();
		
		//sum across threads
		for(int s=0; s<tree.nodes.size(); s++){
			TreeNode curNode=tree.nodes.get(s);
			curNode.sumThreads();
		}
		
		//get EVs
		for(int c=0; c<root.numChildren; c++){
			calcEV(root.children[c]);
		}
				
		//repoint variable vals
		for(int c=0; c<numVars; c++){
			variables[c].value[0]=origVariableVals[c];
		}
				
		endTime=System.currentTimeMillis();
				
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
	
	
	private void updateProgress(int curProg){
		progress.setProgress(curProg);
		//Update progress
		double prog=((curProg+1)/maxProg)*100;
		long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
		remTime=(long) (remTime*(100-prog));
		remTime=remTime/1000;
		String seconds = Integer.toString((int)(remTime % 60));
		String minutes = Integer.toString((int)(remTime/60));
		if(seconds.length()<2){seconds="0"+seconds;}
		if(minutes.length()<2){minutes="0"+minutes;}
		progress.setProgress(curProg+1);
		progress.setNote("Time left: "+minutes+":"+seconds);
	}

	
	/**
	 * Traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(TreeNode node, TreePerson curPerson, int curThread) throws Exception{
		node.nTotalDenom[curThread]++;
		for(int g=0; g<numSubgroups; g++){
			if(curPerson.inSubgroup[g]){node.nTotalDenomGroup[curThread][g]++;}
		}
				
		//Update variables
		if(node.hasVarUpdates){
			//myModel.unlockVars(curThread);
			//Perform variable updates
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].update(true,curThread);
			}
			//Update any dependent variables
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].variable.updateDependents(myModel,curThread);
			}
		}
		
		//Update costs
		if(node.hasCost){
			for(int d=0; d<numDim; d++){
				if(node.costHasVar[d]==false){ //use pre-calculated cost
					node.nTotalCosts[curThread][d]+=node.curCosts[d];
					curPerson.costs[d]+=node.curCosts[d];
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){node.nTotalCostsGroup[curThread][g][d]+=node.curCosts[d];}
					}
				}
				else{ //has variable, re-evaluate cost
					double curCost=Interpreter.evaluateTokens(node.curCostTokens[d], curThread, false).getDouble();
					node.nTotalCosts[curThread][d]+=curCost;
					curPerson.costs[d]+=curCost;
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){node.nTotalCostsGroup[curThread][g][d]+=curCost;}
					}
				}
			}
		}
		
		//Update payoffs
		if(node.type==2){ //terminal node
			for(int d=0; d<numDim; d++){
				if(node.payoffHasVar[d]==false){ //use pre-calculated payoff
					node.nTotalPayoffs[curThread][d]+=node.curPayoffs[d];
					curPerson.payoffs[d]+=node.curPayoffs[d];
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){node.nTotalPayoffsGroup[curThread][g][d]+=node.curPayoffs[d];}
					}
				}
				else{ //has variable, re-evaluate payoff
					double curPayoff=Interpreter.evaluateTokens(node.curPayoffTokens[d], curThread, false).getDouble();
					node.nTotalPayoffs[curThread][d]+=curPayoff;
					curPerson.payoffs[d]+=curPayoff;
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){node.nTotalPayoffsGroup[curThread][g][d]+=curPayoff;}
					}
				}
			}
		}
		else if(node.type==1){ //sim chance node
			double rand=myModel.generatorVar[curThread].nextDouble();
			int k=0;
			if(node.childHasProbVar==true){ //re-evaluate child probs
				evalChildProbs(node,false,curThread);
			}
			while(rand>node.curChildProbs[curThread][k]){k++;}
			TreeNode curChild=node.children[k];
			traverseNode(curChild,curPerson,curThread);
		}
		
	}
	
	/**
	 * Calculate expected values
	 */
	private void calcEV(TreeNode node){
		node.totalNet=new double[numDim]; node.totalNetGroup=new double[numSubgroups][numDim];
		node.expectedValues=new double[numDim]; node.expectedValuesGroup=new double[numSubgroups][numDim];
		if(node.numChildren==0){
			for(int i=0; i<numDim; i++){
				node.totalNet[i]=(node.totalPayoffs[i]+node.totalCosts[i]);
				node.expectedValues[i]=node.totalNet[i]/node.totalDenom;
				for(int g=0; g<numSubgroups; g++){
					node.totalNetGroup[g][i]=(node.totalPayoffsGroup[g][i]+node.totalCostsGroup[g][i]);
					node.expectedValuesGroup[g][i]=node.totalNetGroup[g][i]/node.totalDenomGroup[g];
				}
			}
		}
		else{
			for(int i=0; i<numDim; i++){ //initialize costs
				node.totalNet[i]=node.totalCosts[i];
				for(int g=0; g<numSubgroups; g++){node.totalNetGroup[g][i]=node.totalCostsGroup[g][i];}
			}
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				calcEV(child);
				for(int i=0; i<numDim; i++){
					node.totalNet[i]+=child.totalNet[i];
					for(int g=0; g<numSubgroups; g++){node.totalNetGroup[g][i]+=child.totalNetGroup[g][i];}
				}
			}
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.totalNet[i]/node.totalDenom;
				for(int g=0; g<numSubgroups; g++){node.expectedValuesGroup[g][i]=node.totalNetGroup[g][i]/node.totalDenomGroup[g];}
			}
		}
	}
	
	
	/**
	 * Re-evaluates child probs
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalChildProbs(TreeNode node, boolean recursive, int curThread) throws NumericException, Exception{
		if(node.type==1){
			//Calculate probabilities for children
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				TreeNode curChild=node.children[c];
				if(curChild.prob.matches("C") || curChild.prob.matches("c")){ //Complementary
					curChild.curProb[curThread]=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					curChild.curProb[curThread]=Interpreter.evaluateTokens(curChild.curProbTokens, curThread, false).getDouble();
					sumProb+=curChild.curProb[curThread];
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
					curChild.curProb[curThread]=1.0-sumProb;
				}
			}

			//Get cum. child probs
			TreeNode curChild=node.children[0];
			node.curChildProbs[curThread][0]=curChild.curProb[curThread];
			for(int c=1; c<node.numChildren; c++){
				curChild=node.children[c];
				node.curChildProbs[curThread][c]=node.curChildProbs[curThread][c-1]+curChild.curProb[curThread];
			}
		}
		
		if(recursive==true){ //move down tree
			for(int c=0; c<node.numChildren; c++){
				TreeNode curChild=node.children[c];
				evalChildProbs(curChild,recursive,curThread);
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