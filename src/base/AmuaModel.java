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

package base;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gui.frmCEPlane;
import gui.frmMain;
import gui.frmTraceSummary;
import gui.frmTraceSummaryMulti;
import main.Console;
import main.ConsoleTable;
import main.Constraint;
import main.DimInfo;
import main.ErrorLog;
import main.MersenneTwisterFast;
import main.Metadata;
import main.Parameter;
import main.ParameterSet;
import main.ScaledIcon;
import main.Scenario;
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
import math.Token;
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
	@XmlElement(name="Scenario", type=Scenario.class) public ArrayList<Scenario> scenarios;
	//Simulation settings
	@XmlElement public int simType=0; //0=Cohort, 1=Monte Carlo
	@XmlElement public int cohortSize=1000;
	@XmlElement public boolean CRN; //common random numbers
	@XmlElement public int crnSeed; //CRN seed
	@XmlElement public boolean displayIndResults;
	@XmlElement public int numThreads=1;
	//Subgroup settings
	@XmlElement public boolean reportSubgroups;
	@XmlElement public ArrayList<String> subgroupNames, subgroupDefinitions;
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
	@XmlTransient public Token subgroupTokens[][];
	//sampling
	@XmlTransient public boolean sampleParam, sampleVar;
	@XmlTransient public MersenneTwisterFast generatorParam, generatorVar[], curGenerator[]; //thread-specific
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
		subgroupNames=new ArrayList<String>();
		subgroupDefinitions=new ArrayList<String>();
		
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
			if(type==1) { //Markov
				addT();
			}
			
			//Update tables
			if(parameters==null){parameters=new ArrayList<Parameter>();}
			if(variables==null){variables=new ArrayList<Variable>();}
			if(tables==null){tables=new ArrayList<Table>();}
			if(constraints==null){constraints=new ArrayList<Constraint>();}
			//Construct splines if needed
			for(int t=0; t<tables.size(); t++){
				Table curTable=tables.get(t);
				curTable.myModel=this;
				if(curTable.interpolate!=null && curTable.interpolate.matches("Cubic Splines")){
					curTable.constructSplines();
				}
			}
			if(subgroupNames==null){
				subgroupNames=new ArrayList<String>();
				subgroupDefinitions=new ArrayList<String>();
			}
			
			validateModelObjects();
			
			refreshParamTable();
			refreshVarTable();
			refreshTableTable();
			refreshConstTable();
			//Parse parameters
			if(parameterNames!=null){
				int numSets=parameterSets.length;
				for(int i=0; i<numSets; i++){
					parameterSets[i].parseXMLValues();
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
			mainForm.btnAlignLeft.setIcon(new ScaledIcon("/images/alignLeftSelected",24,24,24,true));
			mainForm.btnAlignLeft.setDisabledIcon(new ScaledIcon("/images/alignLeftSelected",24,24,24,false));
			
			mainForm.btnAlignRight.setIcon(new ScaledIcon("/images/alignRight",24,24,24,true));
			mainForm.btnAlignRight.setDisabledIcon(new ScaledIcon("/images/alignRight",24,24,24,false));
		}
		else{
			mainForm.btnAlignRight.setIcon(new ScaledIcon("/images/alignRightSelected",24,24,24,true));
			mainForm.btnAlignRight.setDisabledIcon(new ScaledIcon("/images/alignRightSelected",24,24,24,false));
		
			mainForm.btnAlignLeft.setIcon(new ScaledIcon("/images/alignLeft",24,24,24,true));
			mainForm.btnAlignLeft.setDisabledIcon(new ScaledIcon("/images/alignLeft",24,24,24,false));
		}
	}
	
	
	public void saveModel(){
		try{
			meta.update();

			JAXBContext context = JAXBContext.newInstance(AmuaModel.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //should be default, but make UTF explicit

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
					curParam.parsedTokens=Interpreter.parse(curParam.expression,this);
					curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, false);
					
				}catch(Exception e){
					curParam.valid=false;
					curParam.parsedTokens=null;
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
				curVar.parsedTokens=Interpreter.parse(curVar.expression, this);
				curVar.value[0]=Interpreter.evaluateTokens(curVar.parsedTokens, 0, false);
				curVar.dependents=new ArrayList<Variable>();
			}catch(Exception e){
				curVar.valid=false;
				curVar.parsedTokens=null;
				curVar.value[0]=null;
			}
		}
		//get variable dependents
		if(type==1) { //Markov
			int indexT=getInnateVariableIndex("t");
			Variable curT=innateVariables.get(indexT);
			curT.dependents=new ArrayList<Variable>();
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
		
		if(dimInfo.analysisType==0) { //EV
			if(dimInfo.objectiveDim<0 || dimInfo.objectiveDim>dimInfo.dimNames.length-1) {
				console.print("Analysis Type Error: EV Outcome is out of bounds! Index "+dimInfo.objectiveDim+"\n" );
				console.print("Go to Model -> Properties to select a valid outcome\n");
				console.newLine();
				return(false);
			}
		}
		else { //CEA or BCA
			String type="CEA";
			if(dimInfo.analysisType==2) {type="BCA";}
			if(dimInfo.costDim<0 || dimInfo.costDim>dimInfo.dimNames.length-1) {
				console.print(type+" Error: Cost outcome is out of bounds! Index "+dimInfo.costDim+"\n" );
				console.print("Go to Model -> Properties to select a valid cost outcome\n");
				console.newLine();
				return(false);
			}
			if(dimInfo.effectDim<0 || dimInfo.effectDim>dimInfo.dimNames.length-1) {
				console.print(type+" Error: Effect outcome is out of bounds! Index "+dimInfo.effectDim+"\n" );
				console.print("Go to Model -> Properties to select a valid effect outcome\n");
				console.newLine();
				return(false);
			}
		}
		
		if(dimInfo.analysisType==1){ //CEA
			if(dimInfo.baseScenario==null || dimInfo.baseScenario.isEmpty()){
				console.print("\nCEA Error: Baseline scenario is not specified!\n");
				console.newLine();
				return(false);
			}
			else{ //ensure strategy is valid
				int baseIndex=getStrategyIndex(dimInfo.baseScenario);
				if(baseIndex==-1){
					console.print("\nCEA Error: Baseline scenario is not recognized! ("+dimInfo.baseScenario+")\n");
					console.newLine();
					return(false);
				}
			}
		}
		
		//check model objects
		validateModelObjects();
		boolean parse=true;
		ArrayList<String> objectErrors=new ArrayList<String>();
		for(int p=0; p<parameters.size(); p++){
			if(parameters.get(p).valid==false){
				parse=false;
				objectErrors.add("Parameter: "+parameters.get(p).name);
			}
		}
		for(int v=0; v<variables.size(); v++){
			if(variables.get(v).valid==false){
				parse=false;
				objectErrors.add("Variable: "+variables.get(v).name);
			}
		}
		for(int c=0; c<constraints.size(); c++){
			if(constraints.get(c).valid==false){
				parse=false;
				objectErrors.add("Constraint: "+constraints.get(c).name);
			}
		}
				
		if(parse==false){ //object errors found
			console.print(objectErrors.size()+" object errors:\n");
			for(int i=0; i<objectErrors.size(); i++){
				console.print(objectErrors.get(i)+"\n");		
			}
			console.newLine();
			return(false);
		}
		else{
			if(type==0){ //Decision tree
				return(panelTree.checkTree(console,checkProbs));
			} 
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

	public void printSimInfo(Console console){
		console.print(new Date()+"\n");
		if(type==0){console.print("Decision Tree:\t"+name+"\n");}
		else if(type==1){console.print("Markov Model:\t"+name+"\n");}
		
		if(simType==0 && cohortSize>1){console.print("Cohort size:\t"+cohortSize+"\n");}
		else if(simType==1){console.print("Monte Carlo simulations:\t"+cohortSize+"\n");}
	}
	
	public String getSimInfoHTML() {
		String info="";
		info+=(new Date()+"<br>");
		if(type==0){info+=("Decision Tree:\t"+name+"<br>");}
		else if(type==1){info+=("Markov Model:\t"+name+"<br>");}
		
		if(simType==0 && cohortSize>1){info+=("Cohort size:\t"+cohortSize+"<br>");}
		else if(simType==1){info+=("Monte Carlo simulations:\t"+cohortSize+"<br>");}
		
		return(info);
	}
	
	public RunReport runModel(Console console,boolean display){
		long startTime=System.currentTimeMillis();
		RunReport report=new RunReport(this);
		
		if(type==0){ //Decision tree
			runDecisionTree(console,display,report);
		}
		else if(type==1){ //Markov model
			if(display){panelMarkov.tree.showEV=true;}
			if(simParamSets==false){ //Base case
				runMarkov(console,display,report);
			}
			else{ //sim parameter sets
				runMarkovParamSets(console,display);
			}	
		}
		
		long endTime=System.currentTimeMillis();
		report.runTime=endTime-startTime;
		
		return(report);
	}
	
	
	private void runDecisionTree(Console console,boolean display,RunReport runReport){
		try{
			if(display){
				console.print("Running tree... ");
				panelTree.tree.showEV=true;
			}
			evaluateParameters(); //get parameters
			panelTree.tree.runModel(display,runReport); //run model
			runReport.getResults(true);
			unlockParams(); //unlock parameters
			
			if(display){
				console.print("done!\n");
				printSimInfo(console);
				runReport.printResults(console,true);
				if(dimInfo.analysisType>0){ //display ICERS/NMB on tree
					panelTree.tree.displayCEAResults(runReport);
					if(dimInfo.analysisType==1) { //CEA
						frmCEPlane plane=new frmCEPlane(this,runReport);
						plane.frmCEPlane.setVisible(true);
					}
				}
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(mainForm.frmMain, e.toString());
			errorLog.recordError(e);
		}
	}
	
	private void runMarkov(Console console, boolean display, RunReport runReport){
		try{
			evaluateParameters(); //get parameters
			if(panelMarkov.curNode==null || panelMarkov.curNode.type!=1){ //No Markov Chain selected, run all chains
				if(display){console.print("Running model... ");}
				markov.runModel(display,runReport,true);
				runReport.getResults(true);
				if(display){
					console.print(" done!\n");
					printSimInfo(console);
					runReport.printResults(console,true);
					if(dimInfo.analysisType>0){ //display ICERs/NMB on tree
						markov.displayCEAResults(runReport);
						if(dimInfo.analysisType==1) { //CEA
							frmCEPlane plane=new frmCEPlane(this,runReport);
							plane.frmCEPlane.setVisible(true);
						}
					}
				}
			}
			else{ //Single Markov Chain selected
				if(display){console.print("Running Markov Chain: "+panelMarkov.curNode.name);}
				markov.runModel(display,runReport,false);
				
				if(display){
					console.print(" done!\n");
				}
			}
			unlockParams(); //unlock parameters
		}catch(Exception e){
			JOptionPane.showMessageDialog(mainForm.frmMain, e.toString());
			errorLog.recordError(e);
		}
	}
	
	private void runMarkovParamSets(Console console,boolean display){
		try{
			ProgressMonitor progress=new ProgressMonitor(mainForm.frmMain, "Running parameter sets", "", 0, 100);
			int prog=0;
			//get number of chains
			ArrayList<MarkovNode> chainRoots=new ArrayList<MarkovNode>();
			for(int n=0; n<markov.nodes.size(); n++){
				MarkovNode curNode=markov.nodes.get(n);
				if(curNode.type==1){
					chainRoots.add(curNode);
				}
			}
			
			int numChains=chainRoots.size();
			int numSets=parameterSets.length;
			MarkovTrace traces[][]=new MarkovTrace[numChains][numSets];
			if(display){
				console.print("Running parameter sets... ");
				progress.setMaximum(numSets+1);
			}
			
			boolean origShowTrace=markov.showTrace;
			markov.showTrace=false;
			
			//Run all parameter sets
			int numStrat=getStrategies();
			int numDim=dimInfo.dimNames.length;
			double results[][][]=new double[numDim][numStrat][numSets];
			boolean cancelled=false;
			RunReport runReports[]=new RunReport[numSets];
			for(int i=0; i<numSets; i++){
				prog++;
				if(display){progress.setProgress(prog);}
				runReports[i]=new RunReport(this);
				parameterSets[i].setParameters(this);
				markov.runModel(false,runReports[i],true);
				runReports[i].getResults(true);
				for(int c=0; c<numChains; c++){
					traces[c][i]=runReports[i].markovTraces.get(c);
				}
				//Get EVs
				for(int d=0; d<numDim; d++){
					for(int s=0; s<numStrat; s++){
						results[d][s][i]=getStrategyEV(s,d);
					}
				}
				if(progress.isCanceled()){  //End loop
					i=numSets;
					cancelled=true;
				}
			}
			unlockParams(); //unlock parameters
			markov.showTrace=origShowTrace;
			
			if(cancelled==false){

				//get mean and bounds of traces
				if(display){
					console.print(" done!\n");
					
					double meanResults[][]=new double[numDim][numStrat];
					double lbResults[][]=new double[numDim][numStrat];
					double ubResults[][]=new double[numDim][numStrat];
					int bounds[]=MathUtils.getBoundIndices(numSets);
					int indexLB=bounds[0], indexUB=bounds[1];

					//Sort ordered arrays
					for(int d=0; d<numDim; d++){
						for(int s=0; s<numStrat; s++){
							Arrays.sort(results[d][s]);
							for(int n=0; n<numSets; n++){
								meanResults[d][s]+=results[d][s][n];
							}
							meanResults[d][s]/=(numSets*1.0);
							lbResults[d][s]=results[d][s][indexLB];
							ubResults[d][s]=results[d][s][indexUB];
						}
					}
					
					//Print results summary to console
					printSimInfo(console);
					console.print("Parameter Sets:\t"+numSets+"\n\n");
					boolean colTypes[]=new boolean[]{false,false,true,true,true}; //is column number (true), or text (false)
					ConsoleTable curTable=new ConsoleTable(console,colTypes);
					String headers[]=new String[]{"Strategy","Outcome","Mean","95% LB","95% UB"};
					curTable.addRow(headers);
					//strategy results
					for(int s=0; s<numStrat; s++){
						String stratName=strategyNames[s];
						for(int d=0; d<numDim; d++){
							String dimName=dimInfo.dimNames[d];
							if(type==1 && markov.discountRewards){dimName+=" (Dis)";}
							double mean=MathUtils.round(meanResults[d][s],dimInfo.decimals[d]);
							double lb=MathUtils.round(lbResults[d][s],dimInfo.decimals[d]);
							double ub=MathUtils.round(ubResults[d][s],dimInfo.decimals[d]);
							String curRow[]=new String[]{stratName,dimName,mean+"",lb+"",ub+""};
							curTable.addRow(curRow);
						}
					}
					curTable.print();
					if(simType==1 && displayIndResults==true){
						console.print("\nIndividual-level Results:\n");
						RunReportSummary summary=new RunReportSummary(runReports);
						for(int s=0; s<numChains; s++){
							console.print("Chain: "+chainRoots.get(s).name+"\n");
							summary.microStatsSummary[s].printSummary(console);
						}
					}
					
					
					console.newLine();
				}
				//get trace summary
				for(int c=0; c<numChains; c++){
					MarkovTraceSummary traceSummary[]=new MarkovTraceSummary[1];
					traceSummary[0]=new MarkovTraceSummary(traces[c]);
					MarkovNode curNode=chainRoots.get(c);
					for(int d=0; d<numDim; d++){
						curNode.expectedValues[d]=traceSummary[0].expectedValues[d][0];
					}
					if(markov.discountRewards){
						for(int d=0; d<numDim; d++){
							curNode.expectedValuesDis[d]=traceSummary[0].expectedValuesDis[d][0];
						}
					}

					if(display){
						if(dimInfo.analysisType==0){ //EV
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
						}
						
						if(markov.showTrace==true && markov.compileTraces==false) {
							frmTraceSummary showSummary=new frmTraceSummary(traceSummary,errorLog,null);
							showSummary.frmTraceSummary.setVisible(true);
						}
					}
				}
				if(markov.showTrace==true && markov.compileTraces==true) {
					RunReportSummary reportSummary=new RunReportSummary(runReports);
					frmTraceSummaryMulti window=new frmTraceSummaryMulti(reportSummary,errorLog);
					window.frmTraceSummaryMulti.setVisible(true);
				}
				
				progress.close();

				if(display){
					if(dimInfo.analysisType>0){
						//panelMarkov.tree.runCEA(console);
					}
					else{
						//panelMarkov.tree.displayModelResults(console);
					}
				}
			} //end check cancelled == false
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
	
	public void unlockVarsAll(int curThread){
		for(int v=0; v<variables.size(); v++){
			variables.get(v).locked[curThread]=false;
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
	
	public double getSubgroupEV(int g, int s, int dim){
		double ev=Double.NaN;
		if(type==0){ //Decision tree
			ev=tree.nodes.get(strategyIndices[s]).expectedValuesGroup[g][dim];
		}
		else if(type==1){ //Markov
			if(markov.discountRewards==false){
				ev=markov.nodes.get(strategyIndices[s]).expectedValuesGroup[g][dim];
			}
			else{ //discounted
				ev=markov.nodes.get(strategyIndices[s]).expectedValuesDisGroup[g][dim];
			}
		}
		
		return(ev);
	}
	
	public int getStrategyIndex(String strat){
		int numStrat=getStrategies();
		int index=-1;
		int s=0;
		while(index==-1 && s<numStrat){
			if(strategyNames[s].equals(strat)){
				index=s;
			}
			s++;
		}
		return(index);
	}

	public void updateDimensions(int dimIndices[]){
		if(type==0){tree.updateDimensions(dimIndices);}
		else if(type==1){markov.updateDimensions(dimIndices);}
		//Refresh panel with same scale
		rescale(scale);
	}
	
	public void addT(){
		Variable curT=new Variable();
		curT.name="t";
		curT.value[0]=new Numeric(0);
		curT.locked[0]=true;
		innateVariables.add(curT);
	}
	
	public void resetT(){
		int indexT=getInnateVariableIndex("t");
		innateVariables.get(indexT).value[0]=new Numeric(0);
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
			if(parameters.get(i).name.equals(name)){paramIndex=i;}
			i++;
		}
		return(paramIndex);
	}
	
	public int getVariableIndex(String name){
		int varIndex=-1;
		int numVars=variables.size();
		int i=0;
		while(varIndex==-1 && i<numVars){
			if(variables.get(i).name.equals(name)){varIndex=i;}
			i++;
		}
		return(varIndex);
	}
	
	public int getInnateVariableIndex(String name){
		int varIndex=-1;
		int numVars=innateVariables.size();
		int i=0;
		while(varIndex==-1 && i<numVars){
			if(innateVariables.get(i).name.equals(name)){varIndex=i;}
			i++;
		}
		return(varIndex);
	}
	
	public int getTableIndex(String name){
		int tableIndex=-1;
		int numTables=tables.size();
		int i=0;
		while(tableIndex==-1 && i<numTables){
			if(tables.get(i).name.equals(name)){tableIndex=i;}
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
			if(isVariable(word)){
				return(true);
			}
			if(index==len){len=0;} //End of word
			else{
				text=text.substring(index+1);
				len=text.length();
			}
		}
		return(hasVariable);
	}
	
	public boolean textHasInnateVariable(String text){
		boolean hasVariable=false;
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			if(isInnateVariable(word)){
				return(true);
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
			if(tables.get(i).name.equals(name)){type=tables.get(i).type;}
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
		String strEV=curVar.value[0].toString().replaceAll("\\n", "<br>");
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
				modelParams.setValueAt(curSet.values[p].saveAsCSVString().replaceAll("\"", ""), i, 2+p);
			}
		}
		
	}
	
	public void evaluateParameters() throws Exception{
		if(curGenerator==null){curGenerator=new MersenneTwisterFast[1];}
		if(generatorVar==null){generatorVar=new MersenneTwisterFast[1];}
		
		//Get parameter values for this run
		curGenerator[0]=generatorParam;
		int numParams=parameters.size();
		for(int p=0; p<numParams; p++){
			Parameter curParam=parameters.get(p);
			if(curParam.locked==false){
				//curParam.value=Interpreter.evaluate(curParam.expression, this,sampleParam);
				curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, sampleParam);
				curParam.locked=true;
			}
		}
		curGenerator[0]=generatorVar[0]; //repoint
	}
	
	public void parseSubgroups() throws Exception{
		int numSubgroups=subgroupDefinitions.size();
		subgroupTokens=new Token[numSubgroups][];
		for(int g=0; g<numSubgroups; g++){
			subgroupTokens[g]=Interpreter.parse(subgroupDefinitions.get(g), this);
		}
	}
}