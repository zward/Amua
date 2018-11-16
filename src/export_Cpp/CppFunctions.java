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


package export_Cpp;


public final class CppFunctions{

	public static boolean inPlace(String fx){
		switch(fx){
		case "abs": return(true); //absolute value
		case "acos": return(true); //arccosine
		case "asin": return(true); //arcsine
		case "atan": return(true); //arctan
		case "bound": return(false); //bound
		case "cbrt": return(true); //cube root
		case "ceil": return(true); //ceiling
		case "choose": return(false); //n choose k
		case "cos": return(true); //cosine
		case "cosh": return(true); //hyperbolic cosine
		case "erf": return(true); //error function
		case "exp": return(true); //exp
		case "fact": return(false); //factorial
		case "floor": return(true); //floor
		case "gamma": return(true); //gamma
		case "hypot": return(true); //hypotenuse
		case "if": return(false); //if
		case "invErf": return(false); //inverse error function
		case "log": return(true); //natural log
		case "logb": return(false); //log base b
		case "logGamma": return(true); //log gamma
		case "log10": return(true); //log base-10
		case "max": return(true); //max(a,b)
		case "min": return(true); //min(a,b)
		case "probRescale": return(false); //prob to prob
		case "probToRate": return(false); //prob to rate
		case "rateToProb": return(false); //rate to prob
		case "round": return(false); //round
		case "sin": return(true); //sine
		case "sinh": return(true); //hyperbolic sine
		case "sqrt": return(true); //square root
		case "tan": return(true); //tan
		case "tanh": return(true); //hyperbolic tan
		case "signum": return(false); //signum
		//summary functions
		case "mean": return(false); //mean
		case "product": return(false); //product
		case "quantile": return(false); //quantile
		case "sd": return(false); //standard deviation
		case "sum": return(false); //sum
		case "var": return(false); //variance
		
		} //end switch
		return(false); //fell through
	}

	public static String translate(String fx){
		switch(fx){
		case "abs": return("abs"); //absolute value
		case "acos": return("acos"); //arccosine
		case "asin": return("asin"); //arcsine
		case "atan": return("atan"); //arctan
		case "bound": return("fx::bound"); //bound
		case "cbrt": return("cbrt"); //cube root
		case "ceil": return("ceil"); //ceiling
		case "choose": return("fx::choose"); //n choose k
		case "cos": return("cos"); //cosine
		case "cosh": return("cosh"); //hyperbolic cosine
		case "erf": return("erf"); //error function
		case "exp": return("exp"); //exp
		case "fact": return("fx::fact"); //factorial
		case "floor": return("floor"); //floor
		case "gamma": return("gamma"); //gamma
		case "hypot": return("hypot"); //hypotenuse
		case "if": return("fx::ifElse"); //if
		case "invErf": return("fx::invErf"); //inverse error function
		case "log": return("log"); //natural log
		case "logb": return("fx::logb"); //log base b
		case "logGamma": return("lgamma"); //log gamma
		case "log10": return("log10"); //log base-10
		case "max": return("max"); //max(a,b)
		case "min": return("min"); //min(a,b)
		case "probRescale": return("fx::probRescale"); //prob to prob
		case "probToRate": return("fx::probToRate"); //prob to rate
		case "rateToProb": return("fx::rateToProb"); //rate to prob
		case "round": return("fx::round"); //round
		case "sin": return("sin"); //sine
		case "sinh": return("sinh"); //hyperbolic sine
		case "sqrt": return("sqrt"); //square root
		case "tan": return("tan"); //tan
		case "tanh": return("tanh"); //hyperbolic tan
		case "signum": return("fx::signum"); //signum
		//summary functions
		case "mean": return("fx::mean"); //mean
		case "product": return("fx::product"); //product
		case "quantile": return("fx::quantile"); //quantile
		case "sd": return("fx::sd"); //standard deviation
		case "sum": return("fx::sum"); //sum
		case "var": return("fx::var"); //variance
		
		} //end switch
		return(null); //fell through
		
	}

	public static String define(String fx){
		switch(fx){
		case "bound":{ //bound
			String function="	double bound(double x, double a, double b){\n";
			function+="		if(x<a){x=a;} //min\n";
			function+="		if(x>b){x=b;} //max\n";
			function+="		return(x);\n";
			function+="	}";
			return(function);
		}
		case "choose":{ //n choose k
			String function="	double choose(int n, int k){\n";
			// to do
			function+="		return(null);\n";
			function+="	}";
			return(function);
		}
		case "fact":{ //factorial
			String function="	int fact(int n){\n";
			// to do
			function+="		return(null);\n";
			function+="	}";
			return(function);
		}
		case "if":{ //if
			String function="	double ifElse(String expr, double a, double b){\n";
			// to do
			function+="		return(null);\n";
			function+="	}";
			return(function);
		}
		case "invErf":{ //inverse error function
			String function="	double invErf(double x){\n";
			// to do
			function+="		return(null);\n";
			function+="	}";
			return(function);
		}
		case "logb":{ //log base b
			String function="	double logb(double x, double b){\n";
			function+="			return(log(x)/log(b));\n";
			function+="	}";
			return(function);
		}
		case "probRescale":{ //prob to prob
			String function="	double probRescale(double p, double t1, double t2){\n";
			function+="		double r0=-log(1-p); //prob to rate\n";
			function+="		double r1=r0/t1; //rate per t1\n";
			function+="		double r2=r1*t2; //rate per t2\n";
			function+="		double pNew=1-exp(-r2); //prob per t2\n";
			function+="		return(pNew);\n";
			function+="	}";
			return(function);
		}
		case "probToRate":{ //prob to rate
			String function="	double probToRate(double p){\n";
			function+="		double r=-log(1-p); //prob to rate\n";
			function+="		return(r);\n";
			function+="	}\n";
			function+="	\n";
			function+="	double probToRate(double p, double t1, double t2){\n";
			function+="		double r0=-log(1-p); //prob to rate\n";
			function+="		double r1=r0/t1; //rate per t1\n";
			function+="		double r2=r1*t2; //rate per t2\n";
			function+="		return(r2);\n";
			function+="	}";
			return(function);
		}
		case "rateToProb":{ //rate to prob
			String function="	double rateToProb(double r){\n";
			function+="		double p=1-exp(-r); //rate to prob\n";
			function+="		return(p);\n";
			function+="	}\n";
			function+="	\n";
			function+="	double rateToProb(double r, double t1, double t2){\n";
			function+="		double r1=r/t1; //rate per t1\n";
			function+="		double r2=r1*t2; //rate per t2\n";
			function+="		double p=1-exp(-r2); //prob per t2\n";
			function+="		return(p);\n";
			function+="	}";
			return(function);
		}
		case "round":{ //round
			String function="	int round(double x){\n";
			function+="		return(round(x));\n";
			function+="	}\n";
			function+="	\n";
			function+="	double round(double x, in n){\n";
			function+="		double digits=pow(10,n);\n";
			function+="		return(round(x*digits)/digits);\n";
			function+="	}";
			return(function);
		}
		case "signum": { //signum
			String function="	int signum(double x):\n";
			function+="  	if(x<0){return(-1);}\n";
			function+="  	else if(x==0){return(0);}\n";
			function+="  	else{return(1);}\n";
			function+="	}";
			function+="	\n";
			function+="	int signum(int x):\n";
			function+="  	if(x<0){return(-1);}\n";
			function+="  	else if(x==0){return(0);}\n";
			function+="  	else{return(1);}\n";
			function+="	}";
			return(function);
		}
		
		//Summary functions
		case "mean":{ //mean
			String function="	double mean(double * x){\n";
			function+="		int count=sizeof(x);\n";
			function+="		double sum=0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			sum+=x[i];\n";
			function+="		}\n";
			function+="		return(sum/(count*1.0));\n";
			function+="	}\n";
			function+="	\n";
			function+="	double mean(int n_args, ...){\n";
			function+="		va_list args;\n";
			function+="		va_start(args,n_args);\n";
			function+="		int count=n_args;\n";
			function+="		double sum=0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			sum+=va_args(args,double);\n";
			function+="		}\n";
			function+="		va_end(args);\n";
			function+="		return(sum/(count*1.0));\n";
			function+="	}";
			return(function);
		}
		case "product":{ //product
			String function="	double product(double * x){\n";
			function+="		int count=sizeof(x);\n";
			function+="		double product=1.0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			product*=x[i];\n";
			function+="		}\n";
			function+="		return(product);\n";
			function+="	}\n";
			function+="	\n";
			function+="	double product(int n_args, ...){\n";
			function+="		va_list args;\n";
			function+="		va_start(args,n_args);\n";
			function+="		int count=n_args;\n";
			function+="		double product=1.0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			product*=va_args(args,double);\n";
			function+="		}\n";
			function+="		va_end(args);\n";
			function+="		return(product);\n";
			function+="	}";
			return(function);
		}
		case "quantile":{ //quantile
			String function="	double quantile(double q, double * x){\n";
			function+="		int count=sizeof(x);\n";
			function+="		double vals[]=new double[count];\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			vals[i]=x[i];\n";
			function+="		}\n";
			function+="		Arrays.sort(vals);\n";
			function+="		int index=(int) (q*count);\n";
			function+="		return(vals[index]);\n";
			function+="	}\n";
			function+="	\n";
			function+="	private static double quantile(double q, double...args){\n";
			function+="		int count=args.length;\n";
			function+="		double vals[]=new double[count];\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			vals[i]=args[i];\n";
			function+="		}\n";
			function+="		Arrays.sort(vals);\n";
			function+="		int index=(int) (q*count);\n";
			function+="		return(vals[index]);\n";
			function+="	}";
			return(function);
		}
		case "sd":{ //standard deviation
			String function="	private static double sd(double * x){\n";
			function+="		int count=sizeof(x);\n";
			function+="		double eX=0, eX2;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			eX+=x[i];\n";
			function+="			eX2+=(x[i]*x[i]);\n";
			function+="		}\n";
			function+="		eX/=(count*1.0);\n";
			function+="		eX2/=(count*1.0);\n";
			function+="		return(sqrt(eX2-(eX*eX)));\n";
			function+="	}\n";
			function+="	\n";
			function+="	private static double sd(double...args){\n";
			function+="		int count=args.length;\n";
			function+="		double eX=0, eX2;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			eX+=args[i];\n";
			function+="			eX2+=(args[i]*args[i]);\n";
			function+="		}\n";
			function+="		eX/=(count*1.0);\n";
			function+="		eX2/=(count*1.0);\n";
			function+="		return(Math.sqrt(eX2-(eX*eX)));\n";
			function+="	}";
			return(function);
		}
		case "sum":{ //sum
			String function="	private static double sum(double x[]){\n";
			function+="		int count=x.length;\n";
			function+="		double sum=0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			sum+=x[i];\n";
			function+="		}\n";
			function+="		return(sum);\n";
			function+="	}\n";
			function+="	\n";
			function+="	private static double sum(double...args){\n";
			function+="		int count=args.length;\n";
			function+="		double sum=0;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			sum+=args[i];\n";
			function+="		}\n";
			function+="		return(sum);\n";
			function+="	}";
			return(function);
		}
		case "var":{ //variance
			String function="	private static double var(double x[]){\n";
			function+="		int count=x.length;\n";
			function+="		double eX=0, eX2;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			eX+=x[i];\n";
			function+="			eX2+=(x[i]*x[i]);\n";
			function+="		}\n";
			function+="		eX/=(count*1.0);\n";
			function+="		eX2/=(count*1.0);\n";
			function+="		return(eX2-(eX*eX));\n";
			function+="	}\n";
			function+="	\n";
			function+="	private static double var(double...args){\n";
			function+="		int count=args.length;\n";
			function+="		double eX=0, eX2;\n";
			function+="		for(int i=0; i<count; i++){\n";
			function+="			eX+=args[i];\n";
			function+="			eX2+=(args[i]*args[i]);\n";
			function+="		}\n";
			function+="		eX/=(count*1.0);\n";
			function+="		eX2/=(count*1.0);\n";
			function+="		return(eX2-(eX*eX));\n";
			function+="	}";
			return(function);
		}
		
		} //end switch
		return(null); //fell through
		
	}
		

}