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
import java.text.MessageFormat;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import base.AmuaModel;
import markov.MarkovNode;
import tree.TreeNode;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Toolkit;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
			frmFindReplace.setTitle("Amua - "+myModel.language.base.getString("menu.find_replace")); //Find/Replace
			frmFindReplace.setFont(myModel.language.font);
			frmFindReplace.setResizable(false);
			frmFindReplace.setBounds(100, 100, 336, 206);
			frmFindReplace.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{55, 54, 96, 90, 0};
			gridBagLayout.rowHeights = new int[]{28, 28, 39, 28, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			frmFindReplace.getContentPane().setLayout(gridBagLayout);

			JLabel lblFind = new JLabel(myModel.language.base.getString("menu.find")+":");
			lblFind.setFont(myModel.language.font);
			GridBagConstraints gbc_lblFind = new GridBagConstraints();
			gbc_lblFind.anchor = GridBagConstraints.EAST;
			gbc_lblFind.insets = new Insets(5, 5, 5, 0);
			gbc_lblFind.gridx = 0;
			gbc_lblFind.gridy = 0;
			frmFindReplace.getContentPane().add(lblFind, gbc_lblFind);

			textFind = new JTextField();
			textFind.setFont(myModel.language.font);
			GridBagConstraints gbc_textFind = new GridBagConstraints();
			gbc_textFind.anchor = GridBagConstraints.NORTH;
			gbc_textFind.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFind.insets = new Insets(0, 0, 5, 5);
			gbc_textFind.gridwidth = 3;
			gbc_textFind.gridx = 1;
			gbc_textFind.gridy = 0;
			frmFindReplace.getContentPane().add(textFind, gbc_textFind);
			textFind.setColumns(10);

			textReplace = new JTextField();
			textReplace.setFont(myModel.language.font);
			GridBagConstraints gbc_textReplace = new GridBagConstraints();
			gbc_textReplace.anchor = GridBagConstraints.NORTH;
			gbc_textReplace.fill = GridBagConstraints.HORIZONTAL;
			gbc_textReplace.insets = new Insets(0, 0, 5, 5);
			gbc_textReplace.gridwidth = 3;
			gbc_textReplace.gridx = 1;
			gbc_textReplace.gridy = 1;
			frmFindReplace.getContentPane().add(textReplace, gbc_textReplace);
			textReplace.setColumns(10);

			JLabel lblScope = new JLabel(myModel.language.base.getString("object.scope")+":");
			lblScope.setFont(myModel.language.font);
			GridBagConstraints gbc_lblScope = new GridBagConstraints();
			gbc_lblScope.anchor = GridBagConstraints.EAST;
			gbc_lblScope.insets = new Insets(5, 5, 5, 5);
			gbc_lblScope.gridx = 0;
			gbc_lblScope.gridy = 3;
			frmFindReplace.getContentPane().add(lblScope, gbc_lblScope);

			final JComboBox<String> comboScope = new JComboBox<String>();
			comboScope.setFont(myModel.language.font);
			if(myModel.type==0){ //Decision Tree
				comboScope.setModel(new DefaultComboBoxModel<String>(new String[] {
						myModel.language.base.getString("node.all"), //All
						myModel.language.base.getString("object.name"), //Name
						myModel.language.math.getString("prob.probability"), //Probability 
						myModel.language.analysis.getString("cea.cost"), //Cost
						myModel.language.base.getString("node.payoff")})); //Payoff
			}
			else if(myModel.type==1){ //Markov
				comboScope.setModel(new DefaultComboBoxModel<String>(new String[] {
						myModel.language.base.getString("node.all"), //All
						myModel.language.base.getString("object.name"), //Name
						myModel.language.math.getString("prob.probability"), //Probability 
						myModel.language.analysis.getString("cea.cost"), //Cost
						myModel.language.base.getString("node.transition")})); //Transition
			}
			comboScope.setSelectedIndex(0);
			
			
			GridBagConstraints gbc_comboScope = new GridBagConstraints();
			gbc_comboScope.anchor = GridBagConstraints.NORTH;
			gbc_comboScope.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboScope.insets = new Insets(0, 0, 5, 5);
			gbc_comboScope.gridwidth = 2;
			gbc_comboScope.gridx = 1;
			gbc_comboScope.gridy = 3;
			frmFindReplace.getContentPane().add(comboScope, gbc_comboScope);
		
			
			JLabel lblReplace = new JLabel(myModel.language.base.getString("menu.replace")+":");
			lblReplace.setFont(myModel.language.font);
			GridBagConstraints gbc_lblReplace = new GridBagConstraints();
			gbc_lblReplace.anchor = GridBagConstraints.EAST;
			gbc_lblReplace.insets = new Insets(5, 5, 5, 0);
			gbc_lblReplace.gridx = 0;
			gbc_lblReplace.gridy = 1;
			frmFindReplace.getContentPane().add(lblReplace, gbc_lblReplace);
			
			JButton btnReplace = new JButton(myModel.language.base.getString("menu.replace")); //Replace
			btnReplace.setFont(myModel.language.font);
			btnReplace.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int scope=comboScope.getSelectedIndex();
					String find=textFind.getText();
					String replace=textReplace.getText();
					myModel.saveSnapshot(myModel.language.base.getString("menu.find_replace")); //Find/Replace
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
					//[num] fields updated!
					String msg = MessageFormat.format(myModel.language.message.getString("info.fields_updated"), count);
					JOptionPane.showMessageDialog(frmFindReplace, msg); 
				}
			});

			GridBagConstraints gbc_btnReplace = new GridBagConstraints();
			gbc_btnReplace.anchor = GridBagConstraints.NORTHWEST;
			gbc_btnReplace.insets = new Insets(0, 0, 0, 5);
			gbc_btnReplace.gridx = 2;
			gbc_btnReplace.gridy = 4;
			frmFindReplace.getContentPane().add(btnReplace, gbc_btnReplace);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.setFont(myModel.language.font);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmFindReplace.dispose();
				}
			});
			GridBagConstraints gbc_btnCancel = new GridBagConstraints();
			gbc_btnCancel.anchor = GridBagConstraints.NORTH;
			gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
			gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnCancel.gridx = 3;
			gbc_btnCancel.gridy = 4;
			frmFindReplace.getContentPane().add(btnCancel, gbc_btnCancel);

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
