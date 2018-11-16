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

import base.AmuaModel;
import markov.MarkovTree;
import tree.DecisionTree;

public class Clipboard{

	public boolean cut; //if 'cut' is true, clear clipboard after paste
	
	//Elements that can be copied
	public AmuaModel copyModel;
	public DecisionTree copyTree;
	public MarkovTree copyMarkov;
	
	public Clipboard(){
		
	}
	
	
	public void clear(){
		copyModel=null;
		copyTree=null;
		copyMarkov=null;
	}
	
	public boolean isCompatible(AmuaModel target){
		boolean compatible=true;
		if(target.type!=copyModel.type){compatible=false;} //Different model types
		else if(target.dimInfo.dimSymbols.length!=copyModel.dimInfo.dimSymbols.length){
			compatible=false;
		}
		else{ //Compare dimension symbols
			int numDim=target.dimInfo.dimSymbols.length;
			for(int d=0; d<numDim; d++){
				if(!target.dimInfo.dimSymbols[d].equals(copyModel.dimInfo.dimSymbols[d])){
					compatible=false;
				}
			}
		}
		
		return(compatible);
		
	}
	
	
}