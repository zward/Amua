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

import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public final class MatrixFunctions{

	public static boolean isFunction(String word){
		switch(word){
		case "chol": return(true);
		case "det": return(true);
		case "diag": return(true);
		case "iden": return(true);
		case "interpolate": return(true);
		case "inv": return(true);
		case "ncol": return(true);
		case "norm": return(true);
		case "nrow": return(true);
		case "renorm": return(true);
		case "rep": return(true);
		case "seq": return(true);
		case "softmax": return(true);
		case "stack": return(true);
		case "tp": return(true);
		case "tr": return(true);
		}
		return(false); //fell through
	}

	public static Numeric evaluate(String fx, Numeric...args) throws NumericException{
		switch(fx){
		case "chol":{ //Cholesky decomposition
			if(args.length!=1){throw new NumericException("Function takes 1 argument","chol");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","chol");}
			return(chol(args[0]));
		}
		case "det":{ //Determinant
			if(args.length!=1){throw new NumericException("Function takes 1 argument","det");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","det");}
			return(det(args[0]));
		}
		case "diag":{ //get diagonals
			if(args.length!=1){throw new NumericException("Function takes 1 argument","diag");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","diag");}
			Numeric matrix=args[0];
			if(matrix.nrow==matrix.ncol){ //square
				Numeric diag=new Numeric(1,matrix.nrow);
				for(int i=0; i<matrix.nrow; i++){
					diag.matrix[0][i]=matrix.matrix[i][i];
				}
				return(diag);
			}
			else{
				throw new NumericException("X should be a square matrix","diag");
			}
		}
		case "iden":{ //identity matrix
			if(args.length!=1){throw new NumericException("Function takes 1 argument","iden");}
			int n=args[0].getInt();
			if(n<=0){throw new NumericException("n should be >0","iden");}
			Numeric I=new Numeric(n,n);
			for(int i=0; i<n; i++){
				I.matrix[i][i]=1;
			}
			return(I);
		}
		case "interpolate":{ //interpolate
			if(args.length!=3) {throw new NumericException("Function takes 3 arguments","interpolate");}
			double x=args[0].getDouble();
			Numeric numXX=args[1];
			if(numXX.format!=Format.MATRIX || numXX.nrow!=1){throw new NumericException("X should be a row vector","interpolate");} 
			Numeric numYY=args[2];
			if(numYY.format!=Format.MATRIX || numYY.nrow!=1){throw new NumericException("Y should be a row vector","interpolate");}
			if(numXX.ncol==0) {throw new NumericException("X should have at least one element","interpolate");}
			if(numYY.ncol==0) {throw new NumericException("Y should have at least one element","interpolate");}
			if(numXX.ncol!=numYY.ncol) {throw new NumericException("X and Y should have the same length","interpolate");} 
			
			double xx[]=numXX.matrix[0];
			double yy[]=numYY.matrix[0];
			int length=xx.length;
			for(int i=0; i<length-1; i++) {
				if(xx[i+1]<xx[i]) {throw new NumericException("X is not in ascending order","interpolate");}
			}
			
			double val=0;
			if(x<=xx[0]){ //Below or at first index
				double slope=(yy[1]-yy[0])/(xx[1]-xx[0]);
				val=yy[0]-(xx[0]-x)*slope;
			}
			else if(x>xx[length-1]){ //Above last index
				double slope=(yy[length-1]-yy[length-2])/(xx[length-1]-xx[length-2]);
				val=yy[length-1]+(x-xx[length-1])*slope;
			}
			else{ //Between
				int row=0;
				while(xx[row]<x){row++;}
				double slope=(yy[row]-yy[row-1])/(xx[row]-xx[row-1]);
				val=yy[row-1]+(x-xx[row-1])*slope;
			}
			return(new Numeric(val));
			
		}
		case "inv":{ //invert
			if(args.length!=1){throw new NumericException("Function takes 1 argument","inv");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","inv");}
			return(inv(args[0]));
		}
		case "ncol":{ //num columns
			if(args.length!=1){throw new NumericException("Function takes 1 argument","ncol");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","ncol");}
			return(new Numeric(args[0].ncol));
		}
		case "norm":{
			if(args.length==1){ //Frobenius norm
				Numeric mat=args[0];
				if(mat.format!=Format.MATRIX){throw new NumericException("X is not a matrix","norm");}
				double sum=0;
				for(int i=0; i<mat.nrow; i++){
					for(int j=0; j<mat.ncol; j++){
						sum+=(mat.matrix[i][j]*mat.matrix[i][j]);
					}
				}
				double norm=Math.sqrt(sum);
				return(new Numeric(norm));
			}
			else if(args.length==2){ //L-n norm
				Numeric mat=args[0];
				if(mat.format!=Format.MATRIX){throw new NumericException("X is not a matrix","norm");}
			}
			else{
				throw new NumericException("Function takes 1 or 2 arguments","norm");
			}
		}
		case "nrow":{ //num rows
			if(args.length!=1){throw new NumericException("Function takes 1 argument","nrow");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","nrow");}
			return(new Numeric(args[0].nrow));
		}
		case "renorm":{
			if(args.length!=1){throw new NumericException("Function takes 1 argument","renorm");}
			Numeric x=args[0];
			if(x.nrow!=1){throw new NumericException("X should be a row vector","renorm");} //ensure x is row vector
			int n=x.ncol;
			if(n==0){throw new NumericException("X should have at least one element","renorm");}
			Numeric newX=new Numeric(x.matrix);
			double sum=0;
			for(int i=0; i<n; i++){
				double curVal=x.matrix[0][i];
				if(curVal<=0){throw new NumericException("X should contain real numbers >0","renorm");}
				sum+=curVal;
			}
			for(int i=0; i<n; i++){
				newX.matrix[0][i]=x.matrix[0][i]/sum;
			}
			return(newX);
		}
		case "rep":{
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","rep");}
			double x=args[0].getDouble();
			int n=args[1].getInt();
			if(n<=0){throw new NumericException("n should be >0","rep");}
			Numeric row=new Numeric(1,n);
			for(int i=0; i<n; i++){
				row.matrix[0][i]=x;
			}
			return(row);
		}
		case "seq":{
			if(args.length<2 || args.length>3){throw new NumericException("Function takes 2 or 3 arguments","seq");}
			double a=args[0].getDouble();
			double b=args[1].getDouble();
			if(a==b){throw new NumericException("a should not equal b","seq");}
			double step=1.0;
			if(args.length==3){
				step=args[2].getDouble();
				if(step<=0){throw new NumericException("Step size should be >0","seq");}
			}
			if(b<a){step=-step;}
			int num=(int) ((b-a)/step);
			double seq[][]=new double[1][num+1];
			for(int i=0; i<=num; i++){
				seq[0][i]=a+step*i;
			}
			return(new Numeric(seq));
		}
		case "softmax":{ //softmax
			if(args.length!=1){throw new NumericException("Function takes 1 argument","softmax");}
			Numeric x=args[0];
			if(x.nrow!=1){throw new NumericException("X should be a row vector","softmax");} //ensure x is row vector
			int n=x.ncol;
			if(n==0){throw new NumericException("X should have at least one element","softmax");}
			Numeric newX=new Numeric(x.matrix);
			double sum=0;
			for(int i=0; i<n; i++){
				double curVal=Math.exp(x.matrix[0][i]);
				newX.matrix[0][i]=curVal;
				sum+=curVal;
			}
			for(int i=0; i<n; i++){
				newX.matrix[0][i]/=sum;
			}
			return(newX);
		}
		case "stack":{
			if(args.length==0){throw new NumericException("Function takes at least 1 argument","stack");}
			int nrow=args.length;
			int ncol=args[0].ncol;
			if(ncol==0){throw new NumericException("X1 should be a non-empty row vector","stack");}
			Numeric newMatrix=new Numeric(nrow,ncol);
			for(int i=0; i<nrow; i++){
				Numeric x=args[i];
				if(x.nrow!=1){throw new NumericException("X"+(i+1)+" should be a row vector","stack");}
				if(x.ncol!=ncol){throw new NumericException("X"+(i+1)+" should have "+ncol+" columns","stack");}
				for(int j=0; j<ncol; j++){
					newMatrix.matrix[i][j]=x.matrix[0][j];
				}
			}
			return(newMatrix);
		}
		case "tp":{ //transpose
			if(args.length!=1){throw new NumericException("Function takes 1 argument","tp");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","tp");}
			return(tp(args[0]));
		}
		case "tr":{ //trace
			if(args.length!=1){throw new NumericException("Function takes 1 argument","tr");}
			if(args[0].format!=Format.MATRIX){throw new NumericException("Argument is not a matrix","tr");}
			Numeric matrix=args[0];
			if(matrix.nrow==matrix.ncol){ //square
				double sum=0;
				for(int i=0; i<matrix.nrow; i++){
					sum+=matrix.matrix[i][i];
				}
				Numeric trace=new Numeric(sum);
				return(trace);
			}
			else{
				throw new NumericException("X should be a square matrix","tr");
			}
		}
		
		}
		return(null); //fell through
	}

	public static Numeric add(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble()+arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]+arg2.getDouble();
				}
			}
			return(result);
		}
		else if(arg1.format!=Format.MATRIX==false && arg2.format!=Format.MATRIX==false){ //matrix + matrix
			if(arg1.nrow==arg2.nrow && arg1.ncol==arg2.ncol){ //check if conformable
				Numeric result=new Numeric(arg1.nrow,arg1.ncol);
				for(int i=0; i<arg1.nrow; i++){
					for(int j=0; j<arg1.ncol; j++){
						result.matrix[i][j]=arg1.matrix[i][j]+arg2.matrix[i][j];
					}
				}
				return(result);
			}
			else{
				throw new NumericException("Matrices are not conformable","Addition");
			}
		}
		return(null);
	}

	public static Numeric subtract(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble()-arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]-arg2.getDouble();
				}
			}
			return(result);
		}
		else if(arg1.format!=Format.MATRIX==false && arg2.format!=Format.MATRIX==false){ //matrix + matrix
			if(arg1.nrow==arg2.nrow && arg1.ncol==arg2.ncol){ //check if conformable
				Numeric result=new Numeric(arg1.nrow,arg1.ncol);
				for(int i=0; i<arg1.nrow; i++){
					for(int j=0; j<arg1.ncol; j++){
						result.matrix[i][j]=arg1.matrix[i][j]-arg2.matrix[i][j];
					}
				}
				return(result);
			}
			else{
				throw new NumericException("Matrices are not conformable","Subtraction");
			}
		}
		return(null);
	}

	public static Numeric multiply(Numeric arg1, Numeric arg2) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble()*arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]*arg2.getDouble();
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format==Format.MATRIX){ //matrix + matrix
			if(arg1.ncol==arg2.nrow){ //check if conformable
				Numeric result;
				if(arg1.nrow>1 || arg2.ncol>1){ //matrix/vector
					result=new Numeric(arg1.nrow,arg2.ncol);
					for(int i=0; i<arg1.nrow; i++){
						for(int j=0; j<arg2.ncol; j++){
							for(int k=0; k<arg1.ncol; k++){
								result.matrix[i][j]+=arg1.matrix[i][k]*arg2.matrix[k][j];
							}
						}
					}
				}
				else{
					double sum=0;
					for(int i=0; i<arg1.nrow; i++){
						for(int j=0; j<arg2.ncol; j++){
							for(int k=0; k<arg1.ncol; k++){
								sum+=arg1.matrix[i][k]*arg2.matrix[k][j];
							}
						}
					}
					result=new Numeric(sum);
				}
				return(result);
			}
			else{
				throw new NumericException("Matrices are not conformable","Multiplication");
			}
		}
		return(null);
	}
	
	/**
	 * Calculate determinant
	 * @param arg1
	 * @return
	 * @throws NumericException 
	 */
	public static Numeric det(Numeric mat) throws NumericException{
		if(mat.nrow!=mat.ncol){
			throw new NumericException("X should be a square matrix","det");
		}
		RealMatrix matrix=MatrixUtils.createRealMatrix(mat.matrix);
		return(new Numeric(new LUDecomposition(matrix).getDeterminant()));
	}

	/**
	 * Invert matrix
	 * @param arg1
	 * @return
	 * @throws NumericException 
	 */
	public static Numeric inv(Numeric mat) throws NumericException{
		if(mat.nrow==mat.ncol){ //square
			RealMatrix matrix=MatrixUtils.createRealMatrix(mat.matrix);
			RealMatrix inverse = new LUDecomposition(matrix).getSolver().getInverse();
			return(new Numeric(inverse.getData()));
		}
		else{
			throw new NumericException("X should be a square matrix","inv");
		}
	}
	
	/**
	 * Transpose matrix
	 * @param matrix
	 * @return
	 */
	public static Numeric tp(Numeric matrix){
		Numeric tp=new Numeric(matrix.ncol,matrix.nrow);
		for(int i=0; i<matrix.nrow; i++){
			for(int j=0; j<matrix.ncol; j++){
				tp.matrix[j][i]=matrix.matrix[i][j];
			}
		}
		return(tp);
	}
	
	public static Numeric chol(Numeric arg){
		RealMatrix matrix=MatrixUtils.createRealMatrix(arg.matrix);
		CholeskyDecomposition chol=new CholeskyDecomposition(matrix);
		return(new Numeric(chol.getL().getData()));
	}
	
	public static String getDescription(String fx){
		String des="";
		switch(fx){
		case "chol": 
			des="<html><b>Cholesky Decomposition</b><br>";
			des+=MathUtils.consoleFont("<b>chol</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the Cholesky Decomposition (lower triangle) of "+MathUtils.consoleFont("<b>X</b>"+"<br>");
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Symmetric positive definite (SPD) matrix<br>";
			des+="</html>";
			return(des);
		case "det": 
			des="<html><b>Determinant</b><br>";
			des+=MathUtils.consoleFont("<b>det</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the determinant of "+MathUtils.consoleFont("<b>X</b>"+"<br>");
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Square matrix<br>";
			des+="</html>";
			return(des);
		case "diag": 
			des="<html><b>Matrix Diagonals</b><br>";
			des+=MathUtils.consoleFont("<b>diag</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the diagonal elements of "+MathUtils.consoleFont("<b>X</b>")+" as a row vector<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Square matrix<br>";
			des+="</html>";
			return(des);
		case "iden": 
			des="<html><b>Identity Matrix</b><br>";
			des+=MathUtils.consoleFont("<b>iden</b>","#800000")+MathUtils.consoleFont("(n)")+": Returns an identity matrix of size "+MathUtils.consoleFont("n"+"<br>");
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("n")+": Size of identity matrix (Integer "+MathUtils.consoleFont(">0")+")<br>";
			des+="</html>";
			return(des);
		case "interpolate": 
			des="<html><b>Linear Interpolation</b><br>";
			des+=MathUtils.consoleFont("<b>interpolate</b>","#800000")+MathUtils.consoleFont("(x,<b>X</b>,<b>Y</b>)")+": Returns a linear interpolation of "+MathUtils.consoleFont("<b>Y</b>")+" at "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": x-value at which to calculate interpolated value of y<br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Row vector of x values (in increasing order)<br>";
			des+=MathUtils.consoleFont("<b>Y</b>")+": Row vector of corresponding y values<br>";
			des+="</html>";
			return(des);
		case "inv": 
			des="<html><b>Inverse Matrix</b><br>";
			des+=MathUtils.consoleFont("<b>inv</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the inverse of "+MathUtils.consoleFont("<b>X</b>")+" using LU decomposition<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Square, non-singular matrix<br>";
			des+="</html>";
			return(des);
		case "ncol":
			des="<html><b>Number of Columns</b><br>";
			des+=MathUtils.consoleFont("<b>ncol</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the number of columns in "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Matrix<br>";
			des+="</html>";
			return(des);
		case "norm":
			des="<html><b>Matrix Norm</b><br>";
			des+="Returns the norm of the matrix";
			des+="<br><i>Functions</i><br>";
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the Frobenius norm of "+MathUtils.consoleFont("<b>X</b>")+" (default)<br>";
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>,n)")+": Returns the (vector induced) L-"+MathUtils.consoleFont("n")+" norm of "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>,inf)")+": Returns the (vector induced) infinity norm of "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Matrix<br>";
			des+=MathUtils.consoleFont("n")+": L-n norm (Integer) (optional)<br>";
			des+="</html>";
			return(des);
		case "nrow":
			des="<html><b>Number of Rows</b><br>";
			des+=MathUtils.consoleFont("<b>nrow</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the number of rows in "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Matrix<br>";
			des+="</html>";
			return(des);
		case "renorm":
			des="<html><b>Renormalize</b><br>";
			des+=MathUtils.consoleFont("<b>renorm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns a row vector with the elements of "+MathUtils.consoleFont("<b>X</b>")+" linearly renormalized to sum to "+MathUtils.consoleFont("1.0")+"<br";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Row vector of real numbers "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "rep":
			des="<html><b>Replicate</b><br>";
			des+=MathUtils.consoleFont("<b>rep</b>","#800000")+MathUtils.consoleFont("(x,n)")+": Returns a row vector of size "+MathUtils.consoleFont("n")+" filled with "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number to replicate<br>";
			des+=MathUtils.consoleFont("n")+": Vector size (Integer "+MathUtils.consoleFont(">0")+")<br>";
			des+="</html>";
			return(des);
		case "seq":
			des="<html><b>Sequence</b><br>";
			des+="Returns a sequence of evenly spaced numbers in "+MathUtils.consoleFont("[a,b]")+"<br>";
			des+="<br><i>Functions</i><br>";
			des+=MathUtils.consoleFont("<b>seq</b>","#800000")+MathUtils.consoleFont("(a,b)")+": Returns a row vector containing a sequence of numbers from "+MathUtils.consoleFont("a")+" to "+MathUtils.consoleFont("b")+" with default step size ("+MathUtils.consoleFont("1.0")+")<br>";
			des+=MathUtils.consoleFont("<b>seq</b>","#800000")+MathUtils.consoleFont("(a,b,∆)")+": Returns a row vector containing a sequence of numbers from "+MathUtils.consoleFont("a")+" to "+MathUtils.consoleFont("b")+" with step size "+MathUtils.consoleFont("∆")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("a")+": Start of sequence (inclusive), real number<br>";
			des+=MathUtils.consoleFont("b")+": End of sequence (inclusive if step size allows), real number<br>";
			des+=MathUtils.consoleFont("∆")+": Step size, real number "+MathUtils.consoleFont(">0")+" (optional)<br>";
			des+="</html>";
			return(des);
		case "softmax":
			des="<html><b>Softmax Function (Normalized Exponential)</b><br>";
			des+=MathUtils.consoleFont("<b>softmax</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns a row vector with the exponentiated elements of "+MathUtils.consoleFont("<b>X</b>")+" renormalized to sum to "+MathUtils.consoleFont("1.0")+"<br";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Row vector of real numbers<br>";
			des+="</html>";
			return(des);
		case "stack":
			des="<html><b>Stack Row Vectors</b><br>";
			des+=MathUtils.consoleFont("<b>stack</b>","#800000")+MathUtils.consoleFont("(<b>X</b><sub>1</sub>,<b>X</b><sub>2</sub>,...,<b>X</b><sub>n</sub>)");
			des+=": Returns a matrix of stacked row vectors<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b><sub>1</sub>,<b>X</b><sub>2</sub>,...,<b>X</b><sub>n</sub>")+": Row vectors<br>";
			des+="</html>";
			return(des);
		case "tp": 
			des="<html><b>Matrix Transpose</b><br>";
			des+=MathUtils.consoleFont("<b>tp</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the transpose of "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Matrix<br>";
			des+="</html>";
			return(des);
		case "tr": 
			des="<html><b>Matrix Trace</b><br>";
			des+=MathUtils.consoleFont("<b>tr</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": Returns the trace (i.e. sum of diagonal elements) of "+MathUtils.consoleFont("<b>X</b>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<b>X</b>")+": Square matrix<br>";
			des+="</html>";
			return(des);
		}
		return(des); //fell through
	}
	
	public static String getDefaultArgs(String word){
		switch(word){
		case "chol": return("chol(X)");
		case "det": return("det(X)");
		case "diag": return("diag(X)");
		case "iden": return("iden(n)");
		case "interpolate": return("interpolate(x,X,Y)");
		case "inv": return("inv(X)");
		case "ncol": return("ncol(X)");
		case "norm": return("norm(X)");
		case "nrow": return("nrow(X)");
		case "renorm": return("renorm(X)");
		case "rep": return("rep(x,n)");
		case "seq": return("seq(a,b)");
		case "softmax": return("softmax(X)");
		case "stack": return("stack(X1,X2,...,Xn)");
		case "tp": return("tp(X)");
		case "tr": return("tr(X)");
		}
		return(""); //fell through
	}

}