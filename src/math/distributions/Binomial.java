/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017 Zachary J. Ward
 *
 * This file is part of Amua. Amua is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Amua is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amua.  If not, see <http://www.gnu.org/licenses/>.
 */

package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Binomial{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		int n=params[1].getInt();
		double p=params[2].getProb();
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		return(new Numeric(MathUtils.bin(k,n,p)));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		int k=params[0].getInt();
		int n=params[1].getInt();
		double p=params[2].getProb();
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		double val=0;
		for(int i=0; i<=k; i++){val+=MathUtils.bin(i,n,p);}
		return(new Numeric(val));
	}
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		double x=params[0].getProb(), CDF=0;
		int n=params[1].getInt();
		double p=params[2].getProb();
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		int k=-1;
		while(x>CDF){
			CDF+=MathUtils.bin(k+1,n,p);
			k++;
		}
		k=Math.max(0, k);
		return(new Numeric(k));
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		int n=params[0].getInt();
		double p=params[1].getProb();
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		return(new Numeric(n*p));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		int n=params[0].getInt();
		double p=params[1].getProb();
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		return(new Numeric(n*p*(1-p)));
	}
	
	public static Numeric sample(Numeric params[], double rand) throws NumericException{
		if(params.length!=2){throw new NumericException("Incorrect number of parameters","Bin");}
		int n=params[0].getInt(), k=-1;
		double p=params[1].getProb(), CDF=0;
		if(n<=0){throw new NumericException("n should be >0","Bin");}
		while(rand>CDF){
			CDF+=MathUtils.bin(k+1,n,p);
			k++;
		}
		return(new Numeric(k));
	}
	
	public static String description(){
		String des="<html><b>Binomial Distribution</b><br>";
		des+="Used to model the number of successes that occur in a fixed number of repeated trials<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("n")+": Number of trials (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("p")+": Probability of success<br>";
		des+="<i><br>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Binomial distribution. Integer in "+MathUtils.consoleFont("{0,1,...,n}")+"<br>";
		des+="<i><br>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(k,n,p,<b><i>f</i></b>)")+": Returns the value of the Binomial PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(k,n,p,<b><i>F</i></b>)")+": Returns the value of the Binomial CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(x,n,p,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Binomial distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>E</i></b>)")+": Returns the mean of the Binomial distribution<br>";
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>V</i></b>)")+": Returns the variance of the Binomial distribution<br>";
		des+="</html>";
		return(des);
	}
}