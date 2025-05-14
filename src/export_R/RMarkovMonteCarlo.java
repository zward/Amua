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

public class RMarkovMonteCarlo{

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
	public RMarkovMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
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
			writeLine("");
			writeLine("# Initialize discount rates");
			if(myModel.markov.discountRewards){
				out.write("discountRates <- c(");
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+", ");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+")"); out.newLine();
				writeLine("startDiscountCycle <- "+myModel.markov.discountStartCycle);
			}
			else{ //no discounting
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
				if(myModel.CRN){
					writeLine("set.seed("+myModel.crnSeed+")");
				}
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
				out.write("initPrev <- c(");
				for(int s=0; s<numStates-1; s++){out.write(rModel.translate(states[s].prob,true)+", ");}
				out.write(rModel.translate(states[numStates-1].prob,true)+")"); out.newLine();
				writeLine("");
				writeLine("numPeople <- "+myModel.cohortSize);
				writeLine("person.state <- sample(1:numStates, numPeople, replace=TRUE, prob=initPrev)  # sample initial states");
				if(numVars>0){
					writeLine("# Initialize variables");
					writeLine("t <- 0  # initialize cycle");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine("person."+curVar.name+" <- c()"); //initialize vector
					}
					writeLine("for(p in 1:numPeople) {");
					//independent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==true){
							writeLine("  person."+curVar.name+"[p] <- "+rModel.translate(curVar.expression, true));
						}
					}
					//dependent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==false){
							writeLine("  person."+curVar.name+"[p] <- "+rModel.translate(curVar.expression, true));
						}
					}
					writeLine("}");
				}
				writeLine("");
				
				writeLine("# Run chain");
				writeLine("# Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine(curChain.nameExport+"_"+dimNames[d]+" <- 0; "+curChain.nameExport+"_Dis_"+dimNames[d]+" <- 0");
				}
				writeLine("t <- 0 # initialize cycle");
				writeLine("terminate <- FALSE");
				writeLine("while(terminate == FALSE) {");
				writeLine("  # Update progress");
				writeLine("  if(t %% 10 == 0) {");
				writeLine("    cat(t, sep=\"\")");
				writeLine("  } else {");
				writeLine("    cat(\".\", sep=\"\")");
				writeLine("  }");
				writeLine("");
				writeLine("  # Cycle outcomes");
				writeLine("  prev <- rep(0, numStates)");
				for(int d=0; d<numDimensions; d++){
					writeLine("  cycle"+dimNames[d]+" <- 0; cycle"+dimNames[d]+"_dis <- 0");
				}
				writeLine("");
				writeLine("  # Update each person");
				writeLine("  for(p in 1:numPeople) {");
				writeLine("    curState <- person.state[p]");
				writeLine("    prev[curState] <- prev[curState] + 1  # record prevalence");
				
				//cycle variable updates
				if(curChain.hasVarUpdates){
					writeLine("");
					writeLine("    # Cycle variable updates");
					String updates[]=curChain.varUpdates.split(";");
					int numUpdates=updates.length;
					ArrayList<Variable> dependents=new ArrayList<Variable>();
					for(int u=0; u<numUpdates; u++){
						writeLine(rModel.translate(updates[u],true)+"  # Orig: "+updates[u], 2);
						for(int d=0; d<curChain.curVariableUpdates[u].variable.dependents.size(); d++){
							Variable curDep=curChain.curVariableUpdates[u].variable.dependents.get(d);
							if(!dependents.contains(curDep)){
								dependents.add(curDep);
							}
						}
					}
					//update dependent variables
					if(dependents.size()>0){
						writeLine("    # Update dependent variables");
						for(int d=0; d<dependents.size(); d++){
							Variable curVar=dependents.get(d);
							writeLine("    person."+curVar.name+"[p] <- "+rModel.translate(curVar.expression,true));
						}
					}
					writeLine("");
				}
				
				
				writeLine("    if (curState == 1) {  # "+states[0].name);
				defineNode(states[0]);
				writeLine("    }");
				for(int s=1; s<numStates; s++){
					writeLine("    else if(curState == "+(s+1)+"){  # "+states[s].name);
					defineNode(states[s]);
					writeLine("    }");
				}
				writeLine("  }  # end person foor loop");
				
				writeLine("");
				writeLine("  # Update outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("  discount <- ifelse(t >= startDiscountCycle, 1 / ((1 + discountRates["+(d+1)+"]) ^ (t - startDiscountCycle + 1)), 1)");
					writeLine("  cycle"+dimNames[d]+"_dis <- cycle"+dimNames[d]+" * discount");
					writeLine("  if(t == 0 & halfCycle) {  # half-cycle correction");
					writeLine("    cycle"+dimNames[d]+" <- 0.5 * cycle"+dimNames[d]+"; cycle"+dimNames[d]+"_dis <- 0.5 * cycle"+dimNames[d]+"_dis");
					writeLine("  }");
					writeLine("  "+curChain.nameExport+"_"+dimNames[d]+" <- "+curChain.nameExport+"_"+dimNames[d]+" + cycle"+dimNames[d]);
					writeLine("  "+curChain.nameExport+"_Dis_"+dimNames[d]+" <- "+curChain.nameExport+"_Dis_"+dimNames[d]+" + cycle"+dimNames[d]+"_dis");
				}
				writeLine("");
				writeLine("  # Update trace");
				out.write("  row <- c(t, prev");
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
				writeLine("  terminate <- ("+rModel.translate(curChain.terminationCondition, true)+")");
				writeLine("  if (terminate & halfCycle) {  # half cycle-correction, update last trace row");
				writeLine("    lastRow <- nrow(trace)");
				int col=numStates+2; //index starts at 1
				for(int d=0; d<numDimensions; d++){
					writeLine("    "+curChain.nameExport+"_"+dimNames[d]+" <- "+curChain.nameExport+"_"+dimNames[d]+" - cycle"+dimNames[d]+" * 0.5");
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow, "+col+"] * 0.5  # "+dimNames[d]); col++;
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow - 1, "+col+"] + trace[lastRow, "+(col-1)+"]  # cum "+dimNames[d]); col++;
					writeLine("    "+curChain.nameExport+"_Dis_"+dimNames[d]+" <- "+curChain.nameExport+"_Dis_"+dimNames[d]+" - cycle"+dimNames[d]+"_dis * 0.5");
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow, "+col+"] * 0.5  # "+dimNames[d]+" discounted"); col++;
					writeLine("    trace[lastRow, "+col+"] <- trace[lastRow - 1, "+col+"] + trace[lastRow, "+(col-1)+"]  # cum "+dimNames[d]+" discounted"); col++;
				}
				writeLine("  }");
				writeLine("");
				writeLine("  t <- t + 1 #next cycle");
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

	private void defineNode(MarkovNode curNode){
		try{
			int level=(curNode.level-curNode.chain.level)+2; //tab indents
			
			if(curNode.type==2){ //State, update rewards
				writeLine("# Update rewards",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+" <- "+dim+" + "+rModel.translate(curNode.rewards[d],true),level);
				}
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine("# Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				ArrayList<Variable> dependents=new ArrayList<Variable>();
				for(int u=0; u<numUpdates; u++){
					writeLine(rModel.translate(updates[u],true)+"  # Orig: "+updates[u], level);
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
						writeLine("person."+curVar.name+"[p] <- "+rModel.translate(curVar.expression,true),level);
					}
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("# Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+" <- "+dim+" + "+rModel.translate(curNode.cost[d],true),level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine("person.state[p] <- "+(indexTo+1)+"  # Transition to "+states[indexTo].name,level);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine("# Calculate child probs",level);
				writeLine("childProbs <- rep(0, "+numChildren+")",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine("childProbs[1] <- "+rModel.translate(prob,true)+"  # Prob "+children[i].name,level);}
						else{writeLine("childProbs["+(i+1)+"] <- childProbs["+i+"] + "+rModel.translate(prob,true)+"  # Prob "+children[i].name,level);}
					}
				}
				
				writeLine("# Sample event",level);
				writeLine("rand <- runif(1)",level);
				if(compIndex==-1){ //no comp prob
					writeLine("if (rand < childProbs[1]) {  # "+children[0].name,level);
					defineNode(children[0]);
					writeLine("}  # end "+children[0].name,level);
					for(int i=1; i<numChildren; i++){
						writeLine("else if(rand < childProbs["+(i+1)+"]) {  # "+children[i].name,level);
						defineNode(children[i]);
						writeLine("}  # end "+children[i].name,level);
					}
				}
				else{ //write comp prob as last 'else'
					if(compIndex!=0){
						writeLine("if (rand < childProbs[1]) {  # "+children[0].name,level);
						defineNode(children[0]);
						writeLine("} # end "+children[0].name,level);
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
								writeLine("}  # end "+children[i].name,level);
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
