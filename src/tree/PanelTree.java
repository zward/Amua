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

package tree;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import base.AmuaModel;
import base.ModelPanel;
import gui.frmMain;
import main.Console;
import main.ErrorLog;
import main.ScaledIcon;
import math.Interpreter;

public class PanelTree extends ModelPanel{
	public DecisionTree tree;
	TreeNode curNode, prevNode;
	
	/**
	 * Default constructor
	 */
	public PanelTree(frmMain mainFrm, AmuaModel myModel, ErrorLog errorLog){
		super(mainFrm,myModel,errorLog);
		curPanel=this;

		tree=new DecisionTree(true);
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
						if(curNode.type!=2 && curNode.collapsed==false){
							TreeNode curParent=curNode;
							int numChildren=curParent.childIndices.size();
							if(numChildren==0){ //Add 2 children
								//Add first child and shift
								addNode(1);
								moveNode(curNode,curNode.xPos+curNode.scale(5),curNode.yPos-curNode.scale(30));
								curNode=curParent;
								//Add second child and shift
								addNode(1);
								moveNode(curNode,curNode.xPos+curNode.scale(5),curNode.yPos+curNode.scale(30));
							}
							else{ //Add 1 child
								addNode(1);
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

		JMenuItem mntmDecision = new JMenuItem("Decision Node");
		mntmDecision.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(0);}});
		mntmDecision.setIcon(new ScaledIcon("/images/decisionNode",16,16,16,true));
		mntmDecision.setDisabledIcon(new ScaledIcon("/images/decisionNode",16,16,16,false));
		mnAdd.add(mntmDecision);

		JMenuItem mntmChance = new JMenuItem("Chance Node");
		mntmChance.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(1);}});
		mntmChance.setIcon(new ScaledIcon("/images/chanceNode",16,16,16,true));
		mntmChance.setDisabledIcon(new ScaledIcon("/images/chanceNode",16,16,16,false));
		mnAdd.add(mntmChance);

		JMenuItem mntmTerminal = new JMenuItem("Terminal Node");
		mntmTerminal.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(2);}});
		mntmTerminal.setIcon(new ScaledIcon("/images/terminalNode",16,16,16,true));
		mntmTerminal.setDisabledIcon(new ScaledIcon("/images/terminalNode",16,16,16,false));
		mnAdd.add(mntmTerminal);

		mntmChangeType= new JMenuItem("Change Node Type");
		mntmChangeType.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {changeNodeType();}});
		popup.add(mntmChangeType);

		mntmUpdateVariable = new JMenuItem("Add/Remove Variable Updates");
		mntmUpdateVariable.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addRemoveVarUpdates();}});
		mntmUpdateVariable.setIcon(new ScaledIcon("/images/updateVariable",16,16,16,true));
		mntmUpdateVariable.setDisabledIcon(new ScaledIcon("/images/updateVariable",16,16,16,false));
		popup.add(mntmUpdateVariable);
		
		mntmShowCost= new JMenuItem("Add/Remove Cost");
		mntmShowCost.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addRemoveCost();}});
		mntmShowCost.setIcon(new ScaledIcon("/images/cost",16,16,16,true));
		mntmShowCost.setDisabledIcon(new ScaledIcon("/images/cost",16,16,16,false));
		popup.add(mntmShowCost);

		popup.addSeparator();

		final JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {cutSubtree();}});
		mntmCut.setIcon(new ScaledIcon("/images/cut",16,16,16,true));
		mntmCut.setDisabledIcon(new ScaledIcon("/images/cut",16,16,16,false));
		popup.add(mntmCut);

		final JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {copySubtree();}});
		mntmCopy.setIcon(new ScaledIcon("/images/copy",16,16,16,true));
		mntmCopy.setDisabledIcon(new ScaledIcon("/images/copy",16,16,16,false));
		popup.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {pasteSubtree();}});
		mntmPaste.setIcon(new ScaledIcon("/images/paste",16,16,16,true));
		mntmPaste.setDisabledIcon(new ScaledIcon("/images/paste",16,16,16,false));
		mntmPaste.setEnabled(false);
		popup.add(mntmPaste);

		popup.addSeparator();
		mntmDelete= new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {deleteNode(true);}});
		mntmDelete.setIcon(new ScaledIcon("/images/delete",16,16,16,true));
		mntmDelete.setDisabledIcon(new ScaledIcon("/images/delete",16,16,16,false));
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
			TreeNode nodePosition=tree.nodes.get(i);
			maxX=Math.max(maxX, nodePosition.xPos+nodePosition.scale(100));
			if(nodePosition.type==2){
				int width=Math.max((int)(nodePosition.textPayoff.getText().length()*10),100);
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
		int i=size; //start from end to select node displayed on top
		while(curNode==null && i>0){ //If click was within bounds of an node, select it as the current node
			i--;
			boolean select=false;
			TreeNode testNode=tree.nodes.get(i);
			int minX=testNode.xPos;
			int maxX=minX+testNode.width;
			if(x>=minX && x<=maxX){ //Within X bounds
				int minY=testNode.yPos;
				int maxY=minY+testNode.height;
				if(y>=minY && y<=maxY){ //Within Y bounds
					curNode=testNode;
					select=true;
					xOffset=x-curNode.xPos;
					yOffset=y-curNode.yPos;
				}
			}
			testNode.selected=select;
		}
		//de-select any other nodes
		while(i>0){
			i--;
			tree.nodes.get(i).selected=false;
		}
		
		if(curNode!=null){
			textAreaNotes.setText(curNode.notes);
			textAreaNotes.setEditable(true);
			mainForm.mntmCut.setEnabled(true);
			mainForm.mntmCopy.setEnabled(true);
			mainForm.btnUpdateVariable.setEnabled(true);
			if(curNode.type!=2 && curNode.collapsed==false){
				mainForm.btnDecisionNode.setEnabled(true);
				mainForm.btnChanceNode.setEnabled(true);
				mainForm.btnTerminalNode.setEnabled(true);
			}
			else{
				mainForm.btnDecisionNode.setEnabled(false);
				mainForm.btnChanceNode.setEnabled(false);
				mainForm.btnTerminalNode.setEnabled(false);
			}
			int numChildren=curNode.childIndices.size();
			mainForm.btnChangeNodeType.setEnabled(true);
			mainForm.btnEqualY.setEnabled(false);
			if(curNode.type==0 || numChildren>0){mainForm.btnChangeNodeType.setEnabled(false);}
			if(numChildren==0){mainForm.btnCollapse.setEnabled(false);}
			else{
				mainForm.btnCollapse.setEnabled(true);
				if(numChildren>2){mainForm.btnEqualY.setEnabled(true);}
			}
			if(curNode.level==0 || curNode.type==2){mainForm.btnShowCost.setEnabled(false);}
			if(curNode.level==0){mainForm.btnUpdateVariable.setEnabled(false);}
			else{mainForm.btnShowCost.setEnabled(true);}
		}
		else{ //Node not selected
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);
			mainForm.btnDecisionNode.setEnabled(false);
			mainForm.btnChanceNode.setEnabled(false);
			mainForm.btnTerminalNode.setEnabled(false);
			mainForm.btnChangeNodeType.setEnabled(false);
			mainForm.btnUpdateVariable.setEnabled(false);
			mainForm.btnShowCost.setEnabled(false);
			mainForm.btnEqualY.setEnabled(false);
			mainForm.btnCollapse.setEnabled(false);
			mainForm.mntmCut.setEnabled(false);
			mainForm.mntmCopy.setEnabled(false);
		}

		repaint();
	}

	/**
	 * Shifts selected node and all children mode to current mouse position
	 * @param x X-position of the mouse
	 * @param y Y-position of the mouse
	 */

	public void moveNode(TreeNode curNode, int x, int y){
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
		if(curNode!=null && curNode.type!=2){
			saveSnapshot("Add Node");
			int size=tree.nodes.size();
			tree.nodes.add(new TreeNode(type,curNode));
			curNode.childIndices.add(size);
			curNode=tree.nodes.get(size);
			selectNode(curNode.xPos, curNode.yPos);
			curNode.setPanel(this);
			this.add(curNode.textName);
			this.add(curNode.lblVarUpdates); this.add(curNode.textVarUpdates);
			if(curNode.parentType!=0){
				this.add(curNode.lblProb); this.add(curNode.textProb);
			}
			if(type!=2){ //Not terminal
				this.add(curNode.lblCost); this.add(curNode.textCost);
				this.add(curNode.textEV);
			}
			else if(type==2){ //Terminal
				this.add(curNode.textPayoff);
				this.add(curNode.textNumEnd);
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
				//JOptionPane.showMessageDialog(this, "You cannot delete a parent node!");
			}
			else{
				if(allowUndo){saveSnapshot("Delete Node");}

				int curIndex=tree.nodes.indexOf(curNode);
				//Remove node
				this.remove(curNode.textName);
				this.remove(curNode.lblVarUpdates); this.remove(curNode.textVarUpdates);
				if(curNode.parentType!=0){
					this.remove(curNode.lblProb); this.remove(curNode.textProb);
				}
				if(curNode.type!=2){
					this.remove(curNode.lblCost); this.remove(curNode.textCost);
					this.remove(curNode.textEV);
				}
				else if(curNode.type==2){
					this.remove(curNode.textPayoff);
					this.remove(curNode.textNumEnd);
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
			if(curNode.type==1){ //Chance->Terminal
				saveSnapshot("Change Node Type");
				curNode.type=2;
				this.remove(curNode.lblCost); this.remove(curNode.textCost);
				this.remove(curNode.textEV);
				curNode.lblCost=null; curNode.textCost=null;
				curNode.textEV=null;
				Arrays.fill(curNode.cost,"0");
				curNode.displayPayoff();
				this.add(curNode.textPayoff);
				curNode.displayNumEnd();
				this.add(curNode.textNumEnd);
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
			else if(curNode.type==2){ //Terminal->Chance
				saveSnapshot("Change Node Type");
				curNode.type=1;
				this.remove(curNode.textPayoff);
				curNode.textPayoff=null;
				Arrays.fill(curNode.payoff, "0");
				this.remove(curNode.textNumEnd);
				curNode.displayCost();
				curNode.displayEV();
				this.add(curNode.lblCost); this.add(curNode.textCost);
				this.add(curNode.textEV);
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
		}
	}

	public void addRemoveVarUpdates(){
		if(curNode!=null && curNode.level!=0){
			if(curNode.hasVarUpdates){saveSnapshot("Remove Variable Updates");}
			else{saveSnapshot("Add Variable Updates");}
			curNode.addRemoveVarUpdates();
		}
	}
	
	public void addRemoveCost(){
		if(curNode!=null && curNode.type!=2 && curNode.level!=0){
			if(curNode.hasCost){saveSnapshot("Remove Cost");}
			else{saveSnapshot("Add Cost");}
			curNode.addRemoveCost();
		}
	}

	public void copySubtree(){
		if(curNode!=null){
			mainForm.clipboard.clear();
			mainForm.clipboard.copyModel=myModel;
			mainForm.clipboard.copyTree=tree.copySubtree(curNode);
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
			mainForm.clipboard.copyTree=tree.copySubtree(curNode);
			mainForm.clipboard.cut=true;
			removeSubtree(curNode);
			mntmPaste.setEnabled(true);
			mainForm.mntmPaste.setEnabled(true);
		}
	}

	public void pasteSubtree(){
		if(curNode!=null && mainForm.clipboard.copyTree!=null && curNode.type!=2 && curNode.collapsed==false){
			//Check if dimensions are compatible
			boolean compatible=mainForm.clipboard.isCompatible(myModel);
			if(compatible==false){
				JOptionPane.showMessageDialog(this, "Incompatible dimensions!");
			}
			else{ //Paste
				saveSnapshot("Paste");
				this.setVisible(false); //Don't paint anything until updated

				//Paste
				TreeNode parent=curNode;
				TreeNode copySubroot=mainForm.clipboard.copyTree.nodes.get(0);
				TreeNode pasteSubroot=copySubroot.copy();
				int xDiff=(parent.xPos-copySubroot.parentX);
				int yDiff=(parent.yPos-copySubroot.parentY);
				int levelDiff=(parent.level+1)-copySubroot.level;
				int origSize=tree.nodes.size();
				//Add subroot to tree
				tree.nodes.add(pasteSubroot);
				parent.childIndices.add(origSize);
				pasteSubroot.parentType=parent.type;
				pasteSubroot.xPos+=xDiff+parent.width; pasteSubroot.yPos+=(yDiff+(parent.height/2)+pasteSubroot.scale(50));
				pasteSubroot.level+=levelDiff;
				pasteSubroot.parentX=parent.xPos+parent.width; pasteSubroot.parentY=parent.yPos+(parent.height/2);
				for(int j=0; j<copySubroot.childIndices.size(); j++){
					int index=copySubroot.childIndices.get(j); //Original Index in subtree
					index+=origSize; //Appended to orig tree
					//subroot.childIndices.set(j, index);
					pasteSubroot.childIndices.add(index);
				}
				updateNodeDisplay(pasteSubroot);
				//Add children			
				for(int i=1; i<mainForm.clipboard.copyTree.nodes.size(); i++){
					TreeNode copyNode=mainForm.clipboard.copyTree.nodes.get(i);
					TreeNode pasteNode=copyNode.copy();
					tree.nodes.add(pasteNode);
					pasteNode.xPos+=xDiff+parent.width; pasteNode.yPos+=(yDiff+(parent.height/2)+pasteNode.scale(50));
					pasteNode.parentX+=xDiff+parent.width; pasteNode.parentY+=(yDiff+(parent.height/2)+pasteNode.scale(50));
					pasteNode.level+=levelDiff;
					//Update child indices
					int numChildren=copyNode.childIndices.size();
					for(int j=0; j<numChildren; j++){
						int index=copyNode.childIndices.get(j); //Original Index of child in subtree
						index+=origSize; //Appended to orig tree
						//copyNode.childIndices.set(j, index);
						pasteNode.childIndices.add(index);
					}
					updateNodeDisplay(pasteNode);
				}
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
			curNode.collapsed=!curNode.collapsed; //flip status
			//change visibility of children
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


	private void updateNodeDisplay(TreeNode node){
		node.setPanel(this);
		node.updateDisplay();
		this.add(node.textName);
		this.add(node.lblVarUpdates); this.add(node.textVarUpdates);
		if(node.parentType!=0){
			this.add(node.lblProb); this.add(node.textProb);
		}
		if(node.type!=2){ //Not terminal
			this.add(node.lblCost); this.add(node.textCost);
			this.add(node.textEV);
			node.textEV.setVisible(node.visible && tree.showEV);
		}
		else if(node.type==2){ //Terminal
			this.add(node.textPayoff);
			this.add(node.textNumEnd);
			node.textNumEnd.setVisible(node.visible && tree.showEV);
		}
		if(node.level==1){
			this.add(node.textICER);
			node.textICER.setVisible(node.visible && tree.showEV && myModel.dimInfo.analysisType>0);
		}
		if(node.visible && node.collapsed){this.add(node.lblCollapsed);}
		if(node.selected){textAreaNotes.setText(node.notes);}
	}
	
	/**
	 * Aligns all nodes
	 * @param actionName
	 * @param alignRight
	 */
	public void ocd(String actionName, boolean alignRight){
		xOffset=0; yOffset=0;
		saveSnapshot(actionName);
		this.setVisible(false);
		int size=tree.nodes.size();
		int maxLevel=0;
		for(int i=0; i<size; i++){
			TreeNode curNode=tree.nodes.get(i);
			maxLevel=Math.max(maxLevel, curNode.level);
		}
		TreeNode root=tree.nodes.get(0);
		maxY=root.scale(25);
		alignNode(root,alignRight,maxLevel);
		
		resizeCanvas();
		revalidate();
		repaint();
		this.setVisible(true);
	}
	
	private void alignNode(TreeNode node, boolean alignRight, int maxLevel){
		//set x
		int curX=(node.level*node.scale(185))+node.scale(50);
		if(alignRight==true && node.type==2){
			curX=(maxLevel*node.scale(185))+node.scale(50);
		}

		//set y
		int curY=node.xPos;
		int numChildren=node.childIndices.size();
		if(numChildren==0 || node.collapsed){
			curY=maxY;
			if(node.hasVarUpdates==false){maxY+=node.scale(60);}
			else{maxY+=node.scale(80);} //give extra line
		}
		else{ //has visible children
			//update order of child indices based on current position
			Object childY[][]=new Object[numChildren][2]; //[y pos][cur position in child indices]
			for(int c=0; c<numChildren; c++){
				int curIndex=node.childIndices.get(c);
				childY[c][0]=tree.nodes.get(curIndex).yPos;
				childY[c][1]=curIndex;
			}
			Arrays.sort(childY, new Comparator<Object[]>(){
				@Override
				public int compare(final Object[] row1, final Object[] row2){
					Integer y1 = (Integer) row1[0];
					Integer y2 = (Integer) row2[0];
					return y1.compareTo(y2);
				}
			});
			node.childIndices.clear();
			for(int c=0; c<numChildren; c++){ //update order
				node.childIndices.add((Integer) childY[c][1]);
			}

			//adjust Y
			int totalY=0;
			for(int c=0; c<numChildren; c++){
				TreeNode child=tree.nodes.get(node.childIndices.get(c));
				alignNode(child,alignRight,maxLevel);
				totalY+=child.yPos;
			}
			totalY=totalY/(numChildren);
			curY=totalY;
		}

		//set new position
		node.xPos=curX;
		node.yPos=curY;
		//Update parent position for children
		curX+=node.width;
		curY+=(node.height/2); //Midpoint
		for(int c=0; c<numChildren; c++){
			TreeNode child=tree.nodes.get(node.childIndices.get(c));
			child.parentX=curX;
			child.parentY=curY;
		}
	}
	
	public void equalY(){
		if(curNode!=null){
			xOffset=0; yOffset=0;
			int numChildren=curNode.childIndices.size();
			if(numChildren>2){
				saveSnapshot("Vertical Spacing");
				TreeNode curChild=tree.nodes.get(curNode.childIndices.get(0));
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

	
	public boolean checkTree(Console console,boolean checkProbs){
		mainForm.tabbedPaneBottom.setSelectedIndex(0);
		//console.setText("");
		ArrayList<String> errors=tree.parseTree();
		boolean valid=false;
		if(errors.size()==0){
			if(console!=null){
				console.print("Tree checked!\n"); console.newLine();;
			}
			valid=true;
		}
		else{
			if(console!=null){
				console.print(errors.size()+" errors:\n");
				for(int i=0; i<errors.size(); i++){
					console.print(errors.get(i)+"\n");		
				}
				console.newLine();
			}
			valid=false;
		}
		if(myModel.simType==0 && myModel.variables.size()>0){ //Variables in cohort model
			console.print("WARNING: Variables are not evaluated in a Decision Tree cohort simulation!\n");
		}
		
		return(valid);
	}

	public void openTree(DecisionTree tree1){
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
			mainForm.btnFx.setEnabled(false);
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);
			
			//Add new tree
			//Initialize root
			TreeNode root=tree.nodes.get(0);
			root.setPanel(this);
			root.cost=new String[myModel.dimInfo.dimNames.length];
			root.numDimensions=myModel.dimInfo.dimNames.length;
			int size=tree.nodes.size();
			for(int i=1; i<size; i++){ //Skip root
				TreeNode curNode=tree.nodes.get(i);
				curNode.setPanel(this);
				curNode.curScale=myModel.scale;
				curNode.numDimensions=myModel.dimInfo.dimNames.length;
				updateNodeDisplay(tree.nodes.get(i));
			}
			this.setVisible(true); //Show updated panel
			rescale(myModel.scale); //Refresh
			
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void removeSubtree(TreeNode subroot){
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
		mntmUpdateVariable.setEnabled(true);
		if(curNode.type==0){
			mntmChangeType.setEnabled(false);
			mntmChangeType.setText("Change Node Type");
			mntmChangeType.setIcon(null);
			if(mainForm.clipboard.copyTree!=null){mntmPaste.setEnabled(true);}
			else{mntmPaste.setEnabled(false);}
		}
		else if(curNode.type==1){
			mntmChangeType.setEnabled(true);
			mntmChangeType.setText("Change to Terminal Node");
			mntmChangeType.setIcon(new ScaledIcon("/images/terminalNode",16,16,16,true));
			mntmChangeType.setDisabledIcon(new ScaledIcon("/images/terminalNode",16,16,16,false));
			if(mainForm.clipboard.copyTree!=null){mntmPaste.setEnabled(true);}
			else{mntmPaste.setEnabled(false);}
		}
		else if(curNode.type==2){
			mnAdd.setEnabled(false);
			mntmChangeType.setEnabled(true);
			mntmChangeType.setText("Change to Chance Node");
			mntmChangeType.setIcon(new ScaledIcon("/images/chanceNode",16,16,16,true));
			mntmChangeType.setDisabledIcon(new ScaledIcon("/images/chanceNode",16,16,16,false));
			mntmShowCost.setEnabled(false);
			mntmPaste.setEnabled(false);
		}
		if(curNode.childIndices.size()>0){
			mntmChangeType.setEnabled(false);
			//mntmDelete.setEnabled(false);
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

		if(curNode.level==0){ //root
			mntmShowCost.setEnabled(false);
			mntmUpdateVariable.setEnabled(false);
		}
		popup.show(this,x,y);
	}

	public void clearAnnotations(){ //Removes expected values and any highlighting
		tree.showEV=false;
		int size=tree.nodes.size();
		for(int i=1; i<size; i++){ //skip root
			curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Payoff
			curNode.highlightTextField(3, null); //Variables
			curNode.highlightTextField(4, null); //Name
			if(curNode.type!=2){
				curNode.textEV.setVisible(false);
			}
			else if(curNode.type==2){
				curNode.textNumEnd.setVisible(false);
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
			this.remove(curNode.textName);
			this.remove(curNode.lblVarUpdates); this.remove(curNode.textVarUpdates);
			if(curNode.parentType!=0){
				this.remove(curNode.lblProb); this.remove(curNode.textProb);
			}
			if(curNode.type!=2){
				this.remove(curNode.lblCost); this.remove(curNode.textCost);
				this.remove(curNode.textEV);
			}
			else if(curNode.type==2){
				this.remove(curNode.textPayoff);
				this.remove(curNode.textNumEnd);
			}
			if(curNode.level==1){
				this.remove(curNode.textICER);
			}
			if(curNode.visible && curNode.collapsed){this.remove(curNode.lblCollapsed);}
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
				TreeNode curNode=tree.nodes.get(i);
				curNode.curScale=myModel.scale;
				curNode.width=curNode.scale(20); curNode.height=curNode.scale(20);
				curNode.xPos*=adjust; curNode.yPos*=adjust;
				//update parent position for children
				int numChildren=curNode.childIndices.size();
				for(int c=0; c<numChildren; c++){
					TreeNode child=tree.nodes.get(curNode.childIndices.get(c));
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
	
	public void highlightParameter(int paramNum){
		String paramName=myModel.parameters.get(paramNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
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
			if(curNode.payoff!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(paramName, curNode.payoff[d])){
						curNode.highlightTextField(2, Color.GREEN); //Payoff
					}
				}
			}
		}
	}
	
	public void highlightVariable(int varNum){
		String varName=myModel.variables.get(varNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
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
			if(curNode.payoff!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(varName, curNode.payoff[d])){
						curNode.highlightTextField(2, Color.GREEN); //Payoff
					}
				}
			}
		}
	}
	
	public void highlightTable(int tableNum){
		String tableName=myModel.tables.get(tableNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
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
			if(curNode.payoff!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(Interpreter.containsWord(tableName, curNode.payoff[d])){
						curNode.highlightTextField(2, Color.GREEN); //Payoff
					}
				}
			}
		}
	}

}