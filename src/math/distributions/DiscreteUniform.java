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

public final class DiscreteUniform{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(), a=params[1].getInt(), b=params[2].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			double val=0;
			if(k<a || k>b){val=0;}
			else{val=1.0/((b-a+1)*1.0);}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and a should be the same size","DUnif");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j], a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					double val=0;
					if(k<a || k>b){val=0;}
					else{val=1.0/((b-a+1)*1.0);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(), a=params[1].getInt(), b=params[2].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			double val=0;
			if(k<a){val=0;}
			else if(k>=b){val=1;}
			else{val=(k-a+1)/((b-a+1)*1.0);}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("k and a should be the same size","DUnif");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j], a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					double val=0;
					if(k<a){val=0;}
					else if(k>=b){val=1;}
					else{val=(k-a+1)/((b-a+1)*1.0);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb();
			int a=params[1].getInt(), b=params[2].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			double val=a+x*(b+1-a);
			val=Math.min(b, val);
			return(new Numeric(Math.floor(val)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("x and a should be the same size","DUnif");
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j); 
					int a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					double val=a+x*(b+1-a);
					val=Math.min(b, val);
					vals.matrix[i][j]=Math.floor(val);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getInt(), b=params[1].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			return(new Numeric((a+b)/2.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					vals.matrix[i][j]=(a+b)/2.0;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getInt(), b=params[1].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			double var=((b-a+1)*(b-a+1)-1)/12.0;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					double var=((b-a+1)*(b-a+1)-1)/12.0;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","DUnif");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int a=params[0].getInt(), b=params[1].getInt();
			if(b<=a){throw new NumericException("a should be <b","DUnif");}
			double rand=generator.nextDouble();
			double val=a+rand*(b+1-a);
			return(new Numeric(Math.floor(val)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException("a and b should be the same size","DUnif");
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException("a should be <b","DUnif");}
					double rand=generator.nextDouble();
					double val=a+rand*(b+1-a);
					vals.matrix[i][j]=Math.floor(val);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Discrete Uniform Distribution</b><br>";
		des+="Used to model a discrete distribution where all values are equally likely<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Minimum value, inclusive (integer)<br>";
		des+=MathUtils.consoleFont("b")+": Maximum value, inclusive (integer)<br>";
		des+="<i><br>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Discrete Uniform distribution. Integer in "+MathUtils.consoleFont("{a,a+1,...,b}")+"<br>";
		des+="<i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(k,a,b,<b><i>f</i></b>)")+": Returns the value of the Discrete Uniform PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(k,a,b,<b><i>F</i></b>)")+": Returns the value of the Discrete Uniform CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Discrete Uniform distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": Returns the mean of the Discrete Uniform distribution<br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": Returns the variance of the Discrete Uniform distribution<br>";
		des+="</html>";
		return(des);
	}
}