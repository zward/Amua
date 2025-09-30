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

public final class HalfCauchy{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), gamma=params[1].getDouble(language);
			if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
			if(x<0){return(new Numeric(0));}
			else{return(new Numeric(2*cauchy.density(x)));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "γ"),"HalfCauchy",language); //x and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
					double val=0;
					if(x<0) {val=0;}
					else {val=2*cauchy.density(x);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), gamma=params[1].getDouble(language);
			if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
			if(x<0){return(new Numeric(0));}
			else{return(new Numeric((2*Math.atan(x/gamma))/Math.PI));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "γ"),"HalfCauchy",language); //x and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
					double val=0;
					if(x<0) {val=0;}
					else {val=(2*Math.atan(x/gamma))/Math.PI;}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language), gamma=params[1].getDouble(language);
			if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
			double q=gamma*Math.tan((Math.PI*x)/2.0);
			return(new Numeric(q));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "γ"),"HalfCauchy",language); //x and γ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), gamma=params[1].matrix[i][j];
					if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
					double q=gamma*Math.tan((Math.PI*x)/2.0);
					vals.matrix[i][j]=q;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		double gamma=params[0].getDouble(language);
		if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
		throw new NumericException(language.message.getString("err.mean_undefined"),"HalfCauchy",language); //Mean is undefined
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		double gamma=params[0].getDouble(language);
		if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
		throw new NumericException(language.message.getString("err.var_undefined"),"HalfCauchy",language); //Variance is undefined
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"HalfCauchy",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double gamma=params[0].getDouble(language);
			if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
			CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
			double rand=generator.nextDouble();
			return(new Numeric(Math.abs(cauchy.inverseCumulativeProbability(rand))));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double gamma=params[0].matrix[i][j];
					if(gamma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ"),"HalfCauchy",language);} //γ should be >0
					CauchyDistribution cauchy=new CauchyDistribution(null,0,gamma);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=Math.abs(cauchy.inverseCumulativeProbability(rand));
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("halfCauchy.name")+"</b><br>"; //Half-Cauchy Distribution
		des+=language.dist.getString("halfCauchy.desc")+"<br><br>"; //Positive Half-Cauchy. <i>Note</i>: Mean and variance are undefined
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("γ")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(γ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample_median")+". "+language.dist.getString("gen.real_num_gt0")+"<br>"; //Returns a random variable (median in base case) from the Half-Cauchy distribution. Real number >0
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Half-Cauchy PDF at x
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Half-Cauchy CDF at x
		des+=MathUtils.consoleFont("<b>HalfCauchy</b>","green")+MathUtils.consoleFont("(x,γ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Half-Cauchy distribution at x
		des+="</html>";
		return(des);
	}
}