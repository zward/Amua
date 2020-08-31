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
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import base.AmuaModel;
import main.Constraint;
import main.Parameter;
import main.Table;
import main.Variable;
import markov.MarkovNode;
import tree.TreeNode;

public class ClusterRun{
	
		
	//Constructor
	public ClusterRun(String args[], String version){
		try {
			String model = null;
			String inputs = null;
			String outpath = null;
			int iteration=1;
			//Ensure args is not empty
			if(args.length>0){
				model=args[0];
				inputs=args[1];
				outpath=args[2];
				iteration=Integer.parseInt(args[3]);
			}

			System.out.println("===========================================");
			System.out.println("Amua "+version);
			System.out.println("===========================================");
			System.out.println("Model: "+model);
			System.out.println("Inputs: "+inputs);
			System.out.println("Output path: "+outpath);
			System.out.println("Iteration: "+iteration);

			//open model
			System.out.println("Opening model...");
			JAXBContext context = JAXBContext.newInstance(AmuaModel.class);
			Unmarshaller un = context.createUnmarshaller();
			AmuaModel myModel = (AmuaModel) un.unmarshal(new File(model));
			myModel.cluster=true;
			
			//initialize model objects
			if(myModel.parameters==null) {myModel.parameters=new ArrayList<Parameter>();}
			if(myModel.variables==null) {myModel.variables=new ArrayList<Variable>();}
			if(myModel.tables==null) {myModel.tables=new ArrayList<Table>();}
			if(myModel.constraints==null) {myModel.constraints=new ArrayList<Constraint>();}
			myModel.innateVariables=new ArrayList<Variable>();
			if(myModel.subgroupNames==null){
				myModel.subgroupNames=new ArrayList<String>();
				myModel.subgroupDefinitions=new ArrayList<String>();
			}
			//parameter sets
			if(myModel.parameterNames!=null){
				int numSets=myModel.parameterSets.length;
				for(int i=0; i<numSets; i++){
					myModel.parameterSets[i].parseXMLValues();
				}
			}
			
			if(myModel.type==0) { //Decision Tree
				myModel.tree.myModel=myModel;
				TreeNode root=myModel.tree.nodes.get(0);
				root.cost=new String[myModel.dimInfo.dimNames.length];
				root.numDimensions=myModel.dimInfo.dimNames.length;
				int size=myModel.tree.nodes.size();
				for(int i=1; i<size; i++){ //Skip root
					TreeNode curNode=myModel.tree.nodes.get(i);
					curNode.myModel=myModel;
					curNode.numDimensions=myModel.dimInfo.dimNames.length;
					curNode.tree=myModel.tree;
				}
			}
			else if(myModel.type==1) { //Markov
				myModel.markov.myModel=myModel;
				myModel.addT();
				
				MarkovNode root=myModel.markov.nodes.get(0);
				root.cost=new String[myModel.dimInfo.dimNames.length];
				root.numDimensions=myModel.dimInfo.dimNames.length;
				int numChildren=root.childIndices.size();
				for(int i=0; i<numChildren; i++){ //Skip root
					int index=root.childIndices.get(i);
					MarkovNode node=myModel.markov.nodes.get(index);
					openMarkovNode(node, myModel);
				}
			}
			
			//Construct splines if needed
			for(int t=0; t<myModel.tables.size(); t++){
				Table curTable=myModel.tables.get(t);
				curTable.myModel=myModel;
				if(curTable.interpolate!=null && curTable.interpolate.matches("Cubic Splines")){
					curTable.constructSplines();
				}
			}
			//check model
			ArrayList<String> errorsBase=myModel.parseModel();
			if(errorsBase.size()>0){
				int numErr=errorsBase.size();
				System.out.println(numErr+" errors found!");
				for(int i=0; i<numErr; i++) {
					System.out.println(errorsBase.get(i));
				}
			}
			else { //no errors
				System.out.println("Model checked!");
		
				//read inputs
				System.out.println("Reading inputs...");
				JAXBContext context2 = JAXBContext.newInstance(ClusterInputs.class);
				Unmarshaller un2 = context2.createUnmarshaller();
				ClusterInputs myInputs = (ClusterInputs) un2.unmarshal(new File(inputs));
				System.out.println("done");

				System.out.println("Threads: "+myModel.numThreads);
				System.out.println("Simulation Seed: "+myModel.crnSeed);

				//perform operation
				if(myInputs.operation.contains("PSA")) {
					if(iteration==1) { //model properties
						writeModelProperties(myModel, outpath);
					}
					new ClusterPSA(myModel, myInputs, outpath, iteration);
				}

			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private void openMarkovNode(MarkovNode node, AmuaModel myModel){
		if(node.type==1){ //chain
			node.chain=node; //set chain
			if(node.stateNames==null) { //no state names saved
				node.stateNames=new ArrayList<String>();
			}
		} 
		node.myModel=myModel;
		node.numDimensions=myModel.dimInfo.dimNames.length;
		node.tree=myModel.markov;
		int numChildren=node.childIndices.size();
		for(int i=0; i<numChildren; i++){
			int index=node.childIndices.get(i);
			MarkovNode child=myModel.markov.nodes.get(index);
			child.chain=node.chain; //pass chain reference
			openMarkovNode(child, myModel);
		}
	}
	
	private void writeModelProperties(AmuaModel myModel, String outpath) {
		try {
			FileWriter fstream = new FileWriter(outpath+"Properties.csv"); //Create new file
			BufferedWriter out = new BufferedWriter(fstream);

			//General
			out.write("Model Name,"+myModel.name); out.newLine();
			if(myModel.type==0) {out.write("Model Type,Decision Tree"); out.newLine();}
			else if(myModel.type==1) {out.write("Model Type,Markov Model"); out.newLine();}
			out.write("Created by,"+myModel.meta.author); out.newLine();
			out.write("Created,"+myModel.meta.dateCreated); out.newLine();
			out.write("Version created,"+myModel.meta.versionCreated); out.newLine();
			out.write("Modified by,"+myModel.meta.modifier); out.newLine();
			out.write("Modified,"+myModel.meta.dateModified); out.newLine();
			out.write("Version modified,"+myModel.meta.versionModified); out.newLine();
			out.newLine();
			
			//Analysis
			int numDim=myModel.dimInfo.dimNames.length;
			out.write("Dimension,Symbol,Decimals"); out.newLine();
			for(int d=0; d<numDim; d++) {
				out.write(myModel.dimInfo.dimNames[d]+",");
				out.write(myModel.dimInfo.dimSymbols[d]+",");
				out.write(myModel.dimInfo.decimals[d]+"");
				out.newLine();
			}
			out.newLine();
			
			if(myModel.dimInfo.analysisType==0) {
				out.write("Analysis type,Expected Value (EV)"); out.newLine();
				if(myModel.dimInfo.objective==0) {out.write("Objective,Maximize"); out.newLine();}
				else {out.write("Objective,Minimize"); out.newLine();}
				out.write("Outcome,"+myModel.dimInfo.dimNames[myModel.dimInfo.objectiveDim]); out.newLine();
			}
			else if(myModel.dimInfo.analysisType==1) {
				out.write("Analysis type,Cost-Effectiveness Analysis (CEA)"); out.newLine();
				out.write("Cost,"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]); out.newLine();
				out.write("Effect,"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]); out.newLine();
				out.write("Baseline Strategy,"+myModel.dimInfo.baseScenario); out.newLine();
				out.write("Willingness-to-pay (WTP),"+myModel.dimInfo.WTP); out.newLine();
			}
			else if(myModel.dimInfo.analysisType==2) {
				out.write("Analysis type,Benefit-Cost Analysis (BCA)\""); out.newLine();
				out.write("Cost,"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]); out.newLine();
				out.write("Benefit,"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]); out.newLine();
				out.write("Willingness-to-pay (WTP),"+myModel.dimInfo.WTP); out.newLine();
			}
			else if(myModel.dimInfo.analysisType==3) {
				out.write("Analysis type,Extended Cost-Effectiveness Analysis (CEA)"); out.newLine();
				out.write("Cost,"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]); out.newLine();
				out.write("Effect,"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]); out.newLine();
				out.write("Baseline Strategy,"+myModel.dimInfo.baseScenario); out.newLine();
				out.write("Willingness-to-pay (WTP),"+myModel.dimInfo.WTP); out.newLine();
				out.write("Additional Dimension,"+myModel.dimInfo.dimNames[myModel.dimInfo.extendedDim]); out.newLine();
			}
			out.newLine();
			
			//Simulation
			if(myModel.simType==0) { //cohort
				out.write("Simulation type,Cohort (Deterministic)"); out.newLine();
				out.write("Cohort size,"+myModel.cohortSize); out.newLine();
			}
			else if(myModel.simType==1) { //monte carlo
				out.write("Simulation type,Monte Carlo (Stochastic)"); out.newLine();
				out.write("# simulations,"+myModel.cohortSize); out.newLine();
				out.write("Seed RNG,"+myModel.CRN); out.newLine();
				if(myModel.CRN) {out.write("Seed,"+myModel.crnSeed); out.newLine();}
				out.write("Display individual-level results,"+myModel.displayIndResults); out.newLine();
			}
			if(myModel.numThreads==1) {
				out.write("Multi-thread simulation,"+false); out.newLine();
			}
			else {
				out.write("Multi-thread simulation,"+true); out.newLine();
				out.write("Threads,"+myModel.numThreads); out.newLine();
			}
			out.newLine();
			
			//Markov
			if(myModel.type==1) {
				out.write("Max cycles,"+myModel.markov.maxCycles); out.newLine();
				out.write("Half-cycle correction,"+myModel.markov.halfCycleCorrection); out.newLine();
				out.write("Discount rewards,"+myModel.markov.discountRewards); out.newLine();
				if(myModel.markov.discountRewards) {
					out.write("Dimension,Discount Rate(%)"); out.newLine();
					for(int d=0; d<numDim; d++) {
						out.write(myModel.dimInfo.dimNames[d]+",");
						out.write(myModel.markov.discountRates[d]+"");
						out.newLine();
					}
					out.write("Discount start cycle,"+myModel.markov.discountStartCycle); out.newLine();
					out.write("Cycles per year,"+myModel.markov.cyclesPerYear); out.newLine();
				}
			}
			
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
		
}