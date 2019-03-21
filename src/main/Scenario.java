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

package main;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;
import math.NumericException;

@XmlRootElement(name="Scenario")
public class Scenario{
	@XmlElement public String name;
	//uncertainty
	@XmlElement public int numIterations;
	@XmlElement public boolean crn1, crn2, sampleParams;
	@XmlElement public int cohortSize,seed1, seed2;
	
	@XmlElement public String objectUpdates;
	@XmlElement public String notes;
	
	@XmlTransient public boolean baseCase;
	@XmlTransient int objectTypes[]; //0=Params, 1=Variables
	@XmlTransient public String objectNames[], strUpdates[];
	@XmlTransient public Numeric testVals[];
	
	//Constructor
	public Scenario(AmuaModel myModel){ //get current settings
		numIterations=1;
		cohortSize=myModel.cohortSize;
		crn1=myModel.CRN;
		seed1=myModel.crnSeed;
	}

	public Scenario(){
		
	}
	
	public Scenario copy(){
		Scenario copyScenario=new Scenario();
		copyScenario.name=name;
		copyScenario.numIterations=numIterations;
		copyScenario.cohortSize=cohortSize;
		copyScenario.crn1=crn1;
		copyScenario.seed1=seed1;
		copyScenario.sampleParams=sampleParams;
		copyScenario.crn2=crn1;
		copyScenario.seed2=seed2;
		copyScenario.objectUpdates=objectUpdates;
		copyScenario.notes=notes;
		return(copyScenario);
	}
	
	public void parseUpdates(AmuaModel myModel) throws Exception{
		if(objectUpdates.isEmpty()){
			baseCase=true;
		}
		else{
			String updates[]=objectUpdates.split(";");
			int numUpdates=updates.length;
			objectTypes=new int[numUpdates];
			objectNames=new String[numUpdates];
			strUpdates=new String[numUpdates];
			testVals=new Numeric[numUpdates];
			for(int i=0; i<numUpdates; i++){
				parseExp(myModel,updates[i],i);
			}
		}
	}
	
	
	public void parseExp(AmuaModel myModel, String expression, int updateIndex) throws Exception{
		String curExpr=expression.replace(" ",""); //remove whitespace
		//find assignment operator
		int pos=curExpr.indexOf('=');
		if(pos==-1){
			throw new NumericException("No assignment operator (=) found in expression: '"+expression+"'","ScheduleRun");
		}
		String strObj=curExpr.substring(0, pos);
		int paramIndex=myModel.getParameterIndex(strObj);
		if(paramIndex!=-1){objectTypes[updateIndex]=0;}
		int varIndex=myModel.getVariableIndex(strObj);
		if(varIndex!=-1){objectTypes[updateIndex]=1;}
		if(paramIndex==-1 && varIndex==-1){
			throw new NumericException("Object not found: "+strObj,"ScheduleRun");
		}
		objectNames[updateIndex]=strObj;
		
		//get new expression
		strUpdates[updateIndex]=curExpr.substring(pos+1);
		
		//validate expression
		testVals[updateIndex]=Interpreter.evaluate(strUpdates[updateIndex], myModel, false);
		
		if(paramIndex!=-1){ //check that expression is valid for parameters
			
		}
	}
	
	public void applyUpdates(AmuaModel myModel) throws Exception{
		if(baseCase==false){
			int numUpdates=objectTypes.length;
			for(int i=0; i<numUpdates; i++){
				if(objectTypes[i]==0){ //parameter
					int index=myModel.getParameterIndex(objectNames[i]);
					if(index==-1){
						throw new NumericException("Parameter not found: "+objectNames[i],"ScheduleRun");
					}
					Parameter curParam=myModel.parameters.get(index);
					curParam.expression=strUpdates[i];
				}
				else if(objectTypes[i]==1){ //variable
					int index=myModel.getVariableIndex(objectNames[i]);
					if(index==-1){
						throw new NumericException("Variable not found: "+objectNames[i],"ScheduleRun");
					}
					Variable curVar=myModel.variables.get(index);
					curVar.expression=strUpdates[i];
				}
			}
		}
	}
	
}