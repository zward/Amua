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
import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class PythonTreeCohort{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;
	int tableFormat;
	String dir;
	PythonModel pyModel;
	
	//Constructor
	public PythonTreeCohort(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			tree=myModel.tree;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
			numDim=myModel.dimInfo.dimNames.length;
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
			
			defineNodeClass();
			pyModel.writeTableClass(tableFormat);
			writeLine(0,""); //Blank
			defineMethods();
			writeLine(0,"");
			pyModel.writeTables(tableFormat);
			pyModel.writeParameters();
			
			pyModel.writeVariables();
			
			writeLine(0,"#Define tree");
			writeLine(0,"Root=Node(1) #Root decision node");

			//Define nodes - add nodes recursively
			addNode(0);
			writeLine(0,"");

			defineCompProbs();

			writeLine(0,"#Run tree");
			writeLine(0,"runTree(Root)");
			writeLine(0,"");
			writeLine(0,"#Display output for each strategy");
			writeLine(0,"for child in Root.children:");
				writeLine(1,"print(\"Strategy:\",child.name)");
				out.write("    print(");
				DimInfo info=myModel.dimInfo;
				for(int d=0; d<numDim-1; d++){out.write("\"EV ("+info.dimSymbols[d]+"):\",child.expected"+info.dimNames[d]+",");}
				out.write("\"EV ("+info.dimSymbols[numDim-1]+"):\",child.expected"+info.dimNames[numDim-1]+")");
				out.newLine();
				writeLine(1,"print(\"Children...\")");
				out.write("    print(\"Name");
				for(int d=0; d<numDim; d++){out.write("	EV("+info.dimSymbols[d]+")");}
				out.write("\")");
				out.newLine();
				writeLine(1,"displayResults(child)");
				writeLine(1,"print(\"\") #Blank");

			out.close();

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void addNode(int curIndex){
		try{
			TreeNode curNode=tree.nodes.get(curIndex);
			int numChildren=curNode.childIndices.size();
			//Add child nodes
			for(int i=0; i<numChildren; i++){
				int childIndex=curNode.childIndices.get(i);
				TreeNode child=tree.nodes.get(childIndex);
				out.write(child.nameExport+"="+curNode.nameExport+".addNode(");
				out.write("\""+child.nameExport+"\","); //Name
				out.write(child.type+","); //Type
				//Prob
				if(child.prob.matches("C") || child.prob.matches("c")){out.write("-1,");}
				else{out.write(pyModel.translate(child.prob,false)+",");}
				//Cost
				for(int d=0; d<numDim; d++){out.write(pyModel.translate(child.cost[d],false)+",");}
				//Payoff
				for(int d=0; d<numDim-1; d++){out.write(pyModel.translate(child.payoff[d],false)+",");}
				out.write(pyModel.translate(child.payoff[numDim-1],false)+")");
				out.newLine();
				addNode(childIndex);
			}
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineMethods(){
		try{
			writeLine(0,"#Recursively roll back tree");
			writeLine(0,"def runTree(curNode):");
				writeLine(1,"#Get expected value of node");
				writeLine(1,"numChildren=len(curNode.children)");
				writeLine(1,"if(numChildren==0): #No children");
				DimInfo info=myModel.dimInfo;
				for(int d=0; d<numDim; d++){
					writeLine(2,"curNode.expected"+info.dimNames[d]+"=curNode.payoff"+info.dimNames[d]+"+curNode.cost"+info.dimNames[d]);
				}
				writeLine(1,"else: #Get expected value of children");
					for(int d=0; d<numDim; d++){writeLine(2,"curNode.expected"+info.dimNames[d]+"=curNode.cost"+info.dimNames[d]);} 
					writeLine(2,"for child in curNode.children:");
						writeLine(3,"runTree(child)");
						for(int d=0; d<numDim; d++){writeLine(3,"curNode.expected"+info.dimNames[d]+"+=(child.prob*child.expected"+info.dimNames[d]+")");}
				writeLine(1,"return;");
			writeLine(0,"");
			writeLine(0,"#Recursively display results");
			writeLine(0,"def displayResults(curNode):");
				writeLine(1,"for child in curNode.children:");
					out.write("        print(child.name,");
					for(int d=0; d<numDim; d++){out.write("child.expected"+info.dimNames[d]+",");}
					out.write("sep=\"	\")");
					out.newLine();
					writeLine(2,"displayResults(child)");
				writeLine(1,"return;");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineNodeClass(){
		try{
			writeLine(0,"class Node:");
				writeLine(1,"\"Tree Node class\"");
				writeLine(1,"name=\"\"");
				writeLine(1,"prob=0");
				DimInfo info=myModel.dimInfo;
				for(int d=0; d<numDim; d++){writeLine(1,"cost"+info.dimNames[d]+"=0");}
				for(int d=0; d<numDim; d++){writeLine(1,"payoff"+info.dimNames[d]+"=0");}
				for(int d=0; d<numDim; d++){writeLine(1,"expected"+info.dimNames[d]+"=0");}
			writeLine(0,"");
				writeLine(1,"def __init__(self,nodeType):");
					writeLine(2,"self.nodeType=nodeType #0=Decision, 1=Chance, 2=Terminal");
					writeLine(2,"self.children=[]");
				out.write("    def addNode(self, name, nodeType, prob, ");
				for(int d=0; d<numDim; d++){out.write("cost"+info.dimNames[d]+", ");}
				for(int d=0; d<numDim-1; d++){out.write("payoff"+info.dimNames[d]+", ");}
				out.write("payoff"+info.dimNames[numDim-1]+"):");
				out.newLine();
					writeLine(2,"child=Node(nodeType)");
					writeLine(2,"child.name=name");
					writeLine(2,"child.nodeType=nodeType");
					writeLine(2,"child.prob=prob");
					for(int d=0; d<numDim; d++){writeLine(2,"child.cost"+info.dimNames[d]+"=cost"+info.dimNames[d]);}
					for(int d=0; d<numDim; d++){writeLine(2,"child.payoff"+info.dimNames[d]+"=payoff"+info.dimNames[d]);}
					writeLine(2,"self.children.append(child)");
					writeLine(2,"return(child)");
			writeLine(0,"");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
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
	
	private void defineCompProbs(){
		try{
			boolean headerWritten=false;
			int numNodes=tree.nodes.size();
			for(int i=1; i<numNodes; i++){ //Skip root node
				TreeNode curNode=tree.nodes.get(i);
				if(curNode.prob.matches("C")||curNode.prob.matches("c")){ //Complementary
					if(headerWritten==false){
						writeLine(0,"#Define complementary probs");
						headerWritten=true;
					}
					out.write(curNode.nameExport+".prob=1.0");
					int parentIndex=tree.getParentIndex(i);
					TreeNode parent=tree.nodes.get(parentIndex);
					int numChildren=parent.childIndices.size();
					for(int c=0; c<numChildren; c++){
						int childIndex=parent.childIndices.get(c);
						if(childIndex!=i){
							TreeNode child=tree.nodes.get(childIndex);
							out.write("-"+child.nameExport+".prob");
						}
					}
					out.newLine();
				}
			}
			writeLine(0,"");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	
}
