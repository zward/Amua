package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;
import org.apache.commons.math3.distribution.GammaDistribution;

public final class Gamma{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getDouble(), theta=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Gamma");}
		if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
		GammaDistribution gamma=new GammaDistribution(null,k,theta);
		return(new Numeric(gamma.density(x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), k=params[1].getDouble(), theta=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Gamma");}
		if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
		GammaDistribution gamma=new GammaDistribution(null,k,theta);
		return(new Numeric(gamma.cumulativeProbability(x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), k=params[1].getDouble(), theta=params[2].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Gamma");}
		if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
		GammaDistribution gamma=new GammaDistribution(null,k,theta);
		return(new Numeric(gamma.inverseCumulativeProbability(x)));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double k=params[0].getDouble(), theta=params[1].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Gamma");}
		if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
		return(new Numeric(k*theta));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double k=params[0].getDouble(), theta=params[1].getDouble();
		if(k<=0){throw new NumericException("k should be >0","Gamma");}
		if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
		return(new Numeric(k*theta*theta));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			double k=params[0].getDouble(), theta=params[1].getDouble();
			if(k<=0){throw new NumericException("k should be >0","Gamma");}
			if(theta<=0){throw new NumericException("θ should be >0","Gamma");}
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.inverseCumulativeProbability(rand)));
		}
		else{throw new NumericException("Incorrect number of parameters","Gamma");}
	}
	
	public static String description(){
		String des="<html><b>Gamma Distribution</b><br>";
		des+="A continuous distribution that yields positive real numbers<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("k")+": Shape ("+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("θ")+": Scale ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Gamma distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>f</i></b>)")+": Returns the value of the Gamma PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>F</i></b>)")+": Returns the value of the Gamma CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Gamma distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>E</i></b>)")+": Returns the mean of the Gamma distribution<br>";
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>V</i></b>)")+": Returns the variance of the Gamma distribution<br>";
		des+="</html>";
		return(des);
	}
}