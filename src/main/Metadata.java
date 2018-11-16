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
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Metadata")
public class Metadata{
	String version;
	
	@XmlElement public String author;
	@XmlElement public String dateCreated;
	@XmlElement public String versionCreated;
	@XmlElement public String modifier;
	@XmlElement public String dateModified;
	@XmlElement public String versionModified;
	
	//Constructor
	public Metadata(){ //no-arg constructor for xml binding
		
	}
	
	public Metadata(String version){
		this.version=version;
	}
	
	public void update(){
		if(author==null){ //Create
			author=System.getProperty("user.name");
			dateCreated=(new Date()+"");
			versionCreated=version;	
		}
		else{ //Modify
			modifier=System.getProperty("user.name");
			dateModified=(new Date()+"");
			versionModified=version;
		}
	}
}