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

public final class Categorical{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==true) { //real number
			//Validate parameters
			int k=params[0].getInt(language);
			Numeric p=params[1];
			if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Cat",language);} //p should be a row vector
			int n=p.ncol;
			double pmf[]=new double[n];
			double cdf[]=new double[n];
			pmf[0]=p.getMatrixProb(0,0,language); //p.matrix[0][0];
			cdf[0]=p.getMatrixProb(0,0,language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				pmf[i]=curP;
				cdf[i]=cdf[i-1]+curP;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			if(k<0||k>=n){return(new Numeric(0));}
			return(new Numeric(pmf[k]));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "k", "p"), "Cat", language); //k and p should have the same number of rows
			}
			if(params[0].ncol!=1) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "k"), "Cat", language); //k should be a column vector
			}
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			Numeric p=params[1];
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				int k=(int) params[0].matrix[r][0];
				double val=0;
				double pmf[]=new double[n];
				double cdf[]=new double[n];
				pmf[0]=p.getMatrixProb(r,0,language);
				cdf[0]=p.getMatrixProb(r,0,language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					pmf[i]=curP;
					cdf[i]=cdf[i-1]+curP;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				if(k<0||k>=n){val=0;}
				else {val=pmf[k];}
				vals.matrix[r][0]=val;
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==true) { //real number
			//Validate parameters
			int k=params[0].getInt(language);
			Numeric p=params[1];
			if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Cat",language);} //p should be a row vector
			int n=p.ncol;
			double cdf[]=new double[n];
			cdf[0]=p.getMatrixProb(0, 0, language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				cdf[i]=cdf[i-1]+curP;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			if(k<0){return(new Numeric(0));}
			if(k>=n){return(new Numeric(1.0));}
			return(new Numeric(cdf[k]));
		}
		else {
			if(params[0].nrow!=params[1].nrow) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "k", "p"), "Cat", language); //k and p should have the same number of rows
			}
			if(params[0].ncol!=1) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "k"), "Cat", language); //k should be a column vector
			}
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			Numeric p=params[1];
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				int k=(int) params[0].matrix[r][0];
				double val=0;
				double cdf[]=new double[n];
				cdf[0]=p.getMatrixProb(r,0,language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					cdf[i]=cdf[i-1]+curP;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				if(k<0) {val=0;}
				else if(k>=n) {val=1.0;}
				else {val=cdf[k];}
				vals.matrix[r][0]=val;
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==true) { //real number
			//Validate parameters
			double x=params[0].getProb(language);
			Numeric p=params[1];
			if(p.nrow!=1){throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector"), "p"),"Cat",language);} //p should be a row vector
			int n=p.ncol;
			double cdf[]=new double[n];
			cdf[0]=p.getMatrixProb(0,0,language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				cdf[i]=cdf[i-1]+curP;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			//Sample
			int k=0;
			while(cdf[k]<x){k++;}
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_have_same_num_rows"), "x", "p"),"Cat",language); //x and p should have the same number of rows
			}
			if(params[0].ncol!=1) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_col_vector"), "x"), "Cat", language); //x should be a column vector
			}
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			Numeric p=params[1];
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				double x=params[0].getMatrixProb(r,0,language);
				double cdf[]=new double[n];
				cdf[0]=p.getMatrixProb(r,0,language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					cdf[i]=cdf[i-1]+curP;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				//Sample
				int k=0;
				while(cdf[k]<x){k++;}
				vals.matrix[r][0]=k;
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		Numeric p=params[0];
		if(p.isMatrix()==false) {
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector_mat"), "p"),"Cat",language); //p should be a row vector or matrix
		}
		if(p.nrow==1) { //row vector
			int n=p.ncol;
			double cdf[]=new double[n];
			double sum=0;
			cdf[0]=p.getMatrixProb(0, 0, language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				cdf[i]=cdf[i-1]+curP;
				sum+=curP*i;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			return(new Numeric((int)Math.round(sum)));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				double cdf[]=new double[n];
				double sum=0;
				cdf[0]=p.getMatrixProb(r,0,language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					cdf[i]=cdf[i-1]+curP;
					sum+=curP*i;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				vals.matrix[r][0]=(int)Math.round(sum);
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		Numeric p=params[0];
		if(p.isMatrix()==false) {
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector_mat"), "p"),"Cat",language); //p should be a row vector or matrix
		}
		if(p.nrow==1) { //row vector
			int n=p.ncol;
			double cdf[]=new double[n];
			double eX=0, eX2=0;
			cdf[0]=p.getMatrixProb(0, 0, language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				cdf[i]=cdf[i-1]+curP;
				eX+=curP*i;
				eX2+=curP*i*i;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			double var=eX2-eX*eX;
			return(new Numeric(var));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				double cdf[]=new double[n];
				double eX=0, eX2=0;
				cdf[0]=p.getMatrixProb(r, 0, language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					cdf[i]=cdf[i-1]+curP;
					eX+=curP*i;
					eX2+=curP*i*i;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				double var=eX2-eX*eX;
				vals.matrix[r][0]=var;
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		Numeric p=params[0];
		if(p.isMatrix()==false) {
			throw new NumericException(MessageFormat.format(language.message.getString("err.should_be_row_vector_mat"), "p"),"Cat",language); //p should be a row vector or matrix
		}
		if(p.nrow==1) { //row vector
			int n=p.ncol;
			double cdf[]=new double[n];
			cdf[0]=p.getMatrixProb(0, 0, language);
			for(int i=1; i<n; i++){
				double curP=p.getMatrixProb(0, i, language);
				cdf[i]=cdf[i-1]+curP;
			}
			if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
			}
			//Sample
			double rand=generator.nextDouble();
			int k=0;
			while(cdf[k]<rand){k++;}
			return(new Numeric(k));
		}
		else { //matrix
			int nrow=params[0].nrow; int ncol=1;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			int n=p.ncol;
			for(int r=0; r<nrow; r++) {
				double cdf[]=new double[n];
				cdf[0]=p.getMatrixProb(r,0,language);
				for(int i=1; i<n; i++){
					double curP=p.getMatrixProb(r, i, language);
					cdf[i]=cdf[i-1]+curP;
				}
				if(Math.abs(1.0-cdf[n-1])>MathUtils.tolerance){
					throw new NumericException(MessageFormat.format(language.message.getString("err.val_sum_to_val"), "p", cdf[n-1]),"Cat",language); //p sums to "+cdf[n-1]"
				}
				//Sample
				double rand=generator.nextDouble();
				int k=0;
				while(cdf[k]<rand){k++;}
				vals.matrix[r][0]=k;
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("cat.name")+"</b><br>"; //Categorical Distribution
		des+=language.dist.getString("cat.desc")+"<br><br>"; //A discrete probability distribution of a random variable that can take on one of n possible values
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("<b>p</b>")+": "+language.dist.getString("cat.params")+"<br>"; //Row vector of size n containing event probabilities (real numbers in [0,1]) that sum to 1.0
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(<b>p</b>,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample_rounded")+". "+language.dist.getString("cat.support")+"<br>"; //Returns a random variable (rounded mean in base case) from the Categorical distribution. Integer in {0,1,...,n-1}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(k,<b>p</b>,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Categorical PMF at k
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(k,<b>p</b>,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Categorical CDF at k
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(x,<b>p</b>,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Categorical distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(<b>p</b>,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Categorical distribution
		des+=MathUtils.consoleFont("<b>Cat</b>","green")+MathUtils.consoleFont("(<b>p</b>,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Categorical distribution
		des+="</html>";
		return(des);
	}
}