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

import org.apache.commons.math3.special.Gamma;

import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Poisson{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			double val=0;
			val=Math.exp(k*Math.log(lambda)-lambda-Gamma.logGamma(k+1));
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and λ should be the same size","Pois");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					double val=0;
					val=Math.exp(k*Math.log(lambda)-lambda-Gamma.logGamma(k+1));
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			double val=0;
			for(int i=0; i<=k; i++){
				val+=Math.exp(i*Math.log(lambda)-lambda-Gamma.logGamma(i+1));
			}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and λ should be the same size","Pois");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					double val=0;
					for(int z=0; z<=k; z++){
						val+=Math.exp(z*Math.log(lambda)-lambda-Gamma.logGamma(z+1));
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb();
			double lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
			int k=-1;
			double CDF=0;
			while(x>CDF){
				double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
				CDF+=curMass;
				k++;
			}
			k=Math.max(0, k);
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and λ should be the same size","Pois");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					double val=0;
					if(x==1) {val=Double.POSITIVE_INFINITY;}
					else {
						int k=-1;
						double CDF=0;
						while(x>CDF){
							double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
							CDF+=curMass;
							k++;
						}
						k=Math.max(0, k);
						val=k;
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			return(new Numeric(lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					vals.matrix[i][j]=lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			return(new Numeric(lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					vals.matrix[i][j]=lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){
			throw new NumericException("Incorrect number of parameters","Pois");
		}
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
			int k=-1;
			double CDF=0;
			double rand=generator.nextDouble();
			while(rand>CDF){
				double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
				CDF+=curMass;
				k++;
			}
			return(new Numeric(k));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Pois");}
					int k=-1;
					double CDF=0;
					double rand=generator.nextDouble();
					while(rand>CDF){
						double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
						CDF+=curMass;
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Poisson Distribution</b><br>";
		des+="Used to model the number of events that occur in a fixed interval of time/space with a known average rate<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("λ")+": Average number of events in the interval ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Poisson distribution. Integer in "+MathUtils.consoleFont("{0,1,...}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(k,λ,<b><i>f</i></b>)")+": Returns the value of the Poisson PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(k,λ,<b><i>F</i></b>)")+": Returns the value of the Poisson CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Poisson distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>E</i></b>)")+": Returns the mean of the Poisson distribution<br>";
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>V</i></b>)")+": Returns the variance of the Poisson distribution<br>";
		des+="</html>";
		return(des);
	}
}