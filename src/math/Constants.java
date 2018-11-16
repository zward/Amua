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


package math;

public final class Constants{
	
	public static boolean isConstant(String text){
		switch(text){
		case "e": return(true);
		case "inf": return(true);
		case "pi":return(true);
		case "π": return(true);
		}
		return(false); //fell through
	}

	public static double evaluate(String text){
		switch(text){
		case "e": return(Math.E);
		case "inf": return(Double.POSITIVE_INFINITY);
		case "pi": return(Math.PI);
		case "π": return(Math.PI);
		}
		return(Double.NaN); //fell through
	}

	private static String consoleFont(String str){
		return("<font face=\"Consolas\">"+str+"</font>");
	}
	
	public static String getDescription(String text){
		String des="";
		switch(text){
		case "e": return("<html><b>Euler's number ("+consoleFont("e")+")</b><br>"+consoleFont("2.718281828459045")+"<br>The double value that is closer than any other to "+consoleFont("e")+", the base of the natural logarithms</html>");
		case "inf": return("<html><b>Infinity ("+consoleFont("∞")+")</b><br>A constant holding the positive infinity of type "+consoleFont("double")+"</html>");
		case "pi": return("<html><b>Pi ("+consoleFont("π")+")</b><br>"+consoleFont("3.141592653589793")+"<br>The "+consoleFont("double")+" value that is closer than any other to "+consoleFont("π")+", the ratio of the circumference of a circle to its diameter</html>");
		case "π": return("<html><b>Pi ("+consoleFont("π")+")</b><br>"+consoleFont("3.141592653589793")+"<br>The "+consoleFont("double")+" value that is closer than any other to "+consoleFont("π")+", the ratio of the circumference of a circle to its diameter</html>");
		}
		return(des); //fell through
	}


}