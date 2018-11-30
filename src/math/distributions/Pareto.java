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

import org.apache.commons.math3.distribution.ParetoDistribution;

public final class Pareto{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getDouble(), alpha=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Pareto");}
		if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
		ParetoDistribution par=new ParetoDistribution(null,k,alpha);
		return(new Numeric(par.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getDouble(), alpha=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Pareto");}
		if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
		ParetoDistribution par=new ParetoDistribution(null,k,alpha);
		return(new Numeric(par.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), k=params[1].getDouble(), alpha=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Pareto");}
		if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
		ParetoDistribution par=new ParetoDistribution(null,k,alpha);
		return(new Numeric(par.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double k=params[0].getDouble(), alpha=params[1].getDouble(); 
		if(k<=0){throw new NumericException("k should be >0","Pareto");}
		if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
		if(alpha<=1){return(new Numeric(Double.POSITIVE_INFINITY));}
		else{return(new Numeric((alpha*k)/(alpha-1)));}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double k=params[0].getDouble(), alpha=params[1].getDouble(); 
		if(k<=0){throw new NumericException("k should be >0","Pareto");}
		if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
		if(alpha<=2){return(new Numeric(Double.POSITIVE_INFINITY));}
		else{
			double num=k*k*alpha;
			double denom=(alpha-1)*(alpha-1)*(alpha-2);
			return(new Numeric(num/denom));
		}
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double k=params[0].getDouble(), alpha=params[1].getDouble(); 
			if(k<=0){throw new NumericException("k should be >0","Pareto");}
			if(alpha<=0){throw new NumericException("α should be >0","Pareto");}
			ParetoDistribution par=new ParetoDistribution(null,k,alpha);
			return(new Numeric(par.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Pareto");}
	}
	
	public static String description(){
		String des="<html><b>Pareto Distribution</b><br>";
		des+="A power law probability distribution<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("k")+": Scale ("+MathUtils.consoleFont(">0")+") minimum possible value of "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("α")+": Shape ("+MathUtils.consoleFont(">0")+") Pareto index<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Pareto distribution. Positive real number <br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>f</i></b>)")+": Returns the value of the Pareto PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>F</i></b>)")+": Returns the value of the Pareto CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Pareto distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>E</i></b>)")+": Returns the mean of the Pareto distribution<br>";
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>V</i></b>)")+": Returns the variance of the Pareto distribution<br>";
		des+="</html>";
		return(des);
	}
}