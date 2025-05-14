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

package main;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import base.AmuaModel;

import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class PSAResults{
	
	public int numIterations;
	public int analysisType;
	public int objective;
	public int objectiveDim, costDim, effectDim;
	public double WTP;
	
	int numParams;
	String paramNames[];
	
	public int numStrat;
	public int numDim;
	int numOutcomes;
	String outcomes[];
	
	/**
	 * Excluding overall
	 */
	int numSubgroups=0;
	
	/**
	 * [Group][Outcome][Strategy][x,y][Iteration]
	 */
	public double dataResultsIter[][][][][], dataResultsVal[][][][][], dataResultsCumDens[][][][][];
	
	int paramDims[];
	/**
	 * [Parameter][Parameter Dimension][Iteration][Value]
	 */
	public double dataParamsIter[][][][], dataParamsVal[][][][], dataParamsCumDens[][][][];
	
	/**
	 * [Strategy][Iteration]
	 */
	public double results[][];
	
	
	String CEAnotes[][][];
	
	public boolean valid;
	
	public int indexLB, indexUB;
	
	public double meanResults[][][], lbResults[][][], ubResults[][][];
	
	
	public PSAResults(AmuaModel myModel, int numIterations){
		this.numIterations=numIterations;
		
		numStrat=myModel.getStrategies();
		numDim=myModel.dimInfo.dimNames.length;
		outcomes=myModel.dimInfo.getOutcomes();
		numOutcomes=outcomes.length;
		
		numParams=myModel.parameters.size();
		
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
		if(analysisType==1){CEAnotes=new String[1+numSubgroups][numStrat][numIterations];} //CEA
		else{CEAnotes=null;}
		
		objective=myModel.dimInfo.objective;
		objectiveDim=myModel.dimInfo.objectiveDim;
		if(analysisType>0) { //CEA or BCA
			costDim=myModel.dimInfo.costDim;
			effectDim=myModel.dimInfo.effectDim;
			WTP=myModel.dimInfo.WTP;
		}
		
		dataResultsIter=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];
		dataResultsVal=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];
		dataResultsCumDens=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];

		dataParamsIter=new double[numParams][][][];
		dataParamsVal=new double[numParams][][][];
		dataParamsCumDens=new double[numParams][][][];
		for(int p=0; p<numParams; p++) {
			int curDim=paramDims[p];
			dataParamsIter[p]=new double[curDim][2][numIterations];
			dataParamsVal[p]=new double[curDim][2][numIterations];
			dataParamsCumDens[p]=new double[curDim][2][numIterations];
		}
		
		results=new double[numStrat][numIterations];
	}

	public void runPSA(AmuaModel myModel, JFrame curFrm, boolean useSeed, int seed, ProgressMonitor progress) throws NumericException, Exception {

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

			progress.setMaximum(numIterations);
			progress.setMillisToDecideToPopup(0);
			progress.setMillisToPopup(0);

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

			//reports=new RunReport[numIterations];

			for(int n=0; n<numIterations; n++){
				//Update progress
				double prog=((n)/(numIterations*1.0))*100;
				long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
				remTime=(long) (remTime*(100-prog));
				remTime=remTime/1000;
				String seconds = Integer.toString((int)(remTime % 60));
				String minutes = Integer.toString((int)(remTime/60));
				if(seconds.length()<2){seconds="0"+seconds;}
				if(minutes.length()<2){minutes="0"+minutes;}
				progress.setProgress(n);
				if(n>0) {
					progress.setNote("Time left: "+minutes+":"+seconds);
				}

				//Sample parameters
				myModel.curGenerator[0]=myModel.generatorParam;
				boolean validParams=false;
				while(validParams==false){
					for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters and orig values
						Parameter curParam=myModel.parameters.get(v);
						curParam.locked=false;
						curParam.value=origValues[v];
					}

					for(int v=0; v<numParams; v++){ //sample all parameters
						Parameter curParam=myModel.parameters.get(v);
						if(curParam.locked==false) {
							curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true);
							curParam.locked=true;
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
						for(int i=0; i<val.nrow; i++) {
							for(int j=0; j<val.ncol; j++) {
								dataParamsIter[v][z][0][n]=n; dataParamsVal[v][z][0][n]=n;
								dataParamsIter[v][z][1][n]=val.matrix[i][j];
								dataParamsVal[v][z][1][n]=val.matrix[i][j];
								z++;
							}
						}
					}
				} 

				//Run model
				myModel.curGenerator=myModel.generatorVar;
				//reports[n]=myModel.runModel(null, false);
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

				if(progress.isCanceled()){  //End loop
					n=numIterations;
					valid=false;
				}
			} //end iteration loop

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
	
	public void summarizeResults() {
		
		meanResults=new double[numSubgroups+1][numOutcomes][numStrat];
		lbResults=new double[numSubgroups+1][numOutcomes][numStrat];
		ubResults=new double[numSubgroups+1][numOutcomes][numStrat];
		int bounds[]=MathUtils.getBoundIndices(numIterations);
		int indexLB=bounds[0], indexUB=bounds[1];
		
		//Sort ordered arrays
		for(int d=0; d<numOutcomes; d++){
			for(int s=0; s<numStrat; s++){
				for(int g=0; g<numSubgroups+1; g++){
					Arrays.sort(dataResultsVal[g][d][s][1]);
					for(int n=0; n<numIterations; n++){
						dataResultsVal[g][d][s][0][n]=n/(numIterations*1.0);
						dataResultsCumDens[g][d][s][0][n]=dataResultsVal[g][d][s][1][n];
						dataResultsCumDens[g][d][s][1][n]=dataResultsVal[g][d][s][0][n];
						meanResults[g][d][s]+=dataResultsVal[g][d][s][1][n];
					}
					meanResults[g][d][s]/=(numIterations*1.0);
					lbResults[g][d][s]=dataResultsVal[g][d][s][1][indexLB];
					ubResults[g][d][s]=dataResultsVal[g][d][s][1][indexUB];
				}
			}
		}
		for(int v=0; v<numParams; v++){
			int curDim=paramDims[v];
			for(int z=0; z<curDim; z++) {
				Arrays.sort(dataParamsVal[v][z][1]);
				for(int n=0; n<numIterations; n++){
					dataParamsVal[v][z][0][n]=n/(numIterations*1.0);
					dataParamsCumDens[v][z][0][n]=dataParamsVal[v][z][1][n];
					dataParamsCumDens[v][z][1][n]=dataParamsVal[v][z][0][n];
				}
			}
		}
		
		//get EVPI results
		for(int n=0; n<numIterations; n++) {
			if(analysisType==0) { //EV
				for(int s=0; s<numStrat; s++) {
					results[s][n]=dataResultsIter[0][objectiveDim][s][1][n];
				}
			}
			else { //CEA or BCA
				for(int s=0; s<numStrat; s++) {
					double cost=dataResultsIter[0][costDim][s][1][n];
					double effect=dataResultsIter[0][effectDim][s][1][n];
					double NMB=WTP*effect-cost;
					results[s][n]=NMB;
				}
			}
		}
	}
	
	public void export(AmuaModel myModel, BufferedWriter out, int group) throws IOException {
		//Headers
		DimInfo info=myModel.dimInfo;
		int numDim=info.dimNames.length;
		int analysisType=info.analysisType;
		int numStrat=myModel.strategyNames.length;
		out.write("Iteration");
		out.write(",Parameters");
		for(int p=0; p<numParams; p++) {
			int curDim=paramDims[p];
			if(curDim==1) { //scalar
				out.write(",\""+paramNames[p]+"\"");
			}
			else { //matrix
				Numeric val=myModel.parameters.get(p).value;
				for(int z=0; z<curDim; z++) {
					out.write(",\""+paramNames[p]+val.getDimLbl(z)+"\"");
				}
			}
		}
		for(int d=0; d<numDim; d++){ //EVs
			out.write(","+info.dimNames[d]);
			for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
		}
		if(analysisType>0){ //CEA or BCA
			if(analysisType==1){out.write(",ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");}
			else if(analysisType==2){out.write(",NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");}
			for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
		}
		out.newLine();

		//Results
		int numPoints=dataResultsIter[group][0][0][0].length;
		for(int i=0; i<numPoints; i++){
			out.write((i+1)+""); //Iteration
			//Parameters
			out.write(",");
			for(int p=0; p<numParams; p++){
				int curDim=paramDims[p];
				for(int z=0; z<curDim; z++) {
					out.write(","+dataParamsIter[p][z][1][i]);
				}
			}
			//Outcomes
			for(int d=0; d<numDim; d++){ //EVs
				out.write(",");
				for(int s=0; s<numStrat; s++){out.write(","+dataResultsIter[group][d][s][1][i]);}
			}
			if(analysisType>0){
				out.write(",");
				if(analysisType==1){ //CEA
					for(int s=0; s<numStrat; s++){
						double icer=dataResultsIter[group][numDim][s][1][i];
						if(!Double.isNaN(icer)){out.write(","+icer);} //valid ICER
						else{out.write(","+CEAnotes[group][s][i]);} //invalid ICER
					}
				}
				else if(analysisType==2){ //BCA
					for(int s=0; s<numStrat; s++){out.write(","+dataResultsIter[group][numDim][s][1][i]);}
				}
			}

			out.newLine();
		}
	}
	
	public void importResults(String path, AmuaModel myModel, JFrame curFrm, ProgressMonitor progress) throws IOException {
		valid=true;
		
		progress.setMaximum(numIterations);
		progress.setNote("Importing PSA Results");
		
		FileInputStream fstream = new FileInputStream(path);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		//map column headers
		String strLine=br.readLine(); //Headers
		String colNames[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		int numCol=colNames.length;
		ArrayList<String> listNames=new ArrayList<String>();
		for(int c=0; c<numCol; c++) {
			listNames.add(colNames[c].replaceAll("\"","")); //strip quotes
		}
		
		///map parameters
		int paramIndex[]=new int[numParams];
		for(int p=0; p<numParams; p++) {
			if(paramDims[p]==1) { //scalar
				int index=listNames.indexOf(paramNames[p]);
				if(index==-1) {
					valid=false;
					JOptionPane.showMessageDialog(curFrm, "Parameter not found: "+paramNames[p]);
				}
				else {
					paramIndex[p]=index;
				}
			}
			else { //matrix
				Parameter curParam=myModel.parameters.get(p);
				Numeric val=curParam.value;
				String suffix="[0]"; //default to vector
				if(val.nrow>1 && val.ncol>1) { //matrix
					suffix="[0,0]";
				}
				int index=listNames.indexOf(paramNames[p]+suffix); //first index
				if(index==-1) {
					valid=false;
					JOptionPane.showMessageDialog(curFrm, "Parameter not found: "+paramNames[p]+suffix);
				}
				else {
					paramIndex[p]=index;
					//check all dimensions
					for(int z=0; z<paramDims[p]; z++) {
						String curLbl=paramNames[p]+val.getDimLbl(z);
						String strCol=colNames[index+z].replaceAll("\"", ""); //strip quotes
						if(!strCol.equals(curLbl)) {
							valid=false;
							JOptionPane.showMessageDialog(curFrm, "Parameter not found: "+curLbl);
						}
					}
				}
			}
		}
		
		//map outcomes
		int dimIndex[]=new int[numDim];
		for(int d=0; d<numDim; d++){ //EVs
			int index=listNames.indexOf(myModel.dimInfo.dimNames[d]);
			if(index==-1) {
				valid=false;
				JOptionPane.showMessageDialog(curFrm, "Outcome not found: "+myModel.dimInfo.dimNames[d]);
			}
			else {
				dimIndex[d]=index;
				//check all strategies
				for(int s=0; s<numStrat; s++) {
					String curLbl=myModel.strategyNames[s];
					String strCol=colNames[index+s+1].replaceAll("\"",""); //strip quotes
					if(!strCol.equals(curLbl)) {
						valid=false;
						JOptionPane.showMessageDialog(curFrm, myModel.dimInfo.dimNames[d]+" strategy not found: "+curLbl);
					}
				}
			}
		}
		
		
		if(valid==true) {

			long startTime=System.currentTimeMillis();


			//read values
			for(int n=0; n<numIterations; n++) {
				//Update progress
				double prog=((n+1)/(numIterations*1.0))*100;
				long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
				remTime=(long) (remTime*(100-prog));
				remTime=remTime/1000;
				String seconds = Integer.toString((int)(remTime % 60));
				String minutes = Integer.toString((int)(remTime/60));
				if(seconds.length()<2){seconds="0"+seconds;}
				if(minutes.length()<2){minutes="0"+minutes;}
				progress.setProgress(n+1);
				progress.setNote("Time left: "+minutes+":"+seconds);

				strLine=br.readLine();
				String data[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				
				//get parameter values
				for(int p=0; p<numParams; p++) {
					int curDim=paramDims[p];
					if(curDim==1) { //scalar
						dataParamsIter[p][0][0][n]=n; dataParamsVal[p][0][0][n]=n;
						try{
							dataParamsIter[p][0][1][n]=Double.parseDouble(data[paramIndex[p]]);
						} catch(Exception e){
							dataParamsIter[p][0][1][n]=Double.NaN;
						}
						dataParamsVal[p][0][1][n]=dataParamsIter[p][0][1][n];
					}
					else { //matrix
						Numeric val=myModel.parameters.get(p).value;
						int z=0;
						for(int i=0; i<val.nrow; i++) {
							for(int j=0; j<val.ncol; j++) {
								dataParamsIter[p][z][0][n]=n; dataParamsVal[p][z][0][n]=n;
								double curVal=Double.parseDouble(data[paramIndex[p]+z]);
								dataParamsIter[p][z][1][n]=curVal;
								dataParamsVal[p][z][1][n]=curVal;
								z++;
							}
						}
					}
				}
				
				//get model outcomes
				for(int d=0; d<numDim; d++){
					for(int s=0; s<numStrat; s++){
						//overall
						dataResultsIter[0][d][s][0][n]=n; dataResultsVal[0][d][s][0][n]=n;
						double curOutcome=Double.parseDouble(data[dimIndex[d]+1+s]);
						dataResultsIter[0][d][s][1][n]=curOutcome; dataResultsVal[0][d][s][1][n]=curOutcome;
					}
				}

			}
		}
		
		br.close();
		progress.close();
		curFrm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
	}
}