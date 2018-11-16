package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.UniformRealDistribution;

public final class Uniform{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=a){throw new NumericException("b should be >a","Unif");}
		UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
		return(new Numeric(uni.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=a){throw new NumericException("b should be >a","Unif");}
		UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
		return(new Numeric(uni.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), a=params[1].getDouble(), b=params[2].getDouble();
		if(b<=a){throw new NumericException("b should be >a","Unif");}
		UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
		return(new Numeric(uni.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(b<=a){throw new NumericException("b should be >a","Unif");}
		return(new Numeric((a+b)/2.0));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double a=params[0].getDouble(), b=params[1].getDouble();
		if(b<=a){throw new NumericException("b should be >a","Unif");}
		double sq=(b-a)*(b-a);
		return(new Numeric(sq/12.0));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double a=params[0].getDouble(), b=params[1].getDouble();
			if(b<=a){throw new NumericException("b should be >a","Unif");}
			return(new Numeric(a+rand*(b-a)));
		}
		else{throw new NumericException("Incorrect number of parameters","Unif");}
	}
	
	public static String description(){
		String des="<html><b>Uniform Distribution</b><br>";
		des+="Used to model a continuous distribution where all values are equally likely<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("a")+": Minimum value, inclusive<br>";
		des+=MathUtils.consoleFont("b")+": Maximum value, exclusive<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Uniform distribution. Real number in "+MathUtils.consoleFont("[a,b)")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>f</i></b>)")+": Returns the value of the Uniform PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>F</i></b>)")+": Returns the value of the Uniform CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Uniform distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": Returns the mean of the Uniform distribution<br>";
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": Returns the variance of the Uniform distribution<br>";
		des+="</html>";
		return(des);
	}
}