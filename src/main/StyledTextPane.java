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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import base.AmuaModel;
import math.Constants;
import math.Distributions;
import math.Functions;
import math.Interpreter;
import math.MatrixFunctions;

public class StyledTextPane extends JTextPane{
	StyledTextPane pane;
	public AmuaModel myModel;
	private int defaultDismiss; //for tooltips
	private int startStyle=0; //index to begin restyle
	
	//Constructor
	public StyledTextPane(AmuaModel myModel){
		pane=this;
		this.myModel=myModel;
		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "none"); //Disable enter key in text pane
		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0), "none");
		this.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void insertUpdate(DocumentEvent e) {
				restyle();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				restyle();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				//restyle();
			}

		});
		
		this.setToolTipText("");
		this.addMouseListener(new MouseListener(){
			@Override
			public void mouseEntered(MouseEvent me) {
				defaultDismiss=ToolTipManager.sharedInstance().getDismissDelay();
				ToolTipManager.sharedInstance().setDismissDelay(100000);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				ToolTipManager.sharedInstance().setDismissDelay(defaultDismiss);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
		});

		this.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent e) {
				matchBrackets(e.getDot());
			}
		});

		this.addKeyListener(new KeyAdapter(){
			@Override
			public void keyTyped(KeyEvent e) {
				try{
					if(e.getKeyChar()=='('){insertString(")");}
					else if(e.getKeyChar()=='['){insertString("]");}
					else if(e.getKeyChar()==')'){
						int pos=pane.getCaretPosition();
						String ch=pane.getText(pos,1);
						if(ch.equals(")")){
							Document doc=pane.getDocument();
							doc.remove(pos, 1);
						}
					}
					else if(e.getKeyChar()==']'){
						int pos=pane.getCaretPosition();
						String ch=pane.getText(pos,1);
						if(ch.equals("]")){
							Document doc=pane.getDocument();
							doc.remove(pos, 1);
						}
					}
					else if(e.getKeyChar()=='"'){
						int pos=pane.getCaretPosition();
						String ch=pane.getText(pos,1);
						if(ch.equals("\"")){
							Document doc=pane.getDocument();
							doc.remove(pos, 1);
						}
						else{
							insertString("\""); //match quotes
						}
					}
					else if(e.getKeyChar()=='\''){
						int pos=pane.getCaretPosition();
						String ch=pane.getText(pos,1);
						if(ch.equals("\'")){
							Document doc=pane.getDocument();
							doc.remove(pos, 1);
						}
						else{
							insertString("'"); //match quotes
						}
					}
				} catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		
		//Parameter
		Style styleParam=this.addStyle("param", null);
		StyleConstants.setForeground(styleParam, Color.BLUE); //Blue
		StyleConstants.setBold(styleParam, true);
		//Variables
		Style styleVar=this.addStyle("var", null);
		StyleConstants.setForeground(styleVar, new Color(139,69,19)); //Brown
		StyleConstants.setBold(styleVar, true);
		//Lookup-table
		Style styleTable=this.addStyle("table", null);
		StyleConstants.setForeground(styleTable, new Color(247,106,24)); //Orange
		StyleConstants.setBold(styleTable, true);
		//Function
		Style styleFx=this.addStyle("fx", null);
		StyleConstants.setForeground(styleFx, new Color(128,0,0)); //Maroon
		StyleConstants.setBold(styleFx, true);
		//Distribution
		Style styleDist=this.addStyle("dist", null);
		StyleConstants.setForeground(styleDist, new Color(0,128,0)); //Green
		StyleConstants.setBold(styleDist, true);
		//Matrix
		Style styleMatrix=this.addStyle("matrix", null);
		StyleConstants.setForeground(styleMatrix, new Color(75,0,130)); //Purple
		StyleConstants.setBold(styleMatrix, true);
		//Italics
		Style styleItal=this.addStyle("ital", null);
		StyleConstants.setItalic(styleItal, true); //Italics
		StyleConstants.setBold(styleItal, true); //Bold
		//Plain
		Style stylePlain=this.addStyle("plain", null);
		StyleConstants.setForeground(stylePlain, Color.BLACK); //Plain
		//String
		Style styleString=this.addStyle("string", null);
		StyleConstants.setForeground(styleString, new Color(220,20,60)); //Crimson
		//Match parens
		Style styleParens=this.addStyle("parens", null);
		StyleConstants.setForeground(styleParens, Color.RED); //Red
		StyleConstants.setBold(styleParens, true);
	}

	@Override
	public String getToolTipText(MouseEvent event){
		String tip=null;
		//Get location of mouse
		int offset=this.viewToModel(event.getPoint());
		String text=pane.getText();
		if(offset<text.length()){
			int nextIndex=offset+Interpreter.getNextBreakIndex(text.substring(offset));
			int prevIndex=offset;
			char curChar=text.charAt(prevIndex);//substring(prevIndex, prevIndex+1);
			while(Interpreter.isBreak(curChar)==false && prevIndex>0){
				prevIndex--;
				//curChar=text.substring(prevIndex, prevIndex+1);
				curChar=text.charAt(prevIndex);
			}
			if(Interpreter.isBreak(curChar)){prevIndex++;}
			if(prevIndex>-1 && nextIndex>-1 && nextIndex>prevIndex){
				String word=text.substring(prevIndex, nextIndex); //Style word if recognized
				if(myModel!=null && myModel.isParameter(word)){tip=myModel.getParamDescription(word);}
				else if(myModel!=null && myModel.isVariable(word)){tip=myModel.getVarDescription(word);}
				else if(myModel!=null && myModel.isTable(word)){tip=myModel.getTableDescription(word);}
				else if(Constants.isConstant(word)){tip=Constants.getDescription(word);}
				else if(Functions.isFunction(word)){tip=Functions.getDescription(word);}
				else if(MatrixFunctions.isFunction(word)){tip=MatrixFunctions.getDescription(word);}
				else if(Distributions.isDistribution(word)){tip=Distributions.getDescription(word);}
			}
		}
		return(tip);
	}

	private void insertString(String insert) throws BadLocationException{
		int pos=pane.getCaretPosition();
		Document doc = pane.getDocument();
		doc.insertString(pos, insert, null);
		pane.setCaretPosition(pos);
	}
	
	private void matchBrackets(final int pos){
		try{
			Runnable doRestyle=new Runnable(){
				@Override
				public void run(){
					String text=pane.getText();
					int textLength=text.length();
					if(textLength>0){
						StyledDocument doc=pane.getStyledDocument();
						char left=' ', right=' ';
						if(pos>0){left=text.charAt(pos-1);}
						if(pos<text.length()){right=text.charAt(pos);}

						//reset all parens
						for(int i=0; i<textLength; i++){
							char ch=text.charAt(i);
							if(ch=='(' || ch==')' || ch=='[' || ch==']'){
								doc.setCharacterAttributes(i, 1, pane.getStyle("plain"),true);
							}
						}

						if(left=='(' || left=='['){ //find closing bracket to right
							int matchPos=pos-1;
							int parenLevel=0, bracketLevel=0;
							if(left=='('){parenLevel=1;}
							else{bracketLevel=1;}
							boolean found=false;
							while(found==false && matchPos<textLength-1){
								matchPos++;
								char ch=text.charAt(matchPos);
								if(ch=='('){parenLevel++;}
								else if(ch=='['){bracketLevel++;}
								else if(ch==')'){parenLevel--;}
								else if(ch==']'){bracketLevel--;}
								if(parenLevel==0 && bracketLevel==0){found=true;}
							}
							if(found==true){
								doc.setCharacterAttributes(pos-1, 1, pane.getStyle("parens"),true); 
								doc.setCharacterAttributes(matchPos, 1, pane.getStyle("parens"),true);
							}
						}
						else if(left==')' || left==']'){ //find closing bracket to left
							int matchPos=pos;
							int parenLevel=0, bracketLevel=0;
							boolean found=false;
							while(found==false && matchPos>0){
								matchPos--;
								char ch=text.charAt(matchPos);
								if(ch==')'){parenLevel++;}
								else if(ch==']'){bracketLevel++;}
								else if(ch=='('){parenLevel--;}
								else if(ch=='['){bracketLevel--;}
								if(parenLevel==0 && bracketLevel==0){found=true;}
							}
							if(found==true){
								doc.setCharacterAttributes(pos-1, 1, pane.getStyle("parens"),true); 
								doc.setCharacterAttributes(matchPos, 1, pane.getStyle("parens"),true);
							}
						}
						else if(right=='(' || right=='['){ //find closing bracket to right
							int matchPos=pos;
							int parenLevel=0, bracketLevel=0;
							if(right=='('){parenLevel=1;}
							else{bracketLevel=1;}
							boolean found=false;
							while(found==false && matchPos<textLength-1){
								matchPos++;
								char ch=text.charAt(matchPos);
								if(ch=='('){parenLevel++;}
								else if(ch=='['){bracketLevel++;}
								else if(ch==')'){parenLevel--;}
								else if(ch==']'){bracketLevel--;}
								if(parenLevel==0 && bracketLevel==0){found=true;}
							}
							if(found==true){
								doc.setCharacterAttributes(pos, 1, pane.getStyle("parens"),true); 
								doc.setCharacterAttributes(matchPos, 1, pane.getStyle("parens"),true);
							}
						}
						else if(right==')' || right==']'){ //find closing bracket to left
							int matchPos=pos+1;
							int parenLevel=0, bracketLevel=0;
							boolean found=false;
							while(found==false && matchPos>0){
								matchPos--;
								char ch=text.charAt(matchPos);
								if(ch==')'){parenLevel++;}
								else if(ch==']'){bracketLevel++;}
								else if(ch=='('){parenLevel--;}
								else if(ch=='['){bracketLevel--;}
								if(parenLevel==0 && bracketLevel==0){found=true;}
							}
							if(found==true){
								doc.setCharacterAttributes(pos, 1, pane.getStyle("parens"),true); 
								doc.setCharacterAttributes(matchPos, 1, pane.getStyle("parens"),true);
							}
						}
					}
				}
			};
			SwingUtilities.invokeLater(doRestyle);
		}catch(Exception e){
			e.printStackTrace();
			//varHelper.errorLog.recordError(e);
		}
	}

	public void restyle(){
		try{
			Runnable doRestyle=new Runnable(){
				@Override
				public void run(){
					String text=pane.getText();
					StyledDocument doc=pane.getStyledDocument();
					doc.setCharacterAttributes(startStyle, text.length(), pane.getStyle("plain"), true); //Reset all to plain text

					//Parse word by word
					text=text.substring(Math.max(0, startStyle)); //ensure >=0
					int offset=startStyle;
					int len=text.length();
					while(len>0){
						int index=Interpreter.getNextBreakIndex(text);
						String word=text.substring(0, index);
						if(isString(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("string"), true);}
						else if(myModel!=null && myModel.isParameter(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("param"), true);}
						else if(myModel!=null && myModel.isVariable(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("var"), true);}
						else if(myModel!=null && myModel.isTable(word)){
							Table curTable=myModel.tables.get(myModel.getTableIndex(word));
							if(curTable.type.matches("Lookup")){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("table"), true);}
							else if(curTable.type.matches("Distribution")){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("dist"), true);}
							else if(curTable.type.matches("Matrix")){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("matrix"), true);}
						}
						else if(Constants.isConstant(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("fx"), true);}
						else if(Functions.isFunction(word) || MatrixFunctions.isFunction(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("fx"), true);}
						else if(Distributions.isDistribution(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("dist"), true);}
						else if(isItalics(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("ital"), true);}
						
						if(index==len){len=0;} //End of word
						else{
							text=text.substring(index+1);
							offset+=index+1;
							len=text.length();
						}
					}
				}
			};
			SwingUtilities.invokeLater(doRestyle);
		}catch(Exception e){
			e.printStackTrace();
			//varHelper.errorLog.recordError(e);
		}
	}

	private boolean isString(String word){
		boolean isStr=false;
		if(word.startsWith("\"") || word.startsWith("\'")){
			if(word.endsWith("\"") || word.endsWith("\'")){
				isStr=true;
			}
		}

		return(isStr);
	}

	private boolean isItalics(String word){
		return(Interpreter.isReservedString(word));
	}
	
	
	public void setStyleStart(int newPos){
		startStyle=Math.max(startStyle, newPos); //ensure >= curPos
	}
	
}