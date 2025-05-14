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


package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public final class FileUtils{
	
	public static ArrayList<String> listFiles(String dir, String ext){
		File folder=new File(dir);
		FileFilter filter = new FileFilter() {
			  public boolean accept(File file) {
			    if (file.getName().endsWith(ext)) {
			      return true;
			    }
			    return false;
			  }
			};
		File files[]=folder.listFiles(filter);
		ArrayList<String> list=new ArrayList<String>();
		int numFiles=files.length;
		for(int f=0; f<numFiles; f++) {
			list.add(files[f].getName());
		}
		return(list);
	}
	
	public static boolean fileExists(String dir, String name) {
		File check=new File(dir+File.separator+name);
		return(check.exists());
	}
	
	/**
	 * Counts number of lines in a file
	 * @param path
	 * @return
	 */
	public static int getLineCount(String path, boolean includeHeader) {
		int numLines=0;
		try {
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine=br.readLine(); //headers
			if(includeHeader) {numLines++;}
			strLine=br.readLine(); //next line
			while(strLine!=null) {
				numLines++;
				strLine=br.readLine(); //get next line
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return(numLines);
	}
	
}