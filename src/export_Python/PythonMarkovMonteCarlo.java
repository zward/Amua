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

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import markov.MarkovNode;
import markov.MarkovTree;

public class PythonMarkovMonteCarlo{

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
	public PythonMarkovMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
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
			
			//Inner class
			writeLine(0,"#Define inner class");
			writeLine(0,"class Person:");
			writeLine(1,"#Attributes");
			writeLine(1,"state=-1");
			int numVars=myModel.variables.size();
			for(int v=0; v<numVars; v++){
				Variable curVar=myModel.variables.get(v);
				if(curVar.value.isMatrix()){writeLine(1,curVar.name+"=[]");}
				else{writeLine(1,curVar.name+"=0");}
			}
			writeLine(0,"");
						
			pyModel.writeTableClass(tableFormat);
			pyModel.writeMarkovTrace();
			
			writeLine(0,""); //Blank
			pyModel.writeTables(tableFormat);
			pyModel.writeParameters();
			
			writeLine(0,"");
			writeLine(0,"#Initialize discount rates");
			writeLine(0,"startDiscountCycle="+myModel.markov.discountStartCycle);
			if(myModel.markov.discountRewards){
				out.write("discountRates=[");
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+",");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+"]"); out.newLine();
			}
			else{
				out.write("discountRates=np.ones("+numDimensions+")"); out.newLine();
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
				if(myModel.CRN){writeLine(0,"np.random.seed("+myModel.crnSeed+") #seed RNG");}
				writeLine(0,"numStates="+numStates);
				out.write("stateNames=[");
				for(int s=0; s<numStates-1; s++){out.write("\""+states[s].name+"\",");}
				out.write("\""+states[numStates-1].name+"\"]"); out.newLine();
				writeLine(0,"trace=MarkovTrace(\""+curChain.name+"\",stateNames) #initialize trace");
				writeLine(0,"");
				writeLine(0,"#Initialize prevalence");
				out.write("initPrev=[");
				for(int s=0; s<numStates-1; s++){out.write(pyModel.translate(states[s].prob,true)+",");}
				out.write(pyModel.translate(states[numStates-1].prob, true)+"]"); out.newLine();
				writeLine(0,"cumPrev=np.zeros(numStates)");
				writeLine(0,"cumPrev[0]=initPrev[0]");
				writeLine(0,"for s in range(1,numStates):");
					writeLine(1,"cumPrev[s]=cumPrev[s-1]+initPrev[s]");
				writeLine(0,"");
				writeLine(0,"numPeople="+myModel.cohortSize);
				writeLine(0,"people=[Person() for i in range(numPeople)]");
				writeLine(0,"for p in range(0,numPeople):");
					writeLine(1,"curPerson=people[p]");
					writeLine(1,"rand=np.random.rand()");
					writeLine(1,"k=0");
					writeLine(1,"while(rand>initPrev[k]): k+=1");
					writeLine(1,"people[p].state=k");
					if(numVars>0){
					writeLine(1,"#Initialize variables");
					//independent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==true){
							writeLine(1,"people[p]."+curVar.name+"="+pyModel.translate(curVar.expression,true));
						}
					}
					//dependent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==false){
							writeLine(1,"people[p]."+curVar.name+"="+pyModel.translate(curVar.expression,true));
						}
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
					writeLine(1,"prev=np.zeros(numStates)");
					for(int d=0; d<numDimensions; d++){
					writeLine(1,"cycle"+dimNames[d]+"=0");
					writeLine(1,"cycle"+dimNames[d]+"_dis=0");
					}
					writeLine(1,"");
					
					writeLine(1,"#Update each person");
					writeLine(1,"for p in range(0,numPeople):");
						writeLine(2,"curPerson=people[p]");
						writeLine(2,"curState=curPerson.state");
						writeLine(2,"prev[curState]+=1 #record prevalence");
						
						//cycle variable updates
						if(curChain.hasVarUpdates){
							writeLine(2,"");
							writeLine(2,"#Cycle variable updates");
							String updates[]=curChain.varUpdates.split(";");
							int numUpdates=updates.length;
							ArrayList<Variable> dependents=new ArrayList<Variable>();
							for(int u=0; u<numUpdates; u++){
								writeLine(2,pyModel.translate(updates[u],true)+" #Orig: "+updates[u]);
								for(int d=0; d<curChain.curVariableUpdates[u].variable.dependents.size(); d++){
									Variable curDep=curChain.curVariableUpdates[u].variable.dependents.get(d);
									if(!dependents.contains(curDep)){
										dependents.add(curDep);
									}
								}
							}
							//update dependent variables
							if(dependents.size()>0){
								writeLine(2,"#Update dependent variables");
								for(int d=0; d<dependents.size(); d++){
									Variable curVar=dependents.get(d);
									writeLine(2,"curPerson."+curVar.name+"="+pyModel.translate(curVar.expression,true));
								}
							}
							writeLine(2,"");
						}
						
						
						writeLine(2,"if(curState==0): #"+states[0].name);
						defineNode(states[0]);
						for(int s=1; s<numStates; s++){
						writeLine(2,"elif(curState=="+s+"): #"+states[s].name);
						defineNode(states[s]);
						}
					writeLine(1,"#end person foor loop");
					
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
					out.write("    trace.update(prev");
					for(int d=0; d<numDimensions; d++){out.write(", cycle"+dimNames[d]+", cycle"+dimNames[d]+"_dis");}
					out.write(")"); out.newLine();
					writeLine(1,"");
					
					writeLine(1,"#Check termination condition");
					writeLine(1,"terminate = ("+pyModel.translate(curChain.terminationCondition, false)+")");
					writeLine(1,"if(terminate and halfCycle): #half cycle-correction, update last trace row");
						writeLine(2,"trace.applyHalfCycle()");
						writeLine(2,"#Update cum rewards");
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
			recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode){
		try{
			int level=(curNode.level-curNode.chain.level)+2; //tab indents //tab indents
					
			if(curNode.type==2){ //State, update rewards
				writeLine(level,"#Update rewards");
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(level,dim+"+="+pyModel.translate(curNode.rewards[d],true));
				}
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine(level,"#Update variables");
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				ArrayList<Variable> dependents=new ArrayList<Variable>();
				for(int u=0; u<numUpdates; u++){
					writeLine(level,pyModel.translate(updates[u],true)+" #Orig: "+updates[u]);
					for(int d=0; d<curNode.curVariableUpdates[u].variable.dependents.size(); d++){
						Variable curDep=curNode.curVariableUpdates[u].variable.dependents.get(d);
						if(!dependents.contains(curDep)){
							dependents.add(curDep);
						}
					}
				}
				//update dependent variables
				if(dependents.size()>0){
					writeLine(level,"#Update dependent variables");
					for(int d=0; d<dependents.size(); d++){
						Variable curVar=dependents.get(d);
						writeLine(level,"curPerson."+curVar.name+"="+pyModel.translate(curVar.expression,true));
					}
				}
			}
			
			if(curNode.hasCost){ //add cost
				writeLine(level,"#Update costs");
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(level,dim+"+="+pyModel.translate(curNode.cost[d],true));
				}
			}
						
			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine(level,"curPerson.state="+indexTo+" #Transition to "+states[indexTo].name);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine(level,"#Calculate child probs");
				writeLine(level,"childProbs=np.zeros("+numChildren+")");
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine(level,"childProbs[0]="+pyModel.translate(prob,true)+" #Prob "+children[i].name);}
						else{writeLine(level,"childProbs["+i+"]=childProbs["+(i-1)+"]+"+pyModel.translate(prob,true)+" #Prob "+children[i].name);}
					}
				}
				
				writeLine(level,"#Sample event");
				writeLine(level,"rand=np.random.rand()");
				if(compIndex==-1){ //no comp prob
					writeLine(level,"if(rand<childProbs[0]): #"+children[0].name);
					defineNode(children[0]);
					writeLine(level,"#end "+children[0].name);
					for(int i=1; i<numChildren; i++){
						writeLine(level,"elif(rand<childProbs["+i+"]): #"+children[i].name);
						defineNode(children[i]);
						writeLine(level,"#end "+children[i].name);
					}
				}
				else{ //write comp prob as last 'else'
					if(compIndex!=0){
						writeLine(level,"if(rand<childProbs[0]): #"+children[0].name);
						defineNode(children[0]);
						writeLine(level,"#end "+children[0].name);
						for(int i=1; i<numChildren; i++){
							if(compIndex!=i){
								writeLine(level,"elif(rand<childProbs["+i+"]): #"+children[i].name);
								defineNode(children[i]);
								writeLine(level,"#end "+children[i].name);
							}
						}
					}
					else{ //compIndex == 0
						writeLine(level,"if(rand<childProbs[1]): #"+children[1].name);
						defineNode(children[1]);
						writeLine(level,"#end "+children[1].name);
						for(int i=2; i<numChildren; i++){
							if(compIndex!=i){
								writeLine(level,"elif(rand<childProbs["+i+"]): #"+children[i].name);
								defineNode(children[i]);
								writeLine(level,"#end "+children[i].name);
							}
						}
					}
					//write comp index
					writeLine(level,"else: #"+children[compIndex].name);
					defineNode(children[compIndex]);
					writeLine(level,"#end "+children[compIndex].name);
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
	
	
	private void writeLine(int indent,String line){
		try{
			for(int i=0; i<indent; i++){out.write("    ");} //4-space indent
			out.write(line); 
			out.newLine();
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
