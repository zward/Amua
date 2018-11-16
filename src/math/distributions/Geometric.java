package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Geometric{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		double p=params[1].getProb();
		double val=0;
		val=Math.pow(1.0-p, k)*p;
		return(new Numeric(val));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		double p=params[1].getProb();
		double val=0;
		for(int i=0; i<=k; i++){val+=Math.pow(1.0-p, i)*p;}
		return(new Numeric(val));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb();
		double p=params[1].getProb();
		if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
		double CDF=0;
		int k=-1;
		while(x>CDF){
			CDF+=Math.pow(1.0-p, k+1)*p;
			k++;
		}
		k=Math.max(0, k);
		return(new Numeric(k));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		double p=params[0].getProb();
		return(new Numeric((1.0-p)/p));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		double p=params[0].getProb();
		return(new Numeric((1.0-p)/(p*p)));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==1){
			double p=params[0].getProb(), CDF=0;
			int k=-1;
			while(rand>CDF){
				CDF+=Math.pow(1.0-p, k+1)*p;
				k++;
			}
			return(new Numeric(k));
		}
		else{throw new NumericException("Incorrect number of parameters","Geom");}
	}
	
	public static String description(){
		String des="<html><b>Geometric Distribution</b><br>";
		des+="Used to model the number of successes that occur before the first failure<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Geometric distribution. Integer in "+MathUtils.consoleFont("{0,1,...}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(k,p,<b><i>f</i></b>)")+": Returns the value of the Geometric PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(k,p,<b><i>F</i></b>)")+": Returns the value of the Geometric CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(x,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Geometric distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>E</i></b>)")+": Returns the mean of the Geometric distribution<br>";
		des+=MathUtils.consoleFont("<b>Geom</b>","green")+MathUtils.consoleFont("(p,<b><i>V</i></b>)")+": Returns the variance of the Geometric distribution<br>";
		des+="</html>";
		return(des);
	}
}