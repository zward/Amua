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
			//throw error
			return(null);
		}
	}
	
	private static Numeric notEqual(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() != arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric less(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() < arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric greater(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() > arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric lessEq(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() <= arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric greaterEq(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format!=Format.MATRIX){
			if(arg1.getDouble() >= arg2.getDouble()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric and(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool() && arg2.getBool()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
		}
	}
	
	private static Numeric or(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format==Format.BOOL && arg2.format==Format.BOOL){
			if(arg1.getBool() || arg2.getBool()){return(new Numeric(true));}
			else{return(new Numeric(false));}
		}
		else{
			//throw error
			return(null);
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
			//throw error
			return(null);
		}
	}
}