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

import markov.MarkovTrace;
import markov.MarkovTraceSummary;
import tree.TreeReport;
import tree.TreeReportSummary;

public class RunReportSummary{
	
	/**0=Decision Tree, 1=Markov Model*/
	int type;
	/**
	 * 0=Cohort (Deterministic), 1=Monte Carlo (Stochastic)
	 */
	int simType;
	
	AmuaModel myModel;
	
	int numMicro;
	String microNames[];

	/**
	 * [Dimension][Strategy][Mean/LB/UB]
	 */
	public double outcomeEVs[][][];
	/**
	 * CEA/BCA results
	 */
	public Object table[][];

	public MicroStatsSummary microStatsSummary[];
	
	TreeReportSummary treeReportSummary;
	public MarkovTraceSummary markovTraceSummary[];

	//Constructor
	public RunReportSummary(RunReport reports[]){
		int numReports=reports.length;
		type=reports[0].type;
		myModel=reports[0].myModel;
		simType=myModel.simType;
	
		//Get summary
		TreeReport treeReports[]=null;
		MarkovTrace traces[][]=null;
		int numChains=0;
		if(type==0){treeReports=new TreeReport[numReports];}
		else if(type==1){
			numChains=reports[0].markovTraces.size();
			traces=new MarkovTrace[numChains][numReports];
		}
		
		for(int i=0; i<numReports; i++){
			if(type==0){treeReports[i]=reports[i].treeReport;}
			else if(type==1){
				for(int j=0; j<numChains; j++){
					traces[j][i]=reports[i].markovTraces.get(j);
				}
			}
		}
		
		//Get report summaries
		if(type==0){treeReportSummary=new TreeReportSummary(treeReports);}
		else if(type==1){
			markovTraceSummary=new MarkovTraceSummary[numChains];
			for(int i=0; i<numChains; i++){
				markovTraceSummary[i]=new MarkovTraceSummary(traces[i]);
			}
		}
		
		if(simType==1 && myModel.displayIndResults==true){
			numMicro=reports[0].microStats.size();
			microNames=new String[numMicro];
			microStatsSummary=new MicroStatsSummary[numMicro];
			for(int s=0; s<numMicro; s++){
				microStatsSummary[s]=new MicroStatsSummary(reports,s);
				microNames[s]=reports[0].names.get(s);
			}
		}
		
	}

	public void write(String filepath) throws IOException{
		if(type==0){treeReportSummary.write(filepath+"_TreeReportSummary.csv");}
		else if(type==1){
			for(int i=0; i<markovTraceSummary.length; i++){
				markovTraceSummary[i].write(filepath);
			}
		}
		
		if(simType==1 && myModel.displayIndResults==true){
			for(int s=0; s<numMicro; s++){
				microStatsSummary[s].write(filepath+microNames[s]+"_IndResultsSummary.csv");
			}
		}
		
	}
	


}