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

import base.AmuaModel;
import base.RunReport;
import main.Constraint;
import main.MersenneTwisterFast;
import main.Parameter;
import math.Interpreter;
import math.Numeric;

public class ClusterPSA{
	
		
	//Constructor
	public ClusterPSA(AmuaModel myModel, ClusterInputs inputs, String outpath, int iteration){
		try {
			System.out.println("Running PSA...");
			
			myModel.sampleParam=true;
			myModel.generatorParam=new MersenneTwisterFast();
			if(myModel.curGenerator==null){
				myModel.curGenerator=new MersenneTwisterFast[1];
			}
			myModel.curGenerator[0]=myModel.generatorParam;
			if(inputs.seedParamRNG){
				int seed=inputs.paramSeed;
				myModel.generatorParam.setSeed(seed+iteration);
			}
			myModel.simParamSets=false; //turn off use parameter sets (no looping through)
			int numSets=-1;
			if(inputs.sampleParamSets) {
				numSets=myModel.parameterSets.length;
			}
			if(inputs.seedIterationRNG) { //seed by iteration
				myModel.CRN=true;
				myModel.crnSeed=iteration;
			}
			
			int numParams=myModel.parameters.size();
			
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
			
			//Sample parameters
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
					ArrayList<String> errors=myModel.parseModel();
					if(errors.size()!=0) {
						validParams=false;
					}
				}
			} //end sample params
			if(inputs.sampleParamSets) {
				int curSet=myModel.generatorParam.nextInt(numSets);
				myModel.parameterSets[curSet].setParameters(myModel);
			}
			System.out.println("done");
			
			//Run model
			myModel.curGenerator=myModel.generatorVar;
			RunReport report=myModel.runModel(null, false);
			
			System.out.println("done");
			
			System.out.println("Writing output...");
			
			//get results
			report.getResults(true);
			report.writeSummary(outpath, iteration);
			report.write(outpath,iteration);
			
			//write params
			FileWriter fstream = new FileWriter(outpath+"params_"+iteration+".csv"); //Create new file
			BufferedWriter outP = new BufferedWriter(fstream);
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
			
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
		
}