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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

public final class HalfNormal{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), sigma=params[1].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
		if(x<0){return(new Numeric(0));}
		else{
			double pre=Math.sqrt(2)/(sigma*Math.sqrt(Math.PI));
			double inner=-(x*x)/(2*sigma*sigma);
			double density=pre*Math.exp(inner);
			return(new Numeric(density));
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), sigma=params[1].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
		if(x<=0){return(new Numeric(0));}
		else{
			double inner=x/(sigma*Math.sqrt(2));
			return(new Numeric(Erf.erf(inner)));
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), sigma=params[1].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
		double q=sigma*Math.sqrt(2)*Erf.erfInv(x);
		return(new Numeric(q));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double sigma=params[0].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
		double mean=sigma*Math.sqrt(2.0/Math.PI);
		return(new Numeric(mean));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double sigma=params[0].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
		double var=(sigma*sigma)*(1-2/Math.PI);
		return(new Numeric(var));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==1){
			double sigma=params[0].getDouble(), mu=0;
			if(sigma<=0){throw new NumericException("σ should be >0","HalfNorm");}
			NormalDistribution norm=new NormalDistribution(null,mu,sigma);
			return(new Numeric(Math.abs(norm.inverseCumulativeProbability(rand))));
		}
		else{throw new NumericException("Incorrect number of parameters","HalfNorm");}
	}
	
	public static String description(){
		String des="<html><b>Half-Normal Distribution</b><br>";
		des+="Positive Half-Normal<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("σ")+": Standard deviation ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Half-Normal distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>f</i></b>)")+": Returns the value of the Half-Normal PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>F</i></b>)")+": Returns the value of the Half-Normal CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Half-Normal distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>E</i></b>)")+": Returns the mean of the Half-Normal distribution<br>";
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>V</i></b>)")+": Returns the variance of the Half-Normal distribution<br>";
		des+="</html>";
		return(des);
	}
}