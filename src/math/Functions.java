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

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;

public final class Functions{

	public static boolean isFunction(String word){
		switch(word){
		case "abs": return(true);
		case "acos": return(true);
		case "asin": return(true);
		case "atan": return(true);
		case "bound": return(true);
		case "cbrt": return(true);
		case "ceil": return(true);
		case "choose": return(true);
		case "cos": return(true);
		case "cosh": return(true);
		case "erf": return(true);
		case "exp": return(true);
		case "fact": return(true);
		case "floor": return(true);
		case "gamma": return(true);
		case "hypot": return(true);
		case "if": return(true);
		case "invErf": return(true);
		case "log": return(true);
		case "logb": return(true);
		case "logGamma": return(true);
		case "log10": return(true);
		case "max": return(true);
		case "min": return(true);
		case "probRescale": return(true);
		case "probToRate": return(true);
		case "rateToProb": return(true);
		case "round": return(true);
		case "sin": return(true);
		case "sinh": return(true);
		case "sqrt": return(true);
		case "tan": return(true);
		case "tanh": return(true);
		case "signum": return(true);
		//Summary functions
		case "mean": return(true);
		case "product": return(true);
		case "quantile": return(true);
		case "sd": return(true);
		case "sum": return(true);
		case "var": return(true);
		
		}
		return(false); //fell through
	}

	public static Numeric evaluate(String fx, Numeric...args) throws NumericException{
		switch(fx){
		case "abs":{ //absolute value 
			if(args.length!=1){throw new NumericException("Function takes 1 argument","abs");}
			if(args[0].format==Format.INTEGER){
				return(new Numeric(Math.abs(args[0].getInt())));
			}
			else if(args[0].format==Format.DOUBLE){
				return(new Numeric(Math.abs(args[0].getDouble())));
			}
			else if(args[0].format==Format.MATRIX){
				double rMatrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						rMatrix[i][j]=Math.abs(args[0].matrix[i][j]);
					}
				}
				return(new Numeric(rMatrix));
			}
		}
		case "acos":{ //arccosine
			if(args.length!=1){throw new NumericException("Function takes 1 argument","acos");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","acos");}
				return(new Numeric(Math.acos(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","acos");}
						matrix[i][j]=Math.acos(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "asin":{ //arcsine
			if(args.length!=1){throw new NumericException("Function takes 1 argument","asin");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","asin");}
				return(new Numeric(Math.asin(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","acos");}
						matrix[i][j]=Math.asin(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "atan":{ //arctan
			if(args.length!=1){throw new NumericException("Function takes 1 argument","atan");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.atan(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.atan(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "bound":{
			if(args.length!=3){throw new NumericException("Function takes 3 arguments","bound");}
			double a=args[1].getDouble(), b=args[2].getDouble();
			if(a>=b){throw new NumericException("a should be <b","bound");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<a){x=a;} //min
				if(x>b){x=b;} //max
				return(new Numeric(x));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<a){x=a;} //min
						if(x>b){x=b;} //max
						matrix[i][j]=x;
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "cbrt":{ //cube root
			if(args.length!=1){throw new NumericException("Function takes 1 argument","cbrt");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cbrt(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.cbrt(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "ceil":{ //ceiling
			if(args.length!=1){throw new NumericException("Function takes 1 argument","ceil");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric((int)Math.ceil(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=(int)Math.ceil(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "choose":{ //n choose k
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","choose");}
			return(new Numeric(MathUtils.choose(args[0].getInt(), args[1].getInt()))); 
		}
		case "cos":{ //cosine
			if(args.length!=1){throw new NumericException("Function takes 1 argument","cos");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cos(args[0].getDouble()))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.cos(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "cosh":{ //hyperbolic cosine
			if(args.length!=1){throw new NumericException("Function takes 1 argument","cosh");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cosh(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.cosh(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "erf":{ //error function
			if(args.length!=1){throw new NumericException("Function takes 1 argument","erf");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Erf.erf(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Erf.erf(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "exp":{ //exp
			if(args.length!=1){throw new NumericException("Function takes 1 argument","exp");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.exp(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.exp(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "fact": { //factorial
			if(args.length!=1){throw new NumericException("Function takes 1 argument","fact");}
			int n=args[0].getInt();
			if(n<0){throw new NumericException("n should be ≥0","fact");}
			return(new Numeric(MathUtils.factorial(args[0].getInt()))); 
		}
		case "floor":{ //floor
			if(args.length!=1){throw new NumericException("Function takes 1 argument","floor");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric((int)Math.floor(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=(int)Math.floor(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "gamma":{ //gamma
			if(args.length!=1){throw new NumericException("Function takes 1 argument","gamma");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<=0){throw new NumericException("x should be >0","gamma");}
				return(new Numeric(Gamma.gamma(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException("x should be >0","gamma");}
						matrix[i][j]=Gamma.gamma(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "hypot":{ //hypotenuse
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","hypot");}
			return(new Numeric(Math.hypot(args[0].getDouble(),args[1].getDouble()))); 
		}
		case "if":{
			if(args.length!=3){throw new NumericException("Function takes 3 arguments","if");}
			if(args[0].getBool()==true){return(args[1]);}
			else{return(args[2]);}
		}
		case "invErf":{ //inverse error function
			if(args.length!=1){throw new NumericException("Function takes 1 argument","invErf");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","invErf");}
				return(new Numeric(Erf.erfInv(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException("x should be in [-1,1]","invErf");}
						matrix[i][j]=Erf.erfInv(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "log":{ //natural log
			if(args.length!=1){throw new NumericException("Function takes 1 argument","log");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<=0){throw new NumericException("x should be >0","log");}
				return(new Numeric(Math.log(args[0].getDouble()))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException("x should be >0","log");}
						matrix[i][j]=Math.log(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "logb": { //log base b
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","logb");}
			if(args[0].format!=Format.MATRIX){ //real number
				double x=args[0].getDouble();
				if(x<=0){throw new NumericException("x should be >0","logb");}
				double b=args[1].getDouble();
				if(b<=0){throw new NumericException("b should be >0","logb");}
				double result=Math.log(x)/Math.log(b);
				return(new Numeric(result)); 
			}
			else{ //matrix
				double b=args[1].getDouble();
				if(b<=0){throw new NumericException("b should be >0","logb");}
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException("x should be >0","logb");}
						matrix[i][j]=Math.log(x)/Math.log(b);
					}
				}
				return(new Numeric(matrix));
			}
		}
		
		case "logGamma":{ //log gamma
			if(args.length!=1){throw new NumericException("Function takes 1 argument","logGamma");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<=0){throw new NumericException("x should be >0","logGamma");}
				return(new Numeric(Gamma.logGamma(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException("x should be >0","logGamma");}
						matrix[i][j]=Gamma.logGamma(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "log10":{ //log base 10
			if(args.length!=1){throw new NumericException("Function takes 1 argument","log10");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<=0){throw new NumericException("x should be >0","log10");}
				return(new Numeric(Math.log10(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException("x should be >0","log10");}
						matrix[i][j]=Math.log10(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "max":{ //max(a,b)
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","max");}
			if(args[0].isInteger() && args[1].isInteger()){ //preserve integer type
				return(new Numeric(Math.max(args[0].getInt(), args[1].getInt())));
			}
			else{ //at least 1 double
				return(new Numeric(Math.max(args[0].getDouble(), args[1].getDouble())));
			}
		}
		case "min":{ //min(a,b)
			if(args.length!=2){throw new NumericException("Function takes 2 arguments","min");}
			if(args[0].isInteger() && args[1].isInteger()){ //preserve integer type
				return(new Numeric(Math.min(args[0].getInt(), args[1].getInt())));
			}
			else{ //at least 1 double
				return(new Numeric(Math.min(args[0].getDouble(), args[1].getDouble())));
			}
		}
		case "probRescale":{ //prob to prob
			if(args.length!=3){throw new NumericException("Function takes 3 arguments","probRescale");}
			double t1=args[1].getDouble();
			double t2=args[2].getDouble();
			if(t1<=0){throw new NumericException("Original time interval should be >0","prob");}
			if(t2<=0){throw new NumericException("New time interval should be >0","prob");}
			if(args[0].format!=Format.MATRIX){
				double p=args[0].getProb();
				double r0=-Math.log(1-p); //prob to rate
				double r1=r0/t1; //rate per t1
				double r2=r1*t2; //rate per t2
				double pNew=1-Math.exp(-r2); //prob per t2
				return(new Numeric(pNew)); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double p=args[0].matrix[i][j];
						if(p<0 || p>1){throw new NumericException("Invalid probability ("+p+")","probRescale");}
						double r0=-Math.log(1-p); //prob to rate
						double r1=r0/t1; //rate per t1
						double r2=r1*t2; //rate per t2
						double pNew=1-Math.exp(-r2); //prob per t2
						matrix[i][j]=pNew;
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "probToRate":{ //prob to rate
			if(args.length!=1 && args.length!=3){throw new NumericException("Function takes 1 or 3 arguments","probToRate");}
			double tProb=1.0, tRate=1.0;
			if(args.length==3){
				tProb=args[1].getDouble();
				tRate=args[2].getDouble();
				if(tRate<=0){throw new NumericException("Rate time interval should be >0","prob");}
				if(tProb<=0){throw new NumericException("Probability time interval should be >0","prob");}
			}
			if(args[0].format!=Format.MATRIX){
				double p=args[0].getProb();
				double r0=-Math.log(1-p); //prob to rate
				double r1=r0/tProb; //rate per t1
				double r2=r1*tRate; //rate per t2
				return(new Numeric(r2)); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double p=args[0].matrix[i][j];
						if(p<0 || p>1){throw new NumericException("Invalid probability ("+p+")","probToRate");}
						double r0=-Math.log(1-p); //prob to rate
						double r1=r0/tProb; //rate per t1
						double r2=r1*tRate; //rate per t2
						matrix[i][j]=r2;
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "rateToProb":{ //rate to prob
			if(args.length!=1 && args.length!=3){throw new NumericException("Function takes 1 or 3 arguments","rateToProb");}
			double tRate=1.0, tProb=1.0;
			if(args.length==3){
				tRate=args[1].getDouble();
				tProb=args[2].getDouble();
				if(tRate<=0){throw new NumericException("Rate time interval should be >0","prob");}
				if(tProb<=0){throw new NumericException("Probability time interval should be >0","prob");}
			}
			if(args[0].format!=Format.MATRIX){
				double r=args[0].getDouble();
				if(r<0){throw new NumericException("Rate should be ≥0","rateToProb");}
				double r1=r/tRate; //rate per t1
				double r2=r1*tProb; //rate per t2
				double p=1-Math.exp(-r2);
				return(new Numeric(p)); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double r=args[0].matrix[i][j];
						if(r<0){throw new NumericException("Rate should be ≥0","rateToProb");}
						double r1=r/tRate; //rate per t1
						double r2=r1*tProb; //rate per t2
						double p=1-Math.exp(-r2);
						matrix[i][j]=p;
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "round":{
			if(args.length==1){ //round to integer
				if(args[0].format!=Format.MATRIX){
					return(new Numeric((int)Math.round(args[0].getDouble())));
				}
				else{
					double matrix[][]=new double[args[0].nrow][args[0].ncol];
					for(int i=0; i<args[0].nrow; i++){
						for(int j=0; j<args[0].ncol; j++){
							double x=args[0].matrix[i][j];
							matrix[i][j]=(int)Math.round(x);
						}
					}
					return(new Numeric(matrix));
				}
			}
			else if(args.length==2){
				int n=args[1].getInt();
				if(n<0){throw new NumericException("Number of decimal places should be ≥0","round");}
				double digits=Math.pow(10, args[1].getInt());
				if(args[0].format!=Format.MATRIX){
					return(new Numeric(Math.round(args[0].getDouble()*digits)/digits));
				}
				else{
					double matrix[][]=new double[args[0].nrow][args[0].ncol];
					for(int i=0; i<args[0].nrow; i++){
						for(int j=0; j<args[0].ncol; j++){
							double x=args[0].matrix[i][j];
							matrix[i][j]=Math.round(x*digits)/digits;
						}
					}
					return(new Numeric(matrix));
				}
			}
			else{throw new NumericException("Function takes 1 or 2 arguments","round");}
		}
		case "sin":{ //sin
			if(args.length!=1){throw new NumericException("Function takes 1 argument","sin");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.sin(args[0].getDouble()))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.sin(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "sinh":{ //hyperbolic sin
			if(args.length!=1){throw new NumericException("Function takes 1 argument","sinh");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.sinh(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.sinh(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "sqrt":{ //square root
			if(args.length!=1){throw new NumericException("Function takes 1 argument","sqrt");}
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble();
				if(x<0){throw new NumericException("x should be ≥0","sqrt");}
				return(new Numeric(Math.sqrt(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<0){throw new NumericException("x should be ≥0","sqrt");}
						matrix[i][j]=Math.sqrt(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "tan":{ //tan
			if(args.length!=1){throw new NumericException("Function takes 1 argument","tan");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.tan(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.tan(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "tanh":{ //hyperbolic tan
			if(args.length!=1){throw new NumericException("Function takes 1 argument","tanh");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.tanh(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.tanh(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "signum":{ //signum
			if(args.length!=1){throw new NumericException("Function takes 1 argument","signum");}
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.signum(args[0].getDouble())));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=Math.signum(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		
		//Summary functions
		case "mean":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException("Function takes at least 1 argument","mean");}
			double sum=0;
			int count=0;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					sum+=args[i].getDouble();
					count++;
				}
				else{
					for(int r=0; r<args[i].nrow; r++){
						for(int c=0; c<args[i].ncol; c++){
							sum+=args[i].matrix[r][c];
							count++;
						}
					}
				}
			}
			double mean=sum/(count*1.0);
			return(new Numeric(mean));
		}
		case "product":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException("Function takes at least 1 argument","product");}
			double prod=1;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					prod*=args[i].getDouble();
				}
				else{
					for(int r=0; r<args[i].nrow; r++){
						for(int c=0; c<args[i].ncol; c++){
							prod*=args[i].matrix[r][c];
						}
					}
				}
			}
			return(new Numeric(prod));
		}
		case "quantile":{
			int numArgs=args.length;
			if(numArgs<2){throw new NumericException("Function takes at least 2 arguments","quantile");}
			//Get quantiles to evaluate
			double q[];
			if(args[0].format!=Format.MATRIX){
				q=new double[1];
				q[0]=args[0].getDouble();
				if(q[0]<0 || q[0]>1){throw new NumericException("q should be in [0,1] but was "+q[0],"quantile");}
			}
			else{
				if(args[0].nrow!=1){throw new NumericException("q should be a row vector","quantile");}
				q=new double[args[0].ncol];
				for(int i=0; i<q.length; i++){
					q[i]=args[0].matrix[0][i];
					if(q[i]<0 || q[i]>1){throw new NumericException("q["+i+"] should be in [0,1] but was "+q[i],"quantile");}
				}
			}
			//Evaluate quantiles
			ArrayList<Double> x=new ArrayList<Double>();
			for(int i=1; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					x.add(args[i].getDouble());
				}
				else{
					for(int r=0; r<args[i].nrow; r++){
						for(int c=0; c<args[i].ncol; c++){
							x.add(args[i].matrix[r][c]);
						}
					}
				}
			}
			Collections.sort(x);
			double quantiles[][]=new double[1][q.length];
			int lenX=x.size()-1;
			for(int i=0; i<q.length; i++){
				int index=(int) (q[i]*lenX);
				quantiles[0][i]=x.get(index);
			}
			if(q.length==1){return new Numeric(quantiles[0][0]);} //one number
			else{return(new Numeric(quantiles));} //row vector
		}
		case "sd":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException("Function takes at least 1 argument","sd");}
			double var=MathUtils.var(args);
			double sd=Math.sqrt(var);
			return(new Numeric(sd));
		}
		case "sum":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException("Function takes at least 1 argument","sum");}
			double sum=0;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					sum+=args[i].getDouble();
				}
				else{
					for(int r=0; r<args[i].nrow; r++){
						for(int c=0; c<args[i].ncol; c++){
							sum+=args[i].matrix[r][c];
						}
					}
				}
			}
			return(new Numeric(sum));
		}
		case "var":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException("Function takes at least 1 argument","var");}
			double var=MathUtils.var(args);
			return(new Numeric(var));
		}
		
		}
		return(new Numeric(Double.NaN)); //fell through
	}

	public static String getDescription(String fx){
		String des="";
		switch(fx){
		case "abs": 
			des="<html><b>Absolute Value</b><br>";
			des+=MathUtils.consoleFont("<b>abs</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the absolute value of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "acos": 
			des="<html><b>Arccosine</b><br>";
			des+=MathUtils.consoleFont("<b>acos</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the arccosine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) in "+MathUtils.consoleFont("[-1,1]")+"<br>";
			des+="</html>";
			return(des);
		case "asin": 
			des="<html><b>Arcsine</b><br>";
			des+=MathUtils.consoleFont("<b>asin</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the arcsine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) in "+MathUtils.consoleFont("[-1,1]")+"<br>";
			des+="</html>";
			return(des);
		case "atan": 
			des="<html><b>Arctangent</b><br>";
			des+=MathUtils.consoleFont("<b>atan</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the arctangent of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "bound": 
			des="<html><b>Bounded Value</b><br>";
			des+=MathUtils.consoleFont("<b>bound</b>","#800000")+MathUtils.consoleFont("(x,a,b)")+": Bounds "+MathUtils.consoleFont("x")+" to be in "+MathUtils.consoleFont("[a,b]")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+=MathUtils.consoleFont("a")+": Lower bound (inclusive), real number<br>";
			des+=MathUtils.consoleFont("b")+": Upper bound (inclusive), real number<br>";
			des+="</html>";
			return(des);
		case "cbrt": 
			des="<html><b>Cube Root</b><br>";
			des+=MathUtils.consoleFont("<b>cbrt</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the cube root of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "ceil": 
			des="<html><b>Ceiling</b><br>";
			des+=MathUtils.consoleFont("<b>ceil</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the nearest integer above "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "choose": 
			des="<html><b>Binomial Coefficient</b><br>";
			des+=MathUtils.consoleFont("<b>choose</b>","#800000")+MathUtils.consoleFont("(n,k)")+": Returns the Binomial coefficient ("+MathUtils.consoleFont("n")+" choose "+MathUtils.consoleFont("k")+")<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("n")+": Integer<br>";
			des+=MathUtils.consoleFont("k")+": Integer<br>";
			des+="</html>";
			return(des);
		case "cos": 
			des="<html><b>Cosine</b><br>";
			des+=MathUtils.consoleFont("<b>cos</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the cosine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "cosh": 
			des="<html><b>Hyperbolic Cosine</b><br>";
			des+=MathUtils.consoleFont("<b>cosh</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the hyperbolic cosine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "erf": 
			des="<html><b>Error Function</b><br>";
			des+=MathUtils.consoleFont("<b>erf</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the error function evaluated at "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "exp": 
			des="<html><b>Exponential Function</b><br>";
			des+=MathUtils.consoleFont("<b>exp</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns Euler's number "+MathUtils.consoleFont("e")+" raised to the power of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "fact": 
			des="<html><b>Factorial</b><br>";
			des+=MathUtils.consoleFont("<b>fact</b>","#800000")+MathUtils.consoleFont("(n)")+": Returns "+MathUtils.consoleFont("n!")+" (the factorial of "+MathUtils.consoleFont("n")+")<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("n")+": Integer "+MathUtils.consoleFont("≥0")+"<br>";
			des+="</html>";
			return(des);
		case "floor": 
			des="<html><b>Floor</b><br>";
			des+=MathUtils.consoleFont("<b>floor</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the nearest integer below "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "gamma": 
			des="<html><b>Gamma Function</b><br>";
			des+=MathUtils.consoleFont("<b>gamma</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the gamma function evaluated at "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)"+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "hypot": 
			des="<html><b>Hypotenuse</b><br>";
			des+=MathUtils.consoleFont("<b>hypot</b>","#800000")+MathUtils.consoleFont("(x,y)")+": Returns "+MathUtils.consoleFont("sqrt(x^2+y^2)")+" without intermediate overflow or underflow<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number <br>";
			des+=MathUtils.consoleFont("y")+": Real number <br>";
			des+="</html>";
			return(des);
		case "if": 
			des="<html><b>If Function</b><br>";
			des+=MathUtils.consoleFont("<b>if</b>","#800000")+MathUtils.consoleFont("(<i>expr</i>,a,b)")+": Returns "+MathUtils.consoleFont("a")+" if the expression is "+MathUtils.consoleFont("true")+" and "+MathUtils.consoleFont("b")+" if it is "+MathUtils.consoleFont("false")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("<i>expr</i>")+": Boolean expression to evaluate<br>";
			des+=MathUtils.consoleFont("a")+": Object returned if "+MathUtils.consoleFont("true")+"<br>";
			des+=MathUtils.consoleFont("b")+": Object returned if "+MathUtils.consoleFont("false")+"<br>";
			des+="</html>";
			return(des);
		case "invErf": 
			des="<html><b>Inverse Error Function</b><br>";
			des+=MathUtils.consoleFont("<b>invErf</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the inverse error function evaluated at "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) in "+MathUtils.consoleFont("[-1,1]")+"<br>";
			des+="</html>";
			return(des);
		case "log": 
			des="<html><b>Natural Log</b><br>";
			des+=MathUtils.consoleFont("<b>log</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the natural logarithm (base "+MathUtils.consoleFont("e")+") of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "logb": 
			des="<html><b>Base-b Log</b><br>";
			des+=MathUtils.consoleFont("<b>log</b>","#800000")+MathUtils.consoleFont("(x,b)")+": Returns the base-b logarithm of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) "+MathUtils.consoleFont(">0")+"<br>";
			des+=MathUtils.consoleFont("b")+": Real number "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "logGamma": 
			des="<html><b>Log-Gamma Function</b><br>";
			des+=MathUtils.consoleFont("<b>logGamma</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the log-Gamma function evaluated at "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "log10": 
			des="<html><b>Base-10 Log</b><br>";
			des+=MathUtils.consoleFont("<b>log10</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the base-"+MathUtils.consoleFont("10")+" logarithm of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "max": 
			des="<html><b>Maximum</b><br>";
			des+=MathUtils.consoleFont("<b>max</b>","#800000")+MathUtils.consoleFont("(a,b)")+": Returns the maximum of "+MathUtils.consoleFont("a")+" and "+MathUtils.consoleFont("b")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("a")+": Real number <br>";
			des+=MathUtils.consoleFont("b")+": Real number <br>";
			des+="</html>";
			return(des);
		case "min": 
			des="<html><b>Minimum</b><br>";
			des+=MathUtils.consoleFont("<b>min</b>","#800000")+MathUtils.consoleFont("(a,b)")+": Returns the minimum of "+MathUtils.consoleFont("a")+" and "+MathUtils.consoleFont("b")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("a")+": Real number <br>";
			des+=MathUtils.consoleFont("b")+": Real number <br>";
			des+="</html>";
			return(des);
		case "probRescale": 
			des="<html><b>Probability Rescale</b><br>";
			des+=MathUtils.consoleFont("<b>probRescale</b>","#800000")+MathUtils.consoleFont("(p,t1,t2)")+": Rescales a probability "+MathUtils.consoleFont("p")+" from time interval "+MathUtils.consoleFont("t1")+" to a new time interval "+MathUtils.consoleFont("t2")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("p")+": Probability (or matrix) in "+MathUtils.consoleFont("[0,1]")+"<br>";
			des+=MathUtils.consoleFont("t1")+": Original time interval "+MathUtils.consoleFont(">0")+"<br>";
			des+=MathUtils.consoleFont("t2")+": New time interval "+MathUtils.consoleFont(">0")+"<br>";
			des+="</html>";
			return(des);
		case "probToRate": 
			des="<html><b>Probability to Rate</b><br>";
			des+="<i>Functions</i><br>";
			des+=MathUtils.consoleFont("<b>probToRate</b>","#800000")+MathUtils.consoleFont("(p)")+": Converts a probability "+MathUtils.consoleFont("p")+" to a rate for a fixed time interval<br>";
			des+=MathUtils.consoleFont("<b>probToRate</b>","#800000")+MathUtils.consoleFont("(p,t1,t2)")+": Converts a probability "+MathUtils.consoleFont("p")+" per time interval "+MathUtils.consoleFont("t1")+" to a rate per time interval "+MathUtils.consoleFont("t2")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("p")+": Probability (or matrix) "+MathUtils.consoleFont("[0,1]")+"<br>";
			des+=MathUtils.consoleFont("t1")+": Probability time interval "+MathUtils.consoleFont(">0")+" (optional)<br>";
			des+=MathUtils.consoleFont("t2")+": Rate time interval "+MathUtils.consoleFont(">0")+" (optional)<br>";
			des+="</html>";
			return(des);
		case "rateToProb": 
			des="<html><b>Rate to Probability</b><br>";
			des+="<i>Functions</i><br>";
			des+=MathUtils.consoleFont("<b>rateToProb</b>","#800000")+MathUtils.consoleFont("(r)")+": Converts a rate "+MathUtils.consoleFont("r")+" to a probability for a fixed time interval<br>";
			des+=MathUtils.consoleFont("<b>rateToProb</b>","#800000")+MathUtils.consoleFont("(r,t1,t2)")+": Converts a rate "+MathUtils.consoleFont("r")+" per time interval "+MathUtils.consoleFont("t1")+" to a probability per time interval "+MathUtils.consoleFont("t2")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("r")+": Rate (or matrix) "+MathUtils.consoleFont("≥0")+"<br>";
			des+=MathUtils.consoleFont("t1")+": Rate time interval "+MathUtils.consoleFont(">0")+" (optional)<br>";
			des+=MathUtils.consoleFont("t2")+": Probability time interval "+MathUtils.consoleFont(">0")+" (optional)<br>";
			des+="</html>";
			return(des);
		case "round": 
			des="<html><b>Round</b><br>";
			des+="<i>Functions</i><br>";
			des+=MathUtils.consoleFont("<b>round</b>","#800000")+MathUtils.consoleFont("(x)")+": Rounds "+MathUtils.consoleFont("x")+" to the nearest integer<br>";
			des+=MathUtils.consoleFont("<b>round</b>","#800000")+MathUtils.consoleFont("(x,n)")+": Rounds "+MathUtils.consoleFont("x")+" to "+MathUtils.consoleFont("n")+" decimal places<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+=MathUtils.consoleFont("n")+": Number of digits (Integer "+MathUtils.consoleFont("≥0")+") (optional)<br>";
			des+="</html>";
			return(des);
		case "sin": 
			des="<html><b>Sine</b><br>";
			des+=MathUtils.consoleFont("<b>sin</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the sine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "sinh": 
			des="<html><b>Hyperbolic Sine</b><br>";
			des+=MathUtils.consoleFont("<b>sinh</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the hyperbolic sine of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "sqrt": 
			des="<html><b>Square Root</b><br>";
			des+=MathUtils.consoleFont("<b>sqrt</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the square root of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix) ≥0<br>";
			des+="</html>";
			return(des);			
		case "tan": 
			des="<html><b>Tangent</b><br>";
			des+=MathUtils.consoleFont("<b>tan</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the tangent of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "tanh": 
			des="<html><b>Hyperbolic Tangent</b><br>";
			des+=MathUtils.consoleFont("<b>tanh</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the hyperbolic tangent of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		case "signum": 
			des="<html><b>Signum Function</b><br>";
			des+=MathUtils.consoleFont("<b>signum</b>","#800000")+MathUtils.consoleFont("(x)")+": Returns the sign of "+MathUtils.consoleFont("x")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x")+": Real number (or matrix)<br>";
			des+="</html>";
			return(des);
		
		//Summary functions
		case "mean":
			des="<html><b>Mean</b><br>";
			des+=MathUtils.consoleFont("<b>mean</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the mean of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		case "product":
			des="<html><b>Product</b><br>";
			des+=MathUtils.consoleFont("<b>product</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the product of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		case "quantile":
			des="<html><b>Quantile</b><br>";
			des+=MathUtils.consoleFont("<b>quantile</b>","#800000")+MathUtils.consoleFont("(q,x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the quantile(s) "+MathUtils.consoleFont("q")+" of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("q")+": Quantile(s) to evaluate. Real number (or row vector of real numbers) in "+MathUtils.consoleFont("[0,1]")+"<br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		case "sd":
			des="<html><b>Standard Deviation</b><br>";
			des+=MathUtils.consoleFont("<b>sd</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the standard deviation of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		case "sum":
			des="<html><b>Sum</b><br>";
			des+=MathUtils.consoleFont("<b>sum</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the sum of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		case "var":
			des="<html><b>Variance</b><br>";
			des+=MathUtils.consoleFont("<b>var</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": Returns the variance of "+MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+"<br>";
			des+="<br><i>Arguments</i><br>";
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": Real numbers (or matrices)<br>";
			des+="</html>";
			return(des);
		
		//end switch
		}

		return(des); //fell through
	}


}