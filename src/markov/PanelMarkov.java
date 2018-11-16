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

package markov;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import base.AmuaModel;
import base.ModelPanel;
import gui.frmMain;
import main.Console;
import main.ErrorLog;
import main.TableCellListener;
import math.Interpreter;

public class PanelMarkov extends ModelPanel{
	public MarkovTree tree;
	public MarkovNode curNode, prevNode;
	
	JMenuItem mntmDecision;
	JMenuItem mntmMarkovChain;
	JMenuItem mntmMarkovState;
	JMenuItem mntmChance;
	JMenuItem mntmTransition;
		
	//markov properties
	DefaultTableModel modelProperties;
	public JTable tableProperties;

	/**
	 * Default constructor
	 */
	public PanelMarkov(frmMain mainFrm, AmuaModel myModel, ErrorLog errorLog){
		super(mainFrm,myModel,errorLog);
		
		tree=new MarkovTree(true);
		tree.myModel=myModel;
		tree.nodes.get(0).setPanel(this);
		
		textAreaNotes.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(curNode!=null){
					curNode.tempNotes=curNode.notes; //Get existing notes on entry
					prevNode=curNode; //Another reference to current node
					notesFocus=true;
				}
			}
			public void focusLost(FocusEvent e){
				if(!e.isTemporary()){
					if(prevNode!=null){
						if(tempNotes!=null && !tempNotes.equals(prevNode.tempNotes)){ //Notes were changed
							saveSnapshot("Edit Notes");
						}
						prevNode.notes=tempNotes;
						notesFocus=false;
						if(curNode!=null){ //If focus switched back to node update notes display
							textAreaNotes.setText(curNode.notes);
						}
					}
				}
			}
		});
		
		
		
		modelProperties=new DefaultTableModel(new Object[][] {}, new String[] {"Name", "Value"}) 
		{boolean[] columnEditables = new boolean[] {false, true};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		};

		tableProperties = new JTable();
		tableProperties.setModel(modelProperties);
		tableProperties.setShowVerticalLines(true);
		tableProperties.setRowSelectionAllowed(false);
		tableProperties.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableProperties.getColumnModel().getColumn(0).setPreferredWidth(150);
		tableProperties.getColumnModel().getColumn(1).setPreferredWidth(150);
		tableProperties.getTableHeader().setReorderingAllowed(false);
		tableProperties.putClientProperty("terminateEditOnFocusLost", true);
		//tableProperties.getColumn("Value").setCellRenderer(new TableCellDisplay(varHelper));
		//tableProperties.getColumn("Value").setCellEditor(new TableCellEditor(varHelper));
		//tableProperties.setRowHeight(18);


		//tableProperties.setCellSelectionEnabled(true);
		/*ListSelectionModel cellSelectionModel = tableProperties.getSelectionModel();
	    cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	    cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	        int row=tableProperties.getSelectedRow();
	        int col=tableProperties.getSelectedColumn();
	        System.out.println(row+","+col);
	        if(curNode.type==1){ //Markov Chain
	        	if(row==0){curNode.tempName=name;}
	        }
	        else if(curNode.type==2){ //Markov State
	        	if(row==0){curNode.tempName=name;}
	        }
	      }
	    });*/

		Action action = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				TableCellListener tcl = (TableCellListener)e.getSource();
				if(tcl.getColumn()==1){
					int row=tcl.getRow();
					String oldVal=(String) tcl.getOldValue();
					String newVal=(String) tcl.getNewValue();
					if(curNode.type==1){ //Markov Chain
						if(row==0){ //name
							curNode.tempName=oldVal;
							curNode.updateName(newVal);
						}
						else if(row==1){ //termination condition
							curNode.tempTermination=oldVal;
							curNode.updateTermination(newVal);
						}
					}
					else if(curNode.type==2){ //Markov State
						if(row==0){ //name
							curNode.tempName=oldVal;
							curNode.updateName(newVal);
						}
						else if(row==1){ //initial probability
							curNode.tempProb=oldVal; //Get existing value on field entry
							curNode.textProb.setText(newVal);
							curNode.textProb.validateEntry();
							curNode.textProb.updateHistory();
						}
						else if(row>2){ //rewards
							int numDim=curNode.rewards.length;
							curNode.tempRewards=new String[numDim];
							String newRewards[]=new String[numDim];
							for(int i=0; i<numDim; i++){
								curNode.tempRewards[i]=curNode.rewards[i];
								newRewards[i]=curNode.rewards[i];
							}
							int curDim=row-3;
							curNode.tempRewards[curDim]=oldVal;
							newRewards[curDim]=newVal;
							
							String dimSymbols[]=curNode.myModel.dimInfo.dimSymbols;
							String buildString="";
							for(int i=0; i<numDim-1; i++){buildString+="("+dimSymbols[i]+") "+newRewards[i]+"; ";}
							buildString+="("+dimSymbols[numDim-1]+") "+newRewards[numDim-1];
							curNode.textRewards.setText(buildString);
							
							curNode.textRewards.validateEntry();
							curNode.textRewards.updateHistory();
							
							
						}
					}
				}
			}
		};
		TableCellListener tcl = new TableCellListener(tableProperties, action);

		/*modelProperties.addTableModelListener(new TableModelListener() {
		      public void tableChanged(TableModelEvent e) {
		    	  if(modelProperties.getRowCount()>0 && e.getType()==TableModelEvent.UPDATE){
		    		  int row=e.getFirstRow();
		    		  String update=(String) modelProperties.getValueAt(row, 1);
		    		  if(curNode.type==1){ //Markov Chain
		    			  if(row==0){curNode.name=update; curNode.textName.setText(curNode.name);}

		    		  }
		    		  else if(curNode.type==2){ //Markov State
		    			  if(row==0){curNode.name=update; curNode.textName.setText(curNode.name);}
		    		  }
		    	  }
		      }
		    });*/

		//Get root
		curNode=tree.nodes.get(0);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				origX=e.getX(); origY=e.getY();
				selectNode(e.getX(),e.getY());
				if(curNode!=null){ //Node selected
					if(e.getButton()==3){ //Right-click
						showPopup(e.getX(),e.getY());
					}
					else if(e.getClickCount()==2){ //Double-click
						if(curNode.type!=4 && curNode.collapsed==false){
							MarkovNode curParent=curNode;
							int numChildren=curParent.childIndices.size();
							if(numChildren==0){ //Add 2 children
								//Add first child and shift
								if(curParent.type==1){addNode(2);} //add state
								else{addNode(3);} //add chance
								moveNode(curNode,curNode.xPos+curNode.scale(5),curNode.yPos-curNode.scale(30));
								curNode=curParent;
								//Add second child and shift
								if(curParent.type==1){addNode(2);} //add state
								else{addNode(3);} //add chance
								moveNode(curNode,curNode.xPos+curNode.scale(5),curNode.yPos+curNode.scale(30));
							}
							else{ //Add 1 child
								if(curParent.type==1){addNode(2);} //add state
								else{addNode(3);} //add chance
							}
						}
					}
				}
			}
			public void mouseReleased(MouseEvent e){
				curPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				if(curNode!=null){
					moveNode(curNode,e.getX(),e.getY());
				}
				else{
					curPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
					//Move panel
					JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, curPanel);
					int deltaX=origX-e.getX();
					int deltaY=origY-e.getY();
					Rectangle view=viewPort.getViewRect();
					view.x+=deltaX;
					view.y+=deltaY;
					curPanel.scrollRectToVisible(view);
				}
			}
		});


		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), "deleteNode");
		this.getActionMap().put("deleteNode", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				deleteNode(true);
			}
		});

		//Pop-up menu
		popup = new JPopupMenu();

		mnAdd = new JMenu("Add");
		popup.add(mnAdd);

		mntmDecision = new JMenuItem("Decision Node");
		mntmDecision.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(0);}});
		mntmDecision.setIcon(new ImageIcon(frmMain.class.getResource("/images/decisionNode_16.png")));
		mnAdd.add(mntmDecision);

		mntmMarkovChain = new JMenuItem("Markov Chain");
		mntmMarkovChain.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(1);}});
		mntmMarkovChain.setIcon(new ImageIcon(frmMain.class.getResource("/images/markovChain_16.png")));
		mnAdd.add(mntmMarkovChain);

		mntmMarkovState = new JMenuItem("Markov State");
		mntmMarkovState.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(2);}});
		mntmMarkovState.setIcon(new ImageIcon(frmMain.class.getResource("/images/markovState_16.png")));
		mnAdd.add(mntmMarkovState);

		mntmChance = new JMenuItem("Chance Node");
		mntmChance.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(3);}});
		mntmChance.setIcon(new ImageIcon(frmMain.class.getResource("/images/chanceNode_16.png")));
		mnAdd.add(mntmChance);

		mntmTransition = new JMenuItem("State Transition");
		mntmTransition.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(4);}});
		mntmTransition.setIcon(new ImageIcon(frmMain.class.getResource("/images/stateTransition_16.png")));
		mnAdd.add(mntmTransition);

		mntmChangeType= new JMenuItem("Change Node Type");
		mntmChangeType.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {changeNodeType();}});
		popup.add(mntmChangeType);

		mntmUpdateVariable = new JMenuItem("Add/Remove Variable Updates");
		mntmUpdateVariable.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addRemoveVarUpdates();}});
		mntmUpdateVariable.setIcon(new ImageIcon(frmMain.class.getResource("/images/updateVariable_16.png")));
		popup.add(mntmUpdateVariable);
		
		mntmShowCost= new JMenuItem("Add/Remove Cost");
		mntmShowCost.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addRemoveCost();}});
		mntmShowCost.setIcon(new ImageIcon(frmMain.class.getResource("/images/cost_16.png")));
		popup.add(mntmShowCost);

		popup.addSeparator();

		final JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {cutSubtree();}});
		mntmCut.setIcon(new ImageIcon(frmMain.class.getResource("/images/cut_16.png")));
		popup.add(mntmCut);

		final JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {copySubtree();}});
		mntmCopy.setIcon(new ImageIcon(frmMain.class.getResource("/images/copy_16.png")));
		popup.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {pasteSubtree();}});
		mntmPaste.setIcon(new ImageIcon(frmMain.class.getResource("/images/paste_16.png")));
		mntmPaste.setEnabled(false);
		popup.add(mntmPaste);

		popup.addSeparator();
		mntmDelete= new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {deleteNode(true);}});
		mntmDelete.setIcon(new ImageIcon(frmMain.class.getResource("/images/delete.png")));
		popup.add(mntmDelete);

		mntmCollapse=new JMenuItem("Collapse Branch");
		mntmCollapse.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {collapseBranch();}});
		popup.add(mntmCollapse);


	}

	/**
	 * Expands canvas if nodes are moved off-screen, or shrinks to default of 1000x1000 if allowed
	 */
	private void resizeCanvas(){
		int maxX=0, maxY=0;
		int size=tree.nodes.size();
		for(int i=0; i<size; i++){ //Skip root
			MarkovNode nodePosition=tree.nodes.get(i);
			maxX=Math.max(maxX, nodePosition.xPos+nodePosition.scale(100));
			if(nodePosition.type==4){
				int width=75;
				if(nodePosition.comboTransition.getSelectedIndex()!=-1){
					width=Math.max((int)(nodePosition.comboTransition.getSelectedItem().toString().length()*7),100);
				}
				maxX=Math.max(maxX, nodePosition.xPos+nodePosition.scale(width));
			}
			maxY=Math.max(maxY, nodePosition.yPos+nodePosition.scale(100));
		}
		canvasWidth=Math.max(maxX, 1000);
		canvasHeight=Math.max(maxY, 1000);
	}

	
	/**
	 * Updates the display
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);       

		for(int i=0; i<tree.nodes.size(); i++){
			tree.nodes.get(i).paintComponent(g);
		}
	}  

	/**
	 * Selects the current node if the mouse is clicked inside the bounds
	 * @param x X-position of the mouse
	 * @param y Y-position of the mouse
	 */
	public void selectNode(int x, int y){
		requestFocusInWindow(); //Remove focus if textfield is selected
		notesFocus=false;
		curNode=null; //Reset selected node to null

		int size=tree.nodes.size();
		for(int i=0; i<size; i++){ //If click was within bounds of an node, select it as the current node
			boolean select=false;
			int minX=tree.nodes.get(i).xPos;
			int maxX=minX+tree.nodes.get(i).width;
			if(x>=minX && x<=maxX){ //Within X bounds
				int minY=tree.nodes.get(i).yPos;
				int maxY=minY+tree.nodes.get(i).height;
				if(y>=minY && y<=maxY){ //Within Y bounds
					curNode=tree.nodes.get(i);
					select=true;
					xOffset=x-curNode.xPos;
					yOffset=y-curNode.yPos;
				}
			}
			tree.nodes.get(i).selected=select;
		}

		modelProperties.setRowCount(0);
		if(curNode!=null){
			refreshPropertiesTable();
			textAreaNotes.setText(curNode.notes);
			textAreaNotes.setEditable(true);
			mainForm.mntmCut.setEnabled(true);
			mainForm.mntmCopy.setEnabled(true);
			mainForm.btnUpdateVariable.setEnabled(false);
			if(curNode.type!=4 && curNode.collapsed==false){
				mainForm.btnDecisionNode.setEnabled(true);
				mainForm.btnMarkovChain.setEnabled(true);
				mainForm.btnChanceNode.setEnabled(true);
			}
			else{
				mainForm.btnDecisionNode.setEnabled(false);
				mainForm.btnMarkovChain.setEnabled(false);
				mainForm.btnChanceNode.setEnabled(false);
			}
			if(curNode.type==1){ //Markov chain
				mainForm.btnDecisionNode.setEnabled(false);
				mainForm.btnMarkovChain.setEnabled(false);
				mainForm.btnMarkovState.setEnabled(true);
				mainForm.btnChanceNode.setEnabled(false);
				mainForm.mntmRunTree.setText("Run Markov Chain");
				mainForm.btnCheckTree.setToolTipText("Check Markov Chain");
				mainForm.btnRunTree.setToolTipText("Run Markov Chain");
			}
			else{ //not Markov chain
				mainForm.btnDecisionNode.setEnabled(true);
				mainForm.btnMarkovChain.setEnabled(true);
				mainForm.btnMarkovState.setEnabled(false);
				mainForm.btnChanceNode.setEnabled(true);
				if(curNode.chain!=null){ //in chain
					mainForm.btnUpdateVariable.setEnabled(true);
				}
				mainForm.mntmRunTree.setText("Run Model");
				mainForm.btnCheckTree.setToolTipText("Check Model");
				mainForm.btnRunTree.setToolTipText("Run Model");
			}
			if(curNode.type==4){ //State transition
				mainForm.btnDecisionNode.setEnabled(false);
				mainForm.btnMarkovChain.setEnabled(false);
				mainForm.btnMarkovState.setEnabled(false);
				mainForm.btnChanceNode.setEnabled(false);
				mainForm.btnStateTransition.setEnabled(false);
			}

			mainForm.btnStateTransition.setEnabled(false);
			mainForm.btnChangeNodeType.setEnabled(false);
			if(curNode.chain!=null){ //only 1 chain allowed per strategy
				mainForm.btnStateTransition.setEnabled(true); //in chain, allow transition
				mainForm.btnChangeNodeType.setEnabled(true);
				mainForm.btnMarkovChain.setEnabled(false);
			}

			int numChildren=curNode.childIndices.size();
			
			mainForm.btnEqualY.setEnabled(false);
			if(curNode.type<3 || numChildren>0){mainForm.btnChangeNodeType.setEnabled(false);}
			if(numChildren==0){mainForm.btnCollapse.setEnabled(false);}
			else{
				mainForm.btnCollapse.setEnabled(true);
				if(numChildren>2){mainForm.btnEqualY.setEnabled(true);}
			}
			if(curNode.level==0 || curNode.type==2){mainForm.btnShowCost.setEnabled(false);}
			else{mainForm.btnShowCost.setEnabled(true);}
		}
		else{ //Node not selected
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);
			mainForm.btnDecisionNode.setEnabled(false);
			mainForm.btnMarkovChain.setEnabled(false);
			mainForm.btnMarkovState.setEnabled(false);
			mainForm.btnChanceNode.setEnabled(false);
			mainForm.btnStateTransition.setEnabled(false);
			mainForm.btnChangeNodeType.setEnabled(false);
			mainForm.btnUpdateVariable.setEnabled(false);
			mainForm.btnShowCost.setEnabled(false);
			mainForm.btnEqualY.setEnabled(false);
			mainForm.btnCollapse.setEnabled(false);
			mainForm.mntmCut.setEnabled(false);
			mainForm.mntmCopy.setEnabled(false);
			mainForm.mntmRunTree.setText("Run Model");
			mainForm.btnCheckTree.setToolTipText("Check Model");
			mainForm.btnRunTree.setToolTipText("Run Model");
		}

		repaint();
	}

	/**
	 * Shifts selected node and all children mode to current mouse position
	 * @param x X-position of the mouse
	 * @param y Y-position of the mouse
	 */

	public void moveNode(MarkovNode curNode, int x, int y){
		// Current node state, stored as final variables 
		// to avoid repeat invocations of the same methods.
		final int CURR_X = curNode.xPos;
		final int CURR_Y = curNode.yPos;

		if ((CURR_X!=x) || (CURR_Y!=y)) {
			int newX=x-xOffset;
			int newY=y-yOffset;
			int xShift=newX-curNode.xPos;
			int yShift=newY-curNode.yPos;

			curNode.move(xShift,yShift,tree.nodes);
			resizeCanvas();
			revalidate();
			repaint();
		}
	}

	/**
	 * Adds a child node to the selected node
	 * @param type Type of node to be added
	 */
	public void addNode(int type){
		if(curNode!=null && curNode.type!=4){
			saveSnapshot("Add Node");
			int size=tree.nodes.size();
			tree.nodes.add(new MarkovNode(type,curNode));
			curNode.childIndices.add(size);
			curNode=tree.nodes.get(size);
			selectNode(curNode.xPos, curNode.yPos);
			curNode.setPanel(this);
			this.add(curNode.textName);
			if(curNode.parentType!=0){
				this.add(curNode.lblProb); this.add(curNode.textProb);
			}
			if(type!=2){ //Not state
				this.add(curNode.lblCost); this.add(curNode.textCost);
			}
			if(type==4){ //Transition
				this.add(curNode.comboTransition);
			}
			if(type==1){ //Chain
				this.add(curNode.textTermination);
				this.add(curNode.textEV);
			}
			else{ //not chain
				this.add(curNode.lblVarUpdates); this.add(curNode.textVarUpdates);
			}
			if(type==2){ //State
				this.add(curNode.lblRewards); this.add(curNode.textRewards);
			}
			if(curNode.level==1){this.add(curNode.textICER);}
		}
	}

	private void deleteNode(boolean allowUndo){
		if(curNode!=null){
			//Check to see if it is a parent
			if(curNode.childIndices.size()>0){
				if(allowUndo){saveSnapshot("Delete Subtree");}
				removeSubtree(curNode);
			}
			else{
				if(allowUndo){saveSnapshot("Delete Node");}

				int curIndex=tree.nodes.indexOf(curNode);
				//Remove node
				this.remove(curNode.textName);
				if(curNode.parentType!=0){
					this.remove(curNode.lblProb); this.remove(curNode.textProb);
				}
				if(curNode.type!=2){ //not state
					this.remove(curNode.lblCost); this.remove(curNode.textCost);
				}
				else{ //state
					if(curNode.chain.stateNames.contains(curNode.name)){
						curNode.chain.stateNames.remove(curNode.name);
					}
					this.remove(curNode.lblRewards); this.remove(curNode.textRewards);
				}
				if(curNode.type==1){ //Chain
					this.remove(curNode.textTermination);
					this.remove(curNode.textEV);
				}
				else{ //not chain
					this.remove(curNode.lblVarUpdates); this.remove(curNode.textVarUpdates);
				}
				if(curNode.type==4){
					this.remove(curNode.comboTransition);
				}
				if(curNode.level==1){
					this.remove(curNode.textICER);
				}

				tree.nodes.remove(curIndex);
				//Update child indices
				ArrayList<Integer> curList=new ArrayList<Integer>();
				curList.add(curIndex);
				for(int i=0; i<tree.nodes.size(); i++){
					//Remove all references to cur index
					tree.nodes.get(i).childIndices.removeAll(curList);
					//Update indices > curIndex
					for(int j=0; j<tree.nodes.get(i).childIndices.size(); j++){
						int testIndex=tree.nodes.get(i).childIndices.get(j);
						if(testIndex>curIndex){
							testIndex--;
							tree.nodes.get(i).childIndices.set(j, testIndex);
						}
					}
				} //End loop
				repaint();
			}
		}
	}

	public void changeNodeType(){
		if(curNode!=null){
			if(curNode.type==3){ //Chance->Transition
				saveSnapshot("Change Node Type");
				curNode.type=4;
				curNode.displayTransition();
				this.add(curNode.comboTransition);
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
			else if(curNode.type==4){ //Transition->Chance
				saveSnapshot("Change Node Type");
				curNode.type=3;
				this.remove(curNode.comboTransition);
				curNode.comboTransition=null;
				curNode.transition="0";
				curNode.displayEV();
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
		}
	}

	public void addRemoveVarUpdates(){
		if(curNode!=null && curNode.chain!=null && curNode.type!=1){ //has chain but not chain root
			if(curNode.hasVarUpdates){saveSnapshot("Remove Variable Updates");}
			else{saveSnapshot("Add Variable Updates");}
			curNode.addRemoveVarUpdates();
		}
	}
	
	public void addRemoveCost(){
		if(curNode!=null && curNode.type!=2 && curNode.level!=0){ //not state
			if(curNode.hasCost){saveSnapshot("Remove Cost");}
			else{saveSnapshot("Add Cost");}
			curNode.addRemoveCost();
		}
	}

	public void copySubtree(){
		if(curNode!=null){
			mainForm.clipboard.clear();
			mainForm.clipboard.copyModel=myModel;
			mainForm.clipboard.copyMarkov=tree.copySubtree(curNode);
			mainForm.clipboard.cut=false;
			mntmPaste.setEnabled(true);
			mainForm.mntmPaste.setEnabled(true);
		}
	}

	public void cutSubtree(){
		if(curNode!=null){
			saveSnapshot("Cut");
			mainForm.clipboard.clear();
			mainForm.clipboard.copyModel=myModel;
			mainForm.clipboard.copyMarkov=tree.copySubtree(curNode);
			mainForm.clipboard.cut=true;
			removeSubtree(curNode);
			mntmPaste.setEnabled(true);
			mainForm.mntmPaste.setEnabled(true);
		}
	}

	public void pasteSubtree(){
		if(curNode!=null && mainForm.clipboard.copyMarkov!=null && curNode.type!=4 && curNode.collapsed==false){
			//Check if dimensions are compatible
			boolean compatible=mainForm.clipboard.isCompatible(myModel);
			if(compatible==false){
				JOptionPane.showMessageDialog(this, "Incompatible dimensions!");
			}
			//Check if Markov nodes are compatible
			if(compatible){
				int subrootType=mainForm.clipboard.copyMarkov.nodes.get(0).type;
				if(curNode.type==1 && subrootType!=2){
					compatible=false;
					JOptionPane.showMessageDialog(this, "Incompatible subtree!");
				}
				else if(curNode.type!=1 && subrootType==2){
					compatible=false;
					JOptionPane.showMessageDialog(this, "Incompatible subtree!");
				}
				else if(curNode.chain!=null && subrootType==1){
					compatible=false;
					JOptionPane.showMessageDialog(this, "Incompatible subtree!");
				}
			}

			if(compatible){ //Paste
				saveSnapshot("Paste");
				this.setVisible(false); //Don't paint anything until updated

				//Paste
				MarkovNode parent=curNode;
				MarkovNode copySubroot=mainForm.clipboard.copyMarkov.nodes.get(0);
				MarkovNode pasteSubroot=copySubroot.copy();
				if(parent.type==1 && pasteSubroot.type==2){ //copying state
					String testName=pasteSubroot.name;
					if(parent.stateNames.contains(testName)){
						int testNum=2;
						testName=pasteSubroot.name+"_"+testNum;
						while(parent.stateNames.contains(testName)){
							testNum++;
							testName=pasteSubroot.name+"_"+testNum;
						}
					}
					
					pasteSubroot.name=testName;
					parent.stateNames.add(testName);
					pasteSubroot.chain=parent; //update chain
				}
								
				int xDiff=(parent.xPos-pasteSubroot.parentX);
				int yDiff=(parent.yPos-pasteSubroot.parentY);
				int levelDiff=(parent.level+1)-pasteSubroot.level;
				int origSize=tree.nodes.size();
				//Add subroot to tree
				tree.nodes.add(pasteSubroot);
				parent.childIndices.add(origSize);
				pasteSubroot.parentType=parent.type;
				pasteSubroot.xPos+=xDiff+parent.width; pasteSubroot.yPos+=(yDiff+(parent.height/2)+pasteSubroot.scale(50));
				pasteSubroot.level+=levelDiff;
				pasteSubroot.parentX=parent.xPos+parent.width; pasteSubroot.parentY=parent.yPos+(parent.height/2);
				for(int j=0; j<copySubroot.childIndices.size(); j++){
					int index=copySubroot.childIndices.get(j); //Index in subtree
					index+=origSize; //Appended to orig tree
					//subroot.childIndices.set(j, index);
					pasteSubroot.childIndices.add(index);
				}
				updateNodeDisplay(pasteSubroot);
				//Add children			
				for(int i=1; i<mainForm.clipboard.copyMarkov.nodes.size(); i++){
					MarkovNode copyNode= mainForm.clipboard.copyMarkov.nodes.get(i);
					MarkovNode pasteNode=copyNode.copy();
					tree.nodes.add(pasteNode);
					pasteNode.xPos+=xDiff+parent.width; pasteNode.yPos+=(yDiff+(parent.height/2)+pasteNode.scale(50));
					pasteNode.parentX+=xDiff+parent.width; pasteNode.parentY+=(yDiff+(parent.height/2)+pasteNode.scale(50));
					pasteNode.level+=levelDiff;
					//Update child indices
					int numChildren=copyNode.childIndices.size();
					for(int j=0; j<numChildren; j++){
						int index=copyNode.childIndices.get(j); //Index of child in subtree
						index+=origSize; //Appended to orig tree
						//copyNode.childIndices.set(j, index);
						pasteNode.childIndices.add(index);
					}
					updateNodeDisplay(pasteNode);
				}
				//Update markov chain pointers for pasted nodes
				tree.updateMarkovChain(parent);
				
				if(mainForm.clipboard.cut==true){
					mainForm.clipboard.clear(); //Clear clipboard
					mntmPaste.setEnabled(false);
					mainForm.mntmPaste.setEnabled(false);
				}

				resizeCanvas();
				
				this.setVisible(true);
			}
		}
	}

	public void collapseBranch(){
		if(curNode!=null && curNode.childIndices.size()>0){
			//Flip boolean on child nodes (recursively)
			curNode.collapsed=!curNode.collapsed;
			int numChildren=curNode.childIndices.size();
			for(int i=0; i<numChildren; i++){
				tree.nodes.get(curNode.childIndices.get(i)).showNode(!curNode.collapsed, tree.nodes);
			}
			//Indicate collapsed/expanded node
			if(curNode.collapsed){
				curNode.displayCollapsed();
				this.add(curNode.lblCollapsed);
			}
			else{
				this.remove(curNode.lblCollapsed);
				curNode.lblCollapsed=null;
			}

			repaint();
		}
	}


	private void updateNodeDisplay(MarkovNode node){
		node.setPanel(this);
		node.updateDisplay();
		this.add(node.textName);
		if(node.parentType!=0){
			this.add(node.lblProb); this.add(node.textProb);
		}
		if(node.type!=2){ //Not state
			this.add(node.lblCost); this.add(node.textCost);
		}
		else{ //state
			this.add(node.lblRewards); this.add(node.textRewards);
		}
		if(node.type==1){ //Chain
			this.add(node.textTermination);
			this.add(node.textEV);
			node.textEV.setVisible(tree.showEV);
		}
		else{ //not chain
			this.add(node.lblVarUpdates); this.add(node.textVarUpdates);
		}
		if(node.type==4){ //Transition
			this.add(node.comboTransition);
		}
		if(node.level==1){
			this.add(node.textICER);
			node.textICER.setVisible(tree.showEV && myModel.dimInfo.analysisType>0);
		}
		if(node.collapsed){this.add(node.lblCollapsed);}
		if(node.selected){textAreaNotes.setText(node.notes);}
	}

	public void alignNodeY(MarkovNode curNode){
		int numChildren=curNode.childIndices.size();
		if(numChildren==0){
			moveNode(curNode,curNode.xPos,maxY);
			if(curNode.hasVarUpdates==false){maxY+=curNode.scale(60);}
			else{maxY+=curNode.scale(80);} //give extra space
		}
		else{
			int totalY=0;
			boolean childChain=false;
			for(int c=0; c<numChildren; c++){
				MarkovNode child=tree.nodes.get(curNode.childIndices.get(c));
				if(child.type==1){childChain=true;}
				alignNodeY(child);
				totalY+=child.yPos;
			}
			totalY=totalY/(numChildren);
			if(curNode.type==1){totalY+=curNode.scale(5);} //Chain
			else if(curNode.type==2){totalY-=curNode.scale(10);} //State
			else if(childChain==true){totalY+=curNode.scale(5);} //parent of chain
			curNode.yPos=totalY;//Move node
			//Update parent position for children
			totalY+=(curNode.height/2); //Midpoint
			for(int c=0; c<numChildren; c++){tree.nodes.get(curNode.childIndices.get(c)).parentY=totalY;}
		}
	}
	
	/**
	 * Aligns all nodes
	 */
	public void ocd(String actionName,boolean alignRight){ //Align all nodes
		xOffset=0; yOffset=0;
		saveSnapshot(actionName);
		//Align x
		int size=tree.nodes.size();
		int maxLevel=0;
		int minY=0;
		//Default to alignBelow
		for(int i=0; i<size; i++){
			MarkovNode curNode=tree.nodes.get(i);
			int curX=(curNode.level*curNode.scale(185))+curNode.scale(50);
			if(curNode.type==2){ //State
				curX=(curNode.level*curNode.scale(185)-curNode.scale(50));
			}
			maxLevel=Math.max(maxLevel, tree.nodes.get(i).level);
			moveNode(curNode,curX,curNode.yPos);
			minY=Math.min(minY, curNode.yPos);
		}
		if(alignRight){
			for(int i=0; i<size; i++){
				MarkovNode curNode=tree.nodes.get(i);
				if(curNode.type==4){ //Align terminal nodes
					int curX=(maxLevel*curNode.scale(185))+curNode.scale(50);
					moveNode(curNode,curX,curNode.yPos);
				}
			}
		}

		MarkovNode root=tree.nodes.get(0);
		if(minY<0){ //Ensure all nodes are visible
			root=tree.nodes.get(0);
			moveNode(root,root.xPos,root.yPos+(25-minY)); //Shift root
		}

		maxY=root.scale(25);
		alignNodeY(root);
		repaint();
	}

	public void equalY(){
		if(curNode!=null){
			xOffset=0; yOffset=0;
			int numChildren=curNode.childIndices.size();
			if(numChildren>2){
				saveSnapshot("Vertical Spacing");
				MarkovNode curChild=tree.nodes.get(curNode.childIndices.get(0));
				int curY=curChild.yPos, minY=curY, maxY=curY;
				ArrayList<Integer> listY=new ArrayList<Integer>();
				listY.add(curY);
				for(int c=1; c<numChildren; c++){
					curY=tree.nodes.get(curNode.childIndices.get(c)).yPos;
					minY=Math.min(minY, curY);
					maxY=Math.max(maxY, curY);
					listY.add(curY);
				}

				int spaceY=(maxY-minY)/(numChildren-1);
				Collections.sort(listY);
				int indexOrder[]=new int[numChildren];
				//Build index
				for(int c=0; c<numChildren; c++){
					curChild=tree.nodes.get(curNode.childIndices.get(c));
					indexOrder[c]=listY.indexOf(curChild.yPos);
				}
				int parentY=(minY+maxY)/2; //Ensure parent node is centered
				curNode.yPos=parentY;//Ensure parent node is centered
				parentY+=(curNode.height/2);//Update parent position for children
				//Space children
				for(int c=0; c<numChildren; c++){
					curChild=tree.nodes.get(curNode.childIndices.get(c));
					moveNode(curChild,curChild.xPos,minY+(spaceY*indexOrder[c]));
					curChild.parentY=parentY;
				}
			}
			repaint();
		}
	}


	public boolean checkModel(Console console,boolean checkProbs){
		mainForm.tabbedPaneBottom.setSelectedIndex(0);
		//console.setText("");
		ArrayList<String> errors=tree.parseTree();
		if(errors.size()==0){
			if(console!=null){console.print("Model checked!\n"); console.newLine();}
			return(true);
		}
		else{
			if(console!=null){
				console.print(errors.size()+" errors:\n");
				for(int i=0; i<errors.size(); i++){console.print(errors.get(i)+"\n");}
				console.newLine();
			}
			return(false);
		}
	}

	public boolean checkChain(Console console,MarkovNode chainRoot,boolean checkProbs){
		mainForm.tabbedPaneBottom.setSelectedIndex(0);
		//console.setText("");
		ArrayList<String> errors=tree.parseChain(chainRoot);
		if(errors.size()==0){
			if(console!=null){console.print("Markov Chain "+chainRoot.name+" checked!\n"); console.newLine();}
			return(true);
		}
		else{
			if(console!=null){
				console.print(errors.size()+" errors:\n");
				for(int i=0; i<errors.size(); i++){console.print(errors.get(i)+"\n");}
				console.newLine();
			}
			return(false);
		}
	}

	public void openTree(MarkovTree tree1){
		try{
			this.tree=tree1;
			tree.myModel=myModel;
			rescalePanel=false; //Turn off re-scaling
			mainForm.setZoomVal(myModel.scale);
			rescalePanel=true;
			paneFormula.myModel=myModel;

			this.setVisible(false); //Don't paint anything until updated
			//clearAll();
			paneFormula.setText("");
			paneFormula.setEditable(false);
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);

			//Add new tree
			//Initialize root
			MarkovNode root=tree.nodes.get(0);
			root.setPanel(this);
			root.cost=new String[myModel.dimInfo.dimNames.length];
			root.numDimensions=myModel.dimInfo.dimNames.length;
			int numChildren=root.childIndices.size();
			for(int i=0; i<numChildren; i++){ //Skip root
				int index=root.childIndices.get(i);
				MarkovNode node=tree.nodes.get(index);
				openNode(node);
			}
			this.setVisible(true); //Show updated panel
			rescale(myModel.scale); //Refresh

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void openNode(MarkovNode node){
		node.setPanel(this);
		if(node.type==1){node.chain=node;} //set chain
		node.curScale=myModel.scale;
		node.numDimensions=myModel.dimInfo.dimNames.length;
		updateNodeDisplay(node);
		int numChildren=node.childIndices.size();
		for(int i=0; i<numChildren; i++){
			int index=node.childIndices.get(i);
			MarkovNode child=tree.nodes.get(index);
			child.chain=node.chain; //pass chain reference
			openNode(child);
		}
	}

	private void removeSubtree(MarkovNode subroot){
		while(subroot.childIndices.size()>0){
			removeSubtree(tree.nodes.get(subroot.childIndices.get(0)));
		}
		curNode=subroot;
		deleteNode(false); //Don't allow undo of each individual node deletion
	}

	private void showPopup(int x, int y){
		mnAdd.setEnabled(true);
		//mntmDelete.setEnabled(true);
		mntmShowCost.setEnabled(true);
		mntmDecision.setEnabled(true);
		mntmMarkovChain.setEnabled(true);
		mntmTransition.setEnabled(false);
		mntmChangeType.setEnabled(false);
		mntmUpdateVariable.setEnabled(false);
		if(curNode.chain!=null){ //in chain
			mntmMarkovChain.setEnabled(false);
			mntmTransition.setEnabled(true);
			mntmChangeType.setEnabled(true);
			if(curNode.type!=1){ //not chain root
				mntmUpdateVariable.setEnabled(true);
			}
		}
		mntmMarkovState.setEnabled(false);
		mntmChance.setEnabled(true);
		if(curNode.type==0){
			mntmChangeType.setEnabled(false);
			mntmChangeType.setText("Change Node Type");
			mntmChangeType.setIcon(null);
			if(mainForm.clipboard.copyMarkov!=null){mntmPaste.setEnabled(true);}
			else{mntmPaste.setEnabled(false);}
		}
		else if(curNode.type==1){
			mntmChangeType.setEnabled(false);
			mntmChangeType.setText("Change Node Type");
			mntmChangeType.setIcon(null);
			mntmDecision.setEnabled(false);
			mntmMarkovChain.setEnabled(false);
			mntmMarkovState.setEnabled(true);
			mntmChance.setEnabled(false);
		}
		else if(curNode.type==2){
			mntmChangeType.setEnabled(false);
			mntmChangeType.setText("Change Node Type");
			mntmChangeType.setIcon(null);
			mntmShowCost.setEnabled(false);
		}
		else if(curNode.type==3){
			mntmChangeType.setText("Change to State Transition");
			mntmChangeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/stateTransition_16.png")));
			if(mainForm.clipboard.copyMarkov!=null){mntmPaste.setEnabled(true);}
			else{mntmPaste.setEnabled(false);}
		}
		else if(curNode.type==4){
			mnAdd.setEnabled(false);
			mntmChangeType.setText("Change to Chance Node");
			mntmChangeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/chanceNode_16.png")));
			mntmPaste.setEnabled(false);
		}
		if(curNode.childIndices.size()>0){
			mntmChangeType.setEnabled(false);
			mntmCollapse.setEnabled(true);
			if(curNode.collapsed){
				mntmCollapse.setText("Expand branch");
				mnAdd.setEnabled(false);
			}
			else{mntmCollapse.setText("Collapse branch");}
		}
		else{
			mntmCollapse.setEnabled(false);
		}
		if(curNode.hasCost){mntmShowCost.setText("Remove Cost");}
		else{mntmShowCost.setText("Add Cost");}
		if(curNode.hasVarUpdates){mntmUpdateVariable.setText("Remove Variable Updates");}
		else{mntmUpdateVariable.setText("Add Variable Updates");}

		if(curNode.level==0){mntmShowCost.setEnabled(false);}
		popup.show(this,x,y);
	}

	public void clearAnnotations(){ //Removes expected values and any highlighting
		tree.showEV=false;
		int size=tree.nodes.size();
		for(int i=0; i<size; i++){
			curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Termination
			curNode.highlightTextField(3, null); //Rewards
			curNode.highlightTextField(4, null); //Variable updates
			if(curNode.type==1){ //chain
				curNode.textEV.setVisible(false);
			}
			else if(curNode.type==4){
				curNode.comboTransition.setBackground(new Color(0,0,0,0));
				curNode.comboTransition.setBorder(null);
			}
			if(curNode.level==1){
				curNode.textICER.setVisible(false);
			}
		}
	}

	public void clearAll(){
		int size=tree.nodes.size();
		//Remove anything currently on the panel
		for(int i=1; i<size; i++){ //Skip root
			curNode=tree.nodes.get(i);
			//Remove node
			if(curNode.textName!=null){
				this.remove(curNode.textName);
			}
			if(curNode.parentType!=0){
				if(curNode.lblProb!=null){this.remove(curNode.lblProb);}
				if(curNode.textProb!=null){this.remove(curNode.textProb);}
			}
			if(curNode.type!=2){ //Not state and not transition
				if(curNode.lblCost!=null){this.remove(curNode.lblCost);}
				if(curNode.textCost!=null){this.remove(curNode.textCost);}
			}
			if(curNode.type==4){
				this.remove(curNode.comboTransition);
			}
			if(curNode.type==1){ //chain
				this.remove(curNode.textTermination);
				this.remove(curNode.textEV);
			}
			else{ //not chain
				if(curNode.lblVarUpdates!=null){this.remove(curNode.lblVarUpdates);} 
				if(curNode.textVarUpdates!=null){this.remove(curNode.textVarUpdates);}
			}
			if(curNode.type==2){ //state
				this.remove(curNode.lblRewards); this.remove(curNode.textRewards);
			}
			if(curNode.level==1){
				this.remove(curNode.textICER);
			}
			if(curNode.collapsed){this.remove(curNode.lblCollapsed);}
		}
	}

	//Revert to previous state
	public void revert(){
		int size=tree.nodes.size();
		for(int i=1; i<size; i++){ //Skip root
			updateNodeDisplay(tree.nodes.get(i));
		}
		this.setVisible(true); //Show updated panel
	}

	public void rescale(int newScale){
		if(rescalePanel){
			int origScale=myModel.scale;
			myModel.scale=newScale;
			double adjust=myModel.scale/(origScale*1.0);


			this.setVisible(false); //Don't paint anything until updated
			clearAll();
			int size=tree.nodes.size();

			//Re-scale nodes
			for(int i=0; i<size; i++){
				MarkovNode curNode=tree.nodes.get(i);
				curNode.curScale=myModel.scale;
				if(curNode.type==1){ //Markov Chain
					curNode.width=curNode.scale(30); curNode.height=curNode.scale(30);
				}
				else if(curNode.type==2){ //Markov State
					curNode.width=curNode.scale(120); curNode.height=curNode.scale(40);
				}
				else{
					curNode.width=curNode.scale(20); curNode.height=curNode.scale(20);
				}
				curNode.xPos*=adjust; curNode.yPos*=adjust;
				//update parent position for children
				int numChildren=curNode.childIndices.size();
				for(int c=0; c<numChildren; c++){
					MarkovNode child=tree.nodes.get(curNode.childIndices.get(c));
					child.parentX=curNode.xPos+curNode.width; //Right
					child.parentY=curNode.yPos+(curNode.height/2); //Middle
				}

				if(i>0){updateNodeDisplay(curNode);} //Skip root

			}
			this.setVisible(true); //Show updated panel
			resizeCanvas();
			revalidate();
			repaint();
		}
	}

	private boolean nodesOverlap(MarkovNode node1, MarkovNode node2){
		boolean overlap=false;
		if(node1.xPos==node2.xPos){
			int top1=node1.yPos;
			int top2=node2.yPos;
			if(top2>=top1 && top2<node1.yPos+node1.scale(60)){
				overlap=true;
				moveNode(node2,node2.xPos,node1.yPos+node1.scale(60));
			}
			else if(top1>=top2 && top1<node2.yPos+node2.scale(60)){
				overlap=true;
				moveNode(node1,node1.xPos,node2.yPos+node2.scale(60));
			}
		}
		return(overlap);
	}
	
	public void refreshPropertiesTable(){
		modelProperties.setRowCount(0);
		if(curNode.type==1){ //Markov chain
			modelProperties.addRow(new Object[]{"Markov Chain Name",curNode.name});
			modelProperties.addRow(new Object[]{"Termination Condition",curNode.terminationCondition});
		}
		if(curNode.type==2){ //Markov state
			modelProperties.addRow(new Object[]{"Markov State Name",curNode.name});
			modelProperties.addRow(new Object[]{"Initial Probability",curNode.prob});
			modelProperties.addRow(new Object[]{"Cycle Rewards",""});
			for(int d=0; d<curNode.numDimensions; d++){
				modelProperties.addRow(new Object[]{myModel.dimInfo.dimNames[d],curNode.rewards[d]});
			}
		}
	}
	
	public void highlightParameter(int paramNum){
		String paramName=myModel.parameters.get(paramNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			MarkovNode curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Payoff

			if(curNode.prob!=null && Interpreter.containsWord(paramName, curNode.prob)){
				curNode.highlightTextField(0, Color.GREEN); //Prob
			}
			if(curNode.cost!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(paramName, curNode.cost[d])){
						curNode.highlightTextField(1, Color.GREEN); //Cost
					}
				}
			}
		}
	}

	public void highlightVariable(int varNum){
		String varName=myModel.variables.get(varNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			MarkovNode curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Payoff

			if(curNode.prob!=null && Interpreter.containsWord(varName, curNode.prob)){
				curNode.highlightTextField(0, Color.GREEN); //Prob
			}
			if(curNode.cost!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(varName, curNode.cost[d])){
						curNode.highlightTextField(1, Color.GREEN); //Cost
					}
				}
			}
		}
	}

	public void highlightTable(int tableNum){
		String tableName=myModel.tables.get(tableNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			MarkovNode curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Payoff

			if(curNode.prob!=null && Interpreter.containsWord(tableName, curNode.prob)){
				curNode.highlightTextField(0, Color.GREEN); //Prob
			}
			if(curNode.cost!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(tableName, curNode.cost[d])){
						curNode.highlightTextField(1, Color.GREEN); //Cost
					}
				}
			}
		}
	}

	
}