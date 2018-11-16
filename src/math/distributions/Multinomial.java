package math.distributions;

import org.apache.commons.math3.special.Gamma;

import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Multinomial{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		//Validate parameters
		Numeric k=params[0];
		if(k.nrow!=1){throw new NumericException("k should be a row vector","Multi");}
		int len=k.ncol;
		int n=params[1].getInt();
		int sum=0;
		for(int i=0; i<len; i++){
			try{
				int curK=Integer.parseInt(k.matrix[0][i]+"");
				sum+=curK;
			}catch(NumberFormatException e){
				throw new NumericException("k should contain integers ("+k.matrix[0][i]+")","Multi");
			}
		}
		if(sum!=n){throw new NumericException("k sums to "+sum+" but should sum to "+n,"Multi");}
		Numeric p=params[2];
		if(p.nrow!=1){throw new NumericException("p should be a row vector","Multi");}
		if(p.ncol!=len){throw new NumericException("k and p should have the same length","Multi");}
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException("Invalid probability in p ("+curP+")","Multi");}
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException("p sums to "+cdf,"Multi");}
		//Calculate log pmf to mitigate overflow
		double logPMF=Gamma.logGamma(n+1); //n
		for(int i=0; i<n; i++){logPMF-=Gamma.logGamma(k.matrix[0][i]+1);} //x_1 to x_n
		for(int i=0; i<n; i++){logPMF+=(k.matrix[0][i]*Math.log(p.matrix[0][i]));}
		double pmf=Math.exp(logPMF);
		return(new Numeric(pmf));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		throw new NumericException("CDF is not implemented","Multi");
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		throw new NumericException("Inverse CDF is not implemented","Multi");
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		//Validate parameters
		int n=params[0].getInt();
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException("p should be a row vector","Multi");}
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException("Invalid probability in p ("+curP+")","Multi");}
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException("p sums to "+cdf,"Multi");}
		//Calc mean
		double mean[][]=new double[1][len];
		for(int i=0; i<len; i++){mean[0][i]=n*p.matrix[0][i];}
		return(new Numeric(mean));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		//Validate parameters
		int n=params[0].getInt();
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException("p should be a row vector","Multi");}
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException("Invalid probability in p ("+curP+")","Multi");}
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException("p sums to "+cdf,"Multi");}
		//Calc co-variance matrix
		double cov[][]=new double[len][len];
		for(int i=0; i<len; i++){
			for(int j=0; j<len; j++){
				if(i==j){ //diag (variance)
					cov[i][j]=n*p.matrix[0][i]*(1-p.matrix[0][i]);
				}
				else{
					cov[i][j]=-n*p.matrix[0][i]*p.matrix[0][j];
				}
			}
		}
		return(new Numeric(cov));
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		//Validate parameters
		int n=params[0].getInt();
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException("p should be a row vector","Multi");}
		double cdf[]=new double[len];
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException("Invalid probability in p ("+curP+")","Multi");}
			if(i>0){cdf[i]=cdf[i-1];}
			cdf[i]+=curP;
		}
		if(cdf[len-1]!=1){throw new NumericException("p sums to "+cdf,"Multi");}
		//sample
		double curSample[][]=new double[1][len];
		for(int i=0; i<n; i++){
			double rand=generator.nextDouble();
			int k=0;
			while(cdf[k]<rand){k++;}
			curSample[0][k]++;
		}
		return(new Numeric(curSample));
	}
	
	public static String description(){
		String des="<html><b>Multinomial Distribution</b><br>";
		des+="A generalization of the Binomial distribution to multiple categories<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("n")+": Number of trials (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("<b>p</b>")+": Row vector containing event probabilities (Real numbers in "+MathUtils.consoleFont("[0,1]")+" that sum to "+MathUtils.consoleFont("1.0")+")<br>";
		des+="<i><br>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>~</i></b>)")+": Returns a random variable (row vector of means in base case) from the Multinomial distribution<br>";
		des+="<i><br>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(<b>k</b>,n,<b>p</b>,<b><i>f</i></b>)")+": Returns the value of the Multinomial PMF at "+MathUtils.consoleFont("<b>k</b>")+"<br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(<b>k</b>,n,<b>p</b>,<b><i>F</i></b>)")+": Returns the value of the Multinomial CDF at "+MathUtils.consoleFont("<b>k</b>")+"<br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(x,n,<b>p</b>,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Multinomial distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>E</i></b>)")+": Returns a row vector of means of the Multinomial distribution<br>";
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>V</i></b>)")+": Returns the covariance matrix of the Multinomial distribution<br>";
		des+="</html>";
		return(des);
	}
}