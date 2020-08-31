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

import org.apache.commons.math3.distribution.LogisticDistribution;

import main.MersenneTwisterFast;

public final class Logistic{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), mu=params[1].getDouble(), s=params[2].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			LogisticDistribution logi=new LogisticDistribution(null,mu,s);
			return(new Numeric(logi.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and μ should be the same size","Logistic");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], s=params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Logistic");}
					LogisticDistribution logi=new LogisticDistribution(null,mu,s);
					vals.matrix[i][j]=logi.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), mu=params[1].getDouble(), s=params[2].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			LogisticDistribution logi=new LogisticDistribution(null,mu,s);
			return(new Numeric(logi.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and μ should be the same size","Logistic");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], s=params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Logistic");}
					LogisticDistribution logi=new LogisticDistribution(null,mu,s);
					vals.matrix[i][j]=logi.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(), mu=params[1].getDouble(), s=params[2].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			LogisticDistribution logi=new LogisticDistribution(null,mu,s);
			return(new Numeric(logi.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and μ should be the same size","Logistic");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j), mu=params[1].matrix[i][j], s=params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Logistic");}
					LogisticDistribution logi=new LogisticDistribution(null,mu,s);
					vals.matrix[i][j]=logi.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(), s=params[1].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			return(new Numeric(mu)); 
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], s=params[1].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Logistic");}
					vals.matrix[i][j]=mu;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[1].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			return(new Numeric((s*s*Math.PI*Math.PI)/3.0)); 
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], s=params[1].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Logistic");}
					vals.matrix[i][j]=(s*s*Math.PI*Math.PI)/3.0;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","Logistic");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(), s=params[1].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			LogisticDistribution logi=new LogisticDistribution(null,mu,s);
			double rand=generator.nextDouble();
			return(new Numeric(logi.inverseCumulativeProbability(rand))); //mean
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("μ and s should be the same size","Logistic");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], s=params[1].matrix[i][j];
					LogisticDistribution logi=new LogisticDistribution(null,mu,s);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=logi.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Logistic Distribution</b><br>";
		des+="A continuous distribution that resembles the Normal distribution in shape but has heavier tails<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Location<br>";
		des+=MathUtils.consoleFont("s")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Logistic distribution. Real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>f</i></b>)")+": Returns the value of the Logistic PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>F</i></b>)")+": Returns the value of the Logistic CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Logistic distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>E</i></b>)")+": Returns the mean of the Logistic distribution<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>V</i></b>)")+": Returns the variance of the Logistic distribution<br>";
		des+="</html>";
		return(des);
	}
}