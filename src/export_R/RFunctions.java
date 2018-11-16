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


public final class RFunctions{

	/**
	 * 
	 * @param fx
	 * @return Code: 0=In place, 1=Define function, 2=In place but change arguments
	 */
	public static int inPlace(String fx){
		switch(fx){
		case "abs": return(0); //absolute value
		case "acos": return(0); //arccosine
		case "asin": return(0); //arcsine
		case "atan": return(0); //arctan
		case "bound": return(1); //bound
		case "cbrt": return(1); //cube root
		case "ceil": return(0); //ceiling
		case "choose": return(0); //n choose k
		case "cos": return(0); //cosine
		case "cosh": return(0); //hyperbolic cosine
		case "erf": return(1); //error function
		case "exp": return(0); //exp
		case "fact": return(0); //factorial
		case "floor": return(0); //floor
		case "gamma": return(0); //gamma
		case "hypot": return(1); //hypotenuse
		case "if": return(0); //if
		case "invErf": return(1); //inverse error function
		case "log": return(0); //natural log
		case "logb": return(0); //log base b
		case "logGamma": return(0); //log gamma
		case "log10": return(0); //log base-10
		case "max": return(0); //max(a,b)
		case "min": return(0); //min(a,b)
		case "probRescale": return(1); //prob to prob
		case "probToRate": return(1); //prob to rate
		case "rateToProb": return(1); //rate to prob
		case "round": return(0); //round
		case "sin": return(0); //sine
		case "sinh": return(0); //hyperbolic sine
		case "sqrt": return(0); //square root
		case "tan": return(0); //tan
		case "tanh": return(0); //hyperbolic tan
		case "signum": return(0); //signum
		//summary functions
		case "mean": return(0); //mean
		case "product": return(0); //product
		case "quantile": return(1); //quantile
		case "sd": return(0); //standard deviation
		case "sum": return(0); //sum
		case "var": return(1); //variance
		
		} //end switch
		return(-1); //fell through
	}

	public static String translate(String fx){
		switch(fx){
		case "abs": return("abs"); //absolute value
		case "acos": return("acos"); //arccosine
		case "asin": return("asin"); //arcsine
		case "atan": return("atan"); //arctan
		case "ceil": return("ceiling"); //ceiling
		case "choose": return("choose"); //n choose k
		case "cos": return("cos"); //cosine
		case "cosh": return("cosh"); //hyperbolic cosine
		case "exp": return("exp"); //exp
		case "fact": return("factorial"); //factorial
		case "floor": return("floor"); //floor
		case "gamma": return("gamma"); //gamma
		case "if": return("ifelse"); //if
		case "log": return("log"); //natural log
		case "logb": return("log"); //log base b
		case "logGamma": return("lgamma"); //log gamma
		case "log10": return("log10"); //log base-10
		case "max": return("max"); //max(a,b)
		case "min": return("min"); //min(a,b)
		case "round": return("round"); //round
		case "sin": return("sin"); //sine
		case "sinh": return("sinh"); //hyperbolic sine
		case "sqrt": return("sqrt"); //square root
		case "tan": return("tan"); //tan
		case "tanh": return("tanh"); //hyperbolic tan
		case "signum": return("sign"); //signum
		//Summary functions
		case "mean": return("mean"); //mean
		case "product": return("prod"); //product
		case "sum":return("sum"); //sum
		
		} //end switch
		return(null); //fell through
	}

	public static String define(String fx){
		switch(fx){
		case "bound":{ //bound
			String function="bound<-function(x, a, b){\n";
			function+="	if(x<a){x=a;} //min\n";
			function+="	if(x>b){x=b;} //max\n";
			function+="	return(x);\n";
			function+="}";
			return(function);
		}
		case "cbrt": return("cbrt<-function(x) x^(1/3) #cube root"); //cube root
		case "erf": return("erf<-function(x) 2*pnorm(x*sqrt(2))-1 #error function"); //error function
		case "hypot": return("hypot<-function(a,b) sqrt(a^2+b^2)"); //hypotenuse
		case "invErf": return("invErf<-function(x) qnorm((1+x)/2)/sqrt(2) #inverse error function"); //inverse error function
		case "probRescale":{ //prob to prob
			String function="probRescale<-function(p, t1, t2){\n";
			function+="	r0=-log(1-p); #prob to rate\n";
			function+="	r1=r0/t1; //rate per t1\n";
			function+="	r2=r1*t2; //rate per t2\n";
			function+="	pNew=1-exp(-r2); #prob per t2\n";
			function+="	return(pNew);\n";
			function+="}";
			return(function);
		}
		case "probToRate":{ //prob to rate
			String function="probToRate<-function(p, t1=NULL, t2=NULL){\n";
			function+="	if(is.null(t1)){\n";
			function+="		r=-log(1-p); #prob to rate\n";
			function+="		return(r);\n";
			function+="	}\n";
			function+="	else{\n";
			function+="		r0=-log(1-p); #prob to rate\n";
			function+="		r1=r0/t1; #rate per t1\n";
			function+="		r2=r1*t2; #rate per t2\n";
			function+="		return(r2);\n";
			function+="	}\n";
			function+="}";
			return(function);
		}
		case "rateToProb":{ //rate to prob
			String function="rateToProb<-function(r, t1=NULL, t2=NULL){\n";
			function+="	if(is.null(t1)){\n";
			function+="		p=1-exp(-r); #rate to prob\n";
			function+="		return(p);\n";
			function+="	}\n";
			function+="	else{\n";
			function+="		r1=r/t1; #rate per t1\n";
			function+="		r2=r1*t2; #rate per t2\n";
			function+="		p=1-exp(-r2); #prob per t2\n";
			function+="		return(p);\n";
			function+="	}\n";
			function+="}";
			return(function);
		}
		
		} //end switch
		return(null); //fell through
	}
	
	public static String changeArgs(String fx,String args[],RModel rModel,boolean personLevel) throws Exception{
		switch(fx){
		case "quantile": { //quantile, to array and flip args
			int numArgs=args.length;
			String q=rModel.translate(args[0], personLevel); //quantile
			String vector="";
			if(numArgs>2){ //entries, not vector
				vector="c("+rModel.translate(args[1], personLevel);
				for(int i=2; i<numArgs; i++){vector+=","+rModel.translate(args[i], personLevel);}
				vector+=")";
			}
			else{ //vector
				vector=rModel.translate(args[1],personLevel);
			}
			return("quantile("+vector+","+q+")"); //quantile
		}
		case "sd": {//standard deviation, args to array
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="c("+rModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+rModel.translate(args[i], personLevel);}
				vector+=")";
			}
			else{ //vector
				vector=rModel.translate(args[0], personLevel);
			}
			return("sd("+vector+")"); //sd
		}
		case "var": {//variance, args to array
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="c("+rModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+rModel.translate(args[i], personLevel);}
				vector+=")";
			}
			else{ //vector
				vector=rModel.translate(args[0], personLevel);
			}
			return("var("+vector+")"); //var
		}
		
		
		} //end switch
		return(null); //fell through
	}
}