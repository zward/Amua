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

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class PythonTreeMonteCarlo{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	PythonModel pyModel;
	String dimNames[];

	//Constructor
	public PythonTreeMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
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
			writeLine(0,"");
			
			pyModel.writeTableClass(tableFormat);
			writeLine(0,""); //Blank
			pyModel.writeTables(tableFormat);
			pyModel.writeParameters();
			
			int numVars=myModel.variables.size();
			pyModel.writeVariables();

			//Define strategies
			writeLine(0,"#Define strategies");
			writeLine(0,"numSim="+myModel.cohortSize);
			int numStrat=myModel.getStrategies();
			for(int s=0; s<numStrat; s++){
				writeLine(0,"");
				writeLine(0,"#"+myModel.strategyNames[s]);
				if(myModel.CRN){writeLine(0,"np.random.seed("+myModel.crnSeed+")");}
				writeLine(0,"#Initialize outcomes");
				for(int d=0; d<dimNames.length; d++){
					writeLine(0,dimNames[d]+"=0");
				}
				writeLine(0,"#Run Monte Carlo simulation");
				writeLine(0,"for i in range(numSim):");
				if(numVars>0){
					writeLine(1,"#Initialize variables");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine(1,curVar.name+"="+pyModel.translate(curVar.initValue,false));
					}
				}
				//define nodes
				int stratIndex=myModel.tree.nodes.get(0).childIndices.get(s);
				TreeNode curStrategy=myModel.tree.nodes.get(stratIndex);
				defineNode(curStrategy);
				writeLine(0,"#end Monte Carlo loop");
				//Print results
				writeLine(0,"#Print results");
				writeLine(0,"print(\"Strategy: "+myModel.strategyNames[s]+"\")");
				for(int d=0; d<numDimensions; d++){
					writeLine(0,"print(\""+dimNames[d]+":\","+dimNames[d]+")");
				}
				writeLine(0,"print(\"\")");
			}
			
			pyModel.defineFunctions();
			
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(TreeNode curNode){
		try{

			int level=curNode.level; //tab indents

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
					String dim=dimNames[d];
					writeLine(level,dim+"="+dim+"+"+pyModel.translate(curNode.cost[d],false));
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //terminal node
				writeLine(level,"#Update payoffs");
				for(int d=0; d<numDimensions; d++){
					String dim=dimNames[d];
					writeLine(level,dim+"="+dim+"+"+pyModel.translate(curNode.payoff[d],false));
				}
			}
			else{
				TreeNode children[]=new TreeNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=tree.nodes.get(index);
				}
				writeLine(level,"#Calculate child probs");
				writeLine(level,"childProbs=np.zeros("+numChildren+")");
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine(level,"childProbs[0]="+pyModel.translate(prob,false)+" #Prob "+children[i].name);}
						else{writeLine(level,"childProbs["+(i)+"]=childProbs["+(i-1)+"]+"+pyModel.translate(prob,false)+" #Prob "+children[i].name);}
					}
				}
				
				writeLine(level,"#Sample event");
				writeLine(level,"rand=np.random.rand()");
				if(compIndex==-1){ //no comp prob
					writeLine(level,"if(rand<childProbs[0]): #"+children[0].name);
					defineNode(children[0]);
					writeLine(level,"#end "+children[0].name);
					for(int i=1; i<numChildren; i++){
						writeLine(level,"elif(rand<childProbs["+(i)+"]): #"+children[i].name);
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
								writeLine(level,"elif(rand<childProbs["+(i)+"]): #"+children[i].name);
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
								writeLine(level,"elif(rand<childProbs["+(i)+"]): #"+children[i].name);
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
