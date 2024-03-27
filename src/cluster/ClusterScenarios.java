/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2020 Zachary J. Ward
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

package cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import base.AmuaModel;
import base.MicroStatsSummary;
import base.RunReport;
import base.RunReportSummary;
import main.Constraint;
import main.DimInfo;
import main.MersenneTwisterFast;
import main.Parameter;
import main.Scenario;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;

public class ClusterScenarios{

	AmuaModel myModel;	

	int numParams, numVars, numConstraints;
	//original model settings
	boolean origCRN;
	int origSeed, origCohortSize;

	//analysis
	int origAnalysisType, origObjective, origObjectiveDim, origCostDim, origEffectDim;
	double origWTP;
	String origBaseScenario;
	int origExtendedDim;

	//markov
	boolean origHalfCycleCorrection, origDiscountRewards;
	double origDiscountRates[];
	int origDiscountStartCycle;
	boolean origShowTrace;

	//objects
	String origParams[], origVars[];

	//Constructor
	public ClusterScenarios(AmuaModel myModel, ClusterInputs inputs, String outpath, int iteration){
		try {
			this.myModel=myModel;

			System.out.println("Running Scenarios...");

			ArrayList<Scenario> scenarios=new ArrayList<Scenario>();
			if(myModel.scenarios!=null){
				for(int i=0; i<myModel.scenarios.size(); i++){
					scenarios.add(myModel.scenarios.get(i).copy());
				}
			}

			DimInfo info=myModel.dimInfo;
			int numDim=myModel.dimInfo.dimNames.length;
			int numOutcomes=numDim+2; //Add ICER and NMB

			//Get orig settings
			origCohortSize=myModel.cohortSize;
			origCRN=myModel.CRN;
			origSeed=myModel.crnSeed;

			//analysis
			origAnalysisType=myModel.dimInfo.analysisType;
			origObjective=myModel.dimInfo.objective;
			origObjectiveDim=myModel.dimInfo.objectiveDim;
			origCostDim=myModel.dimInfo.costDim;
			origEffectDim=myModel.dimInfo.effectDim;
			origWTP=myModel.dimInfo.WTP;
			origBaseScenario=myModel.dimInfo.baseScenario;
			origExtendedDim=myModel.dimInfo.extendedDim;

			//markov
			if(myModel.type==1) {
				origHalfCycleCorrection=myModel.markov.halfCycleCorrection;
				origDiscountRewards=myModel.markov.discountRewards;
				origDiscountRates=new double[numDim];
				for(int d=0; d<numDim; d++) {
					origDiscountRates[d]=myModel.markov.discountRates[d];
				}
				origDiscountStartCycle=myModel.markov.discountStartCycle;

				origShowTrace=myModel.markov.showTrace;
				myModel.markov.showTrace=false; //don't show trace
			}

			//objects
			numParams=myModel.parameters.size();
			origParams=new String[numParams];
			for(int i=0; i<numParams; i++){
				origParams[i]=myModel.parameters.get(i).expression;
			}
			numVars=myModel.variables.size();
			origVars=new String[numVars];
			for(int i=0; i<numVars; i++){
				origVars[i]=myModel.variables.get(i).expression;
			}

			int numScenarios=scenarios.size();
			String scenarioNames[]=new String[numScenarios];
			RunReport[] runReports=new RunReport[numScenarios];

			for(int n=0; n<numScenarios; n++){
				//Reset orig object expressions
				resetModel();
				myModel.validateModelObjects();

				//Apply updates
				Scenario curScenario=scenarios.get(n);
				System.out.println("Running Scenario: "+curScenario.name);
				scenarioNames[n]=curScenario.name;
				curScenario.parseUpdates(myModel);
				curScenario.applyUpdates(myModel);

				//Check for errors
				ArrayList<String> errors=myModel.parseModel();
				if(errors.size()>0){
					System.out.println("Errors found in run: "+curScenario.name+"!");
				}

				//Run model
				if(myModel.curGenerator==null){
					myModel.curGenerator=new MersenneTwisterFast[1];
				}
				myModel.cohortSize=curScenario.cohortSize;
				myModel.CRN=curScenario.crn1;
				myModel.crnSeed=curScenario.seed1;
				if(inputs.seedIterationRNG) {
					myModel.crnSeed=iteration;
				}

				//analysis
				myModel.dimInfo.analysisType=curScenario.analysisType;
				myModel.dimInfo.objective=curScenario.objective;
				myModel.dimInfo.objectiveDim=curScenario.objectiveDim;
				myModel.dimInfo.costDim=curScenario.costDim;
				myModel.dimInfo.effectDim=curScenario.effectDim;
				myModel.dimInfo.WTP=curScenario.WTP;
				myModel.dimInfo.baseScenario=curScenario.baseScenario;
				myModel.dimInfo.extendedDim=curScenario.extendedDim;

				//markov
				if(myModel.type==1) {
					myModel.markov.halfCycleCorrection=curScenario.halfCycleCorrection;
					myModel.markov.discountRewards=curScenario.discountRewards;
					if(curScenario.discountRewards) {
						if(curScenario.discountRates==null || curScenario.discountRates.length!=numDim) {
							System.out.println("Error: Incorrect model dimensions in Scenario "+curScenario.name+" !");
						}
						else {
							for(int d=0; d<numDim; d++) {
								myModel.markov.discountRates[d]=curScenario.discountRates[d];
							}
						}
						myModel.markov.discountStartCycle=curScenario.discountStartCycle;
					}
				}

				Numeric origValues[] = null;
				if(curScenario.sampleParams){
					myModel.sampleParam=true;
					myModel.generatorParam=new MersenneTwisterFast();
					if(curScenario.crn2){
						myModel.generatorParam.setSeed(curScenario.seed2);
					}
					if(inputs.seedIterationRNG) {
						myModel.generatorParam.setSeed(iteration+1);
					}

					//Parse constraints
					numConstraints=myModel.constraints.size();
					for(int c=0; c<numConstraints; c++){
						myModel.constraints.get(c).parseConstraints();
					}
					//Get orig values for all parameters
					origValues=new Numeric[numParams];
					for(int v=0; v<numParams; v++){
						origValues[v]=myModel.parameters.get(v).value.copy();
					}
				}

				if(curScenario.sampleParams){
					System.out.println("Sampling parameters...");
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
				}
				else if(curScenario.useParamSets && myModel.parameterSets!=null) {
					int numSets=myModel.parameterSets.length;
					int curSet=iteration%numSets; //keep looping over sets
					myModel.parameterSets[curSet].setParameters(myModel);
					curScenario.overwriteParams(myModel);
				}

				//run model
				myModel.curGenerator=myModel.generatorVar;
				RunReport curReport=myModel.runModel(null, false);
				runReports[n]=curReport;
				
				System.out.println("Writing output...");
				String curName=scenarioNames[n];
				
				//write results
				runReports[n].getResults(true);
				runReports[n].writeSummary(outpath+curName+"_", iteration);
				runReports[n].write(outpath+curName+"_",iteration);
				
				//write params
				FileWriter fstreamP = new FileWriter(outpath+curName+"_params_"+iteration+".csv"); //Create new file
				BufferedWriter outP = new BufferedWriter(fstreamP);
				//headers
				for(int v=0; v<numParams; v++) {
					outP.write(myModel.parameters.get(v).name+",");
				}
				outP.newLine();
				//values
				for(int v=0; v<numParams; v++) {
					outP.write(myModel.parameters.get(v).value.saveAsCSVString()+",");
				}
				outP.newLine();
				outP.close();

				System.out.println("done");
				
			} //end scenarios loop
			

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void resetModel(){
		myModel.cohortSize=origCohortSize;
		myModel.CRN=origCRN;
		myModel.crnSeed=origSeed;

		//analysis
		myModel.dimInfo.analysisType=origAnalysisType;
		myModel.dimInfo.objective=origObjective;
		myModel.dimInfo.objectiveDim=origObjectiveDim;
		myModel.dimInfo.costDim=origCostDim;
		myModel.dimInfo.effectDim=origEffectDim;
		myModel.dimInfo.WTP=origWTP;
		myModel.dimInfo.baseScenario=origBaseScenario;
		myModel.dimInfo.extendedDim=origExtendedDim;

		//markov
		if(myModel.type==1) {
			myModel.markov.halfCycleCorrection=origHalfCycleCorrection;
			myModel.markov.discountRewards=origDiscountRewards;
			int numDim=myModel.dimInfo.dimNames.length;
			for(int d=0; d<numDim; d++) {
				myModel.markov.discountRates[d]=origDiscountRates[d];
			}
			myModel.markov.discountStartCycle=origDiscountStartCycle;
		}

		//objects
		for(int i=0; i<numParams; i++){
			myModel.parameters.get(i).expression=origParams[i];
		}
		for(int i=0; i<numVars; i++){
			myModel.variables.get(i).expression=origVars[i];
		}
	}

}