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

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class ChiSquare{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
			return(new Numeric(chiSq.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"ChiSq",language); //x and k should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
					vals.matrix[i][j]=chiSq.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
			return(new Numeric(chiSq.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"ChiSq",language); //x and k should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], k=params[1].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
					vals.matrix[i][j]=chiSq.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language), k=params[1].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
			return(new Numeric(chiSq.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"ChiSq",language); //x and k should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language), k=params[1].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
					vals.matrix[i][j]=chiSq.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double k=params[0].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			return(new Numeric(k));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double k=params[0].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			return(new Numeric(2*k));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					vals.matrix[i][j]=2*k;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"ChiSq",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false) { //real number
			double k=params[0].getInt(language);
			if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
			ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
			double rand=generator.nextDouble();
			return(new Numeric(chiSq.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					if(k<1){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"ChiSq",language);} //k should be >0
					ChiSquaredDistribution chiSq=new ChiSquaredDistribution(null,k);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=chiSq.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("chiSq.name")+"</b><br>"; //Chi-Squared Distribution
		des+=language.dist.getString("chiSq.desc")+"<br><br>"; //Distribution of the sum of squares of k independent standard normal variables
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("k")+": "+language.dist.getString("gen.degrees_freedom")+"<br>"; //Degrees of freedom (Integer >0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.pos_real_num")+"<br>"; //Returns a random variable (mean in base case) from the Chi-Squared distribution. Positive real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Chi-Squared PDF at x
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Chi-Squared CDF at x
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(x,k,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Chi-Squared distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Chi-Squared distribution
		des+=MathUtils.consoleFont("<b>ChiSq</b>","green")+MathUtils.consoleFont("(k,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Chi-Squared distribution
		des+="</html>";
		return(des);
	}
}