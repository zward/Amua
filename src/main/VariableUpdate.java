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

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;
import math.NumericException;
import math.Token;


public class VariableUpdate{
	public Variable variable;
	String exprUpdate; //expression to evaluate when updating
	Token exprTokens[];
	
	/**
	 * 0:=, 1:++, 2:--, 3:+=, 4:-=, 5:*=, 6:/=
	 */
	int operation;
	AmuaModel myModel;
	public Numeric testVal;
	
	//Constructor
	public VariableUpdate(String expression, AmuaModel curModel) throws Exception{
		this.myModel=curModel;
		parse(expression);
	}

	public void parse(String expression) throws Exception{
		String curExpr=expression.replace(" ",""); //remove whitespace
		//find variable
		int pos=-1; boolean endWord=false;
		int len=curExpr.length();
		while(endWord==false && pos<len-1){
			pos++;
			if(isOperator(curExpr.charAt(pos))){endWord=true;}
		}
		if(endWord==false){
			throw new NumericException(myModel.language.message.getString("err.no_operator_found")+": '"+expression+"'","VariableUpdate",myModel.language); //No operator found in expression
		}
		String strVar=curExpr.substring(0, pos);
		int varIndex=myModel.getVariableIndex(strVar);
		if(varIndex==-1){
			throw new NumericException(myModel.language.message.getString("err.var_not_found")+": "+strVar,"VariableUpdate",myModel.language); //Variable not found
		}
		variable=myModel.variables.get(varIndex);
		
		//get operator and expression
		operation=-1;
		char curChar=curExpr.charAt(pos);
		if(curChar=='='){ //equals
			operation=0;
			exprUpdate=curExpr.substring(pos+1,len);
		}
		else{ //2-character operator
			String assignOperator=curExpr.substring(pos, pos+2);
			if(assignOperator.equals("++")){operation=1;}
			else if(assignOperator.equals("--")){operation=2;}
			else if(assignOperator.equals("+=")){operation=3;}
			else if(assignOperator.equals("-=")){operation=4;}
			else if(assignOperator.equals("*=")){operation=5;}
			else if(assignOperator.equals("/=")){operation=6;}
			else{ //unrecognized
				throw new NumericException(myModel.language.message.getString("err.invalid_operator")+": "+assignOperator,"VariableUpdate",myModel.language); //Invalid operator
			}
			if(operation==1 || operation==2){// ++ or --
				if(len>pos+2){
					throw new NumericException(myModel.language.message.getString("err.invalid_expression")+": "+expression,"VariableUpdate",myModel.language); //Invalid expression
				}
				exprUpdate="0";
			}
			else{
				exprUpdate=curExpr.substring(pos+2,len);
			}
		}
		//validate expression
		exprTokens=Interpreter.parse(exprUpdate, myModel,myModel.language);
		testVal=Interpreter.evaluateTokens(exprTokens, 0, false, myModel.language);
	}
			
	private static boolean isOperator(char ch){ //operators: =, +, -, *, /
		return(ch=='=' || ch=='+' || ch=='-' || ch=='*' || ch=='/');
	}
	
	public void update(boolean sample, int curThread) throws Exception{
		variable.locked[curThread]=true;
		variable.unlockDependents(curThread);
		Numeric value=variable.value[curThread];
		if(operation==1){ //++
			if(value.isInteger()){value.setInt(value.getInt(myModel.language)+1);}
			else{value.setDouble(value.getDouble(myModel.language)+1);}
		} 
		else if(operation==2){ //--
			if(value.isInteger()){value.setInt(value.getInt(myModel.language)-1);}
			else{value.setDouble(value.getDouble(myModel.language)-1);}
		} 
		else{
			Numeric eval=Interpreter.evaluateTokens(exprTokens, curThread,sample, myModel.language);
			if(operation==0){
				if(eval.isInteger()){value.setInt(eval.getInt(myModel.language));}
				else{value.setDouble(eval.getDouble(myModel.language));}
			}
			else{
				if(operation<6){ //not division
					if(value.isInteger() && eval.isInteger()){ //preseve integer type
						int curVal=value.getInt(myModel.language);
						if(operation==3){curVal+=eval.getInt(myModel.language);}
						else if(operation==4){curVal-=eval.getInt(myModel.language);}
						else if(operation==5){curVal*=eval.getInt(myModel.language);}
						value.setInt(curVal);
					}
					else{ //treat all as double
						double curVal=value.getDouble(myModel.language);
						if(operation==3){curVal+=eval.getDouble(myModel.language);}
						else if(operation==4){curVal-=eval.getDouble(myModel.language);}
						else if(operation==5){curVal*=eval.getDouble(myModel.language);}
						value.setDouble(curVal);
					}
				}
				else if(operation==6){ //division, treat all as double
					double curVal=value.getDouble(myModel.language);
					curVal/=eval.getDouble(myModel.language);
					value.setDouble(curVal);
				}
			}
		}
	}
}