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

import java.text.MessageFormat;

import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import lang.Language;

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

	public static Numeric evaluate(String fx, Language language, Numeric...args) throws NumericException{
		switch(fx){
		case "chol":{ //Cholesky decomposition
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"chol",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"chol",language);} //Argument is not a matrix
			return(chol(args[0]));
		}
		case "det":{ //Determinant
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"det",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"det",language);} //Argument is not a matrix
			return(det(args[0], language));
		}
		case "diag":{ //get diagonals
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"diag",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"diag",language);} //Argument is not a matrix
			Numeric matrix=args[0];
			if(matrix.nrow==matrix.ncol){ //square
				Numeric diag=new Numeric(1,matrix.nrow);
				for(int i=0; i<matrix.nrow; i++){
					diag.matrix[0][i]=matrix.matrix[i][i];
				}
				return(diag);
			}
			else{
				throw new NumericException(language.message.getString("err.not_square_matrix"),"diag",language); //X should be a square matrix"
			}
		}
		case "iden":{ //identity matrix
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"iden",language);} //Function takes 1 argument
			int n=args[0].getInt(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"iden",language);} //n should be >0
			Numeric I=new Numeric(n,n);
			for(int i=0; i<n; i++){
				I.matrix[i][i]=1;
			}
			return(I);
		}
		case "interpolate":{ //interpolate
			if(args.length!=3) {throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 3),"interpolate",language);} //Function takes 3 arguments
			double x=args[0].getDouble(language);
			Numeric numXX=args[1];
			if(numXX.format!=Format.MATRIX || numXX.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "X"),"interpolate",language);} //X should be a row vector
			Numeric numYY=args[2];
			if(numYY.format!=Format.MATRIX || numYY.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "Y"),"interpolate",language);} //Y should be a row vector
			if(numXX.ncol==0) {throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_gte1_element"), "X"),"interpolate",language);} //X should have at least one element
			if(numYY.ncol==0) {throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_gte1_element"), "Y"),"interpolate",language);} //Y should have at least one element
			if(numXX.ncol!=numYY.ncol) {throw new NumericException(language.message.getString("err.should_have_same_length"),"interpolate",language);} //X and Y should have the same length
			
			double xx[]=numXX.matrix[0];
			double yy[]=numYY.matrix[0];
			int length=xx.length;
			for(int i=0; i<length-1; i++) {
				if(xx[i+1]<xx[i]) {throw new NumericException(language.message.getString("err.not_ascending_order"),"interpolate",language);} //X is not in ascending order
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"inv",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"inv",language);} //Argument is not a matrix
			return(inv(args[0], language));
		}
		case "ncol":{ //num columns
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"ncol",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"ncol",language);} //Argument is not a matrix
			return(new Numeric(args[0].ncol));
		}
		case "norm":{
			if(args.length==1){ //Frobenius norm
				Numeric mat=args[0];
				if(mat.format!=Format.MATRIX){throw new NumericException(language.message.getString("err.x_not_matrix"),"norm",language);} //X is not a matrix
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
				if(mat.format!=Format.MATRIX){throw new NumericException(language.message.getString("err.x_not_matrix"),"norm",language);} //X is not a matrix
			}
			else{
				throw new NumericException(MessageFormat.format(language.message.getString("err.fx_one_two_args"), 1, 2),"norm",language); //Function takes 1 or 2 arguments
			}
		}
		case "nrow":{ //num rows
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"nrow",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"nrow",language);} //Argument is not a matrix
			return(new Numeric(args[0].nrow));
		}
		case "renorm":{
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"renorm",language);} //Function takes 1 argument
			Numeric x=args[0];
			if(x.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "X"),"renorm",language);} //X should be a row vector
			int n=x.ncol;
			if(n==0){throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_gte1_element"), "X"),"renorm",language);} //X should have at least one element
			Numeric newX=new Numeric(x.matrix);
			double sum=0;
			for(int i=0; i<n; i++){
				double curVal=x.matrix[0][i];
				if(curVal<=0){throw new NumericException(language.message.getString("err.x_should_real_gt0"),"renorm",language);} //X should contain real numbers >0
				sum+=curVal;
			}
			for(int i=0; i<n; i++){
				newX.matrix[0][i]=x.matrix[0][i]/sum;
			}
			return(newX);
		}
		case "rep":{
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"rep",language);} //Function takes 2 arguments
			double x=args[0].getDouble(language);
			int n=args[1].getInt(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"rep",language);} //n should be >0
			Numeric row=new Numeric(1,n);
			for(int i=0; i<n; i++){
				row.matrix[0][i]=x;
			}
			return(row);
		}
		case "seq":{
			if(args.length<2 || args.length>3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_one_two_args"), 2, 3),"seq",language);} //Function takes 2 or 3 arguments
			double a=args[0].getDouble(language);
			double b=args[1].getDouble(language);
			if(a==b){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_not_equal_val"), "a","b"),"seq",language);} //a should not equal b
			double step=1.0;
			if(args.length==3){
				step=args[2].getDouble(language);
				if(step<=0){throw new NumericException(language.message.getString("err.step_gt0"),"seq",language);} //Step size should be >0
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"softmax",language);} //Function takes 1 argument
			Numeric x=args[0];
			if(x.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "X"),"softmax",language);} //X should be a row vector
			int n=x.ncol;
			if(n==0){throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_gte1_element"), "X"),"softmax",language);} //X should have at least one element
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
			if(args.length==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"stack",language);} //Function takes at least 1 argument
			int nrow=args.length;
			int ncol=args[0].ncol;
			if(ncol==0){throw new NumericException(language.message.getString("err.non_empty_row_vector"),"stack",language);} //X1 should be a non-empty row vector
			Numeric newMatrix=new Numeric(nrow,ncol);
			for(int i=0; i<nrow; i++){
				Numeric x=args[i];
				if(x.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "X"+(i+1)),"stack",language);} //X"+(i+1)+" should be a row vector
				if(x.ncol!=ncol){throw new NumericException(MessageFormat.format(language.message.getString("err.should_n_cols"), (i+1), ncol),"stack",language);} //X(i+1) should have [ncol] columns
				for(int j=0; j<ncol; j++){
					newMatrix.matrix[i][j]=x.matrix[0][j];
				}
			}
			return(newMatrix);
		}
		case "tp":{ //transpose
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"tp",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"tp",language);} //Argument is not a matrix
			return(tp(args[0]));
		}
		case "tr":{ //trace
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"tr",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){throw new NumericException(language.message.getString("err.arg_not_matrix"),"tr",language);} //Argument is not a matrix
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
				throw new NumericException(language.message.getString("err.not_square_matrix"),"tr",language); //X should be a square matrix
			}
		}
		
		}
		return(null); //fell through
	}

	public static Numeric add(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble(language)+arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]+arg2.getDouble(language);
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
				throw new NumericException(language.message.getString("err.mat_not_conformable"),language.math.getString("op.addition"),language); //Matrices are not conformable, Addition
			}
		}
		return(null);
	}

	public static Numeric subtract(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble(language)-arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]-arg2.getDouble(language);
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
				throw new NumericException(language.message.getString("err.mat_not_conformable"), language.math.getString("op.subtraction"),language); //Matrices are not conformable, Subtraction
			}
		}
		return(null);
	}

	public static Numeric multiply(Numeric arg1, Numeric arg2, Language language) throws NumericException{
		if(arg1.format!=Format.MATRIX && arg2.format==Format.MATRIX){ //number + matrix
			Numeric result=new Numeric(arg2.nrow,arg2.ncol);
			for(int i=0; i<arg2.nrow; i++){
				for(int j=0; j<arg2.ncol; j++){
					result.matrix[i][j]=arg1.getDouble(language)*arg2.matrix[i][j];
				}
			}
			return(result);
		}
		else if(arg1.format==Format.MATRIX && arg2.format!=Format.MATRIX){ //matrix + number
			Numeric result=new Numeric(arg1.nrow,arg1.ncol);
			for(int i=0; i<arg1.nrow; i++){
				for(int j=0; j<arg1.ncol; j++){
					result.matrix[i][j]=arg1.matrix[i][j]*arg2.getDouble(language);
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
				throw new NumericException(language.message.getString("err.mat_not_conformable"), language.math.getString("op.multiplication"),language); //Matrices are not conformable, Multiplication
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
	public static Numeric det(Numeric mat, Language language) throws NumericException{
		if(mat.nrow!=mat.ncol){
			throw new NumericException(language.message.getString("err.not_square_matrix"),"det",language); //X should be a square matrix
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
	public static Numeric inv(Numeric mat, Language language) throws NumericException{
		if(mat.nrow==mat.ncol){ //square
			RealMatrix matrix=MatrixUtils.createRealMatrix(mat.matrix);
			RealMatrix inverse = new LUDecomposition(matrix).getSolver().getInverse();
			return(new Numeric(inverse.getData()));
		}
		else{
			throw new NumericException(language.message.getString("err.not_square_matrix"),"inv",language); //X should be a square matrix
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
	
	public static String getDescription(String fx, Language language){
		String des="";
		switch(fx){
		case "chol": 
			des="<html><b>"+language.math.getString("mat.chol_name")+"</b><br>"; //Cholesky Decomposition
			des+=MathUtils.consoleFont("<b>chol</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.chol_desc")+"<br>"; //Returns the Cholesky Decomposition (lower triangle) of X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.matrix_spd")+"<br>"; //Symmetric positive definite (SPD) matrix
			des+="</html>";
			return(des);
		case "det": 
			des="<html><b>"+language.math.getString("mat.det_name")+"</b><br>"; //Determinant
			des+=MathUtils.consoleFont("<b>det</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.det_desc")+"<br>"; //Returns the determinant of X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.matrix_square")+"<br>"; //Square matrix
			des+="</html>";
			return(des);
		case "diag": 
			des="<html><b>"+language.math.getString("mat.diag_name")+"</b><br>"; //Matrix Diagonals
			des+=MathUtils.consoleFont("<b>diag</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.diag_desc")+"<br>"; //Returns the diagonal elements of X as a row vector
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.matrix_square")+"<br>"; //Square matrix
			des+="</html>";
			return(des);
		case "iden": 
			des="<html><b>"+language.math.getString("mat.iden_name")+"</b><br>"; //Identity Matrix
			des+=MathUtils.consoleFont("<b>iden</b>","#800000")+MathUtils.consoleFont("(n)")+": "+language.math.getString("mat.iden_desc")+"<br>"; //Returns an identity matrix of size n
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("mat.iden_size")+"<br>"; //Size of identity matrix (Integer >0)
			des+="</html>";
			return(des);
		case "interpolate": 
			des="<html><b>"+language.math.getString("mat.interpolate_name")+"</b><br>"; //Linear Interpolation
			des+=MathUtils.consoleFont("<b>interpolate</b>","#800000")+MathUtils.consoleFont("(x,<b>X</b>,<b>Y</b>)")+": "+language.math.getString("mat.interpolate_desc")+"<br>"; //Returns a linear interpolation of Y at x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("mat.interpolate_x")+"<br>"; //x-value at which to calculate interpolated value of y
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.interpolate_row_x")+"<br>"; //Row vector of x values (in increasing order)
			des+=MathUtils.consoleFont("<b>Y</b>")+": "+language.math.getString("mat.interpolate_row_y")+"<br>"; //Row vector of corresponding y values
			des+="</html>";
			return(des);
		case "inv": 
			des="<html><b>"+language.math.getString("mat.inv_name")+"</b><br>"; //Inverse Matrix
			des+=MathUtils.consoleFont("<b>inv</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.inv_desc")+"<br>"; //Returns the inverse of X using LU decomposition
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.matrix_square_nonsingular")+"<br>"; //Square, non-singular matrix
			des+="</html>";
			return(des);
		case "ncol":
			des="<html><b>"+language.math.getString("mat.ncol_name")+"</b><br>"; //Number of Columns
			des+=MathUtils.consoleFont("<b>ncol</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.ncol_desc")+"<br>"; //Returns the number of columns in X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.base.getString("table.matrix")+"<br>"; //Matrix
			des+="</html>";
			return(des);
		case "norm":
			des="<html><b>"+language.math.getString("mat.norm_name")+"</b><br>"; //Matrix Norm
			des+=language.math.getString("mat.norm_desc"); //Returns the norm of the matrix
			des+="<br><br><i>"+language.math.getString("fx.functions")+"</i><br>"; //Functions
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.norm_desc_default")+"<br>"; //Returns the Frobenius norm of X (default)
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>,n)")+": "+language.math.getString("mat.norm_desc_L")+"<br>"; //Returns the (vector induced) L-n norm of X
			des+=MathUtils.consoleFont("<b>norm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>,inf)")+": "+language.math.getString("mat.norm_desc_inf")+"<br>"; //Returns the (vector induced) infinity norm of X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.base.getString("table.matrix")+"<br>"; //Matrix
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("mat.norm_n")+"<br>"; //L-n norm (Integer) (optional)
			des+="</html>";
			return(des);
		case "nrow":
			des="<html><b>"+language.math.getString("mat.nrow_name")+"</b><br>"; //Number of Rows
			des+=MathUtils.consoleFont("<b>nrow</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.nrow_desc")+"<br>"; //Returns the number of rows in X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.base.getString("table.matrix")+"<br>"; //Matrix
			des+="</html>";
			return(des);
		case "renorm":
			des="<html><b>"+language.math.getString("mat.renorm_name")+"</b><br>"; //Renormalize
			des+=MathUtils.consoleFont("<b>renorm</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.renorm_desc")+"<br"; //Returns a row vector with the elements of X linearly renormalized to sum to 1.0
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.row_vector_real_gt0")+"<br>"; //Row vector of real numbers >0
			des+="</html>";
			return(des);
		case "rep":
			des="<html><b>"+language.math.getString("mat.rep_name")+"</b><br>"; //Replicate
			des+=MathUtils.consoleFont("<b>rep</b>","#800000")+MathUtils.consoleFont("(x,n)")+": "+language.math.getString("mat.rep_desc")+"<br>"; //Returns a row vector of size n filled with x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("mat.rep_x")+"<br>"; //Real number to replicate
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("mat.rep_n")+"<br>"; //Vector size (Integer >0)
			des+="</html>";
			return(des);
		case "seq":
			des="<html><b>"+language.math.getString("mat.seq_name")+"</b><br>"; //Sequence
			des+=language.math.getString("mat.seq_desc")+"<br>"; //Returns a sequence of evenly spaced numbers in [a,b]
			des+="<br><i>"+language.math.getString("fx.functions")+"</i><br>"; //Functions
			des+=MathUtils.consoleFont("<b>seq</b>","#800000")+MathUtils.consoleFont("(a,b)")+": "+language.math.getString("mat.seq_desc_default")+"<br>"; //Returns a row vector containing a sequence of numbers from a to b with default step size (1.0)
			des+=MathUtils.consoleFont("<b>seq</b>","#800000")+MathUtils.consoleFont("(a,b,∆)")+": "+language.math.getString("mat.seq_desc_step")+"<br>"; //Returns a row vector containing a sequence of numbers from a to b with step size ∆
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("a")+": "+language.math.getString("mat.seq_a")+"<br>"; //Start of sequence (inclusive), real number
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("mat.seq_b")+"<br>"; //End of sequence (inclusive if step size allows), real number
			des+=MathUtils.consoleFont("∆")+": "+language.math.getString("mat.seq_step")+"<br>"; //Step size, real number >0 (optional)
			des+="</html>";
			return(des);
		case "softmax":
			des="<html><b>"+language.math.getString("mat.softmax_name")+"</b><br>"; //Softmax Function (Normalized Exponential)
			des+=MathUtils.consoleFont("<b>softmax</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.softmax_desc")+"<br"; //Returns a row vector with the exponentiated elements of X renormalized to sum to 1.0
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.row_vector_real")+"<br>"; //Row vector of real numbers
			des+="</html>";
			return(des);
		case "stack":
			des="<html><b>"+language.math.getString("mat.stack_name")+"</b><br>"; //Stack Row Vectors
			des+=MathUtils.consoleFont("<b>stack</b>","#800000")+MathUtils.consoleFont("(<b>X</b><sub>1</sub>,<b>X</b><sub>2</sub>,...,<b>X</b><sub>n</sub>)");
			des+=": "+language.math.getString("mat.stack_desc")+"<br>"; //Returns a matrix of stacked row vectors
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b><sub>1</sub>,<b>X</b><sub>2</sub>,...,<b>X</b><sub>n</sub>")+": "+language.math.getString("mat.row_vectors")+"<br>"; //Row vectors
			des+="</html>";
			return(des);
		case "tp": 
			des="<html><b>"+language.math.getString("mat.tp_name")+"</b><br>"; //Matrix Transpose
			des+=MathUtils.consoleFont("<b>tp</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.tp_desc")+"<br>"; //Returns the transpose of X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.base.getString("table.matrix")+"<br>"; //Matrix
			des+="</html>";
			return(des);
		case "tr": 
			des="<html><b>"+language.math.getString("mat.tr_name")+"</b><br>"; //Matrix Trace
			des+=MathUtils.consoleFont("<b>tr</b>","#800000")+MathUtils.consoleFont("(<b>X</b>)")+": "+language.math.getString("mat.tr_desc")+"<br>"; //Returns the trace (i.e., sum of diagonal elements) of X
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<b>X</b>")+": "+language.math.getString("mat.matrix_square")+"<br>"; //Square matrix
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