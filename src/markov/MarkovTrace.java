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

package markov;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import base.AmuaModel;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class MarkovTrace{
	public String traceName;
	public ArrayList<Integer> cycles;
	int numStates;
	public String stateNames[];
	public ArrayList<Double> prev[];
	String dimSymbols[];
	public String dimNames[];
	public int numDim;
	public boolean discounted;
	public ArrayList<Double> cycleRewards[], cycleRewardsDis[];
	public ArrayList<Double> cumRewards[], cumRewardsDis[];
	public int numVariables;
	public String varNames[];
	public ArrayList<Double> cycleVariables[];
	public DefaultTableModel modelTraceRounded;
	DefaultTableModel modelTraceRaw;
	AmuaModel myModel;
	
	//Constructor
	public MarkovTrace(MarkovNode chainRoot){
		traceName=chainRoot.name;
		myModel=chainRoot.myModel;
		cycles=new ArrayList<Integer>();
		numStates=chainRoot.stateNames.size();
		stateNames=new String[numStates];
		prev=new ArrayList[numStates];
		MarkovTree tree=chainRoot.tree;
		for(int s=0; s<numStates; s++){
			int index=chainRoot.childIndices.get(s);
			stateNames[s]=tree.nodes.get(index).name;
			prev[s]=new ArrayList<Double>();
		}
		numDim=chainRoot.numDimensions;
		dimSymbols=chainRoot.myModel.dimInfo.dimSymbols;
		dimNames=chainRoot.myModel.dimInfo.dimNames;
		cycleRewards=new ArrayList[numDim]; cycleRewardsDis=new ArrayList[numDim];
		cumRewards=new ArrayList[numDim]; cumRewardsDis=new ArrayList[numDim];
		discounted=myModel.markov.discountRewards;
		for(int d=0; d<numDim; d++){
			cycleRewards[d]=new ArrayList<Double>(); cycleRewardsDis[d]=new ArrayList<Double>();
			cumRewards[d]=new ArrayList<Double>(); cumRewardsDis[d]=new ArrayList<Double>();
		}
		numVariables=chainRoot.myModel.variables.size();
		varNames=new String[numVariables];
		cycleVariables=new ArrayList[numVariables];
		for(int c=0; c<numVariables; c++){
			varNames[c]=chainRoot.myModel.variables.get(c).name;
			cycleVariables[c]=new ArrayList<Double>();
		}
		//Build Model headers
		modelTraceRaw=new DefaultTableModel(); modelTraceRounded=new DefaultTableModel();
		modelTraceRaw.addColumn("Cycle"); modelTraceRounded.addColumn("Cycle");
		for(int s=0; s<numStates; s++){
			modelTraceRaw.addColumn(stateNames[s]);
			modelTraceRounded.addColumn(stateNames[s]);
		}
		//undiscounted
		for(int d=0; d<numDim; d++){
			modelTraceRaw.addColumn("Cycle_"+dimSymbols[d]);
			modelTraceRounded.addColumn("Cycle_"+dimSymbols[d]);
		}
		for(int d=0; d<numDim; d++){
			modelTraceRaw.addColumn("Cum_"+dimSymbols[d]);
			modelTraceRounded.addColumn("Cum_"+dimSymbols[d]);
		}
		//discounted
		if(discounted==true){
			for(int d=0; d<numDim; d++){
				modelTraceRaw.addColumn("Cycle_Dis_"+dimSymbols[d]);
				modelTraceRounded.addColumn("Cycle_Dis_"+dimSymbols[d]);
			}
			for(int d=0; d<numDim; d++){
				modelTraceRaw.addColumn("Cum_Dis_"+dimSymbols[d]);
				modelTraceRounded.addColumn("Cum_Dis_"+dimSymbols[d]);
			}
		}
		//variables
		for(int c=0; c<numVariables; c++){
			modelTraceRaw.addColumn(chainRoot.myModel.variables.get(c).name);
			modelTraceRounded.addColumn(chainRoot.myModel.variables.get(c).name);
		}
	}
	
	public void updateTable(int t){
		modelTraceRaw.addRow(new Object[]{null}); modelTraceRounded.addRow(new Object[]{null});
		int curCol=0;
		modelTraceRaw.setValueAt(cycles.get(t), t, curCol); modelTraceRounded.setValueAt(cycles.get(t), t, curCol); //cycle
		curCol++;
		//state prevalence
		for(int s=0; s<numStates; s++){
			modelTraceRaw.setValueAt(prev[s].get(t), t, curCol);
			double roundedPrev=MathUtils.round(prev[s].get(t),myModel.markov.stateDecimals);
			modelTraceRounded.setValueAt(roundedPrev, t, curCol);
			curCol++;
		}
		//undiscounted
		for(int d=0; d<numDim; d++){
			modelTraceRaw.setValueAt(cycleRewards[d].get(t),t,curCol);
			modelTraceRounded.setValueAt(MathUtils.round(cycleRewards[d].get(t),myModel.dimInfo.decimals[d]),t,curCol);
			curCol++;
		}
		for(int d=0; d<numDim; d++){
			modelTraceRaw.setValueAt(cumRewards[d].get(t),t,curCol);
			modelTraceRounded.setValueAt(MathUtils.round(cumRewards[d].get(t),myModel.dimInfo.decimals[d]),t,curCol);
			curCol++;
		}
		//discounted
		if(discounted==true){
			for(int d=0; d<numDim; d++){
				modelTraceRaw.setValueAt(cycleRewardsDis[d].get(t),t,curCol);
				modelTraceRounded.setValueAt(MathUtils.round(cycleRewardsDis[d].get(t),myModel.dimInfo.decimals[d]),t,curCol);
				curCol++;
			}
			for(int d=0; d<numDim; d++){
				modelTraceRaw.setValueAt(cumRewardsDis[d].get(t),t,curCol);
				modelTraceRounded.setValueAt(MathUtils.round(cumRewardsDis[d].get(t),myModel.dimInfo.decimals[d]),t,curCol);
				curCol++;
			}
		}
		//variables
		for(int c=0; c<numVariables; c++){
			modelTraceRaw.setValueAt(cycleVariables[c].get(t), t, curCol);
			modelTraceRounded.setValueAt(cycleVariables[c].get(t), t, curCol);
			curCol++;
		}
	}	
	
	public void updateHalfCycle(){
		int row=modelTraceRaw.getRowCount()-1; //get last row
		//update cycle rewards and cum rewards
		int curCol=numStates+1;
		
		//undiscounted
		for(int d=0; d<numDim; d++){
			double halfReward=cycleRewards[d].get(row)*0.5;
			cycleRewards[d].set(row, halfReward); //update with half-cycle reward
			modelTraceRaw.setValueAt(halfReward,row,curCol);
			modelTraceRounded.setValueAt(MathUtils.round(halfReward,myModel.dimInfo.decimals[d]),row,curCol);
			curCol++;
		}
		for(int d=0; d<numDim; d++){
			double rewardPrev=cumRewards[d].get(row-1);
			double halfReward=cycleRewards[d].get(row);
			cumRewards[d].set(row, rewardPrev+halfReward); //update with half-cycle reward
			modelTraceRaw.setValueAt(rewardPrev+halfReward,row,curCol);
			modelTraceRounded.setValueAt(MathUtils.round(rewardPrev+halfReward,myModel.dimInfo.decimals[d]),row,curCol);
			curCol++;
		}
		//discounted
		if(discounted==true){
			for(int d=0; d<numDim; d++){
				double halfReward=cycleRewardsDis[d].get(row)*0.5;
				cycleRewardsDis[d].set(row, halfReward); //update with half-cycle reward
				modelTraceRaw.setValueAt(halfReward,row,curCol);
				modelTraceRounded.setValueAt(MathUtils.round(halfReward,myModel.dimInfo.decimals[d]),row,curCol);
				curCol++;
			}
			for(int d=0; d<numDim; d++){
				double rewardPrev=cumRewardsDis[d].get(row-1);
				double halfReward=cycleRewardsDis[d].get(row);
				cumRewardsDis[d].set(row, rewardPrev+halfReward); //update with half-cycle reward
				modelTraceRaw.setValueAt(rewardPrev+halfReward,row,curCol);
				modelTraceRounded.setValueAt(MathUtils.round(rewardPrev+halfReward,myModel.dimInfo.decimals[d]),row,curCol);
				curCol++;
			}
		}
		
	}	
	
	public void setT0(MarkovNode chainRoot){
		cycles.add(0);
		//Update prev
		int cohortSize=myModel.cohortSize;
		for(int s=0; s<numStates; s++){
			int index=chainRoot.childIndices.get(s);
			double initProb=chainRoot.tree.nodes.get(index).curProb;
			double curPrev=cohortSize*initProb;
			prev[s].add(curPrev);
		}
		//Update rewards
		for(int d=0; d<numDim; d++){
			cycleRewards[d].add(0.0); cycleRewardsDis[d].add(0.0);
			cumRewards[d].add(0.0); cumRewardsDis[d].add(0.0);
		}
		//Update variables
		for(int c=0; c<numVariables; c++){
			cycleVariables[c].add(0.0);
		}
		updateTable(0);
	}
	
	/*public double getValue(int t, String colText){
		//Get column index
		int col=getColumnIndex(colText);
		if(col<1 || col>(modelTraceRaw.getColumnCount()-1)){return(Double.NaN);} //Throw error
		else{ //Valid column
			double val=(double) modelTraceRaw.getValueAt(t, col);
			return(val);
		}
	}*/
	
	public Numeric getValue(String row, String col) throws NumericException, Exception{
		double traceVals[][];
		int numRows=modelTraceRaw.getRowCount();
		int numCols=modelTraceRaw.getColumnCount();
		int startRow, endRow;
		int startCol, endCol;
		if(row.contains(":")){ //Multiple rows
			if(row.length()==1){ //":" all rows
				startRow=0; endRow=numRows-1;
			}
			else{ //"x:y" Sequence of rows
				int index=row.indexOf(":");
				startRow=Interpreter.evaluate(row.substring(0, index),myModel,false).getInt();
				endRow=Interpreter.evaluate(row.substring(index+1),myModel,false).getInt();
			}
		}
		else{
			startRow=Interpreter.evaluate(row,myModel,false).getInt();
			endRow=startRow;
		}
		
		if(col.contains(":")){ //Multiple cols
			if(col.length()==1){ //":" all cols
				startCol=0; endCol=numCols-1;
			}
			else{ //"x:y" Sequence of cols
				int index=col.indexOf(":");
				startCol=Interpreter.evaluate(col.substring(0, index),myModel,false).getInt();
				endCol=Interpreter.evaluate(col.substring(index+1),myModel,false).getInt();
			}
		}
		else{
			startCol=getColumnIndex(col);
			endCol=startCol;
		}
		
		traceVals=new double[endRow-startRow+1][endCol-startCol+1];
		for(int i=startRow; i<=endRow; i++){
			for(int j=startCol; j<=endCol; j++){
				traceVals[i-startRow][j-startCol]=(double) modelTraceRaw.getValueAt(i, j);
			}
		}
		
		return(new Numeric(traceVals));
	}
	
	private int getColumnIndex(String colText) throws NumericException{
		int col=-1;
		if(colText.contains("\"") || colText.contains("\'")){ //String
			//Trim quotes
			colText=colText.replace("\"","");
			colText=colText.replace("\'","");
			boolean found=false;
			while(found==false && col<modelTraceRaw.getColumnCount()){
				col++;
				if(colText.equals(modelTraceRaw.getColumnName(col))){found=true;}
			}
			if(found==false){
				throw new NumericException("Can't find column: "+colText,"trace");
			}
		}
		else{ //Try evaluate as integer
			try{
				col=Interpreter.evaluate(colText,myModel,false).getInt();
			}
			catch(Exception  e){
				col=-1;
				throw new NumericException("Invalid column index: "+colText,"trace");
			}
		}
		return(col);
	}

}