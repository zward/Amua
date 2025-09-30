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

public final class Hypergeometric{
	
	public static Numeric pmf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			int k=params[0].getInt(language), w=params[1].getInt(language), b=params[2].getInt(language), n=params[3].getInt(language);
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			double val=(MathUtils.choose(w,k)*MathUtils.choose(b,n-k))/(MathUtils.choose(w+b,n)*1.0);
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "w"),"HGeom",language);} //k and w should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					double val=(MathUtils.choose(w,k)*MathUtils.choose(b,n-k))/(MathUtils.choose(w+b,n)*1.0);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			int k=params[0].getInt(language), w=params[1].getInt(language), b=params[2].getInt(language), n=params[3].getInt(language);
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			double val=0;
			for(int i=0; i<=k; i++){
				val+=(MathUtils.choose(w,i)*MathUtils.choose(b,n-i))/(MathUtils.choose(w+b,n)*1.0);
			}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "k", "w"),"HGeom",language);} //k and w should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					double val=0;
					for(int z=0; z<=k; z++){
						val+=(MathUtils.choose(w,z)*MathUtils.choose(b,n-z))/(MathUtils.choose(w+b,n)*1.0);
					}
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getProb(language);
			int w=params[1].getInt(language), b=params[2].getInt(language), n=params[3].getInt(language);
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			int k=-1;
			double CDF=0;
			while(x>CDF){
				CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
				k++;
			}
			k=Math.max(0, k);
			return(new Numeric(k));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "x", "w"),"HGeom",language);} //x and w should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j, language);
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					int k=-1;
					double CDF=0;
					while(x>CDF){
						CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
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
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(language), b=params[1].getInt(language), n=params[2].getInt(language);
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			return(new Numeric((n*w)/((w+b)*1.0)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					double val=(n*w)/((w+b)*1.0);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[], Language language) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(language), b=params[1].getInt(language), n=params[2].getInt(language);
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			double mu=mean(params, language).getDouble(language);
			double one=(w+b-n)/((w+b-1)*1.0);
			double two=n*(mu/(n*1.0));
			double three=1-mu/(n*1.0);
			double var=one*two*three;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					double mu=mean(params, language).getDouble(language);
					double one=(w+b-n)/((w+b-1)*1.0);
					double two=n*(mu/(n*1.0));
					double three=1-mu/(n*1.0);
					double var=one*two*three;
					vals.matrix[i][j]=var;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		if(params.length!=3){
			throw new NumericException(language.message.getString("err.incorrect_num_params"),"HGeom",language); //Incorrect number of parameters
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(language), b=params[1].getInt(language), n=params[2].getInt(language), k=-1;
			if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
			if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
			if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
			double CDF=0;
			double rand=generator.nextDouble();
			while(rand>CDF){
				CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "w", "b"),"HGeom",language);} //w and b should be the same size
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException(MessageFormat.format(language.message.getString("err.val_val_same_size"), "b", "n"),"HGeom",language);} //b and n should be the same size
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "w"),"HGeom",language);} //w should be >0
					if(b<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "b"),"HGeom",language);} //b should be >0
					if(n<=0){throw new NumericException(MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "n"),"HGeom",language);} //n should be >0
					int k=-1;
					double CDF=0;
					double rand=generator.nextDouble();
					while(rand>CDF){
						CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
						k++;
					}
					vals.matrix[i][j]=k;
				}
			}
			return(vals);
		}
	}
	
	public static String description(Language language){
		String des="<html><b>"+language.dist.getString("hgeom.name")+"</b><br>"; //Hypergeometric Distribution
		des+=language.dist.getString("hgeom.desc")+"<br><br>"; //Used to model the number of successes in a fixed number of draws without replacement
		des+="<i>"+language.base.getString("object.parameters")+"</i><br>"; //Parameters
		des+=MathUtils.consoleFont("w")+": "+language.dist.getString("hgeom.w")+"<br>"; //Number of possible successes (Integer >0)
		des+=MathUtils.consoleFont("b")+": "+language.dist.getString("hgeom.b")+"<br>"; //Number of other possible outcomes (Integer >0)
		des+=MathUtils.consoleFont("n")+": "+language.dist.getString("hgeom.n")+"<br>"; //Number of draws (Integer >0)
		des+="<i><br>"+language.dist.getString("gen.sample")+"</i><br>"; //Sample
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>~</i></b>)")+": "+language.dist.getString("desc.sample")+". "+language.dist.getString("hgeom.support")+"<br>"; //Returns a random variable (mean in base case) from the the Hypergeometric distribution. Integer in {0,1,...,<i>min</i>(w,n)}
		des+="<i><br>"+language.dist.getString("gen.distribution_functions")+"</i><br>"; //Distribution Functions
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>f</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.pmf"), "k")+"<br>"; //Returns the value of the Hypergeometric PMF at k
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>F</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.cdf"), "k")+"<br>"; //Returns the value of the Hypergeometric CDF at k
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(x,w,b,n,<b><i>Q</i></b>)")+": "+MessageFormat.format(language.dist.getString("desc.quantile"), "x")+"<br>"; //Returns the quantile (inverse CDF) of the Hypergeometric distribution at x
		des+="<i><br>"+language.dist.getString("gen.moments")+"</i><br>"; //Moments
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>E</i></b>)")+": "+language.dist.getString("desc.mean")+"<br>"; //Returns the mean of the Hypergeometric distribution
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>V</i></b>)")+": "+language.dist.getString("desc.var")+"<br>"; //Returns the variance of the Hypergeometric distribution
		des+="</html>";
		return(des);
	}
}