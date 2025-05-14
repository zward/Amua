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
import markov.MarkovNode;
import markov.MarkovTree;

public class RMarkovCohort{

	BufferedWriter out;
	AmuaModel myModel;
	MarkovTree markov;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	RModel rModel;
	String dimNames[];
	MarkovNode states[];

	//Constructor
	public RMarkovCohort(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			markov=myModel.markov;
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
			writeLine("### Define functions");
			writeLine("source(\"functions.R\")");
			writeLine("");
			rModel.writeParameters();
			
			int numVars=myModel.variables.size();
			rModel.writeVariables();
			writeLine("");
			writeLine("cohortSize <- "+myModel.cohortSize);
			writeLine("");
			writeLine("# Initialize discount rates");
			if(myModel.markov.discountRewards){
				out.write("discountRates <- c(");
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+", ");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+")"); out.newLine();
				writeLine("startDiscountCycle <- "+myModel.markov.discountStartCycle);
			}
			else{
				out.write("discountRates <- rep(1, "+numDimensions+")"); out.newLine();
				writeLine("startDiscountCycle <- 0");
			}
			if(myModel.markov.halfCycleCorrection){writeLine("halfCycle <- TRUE");}
			else{writeLine("halfCycle <- FALSE");}
			
			//Define Markov Chains
			ArrayList<MarkovNode> chains=new ArrayList<MarkovNode>();
			for(int n=0; n<markov.nodes.size(); n++){
				MarkovNode curNode=markov.nodes.get(n);
				if(curNode.type==1){chains.add(curNode);}
			}
					
			for(int c=0; c<chains.size(); c++){ //for each chain
				MarkovNode curChain=chains.get(c);
				int numStates=curChain.childIndices.size();
				states=new MarkovNode[numStates];
				for(int s=0; s<numStates; s++){
					int index=curChain.childIndices.get(s);
					states[s]=markov.nodes.get(index);
				}
				writeLine("");
				writeLine("########## Markov Chain: "+curChain.name+" ##########");
				writeLine("print(\"Running Markov Chain: "+curChain.name+" \")");
				writeLine("numStates <- "+numStates);
				out.write("colNames <- c(\"Cycle\"");
				for(int s=0; s<numStates; s++){out.write(", \""+states[s].name+"\"");}
				for(int d=0; d<numDimensions; d++){
					out.write(", \"Cycle_"+dimNames[d]+"\"");	out.write(", \"Cum_"+dimNames[d]+"\"");
					out.write(", \"Cycle_Dis_"+dimNames[d]+"\"");	out.write(", \"Cum_Dis_"+dimNames[d]+"\"");
				}
				out.write(")"); out.newLine();
				int ncol=numStates+numDimensions*4+1;
				writeLine("trace <- data.frame(matrix(nrow=0, ncol="+ncol+"))");
				writeLine("names(trace) <- colNames");
				writeLine("# Initialize prevalence");
				writeLine("curPrev <- c()");
				for(int s=0; s<numStates; s++){
					writeLine("curPrev["+(s+1)+"] <- cohortSize * "+rModel.translate(states[s].prob,false)+"  # "+states[s].name);
				}
				writeLine("newPrev <- curPrev  # copy inital prev");
				if(numVars>0){
					writeLine("# Initialize variables");
					writeLine("t <- 0  # initialize cycle");
					//independent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==true){
							writeLine(curVar.name+" <- "+rModel.translate(curVar.expression,false));
						}
					}
					//dependent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==false){
							writeLine(curVar.name+" <- "+rModel.translate(curVar.expression,false));
						}
					}
				}
				writeLine("");
				writeLine("# Run chain");
				writeLine("# Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine(curChain.nameExport+"_"+dimNames[d]+" <- 0; "+curChain.nameExport+"_Dis_"+dimNames[d]+" <- 0");
				}
				writeLine("t <- 0  # initialize cycle");
				writeLine("terminate <- FALSE");
				writeLine("while (terminate == FALSE) {");
				writeLine("  # Update progress");
				writeLine("  if (t %% 10 == 0){");
				writeLine("    cat(t, sep=\"\")");
				writeLine("  } else {");
				writeLine("    cat(\".\", sep=\"\")");
				writeLine("  }");
				writeLine("");
				
				writeLine("  # Cycle outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("  cycle"+dimNames[d]+" <- 0; cycle"+dimNames[d]+"_dis <- 0");
				}
				
				//cycle variable updates
				if(curChain.hasVarUpdates){
					writeLine("");
					writeLine("  # Cycle variable updates");
					String updates[]=curChain.varUpdates.split(";");
					int numUpdates=updates.length;
					ArrayList<Variable> dependents=new ArrayList<Variable>();
					for(int u=0; u<numUpdates; u++){
						writeLine(rModel.translate(updates[u],false)+"  # Orig: "+updates[u], 1);
						for(int d=0; d<curChain.curVariableUpdates[u].variable.dependents.size(); d++){
							Variable curDep=curChain.curVariableUpdates[u].variable.dependents.get(d);
							if(!dependents.contains(curDep)){
								dependents.add(curDep);
							}
						}
					}
					//update dependent variables
					if(dependents.size()>0){
						writeLine("  # Update dependent variables");
						for(int d=0; d<dependents.size(); d++){
							Variable curVar=dependents.get(d);
							writeLine("  "+curVar.name+" <- "+rModel.translate(curVar.expression,false));
						}
					}
				}
								
				writeLine("");
				writeLine("  # Update prevalence");
				writeLine("  curPrev <- newPrev");
				writeLine("  # Simulate state transitions");
				for(int s=0; s<numStates; s++){
					writeLine("");
					defineNode(states[s],s);
				}
				writeLine("");
				writeLine("  # Update outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("  discount <- ifelse(t >= startDiscountCycle, 1 / ((1 + discountRates["+(d+1)+"]) ^ (t - startDiscountCycle + 1)), 1)");
					writeLine("  cycle"+dimNames[d]+"_dis <- cycle"+dimNames[d]+" * discount");
					writeLine("  if (t == 0 & halfCycle) {  # half-cycle correction");
					writeLine("    cycle"+dimNames[d]+" <- 0.5 * cycle"+dimNames[d]+"; cycle"+dimNames[d]+"_dis <- 0.5 * cycle"+dimNames[d]+"_dis");
					writeLine("  }");
					writeLine("  "+curChain.nameExport+"_"+dimNames[d]+" <- "+curChain.nameExport+"_"+dimNames[d]+" + cycle"+dimNames[d]);
					writeLine("  "+curChain.nameExport+"_Dis_"+dimNames[d]+" <- "+curChain.nameExport+"_Dis_"+dimNames[d]+" + cycle"+dimNames[d]+"_dis");
				}
				writeLine("");
				writeLine("  # Update trace");
				out.write("  row <- c(t, curPrev");
				for(int d=0; d<numDimensions; d++){
					out.write(", cycle"+dimNames[d]+", "+curChain.nameExport+"_"+dimNames[d]);
					out.write(", cycle"+dimNames[d]+"_dis, "+curChain.nameExport+"_Dis_"+dimNames[d]);
				}
				out.write(")"); out.newLine();
				writeLine("  row <- data.frame(matrix(row, nrow=1))");
				writeLine("  names(row) <- colNames");
				writeLine("  trace <- rbind(trace, row)");
				writeLine("");
				writeLine("  # Check termination condition");
				writeLine("  terminate <- ("+rModel.translate(curChain.terminationCondition, false)+")");
				writeLine("  if (terminate & halfCycle){  # half cycle-correction, update last trace row");
				writeLine("    lastRow <- nrow(trace)");
				int col=numStates+2; //index starts at 1
				for(int d=0; d<numDimensions; d++){
					writeLine("    "+curChain.nameExport+"_"+dimNames[d]+" <- "+curChain.nameExport+"_"+dimNames[d]+" - cycle"+dimNames[d]+" * 0.5");
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow, "+col+"] * 0.5  # "+dimNames[d]); col++;
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow - 1, "+col+"] + trace[lastRow, "+(col-1)+"]  # cum "+dimNames[d]); col++;
					writeLine("    "+curChain.nameExport+"_Dis_" + dimNames[d]+" <- "+curChain.nameExport+"_Dis_" + dimNames[d]+" - cycle"+dimNames[d]+"_dis * 0.5");
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow, "+col+"] * 0.5  # "+dimNames[d]+" discounted"); col++;
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow - 1, "+col+"] + trace[lastRow, "+(col-1)+"]  # cum "+dimNames[d]+" discounted"); col++;
				}
				writeLine("  }");
				writeLine("");
				writeLine("  t <- t + 1  # next cycle");
				writeLine("");
				writeLine("}  # end cycle while loop");
				writeLine("setwd(\""+dir.replaceAll("\\\\", "\\\\\\\\")+"\")");
				writeLine("write.csv(trace, \""+curChain.name+"_Trace.csv\")  # write trace");
				writeLine("cat(\"done!\")");
				writeLine("");
				writeLine("# Report totals");
				for(int d=0; d<numDimensions; d++){
					writeLine("print(paste(\""+dimNames[d]+":\", "+curChain.nameExport+"_"+dimNames[d]+"))");
					writeLine("print(paste(\""+dimNames[d]+" (Discounted):\", "+curChain.nameExport+"_Dis_"+dimNames[d]+"))");
				}
				writeLine("print(\"\")");
			}
			out.newLine();
			
			rModel.defineFunctions();
			
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode, int stateIndex){
		try{
			//int level=(curNode.level-curNode.chain.level)+2; //tab indents
			int level=1;
			writeLine("# "+curNode.name,level);
			
			String localPrev;
			
			if(curNode.type==2){ //State, update rewards
				writeLine("# Update rewards",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+" <- "+dim+"+curPrev["+(stateIndex+1)+"] * "+rModel.translate(curNode.rewards[d],false),level);
				}
				localPrev="curPrev["+(stateIndex+1)+"]";
			}
			else{
				localPrev="prev_"+curNode.nameExport;
			}
			
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
					String dim="cycle"+dimNames[d];
					writeLine(dim+" <- "+dim+" + "+localPrev+" * "+rModel.translate(curNode.cost[d],false),level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine("# Transition to "+states[indexTo].name,level);
				writeLine("newPrev["+(stateIndex+1)+"] <- newPrev["+(stateIndex+1)+"] - "+localPrev,level);
				writeLine("newPrev["+(indexTo+1)+"] <- newPrev["+(indexTo+1)+"] + "+localPrev,level);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine("# Calculate child probs",level);
				writeLine("sumProb <- 0",level);
				writeLine("childProbs <- c()",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						writeLine("childProbs["+(i+1)+"] <- "+rModel.translate(prob, false)+"; sumProb <- sumProb + childProbs["+(i+1)+"]  # Prob "+children[i].name,level);
					}
				}
				if(compIndex!=-1){
					writeLine("childProbs["+(compIndex+1)+"] <- 1.0 - sumProb  # Complementary prob",level);
				}
				for(int i=0; i<numChildren; i++){
					String childPrev="prev_"+children[i].nameExport;
					writeLine(childPrev+" <- "+localPrev+" * childProbs["+(i+1)+"]",level);
				}
				for(int i=0; i<numChildren; i++){
					defineNode(children[i],stateIndex);
				}
			}
		}catch(Exception e){
			recordError(e);
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
	
	
	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			recordError(e);
		}
	}

	private void writeLine(String line, int indent){
		try{
			for(int t=0; t<indent; t++){out.write("  ");}
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
