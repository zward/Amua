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

public final class Cauchy{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
			if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and x_0 should be the same size","Cauchy");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("x_0 and γ should be the same size","Cauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
			if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and x_0 should be the same size","Cauchy");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("x_0 and γ should be the same size","Cauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
			if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and x_0 should be the same size","Cauchy");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("x_0 and γ should be the same size","Cauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j), a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		throw new NumericException("Mean is undefined","Cauchy");
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		throw new NumericException("Variance is undefined","Cauchy");
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","Cauchy");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			double rand=generator.nextDouble();
			return(new Numeric(cauchy.inverseCumulativeProbability(rand)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x_0 and γ should be the same size","Cauchy");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j], b=params[1].matrix[i][j];
					if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=cauchy.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Cauchy Distribution</b><br>";
		des+="A bell-shaped distribution with heavy tails. <i>Note</i>: Mean and variance are undefined<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("x\u2080")+": Location parameter <br>";
		des+=MathUtils.consoleFont("γ")+": Scale parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x\u2080,γ,<b><i>~</i></b>)")+": Returns a random variable (median in base case) from the Cauchy distribution. Real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>f</i></b>)")+": Returns the value of the Cauchy PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>F</i></b>)")+": Returns the value of the Cauchy CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Cauchy distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="</html>";
		return(des);
	}
}