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

package tree;

import base.AmuaModel;
import main.Variable;
import math.Interpreter;
import math.NumericException;

public class TreeCohort{
	TreeNode root;
	DecisionTree tree;
	int numDim;
	int numVars;
	Variable variables[];
	AmuaModel myModel;

	//Constructor
	public TreeCohort(TreeNode root){
		this.root=root;
		this.tree=root.tree;
		this.myModel=root.myModel;
		numDim=root.numDimensions;
		numVars=myModel.variables.size();
		variables=new Variable[numVars];
		for(int c=0; c<numVars; c++){ //get pointers
			variables[c]=myModel.variables.get(c);
		}
	}

	public void simulate(boolean display) throws NumericException, Exception{
		//Initialize variables
		for(int c=0; c<numVars; c++){
			variables[c].value=Interpreter.evaluate(variables[c].initValue, myModel,false);
		}

		root.totalDenom=myModel.cohortSize;
		traverseNode(root,display); //run tree

	}

	/**
	 * Recursively traverse tree
	 * @throws Exception 
	 */
	private void traverseNode(TreeNode node, boolean display) throws Exception{
		//Update costs
		if(node.hasCost){
			for(int c=0; c<numDim; c++){
				node.curCosts[c]=Interpreter.evaluate(node.cost[c],myModel,false).getDouble();
			}
		}

		//Update variables
		if(node.hasVarUpdates){
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].updateMonteCarlo(false);
			}
		}

		//Update payoffs
		if(node.type==2){ //terminal node
			for(int c=0; c<numDim; c++){
				node.curPayoffs[c]=Interpreter.evaluate(node.payoff[c],myModel,false).getDouble();
			}
		}


		//Update child probabilities
		if(node.type==1){
			//Calculate probabilities for children
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				TreeNode curChild=node.children[c];
				if(curChild.prob.matches("C") || curChild.prob.matches("c")){ //Complementary
					curChild.curProb=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					curChild.curProb=Interpreter.evaluate(curChild.prob,myModel,false).getDouble();
					sumProb+=curChild.curProb;
				}
			}
			if(indexCompProb==-1){
				if(sumProb!=1.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
			}
			else{
				if(sumProb>1.0 || sumProb<0.0){ //throw error
					throw new Exception("Probability error: "+node.name+" (Prob="+sumProb+")");
				}
				else{
					TreeNode curChild=node.children[indexCompProb];
					curChild.curProb=1.0-sumProb;
				}
			}
		}

		//Get expected value of node
		node.expectedValues=new double[numDim];
		if(node.numChildren==0){
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curPayoffs[i]+node.curCosts[i];
			}
		}
		else{
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=node.curCosts[i];
			}
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				if(node.type==0){child.totalDenom=node.totalDenom;} //decision node, all go down
				else{child.totalDenom=node.totalDenom*child.curProb;}
				traverseNode(child,display);
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=(child.curProb*child.expectedValues[i]);
				}
			}
		}
		if(display==true){//Display
			if(node.type==1){ //chance node
				String buildString="";
				for(int i=0; i<numDim-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+myModel.round(node.expectedValues[i],i)+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[numDim-1]+") "+myModel.round(node.expectedValues[numDim-1],numDim-1);
				node.textEV.setText(buildString);
				if(node.visible){
					node.textEV.setVisible(true);
				}
			}
			else if(node.type==2){ //terminal node
				node.textNumEnd.setText(Math.round(node.totalDenom*100)/100.0+"");
				if(node.visible){
					node.textNumEnd.setVisible(true);
				}
			}
		}


	}



}