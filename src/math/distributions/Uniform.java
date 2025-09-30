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

import org.apache.commons.math3.distribution.UniformRealDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Uniform{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
			return(new Numeric(uni.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Unif",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
					vals.matrix[i][j]=uni.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
			return(new Numeric(uni.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Unif",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
					vals.matrix[i][j]=uni.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), a=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
			return(new Numeric(uni.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Unif",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i,j,language);
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
					vals.matrix[i][j]=uni.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			return(new Numeric((a+b)/2.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					vals.matrix[i][j]=(a+b)/2.0;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			double sq=(b-a)*(b-a);
			return(new Numeric(sq/12.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					double sq=(b-a)*(b-a);
					vals.matrix[i][j]=sq/12.0;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Unif",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
			double rand=generator.nextDouble();
			return(new Numeric(a+rand*(b-a)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Unif",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "b", "a"),"Unif",language);} //b should be >a
					double rand=generator.nextDouble();
					vals.matrix[i][j]=a+rand*(b-a);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("unif.name")+"</b><br>"; //Uniform Distribution
		des+=language.dist.getString("unif.desc")+"<br><br>"; //Used to model a continuous distribution where all values are equally likely
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("a")+": "+language.dist.getString("pert.min")+"<br>"; //Minimum value, inclusive
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("pert.max")+"<br>"; //Maximum value, exclusive
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("unif.support")+"<br>"; //Returns a random variable (mean in base case) from the Uniform distribution. Real number in [a,b)
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Uniform PDF at x
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Uniform CDF at x
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Uniform distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Uniform distribution
		des+=MathUtils.consoleFont("<b>Unif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Uniform distribution
		des+="</html>";
		return(des);
	}
}