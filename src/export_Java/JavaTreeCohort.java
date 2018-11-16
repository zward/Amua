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

package export_Java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class JavaTreeCohort{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	JavaModel javaModel;

	//Constructor
	public JavaTreeCohort(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			tree=myModel.tree;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
			numDimensions=myModel.dimInfo.dimNames.length;
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(dir+"main.java"); //Create new file
			out = new BufferedWriter(fstream);
			
			javaModel=new JavaModel(dir,out,myModel);
			
			javaModel.writeProperties();
			writeLine("");
			if(tableFormat==1){ //csv
				writeLine("import java.io.File;");
			}
			writeLine("import java.util.ArrayList;");
			writeLine("");
			writeLine("public class main {");
			writeLine("");	
			writeLine("	public static void main(String[] args) {");
			writeLine("		//Instantiate main (outer) class");
			writeLine("		main outer=new main();");
			writeLine("");	
			
			javaModel.writeTables(tableFormat);

			javaModel.writeParameters();

			javaModel.writeVariables();

			writeLine("		//Define tree"); 
			writeLine("		Node Root=outer.new Node(1); //Root decision node");
			//Define nodes - add nodes recursively
			addNode(0);
			writeLine("");

			//Define complementary probs
			defineCompProbs();

			writeLine("		//Run tree");
			writeLine("		runTree(Root);");
			writeLine("");	
			writeLine("		//Display output for each strategy");
			writeLine("		int numChildren=Root.children.size();");
			writeLine("		for(int i=0; i<numChildren; i++){");
			writeLine("			Node child=Root.children.get(i);");
			writeLine("			System.out.println(\"Strategy: \"+child.name);");
			out.write("			System.out.println(");
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDimensions-1; d++){
				out.write("\"EV ("+info.dimSymbols[d]+"): \"+child.expected"+info.dimNames[d]+"+\"	\"+");
			}
			out.write("\"EV ("+info.dimSymbols[numDimensions-1]+"): \"+child.expected"+info.dimNames[numDimensions-1]+");");
			out.newLine();
			writeLine("			System.out.println(\"Children...\");");
			out.write("			System.out.println(\"Name	");
			for(int d=0; d<numDimensions-1; d++){
				out.write("EV("+info.dimSymbols[d]+")	");
			}
			out.write("EV("+info.dimSymbols[numDimensions-1]+")\");"); out.newLine();
			writeLine("			displayResults(child);");
			writeLine("			System.out.println(); //Blank");
			writeLine("		}");
			writeLine("	}");

			writeLine("");
			defineMethods();

			writeLine("");
			writeLine("");
			
			javaModel.defineFunctions();
			
			defineNodeClass();
			javaModel.writeTableClass();
			
			

			out.write("}"); //Close class
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
				out.write("		Node "+child.nameExport+"="+curNode.nameExport+".addNode(");
				out.write("\""+child.nameExport+"\","); //Name
				out.write(child.type+","); //Type
				//Prob
				if(child.prob.matches("C") || child.prob.matches("c")){out.write("-1,");}
				else{out.write(javaModel.translate(child.prob,false)+",");}
				//Cost
				for(int d=0; d<numDimensions; d++){out.write(javaModel.translate(child.cost[d],false)+",");}
				//Payoff
				for(int d=0; d<numDimensions-1; d++){out.write(javaModel.translate(child.payoff[d],false)+",");}
				out.write(child.payoff[numDimensions-1]+");");
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
			//Run Tree
			writeLine("	//Recursively roll back tree");
			writeLine("	private static void runTree(Node curNode){");
			writeLine("		//Get expected value of node");
			writeLine("		int numChildren=curNode.children.size();");
			writeLine("		if(numChildren==0){ //No children");
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDimensions; d++){
				String dim=info.dimNames[d];
				writeLine("			curNode.expected"+dim+"=curNode.payoff"+dim+"+curNode.cost"+dim+";");
			}
			writeLine("		}");
			writeLine("		else{ //Get expected value of children");
			for(int d=0; d<numDimensions; d++){
				String dim=info.dimNames[d];
				writeLine("			curNode.expected"+dim+"=curNode.cost"+dim+";");
			}
			writeLine("			for(int i=0; i<numChildren; i++){");
			writeLine("				Node child=curNode.children.get(i);");
			writeLine("				runTree(child);");
			for(int d=0; d<numDimensions; d++){
				String dim=info.dimNames[d];
				writeLine("				curNode.expected"+dim+"+=(child.prob*child.expected"+dim+");");
			}
			writeLine("			}");
			writeLine("		}");
			writeLine("	}");
			writeLine("");
			//Display Results
			writeLine("	private static void displayResults(Node curNode){");
			writeLine("		int numChildren=curNode.children.size();");
			writeLine("		for(int i=0; i<numChildren; i++){");
			writeLine("			Node child=curNode.children.get(i);");
			out.write("			System.out.println(child.name+\"	\"+");
			for(int d=0; d<numDimensions-1; d++){
				out.write("child.expected"+info.dimNames[d]+"+\"	\"+");
			}
			out.write("child.expected"+info.dimNames[numDimensions-1]+");");
			out.newLine();
			writeLine("			displayResults(child);");
			writeLine("		}");
			writeLine("	}");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineNodeClass(){
		try{
			writeLine("	//Define inner class");
			writeLine("	class Node{");
			writeLine("		//Attributes");
			writeLine("		String name;");
			writeLine("		int type; //0=Decision, 1=Chance, 2=Terminal");
			writeLine("		double prob;");
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDimensions; d++){
				String dim=info.dimNames[d];
				writeLine("		double cost"+dim+", payoff"+dim+", expected"+dim+";");
			}
			writeLine("		ArrayList<Node> children;");
			writeLine("");
			writeLine("		//Constructor");
			writeLine("		public Node(int type){");
			writeLine("			this.type=type;");
			writeLine("			children=new ArrayList<Node>();");
			writeLine("		}");
			writeLine("");
			out.write("		public Node addNode(String name, int type, double prob, ");
			for(int d=0; d<numDimensions; d++){out.write("double cost"+info.dimNames[d]+", ");}
			for(int d=0; d<numDimensions-1; d++){out.write("double payoff"+info.dimNames[d]+", ");}
			out.write("double payoff"+info.dimNames[numDimensions-1]+"){");
			out.newLine();
			writeLine("			Node child=new Node(type);");
			writeLine("			child.name=name;");
			writeLine("			child.type=type;");
			writeLine("			child.prob=prob;");
			for(int d=0; d<numDimensions; d++){
				String dim=info.dimNames[d];
				writeLine("			child.cost"+dim+"=cost"+dim+";");
				writeLine("			child.payoff"+dim+"=payoff"+dim+";");
			}
			writeLine("			this.children.add(child);");
			writeLine("			return(child);");
			writeLine("		}");
			writeLine("	}");
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

	private void defineCompProbs(){
		try{
			boolean headerWritten=false;
			int numNodes=tree.nodes.size();
			for(int i=1; i<numNodes; i++){
				TreeNode curNode=tree.nodes.get(i);
				if(curNode.prob.matches("C") || curNode.prob.matches("c")){ //Complementary
					if(headerWritten==false){
						writeLine("		//Define complementary probs");
						headerWritten=true;
					}
					out.write("		"+curNode.nameExport+".prob=1.0");
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
					out.write(";"); out.newLine();
				}
			}
			writeLine("");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	
}
