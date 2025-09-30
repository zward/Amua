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

import org.apache.commons.math3.distribution.TDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class StudentT{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), nu=params[1].getDouble(language);
			if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "ν"),"StudentT",language);} //x and ν should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), nu=params[1].getDouble(language);
			if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "ν"),"StudentT",language);} //x and ν should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language), nu=params[1].getDouble(language);
			if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
			TDistribution stud=new TDistribution(null,nu);
			return(new Numeric(stud.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "ν"),"StudentT",language);} //x and ν should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double nu=params[1].matrix[i][j];
					if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
					TDistribution stud=new TDistribution(null,nu);
					vals.matrix[i][j]=stud.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble(language);
			if(nu>1){return(new Numeric(0));}
			else{throw new NumericException(language.dist.getString("student.mean_undefined"),"StudentT",language);} //Mean is undefined for ν≤1
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu<=1) {
						throw new NumericException(language.dist.getString("student.mean_undefined"),"StudentT",language); //Mean is undefined for ν≤1
					}
					else {
						vals.matrix[i][j]=0;
					}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble(language);
			if(nu>2){return(new Numeric(nu/(nu-2)));}
			else if(nu>1){return(new Numeric(Double.POSITIVE_INFINITY));}
			else{throw new NumericException(language.dist.getString("student.var_undefined"),"StudentT",language);} //Variance is undefined for ν≤1
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu>2){vals.matrix[i][j]=nu/(nu-2);}
					else if(nu>1){vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else{throw new NumericException(language.dist.getString("student.var_undefined"),"StudentT",language);} //Variance is undefined for ν≤1
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"StudentT",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double nu=params[0].getDouble(language);
			if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
			TDistribution stud=new TDistribution(null,nu);
			double rand=generator.nextDouble();
			return(new Numeric(stud.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double nu=params[0].matrix[i][j];
					if(nu<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "ν"),"StudentT",language);} //ν should be >0
					TDistribution stud=new TDistribution(null,nu);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=stud.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("student.name")+"</b><br>"; //Student's t-distribution
		des+=language.dist.getString("student.desc")+"<br><br>"; //A bell-shaped distribution centered at 0
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("ν")+": "+language.dist.getString("gen.degrees_freedom")+"<br>";
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(ν,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.math.getString("fx.real_num")+"<br>"; //Returns a random variable (median in base case) from the Student t-distribution. Real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Student t PDF at x
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Student t CDF at x
		des+=MathUtils.consoleFont("<b>StudentT</b>","green")+MathUtils.consoleFont("(x,ν,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Student t-distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(ν,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Student t-distribution
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(ν,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Student t-distribution
		des+="</html>";
		return(des);
	}
}