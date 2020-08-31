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

public final class NegativeBinomial{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt();
			int r=params[1].getInt();
			double p=params[2].getProb();
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			return(new Numeric(MathUtils.choose(r+k-1,r-1)*Math.pow(p, r)*Math.pow(1-p, k)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and r should be the same size","NBin");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int r=(int)params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					vals.matrix[i][j]=MathUtils.choose(r+k-1,r-1)*Math.pow(p, r)*Math.pow(1-p, k);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt();
			int r=params[1].getInt();
			double p=params[2].getProb();
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			double val=0;
			for(int n=0; n<=k; n++){
				val+=(MathUtils.choose(r+n-1,r-1)*Math.pow(p, r)*Math.pow(1-p, n));
			}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and r should be the same size","NBin");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int r=(int)params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					double val=0;
					for(int n=0; n<=k; n++){
						val+=(MathUtils.choose(r+n-1,r-1)*Math.pow(p, r)*Math.pow(1-p, n));
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb();
			int r=params[1].getInt();
			double p=params[2].getProb();
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
			int k=0;
			double CDF=0;
			while(x>CDF){
				CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and r should be the same size","NBin");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					int r=(int)params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					if(x==1) {vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else {
						int k=0;
						double CDF=0;
						while(x>CDF){
							CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
							k++;
						}
						vals.matrix[i][j]=k;
					}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int r=params[0].getInt();
			double p=params[1].getProb();
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			return(new Numeric((r*(1-p))/p));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int r=(int)params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					vals.matrix[i][j]=(r*(1-p))/p;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int r=params[0].getInt();
			double p=params[1].getProb();
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			return(new Numeric((r*(1-p))/(p*p)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int r=(int)params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					vals.matrix[i][j]=(r*(1-p))/(p*p);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number parameters","NBin");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int r=params[0].getInt(), k=0;
			double p=params[1].getProb(), CDF=0;
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			double rand=generator.nextDouble();
			while(rand>CDF){
				CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("r and p should be the same size","NBin");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int r=(int)params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					if(r<0){throw new NumericException("r should be ≥0","NBin");}
					int k=0;
					double CDF=0;
					double rand=generator.nextDouble();
					while(rand>CDF){
						CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Negative Binomial Distribution</b><br>";
		des+="Used to model the number of successes that occur among repeated trials before a specified number of failures happen<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("r")+": Number of failures until the trials are stopped (Integer "+MathUtils.consoleFont("≥0")+")<br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Negative Binomial distribution. Integer in "+MathUtils.consoleFont("{0,1,...}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(k,r,p,<b><i>f</i></b>)")+": Returns the value of the Negative Binomial PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(k,r,p,<b><i>F</i></b>)")+": Returns the value of the Negative Binomial CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(x,r,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Negative Binomial distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>E</i></b>)")+": Returns the mean of the Negative Binomial distribution<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>V</i></b>)")+": Returns the variance of the Negative Binomial distribution<br>";
		des+="</html>";
		return(des);
	}
}