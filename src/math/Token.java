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


package math;

import base.AmuaModel;
import main.Parameter;
import main.Table;
import main.Variable;

enum Type{NUMERIC,OPERATOR,PAREN_LEFT,PAREN_RIGHT;}
public class Token{
	Type type;
	String word;
	int precedence=-1; //operator precedence
	boolean leftAssociative; //operator association
	String args[];
	int tableType; //0=Lookup, 1=Distribution, 2=Matrix
	Numeric numeric;
	
	public Token(Numeric numeric){
		this.numeric=numeric;
		word=numeric+"";
		type=Type.NUMERIC;
	}
	
	public Token(String word, Type type, AmuaModel myModel, boolean sample) throws Exception{ //numeric or operator
		this.word=word;
		this.type=type;
		if(this.type==Type.OPERATOR){
			precedence=Operators.getPrecedence(word);
			leftAssociative=true;
			if(word.equals("^")){
				leftAssociative=false;
			}
		}
		else if(this.type==Type.NUMERIC){ //get numeric representation
			try{
				int test=Integer.parseInt(word);
				numeric=new Numeric(test);
			}
			catch(Exception exInt){ //not integer
				try{
					double test=Double.parseDouble(word);
					numeric=new Numeric(test);
				}
				catch(NumberFormatException e){ //not an explicit number, could be constant, parameter, variable, or entire matrix
					boolean negate=false;
					if(word.charAt(0)=='-'){
						negate=true;
						word=word.substring(1); //trim unary -
					}
					if(Constants.isConstant(word)){
						numeric=new Numeric(Constants.evaluate(word));
					}
					else if(myModel!=null && myModel.isParameter(word)){
						int index=myModel.getParameterIndex(word);
						Parameter curParam=myModel.parameters.get(index);
						if(curParam.locked==false){
							//if(sample){
								//myModel.curGenerator=myModel.generatorParam; //re-point RNG
								//myModel.curRand=myModel.curRandParam;
							//} 
							curParam.value=Interpreter.evaluate(curParam.expression,myModel,sample);
							if(sample){curParam.locked=true;}
						}
						numeric=curParam.value.copy();
					}
					else if(myModel!=null && myModel.isVariable(word)){
						int index=myModel.getVariableIndex(word);
						Variable curVar=myModel.variables.get(index);
						if(curVar.value==null){ //not initialized
							//if(sample){
								//myModel.curGenerator=myModel.generatorVar; //re-point RNG
								//myModel.curRand=myModel.curRandVar;
							//} 
							curVar.value=Interpreter.evaluate(curVar.initValue, myModel, sample);
						}
						numeric=curVar.value.copy();
					}
					else if(myModel!=null && myModel.isInnateVariable(word)){
						int index=myModel.getInnateVariableIndex(word);
						Variable curVar=myModel.innateVariables.get(index);
						numeric=curVar.value.copy();
					}
					else if(myModel!=null && myModel.isTable(word) && myModel.getTableType(word).equals("Matrix")){
						int index=myModel.getTableIndex(word);
						Table curMatrix=myModel.tables.get(index);
						numeric=new Numeric(curMatrix.data);
					}
					else{ //not understood, throw error
						//try evaluate
						throw new NumericException(word+" not recognized","Token");
					}
					if(negate){
						numeric.negate();
					}
				}
			
			}
		}
	}
	
	
}