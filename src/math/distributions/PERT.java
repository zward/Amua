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

public final class PERT{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","PERT");}
		if(b<a){throw new NumericException("b should be ≥a","PERT");}
		if(b>c){throw new NumericException("b should be ≤c","PERT");}
		double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
		double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
		double a2=(a1*(c-u))/(u-a);
		if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
		x=(x-a)/(c-a); //Transform x to 0,1
		BetaDistribution beta=new BetaDistribution(null,a1,a2);
		return(new Numeric(beta.density(x)/(c-a))); //rescaled to range
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","PERT");}
		if(b<a){throw new NumericException("b should be ≥a","PERT");}
		if(b>c){throw new NumericException("b should be ≤c","PERT");}
		double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
		double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
		double a2=(a1*(c-u))/(u-a);
		if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
		x=(x-a)/(c-a); //Transform x to 0,1
		BetaDistribution beta=new BetaDistribution(null,a1,a2);
		return(new Numeric(beta.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble(), c=params[3].getDouble();
		if(c<=a){throw new NumericException("c should be >a","PERT");}
		if(b<a){throw new NumericException("b should be ≥a","PERT");}
		if(b>c){throw new NumericException("b should be ≤c","PERT");}
		double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
		double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
		double a2=(a1*(c-u))/(u-a);
		if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
		BetaDistribution beta=new BetaDistribution(null,a1,a2);
		return(new Numeric(beta.inverseCumulativeProbability(x)*(c-a)+a)); //rescaled to range
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
		if(c<=a){throw new NumericException("c should be >a","PERT");}
		if(b<a){throw new NumericException("b should be ≥a","PERT");}
		if(b>c){throw new NumericException("b should be ≤c","PERT");}
		double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
		return(new Numeric(mu));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
		if(c<=a){throw new NumericException("c should be >a","PERT");}
		if(b<a){throw new NumericException("b should be ≥a","PERT");}
		if(b>c){throw new NumericException("b should be ≤c","PERT");}
		double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
		double var=((mu-a)*(c-mu))/7.0;
		return(new Numeric(var));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==3){
			double a=params[0].getDouble(), b=params[1].getDouble(), c=params[2].getDouble();
			if(c<=a){throw new NumericException("c should be >a","PERT");}
			if(b<a){throw new NumericException("b should be ≥a","PERT");}
			if(b>c){throw new NumericException("b should be ≤c","PERT");}
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2.0*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			double val=beta.inverseCumulativeProbability(rand);
			val=a+(c-a)*val; //Re-scale back to original min/max
			return(new Numeric(val));
		}
		else{throw new NumericException("Incorrect number of parameters","PERT");}
	}
	
	public static String description(){
		String des="<html><b>PERT Distribution (Program Evaluation and Review Technique)</b><br>";
		des+="The PERT method converts a Triangular distribution to a Beta-shaped distribution. It is often used in risk analysis to model subjective estimates<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Minimum value, inclusive<br>";
		des+=MathUtils.consoleFont("b")+": Mode (most likely value)<br>";
		des+=MathUtils.consoleFont("c")+": Maximum value, exclusive<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the PERT distribution. Real number in "+MathUtils.consoleFont("[a,c)")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>f</i></b>)")+": Returns the value of the PERT PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>F</i></b>)")+": Returns the value of the PERT CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the PERT distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>E</i></b>)")+": Returns the mean of the PERT distribution<br>";
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>V</i></b>)")+": Returns the variance of the PERT distribution<br>";
		des+="</html>";
		return(des);
	}
}