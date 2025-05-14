/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2024 Zachary J. Ward
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

package voi;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import base.AmuaModel;
import main.CEAHelper;
import main.Constraint;
import main.MersenneTwisterFast;
import main.PSAResults;
import main.Parameter;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class EVPPI{
	
	AmuaModel myModel;
	public int numStrat;
	public int numDim;
	int numOutcomes;
	String outcomes[];
	
	public int analysisType;
	public int objective;
	public int objectiveDim, costDim, effectDim;
	public double WTP;
	
	/**
	 * 0=Two-level Monte Carlo, 1=PSA Bins
	 */
	public int method;
	public int numInner, numOuter; //iterations for 2-level Monte Carlo
	
	/**
	 * Excluding overall
	 */
	int numSubgroups=0;
	
	int numParams;
	int paramDims[];
	String paramNames[];
	
	public int numSelected;
	public boolean paramSelected[];
	
	public int numIterations;
	/**
	 * [Group][Outcome][Strategy][x,y][Iteration]
	 */
	public double dataResultsIter[][][][][], dataResultsVal[][][][][], dataResultsCumDens[][][][][];
	public String CEAnotes[][][];
	
	/**
	 * [Parameter][Parameter Dimension][Iteration][Value]
	 */
	public double dataParamsIter[][][][], dataParamsVal[][][][], dataParamsCumDens[][][][];
	
	public double results[][];
	
	public int sign;
	
	//bins
	ArrayList<SortedResult> sortedResults;
	
	/**
	 * [Parameter][Parameter Dimension][Value][Bins]
	 */
	public double evppiBins[][][][];
	
	
	
	//EVPPI estimates
	/**
	 * [Param][ParamDim]
	 */
	public double evppi[][];
	
	
	public boolean valid;
	
	//Constructor
	public EVPPI(AmuaModel myModel, int selectedCount, boolean paramSelected[]){
		this.myModel=myModel;
		this.numSelected=selectedCount;
		this.paramSelected=paramSelected;
		
		numStrat=myModel.getStrategies();
		numDim=myModel.dimInfo.dimNames.length;
		outcomes=myModel.dimInfo.getOutcomes();
		numOutcomes=outcomes.length;
		
		numParams=myModel.parameters.size();
		paramNames=new String[numParams];
		paramDims=new int[numParams];
		for(int i=0; i<numParams; i++){
			Parameter curParam=myModel.parameters.get(i);
			paramNames[i]=curParam.name;
			paramDims[i]=1; //default to single dimension (scalar)
			if(curParam.value.isMatrix()) {
				paramDims[i]=curParam.value.nrow*curParam.value.ncol;
			}
		}
		
		if(myModel.simType==1 && myModel.reportSubgroups){numSubgroups=myModel.subgroupNames.size();}
		
		analysisType=myModel.dimInfo.analysisType;
				
		objective=myModel.dimInfo.objective;
		objectiveDim=myModel.dimInfo.objectiveDim;
		if(analysisType>0) { //CEA or BCA
			costDim=myModel.dimInfo.costDim;
			effectDim=myModel.dimInfo.effectDim;
			WTP=myModel.dimInfo.WTP;
		}
		
		sign=1; //objective is to maximize outcome
		if(analysisType==0 && objective==1) { //EV, minimize - change to maximize negative
			sign=-1;
		}
		if(analysisType>0 && objective==1) { //minimize
			sign=-1;
		}
					
	}
	
	
	//Calculates EVPI
	public void runTwoLevelMonteCarlo(int numOuter, int numInner, boolean useSeed, int seed, ProgressMonitor progress, JFrame curFrm) throws NumericException, Exception {
		
		valid=true;

		//Check model first
		ArrayList<String> errorsBase=myModel.parseModel();

		if(errorsBase.size()>0){
			JOptionPane.showMessageDialog(curFrm, "Errors in base case model!");
		}
		else{

			myModel.sampleParam=true;
			myModel.generatorParam=new MersenneTwisterFast();
			if(myModel.curGenerator==null){
				myModel.curGenerator=new MersenneTwisterFast[1];
			}
			myModel.curGenerator[0]=myModel.generatorParam;
			if(useSeed){
				myModel.generatorParam.setSeed(seed);
			}
			
			//Get orig values for all parameters
			Numeric origValues[]=new Numeric[numParams];
			for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters
				origValues[v]=myModel.parameters.get(v).value.copy();
			}

			//Parse constraints
			int numConstraints=myModel.constraints.size();
			for(int c=0; c<numConstraints; c++){
				myModel.constraints.get(c).parseConstraints();
			}

			boolean origShowTrace=true;
			if(myModel.type==1){
				origShowTrace=myModel.markov.showTrace;
				myModel.markov.showTrace=false;
			}

			long startTime=System.currentTimeMillis();

			int n=0; //total iterations progress
			int totalIterations=numOuter*numInner*numSelected;
			progress.setMaximum(totalIterations);
			progress.setMillisToDecideToPopup(0);
			progress.setMillisToPopup(0);
			
			dataResultsIter=new double[1+numSubgroups][numOutcomes][numStrat][2][totalIterations];
			dataResultsVal=new double[1+numSubgroups][numOutcomes][numStrat][2][totalIterations];
			dataResultsCumDens=new double[1+numSubgroups][numOutcomes][numStrat][2][totalIterations];
			
			if(analysisType==1){CEAnotes=new String[1+numSubgroups][numStrat][totalIterations];} //CEA
			else{CEAnotes=null;}

			dataParamsIter=new double[numParams][][][];
			dataParamsVal=new double[numParams][][][];
			dataParamsCumDens=new double[numParams][][][];
			for(int p=0; p<numParams; p++) {
				int curDim=paramDims[p];
				dataParamsIter[p]=new double[curDim][2][totalIterations];
				dataParamsVal[p]=new double[curDim][2][totalIterations];
				dataParamsCumDens[p]=new double[curDim][2][totalIterations];
			}
			
			results=new double[numStrat][totalIterations];
			
			evppi=new double[numParams][];
			
			for(int p=0; p<numParams; p++) { //parameter loop
				if(paramSelected[p]==true) {
					evppi[p]=new double[paramDims[p]];
					
					int numIterationsCur=numOuter*numInner;
					double resultsCur[][]=new double[numStrat][numIterationsCur];
					int bestStrat[]=new int[numIterationsCur];
					
					int x=0;
					for(int i=0; i<numOuter; i++) { //outer loop
						
						//sample current parameter of interest
						Parameter param0=myModel.parameters.get(p);
						myModel.curGenerator[0]=myModel.generatorParam;
						boolean validParams=false;
						while(validParams==false){
							//Reset 'fixed' for current parameter and orig values
							param0.locked=false;
							param0.value=origValues[p];
							//sample value
							param0.value=Interpreter.evaluateTokens(param0.parsedTokens, 0, true);
							param0.locked=true;
							//check constraints
							validParams=true;
							int c=0;
							while(validParams==true && c<numConstraints){
								Constraint curConst=myModel.constraints.get(c);
								validParams=curConst.checkConstraints(myModel);
								c++;
							}
							if(validParams){ //check model for valid params
								if(myModel.parseModel().size()!=0){validParams=false;}
							}
						}
						Numeric val0=param0.value;
						
						double meanInnerOutcomes[]=new double[numStrat];
						
						for(int j=0; j<numInner; j++) { //inner loop
							updateProgress(n, totalIterations, startTime, progress); 
							bestStrat[x]=-1;
							
							//reset current parameter of interest (all parameters are unlocked at end of runModel)
							param0.value=val0;
							param0.locked=true;
							
							//sample all other parameters
							myModel.curGenerator[0]=myModel.generatorParam;
							validParams=false;
							while(validParams==false){
								//Reset 'fixed' for all parameters and orig values
								for(int v=0; v<numParams; v++){ 
									if(v!=p) {
										Parameter curParam=myModel.parameters.get(v);
										curParam.locked=false;
										curParam.value=origValues[v];
									}
								}
								//sample parameters
								for(int v=0; v<numParams; v++){ 
									if(v!=p) {
										Parameter curParam=myModel.parameters.get(v);
										if(curParam.locked==false) {
											curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true);
											curParam.locked=true;
										}
									}
								}
								//check constraints
								validParams=true;
								int c=0;
								while(validParams==true && c<numConstraints){
									Constraint curConst=myModel.constraints.get(c);
									validParams=curConst.checkConstraints(myModel);
									c++;
								}
								if(validParams){ //check model for valid params
									if(myModel.parseModel().size()!=0){validParams=false;}
								}
							}
							
						
							//record value of all parameters
							for(int v=0; v<numParams; v++){ //Record value
								int curDim=paramDims[v];
								if(curDim==1) { //scalar
									dataParamsIter[v][0][0][n]=n; dataParamsVal[v][0][0][n]=n;
									try{
										dataParamsIter[v][0][1][n]=myModel.parameters.get(v).value.getDouble();
									} catch(Exception e){
										dataParamsIter[v][0][1][n]=Double.NaN;
									}
									dataParamsVal[v][0][1][n]=dataParamsIter[v][0][1][n];
								}
								else { //matrix
									Numeric val=myModel.parameters.get(v).value;
									int z=0;
									for(int r=0; r<val.nrow; r++) {
										for(int c=0; c<val.ncol; c++) {
											dataParamsIter[v][z][0][n]=n; dataParamsVal[v][z][0][n]=n;
											dataParamsIter[v][z][1][n]=val.matrix[r][c];
											dataParamsVal[v][z][1][n]=val.matrix[r][c];
											z++;
										}
									}
								}
							} 
							
							//Run model
							myModel.curGenerator=myModel.generatorVar;
							myModel.runModel(null, false);

							//Get EVs
							for(int d=0; d<numDim; d++){
								for(int s=0; s<numStrat; s++){
									//overall
									dataResultsIter[0][d][s][0][n]=n; dataResultsVal[0][d][s][0][n]=n;
									double curOutcome=myModel.getStrategyEV(s, d);
									dataResultsIter[0][d][s][1][n]=curOutcome; dataResultsVal[0][d][s][1][n]=curOutcome;
									//subgroups
									for(int g=0; g<numSubgroups; g++){
										dataResultsIter[g+1][d][s][0][n]=n; dataResultsVal[g+1][d][s][0][n]=n;
										curOutcome=myModel.getSubgroupEV(g, s, d);
										dataResultsIter[g+1][d][s][1][n]=curOutcome; dataResultsVal[g+1][d][s][1][n]=curOutcome;
									}
								}
							}
							if(analysisType>0){ //CEA or BCA
								if(analysisType==1){ //CEA
									for(int g=0; g<numSubgroups+1; g++){
										Object table[][]=new CEAHelper().calculateICERs(myModel,g-1,true);
										//get baseline row
										int baseIndex=myModel.getStrategyIndex(myModel.dimInfo.baseScenario);
										int baseRow=-1,curRow=0;
										while(baseRow==-1 && curRow<table.length){
											if((int)table[curRow][0]==baseIndex){
												baseRow=curRow;
											}
											curRow++;
										}

										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											if(origStrat!=-1){
												dataResultsIter[g][numDim][origStrat][0][n]=n; dataResultsVal[g][numDim][origStrat][0][n]=n;
												double curOutcome=(double) table[s][4];
												dataResultsIter[g][numDim][origStrat][1][n]=curOutcome; dataResultsVal[g][numDim][origStrat][1][n]=curOutcome;
												CEAnotes[g][origStrat][n]=(String) table[s][5];
											}
										}
									}
								}
								else if(analysisType==2){ //BCA
									for(int g=0; g<numSubgroups+1; g++){
										Object table[][]=new CEAHelper().calculateNMB(myModel,g-1,true);
										//use first row as baseline
										//int baseIndex=myModel.getStrategyIndex(myModel.dimInfo.baseScenario);
										int baseIndex=0;
										int baseRow=-1,curRow=0;
										while(baseRow==-1 && curRow<table.length){
											if((int)table[curRow][0]==baseIndex){
												baseRow=curRow;
											}
											curRow++;
										}
										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											dataResultsIter[g][numDim][origStrat][0][n]=n;	dataResultsVal[g][numDim][origStrat][0][n]=n;
											double curOutcome=(double) table[s][4];
											dataResultsIter[g][numDim][origStrat][1][n]=curOutcome; dataResultsVal[g][numDim][origStrat][1][n]=curOutcome;
										}
									}
								}
							}
							
							//calculate conditional NMB of each strategy
							if(analysisType==0) { //EV
								for(int s=0; s<numStrat; s++) {
									results[s][n]=dataResultsIter[0][objectiveDim][s][1][n];
									meanInnerOutcomes[s]+=sign*results[s][n];
									resultsCur[s][x]=sign*results[s][n];
								}
							}
							else { //CEA or BCA
								for(int s=0; s<numStrat; s++) {
									double cost=dataResultsIter[0][costDim][s][1][n];
									double effect=dataResultsIter[0][effectDim][s][1][n]*sign;
									double NMB=WTP*effect-cost;
									results[s][n]=NMB;
									meanInnerOutcomes[s]+=NMB;
									resultsCur[s][x]=NMB;
								}
							}
							
							if(progress.isCanceled()){  //End loops
								i=numOuter;
								j=numInner;
								valid=false;
							}
							
							x++;
							n++;
						} //end inner loop
						
						//calculate conditional expected NMB of each strategy
						double bestInner=Double.NEGATIVE_INFINITY;
						int bestStratInner=-1;
						for(int s=0; s<numStrat; s++) {
							meanInnerOutcomes[s]/=(numInner*1.0);
							if(meanInnerOutcomes[s]>bestInner) {
								bestInner=meanInnerOutcomes[s];
								bestStratInner=s;
							}
						}
						//record best strat 
						for(int z=x-1; z>=(x-numInner); z--) {
							bestStrat[z]=bestStratInner;
						}
					} //end outer loop
					
					//calculate EVPPI
					//get best strategy overall
					double meanBest=0;
					double meanStrat[]=new double[numStrat];
					for(int i=0; i<numIterationsCur; i++) {
						for(int s=0; s<numStrat; s++) {
							meanStrat[s]+=resultsCur[s][i];
						}
						meanBest+=resultsCur[bestStrat[i]][i]; //best strategy identified in inner loop
					}
					
					double maxMean=Double.NEGATIVE_INFINITY;
					for(int s=0; s<numStrat; s++) {
						meanStrat[s]/=(numIterationsCur*1.0);
						if(meanStrat[s]>maxMean) {
							maxMean=meanStrat[s];
						}
					}
					meanBest/=(numIterationsCur*1.0);
					
					evppi[p][0]=meanBest-maxMean;
					
					
				} //end check if parameter selected
			} //end parameter loop
			
			//Reset all parameters
			myModel.sampleParam=false;
			for(int v=0; v<numParams; v++){ //Reset 'locked' for all parameter and orig values
				Parameter curParam=myModel.parameters.get(v);
				curParam.locked=false;
				curParam.value=origValues[v];
			}
			myModel.validateModelObjects();

			if(myModel.type==1){
				myModel.markov.showTrace=origShowTrace;
			}
			
			progress.close();
		}
		curFrm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
	}
	
	public void estimateTwoLevelMonteCarlo(int numOuter, int numInner) {
		
		int n=0; //total iterations progress
		
		evppi=new double[numParams][];
		
		for(int p=0; p<numParams; p++) { //parameter loop
			if(paramSelected[p]==true) {
				evppi[p]=new double[paramDims[p]];
				
				int numIterationsCur=numOuter*numInner;
				double resultsCur[][]=new double[numStrat][numIterationsCur];
				int bestStrat[]=new int[numIterationsCur];
				
				int x=0;
				for(int i=0; i<numOuter; i++) { //outer loop
					
					double meanInnerOutcomes[]=new double[numStrat];
					
					for(int j=0; j<numInner; j++) { //inner loop
						bestStrat[x]=-1;
						
						//calculate conditional NMB of each strategy
						if(analysisType==0) { //EV
							for(int s=0; s<numStrat; s++) {
								results[s][n]=dataResultsIter[0][objectiveDim][s][1][n];
								meanInnerOutcomes[s]+=sign*results[s][n];
								resultsCur[s][x]=sign*results[s][n];
							}
						}
						else { //CEA or BCA
							for(int s=0; s<numStrat; s++) {
								double cost=dataResultsIter[0][costDim][s][1][n];
								double effect=dataResultsIter[0][effectDim][s][1][n]*sign;
								double NMB=WTP*effect-cost;
								results[s][n]=NMB;
								meanInnerOutcomes[s]+=NMB;
								resultsCur[s][x]=NMB;
							}
						}
						
						x++;
						n++;
					} //end inner loop
					
					//calculate conditional expected NMB of each strategy
					double bestInner=Double.NEGATIVE_INFINITY;
					int bestStratInner=-1;
					for(int s=0; s<numStrat; s++) {
						meanInnerOutcomes[s]/=(numInner*1.0);
						if(meanInnerOutcomes[s]>bestInner) {
							bestInner=meanInnerOutcomes[s];
							bestStratInner=s;
						}
					}
					//record best strat 
					for(int z=x-1; z>=(x-numInner); z--) {
						bestStrat[z]=bestStratInner;
					}
				} //end outer loop
				
				//calculate EVPPI
				//get best strategy overall
				double meanBest=0;
				double meanStrat[]=new double[numStrat];
				for(int i=0; i<numIterationsCur; i++) {
					for(int s=0; s<numStrat; s++) {
						meanStrat[s]+=resultsCur[s][i];
					}
					meanBest+=resultsCur[bestStrat[i]][i]; //best strategy identified in inner loop
				}
				
				double maxMean=Double.NEGATIVE_INFINITY;
				for(int s=0; s<numStrat; s++) {
					meanStrat[s]/=(numIterationsCur*1.0);
					if(meanStrat[s]>maxMean) {
						maxMean=meanStrat[s];
					}
				}
				meanBest/=(numIterationsCur*1.0);
				
				evppi[p][0]=meanBest-maxMean;
				
			} //end check if parameter selected
		} //end parameter loop
		
	}
	
	public void getPSAResults(PSAResults psaResults) {
		numIterations=psaResults.numIterations;
		dataParamsIter=psaResults.dataParamsIter;
		dataParamsVal=psaResults.dataParamsVal;
		dataParamsCumDens=psaResults.dataParamsCumDens;
		dataResultsIter=psaResults.dataResultsIter;
		dataResultsVal=psaResults.dataResultsVal;
		dataResultsCumDens=psaResults.dataResultsCumDens;
		results=psaResults.results;
	}
	
	
	public void estimateBins(int numBins) {
		
		//Get results
		sortedResults=new ArrayList<SortedResult>();
		double meanOutcomes[]=new double[numStrat];
		for(int n=0; n<numIterations; n++) {
			SortedResult curResult=new SortedResult(n,numStrat,numParams);
			for(int p=0; p<numParams; p++){ //Record parameter value
				int curDim=paramDims[p];
				curResult.paramVals[p]=new double[curDim];
				for(int z=0; z<curDim; z++) {
					curResult.paramVals[p][z]=dataParamsIter[p][z][1][n];
				}
			} 
			if(analysisType==0) { //EV
				for(int s=0; s<numStrat; s++) {
					curResult.outcomes[s]=results[s][n];
				}
			}
			else { //CEA or BCA
				for(int s=0; s<numStrat; s++) {
					curResult.outcomes[s]=results[s][n];
				}
			}
			
			for(int s=0; s<numStrat; s++) {
				double curRes=sign*results[s][n];
				meanOutcomes[s]+=curRes;
			}

			sortedResults.add(curResult);
		}
		
		//Get best mean strategy
		double bestMean=Double.NEGATIVE_INFINITY;
		for(int s=0; s<numStrat; s++) {
			double curMean=meanOutcomes[s]/=(numIterations*1.0);
			if(curMean>bestMean) {
				bestMean=curMean;
			}
		}
		
		//Calculate EVPPI (for each parameter)
		double bestPPI[][]=new double[numParams][];
		evppi=new double[numParams][];
		
		int reportNumBins=numBins;
		
		int numSamp=numIterations/numBins;
		for(int p=0; p<numParams; p++) {
			int curDim=paramDims[p];
			bestPPI[p]=new double[curDim];
			evppi[p]=new double[curDim];
			for(int z=0; z<curDim; z++) {
				Collections.sort(sortedResults, new ResultComparator(-1,-1)); //order by iteration (reset previous ordering)
				Collections.sort(sortedResults, new ResultComparator(p,z)); //re-sort ascending by parameter
				double curMax[]=new double[numBins];
				double avgMax=0;
				for(int k=0; k<numBins; k++) {
					int index0=k*numSamp;
					int index1=index0+numSamp;
					//calculate strategy mean within bin
					double binMeans[]=new double[numStrat];
					for(int i=index0; i<index1; i++) {
						for(int s=0; s<numStrat; s++) {
							binMeans[s]+=sortedResults.get(i).outcomes[s];
						}
					}
					//get best strategy among bin means
					double binMax=Double.NEGATIVE_INFINITY;
					for(int s=0; s<numStrat; s++) {
						binMeans[s]/=(numSamp*1.0);
						binMax=Math.max(binMax, sign*binMeans[s]);
					}
					curMax[k]=binMax;
					avgMax+=binMax;
				}
				avgMax/=(numBins*1.0);
				bestPPI[p][z]=avgMax;
				evppi[p][z]=avgMax-bestMean;
				//check for 0 range of parameter values
				double range=MathUtils.calcRange(dataParamsVal[p][z][1]);
				if(range==0) { //no variance
					evppi[p][z]=0;
				}
			}
		}
		
		//Calculate EVPPI for varying bin sizes
		ArrayList<Integer> binSizes=new ArrayList<Integer>();
		for(int b=1; b<numIterations/2; b++) {
			if(numIterations%b==0) { //divides evenly
				binSizes.add(b);
			}
		}
		
		evppiBins=new double[numParams][][][];

		for(int p=0; p<numParams; p++) {
			int curDim=paramDims[p];
			evppiBins[p]=new double[curDim][2][binSizes.size()];
			for(int z=0; z<curDim; z++) {
				Collections.sort(sortedResults, new ResultComparator(-1,-1)); //order by iteration (reset previous ordering)
				Collections.sort(sortedResults, new ResultComparator(p,z)); //re-sort ascending by parameter

				for(int b=0; b<binSizes.size(); b++) { //number of bins
					numBins=binSizes.get(b);
					evppiBins[p][z][0][b]=numBins;
					numSamp=numIterations/numBins;
					double curMax[]=new double[numBins];
					double avgMax=0;
					for(int k=0; k<numBins; k++) {
						int index0=k*numSamp;
						int index1=index0+numSamp;
						//calculate strategy mean within bin
						double binMeans[]=new double[numStrat];
						for(int i=index0; i<index1; i++) {
							for(int s=0; s<numStrat; s++) {
								binMeans[s]+=sortedResults.get(i).outcomes[s];
							}
						}
						//get best strategy among bin means
						double binMax=Double.NEGATIVE_INFINITY;
						for(int s=0; s<numStrat; s++) {
							binMeans[s]/=(numSamp*1.0);
							binMax=Math.max(binMax, sign*binMeans[s]);
						}
						curMax[k]=binMax;
						avgMax+=binMax;
					}
					avgMax/=(numBins*1.0);
					evppiBins[p][z][1][b]=avgMax-bestMean;
					//check for 0 range  of parameter values
					double range=MathUtils.calcRange(dataParamsVal[p][z][1]);
					if(range==0) { //no variance
						evppiBins[p][z][1][b]=0;
					}
				}
			}
		}
		
		
		
	}
	
	private void updateProgress(int z, int numIterations, long startTime, ProgressMonitor progress) {
		//Update progress
		double prog=((z)/(numIterations*1.0))*100;
		long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
		remTime=(long) (remTime*(100-prog));
		remTime=remTime/1000;
		String seconds = Integer.toString((int)(remTime % 60));
		String minutes = Integer.toString((int)(remTime/60));
		if(seconds.length()<2){seconds="0"+seconds;}
		if(minutes.length()<2){minutes="0"+minutes;}
		progress.setProgress(z);
		if(z>0) {
			progress.setNote("Time left: "+minutes+":"+seconds);
		}

	}
	
}

class SortedResult{
	int iteration;
	double outcomes[]; //strategy outcome of interest (e.g. NMB)
	double paramVals[][]; //[parameter][dimension]

	SortedResult(int i, int numStrat, int numParams){ //constructor
		iteration=i;
		outcomes=new double[numStrat];
		paramVals=new double[numParams][];
	}
}

class ResultComparator implements Comparator<SortedResult> {
	int pIndex; //parameter index to sort by
	int dIndex; //parameter dimension to sort by

	ResultComparator(int p, int d){ //Constructor
		this.pIndex=p;
		this.dIndex=d;
	}

	@Override public int compare(SortedResult o1, SortedResult o2) {
		if(pIndex==-1) { //sort by iteration
			return(Double.compare(o1.iteration, o2.iteration));
		}
		else { //sort by parameter value
			return(Double.compare(o1.paramVals[pIndex][dIndex], o2.paramVals[pIndex][dIndex]));
		}
	}
}