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

package export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

import main.*;

public class PythonTree{

	BufferedWriter out;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;

	//Constructor
	public PythonTree(String filepath,PanelTree myPanel){
		try{
			tree=myPanel.tree;
			errorLog=myPanel.errorLog;
			numDim=tree.dimNames.length;
			filepath=filepath.replaceAll(".py", "");
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(filepath+".py"); //Create new file
			out = new BufferedWriter(fstream);

			writeProperties(myPanel);
			writeLine("");

			defineNodeClass();
			writeLine(""); //Blank
			defineMethods();
			writeLine("");
			defineVariables();
			writeLine("#Define tree");
			writeLine("Root=Node(1) #Root decision node");

			//Define nodes - add nodes recursively
			addNode(0);
			writeLine("");

			defineCompProbs();

			writeLine("#Run tree");
			writeLine("runTree(Root)");
			writeLine("");
			writeLine("#Display output for each strategy");
			writeLine("for child in Root.children:");
			writeLine("\tprint(\"Strategy:\",child.name)");
			out.write("\tprint(");
			for(int d=0; d<numDim-1; d++){out.write("\"EV ("+tree.dimSymbols[d]+"):\",child.expected"+tree.dimNames[d]+",");}
			out.write("\"EV ("+tree.dimSymbols[numDim-1]+"):\",child.expected"+tree.dimNames[numDim-1]+")");
			out.newLine();
			writeLine("\tprint(\"Children...\")");
			out.write("\tprint(\"Name");
			for(int d=0; d<numDim; d++){out.write("	EV("+tree.dimSymbols[d]+")");}
			out.write("\")");
			out.newLine();
			writeLine("\tdisplayResults(child)");
			writeLine("\tprint(\"\") #Blank");

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
				if(child.prob.matches("C")){out.write("-1,");}
				else{out.write(child.prob+",");}
				for(int d=0; d<numDim; d++){out.write(child.cost[d]+",");}
				for(int d=0; d<numDim-1; d++){out.write(child.payoff[d]+",");}
				out.write(child.payoff[numDim-1]+")");
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
			writeLine("#Recursively roll back tree");
			writeLine("def runTree(curNode):");
			writeLine("\t#Get expected value of node");
			writeLine("\tnumChildren=len(curNode.children)");
			writeLine("\tif(numChildren==0): #No children");
			for(int d=0; d<numDim; d++){
				writeLine("\t\tcurNode.expected"+tree.dimNames[d]+"=curNode.payoff"+tree.dimNames[d]+"+curNode.cost"+tree.dimNames[d]);
			}
			writeLine("\telse: #Get expected value of children");
			for(int d=0; d<numDim; d++){writeLine("\t\tcurNode.expected"+tree.dimNames[d]+"=curNode.cost"+tree.dimNames[d]);} 
			writeLine("\t\tfor child in curNode.children:");
			writeLine("\t\t\trunTree(child)");
			for(int d=0; d<numDim; d++){writeLine("\t\t\tcurNode.expected"+tree.dimNames[d]+"+=(child.prob*child.expected"+tree.dimNames[d]+")");}
			writeLine("\treturn;");
			writeLine("");
			writeLine("#Recursively display results");
			writeLine("def displayResults(curNode):");
			writeLine("\tfor child in curNode.children:");
			out.write("\t\tprint(child.name,");
			for(int d=0; d<numDim; d++){out.write("child.expected"+tree.dimNames[d]+",");}
			out.write("sep=\"	\")");
			out.newLine();
			writeLine("\t\tdisplayResults(child)");
			writeLine("\treturn;");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineNodeClass(){
		try{
			writeLine("class Node:");
			writeLine("\t\"Tree Node class\"");
			writeLine("\tname=\"\"");
			writeLine("\tprob=0");
			for(int d=0; d<numDim; d++){writeLine("\tcost"+tree.dimNames[d]+"=0");}
			for(int d=0; d<numDim; d++){writeLine("\tpayoff"+tree.dimNames[d]+"=0");}
			for(int d=0; d<numDim; d++){writeLine("\texpected"+tree.dimNames[d]+"=0");}
			writeLine("");
			writeLine("\tdef __init__(self,nodeType):");
			writeLine("\t\tself.nodeType=nodeType #0=Decision, 1=Chance, 2=Terminal");
			writeLine("\t\tself.children=[]");
			out.write("\tdef addNode(self, name, nodeType, prob, ");
			for(int d=0; d<numDim; d++){out.write("cost"+tree.dimNames[d]+", ");}
			for(int d=0; d<numDim-1; d++){out.write("payoff"+tree.dimNames[d]+", ");}
			out.write("payoff"+tree.dimNames[numDim-1]+"):");
			out.newLine();
			writeLine("\t\tchild=Node(nodeType)");
			writeLine("\t\tchild.name=name");
			writeLine("\t\tchild.nodeType=nodeType");
			writeLine("\t\tchild.prob=prob");
			for(int d=0; d<numDim; d++){writeLine("\t\tchild.cost"+tree.dimNames[d]+"=cost"+tree.dimNames[d]);}
			for(int d=0; d<numDim; d++){writeLine("\t\tchild.payoff"+tree.dimNames[d]+"=payoff"+tree.dimNames[d]);}
			writeLine("\t\tself.children.append(child)");
			writeLine("\t\treturn(child)");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineVariables(){
		try{
			int numVars=tree.variables.size();
			if(numVars>0){
				writeLine("#Define variables");
				for(int i=0; i<numVars; i++){
					TreeVariable curVar=tree.variables.get(i);
					writeLine(curVar.name+"="+curVar.value+"; #Expression: "+curVar.expression);
				}
				writeLine("");
			}
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
				if(curNode.prob.matches("C")){ //Complementary
					if(headerWritten==false){
						writeLine("#Define complementary probs");
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
			writeLine("");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void writeProperties(PanelTree myPanel){
		try{
			writeLine("# -*- coding: utf-8 -*-");
			writeLine("\"\"\"");
			writeLine("This code was auto-generated by Amua (https://github.com/zward/Amua)");
			writeLine("Code generated: "+new Date());
			writeLine("Model type: Decision Tree");
			writeLine("Model name: "+myPanel.name);
			writeLine("Created by: "+tree.meta.author);
			writeLine("Created: "+tree.meta.dateCreated);
			writeLine("Version created: "+tree.meta.versionCreated);
			writeLine("Modified by: "+tree.meta.modifier);
			writeLine("Modified: "+tree.meta.dateModified);
			writeLine("Version modified: "+tree.meta.versionModified);
			writeLine("\"\"\"");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}


}
