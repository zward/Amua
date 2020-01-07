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


package math;

import base.AmuaModel;
import main.Parameter;
import main.Table;
import main.Variable;

enum Type{NUMERIC,OPERATOR,PAREN_LEFT,PAREN_RIGHT;}
enum ObjectType{NUMBER,PARAMETER,VARIABLE,MATRIX_STATIC,FUNCTION,MATRIX_FUNCTION,DISTRIBUTION,
	TABLE_DISTRIBUTION,TABLE_LOOKUP,MATRIX_ELEMENT,TRACE,PARAM_MATRIX,VAR_MATRIX,
	MATRIX_DYNAMIC};
public class Token{
	Type type;
	ObjectType objectType;
	String word;
	AmuaModel myModel;
	int numThreads;
	Numeric numeric[]; //thread-specific
		
	int precedence=-1; //operator precedence
	boolean leftAssociative; //operator association
	
	/**
	 * [Argument index][Tokens within argument]
	 */
	Token args[][];
	
	
	String strArgs[];
	
	boolean negate;
		
	/**
	 * -1: ~, 0: f, 1: F, 2: Q, 3: E, 4: V 
	 */
	int distFx;
	
	int tableType; //0=Lookup, 1=Distribution, 2=Matrix
	Numeric matrix;
	
	//pointers
	Parameter curParam;
	Variable curVar;
	Table curTable;
	
	//dynamic matrix
	int nrow, ncol;
	/**
	 * [Row][Col][Tokens]
	 */
	Token matrixTokens[][][];
	
	
	public Token(String word, Type type, AmuaModel myModel, boolean parseWord) throws Exception{
		this.word=word;
		this.type=type;
		this.myModel=myModel;
		if(myModel==null){this.numThreads=1;}
		else{this.numThreads=myModel.numThreads;}
		numeric=new Numeric[numThreads];
		negate=false;
		if(parseWord){
			parseWord();
		}
	}
	
	public Numeric[] getNumeric(){
		return(numeric);
	}
	
	private void parseWord() throws Exception{ //numeric or operator
		if(this.type==Type.OPERATOR){
			precedence=Operators.getPrecedence(word);
			leftAssociative=true;
			if(word.equals("^")){
				leftAssociative=false;
			}
		}
		else if(this.type==Type.NUMERIC){ //get numeric representation
			try{ //try parse as integer
				int test=Integer.parseInt(word);
				objectType=ObjectType.NUMBER;
				for(int t=0; t<numThreads; t++){
					numeric[t]=new Numeric(test); 
				}
			}
			catch(Exception exInt){ //not integer, try parse as double
				try{
					double test=Double.parseDouble(word);
					objectType=ObjectType.NUMBER;
					for(int t=0; t<numThreads; t++){
						numeric[t]=new Numeric(test); 
					}
				}
				catch(NumberFormatException e){ //not an explicit number, could be constant, parameter, variable, or entire matrix
					if(word.charAt(0)=='-'){
						negate=true;
						word=word.substring(1); //trim unary -
					}
					if(Constants.isConstant(word)){
						objectType=ObjectType.NUMBER;
						for(int t=0; t<numThreads; t++){
							numeric[t]=new Numeric(Constants.evaluate(word));
							if(negate){numeric[t].negate();}
						}
					}
					else if(myModel!=null && myModel.isParameter(word)){
						int index=myModel.getParameterIndex(word);
						objectType=ObjectType.PARAMETER;
						curParam=myModel.parameters.get(index);
					}
					else if(myModel!=null && myModel.isVariable(word)){
						int index=myModel.getVariableIndex(word);
						objectType=ObjectType.VARIABLE;
						curVar=myModel.variables.get(index);
					}
					else if(myModel!=null && myModel.isInnateVariable(word)){
						int index=myModel.getInnateVariableIndex(word);
						objectType=ObjectType.VARIABLE;
						curVar=myModel.innateVariables.get(index);
					}
					else if(myModel!=null && myModel.isTable(word) && myModel.getTableType(word).equals("Matrix")){
						int index=myModel.getTableIndex(word);
						objectType=ObjectType.MATRIX_STATIC;
						curTable=myModel.tables.get(index);
						for(int t=0; t<numThreads; t++){
							numeric[t]=new Numeric(curTable.data);
							if(negate){numeric[t].negate();}
						}
					}
					else{ //not understood, throw error
						//try evaluate
						throw new NumericException(word+" not recognized","Token");
					}
				}
			
			}
		}
	}
	
	public void updateValue(int curThread, boolean sample) throws Exception{
		if(objectType!=ObjectType.NUMBER && objectType!=ObjectType.MATRIX_STATIC){ //Not static number or matrix
			if(objectType==ObjectType.PARAMETER){ //Parameter
				if(curParam.locked==false){
					curParam.parsedTokens=Interpreter.parse(curParam.expression, myModel);
					curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens,curThread,sample);
					if(sample){curParam.locked=true;}
				}
				numeric[curThread]=curParam.value.copy();
			}
			else if(objectType==ObjectType.VARIABLE){ //Variable
				if(curVar.value[curThread]==null){ //not initialized
					curVar.parsedTokens=Interpreter.parse(curVar.expression, myModel);
					curVar.value[curThread]=Interpreter.evaluateTokens(curVar.parsedTokens,curThread,sample);
				}
				else if(curVar.locked[curThread]==false && curVar.independent==false) {
					curVar.value[curThread]=Interpreter.evaluateTokens(curVar.parsedTokens,curThread,sample);
					curVar.locked[curThread]=true;
				}
				numeric[curThread]=curVar.value[curThread].copy();
			}
			else if(objectType==ObjectType.FUNCTION){
				Numeric argsNumeric[]=evalArgs(curThread,sample);
				numeric[curThread]=Functions.evaluate(word, argsNumeric);
			}
			else if(objectType==ObjectType.MATRIX_FUNCTION){
				Numeric argsNumeric[]=evalArgs(curThread,sample);
				numeric[curThread]=MatrixFunctions.evaluate(word, argsNumeric);
			}
			else if(objectType==ObjectType.DISTRIBUTION){
				Numeric argsNumeric[]=evalArgs(curThread,sample);
				if(sample==false){
					numeric[curThread]=Distributions.evaluate(word,argsNumeric,distFx);
				}
				else{ //sample is true
					if(distFx!=-1){ //not a random variable
						numeric[curThread]=Distributions.evaluate(word,argsNumeric,distFx);
					}
					else{ //see if can sample
						if(myModel.curGenerator[curThread]!=null){ //RNG available
							double rand=myModel.curGenerator[curThread].nextDouble();
							numeric[curThread]=Distributions.sample(word,argsNumeric,rand,myModel.curGenerator[curThread]);
						}
						else{ //no RNG, shouldn't sample
							numeric[curThread]=Distributions.evaluate(word,argsNumeric,distFx);
						}
					}
				}
			}
			else if(objectType==ObjectType.TABLE_DISTRIBUTION){
				if(sample==false){
					numeric[curThread]=curTable.evaluateDist(strArgs,distFx);
				}
				else{ //sample is true
					if(distFx!=-1){ //not a random variable
						numeric[curThread]=curTable.evaluateDist(strArgs,distFx);
					}
					else{ //see if can sample
						if(myModel.curGenerator[curThread]!=null){ //RNG available
							double rand=myModel.curGenerator[curThread].nextDouble();
							double val=curTable.sample(strArgs,rand);
							numeric[curThread]=new Numeric(val);
						}
						else{ //no RNG, shouldn't sample
							numeric[curThread]=curTable.evaluateDist(strArgs,distFx);
						}
					}
				}
			}
			else if(objectType==ObjectType.TABLE_LOOKUP){
				Numeric index=Interpreter.evaluateTokens(args[0],curThread,sample);
				numeric[curThread]=new Numeric(curTable.getLookupValue(index.getDouble(), strArgs[1]));
			}
			else if(objectType==ObjectType.MATRIX_ELEMENT){
				numeric[curThread]=matrix.getMatrixValue(strArgs, myModel);
			}
			else if(objectType==ObjectType.TRACE){
				numeric[curThread]=myModel.traceMarkov.getValue(strArgs[0],strArgs[1]);
			}
			else if(objectType==ObjectType.PARAM_MATRIX){
				if(curParam.locked==false){
					curParam.parsedTokens=Interpreter.parse(curParam.expression, myModel);
					curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens,curThread,sample);
					if(sample){curParam.locked=true;}
				}
				numeric[curThread]=curParam.value.getMatrixValue(strArgs,myModel);
			}
			else if(objectType==ObjectType.VAR_MATRIX){
				if(curVar.value==null){ //not initialized
					curVar.parsedTokens=Interpreter.parse(curVar.expression, myModel);
					curVar.value[curThread]=Interpreter.evaluateTokens(curVar.parsedTokens,curThread,sample);
				}
				else if(curVar.locked[curThread]==false && curVar.independent==false) {
					curVar.value[curThread]=Interpreter.evaluateTokens(curVar.parsedTokens,curThread,sample);
					curVar.locked[curThread]=true;
				}
				numeric[curThread]=curVar.value[curThread].getMatrixValue(strArgs,myModel);
			}
			else if(objectType==ObjectType.MATRIX_DYNAMIC){
				double matrix[][]=new double[nrow][ncol];
				for(int i=0; i<nrow; i++){
					for(int j=0; j<ncol; j++){
						matrix[i][j]=Interpreter.evaluateTokens(matrixTokens[i][j],curThread,sample).getDouble();
					}
				}
				numeric[curThread]=new Numeric(matrix);
			}
			
			if(negate){
				numeric[curThread].negate();
			}
		}
	}
	
	private Numeric[] evalArgs(int curThread, boolean sample) throws Exception{
		Numeric argsNumeric[]=new Numeric[args.length];
		for(int i=0; i<args.length; i++){
			argsNumeric[i]=Interpreter.evaluateTokens(args[i],curThread,sample);
		}
		return(argsNumeric);
	}
	
}