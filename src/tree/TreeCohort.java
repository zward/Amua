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
import math.MathUtils;
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
			variables[c].value=Interpreter.evaluate(variables[c].expression, myModel,false);
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
			myModel.unlockVars();
			//Perform variable updates
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].update(false);
			}
			//Update any dependent variables
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].variable.updateDependents(myModel);
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
		
		if(node.type==2){ //Terminal node
			for(int i=0; i<numDim; i++){
				node.expectedValues[i]=(node.curPayoffs[i]+node.curCosts[i]);
			}
		}
		else if(node.type==1){ //Chance node
			//Apply cost
			for(int i=0; i<numDim; i++){node.expectedValues[i]=node.curCosts[i];}
			//Get branch EVs
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				child.totalDenom=node.totalDenom*child.curProb;
				traverseNode(child,display);
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=(child.curProb*child.expectedValues[i]);
				}
			}
		}
		else if(node.type==0){ //Decision node
			//Apply cost
			for(int i=0; i<numDim; i++){node.expectedValues[i]=node.curCosts[i];}
			//Get children EVs
			double childEVs[][]=new double[node.numChildren][numDim];
			for(int c=0; c<node.numChildren; c++){
				TreeNode child=node.children[c];
				child.totalDenom=node.totalDenom; //decision node, all go down
				traverseNode(child,display);
				for(int i=0; i<numDim; i++){
					childEVs[c][i]=child.expectedValues[i];
				}
			}
			//Make decision
			if(myModel.dimInfo.analysisType==0){ //EV
				int obj=myModel.dimInfo.objective;
				int objDim=myModel.dimInfo.objectiveDim;
				double bestEV=childEVs[0][objDim];
				int bestChild=0;
				for(int c=1; c<node.numChildren; c++){
					if(obj==0){ //Maximize
						if(childEVs[c][objDim]>bestEV){
							bestEV=childEVs[c][objDim];	bestChild=c;
						}
					}
					else{ //Minimize
						if(childEVs[c][objDim]<bestEV){
							bestEV=childEVs[c][objDim];	bestChild=c;
						}
					}
				}
				//choose best EV
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=childEVs[bestChild][i];
				}
				//don't choose other branches
				if(display==true && node.level!=0){
					for(int c=0; c<node.numChildren; c++){
						if(c!=bestChild){setEndToZero(node.children[c]);}
					}
				}
				
			}
			else if(myModel.dimInfo.analysisType>0){ //CEA or BCA
				int dimCost=myModel.dimInfo.costDim;
				int dimBenefit=myModel.dimInfo.effectDim;
				double wtp=myModel.dimInfo.WTP;
				double bestNMB=(wtp*childEVs[0][dimBenefit])-childEVs[0][dimCost];
				int bestChild=0;
				for(int c=1; c<node.numChildren; c++){
					double curNMB=(wtp*childEVs[c][dimBenefit])-childEVs[c][dimCost];
					if(curNMB>bestNMB){
						bestNMB=curNMB; bestChild=c;
					}
				}
				//choose best NMB
				for(int i=0; i<numDim; i++){
					node.expectedValues[i]+=childEVs[bestChild][i];
				}
				//don't choose other branches
				if(display==true && node.level!=0){
					for(int c=0; c<node.numChildren; c++){
						if(c!=bestChild){setEndToZero(node.children[c]);}
					}
				}
			}
		}
		
		if(display==true){//Display
			if(node.type!=2 && node.level!=0){ //decision/chance node
				String buildString="";
				for(int i=0; i<numDim-1; i++){
					buildString+="("+myModel.dimInfo.dimSymbols[i]+") "+MathUtils.round(node.expectedValues[i],myModel.dimInfo.decimals[i])+"; ";
				}
				buildString+="("+myModel.dimInfo.dimSymbols[numDim-1]+") "+MathUtils.round(node.expectedValues[numDim-1],myModel.dimInfo.decimals[numDim-1]);
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

	private void setEndToZero(TreeNode node){
		if(node.type==2){
			node.totalDenom=0;
			node.textNumEnd.setText("0");
		}
		else{
			for(int c=0; c<node.numChildren; c++){
				setEndToZero(node.children[c]);
			}
		}
	}


}