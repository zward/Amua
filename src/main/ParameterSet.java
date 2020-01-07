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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;

@XmlRootElement(name="ParameterSet")
public class ParameterSet{
	@XmlElement public String id;
	@XmlElement public double score;
	@XmlElement public String strValues[];
	@XmlTransient public Numeric values[];
	
	//Constructor
	public ParameterSet(){
		
	}
	
	//Get current parameters
	public ParameterSet(AmuaModel myModel){
		int numParams=myModel.parameters.size();
		values=new Numeric[numParams];
		strValues=new String[numParams];
		for(int i=0; i<numParams; i++){
			values[i]=myModel.parameters.get(i).value.copy();
			strValues[i]=values[i].saveAsXMLString(); //for writing to xml
		}
	}

	public void parseXMLValues(){
		int numParams=strValues.length;
		values=new Numeric[numParams];
		for(int p=0; p<numParams; p++){
			values[p]=new Numeric(strValues[p]); //convert from string back to Numeric object
		}
	}
	
	public void parseCSVLine(String strLine, AmuaModel myModel) throws Exception {
		String data[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		id=data[0];
		score=Double.parseDouble(data[1]);
		int numParams=data.length-2;
		values=new Numeric[numParams];
		strValues=new String[numParams];
		for(int p=0; p<numParams; p++) {
			String datum=data[p+2].replaceAll("\"", ""); //strip quotes
			values[p]=Interpreter.evaluate(datum, myModel, false);
			strValues[p]=values[p].saveAsXMLString(); //for writing to xml
		}
	}
	
	
	public void setParameters(AmuaModel myModel){
		int numParams=values.length;
		for(int i=0; i<numParams; i++){
			String curParamName=myModel.parameterNames[i];
			int paramIndex=myModel.getParameterIndex(curParamName);
			if(paramIndex!=-1){
				Parameter curParam=myModel.parameters.get(paramIndex);
				curParam.value=values[i].copy();
				curParam.locked=true;
			}
		}
	}

	//returns a copy of this parameter set
	public ParameterSet copy(){
		ParameterSet set=new ParameterSet();
		set.id=this.id;
		set.score=this.score;
		int numParams=strValues.length;
		set.strValues=new String[numParams];
		set.values=new Numeric[numParams];
		for(int i=0; i<numParams; i++){
			set.strValues[i]=this.strValues[i];
			set.values[i]=this.values[i].copy();
		}
		return(set);
	}

}