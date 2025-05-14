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


package math;

import org.apache.commons.math3.special.Gamma;

public final class MathUtils{
	
	/**
	 * Numerical Tolerance to check difference from 1.0
	 */
	public static double tolerance=Math.pow(10, -8);
	
	public static double round(double num, int numDecimals){
		double scale=Math.pow(10, numDecimals);
		num=Math.round(num*scale)/scale;
		return(num);
	}
	
	/**
	 * 
	 * @param k
	 * @param n
	 * @param p
	 * @return
	 */
	public static double bin(int k, int n, double p){
		//orig
		//double binCoeff=choose(n,k);
		//double pSuccess=Math.pow(p, k);
		//double pFail=Math.pow(1.0-p, n-k);
		//double val=binCoeff*pSuccess*pFail;
		//log
		double binCoeff=logChoose(n,k);
		double pSuccess=k*Math.log(p);
		double pFail=(n-k)*Math.log(1-p);
		double val=Math.exp(binCoeff+pSuccess+pFail);
		return(val);
	}

	public static double choose(int n, int k){
		if(k>n || k<0){return(0);}
		double value=1;
		for(int i=1; i<=k; i++){
			double ratio=(n+1-i)/(i*1.0);
			value*=ratio;
		}
		return(value);
	}
	
	public static double logChoose(double x, double y){
		double num=Gamma.logGamma(x+1);
		double denom=Gamma.logGamma(y+1)+Gamma.logGamma(x-y+1);
		double val=num-denom;
		return(val);
	}
	
	public static int factorial(int n){
		if(n==0){return(1);}
		int fact=n;
		while(n>1){
			n--;
			fact*=n;
		}
		return(fact);
	}
	
	public static int[] getBoundIndices(double dNum){
		int num=(int)dNum;
		int lb=(int) Math.round(0.025*num)-1;
		int ub=(int) Math.round(0.975*num)-1;
		lb=Math.max(0, lb); ub=Math.max(0, ub); //floor of 0
		lb=Math.min(num-1, lb); ub=Math.min(num-1, ub); //ceiling of num-1
		if(lb>=ub){ //set to min and max
			lb=0; ub=num-1;
		}
		return(new int[]{lb,ub});
	}
	
	public static int getQuantileIndex(double dNum, double quantile){
		int num=(int)dNum;
		int index=(int) Math.round(quantile*num)-1;
		index=Math.max(0, index); //floor of 0
		index=Math.min(num-1, index); //ceiling of num-1
		return(index);
	}
		
	/**
	 * Calculates the variance
	 * @return
	 * @throws NumericException 
	 */
	public static double var(Numeric...args) throws NumericException{
		int numArgs=args.length;
		double eX=0, eX2=0;
		int count=0;
		for(int i=0; i<numArgs; i++){
			if(args[i].format!=Format.MATRIX){
				double x=args[i].getDouble();
				count++;
				eX+=x;
				eX2+=x*x;
			}
			else{
				for(int r=0; r<args[i].nrow; r++){
					for(int c=0; c<args[i].ncol; c++){
						double x=args[i].matrix[r][c];
						count++;
						eX+=x;
						eX2+=x*x;
					}
				}
			}
		}
		eX/=(count*1.0);
		eX2/=(count*1.0);
		double var=eX2-eX*eX;
		return(var);
	}
	
	public static double calcRange(double vals[]) {
		int numVals=vals.length;
		double min=vals[0], max=vals[0];
		for(int i=0; i<numVals; i++) {
			min=Math.min(min, vals[i]);
			max=Math.max(max, vals[i]);
		}
		double range=max-min;
		return(range);
	}
	
	
	public static String consoleFont(String str){
		return("<font face=\"Consolas\">"+str+"</font>");
	}
	
	public static String consoleFont(String str, String col){
		return("<font face=\"Consolas\" color=\""+col+"\">"+str+"</font>");
	}
}