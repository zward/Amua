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

import math.Numeric;

@XmlRootElement(name="Variable")
public class Variable{
	@XmlElement public String name;
	@XmlElement public String initValue; //initial value
	//@XmlElement public String expression;
	@XmlElement public String notes;
	
	//@XmlTransient public boolean locked=false;
	@XmlTransient public boolean valid=true;
	@XmlTransient public Numeric value;
	
	//Constructor
	public Variable(){

	}

	public Variable copy(){
		Variable copyVar=new Variable();
		copyVar.name=name;
		copyVar.initValue=initValue;
		//copyVar.expression=expression;
		copyVar.notes=notes;
		copyVar.value=value;
		return(copyVar);
	}
}