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
import base.AmuaModel;
import main.*;
import tree.DecisionTree;
import tree.TreeNode;

public class RTreeCohort{

	BufferedWriter out;
	AmuaModel myModel;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;
	int tableFormat;
	String dir;
	RModel rModel;
	
	//Constructor
	public RTreeCohort(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			tree=myModel.tree;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
			numDim=myModel.dimInfo.dimNames.length;
			//Open file for writing
			FileWriter fstream = new FileWriter(dir+"main.R"); //Create new file
			out = new BufferedWriter(fstream);
			
			rModel=new RModel(dir,out,myModel);

			rModel.writeProperties();
			writeLine("");
			writeLine("### Define Node Class");
			writeLine("setRefClass(\"Node\",");
			writeLine("  fields=list(name=\"character\",prob=\"numeric\",");
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDim; d++){
				String dim=info.dimNames[d];
				writeLine("             cost"+dim+"=\"numeric\",payoff"+dim+"=\"numeric\",expected"+dim+"=\"numeric\",");
			}
			writeLine("             children=\"vector\")");
			writeLine(")");
			writeLine("");
			
			rModel.writeTableClass();
			
			writeLine("### Define Run tree");
			writeLine("evaluateNode<-function(curNode){");
			writeLine("  numChildren=length(curNode$children)");
			writeLine("  if(numChildren==0){");
			for(int d=0; d<numDim; d++){writeLine("    curNode$expected"+info.dimNames[d]+"=curNode$payoff"+info.dimNames[d]+"+curNode$cost"+info.dimNames[d]);}
			writeLine("  }");
			writeLine("  else{ #Evaluate children");
			for(int d=0; d<numDim; d++){writeLine("    curNode$expected"+info.dimNames[d]+"=curNode$cost"+info.dimNames[d]);}
			writeLine("    for(i in 1:numChildren){");
			writeLine("      child<-tree[[curNode$children[i]]] #Get child");
			writeLine("      evaluateNode(child)");
			for(int d=0; d<numDim; d++){writeLine("      curNode$expected"+info.dimNames[d]+"=curNode$expected"+info.dimNames[d]+"+(child$prob*child$expected"+info.dimNames[d]+")");}
			writeLine("    }");  
			writeLine("  }");
			writeLine("}");
			writeLine("");
			writeLine("### Define display results");
			writeLine("displayEV<-function(curNode){");
			writeLine("  numChildren=length(curNode$children)");
			writeLine("  if(numChildren>0){");
			writeLine("    for(i in 1:numChildren){");
			writeLine("      curChild<-tree[[curNode$children[i]]]");
			out.write("      print(paste(curChild$name,");
			for(int d=0; d<numDim-1; d++){out.write("curChild$expected"+info.dimNames[d]+",");}
			out.write("curChild$expected"+info.dimNames[numDim-1]+"))");
			out.newLine();
			writeLine("      displayEV(curChild)");
			writeLine("    }");
			writeLine("  }");
			writeLine("}");
			writeLine("");
			writeLine("");
			rModel.writeTables(tableFormat);
			
			rModel.writeParameters();
			
			rModel.writeVariables();
			
			writeLine("### Create tree");
			int numNodes=tree.nodes.size();
			out.write("Root<-new(\"Node\",name=\"Root\"");
			int numChildren=tree.nodes.get(0).childIndices.size();
			if(numChildren>0){
				out.write(",children=c(");
				for(int i=0; i<numChildren-1; i++){
					out.write((tree.nodes.get(0).childIndices.get(i)+1)+","); //Index from 1 instead of 0
				}
				out.write((tree.nodes.get(0).childIndices.get(numChildren-1)+1)+")");
			}
			out.write(")");
			out.newLine();
			for(int i=1; i<numNodes; i++){addNode(i);}
			out.write("tree<-c(");
			for(int i=0; i<numNodes-1; i++){out.write(tree.nodes.get(i).nameExport+",");}
			out.write(tree.nodes.get(numNodes-1).nameExport+")"); out.newLine();
			writeLine("");
			
			defineCompProbs();
			
			writeLine("###Run tree");
			writeLine("evaluateNode(tree[[1]])");
			writeLine("");
			writeLine("### Display output for each strategy");
			writeLine("numStrategies=length(Root$children)");
			writeLine("for(i in 1:numStrategies){");
			writeLine("	curNode<-tree[[Root$children[i]]]");
			out.write("	print(paste(\"Strategy:\",curNode$name,");
			for(int d=0; d<numDim-1; d++){out.write("curNode$expected"+info.dimNames[d]+",");}
			out.write("curNode$expected"+info.dimNames[numDim-1]+"))");
			out.newLine();
			writeLine("	print(\"Children...\")");
			writeLine("	displayEV(curNode)");
			writeLine("}");
			
			out.close();
			
			rModel.defineFunctions();

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void addNode(int curIndex){
		try{
			TreeNode curNode=tree.nodes.get(curIndex);
			out.write(curNode.nameExport+"<-new(\"Node\",name=\""+curNode.nameExport+"\",");
			//Prob
			if(curNode.prob.matches("C") || curNode.prob.matches("c")){out.write("prob=-1,");}
			else{out.write("prob="+rModel.translate(curNode.prob,false)+",");}
			DimInfo info=myModel.dimInfo;
			for(int d=0; d<numDim; d++){out.write("cost"+info.dimNames[d]+"="+rModel.translate(curNode.cost[d],false)+",");}
			for(int d=0; d<numDim-1; d++){out.write("payoff"+info.dimNames[d]+"="+rModel.translate(curNode.payoff[d],false)+",");}
			out.write("payoff"+info.dimNames[numDim-1]+"="+rModel.translate(curNode.payoff[numDim-1],false));
			int numChildren=curNode.childIndices.size();
			if(numChildren>0){
				out.write(",children=c(");
				for(int i=0; i<numChildren-1; i++){
					out.write((curNode.childIndices.get(i)+1)+","); //Index from 1 instead of 0
				}
				out.write((curNode.childIndices.get(numChildren-1)+1)+")");
			}
			out.write(")");
			//out.write("eM=0,eU=0)");
			out.newLine();
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
			for(int i=1; i<numNodes; i++){ //Skip root
				TreeNode curNode=tree.nodes.get(i);
				if(curNode.prob.matches("C")||curNode.prob.matches("c")){ //Complementary
					if(headerWritten==false){
						writeLine("###Define complementary probs");
						headerWritten=true;
					}
					out.write(curNode.nameExport+"$prob=1.0");
					int parentIndex=tree.getParentIndex(i);
					TreeNode parent=tree.nodes.get(parentIndex);
					int numChildren=parent.childIndices.size();
					for(int c=0; c<numChildren; c++){
						int childIndex=parent.childIndices.get(c);
						if(childIndex!=i){
							TreeNode child=tree.nodes.get(childIndex);
							out.write("-"+child.nameExport+"$prob");
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
	
}
