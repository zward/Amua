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

package export_R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class RTreeMonteCarlo{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	RModel rModel;
	String dimNames[];

	//Constructor
	public RTreeMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			tree=myModel.tree;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
			dimNames=myModel.dimInfo.dimNames;
			numDimensions=dimNames.length;
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(dir+"main.R"); //Create new file
			out = new BufferedWriter(fstream);

			rModel=new RModel(dir,out,myModel);

			rModel.writeProperties();
			writeLine("");
			rModel.writeTableClass();
			rModel.writeTables(tableFormat);
			rModel.writeParameters();
			
			int numVars=myModel.variables.size();
			rModel.writeVariables();

			//Define strategies
			writeLine("# Define strategies");
			writeLine("numSim <- "+myModel.cohortSize);
			int numStrat=myModel.getStrategies();
			for(int s=0; s<numStrat; s++){
				writeLine("");
				writeLine("# "+myModel.strategyNames[s]);
				if(myModel.CRN){writeLine("set.seed("+myModel.crnSeed+")");}
				writeLine("# Initialize outcomes");
				for(int d=0; d<dimNames.length; d++){
					writeLine(dimNames[d]+" <- 0");
				}
				writeLine("#Run Monte Carlo simulation");
				writeLine("for(i in 1:numSim) {");
				if(numVars>0){
					writeLine("  # Initialize variables");
					//independent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==true){
							writeLine("  "+curVar.name+" <- "+rModel.translate(curVar.expression,false));
						}
					}
					//dependent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==false){
							writeLine("  "+curVar.name+" <- "+rModel.translate(curVar.expression,false));
						}
					}
				}
				//define nodes
				int stratIndex=myModel.tree.nodes.get(0).childIndices.get(s);
				TreeNode curStrategy=myModel.tree.nodes.get(stratIndex);
				defineNode(curStrategy);
				writeLine("}  # end Monte Carlo loop");
				//Print results
				writeLine("# Print results");
				writeLine("print(\"Strategy: "+myModel.strategyNames[s]+"\")");
				for(int d=0; d<numDimensions; d++){
					writeLine("print(paste(\""+dimNames[d]+": \", "+dimNames[d]+"))");
				}
				writeLine("print(\"\")");
			}
			
			rModel.defineFunctions();
			
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(TreeNode curNode){
		try{

			int level=curNode.level; //tab indents

			if(curNode.hasVarUpdates){ //update variables
				writeLine("# Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				ArrayList<Variable> dependents=new ArrayList<Variable>();
				for(int u=0; u<numUpdates; u++){
					writeLine(rModel.translate(updates[u],false)+"  # Orig: "+updates[u], level);
					for(int d=0; d<curNode.curVariableUpdates[u].variable.dependents.size(); d++){
						Variable curDep=curNode.curVariableUpdates[u].variable.dependents.get(d);
						if(!dependents.contains(curDep)){
							dependents.add(curDep);
						}
					}
				}
				//update dependent variables
				if(dependents.size()>0){
					writeLine("# Update dependent variables",level);
					for(int d=0; d<dependents.size(); d++){
						Variable curVar=dependents.get(d);
						writeLine(curVar.name+" <- "+rModel.translate(curVar.expression,false),level);
					}
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("# Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim=dimNames[d];
					writeLine(dim+" <- "+dim+" + "+rModel.translate(curNode.cost[d],false),level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //terminal node
				writeLine("# Update payoffs",level);
				for(int d=0; d<numDimensions; d++){
					String dim=dimNames[d];
					writeLine(dim+" <- "+dim+" + "+rModel.translate(curNode.payoff[d],false),level);
				}
			}
			else{
				TreeNode children[]=new TreeNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=tree.nodes.get(index);
				}
				writeLine("# Calculate child probs",level);
				writeLine("childProbs <- rep(0, "+numChildren+")",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine("childProbs[1] <- "+rModel.translate(prob,false)+"  # Prob "+children[i].name,level);}
						else{writeLine("childProbs["+(i+1)+"] <- childProbs["+i+"] + "+rModel.translate(prob,false)+"  # Prob "+children[i].name,level);}
					}
				}
				
				writeLine("# Sample event",level);
				writeLine("rand <- runif(1)",level);
				if(compIndex==-1){ //no comp prob
					writeLine("if (rand < childProbs[1]) {  # "+children[0].name,level);
					defineNode(children[0]);
					writeLine("}  # end "+children[0].name,level);
					for(int i=1; i<numChildren; i++){
						writeLine("else if (rand < childProbs["+(i+1)+"]) {  # "+children[i].name,level);
						defineNode(children[i]);
						writeLine("}  # end "+children[i].name,level);
					}
				}
				else{ //write comp prob as last 'else'
					if(compIndex!=0){
						writeLine("if (rand < childProbs[1]) {  # "+children[0].name,level);
						defineNode(children[0]);
						writeLine("}  # end "+children[0].name,level);
						for(int i=1; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if (rand < childProbs["+(i+1)+"]) {  # "+children[i].name,level);
								defineNode(children[i]);
								writeLine("}  # end "+children[i].name,level);
							}
						}
					}
					else{ //compIndex == 0
						writeLine("if (rand < childProbs[2]) {  # "+children[1].name,level);
						defineNode(children[1]);
						writeLine("}  # end "+children[1].name,level);
						for(int i=2; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if (rand < childProbs["+(i+1)+"]) {  # "+children[i].name,level);
								defineNode(children[i]);
								writeLine("# end "+children[i].name,level);
							}
						}
					}
					//write comp index
					writeLine("else {  # "+children[compIndex].name,level);
					defineNode(children[compIndex]);
					writeLine("}  # end "+children[compIndex].name,level);
				}
			}
		}catch(Exception e){
			recordError(e);
		}
	}

	
	
	
	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			recordError(e);
		}
	}

	private void writeLine(String line, int tab){
		try{
			for(int t=0; t<tab; t++){out.write("  ");}
			out.write(line); out.newLine();
		}catch(Exception e){
			recordError(e);
		}
	}
	
	private void recordError(Exception e){
		e.printStackTrace();
		errorLog.recordError(e);
		JOptionPane.showMessageDialog(null, e.toString());
	}

}
