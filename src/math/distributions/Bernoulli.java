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

public final class Bernoulli{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double p=params[1].getProb();
			if(k==0){return(new Numeric(1-p));}
			else if(k==1){return(new Numeric(p));}
			else{return(new Numeric(0));} //outside support
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and p should be the same size","Bern");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					if(k==0) {vals.matrix[i][j]=1-p;}
					else if(k==1) {vals.matrix[i][j]=p;}
					else {vals.matrix[i][j]=0;} //outside support
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double p=params[1].getProb();
			if(k<0){return(new Numeric(0));}
			else if(k==0){return(new Numeric(1-p));}
			else{return(new Numeric(1.0));}  //k>=1
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and p should be the same size","Bern");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					if(k<0) {vals.matrix[i][j]=0;}
					else if(k==0) {vals.matrix[i][j]=1-p;}
					else {vals.matrix[i][j]=1.0;} //k>=1
				}
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb();
			double p=params[1].getProb();
			if(x<=(1-p)){return(new Numeric(0));}
			else{return(new Numeric(1));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and p should be the same size","Bern");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double p=params[1].getMatrixProb(i, j);
					if(x<=(1-p)) {vals.matrix[i][j]=0;}
					else {vals.matrix[i][j]=1;}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb();
			return(new Numeric(p));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					vals.matrix[i][j]=p;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb();
			return(new Numeric(p*(1-p)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					vals.matrix[i][j]=p*(1-p);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){throw new NumericException("Incorrect number of parameters","Bern");}
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb();
			double rand=generator.nextDouble();
			if(rand<p){return(new Numeric(1));}
			else{return(new Numeric(0));}
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					double rand=generator.nextDouble();
					if(rand<p) {vals.matrix[i][j]=1.0;}
					else {vals.matrix[i][j]=0;}
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Bernoulli Distribution</b><br>";
		des+="The probability distribution of a single Boolean-valued outcome<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<i><br>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Bernoulli distribution. Integer in "+MathUtils.consoleFont("{0,1}")+"<br>";
		des+="<i><br>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>f</i></b>)")+": Returns the value of the Bernoulli PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>F</i></b>)")+": Returns the value of the Bernoulli CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(x,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Bernoulli distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>E</i></b>)")+": Returns the mean of the Bernoulli distribution<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>V</i></b>)")+": Returns the variance of the Bernoulli distribution<br>";
		des+="</html>";
		return(des);
	}
}