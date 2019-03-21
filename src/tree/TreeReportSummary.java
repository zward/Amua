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

package tree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import base.AmuaModel;
import math.MathUtils;

public class TreeReportSummary{
	int numRows=0;
	int maxLevels=1;
	ArrayList<String> rowNames[];
	double rowValues[][];
	
	//Constructor
	public TreeReportSummary(TreeReport reports[]){
		int numReports=reports.length;
		reports[0].getNames();
		rowNames=reports[0].rowNames;
		numRows=reports[0].numRows;
		maxLevels=reports[0].maxLevels;
		
		int boundIndices[]=MathUtils.getBoundIndices(numReports);
		//Calculate mean and bounds
		rowValues=new double[numRows][3];
		for(int r=0; r<numRows; r++){
			double vals[]=new double[numReports];
			for(int i=0; i<numReports; i++){
				rowValues[r][0]+=reports[i].rowValues[r];
				vals[i]=reports[i].rowValues[r];
			}
			rowValues[r][0]/=(numReports*1.0);
			Arrays.sort(vals);
			rowValues[r][1]=vals[boundIndices[0]];
			rowValues[r][2]=vals[boundIndices[1]];
		}
	}
	
	public void write(String filepath) throws IOException{
		FileWriter fstream = new FileWriter(filepath); //Create new file
		BufferedWriter out = new BufferedWriter(fstream);

		//Headers
		for(int i=0; i<maxLevels; i++){out.write("Level "+i+",");}
		out.write("Mean,95% LB,95% UB");
		out.newLine();
		
		//Data
		for(int r=0; r<numRows; r++){
			ArrayList<String> names=rowNames[r];
			int numNames=names.size();
			for(int i=0; i<numNames; i++){out.write(names.get(i)+",");}
			for(int i=numNames; i<maxLevels; i++){out.write(",");}
			out.write(rowValues[r][0]+",");
			out.write(rowValues[r][1]+",");
			out.write(rowValues[r][2]+"");
			out.newLine();
		}
		
		out.close();
	}
	
}