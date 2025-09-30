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

import org.apache.commons.math3.special.Gamma;

import lang.Language;
import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Multinomial{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		Numeric k=params[0];
		if(k.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "k"),"Multi",language);} //k should be a row vector
		int len=k.ncol;
		int n=params[1].getInt(language);
		int sum=0;
		for(int i=0; i<len; i++){
			try{
				int curK=Integer.parseInt(k.matrix[0][i]+"");
				sum+=curK;
			}catch(NumberFormatException e){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_contain_integers"), "k")+" ("+k.matrix[0][i]+")","Multi",language); //k should contain integers ("+k.matrix[0][i]+")"
			}
		}
		if(sum!=n){throw new NumericException(MessageFormat.format(language.message.getString("err.val_sums_to_val_but_should_val"), "k", sum, n),"Multi",language);} //k sums to "+sum+" but should sum to "+n
		Numeric p=params[2];
		if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Multi",language);} //p should be a row vector
		if(p.ncol!=len){throw new NumericException(MessageFormat.format(language.message.getString("err.vals_should_have_same_length"),"k","p"),"Multi",language);} //k and p should have the same length
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_prob_in_val"), "p")+" ("+curP+")","Multi",language);} //Invalid probability in p ("+curP+")
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_sums_to_val"), "p", cdf),"Multi",language);} //"p sums to "+cdf
		//Calculate log pmf to mitigate overflow
		double logPMF=Gamma.logGamma(n+1); //n
		for(int i=0; i<n; i++){logPMF-=Gamma.logGamma(k.matrix[0][i]+1);} //x_1 to x_n
		for(int i=0; i<n; i++){logPMF+=(k.matrix[0][i]*Math.log(p.matrix[0][i]));}
		double pmf=Math.exp(logPMF);
		return(new Numeric(pmf));
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.cdf_not_implemented"),"Multi",language); //CDF is not implemented
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.inv_cdf_not_implemented"),"Multi",language); //Inverse CDF is not implemented
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		int n=params[0].getInt(language);
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Multi",language);} //p should be a row vector
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_prob_in_val"), "p")+" ("+curP+")","Multi",language);} //Invalid probability in p ("+curP+")
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_sums_to_val"), "p", cdf),"Multi",language);} //"p sums to "+cdf
		//Calc mean
		double mean[][]=new double[1][len];
		for(int i=0; i<len; i++){mean[0][i]=n*p.matrix[0][i];}
		return(new Numeric(mean));
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		//Validate parameters
		int n=params[0].getInt(language);
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Multi",language);} //p should be a row vector
		double cdf=0;
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_prob_in_val"), "p")+" ("+curP+")","Multi",language);} //Invalid probability in p ("+curP+")
			cdf+=curP;
		}
		if(cdf!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_sums_to_val"), "p", cdf),"Multi",language);} //"p sums to "+cdf
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
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		//Validate parameters
		int n=params[0].getInt(language);
		Numeric p=params[1];
		int len=p.ncol;
		if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Multi",language);} //p should be a row vector
		double cdf[]=new double[len];
		for(int i=0; i<len; i++){
			double curP=p.matrix[0][i];
			if(curP<0 || curP>1){throw new NumericException(MessageFormat.format(language.message.getString("err.invalid_prob_in_val"), "p")+" ("+curP+")","Multi",language);} //Invalid probability in p ("+curP+")
			if(i>0){cdf[i]=cdf[i-1];}
			cdf[i]+=curP;
		}
		if(cdf[len-1]!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_sums_to_val"), "p", cdf),"Multi",language);} //"p sums to "+cdf
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
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("multi.name")+"</b><br>"; //Multinomial Distribution
		des+=language.dist.getString("multi.desc")+"<br><br>"; //A generalization of the Binomial distribution to multiple categories
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("n")+": "+language.dist.getString("gen.num_trials")+"<br>"; //Number of trials (Integer >0)
		des+=MathUtils.consoleFont("<b>p</b>")+": "+language.dist.getString("multi.p")+"<br>"; //Row vector containing event probabilities (Real numbers in [0,1] that sum to 1.0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>~</i></b>)")+": "+language.dist.getString("multi.sample")+"<br>"; //Returns a random vector (row vector of means in base case) from the Multinomial distribution
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(<b>k</b>,n,<b>p</b>,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "<b>k</b>")+"<br>"; //Returns the value of the Multinomial PMF at <b>k</b>
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(<b>k</b>,n,<b>p</b>,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "<b>k</b>")+"<br>"; //Returns the value of the Multinomial CDF at <b>k</b>
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(x,n,<b>p</b>,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Multinomial distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>E</i></b>)")+": "+language.dist.getString("multi.mean")+"<br>"; //Returns a row vector of means of the Multinomial distribution
		des+=MathUtils.consoleFont("<b>Multi</b>","green")+MathUtils.consoleFont("(n,<b>p</b>,<b><i>V</i></b>)")+": "+language.dist.getString("multi.var")+"<br>"; //Returns the covariance matrix of the Multinomial distribution
		des+="</html>";
		return(des);
	}
}