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

package document;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import base.AmuaModel;
import main.*;
import math.Numeric;

public class WriteRMD{
	String dir;
	BufferedWriter out;
	AmuaModel myModel;
	ErrorLog errorLog;

	public WriteRMD(String dir1, AmuaModel myModel){
		try{
			dir=dir1;
			this.myModel=myModel;
			errorLog=myModel.errorLog;

			new File(dir).mkdir(); //make directory
			dir=dir+File.separator;
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(dir+myModel.name+".Rmd"); //Create new file
			out = new BufferedWriter(fstream);
			
			//write header
			writeLine("---");
			writeLine("title: \""+myModel.name+"\"");
			writeLine("author: \""+myModel.meta.author+"\"");
			writeLine("date: \""+new Date()+"\"");
			writeLine("output: html_document"); //or pdf_document
			writeLine("---");
			writeLine("");
			writeLine("```{r setup, include=FALSE}");
			writeLine("knitr::opts_chunk$set(echo = TRUE)");
			writeLine("```");
			writeLine("");
			
			writeLine("This model was developed in Amua (https://github.com/zward/Amua).");
			writeLine("");
			writeLine("|Property|Value|");
			writeLine("|--------|-----|");
			writeLine("|Model name|"+myModel.name+"|");
			if(myModel.type==0){writeLine("|Model type|Decision Tree|");}
			else if(myModel.type==1){writeLine("|Model type|Markov Model|");}
			if(myModel.simType==0){writeLine("|Simulation type|Cohort|");}
			else if(myModel.simType==1){writeLine("|Simulation type|Monte Carlo|");}
			writeLine("");
			
			writeLine("# Model Properties");
			writeLine("");
			//metadata
			writeLine("## Metadata");
			writeLine("|Property|Value|");
			writeLine("|--------|-----|");
			writeLine("|Created by|"+myModel.meta.author+"|");
			writeLine("|Created|"+myModel.meta.dateCreated+"|");
			writeLine("|Version created|"+myModel.meta.versionCreated+"|");
			writeLine("|Modified by|"+myModel.meta.modifier+"|");
			writeLine("|Modified|"+myModel.meta.dateModified+"|");
			writeLine("|Version modified|"+myModel.meta.versionModified+"|");
			writeLine("");
			//analysis
			writeLine("## Analysis");
			writeLine("|Dimension|Symbol|Decimals|");
			writeLine("|---------|------|--------|");
			for(int d=0; d<myModel.dimInfo.dimNames.length; d++){
				out.write("|"+myModel.dimInfo.dimNames[d]);
				out.write("|"+myModel.dimInfo.dimSymbols[d]);
				out.write("|"+myModel.dimInfo.decimals[d]+"|\n");
			}
			writeLine("");
			writeLine("|Property|Value|");
			writeLine("|--------|-----|");
			if(myModel.dimInfo.analysisType==0){ //EV
				writeLine("|Analysis Type|Expected Value (EV)|");
				if(myModel.dimInfo.objective==0){writeLine("|Objective|Maximize|");}
				else if(myModel.dimInfo.objective==1){writeLine("|Objective|Minimize|");}
				writeLine("|Outcome|"+myModel.dimInfo.dimNames[myModel.dimInfo.objectiveDim]+"|");
			}
			else if(myModel.dimInfo.analysisType==1){ //CEA
				writeLine("|Analysis Type|Cost-Effectiveness Analysis (CEA)|");
				writeLine("|Cost|"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]+"|");
				writeLine("|Effect|"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]+"|");
				myModel.getStrategies();
				writeLine("|Baseline Strategy|"+myModel.strategyNames[myModel.dimInfo.baseScenario]+"|");
				writeLine("|Willingness-to-pay (WTP)|"+myModel.dimInfo.WTP+"|");
			}
			else if(myModel.dimInfo.analysisType==2){ //BCA
				writeLine("|Analysis Type|Benefit-Cost Analysis (BCA)|");
				writeLine("|Cost|"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]+"|");
				writeLine("|Benefit|"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]+"|");
				writeLine("|Willingness-to-pay (WTP)|"+myModel.dimInfo.WTP+"|");
			}
			writeLine("");
			//simulation
			writeLine("## Simulation");
			writeLine("|Property|Value|");
			writeLine("|--------|-----|");
			if(myModel.simType==0){ //Cohort
				writeLine("|Simulation type|Cohort (Deterministic)|");
				writeLine("|Cohort size|"+myModel.cohortSize+"|");
			}
			else if(myModel.simType==1){ //Monte Carlo
				writeLine("|Simulation type|Monte Carlo (Stochastic)|");
				writeLine("|Number of simulations|"+myModel.cohortSize+"|");
				if(myModel.CRN){
					writeLine("|RNG Seed|"+myModel.crnSeed+"|");
				}
			}
			if(myModel.type==1){ //Markov
				writeLine("");
				writeLine("## Markov");
				writeLine("|Property|Value|");
				writeLine("|--------|-----|");
				writeLine("|Max cycles|"+myModel.markov.maxCycles+"|");
				writeLine("|Half-cycle correction|"+myModel.markov.halfCycleCorrection+"|");
				writeLine("|Discount Rewards|"+myModel.markov.discountRewards+"|");
				if(myModel.markov.discountRewards){
					writeLine("|Discount start cycle|"+myModel.markov.discountStartCycle+"|");
					writeLine("|*Dimension*|*Discount Rate (%)*|");
					for(int d=0; d<myModel.dimInfo.dimNames.length; d++){
						out.write("|"+myModel.dimInfo.dimNames[d]);
						out.write("|"+myModel.markov.discountRates[d]+"|\n");
					}
				}
				
			}
						
			//model structure
			writeLine("");
			saveDiagram();
			writeLine("# Model Diagram");
			writeLine("![Model Diagram]("+dir+myModel.name+"_Diagram.png)");
			writeLine("\\clearpage");
			writeLine("");
			
			writeLine("# Model Inputs");
			
			writeParameters();
			
			writeVariables();
			
			writeTables();
			
			out.close();

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void saveDiagram() throws IOException{
		BufferedImage bi=new BufferedImage(myModel.getPanel().getWidth(), myModel.getPanel().getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		myModel.getPanel().print(g);

		String format="png";
		String outFile=dir+myModel.name+"_Diagram.png";
		ImageIO.write(bi, format, new File(outFile));
	}
	
	private void writeParameters() throws IOException{
		int numParams=myModel.parameters.size();
		if(numParams>0){
			writeLine("");
			writeLine("## Parameters");
			writeLine("");
			writeLine("|Parameter|Expression|Expected Value|Notes|");
			writeLine("|---------|----------|--------------|-----|");
			for(int i=0; i<numParams; i++){
				Parameter curParam=myModel.parameters.get(i);
				out.write("|"+curParam.name);
				out.write("|"+curParam.expression);
				out.write("|"+writeValue(curParam.value));
				String notes=curParam.notes;
				notes=notes.replace("\n", "; ");
				out.write("|"+notes+"|\n");
			}
		}
	}

	private String writeValue(Numeric curNum){
		if(!curNum.isMatrix()){
			return(curNum.toString());
		}
		else{ //matrix
			String val="";
			int nrow=curNum.nrow;
			int ncol=curNum.ncol;
			if(nrow==1){ //row vector
				val="["+curNum.matrix[0][0];
				for(int c=1; c<ncol; c++){val+=","+curNum.matrix[0][c];}
				val+="]";
			}
			else{
				val="[";
				for(int r=0; r<nrow; r++){
					val+="["+curNum.matrix[r][0];
					for(int c=1; c<ncol; c++){val+=","+curNum.matrix[r][c];}
					val+="]";
				}
				val+="]";
			}
			return(val);
		}
	}
	
	public void writeVariables() throws IOException{
		int numVars=myModel.variables.size();
		if(numVars>0){
			writeLine("");
			writeLine("## Variables");
			writeLine("|Variable|Initial Value|Notes|");
			writeLine("|--------|-------------|-----|");
			for(int i=0; i<numVars; i++){
				Variable curVar=myModel.variables.get(i);
				out.write("|"+curVar.name);
				out.write("|"+curVar.initValue);
				String notes=curVar.notes;
				notes=notes.replace("\n", "; ");
				out.write("|"+notes+"|\n");
			}
		}
	}
	
	public void writeTables() throws IOException{
		int numTables=myModel.tables.size();
		if(numTables>0){
			writeLine("");
			writeLine("## Tables");
			for(int i=0; i<myModel.tables.size(); i++){
				Table curTable=myModel.tables.get(i);
				writeLine("");
				writeLine("### "+curTable.name);
				writeLine("");
				writeLine("|Property|Value|");
				writeLine("|--------|-----|");
				writeLine("|Table Type|"+curTable.type+"|");
				if(curTable.type.matches("Lookup")){
					writeLine("|Lookup Method|"+curTable.lookupMethod+"|");
					if(curTable.lookupMethod.matches("Interpolate")){
						writeLine("|Interpolation Method|"+curTable.interpolate+"|");
						if(curTable.interpolate.matches("Cubic Splines")){
							writeLine("|Boundary Condition|"+curTable.boundary+"|");
						}
						writeLine("|Extrapolate|"+curTable.extrapolate+"|");
					}
				}
				if(!curTable.notes.isEmpty()){
					String notes=curTable.notes;
					notes=notes.replace("\n", "; ");
					writeLine("|Notes|"+notes+"|");
				}
				//table headers
				writeLine("");
				for(int c=0; c<curTable.numCols; c++){out.write("|"+curTable.headers[c]);}
				out.write("|\n");
				for(int c=0; c<curTable.numCols; c++){
					out.write("|");
					for(int z=0; z<curTable.headers.length; z++){out.write("-");}
				}
				out.write("|\n");
				//data
				for(int r=0; r<curTable.numRows; r++){
					for(int c=0; c<curTable.numCols; c++){out.write("|"+curTable.data[r][c]);}
					out.write("|\n");
				}
			}
		}
	}

	
	
	
	
	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			e.printStackTrace();
			myModel.errorLog.recordError(e);
		}
	}
}
