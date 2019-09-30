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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import main.CEAHelper;
import main.Console;
import main.ConsoleTable;
import main.DimInfo;
import markov.MarkovTrace;
import math.MathUtils;
import tree.TreeReport;

public class RunReport{
	AmuaModel myModel;
	public DimInfo dimInfo;
	
	/**0=Decision Tree, 1=Markov Model*/
	int type;
	/**0=Cohort, 1=Monte Carlo**/
	int simType;

	//metadata
	int numThreads;
	boolean rngSeeded;
	int rngSeed;
	String timestamp;
	public long runTime;
		
	int numDim;
	public int numStrat;
	public int numSubgroups=0;
	public String subgroupNames[];
	public int subgroupSizes[];

	/**
	 * [Dimension][Strategy]
	 */
	public double outcomeEVs[][];
	public double outcomeEVsGroup[][][];
	
	/**
	 * CEA/BCA results
	 */
	public Object table[][];
	public Object tableGroup[][][];

	public TreeReport treeReport;
	public TreeReport treeReportGroup[];
	public ArrayList<MarkovTrace> markovTraces;
	public ArrayList<MarkovTrace> markovTracesGroup[];
	public ArrayList<MicroStats> microStats;
	public ArrayList<MicroStats> microStatsGroup[];
	
	/**
	 * Strategy/Markov chain names
	 */
	public ArrayList<String> names;
	
	
	//Constructor
	public RunReport(AmuaModel myModel){
		this.myModel=myModel;
		this.dimInfo=myModel.dimInfo;
		this.type=myModel.type;
		
		//metadata
		numThreads=myModel.numThreads;
		rngSeeded=myModel.CRN;
		rngSeed=myModel.crnSeed;
		timestamp=new Date().toString();
		
		names=new ArrayList<String>();
		if(type==1){ //Markov
			markovTraces=new ArrayList<MarkovTrace>();
		}
		this.simType=myModel.simType;
		if(simType==1){ //Monte Carlo
			microStats=new ArrayList<MicroStats>();
		}
		//subgroups
		if(simType==1 && myModel.reportSubgroups){
			numSubgroups=myModel.subgroupNames.size();
			subgroupNames=new String[numSubgroups];
			subgroupSizes=new int[numSubgroups];
			microStatsGroup=new ArrayList[numSubgroups];
			if(type==1){markovTracesGroup=new ArrayList[numSubgroups];}
			for(int i=0; i<numSubgroups; i++){
				subgroupNames[i]=myModel.subgroupNames.get(i);
				microStatsGroup[i]=new ArrayList<MicroStats>();
				if(type==1){markovTracesGroup[i]=new ArrayList<MarkovTrace>();}
			}
			
		}
	}
	
	/**
	 * Gets expected values and runs CEA/BCA
	 */
	public void getResults(boolean allStrategies){
		numDim=dimInfo.dimNames.length;
		numStrat=myModel.getStrategies();
		
		//EVs
		outcomeEVs=new double[numDim][numStrat];
		for(int d=0; d<numDim; d++){
			for(int s=0; s<numStrat; s++){
				outcomeEVs[d][s]=myModel.getStrategyEV(s, d);
			}
		}
		if(numSubgroups>0){
			outcomeEVsGroup=new double[numSubgroups][numDim][numStrat];
			for(int g=0; g<numSubgroups; g++){
				for(int d=0; d<numDim; d++){
					for(int s=0; s<numStrat; s++){
						outcomeEVsGroup[g][d][s]=myModel.getSubgroupEV(g, s, d);
					}
				}
			}
		}

		if(allStrategies==true){
			if(dimInfo.analysisType==1){ //CEA
				table=new CEAHelper().calculateICERs(myModel,-1,true);

				//Round results
				numStrat=table.length;
				for(int s=0; s<numStrat; s++){
					table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.costDim]); //Cost
					table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.effectDim]); //Effect
					double icer=(double) table[s][4];
					if(Double.isNaN(icer)){
						table[s][4]="---";
					}
					else{
						table[s][4]=MathUtils.round(icer,dimInfo.decimals[dimInfo.costDim]);
					}
				}
				
				if(numSubgroups>0){
					tableGroup=new Object[numSubgroups][][];
					for(int g=0; g<numSubgroups; g++){
						tableGroup[g]=new CEAHelper().calculateICERs(myModel, g, true);
						//Round results
						for(int s=0; s<numStrat; s++){
							tableGroup[g][s][2]=MathUtils.round((double)tableGroup[g][s][2],dimInfo.decimals[dimInfo.costDim]); //Cost
							tableGroup[g][s][3]=MathUtils.round((double)tableGroup[g][s][3],dimInfo.decimals[dimInfo.effectDim]); //Effect
							double icer=(double) tableGroup[g][s][4];
							if(Double.isNaN(icer)){tableGroup[g][s][4]="---";}
							else{tableGroup[g][s][4]=MathUtils.round(icer,dimInfo.decimals[dimInfo.costDim]);}
						}
					}
				}
				
			}
			else if(dimInfo.analysisType==2){ //BCA
				table=new CEAHelper().calculateNMB(myModel,-1,true);

				//Round results
				numStrat=table.length;
				for(int s=0; s<numStrat; s++){
					table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.effectDim]); //Benefit
					table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.costDim]); //Cost
					table[s][4]=MathUtils.round((double)table[s][4],dimInfo.decimals[dimInfo.costDim]); //NMB
				}
				
				if(numSubgroups>0){
					tableGroup=new Object[numSubgroups][][];
					for(int g=0; g<numSubgroups; g++){
						tableGroup[g]=new CEAHelper().calculateNMB(myModel,g,true);
						//Round results
						for(int s=0; s<numStrat; s++){
							tableGroup[g][s][2]=MathUtils.round((double)tableGroup[g][s][2],dimInfo.decimals[dimInfo.effectDim]); //Benefit
							tableGroup[g][s][3]=MathUtils.round((double)tableGroup[g][s][3],dimInfo.decimals[dimInfo.costDim]); //Cost
							tableGroup[g][s][4]=MathUtils.round((double)tableGroup[g][s][4],dimInfo.decimals[dimInfo.costDim]); //NMB
						}
					}
				}
			}
			else if(dimInfo.analysisType==3){ //ECEA
				table=new CEAHelper().calculateECEA(myModel,-1,true);

				//Round results
				numStrat=table.length;
				for(int s=0; s<numStrat; s++){
					table[s][2]=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.effectDim]); //Benefit
					table[s][3]=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.costDim]); //Cost
					table[s][4]=MathUtils.round((double)table[s][4],dimInfo.decimals[dimInfo.costDim]); //NMB
					table[s][5]=MathUtils.round((double)table[s][5],dimInfo.decimals[dimInfo.extendedDim]); //Extended dim
				}
				
				if(numSubgroups>0){
					tableGroup=new Object[numSubgroups][][];
					for(int g=0; g<numSubgroups; g++){
						tableGroup[g]=new CEAHelper().calculateNMB(myModel,g,true);
						//Round results
						for(int s=0; s<numStrat; s++){
							tableGroup[g][s][2]=MathUtils.round((double)tableGroup[g][s][2],dimInfo.decimals[dimInfo.effectDim]); //Benefit
							tableGroup[g][s][3]=MathUtils.round((double)tableGroup[g][s][3],dimInfo.decimals[dimInfo.costDim]); //Cost
							tableGroup[g][s][4]=MathUtils.round((double)tableGroup[g][s][4],dimInfo.decimals[dimInfo.costDim]); //NMB
							tableGroup[g][s][5]=MathUtils.round((double)tableGroup[g][s][5],dimInfo.decimals[dimInfo.extendedDim]); //Extended dim
						}
					}
				}
			}
		}
	}

	public void printResults(Console console, boolean allStrategies){
		if(dimInfo.analysisType==0 || allStrategies==false){
			if(myModel.type==0){myModel.tree.printEVResults(console,this);}
			else if(myModel.type==1){
				if(allStrategies==true){myModel.markov.printModelResults(console,this);}
				else{myModel.markov.printChainResults(console, myModel.panelMarkov.curNode,this);}
			}
		}
		else if(dimInfo.analysisType==1){printCEAResults(console);}
		else if(dimInfo.analysisType==2){printBCAResults(console);}
		else if(dimInfo.analysisType==3){printECEAResults(console);}
		
		if(myModel.simType==1 && myModel.displayIndResults==true){ //Display Monte carlo results
			printMicroResults(console);
		}
	}
	
	private void printCEAResults(Console console){
		//Print results
		console.print("\nCEA Results:\n");
		boolean colTypes[]=new boolean[]{false,true,true,true,false}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.costDim],dimInfo.dimNames[dimInfo.effectDim],"ICER","Notes"};
		curTable.addRow(headers);
		for(int s=0; s<numStrat; s++){
			String row[]=new String[]{table[s][1]+"",table[s][2]+"",table[s][3]+"",table[s][4]+"",table[s][5]+""};
			curTable.addRow(row);
		}
		curTable.print();
		
		if(numSubgroups>0){
			for(int g=0; g<numSubgroups; g++){
				console.print("\nSubgroup Results: "+subgroupNames[g]+" (n="+subgroupSizes[g]+")\n");
				ConsoleTable curTableGroup=new ConsoleTable(console,colTypes);
				curTableGroup.addRow(headers);
				for(int s=0; s<numStrat; s++){
					String row[]=new String[]{tableGroup[g][s][1]+"",tableGroup[g][s][2]+"",tableGroup[g][s][3]+"",tableGroup[g][s][4]+"",tableGroup[g][s][5]+""};
					curTableGroup.addRow(row);
				}
				curTableGroup.print();
			}
		}
		
		console.newLine();
	}

	private void printBCAResults(Console console){
		console.print("\nBCA Results:\n");
		boolean colTypes[]=new boolean[]{false,true,true,true}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.effectDim],dimInfo.dimNames[dimInfo.costDim],"NMB"};
		curTable.addRow(headers);
		for(int s=0; s<numStrat; s++){
			String row[]=new String[]{table[s][1]+"",table[s][2]+"",table[s][3]+"",table[s][4]+""};
			curTable.addRow(row);
		}
		curTable.print();
		
		if(numSubgroups>0){
			for(int g=0; g<numSubgroups; g++){
				console.print("\nSubgroup Results: "+subgroupNames[g]+" (n="+subgroupSizes[g]+")\n");
				ConsoleTable curTableGroup=new ConsoleTable(console,colTypes);
				curTableGroup.addRow(headers);
				for(int s=0; s<numStrat; s++){
					String row[]=new String[]{tableGroup[g][s][1]+"",tableGroup[g][s][2]+"",tableGroup[g][s][3]+"",tableGroup[g][s][4]+""};
					curTableGroup.addRow(row);
				}
				curTableGroup.print();
			}
		}
		
		console.newLine();
	}

	private void printECEAResults(Console console){
		console.print("\nECEA Results:\n");
		boolean colTypes[]=new boolean[]{false,true,true,true,true}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.effectDim],dimInfo.dimNames[dimInfo.costDim],"NMB",dimInfo.dimNames[dimInfo.extendedDim]};
		curTable.addRow(headers);
		for(int s=0; s<numStrat; s++){
			String row[]=new String[]{table[s][1]+"",table[s][2]+"",table[s][3]+"",table[s][4]+"",table[s][5]+""};
			curTable.addRow(row);
		}
		curTable.print();
		
		if(numSubgroups>0){
			for(int g=0; g<numSubgroups; g++){
				console.print("\nSubgroup Results: "+subgroupNames[g]+" (n="+subgroupSizes[g]+")\n");
				ConsoleTable curTableGroup=new ConsoleTable(console,colTypes);
				curTableGroup.addRow(headers);
				for(int s=0; s<numStrat; s++){
					String row[]=new String[]{tableGroup[g][s][1]+"",tableGroup[g][s][2]+"",tableGroup[g][s][3]+"",tableGroup[g][s][4]+"",tableGroup[g][s][5]+""};
					curTableGroup.addRow(row);
				}
				curTableGroup.print();
			}
		}
		
		console.newLine();
	}
	
	private void printMicroResults(Console console){
		console.print("\nIndividual-level Results:\n");
		int numMicro=microStats.size();
		for(int s=0; s<numMicro; s++){
			String nameType="Strategy"; //Decision tree
			if(type==1){nameType="Chain";} //Markov model
			console.print(nameType+": "+names.get(s)+"\n");
			microStats.get(s).printSummary(console);
		}
		
		//subgroup
		if(numSubgroups>0){
			for(int g=0; g<numSubgroups; g++){
				console.print("\nSubgroup: "+subgroupNames[g]+"\n");
				for(int s=0; s<numMicro; s++){
					String nameType="Strategy"; //Decision tree
					if(type==1){nameType="Chain";} //Markov model
					console.print(nameType+": "+names.get(s)+"\n");
					microStatsGroup[g].get(s).printSummary(console);
				}
			}
		}
		
		console.newLine();
	}
	
	public void write(String filepath) throws IOException{
		if(type==0){treeReport.write(filepath+"_TreeReport.csv");}
		else if(type==1){
			int numTrace=markovTraces.size();
			for(int i=0; i<numTrace; i++){
				markovTraces.get(i).write(filepath);
			}
		}
		
		if(myModel.simType==1 && myModel.displayIndResults==true){
			int numMicro=microStats.size();
			for(int s=0; s<numMicro; s++){
				microStats.get(s).write(filepath+names.get(s)+"_Ind.csv");
			}
			
			if(numSubgroups>0){
				for(int g=0; g<numSubgroups; g++){
					for(int s=0; s<numMicro; s++){
						microStatsGroup[g].get(s).write(filepath+names.get(s)+"_Ind_"+subgroupNames[g]+".csv");
					}
				}
			}
		}
	}

}