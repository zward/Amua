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
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;

import lang.Language;

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
		case "logit": return(true);
		case "logistic": return(true);
		case "max": return(true);
		case "min": return(true);
		case "probRescale": return(true);
		case "probToRate": return(true);
		case "probit": return(true);
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

	public static Numeric evaluate(String fx, Language language, Numeric...args) throws NumericException{
		switch(fx){
		case "abs":{ //absolute value 
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"abs",language);} //Function takes 1 argument
			if(args[0].format==Format.INTEGER){
				return(new Numeric(Math.abs(args[0].getInt(language))));
			}
			else if(args[0].format==Format.DOUBLE){
				return(new Numeric(Math.abs(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"acos",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<-1 || x>1){throw new NumericException(language.message.getString("err.x_should_be_in_11"),"acos",language);} //x should be in [-1,1]
				return(new Numeric(Math.acos(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException(language.message.getString("err.x_should_be_in_11"),"acos",language);} //x should be in [-1,1]
						matrix[i][j]=Math.acos(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "asin":{ //arcsine
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"asin",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<-1 || x>1){throw new NumericException(language.message.getString("err.x_should_be_in_11"),"asin",language);} //x should be in [-1,1]
				return(new Numeric(Math.asin(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException(language.message.getString("err.x_should_be_in_11"),"acos",language);} //x should be in [-1,1]
						matrix[i][j]=Math.asin(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "atan":{ //arctan
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"atan",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.atan(args[0].getDouble(language))));
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
			if(args.length!=3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 3),"bound",language);} //Function takes 3 arguments
			double a=args[1].getDouble(language), b=args[2].getDouble(language);
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a","b"),"bound",language);} //a should be <b
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"cbrt",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cbrt(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"ceil",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric((int)Math.ceil(args[0].getDouble(language))));
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
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"choose",language);} //Function takes 2 arguments
			return(new Numeric(MathUtils.choose(args[0].getInt(language), args[1].getInt(language)))); 
		}
		case "cos":{ //cosine
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"cos",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cos(args[0].getDouble(language)))); 
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"cosh",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.cosh(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"erf",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Erf.erf(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"exp",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.exp(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"fact",language);} //Function takes 1 argument
			int n=args[0].getInt(language);
			if(n<0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte0"), "n"),"fact",language);} //n should be ≥0
			return(new Numeric(MathUtils.factorial(args[0].getInt(language)))); 
		}
		case "floor":{ //floor
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"floor",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric((int)Math.floor(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"gamma",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"gamma",language);} //x should be >0
				return(new Numeric(Gamma.gamma(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"gamma",language);} //x should be >0
						matrix[i][j]=Gamma.gamma(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "hypot":{ //hypotenuse
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"hypot",language);} //Function takes 2 arguments
			return(new Numeric(Math.hypot(args[0].getDouble(language),args[1].getDouble(language)))); 
		}
		case "if":{
			if(args.length!=3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 3),"if",language);} //Function takes 3 arguments
			if(args[0].getBool(language)==true){return(args[1]);}
			else{return(args[2]);}
		}
		case "invErf":{ //inverse error function
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"invErf",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<-1 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_11"),"invErf",language);} //x should be in [-1,1]
				return(new Numeric(Erf.erfInv(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<-1 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_11"),"invErf",language);} //x should be in [-1,1]
						matrix[i][j]=Erf.erfInv(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "log":{ //natural log
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"log",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"log",language);} //x should be >0"
				return(new Numeric(Math.log(args[0].getDouble(language)))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"log",language);} //x should be >0"
						matrix[i][j]=Math.log(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "logb": { //log base b
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"logb",language);} //Function takes 2 arguments
			if(args[0].format!=Format.MATRIX){ //real number
				double x=args[0].getDouble(language);
				if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"logb",language);} //x should be >0
				double b=args[1].getDouble(language);
				if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"logb",language);} //b should be >0
				double result=Math.log(x)/Math.log(b);
				return(new Numeric(result)); 
			}
			else{ //matrix
				double b=args[1].getDouble(language);
				if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"logb",language);} //b should be >0
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"logb",language);} //x should be >0
						matrix[i][j]=Math.log(x)/Math.log(b);
					}
				}
				return(new Numeric(matrix));
			}
		}
		
		case "logGamma":{ //log gamma
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"logGamma",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"logGamma",language);} //x should be >0
				return(new Numeric(Gamma.logGamma(x))); 
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"logGamma",language);} //x should be >0
						matrix[i][j]=Gamma.logGamma(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "log10":{ //log base 10
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"log10",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"log10",language);} //x should be >0
				return(new Numeric(Math.log10(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "x"),"log10",language);} //x should be >0
						matrix[i][j]=Math.log10(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "logit":{ //logit
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"logit",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<0 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_01"),"logit",language);} //p should be in [0,1]
				return(new Numeric(Math.log(x/(1.0-x))));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<0 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_01"),"logit",language);} //p should be in [0,1]
						matrix[i][j]=Math.log(x/(1.0-x));
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "logistic":{ //logistic
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"logistic",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				return(new Numeric(1.0/(1+Math.exp(-x))));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						matrix[i][j]=1.0/(1+Math.exp(-x));
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "max":{ //max(a,b)
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"max",language);} //Function takes 2 arguments
			if(args[0].isInteger() && args[1].isInteger()){ //preserve integer type
				return(new Numeric(Math.max(args[0].getInt(language), args[1].getInt(language))));
			}
			else{ //at least 1 double
				return(new Numeric(Math.max(args[0].getDouble(language), args[1].getDouble(language))));
			}
		}
		case "min":{ //min(a,b)
			if(args.length!=2){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 2),"min",language);} //Function takes 2 arguments
			if(args[0].isInteger() && args[1].isInteger()){ //preserve integer type
				return(new Numeric(Math.min(args[0].getInt(language), args[1].getInt(language))));
			}
			else{ //at least 1 double
				return(new Numeric(Math.min(args[0].getDouble(language), args[1].getDouble(language))));
			}
		}
		case "probRescale":{ //prob to prob
			if(args.length!=3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_n_args"), 3),"probRescale",language);} //Function takes 3 arguments
			double t1=args[1].getDouble(language);
			double t2=args[2].getDouble(language);
			if(t1<=0){throw new NumericException(language.message.getString("err.time_orig_pos"),"prob",language);} //Original time interval should be >0
			if(t2<=0){throw new NumericException(language.message.getString("err.time_new_pos"),"prob",language);} //New time interval should be >0
			if(args[0].format!=Format.MATRIX){
				double p=args[0].getProb(language);
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
						if(p<0 || p>1){throw new NumericException(language.message.getString("err.invalid_prob")+" ("+p+")","probRescale",language);} //Invalid probability
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
			if(args.length!=1 && args.length!=3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_one_two_args"), 1, 3),"probToRate",language);} //Function takes 1 or 3 arguments
			double tProb=1.0, tRate=1.0;
			if(args.length==3){
				tProb=args[1].getDouble(language);
				tRate=args[2].getDouble(language);
				if(tRate<=0){throw new NumericException(language.message.getString("err.rate_time_pos"),"prob",language);} //Rate time interval should be >0
				if(tProb<=0){throw new NumericException(language.message.getString("err.prob_time_pos"),"prob",language);} //Probability time interval should be >0
			}
			if(args[0].format!=Format.MATRIX){
				double p=args[0].getProb(language);
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
						if(p<0 || p>1){throw new NumericException(language.message.getString("err.invalid_prob")+" ("+p+")","probToRate",language);} //Invalid probability
						double r0=-Math.log(1-p); //prob to rate
						double r1=r0/tProb; //rate per t1
						double r2=r1*tRate; //rate per t2
						matrix[i][j]=r2;
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "probit":{ //probit
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"probit",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<0 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_01"),"probit",language);} //p should be in [0,1]
				return(new Numeric(Math.sqrt(2)*Erf.erfInv(2*x-1)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<0 || x>1){throw new NumericException(language.message.getString("err.val_should_be_in_01"),"probit",language);} //p should be in [0,1]
						matrix[i][j]=Math.sqrt(2)*Erf.erfInv(2*x-1);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "rateToProb":{ //rate to prob
			if(args.length!=1 && args.length!=3){throw new NumericException(MessageFormat.format(language.message.getString("err.fx_one_two_args"), 1, 3),"rateToProb",language);} //Function takes 1 or 3 arguments
			double tRate=1.0, tProb=1.0;
			if(args.length==3){
				tRate=args[1].getDouble(language);
				tProb=args[2].getDouble(language);
				if(tRate<=0){throw new NumericException(language.message.getString("err.rate_time_pos"),"prob",language);} //Rate time interval should be >0
				if(tProb<=0){throw new NumericException(language.message.getString("err.prob_time_pos"),"prob",language);} //Probability time interval should be >0
			}
			if(args[0].format!=Format.MATRIX){
				double r=args[0].getDouble(language);
				if(r<0){throw new NumericException(language.message.getString("err.rate_pos"),"rateToProb",language);} //Rate should be ≥0
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
						if(r<0){throw new NumericException(language.message.getString("err.rate_pos"),"rateToProb",language);} //Rate should be ≥0
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
					return(new Numeric((int)Math.round(args[0].getDouble(language))));
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
				int n=args[1].getInt(language);
				if(n<0){throw new NumericException(language.message.getString("err.num_decimal_pos"),"round",language);} //Number of decimal places should be ≥0
				double digits=Math.pow(10, args[1].getInt(language));
				if(args[0].format!=Format.MATRIX){
					return(new Numeric(Math.round(args[0].getDouble(language)*digits)/digits));
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
			else{throw new NumericException(MessageFormat.format(language.message.getString("err.fx_one_two_args"), 1, 2),"round",language);} //Function takes 1 or 2 arguments
		}
		case "sin":{ //sin
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"sin",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.sin(args[0].getDouble(language)))); 
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"sinh",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.sinh(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"sqrt",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				double x=args[0].getDouble(language);
				if(x<0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte0"), "x"),"sqrt",language);} //x should be ≥0"
				return(new Numeric(Math.sqrt(x)));
			}
			else{
				double matrix[][]=new double[args[0].nrow][args[0].ncol];
				for(int i=0; i<args[0].nrow; i++){
					for(int j=0; j<args[0].ncol; j++){
						double x=args[0].matrix[i][j];
						if(x<0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte0"), "x"),"sqrt",language);} //x should be ≥0"
						matrix[i][j]=Math.sqrt(x);
					}
				}
				return(new Numeric(matrix));
			}
		}
		case "tan":{ //tan
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"tan",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.tan(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"tanh",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.tanh(args[0].getDouble(language))));
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
			if(args.length!=1){throw new NumericException(language.message.getString("err.fx_one_arg"),"signum",language);} //Function takes 1 argument
			if(args[0].format!=Format.MATRIX){
				return(new Numeric(Math.signum(args[0].getDouble(language))));
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
			if(numArgs==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"mean",language);} //Function takes at least 1 argument
			double sum=0;
			int count=0;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					sum+=args[i].getDouble(language);
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
			if(numArgs==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"product",language);} //Function takes at least 1 argument
			double prod=1;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					prod*=args[i].getDouble(language);
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
			if(numArgs<2){throw new NumericException(language.message.getString("err.fx_gte2_args"),"quantile",language);} //Function takes at least 2 arguments
			//Get quantiles to evaluate
			double q[];
			if(args[0].format!=Format.MATRIX){
				q=new double[1];
				q[0]=args[0].getDouble(language);
				if(q[0]<0 || q[0]>1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_in_01_but"), "q", q[0]),"quantile",language);} //[val1] should be in [0,1] but was [val2]
			}
			else{
				if(args[0].nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "q"),"quantile",language);} //q should be a row vector
				q=new double[args[0].ncol];
				for(int i=0; i<q.length; i++){
					q[i]=args[0].matrix[0][i];
					if(q[i]<0 || q[i]>1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_in_01_but"), "q["+i+"]", q[i]),"quantile",language);} //q["+i+"] should be in [0,1] but was "+q[i],"quantile
				}
			}
			//Evaluate quantiles
			ArrayList<Double> x=new ArrayList<Double>();
			for(int i=1; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					x.add(args[i].getDouble(language));
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
			if(numArgs==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"sd",language);} //Function takes at least 1 argument
			double var=MathUtils.var(language, args);
			double sd=Math.sqrt(var);
			return(new Numeric(sd));
		}
		case "sum":{
			int numArgs=args.length;
			if(numArgs==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"sum",language);} //Function takes at least 1 argument
			double sum=0;
			for(int i=0; i<numArgs; i++){
				if(args[i].format!=Format.MATRIX){
					sum+=args[i].getDouble(language);
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
			if(numArgs==0){throw new NumericException(language.message.getString("err.fx_gte1_arg"),"var",language);} //Function takes at least 1 argument
			double var=MathUtils.var(language, args);
			return(new Numeric(var));
		}
		
		}
		return(new Numeric(Double.NaN)); //fell through
	}

	public static String getDescription(String fx, Language language){
		String des="";
		switch(fx){
		case "abs": 
			des="<html><b>"+language.math.getString("fx.abs_name")+"</b><br>"; //Absolute Value
			des+=MathUtils.consoleFont("<b>abs</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.abs_desc")+"<br>"; //Returns the absolute value of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "acos": 
			des="<html><b>"+language.math.getString("fx.acos_name")+"</b><br>"; //Arccosine
			des+=MathUtils.consoleFont("<b>acos</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.acos_desc")+"<br>"; //Returns the arccosine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_11")+"<br>"; //Real number (or matrix) in [-1,1]
			des+="</html>";
			return(des);
		case "asin": 
			des="<html><b>"+language.math.getString("fx.asin_name")+"</b><br>"; //Arcsine
			des+=MathUtils.consoleFont("<b>asin</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.asin_desc")+"<br>"; //Returns the arcsine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_11")+"<br>"; //Real number (or matrix) in [-1,1]
			des+="</html>";
			return(des);
		case "atan": 
			des="<html><b>"+language.math.getString("fx.atan_name")+"</b><br>"; //Arctangent
			des+=MathUtils.consoleFont("<b>atan</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.atan_desc")+"<br>"; //Returns the arctangent of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "bound": 
			des="<html><b>"+language.math.getString("fx.bound_name")+"</b><br>"; //Bounded Value
			des+=MathUtils.consoleFont("<b>bound</b>","#800000")+MathUtils.consoleFont("(x,a,b)")+": "+language.math.getString("fx.bound_desc")+"<br>"; //Bounds x to be in [a,b]
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+=MathUtils.consoleFont("a")+": "+language.math.getString("fx.lb_incl_real")+"<br>"; //Lower bound (inclusive), real number
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("fx.ub_incl_real")+"<br>"; //Upper bound (inclusive), real number
			des+="</html>";
			return(des);
		case "cbrt": 
			des="<html><b>"+language.math.getString("fx.cbrt_name")+"</b><br>"; //Cube Root
			des+=MathUtils.consoleFont("<b>cbrt</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.cbrt_desc")+"<br>"; //Returns the cube root of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "ceil": 
			des="<html><b>"+language.math.getString("fx.ceil_name")+"</b><br>"; //Ceiling
			des+=MathUtils.consoleFont("<b>ceil</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.ceil_desc")+"<br>"; //Returns the nearest integer above x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "choose": 
			des="<html><b>"+language.math.getString("fx.choose_name")+"</b><br>"; //Binomial Coefficient
			des+=MathUtils.consoleFont("<b>choose</b>","#800000")+MathUtils.consoleFont("(n,k)")+": "+language.math.getString("fx.choose_desc")+"<br>"; //Returns the Binomial coefficient (n choose k)
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("fx.integer")+"<br>"; //Integer
			des+=MathUtils.consoleFont("k")+": "+language.math.getString("fx.integer")+"<br>"; //Integer
			des+="</html>";
			return(des);
		case "cos": 
			des="<html><b>"+language.math.getString("fx.cos_name")+"</b><br>"; //Cosine
			des+=MathUtils.consoleFont("<b>cos</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.cos_desc")+"<br>"; //Returns the cosine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "cosh": 
			des="<html><b>"+language.math.getString("fx.cosh_name")+"</b><br>"; //Hyperbolic Cosine
			des+=MathUtils.consoleFont("<b>cosh</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.cosh_desc")+"<br>"; //Returns the hyperbolic cosine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "erf": 
			des="<html><b>"+language.math.getString("fx.erf_name")+"</b><br>"; //Error Function
			des+=MathUtils.consoleFont("<b>erf</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.erf_desc")+"<br>"; //Returns the error function evaluated at x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "exp": 
			des="<html><b>"+language.math.getString("fx.exp_name")+"</b><br>"; //Exponential Function
			des+=MathUtils.consoleFont("<b>exp</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.exp_desc")+"<br>"; //Returns Euler's number e raised to the power of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "fact": 
			des="<html><b>"+language.math.getString("fx.fact_name")+"</b><br>"; //Factorial
			des+=MathUtils.consoleFont("<b>fact</b>","#800000")+MathUtils.consoleFont("(n)")+": "+language.math.getString("fx.fact_desc")+"<br>"; //Returns n! (the factorial of n)
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("fx.integer_gte0")+"<br>"; //Integer ≥0
			des+="</html>";
			return(des);
		case "floor": 
			des="<html><b>"+language.math.getString("fx.floor_name")+"</b><br>"; //Floor
			des+=MathUtils.consoleFont("<b>floor</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.floor_desc")+"<br>"; //Returns the nearest integer below x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "gamma": 
			des="<html><b>"+language.math.getString("fx.gamma_name")+"</b><br>"; //Gamma Function
			des+=MathUtils.consoleFont("<b>gamma</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.gamma_desc")+"<br>"; //Returns the gamma function evaluated at x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_gt0")+"<br>"; //Real number (or matrix) >0
			des+="</html>";
			return(des);
		case "hypot": 
			des="<html><b>"+language.math.getString("fx.hypot_name")+"</b><br>"; //Hypotenuse
			des+=MathUtils.consoleFont("<b>hypot</b>","#800000")+MathUtils.consoleFont("(x,y)")+": "+language.math.getString("fx.hypot_desc")+"<br>"; //Returns sqrt(x^2+y^2) without intermediate overflow or underflow
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+=MathUtils.consoleFont("y")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+="</html>";
			return(des);
		case "if": 
			des="<html><b>"+language.math.getString("fx.if_name")+"</b><br>"; //If Function
			des+=MathUtils.consoleFont("<b>if</b>","#800000")+MathUtils.consoleFont("(<i>expr</i>,a,b)")+": "+language.math.getString("fx.if_desc")+"<br>"; //Returns a if the expression is true and b if it is false
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("<i>expr</i>")+": "+language.math.getString("fx.bool_expr_eval")+"<br>"; //Boolean expression to evaluate
			des+=MathUtils.consoleFont("a")+": "+language.math.getString("fx.obj_return_true")+"<br>"; //Object returned if true
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("fx.obj_return_false")+"<br>"; //Object returned if false
			des+="</html>";
			return(des);
		case "invErf": 
			des="<html><b>"+language.math.getString("fx.invErf_name")+"</b><br>"; //Inverse Error Function
			des+=MathUtils.consoleFont("<b>invErf</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.invErf_desc")+"<br>"; //Returns the inverse error function evaluated at x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_11")+"<br>"; //Real number (or matrix) in [-1,1]
			des+="</html>";
			return(des);
		case "log": 
			des="<html><b>"+language.math.getString("fx.log_name")+"</b><br>"; //Natural Log
			des+=MathUtils.consoleFont("<b>log</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.log_desc")+"<br>"; //Returns the natural logarithm (base e of x)
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+" "+MathUtils.consoleFont(">0")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "logb": 
			des="<html><b>"+language.math.getString("fx.logb_name")+"</b><br>"; //Base-b Log
			des+=MathUtils.consoleFont("<b>logb</b>","#800000")+MathUtils.consoleFont("(x,b)")+": "+language.math.getString("fx.logb_desc")+"<br>"; //Returns the base-b logarithm of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_gt0")+"<br>"; //Real number (or matrix) >0
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("fx.real_num")+" "+MathUtils.consoleFont(">0")+"<br>"; //Real number >0
			des+="</html>";
			return(des);
		case "logGamma": 
			des="<html><b>"+language.math.getString("fx.logGamma_name")+"</b><br>"; //Log-Gamma Function
			des+=MathUtils.consoleFont("<b>logGamma</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.logGamma_desc")+"<br>"; //Returns the log-Gamma function evaluated at x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_gt0")+"<br>"; //Real number (or matrix) >0
			des+="</html>";
			return(des);
		case "log10": 
			des="<html><b>"+language.math.getString("fx.log10_name")+"</b><br>"; //Base-10 Log
			des+=MathUtils.consoleFont("<b>log10</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.log10_desc")+"<br>"; //Returns the base-10 logarithm of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_gt0")+"<br>"; //Real number (or matrix) >0
			des+="</html>";
			return(des);
		case "logit": 
			des="<html><b>"+language.math.getString("fx.logit_name")+"</b><br>"; //Logit Function
			des+=MathUtils.consoleFont("<b>logit</b>","#800000")+MathUtils.consoleFont("(p)")+": "+language.math.getString("fx.logit_desc")+"<br>"; //Returns the log odds of probability p: i.e. log(p/(1-p))
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("p")+": "+language.math.getString("fx.prob_mat_01")+"<br>"; //Probability (or matrix) in [0,1]
			des+="</html>";
			return(des);
		case "logistic": 
			des="<html><b>"+language.math.getString("fx.logistic_name")+"</b><br>"; //Logistic Function
			des+=MathUtils.consoleFont("<b>logistic</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.logistic_desc")+"<br>"; //Returns the standard logistic transformation: 1/(1+exp(-x))
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "max": 
			des="<html><b>"+language.math.getString("sum.maximum")+"</b><br>"; //Maximum
			des+=MathUtils.consoleFont("<b>max</b>","#800000")+MathUtils.consoleFont("(a,b)")+": "+language.math.getString("fx.max_desc")+"<br>"; //Returns the maximum of a and b
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("a")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+="</html>";
			return(des);
		case "min": 
			des="<html><b>"+language.math.getString("sum.minimum")+"</b><br>"; //Minimum
			des+=MathUtils.consoleFont("<b>min</b>","#800000")+MathUtils.consoleFont("(a,b)")+": "+language.math.getString("fx.min_desc")+"<br>"; //Returns the minimum of a and b
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("a")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+=MathUtils.consoleFont("b")+": "+language.math.getString("fx.real_num")+"<br>"; //Real number
			des+="</html>";
			return(des);
		case "probRescale": 
			des="<html><b>"+language.math.getString("fx.probRescale_name")+"</b><br>"; //Probability Rescale
			des+=MathUtils.consoleFont("<b>probRescale</b>","#800000")+MathUtils.consoleFont("(p,t1,t2)")+": "+language.math.getString("fx.probRescale_desc")+"<br>"; //Rescales a probability p from time interval t1 to a new time interval t2
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("p")+": "+language.math.getString("fx.prob_mat_01")+"<br>"; //Probability (or matrix) in [0,1]
			des+=MathUtils.consoleFont("t1")+": "+language.math.getString("fx.time_orig")+"<br>"; //Original time interval >0
			des+=MathUtils.consoleFont("t2")+": "+language.math.getString("fx.time_new")+"<br>"; //New time interval >0
			des+="</html>";
			return(des);
		case "probToRate": 
			des="<html><b>"+language.math.getString("fx.probToRate_name")+"</b><br>"; //Probability to Rate
			des+="<br><i>"+language.math.getString("fx.functions")+"</i><br>"; //Functions
			des+=MathUtils.consoleFont("<b>probToRate</b>","#800000")+MathUtils.consoleFont("(p)")+": "+language.math.getString("fx.probToRate_desc")+"<br>"; //Converts a probability p to a rate for a fixed time interval
			des+=MathUtils.consoleFont("<b>probToRate</b>","#800000")+MathUtils.consoleFont("(p,t1,t2)")+": "+language.math.getString("fx.probToRate_desc2")+"<br>"; //Converts a probability p per time interval t1 to a rate per time interval t2
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("p")+": "+language.math.getString("fx.prob_mat_01")+"<br>"; //Probability (or matrix) in [0,1]
			des+=MathUtils.consoleFont("t1")+": "+language.math.getString("fx.prob_time_opt")+"<br>"; //Probability time interval >0 (optional)
			des+=MathUtils.consoleFont("t2")+": "+language.math.getString("fx.rate_time_opt")+"<br>"; //Rate time interval >0 (optional)
			des+="</html>";
			return(des);
		case "probit": 
			des="<html><b>"+language.math.getString("fx.probit_name")+"</b><br>"; //Probit Function
			des+=MathUtils.consoleFont("<b>probit</b>","#800000")+MathUtils.consoleFont("(p)")+": "+language.math.getString("fx.probit_desc")+"<br>"; //Returns the inverse of the standard normal CDF for probability p
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("p")+": "+language.math.getString("fx.prob_mat_01")+"<br>"; //Probability (or matrix) in [0,1]
			des+="</html>";
			return(des);
		case "rateToProb": 
			des="<html><b>"+language.math.getString("fx.rateToProb_name")+"</b><br>"; //Rate to Probability
			des+="<br><i>"+language.math.getString("fx.functions")+"</i><br>"; //Functions
			des+=MathUtils.consoleFont("<b>rateToProb</b>","#800000")+MathUtils.consoleFont("(r)")+": "+language.math.getString("fx.rateToProb_desc")+"<br>"; //Converts a rate r to a probability for a fixed time interval
			des+=MathUtils.consoleFont("<b>rateToProb</b>","#800000")+MathUtils.consoleFont("(r,t1,t2)")+": "+language.math.getString("fx.rateToProb_desc2")+"<br>"; //Converts a rate r per time interval t1 to a probability per time interval t2
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("r")+": "+language.math.getString("fx.rate_mat_gte0")+"<br>"; //Rate (or matrix) ≥0
			des+=MathUtils.consoleFont("t1")+": "+language.math.getString("fx.rate_time_opt")+"<br>"; //Rate time interval >0 (optional)
			des+=MathUtils.consoleFont("t2")+": "+language.math.getString("fx.prob_time_opt")+"<br>"; //Probability time interval >0 (optional)
			des+="</html>";
			return(des);
		case "round": 
			des="<html><b>"+language.math.getString("fx.round_name")+"</b><br>"; //Round
			des+="<br><i>"+language.math.getString("fx.functions")+"</i><br>"; //Functions
			des+=MathUtils.consoleFont("<b>round</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.round_desc")+"<br>"; //Rounds x to the nearest integer
			des+=MathUtils.consoleFont("<b>round</b>","#800000")+MathUtils.consoleFont("(x,n)")+": "+language.math.getString("fx.round_desc2")+"<br>"; //Rounds x to n decimal places
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+=MathUtils.consoleFont("n")+": "+language.math.getString("fx.num_digits_opt")+"<br>"; //Number of digits (Integer ≥0) (optional)
			des+="</html>";
			return(des);
		case "sin": 
			des="<html><b>"+language.math.getString("fx.sin_name")+"</b><br>"; //Sine
			des+=MathUtils.consoleFont("<b>sin</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.sin_desc")+"<br>"; //Returns the sine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "sinh": 
			des="<html><b>"+language.math.getString("fx.sinh_name")+"</b><br>"; //Hyperbolic Sine
			des+=MathUtils.consoleFont("<b>sinh</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.sinh_desc")+"<br>"; //Returns the hyperbolic sine of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "sqrt": 
			des="<html><b>"+language.math.getString("fx.sqrt_name")+"</b><br>"; //Square Root
			des+=MathUtils.consoleFont("<b>sqrt</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.sqrt_desc")+"<br>"; //Returns the square root of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat_gte0")+"<br>"; //Real number (or matrix) ≥0
			des+="</html>";
			return(des);			
		case "tan": 
			des="<html><b>"+language.math.getString("fx.tan_name")+"</b><br>"; //Tangent
			des+=MathUtils.consoleFont("<b>tan</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.tan_desc")+"<br>"; //Returns the tangent of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "tanh": 
			des="<html><b>"+language.math.getString("fx.tanh_name")+"</b><br>"; //Hyperbolic Tangent
			des+=MathUtils.consoleFont("<b>tanh</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.tanh_desc")+"<br>"; //Returns the hyperbolic tangent of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		case "signum": 
			des="<html><b>"+language.math.getString("fx.signum_name")+"</b><br>"; //Signum Function
			des+=MathUtils.consoleFont("<b>signum</b>","#800000")+MathUtils.consoleFont("(x)")+": "+language.math.getString("fx.signum_desc")+"<br>"; //Returns the sign of x
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x")+": "+language.math.getString("fx.real_num_mat")+"<br>"; //Real number (or matrix)
			des+="</html>";
			return(des);
		
		//Summary functions
		case "mean":
			des="<html><b>"+language.math.getString("sum.mean")+"</b><br>"; //Mean
			des+=MathUtils.consoleFont("<b>mean</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.mean_desc")+"<br>"; //Returns the mean of x1,x2,...,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		case "product":
			des="<html><b>"+language.math.getString("fx.product_name")+"</b><br>"; //Product
			des+=MathUtils.consoleFont("<b>product</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.product_desc")+"<br>"; //Returns the product of x1,x2,...,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		case "quantile":
			des="<html><b>"+language.base.getString("plot.quantile")+"</b><br>"; //Quantile
			des+=MathUtils.consoleFont("<b>quantile</b>","#800000")+MathUtils.consoleFont("(q,x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.quantile_desc")+"<br>"; //Returns the quantile(s) q of x1,x2,...,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("q")+": "+language.math.getString("fx.quant_eval")+"<br>"; //Quantile(s) to evaluate. Real number (or row vector of real numbers) in [0,1]
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		case "sd":
			des="<html><b>"+language.math.getString("fx.sd_name")+"</b><br>"; //Standard Deviation
			des+=MathUtils.consoleFont("<b>sd</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.sd_desc")+"<br>"; //Returns the standard deviation of x1,x2,...,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		case "sum":
			des="<html><b>"+language.math.getString("fx.sum_name")+"</b><br>"; //Sum
			des+=MathUtils.consoleFont("<b>sum</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.sum_desc")+"<br>"; //Returns the sum of x1,x2,...,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		case "var":
			des="<html><b>"+language.math.getString("fx.var_name")+"</b><br>"; //Variance
			des+=MathUtils.consoleFont("<b>var</b>","#800000")+MathUtils.consoleFont("(x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>)")+": "+language.math.getString("fx.var_desc")+"<br>"; //Returns the variance of x1,x2,…,xn
			des+="<br><i>"+language.math.getString("fx.arguments")+"</i><br>"; //Arguments
			des+=MathUtils.consoleFont("x<sub>1</sub>,x<sub>2</sub>,...,x<sub>n</sub>")+": "+language.math.getString("fx.real_nums_mats")+"<br>"; //Real numbers (or matrices)
			des+="</html>";
			return(des);
		
		//end switch
		}

		return(des); //fell through
	}

	public static String getDefaultArgs(String word){
		switch(word){
		case "abs": return("abs(x)");
		case "acos": return("acos(x)");
		case "asin": return("asin(x)");
		case "atan": return("atan(x)");
		case "bound": return("bound(x,a,b)");
		case "cbrt": return("cbrt(x)");
		case "ceil": return("ceil(x)");
		case "choose": return("choose(n,k)");
		case "cos": return("cos(x)");
		case "cosh": return("cosh(x)");
		case "erf": return("erf(x)");
		case "exp": return("exp(x)");
		case "fact": return("fact(n)");
		case "floor": return("floor(x)");
		case "gamma": return("gamma(x)");
		case "hypot": return("hypot(x,y)");
		case "if": return("if(expr,a,b)");
		case "invErf": return("invErf(x)");
		case "log": return("log(x)");
		case "logb": return("log(x,b)");
		case "logGamma": return("logGamma(x)");
		case "log10": return("log10(x)");
		case "logit": return("logit(p)");
		case "logistic": return("logistic(x)");
		case "max": return("max(a,b)");
		case "min": return("min(a,b)");
		case "probRescale": return("probRescale(p,t1,t2)");
		case "probToRate": return("probToRate(p)");
		case "probit": return("probit(p)");
		case "rateToProb": return("rateToProb(r)");
		case "round": return("round(x)");
		case "sin": return("sin(x)");
		case "sinh": return("sinh(x)");
		case "sqrt": return("sqrt(x)");
		case "tan": return("tan(x)");
		case "tanh": return("tanh(x)");
		case "signum": return("signum(x)");
		//Summary functions
		case "mean": return("mean(x1,x2,...,xn)");
		case "product": return("product(x1,x2,...,xn)");
		case "quantile": return("quantile(q,x1,x2,...,xn)");
		case "sd": return("sd(x1,x2,...,xn)");
		case "sum": return("sum(x1,x2,...,xn)");
		case "var": return("var(x1,x2,...,xn)");
		
		}
		return(""); //fell through
	}

}