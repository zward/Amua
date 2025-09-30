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

import org.apache.commons.math3.distribution.GumbelDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Gumbel{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), beta=params[2].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Gumbel",language); //x and μ should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], beta=params[2].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double val=gumbel.density(x);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), beta=params[2].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Gumbel",language); //x and μ should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], beta=params[2].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double val=gumbel.cumulativeProbability(x);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), mu=params[1].getDouble(language), beta=params[2].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Gumbel",language); //x and μ should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), mu=params[1].matrix[i][j], beta=params[2].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double val=gumbel.inverseCumulativeProbability(x);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), beta=params[1].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.getNumericalMean()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], beta=params[1].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double val=gumbel.getNumericalMean();
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), beta=params[1].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			return(new Numeric(gumbel.getNumericalVariance()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], beta=params[1].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double val=gumbel.getNumericalVariance();
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Gumbel",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), beta=params[1].getDouble(language);
			if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
			GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
			double rand=generator.nextDouble();
			return(new Numeric(gumbel.inverseCumulativeProbability(rand))); //mean
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "β"),"Gumbel",language); //μ and β should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], beta=params[1].matrix[i][j];
					if(beta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "β"),"Gumbel", language);} //β should be >0
					GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
					double rand=generator.nextDouble();
					double val=gumbel.inverseCumulativeProbability(rand);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("gumbel.name")+"</b><br>"; //Gumbel Distribution
		des+=language.dist.getString("gumbel.desc")+"<br><br>"; //Used to model the distribution of the extrema (max/min) of a number of samples of various distributions
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("μ")+": "+language.dist.getString("gen.location")+"<br>"; //Location
		des+=MathUtils.consoleFont("β")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.math.getString("fx.real_num")+"<br>"; //Returns a random variable (mean in base case) from the Gumbel distribution. Real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Gumbel PDF at x
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Gumbel CDF at x
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(x,μ,β,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Gumbel distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Gumbel distribution
		des+=MathUtils.consoleFont("<b>Gumbel</b>","green")+MathUtils.consoleFont("(μ,β,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Gumbel distribution
		des+="</html>";
		return(des);
	}
}