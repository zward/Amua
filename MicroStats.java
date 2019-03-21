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
public class MicroStats{
	AmuaModel myModel;
	public DimInfo dimInfo;
	
	/**0=Decision Tree, 1=Markov Model*/
	int type;

	int numPeople;
	int numDim;
	int numVars;
	
	/**
	 * [Dimension][Person]
	 */
	public double outcomes[][];
	/**
	 * [Variable][Person] 
	 */
	public double variables[][];
	
	//Summaries
	public double outcomesMean[], varsMean[];
	public double outcomesSD[], varsSD[];
	public double outcomesMin[], varsMin[];
	public double outcomesMax[], varsMax[];
	public double outcomesQ1[], varsQ1[]; //25%  1st Qu
	public double outcomesMed[], varsMed[]; //50%
	public double outcomesQ3[], varsQ3[]; //75% 3rd Qu
	
	//Constructor
	public MicroStats(AmuaModel myModel, int numPeople1){
		this.myModel=myModel;
		this.dimInfo=myModel.dimInfo;
		this.numPeople=numPeople1;
		numDim=dimInfo.dimNames.length;
		outcomes=new double[numDim][numPeople];
		numVars=myModel.variables.size();
		variables=new double[numVars][numPeople];
	}

	public void printSummary(Console console){
		calcSummary();
		
		boolean colTypes[]=new boolean[]{false,true,true,true,true,true,true,true}; //is column number (true), or text (false)
		
		//outcomes
		ConsoleTable table1=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Outcome","Mean","SD","Min","Q1 (25%)","Median","Q3 (75%)","Max"};
		table1.addRow(headers);
		int maxDec=0;
		for(int d=0; d<numDim; d++){
			int dec=dimInfo.decimals[d];
			maxDec=Math.max(dec, maxDec);
			String row[]=new String[]{dimInfo.dimNames[d],
					MathUtils.round(outcomesMean[d],dec)+"",
					MathUtils.round(outcomesSD[d],dec)+"",
					MathUtils.round(outcomesMin[d],dec)+"",
					MathUtils.round(outcomesQ1[d],dec)+"",
					MathUtils.round(outcomesMed[d],dec)+"",
					MathUtils.round(outcomesQ3[d],dec)+"",
					MathUtils.round(outcomesMax[d],dec)+""
			};
			table1.addRow(row);
		}
		table1.print();
		console.print("\n");
		
		//variables
		ConsoleTable table2=new ConsoleTable(console,colTypes);
		headers=new String[]{"Variable","Mean","SD","Min","Q1 (25%)","Median","Q3 (75%)","Max"};
		table2.addRow(headers);
		for(int v=0; v<numVars; v++){
			String row[]=new String[]{myModel.variables.get(v).name,
					MathUtils.round(varsMean[v],maxDec)+"",
					MathUtils.round(varsSD[v],maxDec)+"",
					MathUtils.round(varsMin[v],maxDec)+"",
					MathUtils.round(varsQ1[v],maxDec)+"",
					MathUtils.round(varsMed[v],maxDec)+"",
					MathUtils.round(varsQ3[v],maxDec)+"",
					MathUtils.round(varsMax[v],maxDec)+""
			};
			table2.addRow(row);
		}
		table2.print();
		console.print("\n");
	}
	
	public void write(String filepath) throws IOException{
		calcSummary();
		
		FileWriter fstream = new FileWriter(filepath); //Create new file
		BufferedWriter out = new BufferedWriter(fstream);

		out.write("Outcome,Mean,SD,Min,Q1 (25%),Median,Q3 (75%),Max"); out.newLine();
		for(int d=0; d<numDim; d++){
			out.write(dimInfo.dimNames[d]+",");
			out.write(outcomesMean[d]+",");
			out.write(outcomesSD[d]+",");
			out.write(outcomesMin[d]+",");
			out.write(outcomesQ1[d]+",");
			out.write(outcomesMed[d]+",");
			out.write(outcomesQ3[d]+",");
			out.write(outcomesMax[d]+"");
			out.newLine();
		}
		out.newLine();
		
		out.write("Variable,Mean,SD,Min,Q1 (25%),Median,Q3 (75%),Max"); out.newLine();
		for(int v=0; v<numVars; v++){
			out.write(myModel.variables.get(v).name+",");
			out.write(varsMean[v]+",");
			out.write(varsSD[v]+",");
			out.write(varsMin[v]+",");
			out.write(varsQ1[v]+",");
			out.write(varsMed[v]+",");
			out.write(varsQ3[v]+",");
			out.write(varsMax[v]+"");
			out.newLine();
		}
		out.newLine();
		
		out.close();
	}
	
	
	private void calcSummary(){
		//mean
		outcomesMean=new double[numDim];
		for(int d=0; d<numDim; d++){
			for(int p=0; p<numPeople; p++){
				outcomesMean[d]+=outcomes[d][p];
			}
			outcomesMean[d]/=(numPeople*1.0);
		}
		varsMean=new double[numVars];
		for(int v=0; v<numVars; v++){
			for(int p=0; p<numPeople; p++){
				varsMean[v]+=variables[v][p];
			}
			varsMean[v]/=(numPeople*1.0);
		}
		
		//sd
		outcomesSD=new double[numDim];
		for(int d=0; d<numDim; d++){
			for(int p=0; p<numPeople; p++){
				double diff=outcomes[d][p]-outcomesMean[d];
				outcomesSD[d]+=(diff*diff);
			}
			outcomesSD[d]/=(numPeople*1.0); //variance
			outcomesSD[d]=Math.sqrt(outcomesSD[d]); //sd
		}
		varsSD=new double[numVars];
		for(int d=0; d<numVars; d++){
			for(int p=0; p<numPeople; p++){
				double diff=variables[d][p]-varsMean[d];
				varsSD[d]+=(diff*diff);
			}
			varsSD[d]/=(numPeople*1.0); //variance
			varsSD[d]=Math.sqrt(varsSD[d]); //sd
		}
		
		//quantiles
		outcomesMin=new double[numDim]; varsMin=new double[numDim];
		outcomesQ1=new double[numDim]; varsQ1=new double[numDim];
		outcomesMed=new double[numDim]; varsMed=new double[numDim];
		outcomesQ3=new double[numDim]; varsQ3=new double[numDim];
		outcomesMax=new double[numDim]; varsMax=new double[numDim];
		
		int indexQ1=MathUtils.getQuantileIndex(numPeople, 0.25);
		int indexMed=MathUtils.getQuantileIndex(numPeople, 0.50);
		int indexQ3=MathUtils.getQuantileIndex(numPeople, 0.75);
		
		for(int d=0; d<numDim; d++){
			double curVals[]=outcomes[d];
			Arrays.sort(curVals);
			outcomesMin[d]=curVals[0];
			outcomesQ1[d]=curVals[indexQ1];
			outcomesMed[d]=curVals[indexMed];
			outcomesQ3[d]=curVals[indexQ3];
			outcomesMax[d]=curVals[numPeople-1];
		}
		for(int v=0; v<numVars; v++){
			double curVals[]=variables[v];
			Arrays.sort(curVals);
			varsMin[v]=curVals[0];
			varsQ1[v]=curVals[indexQ1];
			varsMed[v]=curVals[indexMed];
			varsQ3[v]=curVals[indexQ3];
			varsMax[v]=curVals[numPeople-1];
		}
	}
	
}