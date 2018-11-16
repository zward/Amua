package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public final class LogNormal{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), sigma=params[2].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
		LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
		return(new Numeric(lnorm.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), mu=params[1].getDouble(), sigma=params[2].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
		LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
		return(new Numeric(lnorm.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), mu=params[1].getDouble(), sigma=params[2].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
		LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
		return(new Numeric(lnorm.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), sigma=params[1].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
		return(new Numeric(Math.exp(mu+(sigma*sigma)/2.0)));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double mu=params[0].getDouble(), sigma=params[1].getDouble();
		if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
		double s2=sigma*sigma;
		double one=Math.exp(s2)-1;
		double two=Math.exp(2*mu+s2);
		double var=one*two;
		return(new Numeric(var));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double mu=params[0].getDouble(), sigma=params[1].getDouble();
			if(sigma<=0){throw new NumericException("σ should be >0","LogNorm");}
			LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
			return(new Numeric(lnorm.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","LogNorm");}
	}
	
	public static String description(){
		String des="<html><b>Log-Normal Distribution</b><br>";
		des+="A continuous distribution of a random variable whose logarithm follows a Normal distribution<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("μ")+": Location<br>";
		des+=MathUtils.consoleFont("σ")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Log-Normal distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>f</i></b>)")+": Returns the value of the Log-Normal PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>F</i></b>)")+": Returns the value of the Log-Normal CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Log-Normal distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>E</i></b>)")+": Returns the mean of the Log-Normal  distribution<br>";
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>V</i></b>)")+": Returns the variance of the Log-Normal  distribution<br>";
		des+="</html>";
		return(des);
	}
}