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

package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.WeibullDistribution;

import main.MersenneTwisterFast;

public final class Weibull{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","Weibull");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					vals.matrix[i][j]=weib.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","Weibull");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					vals.matrix[i][j]=weib.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","Weibull");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i,j);
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					vals.matrix[i][j]=weib.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.getNumericalMean()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					vals.matrix[i][j]=weib.getNumericalMean();
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.getNumericalVariance()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					vals.matrix[i][j]=weib.getNumericalVariance();
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","Weibull");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			double rand=generator.nextDouble();
			return(new Numeric(weib.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","Weibull");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(a<=0){throw new NumericException("a should be >0","Weibull");}
					if(b<=0){throw new NumericException("b should be >0","Weibull");}
					WeibullDistribution weib=new WeibullDistribution(null,a,b);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=weib.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Weibull Distribution</b><br>";
		des+="Often used to model time-to-failure<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Shape parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("b")+": Scale parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Weibull distribution. Positive real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>f</i></b>)")+": Returns the value of the Weibull PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>F</i></b>)")+": Returns the value of the Weibull CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Weibull distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": Returns the mean of the Weibull distribution<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": Returns the variance of the Weibull distribution<br>";
		des+="</html>";
		return(des);
	}
}