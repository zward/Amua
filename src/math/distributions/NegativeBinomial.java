package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class NegativeBinomial{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		int r=params[1].getInt();
		double p=params[2].getProb();
		if(r<0){throw new NumericException("r should be ≥0","NBin");}
		return(new Numeric(MathUtils.choose(r+k-1,r-1)*Math.pow(p, r)*Math.pow(1-p, k)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		int r=params[1].getInt();
		double p=params[2].getProb();
		if(r<0){throw new NumericException("r should be ≥0","NBin");}
		double val=0;
		for(int n=0; n<=k; n++){
			val+=(MathUtils.choose(r+n-1,r-1)*Math.pow(p, r)*Math.pow(1-p, n));
		}
		return(new Numeric(val));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getDouble();
		int r=params[1].getInt();
		double p=params[2].getProb();
		if(r<0){throw new NumericException("r should be ≥0","NBin");}
		if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
		int k=0;
		double CDF=0;
		while(x>CDF){
			CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
			k++;
		}
		return(new Numeric(k));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		int r=params[0].getInt();
		double p=params[1].getProb();
		if(r<0){throw new NumericException("r should be ≥0","NBin");}
		return(new Numeric((r*(1-p))/p));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		int r=params[0].getInt();
		double p=params[1].getProb();
		if(r<0){throw new NumericException("r should be ≥0","NBin");}
		return(new Numeric((r*(1-p))/(p*p)));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==2){
			int r=params[0].getInt(), k=0;
			double p=params[1].getProb(), CDF=0;
			if(r<0){throw new NumericException("r should be ≥0","NBin");}
			while(rand>CDF){
				CDF+=MathUtils.choose(r+k-1,r-1)*Math.pow(p,r*1.0)*Math.pow(1-p, k*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else{throw new NumericException("Incorrect number parameters","NBin");}
	}
	
	public static String description(){
		String des="<html><b>Negative Binomial Distribution</b><br>";
		des+="Used to model the number of successes that occur among repeated trials before a specified number of failures happen<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("r")+": Number of failures until the trials are stopped (Integer "+MathUtils.consoleFont("≥0")+")<br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Negative Binomial distribution. Integer in "+MathUtils.consoleFont("{0,1,...}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(k,r,p,<b><i>f</i></b>)")+": Returns the value of the Negative Binomial PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(k,r,p,<b><i>F</i></b>)")+": Returns the value of the Negative Binomial CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(x,r,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Negative Binomial distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>E</i></b>)")+": Returns the mean of the Negative Binomial distribution<br>";
		des+=MathUtils.consoleFont("<b>NBin</b>","green")+MathUtils.consoleFont("(r,p,<b><i>V</i></b>)")+": Returns the variance of the Negative Binomial distribution<br>";
		des+="</html>";
		return(des);
	}
}