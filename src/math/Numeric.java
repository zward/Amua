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

enum Format{INTEGER,DOUBLE,BOOL,MATRIX};
public class Numeric{
	Format format;
	int intNum;
	double doubleNum;
	boolean bool;
	
	public int nrow;
	public int ncol;
	public double matrix[][];
	
	public Numeric(int num){
		format=Format.INTEGER;
		intNum=num;
	}
	
	public Numeric(double num){
		format=Format.DOUBLE;
		doubleNum=num;
	}
	
	public Numeric(boolean val){
		format=Format.BOOL;
		bool=val;
	}
	
	public Numeric(int nrow, int ncol){
		format=Format.MATRIX;
		this.nrow=nrow;
		this.ncol=ncol;
		matrix=new double[nrow][ncol];
	}
	
	public Numeric(double data[][]){
		nrow=data.length;
		ncol=data[0].length;
		if(nrow>1 || ncol>1){
			format=Format.MATRIX;
			matrix=new double[nrow][ncol];
			for(int i=0; i<nrow; i++){
				for(int j=0; j<ncol; j++){
					matrix[i][j]=data[i][j];
				}
			}
		}
		else{ //scalar
			format=Format.DOUBLE;
			doubleNum=data[0][0];
		}
	}
	
	public Numeric(String expr){ //convert string expression to Numeric
		int index=expr.indexOf(";");
		String curFormat=expr.substring(0,index);
		format=Format.valueOf(curFormat);
		String str=expr.substring(index+1);
		if(format==Format.INTEGER){intNum=Integer.parseInt(str);}
		else if(format==Format.DOUBLE){doubleNum=Double.parseDouble(str);}
		else if(format==Format.BOOL){bool=Boolean.parseBoolean(str);}
		else if(format==Format.MATRIX){ //matrix
			index=str.indexOf(";");
			nrow=Integer.parseInt(str.substring(0, index));
			str=str.substring(index+1);
			index=str.indexOf(";");
			ncol=Integer.parseInt(str.substring(0, index));
			str=str.substring(index+1);
			String rows[]=str.split(";");
			matrix=new double[nrow][ncol];
			for(int i=0; i<nrow; i++){
				String curData[]=rows[i].split(",");
				for(int j=0; j<ncol; j++){
					matrix[i][j]=Double.parseDouble(curData[j]);
				}
			}
		}
	}
	
	public boolean isDouble(){
		if(format==Format.DOUBLE){return(true);}
		else{return(false);}
	}
	
	public boolean isInteger(){
		if(format==Format.INTEGER){return(true);}
		else{return(false);}
	}
	
	public boolean isBoolean(){
		if(format==Format.BOOL){return(true);}
		else{return(false);}
	}
	
	public boolean isMatrix(){
		if(format==Format.MATRIX){return(true);}
		else{return(false);}
	}
	
	public int getInt() throws NumericException{
		if(format==Format.INTEGER){return(intNum);}
		else{
			throw(new NumericException("Invalid integer: "+toString(),"Numeric"));
		}
	}
	
	public double getDouble() throws NumericException{
		if(format==Format.DOUBLE){return(doubleNum);}
		else if(format==Format.INTEGER){return(intNum);}
		else{
			throw(new NumericException("Matrix type, not real number","Numeric"));
		}
	}
	
	public double getProb() throws NumericException{
		double prob=doubleNum;
		if(format==Format.INTEGER){prob=intNum;}
		if(prob<0 || prob>1){
			throw(new NumericException("Invalid probability: "+prob,"Numeric"));
		}
		else{
			return(prob);
		}
	}
	
	public Numeric getMatrixValue(String args[], AmuaModel myModel) throws Exception{
		double subMatrix[][];
		if(args.length==1){ //vector
			if(nrow==1 || ncol==1){ //is vector
				String index=args[0];
				if(index.contains(":")){ //Multiple indices
					int pos=index.indexOf(":");
					int startIndex=Interpreter.evaluate(index.substring(0, pos),myModel,false).getInt();
					int endIndex=Interpreter.evaluate(index.substring(pos+1),myModel,false).getInt();
					if(nrow==1){ //row vector
						subMatrix=new double[1][endIndex-startIndex+1];
						for(int i=startIndex; i<=endIndex; i++){
							subMatrix[0][i-startIndex]=matrix[0][i];
						}
					}
					else{ //column vector
						subMatrix=new double[endIndex-startIndex+1][1];
						for(int i=startIndex; i<=endIndex; i++){
							subMatrix[i-startIndex][0]=matrix[i][0];
						}
					}
					return(new Numeric(subMatrix));
				}
				else{ //one index
					int curIndex=Interpreter.evaluate(index,myModel,false).getInt();
					if(curIndex<0){throw new NumericException("Invalid index: -1","Numeric.getMatrixValue()");}
					if(nrow==1){ //row vector
						return(new Numeric(matrix[0][curIndex]));
					}
					else{ //column vector
						return(new Numeric(matrix[curIndex][0]));
					}
				}
			}
			else{ //not vector
				throw new NumericException("Not a vector","Numeric.getMatrixValue()");
			}
		}
		else if(args.length==2){ //matrix
			String row=args[0], col=args[1];
			int startRow, endRow;
			int startCol, endCol;
			if(row.contains(":")){ //Multiple rows
				if(row.length()==1){ //":" all rows
					startRow=0; endRow=nrow-1;
				}
				else{ //"x:y" Sequence of rows
					int index=row.indexOf(":");
					startRow=Interpreter.evaluate(row.substring(0, index),myModel,false).getInt();
					endRow=Interpreter.evaluate(row.substring(index+1),myModel,false).getInt();
				}
			}
			else{
				startRow=Interpreter.evaluate(row,myModel,false).getInt();
				endRow=startRow;
			}

			if(col.contains(":")){ //Multiple cols
				if(col.length()==1){ //":" all cols
					startCol=0; endCol=ncol-1;
				}
				else{ //"x:y" Sequence of cols
					int index=col.indexOf(":");
					startCol=Interpreter.evaluate(col.substring(0, index),myModel,false).getInt();
					endCol=Interpreter.evaluate(col.substring(index+1),myModel,false).getInt();
				}
			}
			else{
				startCol=Interpreter.evaluate(col,myModel,false).getInt();
				endCol=startCol;
			}

			subMatrix=new double[endRow-startRow+1][endCol-startCol+1];
			for(int i=startRow; i<=endRow; i++){
				for(int j=startCol; j<=endCol; j++){
					subMatrix[i-startRow][j-startCol]=matrix[i][j];
				}
			}
			return(new Numeric(subMatrix));
		}
		else{
			//throw error
			return(null);
		}
	}
	
	public boolean getBool() throws NumericException{
		if(format==Format.BOOL){return(bool);}
		else{
			throw new NumericException("Not a Boolean type","Numeric");
		}
	}
	
	/**
	 * Returns double/int values, casts boolean to indicator variable {0,1}, and returns NaN for matrix
	 * @return double value
	 */
	public double getValue(){
		double value=0;
		if(format==Format.DOUBLE){value=doubleNum;}
		else if(format==Format.INTEGER){value=intNum;}
		else if(format==Format.BOOL){ //indicator
			if(bool==true){value=1.0;}
			else{value=0;}
		}
		else{value=Double.NaN;} //matrix
		return(value);
	}
	
	public void setInt(int newInt){
		format=Format.INTEGER;
		intNum=newInt;
	}
	
	public void setDouble(double newDouble){
		format=Format.DOUBLE;
		doubleNum=newDouble;
	}
	
	public String saveAsString(){
		String str="";
		str+=format+";";
		if(format==Format.INTEGER){str+=intNum+"";}
		else if(format==Format.DOUBLE){str+=doubleNum+"";}
		else if(format==Format.BOOL){str+=bool+"";}
		else{ //matrix
			str+=nrow+";"+ncol+";";
			for(int i=0; i<nrow; i++){ //rows
				for(int j=0; j<ncol-1; j++){str+=matrix[i][j]+",";}
				str+=matrix[i][ncol-1]+";";
			}
		}
		return(str);
	}
	
	public String toString(){
		String str=null;
		if(format==Format.INTEGER){str=intNum+"";}
		else if(format==Format.DOUBLE){str=roundDouble(doubleNum,6)+"";}
		else if(format==Format.BOOL){str=bool+"";}
		else{ //matrix
			str="";
			//Headers
			for(int i=0; i<ncol; i++){str+="\t["+i+"]";} str+="\n";
			for(int i=0; i<nrow; i++){ //rows
				str+="["+i+"]";
				for(int j=0; j<ncol; j++){str+="\t"+roundDouble(matrix[i][j],6);}
				str+="\n";
			}
		}
		return(str);
	}
	
	public Numeric copy(){
		Numeric copy=null;
		if(format==Format.INTEGER){copy=new Numeric(intNum);}
		else if(format==Format.DOUBLE){copy=new Numeric(doubleNum);}
		else if(format==Format.BOOL){copy=new Numeric(bool);}
		else{ //matrix
			copy=new Numeric(nrow,ncol);
			for(int i=0; i<nrow; i++){
				for(int j=0; j<ncol; j++){
					copy.matrix[i][j]=matrix[i][j];
				}
			}
		}
		
		return(copy);
	}
	
	/**
	 * Flip sign
	 */
	public void negate(){
		if(format==Format.INTEGER){intNum=-intNum;}
		else if(format==Format.DOUBLE){doubleNum=-doubleNum;}
		else if(format==Format.BOOL){bool=!bool;}
		else{ //matrix
			for(int i=0; i<nrow; i++){
				for(int j=0; j<ncol; j++){
					matrix[i][j]=-matrix[i][j];
				}
			}
		}
	}
	
	
	/**
	 * Checks if this numeric is equal to the 'test' numeric
	 * @param test
	 * @return
	 */
	public boolean isEqual(Numeric test){
		double tolerance=Math.pow(10, -6);
		boolean equal=true;
		if(format==Format.INTEGER){
			if(test.format==Format.INTEGER){
				if(test.intNum==intNum){return(true);}
				else{return(false);}
			}
			else{ //not same type
				return(false);
			}
		}
		else if(format==Format.DOUBLE){
			if(test.format==Format.DOUBLE){
				if(Math.abs(test.doubleNum-doubleNum)<=tolerance){return(true);}
				else{return(false);}
			}
			else{ //not same type
				return(false);
			}
		}
		else if(format==Format.BOOL){
			if(test.format==Format.BOOL){
				if(test.bool==bool){return(true);}
				else{return(false);}
			}
			else{ //not same type
				return(false);
			}
		}
		else if(format==Format.MATRIX){
			if(test.format==Format.MATRIX){
				if(test.ncol==ncol && test.nrow==nrow){ //same shape
					for(int i=0; i<nrow; i++){
						for(int j=0; j<ncol; j++){
							if(Math.abs(test.matrix[i][j]-matrix[i][j])>tolerance){	
								return(false);
							}
						}
					}
				}
				else{ //not same shape
					return(false);
				}
			}
			else{ //not same type
				return(false);
			}
		}
		
		return(equal);
	}
	
	private static double roundDouble(double num, int numDigits){
		double digits=Math.pow(10, numDigits);
		return((Math.round(num*digits)/digits));
	}
}