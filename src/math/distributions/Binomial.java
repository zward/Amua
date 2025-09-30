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

public final class Binomial{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			int n=params[1].getInt(language);
			double p=params[2].getProb(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			return(new Numeric(MathUtils.bin(k,n,p)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "n"),"Bin", language); //k and n should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					int n=(int) params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					vals.matrix[i][j]=MathUtils.bin(k, n, p);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language);
			int n=params[1].getInt(language);
			double p=params[2].getProb(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			double val=0;
			for(int i=0; i<=k; i++){val+=MathUtils.bin(i,n,p);}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "n"),"Bin", language); //k and n should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int) params[0].matrix[i][j];
					int n=(int) params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					double val=0;
					for(int z=0; z<=k; z++){val+=MathUtils.bin(z,n,p);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language), CDF=0;
			int n=params[1].getInt(language);
			double p=params[2].getProb(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			int k=-1;
			while(x>CDF){
				CDF+=MathUtils.bin(k+1,n,p);
				k++;
			}
			k=Math.max(0, k);
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "n"),"Bin", language); //x and n should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					int n=(int) params[1].matrix[i][j];
					double p=params[2].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					int k=-1;
					double CDF=0;
					while(x>CDF){
						CDF+=MathUtils.bin(k+1,n,p);
						k++;
					}
					k=Math.max(0, k);
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int n=params[0].getInt(language);
			double p=params[1].getProb(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			return(new Numeric(n*p));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int n=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					vals.matrix[i][j]=n*p;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int n=params[0].getInt(language);
			double p=params[1].getProb(language);
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			return(new Numeric(n*p*(1-p)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int n=(int) params[0].matrix[i][j];
					double p=params[1].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					vals.matrix[i][j]=n*p*(1-p);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){throw new NumericException(language.message.getString("err.incorrect_num_params"), "Bin", language);} //Incorrect number of parameters
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int n=params[0].getInt(language), k=-1;
			double p=params[1].getProb(language), CDF=0;
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
			double rand=generator.nextDouble();
			while(rand>CDF){
				CDF+=MathUtils.bin(k+1,n,p);
				k++;
			}
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "n", "p"),"Bin", language); //n and p should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int n=(int) params[0].matrix[i][j], k=-1;
					double p=params[1].getMatrixProb(i, j, language);
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"Bin",language);} //n should be >0
					double CDF=0, rand=generator.nextDouble();
					while(rand>CDF){
						CDF+=MathUtils.bin(k+1,n,p);
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("bin.name")+"</b><br>"; //Binomial Distribution
		des+=language.dist.getString("bin.desc")+"<br><br>"; //Used to model the number of successes that occur in a fixed number of repeated trials
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("n")+": "+language.dist.getString("gen.num_trials")+"<br>"; //Number of trials (Integer >0)
		des+=MathUtils.consoleFont("p")+": "+language.dist.getString("desc.prob_success")+"<br>"; //Probability of success
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("bin.support")+"<br>"; //Returns a random variable (mean in base case) from the Binomial distribution. Integer in {0,1,…,n}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(k,n,p,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Binomial PMF at k
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(k,n,p,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Binomial CDF at k
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(x,n,p,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Binomial distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Binomial distribution
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(n,p,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Binomial distribution
		des+="</html>";
		return(des);
	}
}