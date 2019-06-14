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

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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

public class MarkovMonteCarloPOOL{
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
	double initPrev[][]; //[state][curThread]
	double curPrev[][], newPrev[][], curPrevGroup[][][], newPrevGroup[][][];
	double cycleRewards[][],cumRewards[];
	double cycleRewardsDis[],cumRewardsDis[];
	double cycleRewardsGroup[][][], cumRewardsGroup[][], cycleRewardsDisGroup[][], cumRewardsDisGroup[][];
	double cycleVariables[][]; int cycleVariablesDenom[][];
	double cycleVariablesGroup[][][]; int cycleVariablesDenomGroup[][][];
	
	MarkovTrace trace, traceGroup[];
	MicroStats microStats, microStatsGroup[];
	Variable curT;
	
	MersenneTwisterFast generator[];
	ProgressMonitor progress;
	double discountFactor[];
	
	
	int numThreads=1;
	int blockSize;
	double maxProg;
	long startTime, endTime;
	boolean cancelled;
	Exception threadError; //caught inside multithread and thrown outside
	int guessMaxCycles=100;
	int curProg=0;
	
	//Constructor
	public MarkovMonteCarloPOOL(MarkovTree tree, RunReport runReport){
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
		maxProg=(numChains+1)*guessMaxCycles; //initialize + simulate
		progress=new ProgressMonitor(myModel.mainForm.frmMain, "Monte Carlo simulation", "", 0, (int) maxProg);
		startTime=System.currentTimeMillis();
		
		initializePeople(showProgress); //create people and assign subgroups
		if(cancelled==false){
			runChains(showProgress,showTrace); //simulate each Markov chain
		}
		
		progress.close();
		endTime=System.currentTimeMillis();
	}
	
	
	private void initializePeople(final boolean showProgress) throws Exception{
		numPeople=myModel.cohortSize;
		people=new MarkovPerson[numPeople];
		
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
									Numeric curVal=Interpreter.evaluateTokens(myModel.subgroupTokens[g], finalN, false);
									people[p].inSubgroup[g]=curVal.getBool();
								}
							}

							if(finalN==0 && showProgress){ //update progress from thread 0
								threadProg++;
								int prog=threadProg*numThreads;
								double curProg=(prog/(numPeople*1.0))*guessMaxCycles; //convert to cycle prog
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
	}
	
	private void runChains(final boolean showProgress, boolean showTrace) throws NumericException, Exception{
		//set threads
		for(int i=0; i<markovTree.nodes.size(); i++){
			MarkovNode curNode=markovTree.nodes.get(i);
			curNode.curProb=new double[numThreads];
			curNode.curCosts=new double[numDim][numThreads];
			curNode.curChildProbs=new double[curNode.numChildren][numThreads];
		}
		
		//Chain
		curProg=guessMaxCycles;
		for(int c=0; c<numChains; c++){
			final MarkovNode curChain=markovTree.chains.get(c);
			final int finalC=c;
			
			//Reset t
			for(int n=0; n<numThreads; n++){
				curT.value[n].setInt(0);
			}
			
			//initialize RNG
			for(int n=0; n<numThreads; n++){
				if(myModel.CRN){ //Common random numbers
					myModel.generatorVar[n].setSeed(myModel.crnSeed+n);
				}
				myModel.curGenerator[n]=myModel.generatorVar[n];
			
				evalCosts(curChain,n); //Update expressions for costs/rewards
				evalChildProbs(curChain,true,n); //Update probs
			}
			
			//Get Markov States
			numStates=curChain.stateNames.size();
			states=new MarkovNode[numStates];
			for(int s=0; s<numStates; s++){ //get pointers
				int index=curChain.childIndices.get(s);
				states[s]=markovTree.nodes.get(index);
				states[s].curRewards=new double[numDim][numThreads];
			}

			//Prev/rewards - overall
			curPrev=new double[numStates][numThreads]; newPrev=new double[numStates][numThreads];
			cycleRewards=new double[numDim][numThreads]; cycleRewardsDis=new double[numDim];
			cumRewards=new double[numDim]; cumRewardsDis=new double[numDim];
			cycleVariables=new double[numVars][numThreads]; cycleVariablesDenom=new int[numVars][numThreads];

			//subgroup
			curPrevGroup=new double[numSubgroups][numStates][numThreads]; newPrevGroup=new double[numSubgroups][numStates][numThreads];
			cycleRewardsGroup=new double[numSubgroups][numDim][numThreads]; cycleRewardsDisGroup=new double[numSubgroups][numDim];
			cumRewardsGroup=new double[numSubgroups][numDim]; cumRewardsDisGroup=new double[numSubgroups][numDim];
			cycleVariablesGroup=new double[numSubgroups][numVars][numThreads]; cycleVariablesDenomGroup=new int[numSubgroups][numVars][numThreads];

			trace=new MarkovTrace(curChain);
			myModel.traceMarkov=trace;
			if(myModel.displayIndResults){
				microStats=new MicroStats(myModel, numPeople);
			}
			
			//Subgroups
			traceGroup=null;
			if(myModel.reportSubgroups){
				traceGroup=new MarkovTrace[numSubgroups];
				for(int g=0; g<numSubgroups; g++){
					traceGroup[g]=new MarkovTrace(curChain);
				}
				if(myModel.displayIndResults){
					microStatsGroup=new MicroStats[numSubgroups];
					for(int g=0; g<numSubgroups; g++){
						microStatsGroup[g]=new MicroStats(myModel,subgroupSize[g]);
					}
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
			
			initializeChain(curChain,showProgress,finalC);
			
			//Simulate chain cycles
			int t=0;
			discountFactor=new double[numDim];
			
			boolean terminate=false;
			
			
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
						
			while(terminate==false && t<markovTree.maxCycles){
				if(showProgress){
					curProg++;
					updateProgress(curProg);
				}
				
				//Update discount factor
				if(markovTree.discountRewards){
					int disCycle=t;
					if(t<markovTree.discountStartCycle){disCycle=0;} //don't discount yet
					else{disCycle=(t-markovTree.discountStartCycle)+1;}
					
					for(int d=0; d<numDim; d++){
						double discountRate=markovTree.discountRates[d]/100.0;
						discountFactor[d]=1.0/Math.pow(1+discountRate, disCycle);
					}
				}
				
				//Update expressions for costs/rewards
				for(int n=0; n<numThreads; n++){
					evalCosts(curChain,n);
					for(int s=0; s<numStates; s++){
						for(int d=0; d<numDim; d++){
							double curReward=Interpreter.evaluateTokens(states[s].curRewardTokens[d], n, false).getDouble();
							states[s].curRewards[d][n]=curReward;
						}
					}
				}
				
				//Update probs
				for(int n=0; n<numThreads; n++){
					evalChildProbs(curChain,true,n);
				}
				
				//Sim cycle
				
				//thread pool
				Future futures[]=new Future[numThreads];
				for(int n=0; n<numThreads; n++){
					int beginIndex = n * blockSize;
					int endIndex = (n==numThreads-1) ? numPeople :(n+1)*blockSize;
					
					Runnable worker = new testThread(beginIndex,endIndex,n,t,curChain);
					futures[n]=executor.submit(worker);
				}
				for(int n=0; n<numThreads; n++){ //join
					futures[n].get();
				}
								
				updateTrace(t);
				for(int g=0; g<numSubgroups; g++){updateTraceGroup(g,t);}
				
								
				terminate=checkTerminationCondition(curChain); //check condition
				if(terminate && markovTree.halfCycleCorrection==true){
					trace.updateHalfCycle();
					for(int d=0; d<numDim; d++){ //adjust cum rewards
						cumRewards[d]=trace.cumRewards[d].get(t);
						if(markovTree.discountRewards){
							cumRewardsDis[d]=trace.cumRewardsDis[d].get(t);
						}
					}
					//subgroups
					for(int g=0; g<numSubgroups; g++){
						traceGroup[g].updateHalfCycle();
						for(int d=0; d<numDim; d++){
							cumRewardsGroup[g][d]=traceGroup[g].cumRewards[d].get(t);
							if(markovTree.discountRewards){
								cumRewardsDisGroup[g][d]=traceGroup[g].cumRewardsDis[d].get(t);
							}
						}
					}
				}

				t++; //next cycle
				for(int n=0; n<numThreads; n++){
					curT.value[n].setInt(t);
				}
				
				if(progress.isCanceled()){
					cancelled=true;
					terminate=true;
				}
				
			} //end cycle loop
			
			executor.shutdown();
			
			//Update max cycle guess
			guessMaxCycles=t;
			maxProg=(numChains+1)*guessMaxCycles; //initialize + simulate
			progress.setMaximum((int) maxProg);
			
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
			
				//record individual results
				if(myModel.displayIndResults){
					Thread[] threads = new Thread[numThreads];
					for(int n=0; n<numThreads; n++){
						final int finalN = n;
						threads[n] = new Thread() {
							public void run(){
								try{
									final int beginIndex = finalN * blockSize;
									final int endIndex = (finalN==numThreads-1) ? numPeople :(finalN+1)*blockSize;
									//Update each person
									for(int p=beginIndex; p<endIndex; p++){ 
										MarkovPerson curPerson=people[p];
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
					if(threadError!=null){throw threadError;}
				}
				
				//update run report
				runReport.names.add(curChain.name);
				runReport.markovTraces.add(trace);
				for(int g=0; g<runReport.numSubgroups; g++){
					runReport.markovTracesGroup[g].add(traceGroup[g]);
				}
				if(myModel.displayIndResults){
					runReport.microStats.add(microStats);
					for(int g=0; g<runReport.numSubgroups; g++){
						runReport.microStatsGroup[g].add(microStatsGroup[g]);
					}
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
	
	private void initializeChain(final MarkovNode curChain, final boolean showProgress, final int finalC) throws Exception{
		//Initialize state for all people - multithread
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
							for(int v=0; v<numVars; v++){ //get person-specific variables
								curPerson.variableVals[v]=curPerson.initVariableVals[v].copy();
								variables[v].value[finalN]=curPerson.variableVals[v];
							}
							for(int d=0; d<numDim; d++){ //reset rewards
								people[p].rewards[d]=0;	people[p].rewardsDis[d]=0;
							}
	
							//initialize state
							if(curChain.hasVarUpdates){
								//re-point variables
								for(int v=0; v<numVars; v++){
									variables[v].value[finalN]=people[p].variableVals[v];
								}
								myModel.unlockVars(finalN);
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
							people[p].curState=k;
							
							curPrev[k][finalN]++; newPrev[k][finalN]++;
							for(int g=0; g<numSubgroups; g++){
								if(curPerson.inSubgroup[g]){
									curPrevGroup[g][k][finalN]++; newPrevGroup[g][k][finalN]++;
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
		if(threadError!=null){throw threadError;}
		
		
	}

	/**
	 * @deprecated
	 * @param curChain
	 * @param t
	 * @throws Exception
	 */
	private void simCycle(final MarkovNode curChain, final int t) throws Exception{
		//multi-thread
		Thread[] threads = new Thread[numThreads];
		for(int n=0; n<numThreads; n++){
			final int finalN = n;
			threads[n] = new Thread() {
				public void run(){
					try{
						final int beginIndex = finalN * blockSize;
						final int endIndex = (finalN==numThreads-1) ? numPeople :(finalN+1)*blockSize;
						//Update each person
						for(int p=beginIndex; p<endIndex; p++){ 
							MarkovPerson curPerson=people[p];
							
							//re-point variables
							for(int v=0; v<numVars; v++){
								variables[v].value[finalN]=curPerson.variableVals[v];
							}

							//chain root variable updates
							if(t>0 && curChain.hasVarUpdates){
								myModel.unlockVars(finalN);
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
							for(int d=0; d<numDim; d++){ //Update state rewards
								if(states[curState].rewardHasVariables[d]==false){ //use pre-calculated reward
									cycleRewards[d][finalN]+=states[curState].curRewards[d][finalN];
									for(int g=0; g<numSubgroups; g++){
										if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][finalN]+=states[curState].curRewards[d][finalN];}
									}
									curPerson.rewards[d]+=states[curState].curRewards[d][finalN];
									curPerson.rewardsDis[d]+=states[curState].curRewards[d][finalN]*discountFactor[d];
								}
								else{ //has variable, re-evaluate reward
									double curReward=Interpreter.evaluateTokens(states[curState].curRewardTokens[d], finalN, false).getDouble();
									cycleRewards[d][finalN]+=curReward;
									for(int g=0; g<numSubgroups; g++){
										if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][finalN]+=curReward;}
									}
									curPerson.rewards[d]+=curReward;
									curPerson.rewardsDis[d]+=curReward*discountFactor[d];
								}
							}
							
							//state transition
							traverseNode(states[curState],curPerson,finalN);
							
							//update variables
							for(int v=0; v<numVars; v++){
								double val=variables[v].value[finalN].getDouble();
								cycleVariables[v][finalN]+=val; cycleVariablesDenom[v][finalN]++;
								for(int g=0; g<numSubgroups; g++){
									if(curPerson.inSubgroup[g]){
										cycleVariablesGroup[g][v][finalN]+=val; cycleVariablesDenomGroup[g][v][finalN]++;
									}
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
		
		updateTrace(t);
		for(int g=0; g<numSubgroups; g++){updateTraceGroup(g,t);}
		
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
	
		
	private boolean checkTerminationCondition(MarkovNode curChain) throws Exception{
		boolean terminate=false;
		Numeric check=Interpreter.evaluateTokens(curChain.curTerminationTokens, 0, false);
		if(check.getBool()){ //termination condition true
			terminate=true;
		}
		return(terminate);
	}
		
	
	/**
	 * Recursively traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(MarkovNode node, MarkovPerson curPerson, int curThread) throws Exception{
		//Update variables
		if(node.hasVarUpdates){
			myModel.unlockVars(curThread);
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
				if(node.costHasVariables[d]==false){ //use pre-calculated cost
					cycleRewards[d][curThread]+=node.curCosts[d][curThread];
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][curThread]+=node.curCosts[d][curThread];}
					}
					curPerson.rewards[d]+=node.curCosts[d][curThread];
					curPerson.rewardsDis[d]+=node.curCosts[d][curThread]*discountFactor[d];
				}
				else{ //has variable, re-evaluate cost
					double curCost=Interpreter.evaluateTokens(node.curCostTokens[d], curThread, false).getDouble();
					cycleRewards[d][curThread]+=curCost;
					for(int g=0; g<numSubgroups; g++){
						if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][curThread]+=curCost;}
					}
					curPerson.rewards[d]+=curCost;
					curPerson.rewardsDis[d]+=curCost*discountFactor[d];
				}
			}
		}
		
		if(node.type==4){ //Transition node, end of branch
			newPrev[node.transFrom][curThread]--; //from state
			newPrev[node.transTo][curThread]++; //next state
			for(int g=0; g<numSubgroups; g++){
				if(curPerson.inSubgroup[g]){
					newPrevGroup[g][node.transFrom][curThread]--;
					newPrevGroup[g][node.transTo][curThread]++;
				}
			}
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
			traverseNode(curChild,curPerson,curThread);
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
				double curCost=Interpreter.evaluateTokens(node.curCostTokens[d], curThread, false).getDouble();
				node.curCosts[d][curThread]=curCost;
			}
		}
		for(int c=0; c<node.numChildren; c++){
			evalCosts(node.children[c],curThread);
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

	
	//Sum across threads and update trace
	private void updateTrace(int t){
		trace.cycles.add(t);
		//Update prev
		for(int s=0; s<numStates; s++){
			double totalPrev=0;
			for(int n=0; n<numThreads; n++){
				totalPrev+=curPrev[s][n];
				curPrev[s][n]=newPrev[s][n];
			}
			trace.prev[s].add(totalPrev); //prev at beginning of cycle
		}
		//Check for half-cycle correction - first and last cycle
		if(t==0 && markovTree.halfCycleCorrection==true){
			for(int d=0; d<numDim; d++){
				for(int n=0; n<numThreads; n++){
					cycleRewards[d][n]*=0.5; //half-cycle correction
				}
			}
		}
		//Update rewards
		for(int d=0; d<numDim; d++){
			double curCycleRewards=0;
			for(int n=0; n<numThreads; n++){
				curCycleRewards+=cycleRewards[d][n];
			}
			cumRewards[d]+=curCycleRewards;
			trace.cycleRewards[d].add(curCycleRewards);
			trace.cumRewards[d].add(cumRewards[d]);
			if(markovTree.discountRewards){
				cycleRewardsDis[d]=curCycleRewards*discountFactor[d];
				cumRewardsDis[d]+=cycleRewardsDis[d];
				trace.cycleRewardsDis[d].add(cycleRewardsDis[d]);
				trace.cumRewardsDis[d].add(cumRewardsDis[d]);
			}
			//reset
			for(int n=0; n<numThreads; n++){cycleRewards[d][n]=0;} 
			cycleRewardsDis[d]=0;
		}
		//Update variables
		for(int v=0; v<numVars; v++){
			double num=0, denom=0;
			for(int n=0; n<numThreads; n++){
				num+=cycleVariables[v][n]; cycleVariables[v][n]=0;
				denom+=cycleVariablesDenom[v][n]; cycleVariablesDenom[v][n]=0;
			}
			double mean=num/(denom*1.0);
			trace.cycleVariables[v].add(mean);
		}
		trace.updateTable(t);
	}
	
	private void updateTraceGroup(int g, int t){
		traceGroup[g].cycles.add(t);
		//Update prev
		for(int s=0; s<numStates; s++){
			double totalPrev=0;
			for(int n=0; n<numThreads; n++){
				totalPrev+=curPrevGroup[g][s][n];
				curPrevGroup[g][s][n]=newPrevGroup[g][s][n];
			}
			traceGroup[g].prev[s].add(totalPrev); //prev at beginning of cycle
		}
		//Check for half-cycle correction - first and last cycle
		if(t==0 && markovTree.halfCycleCorrection==true){
			for(int d=0; d<numDim; d++){
				for(int n=0; n<numThreads; n++){
					cycleRewardsGroup[g][d][n]*=0.5; //half-cycle correction
				}
			}
		}
		//Update rewards
		for(int d=0; d<numDim; d++){
			double curCycleRewards=0;
			for(int n=0; n<numThreads; n++){
				curCycleRewards+=cycleRewardsGroup[g][d][n];
			}
			cumRewardsGroup[g][d]+=curCycleRewards;
			traceGroup[g].cycleRewards[d].add(curCycleRewards);
			traceGroup[g].cumRewards[d].add(cumRewardsGroup[g][d]);
			if(markovTree.discountRewards){
				cycleRewardsDisGroup[g][d]=curCycleRewards*discountFactor[d];
				cumRewardsDisGroup[g][d]+=cycleRewardsDisGroup[g][d];
				traceGroup[g].cycleRewardsDis[d].add(cycleRewardsDisGroup[g][d]);
				traceGroup[g].cumRewardsDis[d].add(cumRewardsDisGroup[g][d]);
			}
			//reset
			for(int n=0; n<numThreads; n++){cycleRewardsGroup[g][d][n]=0;} 
			cycleRewardsDisGroup[g][d]=0;
		}
		//Update variables
		for(int v=0; v<numVars; v++){
			double num=0, denom=0;
			for(int n=0; n<numThreads; n++){
				num+=cycleVariablesGroup[g][v][n]; cycleVariablesGroup[g][v][n]=0;
				denom+=cycleVariablesDenomGroup[g][v][n]; cycleVariablesDenomGroup[g][v][n]=0;
			}
			double mean=num/(denom*1.0);
			traceGroup[g].cycleVariables[v].add(mean);
		}
		traceGroup[g].updateTable(t);
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
	
	
	private class testThread implements Runnable{
		private int beginIndex, endIndex;
		private int finalN; //cur thread
		private int t;
		private MarkovNode curChain;
		
		public testThread(int beginIndex, int endIndex, int finalN, int t, MarkovNode curChain){
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.finalN=finalN;
			this.t=t;
			this.curChain=curChain;
		}
		
		@Override
		public void run(){
			try{
				//Update each person
				for(int p=beginIndex; p<endIndex; p++){ 
					MarkovPerson curPerson=people[p];

					//re-point variables
					for(int v=0; v<numVars; v++){
						variables[v].value[finalN]=curPerson.variableVals[v];
					}

					//chain root variable updates
					if(t>0 && curChain.hasVarUpdates){
						myModel.unlockVars(finalN);
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
					for(int d=0; d<numDim; d++){ //Update state rewards
						if(states[curState].rewardHasVariables[d]==false){ //use pre-calculated reward
							cycleRewards[d][finalN]+=states[curState].curRewards[d][finalN];
							for(int g=0; g<numSubgroups; g++){
								if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][finalN]+=states[curState].curRewards[d][finalN];}
							}
							curPerson.rewards[d]+=states[curState].curRewards[d][finalN];
							curPerson.rewardsDis[d]+=states[curState].curRewards[d][finalN]*discountFactor[d];
						}
						else{ //has variable, re-evaluate reward
							double curReward=Interpreter.evaluateTokens(states[curState].curRewardTokens[d], finalN, false).getDouble();
							cycleRewards[d][finalN]+=curReward;
							for(int g=0; g<numSubgroups; g++){
								if(curPerson.inSubgroup[g]){cycleRewardsGroup[g][d][finalN]+=curReward;}
							}
							curPerson.rewards[d]+=curReward;
							curPerson.rewardsDis[d]+=curReward*discountFactor[d];
						}
					}

					//state transition
					traverseNode(states[curState],curPerson,finalN);

					//update variables
					for(int v=0; v<numVars; v++){
						double val=variables[v].value[finalN].getDouble();
						cycleVariables[v][finalN]+=val; cycleVariablesDenom[v][finalN]++;
						for(int g=0; g<numSubgroups; g++){
							if(curPerson.inSubgroup[g]){
								cycleVariablesGroup[g][v][finalN]+=val; cycleVariablesDenomGroup[g][v][finalN]++;
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		} //end run function
	} //end thread class

}