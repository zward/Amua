package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Exponential{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), lambda=params[1].getDouble();
		if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
		return(new Numeric(lambda*Math.exp(-lambda*x)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		double x=params[0].getDouble(), lambda=params[1].getDouble();
		if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
		return(new Numeric(1.0-Math.exp(-lambda*x)));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), lambda=params[1].getDouble();
		if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
		return(new Numeric(-Math.log(1.0-x)/lambda));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double lambda=params[0].getDouble();
		if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
		return(new Numeric(1.0/lambda));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double lambda=params[0].getDouble();
		if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
		return(new Numeric(1.0/(lambda*lambda)));
	}

	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==1){
			double lambda=params[0].getDouble();
			if(lambda<=0){throw new NumericException("λ should be >0","Expo");}
			return(new Numeric(-Math.log(1-rand)/lambda));
		}
		else{throw new NumericException("Incorrect number of parameters","Expo");}
	}
	
	public static String description(){
		String des="<html><b>Exponential Distribution</b><br>";
		des+="Used to model the time between events with a constant average rate<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("λ")+": Average rate of events ("+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>~</b></i>)")+": Returns a random variable (mean in base case) from the Exponential distribution. Real number "+MathUtils.consoleFont(">0")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>f</i></b>)")+": Returns the value of the Exponential PDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>F</i></b>)")+": Returns the value of the Exponential CDF at "+MathUtils.consoleFont("x")+"<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Exponential distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>E</i></b>)")+": Returns the mean of the Exponential distribution<br>";
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>V</i></b>)")+": Returns the variance of the Exponential distribution<br>";
		des+="</html>";
		return(des);
	}
}