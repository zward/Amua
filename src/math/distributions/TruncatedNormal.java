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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import lang.Language;
import main.MersenneTwisterFast;

public final class TruncatedNormal{
	
	public static Numeric pdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false && params[4].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language), a=params[3].getDouble(language), b=params[4].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			if(x<a || x>b){return(new Numeric(0.0));}
			else{
				double xi=(x-mu)/sigma;
				double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
				double Z=phi(beta)-phi(alpha);
				double pre=1.0/Math.sqrt(2*Math.PI);
				double exp=Math.exp(-0.5*xi*xi);
				double num=pre*exp;
				return(new Numeric(num/(sigma*Z)));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"TruncNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[3].nrow!=params[4].nrow || params[3].ncol!=params[4].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double mu=params[1].matrix[i][j];
					double sigma=params[2].matrix[i][j];
					double a=params[3].matrix[i][j];
					double b=params[4].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					if(x<a || x>b){vals.matrix[i][j]=0;}
					else{
						double xi=(x-mu)/sigma;
						double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
						double Z=phi(beta)-phi(alpha);
						double pre=1.0/Math.sqrt(2*Math.PI);
						double exp=Math.exp(-0.5*xi*xi);
						double num=pre*exp;
						vals.matrix[i][j]=num/(sigma*Z);
					}
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false && params[4].isMatrix()==false) { //real number
			double x=params[0].getDouble(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language), a=params[3].getDouble(language), b=params[4].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			if(x<a){return(new Numeric(0.0));}
			else if(x>b){return(new Numeric(1.0));}
			else{
				double xi=(x-mu)/sigma;
				double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
				double Z=phi(beta)-phi(alpha);
				return(new Numeric((phi(xi)-phi(alpha))/Z));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"TruncNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[3].nrow!=params[4].nrow || params[3].ncol!=params[4].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].matrix[i][j];
					double mu=params[1].matrix[i][j];
					double sigma=params[2].matrix[i][j];
					double a=params[3].matrix[i][j];
					double b=params[4].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					if(x<a){vals.matrix[i][j]=0.0;}
					else if(x>b) {vals.matrix[i][j]=1.0;}
					else{
						double xi=(x-mu)/sigma;
						double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
						double Z=phi(beta)-phi(alpha);
						vals.matrix[i][j]=(phi(xi)-phi(alpha))/Z;
					}
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false && params[4].isMatrix()==false) { //real number
			double x=params[0].getProb(language), mu=params[1].getDouble(language), sigma=params[2].getDouble(language), a=params[3].getDouble(language), b=params[4].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			if(x==0){return(new Numeric(a));}
			else if(x==1){return(new Numeric(b));}
			else{
				double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
				double phiA=phi(alpha);
				double Z=phi(beta)-phiA;
				double phiXi=x*Z+phiA;
				double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
				double q=xi*sigma+mu;
				return(new Numeric(q));
			}
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "μ"),"TruncNorm",language);} //x and μ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[3].nrow!=params[4].nrow || params[3].ncol!=params[4].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					double mu=params[1].matrix[i][j];
					double sigma=params[2].matrix[i][j];
					double a=params[3].matrix[i][j];
					double b=params[4].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					if(x==0){vals.matrix[i][j]=a;}
					else if(x==1){vals.matrix[i][j]=b;}
					else{
						double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
						double phiA=phi(alpha);
						double Z=phi(beta)-phiA;
						double phiXi=x*Z+phiA;
						double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
						double q=xi*sigma+mu;
						vals.matrix[i][j]=q;
					}
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language), a=params[2].getDouble(language), b=params[3].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
			double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
			double phiA=norm.density(alpha), phiB=norm.density(beta);
			double mean=mu+((phiA-phiB)/z)*sigma;
			return(new Numeric(mean));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j];
					double sigma=params[1].matrix[i][j];
					double a=params[2].matrix[i][j];
					double b=params[3].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
					NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
					double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
					double phiA=norm.density(alpha), phiB=norm.density(beta);
					double mean=mu+((phiA-phiB)/z)*sigma;
					vals.matrix[i][j]=mean;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language), a=params[2].getDouble(language), b=params[3].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
			double Z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
			double phiA=norm.density(alpha), phiB=norm.density(beta);
			double one=(alpha*phiA-beta*phiB)/Z;
			double two=(phiA-phiB)/Z;
			double s2=sigma*sigma;
			double var=s2*(1+one-two*two);
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j];
					double sigma=params[1].matrix[i][j];
					double a=params[2].matrix[i][j];
					double b=params[3].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
					NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
					double Z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
					double phiA=norm.density(alpha), phiB=norm.density(beta);
					double one=(alpha*phiA-beta*phiB)/Z;
					double two=(phiA-phiB)/Z;
					double s2=sigma*sigma;
					double var=s2*(1+one-two*two);
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}

	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=4){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"TruncNorm",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double mu=params[0].getDouble(language), sigma=params[1].getDouble(language), a=params[2].getDouble(language), b=params[3].getDouble(language);
			if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
			if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
			if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
			if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
			double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
			double phiA=phi(alpha);
			double Z=phi(beta)-phiA;
			double rand=generator.nextDouble();
			double phiXi=rand*Z+phiA;
			double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
			double q=xi*sigma+mu;
			return(new Numeric(q));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "μ", "σ"),"TruncNorm",language);} //μ and σ should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "σ", "a"),"TruncNorm",language);} //σ and a should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"TruncNorm",language);} //a and b should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double mu=params[0].matrix[i][j];
					double sigma=params[1].matrix[i][j];
					double a=params[2].matrix[i][j];
					double b=params[3].matrix[i][j];
					if(sigma<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "σ"),"TruncNorm",language);} //σ should be >0
					if(mu<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_gt_val"), "μ","a"),"TruncNorm",language);} //μ should be >a
					if(mu>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "μ","b"),"TruncNorm",language);} //μ should be <b
					if(a>=b){throw new NumericException(MessageFormat.format(language.message.getString("err.err.val_should_be_lt_val"), "a","b"),"TruncNorm",language);} //a should be <b
					double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
					double phiA=phi(alpha);
					double Z=phi(beta)-phiA;
					double rand=generator.nextDouble();
					double phiXi=rand*Z+phiA;
					double xi=Math.sqrt(2)*Erf.erfInv(2*phiXi-1);
					double q=xi*sigma+mu;
					vals.matrix[i][j]=q;
				}
			}
			return(vals);
		}
	}
	
	private static double phi(double x){
		return(0.5*(1+Erf.erf(x/Math.sqrt(2))));
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("truncNorm.name")+"</b><br>"; //Truncated Normal Distribution
		des+=language.dist.getString("truncNorm.desc")+"<br><br>"; //A normal distribution bound by min/max values
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("μ")+": "+language.math.getString("sum.mean")+"<br>"; //Mean
		des+=MathUtils.consoleFont("σ")+": "+language.dist.getString("halfNorm.param")+"<br>"; //Standard deviation (>0)
		des+=MathUtils.consoleFont("a")+": "+language.dist.getString("gen.min_value")+"<br>"; //Minimum value
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("gen.max_value")+"<br>"; //Maximum value
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("truncNorm.support")+"<br>"; //Returns a random variable (mean in base case) from the Truncated Normal distribution. Real number in [a,b]
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pdf"), "x")+"<br>"; //Returns the value of the Truncated Normal PDF at x
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "x")+"<br>"; //Returns the value of the Truncated Normal CDF at x
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(x,μ,σ,a,b,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Truncated Normal distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Truncated Normal distribution
		des+=MathUtils.consoleFont("<b>TruncNorm</b>","green")+MathUtils.consoleFont("(μ,σ,a,b,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Truncated Normal distribution
		des+="</html>";
		return(des);
	}
}