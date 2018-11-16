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
import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import math.CubicSpline;
import math.Interpreter;
import math.Numeric;
import math.NumericException;

@XmlRootElement(name="Table")
public class Table{
	@XmlElement public String name;
	@XmlElement public String type;
	@XmlElement public String lookupMethod;
	@XmlElement public String interpolate;
	@XmlElement public String boundary;
	@XmlElement public String extrapolate;
	@XmlElement public int numRows, numCols;
	@XmlElement public String headers[];
	@XmlElement public double data[][];
	@XmlElement public String notes;
	
	@XmlTransient double value;
	@XmlTransient public CubicSpline splines[];
	@XmlTransient public AmuaModel myModel;

	//Constructor
	public Table(){

	}

	public Table copy(){
		Table copyTable=new Table();
		copyTable.name=name;
		copyTable.type=type;
		copyTable.lookupMethod=lookupMethod;
		copyTable.interpolate=interpolate;
		copyTable.boundary=boundary;
		copyTable.extrapolate=extrapolate;
		copyTable.numRows=numRows; copyTable.numCols=numCols;
		copyTable.headers=new String[numCols];
		copyTable.data=new double[numRows][numCols];
		for(int c=0; c<numCols; c++){
			copyTable.headers[c]=headers[c];
			for(int r=0; r<numRows; r++){
				copyTable.data[r][c]=data[r][c];
			}
		}
		copyTable.notes=notes;
		copyTable.splines=splines; //pointer
		copyTable.myModel=myModel;
		return(copyTable);
	}

	public int getColumnIndex(String colText){
		int col=-1;
		if(colText.contains("\"") || colText.contains("\'")){ //String
			//Trim quotes
			colText=colText.replace("\"","");
			colText=colText.replace("\'","");
			boolean found=false;
			while(found==false && col<numCols){
				col++;
				if(colText.equals(headers[col])){found=true;}
			}
		}
		else{ //Try evaluate as integer
			try{
				col=Interpreter.evaluate(colText,myModel,false).getInt();
			}
			catch(Exception  e){
				col=-1;
			}
		}
		return(col);
	}
	
	public double getLookupValue(double index, String colText){
		//Get column index
		int col=getColumnIndex(colText);
		if(col<1 || col>(numCols-1)){return(Double.NaN);} //Throw error

		double val=Double.NaN;
		if(lookupMethod.matches("Exact")){
			int row=-1;
			boolean found=false;
			while(found==false && row<numRows){
				row++;
				if(index==data[row][0]){found=true;}
			}
			if(found){val=data[row][col];}
		}
		else if(lookupMethod.matches("Truncate")){
			if(index<data[0][0]){val=Double.NaN;} //Below first value - error
			else if(index>=data[numRows-1][0]){val=data[numRows-1][col];} //Above last value
			else{
				int row=0;
				while(data[row][0]<index){row++;}
				if(index==data[row][0]){val=data[row][col];}
				else{val=data[row-1][col];}
			}
		}
		else if(lookupMethod.matches("Interpolate")){
			if(interpolate.matches("Linear")){
				if(index<=data[0][0]){ //Below or at first index
					double slope=(data[1][col]-data[0][col])/(data[1][0]-data[0][0]);
					val=data[0][col]-(data[0][0]-index)*slope;
				}
				else if(index>data[numRows-1][0]){ //Above last index
					double slope=(data[numRows-1][col]-data[numRows-2][col])/(data[numRows-1][0]-data[numRows-2][0]);
					val=data[numRows-1][col]+(index-data[numRows-1][0])*slope;
				}
				else{ //Between
					int row=0;
					while(data[row][0]<index){row++;}
					double slope=(data[row][col]-data[row-1][col])/(data[row][0]-data[row-1][0]);
					val=data[row-1][col]+(index-data[row-1][0])*slope;
				}
			}
			else if(interpolate.matches("Cubic Splines")){
				val=splines[col-1].evaluate(index);
			}

			//Check extrapolation conditions
			if(extrapolate.matches("No")){
				if(index<=data[0][0]){val=data[0][col];} //Below or at first index
				else if(index>data[numRows-1][0]){val=data[numRows-1][col];} //Above last index
			}
			else if(extrapolate.matches("Left only")){ //truncate right
				if(index>data[numRows-1][0]){val=data[numRows-1][col];} //Above last index
			}
			else if(extrapolate.matches("Right only")){ //truncate left
				if(index<=data[0][0]){val=data[0][col];} //Below or at first index
			}

		}

		return(val);
	}

	public void constructSplines(){
		int numY=numCols-1;
		splines=new CubicSpline[numY];
		for(int i=0; i<numY; i++){
			splines[i]=new CubicSpline(data,i+1,boundary);
		}
	}
	
	public Numeric evaluateDist(String params[], int df) throws Exception{
		if(params.length==2){ //(p,~)
			if(df==-1 || df==3){ //Sample, Mean
				int col=getColumnIndex(params[0]);
				if(col<1 || col>(numCols-1)){throw new NumericException("Invalid column index: "+col+" ("+params[0]+")",name);} //Throw error
				return(new Numeric(calcEV(col)));
			} 
			else if(df==4){ //Variance
				int col=getColumnIndex(params[0]);
				if(col<1 || col>(numCols-1)){throw new NumericException("Invalid column index: "+col+" ("+params[0]+")",name);} //Throw error
				return(new Numeric(calcVariance(col)));
			} 
			else{throw new NumericException("Invalid parameters",name);}
		}
		else if(params.length==3){ //(k,n,f): PMF/CDF
			int col=getColumnIndex(params[1]);
			if(col<1 || col>(numCols-1)){throw new NumericException("Invalid column index: "+col+" ("+params[1]+")",name);} //Throw error
			if(df==0){ //PMF
				double k=Interpreter.evaluate(params[0], myModel,false).getDouble();
				int row=-1;
				boolean found=false;
				double val=0;
				while(found==false && row<(numRows-1)){
					row++;
					if(data[row][col]==k){
						found=true;
						val=data[row][0]; //Prob
					}
				}
				return(new Numeric(val));
			} 
			else if(df==1){ //CDF
				double k=Interpreter.evaluate(params[0], myModel,false).getDouble();
				double CDF=0;
				if(k<data[0][col]){CDF=0;}
				else if(k>=data[numRows-1][col]){CDF=1;}
				else{
					int row=0;
					while(data[row][col]<=k){
						CDF+=data[row][0];
						row++;
					}
				}
				return(new Numeric(CDF));
			}
			else if(df==2){ //Quantile
				double x=Interpreter.evaluate(params[0], myModel,false).getProb(), CDF=0;
				int row=0;
				while(x>CDF){
					CDF+=data[row][0];
					row++;
				}
				return(new Numeric(data[row][col]));
			}
			else{throw new NumericException("Invalid parameters",name);}
		}
		else{throw new NumericException("Incorrect number of parameters",name);}
	}

	public double calcEV(int col){
		double ev=0;
		for(int r=0; r<numRows; r++){
			ev+=data[r][0]*data[r][col];
		}
		return(ev);
	}
	
	public double calcVariance(int col){
		double EX=0, EX2=0;
		for(int r=0; r<numRows; r++){
			EX+=data[r][0]*data[r][col];
			EX2+=data[r][0]*(data[r][col]*data[r][col]);
		}
		double var=EX2-EX*EX;
		return(var);
	}

	public double sample(String params[],double rand) throws Exception{
		int col=getColumnIndex(params[0]);
			if(col<1 || col>(numCols-1)){return(Double.NaN);} //Throw error
			else{ //Valid column
				int row=0;
				double cdf=data[row][0]; //First entry
				while(rand>cdf){
					row++;
					cdf+=data[row][0];
				}
				value=data[row][col];
			}
		
		
		return(value);
	}
	
	public void writeCSV(String filepath, ErrorLog errorLog){
		try{
			FileWriter fstream;
			fstream = new FileWriter(filepath); //Create new file
			BufferedWriter outTable = new BufferedWriter(fstream);
			//Headers
			for(int c=0; c<numCols-1; c++){
				outTable.write(headers[c]+",");
			}
			outTable.write(headers[numCols-1]); 
			outTable.newLine();
			//Data
			for(int r=0; r<numRows; r++){
				for(int c=0; c<numCols-1; c++){
					outTable.write(data[r][c]+",");
				}
				outTable.write(data[r][numCols-1]+"");	
				outTable.newLine();
			}
			outTable.close();
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}
}