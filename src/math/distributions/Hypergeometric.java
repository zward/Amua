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

import main.MersenneTwisterFast;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public final class Hypergeometric{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			int k=params[0].getInt(), w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			double val=(MathUtils.choose(w,k)*MathUtils.choose(b,n-k))/(MathUtils.choose(w+b,n)*1.0);
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and w should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
					double val=(MathUtils.choose(w,k)*MathUtils.choose(b,n-k))/(MathUtils.choose(w+b,n)*1.0);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			int k=params[0].getInt(), w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			double val=0;
			for(int i=0; i<=k; i++){
				val+=(MathUtils.choose(w,i)*MathUtils.choose(b,n-i))/(MathUtils.choose(w+b,n)*1.0);
			}
			return(new Numeric(val));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and w should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
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
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false && params[3].isMatrix()==false) { //real number
			double x=params[0].getProb();
			int w=params[1].getInt(), b=params[2].getInt(), n=params[3].getInt();
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
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
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and w should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[2].nrow!=params[3].nrow || params[2].ncol!=params[3].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					int w=(int)params[1].matrix[i][j];
					int b=(int)params[2].matrix[i][j];
					int n=(int)params[3].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
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
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt();
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			return(new Numeric((n*w)/((w+b)*1.0)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
					double val=(n*w)/((w+b)*1.0);
					vals.matrix[i][j]=val;
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt();
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			double mu=mean(params).getDouble();
			double one=(w+b-n)/((w+b-1)*1.0);
			double two=n*(mu/(n*1.0));
			double three=1-mu/(n*1.0);
			double var=one*two*three;
			return(new Numeric(var));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
					double mu=mean(params).getDouble();
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
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=3){
			throw new NumericException("Incorrect number of parameters","HGeom");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int w=params[0].getInt(), b=params[1].getInt(), n=params[2].getInt(), k=-1;
			if(w<=0){throw new NumericException("w should be >0","HGeom");}
			if(b<=0){throw new NumericException("b should be >0","HGeom");}
			if(n<=0){throw new NumericException("n should be >0","HGeom");}
			double CDF=0;
			double rand=generator.nextDouble();
			while(rand>CDF){
				CDF+=(MathUtils.choose(w,(k+1))*MathUtils.choose(b,n-(k+1)))/(MathUtils.choose(w+b,n)*1.0);
				k++;
			}
			return(new Numeric(k));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("w and b should be the same size","HGeom");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("b and n should be the same size","HGeom");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int w=(int)params[0].matrix[i][j];
					int b=(int)params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(w<=0){throw new NumericException("w should be >0","HGeom");}
					if(b<=0){throw new NumericException("b should be >0","HGeom");}
					if(n<=0){throw new NumericException("n should be >0","HGeom");}
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
	
	public static String description(){
		String des="<html><b>Hypergeometric Distribution</b><br>";
		des+="Used to model the number of successes in a fixed number of draws without replacement<br><br>";
		des+="<i>Parameters</i><br>";
		des+=MathUtils.consoleFont("w")+": Number of possible successes (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("b")+": Number of other possible outcomes (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+=MathUtils.consoleFont("n")+": Number of draws (Integer "+MathUtils.consoleFont(">0")+")<br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the the Hypergeometric distribution. Integer in "+MathUtils.consoleFont("{0,1,...,<i>min</i>(w,n)}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>f</i></b>)")+": Returns the value of the Hypergeometric PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(k,w,b,n,<b><i>F</i></b>)")+": Returns the value of the Hypergeometric CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(x,w,b,n,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Hypergeometric distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>E</i></b>)")+": Returns the mean of the Hypergeometric distribution<br>";
		des+=MathUtils.consoleFont("<b>HGeom</b>","green")+MathUtils.consoleFont("(w,b,n,<b><i>V</i></b>)")+": Returns the variance of the Hypergeometric distribution<br>";
		des+="</html>";
		return(des);
	}
}