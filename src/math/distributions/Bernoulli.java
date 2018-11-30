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

public final class Bernoulli{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		double p=params[1].getProb();
		if(k==0){return(new Numeric(1-p));}
		else if(k==1){return(new Numeric(p));}
		else{return(new Numeric(0));} //outside support
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		double p=params[1].getProb();
		if(k<0){return(new Numeric(0));}
		else if(k==0){return(new Numeric(1-p));}
		else{return(new Numeric(1.0));}  //k>=1
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb();
		double p=params[1].getProb();
		if(x<=(1-p)){return(new Numeric(0));}
		else{return(new Numeric(1));}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double p=params[0].getProb();
		return(new Numeric(p));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double p=params[0].getProb();
		return(new Numeric(p*(1-p)));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length!=1){throw new NumericException("Incorrect number of parameters","Bern");}
		double p=params[0].getProb();
		if(rand<p){return(new Numeric(1));}
		else{return(new Numeric(0));}
	}
	
	public static String description(){
		String des="<html><b>Bernoulli Distribution</b><br>";
		des+="The probability distribution of a single Boolean-valued outcome<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<i><br>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Bernoulli distribution. Integer in "+MathUtils.consoleFont("{0,1}")+"<br>";
		des+="<i><br>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>f</i></b>)")+": Returns the value of the Bernoulli PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>F</i></b>)")+": Returns the value of the Bernoulli CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(x,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Bernoulli distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>E</i></b>)")+": Returns the mean of the Bernoulli distribution<br>";
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>V</i></b>)")+": Returns the variance of the Bernoulli distribution<br>";
		des+="</html>";
		return(des);
	}
}