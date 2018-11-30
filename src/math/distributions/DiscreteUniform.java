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

public final class DiscreteUniform{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt(), a=params[1].getInt(), b=params[2].getInt();
		if(b<=a){throw new NumericException("a should be <b","DUnif");}
		double val=0;
		if(k<a || k>b){val=0;}
		else{val=1.0/((b-a+1)*1.0);}
		return(new Numeric(val));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt(), a=params[1].getInt(), b=params[2].getInt();
		if(b<=a){throw new NumericException("a should be <b","DUnif");}
		double val=0;
		if(k<a){val=0;}
		else if(k>=b){val=1;}
		else{val=(k-a+1)/((b-a+1)*1.0);}
		return(new Numeric(val));
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb();
		int a=params[1].getInt(), b=params[2].getInt();
		if(b<=a){throw new NumericException("a should be <b","DUnif");}
		double val=a+x*(b+1-a);
		val=Math.min(b, val);
		return(new Numeric(Math.floor(val)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getInt(), b=params[1].getInt();
		if(b<=a){throw new NumericException("a should be <b","DUnif");}
		return(new Numeric((a+b)/2.0));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getInt(), b=params[1].getInt();
		if(b<=a){throw new NumericException("a should be <b","DUnif");}
		double var=((b-a+1)*(b-a+1)-1)/12.0;
		return(new Numeric(var));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			int a=params[0].getInt(), b=params[1].getInt();
			double val=a+rand*(b+1-a);
			return(new Numeric(Math.floor(val)));
		}
		else{throw new NumericException("Incorrect number of parameters","DUnif");}
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