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

package export_Cpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class CppTreeMonteCarlo{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	CppModel cppModel;
	String dimNames[];

	//Constructor
	public CppTreeMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
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
			fstream = new FileWriter(dir+"main.cpp"); //Create new file
			out = new BufferedWriter(fstream);

			cppModel=new CppModel(dir,out,myModel);

			cppModel.writeProperties();
			
			writeLine("");
			writeLine("#include <fstream>");
			writeLine("#include <iostream>");
			writeLine("#include <string>");
			writeLine("#include <vector>");
			writeLine("#include <math.h>");
			writeLine("#include \"functions.h\"");
			if(myModel.tables.size()>0){
				writeLine("#include \"Table.h\"");
			}
			writeLine("#include <random>");
			writeLine("");
			writeLine("using namespace std;");
			writeLine("");
			
			writeLine("int main(int argc, char * argv[]) {");
			
			cppModel.writeTableClass();
			cppModel.writeTables(tableFormat);

			cppModel.writeParameters();
			int numVars=myModel.variables.size();
			cppModel.writeVariables();
			
			writeLine("	//Intialize Random Number Generator");
			writeLine("	mt19937 generator; //built-in RNG - feel free to swap out");
			writeLine("	uniform_real_distribution<double> unif(0.0,1.0);");
			writeLine("	double rand;");
			writeLine("");
			writeLine("	//Define outcomes");
			for(int d=0; d<dimNames.length; d++){
				writeLine("	double "+dimNames[d]+"=0;");
			}
			writeLine("");
			
			//Define strategies
			writeLine("	//Define strategies");
			writeLine("	int numSim="+myModel.cohortSize+";");
			writeLine("	double * childProbs;");
			int numStrat=myModel.getStrategies();
			for(int s=0; s<numStrat; s++){
				writeLine("");
				writeLine("	//"+myModel.strategyNames[s]);
				if(myModel.CRN){writeLine("	generator.seed("+myModel.crnSeed+");");}
				writeLine("	//Initialize outcomes");
				for(int d=0; d<dimNames.length; d++){
					writeLine("	"+dimNames[d]+"=0;");
				}
				writeLine("	//Run Monte Carlo simulation");
				writeLine("	for(int i=0; i<numSim; i++){");
				if(numVars>0){
					writeLine("		//Initialize variables");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine("		"+curVar.name+"="+cppModel.translate(curVar.initValue,false)+";");
					}
				}
				//define nodes
				int stratIndex=myModel.tree.nodes.get(0).childIndices.get(s);
				TreeNode curStrategy=myModel.tree.nodes.get(stratIndex);
				defineNode(curStrategy);
				
				//progress
				
				writeLine("	} //end Monte Carlo loop");
				//Print results
				writeLine("	//Print results");
				writeLine("	cout<<\"Strategy: "+myModel.strategyNames[s]+"\\n\";");
				for(int d=0; d<numDimensions; d++){
					writeLine("	cout<<\""+dimNames[d]+": \"<<"+dimNames[d]+"<<\"\\n\";");
				}
				writeLine("	cout<<\"\\n\";");
			}
			writeLine("");
			writeLine("	delete[] childProbs;");
			cppModel.defineFunctions();
			
			out.write("}"); //Close class
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(TreeNode curNode){
		try{

			int level=curNode.level+1; //tab indents

			if(curNode.hasVarUpdates){ //update variables
				writeLine("//Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				for(int u=0; u<numUpdates; u++){
					writeLine(cppModel.translate(updates[u],false)+"; //Orig: "+updates[u], level);
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("//Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim=dimNames[d];
					writeLine(dim+"+="+cppModel.translate(curNode.cost[d],false)+";",level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //terminal node
				writeLine("//Update payoffs",level);
				for(int d=0; d<numDimensions; d++){
					String dim=dimNames[d];
					writeLine(dim+"+="+cppModel.translate(curNode.payoff[d],false)+";",level);
				}
			}
			else{
				TreeNode children[]=new TreeNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=tree.nodes.get(index);
				}
				writeLine("//Calculate child probs",level);
				writeLine("childProbs=new double["+numChildren+"];",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine("childProbs[0]="+cppModel.translate(prob,false)+"; //Prob "+children[i].name,level);}
						else{writeLine("childProbs["+i+"]=childProbs["+(i-1)+"]+"+cppModel.translate(prob,false)+"; //Prob "+children[i].name,level);}
					}
				}
				
				writeLine("//Sample event",level);
				writeLine("rand=unif(generator);",level);
				if(compIndex==-1){ //no comp prob
					writeLine("if(rand<childProbs[0]){ //"+children[0].name,level);
					defineNode(children[0]);
					writeLine("} //end "+children[0].name,level);
					for(int i=1; i<numChildren; i++){
						writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
						defineNode(children[i]);
						writeLine("} //end "+children[i].name,level);
					}
				}
				else{ //write comp prob as last 'else'
					if(compIndex!=0){
						writeLine("if(rand<childProbs[0]){ //"+children[0].name,level);
						defineNode(children[0]);
						writeLine("} //end "+children[0].name,level);
						for(int i=1; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
								defineNode(children[i]);
								writeLine("} //end "+children[i].name,level);
							}
						}
					}
					else{ //compIndex == 0
						writeLine("if(rand<childProbs[1]){ //"+children[1].name,level);
						defineNode(children[1]);
						writeLine("} //end "+children[1].name,level);
						for(int i=2; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
								defineNode(children[i]);
								writeLine("} //end "+children[i].name,level);
							}
						}
					}
					//write comp index
					writeLine("else{ //"+children[compIndex].name,level);
					defineNode(children[compIndex]);
					writeLine("} //end "+children[compIndex].name,level);
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
			for(int t=0; t<tab; t++){out.write("	");}
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
