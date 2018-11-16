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

public final class PythonFunctions{

	public static int inPlace(String fx){
		switch(fx){
		case "abs": return(0); //absolute value
		case "acos": return(0); //arccosine
		case "asin": return(0); //arcsine
		case "atan": return(0); //arctan
		case "bound": return(1); //bound
		case "cbrt": return(1); //cube root
		case "ceil": return(0); //ceiling
		case "choose": return(1); //n choose k
		case "cos": return(0); //cosine
		case "cosh": return(0); //hyperbolic cosine
		case "erf": return(0); //error function
		case "exp": return(0); //exp
		case "fact": return(0); //factorial
		case "floor": return(0); //floor
		case "gamma": return(0); //gamma
		case "hypot": return(1); //hypotenuse
		case "if": return(1); //if
		case "invErf": return(1); //inverse error function
		case "log": return(0); //natural log
		case "logb": return(1); //log base b
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
		case "signum": return(1); //signum
		//summary functions
		case "mean": return(2); //mean
		case "product": return(2); //product
		case "quantile": return(2); //quantile
		case "sd": return(2); //standard deviation
		case "sum": return(2); //sum
		case "var": return(2); //variance
		
		} //end switch
		return(-1); //fell through
	}

	public static String translate(String fx){
		switch(fx){
		case "abs": return("abs"); //absolute value
		case "acos": return("math.acos"); //arccosine
		case "asin": return("math.asin"); //arcsine
		case "atan": return("math.atan"); //arctan
		case "bound": return("fx.bound"); //bound
		case "cbrt": return("fx.cbrt"); //cube root
		case "ceil": return("math.ceil"); //ceiling
		case "choose": return("fx.choose"); //n choose k
		case "cos": return("math.cos"); //cosine
		case "cosh": return("math.cosh"); //hyperbolic cosine
		case "erf": return("math.erf"); //error function
		case "exp": return("math.exp"); //exp
		case "fact": return("math.factorial"); //factorial
		case "floor": return("math.floor"); //floor
		case "gamma": return("math.gamma"); //gamma function
		case "hypot": return("fx.hypot"); //hypotenuse
		case "if": return("fx.ifElse"); //if
		case "invErf": return("fx.invErf"); //inverse error function
		case "log": return("math.log"); //natural log
		case "logb": return("fx.logb"); //log base b
		case "logGamma": return("math.lgamma"); //log gamma
		case "log10": return("math.log10"); //log base-10
		case "max": return("max"); //max(a,b)
		case "min": return("min"); //min(a,b)
		case "probRescale": return("fx.probRescale"); //prob to prob
		case "probToRate": return("fx.probToRate"); //prob to rate
		case "rateToProb": return("fx.rateToProb"); //rate to prob
		case "round": return("round"); //round
		case "sin": return("math.sin"); //sine
		case "sinh": return("math.sinh"); //hyperbolic sine
		case "sqrt": return("math.sqrt"); //square root
		case "tan": return("math.tan"); //tan
		case "tanh": return("math.tanh"); //hyperbolic tan
		case "signum": return("fx.signum"); //signum
		
		} //end switch
		return(null); //fell through
		
	}

	public static String define(String fx){
		switch(fx){
		case "bound":{ //bound
			String function="def bound(x, a, b):\n";
			function+="    if(x<a):x=a #min\n";
			function+="    if(x>b):x=b #max\n";
			function+="    return(x)\n";
			return(function);
		}
		case "cbrt": return("cbrt = lambda x: x**(1/3)"); //cube root
		case "choose":{ //n choose k
			String function="def choose(n,k){\n";
			// to do
			function+="		return(null)\n";
			return(function);
		}
		case "hypot": return("hypot = lambda x,y: math.sqrt(x**2 + y**2)"); //hypotenuse
		case "if":{ //if
			String function="def returnIf(expr, a, b):\n";
			function+="    if(expr): return(a)\n";
			function+="    else: return(b)\n";
			return(function);
		}
		case "invErf":{ //inverse error function
			String function="def invErf(x):\n";
			// to do
			function+="		return(null);\n";
			return(function);
		}
		case "logb": return("logb = lambda x,b: math.log(x)/math.log(b)"); //log base b
		case "probRescale":{ //prob to prob
			String function="def probRescale(p, t1, t2):\n";
			function+="    r0=-Math.log(1-p) #prob to rate\n";
			function+="    r1=r0/t1 #rate per t1\n";
			function+="    r2=r1*t2 #rate per t2\n";
			function+="    pNew=1-math.exp(-r2) #prob per t2\n";
			function+="    return(pNew)\n";
			return(function);
		}
		case "probToRate":{ //prob to rate
			String function="def probToRate(p, t1=1, t2=1):\n";
			function+="    r0=-math.log(1-p) #prob to rate\n";
			function+="    r1=r0/t1 #rate per t1\n";
			function+="    r2=r1*t2 #rate per t2\n";
			function+="    return(r2)\n";
			return(function);
		}
		case "rateToProb":{ //rate to prob
			String function="def rateToProb(r, t1=1, t2=1):\n";
			function+="    r1=r/t1 #rate per t1\n";
			function+="    r2=r1*t2 #rate per t2\n";
			function+="    p=1-math.exp(-r2) #prob per t2\n";
			function+="    return(p)\n";
			return(function);
		}
		case "signum":{
			String function="def signum(x):\n";
			function+="    if(x<0): return(-1)\n";
			function+="    elif(x==0): return(0)\n";
			function+="    else: return(1)\n";
			return(function);
		}
		
		} //end switch
		return(null); //fell through
		
	}
		
	public static String changeArgs(String fx,String args[],PythonModel pyModel,boolean personLevel) throws Exception{
		switch(fx){
		case "mean":{ //mean
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="["+pyModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[0],personLevel);
			}
			return("np.mean("+vector+")");
		}
		case "product":{ //product
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="["+pyModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[0],personLevel);
			}
			return("np.product("+vector+")");
		}
		case "quantile":{ //quantile
			int numArgs=args.length;
			String q=pyModel.translate(args[0], personLevel); //quantile
			String vector="";
			if(numArgs>2){ //entries, not vector
				vector="["+pyModel.translate(args[1], personLevel);
				for(int i=2; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[1],personLevel);
			}
			return("np.quantile("+vector+","+q+")");
		}
		case "sd":{ //standard deviation
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="["+pyModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[0],personLevel);
			}
			return("np.std("+vector+")");
		}
		case "sum":{ //sum
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="["+pyModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[0],personLevel);
			}
			return("sum("+vector+")");
		}
		case "var":{ //variance
			int numArgs=args.length;
			String vector="";
			if(numArgs>1){ //entries, not vector
				vector="["+pyModel.translate(args[0], personLevel);
				for(int i=1; i<numArgs; i++){vector+=","+pyModel.translate(args[i], personLevel);}
				vector+="]";
			}
			else{ //vector
				vector=pyModel.translate(args[0],personLevel);
			}
			return("np.var("+vector+")");
		}
		
		} //end switch
		return(null); //fell through
	}
}