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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import gui.frmMain;

public class RecentFiles{
	
	public ArrayList<String> recentFiles;
	public String filepath;
	
	//Constructor
	public RecentFiles(){
		File empty=new File("");
		String dir=empty.getAbsolutePath();

		//look for recent files
		filepath=dir+File.separator+"_recentFiles";
		recentFiles=new ArrayList<String>();
		File file=new File(filepath);
		if(file.exists()){ //read
			try{
				FileInputStream fstream = new FileInputStream(filepath);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine=br.readLine();
				while(strLine!=null){
					recentFiles.add(strLine);
					strLine=br.readLine();
				}
				br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void updateList(String filename,int type){
		try{
			int index=recentFiles.indexOf(filename+";"+type);
			if(index!=-1){ //already in list
				recentFiles.remove(index); //take out
			}
			recentFiles.add(0,filename+";"+type);
			
			FileWriter fstream = new FileWriter(filepath);
			BufferedWriter out = new BufferedWriter(fstream);

			//write out first 10
			int num=Math.min(10, recentFiles.size());
			for(int i=0; i<num; i++){
				out.write(recentFiles.get(i)); out.newLine();
			}
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void buildList(JMenu mnOpenRecent,final frmMain mainForm){
		mnOpenRecent.removeAll();
		int num=Math.min(10, recentFiles.size());
		for(int i=0; i<num; i++){
			String line=recentFiles.get(i);
			String data[]=line.split(";");
			String file=data[0];
			String separator=File.separator;
			int index0=file.lastIndexOf(separator);
			int index1=file.indexOf(".");
			String name=file.substring(index0+1, index1);
			final JMenuItem mntmNewMenuItem = new JMenuItem(name);
			mntmNewMenuItem.setToolTipText(file);
			if(data.length>1){
				if(data[1].matches("0")){mntmNewMenuItem.setIcon(new ImageIcon(frmMain.class.getResource("/images/modelTree_16.png")));}
				else if(data[1].matches("1")){mntmNewMenuItem.setIcon(new ImageIcon(frmMain.class.getResource("/images/markovChain_16.png")));}
			}
			mntmNewMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String curFilepath=mntmNewMenuItem.getToolTipText();
					mainForm.openModel(new File(curFilepath));
				}
			});
			mnOpenRecent.add(mntmNewMenuItem);
		}
		
	}
		
}