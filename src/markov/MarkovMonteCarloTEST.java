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
import base.MicroStats;
import base.RunReport;
import gui.frmTrace;
import main.MersenneTwisterFast;
import main.Variable;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class MarkovMonteCarloTEST{
	AmuaModel myModel;
	MarkovTree markovTree;
	RunReport runReport;
	
	int numDim;
	int numVars;
	Variable variables[];
	Numeric origVariableVals[];
	
	//people
	int numPeople;
	MarkovPerson people[];
	int numSubgroups;
	int subgroupSize[];
	
	//Markov chain
	int numChains;
	int numStates;
	MarkovNode states[];
	
	/**
	 * Initial prevalence probabilities
	 */
	double initPrev[][];
	
	/**
	 * Max cycles needed - thread-specific
	 */
	int nMaxCycles[];
	/**
	 * [Cycle][State][Thread], [Group][Cycle][State][Thread]
	 */
	double cyclePrev[][][], cyclePrevGroup[][][][];
	double cycleRewards[][][];
	double cycleRewardsGroup[][][][];
	double cycleVariables[][][]; int cycleVariablesDenom[][][];
	double cycleVariablesGroup[][][][]; int cycleVariablesDenomGroup[][][][];
	
	double cumRewards[], cumRewardsDis[];
	double cumRewardsGroup[][], cumRewardsDisGroup[][];
	
	MarkovTrace trace, traceGroup[];
	MicroStats microStats, microStatsGroup[];
	Variable curT;
	
	MersenneTwisterFast generator[];
	ProgressMonitor progress;
	/**
	 * [Cycle][Dim]
	 */
	double discountFactors[][];
	
	int numThreads=1;
	int blockSize;
	long startTime, endTime;
	boolean cancelled;
	Exception threadError; //caught inside multithread and thrown outside
	int curProg=0;
	double maxProg;
	
	//Constructor
	public MarkovMonteCarloTEST(MarkovTree tree, RunReport runReport){
		this.markovTree=tree;
		this.myModel=tree.myModel;
		this.runReport=runReport;
		
		numChains=tree.chains.size();
		numDim=myModel.dimInfo.dimNames.length;
		
		//threads
		numThreads=myModel.numThreads;
		threadError=null;
		
		//Get innate variable 't'
		int indexT=myModel.getInnateVariableIndex("t");
		curT=myModel.innateVariables.get(indexT);
		curT.value=new Numeric[numThreads];
		for(int n=0; n<numThreads; n++){
			curT.value[n]=new Numeric(0);
		}
		
		numVars=myModel.variables.size();
		variables=new Variable[numVars];
		origVariableVals=new Numeric[numVars];
		for(int v=0; v<numVars; v++){ //get pointers
			variables[v]=myModel.variables.get(v);
			origVariableVals[v]=variables[v].value[0];
			variables[v].value=new Numeric[numThreads];
			variables[v].locked=new boolean[numThreads];
		}
	}
	
	public void simulate(boolean showTrace, boolean showProgress) throws NumericException, Exception{
		cancelled=false;
		
		numPeople=myModel.cohortSize;
		people=new MarkovPerson[numPeople];
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Initializing...", "", 0, numPeople);
		maxProg=numPeople;
		
		initializePeople(showProgress); //create people and assign subgroups
		if(cancelled==false){
			startTime=System.currentTimeMillis();
			runChains(showProgress,showTrace); //simulate each Markov chain
		}
		
		progress.close();
		endTime=System.currentTimeMillis();
	}
	
	
	private void initializePeople(final boolean showProgress) throws Exception{
		
		numSubgroups=0;
		if(myModel.reportSubgroups){
			numSubgroups=myModel.subgroupNames.size();
			myModel.parseSubgroups();
		}
		
		//Initialize random number generator
		generator=new MersenneTwisterFast[numThreads];
		myModel.generatorVar=new MersenneTwisterFast[numThreads];
		myModel.curGenerator=new MersenneTwisterFast[numThreads];
		for(int i=0; i<numThreads; i++){
			generator[i]=new MersenneTwisterFast();
			if(myModel.CRN){ //Common random numbers
				generator[i].setSeed(myModel.crnSeed+i+555); //initialization seed
			}
			myModel.generatorVar[i]=generator[i];
			myModel.curGenerator[i]=myModel.generatorVar[i];
		}
		
		//Initialize people
		blockSize = numPeople/numThreads;
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
							people[p]=new MarkovPerson();
							people[p].rewards=new double[numDim];
							people[p].rewardsDis=new double[numDim];

							//initialize independent variables
							people[p].initVariableVals=new Numeric[numVars];
							people[p].variableVals=new Numeric[numVars];
							for(int v=0; v<numVars; v++){
								if(variables[v].independent){
									//people[p].initVariableVals[v]=Interpreter.evaluate(variables[v].expression, myModel,true,finalN);
									people[p].initVariableVals[v]=Interpreter.evaluateTokens(variables[v].parsedTokens, finalN, true);
									variables[v].value[finalN]=people[p].initVariableVals[v];
								}
							}
							//Update any dependent variables
							for(int v=0; v<numVars; v++){
								if(variables[v].independent==false){
									variables[v].updateDependents(myModel,finalN);
									people[p].initVariableVals[v]=variables[v].value[finalN];
								}
							}

							//get subgroup
							if(myModel.reportSubgroups){
								people[p].inSubgroup=new boolean[numSubgroups];
								for(int g=0; g<numSubgroups; g++){
									//Numeric curVal=Interpreter.evaluate(myModel.subgroupDefinitions.get(g), myModel, false,finalN);
									Numeric curVal=Interpreter.evaluateTokens(myModel.subgroupTokens[g], finalN, false);
									people[p].inSubgroup[g]=curVal.getBool();
								}
							}

							if(finalN==0 && showProgress){ //update progress from thread 0
								threadProg++;
								int prog=threadProg*numThreads;
								double curProg=prog/(numPeople*1.0); //convert to cycle prog
								updateProgress((int)curProg);
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
		
		for(int g=0; g<numSubgroups; g++){
			runReport.subgroupSizes[g]=subgroupSize[g];
		}
		progress.close();
	}
	
	private void runChains(final boolean showProgress, boolean showTrace) throws NumericException, Exception{
		//set threads
		for(int i=0; i<markovTree.nodes.size(); i++){
			MarkovNode curNode=markovTree.nodes.get(i);
			curNode.curProb=new double[numThreads];
			curNode.curCosts=new double[numDim][numThreads];
			curNode.curChildProbs=new double[curNode.numChildren][numThreads];
		}
		
		//calculate discount factors
		int maxCycles=markovTree.maxCycles;
		discountFactors=new double[maxCycles][numDim];
		if(markovTree.discountRewards){
			for(int t=0; t<maxCycles; t++){
				int disCycle=t;
				if(t<markovTree.discountStartCycle){disCycle=0;} //don't discount yet
				else{disCycle=(t-markovTree.discountStartCycle)+1;}
				for(int d=0; d<numDim; d++){
					double discountRate=markovTree.discountRates[d]/100.0;
					discountFactors[t][d]=1.0/Math.pow(1+discountRate, disCycle);
				}
			}
		}
		else{
			for(int t=0; t<maxCycles; t++){
				for(int d=0; d<numDim; d++){
					discountFactors[t][d]=1.0;
				}
			}
		}
		
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Monte Carlo simulation", "", 0, numPeople*numChains);
		maxProg=numPeople*numChains;
		
		//Chains
		for(int c=0; c<numChains; c++){
			final MarkovNode curChain=markovTree.chains.get(c);
			
			//Reset t
			for(int n=0; n<numThreads; n++){
				curT.value[n].setInt(0);
			}
			
			//Get Markov States
			numStates=curChain.stateNames.size();
			states=new MarkovNode[numStates];
			for(int s=0; s<numStates; s++){ //get pointers
				int index=curChain.childIndices.get(s);
				states[s]=markovTree.nodes.get(index);
				states[s].curRewards=new double[numDim][numThreads];
			}
			
			//initialize RNG
			for(int n=0; n<numThreads; n++){
				if(myModel.CRN){ //Common random numbers
					myModel.generatorVar[n].setSeed(myModel.crnSeed+n);
				}
				myModel.curGenerator[n]=myModel.generatorVar[n];
			
				evalCosts(curChain,n); //Initialize expressions for costs
				evalRewards(n);
				evalChildProbs(curChain,true,n); //Initialize probs
			}
			
			//Prev/rewards - overall
			nMaxCycles=new int[numThreads];
			cyclePrev=new double[maxCycles][numStates][numThreads];
			cycleRewards=new double[maxCycles][numDim][numThreads]; 
			cycleVariables=new double[maxCycles][numVars][numThreads]; cycleVariablesDenom=new int[maxCycles][numVars][numThreads];

			//subgroup
			cyclePrevGroup=new double[maxCycles][numSubgroups][numStates][numThreads]; 
			cycleRewardsGroup=new double[maxCycles][numSubgroups][numDim][numThreads]; 
			cycleVariablesGroup=new double[maxCycles][numSubgroups][numVars][numThreads]; cycleVariablesDenomGroup=new int[maxCycles][numSubgroups][numVars][numThreads];

			trace=new MarkovTrace(curChain);
			myModel.traceMarkov=trace;
			microStats=new MicroStats(myModel, numPeople);
			
			//Subgroups
			traceGroup=null;
			if(myModel.reportSubgroups){
				microStatsGroup=new MicroStats[numSubgroups];
				traceGroup=new MarkovTrace[numSubgroups];
				for(int g=0; g<numSubgroups; g++){
					traceGroup[g]=new MarkovTrace(curChain);
					microStatsGroup[g]=new MicroStats(myModel,subgroupSize[g]);
				}
			}
			
			//Get state indices for all transition nodes
			curChain.transFrom=-1;
			getTransitionIndex(curChain);
			//Get evaluation tree - check for variables
			checkForVariables(curChain);
			
			//Initialize state prevalence probabilities (assume static probs initially, will be updated for each person if depends on variable)
			initPrev=new double[numStates][numThreads];
			for(int n=0; n<numThreads; n++){
				initPrev[0][n]=states[0].curProb[0];
				for(int s=1; s<numStates; s++){
					initPrev[s][n]=initPrev[s-1][n]+states[s].curProb[0];
				}
			}
			
			//Simulate people
			Thread[] threads = new Thread[numThreads];
			for(int n=0; n<numThreads; n++){
				final int finalN = n;
				threads[n] = new Thread() {
					public void run(){
						try{
							final int beginIndex = finalN * blockSize;
							final int endIndex = (finalN==numThreads-1) ? numPeople :(finalN+1)*blockSize;
							for(int p=beginIndex; p<endIndex; p++){
								MarkovPerson curPerson=people[p];
								initializeState(curPerson,curChain,finalN);
								
								//Simulate cycles
								int t=0;
								curT.value[finalN].setInt(0);
								boolean terminate=false;
								while(terminate==false && t<markovTree.maxCycles){
									
									//chain root variable updates
									if(t>0 && curChain.hasVarUpdates){
										//myModel.unlockVars(finalN);
										//Perform variable updates
										for(int u=0; u<curChain.curVariableUpdates.length; u++){
											curChain.curVariableUpdates[u].update(true,finalN);
										}
										//Update any dependent variables
										for(int u=0; u<curChain.curVariableUpdates.length; u++){
											curChain.curVariableUpdates[u].variable.updateDependents(myModel,finalN);
										}
									}

									//rewards
									int curState=curPerson.curState;
									cyclePrev[t][curState][finalN]++;
									for(int g=0; g<numSubgroups; g++){
										if(curPerson.inSubgroup[g]){
											cyclePrevGroup[t][g][curState][finalN]++;
										}
									}
									
									for(int d=0; d<numDim; d++){ //Update state rewards
										if(states[curState].rewardHasVariables[d]==true){
											states[curState].curRewards[d][finalN]=Interpreter.evaluateTokens(states[curState].curRewardTokens[d], finalN, false).getDouble();
										}
										cycleRewards[t][d][finalN]+=states[curState].curRewards[d][finalN];
										for(int g=0; g<numSubgroups; g++){
											if(curPerson.inSubgroup[g]){cycleRewardsGroup[t][g][d][finalN]+=states[curState].curRewards[d][finalN];}
										}
										curPerson.rewards[d]+=states[curState].curRewards[d][finalN];
										curPerson.rewardsDis[d]+=states[curState].curRewards[d][finalN]*discountFactors[t][d];
									}
									
									//state transition
									traverseNode(states[curState],curPerson,finalN,t);
									
									//update variables
									for(int v=0; v<numVars; v++){
										double val=variables[v].value[finalN].getDouble();
										cycleVariables[t][v][finalN]+=val; cycleVariablesDenom[t][v][finalN]++;
										for(int g=0; g<numSubgroups; g++){
											if(curPerson.inSubgroup[g]){
												cycleVariablesGroup[t][g][v][finalN]+=val; cycleVariablesDenomGroup[t][g][v][finalN]++;
											}
										}
									}
																		
									terminate=checkTerminationCondition(curChain,finalN); //check condition
									//half-cycle correction
									if((t==0 || terminate) && markovTree.halfCycleCorrection==true){
										for(int d=0; d<numDim; d++){
											cycleRewards[t][d][finalN]-=states[curState].curRewards[d][finalN]*0.5;
											for(int g=0; g<numSubgroups; g++){
												if(curPerson.inSubgroup[g]){cycleRewardsGroup[t][g][d][finalN]-=states[curState].curRewards[d][finalN]*0.5;}
											}
											curPerson.rewards[d]-=states[curState].curRewards[d][finalN]*0.5;
											curPerson.rewardsDis[d]-=states[curState].curRewards[d][finalN]*discountFactors[t][d]*0.5;
										}
									}

									t++; //next cycle
									curT.value[finalN].setInt(t);
									
									if(progress.isCanceled()){
										cancelled=true;
										terminate=true;
									}
									
								} //end cycle loop
								
								nMaxCycles[finalN]=Math.max(t,nMaxCycles[finalN]);
								
								//record individual results
								//overall
								for(int d=0; d<numDim; d++){
									if(markovTree.discountRewards){microStats.outcomes[d][p]=curPerson.rewardsDis[d];}
									else{microStats.outcomes[d][p]=curPerson.rewards[d];}
								}
								for(int v=0; v<numVars; v++){
									microStats.variables[v][p]=curPerson.variableVals[v].getValue();
								}
								//subgroups
								for(int g=0; g<numSubgroups; g++){
									if(curPerson.inSubgroup[g]){
										int z=curPerson.subgroupIndex[g];
										for(int d=0; d<numDim; d++){
											if(markovTree.discountRewards){microStatsGroup[g].outcomes[d][z]=curPerson.rewardsDis[d];}
											else{microStatsGroup[g].outcomes[d][z]=curPerson.rewards[d];}
										}
										for(int v=0; v<numVars; v++){
											microStatsGroup[g].variables[v][z]=curPerson.variableVals[v].getValue();
										}
									}
								}
																
								if(finalN==0 && showProgress){ //update progress from thread 0
									curProg+=numThreads;
									updateProgress((int)curProg);
									if(progress.isCanceled()){
										cancelled=true;	p=numPeople;
									}
								}
							} //end person loop
							
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
			if(threadError!=null){throw threadError;}
			
			//get summaries and update trace
			int curMaxCycles=0;
			for(int n=0; n<numThreads; n++){
				curMaxCycles=Math.max(nMaxCycles[n], curMaxCycles);
			}
			calcTrace(curMaxCycles);
			cumRewardsGroup=new double[numSubgroups][numDim]; cumRewardsDisGroup=new double[numSubgroups][numDim];
			for(int g=0; g<numSubgroups; g++){
				calcTraceGroup(g,curMaxCycles);
			}
						
			//Reset variable 't'
			for(int n=0; n<numThreads; n++){
				curT.value[n].setInt(0);
			}
			
			//repoint variable vals
			for(int v=0; v<numVars; v++){
				variables[v].value[0]=origVariableVals[v];
			}
			
			//Get chain EVs
			if(cancelled==false){
				curChain.expectedValues=new double[numDim];
				curChain.expectedValuesDis=new double[numDim];
				for(int d=0; d<numDim; d++){
					curChain.expectedValues[d]=cumRewards[d];
					curChain.expectedValuesDis[d]=cumRewardsDis[d];
				}
				//subgroups
				curChain.expectedValuesGroup=new double[numSubgroups][numDim];
				curChain.expectedValuesDisGroup=new double[numSubgroups][numDim];
				for(int g=0; g<numSubgroups; g++){
					for(int d=0; d<numDim; d++){
						curChain.expectedValuesGroup[g][d]=cumRewardsGroup[g][d];
						curChain.expectedValuesDisGroup[g][d]=cumRewardsDisGroup[g][d];
					}
				}
				
				//update run report
				runReport.names.add(curChain.name);
				runReport.markovTraces.add(trace);
				runReport.microStats.add(microStats);
				for(int g=0; g<runReport.numSubgroups; g++){
					runReport.markovTracesGroup[g].add(traceGroup[g]);
					runReport.microStatsGroup[g].add(microStatsGroup[g]);
				}
				
				if(showTrace){//Show trace
					String subgroupNames[]=new String[numSubgroups];
					for(int g=0; g<numSubgroups; g++){subgroupNames[g]=myModel.subgroupNames.get(g);}
					frmTrace window=new frmTrace(trace,curChain.panel.errorLog,traceGroup,subgroupNames);
					window.frmTrace.setVisible(true);
				}
			}
			else{ //was cancelled
				c=numChains; //end loop
			}
		} //end chain Loop
	}
	
	private void initializeState(MarkovPerson curPerson, MarkovNode curChain, int finalN) throws NumericException, Exception{
		for(int v=0; v<numVars; v++){ //get person-specific variables
			curPerson.variableVals[v]=curPerson.initVariableVals[v].copy();
			variables[v].value[finalN]=curPerson.variableVals[v];
		}
		for(int d=0; d<numDim; d++){ //reset rewards
			curPerson.rewards[d]=0;	curPerson.rewardsDis[d]=0;
		}

		//initialize state
		if(curChain.hasVarUpdates){
			//re-point variables
			for(int v=0; v<numVars; v++){
				variables[v].value[finalN]=curPerson.variableVals[v];
			}
			//myModel.unlockVars(finalN);
			//Perform variable updates
			for(int u=0; u<curChain.curVariableUpdates.length; u++){
				curChain.curVariableUpdates[u].update(true,finalN);
			}
			//Update any dependent variables
			for(int u=0; u<curChain.curVariableUpdates.length; u++){
				curChain.curVariableUpdates[u].variable.updateDependents(myModel,finalN);
			}
		}
		//assign starting state
		if(curChain.childHasProbVariables){
			evalChildProbs(curChain,false,finalN);
			initPrev[0][finalN]=states[0].curProb[finalN];
			for(int s=1; s<numStates; s++){
				initPrev[s][finalN]=initPrev[s-1][finalN]+states[s].curProb[finalN];
			}
		}

		double rand=generator[finalN].nextDouble();
		int k=0;
		while(rand>initPrev[k][finalN]){k++;}
		curPerson.curState=k;
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
	
		
	private boolean checkTerminationCondition(MarkovNode curChain, int curThread) throws Exception{
		boolean terminate=false;
		//Numeric check=Interpreter.evaluate(curChain.terminationCondition, myModel,false);
		Numeric check=Interpreter.evaluateTokens(curChain.curTerminationTokens, curThread, false);
		if(check.getBool()){ //termination condition true
			terminate=true;
		}
		return(terminate);
	}
		
	
	/**
	 * Recursively traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(MarkovNode node, MarkovPerson curPerson, int curThread, int t) throws Exception{
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
				if(node.costHasVariables[d]==true){
					node.curCosts[d][curThread]=Interpreter.evaluateTokens(node.curCostTokens[d], curThread, false).getDouble();
				}
				cycleRewards[t][d][curThread]+=node.curCosts[d][curThread];
				for(int g=0; g<numSubgroups; g++){
					if(curPerson.inSubgroup[g]){cycleRewardsGroup[t][g][d][curThread]+=node.curCosts[d][curThread];}
				}
				curPerson.rewards[d]+=node.curCosts[d][curThread];
				curPerson.rewardsDis[d]+=node.curCosts[d][curThread]*discountFactors[t][d];
			}
		}
		
		if(node.type==4){ //Transition node, end of branch
			curPerson.curState=node.transTo;
		}
		else{ //sim chance node
			double rand=generator[curThread].nextDouble();
			int k=0;
			if(node.childHasProbVariables==true){ //re-evaluate child probs
				evalChildProbs(node,false,curThread);
			}
			while(rand>node.curChildProbs[k][curThread]){k++;}
			MarkovNode curChild=node.children[k];
			traverseNode(curChild,curPerson,curThread,t);
		}
	}
	
	/**
	 * Re-evaluates costs each cycle
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalCosts(MarkovNode node,int curThread) throws NumericException, Exception{
		if(node.hasCost){
			for(int d=0; d<numDim; d++){
				//double curCost=Interpreter.evaluate(node.cost[d],myModel,false,curThread).getDouble();
				double curCost=Interpreter.evaluateTokens(node.curCostTokens[d], curThread, false).getDouble();
				node.curCosts[d][curThread]=curCost;
			}
		}
		for(int c=0; c<node.numChildren; c++){
			evalCosts(node.children[c],curThread);
		}
	}
	
	private void evalRewards(int curThread) throws NumericException, Exception{
		for(int s=0; s<numStates; s++){ //get pointers
			for(int d=0; d<numDim; d++){
				states[s].curRewards[d][curThread]=Interpreter.evaluateTokens(states[s].curRewardTokens[d], curThread, false).getDouble();
			}
		}
	}
	
	/**
	 * Re-evaluates child probs
	 * @param node
	 * @throws NumericException
	 * @throws Exception
	 */
	private void evalChildProbs(MarkovNode node, boolean recursive, int curThread) throws NumericException, Exception{
		if(node.type!=4){ //not transition node
			//Calculate probabilities for children
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
				if(curChild.prob.matches("C") || curChild.prob.matches("c")){ //Complementary
					curChild.curProb[curThread]=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					//curChild.curProb[curThread]=Interpreter.evaluate(curChild.prob,myModel,false,curThread).getDouble();
					curChild.curProb[curThread]=Interpreter.evaluateTokens(curChild.curProbTokens, curThread, false).getDouble();
					sumProb+=curChild.curProb[curThread];
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
					curChild.curProb[curThread]=1.0-sumProb;
				}
			}

			//Get cum. child probs
			MarkovNode curChild=node.children[0];
			node.curChildProbs[0][curThread]=curChild.curProb[curThread];
			for(int c=1; c<node.numChildren; c++){
				curChild=node.children[c];
				node.curChildProbs[c][curThread]=node.curChildProbs[c-1][curThread]+curChild.curProb[curThread];
			}

			if(recursive==true){ //move down tree
				for(int c=0; c<node.numChildren; c++){
					curChild=node.children[c];
					evalChildProbs(curChild,recursive,curThread);
				}
			}
		}
	}

	
	//Sum across threads and calculate trace
	private void calcTrace(int maxCycles){
		cumRewards=new double[numDim]; cumRewardsDis=new double[numDim];
		
		for(int t=0; t<maxCycles; t++){
			trace.cycles.add(t);
			//Update prev
			for(int s=0; s<numStates; s++){
				double totalPrev=0;
				for(int n=0; n<numThreads; n++){
					totalPrev+=cyclePrev[t][s][n];
				}
				trace.prev[s].add(totalPrev); //prev at beginning of cycle
			}
			//Update rewards
			for(int d=0; d<numDim; d++){
				double curCycleRewards=0;
				for(int n=0; n<numThreads; n++){
					curCycleRewards+=cycleRewards[t][d][n];
				}
				cumRewards[d]+=curCycleRewards;
				trace.cycleRewards[d].add(curCycleRewards);
				trace.cumRewards[d].add(cumRewards[d]);
				if(markovTree.discountRewards){
					double cycleRewardsDis=curCycleRewards*discountFactors[t][d];
					cumRewardsDis[d]+=cycleRewardsDis;
					trace.cycleRewardsDis[d].add(cycleRewardsDis);
					trace.cumRewardsDis[d].add(cumRewardsDis[d]);
				}
			}
			//Update variables
			for(int v=0; v<numVars; v++){
				double num=0, denom=0;
				for(int n=0; n<numThreads; n++){
					num+=cycleVariables[t][v][n];
					denom+=cycleVariablesDenom[t][v][n];
				}
				double mean=num/(denom*1.0);
				trace.cycleVariables[v].add(mean);
			}
			trace.updateTable(t);
		}
	}
	
	private void calcTraceGroup(int g, int maxCycles){
		for(int t=0; t<maxCycles; t++){
			traceGroup[g].cycles.add(t);
			//Update prev
			for(int s=0; s<numStates; s++){
				double totalPrev=0;
				for(int n=0; n<numThreads; n++){
					totalPrev+=cyclePrevGroup[t][g][s][n];
				}
				traceGroup[g].prev[s].add(totalPrev); //prev at beginning of cycle
			}
			//Update rewards
			for(int d=0; d<numDim; d++){
				double curCycleRewards=0;
				for(int n=0; n<numThreads; n++){
					curCycleRewards+=cycleRewardsGroup[t][g][d][n];
				}
				cumRewardsGroup[g][d]+=curCycleRewards;
				traceGroup[g].cycleRewards[d].add(curCycleRewards);
				traceGroup[g].cumRewards[d].add(cumRewardsGroup[g][d]);
				if(markovTree.discountRewards){
					double cycleRewardsDisGroup=curCycleRewards*discountFactors[t][d];
					cumRewardsDisGroup[g][d]+=cycleRewardsDisGroup;
					traceGroup[g].cycleRewardsDis[d].add(cycleRewardsDisGroup);
					traceGroup[g].cumRewardsDis[d].add(cumRewardsDisGroup[g][d]);
				}
			}
			//Update variables
			for(int v=0; v<numVars; v++){
				double num=0, denom=0;
				for(int n=0; n<numThreads; n++){
					num+=cycleVariablesGroup[t][g][v][n];
					denom+=cycleVariablesDenomGroup[t][g][v][n];
				}
				double mean=num/(denom*1.0);
				traceGroup[g].cycleVariables[v].add(mean);
			}
			traceGroup[g].updateTable(t);
		}
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
				node.costHasTime[d]=myModel.textHasInnateVariable(node.cost[d]);
			}
		}
		if(node.type==2){ //state, check rewards
			node.rewardHasVariables=new boolean[numDim];
			node.rewardHasTime=new boolean[numDim];
			for(int d=0; d<numDim; d++){
				node.rewardHasVariables[d]=myModel.textHasVariable(node.rewards[d]);
				node.rewardHasTime[d]=myModel.textHasInnateVariable(node.rewards[d]);
			}
		}
		
		node.probHasVariables=myModel.textHasVariable(node.prob); //prob
		node.probHasTime=myModel.textHasInnateVariable(node.prob);
		node.childHasProbVariables=false; node.childHasProbTime=false;
		
		//move down tree
		for(int c=0; c<node.numChildren; c++){
			MarkovNode curChild=node.children[c];
			checkForVariables(curChild);
			if(curChild.probHasVariables==true){node.childHasProbVariables=true;}
			if(curChild.probHasTime==true){node.childHasProbTime=true;}
		}
	}
		

}