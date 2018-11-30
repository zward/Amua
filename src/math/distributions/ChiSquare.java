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
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public final class ChiSquare{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getInt();
		if(k<1){throw new NumericException("k should be >0","ChiSq");}
		ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
		return(new Numeric(chiSq.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getInt();
		if(k<1){throw new NumericException("k should be >0","ChiSq");}
		ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
		return(new Numeric(chiSq.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), k=params[1].getInt();
		if(k<1){throw new NumericException("k should be >0","ChiSq");}
		ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
		return(new Numeric(chiSq.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double k=params[0].getInt();
		if(k<1){throw new NumericException("k should be >0","ChiSq");}
		return(new Numeric(k));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double k=params[0].getInt();
		if(k<1){throw new NumericException("k should be >0","ChiSq");}
		return(new Numeric(2*k));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==1){
			double k=params[0].getInt();
			if(k<1){throw new NumericException("k should be >0","ChiSq");}
			ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
			return(new Numeric(chiSq.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","ChiSq");}
	}
	
	public static String description(){
		String des="<html><b>Chi-Squared Distribution</b><br>";
		des+="Distribution of the sum of squares of "+MathUtils.consoleFont("k")+" independent standard normal variables<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("k")+": Degrees of freedom (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Chi-Squared distribution. Positive real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>f</i></b>)")+": Returns the value of the Chi-Squared PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>F</i></b>)")+": Returns the value of the Chi-Squared CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Chi-Squared distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>E</i></b>)")+": Returns the mean of the Chi-Squared distribution<br>";
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>V</i></b>)")+": Returns the variance of the Chi-Squared distribution<br>";
		des+="</html>";
		return(des);
	}
}