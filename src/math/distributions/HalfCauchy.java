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

public final class HalfCauchy{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), gamma=params[1].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
		if(x<0){return(new Numeric(0));}
		else{return(new Numeric(2*cauchy.density(x)));}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), gamma=params[1].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		if(x<0){return(new Numeric(0));}
		else{return(new Numeric((2*Math.atan(x/gamma))/Math.PI));}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), gamma=params[1].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		double q=gamma*Math.tan((Math.PI*x)/2.0);
		return(new Numeric(q));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double gamma=params[0].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		throw new NumericException("Mean is undefined","HalfCauchy");
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double gamma=params[0].getDouble();
		if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
		throw new NumericException("Variance is undefined","HalfCauchy");
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==1){
			double gamma=params[0].getDouble();
			if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
			return(new Numeric(Math.abs(cauchy.inverseCumulativeProbability(rand))));
		}
		else{throw new NumericException("Incorrect number of parameters","HalfCauchy");}
	}
	
	public static String description(){
		String des="<html><b>Half-Cauchy Distribution</b><br>";
		des+="Positive Half-Cauchy. <i>Note</i>: Mean and variance are undefined<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("γ")+": Scale parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(γ,<b><i>~</i></b>)")+": Returns a random variable (median in base case) from the Half-Cauchy distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>f</i></b>)")+": Returns the value of the Half-Cauchy PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>F</i></b>)")+": Returns the value of the Half-Cauchy CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Half-Cauchy distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="</html>";
		return(des);
	}
}