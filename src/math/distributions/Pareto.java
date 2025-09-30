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

import org.apache.commons.math3.distribution.ParetoDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Pareto{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getDouble(language), alpha=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			ParetoDistribution par=new ParetoDistribution(null,k,alpha);
			return(new Numeric(par.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Pareto",language);} //x and k should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double k=params[1].matrix[i][j];
					double alpha=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					ParetoDistribution par=new ParetoDistribution(null,k,alpha);
					vals.matrix[i][j]=par.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), k=params[1].getDouble(language), alpha=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			ParetoDistribution par=new ParetoDistribution(null,k,alpha);
			return(new Numeric(par.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Pareto",language);} //x and k should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double k=params[1].matrix[i][j];
					double alpha=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					ParetoDistribution par=new ParetoDistribution(null,k,alpha);
					vals.matrix[i][j]=par.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), k=params[1].getDouble(language), alpha=params[2].getDouble(language);
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			ParetoDistribution par=new ParetoDistribution(null,k,alpha);
			return(new Numeric(par.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "k"),"Pareto",language);} //x and k should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i,j,language);
					double k=params[1].matrix[i][j];
					double alpha=params[2].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					ParetoDistribution par=new ParetoDistribution(null,k,alpha);
					vals.matrix[i][j]=par.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), alpha=params[1].getDouble(language); 
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			if(alpha<=1){return(new Numeric(Double.POSITIVE_INFINITY));}
			else{return(new Numeric((alpha*k)/(alpha-1)));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					double alpha=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					if(alpha<=1) {vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else {vals.matrix[i][j]=(alpha*k)/(alpha-1);}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), alpha=params[1].getDouble(language); 
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			if(alpha<=2){return(new Numeric(Double.POSITIVE_INFINITY));}
			else{
				double num=k*k*alpha;
				double denom=(alpha-1)*(alpha-1)*(alpha-2);
				return(new Numeric(num/denom));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					double alpha=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					if(alpha<=2) {vals.matrix[i][j]=Double.POSITIVE_INFINITY;}
					else {
						double num=k*k*alpha;
						double denom=(alpha-1)*(alpha-1)*(alpha-2);
						vals.matrix[i][j]=num/denom;
					}
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Pareto",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double k=params[0].getDouble(language), alpha=params[1].getDouble(language); 
			if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
			if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
			ParetoDistribution par=new ParetoDistribution(null,k,alpha);
			double rand=generator.nextDouble();
			return(new Numeric(par.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "α"),"Pareto",language);} //k and α should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double k=params[0].matrix[i][j];
					double alpha=params[1].matrix[i][j];
					if(k<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "k"),"Pareto",language);} //k should be >0
					if(alpha<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "α"),"Pareto",language);} //α should be >0
					ParetoDistribution par=new ParetoDistribution(null,k,alpha);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=par.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("pareto.name")+"</b><br>"; //Pareto Distribution
		des+=language.dist.getString("pareto.desc")+"<br><br>"; //A power law probability distribution
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("k")+": "+language.dist.getString("pareto.scale")+"<br>"; //Scale (>0) minimum possible value of x
		des+=MathUtils.consoleFont("α")+": "+language.dist.getString("pareto.shape")+"<br>"; //Shape (>0) Pareto index
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("gen.pos_real_num")+"<br>"; //Returns a random variable (mean in base case) from the Pareto distribution. Positive real number
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Pareto PDF at x
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Pareto CDF at x
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(x,k,α,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Pareto distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Pareto distribution
		des+=MathUtils.consoleFont("<b>Pareto</b>","green")+MathUtils.consoleFont("(k,α,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Pareto distribution
		des+="</html>";
		return(des);
	}
}