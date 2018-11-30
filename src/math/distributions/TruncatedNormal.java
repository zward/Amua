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

public final class TruncatedNormal{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), sigma=params[2].getDouble(), a=params[3].getDouble(), b=params[4].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
		if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
		if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
		if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
		if(x<a || x>b){return(new Numeric(0.0));}
		else{
			double xi=(x-mu)/sigma;
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			double Z=phi(beta)-phi(alpha);
			double pre=1.0/Math.sqrt(2*Math.PI);
			double exp=Math.exp(-0.5*xi*xi);
			double num=pre*exp;
			return(new Numeric(num/(sigma*Z)));
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), sigma=params[2].getDouble(), a=params[3].getDouble(), b=params[4].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
		if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
		if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
		if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
		if(x<a){return(new Numeric(0.0));}
		else if(x>b){return(new Numeric(1.0));}
		else{
			double xi=(x-mu)/sigma;
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			double Z=phi(beta)-phi(alpha);
			return(new Numeric((phi(xi)-phi(alpha))/Z));
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), mu=params[1].getDouble(), sigma=params[2].getDouble(), a=params[3].getDouble(), b=params[4].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
		if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
		if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
		if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
		if(x==0){return(new Numeric(a));}
		else if(x==1){return(new Numeric(b));}
		else{
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			double phiA=phi(alpha);
			double Z=phi(beta)-phiA;
			double phiXi=x*Z+phiA;
			double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
			double q=xi*sigma+mu;
			return(new Numeric(q));
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), sigma=params[1].getDouble(), a=params[2].getDouble(), b=params[3].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
		if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
		if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
		if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
		double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
		NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
		double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
		double phiA=norm.density(alpha), phiB=norm.density(beta);
		double mean=mu+((phiA-phiB)/z)*sigma;
		return(new Numeric(mean));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), sigma=params[1].getDouble(), a=params[2].getDouble(), b=params[3].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
		if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
		if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
		if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
		double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
		NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
		double Z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
		double phiA=norm.density(alpha), phiB=norm.density(beta);
		double one=(alpha*phiA-beta*phiB)/Z;
		double two=(phiA-phiB)/Z;
		double s2=sigma*sigma;
		double var=s2*(1+one-two*two);
		return(new Numeric(var));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==4){
			double mu=params[0].getDouble(), sigma=params[1].getDouble(), a=params[2].getDouble(), b=params[3].getDouble();
			if(sigma<=0){throw new NumericException("σ should be >0","TruncNorm");}
			if(mu<=a){throw new NumericException("μ should be >a","TruncNorm");}
			if(mu>=b){throw new NumericException("μ should be <b","TruncNorm");}
			if(a>=b){throw new NumericException("a should be <b","TruncNorm");}
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			double phiA=phi(alpha);
			double Z=phi(beta)-phiA;
			double phiXi=rand*Z+phiA;
			double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
			double q=xi*sigma+mu;
			return(new Numeric(q));
		}
		else{throw new NumericException("Incorrect number of parameters","TruncNorm");}
	}
	
	private static double phi(double x){
		return(0.5*(1+Erf.erf(x/Math.sqrt(2))));
	}
	
	public static String description(){
		String des="<html><b>Truncated Normal Distribution</b><br>";
		des+="A normal distribution bound by min/max values.<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Mean<br>";
		des+=MathUtils.consoleFont("σ")+": Standard deviation ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("a")+": Minimum value<br>";
		des+=MathUtils.consoleFont("b")+": Maximum value<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Truncated Normal distribution. Real number in "+MathUtils.consoleFont("[a,b]")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>f</i></b>)")+": Returns the value of the Truncated Normal PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>F</i></b>)")+": Returns the value of the Truncated Normal CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Truncated Normal distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>E</i></b>)")+": Returns the mean of the Truncated Normal distribution<br>";
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>V</i></b>)")+": Returns the variance of the Truncated Normal distribution<br>";
		des+="</html>";
		return(des);
	}
}