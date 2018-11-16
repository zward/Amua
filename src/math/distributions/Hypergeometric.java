package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Hypergeometric{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt(), w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
		if(w<=0){throw new NumericException("w should be >0","HGeom");}
		if(b<=0){throw new NumericException("b should be >0","HGeom");}
		if(n<=0){throw new NumericException("n should be >0","HGeom");}
		double val=(MathUtils.choose(w,k)*MathUtils.choose(b,n-k))/(MathUtils.choose(w+b,n)*1.0);
		return(new Numeric(val));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt(), w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
		if(w<=0){throw new NumericException("w should be >0","HGeom");}
		if(b<=0){throw new NumericException("b should be >0","HGeom");}
		if(n<=0){throw new NumericException("n should be >0","HGeom");}
		double val=0;
		for(int i=0; i<=k; i++){
			val+=(MathUtils.choose(w,i)*MathUtils.choose(b,n-i))/(MathUtils.choose(w+b,n)*1.0);
		}
		return(new Numeric(val));
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb();
		int w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
		if(w<=0){throw new NumericException("w should be >0","HGeom");}
		if(b<=0){throw new NumericException("b should be >0","HGeom");}
		if(n<=0){throw new NumericException("n should be >0","HGeom");}
		int k=-1;
		double CDF=0;
		while(x>CDF){
			CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
			k++;
		}
		k=Math.max(0, k);
		return(new Numeric(k));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt();
		if(w<=0){throw new NumericException("w should be >0","HGeom");}
		if(b<=0){throw new NumericException("b should be >0","HGeom");}
		if(n<=0){throw new NumericException("n should be >0","HGeom");}
		return(new Numeric((n*w)/((w+b)*1.0)));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt();
		if(w<=0){throw new NumericException("w should be >0","HGeom");}
		if(b<=0){throw new NumericException("b should be >0","HGeom");}
		if(n<=0){throw new NumericException("n should be >0","HGeom");}
		double mu=mean(params).getDouble();
		double one=(w+b-n)/((w+b-1)*1.0);
		double two=n*(mu/(n*1.0));
		double three=1-mu/(n*1.0);
		double var=one*two*three;
		return(new Numeric(var));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length==3){
			int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt(), k=-1;
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			double CDF=0;
			while(rand>CDF){
				CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else{throw new NumericException("Incorrect number of parameters","HGeom");}
	}
	
	public static String description(){
		String des="<html><b>Hypergeometric Distribution</b><br>";
		des+="Used to model the number of successes in a fixed number of draws without replacement<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("w")+": Number of possible successes (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("b")+": Number of other possible outcomes (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("n")+": Number of draws (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the the Hypergeometric distribution. Integer in "+MathUtils.consoleFont("{0,1,...,<i>min</i>(w,n)}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>f</i></b>)")+": Returns the value of the Hypergeometric PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>F</i></b>)")+": Returns the value of the Hypergeometric CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(x,w,b,n,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Hypergeometric distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>E</i></b>)")+": Returns the mean of the Hypergeometric distribution<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>V</i></b>)")+": Returns the variance of the Hypergeometric distribution<br>";
		des+="</html>";
		return(des);
	}
}