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

import org.apache.commons.math3.distribution.TDistribution;

import main.MersenneTwisterFast;

public final class StudentT{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), nu=params[1].getDouble();
			if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and ν should be the same size","StudentT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), nu=params[1].getDouble();
			if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and ν should be the same size","StudentT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(), nu=params[1].getDouble();
			if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and ν should be the same size","StudentT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble();
			if(nu>1){return(new Numeric(0));}
			else{throw new NumericException("Mean is undefined for ν≤1","StudentT");}
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu<=1) {
						throw new NumericException("Mean is undefined for ν≤1","StudentT");
					}
					else {
						vals.matrix[i][j]=0;
					}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble();
			if(nu>2){return(new Numeric(nu/(nu-2)));}
			else if(nu>1){return(new Numeric(Double.POSITIVE_INFINITY));}
			else{throw new NumericException("Variance is undefined for ν≤1","StudentT");}
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu>2){vals.matrix[i][j]=nu/(nu-2);}
					else if(nu>1){vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else{throw new NumericException("Variance is undefined for ν≤1","StudentT");}
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){
			throw new NumericException("Incorrect number of parameters","StudentT");
		}
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble();
			if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
			TDistribution stud=new TDistribution(null,nu);
			double rand=generator.nextDouble();
			return(new Numeric(stud.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu<=0){throw new NumericException("ν should be >0","StudentT");}
					TDistribution stud=new TDistribution(null,nu);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=stud.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Student's t-distribution</b><br>";
		des+="A bell-shaped distribution centered at "+MathUtils.consoleFont("0")+"<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("ν")+": Degrees of freedom ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(ν,<b><i>~</i></b>)")+": Returns a random variable (median in base case) from the Student t-distribution. Real number <br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>f</i></b>)")+": Returns the value of the Student t PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>F</i></b>)")+": Returns the value of the Student t CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Student t-distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(ν,<b><i>E</i></b>)")+": Returns the mean of the Student t-distribution<br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(ν,<b><i>V</i></b>)")+": Returns the variance of the Student t-distribution<br>";
		des+="</html>";
		return(des);
	}
}