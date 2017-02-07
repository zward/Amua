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
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import java.awt.SystemColor;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTabbedPane;

/**
 *
 */
public class frmProperties {

	/**
	 * JFrame for form
	 */
	public JDialog frmProperties;
	private JTextField textDecimals;
	/**
	 * 0=Decision Tree
	 */
	int modelType=-1;
	PanelTree panelTree;
	DecisionTree modelTree;

	JLabel lblModel;
	JLabel lblIcon;
	JLabel lblDispAuthor;
	JLabel lblDispCreated;
	JLabel lblDispVCreated;
	JLabel lblDispModifer;	
	JLabel lblDispModified; 
	JLabel lblVModified; 
	DefaultTableModel modelDimensions;
	private JTable tableDimensions;

	/**
	 *  Default Constructor
	 */
	public frmProperties() {
		initialize();
	}

	public void setModel(PanelTree panelTree){
		this.panelTree=panelTree;
		this.modelTree=panelTree.tree;
		modelType=0;
		displayMetadata(modelTree.meta);
		displayModelSettings();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmProperties = new JDialog();
			frmProperties.getContentPane().setBackground(SystemColor.control);
			frmProperties.setModalityType(ModalityType.APPLICATION_MODAL);
			frmProperties.setTitle("Amua - Properties");
			frmProperties.setResizable(false);
			frmProperties.setBounds(100, 100, 466, 332);
			frmProperties.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmProperties.getContentPane().setLayout(null);

			final String descriptions[]=new String[5];
			descriptions[0]="C++ source file";
			descriptions[1]="Creates an XML Spreadsheet that is compatible with Excel.  The model is converted to matix form.";
			descriptions[2]="Java source file";
			descriptions[3]="Python source file";
			descriptions[4]="R source file";

			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Validate dimensions
					boolean close=true;
					//Ensure unqiue
					int numDimensions=tableDimensions.getRowCount();
					ArrayList<String> dimNames=new ArrayList<String>();
					ArrayList<String> dimSymbols=new ArrayList<String>();
					for(int i=0; i<numDimensions; i++){
						String curName=(String) tableDimensions.getValueAt(i,0);
						String curSymbol=(String) tableDimensions.getValueAt(i,1);
						if(curName==null){
							close=false;
							JOptionPane.showMessageDialog(frmProperties, curName+"Please enter a valid dimension name!");
						}
						else if(dimNames.contains(curName)){
							close=false;
							JOptionPane.showMessageDialog(frmProperties, curName+" is already defined!");
						}
						else{
							dimNames.add(curName);
						}
						
						if(curSymbol==null){
							close=false;
							JOptionPane.showMessageDialog(frmProperties, curName+"Please enter a valid dimension symbol!");
						}
						if(dimSymbols.contains(curSymbol)){
							close=false;
							JOptionPane.showMessageDialog(frmProperties, curSymbol+ "is already defined!");
						}
						else{
							dimSymbols.add(curSymbol);
						}
					}
					
					//Get decimal precision
					int decimals=6;
					try{
						decimals=Integer.parseInt(textDecimals.getText());
					} catch(Exception er){
						close=false;
						JOptionPane.showMessageDialog(frmProperties, "Please enter a valid integer!");
					}
					if(decimals<0){
						close=false;
						JOptionPane.showMessageDialog(frmProperties, "Please enter a valid integer!");
					}

					if(close==true){ //Apply changes and close
						if(modelType==0){
							modelTree.dimNames=new String[numDimensions]; modelTree.dimSymbols=new String[numDimensions];
							for(int i=0; i<numDimensions; i++){
								modelTree.dimNames[i]=dimNames.get(i);
								modelTree.dimSymbols[i]=dimSymbols.get(i);
							}
							panelTree.updateDimensions();
							panelTree.varHelper.updateBreaks(panelTree.tree.dimSymbols);
							modelTree.decimals=decimals;
						}
						frmProperties.dispose();
					}
				}
			});
			btnOk.setBounds(262, 252, 90, 28);
			frmProperties.getContentPane().add(btnOk);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmProperties.dispose();
				}
			});
			btnCancel.setBounds(364, 252, 90, 28);
			frmProperties.getContentPane().add(btnCancel);

			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBounds(6, 6, 448, 234);
			frmProperties.getContentPane().add(tabbedPane);

			JPanel panel = new JPanel();
			tabbedPane.addTab("General", null, panel, null);
			panel.setBackground(SystemColor.window);
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setLayout(null);

			JLabel lblAuthor = new JLabel("Created by:");
			lblAuthor.setBounds(6, 38, 66, 16);
			panel.add(lblAuthor);

			lblDispAuthor = new JLabel("[Author]");
			lblDispAuthor.setBounds(120, 38, 325, 16);
			panel.add(lblDispAuthor);

			JLabel lblCreated = new JLabel("Created:");
			lblCreated.setBounds(6, 65, 55, 16);
			panel.add(lblCreated);

			lblDispCreated = new JLabel("[Date created]");
			lblDispCreated.setBounds(120, 65, 325, 16);
			panel.add(lblDispCreated);

			JLabel lblVersionCreated = new JLabel("Version created:");
			lblVersionCreated.setBounds(6, 92, 105, 16);
			panel.add(lblVersionCreated);

			JLabel lblModifiedBy = new JLabel("Modified by:");
			lblModifiedBy.setBounds(6, 119, 73, 16);
			panel.add(lblModifiedBy);

			JLabel lblModelType = new JLabel("Model Type:");
			lblModelType.setBounds(6, 11, 73, 16);
			panel.add(lblModelType);

			JLabel lblModified = new JLabel("Modified:");
			lblModified.setBounds(6, 146, 56, 16);
			panel.add(lblModified);

			JLabel lblVersionModified = new JLabel("Version modified:");
			lblVersionModified.setBounds(6, 173, 105, 16);
			panel.add(lblVersionModified);

			lblDispVCreated = new JLabel("[Version]");
			lblDispVCreated.setBounds(120, 92, 325, 16);
			panel.add(lblDispVCreated);

			lblDispModifer = new JLabel("[Modifier]");
			lblDispModifer.setBounds(120, 119, 325, 16);
			panel.add(lblDispModifer);

			lblDispModified = new JLabel("[Date modified]");
			lblDispModified.setBounds(120, 146, 325, 16);
			panel.add(lblDispModified);

			lblVModified = new JLabel("[Version]");
			lblVModified.setBounds(120, 173, 325, 16);
			panel.add(lblVModified);

			lblModel = new JLabel("[Model]");
			lblModel.setBounds(120, 11, 325, 16);
			panel.add(lblModel);

			JSeparator separator = new JSeparator();
			separator.setBounds(6, 32, 438, 2);
			panel.add(separator);

			JSeparator separator_1 = new JSeparator();
			separator_1.setBounds(6, 113, 438, 2);
			panel.add(separator_1);

			lblIcon = new JLabel("icon");
			//lblIcon.setIcon(new ImageIcon(frmProperties.class.getResource("/images/modelTree_16.png")));
			lblIcon.setBounds(100, 11, 16, 16);
			panel.add(lblIcon);

			JPanel panel_1 = new JPanel();
			panel_1.setBackground(SystemColor.window);
			tabbedPane.addTab("Model", null, panel_1, null);
			panel_1.setLayout(null);

			JLabel lblDimensions = new JLabel("Dimensions:");
			lblDimensions.setBounds(6, 11, 71, 16);
			panel_1.add(lblDimensions);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 34, 214, 126);
			panel_1.add(scrollPane);

			modelDimensions=new DefaultTableModel(new Object[][] {}, new String[] {"Dimension", "Symbol"});
			tableDimensions = new JTable();
			tableDimensions.setRowSelectionAllowed(false);
			tableDimensions.setModel(modelDimensions);
			scrollPane.setViewportView(tableDimensions);

			JLabel lblDecimalPrecision = new JLabel("Decimal precision:");
			lblDecimalPrecision.setBounds(6, 172, 110, 16);
			panel_1.add(lblDecimalPrecision);

			textDecimals = new JTextField();
			textDecimals.setBounds(120, 166, 44, 28);
			panel_1.add(textDecimals);
			textDecimals.setHorizontalAlignment(SwingConstants.CENTER);
			textDecimals.setText("6");
			textDecimals.setColumns(10);

			JLabel lblPlaces = new JLabel("places");
			lblPlaces.setBounds(176, 172, 44, 16);
			panel_1.add(lblPlaces);

			JButton btnAddDimension = new JButton("Add Dimension");
			btnAddDimension.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					modelDimensions.addRow(new Object[]{null});
				}
			});
			btnAddDimension.setBounds(252, 55, 162, 28);
			panel_1.add(btnAddDimension);

			JButton btnRemoveDimension = new JButton("Remove Dimension");
			btnRemoveDimension.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=tableDimensions.getSelectedRow();
					if(selected!=-1 && tableDimensions.getRowCount()>1){
						modelDimensions.removeRow(selected);
					}
				}
			});
			btnRemoveDimension.setBounds(252, 116, 162, 28);
			panel_1.add(btnRemoveDimension);

		} catch (Exception ex){
			ex.printStackTrace();
			panelTree.errorLog.recordError(ex);
		}
	}

	private void displayMetadata(Metadata meta){
		String modelTypes[]={"Decision Tree"};
		String icons[]={"/images/modelTree_16.png"};
		lblModel.setText(modelTypes[modelType]);
		lblIcon.setIcon(new ImageIcon(frmProperties.class.getResource(icons[modelType])));
		lblDispAuthor.setText(meta.author);
		lblDispCreated.setText(meta.dateCreated);
		lblDispVCreated.setText(meta.versionCreated); 
		lblDispModifer.setText(meta.modifier);
		lblDispModified.setText(meta.dateModified);
		lblVModified.setText(meta.versionModified);
	}
	
	private void displayModelSettings(){
		for(int i=0; i<modelTree.dimNames.length; i++){
			modelDimensions.addRow(new Object[]{null});
			modelDimensions.setValueAt(modelTree.dimNames[i], i, 0);
			modelDimensions.setValueAt(modelTree.dimSymbols[i], i, 1);
		}
		textDecimals.setText(modelTree.decimals+"");
	}
}
