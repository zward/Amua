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

import java.util.ArrayList;

public class ConsoleTable{
	Console console;
	ArrayList<String[]> rows;
	/**
	 * True=number (align right), False=text (alight left)
	 */
	boolean colTypes[];
	
	//Constructor
	public ConsoleTable(Console console, boolean colTypes[]){
		this.console=console;
		this.colTypes=colTypes;
		rows=new ArrayList<String[]>();
	}	
	
	public void addRow(String row[]){
		rows.add(row);
	}
	
	public void print(){
		int numRows=rows.size();
		int numCols=rows.get(0).length;
		//get max column widths
		int colWidth[]=new int[numCols];
		for(int r=0; r<numRows; r++){
			String row[]=rows.get(r);
			for(int c=0; c<numCols; c++){
				String text=row[c];
				colWidth[c]=Math.max(colWidth[c],text.length());
			}
		}
		//print aligned columns
		//headers
		String row[]=rows.get(0);
		for(int c=0; c<numCols; c++){
			String text=align(row[c],colWidth[c],false);
			if(c>0){console.print("\t");}
			console.print(text);
		}
		console.print("\n");
		//'---'
		for(int c=0; c<numCols; c++){
			String text="";
			for(int i=0; i<colWidth[c]; i++){text+="-";}
			if(c>0){console.print("\t");}
			console.print(text);
		}
		console.print("\n");
		//rows
		for(int r=1; r<numRows; r++){
			row=rows.get(r);
			for(int c=0; c<numCols; c++){
				String text=align(row[c],colWidth[c],colTypes[c]);
				if(c>0){console.print("\t");}
				console.print(text);
			}
			console.print("\n");
		}
	}
	
	private String align(String text, int targetLength, boolean number){
		int curLength=text.length();
		int diff=targetLength-curLength;
		String pad="";
		for(int i=0; i<diff; i++){pad+=" ";}
		if(number==false){ //text, align left
			text+=pad;
		}
		else{ //number, align right
			text=pad+text;
		}
		
		return(text);
	}
}