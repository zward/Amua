package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;
import org.apache.commons.math3.distribution.GumbelDistribution;

public final class Gumbel{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), beta=params[2].getDouble();
		if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
		GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
		return(new Numeric(gumbel.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), beta=params[2].getDouble();
		if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
		GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
		return(new Numeric(gumbel.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), mu=params[1].getDouble(), beta=params[2].getDouble();
		if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
		GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
		return(new Numeric(gumbel.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), beta=params[1].getDouble();
		if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
		GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
		return(new Numeric(gumbel.getNumericalMean()));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), beta=params[1].getDouble();
		if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
		GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
		return(new Numeric(gumbel.getNumericalVariance()));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double mu=params[0].getDouble(), beta=params[1].getDouble();
			if(beta<=0){throw new NumericException("β should be >0","Gumbel");}
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.inverseCumulativeProbability(rand))); //mean
		}
		else{throw new NumericException("Incorrect number of parameters","Gumbel");}
	}
	
	public static String description(){
		String des="<html><b>Gumbel Distribution</b><br>";
		des+="Used to model the distribution of the extrema (max/min) of a number of samples of various distributions<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Location<br>";
		des+=MathUtils.consoleFont("β")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Gumbel distribution. Real number<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>f</i></b>)")+": Returns the value of the Gumbel PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>F</i></b>)")+": Returns the value of the Gumbel CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Gumbel distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>E</i></b>)")+": Returns the mean of the Gumbel distribution<br>";
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>V</i></b>)")+": Returns the variance of the Gumbel distribution<br>";
		des+="</html>";
		return(des);
	}
}