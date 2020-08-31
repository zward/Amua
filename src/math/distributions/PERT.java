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

import org.apache.commons.math3.distribution.BetaDistribution;

import main.MersenneTwisterFast;

public final class PERT{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			x=(x-a)/(c-a); //Transform x to 0,1
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.density(x)/(c-a))); //rescaled to range
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.density(x)/(c-a); //rescaled to range
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			x=(x-a)/(c-a); //Transform x to 0,1
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.inverseCumulativeProbability(x)*(c-a)+a)); //rescaled to range
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and a should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.inverseCumulativeProbability(x)*(c-a)+a; //rescaled to range
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			return(new Numeric(mu));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					vals.matrix[i][j]=mu;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double var=((mu-a)*(c-mu))/7.0;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double var=((mu-a)*(c-mu))/7.0;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=3){
			throw new NumericException("Incorrect number of parameters","PERT");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2.0*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			double rand=generator.nextDouble();
			double val=beta.inverseCumulativeProbability(rand);
			val=a+(c-a)*val; //Re-scale back to original min/max
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("a and b should be the same size","PERT");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and c should be the same size","PERT");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException("c should be >a","PERT");}
					if(b<a){throw new NumericException("b should be ≥a","PERT");}
					if(b>c){throw new NumericException("b should be ≤c","PERT");}
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2.0*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					double rand=generator.nextDouble();
					double val=beta.inverseCumulativeProbability(rand);
					val=a+(c-a)*val; //Re-scale back to original min/max
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>PERT Distribution (Program Evaluation and Review Technique)</b><br>";
		des+="The PERT method converts a Triangular distribution to a Beta-shaped distribution. It is often used in risk analysis to model subjective estimates<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Minimum value, inclusive<br>";
		des+=MathUtils.consoleFont("b")+": Mode (most likely value)<br>";
		des+=MathUtils.consoleFont("c")+": Maximum value, exclusive<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the PERT distribution. Real number in "+MathUtils.consoleFont("[a,c)")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>f</i></b>)")+": Returns the value of the PERT PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>F</i></b>)")+": Returns the value of the PERT CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the PERT distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>E</i></b>)")+": Returns the mean of the PERT distribution<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>V</i></b>)")+": Returns the variance of the PERT distribution<br>";
		des+="</html>";
		return(des);
	}
}