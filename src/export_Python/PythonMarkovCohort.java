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

package export_Python;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import base.AmuaModel;
import main.*;
import markov.MarkovNode;
import markov.MarkovTree;

public class PythonMarkovCohort{

	BufferedWriter out;
	AmuaModel myModel;
	MarkovTree markov;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	PythonModel pyModel;
	String dimNames[];
	MarkovNode states[];

	//Constructor
	public PythonMarkovCohort(String dir1,AmuaModel myModel1,int tableFormat){
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
			fstream = new FileWriter(dir+"main.py"); //Create new file
			out = new BufferedWriter(fstream);

			pyModel=new PythonModel(dir,out,myModel);

			pyModel.writeProperties();
			writeLine(0,"");
			if(tableFormat==1){writeLine(0,"import csv");}
			writeLine(0,"import math");
			writeLine(0,"import numpy as np");
			if(myModel.tables.size()>0){
				writeLine(0,"from Table import Table");
				writeLine(0,"from Table import CubicSpline");
			}
			writeLine(0,"from MarkovTrace import MarkovTrace");
			writeLine(0,"import functions as fx");
			writeLine(0,"");
			
			pyModel.writeTableClass(tableFormat);
			pyModel.writeMarkovTrace();
			
			writeLine(0,""); //Blank
			pyModel.writeTables(tableFormat);
			pyModel.writeParameters();
			
			int numVars=myModel.variables.size();
			pyModel.writeVariables();
			writeLine(0,"");
			writeLine(0,"cohortSize="+myModel.cohortSize);
			writeLine(0,"");
			writeLine(0,"#Initialize discount rates");
			if(myModel.markov.discountRewards){
				out.write("discountRates=[");
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+",");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+"]"); out.newLine();
				writeLine(0,"startDiscountCycle="+myModel.markov.discountStartCycle);
			}
			else{
				out.write("discountRates<-np.ones("+numDimensions+")"); out.newLine();
			}
			if(myModel.markov.halfCycleCorrection){writeLine(0,"halfCycle=True");}
			else{writeLine(0,"halfCycle=False");}
			
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
				writeLine(0,"");
				writeLine(0,"########## Markov Chain: "+curChain.name+" ##########");
				writeLine(0,"print(\"Running Markov Chain: "+curChain.name+" \")");
				writeLine(0,"numStates="+numStates);
				out.write("stateNames=[");
				for(int s=0; s<numStates-1; s++){out.write("\""+states[s].name+"\",");}
				out.write("\""+states[numStates-1].name+"\"]"); out.newLine();
				writeLine(0,"trace=MarkovTrace(\""+curChain.name+"\",stateNames) #initialize trace");
				writeLine(0,"");
				writeLine(0,"#Initialize prevalence");
				writeLine(0,"curPrev=np.zeros(numStates)");
				for(int s=0; s<numStates; s++){
					writeLine(0,"curPrev["+(s)+"]=cohortSize*"+pyModel.translate(states[s].prob,false)+" #"+states[s].name);
				}
				writeLine(0,"newPrev=curPrev.copy() #copy inital prev");
				if(numVars>0){
					writeLine(0,"#Initialize variables");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine(0,curVar.name+"="+pyModel.translate(curVar.initValue,false));
					}
				}
				writeLine(0,"");
				writeLine(0,"#Run chain");
				writeLine(0,"#Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine(0,curChain.nameExport+"_"+dimNames[d]+"=0");
					writeLine(0,curChain.nameExport+"_Dis_"+dimNames[d]+"=0");
				}
				writeLine(0,"t=0 #initialize cycle");
				writeLine(0,"terminate=False");
				writeLine(0,"while(terminate==False):");
					writeLine(1,"#update progress");
					writeLine(1,"if(t%10==0): print(t,end=\"\")");
					writeLine(1,"else: print('.',end=\"\")");
					writeLine(1,"");
					writeLine(1,"#Cycle outcomes");
					for(int d=0; d<numDimensions; d++){
					writeLine(1,"cycle"+dimNames[d]+"=0");
					writeLine(1,"cycle"+dimNames[d]+"_dis=0");
					}
					writeLine(1,"");
					writeLine(1,"#Update prevalence");
					writeLine(1,"curPrev=newPrev.copy()");
					writeLine(1,"#Simulate state transitions");
					for(int s=0; s<numStates; s++){
					writeLine(1,"");
					defineNode(states[s],s);
					}
					writeLine(1,"");
					writeLine(1,"#Update outcomes");
					for(int d=0; d<numDimensions; d++){
					writeLine(1,"discount=1.0");
					writeLine(1,"if(t>=startDiscountCycle): discount=1/((1+discountRates["+d+"])**(t-startDiscountCycle+1))");
					writeLine(1,"cycle"+dimNames[d]+"_dis=cycle"+dimNames[d]+"*discount");
					writeLine(1,"if(t==0 and halfCycle): #half-cycle correction");
						writeLine(2,"cycle"+dimNames[d]+"*=0.5");
						writeLine(2,"cycle"+dimNames[d]+"_dis*=0.5");
					writeLine(1,curChain.nameExport+"_"+dimNames[d]+"+=cycle"+dimNames[d]);
					writeLine(1,curChain.nameExport+"_Dis_"+dimNames[d]+"+=cycle"+dimNames[d]+"_dis");
					}
					writeLine(1,"");
					writeLine(1,"#Update trace");
					out.write("    trace.update(curPrev");
					for(int d=0; d<numDimensions; d++){out.write(", cycle"+dimNames[d]+", cycle"+dimNames[d]+"_dis");}
					out.write(")"); out.newLine();
					writeLine(1,"");
					
					writeLine(1,"#Check termination condition");
					writeLine(1,"terminate = ("+pyModel.translate(curChain.terminationCondition, false)+")");
					writeLine(1,"if(terminate and halfCycle): #half cycle-correction, update last trace row");
						writeLine(2,"trace.applyHalfCycle()");
						writeLine(2,"#Update cum rewards)");
						for(int d=0; d<numDimensions; d++){
						writeLine(2,curChain.nameExport+"_"+dimNames[d]+"-=cycle"+dimNames[d]+"*0.5;");
						writeLine(2,curChain.nameExport+"_Dis_"+dimNames[d]+"-=cycle"+dimNames[d]+"_dis*0.5;");
					}
					writeLine(1,"");
					writeLine(1,"t+=1 #next cycle");
					writeLine(1,"");
					writeLine(0,"#end cycle while loop");
					writeLine(0,"trace.writeCSV() #write trace");
					writeLine(0,"print(\"done!\",sep=\"\")");
				writeLine(0,"");
				writeLine(0,"#Report totals");
				for(int d=0; d<numDimensions; d++){
				writeLine(0,"print(\""+dimNames[d]+":\","+curChain.nameExport+"_"+dimNames[d]+")");
				writeLine(0,"print(\""+dimNames[d]+" (Discounted):\","+curChain.nameExport+"_Dis_"+dimNames[d]+")");
				}
				writeLine(0,"print(\"\")");
			}
			out.newLine();
			
			pyModel.defineFunctions();
			
			out.close();

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode, int stateIndex){
		try{
			//int level=(curNode.level-curNode.chain.level)+2; //tab indents
			int level=1;
			writeLine(level,"#"+curNode.name);
			
			String localPrev;
			
			if(curNode.type==2){ //State, update rewards
				writeLine(level,"#Update rewards");
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(level,dim+" += curPrev["+(stateIndex)+"]*"+pyModel.translate(curNode.rewards[d],false));
				}
				localPrev="curPrev["+(stateIndex)+"]";
			}
			else{
				localPrev="prev_"+curNode.nameExport;
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine(level,"#Update variables");
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				for(int u=0; u<numUpdates; u++){
					writeLine(level,pyModel.translate(updates[u],false)+" #Orig: "+updates[u]);
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine(level,"#Update costs");
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(level,dim+"+="+localPrev+"*"+pyModel.translate(curNode.cost[d],false));
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine(level,"#Transition to "+states[indexTo].name);
				writeLine(level,"newPrev["+(stateIndex)+"]-="+localPrev);
				writeLine(level,"newPrev["+(indexTo)+"]+="+localPrev);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine(level,"#Calculate child probs");
				writeLine(level,"sumProb=0");
				writeLine(level,"childProbs=np.zeros("+numChildren+")");
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						writeLine(level,"childProbs["+(i)+"]="+pyModel.translate(prob, false)+" #Prob "+children[i].name); 
						writeLine(level,"sumProb+=childProbs["+(i)+"]");
					}
				}
				if(compIndex!=-1){
					writeLine(level,"childProbs["+(compIndex)+"]=1.0-sumProb #Complementary prob");
				}
				for(int i=0; i<numChildren; i++){
					String childPrev="prev_"+children[i].nameExport;
					writeLine(level,childPrev+"="+localPrev+"*childProbs["+(i)+"]");
				}
				for(int i=0; i<numChildren; i++){
					defineNode(children[i],stateIndex);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
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
	
	
	private void writeLine(int indent,String line){
		try{
			for(int i=0; i<indent; i++){out.write("    ");} //4-space indent
			out.write(line); 
			out.newLine();
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

}
