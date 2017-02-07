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

public class CppTree{

	BufferedWriter out;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;

	//Constructor
	public CppTree(String filepath,PanelTree myPanel){
		try{
			tree=myPanel.tree;
			numDim=tree.dimNames.length;
			errorLog=myPanel.errorLog;
			filepath=filepath.replaceAll(".cpp", "");
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(filepath+".cpp"); //Create new file
			out = new BufferedWriter(fstream);

			writeProperties(myPanel);
			writeLine("");
			writeLine("#include <iostream>");
			writeLine("#include <string>");
			writeLine("#include <vector>");
			writeLine("");
			writeLine("using namespace std;");
			writeLine("");
			defineNodeClass();
			writeLine("");
			defineMethods();
			writeLine("");
			writeLine("int main(int argc, char * argv[]) {");
			defineVariables();
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
			for(int d=0; d<numDim-1; d++){
				out.write("<<\"EV ("+tree.dimSymbols[d]+"): \" << child->expected"+tree.dimNames[d]+" << \"	\"");
			}
			out.write("<<\"EV ("+tree.dimSymbols[numDim-1]+"): \" << child->expected"+tree.dimNames[numDim-1]+"<<\"\\n\";");
			out.newLine();
			writeLine("		cout<<\"Children...\\n\";");
			out.write("		cout<<(\"Name	");
			for(int d=0; d<numDim-1; d++){out.write("EV("+tree.dimSymbols[d]+")	");}
			out.write("EV("+tree.dimSymbols[numDim-1]+")\\n\");");
			out.newLine();
			writeLine("		displayResults(child);");
			writeLine("		cout << \"\\n\"; //Blank");
			writeLine("	}");
			writeLine("");
			writeLine("	system(\"pause\");");
			writeLine("	return(0);");
			writeLine("}");
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
				out.write("	Node* "+child.nameExport+"="+curNode.nameExport+"->addNode(");
				out.write("\""+child.nameExport+"\","); //Name
				out.write(child.type+","); //Type
				//Prob
				if(child.prob.matches("C")){out.write("-1,");}
				else{out.write(child.prob+",");}
				for(int d=0; d<numDim; d++){out.write(child.cost[d]+",");}
				for(int d=0; d<numDim-1; d++){out.write(child.payoff[d]+",");}
				out.write(child.payoff[numDim-1]+");");
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
			writeLine("//Recursively roll back tree");
			writeLine("void runTree(Node* curNode) {");
			writeLine("	//Get expected value of node");
			writeLine("	int numChildren = curNode->children.size();");
			writeLine("	if (numChildren == 0) { //No children");
			for(int d=0; d<numDim; d++){
				String dim=tree.dimNames[d];
				writeLine("		curNode->expected"+dim+" = curNode->payoff"+dim+" + curNode->cost"+dim+";");
			}
			writeLine("	}");
			writeLine("	else { //Get expected value of children");
			for(int d=0; d<numDim; d++){
				String dim=tree.dimNames[d];
				writeLine("		curNode->expected"+dim+" =  curNode->cost"+dim+";");
			}
			writeLine("		for (int i = 0; i<numChildren; i++) {");
			writeLine("			Node* child = curNode->children[i];");
			writeLine("			runTree(child);");
			for(int d=0; d<numDim; d++){
				String dim=tree.dimNames[d];
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
			for(int d=0; d<numDim-1; d++){out.write(" << child->expected"+tree.dimNames[d]+" << \"	\"");}
			out.write(" << child->expected"+tree.dimNames[numDim-1]+" << \"\\n\";"); out.newLine();
			writeLine("		displayResults(child);");
			writeLine("	}");
			writeLine("}");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineNodeClass(){
		try{
			writeLine("//Define inner class");
			writeLine("class Node {");
			writeLine("	//Attributes");
			writeLine("	public:");
			writeLine("		string name;");
			writeLine("		int type; //0=Decision, 1=Chance, 2=Terminal");
			writeLine("		double prob;");
			for(int d=0; d<numDim; d++){
				String dim=tree.dimNames[d];
				writeLine("		double cost"+dim+", payoff"+dim+", expected"+dim+";");
			}
			writeLine("		vector<Node*> children;");
			writeLine("");
			writeLine("	//Constructor");
			writeLine("	public:"); 
			writeLine("		Node(int type1) {");
			writeLine("			type = type1;//children = new Node(type);");
			writeLine("		}");
			writeLine("		Node();");
			writeLine("");		
			out.write("	public: Node* addNode(string name, int type, double prob, ");
			for(int d=0; d<numDim; d++){out.write("double cost"+tree.dimNames[d]+", ");}
			for(int d=0; d<numDim-1; d++){out.write("double payoff"+tree.dimNames[d]+", ");}
			out.write("double payoff"+tree.dimNames[numDim-1]+") {");
			out.newLine();
			writeLine("		Node * child = new Node(type);");
			writeLine("		child->name = name;");
			writeLine("		child->type = type;");
			writeLine("		child->prob = prob;");
			for(int d=0; d<numDim; d++){writeLine("		child->cost"+tree.dimNames[d]+" = cost"+tree.dimNames[d]+";");}
			for(int d=0; d<numDim; d++){writeLine("		child->payoff"+tree.dimNames[d]+" = payoff"+tree.dimNames[d]+";");}
			writeLine("		children.push_back(child);");
			writeLine("		return(child);");
			writeLine("	}");
			writeLine("};");
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
				writeLine("	//Define variables");
				for(int i=0; i<numVars; i++){
					TreeVariable curVar=tree.variables.get(i);
					writeLine("	double "+curVar.name+"="+curVar.value+"; //Expression: "+curVar.expression);
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
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void writeProperties(PanelTree myPanel){
		try{
			writeLine("/*");
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
			writeLine("*/");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

}
