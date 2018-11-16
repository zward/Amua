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


package export_Cpp;

public final class CppConstants{
	
	public static String translate(String text){
		switch(text){
		case "e": return("M_E");
		case "inf": return("INFINITY");
		case "pi": return("M_PI");
		case "π": return("M_PI");
		}
		return(null); //fell through
	}
}