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

	public static Numeric evaluate(String operator,Numeric arg1, Numeric arg2) throws NumericException{
		switch(operator){
		case "+": return(add(arg1,arg2));
		case "-": return(subtract(arg1,arg2));
		case "*": return(multiply(arg1,arg2));
		case "/": return(divide(arg1,arg2));
		case "^": return(power(arg1,arg2));
		case "%": return(modulus(arg1,arg2));
		//Logical
		case "==": return(equal(arg1,arg2)); //after addition
		case "<": return(less(arg1,arg2)); //after addition
		case ">": return(greater(arg1,arg2));
		case "<=": return(lessEq(arg1,arg2));
		case ">=": return(greaterEq(arg1,arg2));
		case "&": return(and(arg1,arg2));
		case "|": return(or(arg1,arg2));
		case "!=": return(notEqual(arg1,arg2));
		case "^|": return(xor(arg1,arg2));
		}
		return(null); //fell through
	}

	private static Numeric add(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt()+arg2.getInt()));
			}
			else{
				return(new Numeric(arg1.getDouble()+arg2.getDouble()));
			}
		}
		else{
			return(MatrixFunctions.add(arg1, arg2));
		}
	}
	
	private static Numeric subtract(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt()-arg2.getInt()));
			}
			else{
				return(new Numeric(arg1.getDouble()-arg2.getDouble()));
			}
		}
		else{
			return(MatrixFunctions.subtract(arg1, arg2));
		}
	}
	
	private static Numeric multiply(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer
				return(new Numeric(arg1.getInt()*arg2.getInt()));
			}
			else{
				return(new Numeric(arg1.getDouble()*arg2.getDouble()));
			}
		}
		else{
			return(MatrixFunctions.multiply(arg1, arg2));
		}
	}
	
	private static Numeric divide(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			Numeric result=new Numeric(arg1.getDouble()/arg2.getDouble());
			//check if integer
			int test=(int) Math.round(result.getDouble());
			if(Math.abs(test-result.getDouble())<MathUtils.tolerance){
				result=new Numeric(test); //covert to integer type
			}
			return(result);
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException("Can't divide by a matrix","Division");
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]/arg2.getDouble();
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException("Matrix division is undefined","Division");
		}
		return(null);
	}
	
	private static Numeric power(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ 
				if(arg2.getInt()>=0){ //preserve integer
					Numeric result=new Numeric((int)(Math.pow(arg1.getInt(),arg2.getInt())));
					return(result);
				}
				Numeric result=new Numeric(Math.pow(arg1.getDouble(),arg2.getDouble()));
				return(result);
			}
			else{
				Numeric result=new Numeric(Math.pow(arg1.getDouble(),arg2.getDouble()));
				return(result);
			}
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException("Can't raise a number to the power of a matrix","Exponentiation");
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			if(arg2.format==Format.INTEGER){
				int n=arg2.getInt();
				if(n<1){
					throw new NumericException("Matrix can only be raised to an integer power >0","Exponentiation");
				}
				Numeric matrix=new Numeric(arg1.matrix);
				for(int i=0; i<n-1; i++){
					matrix=multiply(matrix,arg1);
				}
				return(matrix);
			}
			else{
				throw new NumericException("Can't raise a matrix to the power of a non-integer (Argument: "+arg2.toString()+")","Exponentiation");
			}
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException("Matrix exponentiation is undefined","Exponentiation");
		}
		return(null);
	}
	
	private static Numeric modulus(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){ //number + number
			if(arg1.format==Format.INTEGER && arg2.format==Format.INTEGER){ //preserve integer type
				Numeric result=new Numeric(arg1.getInt() % arg2.getInt());
				return(result);
			}
			else{ //at least one double
				Numeric result=new Numeric(arg1.getDouble() % arg2.getDouble());
				return(result);
			}
		}
		else if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			throw new NumericException("Can't divide by a matrix","Modulus");
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j] % arg2.getDouble();
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			throw new NumericException("Can't divide by a matrix","Modulus");
		}
		return(null);
	}
	
	private static Numeric equal(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() == arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Equal");
		}
		//return(null);
	}
	
	private static Numeric notEqual(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() != arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Not Equal");
			//return(null);
		}
	}
	
	private static Numeric less(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() < arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Less Than");
			//return(null);
		}
	}
	
	private static Numeric greater(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() > arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Greater Than");
			//return(null);
		}
	}
	
	private static Numeric lessEq(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() <= arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Less Than or Equal");
			//return(null);
		}
	}
	
	private static Numeric greaterEq(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() >= arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Not defined for matrix","Greater Than or Equal");
			//return(null);
		}
	}
	
	private static Numeric and(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool() && arg2.getBool()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Both arguments must be boolean","And");
			//return(null);
		}
	}
	
	private static Numeric or(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool() || arg2.getBool()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Both arguments must be boolean","Or");
			//return(null);
		}
	}
	
	private static Numeric xor(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			int trueCount=0;
			if(arg1.getBool()==true){trueCount++;}
			if(arg2.getBool()==true){trueCount++;}
			if(trueCount==1){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			throw new NumericException("Both arguments must be boolean","Exclusive Or");
			//return(null);
		}
	}
	
	public static String getDescription(String text){
		String des="";
		switch(text){
		case "+":
			des="<html><b>Addition</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a + b")+": Returns the sum of two real numbers<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("3 + 3 = 6")+"<br><br>";
			des+="<u>Real Number and Matrix</u><br>";
			des+=MathUtils.consoleFont("a + <b>X</b>")+" or "+MathUtils.consoleFont("<b>X</b> + a")+": Returns the sum of a real number "+MathUtils.consoleFont("a")+" and a matrix "+MathUtils.consoleFont("<b>X</b>")+"<br><br>";
			des+="<i>Example:</i><br>"+MathUtils.consoleFont("2 + [3 , 2] = [5 , 4]")+"<br>"+MathUtils.consoleFont("[3 , 2] + 2 = [5 , 4]")+"<br><br>";
			des+="<u>Matrices</u><br>";
			des+=MathUtils.consoleFont("<b>A</b> + <b>B</b>")+": Returns the sum of two conformable matrices<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[1 , 2] + [3 , 4] = [4 , 6]")+"<br>";
			des+="</html>";
			return(des);
		case "-":
			des="<html><b>Subtraction</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a - b")+": Returns the difference of two real numbers<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("3 - 3 = 0")+"<br><br>";
			des+="<u>Real Number and Matrix</u><br>";
			des+=MathUtils.consoleFont("a - <b>X</b>")+" or "+MathUtils.consoleFont("<b>X</b> - a")+": Returns the difference of a real number "+MathUtils.consoleFont("a")+" and a matrix "+MathUtils.consoleFont("<b>X</b>")+"<br><br>";
			des+="<i>Example:</i><br>"+MathUtils.consoleFont("2 - [3 , 2] = [-1 , 0]")+"<br>"+MathUtils.consoleFont("[3 , 2] - 2 = [1 , 0]")+"<br><br>";
			des+="<u>Matrices</u><br>";
			des+=MathUtils.consoleFont("<b>A</b> - <b>B</b>")+": Returns the difference of two conformable matrices<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[1 , 2] - [3 , 4] = [-2 , -2]")+"<br>";
			des+="</html>";
			return(des);
		case "*": 
			des="<html><b>Multiplication</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a * b")+": Returns the product of two real numbers<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("3 * 3 = 9")+"<br><br>";
			des+="<u>Real Number and Matrix</u><br>";
			des+=MathUtils.consoleFont("a * <b>X</b>")+" or "+MathUtils.consoleFont("<b> X </b>*a")+": Returns the product of a real number "+MathUtils.consoleFont("a")+" and a matrix "+MathUtils.consoleFont("<b>X</b>")+"<br><br>";
			des+="<i>Example:</i><br>"+MathUtils.consoleFont("2 * [3 , 2] = [6 , 4]")+"<br>"+MathUtils.consoleFont("[3 , 2] * 2 = [6 , 4]")+"<br><br>";
			des+="<u>Matrices</u><br>";
			des+=MathUtils.consoleFont("<b>A</b> * <b>B</b>")+": Returns the product of two conformable matrices<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[3 , 2] * ")+MathUtils.consoleFont("<b>tp</b>","#800000")+MathUtils.consoleFont("([1 , 4]) = 11")+"<br>";
			des+="</html>";
			return(des);
		case "/":
			des="<html><b>Division</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a / b")+": Returns the quotient of two real numbers<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("3 / 3 = 1")+"<br><br>";
			des+="<u>Matrix and Real Number</u><br>";
			des+=MathUtils.consoleFont("<b>X</b> / a")+": Returns a matrix "+MathUtils.consoleFont("<b>X</b>")+" divided by a real number "+MathUtils.consoleFont("a")+"<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[6 , 4] / 2 = [3 , 2]")+"<br>";
			des+="</html>";
			return(des);
		case "^":
			des="<html><b>Exponentiation</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a ^ b")+": Returns "+MathUtils.consoleFont("a")+" raised to the power of "+MathUtils.consoleFont("b")+"<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("3 ^ 3 = 27")+"<br><br>";
			des+="<u>Matrix and Positive Integer</u><br>";
			des+=MathUtils.consoleFont("<b>X</b> ^ n")+": Returns a matrix "+MathUtils.consoleFont("<b>X</b>")+" multiplied by itself "+MathUtils.consoleFont("n")+" times<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[[0.3 , 0.7] , [0.1 , 0.9]] ^ 10 = [[0.125 , 0.875] , [0.125 , 0.875]]")+"<br>";
			des+="</html>";
			return(des);
		case "%":
			des="<html><b>Modulus</b><br><br>";
			des+="<u>Real Numbers</u><br>";
			des+=MathUtils.consoleFont("a % b")+": Returns the remainder of "+MathUtils.consoleFont("a")+" divided by "+MathUtils.consoleFont("b")+"<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("6 % 3 = 0")+"<br><br>";
			des+="<u>Matrix and Real Number</u><br>";
			des+=MathUtils.consoleFont("<b>X</b> % a")+": Returns a matrix of remainders of "+MathUtils.consoleFont("<b>X</b>")+" divided by "+MathUtils.consoleFont("a")+"<br><br>";
			des+="<i>Example:</i> "+MathUtils.consoleFont("[8 , 6] % 4 = [0 , 2]")+"<br>";
			des+="</html>";
			return(des);
		//Logical
		case "==":
			des="<html><b>Equal</b><br>";
			des+=MathUtils.consoleFont("a == b")+": Returns "+MathUtils.consoleFont("true")+" if two values are equal, and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 == 3")+" returns "+MathUtils.consoleFont("true")+"<br>";
			des+="</html>";
			return(des);
		case "!=":
			des="<html><b>Not Equal</b><br>";
			des+=MathUtils.consoleFont("a != b")+": Returns "+MathUtils.consoleFont("true")+" if two values are not equal, and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 != 3")+" returns "+MathUtils.consoleFont("false")+"<br>";
			des+="</html>";
			return(des);
		case "<":
			des="<html><b>Less Than</b><br>";
			des+=MathUtils.consoleFont("a &lt b")+": Returns "+MathUtils.consoleFont("true")+" if "+MathUtils.consoleFont("a")+" is less than "+MathUtils.consoleFont("b")+", and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 &lt 4")+" returns "+MathUtils.consoleFont("true")+"<br>";
			des+="</html>";
			return(des);
		case ">":
			des="<html><b>Greater Than</b><br>";
			des+=MathUtils.consoleFont("a &gt b")+": Returns "+MathUtils.consoleFont("true")+" if "+MathUtils.consoleFont("a")+" is greater than "+MathUtils.consoleFont("b")+", and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 &gt 4")+" returns "+MathUtils.consoleFont("false")+"<br>";
			des+="</html>";
			return(des);
		case "<=":
			des="<html><b>Less Than Or Equal</b><br>";
			des+=MathUtils.consoleFont("a &lt= b")+": Returns "+MathUtils.consoleFont("true")+" if "+MathUtils.consoleFont("a")+" is less than or equal to "+MathUtils.consoleFont("b")+", and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 &lt= 3")+" returns "+MathUtils.consoleFont("true")+"<br>";
			des+="</html>";
			return(des);
		case ">=":
			des="<html><b>Greater Than Or Equal</b><br>";
			des+=MathUtils.consoleFont("a >= b")+": Returns "+MathUtils.consoleFont("true")+" if "+MathUtils.consoleFont("a")+" is greater than or equal to "+MathUtils.consoleFont("b")+", and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("3 >= 3")+" returns "+MathUtils.consoleFont("true")+"<br>";
			des+="</html>";
			return(des);
		case "&":
			des="<html><b>And</b><br>";
			des+=MathUtils.consoleFont("<i>expr1</i> & <i>expr2</i>")+": Returns "+MathUtils.consoleFont("true")+" if both expressions are true, and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("(3 > 4) & (3 > 2)")+" returns "+MathUtils.consoleFont("false")+"<br>";
			des+="</html>";
			return(des);
		case "|":
			des="<html><b>Or</b><br>";
			des+=MathUtils.consoleFont("<i>expr1</i> | <i>expr2</i>")+": Returns "+MathUtils.consoleFont("true")+" if at least one expression is true, and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("(3 > 4) | (3 > 2)")+" returns "+MathUtils.consoleFont("true")+"<br>";
			des+="</html>";
			return(des);
		case "^|":
			des="<html><b>Exclusive Or</b><br>";
			des+=MathUtils.consoleFont("<i>expr1</i> ^| <i>expr2</i>")+": Returns "+MathUtils.consoleFont("true")+" if only one expression is true (i.e. not both), and "+MathUtils.consoleFont("false")+" otherwise<br><br>";
			des+="<i>Example</i><br>"+MathUtils.consoleFont("(2 > 1) ^| (3 > 2)")+" returns "+MathUtils.consoleFont("false")+"<br>";
			des+="</html>";
			return(des);
		}
		return(des); //fell through
	}
}