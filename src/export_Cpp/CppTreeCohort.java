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

public class CppTreeCohort{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;
	int tableFormat;
	String dir;
	CppModel cppModel;

	//Constructor
	public CppTreeCohort(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			this.myModel=myModel1;
			tree=myModel.tree;
			numDim=myModel.dimInfo.dimNames.length;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
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
			writeLine("");
			writeLine("using namespace std;");
			writeLine("");
			
			defineNodeClass();
			
			writeLine("");
			defineMethods();
			writeLine("");
			writeLine("int main(int argc, char * argv[]) {");
			
			cppModel.writeTableClass();
			cppModel.writeTables(tableFormat);

			cppModel.writeParameters();

			cppModel.writeVariables();
			
			writeLine("	//Define tree");
			//Define nodes - add nodes recursively
			writeLine("	Node* Root = new Node(1); //Root decision");
			addNode(0);
			writeLine("");

			defineCompProbs();

			writeLine("	//Run tree");
			writeLine("	runTree(Root);");
			writeLine("");
			writeLine("	//Display output for each strategy");
			writeLine("	int numChildren = Root->children.size();");
			writeLine("	for (int i = 0; i<numChildren; i++) {");
			writeLine("		Node* child = Root->children[i];");
			writeLine("		cout<<\"Strategy: \" << child->name<<\"\\n\";");
			out.write("		cout");
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDim-1; d++){
				out.write("<<\"EV ("+info.dimSymbols[d]+"): \" << child->expected"+info.dimNames[d]+" << \"	\"");
			}
			out.write("<<\"EV ("+info.dimSymbols[numDim-1]+"): \" << child->expected"+info.dimNames[numDim-1]+"<<\"\\n\";");
			out.newLine();
			writeLine("		cout<<\"Children...\\n\";");
			out.write("		cout<<(\"Name	");
			for(int d=0; d<numDim-1; d++){out.write("EV("+info.dimSymbols[d]+")	");}
			out.write("EV("+info.dimSymbols[numDim-1]+")\\n\");");
			out.newLine();
			writeLine("		displayResults(child);");
			writeLine("		cout << \"\\n\"; //Blank");
			writeLine("	}");
			writeLine("");
			writeLine("	delete Root;");
			cppModel.deleteTables();
			writeLine("");
			//writeLine("	system(\"pause\");");
			writeLine("	return(0);");
			writeLine("}");
			
			cppModel.defineFunctions();
			
			out.close();

		}catch(Exception e){
			recordError(e);
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
				out.write("	Node* "+child.nameExport+"="+curNode.nameExport+"->addNode(");
				out.write("\""+child.nameExport+"\","); //Name
				out.write(child.type+","); //Type
				//Prob
				if(child.prob.matches("C") || child.prob.matches("c")){out.write("-1,");}
				else{out.write(cppModel.translate(child.prob,false)+",");}
				for(int d=0; d<numDim; d++){out.write(cppModel.translate(child.cost[d],false)+",");}
				for(int d=0; d<numDim-1; d++){out.write(cppModel.translate(child.payoff[d],false)+",");}
				out.write(cppModel.translate(child.payoff[numDim-1],false)+");");
				out.newLine();
				addNode(childIndex);
			}
		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineMethods(){
		try{
			DimInfo info=myModel.dimInfo;
			writeLine("//Recursively roll back tree");
			writeLine("void runTree(Node* curNode) {");
			writeLine("	//Get expected value of node");
			writeLine("	int numChildren = curNode->children.size();");
			writeLine("	if (numChildren == 0) { //No children");
			for(int d=0; d<numDim; d++){
				String dim=info.dimNames[d];
				writeLine("		curNode->expected"+dim+" = curNode->payoff"+dim+" + curNode->cost"+dim+";");
			}
			writeLine("	}");
			writeLine("	else { //Get expected value of children");
			for(int d=0; d<numDim; d++){
				String dim=info.dimNames[d];
				writeLine("		curNode->expected"+dim+" =  curNode->cost"+dim+";");
			}
			writeLine("		for (int i = 0; i<numChildren; i++) {");
			writeLine("			Node* child = curNode->children[i];");
			writeLine("			runTree(child);");
			for(int d=0; d<numDim; d++){
				String dim=info.dimNames[d];
				writeLine("			curNode->expected"+dim+" += (child->prob*child->expected"+dim+");");
			}
			writeLine("		}");
			writeLine("	}");
			writeLine("}");
			writeLine("");
			writeLine("void displayResults(Node* curNode) {");
			writeLine("	int numChildren = curNode->children.size();");
			writeLine("	for (int i = 0; i<numChildren; i++) {");
			writeLine("		Node * child = curNode->children[i];");
			out.write("		cout << child->name << \"	\"");
			for(int d=0; d<numDim-1; d++){out.write(" << child->expected"+info.dimNames[d]+" << \"	\"");}
			out.write(" << child->expected"+info.dimNames[numDim-1]+" << \"\\n\";"); out.newLine();
			writeLine("		displayResults(child);");
			writeLine("	}");
			writeLine("}");
		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNodeClass(){
		try{
			DimInfo info=myModel.dimInfo;
			writeLine("//Define inner class");
			writeLine("class Node {");
			writeLine("	//Attributes");
			writeLine("	public:");
			writeLine("		string name;");
			writeLine("		int type; //0=Decision, 1=Chance, 2=Terminal");
			writeLine("		double prob;");
			for(int d=0; d<numDim; d++){
				String dim=info.dimNames[d];
				writeLine("		double cost"+dim+", payoff"+dim+", expected"+dim+";");
			}
			writeLine("		vector<Node*> children;");
			writeLine("");
			writeLine("	public:"); 
			writeLine("		Node(int type1){ //Constructor");
			writeLine("			type = type1;");
			writeLine("		}");
			writeLine("");
			writeLine("		~Node(){ //Destructor");
			writeLine("			int size=children.size();");
			writeLine("			for(int c=0; c<size; c++){");
			writeLine("				delete children[c];");
			writeLine("			}");
			writeLine("			children.clear(); //clear pointers");
			writeLine("		}");
			writeLine("");		
			out.write("	public: Node* addNode(string name, int type, double prob, ");
			for(int d=0; d<numDim; d++){out.write("double cost"+info.dimNames[d]+", ");}
			for(int d=0; d<numDim-1; d++){out.write("double payoff"+info.dimNames[d]+", ");}
			out.write("double payoff"+info.dimNames[numDim-1]+") {");
			out.newLine();
			writeLine("		Node * child = new Node(type);");
			writeLine("		child->name = name;");
			writeLine("		child->type = type;");
			writeLine("		child->prob = prob;");
			for(int d=0; d<numDim; d++){writeLine("		child->cost"+info.dimNames[d]+" = cost"+info.dimNames[d]+";");}
			for(int d=0; d<numDim; d++){writeLine("		child->payoff"+info.dimNames[d]+" = payoff"+info.dimNames[d]+";");}
			writeLine("		children.push_back(child);");
			writeLine("		return(child);");
			writeLine("	}");
			writeLine("};");
			writeLine("");
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

	private void defineCompProbs(){
		try{
			boolean headerWritten=false;
			int numNodes=tree.nodes.size();
			for(int i=1; i<numNodes; i++){ //Skip root node
				TreeNode curNode=tree.nodes.get(i);
				if(curNode.prob.matches("C") || curNode.prob.matches("c")){ //Complementary
					if(headerWritten==false){
						writeLine("	//Define complementary probs");
						headerWritten=true;
					}
					out.write("	"+curNode.nameExport+"->prob=1.0");
					int parentIndex=tree.getParentIndex(i);
					TreeNode parent=tree.nodes.get(parentIndex);
					int numChildren=parent.childIndices.size();
					for(int c=0; c<numChildren; c++){
						int childIndex=parent.childIndices.get(c);
						if(childIndex!=i){
							TreeNode child=tree.nodes.get(childIndex);
							out.write("-"+child.nameExport+"->prob");
						}
					}
					out.write(";"); out.newLine();
				}
			}
			writeLine("");
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
