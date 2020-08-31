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

import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Exponential{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(lambda*Math.exp(-lambda*x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and λ should be the same size","Expo");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					vals.matrix[i][j]=lambda*Math.exp(-lambda*x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(), lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(1.0-Math.exp(-lambda*x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and λ should be the same size","Expo");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					vals.matrix[i][j]=1.0-Math.exp(-lambda*x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(), lambda=params[1].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(-Math.log(1.0-x)/lambda));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and λ should be the same size","Expo");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j), lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					vals.matrix[i][j]=-Math.log(1.0-x)/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(1.0/lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					vals.matrix[i][j]=1.0/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(1.0/(lambda*lambda)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					vals.matrix[i][j]=1.0/(lambda*lambda);
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){
			throw new NumericException("Incorrect number of parameters","Expo");
		}
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			double rand=generator.nextDouble();
			return(new Numeric(-Math.log(1-rand)/lambda));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
					double rand=generator.nextDouble();
					vals.matrix[i][j]=-Math.log(1-rand)/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Exponential Distribution</b><br>";
		des+="Used to model the time between events with a constant average rate<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("λ")+": Average rate of events ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>~</b></i>)")+": Returns a random variable (mean in base case) from the Exponential distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>f</i></b>)")+": Returns the value of the Exponential PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>F</i></b>)")+": Returns the value of the Exponential CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Exponential distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>E</i></b>)")+": Returns the mean of the Exponential distribution<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>V</i></b>)")+": Returns the variance of the Exponential distribution<br>";
		des+="</html>";
		return(des);
	}
}