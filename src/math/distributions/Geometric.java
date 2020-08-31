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

public final class Geometric{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double p=params[1].getProb();
			double val=0;
			if(k<0) {val=0;} //outside support
			else{val=Math.pow(1.0-p, k)*p;}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and p should be the same size","Geom");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					double val=0;
					if(k<0) {val=0;} //outside support
					else{val=Math.pow(1.0-p, k)*p;}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double p=params[1].getProb();
			double val=0;
			for(int i=0; i<=k; i++){val+=Math.pow(1.0-p, i)*p;}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and p should be the same size","Geom");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j);
					double val=0;
					for(int z=0; z<=k; z++){val+=Math.pow(1.0-p, z)*p;}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb();
			double p=params[1].getProb();
			if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
			double CDF=0;
			int k=-1;
			while(x>CDF){
				CDF+=Math.pow(1.0-p, k+1)*p;
				k++;
			}
			k=Math.max(0, k);
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and p should be the same size","Geom");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double p=params[1].getMatrixProb(i, j);
					if(x==1) {vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else {
						double CDF=0;
						int k=-1;
						while(x>CDF){
							CDF+=Math.pow(1.0-p, k+1)*p;
							k++;
						}
						k=Math.max(0, k);
						vals.matrix[i][j]=k;
					}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()) { //real number
			double p=params[0].getProb();
			return(new Numeric((1.0-p)/p));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					double val=(1.0-p)/p;
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()) { //real number
			double p=params[0].getProb();
			return(new Numeric((1.0-p)/(p*p)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					double val=(1.0-p)/(p*p);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=1){
			throw new NumericException("Incorrect number of parameters","Geom");
		}
		if(params[0].isMatrix()) { //real number
			double p=params[0].getProb(), CDF=0;
			int k=-1;
			double rand=generator.nextDouble();
			while(rand>CDF){
				CDF+=Math.pow(1.0-p, k+1)*p;
				k++;
			}
			return(new Numeric(k));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j);
					double CDF=0;
					int k=-1;
					double rand=generator.nextDouble();
					while(rand>CDF){
						CDF+=Math.pow(1.0-p, k+1)*p;
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Geometric Distribution</b><br>";
		des+="Used to model the number of successes that occur before the first failure<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Geometric distribution. Integer in "+MathUtils.consoleFont("{0,1,...}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(k,p,<b><i>f</i></b>)")+": Returns the value of the Geometric PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(k,p,<b><i>F</i></b>)")+": Returns the value of the Geometric CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(x,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Geometric distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>E</i></b>)")+": Returns the mean of the Geometric distribution<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>V</i></b>)")+": Returns the variance of the Geometric distribution<br>";
		des+="</html>";
		return(des);
	}
}