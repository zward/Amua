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

import javax.swing.JFrame;

import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import base.AmuaModel;
import markov.MarkovNode;
import tree.TreeNode;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Toolkit;

/**
 *
 */
public class frmFindReplace {

	/**
	 * JFrame for form
	 */
	public JDialog frmFindReplace;
	AmuaModel myModel;
	int numNodes;
	private JTextField textFind;
	private JTextField textReplace;

	public frmFindReplace(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmFindReplace = new JDialog();
			frmFindReplace.setIconImage(Toolkit.getDefaultToolkit().getImage(frmFindReplace.class.getResource("/images/find_128.png")));
			frmFindReplace.setModalityType(ModalityType.APPLICATION_MODAL);
			frmFindReplace.setTitle("Amua - Find/Replace");
			frmFindReplace.setResizable(false);
			frmFindReplace.setBounds(100, 100, 336, 206);
			frmFindReplace.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmFindReplace.getContentPane().setLayout(null);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmFindReplace.dispose();
				}
			});
			btnCancel.setBounds(223, 133, 90, 28);
			frmFindReplace.getContentPane().add(btnCancel);

			JLabel lblFind = new JLabel("Find:");
			lblFind.setBounds(12, 13, 55, 16);
			frmFindReplace.getContentPane().add(lblFind);

			textFind = new JTextField();
			textFind.setBounds(63, 7, 250, 28);
			frmFindReplace.getContentPane().add(textFind);
			textFind.setColumns(10);

			JLabel lblReplace = new JLabel("Replace:");
			lblReplace.setBounds(12, 45, 55, 16);
			frmFindReplace.getContentPane().add(lblReplace);

			textReplace = new JTextField();
			textReplace.setBounds(63, 39, 248, 28);
			frmFindReplace.getContentPane().add(textReplace);
			textReplace.setColumns(10);

			JLabel lblScope = new JLabel("Scope:");
			lblScope.setBounds(12, 93, 55, 16);
			frmFindReplace.getContentPane().add(lblScope);

			final JComboBox<String> comboScope = new JComboBox<String>();
			if(myModel.type==0){ //Decision Tree
				comboScope.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Name", "Probability", "Cost", "Payoff"}));
			}
			else if(myModel.type==1){ //Markov
				comboScope.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Name", "Probability", "Cost", "Transition"}));
			}
			comboScope.setSelectedIndex(0);
			comboScope.setBounds(63, 88, 154, 26);
			frmFindReplace.getContentPane().add(comboScope);

			JButton btnReplace = new JButton("Replace");
			btnReplace.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int scope=comboScope.getSelectedIndex();
					String find=textFind.getText();
					String replace=textReplace.getText();
					myModel.saveSnapshot("Find/Replace");
					int count=0;
					if(myModel.type==0){ //Decision tree
						if(scope==0){ //All
							count=replaceName(find,replace);
							count+=replaceProb(find,replace);
							count+=replaceCost(find,replace);
							count+=replacePayoff(find,replace);
						}
						else if(scope==1){count=replaceName(find,replace);}
						else if(scope==2){count=replaceProb(find,replace);}
						else if(scope==3){count=replaceCost(find,replace);}
						else if(scope==4){count=replacePayoff(find,replace);}
					}
					else if(myModel.type==1){ //Markov Model
						if(scope==0){ //All
							count=replaceName(find,replace);
							count+=replaceProb(find,replace);
							count+=replaceCost(find,replace);
							count+=replaceTransition(find,replace);
						}
						else if(scope==1){count=replaceName(find,replace);}
						else if(scope==2){count=replaceProb(find,replace);}
						else if(scope==3){count=replaceCost(find,replace);}
						else if(scope==4){count=replaceTransition(find,replace);}
					}
					
					myModel.rescale(myModel.scale); //Refresh panel
					JOptionPane.showMessageDialog(frmFindReplace, count+" fields updated!");
				}
			});
			btnReplace.setBounds(121, 133, 90, 28);
			frmFindReplace.getContentPane().add(btnReplace);

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private int replaceName(String find, String replace){
		int count=0;
		if(myModel.type==0){ //Decision tree
			for(int i=0; i<numNodes; i++){
				TreeNode curNode=myModel.tree.nodes.get(i);
				String text=curNode.name;
				if(text.contains(find)){
					curNode.name=text.replace(find, replace);
					count++;
				}
			}
		}
		else if(myModel.type==1){ //Markov
			for(int i=0; i<numNodes; i++){
				MarkovNode curNode=myModel.markov.nodes.get(i);
				String text=curNode.name;
				if(text.contains(find)){
					curNode.name=text.replace(find, replace);
					count++;
				}
			}
		}
		
		return(count);
	}

	private int replaceProb(String find, String replace){
		int count=0;
		if(myModel.type==0){ //Decision tree
			for(int i=0; i<numNodes; i++){
				TreeNode curNode=myModel.tree.nodes.get(i);
				String text=curNode.prob;
				if(text!=null && text.contains(find)){
					curNode.prob=text.replace(find, replace);
					count++;
				}
			}
		}
		else if(myModel.type==1){ //Markov
			for(int i=0; i<numNodes; i++){
				MarkovNode curNode=myModel.markov.nodes.get(i);
				String text=curNode.prob;
				if(text!=null && text.contains(find)){
					curNode.prob=text.replace(find, replace);
					count++;
				}
			}
		}
		return(count);
	}

	private int replaceCost(String find, String replace){
		int count=0;
		if(myModel.type==0){ //Decision tree
			for(int i=0; i<numNodes; i++){
				TreeNode curNode=myModel.tree.nodes.get(i);
				if(curNode.hasCost){
					for(int d=0; d<curNode.numDimensions; d++){
						String text=curNode.cost[d];
						if(text.contains(find)){
							curNode.cost[d]=text.replace(find, replace);
							count++;
						}
					}
				}
			}
		}
		else if(myModel.type==1){
			for(int i=0; i<numNodes; i++){
				MarkovNode curNode=myModel.markov.nodes.get(i);
				if(curNode.hasCost){
					for(int d=0; d<curNode.numDimensions; d++){
						String text=curNode.cost[d];
						if(text.contains(find)){
							curNode.cost[d]=text.replace(find, replace);
							count++;
						}
					}
				}
			}
		}
		return(count);
	}

	private int replacePayoff(String find, String replace){
		int count=0;
		if(myModel.type==0){ //Decision tree
			for(int i=0; i<numNodes; i++){
				TreeNode curNode=myModel.tree.nodes.get(i);
				for(int d=0; d<curNode.numDimensions; d++){
					String text=curNode.payoff[d];
					if(text.contains(find)){
						curNode.payoff[d]=text.replace(find, replace);
						count++;
					}
				}
			}
		}
		return(count);
	}
	
	private int replaceTransition(String find, String replace){
		int count=0;
		if(myModel.type==1){ //Markov  tree
			for(int i=0; i<numNodes; i++){
				MarkovNode curNode=myModel.markov.nodes.get(i);
				for(int d=0; d<curNode.numDimensions; d++){
					String text=curNode.transition;
					if(text.contains(find)){
						curNode.transition=text.replace(find, replace);
						count++;
					}
				}
			}
		}
		return(count);
	}
}
