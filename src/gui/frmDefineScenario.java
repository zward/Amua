/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import base.AmuaModel;
import main.ScaledIcon;
import main.Scenario;
import main.StyledTextPane;
import math.Interpreter;
import math.Numeric;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import java.awt.Toolkit;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 *
 */
public class frmDefineScenario {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineScenario;
	AmuaModel myModel;
	private JTextField textName;
	private JTextPane paneValue;
	JTextArea textNotes;
	Scenario scenario;
	int scenarioNum;
	StyledTextPane paneExpression;
	frmScenarios schedule;
	//uncertainty
	private JTextField textIterations;
	JCheckBox chckbxSeed1, chckbxSeedParams;
	JCheckBox chckbxSampleParameters;
	private JTextField textSeed1,textSeedParams;
	private JTextField textCohortSize;
	JCheckBox chckbxUseParameterSets;
	//analysis
	JLabel lblAnalysisType;
	JComboBox comboAnalysis;
	private DefaultTableModel modelAnalysis;
	private analysisTable tableAnalysis;
	int testAnalysisType=-1, testObjective=-1, testObjectiveDim=-1, testCostDim=-1, testEffectDim=-1;
	double testWTP=-1;
	String testBaseStrategy;
	int testExtendedDim=-1;
	//markov
	JLabel lblMarkov;
	JCheckBox chckbxHalfcycleCorrection;
	JCheckBox chckbxDiscountRewards;
	JLabel lblDiscountStartCycle;
	private JTextField textDiscountStartCycle;
	DefaultTableModel modelDiscount;
	private JTable tableDiscount;
	boolean testHalfCycleCorrection, testDiscountRewards;
	String testDiscountRates[];
	int testDiscountStartCycle=0;


	public frmDefineScenario(AmuaModel myModel, int runNum, frmScenarios schedule){
		this.myModel=myModel;
		myModel.getStrategies();
		this.scenarioNum=runNum;
		this.schedule=schedule;
		initialize();
		if(runNum==-1){scenario=new Scenario(myModel);}
		else{scenario=schedule.scenarios.get(scenarioNum);}
		getScenario();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineScenario = new JDialog();
			frmDefineScenario.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineScenario.class.getResource("/images/scenario_128.png")));
			frmDefineScenario.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineScenario.setTitle("Amua - "+myModel.language.base.getString("title.define_scenario")); //Define Scenario
			frmDefineScenario.setResizable(false);
			frmDefineScenario.setBounds(100, 100, 850, 499);
			frmDefineScenario.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{83, 87, 89, 104, 75, 287, 0};
			gridBagLayout.rowHeights = new int[]{28, 109, 182, 74, 28, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			frmDefineScenario.getContentPane().setLayout(gridBagLayout);

			JLabel lblName = new JLabel(myModel.language.base.getString("object.scenario_name")+":");
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.gridwidth = 2;
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.insets = new Insets(5, 5, 5, 5);
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			frmDefineScenario.getContentPane().add(lblName, gbc_lblName);

			textName = new JTextField();
			GridBagConstraints gbc_textName = new GridBagConstraints();
			gbc_textName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textName.insets = new Insets(5, 5, 5, 5);
			gbc_textName.gridwidth = 3;
			gbc_textName.gridx = 2;
			gbc_textName.gridy = 0;
			frmDefineScenario.getContentPane().add(textName, gbc_textName);
			textName.setColumns(10);

			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.insets = new Insets(5, 5, 5, 0);
			gbc_panel_2.gridheight = 2;
			gbc_panel_2.gridx = 5;
			gbc_panel_2.gridy = 0;
			frmDefineScenario.getContentPane().add(panel_2, gbc_panel_2);
			GridBagLayout gbl_panel_2 = new GridBagLayout();
			gbl_panel_2.columnWidths = new int[]{50, 0, 0};
			gbl_panel_2.rowHeights = new int[]{16, 26, 80, 0};
			gbl_panel_2.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			panel_2.setLayout(gbl_panel_2);

			JLabel lblAnalysis = new JLabel(myModel.language.analysis.getString("gen.analysis")); //Analysis
			lblAnalysis.setFont(new Font("SansSerif", Font.BOLD, 12));
			GridBagConstraints gbc_lblAnalysis = new GridBagConstraints();
			gbc_lblAnalysis.gridwidth = 2;
			gbc_lblAnalysis.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblAnalysis.insets = new Insets(5, 5, 0, 5);
			gbc_lblAnalysis.gridx = 0;
			gbc_lblAnalysis.gridy = 0;
			panel_2.add(lblAnalysis, gbc_lblAnalysis);

			lblAnalysisType = new JLabel(myModel.language.base.getString("object.type")+":");
			GridBagConstraints gbc_lblAnalysisType = new GridBagConstraints();
			gbc_lblAnalysisType.anchor = GridBagConstraints.WEST;
			gbc_lblAnalysisType.insets = new Insets(0, 5, 5, 5);
			gbc_lblAnalysisType.gridx = 0;
			gbc_lblAnalysisType.gridy = 1;
			panel_2.add(lblAnalysisType, gbc_lblAnalysisType);

			String types[]=new String[4];
			types[0]=myModel.language.analysis.getString("gen.expected_value"); //Expected Value (EV)
			types[1]=myModel.language.analysis.getString("cea.cost_effectiveness_analysis"); //Cost-Effectiveness Analysis (CEA)
			types[2]=myModel.language.analysis.getString("bca.benefit_cost_analysis"); //Benefit-Cost Analysis (BCA)
			types[3]=myModel.language.analysis.getString("cea.extended_cea"); //Extended Cost-Effectiveness Analysis (ECEA)
			
			comboAnalysis = new JComboBox<String>();
			comboAnalysis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAnalysisType(comboAnalysis.getSelectedIndex());
				}
			});
			comboAnalysis.setModel(new DefaultComboBoxModel(types));
			GridBagConstraints gbc_comboAnalysis = new GridBagConstraints();
			gbc_comboAnalysis.anchor = GridBagConstraints.NORTH;
			gbc_comboAnalysis.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboAnalysis.insets = new Insets(0, 0, 5, 5);
			gbc_comboAnalysis.gridx = 1;
			gbc_comboAnalysis.gridy = 1;
			panel_2.add(comboAnalysis, gbc_comboAnalysis);

			JScrollPane scrollPane_Analysis = new JScrollPane();
			GridBagConstraints gbc_scrollPane_Analysis = new GridBagConstraints();
			gbc_scrollPane_Analysis.gridwidth = 2;
			gbc_scrollPane_Analysis.insets = new Insets(0, 5, 5, 5);
			gbc_scrollPane_Analysis.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_Analysis.gridx = 0;
			gbc_scrollPane_Analysis.gridy = 2;
			panel_2.add(scrollPane_Analysis, gbc_scrollPane_Analysis);

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
			tableAnalysis.tempDimInfo=myModel.dimInfo; //can't modify dimensions in scenarios
			tableAnalysis.getTableHeader().setReorderingAllowed(false);
			tableAnalysis.setModel(modelAnalysis);
			tableAnalysis.getColumnModel().getColumn(0).setPreferredWidth(170);
			tableAnalysis.getColumnModel().getColumn(1).setPreferredWidth(170);
			tableAnalysis.setShowVerticalLines(true);
			tableAnalysis.setRowSelectionAllowed(false);
			tableAnalysis.putClientProperty("terminateEditOnFocusLost", true);
			scrollPane_Analysis.setViewportView(tableAnalysis);

			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 5, 5, 5);
			gbc_panel.gridwidth = 5;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;
			frmDefineScenario.getContentPane().add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{80, 70, 145, 55, 0};
			gbl_panel.rowHeights = new int[]{23, 4, 23, 23, 23, 0};
			gbl_panel.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);

			JLabel lblModelUncertainty = new JLabel(myModel.language.analysis.getString("sim.model_uncertainty")); //Model Uncertainty
			lblModelUncertainty.setFont(new Font("SansSerif", Font.BOLD, 12));
			GridBagConstraints gbc_lblModelUncertainty = new GridBagConstraints();
			gbc_lblModelUncertainty.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblModelUncertainty.insets = new Insets(5, 5, 0, 5);
			gbc_lblModelUncertainty.gridwidth = 2;
			gbc_lblModelUncertainty.gridx = 0;
			gbc_lblModelUncertainty.gridy = 0;
			panel.add(lblModelUncertainty, gbc_lblModelUncertainty);

			JLabel lblIterations = new JLabel(myModel.language.analysis.getString("sim.num_iterations")+":");
			GridBagConstraints gbc_lblIterations = new GridBagConstraints();
			gbc_lblIterations.anchor = GridBagConstraints.EAST;
			gbc_lblIterations.insets = new Insets(5, 0, 0, 5);
			gbc_lblIterations.gridx = 2;
			gbc_lblIterations.gridy = 0;
			panel.add(lblIterations, gbc_lblIterations);

			textIterations = new JTextField();
			GridBagConstraints gbc_textIterations = new GridBagConstraints();
			gbc_textIterations.insets = new Insets(5, 0, 0, 5);
			gbc_textIterations.fill = GridBagConstraints.HORIZONTAL;
			gbc_textIterations.gridx = 3;
			gbc_textIterations.gridy = 0;
			panel.add(textIterations, gbc_textIterations);
			textIterations.setText("1");
			textIterations.setColumns(10);

			JLabel lblFirstorder = new JLabel(myModel.language.analysis.getString("sim.first_order")); //First-order
			lblFirstorder.setHorizontalAlignment(SwingConstants.CENTER);
			lblFirstorder.setFont(new Font("SansSerif", Font.ITALIC, 11));
			GridBagConstraints gbc_lblFirstorder = new GridBagConstraints();
			gbc_lblFirstorder.anchor = GridBagConstraints.NORTH;
			gbc_lblFirstorder.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblFirstorder.insets = new Insets(0, 5, 0, 5);
			gbc_lblFirstorder.gridwidth = 2;
			gbc_lblFirstorder.gridx = 0;
			gbc_lblFirstorder.gridy = 1;
			panel.add(lblFirstorder, gbc_lblFirstorder);

			chckbxSampleParameters = new JCheckBox(myModel.language.analysis.getString("sim.sample_parameters")); //Sample Parameters
			chckbxSampleParameters.setFont(new Font("SansSerif", Font.PLAIN, 11));
			chckbxSampleParameters.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(chckbxSampleParameters.isSelected()) {
						chckbxSeedParams.setEnabled(true);
						if(chckbxSeedParams.isSelected()){textSeedParams.setEnabled(true);}
						else{textSeedParams.setEnabled(false);}
						chckbxUseParameterSets.setEnabled(false);
					}
					else {
						chckbxSeedParams.setEnabled(false);
						textSeedParams.setEnabled(false);
						if(myModel.parameterSets!=null) {
							chckbxUseParameterSets.setEnabled(true);
						}
					}
				}
			});
			GridBagConstraints gbc_chckbxSampleParameters = new GridBagConstraints();
			gbc_chckbxSampleParameters.gridwidth = 2;
			gbc_chckbxSampleParameters.anchor = GridBagConstraints.WEST;
			gbc_chckbxSampleParameters.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxSampleParameters.gridx = 2;
			gbc_chckbxSampleParameters.gridy = 2;
			panel.add(chckbxSampleParameters, gbc_chckbxSampleParameters);

			JLabel lblSecondorder = new JLabel(myModel.language.analysis.getString("sim.second_order")); //Second-order
			lblSecondorder.setHorizontalAlignment(SwingConstants.CENTER);
			lblSecondorder.setFont(new Font("SansSerif", Font.ITALIC, 11));
			GridBagConstraints gbc_lblSecondorder = new GridBagConstraints();
			gbc_lblSecondorder.insets = new Insets(0, 0, 0, 0);
			gbc_lblSecondorder.anchor = GridBagConstraints.NORTH;
			gbc_lblSecondorder.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblSecondorder.gridwidth = 2;
			gbc_lblSecondorder.gridx = 2;
			gbc_lblSecondorder.gridy = 1;
			panel.add(lblSecondorder, gbc_lblSecondorder);

			JLabel lblCohortSize = new JLabel(myModel.language.analysis.getString("sim.cohort_size")+":"); //Cohort size
			lblCohortSize.setFont(new Font("SansSerif", Font.PLAIN, 11));
			GridBagConstraints gbc_lblCohortSize = new GridBagConstraints();
			gbc_lblCohortSize.anchor = GridBagConstraints.EAST;
			gbc_lblCohortSize.insets = new Insets(0, 5, 0, 5);
			gbc_lblCohortSize.gridx = 0;
			gbc_lblCohortSize.gridy = 2;
			panel.add(lblCohortSize, gbc_lblCohortSize);

			textCohortSize = new JTextField();
			GridBagConstraints gbc_textCohortSize = new GridBagConstraints();
			gbc_textCohortSize.fill = GridBagConstraints.HORIZONTAL;
			gbc_textCohortSize.insets = new Insets(0, 0, 0, 5);
			gbc_textCohortSize.gridx = 1;
			gbc_textCohortSize.gridy = 2;
			panel.add(textCohortSize, gbc_textCohortSize);
			textCohortSize.setColumns(10);

			chckbxSeedParams = new JCheckBox(myModel.language.analysis.getString("sim.seed_param_rng")); //Seed Parameter RNG
			chckbxSeedParams.setToolTipText(myModel.language.analysis.getString("sim.seed_param_rng"));
			chckbxSeedParams.setFont(new Font("SansSerif", Font.PLAIN, 11));
			chckbxSeedParams.setEnabled(false);
			chckbxSeedParams.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeedParams.isSelected()){textSeedParams.setEnabled(true);}
					else{textSeedParams.setEnabled(false);}
				}
			});

			chckbxSeed1 = new JCheckBox(myModel.language.analysis.getString("sim.seed_rng")); //Seed RNG
			chckbxSeed1.setToolTipText(myModel.language.analysis.getString("sim.seed_rng"));
			chckbxSeed1.setFont(new Font("SansSerif", Font.PLAIN, 11));
			chckbxSeed1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeed1.isSelected()){textSeed1.setEnabled(true);}
					else{textSeed1.setEnabled(false);}
				}
			});
			GridBagConstraints gbc_chckbxSeed1 = new GridBagConstraints();
			gbc_chckbxSeed1.gridheight = 2;
			gbc_chckbxSeed1.insets = new Insets(0, 5, 5, 5);
			gbc_chckbxSeed1.gridx = 0;
			gbc_chckbxSeed1.gridy = 3;
			panel.add(chckbxSeed1, gbc_chckbxSeed1);

			GridBagConstraints gbc_chckbxSeedParams = new GridBagConstraints();
			gbc_chckbxSeedParams.anchor = GridBagConstraints.WEST;
			gbc_chckbxSeedParams.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxSeedParams.gridx = 2;
			gbc_chckbxSeedParams.gridy = 3;
			panel.add(chckbxSeedParams, gbc_chckbxSeedParams);

			textSeedParams = new JTextField();
			textSeedParams.setEnabled(false);
			GridBagConstraints gbc_textSeedParams = new GridBagConstraints();
			gbc_textSeedParams.insets = new Insets(0, 0, 0, 5);
			gbc_textSeedParams.fill = GridBagConstraints.HORIZONTAL;
			gbc_textSeedParams.gridx = 3;
			gbc_textSeedParams.gridy = 3;
			panel.add(textSeedParams, gbc_textSeedParams);
			textSeedParams.setColumns(10);



			textSeed1 = new JTextField();
			textSeed1.setEnabled(false);
			textSeed1.setColumns(10);
			GridBagConstraints gbc_textSeed1 = new GridBagConstraints();
			gbc_textSeed1.gridheight = 2;
			gbc_textSeed1.fill = GridBagConstraints.HORIZONTAL;
			gbc_textSeed1.insets = new Insets(0, 0, 0, 5);
			gbc_textSeed1.gridx = 1;
			gbc_textSeed1.gridy = 3;
			panel.add(textSeed1, gbc_textSeed1);

			chckbxUseParameterSets = new JCheckBox(myModel.language.analysis.getString("sim.use_param_sets")); //Use Parameter Sets
			chckbxUseParameterSets.setFont(new Font("SansSerif", Font.PLAIN, 11));
			chckbxUseParameterSets.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(chckbxUseParameterSets.isSelected()) {
						chckbxSampleParameters.setEnabled(false);
						chckbxSeedParams.setEnabled(false);
						textSeedParams.setEnabled(false);
					}
					else {
						chckbxSampleParameters.setEnabled(true);
						chckbxSeedParams.setEnabled(true);
						if(chckbxSeedParams.isSelected()){textSeedParams.setEnabled(true);}
						else{textSeedParams.setEnabled(false);}
					}
				}
			});
			GridBagConstraints gbc_chckbxUseParameterSets = new GridBagConstraints();
			gbc_chckbxUseParameterSets.anchor = GridBagConstraints.WEST;
			gbc_chckbxUseParameterSets.insets = new Insets(0, 0, 5, 0);
			gbc_chckbxUseParameterSets.gridwidth = 2;
			gbc_chckbxUseParameterSets.gridx = 2;
			gbc_chckbxUseParameterSets.gridy = 4;
			panel.add(chckbxUseParameterSets, gbc_chckbxUseParameterSets);

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.insets = new Insets(0, 5, 5, 5);
			gbc_panel_1.gridwidth = 5;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 2;
			frmDefineScenario.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{23, 86, 184, 0};
			gbl_panel_1.rowHeights = new int[]{28, 74, 26, 23, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.SOUTHWEST;
			gbc_toolBar.insets = new Insets(0, 0, 0, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			panel_1.add(toolBar, gbc_toolBar);

			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,true);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText(myModel.language.base.getString("button.build_expression")); //Build Expression
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ScaledIcon("/images/formula",24,24,24,true));
			toolBar.add(btnFx);

			JLabel lblExpression = new JLabel(myModel.language.base.getString("object.object_updates")); //Object Updates
			lblExpression.setFont(new Font("SansSerif", Font.BOLD, 12));
			GridBagConstraints gbc_lblExpression = new GridBagConstraints();
			gbc_lblExpression.gridwidth = 2;
			gbc_lblExpression.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblExpression.insets = new Insets(0, 0, 5, 0);
			gbc_lblExpression.gridx = 1;
			gbc_lblExpression.gridy = 0;
			panel_1.add(lblExpression, gbc_lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
			gbc_scrollPane.gridwidth = 3;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			panel_1.add(scrollPane, gbc_scrollPane);

			paneExpression=new StyledTextPane(myModel, myModel.language);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);

			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluateUpdates();
					}
				}
			});

			JButton btnEvaluate = new JButton(myModel.language.base.getString("button.evaluate"));
			GridBagConstraints gbc_btnEvaluate = new GridBagConstraints();
			gbc_btnEvaluate.gridwidth = 2;
			gbc_btnEvaluate.anchor = GridBagConstraints.NORTH;
			gbc_btnEvaluate.insets = new Insets(0, 0, 0, 5);
			gbc_btnEvaluate.gridx = 0;
			gbc_btnEvaluate.gridy = 2;
			panel_1.add(btnEvaluate, gbc_btnEvaluate);
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluateUpdates();
				}
			});

			JScrollPane scrollPaneValue = new JScrollPane();
			GridBagConstraints gbc_scrollPaneValue = new GridBagConstraints();
			gbc_scrollPaneValue.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPaneValue.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneValue.gridheight = 2;
			gbc_scrollPaneValue.gridx = 2;
			gbc_scrollPaneValue.gridy = 2;
			panel_1.add(scrollPaneValue, gbc_scrollPaneValue);

			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);

			JLabel lblValue = new JLabel(myModel.language.math.getString("sum.expected_values")+":");
			GridBagConstraints gbc_lblValue = new GridBagConstraints();
			gbc_lblValue.gridwidth = 2;
			gbc_lblValue.anchor = GridBagConstraints.NORTHEAST;
			gbc_lblValue.insets = new Insets(0, 0, 5, 5);
			gbc_lblValue.gridx = 0;
			gbc_lblValue.gridy = 3;
			panel_1.add(lblValue, gbc_lblValue);

			JPanel panel_Markov = new JPanel();
			panel_Markov.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel_Markov = new GridBagConstraints();
			gbc_panel_Markov.fill = GridBagConstraints.BOTH;
			gbc_panel_Markov.insets = new Insets(0, 5, 5, 0);
			gbc_panel_Markov.gridx = 5;
			gbc_panel_Markov.gridy = 2;
			frmDefineScenario.getContentPane().add(panel_Markov, gbc_panel_Markov);
			GridBagLayout gbl_panel_Markov = new GridBagLayout();
			gbl_panel_Markov.columnWidths = new int[]{54, 58, 0};
			gbl_panel_Markov.rowHeights = new int[]{16, 18, 18, 28, 77, 0};
			gbl_panel_Markov.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			gbl_panel_Markov.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panel_Markov.setLayout(gbl_panel_Markov);

			chckbxDiscountRewards = new JCheckBox(myModel.language.analysis.getString("gen.discount_rewards")); //Discount Rewards
			chckbxDiscountRewards.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxDiscountRewards.isSelected()) {
						textDiscountStartCycle.setEnabled(true);
						tableDiscount.setEnabled(true);
					}
					else {
						textDiscountStartCycle.setEnabled(false);
						tableDiscount.setEnabled(false);
					}
				}
			});

			GridBagConstraints gbc_chckbxDiscountRewards = new GridBagConstraints();
			gbc_chckbxDiscountRewards.anchor = GridBagConstraints.NORTHWEST;
			gbc_chckbxDiscountRewards.insets = new Insets(0, 5, 0, 0);
			gbc_chckbxDiscountRewards.gridwidth = 2;
			gbc_chckbxDiscountRewards.gridx = 0;
			gbc_chckbxDiscountRewards.gridy = 2;
			panel_Markov.add(chckbxDiscountRewards, gbc_chckbxDiscountRewards);

			lblMarkov = new JLabel(myModel.language.base.getString("markov.markov")); //Markov
			lblMarkov.setFont(new Font("SansSerif", Font.BOLD, 12));
			GridBagConstraints gbc_lblMarkov = new GridBagConstraints();
			gbc_lblMarkov.gridwidth = 2;
			gbc_lblMarkov.anchor = GridBagConstraints.NORTH;
			gbc_lblMarkov.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblMarkov.insets = new Insets(5, 5, 5, 0);
			gbc_lblMarkov.gridx = 0;
			gbc_lblMarkov.gridy = 0;
			panel_Markov.add(lblMarkov, gbc_lblMarkov);

			chckbxHalfcycleCorrection = new JCheckBox(myModel.language.analysis.getString("gen.half_cycle_correction"));
			GridBagConstraints gbc_chckbxHalfcycleCorrection = new GridBagConstraints();
			gbc_chckbxHalfcycleCorrection.anchor = GridBagConstraints.NORTHWEST;
			gbc_chckbxHalfcycleCorrection.insets = new Insets(0, 5, 5, 0);
			gbc_chckbxHalfcycleCorrection.gridwidth = 2;
			gbc_chckbxHalfcycleCorrection.gridx = 0;
			gbc_chckbxHalfcycleCorrection.gridy = 1;
			panel_Markov.add(chckbxHalfcycleCorrection, gbc_chckbxHalfcycleCorrection);

			lblDiscountStartCycle = new JLabel(myModel.language.analysis.getString("gen.discount_start_cycle")+":");
			GridBagConstraints gbc_lblDiscountStartCycle = new GridBagConstraints();
			gbc_lblDiscountStartCycle.anchor = GridBagConstraints.EAST;
			gbc_lblDiscountStartCycle.insets = new Insets(0, 0, 5, 5);
			gbc_lblDiscountStartCycle.gridx = 0;
			gbc_lblDiscountStartCycle.gridy = 3;
			panel_Markov.add(lblDiscountStartCycle, gbc_lblDiscountStartCycle);

			textDiscountStartCycle = new JTextField();
			GridBagConstraints gbc_textDiscountStartCycle = new GridBagConstraints();
			gbc_textDiscountStartCycle.fill = GridBagConstraints.HORIZONTAL;
			gbc_textDiscountStartCycle.anchor = GridBagConstraints.NORTH;
			gbc_textDiscountStartCycle.insets = new Insets(0, 0, 5, 5);
			gbc_textDiscountStartCycle.gridx = 1;
			gbc_textDiscountStartCycle.gridy = 3;
			panel_Markov.add(textDiscountStartCycle, gbc_textDiscountStartCycle);
			textDiscountStartCycle.setColumns(10);

			JScrollPane scrollPane_Discount = new JScrollPane();
			GridBagConstraints gbc_scrollPane_Discount = new GridBagConstraints();
			gbc_scrollPane_Discount.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_Discount.insets = new Insets(0, 5, 5, 5);
			gbc_scrollPane_Discount.gridwidth = 2;
			gbc_scrollPane_Discount.gridx = 0;
			gbc_scrollPane_Discount.gridy = 4;
			panel_Markov.add(scrollPane_Discount, gbc_scrollPane_Discount);

			modelDiscount=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.analysis.getString("gen.dimension"), myModel.language.analysis.getString("gen.discount_rate")} //Dimension, Discount Rate (%)
					) {
				boolean[] columnEditables = new boolean[] {false, true};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
			
			
			tableDiscount = new JTable();
			tableDiscount.setModel(modelDiscount);
			tableDiscount.setRowSelectionAllowed(false);
			tableDiscount.getTableHeader().setReorderingAllowed(false);
			tableDiscount.putClientProperty("terminateEditOnFocusLost", true);
			scrollPane_Discount.setViewportView(tableDiscount);

			JScrollPane scrollPane_1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.gridwidth = 5;
			gbc_scrollPane_1.gridx = 1;
			gbc_scrollPane_1.gridy = 3;
			frmDefineScenario.getContentPane().add(scrollPane_1, gbc_scrollPane_1);

			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

			JLabel lblNotes = new JLabel(myModel.language.base.getString("menu.notes")+":");
			GridBagConstraints gbc_lblNotes = new GridBagConstraints();
			gbc_lblNotes.anchor = GridBagConstraints.EAST;
			gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
			gbc_lblNotes.gridx = 0;
			gbc_lblNotes.gridy = 3;
			frmDefineScenario.getContentPane().add(lblNotes, gbc_lblNotes);
						
									JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
									btnSave.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											boolean proceed=true;
											//Ensure name is valid and unique
											String testName=textName.getText();
											String testNotes=textNotes.getText();
											int testIterations=-1, testCohortSize=-1, testSeed1=-1, testSeed2=-1;
											if(testName.length()==0){
												JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.please_enter_name")); //Please enter a name!
												proceed=false;
											}
											else{
												//Ensure name is unique
												boolean unique=true;
												int i=0;
												int numScenarios=schedule.scenarios.size();
												while(unique==true && i<numScenarios){
													if(i!=scenarioNum && schedule.scenarios.get(i).name.equals(testName)){unique=false;}
													i++;
												}
												if(unique==false){
													//[name] is already defined!
													String msg=MessageFormat.format(myModel.language.message.getString("err.already_defined"), testName);
													JOptionPane.showMessageDialog(frmDefineScenario, msg); 
													proceed=false;
												}

												//Check iterations
												try{
													testIterations=Integer.parseInt(textIterations.getText());
												} catch(Exception e1){
													JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_num_iterations")); //Please enter a valid number of iterations!
													proceed=false;
												}
												if(proceed && testIterations<=0){
													JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_num_iterations")); //Please enter a valid number of iterations!
													proceed=false;
												}

												//Check cohort size
												try{
													testCohortSize=Integer.parseInt(textCohortSize.getText());
												}catch(Exception e1){
													if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_cohort_size"));} //Please enter a valid cohort size!
													else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_num_sim"));} //Please enter a valid # of simulations!
													proceed=false;
												}
												if(proceed && testCohortSize<=0){
													if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_cohort_size"));} //Please enter a valid cohort size!
													else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_num_sim"));} //Please enter a valid # of simulations!
													proceed=false;
												}


												//check seeds
												if(chckbxSeed1.isSelected()){
													try{
														testSeed1=Integer.parseInt(textSeed1.getText());
													} catch(Exception e1){
														JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_seed")); //Please enter a valid seed!
														proceed=false;
													}
												}
												if(chckbxSeedParams.isSelected()){
													try{
														testSeed2=Integer.parseInt(textSeedParams.getText());
													} catch(Exception e1){
														JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_param_seed")); //Please enter a valid parameter seed!
														proceed=false;
													}
												}

											}

											//analysis
											if(proceed==true) {
												proceed=saveAnalysisSettings();
											}
											if(proceed==true) {
												proceed=saveMarkovSettings();
											}

											if(proceed==true){
												boolean sampleParams=chckbxSampleParameters.isSelected();

												//Evaluate updates
												String testExp=paneExpression.getText();
												proceed=evaluateUpdates();

												if(proceed==true){ //save
													scenario.name=testName;
													//uncertainty
													scenario.numIterations=testIterations;
													scenario.cohortSize=testCohortSize;
													scenario.crn1=chckbxSeed1.isSelected();
													scenario.seed1=testSeed1;
													scenario.sampleParams=sampleParams;
													scenario.crn2=chckbxSeedParams.isSelected();
													scenario.seed2=testSeed2;
													scenario.useParamSets=chckbxUseParameterSets.isSelected();

													//analysis
													scenario.analysisType=testAnalysisType;
													scenario.objective=testObjective;
													scenario.objectiveDim=testObjectiveDim;
													scenario.costDim=testCostDim;
													scenario.effectDim=testEffectDim;
													scenario.WTP=testWTP;
													scenario.baseScenario=testBaseStrategy;
													scenario.extendedDim=testExtendedDim;

													//markov
													scenario.halfCycleCorrection=testHalfCycleCorrection;
													scenario.discountRewards=testDiscountRewards;
													scenario.discountRates=testDiscountRates;
													scenario.discountStartCycle=testDiscountStartCycle;

													scenario.objectUpdates=testExp;
													scenario.notes=testNotes;

													if(scenarioNum==-1){schedule.scenarios.add(scenario);}
													schedule.updateScenarios();

													frmDefineScenario.dispose();
												}
											}
										}
									});
									
									GridBagConstraints gbc_btnSave = new GridBagConstraints();
									gbc_btnSave.anchor = GridBagConstraints.NORTHEAST;
									gbc_btnSave.insets = new Insets(0, 5, 0, 5);
									gbc_btnSave.gridx = 3;
									gbc_btnSave.gridy = 4;
									frmDefineScenario.getContentPane().add(btnSave, gbc_btnSave);
						
									JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
									btnCancel.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											frmDefineScenario.dispose();
										}
									});
									
									GridBagConstraints gbc_btnCancel = new GridBagConstraints();
									gbc_btnCancel.anchor = GridBagConstraints.NORTHWEST;
									gbc_btnCancel.insets = new Insets(0, 5, 0, 5);
									gbc_btnCancel.gridx = 4;
									gbc_btnCancel.gridy = 4;
									frmDefineScenario.getContentPane().add(btnCancel, gbc_btnCancel);
			if(myModel.simType==1){lblCohortSize.setText(myModel.language.analysis.getString("sim.num_simulations")+":");}
			if(myModel.parameterSets==null) {
				chckbxUseParameterSets.setEnabled(false);
			}
			

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void setAnalysisType(int analysisType){
		tableAnalysis.analysisType=analysisType;
		if(analysisType==0){ //EV
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("gen.objective"),null}); tableAnalysis.enabled[0]=true; //Objective
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("result.outcome"),null}); tableAnalysis.enabled[1]=true; //Outcome
		}
		else{ //CEA, BCA, or ECEA
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.cost"),null}); tableAnalysis.enabled[0]=true; //Cost
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.effect"),null}); tableAnalysis.enabled[1]=true; //Effect
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.baseline_strategy"),null}); tableAnalysis.enabled[2]=true; //Baseline Strategy
			modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.wtp"),null}); tableAnalysis.enabled[3]=true; //Willingness-to-pay (WTP)
			if(analysisType==1){ //CEA
				tableAnalysis.setValueAt(myModel.language.analysis.getString("cea.effect"), 1, 0); //Effect
			}
			else if(analysisType==2){ //BCA
				tableAnalysis.setValueAt(myModel.language.analysis.getString("bca.benefit"), 1, 0); //Benefit
				tableAnalysis.enabled[2]=false; //baseline strategy
			}
			else if(analysisType==3){ //ECEA
				modelAnalysis.addRow(new Object[]{myModel.language.analysis.getString("cea.additional_dimension"),null}); tableAnalysis.enabled[4]=true; //Additional Dimension
			}
		}

		tableAnalysis.repaint();
	}

	private boolean saveAnalysisSettings() {
		boolean valid=true;
		//Check analysis settings
		testAnalysisType=comboAnalysis.getSelectedIndex();

		if(testAnalysisType==0){ //EV
			String strObj=(String) tableAnalysis.getValueAt(0, 1);
			if(strObj==null){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_objective")); //Please select an objective!
				return(valid);
			}
			else{
				if(strObj.matches(myModel.language.analysis.getString("gen.maximize"))){testObjective=0;} //Maximize
				else{testObjective=1;}
			}
			String strDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_outcome")); //Please select an outcome!
				return(valid);
			}
			else{
				testObjectiveDim=getDimIndex(strDim);
			}
		}
		else{ //CEA, BCA, or ECEA
			String strCostDim=(String) tableAnalysis.getValueAt(0, 1);
			if(strCostDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_cost")); //Please select a Cost!
				return(valid);
			}
			else{testCostDim=getDimIndex(strCostDim);}
			String strEffectDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strEffectDim==null){
				valid=false;
				if(testAnalysisType==1 || testAnalysisType==3){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_effect"));} //CEA or ECEA: Please select an Effect!
				else if(testAnalysisType==2){JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_benefit"));} //BCA: Please select a Benefit!
				return(valid);
			}
			else{
				testEffectDim=getDimIndex(strEffectDim);
			}
			if(testCostDim==testEffectDim){
				valid=false;
				if(testAnalysisType==1 || testAnalysisType==3){ //CEA or ECEA
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.same_cost_effect")); //Cost and Effect must be different!
				}
				else if(testAnalysisType==2){ //BCA
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.same_cost_benefit")); //Cost and Benefit must be different!
				}
				return(valid);
			}
			if(testAnalysisType==1){ //CEA
				testBaseStrategy=(String)tableAnalysis.getValueAt(2, 1);
				if(testBaseStrategy==null || testBaseStrategy.isEmpty()){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.choose_baseline")); //Please choose a baseline strategy!
					return(valid);
				}
				else{
					int baseIndex=myModel.getStrategyIndex(testBaseStrategy);
					if(baseIndex==-1){
						valid=false;
						JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.baseline_not_recognized")); //Baseline strategy not recognized!
						return(valid);
					}
				}
			}
			try{
				String strWTP=(String) tableAnalysis.getValueAt(3, 1);
				testWTP=Double.parseDouble(strWTP.replaceAll(",",""));
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_wtp")); //Please enter a valid willingness-to-pay!
				return(valid);
			}
			if(testAnalysisType==3){ //ECEA
				String strExtendedDim=(String) tableAnalysis.getValueAt(4, 1);
				if(strExtendedDim==null){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.select_additional_dimension")); //Please select an Additional Dimension!
					return(valid);
				}
				else{
					testExtendedDim=getDimIndex(strExtendedDim);
				}
				if(testExtendedDim==testCostDim || testExtendedDim==testEffectDim){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.same_add_dim")); //Additional Dimension must be different from Cost and Effect!
					return(valid);
				}
			}
		}
		return(valid);
	}

	private boolean saveMarkovSettings() {
		boolean valid=true;
		//Check Markov settings
		if(myModel.type==1) {
			testHalfCycleCorrection=chckbxHalfcycleCorrection.isSelected();

			//discount rates
			testDiscountRewards=chckbxDiscountRewards.isSelected();
			if(testDiscountRewards){
				try{
					testDiscountStartCycle=Integer.parseInt(textDiscountStartCycle.getText());
				} catch(Exception er){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_discount_start")+" ("+textDiscountStartCycle.getText()+")"); //Please enter a valid discount start cycle
					return(valid);
				}

				int numDim=myModel.dimInfo.dimNames.length;
				testDiscountRates=new String[numDim];
				for(int i=0; i<numDim; i++){
					try{
						String test=(String) tableDiscount.getValueAt(i, 1); 
						Numeric testVal=Interpreter.evaluate(test, myModel,false,myModel.language);
						if(!(testVal.isDouble() || testVal.isInteger()) ){ //not double or integer
							valid=false;
							JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_discount_rate")+" ("+tableDiscount.getValueAt(i, 0)+")"); //Please enter a valid discount rate
						}
						//check range of discount value
						double curVal=testVal.getValue();
						if(curVal<0) {
							JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("warn.discount_rate_negative")+" ("+tableDiscount.getValueAt(i, 0)+")"); //Warning: Discount rate <0
						}
						else if(curVal>0 && curVal<0.5) {
							//Warning: Discount rates should be a percentage (%).  Current value is [value]
							String msg=MessageFormat.format(myModel.language.message.getString("warn.discount_rate_percent"), curVal);
							JOptionPane.showMessageDialog(frmDefineScenario, msg+" ("+tableDiscount.getValueAt(i, 0)+")"); 
						}

						testDiscountRates[i]=test;
					} catch(Exception er){
						valid=false;
						JOptionPane.showMessageDialog(frmDefineScenario, myModel.language.message.getString("err.valid_discount_rate")+" ("+tableDiscount.getValueAt(i, 0)+")"); //Please enter a valid discount rate
						return(valid);
					}
				}
			}
		}

		return(valid);
	}

	private int getDimIndex(String dimName){
		int index=-1;
		int i=-1;
		boolean found=false;
		while(found==false && i<myModel.dimInfo.dimNames.length){
			i++;
			if(myModel.dimInfo.dimNames[i].matches(dimName)){
				found=true;
				index=i;
			}
		}
		return(index);
	}

	private boolean evaluateUpdates(){
		boolean valid=true;
		String testExp=paneExpression.getText();
		if(testExp.isEmpty()){ //no updates
			paneValue.setText("["+myModel.language.base.getString("object.no_updates")+"]"); //No updates
		}
		else{
			Scenario test=new Scenario();
			test.objectUpdates=testExp;

			try{
				test.parseUpdates(myModel);
			} catch(Exception e){
				valid=false;
				paneValue.setText(e.getMessage());
			}

			if(valid==true){
				int numUpdates=test.objectNames.length;
				paneValue.setText("");
				String ev="";
				for(int i=0; i<numUpdates; i++){
					ev+=test.objectNames[i]+": "+test.testVals[i].toString()+"\n";
				}
				paneValue.setText(ev);
			}
		}
		return(valid);

	}


	private void getScenario(){
		textName.setText(scenario.name);

		//uncertainty
		textIterations.setText(scenario.numIterations+"");
		textCohortSize.setText(scenario.cohortSize+"");
		chckbxSeed1.setSelected(scenario.crn1);
		if(chckbxSeed1.isSelected()){textSeed1.setText(scenario.seed1+"");}
		chckbxSampleParameters.setSelected(scenario.sampleParams);
		chckbxSeedParams.setSelected(scenario.crn2);
		if(chckbxSeedParams.isSelected()){textSeedParams.setText(scenario.seed2+"");}
		chckbxUseParameterSets.setSelected(scenario.useParamSets);

		//analysis
		comboAnalysis.setSelectedIndex(scenario.analysisType);
		setAnalysisType(scenario.analysisType);
		if(myModel.dimInfo.dimNames.length==1){
			lblAnalysisType.setEnabled(false);
			comboAnalysis.setEnabled(false);
		}
		else{
			lblAnalysisType.setEnabled(true);
			comboAnalysis.setEnabled(true);
		}
		if(scenario.analysisType==0){ //EV
			if(scenario.objective==0){tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.maximize"), 0, 1);} //Maximize
			else if(scenario.objective==1){tableAnalysis.setValueAt(myModel.language.analysis.getString("gen.minimize"), 0, 1);} //Minimize
			tableAnalysis.setValueAt(myModel.dimInfo.dimNames[scenario.objectiveDim], 1, 1);
		}
		else{ //CEA, BCA, or ECEA
			tableAnalysis.setValueAt(myModel.dimInfo.dimNames[scenario.costDim], 0, 1);
			tableAnalysis.setValueAt(myModel.dimInfo.dimNames[scenario.effectDim], 1, 1);
			if(scenario.analysisType==1){ //CEA
				tableAnalysis.setValueAt(scenario.baseScenario, 2, 1);
			}
			tableAnalysis.setValueAt(scenario.WTP+"",3,1);
			if(scenario.analysisType==3){ //ECEA
				tableAnalysis.setValueAt(myModel.dimInfo.dimNames[scenario.extendedDim], 4, 1);
			}
		}

		//markov
		if(myModel.type==1) { //markov
			int numDim=myModel.dimInfo.dimNames.length;
			if(scenario.discountRates==null || scenario.discountRates.length!=numDim) { //get defaults
				chckbxHalfcycleCorrection.setSelected(myModel.markov.halfCycleCorrection);
				chckbxDiscountRewards.setSelected(myModel.markov.discountRewards);
				textDiscountStartCycle.setText(myModel.markov.discountStartCycle+"");
				for(int d=0; d<numDim; d++) {
					modelDiscount.addRow(new Object[] {null});
					modelDiscount.setValueAt(myModel.dimInfo.dimNames[d], d, 0);
					modelDiscount.setValueAt(myModel.markov.discountRates[d]+"", d, 1);
				}
			}
			else { //get scenario
				chckbxHalfcycleCorrection.setSelected(scenario.halfCycleCorrection);
				chckbxDiscountRewards.setSelected(scenario.discountRewards);
				textDiscountStartCycle.setText(scenario.discountStartCycle+"");
				for(int d=0; d<numDim; d++) {
					modelDiscount.addRow(new Object[] {null});
					modelDiscount.setValueAt(myModel.dimInfo.dimNames[d], d, 0);
					modelDiscount.setValueAt(scenario.discountRates[d]+"", d, 1);
				}
			}
			if(chckbxDiscountRewards.isSelected()==false) {
				textDiscountStartCycle.setEnabled(false);
				tableDiscount.setEnabled(false);
			}
		}
		else {
			lblMarkov.setEnabled(false);
			chckbxHalfcycleCorrection.setEnabled(false);
			chckbxDiscountRewards.setEnabled(false);
			lblDiscountStartCycle.setEnabled(false);
			textDiscountStartCycle.setEnabled(false);
			tableDiscount.setEnabled(false);
		}


		//object updates
		paneExpression.setText(scenario.objectUpdates);
		paneExpression.restyle();
		textNotes.setText(scenario.notes);
	}
}
