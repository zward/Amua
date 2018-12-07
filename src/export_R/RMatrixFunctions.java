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


package export_R;

public final class RMatrixFunctions{

	/**
	 * 
	 * @param fx
	 * @return Code: 0=In place, 1=Define function, 2=In place but change arguments
	 */
	public static int inPlace(String word){
		switch(word){
		case "chol": return(0);
		case "det": return(0);
		case "diag": return(0);
		case "iden": return(0);
		case "inv": return(0);
		case "ncol": return(0);
		case "norm": return(2);
		case "nrow": return(0);
		case "renorm": return(1);
		case "rep": return(0);
		case "seq": return(0);
		case "softmax": return(1);
		case "stack": return(0);
		case "tp": return(0);
		}
		return(-1); //fell through
	}

	public static String translate(String fx){
		switch(fx){
		case "chol": return("chol"); //Cholesky decomposition
		case "det": return("det"); //Determinant
		case "diag": return("diag"); //get diagonals
		case "iden": return("diag"); //identity matrix (diag takes 1 arg in R and returns identity matrix)
		case "inv": return("solve"); //invert
		case "ncol": return("ncol"); //num columns
		case "nrow": return("nrow"); //num rows
		case "rep": return("rep"); //replicate
		case "seq": return("seq"); //sequence
		case "stack": return("rbind"); //stack
		case "tp": return("t"); //transpose
		
		} //end switch
		return(null); //fell through
	}
	
	public static String define(String fx){
		switch(fx){
		
		case "renorm":{
			String function="renorm <- function(X) {\n";
			function+="  # Returns a vector with the elements of X linearly renormalized to sum to 1.0\n";
			function+="  #\n";
			function+="  # Args:\n";
			function+="  #   X: Vector of real numbers (>0)\n";
			function+="  #\n";
			function+="  # Returns:\n";
			function+="  #   Vector of real numbers that sum to 1.0\n\n";
			function+="  return(X / sum(X))\n";
			function+="}";
			return(function);
			
		}
		case "softmax":{
			String function="softmax <- function(X) {\n";
			function+="  # Returns a vector with the exponentiated elements of X renormalized to sum to 1.0\n";
			function+="  #\n";
			function+="  # Args:\n";
			function+="  #   X: Vector of real numbers\n";
			function+="  #\n";
			function+="  # Returns:\n";
			function+="  #   Vector of real numbers that sum to 1.0\n\n";
			function+="  return(exp(X) / sum(exp(X)))\n";
			function+="}";
			return(function);
		}
		
		} //end switch
		return(null); //fell through
	}
	
	public static String changeArgs(String fx,String args[],RModel rModel,boolean personLevel) throws Exception{
		switch(fx){
		case "norm": { //Frobenius norm
			String text="norm("+rModel.translate(args[0], personLevel)+", type=c(\"F\"))";
			return(text);
		}
		
		}//end switch
		return(null); //fell through
	}
	
}