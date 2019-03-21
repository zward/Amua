/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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

package tree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import math.MathUtils;

public class TreeReport{
	DecisionTree tree;
	int rowIndex=-1;
	int numRows=0;
	int maxLevels=1;
	ArrayList<String> rowNames[];
	double rowValues[];
	
	//Constructor
	public TreeReport(DecisionTree tree1){
		this.tree=tree1;
		
		//Get # of rows
		int numNodes=tree.nodes.size();
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
			if(curNode.type==2){ //terminal node
				maxLevels=Math.max(curNode.level,maxLevels);
				numRows++;
			}
		}
		
		rowValues=new double[numRows];
		rowIndex=0;
		getBranchValues(tree.nodes.get(0));
	}
	
	private void getBranchValues(TreeNode node){
		if(node.type==2){ //Terminal
			rowValues[rowIndex]=node.totalDenom;
			rowIndex++;
		}
		else{
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				getBranchValues(child);
			}
		}
	}
	
	public void getNames(){
		rowNames=new ArrayList[numRows];
		rowIndex=0;
		TreeNode root=tree.nodes.get(0);
		for(int c=0; c<root.numChildren; c++){
			ArrayList<String> curPath=new ArrayList<String>();
			getBranchNames(root.children[c],curPath);
		}
	}
	
	private void getBranchNames(TreeNode node,ArrayList<String> parentPath){
		ArrayList<String> curPath=new ArrayList<String>();
		for(int i=0; i<parentPath.size(); i++){
			curPath.add(parentPath.get(i));
		}
		curPath.add(node.name);
		if(node.type==2){ //Terminal
			rowNames[rowIndex]=new ArrayList<String>();
			for(int i=0; i<curPath.size(); i++){
				rowNames[rowIndex].add(curPath.get(i));
			}
			rowIndex++; //next row
		}
		else{
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				getBranchNames(child,curPath);
			}
		}
	}
		
	public void write(String filepath) throws IOException{
		getNames();
		
		FileWriter fstream = new FileWriter(filepath); //Create new file
		BufferedWriter out = new BufferedWriter(fstream);

		//Headers
		for(int i=0; i<maxLevels; i++){out.write("Level "+i+",");}
		out.write("#");
		out.newLine();
		
		//Data
		for(int r=0; r<numRows; r++){
			ArrayList<String> names=rowNames[r];
			int numNames=names.size();
			for(int i=0; i<numNames; i++){out.write(names.get(i)+",");}
			for(int i=numNames; i<maxLevels; i++){out.write(",");}
			out.write(rowValues[r]+"");
			out.newLine();
		}
		
		out.close();
	}
}