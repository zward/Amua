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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StyledTextPane extends JTextPane{
	StyledTextPane pane;	
	VarHelper varHelper;
	int defaultDismiss; //for tooltips

	//Constructor
	public StyledTextPane(VarHelper varHelper){
		pane=this;
		this.varHelper=varHelper;
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

		Style styleVar=this.addStyle("var", null);
		Style styleFx=this.addStyle("fx", null);
		Style styleDist=this.addStyle("dist", null);
		Style styleItal=this.addStyle("ital", null);
		Style stylePlain=this.addStyle("plain", null);

		StyleConstants.setForeground(styleVar, Color.BLUE);
		StyleConstants.setBold(styleVar, true);
		StyleConstants.setForeground(styleFx, new Color(128,0,0));
		StyleConstants.setBold(styleFx, true);
		StyleConstants.setForeground(styleDist, new Color(0,128,0));
		StyleConstants.setBold(styleDist, true);
		StyleConstants.setItalic(styleItal, true);
		StyleConstants.setBold(styleItal, true);
		StyleConstants.setForeground(stylePlain, Color.BLACK);
	}

	@Override
	public String getToolTipText(MouseEvent event){
		String tip=null;
		//Get location of mouse
		int offset=this.viewToModel(event.getPoint());
		String text=pane.getText();
		if(offset<text.length()){
			int nextIndex=offset+varHelper.getNextIndex(text.substring(offset));
			int prevIndex=offset;
			String curChar=text.substring(prevIndex, prevIndex+1);
			while(varHelper.isBreak(curChar)==false && prevIndex>0){
				prevIndex--;
				curChar=text.substring(prevIndex, prevIndex+1);
			}
			if(varHelper.isBreak(curChar)){prevIndex++;}
			if(prevIndex>-1 && nextIndex>-1 && nextIndex>prevIndex){
				String word=text.substring(prevIndex, nextIndex); //Style word if recognized
				if(varHelper.isVariable(word)){tip=varHelper.getVarDescription(word);}
				else if(varHelper.isFunction(word)){tip=varHelper.getFxDescription(word);}
				else if(varHelper.isDistribution(word)){tip=varHelper.getDistDescription(word);}
			}
		}
		return(tip);
	}

	public void restyle(){
		try{
			Runnable doRestyle=new Runnable(){
				@Override
				public void run(){
					String text=pane.getText();
					StyledDocument doc=pane.getStyledDocument();
					doc.setCharacterAttributes(0, text.length(), pane.getStyle("plain"), true); //Reset all to plain text

					//Parse word by word
					int offset=0;
					int len=text.length();
					while(len>0){
						int index=varHelper.getNextIndex(text);
						String word=text.substring(0, index);
						if(varHelper.isVariable(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("var"), true);}
						else if(varHelper.isFunction(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("fx"), true);}
						else if(varHelper.isDistribution(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("dist"), true);}
						else if(varHelper.isItalics(word)){doc.setCharacterAttributes(offset, word.length(), pane.getStyle("ital"), true);}
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
			varHelper.errorLog.recordError(e);
		}
	}
}