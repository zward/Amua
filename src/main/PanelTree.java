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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class PanelTree extends JPanel{

	PanelTree curPanel;
	frmMain mainForm;
	public DecisionTree tree;
	TreeNode curNode, prevNode;
	public String name=null;
	String filepath=null;
	DecisionTree copyTree; //Clipboard
	JPopupMenu popup;
	JMenu mnAdd;
	JMenuItem mntmChangeType;
	JMenuItem mntmShowCost;
	JMenuItem mntmPaste;
	JMenuItem mntmDelete;
	JMenuItem mntmCollapse;

	Stack<DecisionTree> treeStackUndo, treeStackRedo;
	Stack<String> actionStackUndo, actionStackRedo;
	boolean unsavedChanges;

	int xOffset=0;
	int yOffset=0;
	int origX, origY;

	int canvasWidth=1000, canvasHeight=1000; //Default canvas size
	boolean rescalePanel=true;
	public VarHelper varHelper;

	StyledTextPane paneFormula;
	boolean formulaBarFocus=false;
	TreeTextField curFocus;
	JTextArea textAreaNotes;
	String tempNotes;
	boolean notesFocus=false;
	
	public ErrorLog errorLog;
	
	/**
	 * Default constructor
	 */
	public PanelTree(frmMain mainFrm, ErrorLog errorLog){
		curPanel=this;
		this.mainForm=mainFrm;
		this.errorLog=errorLog;
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(Color.WHITE);

		tree=new DecisionTree(true);
		tree.nodes.get(0).setPanel(this);
		treeStackUndo=new Stack<DecisionTree>();
		treeStackRedo=new Stack<DecisionTree>();
		actionStackUndo=new Stack<String>();
		actionStackRedo=new Stack<String>();
		varHelper=new VarHelper(tree.variables,tree.dimSymbols,errorLog);
		paneFormula=new StyledTextPane(varHelper);
		paneFormula.setEditable(false);

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

		textAreaNotes=new JTextArea();
		textAreaNotes.setEditable(false);

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
		textAreaNotes.getDocument().addDocumentListener(new DocumentListener(){
			@Override public void insertUpdate(DocumentEvent e) {
				if(notesFocus){tempNotes=textAreaNotes.getText();}
			}
			@Override public void removeUpdate(DocumentEvent e) {
				if(notesFocus){tempNotes=textAreaNotes.getText();}
			}
			@Override public void changedUpdate(DocumentEvent e) {}
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
		mntmDecision.setIcon(new ImageIcon(frmMain.class.getResource("/images/decisionNode_16.png")));
		mnAdd.add(mntmDecision);

		JMenuItem mntmChance = new JMenuItem("Chance Node");
		mntmChance.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(1);}});
		mntmChance.setIcon(new ImageIcon(frmMain.class.getResource("/images/chanceNode_16.png")));
		mnAdd.add(mntmChance);

		JMenuItem mntmTerminal = new JMenuItem("Terminal Node");
		mntmTerminal.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addNode(2);}});
		mntmTerminal.setIcon(new ImageIcon(frmMain.class.getResource("/images/terminalNode_16.png")));
		mnAdd.add(mntmTerminal);

		mntmChangeType= new JMenuItem("Change Node Type");
		mntmChangeType.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {changeNodeType();}});
		popup.add(mntmChangeType);

		mntmShowCost= new JMenuItem("Add/Remove Cost");
		mntmShowCost.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {addRemoveCost();}});
		mntmShowCost.setIcon(new ImageIcon(frmMain.class.getResource("/images/cost_16.png")));
		popup.add(mntmShowCost);

		popup.addSeparator();

		final JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {cutSubtree();}});
		mntmCut.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/control/skin/modena/HTMLEditor-Cut.png")));
		popup.add(mntmCut);

		final JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {copySubtree();}});
		mntmCopy.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Copy_16x16_JFX.png")));
		popup.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {pasteSubtree();}});
		mntmPaste.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Paste_16x16_JFX.png")));
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
			TreeNode nodePosition=tree.nodes.get(i);
			maxX=Math.max(maxX, nodePosition.xPos+nodePosition.scale(100));
			maxY=Math.max(maxY, nodePosition.yPos+nodePosition.scale(100));
		}
		canvasWidth=Math.max(maxX, 1000);
		canvasHeight=Math.max(maxY, 1000);
	}

	/**
	 * Returns the canvas size
	 */
	public Dimension getPreferredSize() {
		return new Dimension(canvasWidth,canvasHeight);
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

		if(curNode!=null){
			textAreaNotes.setText(curNode.notes);
			textAreaNotes.setEditable(true);
			mainForm.mntmCut.setEnabled(true);
			mainForm.mntmCopy.setEnabled(true);
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
			else{mainForm.btnShowCost.setEnabled(true);}
		}
		else{ //Node not selected
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);
			mainForm.btnDecisionNode.setEnabled(false);
			mainForm.btnChanceNode.setEnabled(false);
			mainForm.btnTerminalNode.setEnabled(false);
			mainForm.btnChangeNodeType.setEnabled(false);
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
			if(curNode.parentType!=0){
				this.add(curNode.lblProb); this.add(curNode.textProb);
			}
			if(type!=2){ //Not terminal
				this.add(curNode.lblCost); this.add(curNode.textCost);
				this.add(curNode.textEV);
			}
			else if(type==2){ //Terminal
				this.add(curNode.textPayoff);
			}
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
				if(curNode.parentType!=0){
					this.remove(curNode.lblProb); this.remove(curNode.textProb);
				}
				if(curNode.type!=2){
					this.remove(curNode.lblCost); this.remove(curNode.textCost);
					this.remove(curNode.textEV);
				}
				else if(curNode.type==2){
					this.remove(curNode.textPayoff);
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
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
			else if(curNode.type==2){ //Terminal->Chance
				saveSnapshot("Change Node Type");
				curNode.type=1;
				this.remove(curNode.textPayoff);
				curNode.textPayoff=null;
				Arrays.fill(curNode.payoff, "0");
				curNode.displayCost();
				curNode.displayEV();
				this.add(curNode.lblCost); this.add(curNode.textCost);
				this.add(curNode.textEV);
				repaint();
				selectNode(curNode.xPos,curNode.yPos);
			}
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
			copyTree=tree.copySubtree(curNode);
			mntmPaste.setEnabled(true);
			mainForm.mntmPaste.setEnabled(true);
		}
	}

	public void cutSubtree(){
		if(curNode!=null){
			saveSnapshot("Cut");
			copyTree=tree.copySubtree(curNode);
			removeSubtree(curNode);
			mntmPaste.setEnabled(true);
			mainForm.mntmPaste.setEnabled(true);
		}
	}

	public void pasteSubtree(){
		if(curNode!=null && copyTree!=null && curNode.type!=2 && curNode.collapsed==false){
			saveSnapshot("Paste");
			this.setVisible(false); //Don't paint anything until updated

			//Paste
			TreeNode parent=curNode;
			TreeNode subroot=copyTree.nodes.get(0);
			int xDiff=(parent.xPos-subroot.parentX);
			int yDiff=(parent.yPos-subroot.parentY);
			int levelDiff=(parent.level+1)-subroot.level;
			int origSize=tree.nodes.size();
			//Add subroot to tree
			tree.nodes.add(subroot);
			parent.childIndices.add(origSize);
			subroot.parentType=parent.type;
			subroot.xPos+=xDiff+parent.width; subroot.yPos+=(yDiff+(parent.height/2)+subroot.scale(50));
			subroot.level+=levelDiff;
			subroot.parentX=parent.xPos+parent.width; subroot.parentY=parent.yPos+(parent.height/2);
			for(int j=0; j<subroot.childIndices.size(); j++){
				int index=subroot.childIndices.get(j); //Index in subtree
				index+=origSize; //Appended to orig tree
				subroot.childIndices.set(j, index);
			}
			updateNodeDisplay(subroot);
			//Add children			
			for(int i=1; i<copyTree.nodes.size(); i++){
				TreeNode copyNode=copyTree.nodes.get(i);
				tree.nodes.add(copyNode);
				copyNode.xPos+=xDiff+parent.width; copyNode.yPos+=(yDiff+(parent.height/2)+copyNode.scale(50));
				copyNode.parentX+=xDiff+parent.width; copyNode.parentY+=(yDiff+(parent.height/2)+copyNode.scale(50));
				copyNode.level+=levelDiff;
				//Update child indices
				int numChildren=copyNode.childIndices.size();
				for(int j=0; j<numChildren; j++){
					int index=copyNode.childIndices.get(j); //Index of child in subtree
					index+=origSize; //Appended to orig tree
					copyNode.childIndices.set(j, index);
				}
				updateNodeDisplay(copyNode);
			}
			copyTree=null; //Clear clipboard
			mntmPaste.setEnabled(false);
			mainForm.mntmPaste.setEnabled(false);

			this.setVisible(true);
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


	private void updateNodeDisplay(TreeNode node){
		node.setPanel(this);
		node.updateDisplay();
		this.add(node.textName);
		if(node.parentType!=0){
			this.add(node.lblProb); this.add(node.textProb);
		}
		if(node.type!=2){ //Not terminal
			this.add(node.lblCost); this.add(node.textCost);
			this.add(node.textEV);
			node.textEV.setVisible(tree.showEV);
		}
		else if(node.type==2){ //Terminal
			this.add(node.textPayoff);
		}
		if(node.collapsed){this.add(node.lblCollapsed);}
		if(node.selected){textAreaNotes.setText(node.notes);}
	}

	/**
	 * Aligns all nodes
	 */
	public void ocd(String actionName){ //Align all nodes
		xOffset=0; yOffset=0;
		saveSnapshot(actionName);
		//saveSnapshot("OCD");
		//Align x
		int size=tree.nodes.size();
		int maxLevel=0;
		int minY=0;
		//Default to alignBelow
		for(int i=0; i<size; i++){
			TreeNode curNode=tree.nodes.get(i);
			int curX=(curNode.level*curNode.scale(185))+curNode.scale(50);
			maxLevel=Math.max(maxLevel, tree.nodes.get(i).level);
			moveNode(curNode,curX,curNode.yPos);
			minY=Math.min(minY, curNode.yPos);
		}
		if(tree.alignRight){
			for(int i=0; i<size; i++){
				TreeNode curNode=tree.nodes.get(i);
				if(curNode.type==2){ //Align terminal nodes
					int curX=(maxLevel*curNode.scale(185))+curNode.scale(50);
					moveNode(curNode,curX,curNode.yPos);
				}
			}
		}

		if(minY<0){ //Ensure all nodes are visible
			TreeNode root=tree.nodes.get(0);
			moveNode(root,root.xPos,root.yPos+(25-minY)); //Shift root
		}

		//Align y
		boolean overlap=true;
		int counter=0; //Break out of infinite loops
		while(overlap==true && counter<10000){
			//Center parent nodes, working backwards
			for(int l=maxLevel-1; l>=0; l--){
				for(int i=0; i<size; i++){
					TreeNode curNode=tree.nodes.get(i);
					if(curNode.level==l){
						int numChildren=curNode.childIndices.size();
						if(numChildren>0){
							int totalY=0;
							for(int c=0; c<numChildren; c++){
								totalY+=tree.nodes.get(curNode.childIndices.get(c)).yPos;
							}
							totalY=totalY/(numChildren);
							curNode.yPos=totalY;//Move node
							//Update parent position for children
							totalY+=(curNode.height/2); //Midpoint
							for(int c=0; c<numChildren; c++){tree.nodes.get(curNode.childIndices.get(c)).parentY=totalY;}
						}
					}
				}
			}
			overlap=false;
			//Check for overlap
			for(int i=0; i<size; i++){
				TreeNode node1=tree.nodes.get(i);
				for(int j=i+1; j<size; j++){
					TreeNode node2=tree.nodes.get(j);
					if(nodesOverlap(node1,node2)){
						overlap=true;
					}
				}
			}
			counter++;
		}

		repaint();
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


	public boolean checkTree(JTextArea console,boolean checkProbs){
		mainForm.tabbedPaneBottom.setSelectedIndex(0);
		console.setText("");
		ArrayList<String> errors=tree.parseTree();
		if(errors.size()==0){
			if(console!=null){
				console.append("Tree checked!\n");
			}
			return(true);
		}
		else{
			if(console!=null){
				console.append(errors.size()+" errors:\n");
				for(int i=0; i<errors.size(); i++){
					console.append(errors.get(i)+"\n");		
				}
			}
			return(false);
		}

	}

	public void saveTree(JTextArea console){
		try{
			tree.meta.update();

			JAXBContext context = JAXBContext.newInstance(DecisionTree.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Write to File
			FileWriter fstreamO=new FileWriter(filepath);
			BufferedWriter out=new BufferedWriter(fstreamO);
			m.marshal(tree,out);
			out.close();

			unsavedChanges=false;
			mainForm.tabbedPaneBottom.setSelectedIndex(0);
			console.append("Model saved\n");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	public void openTree(){
		try{
			this.setVisible(false); //Don't paint anything until updated
			clearAll();
			paneFormula.setText("");
			paneFormula.setEditable(false);
			textAreaNotes.setText("");
			textAreaNotes.setEditable(false);
			
			JAXBContext context = JAXBContext.newInstance(DecisionTree.class);
			Unmarshaller un = context.createUnmarshaller();
			tree = (DecisionTree) un.unmarshal(new File(filepath));
			rescalePanel=false; //Turn off re-scaling
			mainForm.setZoomVal(tree.scale);
			rescalePanel=true;

			//Add new tree
			//Initialize root
			TreeNode root=tree.nodes.get(0);
			root.setPanel(this);
			root.cost=new String[tree.dimNames.length];
			root.numDimensions=tree.dimNames.length;
			int size=tree.nodes.size();
			for(int i=1; i<size; i++){ //Skip root
				TreeNode curNode=tree.nodes.get(i);
				curNode.setPanel(this);
				curNode.curScale=tree.scale;
				curNode.numDimensions=tree.dimNames.length;
				updateNodeDisplay(tree.nodes.get(i));
			}
			this.setVisible(true); //Show updated panel

			//Update variables table
			if(tree.variables==null){tree.variables=new ArrayList<TreeVariable>();}
			refreshVarTable();
			//Update undo stacks
			treeStackUndo=new Stack<DecisionTree>();
			treeStackRedo=new Stack<DecisionTree>();
			actionStackUndo=new Stack<String>();
			actionStackRedo=new Stack<String>();
			mainForm.mntmUndo.setEnabled(false);
			mainForm.mntmUndo.setText("Undo");
			mainForm.mntmRedo.setEnabled(false);
			mainForm.mntmRedo.setText("Redo");

			varHelper=new VarHelper(tree.variables,tree.dimSymbols,errorLog);
			paneFormula.varHelper=varHelper;
			rescale(tree.scale); //Refresh

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
		if(curNode.type==0){
			mntmChangeType.setEnabled(false);
			mntmChangeType.setText("Change Node Type");
			mntmChangeType.setIcon(null);
		}
		else if(curNode.type==1){
			mntmChangeType.setEnabled(true);
			mntmChangeType.setText("Change to Terminal Node");
			mntmChangeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/terminalNode_16.png")));
		}
		else if(curNode.type==2){
			mnAdd.setEnabled(false);
			mntmChangeType.setEnabled(true);
			mntmChangeType.setText("Change to Chance Node");
			mntmChangeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/chanceNode_16.png")));
			mntmShowCost.setEnabled(false);
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
			curNode.highlightTextField(2, null); //Payoff
			if(curNode.type==1){
				curNode.textEV.setVisible(false);
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
			if(curNode.parentType!=0){
				this.remove(curNode.lblProb); this.remove(curNode.textProb);
			}
			if(curNode.type!=2){
				this.remove(curNode.lblCost); this.remove(curNode.textCost);
				this.remove(curNode.textEV);
			}
			else if(curNode.type==2){
				this.remove(curNode.textPayoff);
			}
			if(curNode.collapsed){this.remove(curNode.lblCollapsed);}
		}
	}

	public void saveSnapshot(String action){
		unsavedChanges=true;
		//Add undoable action
		treeStackUndo.push(tree.snapshot());
		actionStackUndo.push(action);
		mainForm.mntmUndo.setEnabled(true);
		mainForm.mntmUndo.setText("Undo "+action);
		//Clear redo stack
		treeStackRedo.clear();
		actionStackRedo.clear();
		mainForm.mntmRedo.setEnabled(false);
		mainForm.mntmRedo.setText("Redo");
	}

	public void undoAction(){
		unsavedChanges=true;
		this.setVisible(false); //Don't paint anything until updated
		clearAll();
		//Add current state to redo stack
		String lastAction=actionStackUndo.pop();
		treeStackRedo.push(tree.snapshot());
		actionStackRedo.push(lastAction);
		mainForm.mntmRedo.setEnabled(true);
		mainForm.mntmRedo.setText("Redo "+lastAction);

		//Revert to previous state
		textAreaNotes.setText("");
		tree=treeStackUndo.pop();
		int size=tree.nodes.size();
		for(int i=1; i<size; i++){ //Skip root
			updateNodeDisplay(tree.nodes.get(i));
		}
		varHelper.variables=tree.variables;
		this.setVisible(true); //Show updated panel
		refreshVarTable();
		
		if(lastAction.contains("Variable")){rescale(tree.scale);} //Re-validates textfields

		if(treeStackUndo.size()==0){
			mainForm.mntmUndo.setEnabled(false);
			mainForm.mntmUndo.setText("Undo");
		}
		else{
			mainForm.mntmUndo.setText("Undo "+actionStackUndo.peek());
		}
	}

	public void redoAction(){
		unsavedChanges=true;
		this.setVisible(false); //Don't paint anything until updated
		clearAll();
		//Add current state to undo stack
		String lastAction=actionStackRedo.pop();
		treeStackUndo.push(tree.snapshot());
		actionStackUndo.push(lastAction);
		mainForm.mntmUndo.setEnabled(true);
		mainForm.mntmUndo.setText("Undo "+lastAction);

		//Revert to previous state
		textAreaNotes.setText("");
		tree=treeStackRedo.pop();
		int size=tree.nodes.size();
		for(int i=1; i<size; i++){ //Skip root
			updateNodeDisplay(tree.nodes.get(i));
		}
		varHelper.variables=tree.variables;
		this.setVisible(true); //Show updated panel
		refreshVarTable();

		if(lastAction.contains("Variable")){rescale(tree.scale);} //Re-validates textfields
		
		if(treeStackRedo.size()==0){
			mainForm.mntmRedo.setEnabled(false);
			mainForm.mntmRedo.setText("Redo");
		}
		else{
			mainForm.mntmRedo.setText("Redo "+actionStackRedo.peek());
		}
	}

	public void rescale(int newScale){
		if(rescalePanel){
			int origScale=tree.scale;
			tree.scale=newScale;
			double adjust=tree.scale/(origScale*1.0);

			this.setVisible(false); //Don't paint anything until updated
			clearAll();
			int size=tree.nodes.size();

			//Re-scale nodes
			for(int i=0; i<size; i++){
				TreeNode curNode=tree.nodes.get(i);
				curNode.curScale=tree.scale;
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

	private boolean nodesOverlap(TreeNode node1, TreeNode node2){
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

	public void addVariable(TreeVariable variable){
		mainForm.modelVariables.addRow(new Object[]{variable.name,variable.expression});
		validateVariables();
	}

	public void refreshVarTable(){
		mainForm.modelVariables.setRowCount(0);
		int numVars=tree.variables.size();
		for(int i=0; i<numVars; i++){addVariable(tree.variables.get(i));}
	}

	public void editVariable(int varNum){
		TreeVariable variable=tree.variables.get(varNum);
		mainForm.modelVariables.setValueAt(variable.name, varNum, 0);
		mainForm.modelVariables.setValueAt(variable.expression, varNum, 1);
		validateVariables();
	}

	public void deleteVariable(int varNum){
		saveSnapshot("Delete Variable");//Add to undo stack
		tree.variables.remove(varNum);
		mainForm.modelVariables.removeRow(varNum);
		validateVariables();
	}
	
	public void validateVariables(){
		int numVars=tree.variables.size();
		for(int i=0; i<numVars; i++){
			TreeVariable curVar=tree.variables.get(i);
			curVar.evaluate(varHelper);
		}
	}

	public void highlightVariable(int varNum){
		String varName=tree.variables.get(varNum).name;
		int numNodes=tree.nodes.size();
		for(int i=1; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
			//Reset highlighting
			curNode.highlightTextField(0, null); //Prob
			curNode.highlightTextField(1, null); //Cost
			curNode.highlightTextField(2, null); //Payoff

			if(curNode.prob!=null && varHelper.containsVar(varName, curNode.prob)){
				curNode.highlightTextField(0, Color.GREEN); //Prob
			}
			if(curNode.cost!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(varHelper.containsVar(varName, curNode.cost[d])){
						curNode.highlightTextField(1, Color.GREEN); //Cost
					}
				}
			}
			if(curNode.payoff!=null){
				for(int d=0; d<curNode.numDimensions; d++){
					if(varHelper.containsVar(varName, curNode.payoff[d])){
						curNode.highlightTextField(2, Color.GREEN); //Payoff
					}
				}
			}
		}
	}

	public void updateDimensions(){
		int numNodes=tree.nodes.size();
		int numDimensions=tree.dimNames.length;
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=tree.nodes.get(i);
			curNode.numDimensions=numDimensions;
			curNode.cost=Arrays.copyOf(curNode.cost, numDimensions);
			curNode.payoff=Arrays.copyOf(curNode.payoff, numDimensions);
			if(curNode.expectedValues==null){curNode.expectedValues=new double[numDimensions];}
			else{curNode.expectedValues=Arrays.copyOf(curNode.expectedValues, numDimensions);}
			for(int d=0; d<numDimensions; d++){
				if(curNode.cost[d]==null){curNode.cost[d]="0";}
				if(curNode.payoff[d]==null){curNode.payoff[d]="0";}
			}
		}
		//Refresh panel with same scale
		rescale(tree.scale);
	}

}