package math.distributions;

import math.MathUtils;
import math.MatrixFunctions;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.NormalDistribution;
import main.MersenneTwisterFast;

public final class MultivariateNormal{
	
	public static Numeric pdf(Numeric params[]) throws NumericException{
		Numeric x=params[0], mu=params[1], sigma=params[2];
		if(x.ncol!=1){ //ensure mu is column vector
			throw new NumericException("X should be a column vector","MvNorm");
		}
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException("μ should be a column vector","MvNorm");
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException("Σ should be a square matrix","MvNorm");
		}
		if(sigma.nrow!=x.nrow){ //ensure sigma and x are the right size
			throw new NumericException("μ and X should have the same number of rows","MvNorm");
		}
		int n=sigma.nrow;
		double absDet=Math.abs(MatrixFunctions.det(sigma).getDouble());
		double pre=1.0/(Math.sqrt(Math.pow(2*Math.PI,n)*absDet));
		Numeric inv=MatrixFunctions.inv(sigma);
		Numeric xMinusMu=MatrixFunctions.subtract(x, mu);
		Numeric mat1=MatrixFunctions.multiply(MatrixFunctions.tp(xMinusMu),inv);
		Numeric num=MatrixFunctions.multiply(mat1,xMinusMu);
		double post=Math.exp(-0.5*num.getDouble());
		double density=pre*post;
		return(new Numeric(density));
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		throw new NumericException("CDF is not implemented","MvNorm");
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		throw new NumericException("Inverse CDF is not implemented","MvNorm");
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		Numeric mu=params[0], sigma=params[1];
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException("μ should be a column vector","MvNorm");
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException("Σ should be a square matrix","MvNorm");
		}
		if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
			throw new NumericException("μ and Σ should have the same number of rows","MvNorm");
		}
		return(new Numeric(params[0].matrix));
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		Numeric mu=params[0], sigma=params[1];
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException("μ should be a column vector","MvNorm");
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException("Σ should be a square matrix","MvNorm");
		}
		if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
			throw new NumericException("μ and Σ should have the same number of rows","MvNorm");
		}
		return(new Numeric(params[1].matrix));
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length==2){
			Numeric mu=params[0], sigma=params[1];
			if(mu.ncol!=1){ //ensure mu is column vector
				throw new NumericException("μ should be a column vector","MvNorm");
			}
			if(sigma.ncol!=sigma.nrow){//ensure sigma is square
				throw new NumericException("Σ should be a square matrix","MvNorm");
			}
			if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
				throw new NumericException("μ and Σ should have the same number of rows","MvNorm");
			}
			int n=sigma.nrow;
			Numeric A=MatrixFunctions.chol(sigma); //get cholesky decomposition
			double randVector[][]=new double[n][1]; //random column vector of standard normals
			NormalDistribution stdNorm=new NormalDistribution(null,0,1);
			for(int i=0; i<n; i++){
				double rand=generator.nextDouble(); //Get next rand
				randVector[i][0]=stdNorm.inverseCumulativeProbability(rand);
			}
			Numeric z=new Numeric(randVector);
			Numeric Az=MatrixFunctions.multiply(A, z);
			Numeric x=MatrixFunctions.add(mu,Az);
			return(x);
		}
		else{throw new NumericException("Incorrect number of parameters","MvNorm");}
	}
	
	public static String description(){
		String des="<html><b>Multivariate Normal Distribution</b><br>";
		des+="A multivariate normal distribution<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("<b>μ</b>")+": Mean (column vector)<br>";
		des+=MathUtils.consoleFont("<b>Σ</b>")+": Covariance matrix (symmetric positive definite)<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>)")+": Returns a random vector (<b>μ</b> in base case) from the Multivariate Normal distribution. Column vector of real numbers<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>X</b>,<b>μ</b>,<b>Σ</b>,<b><i>f</i></b>)")+": Returns the value of the Multivariate Normal PDF at "+MathUtils.consoleFont("<b>X</b>")+" (column vector)<br>";
		des+="<i><br>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>,<b><i>E</i></b>)")+": Returns the mean of the Multivariate Normal distribution<br>";
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>,<b><i>V</i></b>)")+": Returns the variance of the Multivariate Normal distribution<br>";
		des+="</html>";
		return(des);
	}
}