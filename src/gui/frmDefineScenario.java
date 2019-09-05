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
	double testDiscountRates[];
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
			frmDefineScenario.setTitle("Amua - Define Scenario");
			frmDefineScenario.setResizable(false);
			frmDefineScenario.setBounds(100, 100, 679, 464);
			frmDefineScenario.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineScenario.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					//Ensure name is valid and unique
					String testName=textName.getText();
					String testNotes=textNotes.getText();
					int testIterations=-1, testCohortSize=-1, testSeed1=-1, testSeed2=-1;
					if(testName.length()==0){
						JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a name!"); 
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
							JOptionPane.showMessageDialog(frmDefineScenario, testName+" is already defined!");
							proceed=false;
						}

						//Check iterations
						try{
							testIterations=Integer.parseInt(textIterations.getText());
						} catch(Exception e1){
							JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid number of iterations!");
							proceed=false;
						}
						if(proceed && testIterations<=0){
							JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid number of iterations!");
							proceed=false;
						}

						//Check cohort size
						try{
							testCohortSize=Integer.parseInt(textCohortSize.getText());
						}catch(Exception e1){
							if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid cohort size!");}
							else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid # of simulations!");}
							proceed=false;
						}
						if(proceed && testCohortSize<=0){
							if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid cohort size!");}
							else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid # of simulations!");}
							proceed=false;
						}


						//check seeds
						if(chckbxSeed1.isSelected()){
							try{
								testSeed1=Integer.parseInt(textSeed1.getText());
							} catch(Exception e1){
								JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid seed!");
								proceed=false;
							}
						}
						if(chckbxSeedParams.isSelected()){
							try{
								testSeed2=Integer.parseInt(textSeedParams.getText());
							} catch(Exception e1){
								JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid parameter seed!");
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
			btnSave.setBounds(230, 400, 90, 28);
			frmDefineScenario.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineScenario.dispose();
				}
			});
			btnCancel.setBounds(329, 400, 90, 28);
			frmDefineScenario.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Scenario Name:");
			lblName.setBounds(6, 13, 107, 16);
			frmDefineScenario.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(108, 7, 296, 28);
			frmDefineScenario.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(6, 360, 55, 16);
			frmDefineScenario.getContentPane().add(lblNotes);

			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(55, 338, 609, 63);
			frmDefineScenario.getContentPane().add(scrollPane_1);

			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setBounds(6, 36, 398, 109);
			frmDefineScenario.getContentPane().add(panel);
			panel.setLayout(null);

			JLabel lblModelUncertainty = new JLabel("Model Uncertainty");
			lblModelUncertainty.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblModelUncertainty.setBounds(6, 6, 107, 16);
			panel.add(lblModelUncertainty);

			JLabel lblIterations = new JLabel("# iterations:");
			lblIterations.setBounds(128, 6, 64, 16);
			panel.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setBounds(196, 1, 55, 28);
			panel.add(textIterations);
			textIterations.setText("1");
			textIterations.setColumns(10);

			chckbxSampleParameters = new JCheckBox("Sample Parameters");
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
			chckbxSampleParameters.setBounds(192, 45, 143, 18);
			panel.add(chckbxSampleParameters);

			JLabel lblFirstorder = new JLabel("First-order");
			lblFirstorder.setHorizontalAlignment(SwingConstants.CENTER);
			lblFirstorder.setFont(new Font("SansSerif", Font.ITALIC, 12));
			lblFirstorder.setBounds(6, 30, 150, 16);
			panel.add(lblFirstorder);

			JLabel lblSecondorder = new JLabel("Second-order");
			lblSecondorder.setHorizontalAlignment(SwingConstants.CENTER);
			lblSecondorder.setFont(new Font("SansSerif", Font.ITALIC, 12));
			lblSecondorder.setBounds(192, 30, 200, 16);
			panel.add(lblSecondorder);

			chckbxSeedParams = new JCheckBox("Seed Parameter RNG");
			chckbxSeedParams.setEnabled(false);
			chckbxSeedParams.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeedParams.isSelected()){textSeedParams.setEnabled(true);}
					else{textSeedParams.setEnabled(false);}
				}
			});
			chckbxSeedParams.setBounds(192, 65, 145, 18);
			panel.add(chckbxSeedParams);

			textSeedParams = new JTextField();
			textSeedParams.setEnabled(false);
			textSeedParams.setBounds(338, 58, 54, 28);
			panel.add(textSeedParams);
			textSeedParams.setColumns(10);

			chckbxSeed1 = new JCheckBox("Seed RNG");
			chckbxSeed1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeed1.isSelected()){textSeed1.setEnabled(true);}
					else{textSeed1.setEnabled(false);}
				}
			});
			chckbxSeed1.setBounds(16, 77, 90, 18);
			panel.add(chckbxSeed1);

			textSeed1 = new JTextField();
			textSeed1.setEnabled(false);
			textSeed1.setColumns(10);
			textSeed1.setBounds(102, 72, 54, 28);
			panel.add(textSeed1);

			JLabel lblCohortSize = new JLabel("Cohort size:");
			if(myModel.simType==1){lblCohortSize.setText("# simulations:");}
			lblCohortSize.setBounds(6, 52, 80, 16);
			panel.add(lblCohortSize);

			textCohortSize = new JTextField();
			textCohortSize.setBounds(85, 46, 74, 28);
			panel.add(textCohortSize);
			textCohortSize.setColumns(10);
			
			chckbxUseParameterSets = new JCheckBox("Use Parameter Sets");
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
			chckbxUseParameterSets.setBounds(192, 85, 155, 18);
			panel.add(chckbxUseParameterSets);
			if(myModel.parameterSets==null) {
				chckbxUseParameterSets.setEnabled(false);
			}

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_1.setBounds(6, 151, 398, 182);
			frmDefineScenario.getContentPane().add(panel_1);
			panel_1.setLayout(null);

			JLabel lblExpression = new JLabel("Object Updates");
			lblExpression.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblExpression.setBounds(35, 9, 101, 16);
			panel_1.add(lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 27, 386, 87);
			panel_1.add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.setBounds(6, 113, 90, 28);
			panel_1.add(btnEvaluate);

			JLabel lblValue = new JLabel("Expected Values:");
			lblValue.setBounds(6, 143, 101, 16);
			panel_1.add(lblValue);

			JScrollPane scrollPaneValue = new JScrollPane();
			scrollPaneValue.setBounds(110, 118, 282, 63);
			panel_1.add(scrollPaneValue);

			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluateUpdates();
				}
			});

			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluateUpdates();
					}
				}
			});
			
			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			toolBar.setBounds(1, 6, 48, 24);
			panel_1.add(toolBar);
			
			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,paneExpression,true);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText("Build Expression");
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ScaledIcon("/images/formula",24,24,24,true));
			toolBar.add(btnFx);
			
			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_2.setBounds(410, 7, 254, 138);
			frmDefineScenario.getContentPane().add(panel_2);
			panel_2.setLayout(null);
			
			JLabel lblAnalysis = new JLabel("Analysis");
			lblAnalysis.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblAnalysis.setBounds(6, 6, 54, 16);
			panel_2.add(lblAnalysis);
			
			lblAnalysisType = new JLabel("Type:");
			lblAnalysisType.setBounds(6, 29, 38, 16);
			panel_2.add(lblAnalysisType);
			
			comboAnalysis = new JComboBox<String>();
			comboAnalysis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setAnalysisType(comboAnalysis.getSelectedIndex());
				}
			});
			comboAnalysis.setModel(new DefaultComboBoxModel(new String[] {"Expected Value (EV)", "Cost-Effectiveness Analysis (CEA)", "Benefit-Cost Analysis (BCA)", "Extended Cost-Effectiveness Analysis (ECEA)"}));
			comboAnalysis.setBounds(39, 24, 209, 26);
			panel_2.add(comboAnalysis);
			
			JScrollPane scrollPane_Analysis = new JScrollPane();
			scrollPane_Analysis.setBounds(6, 52, 242, 80);
			panel_2.add(scrollPane_Analysis);
			
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
			tableAnalysis.tempDimInfo=myModel.dimInfo; //can't modify dimensions in scenarios
			tableAnalysis.getTableHeader().setReorderingAllowed(false);
			tableAnalysis.setModel(modelAnalysis);
			tableAnalysis.getColumnModel().getColumn(0).setPreferredWidth(170);
			tableAnalysis.getColumnModel().getColumn(1).setPreferredWidth(170);
			tableAnalysis.setShowVerticalLines(true);
			tableAnalysis.setRowSelectionAllowed(false);
			tableAnalysis.putClientProperty("terminateEditOnFocusLost", true);
			scrollPane_Analysis.setViewportView(tableAnalysis);
			
			JPanel panel_Markov = new JPanel();
			panel_Markov.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_Markov.setBounds(410, 151, 254, 182);
			frmDefineScenario.getContentPane().add(panel_Markov);
			panel_Markov.setLayout(null);
			
			lblMarkov = new JLabel("Markov");
			lblMarkov.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblMarkov.setBounds(6, 6, 54, 16);
			panel_Markov.add(lblMarkov);
			
			chckbxHalfcycleCorrection = new JCheckBox("Half-cycle correction");
			chckbxHalfcycleCorrection.setBounds(6, 25, 138, 18);
			panel_Markov.add(chckbxHalfcycleCorrection);
			
			chckbxDiscountRewards = new JCheckBox("Discount Rewards");
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
			chckbxDiscountRewards.setBounds(6, 46, 138, 18);
			panel_Markov.add(chckbxDiscountRewards);
			
			lblDiscountStartCycle = new JLabel("Discount start cycle:");
			lblDiscountStartCycle.setBounds(6, 71, 119, 16);
			panel_Markov.add(lblDiscountStartCycle);
			
			textDiscountStartCycle = new JTextField();
			textDiscountStartCycle.setBounds(118, 65, 47, 28);
			panel_Markov.add(textDiscountStartCycle);
			textDiscountStartCycle.setColumns(10);
			
			JScrollPane scrollPane_Discount = new JScrollPane();
			scrollPane_Discount.setBounds(6, 99, 242, 77);
			panel_Markov.add(scrollPane_Discount);
			
			modelDiscount=new DefaultTableModel(
					new Object[][] {},
					new String[] {"Dimension", "Discount Rate (%)"}
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
	
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void setAnalysisType(int analysisType){
		tableAnalysis.analysisType=analysisType;
		if(analysisType==0){ //EV
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{"Objective",null}); tableAnalysis.enabled[0]=true;
			modelAnalysis.addRow(new Object[]{"Outcome",null}); tableAnalysis.enabled[1]=true;
		}
		else{ //CEA, BCA, or ECEA
			modelAnalysis.setRowCount(0);
			modelAnalysis.addRow(new Object[]{"Cost",null}); tableAnalysis.enabled[0]=true;
			modelAnalysis.addRow(new Object[]{"Effect",null}); tableAnalysis.enabled[1]=true;
			modelAnalysis.addRow(new Object[]{"Baseline Strategy",null}); tableAnalysis.enabled[2]=true;
			modelAnalysis.addRow(new Object[]{"Willingness-to-pay (WTP)",null}); tableAnalysis.enabled[3]=true;
			if(analysisType==1){ //CEA
				tableAnalysis.setValueAt("Effect", 1, 0);
			}
			else if(analysisType==2){ //BCA
				tableAnalysis.setValueAt("Benefit", 1, 0);
				tableAnalysis.enabled[2]=false; //baseline strategy
			}
			else if(analysisType==3){ //ECEA
				modelAnalysis.addRow(new Object[]{"Additional Dimension",null}); tableAnalysis.enabled[4]=true;
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
				JOptionPane.showMessageDialog(frmDefineScenario, "Please select an objective!");
				return(valid);
			}
			else{
				if(strObj.matches("Maximize")){testObjective=0;}
				else{testObjective=1;}
			}
			String strDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strDim==null){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, "Please select an outcome!");
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
				JOptionPane.showMessageDialog(frmDefineScenario, "Please select a Cost!");
				return(valid);
			}
			else{testCostDim=getDimIndex(strCostDim);}
			String strEffectDim=(String) tableAnalysis.getValueAt(1, 1);
			if(strEffectDim==null){
				valid=false;
				if(testAnalysisType==1 || testAnalysisType==3){JOptionPane.showMessageDialog(frmDefineScenario, "Please select an Effect!");} //CEA or ECEA
				else if(testAnalysisType==2){JOptionPane.showMessageDialog(frmDefineScenario, "Please select a Benefit!");} //BCA
				return(valid);
			}
			else{
				testEffectDim=getDimIndex(strEffectDim);
			}
			if(testCostDim==testEffectDim){
				valid=false;
				if(testAnalysisType==1 || testAnalysisType==3){ //CEA or ECEA
					JOptionPane.showMessageDialog(frmDefineScenario, "Cost and Effect must be different!");
				}
				else if(testAnalysisType==2){ //BCA
					JOptionPane.showMessageDialog(frmDefineScenario, "Cost and Benefit must be different!");
				}
				return(valid);
			}
			if(testAnalysisType==1){ //CEA
				testBaseStrategy=(String)tableAnalysis.getValueAt(2, 1);
				if(testBaseStrategy==null || testBaseStrategy.isEmpty()){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, "Please choose a baseline strategy!");
					return(valid);
				}
				else{
					int baseIndex=myModel.getStrategyIndex(testBaseStrategy);
					if(baseIndex==-1){
						valid=false;
						JOptionPane.showMessageDialog(frmDefineScenario, "Baseline strategy not recognized!");
						return(valid);
					}
				}
			}
			try{
				String strWTP=(String) tableAnalysis.getValueAt(3, 1);
				testWTP=Double.parseDouble(strWTP.replaceAll(",",""));
			} catch(Exception er){
				valid=false;
				JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid willingness-to-pay!");
				return(valid);
			}
			if(testAnalysisType==3){ //ECEA
				String strExtendedDim=(String) tableAnalysis.getValueAt(4, 1);
				if(strExtendedDim==null){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, "Please select an Additional Dimension!");
					return(valid);
				}
				else{
					testExtendedDim=getDimIndex(strExtendedDim);
				}
				if(testExtendedDim==testCostDim || testExtendedDim==testEffectDim){
					valid=false;
					JOptionPane.showMessageDialog(frmDefineScenario, "Additional Dimension must be different from Cost and Effect!");
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
					JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid discount start cycle ("+textDiscountStartCycle.getText()+")");
					return(valid);
				}

				int numDim=myModel.dimInfo.dimNames.length;
				testDiscountRates=new double[numDim];
				for(int i=0; i<numDim; i++){
					try{
						double test=Double.parseDouble((String) tableDiscount.getValueAt(i, 1));
						testDiscountRates[i]=test;
					} catch(Exception er){
						valid=false;
						JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid discount rate ("+tableDiscount.getValueAt(i, 0)+")");
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
			paneValue.setText("[No updates]");
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
			if(scenario.objective==0){tableAnalysis.setValueAt("Maximize", 0, 1);}
			else if(scenario.objective==1){tableAnalysis.setValueAt("Minimize", 0, 1);}
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
