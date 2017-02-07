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

package main;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DecisionTree")
/**
 * 
 * @author zward
 *
 */
public class DecisionTree{
	@XmlElement(name="Metadata", type=Metadata.class) public Metadata meta;
	@XmlElement public String dimNames[], dimSymbols[];
	@XmlElement public int decimals=6;
	@XmlElement public int scale=100;
	@XmlElement public boolean alignRight=false;
	@XmlElement(name="Variable", type=TreeVariable.class) public ArrayList<TreeVariable> variables;
	@XmlElement(name = "Node", type = TreeNode.class)public ArrayList<TreeNode> nodes; //Save ArrayList of nodes for direct access to children
	boolean showEV=false;

	//Constructor
	/**
	 * Default constructor
	 * @param createRoot  If true adds a decision node as the root.  If false, leaves the nodes empty
	 */
	public DecisionTree(boolean createRoot){
		meta=new Metadata();
		meta.update();
		dimNames=new String[]{"Cost"};
		dimSymbols=new String[]{"$"};
		variables=new ArrayList<TreeVariable>();
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
		copy.meta=meta;
		int numDim=dimNames.length;
		copy.dimNames=Arrays.copyOf(dimNames,numDim);
		copy.dimSymbols=Arrays.copyOf(dimSymbols,numDim);
		copy.decimals=decimals;
		copy.alignRight=alignRight;
		copy.scale=scale;
		for(int i=0; i<nodes.size(); i++){
			TreeNode copyNode=nodes.get(i).copy();
			for(int j=0; j<nodes.get(i).childIndices.size(); j++){
				copyNode.childIndices.add(nodes.get(i).childIndices.get(j));
			}
			copy.nodes.add(copyNode);
		}
		for(int i=0; i<variables.size(); i++){
			TreeVariable copyVar=variables.get(i).copy();
			copy.variables.add(copyVar);
		}
		return(copy);
	}

	/**
	 * Parse text entries and ensure tree inputs are plausible.
	 * @return ArrayList of error messages
	 */
	public ArrayList<String> parseTree(){
		evalAllVars();
		ArrayList<String> errors=new ArrayList<String>();
		//Initialize root
		TreeNode root=nodes.get(0);
		root.curCosts=new double[dimNames.length];
		root.curPayoffs=new double[dimNames.length];
		//Parse tree inputs and variables
		boolean validProbs=true;
		int size=nodes.size();
		for(int i=1; i<size; i++){ //Exclude root node
			TreeNode curNode=nodes.get(i);
			curNode.curCosts=new double[dimNames.length];
			curNode.curPayoffs=new double[dimNames.length];
			
			if(curNode.parentType!=0){ //Validate probability
				curNode.highlightTextField(0,null); //Prob
				if(curNode.prob.matches("C") || curNode.prob.matches("c")){curNode.curProb=-1;} //Complementary
				else{ //Evaluate text
					try{
						curNode.curProb=curNode.evaluateExpression(curNode.prob);
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
			if(curNode.type!=2){ //Not terminal, validate cost
				curNode.highlightTextField(1,null); //Cost
				for(int c=0; c<dimNames.length; c++){
					try{
						curNode.curCosts[c]=curNode.evaluateExpression(curNode.cost[c]);
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
				for(int c=0; c<dimNames.length; c++){
					try{
						curNode.curPayoffs[c]=curNode.evaluateExpression(curNode.payoff[c]);
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
		}

		//Ensure probabilities sum to 1.0
		if(validProbs){
			for(int i=1; i<size; i++){ //Exclude root node
				TreeNode curNode=nodes.get(i);
				if(curNode.type!=0){ //Not decision node
					int numChildren=curNode.childIndices.size();
					if(numChildren>0){
						double sumProb=0;
						int numCompProb=0;
						for(int j=0; j<numChildren; j++){
							TreeNode child=nodes.get(curNode.childIndices.get(j));
							if(child.curProb==-1){numCompProb++;}
							else{sumProb+=child.curProb;}
						}
						if(numCompProb==0){
							if(sumProb!=1.0){
								errors.add("Node "+curNode.name+": Probabilities sum to "+sumProb+"!");
								for(int j=0; j<numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									child.highlightTextField(0, Color.YELLOW); //Prob
								}
							}
						}
						else if(numCompProb==1){
							if(sumProb<0 || sumProb>1){
								errors.add("Node "+curNode.name+": Entered probabilities sum to "+sumProb+"!");
								for(int j=0; j<numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									if(child.curProb!=-1){child.highlightTextField(0, Color.YELLOW);} //Entered prob
								}
							}
							else{ //Calculate complementary prob
								for(int j=0; j<numChildren; j++){
									TreeNode child=nodes.get(curNode.childIndices.get(j));
									if(child.curProb==-1){child.curProb=1.0-sumProb;}
								}
							}
						}
						else{ //2+ comp probs
							errors.add("Node "+curNode.name+": At most 1 probability can be complementary!");
							for(int j=0; j<numChildren; j++){
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
	 */
	public void runTree(TreeNode node, boolean display){
		//Get expected value of node
		node.expectedValues=new double[node.numDimensions];
		int numChildren=node.childIndices.size();
		if(numChildren==0){
			for(int i=0; i<node.numDimensions; i++){
				node.expectedValues[i]=node.curPayoffs[i]+node.curCosts[i];
			}
		}
		else{
			for(int i=0; i<node.numDimensions; i++){
				node.expectedValues[i]=node.curCosts[i];
			}
			for(int c=0; c<numChildren; c++){
				TreeNode child=nodes.get(node.childIndices.get(c));
				runTree(child,display);
				for(int i=0; i<node.numDimensions; i++){
					node.expectedValues[i]+=(child.curProb*child.expectedValues[i]);
				}
			}
		}
		if(display==true){//Display
			if(node.type==1){
				String buildString="";
				for(int i=0; i<node.numDimensions-1; i++){
					buildString+="("+dimSymbols[i]+") "+round(node.expectedValues[i])+"; ";
				}
				buildString+="("+dimSymbols[node.numDimensions-1]+") "+round(node.expectedValues[node.numDimensions-1]);
				node.textEV.setText(buildString);
				if(node.visible){
					node.textEV.setVisible(true);
				}
			}
		}
	}

	public void evalAllVars(){ //Evaluate all variables
		int numVars=variables.size();
		VarHelper varHelper=nodes.get(0).panel.varHelper;
		for(int v=0; v<numVars; v++){
			variables.get(v).evaluate(varHelper);
		}
	}
	
	public double round(double num){
		double scale=Math.pow(10, decimals);
		num=Math.round(num*scale)/scale;
		return(num);
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
	
	public void displayResults(JTextArea console){
		//Display output for each strategy
		TreeNode root=nodes.get(0);
		int numDimensions=dimSymbols.length;
		int numChildren=root.childIndices.size(); //Root
		for(int i=0; i<numChildren; i++){
			TreeNode child=nodes.get(root.childIndices.get(i));
			console.append("Strategy: "+child.name+"\n");
			for(int d=0; d<numDimensions-1; d++){
				console.append("EV ("+dimSymbols[d]+"): "+round(child.expectedValues[d])+"	");
			}
			console.append("EV ("+dimSymbols[numDimensions-1]+"): "+round(child.expectedValues[numDimensions-1])+"\n");
			console.append("Children...\n");
			console.append("Name	");
			for(int d=0; d<numDimensions-1; d++){console.append("EV ("+dimSymbols[d]+")	");}
			console.append("EV ("+dimSymbols[numDimensions-1]+")\n");
			appendResults(child,console);
			console.append("\n");
		}
	}
	
	public void appendResults(TreeNode curNode, JTextArea console){
		int numChildren=curNode.childIndices.size();
		for(int i=0; i<numChildren; i++){
			TreeNode child=nodes.get(curNode.childIndices.get(i));
			console.append(child.name+"	");
			for(int d=0; d<child.numDimensions-1; d++){
				console.append(round(child.expectedValues[d])+"	");
			}
			console.append(round(child.expectedValues[child.numDimensions-1])+"\n");
			appendResults(child,console);
		}
	}

}