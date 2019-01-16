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

package base;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gui.frmMain;
import gui.frmTraceSummary;
import main.Console;
import main.Constraint;
import main.DimInfo;
import main.ErrorLog;
import main.MersenneTwisterFast;
import main.Metadata;
import main.Parameter;
import main.ParameterSet;
import main.Table;
import main.Variable;
import markov.MarkovNode;
import markov.MarkovTrace;
import markov.MarkovTraceSummary;
import markov.MarkovTree;
import markov.PanelMarkov;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import tree.DecisionTree;
import tree.PanelTree;
import tree.TreeNode;

@XmlRootElement(name="Model")
public class AmuaModel{
	//Data
	@XmlElement public String name; //Model name
	@XmlElement	public int type; //0=Decision tree; 1=Markov
	@XmlElement(name="Metadata", type=Metadata.class) public Metadata meta;
	@XmlElement(name="DimInfo", type=DimInfo.class) public DimInfo dimInfo;
	@XmlElement public int scale=100;
	@XmlElement public boolean alignRight=false;
	@XmlElement(name="Parameter", type=Parameter.class) public ArrayList<Parameter> parameters;
	@XmlElement(name="Variable", type=Variable.class) public ArrayList<Variable> variables;
	@XmlElement(name="Table", type=Table.class) public ArrayList<Table> tables;
	@XmlElement(name="Constraint",type=Constraint.class) public ArrayList<Constraint> constraints;
	@XmlElement public String parameterNames[];
	@XmlElement public boolean simParamSets;
	@XmlElement(name="ParameterSet", type=ParameterSet.class) public ParameterSet[] parameterSets;
	//Simulation settings
	@XmlElement public int simType=0; //0=Cohort, 1=Monte Carlo
	@XmlElement public int cohortSize=1000;
	@XmlElement public boolean CRN; //common random numbers
	@XmlElement public int crnSeed; //CRN seed
	//Model types
	@XmlElement public DecisionTree tree;
	@XmlElement public MarkovTree markov;

	//Ethereal
	@XmlTransient public String filepath=null;
	@XmlTransient public ErrorLog errorLog;
	@XmlTransient public Stack<String> actionStackUndo, actionStackRedo;
	@XmlTransient public Stack<ModelSnapshot> modelStackUndo, modelStackRedo;
	@XmlTransient public boolean unsavedChanges;
	@XmlTransient public int strategyIndices[];
	@XmlTransient public String strategyNames[];
	//sampling
	@XmlTransient public boolean sampleParam, sampleVar;
	@XmlTransient public MersenneTwisterFast generatorParam, generatorVar, curGenerator;
	//innate vars
	@XmlTransient public ArrayList<Variable> innateVariables;
	@XmlTransient public MarkovTrace traceMarkov;
		
	//Display
	@XmlTransient public frmMain mainForm;
	@XmlTransient public PanelTree panelTree;
	@XmlTransient public PanelMarkov panelMarkov;


	//Constructor
	public AmuaModel(int modelType,frmMain mainFrm, ErrorLog errorLog){
		this.mainForm=mainFrm;
		this.errorLog=errorLog;
		meta=new Metadata(mainFrm.version);
		meta.update();
		dimInfo=new DimInfo();
		parameters=new ArrayList<Parameter>();
		variables=new ArrayList<Variable>();
		innateVariables=new ArrayList<Variable>();
		tables=new ArrayList<Table>();
		constraints=new ArrayList<Constraint>();
		
		actionStackUndo=new Stack<String>();
		actionStackRedo=new Stack<String>();
		modelStackUndo=new Stack<ModelSnapshot>();
		modelStackRedo=new Stack<ModelSnapshot>();

		type=modelType;
		if(modelType==0){ //Decision tree
			cohortSize=1;
			panelTree=new PanelTree(mainFrm,this,errorLog);
			tree=panelTree.tree;
		}
		else if(modelType==1){ //Markov
			cohortSize=1000;
			addT();
			panelMarkov=new PanelMarkov(mainFrm,this,errorLog);
			markov=panelMarkov.tree;
		}
	}

	/** 
	 * No argument constructor for XML unmarshalling.
	 */
	public AmuaModel(){

	}
	
	public JPanel getPanel(){
		if(type==0){return(panelTree);}
		else if(type==1){return(panelMarkov);}
		return(null);
	}



	public void openModel(frmMain mainFrm, ErrorLog errorLog){
		try{
			this.mainForm=mainFrm;
			this.errorLog=errorLog;
			
			innateVariables=new ArrayList<Variable>();
			
			//Update tables
			if(parameters==null){parameters=new ArrayList<Parameter>();}
			if(variables==null){variables=new ArrayList<Variable>();}
			if(tables==null){tables=new ArrayList<Table>();}
			if(constraints==null){constraints=new ArrayList<Constraint>();}
			//Construct splines if needed
			for(int t=0; t<tables.size(); t++){
				Table curTable=tables.get(t);
				if(curTable.interpolate!=null && curTable.interpolate.matches("Cubic Splines")){
					curTable.constructSplines();
				}
			}
			
			refreshParamTable();
			refreshVarTable();
			refreshTableTable();
			refreshConstTable();
			//Parse parameters
			if(parameterNames!=null){
				int numSets=parameterSets.length;
				for(int i=0; i<numSets; i++){
					parameterSets[i].parseValues();
				}
			}
			refreshParamSetsTable();
			refreshAlignment();
			//Update undo stacks
			modelStackUndo=new Stack<ModelSnapshot>();
			modelStackRedo=new Stack<ModelSnapshot>();
			actionStackUndo=new Stack<String>();
			actionStackRedo=new Stack<String>();
			mainForm.mntmUndo.setEnabled(false);
			mainForm.mntmUndo.setText("Undo");
			mainForm.mntmRedo.setEnabled(false);
			mainForm.mntmRedo.setText("Redo");
			
			if(type==0){ //Decision tree
				panelTree=new PanelTree(mainFrm,this,errorLog);
				panelTree.openTree(tree);
			}
			else if(type==1){ //Markov
				addT();
				panelMarkov=new PanelMarkov(mainFrm,this,errorLog);
				panelMarkov.openTree(markov);
			}
		
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	public void refreshParamTable(){
		mainForm.modelParameters.setRowCount(0);
		int numParams=parameters.size();
		for(int i=0; i<numParams; i++){
			Parameter curParam=parameters.get(i);
			mainForm.modelParameters.addRow(new Object[]{curParam.name,curParam.expression});
		}
	}
	
	public void refreshVarTable(){
		mainForm.modelVariables.setRowCount(0);
		int numVars=variables.size();
		for(int i=0; i<numVars; i++){
			Variable curVar=variables.get(i);
			mainForm.modelVariables.addRow(new Object[]{curVar.name,curVar.expression});
		}
	}

	public void refreshTableTable(){
		mainForm.modelTables.setRowCount(0);
		int numTables=tables.size();
		for(int i=0; i<numTables; i++){
			Table table=tables.get(i);
			mainForm.modelTables.addRow(new Object[]{table.name,table.type,table.numRows+" x "+table.numCols});
		}
	}
	
	public void refreshConstTable(){
		mainForm.modelConstraints.setRowCount(0);
		int numConst=constraints.size();
		for(int i=0; i<numConst; i++){
			Constraint curConst=constraints.get(i);
			mainForm.modelConstraints.addRow(new Object[]{curConst.name,curConst.expression});
		}
	}
	
	public void refreshParamSetsTable(){
		if(parameterNames!=null){
			mainForm.chckbxUseParamSets.setEnabled(true);
			mainForm.chckbxUseParamSets.setSelected(simParamSets);
			buildParamSetsTable(mainForm.modelParamSets);
		}
		else{
			mainForm.modelParamSets.setRowCount(0);
			mainForm.modelParamSets.setColumnCount(0);
			mainForm.chckbxUseParamSets.setSelected(false);
			mainForm.chckbxUseParamSets.setEnabled(false);
		}
	}
	
	public void refreshAlignment(){
		//node alignment buttons
		if(alignRight==false){
			URL imageURL = frmMain.class.getResource("/images/alignLeftSelected.png");
			mainForm.btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
			imageURL = frmMain.class.getResource("/images/alignRight.png");
			mainForm.btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
		}
		else{
			URL imageURL = frmMain.class.getResource("/images/alignRightSelected.png");
			mainForm.btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
			imageURL = frmMain.class.getResource("/images/alignLeft.png");
			mainForm.btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
		}
	}
	
	
	public void saveModel(){
		try{
			meta.update();

			JAXBContext context = JAXBContext.newInstance(AmuaModel.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Write to File
			FileWriter fstreamO=new FileWriter(filepath);
			BufferedWriter out=new BufferedWriter(fstreamO);
			m.marshal(this,out);
			out.close();

			unsavedChanges=false;
			
			mainForm.recentFiles.updateList(filepath,type);

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	public void undoAction(){
		unsavedChanges=true;
		setUnsavedStatus();
		
		if(type==0){
			panelTree.setVisible(false); //Don't paint anything until updated
			panelTree.clearAll();
			panelTree.textAreaNotes.setText("");
		}
		else if(type==1){
			panelMarkov.setVisible(false); //Don't paint anything until updated
			panelMarkov.clearAll();
			panelMarkov.textAreaNotes.setText("");
		}
		
		//Add current state to redo stack
		String lastAction=actionStackUndo.pop();
		modelStackRedo.push(new ModelSnapshot(this));
		actionStackRedo.push(lastAction);
		mainForm.mntmRedo.setEnabled(true);
		mainForm.mntmRedo.setText("Redo "+lastAction);

		modelStackUndo.pop().getSnapshot(this); //gets previous model
		
		//Update nodes on panel
		if(type==0){panelTree.revert();}
		else if(type==1){panelMarkov.revert();}
		
		refreshParamTable();
		refreshVarTable();
		refreshTableTable();
		refreshConstTable();
		refreshAlignment();
		refreshParamSetsTable();
		
		if(lastAction.contains("Parameter") || lastAction.contains("Variable") || lastAction.contains("Table") || lastAction.contains("Constraint")){rescale(scale);} //Re-validates textfields

		if(modelStackUndo.size()==0){
			mainForm.mntmUndo.setEnabled(false);
			mainForm.mntmUndo.setText("Undo");
		}
		else{
			mainForm.mntmUndo.setText("Undo "+actionStackUndo.peek());
		}
	}

	public void redoAction(){
		unsavedChanges=true;
		setUnsavedStatus();
		
		if(type==0){
			panelTree.setVisible(false); //Don't paint anything until updated
			panelTree.clearAll();
			panelTree.textAreaNotes.setText("");
		}
		else if(type==1){
			panelMarkov.setVisible(false); //Don't paint anything until updated
			panelMarkov.clearAll();
			panelMarkov.textAreaNotes.setText("");
		}
		
		//Add current state to undo stack
		String lastAction=actionStackRedo.pop();
		modelStackUndo.push(new ModelSnapshot(this));
		actionStackUndo.push(lastAction);
		mainForm.mntmUndo.setEnabled(true);
		mainForm.mntmUndo.setText("Undo "+lastAction);

		modelStackRedo.pop().getSnapshot(this); //gets previous model
		
		//Update nodes on panel
		if(type==0){panelTree.revert();}
		else if(type==1){panelMarkov.revert();}
		
		refreshParamTable();
		refreshVarTable();
		refreshTableTable();	
		refreshConstTable();
		refreshAlignment();
		refreshParamSetsTable();
		
		if(lastAction.contains("Parameter") || lastAction.contains("Variable") || lastAction.contains("Table") || lastAction.contains("Constraint")){rescale(scale);} //Re-validates textfields
		
		if(modelStackRedo.size()==0){
			mainForm.mntmRedo.setEnabled(false);
			mainForm.mntmRedo.setText("Redo");
		}
		else{
			mainForm.mntmRedo.setText("Redo "+actionStackRedo.peek());
		}
	}

	public void cutSubtree(){
		if(type==0){panelTree.cutSubtree();}
		else if(type==1){panelMarkov.cutSubtree();}
	}

	public void copySubtree(){
		if(type==0){panelTree.copySubtree();}
		else if(type==1){panelMarkov.copySubtree();}
	}

	public void pasteSubtree(){
		if(type==0){panelTree.pasteSubtree();}
		else if(type==1){panelMarkov.pasteSubtree();}
	}

	public void deleteParameter(int paramNum){
		saveSnapshot("Delete Parameter");//Add to undo stack
		parameters.remove(paramNum);
		mainForm.modelParameters.removeRow(paramNum);
		validateModelObjects();
	}
	
	public void deleteVariable(int varNum){
		saveSnapshot("Delete Variable");//Add to undo stack
		variables.remove(varNum);
		mainForm.modelVariables.removeRow(varNum);
		validateModelObjects();
	}

	public void deleteTable(int tableNum){
		saveSnapshot("Delete Table");//Add to undo stack
		tables.remove(tableNum);
		mainForm.modelTables.removeRow(tableNum);
		validateModelObjects();
	}
	
	public void deleteConstraint(int constNum){
		saveSnapshot("Delete Constraint"); //Add to undo stack
		constraints.remove(constNum);
		mainForm.modelConstraints.removeRow(constNum);
		validateModelObjects();
	}
	
	public void addParameter(Parameter param){
		mainForm.modelParameters.addRow(new Object[]{param.name,param.expression});
		validateModelObjects();
	}
	
	public void addVariable(Variable variable){
		mainForm.modelVariables.addRow(new Object[]{variable.name,variable.expression});
		validateModelObjects();
	}

	public void addTable(Table table){
		mainForm.modelTables.addRow(new Object[]{table.name,table.type,table.numRows+" x "+table.numCols});
		validateModelObjects();
	}
	
	public void addConstraint(Constraint curConst){
		mainForm.modelConstraints.addRow(new Object[]{curConst.name,curConst.expression});
		validateModelObjects();
	}
	
	public void editTable(int tableNum){
		Table table=tables.get(tableNum);
		mainForm.modelTables.setValueAt(table.name, tableNum, 0);
		mainForm.modelTables.setValueAt(table.type, tableNum, 1);
		mainForm.modelTables.setValueAt(table.numRows+" x "+table.numCols, tableNum, 2);
		validateModelObjects();
	}

	public void editParameter(int paramNum){
		Parameter param=parameters.get(paramNum);
		mainForm.modelParameters.setValueAt(param.name, paramNum, 0);
		mainForm.modelParameters.setValueAt(param.expression, paramNum, 1);
		validateModelObjects();
	}
	
	public void editVariable(int varNum){
		Variable variable=variables.get(varNum);
		mainForm.modelVariables.setValueAt(variable.name, varNum, 0);
		mainForm.modelVariables.setValueAt(variable.expression, varNum, 1);
		validateModelObjects();
	}
	
	public void editConstraint(int constNum){
		Constraint constraint=constraints.get(constNum);
		mainForm.modelConstraints.setValueAt(constraint.name,constNum,0);
		mainForm.modelConstraints.setValueAt(constraint.expression,constNum,1);
	}
	
	/**
	 * Evaluates all parameters, variables, and constraints
	 */
	public void validateModelObjects(){
		//parameters
		int numParams=parameters.size();
		for(int i=0; i<numParams; i++){
			Parameter curParam=parameters.get(i);
			curParam.valid=true;
			if(curParam.locked==false){
				try{
					curParam.value=Interpreter.evaluate(curParam.expression, this,false);
				}catch(Exception e){
					curParam.valid=false;
					curParam.value=null;
				}
			}
		}
		
		//variables
		int numVars=variables.size();
		for(int i=0; i<numVars; i++){
			Variable curVar=variables.get(i);
			curVar.valid=true;
			try{
				curVar.value=Interpreter.evaluate(curVar.expression, this,false);
				curVar.dependents=new ArrayList<Variable>();
			}catch(Exception e){
				curVar.valid=false;
				curVar.value=null;
			}
		}
		for(int i=0; i<numVars; i++){
			Variable curVar=variables.get(i);
			if(curVar.valid==true){
				curVar.getDependents(this);
			}
		}
		
		//constraints
		int numConst=constraints.size();
		for(int i=0; i<numConst; i++){
			Constraint curConst=constraints.get(i);
			curConst.valid=true;
			try{
				curConst.parseConstraints();
				curConst.checkConstraints(this);
			} catch(Exception e){
				curConst.valid=false;
			}
		}
	}

	public void rescale(int scale){
		if(type==0){panelTree.rescale(scale);}
		else if(type==1){panelMarkov.rescale(scale);}
	}

	public void highlightParameter(int paramNum){
		if(type==0){panelTree.highlightParameter(paramNum);}
		else if(type==1){panelMarkov.highlightParameter(paramNum);}
	}
	
	public void highlightVariable(int varNum){
		if(type==0){panelTree.highlightVariable(varNum);}
		else if(type==1){panelMarkov.highlightVariable(varNum);}
	}

	public void highlightTable(int tableNum){
		if(type==0){panelTree.highlightTable(tableNum);}
		else if(type==1){panelMarkov.highlightTable(tableNum);}
	}

	public void addNode(int nodeType){
		if(type==0){panelTree.addNode(nodeType);}
		else if(type==1){panelMarkov.addNode(nodeType);}
	}

	public void changeNodeType(){
		if(type==0){panelTree.changeNodeType();}
		else if(type==1){panelMarkov.changeNodeType();}
	}

	public void addRemoveVarUpdates(){
		if(type==0){panelTree.addRemoveVarUpdates();}
		else if(type==1){panelMarkov.addRemoveVarUpdates();}
	}
	
	public void addRemoveCost(){
		if(type==0){panelTree.addRemoveCost();}
		else if(type==1){panelMarkov.addRemoveCost();}
	}

	public void ocd(String operation){
		if(type==0){panelTree.ocd(operation,alignRight);}
		else if(type==1){panelMarkov.ocd(operation,alignRight);}
	}

	public void equalY(){
		if(type==0){panelTree.equalY();}
		else if(type==1){panelMarkov.equalY();}
	}

	public void collapseBranch(){
		if(type==0){panelTree.collapseBranch();}
		else if(type==1){panelMarkov.collapseBranch();}
	}

	public void clearAnnotations(){
		if(type==0){panelTree.clearAnnotations();}
		else if(type==1){panelMarkov.clearAnnotations();}
	}

	public void saveSnapshot(String action){
		unsavedChanges=true;
		setUnsavedStatus();
		//Add undoable action
		actionStackUndo.push(action);
		modelStackUndo.push(new ModelSnapshot(this));
		mainForm.mntmUndo.setEnabled(true);
		mainForm.mntmUndo.setText("Undo "+action);
		//Clear redo stack
		actionStackRedo.clear();
		modelStackRedo.clear();

		mainForm.mntmRedo.setEnabled(false);
		mainForm.mntmRedo.setText("Redo");
	}

	private void setUnsavedStatus(){
		mainForm.setTabName("*"+name,type);
	}

	public void refreshUndoButtons(){
		//Undo
		if(modelStackUndo.size()==0){
			mainForm.mntmUndo.setEnabled(false);
			mainForm.mntmUndo.setText("Undo");
		}
		else{
			mainForm.mntmUndo.setEnabled(true);
			mainForm.mntmUndo.setText("Undo "+actionStackUndo.peek());
		}
		//Redo
		if(modelStackRedo.size()==0){
			mainForm.mntmRedo.setEnabled(false);
			mainForm.mntmRedo.setText("Redo");
		}
		else{
			mainForm.mntmRedo.setEnabled(true);
			mainForm.mntmRedo.setText("Redo "+actionStackRedo.peek());
		}

	}
	
	/*public double round(double num, int dim){
		double scale=Math.pow(10, dimInfo.decimals[dim]);
		num=Math.round(num*scale)/scale;
		return(num);
	}*/

	public boolean checkParamSets(Console console){
		boolean valid=true;
		int numParams=parameterNames.length;
		for(int i=0; i<numParams; i++){
			int index=getParameterIndex(parameterNames[i]);
			if(index==-1){
				console.print("Parameter not found: "+parameterNames[i]+"\n"); console.newLine();
				valid=false;
			}
		}
		return(valid);
	}
	
	public boolean checkModel(Console console, boolean checkProbs){
		if(simParamSets){ //check paramsets
			boolean validParams=checkParamSets(console);
			if(validParams==false){
				return(false);
			}
		}
		
		if(type==0){return(panelTree.checkTree(console,checkProbs));} //Decision tree
		else if(type==1){ //Markov
			
			//Reset t=0
			resetT();
			
			if(panelMarkov.curNode==null || panelMarkov.curNode.type!=1){ //No Markov Chain selected, check whole model
				return(panelMarkov.checkModel(console, checkProbs));
			}
			else{ //Markov Chain selected
				return(panelMarkov.checkChain(console,panelMarkov.curNode,checkProbs));
			}
			
		}
		return(false);
	}
	
	public ArrayList<String> parseModel(){
		if(type==0){return(tree.parseTree());}
		else if(type==1){
			if(panelMarkov.curNode==null || panelMarkov.curNode.type!=1){ //No Markov Chain selected, check whole model
				return(markov.parseTree());
			}
			else{ //Markov Chain selected
				return(markov.parseChain(panelMarkov.curNode));
			}
		}
		return(null);
	}

	private void printSimInfo(Console console){
		console.print(new Date()+"\n");
		if(type==0){console.print("Decision Tree: "+name+"\n");}
		else if(type==1){console.print("Markov Model: "+name+"\n");}
		
		if(simType==0 && cohortSize>1){console.print("Cohort size:\t"+cohortSize+"\n");}
		else if(simType==1){console.print("Monte Carlo simulations:\t"+cohortSize+"\n");}
	}
	
	public void runModel(Console console,boolean display){
		try{
			//curGenerator=generatorVar; //initialize to var RNG
			if(type==0){ //Decision tree
				if(display){
					console.print("Running tree... ");
					panelTree.tree.showEV=true;
				}
				evaluateParameters(); //get parameters
				panelTree.tree.runModel(display); //run model
				unlockParams(); //unlock parameters
				
				if(display){
					console.print("done!\n");
					printSimInfo(console);
					
					if(dimInfo.analysisType>0){
						panelTree.tree.runCEA(console);
					}
					else{ //Display output on console
						panelTree.tree.displayResults(console);
					}
				}
			}
			else if(type==1){ //Markov model
				if(display){panelMarkov.tree.showEV=true;}
				if(simParamSets==false){ //Base case
					evaluateParameters(); //get parameters
					if(panelMarkov.curNode==null || panelMarkov.curNode.type!=1){ //No Markov Chain selected, run all chains
						if(display){console.print("Running model... ");}
						for(int n=0; n<markov.nodes.size(); n++){
							MarkovNode curNode=markov.nodes.get(n);
							if(curNode.type==1){
								panelMarkov.tree.runModel(curNode,display);
								if(display){
									console.print(" done!\n");
									printSimInfo(console);
									if(dimInfo.analysisType>0){
										panelMarkov.tree.runCEA(console);
									}
								}
							}
						}
					}
					else{ //Markov Chain selected
						if(display){console.print("Running Markov Chain: "+panelMarkov.curNode.name);}
						panelMarkov.tree.runModel(panelMarkov.curNode, display);
						if(display){
							console.print(" done!\n");
							printSimInfo(console);
						}
					}
					unlockParams(); //unlock parameters
				}
				else{ //sim parameter sets
					ProgressMonitor progress=new ProgressMonitor(mainForm.frmMain, "Running parameter sets", "", 0, 100);
					int prog=0;
					//get number of chains
					ArrayList<MarkovNode> chainRoots=new ArrayList<MarkovNode>();
					if(panelMarkov.curNode==null || panelMarkov.curNode.type!=1){ //No Markov Chain selected, run all chains
						for(int n=0; n<markov.nodes.size(); n++){
							MarkovNode curNode=markov.nodes.get(n);
							if(curNode.type==1){
								chainRoots.add(curNode);
							}
						}
					}
					else{
						chainRoots.add(panelMarkov.curNode);
					}
					int numChains=chainRoots.size();
					int numSets=parameterSets.length;
					MarkovTrace traces[][]=new MarkovTrace[numChains][numSets];
					if(display){
						console.print("Running parameter sets... ");
						progress.setMaximum(numChains*numSets+1);
					}
					//Run all parameter sets
					for(int c=0; c<numChains; c++){
						for(int i=0; i<numSets; i++){
							prog++;
							if(display){progress.setProgress(prog);}
							parameterSets[i].setParameters(this);
							traces[c][i]=panelMarkov.tree.runModel(chainRoots.get(c),false);
						}
					}
					unlockParams(); //unlock parameters
					
					//get mean and bounds of results
					int numDim=dimInfo.dimSymbols.length;
					if(display){
						console.print(" done!\n");
						printSimInfo(console);
					}
					for(int c=0; c<numChains; c++){
						MarkovTraceSummary traceSummary=new MarkovTraceSummary(traces[c]);
						MarkovNode curNode=chainRoots.get(c);
						for(int d=0; d<numDim; d++){
							curNode.expectedValues[d]=traceSummary.expectedValues[d][0];
						}
						if(markov.discountRewards){
							for(int d=0; d<numDim; d++){
								curNode.expectedValuesDis[d]=traceSummary.expectedValuesDis[d][0];
							}
						}
						
						if(display){
							String buildString="";
							if(markov.discountRewards==false){
								for(int i=0; i<numDim-1; i++){buildString+="("+dimInfo.dimSymbols[i]+") "+MathUtils.round(curNode.expectedValues[i],dimInfo.decimals[i])+"; ";}
								buildString+="("+dimInfo.dimSymbols[numDim-1]+") "+MathUtils.round(curNode.expectedValues[numDim-1],dimInfo.decimals[numDim-1]);
							}
							else{
								for(int i=0; i<numDim-1; i++){buildString+="("+dimInfo.dimSymbols[i]+") "+MathUtils.round(curNode.expectedValuesDis[i],dimInfo.decimals[i])+"; ";}
								buildString+="("+dimInfo.dimSymbols[numDim-1]+") "+MathUtils.round(curNode.expectedValuesDis[numDim-1],dimInfo.decimals[numDim-1]);
							}
							curNode.textEV.setText(buildString);
							if(curNode.visible){curNode.textEV.setVisible(true);}
							
							frmTraceSummary showSummary=new frmTraceSummary(traceSummary,errorLog);
							showSummary.frmTraceSummary.setVisible(true);
						}
					}
					progress.close();
				}	

				if(display){
					//Display output on console
					panelMarkov.tree.displayResults(console);
				}
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(mainForm.frmMain, e.toString());
			errorLog.recordError(e);
		}
	}
	
	public void unlockParams(){
		for(int v=0; v<parameters.size(); v++){
			parameters.get(v).locked=false;
		}
	}
	
	public void unlockVars(){
		for(int v=0; v<variables.size(); v++){
			variables.get(v).locked=false;
		}
	}
	
	public int getStrategies(){
		if(type==0){
			TreeNode root=tree.nodes.get(0);
			strategyIndices=new int[root.childIndices.size()];
			int numStrat=strategyIndices.length;
			strategyNames=new String[numStrat];
			for(int i=0; i<numStrat; i++){
				strategyIndices[i]=root.childIndices.get(i);
				strategyNames[i]=tree.nodes.get(strategyIndices[i]).name;
			}
		}
		else if(type==1){
			MarkovNode root=markov.nodes.get(0);
			strategyIndices=new int[root.childIndices.size()];
			int numStrat=strategyIndices.length;
			strategyNames=new String[numStrat];
			for(int i=0; i<numStrat; i++){
				strategyIndices[i]=root.childIndices.get(i);
				strategyNames[i]=markov.nodes.get(strategyIndices[i]).name;
			}
		}
		return(strategyNames.length);
	}
	
	public double getStrategyEV(int s, int dim){
		double ev=Double.NaN;
		if(type==0){ //Decision tree
			ev=tree.nodes.get(strategyIndices[s]).expectedValues[dim];
		}
		else if(type==1){ //Markov
			if(markov.discountRewards==false){
				ev=markov.nodes.get(strategyIndices[s]).expectedValues[dim];
			}
			else{ //discounted
				ev=markov.nodes.get(strategyIndices[s]).expectedValuesDis[dim];
			}
		}
		return(ev);
	}

	public void updateDimensions(){
		if(type==0){tree.updateDimensions(dimInfo.dimSymbols.length);}
		else if(type==1){markov.updateDimensions(dimInfo.dimSymbols.length);}
		//Refresh panel with same scale
		rescale(scale);
	}
	
	public void addT(){
		Variable curT=new Variable();
		curT.name="t";
		curT.value=new Numeric(0);
		innateVariables.add(curT);
	}
	
	public void resetT(){
		int indexT=getInnateVariableIndex("t");
		innateVariables.get(indexT).value=new Numeric(0);
	}
	
	public boolean isParameter(String word){
		return(getParameterIndex(word)!=-1);
	}
	
	public boolean isVariable(String word){
		return(getVariableIndex(word)!=-1);
	}
	
	public boolean isInnateVariable(String word){
		return(getInnateVariableIndex(word)!=-1);
	}
	
	public boolean isTable(String word){
		return(getTableIndex(word)!=-1);
	}
	
	public int getParameterIndex(String name){
		int paramIndex=-1;
		int numParams=parameters.size();
		int i=0;
		while(paramIndex==-1 && i<numParams){
			if(parameters.get(i).name.matches(name)){paramIndex=i;}
			i++;
		}
		return(paramIndex);
	}
	
	public int getVariableIndex(String name){
		int varIndex=-1;
		int numVars=variables.size();
		int i=0;
		while(varIndex==-1 && i<numVars){
			if(variables.get(i).name.matches(name)){varIndex=i;}
			i++;
		}
		return(varIndex);
	}
	
	public int getInnateVariableIndex(String name){
		int varIndex=-1;
		int numVars=innateVariables.size();
		int i=0;
		while(varIndex==-1 && i<numVars){
			if(innateVariables.get(i).name.matches(name)){varIndex=i;}
			i++;
		}
		return(varIndex);
	}
	
	public int getTableIndex(String name){
		int tableIndex=-1;
		int numTables=tables.size();
		int i=0;
		while(tableIndex==-1 && i<numTables){
			if(tables.get(i).name.matches(name)){tableIndex=i;}
			i++;
		}
		return(tableIndex);
	}
	
	public boolean textHasVariable(String text){
		boolean hasVariable=false;
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			int varIndex=getVariableIndex(word);
			if(varIndex!=-1){ //is variable
				return(true);
			}
			int paramIndex=getParameterIndex(word);
			if(paramIndex!=-1){ //Check nested variable
				Parameter param=parameters.get(paramIndex);
				String paramExpr=param.expression;
				hasVariable=textHasVariable(paramExpr);
				if(hasVariable){
					return(true);
				}
			}
			
			if(index==len){len=0;} //End of word
			else{
				text=text.substring(index+1);
				len=text.length();
			}
		}
		return(hasVariable);
	}
	
	public String getTableType(String name){
		String type=null;
		int numTables=tables.size();
		int i=0;
		while(type==null && i<numTables){
			if(tables.get(i).name.matches(name)){type=tables.get(i).type;}
			i++;
		}
		return(type);
	}
	
	public String getParamDescription(String param){
		String des="";
		int index=getParameterIndex(param);
		Parameter curParam=parameters.get(index);
		des="<html><b>"+curParam.name+"</b><br>";
		des+="Expression: <br>"+MathUtils.consoleFont(curParam.expression)+"<br><br>";
		String strEV=curParam.value.toString().replaceAll("\\n", "<br>");
		//strEV=strEV.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		des+="Expected Value: <br>"+MathUtils.consoleFont(strEV)+"<br><br>";
		if(curParam.notes!=null && !curParam.notes.isEmpty()){
			String strNotes=curParam.notes.replaceAll("\\n", "<br>");
			des+="Notes: "+strNotes+"<br>";
		}
		des+="</html>";
		return(des);
	}
	
	public String getVarDescription(String var){
		String des="";
		int index=getVariableIndex(var);
		Variable curVar=variables.get(index);
		des="<html><b>"+curVar.name+"</b><br>";
		des+="Expression: <br>"+MathUtils.consoleFont(curVar.expression)+"<br><br>";
		String strEV=curVar.value.toString().replaceAll("\\n", "<br>");
		//strEV=strEV.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		des+="Expected Value: <br>"+MathUtils.consoleFont(strEV)+"<br><br>";
		if(curVar.notes!=null && !curVar.notes.isEmpty()){
			String strNotes=curVar.notes.replaceAll("\\n", "<br>");
			des+="Notes: "+strNotes+"<br>";
		}
		des+="</html>";
		return(des);
	}

	public String getTableDescription(String table){
		int index=getTableIndex(table);
		Table curTable=tables.get(index);
		if(curTable.type.matches("Lookup")){
			String des="<html><b>"+curTable.name+"</b><br>";
			des+="User-defined Lookup Table<br>";
			des+="Lookup Method: "+curTable.lookupMethod+"<br>";
			if(curTable.lookupMethod.matches("Interpolate")){
				des+="Interpolation Method: "+curTable.interpolate+"<br>";
				if(curTable.interpolate.matches("Cubic Splines")){
					des+="Boundary Condition: "+curTable.boundary+"<br>";
				}
				des+="Extrapolate: "+curTable.extrapolate+"<br>";
			}
			des+="Size: "+MathUtils.consoleFont(curTable.numRows+"")+" x "+MathUtils.consoleFont(curTable.numCols+"")+"<br>";
			if(curTable.notes!=null && !curTable.notes.isEmpty()){
				String strNotes=curTable.notes.replaceAll("\\n", "<br>");
				des+="Notes: "+strNotes+"<br>";
			}
			des+="</html>";
			return(des);
		}
		else if(curTable.type.matches("Distribution")){
			String des="<html><b>"+curTable.name+"</b><br>";
			des+="User-defined (empirical) distribution<br><br>";
			if(curTable.notes!=null && !curTable.notes.isEmpty()){des+="Notes: "+curTable.notes+"<br>";}
			des+="<i>Parameters</i><br>";
			int maxCol=curTable.numCols-1;
			if(maxCol==1){des+=MathUtils.consoleFont("n")+": Table column ("+MathUtils.consoleFont("1")+")<br>";}
			else{des+=MathUtils.consoleFont("n")+": Table column "+MathUtils.consoleFont("{1,...,"+maxCol+"}")+"<br><";}
			des+="<i><br>Sample</i><br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(n,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the user-defined distribution.<br>";
			des+="<i><br>Distribution Functions</i><br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(k,n,<b><i>f</i></b>)")+": Returns the value of the user-defined PMF at "+MathUtils.consoleFont("k")+"<br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(k,n,<b><i>F</i></b>)")+": Returns the value of the user-defined CDF at "+MathUtils.consoleFont("k")+"<br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(x,n,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the user-defined distribution at "+MathUtils.consoleFont("x")+"<br>";
			des+="<i><br>Moments</i><br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(n,<b><i>E</i></b>)")+": Returns the mean of the user-defined distribution<br>";
			des+=MathUtils.consoleFont("<b>"+curTable.name+"</b>","green")+MathUtils.consoleFont("(n,<b><i>V</i></b>)")+": Returns the variance of the user-defined distribution<br>";
			des+="</html>";
			return(des);
		}
		else if(curTable.type.matches("Matrix")){
			String des="<html><b>"+curTable.name+"</b><br>";
			des+="Matrix<br>";
			des+="Size: "+MathUtils.consoleFont(curTable.numRows+"")+" x "+MathUtils.consoleFont(curTable.numCols+"")+"<br>";
			if(curTable.notes!=null && !curTable.notes.isEmpty()){
				String strNotes=curTable.notes.replaceAll("\\n", "<br>");
				des+="Notes: "+strNotes+"<br>";
			}
			des+="</html>";
			return(des);
		}
		else{
			return(null);
		}
	}
	
	public void buildParamSetsTable(DefaultTableModel modelParams){
		modelParams.setRowCount(0);
		modelParams.setColumnCount(0);
		modelParams.addColumn("Set");
		modelParams.addColumn("Score");
		int numParams=parameterNames.length;
		for(int p=0; p<numParams; p++){
			modelParams.addColumn(parameterNames[p]);
		}
		int numSets=parameterSets.length;
		for(int i=0; i<numSets; i++){
			ParameterSet curSet=parameterSets[i];
			modelParams.addRow(new Object[]{null});
			modelParams.setValueAt(curSet.id, i, 0);
			modelParams.setValueAt(curSet.score, i, 1);
			for(int p=0; p<numParams; p++){
				modelParams.setValueAt(curSet.values[p].toString(), i, 2+p);
			}
		}
		
	}
	
	public void evaluateParameters() throws Exception{
		//Get parameter values for this run
		curGenerator=generatorParam;
		int numParams=parameters.size();
		for(int p=0; p<numParams; p++){
			Parameter curParam=parameters.get(p);
			if(curParam.locked==false){
				curParam.value=Interpreter.evaluate(curParam.expression, this,sampleParam);
				curParam.locked=true;
			}
		}
		curGenerator=generatorVar; //repoint
	}
}