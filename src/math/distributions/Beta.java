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

public final class Beta{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Beta");}
		if(b<=0){throw new NumericException("b should be >0","Beta");}
		BetaDistribution beta=new BetaDistribution(null,a,b);
		return(new Numeric(beta.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Beta");}
		if(b<=0){throw new NumericException("b should be >0","Beta");}
		BetaDistribution beta=new BetaDistribution(null,a,b);
		return(new Numeric(beta.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Beta");}
		if(b<=0){throw new NumericException("b should be >0","Beta");}
		BetaDistribution beta=new BetaDistribution(null,a,b);
		return(new Numeric(beta.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Beta");}
		if(b<=0){throw new NumericException("b should be >0","Beta");}
		return(new Numeric(a/(a+b)));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Beta");}
		if(b<=0){throw new NumericException("b should be >0","Beta");}
		double num=a*b;
		double oneD=(a+b)*(a+b);
		double twoD=(a+b+1);
		double var=num/(oneD*twoD);
		return(new Numeric(var));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Beta");}
			if(b<=0){throw new NumericException("b should be >0","Beta");}
			BetaDistribution beta=new BetaDistribution(null,a,b);
			return(new Numeric(beta.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Beta");}
	}
	
	public static String description(){
		String des="<html><b>Beta Distribution</b><br>";
		des+="A continuous distribution bounded by "+MathUtils.consoleFont("0")+" and "+MathUtils.consoleFont("1")+".  Often used to model probabilities<br>";
		des+="<br><i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Shape parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("b")+": Shape parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Beta distribution. Real number in "+MathUtils.consoleFont("[0,1]")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>f</i></b>)")+": Returns the value of the Beta PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>F</i></b>)")+": Returns the value of the Beta CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Beta distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": Returns the mean of the Beta distribution<br>";
		des+=MathUtils.consoleFont("<b>Beta</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": Returns the variance of the Beta distribution<br>";
		des+="</html>";
		return(des);
	}
}