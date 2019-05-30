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

//enum Type{NUMERIC,OPERATOR,PAREN_LEFT,PAREN_RIGHT;}
/**
 * @deprecated
 * @author zward
 *
 */
public class TokenOLD{
	Type type;
	String word;
	int precedence=-1; //operator precedence
	boolean leftAssociative; //operator association
	String args[];
	int tableType; //0=Lookup, 1=Distribution, 2=Matrix
	Numeric numeric;
	//pointers
	int tokenType; //0=Static number (no update), 1=Parameter, 2=Variable, 3=Matrix
	Parameter curParam;
	Variable curVar;
	Table curMatrix;
	int curThread;
	
	public TokenOLD(Numeric numeric){
		this.numeric=numeric;
		word=numeric+"";
		type=Type.NUMERIC;
	}
	
	public TokenOLD(String word, Type type, AmuaModel myModel, boolean sample, int curThread) throws Exception{ //numeric or operator
		this.word=word;
		this.type=type;
		this.curThread=curThread;
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
				numeric=new Numeric(test); tokenType=0;
			}
			catch(Exception exInt){ //not integer
				try{
					double test=Double.parseDouble(word);
					numeric=new Numeric(test); tokenType=0;
				}
				catch(NumberFormatException e){ //not an explicit number, could be constant, parameter, variable, or entire matrix
					boolean negate=false;
					if(word.charAt(0)=='-'){
						negate=true;
						word=word.substring(1); //trim unary -
					}
					if(Constants.isConstant(word)){
						numeric=new Numeric(Constants.evaluate(word)); tokenType=0;
					}
					else if(myModel!=null && myModel.isParameter(word)){
						int index=myModel.getParameterIndex(word);
						tokenType=1;
						curParam=myModel.parameters.get(index);
						if(curParam.locked==false){
							curParam.value=InterpreterOLD.evaluate(curParam.expression,myModel,sample, curThread);
							if(sample){curParam.locked=true;}
						}
						numeric=curParam.value.copy();
					}
					else if(myModel!=null && myModel.isVariable(word)){
						int index=myModel.getVariableIndex(word);
						tokenType=2;
						curVar=myModel.variables.get(index);
						if(curVar.value[curThread]==null){ //not initialized
							curVar.value[curThread]=InterpreterOLD.evaluate(curVar.expression, myModel, sample, curThread);
						}
						numeric=curVar.value[curThread].copy();
					}
					else if(myModel!=null && myModel.isInnateVariable(word)){
						int index=myModel.getInnateVariableIndex(word);
						tokenType=2;
						curVar=myModel.innateVariables.get(index);
						numeric=curVar.value[curThread].copy();
					}
					else if(myModel!=null && myModel.isTable(word) && myModel.getTableType(word).equals("Matrix")){
						int index=myModel.getTableIndex(word);
						tokenType=3;
						curMatrix=myModel.tables.get(index);
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
	
	public void updateValue(){
		if(tokenType!=0){ //Not static number
			if(tokenType==1){ //Parameter
				numeric=curParam.value.copy();
			}
			else if(tokenType==2){ //Variable
				numeric=curVar.value[curThread].copy();
			}
			else if(tokenType==3){ //Matrix
				numeric=new Numeric(curMatrix.data);
			}
		}
	}
	
}