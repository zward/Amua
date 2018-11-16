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

package base;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import gui.frmMain;
import main.ErrorLog;
import main.StyledTextPane;

public class ModelPanel extends JPanel{

	public ModelPanel curPanel;
	public AmuaModel myModel;
	public frmMain mainForm;
	
	public JPopupMenu popup;
	public JMenu mnAdd;
	public JMenuItem mntmChangeType;
	public JMenuItem mntmUpdateVariable;
	public JMenuItem mntmShowCost;
	public JMenuItem mntmPaste;
	public JMenuItem mntmDelete;
	public JMenuItem mntmCollapse;
	
	protected int xOffset=0;
	protected int yOffset=0;
	protected int origX, origY;
	protected int maxY=0;

	protected int canvasWidth=1000, canvasHeight=1000; //Default canvas size
	public boolean rescalePanel=true;
	
	public StyledTextPane paneFormula;
	public boolean formulaBarFocus=false;
	
	public JTextArea textAreaNotes;
	protected String tempNotes;
	protected boolean notesFocus=false;
	public ModelTextField curFocus;
	
	public ErrorLog errorLog;
	
	/**
	 * Constructor
	 */
	public ModelPanel(frmMain mainFrm, AmuaModel myModel, ErrorLog errorLog){
		this.curPanel=this;
		this.mainForm=mainFrm;
		this.myModel=myModel;
		this.errorLog=errorLog;
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(Color.WHITE);
		
		paneFormula=new StyledTextPane(myModel);
		paneFormula.setEditable(false);
		paneFormula.setFont(new Font("Consolas", Font.PLAIN,15));
		
		paneFormula.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				formulaBarFocus=true;
			}
			public void focusLost(FocusEvent e){
				if(!e.isTemporary()){
					formulaBarFocus=false;
					if(e.getOppositeComponent()!=curFocus){ //Not switching focus back to textfield
						if(curFocus!=null){
							curFocus.validateEntry();
							curFocus.updateHistory();
						}
						paneFormula.setText("");
						paneFormula.setEditable(false);
					}
				}
			}
		});
		paneFormula.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				formulaBarFocus=true;
			}
			public void focusLost(FocusEvent e){
				if(!e.isTemporary()){
					formulaBarFocus=false;
					if(e.getOppositeComponent()!=curFocus){ //Not switching focus back to textfield
						if(curFocus!=null){
							curFocus.validateEntry();
							curFocus.updateHistory();
						}
						paneFormula.setText("");
						paneFormula.setEditable(false);
					}
				}
			}
		});
		paneFormula.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				//paneFormula.restyle();
				if(formulaBarFocus && curFocus!=null){curFocus.setText(paneFormula.getText());}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(formulaBarFocus && curFocus!=null){curFocus.setText(paneFormula.getText());}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
		paneFormula.addKeyListener(new KeyAdapter(){ //Save text on Enter
			@Override
			public void keyPressed(KeyEvent e){
				int key=e.getKeyCode();
				if(key==KeyEvent.VK_ENTER){
					curPanel.requestFocusInWindow();
				}
			}
		});
		
		textAreaNotes=new JTextArea();
		textAreaNotes.setEditable(false);
		
		textAreaNotes.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(notesFocus){tempNotes=textAreaNotes.getText();}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(notesFocus){tempNotes=textAreaNotes.getText();}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
		});
	}
	
	
	
	/**
	 * Returns the canvas size
	 */
	public Dimension getPreferredSize() {
		return new Dimension(canvasWidth,canvasHeight);
	}
	
	public void saveSnapshot(String action){
		myModel.saveSnapshot(action);
	}
	
	
}