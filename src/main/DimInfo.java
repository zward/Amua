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

package main;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DimInfo")
public class DimInfo{

	@XmlElement public String dimNames[], dimSymbols[];
	@XmlElement public int decimals[];
	@XmlElement public int analysisType=0;
	/**
	 * Objective: 0=Maximize, 1=Minimize
	 */
	@XmlElement public int objective=0, objectiveDim=0;
	@XmlElement public int costDim, effectDim;
	@XmlElement public double WTP;
	@XmlElement public String baseScenario; //baseline strategy
	@XmlElement public int extendedDim;
	
	//Constructor
	public DimInfo(){
		dimNames=new String[]{"Cost"};
		dimSymbols=new String[]{"$"};
		decimals=new int[]{4};
	}
	
	public DimInfo copy(){
		DimInfo copy=new DimInfo();
		int numDim=dimNames.length;
		copy.dimNames=new String[numDim];
		copy.dimSymbols=new String[numDim];
		copy.decimals=new int[numDim];
		for(int d=0; d<numDim; d++){
			copy.dimNames[d]=dimNames[d];
			copy.dimSymbols[d]=dimSymbols[d];
			copy.decimals[d]=decimals[d];
		}
		copy.analysisType=analysisType;
		copy.objective=objective;
		copy.objectiveDim=objectiveDim;
		copy.costDim=costDim;
		copy.effectDim=effectDim;
		copy.WTP=WTP;
		copy.baseScenario=baseScenario;
		copy.extendedDim=extendedDim;
		
		return(copy);
	}
	
	public String[] getOutcomes() {
		String outcomes[] = null;
		if(analysisType==0){ //EV
			outcomes=new String[dimNames.length];
			for(int d=0; d<dimNames.length; d++){
				outcomes[d]=dimNames[d];
			}
		}
		else{ //CEA or BCA
			outcomes=new String[dimNames.length+1];
			for(int d=0; d<dimNames.length; d++){
				outcomes[d]=dimNames[d];
			}
			if(analysisType==1){ //CEA
				outcomes[dimNames.length]="ICER ("+dimNames[costDim]+"/"+dimNames[effectDim]+")";
			}
			else if(analysisType==2){ //BCA
				outcomes[dimNames.length]="NMB ("+dimNames[effectDim]+"-"+dimNames[costDim]+")";
			}
		}
		return(outcomes);
	}
}