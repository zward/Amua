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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import main.CEAHelper;
import main.Console;
import main.DimInfo;
import main.VariableUpdate;
import math.Interpreter;
import math.MathUtils;

@XmlRootElement(name="DecisionTree")
/**
 * 
 * @author zward
 *
 */
public class DecisionTree{
	@XmlElement(name = "Node", type = TreeNode.class)public ArrayList<TreeNode> nodes; //Save ArrayList of nodes for direct access to children
	
	@XmlTransient public boolean showEV=false;
	@XmlTransient public AmuaModel myModel;
	@XmlTransient int numDim;

	//Constructor
	/**
	 * Default constructor
	 * @param createRoot  If true adds a decision node as the root.  If false, leaves the nodes empty
	 */
	public DecisionTree(boolean createRoot){
		nodes=new ArrayList<TreeNode>();
		if(createRoot==true){
			nodes.add(new TreeNode(175,175,20,20,1,this));
		}
	}

	/** 
	 * No argument constructor for XML unmarshalling.  Not explicitly called anywhere else in the code.
	 */
	public DecisionTree(){
	
	}

	public DecisionTree copySubtree(TreeNode origNode){
		DecisionTree subTree=new DecisionTree(false);
		TreeNode copyNode=origNode.copy();
		subTree.nodes.add(copyNode); //Add subtree root
		copyChildren(subTree,origNode,copyNode);
		return(subTree);
	}

	private void copyChildren(DecisionTree subTree, TreeNode origNode, TreeNode copyNode){
		int numChildren=origNode.childIndices.size();
		for(int i=0; i<numChildren; i++){
			int size=subTree.nodes.size();
			int childIndex=origNode.childIndices.get(i);
			TreeNode origChild=nodes.get(childIndex);
			TreeNode copyChild=origChild.copy();
			subTree.nodes.add(copyChild); //Copy child
			copyNode.childIndices.add(size);
			copyChildren(subTree,origChild,copyChild);
		}
	}

	public DecisionTree snapshot(){ //Return copy of this tree
		DecisionTree copy=new DecisionTree(false);
		for(int i=0; i<nodes.size(); i++){
			TreeNode copyNode=nodes.get(i).copy();
			for(int j=0; j<nodes.get(i).childIndices.size(); j++){
				copyNode.childIndices.add(nodes.get(i).childIndices.get(j));
			}
			copy.nodes.add(copyNode);
		}
		return(copy);
	}

	/**
	 * Parse text entries and ensure tree inputs are plausible.
	 * @return ArrayList of error messages
	 */
	public ArrayList<String> parseTree(){
		myModel.validateModelObjects();
		ArrayList<String> errors=new ArrayList<String>();
		//Initialize root
		TreeNode root=nodes.get(0);
		int numDim=myModel.dimInfo.dimSymbols.length;
		root.curCosts=new double[numDim]; 
		root.curPayoffs=new double[numDim]; 
		root.numChildren=root.childIndices.size();
		root.children=new TreeNode[root.numChildren];
		for(int j=0; j<root.numChildren; j++){
			root.children[j]=nodes.get(root.childIndices.get(j));
		}
		
		//Parse tree inputs and variables
		boolean validProbs=true;
		int size=nodes.size();
		for(int i=1; i<size; i++){ //Exclude root node
			TreeNode curNode=nodes.get(i);
			curNode.curCosts=new double[numDim]; 
			curNode.curPayoffs=new double[numDim]; 

			if(curNode.type==0 && myModel.simType==1){
				errors.add("Node "+curNode.name+": Sequential decision nodes are not allowed in Monte Carlo simulations");
			}
			
			if(curNode.parentType!=0){ //Validate probability
				curNode.highlightTextField(0,null); //Prob
				if(curNode.prob.matches("C") || curNode.prob.matches("c")){curNode.curProb=-1;} //Complementary
				else{ //Evaluate text
					try{
						curNode.curProb=Interpreter.evaluate(curNode.prob,myModel,false).getDouble();
					}catch(Exception e){
						validProbs=false;
						curNode.highlightTextField(0, Color.YELLOW); //Prob
						errors.add("Node "+curNode.name+": Probability Error ("+curNode.prob+")");
					}
					if(curNode.curProb<0 || curNode.curProb>1 || Double.isNaN(curNode.curProb)){
						validProbs=false;
						curNode.highlightTextField(0, Color.YELLOW); //Prob
						errors.add("Node "+curNode.name+": Probability Error ("+curNode.prob+")");
					}
				}
			}
			if(curNode.type!=2){ //Not terminal, ensure it has children and validate cost
				if(curNode.childIndices.size()==0){
					errors.add("Node "+curNode.name+": Branches must end in a terminal node");
				}

				curNode.highlightTextField(1,null); //Cost
				for(int c=0; c<numDim; c++){
					try{
						curNode.curCosts[c]=Interpreter.evaluate(curNode.cost[c],myModel,false).getDouble();
						if(Double.isNaN(curNode.curCosts[c])){
							curNode.highlightTextField(1, Color.YELLOW); //Cost
							errors.add("Node "+curNode.name+": Cost Error ("+curNode.cost[c]+")");
						}
					}catch(Exception e){
						curNode.highlightTextField(1, Color.YELLOW); //Cost
						errors.add("Node "+curNode.name+": Cost Error ("+curNode.cost[c]+")");
					}
				}
			}
			else if(curNode.type==2){ //Terminal, validate payoff
				curNode.highlightTextField(2,null); //Payoff
				for(int c=0; c<numDim; c++){
					try{
						curNode.curPayoffs[c]=Interpreter.evaluate(curNode.payoff[c],myModel,false).getDouble();
						if(Double.isNaN(curNode.curPayoffs[c])){
							curNode.highlightTextField(2, Color.YELLOW); //Payoff
							errors.add("Node "+curNode.name+": Payoff Error ("+curNode.payoff[c]+")");
						}
					}catch(Exception e){
						curNode.highlightTextField(2, Color.YELLOW); //Payoff
						errors.add("Node "+curNode.name+": Payoff Error ("+curNode.payoff[c]+")");
					}
				}
			}
			//check variable updates
			curNode.highlightTextField(3,null); //Variable updates
			if(curNode.hasVarUpdates){
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				curNode.curVariableUpdates=new VariableUpdate[numUpdates];
				for(int u=0; u<updates.length; u++){
					try{
						curNode.curVariableUpdates[u]=new VariableUpdate(updates[u],myModel);
						double testVal=curNode.curVariableUpdates[u].testVal.getDouble();
						if(Double.isNaN(testVal)){
							curNode.highlightTextField(3, Color.YELLOW); //Variable updates
							errors.add("Node "+curNode.name+": Variable Update Error ("+updates[u]+")");
						}
					}catch(Exception e){
						curNode.highlightTextField(3, Color.YELLOW); //Variable updates
						errors.add("Node "+curNode.name+": Variable Update Error ("+updates[u]+")");
					}
				}
			}
		}

		//Ensure probabilities sum to 1.0
		if(validProbs){
			for(int i=1; i<size; i++){ //Exclude root node
				TreeNode curNode=nodes.get(i);
				curNode.numChildren=curNode.childIndices.size();
				curNode.children=new TreeNode[curNode.numChildren];
				if(curNode.type==0){ //Decision node
					for(int j=0; j<curNode.numChildren; j++){
						TreeNode child=nodes.get(curNode.childIndices.get(j));
						curNode.children[j]=child;
					}
				}
				else{ //Not decision node
					if(curNode.numChildren>0){
						double sumProb=0;
						int numCompProb=0;
						for(int j=0; j<curNode.numChildren; j++){
							TreeNode child=nodes.get(curNode.childIndices.get(j));
							curNode.children[j]=child;
							if(child.curProb==-1){numCompProb++;}
							else{sumProb+=child.curProb;}
						}
						if(numCompProb==0){
							if(sumProb!=1.0){
								errors.add("Node "+curNode.name+": Probabilities sum to "+sumProb+"!");
								for(int j=0; j<curNode.numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									child.highlightTextField(0, Color.YELLOW); //Prob
								}
							}
						}
						else if(numCompProb==1){
							if(sumProb<0 || sumProb>1){
								errors.add("Node "+curNode.name+": Entered probabilities sum to "+sumProb+"!");
								for(int j=0; j<curNode.numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									if(child.curProb!=-1){child.highlightTextField(0, Color.YELLOW);} //Entered prob
								}
							}
							else{ //Calculate complementary prob
								for(int j=0; j<curNode.numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									if(child.curProb==-1){child.curProb=1.0-sumProb;}
								}
							}
						}
						else{ //2+ comp probs
							errors.add("Node "+curNode.name+": At most 1 probability can be complementary!");
							for(int j=0; j<curNode.numChildren; j++){
								TreeNode child=nodes.get(curNode.childIndices.get(j));
								if(child.curProb==-1){child.highlightTextField(0, Color.YELLOW);} //Comp. prob
							}
						}
					}
				}
			}
		}
		return(errors);
	}

	/**
	 * Recursively rolls back the tree
	 * @param node Current node - passed recursively
	 * @throws Exception 
	 */
	public void runModel(boolean display) throws Exception{
		if(myModel.simType==0){ //Cohort Tree
			TreeCohort cohortModel=new TreeCohort(nodes.get(0)); //send root
			cohortModel.simulate(display);
		}
		else if(myModel.simType==1){ //Monte Carlo
			TreeMonteCarlo microModel=new TreeMonteCarlo(nodes.get(0)); //send root
			microModel.simulate(display);
		}
	}
		
	public int getParentIndex(int childIndex){
		int parentIndex=-1;
		int i=0;
		while(parentIndex==-1){
			if(nodes.get(i).childIndices.contains(childIndex)){parentIndex=i;}
			i++;
		}
		return(parentIndex);
	}

	public void runCEA(Console console){
		DimInfo dimInfo=myModel.dimInfo;
		Object table[][]=null;
		if(dimInfo.analysisType==1){table=new CEAHelper().calculateICERs(myModel);} //CEA
		else if(dimInfo.analysisType==2){table=new CEAHelper().calculateNMB(myModel);} //BCA
		int numStrat=table.length;

		//Round results
		for(int s=0; s<numStrat; s++){
			table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.costDim]); //Cost
			table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.effectDim]); //Effect
			double icer=(double) table[s][4];
			if(Double.isNaN(icer)){
				table[s][4]="---";
			}
			else{
				table[s][4]=MathUtils.round(icer,dimInfo.decimals[dimInfo.costDim]);
			}
		}

		//Display results in console
		if(dimInfo.analysisType==1){ //CEA
			console.print("\nCEA Results:\n");
			console.print("Strategy	"+dimInfo.dimNames[dimInfo.costDim]+"	"+dimInfo.dimNames[dimInfo.effectDim]+"	ICER	Notes\n");
			for(int s=0; s<numStrat; s++){
				console.print(table[s][1]+"	");
				console.print(table[s][2]+"	");
				console.print(table[s][3]+"	");
				console.print(table[s][4]+"	");
				console.print(table[s][5]+"\n");
			}
			console.newLine();
		}
		else if(dimInfo.analysisType==2){ //BCA
			console.print("\nBCA Results:\n");
			console.print("Strategy	"+dimInfo.dimNames[dimInfo.effectDim]+"	"+dimInfo.dimNames[dimInfo.costDim]+"	NMB\n");
			for(int s=0; s<numStrat; s++){
				console.print(table[s][1]+"	");
				console.print(table[s][3]+"	");
				console.print(table[s][2]+"	");
				console.print(table[s][4]+"\n");
			}
			console.newLine();
		}
		
		//Display on tree
		TreeNode root=nodes.get(0);
		for(int s=0; s<numStrat; s++){
			int origStrat=(int) table[s][0];
			if(origStrat!=-1){
				int index=root.childIndices.get(origStrat);
				TreeNode child=nodes.get(index);
				String icer=""+table[s][4];
				if(icer.matches("---")){
					icer=(String)table[s][5];
				}
				if(dimInfo.analysisType==1){icer="ICER: "+icer;}
				else if(dimInfo.analysisType==2){icer="NMB: "+icer;}
				child.icer=icer;
				child.textICER.setText(icer);
				if(child.visible){
					child.textICER.setVisible(true);
				}
			}
		}
	}

	public void displayResults(Console console){
		//Display output for each strategy
		DimInfo dimInfo=myModel.dimInfo;
		TreeNode root=nodes.get(0);
		int numDimensions=root.numDimensions;
		int numChildren=root.numChildren; //Root
		console.print("Strategy");
		for(int d=0; d<numDimensions; d++){
			console.print("\tEV ("+dimInfo.dimSymbols[d]+")");
		}
		console.print("\n");
		for(int i=0; i<numChildren; i++){ //strategy results
			TreeNode child=root.children[i];
			console.print(child.name);
			for(int d=0; d<numDimensions; d++){
				console.print("\t"+MathUtils.round(child.expectedValues[d]*myModel.cohortSize,dimInfo.decimals[d]));
			}
			console.print("\n");
		}
		console.newLine();
	}

	public void appendResults(TreeNode curNode, Console console){
		int numChildren=curNode.childIndices.size();
		for(int i=0; i<numChildren; i++){
			TreeNode child=nodes.get(curNode.childIndices.get(i));
			console.print(child.name+"	");
			for(int d=0; d<child.numDimensions-1; d++){
				console.print(MathUtils.round(child.expectedValues[d],myModel.dimInfo.decimals[d])+"	");
			}
			console.print(MathUtils.round(child.expectedValues[child.numDimensions-1],myModel.dimInfo.decimals[child.numDimensions-1])+"\n");
			appendResults(child,console);
		}
	}

	public void updateDimensions(int numDimensions){
		int numNodes=nodes.size();
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=nodes.get(i);
			curNode.numDimensions=numDimensions;
			curNode.cost=Arrays.copyOf(curNode.cost, numDimensions);
			curNode.payoff=Arrays.copyOf(curNode.payoff, numDimensions);
			if(curNode.expectedValues==null){curNode.expectedValues=new double[numDimensions];}
			else{curNode.expectedValues=Arrays.copyOf(curNode.expectedValues, numDimensions);}
			for(int d=0; d<numDimensions; d++){
				if(curNode.cost[d]==null){curNode.cost[d]="0";}
				if(curNode.payoff[d]==null){curNode.payoff[d]="0";}
			}
		}
		
	}
}