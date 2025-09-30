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

public final class DiscreteUniform{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language), a=params[1].getInt(language), b=params[2].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b  
			double val=0;
			if(k<a || k>b){val=0;}
			else{val=1.0/((b-a+1)*1.0);}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "a"),"DUnif", language); //k and a should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j], a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b 
					double val=0;
					if(k<a || k>b){val=0;}
					else{val=1.0/((b-a+1)*1.0);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt(language), a=params[1].getInt(language), b=params[2].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
			double val=0;
			if(k<a){val=0;}
			else if(k>=b){val=1;}
			else{val=(k-a+1)/((b-a+1)*1.0);}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "a"),"DUnif", language); //k and a should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j], a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
					double val=0;
					if(k<a){val=0;}
					else if(k>=b){val=1;}
					else{val=(k-a+1)/((b-a+1)*1.0);}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb(language);
			int a=params[1].getInt(language), b=params[2].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
			double val=a+x*(b+1-a);
			val=Math.min(b, val);
			return(new Numeric(Math.floor(val)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "a"),"DUnif", language); //x and a should be the same size
			}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language); 
					int a=(int)params[1].matrix[i][j], b=(int)params[2].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
					double val=a+x*(b+1-a);
					val=Math.min(b, val);
					vals.matrix[i][j]=Math.floor(val);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getInt(language), b=params[1].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
			return(new Numeric((a+b)/2.0));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
					vals.matrix[i][j]=(a+b)/2.0;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double a=params[0].getInt(language), b=params[1].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
			double var=((b-a+1)*(b-a+1)-1)/12.0;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
					double var=((b-a+1)*(b-a+1)-1)/12.0;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=2){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"DUnif",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			int a=params[0].getInt(language), b=params[1].getInt(language);
			if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
			double rand=generator.nextDouble();
			double val=a+rand*(b+1-a);
			return(new Numeric(Math.floor(val)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {
				throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "a", "b"),"DUnif",language); //a and b should be the same size
			}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int a=(int)params[0].matrix[i][j], b=(int)params[1].matrix[i][j];
					if(b<=a){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_lt_val"), "a", "b"),"DUnif", language);} //a should be <b
					double rand=generator.nextDouble();
					double val=a+rand*(b+1-a);
					vals.matrix[i][j]=Math.floor(val);
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("dunif.name")+"</b><br>"; //Discrete Uniform Distribution
		des+=language.dist.getString("dunif.desc")+"<br><br>"; //Used to model a discrete distribution where all values are equally likely
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("a")+": "+language.dist.getString("dunif.min")+"<br>"; //Minimum value, inclusive (integer)
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("dunif.max")+"<br>"; //Maximum value, inclusive (integer)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(a,b,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("dunif.support")+"<br>"; //Returns a random variable (mean in base case) from the Discrete Uniform distribution. Integer in {a,a+1,...,b}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(k,a,b,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Discrete Uniform PMF at k
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(k,a,b,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Discrete Uniform CDF at k
		des+=MathUtils.consoleFont("<b>DUnif</b>","green")+MathUtils.consoleFont("(x,a,b,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Discrete Uniform distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(a,b,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Discrete Uniform distribution
		des+=MathUtils.consoleFont("<b>Bin</b>","green")+MathUtils.consoleFont("(a,b,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Discrete Uniform distribution
		des+="</html>";
		return(des);
	}
}