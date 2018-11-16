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


package export_Python;

public final class PythonMatrixFunctions{

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
		case "ncol": return(2);
		case "norm": return(0);
		case "nrow": return(2);
		case "renorm": return(1);
		case "rep": return(0);
		case "seq": return(2);
		case "softmax": return(1);
		case "stack": return(2);
		case "tp": return(0);
		}
		return(-1); //fell through
	}

	public static String translate(String fx){
		switch(fx){
		case "chol": return("np.linalg.cholesky"); //Cholesky decomposition
		case "det": return("np.linalg.det"); //Determinant
		case "diag": return("np.diagonal"); //get diagonals
		case "iden": return("np.identity"); //identity matrix
		case "inv": return("np.linalg.inv"); //invert
		case "norm": return("np.linalg.norm"); //norm (Frobenius)
		case "renorm": return("renorm"); //renormalize
		case "rep": return("np.repeat"); //replicate
		case "softmax": return("softmax"); //softmax
		case "tp": return("np.transpose"); //transpose
		
		} //end switch
		return(null); //fell through
	}
	
	public static String define(String fx){
		switch(fx){
		case "renorm": return("renorm = lambda X: X/sum(X)"); //renormalize		
		case "softmax": return("softmax = lambda X: np.exp(X)/sum(np.exp(X)) #softmax (sum to 1.0)"); //softmax
		
		} //end switch
		return(null); //fell through
	}
	
	public static String changeArgs(String fx,String args[], PythonModel pyModel,boolean personLevel) throws Exception{
		switch(fx){
		case "nrow": return("np.shape("+pyModel.translate(args[0], personLevel)+")[0]");
		case "ncol": return("np.shape("+pyModel.translate(args[0], personLevel)+")[1]");
		case "seq":{
			String arg0=pyModel.translate(args[0],personLevel);
			String arg1=pyModel.translate(args[1], personLevel);
			if(args.length==2){ //step size=1 (default)
				return("np.arange("+arg0+","+arg1+"+1)"); //make inclusive of end interval
			}
			else if(args.length==3){
				String step=pyModel.translate(args[2], personLevel);
				return("np.arange("+arg0+","+arg1+"+"+step+","+step+")"); //make inclusive of end interval
			}
		}
		case "stack": {
			int numArgs=args.length;
			String call="np.stack(("+pyModel.translate(args[0], personLevel); //tuple of arrays
			for(int i=1; i<numArgs; i++){
				call+=","+pyModel.translate(args[1], personLevel);
			}
			call+="))";
			return(call);
		}
		
		}//end switch
		return(null); //fell through
	}
	
}