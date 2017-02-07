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

package main;
import org.sf.surfaceplot.ISurfacePlotModel;


public class SurfaceModel implements ISurfacePlotModel
{
	double data[][][];

	float xMin=Float.POSITIVE_INFINITY, xMax=Float.NEGATIVE_INFINITY, xStep=0;
	float yMin=Float.POSITIVE_INFINITY, yMax=Float.NEGATIVE_INFINITY, yStep=0;
	float zMin=Float.POSITIVE_INFINITY, zMax=Float.NEGATIVE_INFINITY;
	String xLabel, yLabel, zLabel;
	int curStrategy=0;
	int intervals=50;

	public SurfaceModel(double data[][][],int curStrategy,int intervals,double xMin,double xMax,double yMin,double yMax,String xLabel, String yLabel, String zLabel){
		this.data=data;
		this.curStrategy=curStrategy;
		this.intervals=intervals;
		//Get range of values
		this.xMin=(float) xMin; this.xMax=(float)xMax;
		this.yMin=(float)yMin; this.yMax=(float)yMax;
		for(int i=0; i<=intervals; i++){
			for(int j=0; j<=intervals; j++){
				float curZ=(float) data[curStrategy][i][j]; 
				zMin=Math.min(zMin, curZ); zMax=Math.max(zMax, curZ);
			}
		}
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		this.zLabel=zLabel;
		xStep=(float) ((xMax-xMin)/(intervals*1.0));
		yStep=(float) ((yMax-yMin)/(intervals*1.0));
	}


	public float calculateZ(float x, float y) {
		double z=0;
		//Get closest z
		int xIndex=(int) ((x-xMin)/xStep);
		int yIndex=(int) ((y-yMin)/yStep);
		z=data[curStrategy][xIndex][yIndex];
		return((float)z);
	}

	public void setStrategy(int strategy){
		curStrategy=strategy;
	}

	public int getPlotMode(){return ISurfacePlotModel.PLOT_MODE_SPECTRUM;}

	public boolean isBoxed(){return true;}

	public boolean isMesh(){return true;}

	public boolean isScaleBox(){return false;}

	public boolean isDisplayXY(){return true;}

	public boolean isDisplayZ(){return true;}

	public boolean isDisplayGrids(){return true;}

	public int getCalcDivisions(){return intervals+1;}

	public int getDispDivisions(){return intervals+1;}

	public float getXMin(){return xMin;}

	public float getXMax(){return xMax;}

	public float getYMin(){return yMin;}

	public float getYMax(){return yMax;}

	public float getZMin(){return zMin;}

	public float getZMax(){return zMax;}

	public String getXAxisLabel(){return xLabel;}

	public String getYAxisLabel(){return yLabel;}

	public String getZAxisLabel(){return zLabel;}
}