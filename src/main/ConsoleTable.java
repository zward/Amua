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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ConsoleTable{
	Console console;
	ArrayList<String[]> rows;
	/**
	 * True=number (align right), False=text (alight left)
	 */
	boolean colTypes[];
	Font curFont;
	boolean monospaced;
	
	//Constructor
	public ConsoleTable(Console console, boolean colTypes[]){
		this.console=console;
		this.colTypes=colTypes;
		curFont=console.textConsole.getFont();
		if(curFont.getFamily().contains("Consolas")) {
			monospaced=true;
		}
		else {
			monospaced=false;
		}
		rows=new ArrayList<String[]>();
	}	
	
	public void addRow(String row[]){
		rows.add(row);
	}
	
	public void print() {
		if(monospaced==true) {
			print_mono();
		}
		else {
			print_pixels();
		}
	}
	
	private void print_mono(){
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
			String text=align_mono(row[c],colWidth[c],false);
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
				String text=align_mono(row[c],colWidth[c],colTypes[c]);
				if(c>0){console.print("\t");}
				console.print(text);
			}
			console.print("\n");
		}
	}
	
	private String align_mono(String text, int targetLength, boolean number){
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
	
	private void print_pixels(){ //print_px
		int numRows=rows.size();
		int numCols=rows.get(0).length;
		
		//get max column widths (pixels)
		int colWidth[]=new int[numCols];
		for(int r=0; r<numRows; r++){
			String row[]=rows.get(r);
			for(int c=0; c<numCols; c++){
				String text=row[c]+" "; //pad with extra space to ensure tabs separate
				colWidth[c]=Math.max(colWidth[c], getStringWidth(text)); //get pixel width
			}
		}
		//print aligned columns
		//headers
		String row[]=rows.get(0);
		for(int c=0; c<numCols; c++){
			String text=align_px(row[c],colWidth[c],false);
			if(c>0){console.print("\t");}
			console.print(text);
		}
		console.print("\n");
		//'---'
		for(int c=0; c<numCols; c++){
			String text=buildDashLine(colWidth[c]);
			//for(int i=0; i<colWidth[c]; i++){text+="-";}
			if(c>0){console.print("\t");}
			console.print(text);
		}
		console.print("\n");
		//rows
		for(int r=1; r<numRows; r++){
			row=rows.get(r);
			for(int c=0; c<numCols; c++){
				String text=align_px(row[c],colWidth[c],colTypes[c]);
				if(c>0){console.print("\t");}
				console.print(text);
			}
			console.print("\n");
		}
	}
	
	private int getStringWidth(String text) {
        // Create a temporary Graphics2D just for measurement
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setFont(curFont);
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(text, curFont, frc);
            // layout.getAdvance() often matches what you want better than stringWidth
            return Math.round(layout.getAdvance());
        } finally {
            g2.dispose();
        }
    }
	

    private String buildDashLine(int targetWidthPx) {
        int dashWidth = getStringWidth("-");
       // if (dashWidth <= 0) dashWidth = 1; // fallback

        int count =  Math.max(1, targetWidthPx / dashWidth);
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append('-');
        return sb.toString();
    }
    
    private String align_px(String text, int targetWidth, boolean number){
    	if(text==null) {text="";}
		int curWidth=getStringWidth(text);
		int diff=targetWidth-curWidth;
		
		if(diff<=0) {
			return(text); //already as wide as target
		}
		
		int spaceW=getStringWidth(" ");
		if(spaceW<=0) {
			return(text); //return original text
		}
		
		int spaceCount=Math.round((float)diff/spaceW);
		if(spaceCount<0) {spaceCount=0;}
		
		StringBuilder pad = new StringBuilder(spaceCount);
		for(int i=0; i<spaceCount; i++) {
			pad.append(" ");
		}
		
		if(number==false){ //text, align left
			text+=pad;
		}
		else{ //number, align right
			text=pad+text;
		}
		
		return(text);
	}
	
	
}