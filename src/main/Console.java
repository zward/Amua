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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.text.Document;
import javax.swing.text.Element;

import base.AmuaModel;
import math.Interpreter;
import math.Numeric;

public class Console{
	public StyledTextPane textConsole;
	AmuaModel myModel;
	int prevCaretPos;
	ArrayList<String> cmdHistory;
	int upCount=0;
	int upPos=0;
	int posClick;
	boolean mouseDrag, clickEdit;
	
	//Constructor
	public Console(String version){
		cmdHistory=new ArrayList<String>();
		
		textConsole = new StyledTextPane(myModel);
		textConsole.setFont(new Font("Consolas", Font.PLAIN, 15));
		//Start-up text
		textConsole.setText("Amua version "+version+"\n");
		print("Copyright \u00A9 2017, 2018 Zachary J. Ward (https://github.com/zward/Amua)\n\n");
		print("Amua is free software and is distributed in the hope that it will be useful, but comes with ABSOLUTELY NO WARRANTY.\n");
		print("See Help -> About Amua for distribution details.\n");
		
		newLine();
		
		
		textConsole.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0){
				if(arg0.getButton()==MouseEvent.BUTTON1){ //Left
					mouseDrag=false;
					posClick=textConsole.viewToModel(arg0.getPoint());
					clickEdit=mouseEditable(posClick);
					if(clickEdit==false){
						textConsole.setCaretColor(new Color(0,0,0,0)); //make clear
					}
				}
			}
			public void mouseReleased(MouseEvent arg0){
				if(arg0.getButton()==MouseEvent.BUTTON1){ //Left
					int posRelease=textConsole.viewToModel(arg0.getPoint());
					boolean releaseEdit=mouseEditable(posRelease);
					if(mouseDrag==false){ //not highlighting text
						if(releaseEdit==false){ //disallow
							textConsole.setCaretPosition(textConsole.getText().length());
						}
						textConsole.setEditable(true);
						textConsole.setCaretColor(null); //make black
					}
					else{ //highlighting text
						if(clickEdit==false || releaseEdit==false){ //non-editable section highlighted
							textConsole.setEditable(false);
							textConsole.setCaretColor(new Color(0,0,0,0)); //make clear
						}
						else{
							textConsole.setEditable(true);
							textConsole.setCaretColor(null); //make black
						}
					}
				}
			}
		});
				
		textConsole.addMouseMotionListener(new MouseAdapter(){
			@Override
			public void mouseDragged(MouseEvent arg0){
				mouseDrag=true;
			}
		});
		
		textConsole.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try{
					prevCaretPos=textConsole.getCaretPosition();
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						e.consume();
						String all=textConsole.getText();
						String lines[]=all.split("\n");
						String line=lines[lines.length-1]; //last line
						String cmd=line.substring(2); //trim '> '
						Document doc = textConsole.getDocument();
						if(!cmd.isEmpty()){
							cmdHistory.add(cmd);
							Numeric result=Interpreter.evaluate(cmd, myModel,false);
							doc.insertString(doc.getLength(), " \n: "+result.toString(), null);
						}
						doc.insertString(doc.getLength(), "\n> ", null);
						textConsole.setCaretPosition(doc.getLength());
						prevCaretPos=textConsole.getCaretPosition();
						textConsole.setStyleStart(prevCaretPos);
						upCount=0; //reset
					}
					else if(e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
						int pos = textConsole.getCaretPosition();
						Element map = textConsole.getDocument().getDefaultRootElement();
						int row = map.getElementIndex(pos);
						Element lineElem = map.getElement(row);
						int col = pos - lineElem.getStartOffset();
						if(col<3){ //disallow
							e.consume();
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_UP){
						e.consume();
						if(upCount==0){
							Document doc = textConsole.getDocument();
							upPos=doc.getLength();
						}
						upCount++;
						int size=cmdHistory.size();
						if(upCount>size){upCount=size;}
						if(size>0){
							Document doc = textConsole.getDocument();
							String cmd=cmdHistory.get(size-upCount);
							doc.remove(upPos, doc.getLength()-upPos);
							doc.insertString(upPos, cmd, null);
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_DOWN){
						e.consume();
						upCount--;
						if(upCount>0){
							int size=cmdHistory.size();
							Document doc = textConsole.getDocument();
							String cmd=cmdHistory.get(size-upCount);
							doc.remove(upPos, doc.getLength()-upPos);
							doc.insertString(upPos, cmd, null);	
						}
						else{
							upCount=0; //keep at 1
							Document doc = textConsole.getDocument();
							doc.remove(upPos, doc.getLength()-upPos);
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_PAGE_UP || e.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
						e.consume(); //disallow
					}
					else if(e.getKeyCode()==KeyEvent.VK_HOME){
						e.consume();
						int pos = textConsole.getCaretPosition();
						Element map = textConsole.getDocument().getDefaultRootElement();
						int row = map.getElementIndex(pos);
						Element lineElem = map.getElement(row);
						textConsole.setCaretPosition(lineElem.getStartOffset()+2);
					}


				}catch(Exception ex){
					Document doc = textConsole.getDocument();
					try{
						doc.insertString(doc.getLength(), " \n: "+ex.getMessage(), null);
						doc.insertString(doc.getLength(), "\n> ", null);
						textConsole.setCaretPosition(doc.getLength());
						prevCaretPos=textConsole.getCaretPosition();
						textConsole.setStyleStart(prevCaretPos);
						ex.printStackTrace();
					}catch(Exception ex2){
						ex2.printStackTrace();
					}
				}
			}
		});
		
	}
	
	private boolean mouseEditable(int mousePos){
		Element map = textConsole.getDocument().getDefaultRootElement();
		int row = map.getElementIndex(mousePos);
		Element lineElem = map.getElement(row);
		int col = mousePos - lineElem.getStartOffset();
		String all=textConsole.getText();
		String lines[]=all.split("\n");
		int numRows=lines.length;
		if(col<3 || row<(numRows-1)){ //disallow
			return(false);
		}
		else{
			return(true);
		}
	}
	
	public void switchModel(AmuaModel model){
		myModel=model;
		textConsole.myModel=model;
		textConsole.restyle();
	}
	
	public void print(String text){
		try{
			Document doc = textConsole.getDocument();
			doc.insertString(doc.getLength(),text,null);

			textConsole.setCaretPosition(doc.getLength());
			prevCaretPos=textConsole.getCaretPosition();
			textConsole.setStyleStart(prevCaretPos);
			
		}catch(Exception ex2){
			ex2.printStackTrace();
		}
	}
	
	public void newLine(){
		try{
			Document doc = textConsole.getDocument();
			doc.insertString(doc.getLength(), "\n> ", null);
			textConsole.setCaretPosition(doc.getLength());
			prevCaretPos=textConsole.getCaretPosition();
			textConsole.setStyleStart(prevCaretPos);
			
		}catch(Exception ex2){
			ex2.printStackTrace();
		}
	}
	
	/*public void append(String text){
		try{
			Document doc = textConsole.getDocument();
			doc.insertString(doc.getLength(),text,null);
			
			doc.insertString(doc.getLength(), "\n> ", null);
			textConsole.setCaretPosition(doc.getLength());
			prevCaretPos=textConsole.getCaretPosition();
		}catch(Exception ex2){
			ex2.printStackTrace();
		}
	}*/
}