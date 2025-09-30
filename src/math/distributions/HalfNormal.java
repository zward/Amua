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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import lang.Language;
import main.MersenneTwisterFast;

public final class HalfNormal{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			if(x<0){return(new Numeric(0));}
			else{
				double pre=Math.sqrt(2)/(sigma*Math.sqrt(Math.PI));
				double inner=-(x*x)/(2*sigma*sigma);
				double density=pre*Math.exp(inner);
				return(new Numeric(density));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "σ"),"HalfNorm",language); //x and σ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					double val=0;
					if(x<0) {val=0;}
					else {
						double pre=Math.sqrt(2)/(sigma*Math.sqrt(Math.PI));
						double inner=-(x*x)/(2*sigma*sigma);
						double density=pre*Math.exp(inner);
						val=density;
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			if(x<=0){return(new Numeric(0));}
			else{
				double inner=x/(sigma*Math.sqrt(2));
				return(new Numeric(Erf.erf(inner)));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "σ"),"HalfNorm",language); //x and σ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					double val=0;
					if(x<=0) {val=0;}
					else {
						double inner=x/(sigma*Math.sqrt(2));
						val=Erf.erf(inner);
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language), sigma=params[1].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			double q=sigma*Math.sqrt(2)*Erf.erfInv(x);
			return(new Numeric(q));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "σ"),"HalfNorm",language); //x and σ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i,j,language), sigma=params[1].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					double q=sigma*Math.sqrt(2)*Erf.erfInv(x);
					vals.matrix[i][j]=q;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double sigma=params[0].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			double mean=sigma*Math.sqrt(2.0/Math.PI);
			return(new Numeric(mean));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double sigma=params[0].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					double mean=sigma*Math.sqrt(2.0/Math.PI);
					vals.matrix[i][j]=mean;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double sigma=params[0].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			double var=(sigma*sigma)*(1-2/Math.PI);
			return(new Numeric(var));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double sigma=params[0].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					double var=(sigma*sigma)*(1-2/Math.PI);
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"HalfNorm",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double sigma=params[0].getDouble(language), mu=0;
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
			NormalDistribution norm=new NormalDistribution(null,mu,sigma);
			double rand=generator.nextDouble();
			return(new Numeric(Math.abs(norm.inverseCumulativeProbability(rand))));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double sigma=params[0].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"HalfNorm",language);} //σ should be >0
					NormalDistribution norm=new NormalDistribution(null,0,sigma);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=Math.abs(norm.inverseCumulativeProbability(rand));
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("halfNorm.name")+"</b><br>"; //Half-Normal Distribution
		des+=language.dist.getString("halfNorm.desc")+"<br><br>"; //Positive Half-Normal
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("σ")+": "+language.dist.getString("halfNorm.param")+"<br>"; //Standard deviation (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.real_num_gt0")+"<br>"; //Returns a random variable (mean in base case) from the Half-Normal distribution. Real number >0
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Half-Normal PDF at x
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Half-Normal CDF at x
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(x,σ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Half-Normal distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Half-Normal distribution
		des+=MathUtils.consoleFont("<b>HalfNorm</b>","green")+MathUtils.consoleFont("(σ,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Half-Normal distribution
		des+="</html>";
		return(des);
	}
}