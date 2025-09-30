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

import org.apache.commons.math3.distribution.ZipfDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Zipf{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double s=params[1].getDouble(language); int n=params[2].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.probability(k)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "s"),"Zipf",language);} //k and s should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.probability(k);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double s=params[1].getDouble(language); int n=params[2].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.cumulativeProbability(k)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "s"),"Zipf",language);} //k and s should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.cumulativeProbability(k);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language);
			double s=params[1].getDouble(language); int n=params[2].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "s"),"Zipf",language);} //k and s should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(language); int n=params[1].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.getNumericalMean()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.getNumericalMean();
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(language); int n=params[1].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.getNumericalVariance()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.getNumericalVariance();
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Zipf",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(language); int n=params[1].getInt(language);
			if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
			if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			double rand=generator.nextDouble();
			return(new Numeric(zipf.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "s", "n"),"Zipf",language);} //s and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "s"),"Zipf",language);} //s should be >0
					if(n<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte1"), "n"),"Zipf",language);} //n should be ≥1
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=zipf.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("zipf.name")+"</b><br>"; //Zipf Distribution
		des+=language.dist.getString("zipf.desc")+"<br><br>"; //Used to model discrete power law distributions
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("s")+": "+language.dist.getString("zipf.s")+"<br>"; //Exponent (real number >0)
		des+=MathUtils.consoleFont("n")+": "+language.dist.getString("zipf.n")+"<br>"; //Number of elements (integer ≥1)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("zipf.support")+"<br>"; //Returns a random variable (mean in base case) from the Zipf distribution. Integer in {1,2,...,n}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(k,s,n,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Zipf PMF at k
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(k,s,n,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Zipf CDF at k
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(x,s,n,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Zipf distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Zipf distribution
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Zipf distribution
		des+="</html>";
		return(des);
	}
}