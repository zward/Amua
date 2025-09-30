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

import org.apache.commons.math3.distribution.BetaDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class PERT{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			x=(x-a)/(c-a); //Transform x to 0,1
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.density(x)/(c-a))); //rescaled to range
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"PERT",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.density(x)/(c-a); //rescaled to range
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			x=(x-a)/(c-a); //Transform x to 0,1
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"PERT",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getProb(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			return(new Numeric(beta.inverseCumulativeProbability(x)*(c-a)+a)); //rescaled to range
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"PERT",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					x=(x-a)/(c-a); //Transform x to 0,1
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					vals.matrix[i][j]=beta.inverseCumulativeProbability(x)*(c-a)+a; //rescaled to range
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			return(new Numeric(mu));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					vals.matrix[i][j]=mu;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double var=((mu-a)*(c-mu))/7.0;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double mu=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double var=((mu-a)*(c-mu))/7.0;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=3){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"PERT",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
			double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
			double a1=((u-a)*(2.0*b-a-c))/((b-u)*(c-a));
			double a2=(a1*(c-u))/(u-a);
			if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
			BetaDistribution beta=new BetaDistribution(null,a1,a2);
			double rand=generator.nextDouble();
			double val=beta.inverseCumulativeProbability(rand);
			val=a+(c-a)*val; //Re-scale back to original min/max
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"PERT",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"PERT",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c","a"),"PERT", language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b","a"),"PERT",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b","c"),"PERT",language);} //b should be ≤c
					double u=(a+(4.0*b)+c)/6.0; //Weighted mean, mode is worth 4x
					double a1=((u-a)*(2.0*b-a-c))/((b-u)*(c-a));
					double a2=(a1*(c-u))/(u-a);
					if(b==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
					BetaDistribution beta=new BetaDistribution(null,a1,a2);
					double rand=generator.nextDouble();
					double val=beta.inverseCumulativeProbability(rand);
					val=a+(c-a)*val; //Re-scale back to original min/max
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("pert.name")+"</b><br>"; //PERT Distribution (Program Evaluation and Review Technique)
		des+=language.dist.getString("pert.desc")+"<br><br>"; //The PERT method converts a Triangular distribution to a Beta-shaped distribution. It is often used in risk analysis to model subjective estimates
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("a")+": "+language.dist.getString("pert.min")+"<br>"; //Minimum value, inclusive
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("pert.mode")+"<br>"; //Mode (most likely value)
		des+=MathUtils.consoleFont("c")+": "+language.dist.getString("pert.max")+"<br>"; //Maximum value, exclusive
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("pert.support")+"<br>"; //Returns a random variable (mean in base case) from the PERT distribution. Real number in [a,c)
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the PERT PDF at x
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the PERT CDF at x
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the PERT distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the PERT distribution
		des+=MathUtils.consoleFont("<b>PERT</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the PERT distribution
		des+="</html>";
		return(des);
	}
}