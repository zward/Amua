package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.WeibullDistribution;

public final class Weibull{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Weibull");}
		if(b<=0){throw new NumericException("b should be >0","Weibull");}
		WeibullDistribution weib=new WeibullDistribution(null,a,b);
		return(new Numeric(weib.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Weibull");}
		if(b<=0){throw new NumericException("b should be >0","Weibull");}
		WeibullDistribution weib=new WeibullDistribution(null,a,b);
		return(new Numeric(weib.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Weibull");}
		if(b<=0){throw new NumericException("b should be >0","Weibull");}
		WeibullDistribution weib=new WeibullDistribution(null,a,b);
		return(new Numeric(weib.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Weibull");}
		if(b<=0){throw new NumericException("b should be >0","Weibull");}
		WeibullDistribution weib=new WeibullDistribution(null,a,b);
		return(new Numeric(weib.getNumericalMean()));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(a<=0){throw new NumericException("a should be >0","Weibull");}
		if(b<=0){throw new NumericException("b should be >0","Weibull");}
		WeibullDistribution weib=new WeibullDistribution(null,a,b);
		return(new Numeric(weib.getNumericalVariance()));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(a<=0){throw new NumericException("a should be >0","Weibull");}
			if(b<=0){throw new NumericException("b should be >0","Weibull");}
			WeibullDistribution weib=new WeibullDistribution(null,a,b);
			return(new Numeric(weib.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Weibull");}
	}
	
	public static String description(){
		String des="<html><b>Weibull Distribution</b><br>";
		des+="Often used to model time-to-failure<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Shape parameter ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("b")+": Scale parameter ("+MathUtils.consoleFont(">0")+")<br><br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Weibull distribution. Positive real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>f</i></b>)")+": Returns the value of the Weibull PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>F</i></b>)")+": Returns the value of the Weibull CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Weibull distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": Returns the mean of the Weibull distribution<br>";
		des+=MathUtils.consoleFont("<b>Weibull</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": Returns the variance of the Weibull distribution<br>";
		des+="</html>";
		return(des);
	}
}