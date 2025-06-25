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
//Utility class
package main;

import javax.swing.JOptionPane;

import base.AmuaModel;
import markov.MarkovNode;
import markov.MarkovTree;
import tree.DecisionTree;
import tree.TreeNode;

public class Clipboard{

	public boolean cut; //if 'cut' is true, clear clipboard after paste
	
	//Elements that can be copied
	public AmuaModel copyModel;
	public DecisionTree copyTree;
	public MarkovTree copyMarkov;
	
	public Clipboard(){
		
	}
	
	
	public void clear(){
		copyModel=null;
		copyTree=null;
		copyMarkov=null;
	}
	
	/**
	 * Check if can paste subtree on target node
	 * @param target
	 * @return
	 */
	public boolean isCompatible(AmuaModel target){
		boolean compatible=true;
		if(target.type==0) { //target=Decision tree
			if(copyModel.type==0) { //source=Decision tree
				compatible=true;
			}
			else if(copyModel.type==1) { //source=Markov model
				compatible=false;
			}
		}
		else if(target.type==1) { //target=Markov
			if(copyModel.type==0) { //source=Decision tree
				TreeNode sourceNode=copyTree.nodes.get(0); //source subroot
				MarkovNode targetNode=target.panelMarkov.curNode;
				if(targetNode.type==1) { //target = Chain
					compatible=false;
				}
				else if(targetNode.chain!=null) { //target already in a chain
					int numNodes=copyTree.nodes.size();
					for(int i=0; i<numNodes; i++) {
						if(copyTree.nodes.get(i).type==0) { //Decision node in source subtree
							compatible=false;
							i=numNodes; //end loop
						}
					}
				}
				if(compatible==true) {
					convertNodes_TreeToMarkov(target);
				}
			}
			else if(copyModel.type==1){ //source = Markov model
				MarkovNode sourceNode=copyMarkov.nodes.get(0); //source subroot
				MarkovNode targetNode=target.panelMarkov.curNode;
				if(sourceNode.type==0) { //source = Decision node
					if(targetNode.chain!=null) { //target already in a chain
						compatible=false;
					}
				}
				else if(sourceNode.type==1) { //source = Markov Chain
					if(targetNode.chain!=null) { //target already in a chain
						compatible=false;
					}
				}
				else if(sourceNode.type==2) { //source = Markov State
					if(targetNode.type!=1) { //target !=chain
						compatible=false;
					}
				}
				else if(sourceNode.type==3) { //source = Chance node
					if(sourceNode.chain==null && targetNode.chain!=null) { //source = before chain, target = in chain
						compatible=false;
					}
					else if(sourceNode.chain!=null && targetNode.chain==null) { //source = in chain, target = before chain
						compatible=false;
					}
				}
				else if(sourceNode.type==4) { //source = Transition node
					if(targetNode.chain==null) { //target = before chain
						compatible=false;
					}
				}
				
				if(targetNode.type==1 && sourceNode.type!=2){ //target=chain, source!=state
					compatible=false;
				}
			}
		}
		
		return(compatible);
	}
	
	public boolean compareDimensions(AmuaModel target) {
		boolean comparable=true;
		if(target.dimInfo.dimSymbols.length!=copyModel.dimInfo.dimSymbols.length){
			comparable=false;
		}
		else{ //Compare dimension symbols
			int numDim=target.dimInfo.dimSymbols.length;
			for(int d=0; d<numDim; d++){
				if(!target.dimInfo.dimSymbols[d].equals(copyModel.dimInfo.dimSymbols[d])){
					comparable=false;
				}
			}
		}
		return(comparable);
	}
	
	public String coerceDimensions(AmuaModel target) {
		int numDim_Target=target.dimInfo.dimSymbols.length;
		int numDim_Source=copyModel.dimInfo.dimSymbols.length;
		
		int dimIndices[]=new int[numDim_Target];
		for(int i=0; i<numDim_Target; i++) {
			String targetSymbol=target.dimInfo.dimSymbols[i];
			int index=-1;
			for(int j=0; j<numDim_Source; j++) {
				if(targetSymbol.equals(copyModel.dimInfo.dimSymbols[j])) {
					index=j; //matching dim symbol found
				}
			}
			dimIndices[i]=index;
		}
		
		//update dimensions
		if(copyModel.type==0) { //Decision tree
			copyTree.updateDimensions(dimIndices);
		}
		else if(copyModel.type==1) { //Markov model
			copyMarkov.updateDimensions(dimIndices);
		}
		
		String warn="";
		if(numDim_Target>numDim_Source) {warn="Padded model dimensions";}
		else if(numDim_Target<numDim_Source) {warn="Truncated model dimensions";}
		else {warn="Re-ordered model dimensions";}
		return(warn);
	}
	
	private void convertNodes_TreeToMarkov(AmuaModel target) {
		MarkovNode targetNode=target.panelMarkov.curNode;
		int numNodes=copyTree.nodes.size();
		copyMarkov=new MarkovTree(false);

		//add root
		TreeNode origRoot=copyTree.nodes.get(0);
		MarkovNode newRoot=null;
		if(origRoot.type==0) { //decision node
			newRoot=new MarkovNode(0, targetNode); //new Decision node
		}
		else if(origRoot.type==1) { //chance node
			newRoot=new MarkovNode(3, targetNode); //new Chance node
		}
		else if(origRoot.type==2) { //terminal node
			if(targetNode.chain==null) { //convert to Chance node
				newRoot=new MarkovNode(3, targetNode);
			}
			else { //convert to Transition node
				newRoot=new MarkovNode(4, targetNode);
			}
		}
		newRoot.convertFromTree(origRoot);
		copyMarkov.nodes.add(newRoot);
		
		boolean inChain=false;
		if(targetNode.chain!=null) {inChain=true;}
		convertChildren_TreeToMarkov(origRoot, newRoot, inChain);

	}
	
	private void convertChildren_TreeToMarkov(TreeNode parentTree, MarkovNode parentMarkov, boolean inChain) {
		int numChildren=parentTree.childIndices.size();
		for(int i=0; i<numChildren; i++){
			int size=copyMarkov.nodes.size();
			int childIndex=parentTree.childIndices.get(i);
			TreeNode origChild=copyTree.nodes.get(childIndex);
			MarkovNode newChild=null;
			if(origChild.type==0) { //decision node
				newChild=new MarkovNode(0, parentMarkov); //new Decision node
			}
			else if(origChild.type==1) { //chance node
				newChild=new MarkovNode(3, parentMarkov); //new Chance node
			}
			else if(origChild.type==2) { //terminal node
				if(inChain==false) { //convert to Chance node
					newChild=new MarkovNode(3, parentMarkov);
				}
				else { //convert to Transition node
					newChild=new MarkovNode(4, parentMarkov);
				}
			}
			newChild.convertFromTree(origChild);
			copyMarkov.nodes.add(newChild);
			
			convertChildren_TreeToMarkov(origChild, newChild, inChain);
		}
	}


}