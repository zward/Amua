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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import base.AmuaModel;
import base.MicroStatsSummary;
import base.RunReport;
import base.RunReportSummary;
import filters.CSVFilter;
import main.CEAHelper;
import main.Constraint;
import main.DimInfo;
import main.MersenneTwisterFast;
import main.Parameter;
import main.Scenario;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;

import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/**
 *
 */
public class frmScenarios {

	frmScenarios me;
	public JFrame frmScenarios;
	AmuaModel myModel;
	DefaultTableModel modelSchedule;
	private JTable tableSchedule;
	JList<String> listSchedule;
	DefaultListModel<String> listModelSchedule;

	int numStrat;
	int scenarioIndices[];
	int numScenarios;
	String scenarioNames[];
	
	JComboBox comboGroup;
	int numSubgroups=0;
	String subgroupNames[];
	
	/**
	 * [Scenario][Group][Outcome][Strategy][Iteration]
	 */
	double results[][][][][];
	double meanResults[][][][];
	double lbResults[][][][];
	double ubResults[][][][];
	double curResults[][][][];
	
	RunReport runReports[][];
	RunReportSummary runReportSummaries[];
	
	String CEAnotes[][][][];
	ArrayList<Scenario> scenarios;

	int numParams, numVars, numConstraints;
	//original model settings
	boolean origCRN;
	int origSeed, origCohortSize;
	String origParams[], origVars[];
	
	JTabbedPane tabbedPane;
	DefaultTableModel modelResults[], modelIndResults[];
	private JTable tableResults, tableIndResults;
	JCheckBox chckbxExportIterations;
	

	public frmScenarios(AmuaModel myModel){
		this.myModel=myModel;
		me=this;
		scenarios=new ArrayList<Scenario>();
		if(myModel.scenarios!=null){
			for(int i=0; i<myModel.scenarios.size(); i++){
				scenarios.add(myModel.scenarios.get(i).copy());
			}
		}
		initialize();
		updateScenarios();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmScenarios = new JFrame();
			frmScenarios.setTitle("Amua - Scenarios");
			frmScenarios.setIconImage(Toolkit.getDefaultToolkit().getImage(frmScenarios.class.getResource("/images/scenario_16.png")));
			frmScenarios.setBounds(100, 100, 1100, 500);
			frmScenarios.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 250, 180, 0};
			gridBagLayout.rowHeights = new int[]{0, 514, 32, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			frmScenarios.getContentPane().setLayout(gridBagLayout);

			String outcomes[] = null;
			DimInfo dimInfo=myModel.dimInfo;
			if(dimInfo.analysisType==0){ //EV
				outcomes=new String[dimInfo.dimNames.length];
				for(int d=0; d<dimInfo.dimNames.length; d++){
					outcomes[d]=dimInfo.dimNames[d];
				}
			}
			else{ //CEA or BCA
				outcomes=new String[dimInfo.dimNames.length+1];
				for(int d=0; d<dimInfo.dimNames.length; d++){
					outcomes[d]=dimInfo.dimNames[d];
				}
				if(dimInfo.analysisType==1){ //CEA
					outcomes[dimInfo.dimNames.length]="ICER ("+dimInfo.dimNames[dimInfo.costDim]+"/"+dimInfo.dimNames[dimInfo.effectDim]+")";
				}
				else if(dimInfo.analysisType==2){ //BCA
					outcomes[dimInfo.dimNames.length]="NMB ("+dimInfo.dimNames[dimInfo.effectDim]+"-"+dimInfo.dimNames[dimInfo.costDim]+")";
				}
			}

			modelSchedule=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"#", "Scenario Name","# Iterations","Object Updates","Sample Parameters"}) {
				public boolean isCellEditable(int row, int column) {return false;}
			};

			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.WEST;
			gbc_toolBar.insets = new Insets(0, 0, 5, 5);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			frmScenarios.getContentPane().add(toolBar, gbc_toolBar);

			JButton btnAdd = new JButton();
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmDefineScenario window=new frmDefineScenario(myModel,-1,me);
					window.frmDefineScenario.setVisible(true);

				}
			});
			btnAdd.setToolTipText("Add");
			URL imageURL = frmMain.class.getResource("/images/add.png");
			btnAdd.setIcon(new ImageIcon(imageURL,"Add"));
			toolBar.add(btnAdd);

			JButton btnEdit = new JButton();
			btnEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index=tableSchedule.getSelectedRow();
					if(index!=-1){
						frmDefineScenario window=new frmDefineScenario(myModel,index,me);
						window.frmDefineScenario.setVisible(true);
					}
				}
			});
			btnEdit.setToolTipText("Edit");
			imageURL = frmMain.class.getResource("/images/edit_16.png");
			btnEdit.setIcon(new ImageIcon(imageURL,"Edit"));
			toolBar.add(btnEdit);

			JButton btnDelete = new JButton();
			btnDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int index=tableSchedule.getSelectedRow();
					if(index!=-1){
						scenarios.remove(index);
						updateScenarios();
					}
				}
			});
			btnDelete.setToolTipText("Delete");
			imageURL = frmMain.class.getResource("/images/delete.png");
			btnDelete.setIcon(new ImageIcon(imageURL,"Delete"));
			toolBar.add(btnDelete);

			JButton btnMoveUp = new JButton();
			btnMoveUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index=tableSchedule.getSelectedRow();
					if(index>0){
						int colIndex=tableSchedule.getSelectedColumn();
						Scenario curRun=scenarios.get(index);
						scenarios.remove(index);
						scenarios.add(index-1,curRun);
						updateScenarios();
						tableSchedule.changeSelection(index-1, colIndex, false, false);
						tableSchedule.requestFocus();
					}
				}
			});
			btnMoveUp.setToolTipText("Move Up");
			imageURL = frmMain.class.getResource("/images/upArrow_16.png");
			btnMoveUp.setIcon(new ImageIcon(imageURL,"Move Up"));
			toolBar.add(btnMoveUp);

			JButton btnMoveDown = new JButton();
			btnMoveDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index=tableSchedule.getSelectedRow();
					if(index!=-1 && index<scenarios.size()-1){
						int colIndex=tableSchedule.getSelectedColumn();
						Scenario curRun=scenarios.get(index);
						scenarios.remove(index);
						scenarios.add(index+1,curRun);
						updateScenarios();
						tableSchedule.changeSelection(index+1, colIndex, false, false);
						tableSchedule.requestFocus();
					}
				}
			});
			btnMoveDown.setToolTipText("Move Down");
			imageURL = frmMain.class.getResource("/images/downArrow_16.png");
			btnMoveDown.setIcon(new ImageIcon(imageURL,"Move Down"));
			toolBar.add(btnMoveDown);

			JLabel lblSelectScenariosTo = new JLabel("Select Scenarios to Run:");
			GridBagConstraints gbc_lblSelectScenariosTo = new GridBagConstraints();
			gbc_lblSelectScenariosTo.anchor = GridBagConstraints.SOUTH;
			gbc_lblSelectScenariosTo.insets = new Insets(0, 0, 5, 5);
			gbc_lblSelectScenariosTo.gridx = 1;
			gbc_lblSelectScenariosTo.gridy = 0;
			frmScenarios.getContentPane().add(lblSelectScenariosTo, gbc_lblSelectScenariosTo);

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
			gbc_tabbedPane.gridheight = 2;
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 2;
			gbc_tabbedPane.gridy = 0;
			frmScenarios.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panel_3 = new JPanel();
			tabbedPane.addTab("Results", null, panel_3, null);
			panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

			JScrollPane scrollPaneResults = new JScrollPane();
			panel_3.add(scrollPaneResults);
			
			JPanel panel_2 = new JPanel();
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.gridwidth = 3;
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.insets = new Insets(0, 0, 0, 5);
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 2;
			frmScenarios.getContentPane().add(panel_2, gbc_panel_2);
			panel_2.setBorder(null);
			panel_2.setLayout(null);

			comboGroup = new JComboBox();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=comboGroup.getSelectedIndex();
					tableResults.setModel(modelResults[selected]);
					tableIndResults.setModel(modelIndResults[selected]);
				}
			});
			comboGroup.setVisible(false);
			comboGroup.setBounds(935, 1, 136, 26);
			panel_2.add(comboGroup);

			if(myModel.simType==1 && myModel.reportSubgroups){
				numSubgroups=myModel.subgroupNames.size();
				subgroupNames=new String[numSubgroups+1];
				subgroupNames[0]="Overall";
				for(int g=0; g<numSubgroups; g++){
					subgroupNames[g+1]=myModel.subgroupNames.get(g);
				}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
			}
			
			
			modelResults=new DefaultTableModel[numSubgroups+1];
			for(int g=0; g<numSubgroups+1; g++){
				modelResults[g]=new DefaultTableModel(
						new Object[][] {,},
						new String[] {}) {
					public boolean isCellEditable(int row, int column) {return false;}
				};
			}
			
			tableResults = new JTable();
			tableResults.setModel(modelResults[0]);
			tableResults.setRowSelectionAllowed(false);
			tableResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableResults.setShowVerticalLines(true);
			tableResults.getTableHeader().setReorderingAllowed(false);
			tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			scrollPaneResults.setViewportView(tableResults);
			
			JPanel panel = new JPanel();
			tabbedPane.addTab("Individual-level Results", null, panel, null);
			tabbedPane.setEnabledAt(1, false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			
			JScrollPane scrollPane_1 = new JScrollPane();
			panel.add(scrollPane_1);
			
			modelIndResults=new DefaultTableModel[numSubgroups+1];
			for(int g=0; g<numSubgroups+1; g++){
				modelIndResults[g]=new DefaultTableModel(
						new Object[][] {,},
						new String[] {}) {
					public boolean isCellEditable(int row, int column) {return false;}
				};
			}
			
			tableIndResults = new JTable();
			tableIndResults.setModel(modelIndResults[0]);
			tableIndResults.setRowSelectionAllowed(false);
			tableIndResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableIndResults.setShowVerticalLines(true);
			tableIndResults.getTableHeader().setReorderingAllowed(false);
			tableIndResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			scrollPane_1.setViewportView(tableIndResults);
			
			JScrollPane scrollPaneSchedule = new JScrollPane();
			GridBagConstraints gbc_scrollPaneSchedule = new GridBagConstraints();
			gbc_scrollPaneSchedule.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneSchedule.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPaneSchedule.gridx = 0;
			gbc_scrollPaneSchedule.gridy = 1;
			frmScenarios.getContentPane().add(scrollPaneSchedule, gbc_scrollPaneSchedule);

			tableSchedule = new JTable();
			tableSchedule.setRowSelectionAllowed(false);
			tableSchedule.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableSchedule.setShowVerticalLines(true);
			tableSchedule.getTableHeader().setReorderingAllowed(false);
			tableSchedule.setModel(modelSchedule);
			tableSchedule.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tableSchedule.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
						int selected=tableSchedule.getSelectedRow();
						if(selected!=-1){
							frmDefineScenario window=new frmDefineScenario(myModel,selected,me);
							window.frmDefineScenario.setVisible(true);
						}
					}
					else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
						int selected=tableSchedule.getSelectedRow();
						if(selected!=-1){
							e.consume();
							scenarios.remove(selected);
							updateScenarios();
						}
					}
				}
			});
			tableSchedule.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2){
						int selected=tableSchedule.getSelectedRow();
						if(selected!=-1){
							frmDefineScenario window=new frmDefineScenario(myModel,selected,me);
							window.frmDefineScenario.setVisible(true);
						}
					}
				}
			});
			scrollPaneSchedule.setViewportView(tableSchedule);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 1;
			frmScenarios.getContentPane().add(scrollPane, gbc_scrollPane);

			listModelSchedule= new DefaultListModel<String>();
			listSchedule = new JList<String>();
			listSchedule.setModel(listModelSchedule);
			scrollPane.setViewportView(listSchedule);

			

			final JButton btnExport = new JButton("Export");
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						
						fc.setDialogTitle("Select Export Folder");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmScenarios);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath()+File.separator;
							//Open file for writing
							FileWriter fstreamS = new FileWriter(path+"ScenarioSummary.csv"); //Create new file
							BufferedWriter outS = new BufferedWriter(fstreamS);

							//Summary
							int numRows=tableResults.getRowCount();
							int numCols=tableResults.getColumnCount();
							//headers
							outS.write("Scenarios");
							for(int c=1; c<numCols; c++){outS.write(","+tableResults.getColumnName(c));}
							outS.newLine();
							//data
							for(int r=0; r<numRows; r++){
								outS.write(tableResults.getValueAt(r, 0)+"");
								for(int c=1; c<numCols; c++){outS.write(","+tableResults.getValueAt(r,c));}
								outS.newLine();
							}
							outS.close();
							
							if(myModel.simType==1 && myModel.displayIndResults){
								//Open file for writing
								FileWriter fstreamS2 = new FileWriter(path+"ScenarioSummary_Ind.csv"); //Create new file
								BufferedWriter outS2 = new BufferedWriter(fstreamS2);

								//Summary
								int numRows2=tableIndResults.getRowCount();
								int numCols2=tableIndResults.getColumnCount();
								//headers
								outS2.write("Scenario");
								for(int c=1; c<numCols2; c++){outS2.write(","+tableIndResults.getColumnName(c));}
								outS2.newLine();
								//data
								for(int r=0; r<numRows2; r++){
									outS2.write(tableIndResults.getValueAt(r, 0)+"");
									for(int c=1; c<numCols2; c++){outS2.write(","+tableIndResults.getValueAt(r,c));}
									outS2.newLine();
								}
								outS2.close();
							}
							
							//Run report summaries
							for(int n=0; n<numScenarios; n++){
								runReportSummaries[n].write(path+scenarioNames[n]);
							}
							
							//Each iteration if selected
							if(chckbxExportIterations.isSelected()){
								for(int r=0; r<numRows; r++){
									String scenName=(String) tableResults.getValueAt(r, 0);
									FileWriter fstream = new FileWriter(path+scenName+"_Iterations.csv"); //Create new file
									BufferedWriter out = new BufferedWriter(fstream);
									
									//Headers
									out.write("Iteration");
									
									DimInfo info=myModel.dimInfo;
									int numDim=info.dimNames.length;
									int analysisType=info.analysisType;
									for(int d=0; d<numDim; d++){ //EVs
										out.write(","+info.dimNames[d]);
										for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
									}
									if(analysisType>0){ //CEA or BCA
										if(analysisType==1){out.write(",ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");}
										else if(analysisType==2){out.write(",NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");}
										for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
									}
									out.newLine();

									//Results
									int group=comboGroup.getSelectedIndex();
									Scenario curScenario=scenarios.get(scenarioIndices[r]);
									int numIterations=curScenario.numIterations;
									for(int i=0; i<numIterations; i++){
										out.write((i+1)+""); //Iteration
										for(int d=0; d<numDim; d++){ //EVs
											out.write(",");
											for(int s=0; s<numStrat; s++){out.write(","+results[r][d][s][i]);}
										}
										if(analysisType>0){
											out.write(",");
											if(analysisType==1){ //CEA
												for(int s=0; s<numStrat; s++){
													double icer=results[r][group][numDim][s][i];
													if(!Double.isNaN(icer)){out.write(","+icer);} //valid ICER
													else{out.write(","+CEAnotes[r][group][s][i]);} //invalid ICER
												}
											}
											else if(analysisType==2){ //BCA
												for(int s=0; s<numStrat; s++){out.write(","+results[r][group][numDim][s][i]);}
											}
										}
										out.newLine();
									}
									out.close();
									
									//Run reports
									for(int i=0; i<numIterations; i++){
										runReports[r][i].write(path+scenarioNames[r]+"_"+i);
									}
								}
							}
							
							JOptionPane.showMessageDialog(frmScenarios, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmScenarios, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(712, 0, 90, 28);
			panel_2.add(btnExport);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myModel.saveSnapshot("Edit Scenarios"); //Add to undo stack
					myModel.scenarios=scenarios;
					frmScenarios.dispose();
				}
			});
			btnSave.setBounds(116, 0, 83, 28);
			panel_2.add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmScenarios.dispose();
				}
			});
			btnCancel.setBounds(226, 0, 90, 28);
			panel_2.add(btnCancel);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(543, 0, 72, 28);
			panel_2.add(btnRun);
			
			chckbxExportIterations = new JCheckBox("Export iterations");
			chckbxExportIterations.setBounds(806, 5, 117, 18);
			panel_2.add(chckbxExportIterations);
			
			
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnExport.setEnabled(false);
					final ProgressMonitor progress=new ProgressMonitor(frmScenarios, "Running Scenarios", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								scenarioIndices=listSchedule.getSelectedIndices();
								numScenarios=scenarioIndices.length;
								scenarioNames=new String[numScenarios];
								
								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmScenarios, "Errors in base case model!");
								}
								else if(numScenarios==0){
									JOptionPane.showMessageDialog(frmScenarios, "Please select at least one scenario to run!");
								}
								else{
									boolean cancelled=false;
									
									//Run scenarios
									numStrat=myModel.getStrategies();

									DimInfo info=myModel.dimInfo;
									int analysisType=myModel.dimInfo.analysisType;
									int numDim=myModel.dimInfo.dimNames.length;
									int numOutcomes=numDim;
									if(analysisType>0){numOutcomes++;}
									
									progress.setMaximum(numScenarios+1);

									//Get orig settings
									origCohortSize=myModel.cohortSize;
									origCRN=myModel.CRN;
									origSeed=myModel.crnSeed;
									numParams=myModel.parameters.size();
									origParams=new String[numParams];
									for(int i=0; i<numParams; i++){
										origParams[i]=myModel.parameters.get(i).expression;
									}
									numVars=myModel.variables.size();
									origVars=new String[numVars];
									for(int i=0; i<numVars; i++){
										origVars[i]=myModel.variables.get(i).expression;
									}
									
									//Get total number of iterations
									int totalIterations=0, maxIterations=0;
									for(int s=0; s<numScenarios; s++){
										int curIterations=scenarios.get(scenarioIndices[s]).numIterations;
										maxIterations=Math.max(maxIterations, curIterations);
										totalIterations+=maxIterations;
									}
									progress.setMaximum(totalIterations);
									
									for(int g=0; g<numSubgroups+1; g++){
										modelResults[g].setRowCount(0);
										modelResults[g].setColumnCount(0);
										modelResults[g].addColumn("Scenario");
										if(maxIterations==1){ //no uncertainty
											for(int d=0; d<numDim; d++){
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn(info.dimNames[d]+" "+myModel.strategyNames[s]);
												}
											}
											if(analysisType==1){ //cea
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+") "+myModel.strategyNames[s]);
												}
											}
											else if(analysisType==2){ //bca
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+") "+myModel.strategyNames[s]);
												}
											}
										}
										else{ //mean and bounds
											for(int d=0; d<numDim; d++){
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn(info.dimNames[d]+" "+myModel.strategyNames[s]+" (Mean)");
													modelResults[g].addColumn(info.dimNames[d]+" "+myModel.strategyNames[s]+" (95% LB)");
													modelResults[g].addColumn(info.dimNames[d]+" "+myModel.strategyNames[s]+" (95% UB)");
												}
											}
											if(analysisType==1){ //cea
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+") "+myModel.strategyNames[s]+ "(Mean)");
													modelResults[g].addColumn("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+") "+myModel.strategyNames[s]+ "(95% LB)");
													modelResults[g].addColumn("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+") "+myModel.strategyNames[s]+ "(95% UB)");
												}
											}
											else if(analysisType==2){ //bca
												for(int s=0; s<numStrat; s++){
													modelResults[g].addColumn("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+") "+myModel.strategyNames[s]+ "(Mean)");
													modelResults[g].addColumn("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+") "+myModel.strategyNames[s]+ "(95% LB)");
													modelResults[g].addColumn("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+") "+myModel.strategyNames[s]+ "(95% UB)");
												}
											}
										}
									}
									
									if(myModel.simType==1 && myModel.displayIndResults==true){
										tabbedPane.setEnabledAt(1, true);
										for(int g=0; g<numSubgroups+1; g++){
											modelIndResults[g].setRowCount(0);
											modelIndResults[g].setColumnCount(0);
											modelIndResults[g].addColumn("Scenario");
											modelIndResults[g].addColumn("Strategy");
											modelIndResults[g].addColumn("Outcome");
											if(maxIterations==1){ //no uncertainty
												modelIndResults[g].addColumn("Mean");
												modelIndResults[g].addColumn("SD");
												modelIndResults[g].addColumn("Min");
												modelIndResults[g].addColumn("Q1");
												modelIndResults[g].addColumn("Median");
												modelIndResults[g].addColumn("Q3");
												modelIndResults[g].addColumn("Max");
											}
											else{ //mean and bounds
												modelIndResults[g].addColumn("Mean: Mean (95% UI)");
												modelIndResults[g].addColumn("SD: Mean (95% UI)");
												modelIndResults[g].addColumn("Min: Mean (95% UI)");
												modelIndResults[g].addColumn("Q1: Mean (95% UI)");
												modelIndResults[g].addColumn("Median: Mean (95% UI)");
												modelIndResults[g].addColumn("Q3: Mean (95% UI)");
												modelIndResults[g].addColumn("Max: Mean (95% UI)");
											}
										}
									}
																		
									results=new double[numScenarios][numSubgroups+1][numOutcomes][numStrat][maxIterations];
									if(analysisType==1){CEAnotes=new String[numScenarios][numSubgroups+1][numStrat][maxIterations];} //CEA
									else{CEAnotes=null;}
									
									meanResults=new double[numScenarios][numSubgroups+1][numOutcomes][numStrat];
									lbResults=new double[numScenarios][numSubgroups+1][numOutcomes][numStrat];
									ubResults=new double[numScenarios][numSubgroups+1][numOutcomes][numStrat];
									
									runReports=new RunReport[numScenarios][];
									runReportSummaries=new RunReportSummary[numScenarios];
									
									long startTime=System.currentTimeMillis();

									int curProg=0;
									for(int n=0; n<numScenarios; n++){
										//Reset orig object expressions
										resetModel();
										myModel.validateModelObjects();

										//Apply updates
										Scenario curScenario=scenarios.get(scenarioIndices[n]);
										scenarioNames[n]=curScenario.name;
										curScenario.parseUpdates(myModel);
										curScenario.applyUpdates(myModel);

										//Check for errors
										ArrayList<String> errors=myModel.parseModel();
										if(errors.size()>0){
											JOptionPane.showMessageDialog(frmScenarios, "Errors found in run: "+curScenario.name+"!");
										}

										//Run model
										curResults=new double[numSubgroups+1][numOutcomes][numStrat][curScenario.numIterations];
										runReports[n]=new RunReport[curScenario.numIterations];
										
										myModel.cohortSize=curScenario.cohortSize;
										myModel.CRN=curScenario.crn1;
										myModel.crnSeed=curScenario.seed1;
										
										Numeric origValues[] = null;
										if(curScenario.sampleParams){
											myModel.sampleParam=true;
											myModel.generatorParam=new MersenneTwisterFast();
											if(curScenario.crn2){
												myModel.generatorParam.setSeed(curScenario.seed2);
											}
											//Parse constraints
											numConstraints=myModel.constraints.size();
											for(int c=0; c<numConstraints; c++){
												myModel.constraints.get(c).parseConstraints();
											}
											//Get orig values for all parameters
											origValues=new Numeric[numParams];
											for(int v=0; v<numParams; v++){
												origValues[v]=myModel.parameters.get(v).value.copy();
											}
										}
										
										
										for(int i=0; i<curScenario.numIterations; i++){
											//Update progress
											curProg++;
											double prog=(curProg/(totalIterations*1.0))*100;
											long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
											remTime=(long) (remTime*(100-prog));
											remTime=remTime/1000;
											String seconds = Integer.toString((int)(remTime % 60));
											String minutes = Integer.toString((int)(remTime/60));
											if(seconds.length()<2){seconds="0"+seconds;}
											if(minutes.length()<2){minutes="0"+minutes;}
											progress.setProgress(curProg);
											progress.setNote("Time left: "+minutes+":"+seconds);

											if(curScenario.sampleParams){
												myModel.curGenerator[0]=myModel.generatorParam;
												boolean validParams=false;
												while(validParams==false){
													for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters and orig values
														Parameter curParam=myModel.parameters.get(v);
														curParam.locked=false;
														curParam.value=origValues[v];
													}
													for(int v=0; v<numParams; v++){ //sample all parameters
														Parameter curParam=myModel.parameters.get(v);
														curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true);
													}
													//check constraints
													validParams=true;
													int c=0;
													while(validParams==true && c<numConstraints){
														Constraint curConst=myModel.constraints.get(c);
														validParams=curConst.checkConstraints(myModel);
														c++;
													}
													if(validParams){ //check model for valid params
														if(myModel.parseModel().size()!=0){validParams=false;}
													}
												}
											}
											
											
											RunReport curReport=myModel.runModel(null, false);
											runReports[n][i]=curReport;

											//Get EVs
											for(int d=0; d<numDim; d++){
												for(int s=0; s<numStrat; s++){
													//overall
													results[n][0][d][s][i]=curReport.outcomeEVs[d][s];
													curResults[0][d][s][i]=results[n][0][d][s][i];
													meanResults[n][0][d][s]+=results[n][0][d][s][i];
													//subgroups
													for(int g=0; g<numSubgroups; g++){
														results[n][g+1][d][s][i]=curReport.outcomeEVsGroup[g][d][s];
														curResults[g+1][d][s][i]=results[n][g+1][d][s][i];
														meanResults[n][g+1][d][s]+=results[n][g+1][d][s][i];
													}
												}
											}
											if(analysisType>0){ //CEA or BCA
												if(analysisType==1){ //CEA
													//overall
													for(int s=0; s<curReport.table.length; s++){	
														int origStrat=(int) curReport.table[s][0];
														if(origStrat!=-1){
															double val=Double.NaN;
															try{
																val=Double.parseDouble((String) curReport.table[s][4]);
															} catch(Exception err){
																//do nothing
															}
															results[n][0][numDim][origStrat][i]=val;
															curResults[0][numDim][origStrat][i]=val;
															CEAnotes[n][0][origStrat][i]=(String) curReport.table[s][5];
														}
													}
													//subgroups
													for(int g=0; g<numSubgroups; g++){
														for(int s=0; s<curReport.tableGroup[g].length; s++){	
															int origStrat=(int) curReport.tableGroup[g][s][0];
															if(origStrat!=-1){
																double val=Double.NaN;
																try{
																	val=Double.parseDouble((String) curReport.tableGroup[g][s][4]);
																} catch(Exception err){
																	//do nothing
																}
																results[n][g+1][numDim][origStrat][i]=val;
																curResults[g+1][numDim][origStrat][i]=val;
																CEAnotes[n][g+1][origStrat][i]=(String) curReport.tableGroup[g][s][5];
															}
														}
													}
													
												}
												else if(analysisType==2){
													//overall
													for(int s=0; s<curReport.table.length; s++){	
														int origStrat=(int) curReport.table[s][0];
														results[n][0][numDim][origStrat][i]=(double) curReport.table[s][4];
														curResults[0][numDim][origStrat][i]=(double) curReport.table[s][4];
														meanResults[n][0][numDim][origStrat]+=(double) curReport.table[s][4];
													}
													//subgroups
													for(int g=0; g<numSubgroups; g++){
														for(int s=0; s<curReport.tableGroup[g].length; s++){	
															int origStrat=(int) curReport.tableGroup[g][s][0];
															results[n][g+1][numDim][origStrat][i]=(double) curReport.tableGroup[g][s][4];
															curResults[g+1][numDim][origStrat][i]=(double) curReport.tableGroup[g][s][4];
															meanResults[n][g+1][numDim][origStrat]+=(double) curReport.tableGroup[g][s][4];
														}
													}
												}
											}

											if(progress.isCanceled()){ //End loop
												cancelled=true;
												i=curScenario.numIterations;
												n=numScenarios;
											}
										} //end iterations loop
										
										//Calculate results summary
										if(cancelled==false){
											int bounds[]=MathUtils.getBoundIndices(curScenario.numIterations);
											int indexLB=bounds[0], indexUB=bounds[1];
											for(int d=0; d<numOutcomes; d++){
												for(int s=0; s<numStrat; s++){
													for(int g=0; g<numSubgroups+1; g++){ //overall + subgroups
														meanResults[n][g][d][s]/=(curScenario.numIterations*1.0);
														Arrays.sort(curResults[g][d][s]);//bounds
														lbResults[n][g][d][s]=curResults[g][d][s][indexLB];
														ubResults[n][g][d][s]=curResults[g][d][s][indexUB];
													}
												}
											}

											//Append results to table
											for(int g=0; g<numSubgroups+1; g++){
												modelResults[g].addRow(new Object[]{null});
												modelResults[g].setValueAt(curScenario.name, n, 0);
												int curCol=1;
												if(maxIterations==1){ //no uncertainty
													for(int d=0; d<numDim; d++){
														for(int s=0; s<numStrat; s++){
															modelResults[g].setValueAt(MathUtils.round(meanResults[n][g][d][s],info.decimals[d]), n, curCol); curCol++;
														}
													}
													if(analysisType==1 || analysisType==2){ //cea or bca
														for(int s=0; s<numStrat; s++){
															modelResults[g].setValueAt(MathUtils.round(meanResults[n][g][numDim][s],info.decimals[info.costDim]), n, curCol); curCol++;
														}
													}
												}
												else{ //mean and bounds
													for(int d=0; d<numDim; d++){
														for(int s=0; s<numStrat; s++){
															modelResults[g].setValueAt(MathUtils.round(meanResults[n][g][d][s],info.decimals[d]), n, curCol); curCol++;
															modelResults[g].setValueAt(MathUtils.round(lbResults[n][g][d][s],info.decimals[d]), n, curCol); curCol++;
															modelResults[g].setValueAt(MathUtils.round(ubResults[n][g][d][s],info.decimals[d]), n, curCol); curCol++;
														}
													}
													if(analysisType==1 || analysisType==2){ //cea or bca
														for(int s=0; s<numStrat; s++){
															modelResults[g].setValueAt(MathUtils.round(meanResults[n][g][numDim][s],info.decimals[info.costDim]), n, curCol); curCol++;
															modelResults[g].setValueAt(MathUtils.round(lbResults[n][g][numDim][s],info.decimals[info.costDim]), n, curCol); curCol++;
															modelResults[g].setValueAt(MathUtils.round(ubResults[n][g][numDim][s],info.decimals[info.costDim]), n, curCol); curCol++;
														}
													}
												}
											}
										}
										
										//Get reports summary
										runReportSummaries[n]=new RunReportSummary(runReports[n]);
											
										//Get ind results summary
										if(cancelled==false && myModel.simType==1 && myModel.displayIndResults==true){
											for(int s=0; s<numStrat; s++){
												String stratName=myModel.strategyNames[s];
												//overall
												MicroStatsSummary curSummary=runReportSummaries[n].microStatsSummary[s];
												//outcomes
												int maxDec=0;
												for(int d=0; d<numDim; d++){
													int dec=myModel.dimInfo.decimals[d];
													maxDec=Math.max(dec, maxDec);
													modelIndResults[0].addRow(new Object[]{null});
													int r=modelIndResults[0].getRowCount()-1;
													modelIndResults[0].setValueAt(curScenario.name, r, 0);
													modelIndResults[0].setValueAt(stratName, r, 1);
													modelIndResults[0].setValueAt(myModel.dimInfo.dimNames[d], r, 2);
													for(int j=0; j<7; j++){ //estimate
														String cell=curSummary.getCell(curSummary.outcomesSummary[d][j], dec);
														modelIndResults[0].setValueAt(cell, r, 3+j);
													}
												}
												//variables
												for(int v=0; v<numVars; v++){
													modelIndResults[0].addRow(new Object[]{null});
													int r=modelIndResults[0].getRowCount()-1;
													modelIndResults[0].setValueAt(curScenario.name, r, 0);
													modelIndResults[0].setValueAt(stratName, r, 1);
													modelIndResults[0].setValueAt(myModel.variables.get(v).name, r, 2);
													for(int j=0; j<7; j++){ //estimate
														String cell=curSummary.getCell(curSummary.varsSummary[v][j], maxDec);
														modelIndResults[0].setValueAt(cell, r, 3+j);
													}
												}
												
												//subgroups
												for(int g=0; g<numSubgroups; g++){
													curSummary=runReportSummaries[n].microStatsSummaryGroup[g][s];
													//outcomes
													for(int d=0; d<numDim; d++){
														int dec=myModel.dimInfo.decimals[d];
														modelIndResults[g+1].addRow(new Object[]{null});
														int r=modelIndResults[g+1].getRowCount()-1;
														modelIndResults[g+1].setValueAt(curScenario.name, r, 0);
														modelIndResults[g+1].setValueAt(stratName, r, 1);
														modelIndResults[g+1].setValueAt(myModel.dimInfo.dimNames[d], r, 2);
														for(int j=0; j<7; j++){ //estimate
															String cell=curSummary.getCell(curSummary.outcomesSummary[d][j], dec);
															modelIndResults[g+1].setValueAt(cell, r, 3+j);
														}
													}
													//variables
													for(int v=0; v<numVars; v++){
														modelIndResults[g+1].addRow(new Object[]{null});
														int r=modelIndResults[g+1].getRowCount()-1;
														modelIndResults[g+1].setValueAt(curScenario.name, r, 0);
														modelIndResults[g+1].setValueAt(stratName, r, 1);
														modelIndResults[g+1].setValueAt(myModel.variables.get(v).name, r, 2);
														for(int j=0; j<7; j++){ //estimate
															String cell=curSummary.getCell(curSummary.varsSummary[v][j], maxDec);
															modelIndResults[g+1].setValueAt(cell, r, 3+j);
														}
													}
												}
											}
										}
										
									} //end scenarios loop

									resetModel();
									myModel.validateModelObjects();
									progress.close();
									if(cancelled==false){
										btnExport.setEnabled(true);
									}
									
								}
							} catch (Exception e) {
								//Reset orig model expressions
								resetModel();
								myModel.validateModelObjects();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmScenarios, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});



		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void resetModel(){
		myModel.cohortSize=origCohortSize;
		myModel.CRN=origCRN;
		myModel.crnSeed=origSeed;
		for(int i=0; i<numParams; i++){
			myModel.parameters.get(i).expression=origParams[i];
		}
		for(int i=0; i<numVars; i++){
			myModel.variables.get(i).expression=origVars[i];
		}
	}

	public void updateScenarios(){
		int numScenarios=scenarios.size();
		modelSchedule.setRowCount(0);
		listModelSchedule.clear();
		for(int r=0; r<numScenarios; r++){
			Scenario curScenario=scenarios.get(r);
			modelSchedule.addRow(new Object[]{(r+1),curScenario.name,curScenario.numIterations,curScenario.objectUpdates,curScenario.sampleParams});
			listModelSchedule.addElement(curScenario.name);
		}
	}
}
