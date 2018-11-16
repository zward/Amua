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
import javax.xml.bind.annotation.XmlTransient;

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;

@XmlRootElement(name="Constraint")
public class Constraint{
	@XmlElement public String name;
	@XmlElement public String expression;
	@XmlElement public String notes;
	
	@XmlTransient public boolean valid=true;
	String constraints[];
	int numConst;
	//@XmlTransient public Numeric value;
	
	//Constructor
	public Constraint(){

	}

	public Constraint copy(){
		Constraint copyConst=new Constraint();
		copyConst.name=name;
		copyConst.expression=expression;
		copyConst.notes=notes;
		//copyConst.value=value;
		return(copyConst);
	}
	
	public void parseConstraints(){
		constraints=expression.split(";");
		numConst=constraints.length;
	}
	
	public boolean checkConstraints(AmuaModel myModel) throws Exception{
		boolean valid=true;
		int c=0;
		while(valid==true && c<numConst){
			Numeric test=Interpreter.evaluate(constraints[c], myModel, false);
			if(test.getBool()==false){
				valid=false;
			}
			c++;
		}
		return(valid);
	}
}