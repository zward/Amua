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

import org.apache.commons.math3.distribution.CauchyDistribution;
import main.MersenneTwisterFast;

public final class HalfCauchy{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), gamma=params[1].getDouble();
			if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
			if(x<0){return(new Numeric(0));}
			else{return(new Numeric(2*cauchy.density(x)));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and γ should be the same size","HalfCauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
					double val=0;
					if(x<0) {val=0;}
					else {val=2*cauchy.density(x);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), gamma=params[1].getDouble();
			if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
			if(x<0){return(new Numeric(0));}
			else{return(new Numeric((2*Math.atan(x/gamma))/Math.PI));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and γ should be the same size","HalfCauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
					double val=0;
					if(x<0) {val=0;}
					else {val=(2*Math.atan(x/gamma))/Math.PI;}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(), gamma=params[1].getDouble();
			if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
			double q=gamma*Math.tan((Math.PI*x)/2.0);
			return(new Numeric(q));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and γ should be the same size","HalfCauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j), gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
					double q=gamma*Math.tan((Math.PI*x)/2.0);
					vals.matrix[i][j]=q;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double gamma=params[0].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		throw new NumericException("Mean is undefined","HalfCauchy");
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double gamma=params[0].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		throw new NumericException("Variance is undefined","HalfCauchy");
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){
			throw new NumericException("Incorrect number of parameters","HalfCauchy");
		}
		if(params[0].isMatrix()==false) { //real number
			double gamma=params[0].getDouble();
			if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
			double rand=generator.nextDouble();
			return(new Numeric(Math.abs(cauchy.inverseCumulativeProbability(rand))));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double gamma=params[0].matrix[i][j];
					if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=Math.abs(cauchy.inverseCumulativeProbability(rand));
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Half-Cauchy Distribution</b><br>";
		des+="Positive Half-Cauchy. <i>Note</i>: Mean and variance are undefined<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("γ")+": Scale parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(γ,<b><i>~</i></b>)")+": Returns a random variable (median in base case) from the Half-Cauchy distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>f</i></b>)")+": Returns the value of the Half-Cauchy PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>F</i></b>)")+": Returns the value of the Half-Cauchy CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Half-Cauchy distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="</html>";
		return(des);
	}
}