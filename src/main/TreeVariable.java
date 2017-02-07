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

@XmlRootElement(name="TreeVariable")
public class TreeVariable{
	@XmlElement public String name;
	@XmlElement public String expression;
	@XmlElement public double value;
	@XmlElement public String notes;
	boolean fixed=false;
	boolean valid=true;

	//Constructor
	public TreeVariable(){

	}

	public TreeVariable copy(){
		TreeVariable copyVar=new TreeVariable();
		copyVar.name=name;
		copyVar.expression=expression;
		copyVar.value=value;
		copyVar.notes=notes;
		return(copyVar);
	}

	public void evaluate(VarHelper varHelper){
		try{
			valid=true;
			if(fixed==false){
				//Evaluate any nested variables first
				String text=expression;
				int len=text.length();
				while(len>0){
					int index=varHelper.getNextIndex(text);
					String word=text.substring(0, index);
					int varIndex=varHelper.getVariableIndex(word);
					if(varIndex!=-1){ //Evaluate nested variable
						TreeVariable var=varHelper.variables.get(varIndex);
						var.evaluate(varHelper);
						if(var.valid==false){
							valid=false;
						}
					}
					if(index==len){len=0;} //End of word
					else{
						text=text.substring(index+1);
						len=text.length();
					}
				}
				//Evaluate this variable
				value=varHelper.evaluateExpression(expression);
			}
		}catch(Exception e){
			valid=false;
		}
	}

	public void sample(VarHelper varHelper){
		if(fixed==false){
			//Evaluate any nested variables first
			String text=expression;
			int len=text.length();
			while(len>0){
				int index=varHelper.getNextIndex(text);
				String word=text.substring(0, index);
				int varIndex=varHelper.getVariableIndex(word);
				if(varIndex!=-1){ //Evaluate nested variable
					TreeVariable var=varHelper.variables.get(varIndex);
					var.sample(varHelper);
				}
				if(index==len){len=0;} //End of word
				else{
					text=text.substring(index+1);
					len=text.length();
				}
			}
			//Sample this variable
			value=varHelper.sampleExpression(expression);
			fixed=true;
		}
	}
}