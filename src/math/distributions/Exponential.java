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

import lang.Language;
import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Exponential{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			return(new Numeric(lambda*Math.exp(-lambda*x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "λ"),"Expo",language); //x and λ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					vals.matrix[i][j]=lambda*Math.exp(-lambda*x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			return(new Numeric(1.0-Math.exp(-lambda*x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "λ"),"Expo",language); //x and λ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					vals.matrix[i][j]=1.0-Math.exp(-lambda*x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language), lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			return(new Numeric(-Math.log(1.0-x)/lambda));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "λ"),"Expo",language); //x and λ should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					vals.matrix[i][j]=-Math.log(1.0-x)/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			return(new Numeric(1.0/lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					vals.matrix[i][j]=1.0/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			return(new Numeric(1.0/(lambda*lambda)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					vals.matrix[i][j]=1.0/(lambda*lambda);
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Expo",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
			double rand=generator.nextDouble();
			return(new Numeric(-Math.log(1-rand)/lambda));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Expo",language);} //λ should be >0
					double rand=generator.nextDouble();
					vals.matrix[i][j]=-Math.log(1-rand)/lambda;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("expo.name")+"</b><br>"; //Exponential Distribution
		des+=language.dist.getString("expo.desc")+"<br><br>"; //Used to model the time between events with a constant average rate
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("λ")+": "+language.dist.getString("expo.param")+"<br>"; //Average rate of events (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>~</b></i>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.real_num_gt0")+"<br>"; //Returns a random variable (mean in base case) from the Exponential distribution. Real number >0
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Exponential PDF at x
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Exponential CDF at x
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Exponential distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Exponential distribution
		des+=MathUtils.consoleFont("<b>Expo</b>","green")+MathUtils.consoleFont("(λ,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Exponential distribution
		des+="</html>";
		return(des);
	}
}