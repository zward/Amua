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

import lang.Language;


public class ErrorLog{
	public String version;
	public String systemInfo[][];
	public ArrayList<String> errors;
	Language language;
	
	//Constructor
	public ErrorLog(String curVersion, Language language){
		version=curVersion;
		this.language=language;
		systemInfo=new String[24][2];
		systemInfo[0][0]=language.base.getString("system.jre_version"); //Java Runtime Environment version
		systemInfo[0][1]=System.getProperty("java.version");
		systemInfo[1][0]=language.base.getString("system.jre_vendor"); //Java Runtime Environment vendor
		systemInfo[1][1]=System.getProperty("java.vendor");
		systemInfo[2][0]=language.base.getString("system.vendor_url"); //Java vendor URL
		systemInfo[2][1]=System.getProperty("java.vendor.url");
		systemInfo[3][0]=language.base.getString("system.java_install_dir"); //Java installation directory
		systemInfo[3][1]=System.getProperty("java.home");
		systemInfo[4][0]=language.base.getString("system.jvm_spec_version"); //Java Virtual Machine specification version
		systemInfo[4][1]=System.getProperty("java.vm.specification.version");
		systemInfo[5][0]=language.base.getString("system.jvm_spec_vendor"); //Java Virtual Machine specification vendor
		systemInfo[5][1]=System.getProperty("java.vm.specification.vendor");
		systemInfo[6][0]=language.base.getString("system.jvm_spec_name"); //Java Virtual Machine specification name
		systemInfo[6][1]=System.getProperty("java.vm.specification.name");
		systemInfo[7][0]=language.base.getString("system.jvm_imp_version"); //Java Virtual Machine implementation version
		systemInfo[7][1]=System.getProperty("java.vm.version");
		systemInfo[8][0]=language.base.getString("system.jvm_imp_vendor"); //Java Virtual Machine implementation vendor
		systemInfo[8][1]=System.getProperty("java.vm.vendor");
		systemInfo[9][0]=language.base.getString("system.jvm_imp_name"); //Java Virtual Machine implementation name
		systemInfo[9][1]=System.getProperty("java.vm.name");
		systemInfo[10][0]=language.base.getString("system.jre_spec_version"); //Java Runtime Environment specification version
		systemInfo[10][1]=System.getProperty("java.specification.version");
		systemInfo[11][0]=language.base.getString("system.jre_spec_vendor"); //Java Runtime Environment specification vendor
		systemInfo[11][1]=System.getProperty("java.specification.vendor");
		systemInfo[12][0]=language.base.getString("system.jre_spec_name"); //Java Runtime Environment specification name
		systemInfo[12][1]=System.getProperty("java.specification.name");
		systemInfo[13][0]=language.base.getString("system.java_class_format_version"); //Java class format version number"
		systemInfo[13][1]=System.getProperty("java.class.version");
		systemInfo[14][0]=language.base.getString("system.java_class_path"); //Java class path
		systemInfo[14][1]=System.getProperty("java.class.path");
		systemInfo[15][0]=language.base.getString("system.temp_filepath"); //Default temp file path
		systemInfo[15][1]=System.getProperty("java.io.tmpdir");
		systemInfo[16][0]=language.base.getString("system.jit_compiler"); //Name of JIT compiler to use
		systemInfo[16][1]=System.getProperty("java.compiler");
		systemInfo[17][0]=language.base.getString("system.os_name"); //Operating system name
		systemInfo[17][1]=System.getProperty("os.name");
		systemInfo[18][0]=language.base.getString("system.os_arch"); //Operating system architecture
		systemInfo[18][1]=System.getProperty("os.arch");
		systemInfo[19][0]=language.base.getString("system.os_version"); //Operating system version
		systemInfo[19][1]=System.getProperty("os.version");
		int mb = 1024*1024;
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        systemInfo[20][0]=language.base.getString("system.heap_used"); //Heap - Used Memory
		systemInfo[20][1]=(runtime.totalMemory() - runtime.freeMemory()) / mb+" MB";
		systemInfo[21][0]=language.base.getString("system.heap_free"); //Heap - Free Memory"
		systemInfo[21][1]=(runtime.freeMemory()) / mb+" MB";
		systemInfo[21][0]=language.base.getString("system.heap_total"); //Heap - Total Memory
		systemInfo[21][1]=(runtime.totalMemory()) / mb+" MB";
		systemInfo[22][0]=language.base.getString("system.heap_max"); //Heap - Max Memory
		systemInfo[22][1]=(runtime.maxMemory()) / mb+" MB";
		systemInfo[23][0]=language.base.getString("system.avail_processors"); //Available Processors
		systemInfo[23][1]=runtime.availableProcessors()+"";
		
		errors=new ArrayList<String>();
	}
	
	public void recordError(Exception e){
		String error=new Date()+"\n";
		error+=e.toString()+"\n";
		error+=language.message.getString("err.message")+": "+e.getMessage()+"\n"; //Message:
		error+=language.message.getString("err.stack_trace")+":\n"; //Stack Trace
		StackTraceElement[] trace= e.getStackTrace();
		for(int i=0; i<trace.length; i++){
			error+=trace[i]+"\n";
		}
		errors.add(error);
	}
	
	
}