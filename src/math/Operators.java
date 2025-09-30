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

import lang.Language;

public final class Operators{

	public static boolean isOperator(String text){
		switch(text){
		case "+":return(true);
		case "-": return(true);
		case "/": return(true);
		case "*": return(true);
		case "^": return(true);
		case "%": return(true);
		//Logical
		case "==": return(true);
		case "<": return(true);
		case ">": return(true);
		case "<=": return(true);
		case ">=": return(true);
		case "&": return(true);
		case "|": return(true);
		case "!=": return(true);
		case "^|": return(true);
		}
		return(false); //fell through
	}
	
	//Ordr of operations: 0-n
	public static int getPrecedence(String operator){
		switch(operator){
		case "+":return(2);
		case "-": return(2);
		case "/": return(3);
		case "*": return(3);
		case "^": return(4);
		case "%": return(3);
		//Logical
		case "==": return(1); //after addition
		case "<": return(1); //after addition
		case ">": return(1);
		case "<=": return(1);
		case ">=": return(1);
		case "&": return(0);
		case "|": return(0);
		case "!=": return(1);
		case "^|": return(0);
		}
		return(-1); //fell through
	}

	public static Numeric evaluate(String operator,Numeric arg1, Numeric arg2, Language language) throws NumericException{
		switch(operator){
		case "+": return(add(arg1,arg2,language));
		case "-": return(subtract(arg1,arg2,language));
		case "*": return(multiply(arg1,arg2,language));
		case "/": return(divide(arg1,arg2,language));
		case "^": return(power(arg1,arg2, language));
		case "%": return(modulus(arg1,arg2,language));
		//Logical
		case "==": return(equal(arg1,arg2,language)); //after addition
		case "<": return(less(arg1,arg2,language)); //after addition
		case ">": return(greater(arg1,arg2,language));
		case "<=": return(lessEq(arg1,arg2,language));
		case ">=": return(greaterEq(arg1,arg2,language));
		case "&": return(and(arg1,arg2,language));
		case "|": return(or(arg1,arg2,language));
		case "!=": return(notEqual(arg1,arg2,language));
		case "^|": return(xor(arg1,arg2,language));
		}
		return(null); //fell through
	}

	private static Numeric add(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt(language)+arg2.getInt(language)));
			}
			else{
				return(new Numeric(arg1.getDouble(language)+arg2.getDouble(language)));
			}
		}
		else{
			return(MatrixFunctions.add(arg1, arg2, language));
		}
	}
	
	private static Numeric subtract(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt(language)-arg2.getInt(language)));
			}
			else{
				return(new Numeric(arg1.getDouble(language)-arg2.getDouble(language)));
			}
		}
		else{
			return(MatrixFunctions.subtract(arg1, arg2, language));
		}
	}
	
	private static Numeric multiply(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt(language)*arg2.getInt(language)));
			}
			else{
				return(new Numeric(arg1.getDouble(language)*arg2.getDouble(language)));
			}
		}
		else{
			return(MatrixFunctions.multiply(arg1, arg2, language));
		}
	}
	
	private static Numeric divide(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			Numeric result=new Numeric(arg1.getDouble(language)/arg2.getDouble(language));
			//check if integer
			int test=(int) Math.round(result.getDouble(language));
			if(Math.abs(test-result.getDouble(language))<MathUtils.tolerance){
				result=new Numeric(test); //covert to integer type
			}
			return(result);
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException(language.message.getString("err.cant_divide_by_matrix"), "/", language); //Can't divide by a matrix
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]/arg2.getDouble(language);
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException(language.message.getString("err.mat_div_undefined"),"/",language); //Matrix division is undefined
		}
		return(null);
	}
	
	private static Numeric power(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ 
				if(arg2.getInt(language)>=0){ //preserve integer
					Numeric result=new Numeric((int)(Math.pow(arg1.getInt(language),arg2.getInt(language))));
					return(result);
				}
				Numeric result=new Numeric(Math.pow(arg1.getDouble(language),arg2.getDouble(language)));
				return(result);
			}
			else{
				Numeric result=new Numeric(Math.pow(arg1.getDouble(language),arg2.getDouble(language)));
				return(result);
			}
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException(language.message.getString("err.cant_power_mat"), "^", language); //Can't raise a number to the power of a matrix, Exponentiation
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			if(arg2.format==Format.INTEGER){
				int n=arg2.getInt(language);
				if(n<1){
					throw new NumericException(language.message.getString("err.mat_pow_gt0"), "^", language); //Matrix can only be raised to an integer power >0, Exponentiation
				}
				Numeric matrix=new Numeric(arg1.matrix);
				for(int i=0; i<n-1; i++){
					matrix=multiply(matrix,arg1,language);
				}
				return(matrix);
			}
			else{
				throw new NumericException(language.message.getString("err.mat_pow_nonint")+" ("+arg2.toString()+")","^", language); //Can't raise a matrix to the power of a non-integer ("+arg2.toString()+"), Exponentiation
			}
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException(language.message.getString("err.mat_exp_undefined"), "^", language); //Matrix exponentiation is undefined, Exponentiation
		}
		return(null);
	}
	
	private static Numeric modulus(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer type
				Numeric result=new Numeric(arg1.getInt(language) % arg2.getInt(language));
				return(result);
			}
			else{ //at least one double
				Numeric result=new Numeric(arg1.getDouble(language) % arg2.getDouble(language));
				return(result);
			}
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException(language.message.getString("err.cant_divide_by_matrix"),"%",language); //Can't divide by a matrix, Modulus
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j] % arg2.getDouble(language);
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException(language.message.getString("err.cant_divide_by_matrix"), "%",language); //Can't divide by a matrix, Modulus
		}
		return(null);
	}
	
	private static Numeric equal(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) == arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),"==",language); //Not defined for matrix, Equal
		}
		//return(null);
	}
	
	private static Numeric notEqual(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) != arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),"!=",language); //Not defined for matrix, Not Equal
			//return(null);
		}
	}
	
	private static Numeric less(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) < arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),"<",language); //Not defined for matrix
			//return(null);
		}
	}
	
	private static Numeric greater(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) > arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),">",language); //Not defined for matrix
			//return(null);
		}
	}
	
	private static Numeric lessEq(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) <= arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),"<=",language); //Not defined for matrix
			//return(null);
		}
	}
	
	private static Numeric greaterEq(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble(language) >= arg2.getDouble(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.not_defined_mat"),">=",language); //Not defined for matrix
			//return(null);
		}
	}
	
	private static Numeric and(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool(language) && arg2.getBool(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.both_args_boolean"),"&",language); //Both arguments must be boolean
			//return(null);
		}
	}
	
	private static Numeric or(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool(language) || arg2.getBool(language)){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.both_args_boolean"),"|",language); //Both arguments must be boolean
			//return(null);
		}
	}
	
	private static Numeric xor(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			int trueCount=0;
			if(arg1.getBool(language)==true){trueCount++;}
			if(arg2.getBool(language)==true){trueCount++;}
			if(trueCount==1){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException(language.message.getString("err.both_args_boolean"),"^|",language); //Both arguments must be boolean
			//return(null);
		}
	}
	
	public static String getDescription(String text, Language language){
		String des="";
		switch(text){
		case "+":
			des="<html><b>"+language.math.getString("op.addition")+"</b><br><br>"; //Addition
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a + b")+": "+language.math.getString("op.add_real")+"<br><br>"; //Returns the sum of two real numbers
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("3 + 3 = 6")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.real_and_mat")+"</u><br>"; //Real Number and Matrix
			des+=MathUtils.consoleFont("a + <b>X</b>")+" or "+MathUtils.consoleFont("<b>X</b> + a")+": "+language.math.getString("op.add_real_mat")+"<br><br>"; //Returns the sum of a real number a and a matrix X
			des+="<i>"+language.base.getString("title.example")+":</i><br>"+MathUtils.consoleFont("2 + [3 , 2] = [5 , 4]")+"<br>"+MathUtils.consoleFont("[3 , 2] + 2 = [5 , 4]")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.matrices")+"</u><br>"; //Matrices
			des+=MathUtils.consoleFont("<b>A</b> + <b>B</b>")+": "+language.math.getString("op.add_mat")+"<br><br>"; //Returns the sum of two conformable matrices
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[1 , 2] + [3 , 4] = [4 , 6]")+"<br>"; //Example
			des+="</html>";
			return(des);
		case "-":
			des="<html><b>"+language.math.getString("op.subtraction")+"</b><br><br>"; //Subtraction
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a - b")+": "+language.math.getString("op.subtract_real")+"<br><br>"; //Returns the difference of two real numbers
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("3 - 3 = 0")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.real_and_mat")+"</u><br>"; //Real Number and Matrix
			des+=MathUtils.consoleFont("a - <b>X</b>")+" "+language.math.getString("op.or").toLowerCase(language.locale)+" "+MathUtils.consoleFont("<b>X</b> - a")+": "+language.math.getString("op.subtract_real_mat")+"<br><br>"; //Returns the difference of a real number a and a matrix X
			des+="<i>"+language.base.getString("title.example")+":</i><br>"+MathUtils.consoleFont("2 - [3 , 2] = [-1 , 0]")+"<br>"+MathUtils.consoleFont("[3 , 2] - 2 = [1 , 0]")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.matrices")+"</u><br>"; //Matrices
			des+=MathUtils.consoleFont("<b>A</b> - <b>B</b>")+": "+language.math.getString("op.subtract_mat")+"<br><br>"; //Returns the difference of two conformable matrices
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[1 , 2] - [3 , 4] = [-2 , -2]")+"<br>"; //Example
			des+="</html>";
			return(des);
		case "*": 
			des="<html><b>"+language.math.getString("op.multiplication")+"</b><br><br>"; //Multiplication
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a * b")+": "+language.math.getString("op.mult_real")+"<br><br>"; //Returns the product of two real numbers
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("3 * 3 = 9")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.real_and_mat")+"</u><br>"; //Real Number and Matrix
			des+=MathUtils.consoleFont("a * <b>X</b>")+" "+language.math.getString("op.or").toLowerCase(language.locale)+" "+MathUtils.consoleFont("<b>X</b> * a")+": "+language.math.getString("op.mult_real_mat")+"<br><br>"; //Returns the product of a real number a and a matrix X
			des+="<i>"+language.base.getString("title.example")+":</i><br>"+MathUtils.consoleFont("2 * [3 , 2] = [6 , 4]")+"<br>"+MathUtils.consoleFont("[3 , 2] * 2 = [6 , 4]")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.matrices")+"</u><br>"; //Matrices
			des+=MathUtils.consoleFont("<b>A</b> * <b>B</b>")+": "+language.math.getString("op.mult_mat")+"<br><br>"; //Returns the product of two conformable matrices
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[3 , 2] * ")+MathUtils.consoleFont("<b>tp</b>","#800000")+MathUtils.consoleFont("([1 , 4]) = 11")+"<br>"; //Example
			des+="</html>";
			return(des);
		case "/":
			des="<html><b>"+language.math.getString("op.division")+"</b><br><br>"; //Division
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a / b")+": "+language.math.getString("op.divide_real")+"<br><br>"; //Returns the quotient of two real numbers
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("3 / 3 = 1")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.mat_and_real")+"</u><br>"; //Matrix and Real Number
			des+=MathUtils.consoleFont("<b>X</b> / a")+": "+language.math.getString("op.divide_mat")+"<br><br>"; //Returns a matrix X divided by a real number a
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[6 , 4] / 2 = [3 , 2]")+"<br>"; //Example
			des+="</html>";
			return(des);
		case "^":
			des="<html><b>"+language.math.getString("op.exponentiation")+"</b><br><br>"; //Exponentiation
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a ^ b")+": "+language.math.getString("op.exp_real")+"<br><br>"; //Returns a raised to the power of b
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("3 ^ 3 = 27")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.mat_pos_int")+"</u><br>"; //Matrix and Positive Integer
			des+=MathUtils.consoleFont("<b>X</b> ^ n")+": "+language.math.getString("op.exp_mat")+"<br><br>"; //Returns a matrix X multiplied by itself n times 
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[[0.3 , 0.7] , [0.1 , 0.9]] ^ 10 = [[0.125 , 0.875] , [0.125 , 0.875]]")+"<br>"; //Example
			des+="</html>";
			return(des);
		case "%":
			des="<html><b>"+language.math.getString("op.modulus")+"</b><br><br>"; //Modulus
			des+="<u>"+language.math.getString("op.real_nums")+"</u><br>"; //Real Numbers
			des+=MathUtils.consoleFont("a % b")+": "+language.math.getString("op.mod_real")+"<br><br>"; //Returns the remainder of a divided by b
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("6 % 3 = 0")+"<br><br>"; //Example
			des+="<u>"+language.math.getString("op.mat_and_real")+"</u><br>"; //Matrix and Real Number
			des+=MathUtils.consoleFont("<b>X</b> % a")+": "+language.math.getString("op.mod_mat")+"<br><br>"; //Returns a matrix of remainders of X divided by a
			des+="<i>"+language.base.getString("title.example")+":</i> "+MathUtils.consoleFont("[8 , 6] % 4 = [0 , 2]")+"<br>"; //Example
			des+="</html>";
			return(des);
		//Logical
		case "==":
			des="<html><b>"+language.math.getString("op.equal")+"</b><br>"; //Equal
			des+=MathUtils.consoleFont("a == b")+": "+language.math.getString("op.equal_desc")+"<br><br>"; //Returns true if two values are equal, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 == 3")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("true")+"<br>";//Example, returns
			des+="</html>";
			return(des);
		case "!=":
			des="<html><b>"+language.math.getString("op.not_equal")+"</b><br>"; //Not Equal
			des+=MathUtils.consoleFont("a != b")+": "+language.math.getString("op.not_equal_desc")+"<br><br>"; //Returns true if two values are not equal, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 != 3")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("false")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case "<":
			des="<html><b>"+language.math.getString("op.less_than")+"</b><br>"; //Less Than
			des+=MathUtils.consoleFont("a &lt b")+": "+language.math.getString("op.lt_desc")+"<br><br>"; //Returns true if a is less than b, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 &lt 4")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("true")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case ">":
			des="<html><b>"+language.math.getString("op.greater_than")+"</b><br>"; //Greater Than
			des+=MathUtils.consoleFont("a &gt b")+": "+language.math.getString("op.gt_desc")+"<br><br>"; //Returns true if a is greather than b, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 &gt 4")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("false")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case "<=":
			des="<html><b>"+language.math.getString("op.lte")+"</b><br>"; //Less Than Or Equal
			des+=MathUtils.consoleFont("a &lt= b")+": "+language.math.getString("op.lte_desc")+"<br><br>"; //Returns true if a is less than or equal to b, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 &lt= 3")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("true")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case ">=":
			des="<html><b>"+language.math.getString("op.gte")+"</b><br>"; //Greater Than Or Equal
			des+=MathUtils.consoleFont("a >= b")+": "+language.math.getString("op.gte_desc")+"<br><br>"; //Returns true if a is greater than or equal to b, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("3 >= 3")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("true")+"<br>"; //Example, true
			des+="</html>";
			return(des);
		case "&":
			des="<html><b>"+language.math.getString("op.and")+"</b><br>"; //And
			des+=MathUtils.consoleFont("<i>expr1</i> & <i>expr2</i>")+": "+language.math.getString("op.and_desc")+"<br><br>"; //Returns true if both expressions are true, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("(3 > 4) & (3 > 2)")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("false")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case "|":
			des="<html><b>"+language.math.getString("op.or")+"</b><br>"; //Or
			des+=MathUtils.consoleFont("<i>expr1</i> | <i>expr2</i>")+": "+language.math.getString("op.or_desc")+"<br><br>"; //Returns true if at least one expression is true, and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("(3 > 4) | (3 > 2)")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("true")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		case "^|":
			des="<html><b>"+language.math.getString("op.xor")+"</b><br>"; //Exclusive Or
			des+=MathUtils.consoleFont("<i>expr1</i> ^| <i>expr2</i>")+": "+language.math.getString("op.xor_desc")+"<br><br>"; //Returns true if only one expression is true (i.e., not both), and false otherwise
			des+="<i>"+language.base.getString("title.example")+"</i><br>"+MathUtils.consoleFont("(2 > 1) ^| (3 > 2)")+" "+language.math.getString("op.returns")+" "+MathUtils.consoleFont("false")+"<br>"; //Example, returns
			des+="</html>";
			return(des);
		}
		return(des); //fell through
	}
}