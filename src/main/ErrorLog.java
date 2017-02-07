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
import java.util.Date;


public class ErrorLog{
	String version="0.0.1";
	String systemInfo[][];
	ArrayList<String> errors;

	//Constructor
	public ErrorLog(){
		systemInfo=new String[24][2];
		systemInfo[0][0]="Java Runtime Environment version";
		systemInfo[0][1]=System.getProperty("java.version");
		systemInfo[1][0]="Java Runtime Environment vendor";
		systemInfo[1][1]=System.getProperty("java.vendor");
		systemInfo[2][0]="Java vendor URL";
		systemInfo[2][1]=System.getProperty("java.vendor.url");
		systemInfo[3][0]="Java installation directory";
		systemInfo[3][1]=System.getProperty("java.home");
		systemInfo[4][0]="Java Virtual Machine specification version";
		systemInfo[4][1]=System.getProperty("java.vm.specification.version");
		systemInfo[5][0]="Java Virtual Machine specification vendor";
		systemInfo[5][1]=System.getProperty("java.vm.specification.vendor");
		systemInfo[6][0]="Java Virtual Machine specification name";
		systemInfo[6][1]=System.getProperty("java.vm.specification.name");
		systemInfo[7][0]="Java Virtual Machine implementation version";
		systemInfo[7][1]=System.getProperty("java.vm.version");
		systemInfo[8][0]="Java Virtual Machine implementation vendor";
		systemInfo[8][1]=System.getProperty("java.vm.vendor");
		systemInfo[9][0]="Java Virtual Machine implementation name";
		systemInfo[9][1]=System.getProperty("java.vm.name");
		systemInfo[10][0]="Java Runtime Environment specification version";
		systemInfo[10][1]=System.getProperty("java.specification.version");
		systemInfo[11][0]="Java Runtime Environment specification vendor";
		systemInfo[11][1]=System.getProperty("java.specification.vendor");
		systemInfo[12][0]="Java Runtime Environment specification name";
		systemInfo[12][1]=System.getProperty("java.specification.name");
		systemInfo[13][0]="Java class format version number";
		systemInfo[13][1]=System.getProperty("java.class.version");
		systemInfo[14][0]="Java class path";
		systemInfo[14][1]=System.getProperty("java.class.path");
		systemInfo[15][0]="Default temp file path";
		systemInfo[15][1]=System.getProperty("java.io.tmpdir");
		systemInfo[16][0]="Name of JIT compiler to use";
		systemInfo[16][1]=System.getProperty("java.compiler");
		systemInfo[17][0]="Operating system name";
		systemInfo[17][1]=System.getProperty("os.name");
		systemInfo[18][0]="Operating system architecture";
		systemInfo[18][1]=System.getProperty("os.arch");
		systemInfo[19][0]="Operating system version";
		systemInfo[19][1]=System.getProperty("os.version");
		int mb = 1024*1024;
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        systemInfo[20][0]="Heap - Used Memory";
		systemInfo[20][1]=(runtime.totalMemory() - runtime.freeMemory()) / mb+" MB";
		systemInfo[21][0]="Heap - Free Memory";
		systemInfo[21][1]=(runtime.freeMemory()) / mb+" MB";
		systemInfo[21][0]="Heap - Total Memory";
		systemInfo[21][1]=(runtime.totalMemory()) / mb+" MB";
		systemInfo[22][0]="Heap - Max Memory";
		systemInfo[22][1]=(runtime.maxMemory()) / mb+" MB";
		systemInfo[23][0]="Available Processors";
		systemInfo[23][1]=runtime.availableProcessors()+"";
		
		errors=new ArrayList<String>();
	}
	
	public void recordError(Exception e){
		StackTraceElement[] trace= e.getStackTrace();
		String error=new Date()+"\n";
		for(int i=0; i<trace.length; i++){
			error+=trace[i]+"\n";
		}
		errors.add(error);
	}
	
	
}