package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.LaplaceDistribution;

public final class Laplace{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("b should be >0","Laplace");}
		LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
		return(new Numeric(lap.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("b should be >0","Laplace");}
		LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
		return(new Numeric(lap.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), mu=params[1].getDouble(), b=params[2].getDouble();
		if(b<=0){throw new NumericException("b should be >0","Laplace");}
		LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
		return(new Numeric(lap.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), b=params[1].getDouble();
		if(b<=0){throw new NumericException("b should be >0","Laplace");}
		return(new Numeric(mu)); 
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double b=params[1].getDouble();
		if(b<=0){throw new NumericException("b should be >0","Laplace");}
		return(new Numeric(2*b*b));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double mu=params[0].getDouble(), b=params[1].getDouble();
			if(b<=0){throw new NumericException("b should be >0","Laplace");}
			LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
			return(new Numeric(lap.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Laplace");}
	}
	
	public static String description(){
		String des="<html><b>Laplace Distribution</b><br>";
		des+="A continuous distribution that can be thought of as two Exponential distributions put back-to-back<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Location<br>";
		des+=MathUtils.consoleFont("b")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Laplace distribution. Real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>f</i></b>)")+": Returns the value of the Laplace PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>F</i></b>)")+": Returns the value of the Laplace CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Laplace distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>E</i></b>)")+": Returns the mean of the Laplace distribution<br>";
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>V</i></b>)")+": Returns the variance of the Laplace distribution<br>";
		des+="</html>";
		return(des);
	}
}