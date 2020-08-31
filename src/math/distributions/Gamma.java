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
import org.apache.commons.math3.distribution.GammaDistribution;

import main.MersenneTwisterFast;

public final class Gamma{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), k=params[1].getDouble(), theta=params[2].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and k should be the same size","Gamma");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), k=params[1].getDouble(), theta=params[2].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and k should be the same size","Gamma");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(), k=params[1].getDouble(), theta=params[2].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and k should be the same size","Gamma");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j), k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(), theta=params[1].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			return(new Numeric(k*theta));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					vals.matrix[i][j]=k*theta;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(), theta=params[1].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			return(new Numeric(k*theta*theta));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					vals.matrix[i][j]=k*theta*theta;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","Gamma");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(), theta=params[1].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			double rand=generator.nextDouble();
			return(new Numeric(gamma.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and θ should be the same size","Gamma");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException("k should be >0","Gamma");}
					if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=gamma.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Gamma Distribution</b><br>";
		des+="A continuous distribution that yields positive real numbers<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("k")+": Shape ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("θ")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Gamma distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>f</i></b>)")+": Returns the value of the Gamma PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>F</i></b>)")+": Returns the value of the Gamma CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Gamma distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>E</i></b>)")+": Returns the mean of the Gamma distribution<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>V</i></b>)")+": Returns the variance of the Gamma distribution<br>";
		des+="</html>";
		return(des);
	}
}