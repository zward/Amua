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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

public class ImportTree{


	public ImportTree(final PanelTree panelTree, final String filepath){
		final ProgressMonitor progress=new ProgressMonitor(panelTree, "Import Tree", "Importing...", 0, 100);

		//Thread SimThread = new Thread(){ //Non-UI
		//public void run(){
		try{
			//Read xml file
			panelTree.clearAll();
			panelTree.tree=new DecisionTree(true);
			panelTree.tree.nodes.get(0).setPanel(panelTree);
			IntNode root=new IntNode();
			IntNode curNodeIndex=root;
			panelTree.repaint();
			//frmMain.setTitle("Amua");
			panelTree.name=null;
			panelTree.filepath=null;
			panelTree.curNode=panelTree.tree.nodes.get(0);

			File file=new File(filepath);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int index=0;
			strLine=br.readLine(); //xml version
			strLine=br.readLine(); //xmi:version
			strLine=br.readLine(); //<tree
			strLine=br.readLine(); //<Node - root
			//Check for global variables
			strLine=br.readLine();
			while(strLine.contains("<Definition")){ //Global variable
				TreeVariable curVar=new TreeVariable();
				index=strLine.indexOf("Variable");
				strLine=strLine.substring(index+10); //Move to field entry
				index=strLine.indexOf("\"");
				String name=strLine.substring(0, index);
				curVar.name=name;
				strLine=strLine.substring(index); //Trim
				index=strLine.indexOf("Value");
				strLine=strLine.substring(index+7); //Move to field entry
				index=strLine.indexOf("\"");
				String expr=strLine.substring(0, index);
				curVar.expression=expr;
				boolean add=true;
				if(curVar.expression.contains("def")){ //See if user-defined
					if(expr.substring(0,3).equals("def")){ //User-defined, ignore variable
						add=false;
					}
				}
				if(add){
					panelTree.tree.variables.add(curVar);
				}
				strLine=br.readLine();
			}
			panelTree.varHelper=new VarHelper(panelTree.tree.variables,panelTree.tree.dimSymbols,panelTree.errorLog);
			panelTree.paneFormula.varHelper=panelTree.varHelper;
			
			//Read to EOF...
			while(strLine!=null){
				if(strLine.contains("<Node")){
					//Name
					index=strLine.indexOf("NameID");
					strLine=strLine.substring(index+8); //Move to field entry
					index=strLine.indexOf("\"");
					String name=strLine.substring(0, index);
					strLine=strLine.substring(index); //Trim
					//Label
					index=strLine.indexOf("Label");
					strLine=strLine.substring(index+7); //Move to field entry
					index=strLine.indexOf("\"");
					String label=strLine.substring(0, index);
					label=label.replaceAll("&quot;", "\""); //Quotation marks
					strLine=strLine.substring(index); //Trim
					//NodeType
					index=strLine.indexOf("NodeType");
					strLine=strLine.substring(index+10); //Move to field entry
					index=strLine.indexOf("\"");
					String nodeType=strLine.substring(0, index);
					int type=-1;
					if(nodeType.contains("Decision")){type=0;}
					else if(nodeType.contains("Chance")){type=1;}
					else if(nodeType.contains("Terminal")){type=2;}

					//Add node
					curNodeIndex=curNodeIndex.addChild(panelTree.tree.nodes.size());
					panelTree.addNode(type);
					panelTree.curNode.name=label;
					panelTree.curNode.textName.setText(label);
				}
				else if(strLine.contains("<Prob")){
					String prob="0";
					index=strLine.indexOf("Value=");
					strLine=strLine.substring(index+7); //Move to field entry
					index=strLine.indexOf("\"");
					prob=strLine.substring(0,index);
					if(prob.equals("#")){prob="C";} //Complementary prob
					if(panelTree.curNode.textProb!=null){
						panelTree.curNode.prob=prob;
						panelTree.curNode.textProb.setText(prob+"");
					}
				}
				else if(strLine.contains("<Definition")){ //Cost or Local Variable
					index=strLine.indexOf("Variable");
					strLine=strLine.substring(index+10); //Move to field entry
					index=strLine.indexOf("\"");
					String name=strLine.substring(0, index);
					if(name.equals("Cost")){ //Add cost
						String cost="0";
						index=strLine.indexOf("Value=");
						strLine=strLine.substring(index+7); //Move to field entry
						index=strLine.indexOf("\"");
						cost=strLine.substring(0,index);
						cost=cost.replaceAll(" ", ""); //Remove spaces
						cost=cost.replaceAll("Cost\\+", "");
						panelTree.curNode.cost[0]=cost;
						panelTree.curNode.textCost.setText("$"+cost);
						panelTree.curNode.hasCost=true;
					}
					else{ //Local variable
						TreeVariable curVar=new TreeVariable();
						String varName=name;
						//Ensure unique name
						if(panelTree.varHelper.isVariable(varName)){
							int suffix=1;
							while(panelTree.varHelper.isVariable(varName)){
								suffix++;
								varName=name+suffix;
							}
						}
						curVar.name=varName;
						strLine=strLine.substring(index); //Trim
						index=strLine.indexOf("Value");
						strLine=strLine.substring(index+7); //Move to field entry
						index=strLine.indexOf("\"");
						String expr=strLine.substring(0, index);
						curVar.expression=expr;
						boolean add=true;
						if(curVar.expression.contains("def")){ //See if user-defined
							if(expr.substring(0,3).equals("def")){ //User-defined, ignore variable
								add=false;
							}
						}
						if(add){
							panelTree.tree.variables.add(curVar);
							//Check probs, costs, payoffs for orig variable name
							if(panelTree.curNode.textProb!=null){
								String newProb=panelTree.curNode.prob.replaceAll(name, varName);
								panelTree.curNode.prob=newProb;
								panelTree.curNode.textProb.setText(newProb+"");
							}
						}
					}
				}
				else if(strLine.contains("<Payoff")){
					String payoff="0";
					index=strLine.indexOf("Value=");
					strLine=strLine.substring(index+7); //Move to field entry
					index=strLine.indexOf("\"");
					payoff=strLine.substring(0,index);
					payoff=payoff.replaceAll(" ", ""); //Remove spaces
					payoff=payoff.replaceAll("Cost\\+", "");
					if(panelTree.curNode.type==2){
						panelTree.curNode.payoff[0]=payoff;
						panelTree.curNode.textPayoff.setText("$"+payoff);
					}
				}
				else if(strLine.contains("</Node>")){
					//Move to parent
					if(curNodeIndex.parent!=null){
						curNodeIndex=curNodeIndex.parent;
						panelTree.curNode=panelTree.tree.nodes.get(curNodeIndex.index);
						panelTree.selectNode(panelTree.curNode.xPos, panelTree.curNode.yPos);
					}
				}

				strLine=br.readLine();
			}

			br.close();

			for(int v=0; v<panelTree.tree.variables.size(); v++){ //Try to evaluate variables
				TreeVariable curVar=panelTree.tree.variables.get(v);
				double value=0;
				try{
					value=panelTree.varHelper.evaluateExpression(curVar.expression);
				}
				catch(Exception e){
					//Do nothing
				}
				curVar.value=value;
			}
			panelTree.refreshVarTable();

			panelTree.treeStackUndo=new Stack<DecisionTree>();
			panelTree.treeStackRedo=new Stack<DecisionTree>();
			panelTree.actionStackUndo=new Stack<String>();
			panelTree.actionStackRedo=new Stack<String>();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(panelTree, e.getMessage());
			panelTree.errorLog.recordError(e);
		}
		//}
		//};
		//SimThread.start();
	}

	//Define inner class
	class IntNode{
		int index; //Index of node in main arraylist	int type; //0=Decision, 1=Chance, 2=Terminal
		IntNode parent;
		ArrayList<IntNode> children;

		//Constructor
		public IntNode(){
			children=new ArrayList<IntNode>();
		}

		public IntNode(int index, IntNode parent){
			this.index=index;
			this.parent=parent;
			children=new ArrayList<IntNode>();
		}

		public IntNode addChild(int index){
			IntNode child=new IntNode(index,this);
			children.add(child);
			return(child);
		}
	}
}