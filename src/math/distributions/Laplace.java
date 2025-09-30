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

import org.apache.commons.math3.distribution.LaplaceDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Laplace{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
			return(new Numeric(lap.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Laplace",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
					vals.matrix[i][j]=lap.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
			return(new Numeric(lap.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Laplace",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j], mu=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
					vals.matrix[i][j]=lap.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), mu=params[1].getDouble(language), b=params[2].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
			return(new Numeric(lap.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"Laplace",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i,j,language), mu=params[1].matrix[i][j], b=params[2].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
					vals.matrix[i][j]=lap.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			return(new Numeric(mu)); 
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], b=params[1].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					vals.matrix[i][j]=mu;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double b=params[1].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			return(new Numeric(2*b*b));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], b=params[1].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					vals.matrix[i][j]=2*b*b;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Laplace",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), b=params[1].getDouble(language);
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
			LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
			double rand=generator.nextDouble();
			return(new Numeric(lap.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "b"),"Laplace",language);} //μ and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j], b=params[1].matrix[i][j];
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"Laplace",language);} //b should be >0
					LaplaceDistribution lap=new LaplaceDistribution(null,mu,b);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=lap.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("laplace.name")+"</b><br>"; //Laplace Distribution
		des+=language.dist.getString("laplace.desc")+"<br><br>"; //A continuous distribution that can be thought of as two Exponential distributions put back-to-back
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("μ")+": "+language.dist.getString("gen.location")+"<br>"; //Location
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("gen.scale_gt0")+"<br>"; //Scale (>0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.math.getString("fx.real_num")+"<br>"; //Returns a random variable (mean in base case) from the Laplace distribution. Real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Laplace PDF at x
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Laplace CDF at x
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(x,μ,b,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Laplace distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Laplace distribution
		des+=MathUtils.consoleFont("<b>Laplace</b>","green")+MathUtils.consoleFont("(μ,b,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Laplace distribution
		des+="</html>";
		return(des);
	}
}