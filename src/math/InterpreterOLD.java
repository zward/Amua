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

import java.util.ArrayList;
import java.util.Stack;

import base.AmuaModel;
import main.Parameter;
import main.Table;
import main.Variable;
import math.Distributions;

/**
 * @deprecated
 * @author zward
 *
 */
public final class InterpreterOLD{

	//Single-thread, defaults to thread 0
	public static Numeric evaluate(String expression, AmuaModel myModel, boolean sample) throws Exception{
		return(evaluate(expression,myModel,sample,0));
	}
	
	//Thread-specific
	public static Numeric evaluate(String expression,AmuaModel myModel,boolean sample,int curThread) throws Exception{
		ArrayList<TokenOLD> tokens=tokenize(expression,myModel,sample,curThread);
		/*System.out.println("Tokens:");
		for(int i=0; i<tokens.size(); i++){
			System.out.print(tokens.get(i).word+" ");
		}*/
		ArrayList<TokenOLD> output=shuntTokens(tokens);
		/*System.out.println("\nOutput:");
		for(int i=0; i<output.size(); i++){
			System.out.print(output.get(i).word+" ");
		}
		System.out.println("\n");*/
		Numeric result=evaluateResult(output);
		return(result);
	}
	
	public static ArrayList<TokenOLD> parse(String expression,AmuaModel myModel, boolean sample, int curThread) throws Exception{
		ArrayList<TokenOLD> tokens=tokenize(expression,myModel,sample,curThread);
		ArrayList<TokenOLD> output=shuntTokens(tokens);
		return(output);
	}
	
	public static String[] splitArgs(String strArgs){
		ArrayList<Integer> indices=new ArrayList<Integer>();
		int parenLevel=0, bracketLevel=0;
		indices.add(-1);
		for(int i=0; i<strArgs.length(); i++){
			char ch=strArgs.charAt(i);
			if(ch=='('){parenLevel++;}
			else if(ch==')'){parenLevel--;}
			else if(ch=='['){bracketLevel++;}
			else if(ch==']'){bracketLevel--;}
			else if(ch==',' && parenLevel==0 && bracketLevel==0){indices.add(i);}
		}
		indices.add(strArgs.length());
		int numArgs=indices.size()-1;
		String args[]=new String[numArgs];
		for(int i=0; i<numArgs; i++){
			args[i]=strArgs.substring(indices.get(i)+1, indices.get(i+1));
		}
		return(args);
	}
	
	private static String[] splitRows(String strMatrix){
		ArrayList<Integer> indices=new ArrayList<Integer>();
		int bracketLevel=0;
		indices.add(-1);
		for(int i=0; i<strMatrix.length(); i++){
			char ch=strMatrix.charAt(i);
			if(ch=='['){bracketLevel++;}
			else if(ch==']'){bracketLevel--;}
			else if(ch==',' && bracketLevel==0){indices.add(i);}
		}
		indices.add(strMatrix.length());
		int numRows=indices.size()-1;
		String rows[]=new String[numRows];
		for(int i=0; i<numRows; i++){
			rows[i]=strMatrix.substring(indices.get(i)+2, indices.get(i+1)-1); //trim []
		}
		return(rows);
	}
	
	private static Numeric[] evalArgs(String strArgs, AmuaModel myModel, boolean sample, int curThread) throws Exception{
		String args[]=splitArgs(strArgs);
		Numeric argsNumeric[]=new Numeric[args.length];
		for(int i=0; i<args.length; i++){
			argsNumeric[i]=evaluate(args[i],myModel,sample,curThread);
		}
		return(argsNumeric);
	}
	
	public static Numeric parseMatrix(String strMatrix, AmuaModel myModel,boolean sample,int curThread) throws Exception{
		//Get dimensions
		String rows[];
		if(strMatrix.contains("[")){rows=splitRows(strMatrix);}
		else{rows=new String[]{strMatrix};}
		int nrow=rows.length;
		int ncol=rows[0].split(",").length;
		double matrix[][]=new double[nrow][ncol];
		for(int i=0; i<nrow; i++){
			String curRow=rows[i];
			Numeric entries[]=evalArgs(curRow,myModel,sample,curThread);
			if(entries.length!=ncol){
				//throw error
				return(null);
			}
			for(int j=0; j<ncol; j++){
				matrix[i][j]=entries[j].getDouble();
			}
		}
		return(new Numeric(matrix));
	}
	
	public static int findRightParen(String curExpr, int pos){
		int index=-1;
		boolean found=false;
		int parenCount=1;
		while(found==false){
			pos++;
			if(curExpr.charAt(pos)=='('){parenCount++;}
			else if(curExpr.charAt(pos)==')'){
				parenCount--;
				if(parenCount==0){
					index=pos;
					found=true;
				}
			}
		}
		return(index);
	}
	
	public static int findRightBracket(String curExpr, int pos){
		int index=-1;
		boolean found=false;
		int parenCount=1;
		while(found==false){
			pos++;
			if(curExpr.charAt(pos)=='['){parenCount++;}
			else if(curExpr.charAt(pos)==']'){
				parenCount--;
				if(parenCount==0){
					index=pos;
					found=true;
				}
			}
		}
		return(index);
	}
	
	private static ArrayList<TokenOLD> tokenize(String expression,AmuaModel myModel,boolean sample,int curThread) throws Exception{
		ArrayList<TokenOLD> tokens=new ArrayList<TokenOLD>();
		String curExpr=expression.replace(" ",""); //remove whitespace
		curExpr=curExpr.replace('−', '-'); //convert long dashes to short dashes
		while(curExpr.length()>0){
			int pos=0; boolean endWord=false;
			while(endWord==false){
				char curChar=curExpr.charAt(pos);
				if(isOpenParen(curChar)){
					if(pos>0){ //get preceding word
						String word=curExpr.substring(0,pos);
						int off=1;
						if(Functions.isFunction(word)){ //Function
							int close=findRightParen(curExpr,pos);
							String args=curExpr.substring(pos+1,close);
							Numeric argsNumeric[]=evalArgs(args,myModel,sample,curThread);
							Numeric fxResult=Functions.evaluate(word, argsNumeric);
							tokens.add(new TokenOLD(fxResult));
							off=(close+1)-pos;
						}
						else if(MatrixFunctions.isFunction(word)){ //Matrix Function
							int close=findRightParen(curExpr,pos);
							String args=curExpr.substring(pos+1,close);
							Numeric argsNumeric[]=evalArgs(args,myModel,sample,curThread);
							Numeric fxResult=MatrixFunctions.evaluate(word, argsNumeric);
							tokens.add(new TokenOLD(fxResult));
							off=(close+1)-pos;
						}
						else if(Distributions.isDistribution(word)){ //Distribution
							int close=findRightParen(curExpr,pos);
							String params[]=splitArgs(curExpr.substring(pos+1,close));
							int numParams=params.length;
							int df=-1;
							if(params[numParams-1].equals("~")){df=-1; numParams--;}
							else if(params[numParams-1].equals("f")){df=0; numParams--;}
							else if(params[numParams-1].equals("F")){df=1; numParams--;}
							else if(params[numParams-1].equals("Q")){df=2; numParams--;}
							else if(params[numParams-1].equals("E")){df=3; numParams--;}
							else if(params[numParams-1].equals("V")){df=4; numParams--;}
							else{throw new NumericException("Invalid parameter \""+params[numParams-1]+"\"",word);}
							
							Numeric paramsEval[]=new Numeric[numParams];
							for(int i=0; i<numParams; i++){paramsEval[i]=evaluate(params[i], myModel,sample,curThread);}
														
							Numeric distResult;
							if(sample==false){
								distResult=Distributions.evaluate(word,paramsEval,df);
							}
							else{ //sample is true
								if(df!=-1){ //not a random variable
									distResult=Distributions.evaluate(word,paramsEval,df);
								}
								else{ //see if can sample
									if(myModel.curGenerator[curThread]!=null){ //RNG available
										//double rand=myModel.curGenerator[curThread].nextDouble();
										//distResult=Distributions.sample(word,paramsEval,rand,myModel.curGenerator[curThread]);
										distResult=Distributions.sample(word,paramsEval,myModel.curGenerator[curThread]);
									}
									else{ //no RNG, shouldn't sample
										distResult=Distributions.evaluate(word,paramsEval,df);
									}
								}
							}
														
							tokens.add(new TokenOLD(distResult));
							off=(close+1)-pos;
						}
						else if(myModel!=null && myModel.isTable(word)){ //Table
							int tableIndex=myModel.getTableIndex(word);
							Table curTable=myModel.tables.get(tableIndex);
							String tableType=curTable.type;
							if(tableType.equals("Distribution")){
								int close=findRightParen(curExpr,pos);
								
								String params[]=splitArgs(curExpr.substring(pos+1,close));
								int numParams=params.length;
								int df=-1;
								if(params[numParams-1].equals("~")){df=-1; numParams--;}
								else if(params[numParams-1].equals("f")){df=0; numParams--;}
								else if(params[numParams-1].equals("F")){df=1; numParams--;}
								else if(params[numParams-1].equals("Q")){df=2; numParams--;}
								else if(params[numParams-1].equals("E")){df=3; numParams--;}
								else if(params[numParams-1].equals("V")){df=4; numParams--;}
								else{throw new NumericException("Invalid parameter \""+params[numParams-1]+"\"",word);}
								
								Numeric distResult;
								if(sample==false){
									distResult=curTable.evaluateDist(params,df);
								}
								else{ //sample is true
									if(df!=-1){ //not a random variable
										distResult=curTable.evaluateDist(params,df);
									}
									else{ //see if can sample
										if(myModel.curGenerator[curThread]!=null){ //RNG available
											double rand=myModel.curGenerator[curThread].nextDouble();
											double val=curTable.sample(params,rand);
											distResult=new Numeric(val);
										}
										else{ //no RNG, shouldn't sample
											distResult=curTable.evaluateDist(params,df);
										}
									}
								}
								
								tokens.add(new TokenOLD(distResult));
								off=(close+1)-pos;
							}
						}
						else{
							tokens.add(new TokenOLD(word,Type.NUMERIC,myModel,sample,curThread));
							tokens.add(new TokenOLD("(",Type.PAREN_LEFT,myModel,sample,curThread));
						}
						endWord=true;
						if(off<curExpr.length()-1){curExpr=curExpr.substring(pos+off);}
						else{curExpr="";} //end
						
					}
					else{
						tokens.add(new TokenOLD("(",Type.PAREN_LEFT,myModel,sample,curThread));
						endWord=true;
						curExpr=curExpr.substring(pos+1);
					}
				}
				else if(isOpenBracket(curChar)){
					if(pos>0){ //get preceding word
						String word=curExpr.substring(0,pos);
						boolean negate=false;
						if(word.charAt(0)=='-'){
							negate=true;
							word=word.substring(1); //trim unary -
						}
						int off=1;
						if(myModel!=null && myModel.isTable(word)){ //Table
							int tableIndex=myModel.getTableIndex(word);
							Table curTable=myModel.tables.get(tableIndex);
							String tableType=curTable.type;
							if(tableType.equals("Lookup")){
								int close=findRightBracket(curExpr,pos);
								String args[]=splitArgs(curExpr.substring(pos+1,close));
								Numeric index=evaluate(args[0],myModel,sample,curThread);
								Numeric val=new Numeric(curTable.getLookupValue(index.getDouble(), args[1]));
								if(negate){val.negate();}
								tokens.add(new TokenOLD(val));
								off=(close+1)-pos;
							}
							else if(tableType.equals("Matrix")){
								int close=findRightBracket(curExpr,pos);
								String args[]=splitArgs(curExpr.substring(pos+1,close));
								Numeric matrix=new Numeric(curTable.data);
								Numeric mat=matrix.getMatrixValue(args, myModel);
								if(negate){mat.negate();}
								tokens.add(new TokenOLD(mat));
								off=(close+1)-pos;
							}
						}
						else if(myModel!=null && word.equals("trace")){ //get trace value
							int close=findRightBracket(curExpr,pos);
							String args[]=splitArgs(curExpr.substring(pos+1,close));
							if(args.length!=2){
								throw new NumericException("Invalid trace arguments","trace");
								//throw error
							}
							Numeric trace=myModel.traceMarkov.getValue(args[0],args[1]);
							if(negate){trace.negate();}
							tokens.add(new TokenOLD(trace));
							off=(close+1)-pos;
						}
						else if(myModel!=null && myModel.isParameter(word)){ //Parameter matrix
							int paramIndex=myModel.getParameterIndex(word);
							Parameter curParam=myModel.parameters.get(paramIndex);
							if(curParam.locked==false){
								curParam.value=InterpreterOLD.evaluate(curParam.expression,myModel,sample,curThread);
								if(sample){curParam.locked=true;}
							}
							int close=findRightBracket(curExpr,pos);
							String args[]=splitArgs(curExpr.substring(pos+1,close));
							Numeric mat=curParam.value.getMatrixValue(args,myModel);
							if(negate){mat.negate();}
							tokens.add(new TokenOLD(mat));
							off=(close+1)-pos;
						}
						else if(myModel!=null && myModel.isVariable(word)){ //Variable matrix
							int varIndex=myModel.getVariableIndex(word);
							Variable curVar=myModel.variables.get(varIndex);
							if(curVar.value==null){ //not initialized
								curVar.value[curThread]=InterpreterOLD.evaluate(curVar.expression,myModel,sample,curThread);
							}
							int close=findRightBracket(curExpr,pos);
							String args[]=splitArgs(curExpr.substring(pos+1,close));
							Numeric mat=curVar.value[curThread].getMatrixValue(args,myModel);
							if(negate){mat.negate();}
							tokens.add(new TokenOLD(mat));
							off=(close+1)-pos;
						}
											
						endWord=true;
						if(off<curExpr.length()-1){curExpr=curExpr.substring(pos+off);}
						else{curExpr="";} //end
					}
					else{ //beginning of word, get matrix values
						int close=findRightBracket(curExpr,pos);
						String strMatrix=curExpr.substring(pos+1,close);
						Numeric matrix=parseMatrix(strMatrix,myModel,sample,curThread);
						tokens.add(new TokenOLD(matrix));
						curExpr=curExpr.substring(close+1);
						endWord=true;
					}
				}
				else if(isCloseParen(curChar)){
					if(pos>0){ //get preceding word
						String word=curExpr.substring(0, pos); //get preceding word
						tokens.add(new TokenOLD(word,Type.NUMERIC,myModel,sample,curThread));
					}
					tokens.add(new TokenOLD(")",Type.PAREN_RIGHT,myModel,sample,curThread));
					endWord=true;
					curExpr=curExpr.substring(pos+1);
				}
				else if(isOperator1(curChar)){
					if(pos>0){ //get preceding word
						String word=curExpr.substring(0, pos);
						tokens.add(new TokenOLD(word,Type.NUMERIC,myModel,sample,curThread));
						tokens.add(new TokenOLD(curExpr.substring(pos,pos+1),Type.OPERATOR,myModel,sample,curThread));
						endWord=true;
						curExpr=curExpr.substring(pos+1);
					}
					else{ //first char in string
						if(curExpr.charAt(0)!='-'){ //not '-'
							tokens.add(new TokenOLD(curExpr.substring(pos,pos+1),Type.OPERATOR,myModel,sample,curThread));
							endWord=true;
							curExpr=curExpr.substring(pos+1);
						}
						else{ //is '-', check if should be subtraction operator
							if(isOpenParen(curExpr.charAt(1)) || isOpenBracket(curExpr.charAt(1))){ //followed by ( or [, operator
								tokens.add(new TokenOLD(curExpr.substring(pos,pos+1),Type.OPERATOR,myModel,sample,curThread));
								endWord=true;
								curExpr=curExpr.substring(pos+1);
							}
							else{
								if(!tokens.isEmpty()){ //not first token
									TokenOLD prevToken=tokens.get(tokens.size()-1);
									if(prevToken.type==Type.PAREN_RIGHT || prevToken.type==Type.NUMERIC){ //operator
										tokens.add(new TokenOLD(curExpr.substring(pos,pos+1),Type.OPERATOR,myModel,sample,curThread));
										endWord=true;
										curExpr=curExpr.substring(pos+1);
									}
									else{ //add 0-
										/*tokens.add(new Token("0",Type.NUMERIC,myModel,sample));
										tokens.add(new Token("-",Type.OPERATOR,myModel,sample));
										endWord=true;
										curExpr=curExpr.substring(pos+1);*/
									}
								}
								else{ //first token, add 0-
									/*tokens.add(new Token("0",Type.NUMERIC,myModel,sample));
									tokens.add(new Token("-",Type.OPERATOR,myModel,sample));
									endWord=true;
									curExpr=curExpr.substring(pos+1);*/
								}
							}
							
						}
					}
				}
				else if(isOperator2_1(curChar)){
					if(pos>0){ //get preceding word
						String word=curExpr.substring(0, pos);
						tokens.add(new TokenOLD(word,Type.NUMERIC,myModel,sample,curThread));
					}
					int off=1;
					if(curExpr.length()>2 && isOperator2_2(curExpr.substring(pos,pos+2))){off=2;} //check double digit operator
					tokens.add(new TokenOLD(curExpr.substring(pos,pos+off),Type.OPERATOR,myModel,sample,curThread));
					endWord=true;
					curExpr=curExpr.substring(pos+off);
				}
				else if(pos==curExpr.length()-1){ //end of expression
					tokens.add(new TokenOLD(curExpr,Type.NUMERIC,myModel,sample,curThread));
					endWord=true;
					curExpr=""; //empty
				}
				
				pos++; //next character
				
			} //end while word
		} //end while expr
		return(tokens);
	}
	
	/**
	 * Shunting yard algorithm to parse expression into Reverse Polish notation
	 * @param expression
	 * @return
	 * @throws NumericException 
	 */
	private static ArrayList<TokenOLD> shuntTokens(ArrayList<TokenOLD> input) throws NumericException{
		ArrayList<TokenOLD> output=new ArrayList<TokenOLD>();
		Stack<TokenOLD> stack=new Stack<TokenOLD>();
		int numTokens=input.size();
		for(int i=0; i<numTokens; i++){
			TokenOLD curToken=input.get(i);
			if(curToken.type==Type.NUMERIC){
				output.add(curToken);
			}
			else if(curToken.type==Type.OPERATOR){
				if(!stack.isEmpty()){
					TokenOLD top=stack.peek();
					boolean empty=false;
					while(empty==false && top.type!=Type.PAREN_LEFT && (
							(top.type==Type.OPERATOR && top.precedence>curToken.precedence) || 
							(top.type==Type.OPERATOR && top.precedence==curToken.precedence && top.leftAssociative))
							){
						output.add(stack.pop());
						if(stack.isEmpty()){empty=true;}
						else{top=stack.peek();}
					}
				}
				stack.push(curToken);
			}
			else if(curToken.type==Type.PAREN_LEFT){
				stack.push(curToken);
			}
			else if(curToken.type==Type.PAREN_RIGHT){
				while(stack.peek().type!=Type.PAREN_LEFT){
					output.add(stack.pop());
					//if empty stack we have mismatched parens
					if(stack.isEmpty()){
						throw new NumericException("Mismatched parantheses","Calculator");
					}
				}
				stack.pop(); //remove left paren
			}
		}
		//Get remaining operators on stack
		while(stack.size()>0){
			if(stack.peek().type==Type.PAREN_RIGHT || stack.peek().type==Type.PAREN_LEFT){
				//mismatched
				throw new NumericException("Mismatched parantheses","Calculator");
			}
			else{
				output.add(stack.pop());
			}
		}
	
		return(output);
	}
	
	/**
	 * Evaluates post-fix expression from left to right
	 * @param tokens
	 * @return
	 * @throws NumericException 
	 */
	private static Numeric evaluateResult(ArrayList<TokenOLD> tokens) throws NumericException{
		int numTokens=tokens.size();
		Stack<Numeric> operands=new Stack<Numeric>();
		for(int i=0; i<numTokens; i++){
			TokenOLD curToken=tokens.get(i);
			if(curToken.type==Type.OPERATOR){
				String operator=curToken.word;
				Numeric operand_2=operands.pop();
				Numeric operand_1=operands.pop();
				Numeric result=Operators.evaluate(operator,operand_1,operand_2);
				operands.push(result);
			}
			else{ //operand
				Numeric operand=curToken.numeric;
				operands.push(operand);
			}
		} //end of tokens loop
		Numeric result=operands.pop();
				
		return(result);
	}
	
	private static boolean isOpenParen(char ch){
		return(ch=='('); 
	}
	
	private static boolean isOpenBracket(char ch){
		return(ch=='['); 
	}
	
	private static boolean isCloseParen(char ch){
		return(ch==')'); 
	}

	private static boolean isOperator1(char ch){ //single character operators: +,-,/,*,%,&,|
		return(ch=='+' || ch=='-' || ch=='/' || ch=='*' || ch=='%' || ch=='&' || ch=='|');
	}
	
	private static boolean isOperator2_1(char ch){ //double or potentially double character operators
		//^,^|,==,<,<=,>,>=,!,!=,
		return(ch=='^' || ch=='=' || ch=='<' || ch=='>' || ch=='!');
	}
	
	private static boolean isOperator2_2(String op){ //allowed doubles: ^|,==,<=,>=,!=
		return(op.equals("==") || op.equals("<=") || op.equals(">=") || op.equals("!=") || op.equals("^|"));
	}
	
	public static boolean isReservedString(String text){
		return(text.equals("c") || text.equals("C") || text.equals("t") || text.equals("trace") ||
				text.equals("x") || text.equals("y") || text.equals("~") || text.equals("f") || text.equals("F") || 
				text.equals("Q") || text.equals("E") || text.equals("V"));
	}
	
	public static boolean isBreak(char ch){
		return(ch==' ' || ch==',' || ch==';' || ch=='(' || ch==')' || ch=='[' || ch==']' || 
				isOperator1(ch) || isOperator2_1(ch));
	}

	public static int getNextBreakIndex(String text){
		boolean found=false;
		int i=0, len=text.length(), index=len;
		while(found==false && i<len){
			if(Interpreter.isBreak(text.charAt(i))){
				found=true;
				index=i;
			}
			i++;
		}
		return(index);
	}
	
	public static boolean containsWord(String curWord, String text){
		boolean contains=false;
		String curText=text;
		int len=curText.length();
		while(contains==false && len>0){
			int index=getNextBreakIndex(curText);
			String word=curText.substring(0, index);
			//See if matches current word
			if(word.matches(curWord)){contains=true;}
			if(index==len){len=0;} //End of word
			else{
				curText=curText.substring(index+1);
				len=curText.length();
			}
		}
		return(contains);
	}
	
}