/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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

package base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import main.Console;
import main.ConsoleTable;
import main.DimInfo;
import math.MathUtils;

/**
 * Individual-level stats
 * @author zward
 *
 */
public class MicroStatsSummary{
	
	/**
	 * [Outcome][Estimator][Mean/LB/UB]
	 */
	public double outcomesSummary[][][];
	/**
	 * [Variable][Estimator][Mean/LB/UB]
	 */
	public double varsSummary[][][];
	
	int numDim;
	int numVars;
	DimInfo dimInfo;
	AmuaModel myModel;
	
	//Constructor
	public MicroStatsSummary(RunReport reports[], int s){
		int numReports=reports.length;
		int numEst=7; //Mean, SD, Min, Q1, Med, Q3, Max
		numDim=reports[0].numDim;
		numVars=reports[0].microStats.get(0).numVars;
		myModel=reports[0].myModel;
		dimInfo=reports[0].myModel.dimInfo;
		
		outcomesSummary=new double[numDim][numEst][3];
		varsSummary=new double[numVars][numEst][3];
		
		//calculate underlying summaries
		for(int r=0; r<numReports; r++){
			reports[r].microStats.get(s).calcSummary();
		}
		
		//get mean and 95% UIs
		int boundIndices[]=MathUtils.getBoundIndices(numReports);
		int lb=boundIndices[0], ub=boundIndices[1];
		//outcomes
		for(int d=0; d<numDim; d++){
			double means[]=new double[7]; 
			double vals[][]=new double[7][numReports];
			for(int i=0; i<numReports; i++){
				MicroStats curStats=reports[i].microStats.get(s);
				means[0]+=curStats.outcomesMean[d]; vals[0][i]=curStats.outcomesMean[d];
				means[1]+=curStats.outcomesSD[d]; vals[1][i]=curStats.outcomesSD[d];
				means[2]+=curStats.outcomesMin[d]; vals[2][i]=curStats.outcomesMin[d];
				means[3]+=curStats.outcomesQ1[d]; vals[3][i]=curStats.outcomesQ1[d];
				means[4]+=curStats.outcomesMed[d]; vals[4][i]=curStats.outcomesMed[d];
				means[5]+=curStats.outcomesQ3[d]; vals[5][i]=curStats.outcomesQ3[d];
				means[6]+=curStats.outcomesMax[d]; vals[6][i]=curStats.outcomesMax[d];
			}
			//calc estimator summaries
			for(int e=0; e<7; e++){
				outcomesSummary[d][e][0]=means[e]/(numReports*1.0);
				double curVals[]=vals[e];
				Arrays.sort(curVals);
				outcomesSummary[d][e][1]=curVals[lb];
				outcomesSummary[d][e][2]=curVals[ub];
			}
		}
		
		//variables
		for(int v=0; v<numVars; v++){
			double means[]=new double[7]; 
			double vals[][]=new double[7][numReports];
			for(int i=0; i<numReports; i++){
				MicroStats curStats=reports[i].microStats.get(s);
				means[0]+=curStats.varsMean[v]; vals[0][i]=curStats.varsMean[v];
				means[1]+=curStats.varsSD[v]; vals[1][i]=curStats.varsSD[v];
				means[2]+=curStats.varsMin[v]; vals[2][i]=curStats.varsMin[v];
				means[3]+=curStats.varsQ1[v]; vals[3][i]=curStats.varsQ1[v];
				means[4]+=curStats.varsMed[v]; vals[4][i]=curStats.varsMed[v];
				means[5]+=curStats.varsQ3[v]; vals[5][i]=curStats.varsQ3[v];
				means[6]+=curStats.varsMax[v]; vals[6][i]=curStats.varsMax[v];
			}
			//calc estimator summaries
			for(int e=0; e<7; e++){
				varsSummary[v][e][0]=means[e]/(numReports*1.0);
				double curVals[]=vals[e];
				Arrays.sort(curVals);
				varsSummary[v][e][1]=curVals[lb];
				varsSummary[v][e][2]=curVals[ub];
			}
		}
	}

	public String getCell(double vals[], int dec){
		String cell=MathUtils.round(vals[0], dec)+" (";
		cell+=MathUtils.round(vals[1], dec)+"-";
		cell+=MathUtils.round(vals[2], dec)+")";
		return(cell);
	}
	
	
	public void printSummary(Console console){
		boolean colTypes[]=new boolean[]{false,true,true,true,true,true,true,true}; //is column number (true), or text (false)
		
		//outcomes
		ConsoleTable table1=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Outcome","Mean","SD","Min","Q1 (25%)","Median","Q3 (75%)","Max"};
		table1.addRow(headers);
		int maxDec=0;
		for(int d=0; d<numDim; d++){
			int dec=dimInfo.decimals[d];
			maxDec=Math.max(dec, maxDec);
			String row[]=new String[8];
			row[0]=dimInfo.dimNames[d];
			for(int i=0; i<7; i++){
				row[i+1]=getCell(outcomesSummary[d][i],dec);
			}
			table1.addRow(row);
		}
		table1.print();
		console.print("\n");
		
		//variables
		if(numVars>0){
			ConsoleTable table2=new ConsoleTable(console,colTypes);
			headers=new String[]{"Variable","Mean","SD","Min","Q1 (25%)","Median","Q3 (75%)","Max"};
			table2.addRow(headers);
			for(int v=0; v<numVars; v++){
				String row[]=new String[8];
				row[0]=myModel.variables.get(v).name;
				for(int i=0; i<7; i++){
					row[i+1]=getCell(varsSummary[v][i],maxDec);
				}
				table2.addRow(row);
			}
			table2.print();
			console.print("\n");
		}
	}
	
	public void write(String filepath) throws IOException{
		FileWriter fstream = new FileWriter(filepath); //Create new file
		BufferedWriter out = new BufferedWriter(fstream);

		String est[]=new String[]{"Mean","SD","Min","Q1 (25%)","Median","Q3 (75%)","Max"};
		out.write("Outcome,Estimate,Mean,95% LB,95% UB"); out.newLine();
		for(int d=0; d<numDim; d++){
			for(int e=0; e<7; e++){
				out.write(dimInfo.dimNames[d]+",");
				out.write(est[e]+",");
				out.write(outcomesSummary[d][e][0]+",");
				out.write(outcomesSummary[d][e][1]+",");
				out.write(outcomesSummary[d][e][2]+"");
				out.newLine();
			}
		}
		out.newLine();
		
		if(numVars>0){
			out.write("Variable,Estimate,Mean,95% LB,95% UB"); out.newLine();
			for(int v=0; v<numVars; v++){
				String varName=myModel.variables.get(v).name;
				for(int e=0; e<7; e++){
					out.write(varName+",");
					out.write(est[e]+",");
					out.write(varsSummary[v][e][0]+",");
					out.write(varsSummary[v][e][1]+",");
					out.write(varsSummary[v][e][2]+"");
					out.newLine();
				}
			}
			out.newLine();
		}
		
		out.close();
	}
	
	

	
}