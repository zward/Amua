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

import org.apache.commons.math3.distribution.CauchyDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Cauchy{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "x_0"),"Cauchy",language); //x and x_0 should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x_0", "γ"),"Cauchy",language); //x_0 and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "x_0"),"Cauchy",language); //x and x_0 should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x_0", "γ"),"Cauchy",language); //x_0 and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			return(new Numeric(cauchy.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "x_0"),"Cauchy",language); //x and x_0 should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x_0", "γ"),"Cauchy",language); //x_0 and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), a=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					vals.matrix[i][j]=cauchy.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.mean_undefined"),"Cauchy",language); //Mean is undefined
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		throw new NumericException(language.message.getString("err.var_undefined"),"Cauchy",language); //Variance is undefined
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Cauchy", language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
			double rand=generator.nextDouble();
			return(new Numeric(cauchy.inverseCumulativeProbability(rand)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x_0", "γ"),"Cauchy",language); //x_0 and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j], b=params[1].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"Cauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,a,b);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=cauchy.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("cauchy.name")+"</b><br>"; //Cauchy Distribution
		des+=language.dist.getString("cauchy.desc")+"<br><br>"; //A bell-shaped distribution with heavy tails. <i>Note</i>: Mean and variance are undefined
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("x\u2080")+": "+language.dist.getString("gen.location")+"<br>"; //Location
		des+=MathUtils.consoleFont("γ")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x\u2080,γ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample_median")+". "+language.math.getString("fx.real_num")+"<br>"; //Returns a random variable (median in base case) from the Cauchy distribution. Real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Cauchy PDF at x
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Cauchy CDF at x
		des+=MathUtils.consoleFont("<b>Cauchy</b>","green")+MathUtils.consoleFont("(x,x\u2080,γ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Cauchy distribution at x
		des+="</html>";
		return(des);
	}
}