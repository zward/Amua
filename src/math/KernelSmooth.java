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

import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;

public final class KernelSmooth{
	
	public static double[][] density(double data[], int n){
		int numX=data.length;
		double min=data[0], max=data[0];
		for(int i=0; i<numX; i++){
			min=Math.min(min, data[i]);
			max=Math.max(max, data[i]);
		}
		double range=max-min;
		min=min-range/10.0; max=max+range/10.0;
		range=max-min; //update range
		double nStep=range/((n-1)*1.0);
		//set up grid
		double density[][]=new double[2][n];
		
		//calculate standard deviation
		double eX=0, eX2=0;
		for(int i=0; i<numX; i++){
			eX+=data[i];
			eX2+=(data[i]*data[i]);
		}
		eX=eX/(numX*1.0);
		eX2=eX2/(numX*1.0);
		double var=eX2-eX*eX;
		double sd=Math.sqrt(var);
		double h=(4*Math.pow(sd, 5))/(3.0*n); //bandwidth
		h=Math.pow(h, 0.2);
		
		NormalDistribution norm=new NormalDistribution(null,0,1); //standard normal
		
		for(int i=0; i<n; i++){
			double curX=min+nStep*i; //x location
			density[0][i]=curX;
			double curDensity=0;
			for(int j=0; j<numX; j++){
				double xStar=(curX-data[j])/h; //(x-x_i)/h
				curDensity+=norm.density(xStar);
			}
			curDensity=curDensity/(n*h);
			density[1][i]=curDensity;
		}
		
		return(density);
	}
	
	public static double[][] histogram(double data[], int n, int numBins){
		int numX=data.length;
		double min=data[0], max=data[0];
		for(int i=0; i<numX; i++){
			min=Math.min(min, data[i]);
			max=Math.max(max, data[i]);
		}
		//bins
		int bins[]=new int[numBins+1];
		double range=(max-min);
		double binWidth=range/(numBins*1.0);
		
		for(int i=0; i<numX; i++){
			int b=(int) ((data[i]-min)/binWidth);
			bins[b]++;
		}
		
		//grid		
		double hist[][]=new double[2][n+2];
		double nStep=range/(n*1.0);
		//just left of min value (line down to 0)
		hist[0][0]=min-nStep;
		hist[1][0]=0;
		for(int i=0; i<n; i++){
			double curX=min+nStep*i; //x location
			hist[0][i+1]=curX;
			int b=(int) ((curX-min)/binWidth);
			hist[1][i+1]=bins[b];
		}
		//just right of max value (line down to 0)
		hist[0][n+1]=max+nStep;
		hist[1][n+1]=0;
		return(hist);
	}
	
	public static double[][] cdf(double dataOrig[]){
		double data[]=Arrays.copyOf(dataOrig,dataOrig.length);
		int numX=data.length;
		Arrays.sort(data);
		double cdf[][]=new double[2][numX];
		
		for(int n=0; n<numX; n++){
			cdf[0][n]=data[n]; //value
			cdf[1][n]=n/(numX*1.0); //cdf
		}
		return(cdf);
	}

	public static double[][] quantiles(double dataOrig[]){
		double data[]=Arrays.copyOf(dataOrig,dataOrig.length);
		int numX=data.length;
		Arrays.sort(data);
		double quants[][]=new double[2][numX];
		
		for(int n=0; n<numX; n++){
			quants[0][n]=n/(numX*1.0); //quantile
			quants[1][n]=data[n]; //value
			
		}
		return(quants);
	}

}