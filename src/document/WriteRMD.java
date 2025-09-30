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
import javax.swing.JOptionPane;

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
			
			writeLine(myModel.language.base.getString("title.this_model_amua")+" (https://github.com/zward/Amua)."); //This model was developed in Amua (https://github.com/zward/Amua)
			writeLine("");
			writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
			writeLine("|--------|-----|");
			writeLine("|"+myModel.language.base.getString("meta.model_name")+"|"+myModel.name+"|"); //Model name
			if(myModel.type==0){writeLine("|"+myModel.language.base.getString("meta.model_type")+"|"+myModel.language.base.getString("menu.decision_tree")+"|");} //Model type, Decision Tree
			else if(myModel.type==1){writeLine("|"+myModel.language.base.getString("meta.model_type")+"|"+myModel.language.base.getString("menu.markov_model")+"|");} //Model type, Markov Model
			if(myModel.simType==0){writeLine("|"+myModel.language.analysis.getString("sim.simulation_type")+"|"+myModel.language.analysis.getString("sim.cohort")+"|");} //Simulation type, Cohort
			else if(myModel.simType==1){writeLine("|"+myModel.language.analysis.getString("sim.simulation_type")+"|"+myModel.language.analysis.getString("sim.monte_carlo")+"|");} //Simulation type, Monte Carlo
			writeLine("");
			
			writeLine("# "+myModel.language.base.getString("title.model_properties")); //Model Properties
			writeLine("");
			//metadata
			writeLine("## "+myModel.language.base.getString("meta.metadata")); //Metadata
			writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
			writeLine("|--------|-----|");
			writeLine("|"+myModel.language.base.getString("meta.created_by")+"|"+myModel.meta.author+"|"); //Created by
			writeLine("|"+myModel.language.base.getString("meta.created")+"|"+myModel.meta.dateCreated+"|"); //Created
			writeLine("|"+myModel.language.base.getString("meta.version_created")+"|"+myModel.meta.versionCreated+"|"); //Version created
			writeLine("|"+myModel.language.base.getString("meta.modified_by")+"|"+myModel.meta.modifier+"|"); //Modified by
			writeLine("|"+myModel.language.base.getString("meta.modified")+"|"+myModel.meta.dateModified+"|"); //Modified
			writeLine("|"+myModel.language.base.getString("meta.version_modified")+"|"+myModel.meta.versionModified+"|"); //Version modified
			writeLine("");
			//analysis
			writeLine("## "+myModel.language.analysis.getString("gen.analysis")); //Analysis
			writeLine("|"+myModel.language.analysis.getString("gen.dimension")+"|"+myModel.language.analysis.getString("gen.symbol")+"|"+myModel.language.analysis.getString("gen.decimals")+"|"); //Dimension|Symbol|Decimals
			writeLine("|---------|------|--------|");
			for(int d=0; d<myModel.dimInfo.dimNames.length; d++){
				out.write("|"+myModel.dimInfo.dimNames[d]);
				out.write("|"+myModel.dimInfo.dimSymbols[d]);
				out.write("|"+myModel.dimInfo.decimals[d]+"|\n");
			}
			writeLine("");
			writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
			writeLine("|--------|-----|");
			if(myModel.dimInfo.analysisType==0){ //EV
				writeLine("|"+myModel.language.analysis.getString("gen.analysis_type")+"|"+myModel.language.analysis.getString("gen.expected_value")+"|"); //Analysis Type|Expected Value (EV)
				if(myModel.dimInfo.objective==0){writeLine("|"+myModel.language.analysis.getString("gen.objective")+"|"+myModel.language.analysis.getString("gen.maximize")+"|");} //Objective|Maximize
				else if(myModel.dimInfo.objective==1){writeLine("|"+myModel.language.analysis.getString("gen.objective")+"|"+myModel.language.analysis.getString("gen.minimize")+"|");} //Objective|Minimize
				writeLine("|"+myModel.language.analysis.getString("result.outcome")+"|"+myModel.dimInfo.dimNames[myModel.dimInfo.objectiveDim]+"|"); //Outcome
			}
			else if(myModel.dimInfo.analysisType==1){ //CEA
				writeLine("|"+myModel.language.analysis.getString("gen.analysis_type")+"|"+myModel.language.analysis.getString("cea.cost_effectiveness_analysis")+"|"); //|Analysis Type|Cost-Effectiveness Analysis (CEA)|
				writeLine("|"+myModel.language.analysis.getString("cea.cost")+"|"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]+"|"); //Cost
				writeLine("|"+myModel.language.analysis.getString("cea.effect")+"|"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]+"|"); //Effect
				myModel.getStrategies();
				writeLine("|"+myModel.language.analysis.getString("cea.baseline_strategy")+"|"+myModel.dimInfo.baseScenario+"|"); //Baseline Strategy
				writeLine("|"+myModel.language.analysis.getString("cea.wtp")+"|"+myModel.dimInfo.WTP+"|"); //Willingness-to-pay (WTP)
			}
			else if(myModel.dimInfo.analysisType==2){ //BCA
				writeLine("|"+myModel.language.analysis.getString("gen.analysis_type")+"|"+myModel.language.analysis.getString("bca.benefit_cost_analysis")+"|"); //|Analysis Type|Benefit-Cost Analysis (BCA)|
				writeLine("|"+myModel.language.analysis.getString("cea.cost")+"|"+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]+"|"); //Cost
				writeLine("|"+myModel.language.analysis.getString("bca.benefit")+"|"+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]+"|"); //Benefit
				writeLine("|"+myModel.language.analysis.getString("cea.wtp")+"|"+myModel.dimInfo.WTP+"|"); //Willingness-to-pay (WTP)
			}
			writeLine("");
			//simulation
			writeLine("## "+myModel.language.analysis.getString("sim.simulation")); //Simulation
			writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
			writeLine("|--------|-----|");
			if(myModel.simType==0){ //Cohort
				writeLine("|"+myModel.language.analysis.getString("sim.simulation_type")+"|"+myModel.language.analysis.getString("sim.cohort_deterministic")+"|"); //Simulation type|Cohort (Deterministic)
				writeLine("|"+myModel.language.analysis.getString("sim.cohort_size")+"|"+myModel.cohortSize+"|"); //Cohort size
			}
			else if(myModel.simType==1){ //Monte Carlo
				writeLine("|"+myModel.language.analysis.getString("sim.simulation_type")+"|"+myModel.language.analysis.getString("sim.monte_carlo_stochastic")+"|"); //Simulation type|Monte Carlo (Stochastic)
				writeLine("|"+myModel.language.analysis.getString("sim.number_of_simulations")+"|"+myModel.cohortSize+"|"); //Number of simulations
				if(myModel.CRN){
					writeLine("|"+myModel.language.analysis.getString("sim.rng_seed")+"|"+myModel.crnSeed+"|"); //RNG Seed
				}
			}
			if(myModel.type==1){ //Markov
				writeLine("");
				writeLine("## "+myModel.language.base.getString("markov.markov")); //Markov
				writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
				writeLine("|--------|-----|");
				writeLine("|"+myModel.language.analysis.getString("sim.max_cycles")+"|"+myModel.markov.maxCycles+"|"); //Max cycles
				writeLine("|"+myModel.language.analysis.getString("gen.half_cycle_correction")+"|"+myModel.markov.halfCycleCorrection+"|"); //Half-cycle correction
				writeLine("|"+myModel.language.analysis.getString("gen.discount_rewards")+"|"+myModel.markov.discountRewards+"|"); //Discount Rewards
				if(myModel.markov.discountRewards){
					writeLine("|"+myModel.language.analysis.getString("gen.discount_start_cycle")+"|"+myModel.markov.discountStartCycle+"|"); //Discounted start cycle
					writeLine("|*"+myModel.language.analysis.getString("gen.dimension")+"*|*"+myModel.language.analysis.getString("gen.discount_rate")+"*|"); //Dimension, Discount rate (%)
					for(int d=0; d<myModel.dimInfo.dimNames.length; d++){
						out.write("|"+myModel.dimInfo.dimNames[d]);
						out.write("|"+myModel.markov.discountRates[d]+"|\n");
					}
				}
				
			}
						
			//model structure
			writeLine("");
			saveDiagram();
			writeLine("# "+myModel.language.base.getString("title.model_diagram")); //Model Diagram
			writeLine("![Model Diagram]("+dir+myModel.name+"_Diagram.png)");
			writeLine("\\clearpage");
			writeLine("");
			
			writeLine("# "+myModel.language.base.getString("title.model_inputs")); //Model Inputs
			
			writeParameters();
			
			writeVariables();
			
			writeTables();
			
			out.close();

		}catch(Exception e){
			recordError(e);
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
			writeLine("## "+myModel.language.base.getString("object.parameters")); //Parameters
			writeLine("");
			writeLine("|"+myModel.language.base.getString("object.parameter")+"|"+myModel.language.base.getString("object.expression")+"|"+myModel.language.base.getString("object.expected_value")+"|"+myModel.language.base.getString("menu.notes")+"|"); //Parameter|Expression|Expected Value|Notes
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
			writeLine("## "+myModel.language.base.getString("object.variables")); //Variables
			writeLine("|"+myModel.language.base.getString("object.variable")+"|"+myModel.language.base.getString("object.initial_value")+"|"+myModel.language.base.getString("menu.notes")+"|"); //Variable|Initial Value|Notes
			writeLine("|--------|-------------|-----|");
			for(int i=0; i<numVars; i++){
				Variable curVar=myModel.variables.get(i);
				out.write("|"+curVar.name);
				out.write("|"+curVar.expression);
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
			writeLine("## "+myModel.language.base.getString("object.tables")); //Tables
			for(int i=0; i<myModel.tables.size(); i++){
				Table curTable=myModel.tables.get(i);
				writeLine("");
				writeLine("### "+curTable.name);
				writeLine("");
				writeLine("|"+myModel.language.base.getString("title.property")+"|"+myModel.language.analysis.getString("result.value")+"|"); //Property, Value
				writeLine("|--------|-----|");
				writeLine("|"+myModel.language.base.getString("table.table_type")+"|"+curTable.type+"|"); //Table Type
				if(curTable.type.matches("Lookup")){
					writeLine("|"+myModel.language.base.getString("table.lookup_method")+"|"+curTable.lookupMethod+"|"); //Lookup Method
					if(curTable.lookupMethod.matches("Interpolate")){
						writeLine("|"+myModel.language.base.getString("table.interpolation_method")+"|"+curTable.interpolate+"|"); //Interpolation Method
						if(curTable.interpolate.matches("Cubic Splines")){
							writeLine("|"+myModel.language.base.getString("table.boundary_condition")+"|"+curTable.boundary+"|"); //Boundary Condition
						}
						writeLine("|"+myModel.language.base.getString("table.extrapolate")+"|"+curTable.extrapolate+"|"); //Extrapolate
					}
				}
				if(!curTable.notes.isEmpty()){
					String notes=curTable.notes;
					notes=notes.replace("\n", "; ");
					writeLine("|"+myModel.language.base.getString("menu.notes")+"|"+notes+"|"); //Notes
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
			recordError(e);
		}
	}
	
	private void recordError(Exception e){
		e.printStackTrace();
		errorLog.recordError(e);
		JOptionPane.showMessageDialog(null, e.toString());
	}
}
