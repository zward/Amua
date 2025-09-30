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

public final class Poisson{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			double val=0;
			val=Math.exp(k*Math.log(lambda)-lambda-Gamma.logGamma(k+1));
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "λ"),"Pois",language);} //k and λ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					double val=0;
					val=Math.exp(k*Math.log(lambda)-lambda-Gamma.logGamma(k+1));
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			double val=0;
			for(int i=0; i<=k; i++){
				val+=Math.exp(i*Math.log(lambda)-lambda-Gamma.logGamma(i+1));
			}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "λ"),"Pois",language);} //k and λ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					double val=0;
					for(int z=0; z<=k; z++){
						val+=Math.exp(z*Math.log(lambda)-lambda-Gamma.logGamma(z+1));
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language);
			double lambda=params[1].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			if(x==1){return(new Numeric(Double.POSITIVE_INFINITY));}
			int k=-1;
			double CDF=0;
			while(x>CDF){
				double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
				CDF+=curMass;
				k++;
			}
			k=Math.max(0, k);
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "λ"),"Pois",language);} //x and λ should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double lambda=params[1].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					double val=0;
					if(x==1) {val=Double.POSITIVE_INFINITY;}
					else {
						int k=-1;
						double CDF=0;
						while(x>CDF){
							double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
							CDF+=curMass;
							k++;
						}
						k=Math.max(0, k);
						val=k;
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			return(new Numeric(lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					vals.matrix[i][j]=lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			return(new Numeric(lambda));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					vals.matrix[i][j]=lambda;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Pois",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double lambda=params[0].getDouble(language);
			if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
			int k=-1;
			double CDF=0;
			double rand=generator.nextDouble();
			while(rand>CDF){
				double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
				CDF+=curMass;
				k++;
			}
			return(new Numeric(k));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double lambda=params[0].matrix[i][j];
					if(lambda<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "λ"),"Pois", language);} //λ should be >0
					int k=-1;
					double CDF=0;
					double rand=generator.nextDouble();
					while(rand>CDF){
						double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
						CDF+=curMass;
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("pois.name")+"</b><br>"; //Poisson Distribution
		des+=language.dist.getString("pois.desc")+"<br><br>"; //Used to model the number of events that occur in a fixed interval of time/space with a constant average rate
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("λ")+": "+language.dist.getString("pois.lambda")+"<br>";
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("geom.support")+"<br>"; //Returns a random variable (mean in base case) from the Poisson distribution. Integer in {0,1,...}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(k,λ,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Poisson PMF at k
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(k,λ,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Poisson CDF at k
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(x,λ,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Poisson distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Poisson distribution
		des+=MathUtils.consoleFont("<b>Pois</b>","green")+MathUtils.consoleFont("(λ,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Poisson distribution
		des+="</html>";
		return(des);
	}
}