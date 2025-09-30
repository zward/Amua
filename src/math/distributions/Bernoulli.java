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

public final class Bernoulli{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double p=params[1].getProb(language);
			if(k==0){return(new Numeric(1-p));}
			else if(k==1){return(new Numeric(p));}
			else{return(new Numeric(0));} //outside support
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				//k and p should be the same size
				String msg = MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "p");
				throw new NumericException(msg, "Bern", language);
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j, language);
					if(k==0) {vals.matrix[i][j]=1-p;}
					else if(k==1) {vals.matrix[i][j]=p;}
					else {vals.matrix[i][j]=0;} //outside support
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			double p=params[1].getProb(language);
			if(k<0){return(new Numeric(0));}
			else if(k==0){return(new Numeric(1-p));}
			else{return(new Numeric(1.0));}  //k>=1
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				//k and p should be the same size
				String msg = MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "p");
				throw new NumericException(msg, "Bern", language);
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j, language);
					if(k<0) {vals.matrix[i][j]=0;}
					else if(k==0) {vals.matrix[i][j]=1-p;}
					else {vals.matrix[i][j]=1.0;} //k>=1
				}
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double x=params[0].getProb(language);
			double p=params[1].getProb(language);
			if(x<=(1-p)){return(new Numeric(0));}
			else{return(new Numeric(1));}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				//x and p should be the same size
				String msg = MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "p");
				throw new NumericException(msg, "Bern", language);
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double p=params[1].getMatrixProb(i, j, language);
					if(x<=(1-p)) {vals.matrix[i][j]=0;}
					else {vals.matrix[i][j]=1;}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb(language);
			return(new Numeric(p));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j, language);
					vals.matrix[i][j]=p;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb(language);
			return(new Numeric(p*(1-p)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j, language);
					vals.matrix[i][j]=p*(1-p);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=1){throw new NumericException(language.message.getString("err.incorrect_num_params"), "Bern", language);} //Incorrect number of parameters
		if(params[0].isMatrix()==false) { //real number
			double p=params[0].getProb(language);
			double rand=generator.nextDouble();
			if(rand<p){return(new Numeric(1));}
			else{return(new Numeric(0));}
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double p=params[0].getMatrixProb(i, j, language);
					double rand=generator.nextDouble();
					if(rand<p) {vals.matrix[i][j]=1.0;}
					else {vals.matrix[i][j]=0;}
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("bern.name")+"</b><br>"; //Bernoulli Distribution
		des+=language.dist.getString("bern.desc")+"<br><br>"; //The probability distribution of a single Boolean-valued outcome
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("p")+": "+language.dist.getString("desc.prob_success")+"<br>"; //Probability of success
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("bern.support")+"<br>"; //Returns a random variable (mean in base case) from the Bernoulli distribution. Integer in {0,1}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Bernoulli PMF at k
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(k,p,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Bernoulli CDF at k
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(x,p,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Bernoulli distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Bernoulli distribution
		des+=MathUtils.consoleFont("<b>Bern</b>","green")+MathUtils.consoleFont("(p,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Bernoulli distribution
		des+="</html>";
		return(des);
	}
}