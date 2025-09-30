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

import org.apache.commons.math3.distribution.GammaDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Gamma{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getDouble(language), theta=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Gamma", language); //x and k should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getDouble(language), theta=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Gamma", language); //x and k should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), k=params[1].getDouble(language), theta=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			return(new Numeric(gamma.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Gamma", language); //x and k should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), k=params[1].matrix[i][j], theta=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					vals.matrix[i][j]=gamma.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), theta=params[1].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			return(new Numeric(k*theta));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					vals.matrix[i][j]=k*theta;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), theta=params[1].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			return(new Numeric(k*theta*theta));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					vals.matrix[i][j]=k*theta*theta;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Gamma",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), theta=params[1].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
			if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
			GammaDistribution gamma=new GammaDistribution(null,k,theta);
			double rand=generator.nextDouble();
			return(new Numeric(gamma.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "θ"),"Gamma", language); //k and θ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j], theta=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Gamma", language);} //k should be >0
					if(theta<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "θ"),"Gamma", language);} //θ should be >0
					GammaDistribution gamma=new GammaDistribution(null,k,theta);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=gamma.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("gamma.name")+"</b><br>"; //Gamma Distribution
		des+=language.dist.getString("gamma.desc")+"<br><br>"; //A continuous distribution that yields positive real numbers
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("k")+": "+language.dist.getString("gen.shape_gt0")+"<br>"; //Shape (>0)
		des+=MathUtils.consoleFont("θ")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.real_num_gt0")+"<br>"; //Returns a random variable (mean in base case) from the Gamma distribution. Real number >0
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Gamma PDF at x
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Gamma CDF at x
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(x,k,θ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Gamma distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Gamma distribution
		des+=MathUtils.consoleFont("<b>Gamma</b>","green")+MathUtils.consoleFont("(k,θ,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Gamma distribution
		des+="</html>";
		return(des);
	}
}