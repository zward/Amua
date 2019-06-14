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

public final class Cauchy{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
		CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
		return(new Numeric(cauchy.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
		CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
		return(new Numeric(cauchy.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
		CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
		return(new Numeric(cauchy.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double b=params[1].getDouble();
		if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
		throw new NumericException("Mean is undefined","Cauchy");
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double b=params[1].getDouble();
		if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
		throw new NumericException("Variance is undefined","Cauchy");
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Cauchy");}
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