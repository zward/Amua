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

import java.util.ArrayList;

import main.Constraint;
import main.DimInfo;
import main.Metadata;
import main.Parameter;
import main.ParameterSet;
import main.Scenario;
import main.Table;
import main.Variable;
import markov.MarkovTree;
import tree.DecisionTree;

public class ModelSnapshot{

	//Data
	public String name; //Model name
	int type; //0=Decision tree; 1=Markov
	public Metadata meta;
	public DimInfo dimInfo;
	public int scale=100;
	public boolean alignRight=false;
	public ArrayList<Parameter> parameters;
	public ArrayList<Variable> variables;
	public ArrayList<Table> tables;
	public ArrayList<Constraint> constraints;
	public boolean simParamSets;
	public String parameterNames[];
	public ParameterSet[] parameterSets;
	public ArrayList<Scenario> scenarios;
	public int simType=0; //0=Cohort, 1=Monte Carlo
	public int cohortSize=1000;
	public boolean CRN;
	public int crnSeed;
	public boolean displayIndResults;
	public int numThreads;
	public boolean reportSubgroups;
	public ArrayList<String> subgroupNames, subgroupDefinitions;
	
	//Model types
	DecisionTree tree;
	MarkovTree markov;

	//Constructor - takes snapshot
	public ModelSnapshot(AmuaModel model){
		name=model.name;
		type=model.type;
		meta=model.meta; //reference
		dimInfo=model.dimInfo.copy();
		alignRight=model.alignRight;
		scale=model.scale;
	
		parameters=new ArrayList<Parameter>();
		for(int i=0; i<model.parameters.size(); i++){
			Parameter copyParam=model.parameters.get(i).copy();
			parameters.add(copyParam);
		}
		variables=new ArrayList<Variable>();
		for(int i=0; i<model.variables.size(); i++){
			Variable copyVar=model.variables.get(i).copy();
			variables.add(copyVar);
		}
		tables=new ArrayList<Table>();
		for(int i=0; i<model.tables.size(); i++){
			Table copyTable=model.tables.get(i).copy();
			tables.add(copyTable);
		}
		constraints=new ArrayList<Constraint>();
		for(int i=0; i<model.constraints.size(); i++){
			Constraint copyConst=model.constraints.get(i).copy();
			constraints.add(copyConst);
		}
		//parameter sets
		simParamSets=model.simParamSets;
		if(model.parameterNames!=null){
			int numParams=model.parameterNames.length;
			parameterNames=new String[numParams];
			for(int i=0; i<numParams; i++){
				parameterNames[i]=model.parameterNames[i];
			}
			int numSets=model.parameterSets.length;
			parameterSets=new ParameterSet[numSets];
			for(int i=0; i<numSets; i++){
				parameterSets[i]=model.parameterSets[i].copy();
			}
		}
		//scenarios
		if(model.scenarios!=null){
			scenarios=new ArrayList<Scenario>();
			int numRuns=model.scenarios.size();
			for(int i=0; i<numRuns; i++){
				scenarios.add(model.scenarios.get(i).copy());
			}
		}
		
		//simulation settings
		simType=model.simType;
		cohortSize=model.cohortSize;
		CRN=model.CRN;
		crnSeed=model.crnSeed;
		displayIndResults=model.displayIndResults;
		numThreads=model.numThreads;
		
		//subgroup settings
		reportSubgroups=model.reportSubgroups;
		subgroupNames=new ArrayList<String>(); subgroupDefinitions=new ArrayList<String>();
		for(int i=0; i<model.subgroupNames.size(); i++){
			subgroupNames.add(model.subgroupNames.get(i));
			subgroupDefinitions.add(model.subgroupDefinitions.get(i));
		}
				
		if(type==0){tree=model.tree.snapshot();}
		else if(type==1){markov=model.markov.snapshot();}
	}

	//Re-points model data/objects
	public void getSnapshot(AmuaModel model){
		model.name=name;
		model.type=type;
		model.meta=meta; //reference
		model.dimInfo=dimInfo;
		model.alignRight=alignRight;
		model.scale=scale;
	
		model.parameters=parameters;
		model.variables=variables;
		model.tables=tables;
		model.constraints=constraints;
		model.simParamSets=simParamSets;
		model.parameterNames=parameterNames;
		model.parameterSets=parameterSets;
		model.scenarios=scenarios;
		
		model.simType=simType;
		model.cohortSize=cohortSize;
		model.CRN=CRN;
		model.crnSeed=crnSeed;
		model.displayIndResults=displayIndResults;
		model.numThreads=numThreads;
		
		model.reportSubgroups=reportSubgroups;
		model.subgroupNames=subgroupNames;
		model.subgroupDefinitions=subgroupDefinitions;
		
		if(type==0){
			model.tree=tree;
			model.panelTree.tree=model.tree;
			model.tree.myModel=model;
			for(int i=0; i<model.tree.nodes.size(); i++){
				model.tree.nodes.get(i).setPanel(model.panelTree);
			}
		}
		else if(type==1){
			model.markov=markov;
			model.panelMarkov.tree=model.markov;
			model.markov.myModel=model;
			model.markov.updateMarkovChain(model.markov.nodes.get(0));
		}
	}

}