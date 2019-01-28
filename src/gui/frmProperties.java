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
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import base.AmuaModel;
import main.DimInfo;
import main.Metadata;

import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmProperties {

	/**
	 * JFrame for form
	 */
	public JDialog frmProperties;
	AmuaModel myModel;
	DimInfo tempDimInfo;
	JTabbedPane tabbedPane;
	
	//Metadata
	JLabel lblName;
	JLabel lblModel;
	JLabel lblIcon;
	JLabel lblDispAuthor;
	JLabel lblDispCreated;
	JLabel lblDispVCreated;
	JLabel lblDispModifer;	
	JLabel lblDispModified; 
	JLabel lblVModified; 
	
	int numDimensions;
	ArrayList<String> dimNames, dimSymbols;
	ArrayList<Integer> decimals;
	ArrayList<Double> dimDiscount;
	
	//Analysis
	DefaultTableModel modelDimensions;
	private JTable tableDimensions;
	JComboBox<String> comboAnalysis;
	JButton btnRemoveDimension;
	private DefaultTableModel modelAnalysis;
	private analysisTable tableAnalysis;
	
	//Simulation
	JComboBox comboSimType;
	JLabel lblCohortSize;
	private JTextField textCohortSize;
	JCheckBox chckbxCRN;
	private JTextField textCRNSeed;
	
	//Markov
	private JTextField textMarkovMaxCycles;
	JCheckBox chckbxHalfcycleCorrection;
	JCheckBox chckbxDiscount;
	DefaultTableModel modelDiscountRates;
	private JTable tableDiscountRates;
	JLabel lblDiscountStartCycle;
	private JTextField textDiscountStartCycle;
	private JTextField textMarkovStateDecimals;
		
	/**
	 *  Default Constructor
	 */
	public frmProperties(AmuaModel myModel) {
		this.myModel=myModel;
		this.tempDimInfo=myModel.dimInfo.copy();
		myModel.getStrategies();
		initialize();
		refreshDisplay();
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

			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(applyChanges()){
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

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBounds(6, 6, 448, 234);
			frmProperties.getContentPane().add(tabbedPane);

			JPanel panel = new JPanel();
			tabbedPane.addTab("General", null, panel, null);
			panel.setBackground(SystemColor.window);
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setLayout(null);

			JLabel lblModelName = new JLabel("Model Name:");
			lblModelName.setBounds(6, 8, 73, 16);
			panel.add(lblModelName);
			
			lblName = new JLabel("[Name]");
			lblName.setBounds(120, 8, 325, 16);
			panel.add(lblName);
			
			JLabel lblAuthor = new JLabel("Created by:");
			lblAuthor.setBounds(6, 56, 66, 16);
			panel.add(lblAuthor);

			lblDispAuthor = new JLabel("[Author]");
			lblDispAuthor.setBounds(120, 56, 325, 16);
			panel.add(lblDispAuthor);

			JLabel lblCreated = new JLabel("Created:");
			lblCreated.setBounds(6, 80, 55, 16);
			panel.add(lblCreated);

			lblDispCreated = new JLabel("[Date created]");
			lblDispCreated.setBounds(120, 80, 325, 16);
			panel.add(lblDispCreated);

			JLabel lblVersionCreated = new JLabel("Version created:");
			lblVersionCreated.setBounds(6, 104, 105, 16);
			panel.add(lblVersionCreated);

			JLabel lblModifiedBy = new JLabel("Modified by:");
			lblModifiedBy.setBounds(6, 128, 73, 16);
			panel.add(lblModifiedBy);

			JLabel lblModelType = new JLabel("Model Type:");
			lblModelType.setBounds(6, 32, 73, 16);
			panel.add(lblModelType);

			JLabel lblModified = new JLabel("Modified:");
			lblModified.setBounds(6, 152, 56, 16);
			panel.add(lblModified);

			JLabel lblVersionModified = new JLabel("Version modified:");
			lblVersionModified.setBounds(6, 176, 105, 16);
			panel.add(lblVersionModified);

			lblDispVCreated = new JLabel("[Version]");
			lblDispVCreated.setBounds(120, 104, 325, 16);
			panel.add(lblDispVCreated);

			lblDispModifer = new JLabel("[Modifier]");
			lblDispModifer.setBounds(120, 128, 325, 16);
			panel.add(lblDispModifer);

			lblDispModified = new JLabel("[Date modified]");
			lblDispModified.setBounds(120, 152, 325, 16);
			panel.add(lblDispModified);

			lblVModified = new JLabel("[Version]");
			lblVModified.setBounds(120, 176, 325, 16);
			panel.add(lblVModified);

			lblModel = new JLabel("[Model]");
			lblModel.setBounds(120, 32, 325, 16);
			panel.add(lblModel);

			JSeparator separator = new JSeparator();
			separator.setBounds(6, 52, 438, 2);
			panel.add(separator);

			JSeparator separator_1 = new JSeparator();
			separator_1.setBounds(6, 124, 438, 2);
			panel.add(separator_1);

			lblIcon = new JLabel("icon");
			//lblIcon.setIcon(new ImageIcon(frmProperties.class.getResource("/images/modelTree_16.png")));
			lblIcon.setBounds(100, 32, 16, 16);
			panel.add(lblIcon);
			
			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_1.setBackground(SystemColor.window);
			tabbedPane.addTab("Analysis", null, panel_1, null);
			panel_1.setLayout(null);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 6, 273, 73);
			panel_1.add(scrollPane);

			modelDimensions=new DefaultTableModel(new Object[][] {}, new String[] {"Dimension", "Symbol", "Decimals"});
			tableDimensions = new JTable();
			tableDimensions.setRowSelectionAllowed(false);
			tableDimensions.getTableHeader().setReorderingAllowed(false);
			tableDimensions.setModel(modelDimensions);
			scrollPane.setViewportView(tableDimensions);

			JButton btnAddDimension = new JButton("");
			btnAddDimension.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					modelDimensions.addRow(new Object[]{null});
					if(modelDimensions.getRowCount()>1){btnRemoveDimension.setEnabled(true);}
					else{btnRemoveDimension.setEnabled(false);}
					if(myModel.type==1){ //Markov
						modelDiscountRates.addRow(new Object[]{null,"0"});
					}
				}
			});
			btnAddDimension.setToolTipText("Add Dimension");
			btnAddDimension.setIcon(new ImageIcon(frmDefineTable.class.getResource("/images/add.png")));
			btnAddDimension.setBounds(282, 11, 35, 28);
			panel_1.add(btnAddDimension);

			btnRemoveDimension = new JButton("");
			btnRemoveDimension.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=tableDimensions.getSelectedRow();
					if(selected!=-1 && tableDimensions.getRowCount()>1){
						modelDimensions.removeRow(selected);
						if(modelDimensions.getRowCount()>1){btnRemoveDimension.setEnabled(true);}
						else{btnRemoveDimension.setEnabled(false);}
						if(myModel.type==1){ //Markov
							modelDiscountRates.removeRow(selected);
						}
					}
				}
			});
			btnRemoveDimension.setBounds(282, 46, 35, 28);
			btnRemoveDimension.setToolTipText("Remove Dimension");
			btnRemoveDimension.setIcon(new ImageIcon(frmDefineTable.class.getResource("/images/remove.png")));
			panel_1.add(btnRemoveDimension);
			
			JLabel lblAnalysisType = new JLabel("Analysis type:");
			lblAnalysisType.setBounds(6, 91, 85, 16);
			panel_1.add(lblAnalysisType);
			
			comboAnalysis = new JComboBox<String>();
			comboAnalysis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAnalysisType(comboAnalysis.getSelectedIndex());
				}
			});
			comboAnalysis.setModel(new DefaultComboBoxModel<String>(new String[] {"Expected Value (EV)", "Cost-Effectiveness Analysis (CEA)", "Benefit-Cost Analysis (BCA)"}));
			comboAnalysis.setBounds(92, 86, 225, 26);
			panel_1.add(comboAnalysis);
			
			JScrollPane scrollPane_2 = new JScrollPane();
			scrollPane_2.setBounds(6, 122, 432, 76);
			panel_1.add(scrollPane_2);
			
			modelAnalysis=new DefaultTableModel(
					new Object[][] {
						{"Objective", null},
						{"Outcome", null},
					},
					new String[] {
						"", ""
					}
				) {
					boolean[] columnEditables = new boolean[] {
						false, true
					};
					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				};
			
			tableAnalysis = new analysisTable();
			tableAnalysis.myModel=myModel;
			tableAnalysis.tempDimInfo=tempDimInfo;
			tableAnalysis.getTableHeader().setReorderingAllowed(false);
			tableAnalysis.setModel(modelAnalysis);
			tableAnalysis.getColumnModel().getColumn(0).setPreferredWidth(170);
			tableAnalysis.getColumnModel().getColumn(1).setPreferredWidth(170);
			tableAnalysis.setShowVerticalLines(true);
			tableAnalysis.setRowSelectionAllowed(false);
			scrollPane_2.setViewportView(tableAnalysis);
			
			JButton btnRefreshDim = new JButton("Refresh");
			btnRefreshDim.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Update dimensions
					if(validateDimensions()){
						tempDimInfo.dimNames=new String[numDimensions]; tempDimInfo.dimSymbols=new String[numDimensions];
						tempDimInfo.decimals=new int[numDimensions];
						for(int i=0; i<numDimensions; i++){
							tempDimInfo.dimNames[i]=dimNames.get(i);
							tempDimInfo.dimSymbols[i]=dimSymbols.get(i);
							tempDimInfo.decimals[i]=decimals.get(i);
						}
						
						if(numDimensions==1){
							comboAnalysis.setEnabled(false);
							comboAnalysis.setSelectedIndex(0);
							setAnalysisType(0);
						}
						else{comboAnalysis.setEnabled(true);}
												
						//update discount names
						if(myModel.type==1){
							myModel.markov.discountRates=new double[numDimensions];
							for(int i=0; i<numDimensions; i++){
								modelDiscountRates.setValueAt(dimNames.get(i), i, 0);
								try{
									myModel.markov.discountRates[i]=Double.parseDouble((String) tableDiscountRates.getValueAt(i, 1));
								}catch (Exception e1){
									myModel.markov.discountRates[i]=0;
								}
							}
						}
					}
				}
			});
			btnRefreshDim.setIcon(new ImageIcon(frmDefineTable.class.getResource("/images/refresh_16.png")));
			btnRefreshDim.setBounds(329, 30, 109, 28);
			panel_1.add(btnRefreshDim);
			
			tableAnalysis.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			    @Override
			    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			        if(tableAnalysis.enabled[row]){setForeground(tableAnalysis.getForeground());}
			        else{setForeground(new Color(220,220,220));}
			        return this;
			    }   
			});
			
			JPanel panel_3 = new JPanel();
			panel_3.setBackground(SystemColor.window);
			tabbedPane.addTab("Simulation", null, panel_3, null);
			panel_3.setLayout(null);
			
			JLabel label = new JLabel("Simulation type:");
			label.setBounds(6, 11, 92, 16);
			panel_3.add(label);
			
			comboSimType = new JComboBox();
			comboSimType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=comboSimType.getSelectedIndex();
					if(selected==0){ //Cohort
						lblCohortSize.setText("Cohort size:");
						chckbxCRN.setEnabled(false);
						textCRNSeed.setEnabled(false);
					}
					else if(selected==1){ //Monte Carlo
						lblCohortSize.setText("# simulations:");
						chckbxCRN.setEnabled(true);
						if(chckbxCRN.isSelected()){textCRNSeed.setEnabled(true);}
						else{textCRNSeed.setEnabled(false);}
					}
				}
			});
			comboSimType.setModel(new DefaultComboBoxModel(new String[] {"Cohort (Deterministic)", "Monte Carlo (Stochastic)"}));
			comboSimType.setBounds(100, 6, 167, 26);
			panel_3.add(comboSimType);
			
			lblCohortSize = new JLabel("Cohort size:");
			lblCohortSize.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCohortSize.setBounds(6, 46, 92, 16);
			panel_3.add(lblCohortSize);
			
			textCohortSize = new JTextField();
			textCohortSize.setColumns(10);
			textCohortSize.setBounds(103, 40, 105, 28);
			panel_3.add(textCohortSize);
			
			chckbxCRN = new JCheckBox("Seed RNG");
			chckbxCRN.setEnabled(false);
			chckbxCRN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxCRN.isSelected()){textCRNSeed.setEnabled(true);}
					else{textCRNSeed.setEnabled(false);}
				}
			});
			chckbxCRN.setBounds(15, 79, 92, 18);
			panel_3.add(chckbxCRN);
			
			JLabel lblSeed = new JLabel("Seed:");
			lblSeed.setBounds(107, 80, 37, 16);
			panel_3.add(lblSeed);
			
			textCRNSeed = new JTextField();
			textCRNSeed.setEnabled(false);
			textCRNSeed.setText("999");
			textCRNSeed.setBounds(142, 74, 66, 28);
			panel_3.add(textCRNSeed);
			textCRNSeed.setColumns(10);
			
			JPanel testDiscountStartCycle = new JPanel();
			testDiscountStartCycle.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			testDiscountStartCycle.setBackground(SystemColor.window);
			tabbedPane.addTab("Markov", null, testDiscountStartCycle, null);
			tabbedPane.setEnabledAt(3, false);
			testDiscountStartCycle.setLayout(null);
			
			JLabel lblMaxCycles = new JLabel("Max cycles:");
			lblMaxCycles.setBounds(6, 23, 71, 16);
			testDiscountStartCycle.add(lblMaxCycles);
			
			textMarkovMaxCycles = new JTextField();
			textMarkovMaxCycles.setBounds(72, 17, 71, 28);
			testDiscountStartCycle.add(textMarkovMaxCycles);
			textMarkovMaxCycles.setColumns(10);
			
			chckbxHalfcycleCorrection = new JCheckBox("Half-cycle correction");
			chckbxHalfcycleCorrection.setBounds(6, 57, 141, 18);
			testDiscountStartCycle.add(chckbxHalfcycleCorrection);
			
			lblDiscountStartCycle = new JLabel("Discount start cycle:");
			lblDiscountStartCycle.setEnabled(false);
			lblDiscountStartCycle.setBounds(144, 86, 116, 16);
			testDiscountStartCycle.add(lblDiscountStartCycle);
			
			textDiscountStartCycle = new JTextField();
			textDiscountStartCycle.setEnabled(false);
			textDiscountStartCycle.setBounds(254, 80, 57, 28);
			testDiscountStartCycle.add(textDiscountStartCycle);
			textDiscountStartCycle.setColumns(10);
			
			chckbxDiscount = new JCheckBox("Discount Rewards");
			chckbxDiscount.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxDiscount.isSelected()){
						lblDiscountStartCycle.setEnabled(true);
						textDiscountStartCycle.setEnabled(true);
						tableDiscountRates.setEnabled(true);
					}
					else{
						lblDiscountStartCycle.setEnabled(false);
						textDiscountStartCycle.setEnabled(false);
						tableDiscountRates.setEnabled(false);
					}
				}
			});
			chckbxDiscount.setBounds(6, 85, 141, 18);
			testDiscountStartCycle.add(chckbxDiscount);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 115, 233, 71);
			testDiscountStartCycle.add(scrollPane_1);
			
			modelDiscountRates=new DefaultTableModel(
					new Object[][] {},
					new String[] {"Dimension", "Discount Rate (%)"}
				) {
					boolean[] columnEditables = new boolean[] {false, true};
					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				};
			tableDiscountRates = new JTable();
			tableDiscountRates.setModel(modelDiscountRates);
			tableDiscountRates.setRowSelectionAllowed(false);
			tableDiscountRates.getTableHeader().setReorderingAllowed(false);
			tableDiscountRates.setEnabled(false);
			scrollPane_1.setViewportView(tableDiscountRates);
			
			JLabel lblStatePrevalenceDecimals = new JLabel("State Prevalence Decimals:");
			lblStatePrevalenceDecimals.setBounds(184, 23, 158, 16);
			testDiscountStartCycle.add(lblStatePrevalenceDecimals);
			
			textMarkovStateDecimals = new JTextField();
			textMarkovStateDecimals.setBounds(340, 17, 47, 28);
			testDiscountStartCycle.add(textMarkovStateDecimals);
			textMarkovStateDecimals.setColumns(10);
			
			
			
			
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void refreshDisplay(){
		displayMetadata(myModel.meta);
		displayAnalysisSettings();
		displaySimSettings();
		if(myModel.type==1){
			displayMarkovSettings();
		}
	}
	
	private void displayMetadata(Metadata meta){
		String modelTypes[]={"Decision Tree","Markov Model"};
		String icons[]={"/images/modelTree_16.png","/images/markovChain_16.png"};
		lblName.setText(myModel.name);
		lblModel.setText(modelTypes[myModel.type]);
		lblIcon.setIcon(new ImageIcon(frmProperties.class.getResource(icons[myModel.type])));
		lblDispAuthor.setText(meta.author);
		lblDispCreated.setText(meta.dateCreated);
		lblDispVCreated.setText(meta.versionCreated); 
		lblDispModifer.setText(meta.modifier);
		lblDispModified.setText(meta.dateModified);
		lblVModified.setText(meta.versionModified);
	}
	
	private void displayAnalysisSettings(){
		modelDimensions.setRowCount(0);
		for(int i=0; i<tempDimInfo.dimNames.length; i++){
			modelDimensions.addRow(new Object[]{null});
			modelDimensions.setValueAt(tempDimInfo.dimNames[i], i, 0);
			modelDimensions.setValueAt(tempDimInfo.dimSymbols[i], i, 1);
			modelDimensions.setValueAt(tempDimInfo.decimals[i]+"", i, 2);
		}
		
		if(tempDimInfo.dimNames.length<2){
			btnRemoveDimension.setEnabled(false);
		}
		
		
		comboAnalysis.setSelectedIndex(tempDimInfo.analysisType);
		
		setAnalysisType(tempDimInfo.analysisType);
		if(tempDimInfo.analysisType==0){ //EV
			if(tempDimInfo.objective==0){tableAnalysis.setValueAt("Maximize", 0, 1);}
			else if(tempDimInfo.objective==1){tableAnalysis.setValueAt("Minimize", 0, 1);}
			tableAnalysis.setValueAt(tempDimInfo.dimNames[tempDimInfo.objectiveDim], 1, 1);
		}
		else if(tempDimInfo.analysisType>0){ //CEA or BCA
			tableAnalysis.setValueAt(tempDimInfo.dimNames[tempDimInfo.costDim], 0, 1);
			tableAnalysis.setValueAt(tempDimInfo.dimNames[tempDimInfo.effectDim], 1, 1);
			if(tempDimInfo.analysisType==1){ //CEA
				tableAnalysis.setValueAt(myModel.strategyNames[tempDimInfo.baseScenario], 2, 1);
			}
			tableAnalysis.setValueAt(tempDimInfo.WTP+"",3,1);
		}
		
	}
	
	private void displaySimSettings(){
		comboSimType.setSelectedIndex(myModel.simType);
		textCohortSize.setText(myModel.cohortSize+"");
		chckbxCRN.setSelected(myModel.CRN);
		if(myModel.CRN){
			textCRNSeed.setEnabled(true);
			textCRNSeed.setText(myModel.crnSeed+"");
		}
		
	}
	
	private void displayMarkovSettings(){
		tabbedPane.setEnabledAt(3, true);
		textMarkovMaxCycles.setText(myModel.markov.maxCycles+"");
		textMarkovStateDecimals.setText(myModel.markov.stateDecimals+"");
		chckbxHalfcycleCorrection.setSelected(myModel.markov.halfCycleCorrection);
		chckbxDiscount.setSelected(myModel.markov.discountRewards);
		if(myModel.markov.discountRewards==true){
			lblDiscountStartCycle.setEnabled(true);
			textDiscountStartCycle.setEnabled(true);
			tableDiscountRates.setEnabled(true);
		}
		textDiscountStartCycle.setText(myModel.markov.discountStartCycle+"");
		modelDiscountRates.setRowCount(0);
		for(int i=0; i<tempDimInfo.dimNames.length; i++){
			modelDiscountRates.addRow(new Object[]{null});
			modelDiscountRates.setValueAt(tempDimInfo.dimNames[i],i,0);
			if(myModel.markov.discountRates!=null){
				modelDiscountRates.setValueAt(myModel.markov.discountRates[i]+"", i, 1);
			}
		}
	}
	
	private boolean validateDimensions(){
		boolean valid=true;
		//Validate dimensions
		//Ensure unqiue
		numDimensions=tableDimensions.getRowCount();
		dimNames=new ArrayList<String>();
		dimSymbols=new ArrayList<String>();
		decimals=new ArrayList<Integer>();
		dimDiscount=new ArrayList<Double>();
		for(int i=0; i<numDimensions; i++){
			String curName=(String) tableDimensions.getValueAt(i,0);
			String curSymbol=(String) tableDimensions.getValueAt(i,1);
			String curDecimal=(String) tableDimensions.getValueAt(i, 2);
			if(curName==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+"Please enter a valid dimension name!");
			}
			else if(dimNames.contains(curName)){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+" is already defined!");
			}
			else{
				dimNames.add(curName);
			}

			if(curSymbol==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+"Please enter a valid dimension symbol!");
			}
			else if(dimSymbols.contains(curSymbol)){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curSymbol+ "is already defined!");
			}
			else{
				dimSymbols.add(curSymbol);
			}

			if(curDecimal==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+"Please enter decimal places!");
			}
			else{
				//Try to convert to integer
				int curDec=0;
				try{
					curDec=Integer.parseInt(curDecimal);
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please enter a valid integer for decimal places!");
				}
				if(valid==true){ //Integer entered
					if(curDec<0){
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, "Please enter a valid integer!");
					}
					else{
						decimals.add(curDec);
					}
				}
			}
		}
		return(valid);
	}
	
	private boolean applyChanges(){
		boolean valid=true;
		
		//Validate dimensions
		valid=validateDimensions();
		
		//Check analysis settings
		int objective=0, objectiveDim=0;
		int costDim=-1, effectDim=-1;
		int baseIndex=-1;
		double WTP=0;
		if(comboAnalysis.getSelectedIndex()==0){ //EV
			String strObj=(String) tableAnalysis.getValueAt(0, 1);
			if(strObj==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please select an objective!");
			}
			else{
				if(strObj.matches("Maximize")){objective=0;}
				else{objective=1;}
			}
			String strDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please select an outcome!");
			}
			else{
				objectiveDim=getDimIndex(strDim);
			}
		}
		else if(comboAnalysis.getSelectedIndex()>0){ //CEA or BCA
			String strCostDim=(String) tableAnalysis.getValueAt(0, 1);
			if(strCostDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please select a Cost!");
			}
			else{costDim=getDimIndex(strCostDim);}
			String strEffectDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strEffectDim==null){
				if(comboAnalysis.getSelectedIndex()==1){JOptionPane.showMessageDialog(frmProperties, "Please select an Effect!");} //CEA
				else if(comboAnalysis.getSelectedIndex()==2){JOptionPane.showMessageDialog(frmProperties, "Please select a Benefit!");} //BCA
			}
			else{
				effectDim=getDimIndex(strEffectDim);
			}
			if(costDim==effectDim){
				valid=false;
				if(comboAnalysis.getSelectedIndex()==1){ //CEA
					JOptionPane.showMessageDialog(frmProperties, "Cost and Effect must be different!");
				}
				else if(comboAnalysis.getSelectedIndex()==2){ //BCA
					JOptionPane.showMessageDialog(frmProperties, "Cost and Benefit must be different!");
				}
			}
			if(comboAnalysis.getSelectedIndex()==1){ //CEA
				String strBase=(String)tableAnalysis.getValueAt(2, 1);
				baseIndex=-1;
				if(strBase!=null){baseIndex=getScenarioIndex(strBase);}
				if(baseIndex==-1){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please choose a baseline scenario!");
				}
			}
			try{
				String strWTP=(String) tableAnalysis.getValueAt(3, 1);
				WTP=Double.parseDouble(strWTP.replaceAll(",",""));
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid willingness-to-pay!");
			}
		}
		
		//Check simulation settings
		int simType=comboSimType.getSelectedIndex();
		int cohortSize=-1;
		boolean CRN=false;
		int crnSeed=-1;
		if(simType==0){ //Cohort
			try{
				String text=textCohortSize.getText().replaceAll(",",""); //remove commas
				cohortSize=Integer.parseInt(text);
				if(cohortSize<=0){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please enter a valid Cohort Size!");
				}
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid Cohort Size!");
			}
		}
		else if(simType==1){ //Monte Carlo
			try{
				String text=textCohortSize.getText().replaceAll(",",""); //remove commas
				cohortSize=Integer.parseInt(text);
				if(cohortSize<=0){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of Monte Carlo simulations!");
				}
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of Monte Carlo simulations!");
			}
			
			CRN=chckbxCRN.isSelected(); //CRN
			if(CRN){ //get seed
				try{
					String text=textCRNSeed.getText().replaceAll(",",""); //remove commas
					crnSeed=Integer.parseInt(text);
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please enter a valid CRN seed!");
				}
			}
		}
		
		//Check Markov settings
		int maxCycles=10000, statePrevDecimals=4;
		boolean halfCycleCorrection=false;
		if(myModel.type==1){
			//max cycles
			try{
				String text=textMarkovMaxCycles.getText().replaceAll(",",""); //remove commas
				maxCycles=Integer.parseInt(text);
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of max cycles!");
			}
			if(maxCycles<0){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of max cycles!");
			}
			
			//state prev decimals
			try{
				String text=textMarkovStateDecimals.getText().replaceAll(",",""); //remove commas
				statePrevDecimals=Integer.parseInt(text);
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of Markov State prevalence decimals!");
			}
			if(statePrevDecimals<0){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, "Please enter a valid number of Markov State prevalence decimals!");
			}
			
			halfCycleCorrection=chckbxHalfcycleCorrection.isSelected();
			
			//discount rates
			if(chckbxDiscount.isSelected()){
				try{
					int test=Integer.parseInt(textDiscountStartCycle.getText());
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, "Please enter a valid discount start cycle ("+textDiscountStartCycle.getText()+")");
				}
				
				for(int i=0; i<numDimensions; i++){
					try{
						double test=Double.parseDouble((String) tableDiscountRates.getValueAt(i, 1));
						dimDiscount.add(test);
					} catch(Exception er){
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, "Please enter a valid discount rate ("+tableDiscountRates.getValueAt(i, 0)+")");
					}
				}
			}
			
		}
		
		if(valid==true){ //Apply changes
			//add to undo
			myModel.saveSnapshot("Change Properties");
			
			//dimensions
			myModel.dimInfo.dimNames=new String[numDimensions]; myModel.dimInfo.dimSymbols=new String[numDimensions];
			myModel.dimInfo.decimals=new int[numDimensions];
			for(int i=0; i<numDimensions; i++){
				myModel.dimInfo.dimNames[i]=dimNames.get(i);
				myModel.dimInfo.dimSymbols[i]=dimSymbols.get(i);
				myModel.dimInfo.decimals[i]=decimals.get(i);
			}
			myModel.updateDimensions();
			
			//analysis type
			myModel.dimInfo.analysisType=comboAnalysis.getSelectedIndex();
			myModel.dimInfo.objective=objective; myModel.dimInfo.objectiveDim=objectiveDim;
			myModel.dimInfo.costDim=costDim; myModel.dimInfo.effectDim=effectDim;
			myModel.dimInfo.baseScenario=baseIndex;
			myModel.dimInfo.WTP=WTP;
			
			tempDimInfo=myModel.dimInfo.copy(); //get new copy
			tableAnalysis.tempDimInfo=tempDimInfo;
			
			//simulation
			myModel.simType=simType;
			myModel.cohortSize=cohortSize;
			myModel.CRN=CRN;
			myModel.crnSeed=crnSeed;
			
			//markov settings
			if(myModel.type==1){
				myModel.markov.maxCycles=maxCycles;
				myModel.markov.stateDecimals=statePrevDecimals;
				myModel.markov.halfCycleCorrection=halfCycleCorrection;
				
				myModel.markov.discountRewards=chckbxDiscount.isSelected();
				if(myModel.markov.discountRewards==true){
					myModel.markov.discountStartCycle=Integer.parseInt(textDiscountStartCycle.getText());
					myModel.markov.discountRates=new double[numDimensions];
					for(int i=0; i<numDimensions; i++){
						myModel.markov.discountRates[i]=dimDiscount.get(i);
					}
				}
			}
		}
		
		return(valid);
	}
	
	
	private void setAnalysisType(int analysisType){
		if(tempDimInfo.dimNames.length==1 || modelDimensions.getRowCount()<2){comboAnalysis.setEnabled(false);}
		else{comboAnalysis.setEnabled(true);}
				
		tableAnalysis.analysisType=analysisType;
		if(analysisType==0){ //EV
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{"Objective",null}); tableAnalysis.enabled[0]=true;
			modelAnalysis.addRow(new Object[]{"Outcome",null}); tableAnalysis.enabled[1]=true;
		}
		else if(analysisType==1 || analysisType==2){ //CEA or BCA
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{"Cost",null}); tableAnalysis.enabled[0]=true;
			modelAnalysis.addRow(new Object[]{"Effect",null}); tableAnalysis.enabled[1]=true;
			modelAnalysis.addRow(new Object[]{"Baseline Strategy",null}); tableAnalysis.enabled[2]=true;
			modelAnalysis.addRow(new Object[]{"Willingness-to-pay (WTP)",null}); tableAnalysis.enabled[3]=true;
			if(analysisType==1){ //CEA
				tableAnalysis.setValueAt("Effect", 1, 0);
			}
			else{ //BCA
				tableAnalysis.setValueAt("Benefit", 1, 0);
				tableAnalysis.enabled[2]=false; //baseline strategy
			}
		}
		
		tableAnalysis.repaint();
	}
	
	private int getDimIndex(String dimName){
		int index=-1;
		int i=-1;
		boolean found=false;
		while(found==false && i<tempDimInfo.dimNames.length){
			i++;
			if(tempDimInfo.dimNames[i].matches(dimName)){
				found=true;
				index=i;
			}
		}
		return(index);
	}

	private int getScenarioIndex(String scenName){
		int index=-1;
		int i=-1;
		boolean found=false;
		while(found==false && i<myModel.strategyNames.length){
			i++;
			if(myModel.strategyNames[i].matches(scenName)){
				found=true;
				index=i;
			}
		}
		return(index);
	}
}


class analysisTable extends JTable{
	public int analysisType=0;
	public boolean enabled[]=new boolean[]{false,false,false,false};
	analysisTable thisTable=this;
	public AmuaModel myModel;
	public DimInfo tempDimInfo;;
	
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(int i=0; i<4; i++){this.enabled[i]=enabled;}
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column){
		if(analysisType==0){ //EV
			if(column==1){
				if(row==0){ //not editable
					JComboBox<String> comboRule = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"Maximize","Minimize"}));
					return(new DefaultCellEditor(comboRule));
				}
				else if(column==1){ //dimension
					JComboBox<String> comboDim = new JComboBox<String>(new DefaultComboBoxModel(tempDimInfo.dimNames));
					return(new DefaultCellEditor(comboDim));
				}
			}
		}
		else if(analysisType==1 || analysisType==2){ //CEA/BCA
			if(column==1){
				if(row==0 || row==1){ //Cost/Effect
					JComboBox<String> comboDim = new JComboBox<String>(new DefaultComboBoxModel(tempDimInfo.dimNames));
					return(new DefaultCellEditor(comboDim));
				}
				else if(row==2){ //Baseline scenario
					myModel.getStrategies();
					JComboBox<String> comboScen = new JComboBox<String>(new DefaultComboBoxModel(myModel.strategyNames));
					comboScen.setEnabled(enabled[2]);
					return(new DefaultCellEditor(comboScen));
				}
				else if(row==3){ //Willingness to pay
					return(thisTable.getDefaultEditor(Object.class));
				}
			}
		}
		return(null);
	}
}