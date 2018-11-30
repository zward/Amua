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

import org.apache.commons.math3.distribution.TriangularDistribution;

public final class Triangular{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","Tri");}
		if(b<a){throw new NumericException("b should be ≥a","Tri");}
		if(b>c){throw new NumericException("b should be ≤c","Tri");}
		TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
		return(new Numeric(tri.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","Tri");}
		if(b<a){throw new NumericException("b should be ≥a","Tri");}
		if(b>c){throw new NumericException("b should be ≤c","Tri");}
		TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
		return(new Numeric(tri.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","Tri");}
		if(b<a){throw new NumericException("b should be ≥a","Tri");}
		if(b>c){throw new NumericException("b should be ≤c","Tri");}
		TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
		return(new Numeric(tri.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble(); 
		if(c<=a){throw new NumericException("c should be >a","Tri");}
		if(b<a){throw new NumericException("b should be ≥a","Tri");}
		if(b>c){throw new NumericException("b should be ≤c","Tri");}
		return(new Numeric((a+b+c)/3.0));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble(); 
		if(c<=a){throw new NumericException("c should be >a","Tri");}
		if(b<a){throw new NumericException("b should be ≥a","Tri");}
		if(b>c){throw new NumericException("b should be ≤c","Tri");}
		double num=(a*a)+(b*b)+(c*c)-(a*b)-(a*c)-(b*c);
		return(new Numeric(num/18.0));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==3){
			double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
			if(c<=a){throw new NumericException("c should be >a","Tri");}
			if(b<a){throw new NumericException("b should be ≥a","Tri");}
			if(b>c){throw new NumericException("b should be ≤c","Tri");}
			TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
			return(new Numeric(tri.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Tri");}
	}
	
	public static String description(){
		String des="<html><b>Triangular Distribution</b><br>";
		des+="A simple distribution to model the minimum, most likely, and maximum values of a random variable<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Minimum value, inclusive<br>";
		des+=MathUtils.consoleFont("b")+": Mode (most likely value)<br>";
		des+=MathUtils.consoleFont("c")+": Maximum value, exclusive<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Triangular distribution. Real number in "+MathUtils.consoleFont("[a,c)")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>f</i></b>)")+": Returns the value of the Triangular PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>F</i></b>)")+": Returns the value of the Triangular CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Triangular distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>E</i></b>)")+": Returns the mean of the Triangular distribution<br>";
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>V</i></b>)")+": Returns the variance of the Triangular distribution<br>";
		des+="</html>";
		return(des);
	}
}