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
import math.MatrixFunctions;
import math.Numeric;
import math.NumericException;

import java.text.MessageFormat;

import org.apache.commons.math3.distribution.NormalDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class MultivariateNormal{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		Numeric x=params[0], mu=params[1], sigma=params[2];
		if(x.ncol!=1){ //ensure mu is column vector
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "X"),"MvNorm",language); //X should be a column vector
		}
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "μ"),"MvNorm",language); //μ should be a column vector
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_square_matrix"), "Σ"),"MvNorm",language); //Σ should be a square matrix
		}
		if(sigma.nrow!=x.nrow){ //ensure sigma and x are the right size
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "Σ", "X"),"MvNorm",language); //Σ and X should have the same number of rows
		}
		int n=sigma.nrow;
		double absDet=Math.abs(MatrixFunctions.det(sigma, language).getDouble(language));
		double pre=1.0/(Math.sqrt(Math.pow(2*Math.PI,n)*absDet));
		Numeric inv=MatrixFunctions.inv(sigma, language);
		Numeric xMinusMu=MatrixFunctions.subtract(x, mu, language);
		Numeric mat1=MatrixFunctions.multiply(MatrixFunctions.tp(xMinusMu),inv, language);
		Numeric num=MatrixFunctions.multiply(mat1,xMinusMu, language);
		double post=Math.exp(-0.5*num.getDouble(language));
		double density=pre*post;
		return(new Numeric(density));
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.cdf_not_implmented"),"MvNorm",language); //CDF is not implemented
	}	
	
	public static Numeric quantile(Numeric params[],  Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.inv_cdf_not_implmented"),"MvNorm",language); //Inverse CDF is not implemented
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		Numeric mu=params[0], sigma=params[1];
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "μ"),"MvNorm",language); //μ should be a column vector
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_square_matrix"), "Σ"),"MvNorm",language); //Σ should be a square matrix
		}
		if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "μ", "Σ"),"MvNorm",language); //μ and Σ should have the same number of rows
		}
		return(new Numeric(params[0].matrix));
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		Numeric mu=params[0], sigma=params[1];
		if(mu.ncol!=1){ //ensure mu is column vector
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "μ"),"MvNorm",language); //μ should be a column vector
		}
		if(sigma.ncol!=sigma.nrow){//ensure sigma is square
			throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_square_matrix"), "Σ"),"MvNorm",language); //Σ should be a square matrix
		}
		if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "μ", "Σ"),"MvNorm",language); //μ and Σ should have the same number of rows
		}
		return(new Numeric(params[1].matrix));
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length==2){
			Numeric mu=params[0], sigma=params[1];
			if(mu.ncol!=1){ //ensure mu is column vector
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "μ"),"MvNorm",language); //μ should be a column vector
			}
			if(sigma.ncol!=sigma.nrow){//ensure sigma is square
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_square_matrix"), "Σ"),"MvNorm",language); //Σ should be a square matrix
			}
			if(sigma.nrow!=mu.nrow){ //ensure sigma and mu are the right size
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "μ", "Σ"),"MvNorm",language); //μ and Σ should have the same number of rows
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
			Numeric Az=MatrixFunctions.multiply(A, z, language);
			Numeric x=MatrixFunctions.add(mu,Az, language);
			return(x);
		}
		else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"MvNorm",language);} //Incorrect number of parameters
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("mvNorm.name")+"</b><br>"; //Multivariate Normal Distribution
		des+=language.dist.getString("mvNorm.desc")+"<br><br>"; //A multivariate normal distribution
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("<b>μ</b>")+": "+language.dist.getString("mvNorm.mu")+"<br>"; //Mean (column vector)
		des+=MathUtils.consoleFont("<b>Σ</b>")+": "+language.dist.getString("mvNorm.sigma")+"<br>"; //Covariance matrix (symmetric positive definite)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>)")+": "+language.dist.getString("mvNorm.sample")+"<br>"; //Returns a random vector (<b>μ</b> in base case) from the Multivariate Normal distribution. Column vector of real numbers
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>X</b>,<b>μ</b>,<b>Σ</b>,<b><i>f</i></b>)")+": "+language.dist.getString("mvNorm.pdf")+"<br>"; //Returns the value of the Multivariate Normal PDF at "+MathUtils.consoleFont("<b>X</b>")+" (column vector)
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Multivariate Normal distribution
		des+=MathUtils.consoleFont("<b>MvNorm</b>","green")+MathUtils.consoleFont("(<b>μ</b>,<b>Σ</b>,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Multivariate Normal distribution
		des+="</html>";
		return(des);
	}
}