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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.SystemColor;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import base.AmuaModel;
import main.DimInfo;
import main.Metadata;
import main.ScaledIcon;
import math.Interpreter;
import math.Numeric;

import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Toolkit;

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
	
	frmProperties form=this;
	
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
	//ArrayList<Double> dimDiscount;
	ArrayList<String> prevDimNames;
	String tempDiscountRates[];
	
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
	JCheckBox chckbxDisplayIndResults;
	JCheckBox chckbxMultithread;
	private JTextField textNumThreads;
	JLabel lblThreads;
	JButton btnSetToMax;
	
	//Markov
	private JTextField textMarkovMaxCycles;
	JCheckBox chckbxHalfcycleCorrection;
	JCheckBox chckbxDiscount;
	DefaultTableModel modelDiscountRates;
	private JTable tableDiscountRates;
	JLabel lblDiscountStartCycle;
	private JTextField textDiscountStartCycle;
	JLabel lblCyclesPerYear;
	private JTextField textCyclesPerYear;
	JCheckBox chckbxShowMarkovTrace;
	JCheckBox chckbxCompileTraces;
	private JTextField textMarkovStateDecimals;
	
	//Subgroups
	JCheckBox chckbxSubgroups;
	ArrayList<String> subgroupNames, subgroupDefs;
	JButton btnRemoveSubgroup;
	DefaultTableModel modelSubgroups;
	private JTable tableSubgroups;
		
		
	/**
	 *  Default Constructor
	 */
	public frmProperties(AmuaModel myModel) {
		try {
			this.myModel=myModel;
			this.tempDimInfo=myModel.dimInfo.copy();
			int numDim=tempDimInfo.dimNames.length;
			numDimensions=numDim;
			prevDimNames=new ArrayList<String>();
			for(int d=0; d<numDim; d++) {
				prevDimNames.add(tempDimInfo.dimNames[d]);
			}
			if(myModel.type==1) { //markov
				tempDiscountRates=new String[numDim];
				if(myModel.markov.discountRates!=null) {
					int minNum=Math.min(numDim, myModel.markov.discountRates.length); //don't go over length of array if shorter for some reason,
					for(int d=0; d<minNum; d++) {
						tempDiscountRates[d]=myModel.markov.discountRates[d];
					}
				}
			}

			myModel.getStrategies();
			initialize();
			refreshDisplay();
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmProperties = new JDialog();
			frmProperties.setIconImage(Toolkit.getDefaultToolkit().getImage(frmProperties.class.getResource("/images/properties_128.png")));
			frmProperties.getContentPane().setBackground(SystemColor.control);
			frmProperties.setModalityType(ModalityType.APPLICATION_MODAL);
			frmProperties.setTitle("Amua - "+myModel.language.base.getString("menu.properties")); //Properties
			frmProperties.setResizable(false);
			frmProperties.setBounds(100, 100, 466, 332);
			frmProperties.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmProperties.getContentPane().setLayout(null);

			JButton btnOk = new JButton(myModel.language.base.getString("button.ok")); //OK
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(applyChanges()){
						frmProperties.dispose();
					}
				}
			});
			btnOk.setBounds(262, 263, 90, 28);
			frmProperties.getContentPane().add(btnOk);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmProperties.dispose();
				}
			});
			btnCancel.setBounds(364, 263, 90, 28);
			frmProperties.getContentPane().add(btnCancel);

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBounds(6, 6, 448, 251);
			frmProperties.getContentPane().add(tabbedPane);

			//General ###############################################################################
			
			JPanel panelGeneral = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("title.general"), null, panelGeneral, null); //General
			panelGeneral.setBackground(SystemColor.window);
			panelGeneral.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelGeneral.setLayout(null);

			JLabel lblModelName = new JLabel(myModel.language.base.getString("meta.model_name")+":"); //Model Name
			lblModelName.setBounds(6, 8, 115, 16);
			panelGeneral.add(lblModelName);
			
			lblName = new JLabel("[Name]");
			lblName.setBounds(130, 8, 315, 16);
			panelGeneral.add(lblName);
			
			JLabel lblAuthor = new JLabel(myModel.language.base.getString("meta.created_by")+":"); //Created by
			lblAuthor.setBounds(6, 56, 115, 16);
			panelGeneral.add(lblAuthor);

			lblDispAuthor = new JLabel("[Author]");
			lblDispAuthor.setBounds(130, 56, 315, 16);
			panelGeneral.add(lblDispAuthor);

			JLabel lblCreated = new JLabel(myModel.language.base.getString("meta.created")+":"); //Created
			lblCreated.setBounds(6, 80, 115, 16);
			panelGeneral.add(lblCreated);

			lblDispCreated = new JLabel("[Date created]");
			lblDispCreated.setBounds(130, 80, 315, 16);
			panelGeneral.add(lblDispCreated);

			JLabel lblVersionCreated = new JLabel(myModel.language.base.getString("meta.version_created")+":"); //Version created
			lblVersionCreated.setBounds(6, 104, 115, 16);
			panelGeneral.add(lblVersionCreated);

			JLabel lblModifiedBy = new JLabel(myModel.language.base.getString("meta.modified_by")+":"); //Modified by
			lblModifiedBy.setBounds(6, 128, 115, 16);
			panelGeneral.add(lblModifiedBy);

			JLabel lblModelType = new JLabel(myModel.language.base.getString("meta.model_type")+":"); //Model Type
			lblModelType.setBounds(6, 32, 105, 16);
			panelGeneral.add(lblModelType);

			JLabel lblModified = new JLabel(myModel.language.base.getString("meta.modified")+":"); //Modified
			lblModified.setBounds(6, 152, 115, 16);
			panelGeneral.add(lblModified);

			JLabel lblVersionModified = new JLabel(myModel.language.base.getString("meta.version_modified")+":"); //Version modified
			lblVersionModified.setBounds(6, 176, 115, 16);
			panelGeneral.add(lblVersionModified);

			lblDispVCreated = new JLabel("[Version]");
			lblDispVCreated.setBounds(130, 104, 315, 16);
			panelGeneral.add(lblDispVCreated);

			lblDispModifer = new JLabel("[Modifier]");
			lblDispModifer.setBounds(130, 128, 315, 16);
			panelGeneral.add(lblDispModifer);

			lblDispModified = new JLabel("[Date modified]");
			lblDispModified.setBounds(130, 152, 315, 16);
			panelGeneral.add(lblDispModified);

			lblVModified = new JLabel("[Version]");
			lblVModified.setBounds(130, 176, 315, 16);
			panelGeneral.add(lblVModified);

			lblModel = new JLabel("[Model]");
			lblModel.setBounds(130, 32, 315, 16);
			panelGeneral.add(lblModel);

			JSeparator separator = new JSeparator();
			separator.setBounds(6, 52, 438, 2);
			panelGeneral.add(separator);

			JSeparator separator_1 = new JSeparator();
			separator_1.setBounds(6, 124, 438, 2);
			panelGeneral.add(separator_1);

			lblIcon = new JLabel("icon");
			lblIcon.setBounds(110, 32, 16, 16);
			panelGeneral.add(lblIcon);
			
			//Analysis ###############################################################################
			
			JPanel panelAnalysis = new JPanel();
			panelAnalysis.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelAnalysis.setBackground(SystemColor.window);
			tabbedPane.addTab(myModel.language.analysis.getString("gen.analysis"), null, panelAnalysis, null); //Analysis
			panelAnalysis.setLayout(null);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 6, 273, 84);
			panelAnalysis.add(scrollPane);

			modelDimensions=new DefaultTableModel(new Object[][] {}, new String[] {
					myModel.language.analysis.getString("gen.dimension"), //Dimension
					myModel.language.analysis.getString("gen.symbol"), //Symbol
					myModel.language.analysis.getString("gen.decimals")}); //Decimals
			tableDimensions = new JTable();
			tableDimensions.setRowSelectionAllowed(false);
			tableDimensions.getTableHeader().setReorderingAllowed(false);
			tableDimensions.setModel(modelDimensions);
			tableDimensions.putClientProperty("terminateEditOnFocusLost", true);
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
			btnAddDimension.setToolTipText(myModel.language.base.getString("button.add_dimension")); //Add Dimension
			btnAddDimension.setIcon(new ScaledIcon("/images/add",16,16,16,true));
			btnAddDimension.setBounds(282, 11, 35, 28);
			panelAnalysis.add(btnAddDimension);

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
			btnRemoveDimension.setToolTipText(myModel.language.base.getString("button.remove_dimension")); //Remove Dimension
			btnRemoveDimension.setIcon(new ScaledIcon("/images/remove",16,16,16,true));
			btnRemoveDimension.setDisabledIcon(new ScaledIcon("/images/remove",16,16,16,false));
			panelAnalysis.add(btnRemoveDimension);
			
			JLabel lblAnalysisType = new JLabel(myModel.language.analysis.getString("gen.analysis_type")+":"); //Analysis type
			lblAnalysisType.setHorizontalAlignment(SwingConstants.RIGHT);
			lblAnalysisType.setBounds(6, 98, 95, 16);
			panelAnalysis.add(lblAnalysisType);
			
			comboAnalysis = new JComboBox<String>();
			comboAnalysis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAnalysisType(comboAnalysis.getSelectedIndex());
				}
			});
			comboAnalysis.setModel(new DefaultComboBoxModel(new String[] {
					myModel.language.analysis.getString("gen.expected_value"), //Expected Value (EV)
					myModel.language.analysis.getString("cea.cost_effectiveness_analysis"), //Cost-Effectiveness Analysis (CEA)
					myModel.language.analysis.getString("bca.benefit_cost_analysis"), //Benefit-Cost Analysis (BCA)
					myModel.language.analysis.getString("cea.extended_cea")})); //Extended Cost-Effectiveness Analysis (ECEA)
			comboAnalysis.setBounds(103, 93, 282, 26);
			panelAnalysis.add(comboAnalysis);
			
			JScrollPane scrollPane_2 = new JScrollPane();
			scrollPane_2.setBounds(6, 122, 432, 93);
			panelAnalysis.add(scrollPane_2);
			
			modelAnalysis=new DefaultTableModel(
					new Object[][] {
						{myModel.language.analysis.getString("gen.objective"), null}, //Objective
						{myModel.language.analysis.getString("result.outcome"), null}, //Outcome
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
			tableAnalysis.putClientProperty("terminateEditOnFocusLost", true);
			scrollPane_2.setViewportView(tableAnalysis);
			
			JButton btnRefreshDim = new JButton(myModel.language.base.getString("button.refresh")); //Refresh
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
							tempDiscountRates=new String[numDimensions];
							for(int i=0; i<numDimensions; i++){
								modelDiscountRates.setValueAt(dimNames.get(i), i, 0);
								try{
									//tempDiscountRates[i]=Double.parseDouble((String) tableDiscountRates.getValueAt(i, 1));
									tempDiscountRates[i]=(String) tableDiscountRates.getValueAt(i, 1);
								}catch (Exception e1){
									tempDiscountRates[i]="0";
								}
							}
						}
					}
				}
			});
			btnRefreshDim.setIcon(new ScaledIcon("/images/refresh",16,16,16,true));
			btnRefreshDim.setBounds(329, 30, 109, 28);
			panelAnalysis.add(btnRefreshDim);
			
			tableAnalysis.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			    @Override
			    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			        if(tableAnalysis.enabled[row]){setForeground(tableAnalysis.getForeground());}
			        else{setForeground(new Color(220,220,220));}
			        return this;
			    }   
			});
			
			//Simulation ###############################################################################
			
			JPanel panelSimulation = new JPanel();
			panelSimulation.setBackground(SystemColor.window);
			tabbedPane.addTab(myModel.language.analysis.getString("sim.simulation"), null, panelSimulation, null); //Simulation
			panelSimulation.setLayout(null);
			
			JLabel label = new JLabel(myModel.language.analysis.getString("sim.simulation_type")+":"); //Simulation type
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBounds(4, 11, 110, 16);
			panelSimulation.add(label);
			
			comboSimType = new JComboBox();
			comboSimType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=comboSimType.getSelectedIndex();
					if(selected==0){ //Cohort
						lblCohortSize.setText(myModel.language.analysis.getString("sim.cohort_size")+":"); //Cohort size
						chckbxCRN.setEnabled(false);
						textCRNSeed.setEnabled(false);
						chckbxDisplayIndResults.setEnabled(false);
						tabbedPane.setEnabledAt(4, false); //no subgroups
					}
					else if(selected==1){ //Monte Carlo
						lblCohortSize.setText(myModel.language.analysis.getString("sim.num_simulations")+":"); //# simulations
						chckbxCRN.setEnabled(true);
						if(chckbxCRN.isSelected()){textCRNSeed.setEnabled(true);}
						else{textCRNSeed.setEnabled(false);}
						chckbxDisplayIndResults.setEnabled(true);
						tabbedPane.setEnabledAt(4, true);
					}
				}
			});
			comboSimType.setModel(new DefaultComboBoxModel(new String[] {
					myModel.language.analysis.getString("sim.cohort_deterministic"), //Cohort (Deterministic)
					myModel.language.analysis.getString("sim.monte_carlo_stochastic")})); //Monte Carlo (Stochastic)
			comboSimType.setBounds(115, 6, 181, 26);
			panelSimulation.add(comboSimType);
			
			lblCohortSize = new JLabel(myModel.language.analysis.getString("sim.cohort_size")+":"); //Cohort size
			lblCohortSize.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCohortSize.setBounds(6, 45, 108, 16);
			panelSimulation.add(lblCohortSize);
			
			textCohortSize = new JTextField();
			textCohortSize.setColumns(10);
			textCohortSize.setBounds(115, 39, 105, 28);
			panelSimulation.add(textCohortSize);
			
			chckbxCRN = new JCheckBox(myModel.language.analysis.getString("sim.seed_rng")); //Seed RNG
			chckbxCRN.setEnabled(false);
			chckbxCRN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxCRN.isSelected()){textCRNSeed.setEnabled(true);}
					else{textCRNSeed.setEnabled(false);}
				}
			});
			chckbxCRN.setBounds(15, 78, 188, 18);
			panelSimulation.add(chckbxCRN);
			
			JLabel lblSeed = new JLabel(myModel.language.analysis.getString("sim.seed")+":"); //Seed
			lblSeed.setBounds(204, 79, 43, 16);
			panelSimulation.add(lblSeed);
			
			textCRNSeed = new JTextField();
			textCRNSeed.setEnabled(false);
			textCRNSeed.setText("999");
			textCRNSeed.setBounds(250, 73, 66, 28);
			panelSimulation.add(textCRNSeed);
			textCRNSeed.setColumns(10);
			
			JLabel lblstOrder = new JLabel(myModel.language.analysis.getString("sim.1st_order_uncertainty")); //1st-order uncertainty
			lblstOrder.setHorizontalAlignment(SwingConstants.LEFT);
			lblstOrder.setFont(new Font("SansSerif", Font.PLAIN, 9));
			lblstOrder.setBounds(298, 11, 144, 16);
			panelSimulation.add(lblstOrder);
			
			chckbxDisplayIndResults = new JCheckBox(myModel.language.analysis.getString("result.display_ind_results")); //Display individual-level results
			chckbxDisplayIndResults.setEnabled(false);
			chckbxDisplayIndResults.setBounds(15, 107, 281, 18);
			panelSimulation.add(chckbxDisplayIndResults);
						
			lblThreads = new JLabel(myModel.language.analysis.getString("sim.threads").toLowerCase(myModel.language.locale)); //threads
			lblThreads.setEnabled(false);
			lblThreads.setBounds(92, 161, 55, 16);
			panelSimulation.add(lblThreads);
			
			btnSetToMax = new JButton(myModel.language.analysis.getString("sim.set_to_max")); //Set to max
			btnSetToMax.setEnabled(false);
			btnSetToMax.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int threads=Runtime.getRuntime().availableProcessors();
					textNumThreads.setText(threads+"");
				}
			});
			btnSetToMax.setBounds(142, 155, 144, 28);
			panelSimulation.add(btnSetToMax);
			
			chckbxMultithread = new JCheckBox(myModel.language.analysis.getString("sim.multi_thread")); //Multi-thread simulation
			chckbxMultithread.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxMultithread.isSelected()){
						textNumThreads.setEnabled(true);
						lblThreads.setEnabled(true);
						btnSetToMax.setEnabled(true);
					}
					else{
						textNumThreads.setEnabled(false);
						lblThreads.setEnabled(false);
						btnSetToMax.setEnabled(false);
					}
				}
			});
			chckbxMultithread.setBounds(15, 135, 281, 18);
			panelSimulation.add(chckbxMultithread);
			
			textNumThreads = new JTextField();
			textNumThreads.setEnabled(false);
			textNumThreads.setText("1");
			textNumThreads.setBounds(52, 155, 37, 28);
			panelSimulation.add(textNumThreads);
			textNumThreads.setColumns(10);
			
			
			
			//Markov #######################################################################
			
			JPanel panelMarkov = new JPanel();
			panelMarkov.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelMarkov.setBackground(SystemColor.window);
			tabbedPane.addTab(myModel.language.base.getString("markov.markov"), null, panelMarkov, null); //Markov
			tabbedPane.setEnabledAt(3, false);
			panelMarkov.setLayout(null);
			
			JLabel lblMaxCycles = new JLabel(myModel.language.analysis.getString("sim.max_cycles")+":"); //Max cycles
			lblMaxCycles.setHorizontalAlignment(SwingConstants.RIGHT);
			lblMaxCycles.setBounds(6, 23, 104, 16);
			panelMarkov.add(lblMaxCycles);
			
			textMarkovMaxCycles = new JTextField();
			textMarkovMaxCycles.setBounds(110, 17, 54, 28);
			panelMarkov.add(textMarkovMaxCycles);
			textMarkovMaxCycles.setColumns(10);
			
			chckbxHalfcycleCorrection = new JCheckBox(myModel.language.analysis.getString("gen.half_cycle_correction")); //Half-cycle correction
			chckbxHalfcycleCorrection.setBounds(6, 57, 195, 18);
			panelMarkov.add(chckbxHalfcycleCorrection);
			
			lblDiscountStartCycle = new JLabel(myModel.language.analysis.getString("gen.discount_start_cycle")+":"); //Discount start cycle
			lblDiscountStartCycle.setHorizontalAlignment(SwingConstants.RIGHT);
			lblDiscountStartCycle.setEnabled(false);
			lblDiscountStartCycle.setBounds(235, 121, 168, 16);
			panelMarkov.add(lblDiscountStartCycle);
			
			textDiscountStartCycle = new JTextField();
			textDiscountStartCycle.setEnabled(false);
			textDiscountStartCycle.setBounds(404, 115, 38, 28);
			panelMarkov.add(textDiscountStartCycle);
			textDiscountStartCycle.setColumns(10);
			
			chckbxDiscount = new JCheckBox(myModel.language.analysis.getString("gen.discount_rewards")); //Discount Rewards
			chckbxDiscount.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxDiscount.isSelected()){
						lblDiscountStartCycle.setEnabled(true);
						textDiscountStartCycle.setEnabled(true);
						lblCyclesPerYear.setEnabled(true);
						textCyclesPerYear.setEnabled(true);
						tableDiscountRates.setEnabled(true);
					}
					else{
						lblDiscountStartCycle.setEnabled(false);
						textDiscountStartCycle.setEnabled(false);
						lblCyclesPerYear.setEnabled(false);
						textCyclesPerYear.setEnabled(false);
						tableDiscountRates.setEnabled(false);
					}
				}
			});
			chckbxDiscount.setBounds(6, 85, 195, 18);
			panelMarkov.add(chckbxDiscount);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 115, 223, 83);
			panelMarkov.add(scrollPane_1);
			
			modelDiscountRates=new DefaultTableModel(
					new Object[][] {},
					new String[] {
							myModel.language.analysis.getString("gen.dimension"), //Dimension 
							myModel.language.analysis.getString("gen.discount_rate")} //Discount Rate (%)
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
			tableDiscountRates.putClientProperty("terminateEditOnFocusLost", true);
			scrollPane_1.setViewportView(tableDiscountRates);
			
			JLabel lblStatePrevalenceDecimals = new JLabel(myModel.language.analysis.getString("gen.state_prev_decimals")+":"); //State Prevalence Decimals
			lblStatePrevalenceDecimals.setHorizontalAlignment(SwingConstants.RIGHT);
			lblStatePrevalenceDecimals.setBounds(201, 70, 202, 16);
			panelMarkov.add(lblStatePrevalenceDecimals);
			
			textMarkovStateDecimals = new JTextField();
			textMarkovStateDecimals.setBounds(405, 64, 37, 28);
			panelMarkov.add(textMarkovStateDecimals);
			textMarkovStateDecimals.setColumns(10);
			
			chckbxShowMarkovTrace = new JCheckBox(myModel.language.analysis.getString("result.show_trace")); //Show trace
			chckbxShowMarkovTrace.setBounds(274, 11, 168, 18);
			panelMarkov.add(chckbxShowMarkovTrace);
			
			chckbxCompileTraces = new JCheckBox(myModel.language.analysis.getString("result.compile_traces")); //Compile traces
			chckbxCompileTraces.setBounds(274, 38, 168, 18);
			panelMarkov.add(chckbxCompileTraces);
			
			lblCyclesPerYear = new JLabel(myModel.language.analysis.getString("sim.cycles_per_year")+":"); //Cycles per year
			lblCyclesPerYear.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCyclesPerYear.setEnabled(false);
			lblCyclesPerYear.setBounds(235, 148, 168, 16);
			panelMarkov.add(lblCyclesPerYear);
			
			textCyclesPerYear = new JTextField();
			textCyclesPerYear.setEnabled(false);
			textCyclesPerYear.setColumns(10);
			textCyclesPerYear.setBounds(404, 142, 38, 28);
			panelMarkov.add(textCyclesPerYear);
			
			
			//Subgroups ###########################################################################
			
			JPanel panelSubgroups = new JPanel();
			panelSubgroups.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panelSubgroups.setBackground(SystemColor.window);
			tabbedPane.addTab(myModel.language.analysis.getString("result.subgroups"), null, panelSubgroups, null); //Subgroups
			tabbedPane.setEnabledAt(4, false);
			panelSubgroups.setLayout(null);
			
			chckbxSubgroups = new JCheckBox(myModel.language.analysis.getString("result.report_subgroup_outcomes")); //Report subgroup-specific outcomes
			chckbxSubgroups.setBounds(6, 11, 326, 18);
			panelSubgroups.add(chckbxSubgroups);
			
			JScrollPane scrollPaneSubgroups = new JScrollPane();
			scrollPaneSubgroups.setBounds(6, 36, 436, 149);
			panelSubgroups.add(scrollPaneSubgroups);
			
			modelSubgroups=new DefaultTableModel(
					new Object[][] {},
					new String[] {
							myModel.language.analysis.getString("result.subgroup"), //Subgroup
							myModel.language.base.getString("title.definition")} //Definition
					) {
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};

			tableSubgroups = new JTable();
			tableSubgroups.setRowSelectionAllowed(false);
			tableSubgroups.setShowVerticalLines(true);
			tableSubgroups.getTableHeader().setReorderingAllowed(false);
			tableSubgroups.setModel(modelSubgroups);
			scrollPaneSubgroups.setViewportView(tableSubgroups);
			
			tableSubgroups.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
						int selected=tableSubgroups.getSelectedRow();
						if(selected!=-1){
							defineSubgroup(selected);
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
						int selected=tableSubgroups.getSelectedRow();
						if(selected!=-1){
							e.consume();
							removeSubgroup(selected);
						}
					}
				}
			});
			tableSubgroups.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2){
						int selected=tableSubgroups.getSelectedRow();
						if(selected!=-1){
							defineSubgroup(selected);
						}
					}
				}
			});
			
			JButton btnAddSubgroup = new JButton("");
			btnAddSubgroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					defineSubgroup(-1);
				}
			});
			btnAddSubgroup.setIcon(new ScaledIcon("/images/add",16,16,16,true));
			btnAddSubgroup.setToolTipText(myModel.language.base.getString("button.add_subgroup")); //Add Subgroup
			btnAddSubgroup.setBounds(335, 6, 35, 28);
			panelSubgroups.add(btnAddSubgroup);
			
			btnRemoveSubgroup = new JButton("");
			btnRemoveSubgroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=tableSubgroups.getSelectedRow();
					if(selected!=-1){
						removeSubgroup(selected);
					}
				}
			});
			btnRemoveSubgroup.setIcon(new ScaledIcon("/images/remove",16,16,16,true));
			btnRemoveSubgroup.setDisabledIcon(new ScaledIcon("/images/remove",16,16,16,false));
			btnRemoveSubgroup.setToolTipText(myModel.language.base.getString("button.remove_subgroup")); //Remove Subgroup
			btnRemoveSubgroup.setBounds(407, 6, 35, 28);
			panelSubgroups.add(btnRemoveSubgroup);
			
			JButton btnEditSubgroup = new JButton("");
			btnEditSubgroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=tableSubgroups.getSelectedRow();
					if(selected!=-1){ //selected
						defineSubgroup(selected);
					}
				}
			});
			btnEditSubgroup.setIcon(new ScaledIcon("/images/edit",16,16,16,true));
			btnEditSubgroup.setToolTipText(myModel.language.base.getString("button.edit_subgroup")); //Edit Subgroup
			btnEditSubgroup.setBounds(371, 6, 35, 28);
			panelSubgroups.add(btnEditSubgroup);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void refreshDisplay(){
		displayMetadata(myModel.meta);
		displayAnalysisSettings();
		displaySimSettings();
		if(myModel.type==1){displayMarkovSettings();}
		displaySubgroupSettings();
	}
	
	private void displayMetadata(Metadata meta){
		String modelTypes[]={myModel.language.base.getString("menu.decision_tree"),
				myModel.language.base.getString("menu.markov_model")}; //Decision Tree, Markov Model
		String icons[]={"/images/modelTree","/images/markovChain"};
		lblName.setText(myModel.name);
		lblModel.setText(modelTypes[myModel.type]);
		lblIcon.setIcon(new ScaledIcon(icons[myModel.type],16,16,16,true));
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
	}
	
	private void displaySimSettings(){
		comboSimType.setSelectedIndex(myModel.simType);
		textCohortSize.setText(myModel.cohortSize+"");
		chckbxCRN.setSelected(myModel.CRN);
		if(myModel.CRN){
			textCRNSeed.setEnabled(true);
			textCRNSeed.setText(myModel.crnSeed+"");
		}
		chckbxDisplayIndResults.setSelected(myModel.displayIndResults);
		if(myModel.numThreads>1){
			chckbxMultithread.setSelected(true);
			textNumThreads.setText(myModel.numThreads+"");
			textNumThreads.setEnabled(true);
			lblThreads.setEnabled(true);
			btnSetToMax.setEnabled(true);
		}
	}
	
	private void displayMarkovSettings(){
		tabbedPane.setEnabledAt(3, true);
		textMarkovMaxCycles.setText(myModel.markov.maxCycles+"");
		chckbxShowMarkovTrace.setSelected(myModel.markov.showTrace);
		chckbxCompileTraces.setSelected(myModel.markov.compileTraces);
		textMarkovStateDecimals.setText(myModel.markov.stateDecimals+"");
		chckbxHalfcycleCorrection.setSelected(myModel.markov.halfCycleCorrection);
		chckbxDiscount.setSelected(myModel.markov.discountRewards);
		if(myModel.markov.discountRewards==true){
			lblDiscountStartCycle.setEnabled(true);
			textDiscountStartCycle.setEnabled(true);
			lblCyclesPerYear.setEnabled(true);
			textCyclesPerYear.setEnabled(true);
			tableDiscountRates.setEnabled(true);
		}
		textDiscountStartCycle.setText(myModel.markov.discountStartCycle+"");
		textCyclesPerYear.setText(myModel.markov.cyclesPerYear+"");
		modelDiscountRates.setRowCount(0);
		for(int i=0; i<tempDimInfo.dimNames.length; i++){
			modelDiscountRates.addRow(new Object[]{null});
			modelDiscountRates.setValueAt(tempDimInfo.dimNames[i],i,0);
			if(myModel.markov.discountRates!=null && i<myModel.markov.discountRates.length){
				modelDiscountRates.setValueAt(myModel.markov.discountRates[i]+"", i, 1);
			}
		}
	}
	
	private void displaySubgroupSettings(){
		if(myModel.simType==1){tabbedPane.setEnabledAt(4, true);}
		subgroupNames=new ArrayList<String>();
		subgroupDefs=new ArrayList<String>();
		chckbxSubgroups.setSelected(myModel.reportSubgroups);
		int numSubgroups=myModel.subgroupNames.size();
		for(int i=0; i<numSubgroups; i++){
			subgroupNames.add(myModel.subgroupNames.get(i));
			subgroupDefs.add(myModel.subgroupDefinitions.get(i));
		}
		refreshSubgroupTable();
	}
	
	private void defineSubgroup(int selected){
		frmDefineSubgroup window=new frmDefineSubgroup(myModel, selected, form);
		window.frmDefineSubgroup.setVisible(true);
	}
	
	private void removeSubgroup(int selected){
		modelSubgroups.removeRow(selected);
		subgroupNames.remove(selected);
		subgroupDefs.remove(selected);
		if(modelSubgroups.getRowCount()>0){btnRemoveSubgroup.setEnabled(true);}
		else{btnRemoveSubgroup.setEnabled(false);}
	}
	
	public void refreshSubgroupTable(){
		modelSubgroups.setRowCount(0);
		int numSubgroups=subgroupNames.size();
		for(int i=0; i<numSubgroups; i++){
			modelSubgroups.addRow(new Object[]{null});
			modelSubgroups.setValueAt(subgroupNames.get(i), i, 0);
			modelSubgroups.setValueAt(subgroupDefs.get(i), i, 1);
		}
		if(numSubgroups>0){btnRemoveSubgroup.setEnabled(true);}
		else{btnRemoveSubgroup.setEnabled(false);}
	}
	
	private boolean validateDimensions(){
		boolean valid=true;
		//Validate dimensions
		//Ensure unqiue
		numDimensions=tableDimensions.getRowCount();
		dimNames=new ArrayList<String>();
		dimSymbols=new ArrayList<String>();
		decimals=new ArrayList<Integer>();
		//dimDiscount=new ArrayList<Double>();
		for(int i=0; i<numDimensions; i++){
			String curName=(String) tableDimensions.getValueAt(i,0);
			String curSymbol=(String) tableDimensions.getValueAt(i,1);
			String curDecimal=(String) tableDimensions.getValueAt(i, 2);
			if(curName==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("info.invalid_dim_name")); //Please enter a valid dimension name
			}
			else if(dimNames.contains(curName)){
				valid=false;
				//[name] is already defined!
				String msg = MessageFormat.format(myModel.language.message.getString("err.already_defined"), curName);
				JOptionPane.showMessageDialog(frmProperties, msg);
			}
			else{
				dimNames.add(curName);
			}

			if(curSymbol==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+": "+myModel.language.message.getString("info.invalid_dim_symbol")); //Please enter a valid dimension symbol!
			}
			else if(dimSymbols.contains(curSymbol)){
				valid=false;
				//[name] is already defined!
				String msg = MessageFormat.format(myModel.language.message.getString("err.already_defined"), curSymbol);
				JOptionPane.showMessageDialog(frmProperties, msg);
			}
			else{
				dimSymbols.add(curSymbol);
			}

			if(curDecimal==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, curName+": "+myModel.language.message.getString("err.enter_decimal_places")); //Please enter decimal places!
			}
			else{
				//Try to convert to integer
				int curDec=0;
				try{
					curDec=Integer.parseInt(curDecimal);
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.enter_decimal_places")); //Please enter a valid integer for decimal places!
				}
				if(valid==true){ //Integer entered
					if(curDec<0){
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.enter_decimal_places")); //Please enter a valid integer for decimal places!
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
		int analysisType=comboAnalysis.getSelectedIndex();
		int objective=0, objectiveDim=0;
		int costDim=-1, effectDim=-1, extendedDim=-1;
		String baseStrategy = null;
		double WTP=0;
		if(analysisType==0){ //EV
			String strObj=(String) tableAnalysis.getValueAt(0, 1);
			if(strObj==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_objective")); //Please select an objective!
			}
			else{
				if(strObj.matches(myModel.language.analysis.getString("gen.maximize"))){objective=0;} //Maximize
				else{objective=1;}
			}
			String strDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_outcome")); //Please select an outcome!
			}
			else{
				objectiveDim=getDimIndex(strDim);
			}
		}
		else{ //CEA, BCA, or ECEA
			if(numDimensions==1) {
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.only_one_outcome")); //Only 1 outcome - please change analysis to Expected Value!
			}
			
			
			String strCostDim=(String) tableAnalysis.getValueAt(0, 1);
			if(strCostDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_cost")); //Please select a Cost!
			}
			else{costDim=getDimIndex(strCostDim);}
			String strEffectDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strEffectDim==null){
				valid=false;
				if(analysisType==1 || analysisType==3){JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_effect"));} //CEA or ECEA: Please select an Effect!
				else if(analysisType==2){JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_benefit"));} //BCA: Please select a Benefit!
			}
			else{
				effectDim=getDimIndex(strEffectDim);
			}
			if(costDim==effectDim){
				valid=false;
				if(analysisType==1 || analysisType==3){ //CEA or ECEA
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.same_cost_effect")); //Cost and Effect must be different!
				}
				else if(analysisType==2){ //BCA
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.same_cost_benefit")); //Cost and Benefit must be different!
				}
			}
			//check effect objective
			String strEffectObj=(String) tableAnalysis.getValueAt(2,1);
			if(strEffectObj==null) {
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_effect_objective")); //Please select an Effect Objective!
			}
			else{
				if(strEffectObj.matches(myModel.language.analysis.getString("gen.maximize"))){objective=0;} //Maximize
				else{objective=1;}
			}
			
			if(analysisType==1){ //CEA
				baseStrategy=(String)tableAnalysis.getValueAt(3, 1);
				if(baseStrategy==null || baseStrategy.isEmpty()){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.choose_baseline")); //Please choose a baseline strategy!
				}
				else{
					int baseIndex=myModel.getStrategyIndex(baseStrategy);
					if(baseIndex==-1){
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.baseline_not_recognized")); //Baseline strategy not recognized!
					}
				}
			}
			try{
				String strWTP=(String) tableAnalysis.getValueAt(4, 1);
				WTP=Double.parseDouble(strWTP.replaceAll(",",""));
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_wtp")); //Please enter a valid willingness-to-pay!
			}
			if(analysisType==3){ //ECEA
				String strExtendedDim=(String) tableAnalysis.getValueAt(5, 1);
				if(strExtendedDim==null){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.select_additional_dimension")); //Please select an Additional Dimension!
				}
				else{
					extendedDim=getDimIndex(strExtendedDim);
				}
				if(extendedDim==costDim || extendedDim==effectDim){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.same_add_dim")); //Additional Dimension must be different from Cost and Effect!
				}
			}
		}
		
		//Check simulation settings
		int simType=comboSimType.getSelectedIndex();
		int cohortSize=-1;
		boolean CRN=false;
		int crnSeed=-1;
		boolean displayIndResults=false;
		int numThreads=1;
		if(simType==0){ //Cohort
			try{
				String text=textCohortSize.getText().replaceAll(",",""); //remove commas
				cohortSize=Integer.parseInt(text);
				if(cohortSize<=0){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_cohort_size")); //Please enter a valid Cohort Size!
				}
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_cohort_size")); //Please enter a valid Cohort Size!
			}
		}
		else if(simType==1){ //Monte Carlo
			try{
				String text=textCohortSize.getText().replaceAll(",",""); //remove commas
				cohortSize=Integer.parseInt(text);
				if(cohortSize<=0){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_monte_carlo")); //Please enter a valid number of Monte Carlo simulations!
				}
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_monte_carlo")); //Please enter a valid number of Monte Carlo simulations!
			}
			
			CRN=chckbxCRN.isSelected(); //CRN
			displayIndResults=chckbxDisplayIndResults.isSelected();
			if(CRN){ //get seed
				try{
					String text=textCRNSeed.getText().replaceAll(",",""); //remove commas
					crnSeed=Integer.parseInt(text);
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_crn_seed")); //Please enter a valid CRN seed!
				}
			}
		}
		if(chckbxMultithread.isSelected()){
			try{
				numThreads=Integer.parseInt(textNumThreads.getText());
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_threads")); //Please enter a valid number of threads!
			}
			if(numThreads<1){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_threads")); //Please enter a valid number of threads!
			}
		}
		
		//Check Markov settings
		int maxCycles=10000, statePrevDecimals=4;
		boolean halfCycleCorrection=false;
		boolean showTrace=true;
		boolean compileTraces=false;
		if(myModel.type==1){
			//max cycles
			try{
				String text=textMarkovMaxCycles.getText().replaceAll(",",""); //remove commas
				maxCycles=Integer.parseInt(text);
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_max_cycles")); //Please enter a valid number of max cycles!
			}
			if(maxCycles<0){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_max_cycles")); //Please enter a valid number of max cycles!
			}
			
			showTrace=chckbxShowMarkovTrace.isSelected();
			compileTraces=chckbxCompileTraces.isSelected();
			
			//state prev decimals
			try{
				String text=textMarkovStateDecimals.getText().replaceAll(",",""); //remove commas
				statePrevDecimals=Integer.parseInt(text);
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_state_prev_decimals")); //Please enter a valid number of Markov State prevalence decimals!
			}
			if(statePrevDecimals<0){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_num_state_prev_decimals")); //Please enter a valid number of Markov State prevalence decimals!
			}
			
			halfCycleCorrection=chckbxHalfcycleCorrection.isSelected();
			
			//discount rates
			if(chckbxDiscount.isSelected()){
				try{
					int test=Integer.parseInt(textDiscountStartCycle.getText());
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_discount_start")); //Please enter a valid discount start cycle
				}
				
				try {
					double test=Double.parseDouble(textCyclesPerYear.getText());
					if(test<=0) {
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_cycles_year")); //Please enter a valid number of cycles per year!
					}
				} catch(Exception er) {
					valid=false;
					JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_cycles_year")); //Please enter a valid number of cycles per year!
				}
				
				for(int i=0; i<numDimensions; i++){
					try{
						String test=(String) tableDiscountRates.getValueAt(i, 1); 
						Numeric testVal=Interpreter.evaluate(test, myModel,false,myModel.language);
						if(!(testVal.isDouble() || testVal.isInteger()) ){ //not double or integer
							valid=false;
							JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_discount_rate")); //Please enter a valid discount rate 
						}
						//check range of discount value
						double curVal=testVal.getValue();
						if(curVal<0) {
							JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("warn.discount_rate_negative")); //Warning: Discount rate <0
						}
						else if(curVal>0 && curVal<0.5) {
							//Warning: Discount rates should be a percentage (%).  Current value is [val]
							String msg = MessageFormat.format(myModel.language.message.getString("warn.discount_rate_percent"), curVal);
							JOptionPane.showMessageDialog(frmProperties, msg);
						}
						
						tempDiscountRates[i]=test;
					} catch(Exception er){
						valid=false;
						JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.valid_discount_rate")); //Please enter a valid discount rate 
					}
				}
			}
		} //end Markov
		
		if(myModel.simType==1) {
			//subgroups
			if(chckbxSubgroups.isSelected() && subgroupNames.size()==0){
				valid=false;
				JOptionPane.showMessageDialog(frmProperties, myModel.language.message.getString("err.no_subgroups_defined")); //No subgroups are defined!
			}
		}
		
		if(valid==true){ //Apply changes
			frmProperties.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			//add to undo
			myModel.saveSnapshot(myModel.language.base.getString("title.change_properties")); //Change Properties
			
			//dimensions
			myModel.dimInfo.dimNames=new String[numDimensions]; myModel.dimInfo.dimSymbols=new String[numDimensions];
			myModel.dimInfo.decimals=new int[numDimensions];
			int dimIndices[]=new int[numDimensions]; //previous dimension indicies
			for(int i=0; i<numDimensions; i++){
				myModel.dimInfo.dimNames[i]=dimNames.get(i);
				myModel.dimInfo.dimSymbols[i]=dimSymbols.get(i);
				myModel.dimInfo.decimals[i]=decimals.get(i);
				dimIndices[i]=prevDimNames.indexOf(dimNames.get(i));
			}
			myModel.updateDimensions(dimIndices);
			
			//analysis type
			myModel.dimInfo.analysisType=comboAnalysis.getSelectedIndex();
			if(myModel.dimInfo.analysisType==0) { //EV
				myModel.dimInfo.objective=objective; myModel.dimInfo.objectiveDim=objectiveDim;
			}
			else { //CEA, BCA, ECEA
				myModel.dimInfo.costDim=costDim; myModel.dimInfo.effectDim=effectDim;
				myModel.dimInfo.objective=objective; //effect objective
				myModel.dimInfo.WTP=WTP;
				if(myModel.dimInfo.analysisType==1 || myModel.dimInfo.analysisType==3) {
					myModel.dimInfo.baseScenario=baseStrategy;
				}
			}
			
			tempDimInfo=myModel.dimInfo.copy(); //get new copy
			tableAnalysis.tempDimInfo=tempDimInfo;
			
			//simulation
			myModel.simType=simType;
			myModel.cohortSize=cohortSize;
			myModel.CRN=CRN;
			myModel.crnSeed=crnSeed;
			myModel.displayIndResults=displayIndResults;
			myModel.numThreads=numThreads;
			
			//markov settings
			if(myModel.type==1){
				myModel.markov.maxCycles=maxCycles;
				myModel.markov.showTrace=showTrace;
				myModel.markov.compileTraces=compileTraces;
				myModel.markov.stateDecimals=statePrevDecimals;
				myModel.markov.halfCycleCorrection=halfCycleCorrection;
				
				myModel.markov.discountRewards=chckbxDiscount.isSelected();
				if(myModel.markov.discountRewards==true){
					myModel.markov.discountStartCycle=Integer.parseInt(textDiscountStartCycle.getText());
					myModel.markov.cyclesPerYear=Double.parseDouble(textCyclesPerYear.getText());
					myModel.markov.discountRates=new String[numDimensions];
					for(int i=0; i<numDimensions; i++){
						myModel.markov.discountRates[i]=tempDiscountRates[i];
					}
				}
			}
			
			//subgroup settings
			myModel.reportSubgroups=chckbxSubgroups.isSelected();
			int numSubgroups=modelSubgroups.getRowCount();
			myModel.subgroupNames=new ArrayList<String>(); myModel.subgroupDefinitions=new ArrayList<String>();
			for(int i=0; i<numSubgroups; i++){
				myModel.subgroupNames.add((String) modelSubgroups.getValueAt(i, 0));
				myModel.subgroupDefinitions.add((String) modelSubgroups.getValueAt(i, 1));
			}
						
			frmProperties.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
			if(tempDimInfo.objective==0) {tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.maximize"), 0, 1);} //Maximize
			else {tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.minimize"), 0, 1);} //Minimize
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("result.outcome"),null}); tableAnalysis.enabled[1]=true; //Outcome
			int dim=tempDimInfo.objectiveDim;
			if(dim>=0 && dim<numDimensions) { //within bounds
				tableAnalysis.setValueAt(tempDimInfo.dimNames[dim], 1, 1);
			}
		}
		else{ //CEA, BCA, or ECEA
			modelAnalysis.setRowCount(0);
			//row 0
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.cost"),null}); tableAnalysis.enabled[0]=true; //Cost
			if(tempDimInfo.costDim!=-1) {
				int dim=tempDimInfo.costDim;
				if(dim>=0 && dim<numDimensions) { //within bounds
					tableAnalysis.setValueAt(tempDimInfo.dimNames[dim], 0, 1);
				}
			}
			//row 1
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.effect"),null}); tableAnalysis.enabled[1]=true; //Effect
			if(tempDimInfo.effectDim!=-1) {
				int dim=tempDimInfo.effectDim;
				if(dim>=0 && dim<numDimensions) { //within bounds
					tableAnalysis.setValueAt(tempDimInfo.dimNames[dim], 1, 1);
				}
			}
			//row 2
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.effect_objective"),null}); tableAnalysis.enabled[2]=true; //Effect Objective
			if(tempDimInfo.objective==0) {tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.maximize"), 2, 1);} //Maximize
			else {tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.minimize"), 2, 1);} //Minimize
			//row 3
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.baseline_strategy"),null}); tableAnalysis.enabled[3]=true; //Baseline Strategy
			//row 4
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.wtp"),null}); tableAnalysis.enabled[4]=true; //Willingness-to-pay (WTP)
			tableAnalysis.setValueAt(tempDimInfo.WTP+"", 4, 1);
			
			if(analysisType==1){ //CEA
				tableAnalysis.setValueAt(myModel.language.analysis.getString("cea.effect"), 1, 0); //Effect
				tableAnalysis.setValueAt(tempDimInfo.baseScenario,3,1);
			}
			else if(analysisType==2){ //BCA
				tableAnalysis.setValueAt(myModel.language.analysis.getString("bca.benefit"), 1, 0); //Benefit
				tableAnalysis.enabled[3]=false; //baseline strategy
			}
			else if(analysisType==3){ //ECEA
				tableAnalysis.setValueAt(myModel.language.analysis.getString("cea.effect"), 1, 0); //Effect
				tableAnalysis.setValueAt(tempDimInfo.baseScenario,3,1);
				//row 5
				modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.additional_dimension"),null}); tableAnalysis.enabled[5]=true; //Additional Dimension
			}
		}
		
		tableAnalysis.repaint();
	}
	
	private int getDimIndex(String dimName){
		int index=-1;
		int i=0;
		boolean found=false;
		while(found==false && i<tempDimInfo.dimNames.length){
			if(tempDimInfo.dimNames[i].equals(dimName)){
				found=true;
				index=i;
			}
			i++;
		}
		return(index);
	}
}


class analysisTable extends JTable{
	public int analysisType=0;
	public boolean enabled[]=new boolean[]{false,false,false,false,false,false};
	analysisTable thisTable=this;
	public AmuaModel myModel;
	public DimInfo tempDimInfo;;
	
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(int i=0; i<6; i++){this.enabled[i]=enabled;}
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column){
		if(analysisType==0){ //EV
			if(column==1){
				if(row==0){ //not editable
					JComboBox<String> comboRule = new JComboBox<String>(new DefaultComboBoxModel(new String[]{
							myModel.language.analysis.getString("gen.maximize"), //Maximize
							myModel.language.analysis.getString("gen.minimize")})); //Minimize
					return(new DefaultCellEditor(comboRule));
				}
				else if(column==1){ //dimension
					JComboBox<String> comboDim = new JComboBox<String>(new DefaultComboBoxModel(tempDimInfo.dimNames));
					return(new DefaultCellEditor(comboDim));
				}
			}
		}
		else{ //CEA, BCA, or ECEA
			if(column==1){
				if(row==0 || row==1){ //Cost/Effect
					JComboBox<String> comboDim = new JComboBox<String>(new DefaultComboBoxModel(tempDimInfo.dimNames));
					return(new DefaultCellEditor(comboDim));
				}
				else if(row==2) { //Effect Objective
					JComboBox<String> comboRule = new JComboBox<String>(new DefaultComboBoxModel(new String[]{
							myModel.language.analysis.getString("gen.maximize"), //Maximize
							myModel.language.analysis.getString("gen.minimize")})); //Minimize
					return(new DefaultCellEditor(comboRule));
				}
				else if(row==3){ //Baseline scenario
					myModel.getStrategies();
					JComboBox<String> comboScen = new JComboBox<String>(new DefaultComboBoxModel(myModel.strategyNames));
					comboScen.setEnabled(enabled[3]);
					return(new DefaultCellEditor(comboScen));
				}
				else if(row==4){ //Willingness to pay
					return(thisTable.getDefaultEditor(Object.class));
				}
				else if(row==5){ //Additional Dimension
					JComboBox<String> comboDim = new JComboBox<String>(new DefaultComboBoxModel(tempDimInfo.dimNames));
					return(new DefaultCellEditor(comboDim));
				}
			}
		}
		return(null);
	}
}