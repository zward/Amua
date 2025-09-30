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

import java.text.MessageFormat;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class LogNormal{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
			return(new Numeric(lnorm.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"LogNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], sigma=params[2].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
					vals.matrix[i][j]=lnorm.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
			return(new Numeric(lnorm.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"LogNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], sigma=params[2].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
					vals.matrix[i][j]=lnorm.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
			return(new Numeric(lnorm.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"LogNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), mu=params[1].matrix[i][j], sigma=params[2].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
					vals.matrix[i][j]=lnorm.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			return(new Numeric(Math.exp(mu+(sigma*sigma)/2.0)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					vals.matrix[i][j]=Math.exp(mu+(sigma*sigma)/2.0);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			double s2=sigma*sigma;
			double one=Math.exp(s2)-1;
			double two=Math.exp(2*mu+s2);
			double var=one*two;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					double s2=sigma*sigma;
					double one=Math.exp(s2)-1;
					double two=Math.exp(2*mu+s2);
					double var=one*two;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"LogNorm",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
			LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
			double rand=generator.nextDouble();
			return(new Numeric(lnorm.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"LogNorm",language);} //μ and σ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"LogNorm",language);} //σ should be >0
					LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=lnorm.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("logNorm.name")+"</b><br>"; //Log-Normal Distribution
		des+=language.dist.getString("logNorm.desc")+"<br><br>"; //A continuous distribution of a random variable whose logarithm follows a Normal distribution
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("μ")+": "+language.dist.getString("gen.location")+"<br>"; //Location
		des+=MathUtils.consoleFont("σ")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.real_num_gt0")+"<br>"; //Returns a random variable (mean in base case) from the Log-Normal distribution. Real number >0
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Log-Normal PDF at x
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Log-Normal CDF at x
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Log-Normal distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Log-Normal distribution
		des+=MathUtils.consoleFont("<b>LogNorm</b>","green")+MathUtils.consoleFont("(μ,σ,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Log-Normal distribution
		des+="</html>";
		return(des);
	}
}