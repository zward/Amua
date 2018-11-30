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

import org.apache.commons.math3.distribution.LogisticDistribution;

public final class Logistic{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), s=params[2].getDouble();
		if(s<=0){throw new NumericException("s should be >0","Logistic");}
		LogisticDistribution logi=new LogisticDistribution(null,mu,s);
		return(new Numeric(logi.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), s=params[2].getDouble();
		if(s<=0){throw new NumericException("s should be >0","Logistic");}
		LogisticDistribution logi=new LogisticDistribution(null,mu,s);
		return(new Numeric(logi.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), mu=params[1].getDouble(), s=params[2].getDouble();
		if(s<=0){throw new NumericException("s should be >0","Logistic");}
		LogisticDistribution logi=new LogisticDistribution(null,mu,s);
		return(new Numeric(logi.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), s=params[1].getDouble();
		if(s<=0){throw new NumericException("s should be >0","Logistic");}
		return(new Numeric(mu)); 
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double s=params[1].getDouble();
		if(s<=0){throw new NumericException("s should be >0","Logistic");}
		return(new Numeric((s*s*Math.PI*Math.PI)/3.0)); 
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double mu=params[0].getDouble(), s=params[1].getDouble();
			if(s<=0){throw new NumericException("s should be >0","Logistic");}
			LogisticDistribution logi=new LogisticDistribution(null,mu,s);
			return(new Numeric(logi.inverseCumulativeProbability(rand))); //mean
		}
		else{throw new NumericException("Incorrect number of parameters","Logistic");}
	}
	
	public static String description(){
		String des="<html><b>Logistic Distribution</b><br>";
		des+="A continuous distribution that resembles the Normal distribution in shape but has heavier tails<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Location<br>";
		des+=MathUtils.consoleFont("s")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Logistic distribution. Real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>f</i></b>)")+": Returns the value of the Logistic PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>F</i></b>)")+": Returns the value of the Logistic CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(x,μ,s,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Logistic distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>E</i></b>)")+": Returns the mean of the Logistic distribution<br>";
		des+=MathUtils.consoleFont("<b>Logistic</b>","green")+MathUtils.consoleFont("(μ,s,<b><i>V</i></b>)")+": Returns the variance of the Logistic distribution<br>";
		des+="</html>";
		return(des);
	}
}