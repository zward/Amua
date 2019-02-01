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
//Utility class
package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import base.AmuaModel;

public class CEAHelper{
	int numStrat;
	double costs[], effects[];

	public CEAHelper(){ //Constructor
		
	}
	
	//ErrorLog errorLog;
	public Object[][] calculateICERs(AmuaModel myModel){
		getResults(myModel); //Get EVs
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
		Object table[][]=new Object[numStrat][6];
		for(int s=0; s<numStrat; s++){  //add other strategies in order by cost
			int curStrat=(int) costTable[s][0];
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
		//Check for cost saving
		double baseCost=costs[baseline], baseEffect=effects[baseline];
		boolean anyCostSaving=false;
		for(int s=0; s<numStrat; s++){
			if(s!=baseline){
				double cost1=(double) table[s][2]; double effect1=(double) table[s][3];
				if(cost1<baseCost && effect1>=baseEffect){
					table[s+1][4]=Double.NaN;
					table[s+1][5]="Cost Saving";
					table[baseline][4]=Double.NaN;
					anyCostSaving=true;
				}
			}
		}
		if(anyCostSaving==true){
			viable.remove(baseline);
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
				double incEffect=(double)table[index1][3]-(double)table[index0][3];
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
		//table[baseline][5]="Baseline";

		for(int s=0; s<numStrat; s++){
			int curStrat=(int) costTable[s][0];
			if(curStrat==baseline){
				//table[s][4]=Double.NaN;
				String curNote=(String) table[s][5];
				if(curNote==null || curNote.isEmpty()){table[s][5]="Baseline";}
				else{table[s][5]+=" (Baseline)";}
			}
		}

		return(table);
	}

	private void getResults(AmuaModel myModel){
		//Get EVs
		numStrat=myModel.getStrategies();
		costs=new double[numStrat];
		effects=new double[numStrat];
		for(int s=0; s<numStrat; s++){
			costs[s]=myModel.getStrategyEV(s, myModel.dimInfo.costDim);
			effects[s]=myModel.getStrategyEV(s, myModel.dimInfo.effectDim);
		}
	}


	public Object[][] calculateNMB(AmuaModel myModel){
		getResults(myModel); //Get EVs
		Object table[][]=new Object[numStrat][6];
		
		//Get EVs
		for(int s=0; s<numStrat; s++){
			table[s][0]=s; //Row
			table[s][1]=myModel.strategyNames[s]; //Name
			double cost, benefit;
			cost=costs[s];
			benefit=effects[s];
			table[s][2]=benefit;
			table[s][3]=cost;
			table[s][4]=(benefit*myModel.dimInfo.WTP)-cost; //NMB
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