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

import org.apache.commons.math3.distribution.TriangularDistribution;

import lang.Language;
import main.MersenneTwisterFast;

public final class Triangular{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
			return(new Numeric(tri.density(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Tri",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
					vals.matrix[i][j]=tri.density(x);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
			return(new Numeric(tri.cumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Tri",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
					vals.matrix[i][j]=tri.cumulativeProbability(x);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getProb(language), a=params[1].getDouble(language), b=params[2].getDouble(language), c=params[3].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
			return(new Numeric(tri.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"Tri",language);} //x and a should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double a=params[1].matrix[i][j];
					double b=params[2].matrix[i][j];
					double c=params[3].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
					vals.matrix[i][j]=tri.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language); 
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			return(new Numeric((a+b+c)/3.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					vals.matrix[i][j]=(a+b+c)/3.0;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language); 
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			double num=(a*a)+(b*b)+(c*c)-(a*b)-(a*c)-(b*c);
			return(new Numeric(num/18.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					double num=(a*a)+(b*b)+(c*c)-(a*b)-(a*c)-(b*c);
					vals.matrix[i][j]=num/18.0;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=3){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"Tri",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double a=params[0].getDouble(language), b=params[1].getDouble(language), c=params[2].getDouble(language);
			if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
			if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
			if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
			TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
			double rand=generator.nextDouble();
			return(new Numeric(tri.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"Tri",language);} //a and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "c"),"Tri",language);} //b and c should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double a=params[0].matrix[i][j];
					double b=params[1].matrix[i][j];
					double c=params[2].matrix[i][j];
					if(c<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt_val"), "c", "a"),"Tri",language);} //c should be >a
					if(b<a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gte_val"), "b", "a"),"Tri",language);} //b should be ≥a
					if(b>c){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lte_val"), "b", "c"),"Tri",language);} //b should be ≤c
					TriangularDistribution tri=new TriangularDistribution(null,a,b,c);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=tri.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("tri.name")+"</b><br>"; //Triangular Distribution
		des+=language.dist.getString("tri.desc")+"<br><br>"; //A simple distribution to model the minimum, most likely, and maximum values of a random variable
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("a")+": "+language.dist.getString("pert.min")+"<br>"; //Minimum value, inclusive
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("pert.mode")+"<br>"; //Mode (most likely value)
		des+=MathUtils.consoleFont("c")+": "+language.dist.getString("pert.max")+"<br>"; //Maximum value, exclusive
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("pert.support")+"<br>"; //Returns a random variable (mean in base case) from the Triangular distribution. Real number in [a,c)
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Triangular PDF at x
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Triangular CDF at x
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(x,a,b,c,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Triangular distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Triangular distribution
		des+=MathUtils.consoleFont("<b>Tri</b>","green")+MathUtils.consoleFont("(a,b,c,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Triangular distribution
		des+="</html>";
		return(des);
	}
}