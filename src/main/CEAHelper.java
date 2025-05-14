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
//Utility class
package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import base.AmuaModel;

public class CEAHelper{
	public int numStrat;
	public double costs[], effects[];
	public double extendedDim[];

	public CEAHelper(){ //Constructor
		
	}
	
	/**
	 * 
	 * @param myModel
	 * @param group -1: Overall, 0-n: Subgroup index
	 * @param getResults: If true gets EVs from model, if false uses global variables in this class
	 * @return
	 */
	public Object[][] calculateICERs(AmuaModel myModel, int group, boolean getResults){
		if(getResults) {
			getResults(myModel,group); //Get EVs
		}
		
		int baseline=-1;
		int strat=0;
		while(baseline==-1 && strat<numStrat){
			if(myModel.strategyNames[strat].equals(myModel.dimInfo.baseScenario)){
				baseline=strat;
			}
			strat++;
		}
		
		//Sort by costs
		Object costTable[][]=new Object[numStrat][2];
		for(int s=0; s<numStrat; s++){
			costTable[s][0]=s; //index
			costTable[s][1]=costs[s];
		}
		Arrays.sort(costTable, new Comparator<Object[]>(){
			@Override
			public int compare(final Object[] row1, final Object[] row2){
				Double cost1 = (Double) row1[1];
				Double cost2 = (Double) row2[1];
				return cost1.compareTo(cost2);
			}
		});
		
		//Make table
		int stratIndices[]=new int[numStrat];
		Object table[][]=new Object[numStrat][6];
		for(int s=0; s<numStrat; s++){  //add other strategies in order by cost
			int curStrat=(int) costTable[s][0];
			stratIndices[curStrat]=s;
			table[s][0]=curStrat;
			table[s][1]=myModel.strategyNames[curStrat];
			table[s][2]=costs[curStrat];
			table[s][3]=effects[curStrat];
			table[s][5]="";
		}
		
		//Calc ICERs
		ArrayList<Integer> viable=new ArrayList<Integer>();
		for(int s=0; s<numStrat; s++){viable.add(s);}

		//CEA algorithm
		int objSign=1;
		if(myModel.dimInfo.objective==1) { //minimize
			objSign=-1;
		}
		//Check for cost saving
		double baseCost=costs[baseline], baseEffect=effects[baseline]*objSign;
		boolean anyCostSaving=false;
		for(int s=0; s<numStrat; s++){
			if((int)table[s][0]!=baseline) {
				double cost1=(double) table[s][2]; double effect1=(double) table[s][3]*objSign;
				if(cost1<baseCost && effect1>=baseEffect){
					table[s][4]=Double.NaN;
					table[s][5]="Cost Saving";
					anyCostSaving=true;
				}
			}
		}
		if(anyCostSaving==true){
			int baseIndex=stratIndices[baseline];
			viable.remove(baseIndex);
			table[baseIndex][4]=Double.NaN;
			table[baseIndex][5]="Strongly Dominated";
		}
				
		//CEA algorithm
		boolean repeat=true;
		while(repeat==true){
			repeat=false;
			int numViable=viable.size();
			double curMaxICER=0;
			int v=1;
			while(v<numViable && repeat==false){
				int index0=viable.get(v-1);
				int index1=viable.get(v);
				double incCost=(double)table[index1][2]-(double)table[index0][2];
				double incEffect=((double)table[index1][3]*objSign)-((double)table[index0][3]*objSign);
				if(incEffect<0){ //smaller effect
					repeat=true;
					table[index1][4]=Double.NaN;
					table[index1][5]="Strongly Dominated";
					viable.remove(v);
				}
				else if(incEffect==0){
					if(incCost>0){ //Same effect, greater cost
						repeat=true;
						table[index1][4]=Double.NaN;
						table[index1][5]="Strongly Dominated";
						viable.remove(v);
					}
					else if(incCost==0){ //Same effect, same cost - set to previous ICER
						table[index1][4]=table[index0][4];
						table[index1][5]="Indifferent";
					}
				}
				else{ //Increasing effect: Calculate ICER
					if(incCost==0){ //Greater effect, same cost
						repeat=true;
						table[index0][4]=Double.NaN;
						table[index0][5]="Strongly Dominated";
						table[index1][4]=0.0; //set dominating ICER to 0 for now
						viable.remove(v-1);
					}
					else{
						double icer=incCost/incEffect;
						if(icer<curMaxICER){ //Non-increasing
							repeat=true;
							table[index0][4]=Double.NaN;
							table[index0][5]="Weakly Dominated";
							viable.remove(v-1);
						}
						else{
							curMaxICER=icer;
							table[index1][4]=icer;
						}
					}
				}
				v++;
			}
		}
		//Set first ICER to NAN
		table[0][4]=Double.NaN;
		
		for(int s=0; s<numStrat; s++){
			int curStrat=(int) costTable[s][0];
			if(curStrat==baseline){
				String curNote=(String) table[s][5];
				if(curNote==null || curNote.isEmpty()){table[s][5]="Baseline";}
				else{table[s][5]+=" (Baseline)";}
			}
			if(table[s][4]==null) {table[s][4]=Double.NaN;} //replace nulls with NaNs
		}

		return(table);
	}

	private void getResults(AmuaModel myModel, int group){
		//Get EVs
		numStrat=myModel.getStrategies();
		costs=new double[numStrat];
		effects=new double[numStrat];
		boolean ECEA=false;
		if(myModel.dimInfo.analysisType==3){
			ECEA=true;
			extendedDim=new double[numStrat];
		}
		
		if(group==-1){ //overall
			for(int s=0; s<numStrat; s++){
				costs[s]=myModel.getStrategyEV(s, myModel.dimInfo.costDim);
				effects[s]=myModel.getStrategyEV(s, myModel.dimInfo.effectDim);
				if(ECEA){extendedDim[s]=myModel.getStrategyEV(s, myModel.dimInfo.extendedDim);}
			}
		}
		else{ //subgroup
			for(int s=0; s<numStrat; s++){
				costs[s]=myModel.getSubgroupEV(group, s, myModel.dimInfo.costDim);
				effects[s]=myModel.getSubgroupEV(group, s, myModel.dimInfo.effectDim);
				if(ECEA){extendedDim[s]=myModel.getSubgroupEV(group, s, myModel.dimInfo.extendedDim);}
			}
		}
	}


	public Object[][] calculateNMB(AmuaModel myModel, int group, boolean getResults){
		if(getResults) {
			getResults(myModel,group); //Get EVs
		}
		
		Object table[][]=new Object[numStrat][6];
		
		int objSign=1;
		if(myModel.dimInfo.objective==1) { //minimize
			objSign=-1;
		}
		
		//Get EVs
		for(int s=0; s<numStrat; s++){
			table[s][0]=s; //Row
			table[s][1]=myModel.strategyNames[s]; //Name
			double cost, benefit;
			cost=costs[s];
			benefit=effects[s];
			table[s][2]=benefit;
			table[s][3]=cost;
			table[s][4]=(benefit*objSign*myModel.dimInfo.WTP)-cost; //NMB
		}

		//Sort by NMB
		Arrays.sort(table, new Comparator<Object[]>() {
			@Override
			public int compare(final Object[] row1, final Object[] row2) {
				Double cost1 = (Double) row1[4];
				Double cost2 = (Double) row2[4];
				return cost1.compareTo(cost2);
			}
		});

		return(table);
	}

	public Object[][] calculateECEA(AmuaModel myModel, int group, boolean getResults){
		if(getResults) {
			getResults(myModel,group); //Get EVs
		}
		
		Object table[][]=new Object[numStrat][7];
		
		int objSign=1;
		if(myModel.dimInfo.objective==1) { //minimize
			objSign=-1;
		}
		
		//Get EVs
		for(int s=0; s<numStrat; s++){
			table[s][0]=s; //Row
			table[s][1]=myModel.strategyNames[s]; //Name
			double cost, benefit;
			cost=costs[s];
			benefit=effects[s];
			table[s][2]=benefit;
			table[s][3]=cost;
			table[s][4]=(benefit*objSign*myModel.dimInfo.WTP)-cost; //NMB
			table[s][5]=extendedDim[s]; //Extended dimension
		}

		//Sort by NMB
		Arrays.sort(table, new Comparator<Object[]>() {
			@Override
			public int compare(final Object[] row1, final Object[] row2) {
				Double cost1 = (Double) row1[4];
				Double cost2 = (Double) row2[4];
				return cost1.compareTo(cost2);
			}
		});

		return(table);
	}

	
}