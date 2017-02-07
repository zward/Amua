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

import javax.swing.JFrame;

import java.awt.Dialog.ModalityType;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 *
 */
public class frmFindReplace {

	/**
	 * JFrame for form
	 */
	public JDialog frmFindReplace;
	PanelTree myPanel;
	ArrayList<TreeNode> nodes;
	int numNodes;
	private JTextField textFind;
	private JTextField textReplace;

	/**
	 *  Default Constructor
	 */
	public frmFindReplace(PanelTree myPanel) {
		this.myPanel=myPanel;
		this.nodes=myPanel.tree.nodes;
		numNodes=nodes.size();
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmFindReplace = new JDialog();
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

			final JComboBox comboScope = new JComboBox();
			comboScope.setModel(new DefaultComboBoxModel(new String[] {"All", "Name", "Probability", "Cost", "Payoff"}));
			comboScope.setSelectedIndex(0);
			comboScope.setBounds(63, 88, 154, 26);
			frmFindReplace.getContentPane().add(comboScope);

			JButton btnReplace = new JButton("Replace");
			btnReplace.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int scope=comboScope.getSelectedIndex();
					String find=textFind.getText();
					String replace=textReplace.getText();
					myPanel.saveSnapshot("Find/Replace");
					int count=0;
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
					myPanel.rescale(myPanel.tree.scale); //Refresh panel
					JOptionPane.showMessageDialog(frmFindReplace, count+" fields updated!");
				}
			});
			btnReplace.setBounds(121, 133, 90, 28);
			frmFindReplace.getContentPane().add(btnReplace);



		} catch (Exception ex){
			ex.printStackTrace();
			myPanel.errorLog.recordError(ex);
		}
	}

	private int replaceName(String find, String replace){
		int count=0;
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=nodes.get(i);
			String text=curNode.name;
			if(text.contains(find)){
				curNode.name=text.replace(find, replace);
				count++;
			}
		}
		return(count);
	}

	private int replaceProb(String find, String replace){
		int count=0;
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=nodes.get(i);
			String text=curNode.prob;
			if(text!=null && text.contains(find)){
				curNode.prob=text.replace(find, replace);
				count++;
			}
		}
		return(count);
	}

	private int replaceCost(String find, String replace){
		int count=0;
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=nodes.get(i);
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
		return(count);
	}

	private int replacePayoff(String find, String replace){
		int count=0;
		for(int i=0; i<numNodes; i++){
			TreeNode curNode=nodes.get(i);
			for(int d=0; d<curNode.numDimensions; d++){
				String text=curNode.payoff[d];
				if(text.contains(find)){
					curNode.payoff[d]=text.replace(find, replace);
					count++;
				}
			}
		}
		return(count);
	}
}
