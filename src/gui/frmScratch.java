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

package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.Element;

import base.AmuaModel;
import main.StyledTextPane;
import math.Interpreter;
import math.Numeric;

/**
 *
 */
public class frmScratch {

	public JFrame frmScratch;
	StyledTextPane textConsole;
	AmuaModel myModel;
	int prevCaretPos;
	ArrayList<String> cmdHistory;
	int upCount=0;
	int upPos=0;
	
	/**
	 *  Default Constructor
	 */
	public frmScratch(AmuaModel myModel) {
		cmdHistory=new ArrayList<String>();
		this.myModel=myModel;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmScratch = new JFrame();
			frmScratch.setTitle("Amua - ScratchPad");
			if(myModel!=null){frmScratch.setTitle("Amua - ScratchPad - "+myModel.name);}
			frmScratch.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmScratch.setBounds(100, 100, 1000, 336);
			frmScratch.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0};
			gridBagLayout.rowHeights = new int[]{174, 0};
			gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmScratch.getContentPane().setLayout(gridBagLayout);
			
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 0;
			frmScratch.getContentPane().add(scrollPane, gbc_scrollPane);
			
			textConsole = new StyledTextPane(myModel);
			textConsole.setFont(new Font("Consolas", Font.PLAIN, 15));
			textConsole.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent arg0){
					if(arg0.getButton()==MouseEvent.BUTTON1){ //Left
						int posClick=textConsole.viewToModel(arg0.getPoint());
						Element map = textConsole.getDocument().getDefaultRootElement();
						int row = map.getElementIndex(posClick);
						Element lineElem = map.getElement(row);
						int col = posClick - lineElem.getStartOffset();
						String all=textConsole.getText();
						String lines[]=all.split("\n");
						int numRows=lines.length;
						if(col<3 || row<(numRows-1)){ //disallow
							arg0.consume();
							textConsole.setCaretPosition(prevCaretPos);
						} 
					}
				}
				public void mouseReleased(MouseEvent arg0){
					if(arg0.getButton()==MouseEvent.BUTTON1){ //Left
						int posClick=textConsole.viewToModel(arg0.getPoint());
						Element map = textConsole.getDocument().getDefaultRootElement();
						int row = map.getElementIndex(posClick);
						Element lineElem = map.getElement(row);
						int col = posClick - lineElem.getStartOffset();
						String all=textConsole.getText();
						String lines[]=all.split("\n");
						int numRows=lines.length;
						if(col<3 || row<(numRows-1)){ //disallow
							arg0.consume();
							textConsole.setCaretPosition(prevCaretPos);
						} 
					}
				}
			});
			textConsole.setText("> ");
			textConsole.setCaretPosition(2);
			prevCaretPos=2;
			scrollPane.setViewportView(textConsole);
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
							ex.printStackTrace();
						}catch(Exception ex2){
							ex2.printStackTrace();
						}
					}
				}
			});
			
			
		

		} catch (Exception ex){
			ex.printStackTrace();
			//varHelper.errorLog.recordError(ex);
		}
	}
	
}
