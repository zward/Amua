/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2024 Zachary J. Ward
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

package voi;

import main.PSAResults;
import math.Numeric;

public class EVPI{
	
	PSAResults psaResults;
	
	public int numStrat;
	public int sign;
	
	public double meanOutcomes[];
	public int numBest[];
	
	public double bestOutcome, bestMean;
	public int bestStrat;
	
	public double evpi;
	
	
	//Constructor
	public EVPI(PSAResults psaResults){
		this.psaResults=psaResults;
	}
	
	
	//Calculates EVPI
	public void calculate() {
		//Calculate EVPI
		sign=1; //objective is to maximize outcome
		if(psaResults.analysisType==0 && psaResults.objective==1) { //EV, minimize - change to maximize negative
			sign=-1;
		}
		if(psaResults.analysisType>0) { //CEA, BCA, or ECEA
			if(psaResults.objective==1) { //minimize
				sign=-1;
			}
		}
		
		int numIterations=psaResults.numIterations;
		numStrat=psaResults.numStrat;
		meanOutcomes=new double[numStrat];
		numBest=new int[numStrat];
		bestOutcome=0; //mean of max
		for(int n=0; n<numIterations; n++) {
			double curBest=Double.NEGATIVE_INFINITY;
			int bestS=-1;
			for(int s=0; s<numStrat; s++) {
				double curRes=sign*psaResults.results[s][n];
				meanOutcomes[s]+=curRes;
				if(curRes>curBest) {
					curBest=curRes;
					bestS=s;
				}
			}
			bestOutcome+=curBest;
			numBest[bestS]++;
		}
		bestOutcome/=(numIterations*1.0);
		bestMean=Double.NEGATIVE_INFINITY;
		bestStrat=-1;
		for(int s=0; s<numStrat; s++) {
			double curMean=meanOutcomes[s]/=(numIterations*1.0);
			if(curMean>bestMean) {
				bestMean=curMean;
				bestStrat=s;
			}
		}

		//EVPI: E[max] - max E[]
		evpi=bestOutcome-bestMean;
	}
	
}