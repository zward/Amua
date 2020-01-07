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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;
import math.Token;

@XmlRootElement(name="Variable")
public class Variable{
	@XmlElement public String name;
	@XmlElement public String expression;
	@XmlElement public String notes;
	
	@XmlTransient public boolean valid=true;
	@XmlTransient public ArrayList<Variable> dependents; //variables that depend on me
	@XmlTransient public boolean independent;
		
	@XmlTransient public Numeric value[]; //thread-specific
	@XmlTransient public boolean locked[]; //thread-specific
	@XmlTransient public Token parsedTokens[];
	
	//Constructor
	public Variable(){
		value=new Numeric[1]; //construct test values
		locked=new boolean[1];
	}

	public Variable copy(){
		Variable copyVar=new Variable();
		copyVar.name=name;
		copyVar.expression=expression;
		copyVar.notes=notes;
		copyVar.parsedTokens=parsedTokens;
		copyVar.value=value;
		return(copyVar);
	}
	
	public void getDependents(AmuaModel myModel){
		independent=true;
		//Parse word by word
		String text=expression;
		int len=text.length();
		while(len>0){
			int index=Interpreter.getNextBreakIndex(text);
			String word=text.substring(0, index);
			int varIndex=myModel.getVariableIndex(word);
			if(varIndex!=-1){ //is variable
				independent=false;
				Variable dep=myModel.variables.get(varIndex);
				if(!dep.dependents.contains(this)){
					dep.dependents.add(this); //add myself as dependent
				}
			}
			else if(word.equals("t")) { //time dependent
				Variable t=myModel.innateVariables.get(myModel.getInnateVariableIndex("t"));
				if(!t.dependents.contains(this)) {
					t.dependents.add(this); //add myself as dependent
				}
			}
			
			if(index==len){len=0;} //End of word
			else{
				text=text.substring(index+1);
				len=text.length();
			}
		}
	}
	
	public void updateDependents(AmuaModel myModel, int curThread) throws Exception{
		for(int d=0; d<dependents.size(); d++){
			Variable curDep=dependents.get(d);
			if(curDep.locked[curThread]==false){
				curDep.locked[curThread]=true;
				curDep.value[curThread]=Interpreter.evaluateTokens(curDep.parsedTokens, curThread, false);
				curDep.updateDependents(myModel, curThread);
			}
		}
	}
	
	public void unlockDependents(int curThread) {
		for(int d=0; d<dependents.size(); d++) {
			Variable curDep=dependents.get(d);
			if(curDep.locked[curThread]==true) {
				curDep.locked[curThread]=false;
				curDep.unlockDependents(curThread);
			}
		}
	}
}