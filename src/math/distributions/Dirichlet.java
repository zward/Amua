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

import java.text.MessageFormat;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.special.Gamma;

import lang.Language;
import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Dirichlet{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		Numeric x=params[0];
		if(x.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "X"),"Dir",language);} //X should be a row vector
		Numeric alpha=params[1];
		int len=alpha.ncol;
		if(alpha.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "α"),"Dir",language);} //α should be a row vector
		if(x.ncol!=alpha.ncol){throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_cols"),"X", "α"),"Dir",language);} //X and α should have the same number of columns
		double a0=0;
		double sumX=0;
		double Bnum=0;
		double logInner=0;
		for(int i=0; i<len; i++){
			double curX=x.matrix[0][i];
			if(curX<=0 || curX>=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_01_excl"), "X"),"Dir",language);} //X should be in (0,1)
			sumX+=curX;
			double curAlpha=alpha.matrix[0][i];
			if(curAlpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_concetration_param"), curAlpha),"Dir",language);} //Invalid concentration parameter in α ("+curAlpha+")
			a0+=curAlpha;
			Bnum+=Gamma.logGamma(curAlpha); //sum log-gammas to avoid overflow
			logInner+=(curAlpha-1)*Math.log(curX);
		}
		if(sumX!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_sum_1_but_val"), "X", sumX),"Dir",language);} //X should sum to 1.0 but sums to "+sumX
		double logB=Bnum-Gamma.logGamma(a0);
		double B=Math.exp(logB);
		double inner=Math.exp(logInner);
		double val=(1.0/B)*inner;
		return(new Numeric(val));
		
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.cdf_not_implemented"),"Dir",language); //CDF is not implemented
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.inv_cdf_not_implemented"),"Dir",language); //Inverse CDF is not implemented
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		Numeric alpha=params[0];
		int len=alpha.ncol;
		if(alpha.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "α"),"Dir",language);} //α should be a row vector
		double sum=0;
		for(int i=0; i<len; i++){
			double curAlpha=alpha.matrix[0][i];
			if(curAlpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_concetration_param"), curAlpha),"Dir",language);} //Invalid concentration parameter in α ("+curAlpha+")
			sum+=curAlpha;
		}
		//Calculate means
		double mean[][]=new double[1][len];
		for(int i=0; i<len; i++){
			mean[0][i]=alpha.matrix[0][i]/sum;
		}
		return(new Numeric(mean));
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		Numeric alpha=params[0];
		int len=alpha.ncol;
		if(alpha.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "α"),"Dir",language);} //α should be a row vector
		double sum=0;
		for(int i=0; i<len; i++){
			double curAlpha=alpha.matrix[0][i];
			if(curAlpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_concetration_param"), curAlpha),"Dir",language);} //Invalid concentration parameter in α ("+curAlpha+")
			sum+=curAlpha;
		}
		//Calculate covariance matrix
		double cov[][]=new double[len][len];
		for(int i=0; i<len; i++){
			for(int j=0; j<len; j++){
				if(i!=j){ //covar
					double num=-alpha.matrix[0][i]*alpha.matrix[0][j];
					double denom=sum*sum*(sum+1);
					cov[i][j]=num/denom;
				}
				else{ //var
					double num=alpha.matrix[0][i]*(sum-alpha.matrix[0][i]);
					double denom=sum*sum*(sum+1);
					cov[i][j]=num/denom;
				}
			}
		}
		return(new Numeric(cov));
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		//Validate parameters
		Numeric alpha=params[0];
		int len=alpha.ncol;
		if(alpha.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "α"),"Dir",language);} //α should be a row vector
		double y[]=new double[len];
		double sumY=0;
		for(int i=0; i<len; i++){
			double curAlpha=alpha.matrix[0][i];
			if(curAlpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_concetration_param"), curAlpha),"Dir",language);} //Invalid concentration parameter in α ("+curAlpha+")
			//Sample using independent Gammas
			GammaDistribution gamma=new GammaDistribution(null,curAlpha,1.0);
			double rand=generator.nextDouble();
			y[i]=gamma.inverseCumulativeProbability(rand);
			sumY+=y[i];
		}
		double x[][]=new double[1][len];
		for(int i=0; i<len; i++){
			x[0][i]=y[i]/sumY;
		}
		return(new Numeric(x));
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("dir.name")+"</b><br>"; //Dirichlet Distribution
		des+=language.dist.getString("dir.desc")+"<br><br>"; //A multivariate generalization of the Beta distribution
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("<b>α</b>")+": "+language.dist.getString("dir.params")+"<br>"; //Row vector of concentration parameters (Real numbers >0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Dir</b>","green")+MathUtils.consoleFont("(<b>α</b>,<b><i>~</i></b>)")+": "+language.dist.getString("dir.sample")+"<br>"; //Returns a random variable (row vector of means in base case) from the Dirichlet distribution
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Dir</b>","green")+MathUtils.consoleFont("(<b>x</b>,<b>α</b>,<b><i>f</i></b>)")+": "+language.dist.getString("dir.pdf")+"<br>"; //Returns the value of the Dirichlet PDF at x where x contains real numbers in (0,1) that sum to 1.0
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Dir</b>","green")+MathUtils.consoleFont("(<b>α</b>,<b><i>E</i></b>)")+": "+language.dist.getString("dir.mean")+"<br>"; //Returns a row vector of means of the Dirichlet distribution
		des+=MathUtils.consoleFont("<b>Dir</b>","green")+MathUtils.consoleFont("(<b>α</b>,<b><i>V</i></b>)")+": "+language.dist.getString("dir.var")+"<br>"; //Returns the covariance matrix of the Dirichlet distribution
		des+="</html>";
		return(des);
	}
}