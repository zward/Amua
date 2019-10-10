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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Toolkit;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import base.AmuaModel;
import filters.CSVFilter;
import main.CEAHelper;
import main.DimInfo;
import main.Parameter;
import math.MathUtils;
import math.Numeric;

import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmSensOneWayStacked {

	public JFrame frmSensOneWayStacked;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	ChartPanel panelChart;
	JFreeChart chart;
	
	JComboBox<String> comboChartType;
	JComboBox<String> comboGroup;
	JComboBox<String> comboParamVals;
	int numStrat;
	int numParams;
	int intervals;
	
	/**
	 * [Outcome][Strategy][Parameter][Iteration]
	 */
	double results[][][][];
	int numSubgroups=0;
	double resultsGroup[][][][][];
	private JTextField textIntervals;
	String CEAnotes[][], CEAnotesGroup[][][];

	/**
	 * [Outcome][Strategy]
	 */
	double resultsBase[][];
	/**
	 * [Group][Outcome][Strategy]
	 */
	double resultsBaseGroup[][][];
	
	
	String paramNames[];
	/**
	 * [Parameter][Iteration]
	 */
	int bestStrategy[][];
	/**
	 * [Group][Parameter][Iteration]
	 */
	int bestStrategyGroup[][][];
	
	double bestResultBase;
	double bestResultsBaseGroup[];
	
	/**
	 * [Parameter][Min/Max]
	 */
	double paramVals[][];
	double paramValsBase[];

	Parameter curParam;
	private JLabel lblParameterLabels;

	public frmSensOneWayStacked(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmSensOneWayStacked = new JFrame();
			frmSensOneWayStacked.setTitle("Amua - Stacked One-way Sensitivity Analyses");
			frmSensOneWayStacked.setIconImage(Toolkit.getDefaultToolkit().getImage(frmSensOneWayStacked.class.getResource("/images/oneWayStacked_128.png")));
			frmSensOneWayStacked.setBounds(100, 100, 1020, 499);
			frmSensOneWayStacked.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 180, 180, 0, 0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmSensOneWayStacked.getContentPane().setLayout(gridBagLayout);

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


			comboChartType = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] {"Parameter Values","Outcomes"}));
			comboChartType.setEnabled(false);
			comboChartType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			GridBagConstraints gbc_comboDimensions = new GridBagConstraints();
			gbc_comboDimensions.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboDimensions.insets = new Insets(0, 0, 5, 5);
			gbc_comboDimensions.gridx = 1;
			gbc_comboDimensions.gridy = 0;
			frmSensOneWayStacked.getContentPane().add(comboChartType, gbc_comboDimensions);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.gridheight = 2;
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmSensOneWayStacked.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{466, 38, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelParams=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Parameter", "Expression","Min","Max"}) {
				boolean[] columnEditables = new boolean[] {false, false,true,true};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			for(int i=0; i<myModel.parameters.size(); i++){
				modelParams.addRow(new Object[]{null});
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(curParam.name, i, 0);
				modelParams.setValueAt(curParam.expression, i, 1);
				modelParams.setValueAt(curParam.sensMin, i, 2);
				modelParams.setValueAt(curParam.sensMax, i, 3);
			}

			JScrollPane scrollPaneParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneParams = new GridBagConstraints();
			gbc_scrollPaneParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneParams.gridx = 0;
			gbc_scrollPaneParams.gridy = 0;
			panel_1.add(scrollPaneParams, gbc_scrollPaneParams);
			tableParams = new JTable();
			tableParams.setRowSelectionAllowed(false);
			tableParams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableParams.setShowVerticalLines(true);
			tableParams.getTableHeader().setReorderingAllowed(false);
			tableParams.setModel(modelParams);
			scrollPaneParams.setViewportView(tableParams);

			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_2.setLayout(null);
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 1;
			panel_1.add(panel_2, gbc_panel_2);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(184, 5, 90, 28);
			panel_2.add(btnRun);

			final JButton btnExport = new JButton("Export");
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter());

						int group=0; //overall
						if(numSubgroups>0){
							group=comboGroup.getSelectedIndex();
						}
						if(group==0) {
							fc.setDialogTitle("Export Data");
						}
						else {
							fc.setDialogTitle("Export Subgroup Data");
						}
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmSensOneWayStacked);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);

							//Headers
							DimInfo info=myModel.dimInfo;
							int numDim=info.dimNames.length;
							int numOutcomes=numDim;
							int analysisType=info.analysisType;
							if(analysisType>0) {numOutcomes++;}
							out.write("Parameter,Value");
							for(int s=0; s<numStrat; s++) {
								for(int d=0; d<numDim; d++) { //EVs
									out.write(","+myModel.strategyNames[s]+" "+info.dimNames[d]);
								}
								if(analysisType==1) {
									out.write(","+myModel.strategyNames[s]+" ICER");
								}
								else if(analysisType==2) {
									out.write(","+myModel.strategyNames[s]+" NMB");
								}
							}
							out.newLine();
							
							//Results
							if(group==0){ //overall
								for(int p=0; p<numParams; p++) {
									double min=paramVals[p][0];
									double max=paramVals[p][1];
									double step=(max-min)/(intervals*1.0);
									for(int i=0; i<=intervals; i++) {
										double curVal=min+(i*step);
										out.write(paramNames[p]+","+curVal);
										for(int s=0; s<numStrat; s++) {
											for(int d=0; d<numOutcomes; d++) {
												out.write(","+results[d][s][p][i]);
											}
										}
										out.newLine();
									}
								}
							}
							else { //subgroup
								int g=group-1;
								for(int p=0; p<numParams; p++) {
									double min=paramVals[p][0];
									double max=paramVals[p][1];
									double step=(max-min)/(intervals*1.0);
									for(int i=0; i<=intervals; i++) {
										double curVal=min+(i*step);
										out.write(paramNames[p]+","+curVal);
										for(int s=0; s<numStrat; s++) {
											for(int d=0; d<numOutcomes; d++) {
												out.write(","+resultsGroup[g][d][s][p][i]);
											}
										}
										out.newLine();
									}
								}
							}
								
							out.close();

							JOptionPane.showMessageDialog(frmSensOneWayStacked, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmSensOneWayStacked, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(359, 5, 90, 28);
			panel_2.add(btnExport);

			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(6, 11, 55, 16);
			panel_2.add(lblIntervals);

			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("10");
			textIntervals.setBounds(62, 5, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);


			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmSensOneWayStacked, "Stacked one-way sensitivity", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmSensOneWayStacked, "Errors in base case model!");
								}
								else{
									boolean origShowTrace=false;
									if(myModel.type==1) {
										origShowTrace=myModel.markov.showTrace;
										myModel.markov.showTrace=false;
									}
									
									//Get baseline results
									myModel.runModel(null, false);
									int numDim=myModel.dimInfo.dimNames.length;
									numStrat=myModel.getStrategies();
									resultsBase=new double[numDim+1][numStrat];
									resultsBaseGroup=new double[numSubgroups][numDim+1][numStrat];
									for(int s=0; s<numStrat; s++) {
										for(int d=0; d<numDim; d++) {
											resultsBase[d][s]=myModel.getStrategyEV(s, d);
										}
									}
									for(int g=0; g<numSubgroups; g++) {
										for(int s=0; s<numStrat; s++) {
											for(int d=0; d<numDim; d++) {
												resultsBaseGroup[g][d][s]=myModel.getSubgroupEV(g, s, d);
											}
										}
									}
									if(myModel.dimInfo.analysisType==1){ //CEA
										//overall
										Object table[][]=new CEAHelper().calculateICERs(myModel,-1,true);
										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											if(origStrat!=-1){
												resultsBase[numDim][origStrat]=(double) table[s][4];
												//CEAnotes[origStrat][i]=(String) table[s][5];
											}
										}
										//subgroups
										for(int g=0; g<numSubgroups; g++){
											table=new CEAHelper().calculateICERs(myModel, g, true);
											for(int s=0; s<table.length; s++){
												int origStrat=(int) table[s][0];
												if(origStrat!=-1){
													resultsBaseGroup[g][numDim][origStrat]=(double) table[s][4];
													//CEAnotesGroup[g][origStrat][i]=(String) table[s][5];
												}
											}
										}
									}
									else if(myModel.dimInfo.analysisType==2){
										Object table[][]=new CEAHelper().calculateNMB(myModel,-1,true);
										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											resultsBase[numDim][origStrat]=(double) table[s][4];
										}
										//subgroups
										for(int g=0; g<numSubgroups; g++){
											table=new CEAHelper().calculateNMB(myModel, g, true);
											for(int s=0; s<table.length; s++){
												int origStrat=(int) table[s][0];
												resultsBaseGroup[g][numDim][origStrat]=(double) table[s][4];
											}
										}
									}
									
									
									
									//Get parameters
									boolean proceed=true;

									ArrayList<Integer> paramIndices=new ArrayList<Integer>();
									for(int p=0; p<tableParams.getRowCount(); p++){
										String paramName=(String)tableParams.getValueAt(p, 0);
										String strMin=(String)tableParams.getValueAt(p, 2);
										String strMax=(String)tableParams.getValueAt(p, 3);
										if(strMin!=null && strMin.length()>0 && strMax!=null && strMax.length()>0){
											strMin=strMin.replaceAll(",",""); //Replace any commas
											strMax=strMax.replaceAll(",",""); //Replace any commas
											paramIndices.add(p);
											try {
												double min=Double.parseDouble(strMin);
												double max=Double.parseDouble(strMax);
											} catch(Exception err) {
												proceed=false;
												JOptionPane.showMessageDialog(frmSensOneWayStacked,"Invalid entry: "+paramName);
												p=tableParams.getRowCount();
											}
										}
									}

									if(proceed==true) {

										intervals=Integer.parseInt(textIntervals.getText());
										numParams=paramIndices.size();
										
										results=new double[numDim+1][numStrat][numParams][intervals+1];
										resultsGroup=new double[numSubgroups][numDim+1][numStrat][numParams][intervals+1];
										int analysisType=myModel.dimInfo.analysisType;
										if(analysisType==1){ //CEA
											CEAnotes=new String[numStrat][intervals+1];
											CEAnotesGroup=new String[numSubgroups][numStrat][intervals+1];
										}
										else{
											CEAnotes=null; CEAnotesGroup=null;
										}
										
										boolean cancelled=false;
										long startTime=System.currentTimeMillis();
										double maxProg=numParams*(intervals+1);
										progress.setMaximum((int)maxProg);
										
										paramNames=new String[numParams];
										paramVals=new double[numParams][2];
										paramValsBase=new double[numParams];

										for(int p=0; p<numParams; p++) {
											int pIndex=paramIndices.get(p);
											String strMin=(String)tableParams.getValueAt(pIndex, 2);
											String strMax=(String)tableParams.getValueAt(pIndex, 3);
											strMin=strMin.replaceAll(",",""); //Replace any commas
											strMax=strMax.replaceAll(",",""); //Replace any commas
											double min=Double.parseDouble(strMin);
											double max=Double.parseDouble(strMax);
											paramVals[p][0]=min; paramVals[p][1]=max;

											double step=(max-min)/(intervals*1.0);
											curParam=myModel.parameters.get(pIndex);
											paramNames[p]=curParam.name;
											curParam.locked=true;
											Numeric origValue=curParam.value.copy();
											paramValsBase[p]=origValue.getValue();

											boolean error=false;
											//Test parameter at min and max...
											curParam.value.setDouble(min);
											ArrayList<String> errorsMin=myModel.parseModel();
											curParam.value.setDouble(max);
											ArrayList<String> errorsMax=myModel.parseModel();
											curParam.value=origValue; //Reset
											if(errorsMin.size()>0){
												error=true;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmSensOneWayStacked, "Error: Min value");
											}
											if(errorsMax.size()>0){
												error=true;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmSensOneWayStacked, "Error: Max value");
											}

											if(error==false){
												//record min/max
												curParam.sensMin=strMin;
												curParam.sensMax=strMax;

												for(int i=0; i<=intervals; i++){
													double curVal=min+(step*i);
													curParam.value.setDouble(curVal);
													curParam.locked=true;
													myModel.parseModel();
													myModel.runModel(null, false);

													//Get EVs
													for(int d=0; d<numDim; d++){
														for(int s=0; s<numStrat; s++){
															results[d][s][p][i]=myModel.getStrategyEV(s, d);
															//subgroups
															for(int g=0; g<numSubgroups; g++){
																resultsGroup[g][d][s][p][i]=myModel.getSubgroupEV(g,s,d);
															}
														}
													}
													if(myModel.dimInfo.analysisType==1){ //CEA
														//overall
														Object table[][]=new CEAHelper().calculateICERs(myModel,-1,true);
														for(int s=0; s<table.length; s++){	
															int origStrat=(int) table[s][0];
															if(origStrat!=-1){
																results[numDim][origStrat][p][i]=(double) table[s][4];
																//CEAnotes[origStrat][i]=(String) table[s][5];
															}
														}
														//subgroups
														for(int g=0; g<numSubgroups; g++){
															table=new CEAHelper().calculateICERs(myModel, g, true);
															for(int s=0; s<table.length; s++){
																int origStrat=(int) table[s][0];
																if(origStrat!=-1){
																	resultsGroup[g][numDim][origStrat][p][i]=(double) table[s][4];
																	//CEAnotesGroup[g][origStrat][i]=(String) table[s][5];
																}
															}
														}
													}
													else if(myModel.dimInfo.analysisType==2){
														Object table[][]=new CEAHelper().calculateNMB(myModel,-1,true);
														for(int s=0; s<table.length; s++){	
															int origStrat=(int) table[s][0];
															results[numDim][origStrat][p][i]=(double) table[s][4];
														}
														//subgroups
														for(int g=0; g<numSubgroups; g++){
															table=new CEAHelper().calculateNMB(myModel, g, true);
															for(int s=0; s<table.length; s++){
																int origStrat=(int) table[s][0];
																resultsGroup[g][numDim][origStrat][p][i]=(double) table[s][4];
															}
														}
													}
													
													
													
													//Update progress
													double prog=((p*intervals+i)/maxProg)*100;
													long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
													remTime=(long) (remTime*(100-prog));
													remTime=remTime/1000;
													String seconds = Integer.toString((int)(remTime % 60));
													String minutes = Integer.toString((int)(remTime/60));
													if(seconds.length()<2){seconds="0"+seconds;}
													if(minutes.length()<2){minutes="0"+minutes;}
													progress.setProgress(p*intervals+i);
													progress.setNote("Time left: "+minutes+":"+seconds);

													if(progress.isCanceled()){ //End loop
														cancelled=true;
														i=intervals+1;
														p=numParams;
													}
													
												} //end interval loop

												//Reset param value
												curParam.value=origValue;
												curParam.locked=false;


											} //end error check
										}  //end parameter loop

										myModel.validateModelObjects();

										if(myModel.type==1) {
											myModel.markov.showTrace=origShowTrace;
										}

										if(cancelled==false){
											getBestStrategies();
											
											updateChart();
											comboChartType.setEnabled(true);
											comboGroup.setEnabled(true);
											comboParamVals.setEnabled(true);
											btnExport.setEnabled(true);
										}
										progress.close();

									} //end if proceed==true
								}
							} catch (Exception e) {
								curParam.locked=false;
								myModel.validateModelObjects();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmSensOneWayStacked, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			
			comboGroup = new JComboBox<String>();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			comboGroup.setVisible(false);
			comboGroup.setEnabled(false);
			if(myModel.simType==1 && myModel.reportSubgroups){
				numSubgroups=myModel.subgroupNames.size();
				String groups[]=new String[numSubgroups+1];
				groups[0]="Overall";
				for(int g=0; g<numSubgroups; g++){
					groups[g+1]=myModel.subgroupNames.get(g);
				}
				comboGroup.setModel(new DefaultComboBoxModel<String>(groups));
				comboGroup.setVisible(true);
			}


			GridBagConstraints gbc_comboGroup = new GridBagConstraints();
			gbc_comboGroup.insets = new Insets(0, 0, 5, 5);
			gbc_comboGroup.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroup.gridx = 2;
			gbc_comboGroup.gridy = 0;
			frmSensOneWayStacked.getContentPane().add(comboGroup, gbc_comboGroup);
			
			lblParameterLabels = new JLabel("Parameter Labels:");
			GridBagConstraints gbc_lblParameterLabels = new GridBagConstraints();
			gbc_lblParameterLabels.insets = new Insets(0, 0, 5, 5);
			gbc_lblParameterLabels.anchor = GridBagConstraints.EAST;
			gbc_lblParameterLabels.gridx = 3;
			gbc_lblParameterLabels.gridy = 0;
			frmSensOneWayStacked.getContentPane().add(lblParameterLabels, gbc_lblParameterLabels);
			
			comboParamVals = new JComboBox();
			comboParamVals.setModel(new DefaultComboBoxModel(new String[] {"None", "Range", "All"}));
			comboParamVals.setSelectedIndex(1);
			comboParamVals.setEnabled(false);
			comboParamVals.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			GridBagConstraints gbc_comboParamVals = new GridBagConstraints();
			gbc_comboParamVals.insets = new Insets(0, 0, 5, 5);
			gbc_comboParamVals.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboParamVals.gridx = 4;
			gbc_comboParamVals.gridy = 0;
			frmSensOneWayStacked.getContentPane().add(comboParamVals, gbc_comboParamVals);

			panelChart = new ChartPanel(null,false);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.insets = new Insets(0, 0, 0, 5);
			gbc_panelChart.gridwidth = 5;
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 1;
			gbc_panelChart.gridy = 1;
			frmSensOneWayStacked.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void getBestStrategies() {
		//get best strategy
		bestStrategy=new int[numParams][intervals+1];
		bestStrategyGroup=new int[numSubgroups][numParams][intervals+1];
		
		bestResultsBaseGroup=new double[numSubgroups];
		
		DimInfo dimInfo= myModel.dimInfo;
		
		if(dimInfo.analysisType==0) { //EV
			int objective=dimInfo.objective;
			int objectiveDim=dimInfo.objectiveDim;
			
			//overall
			bestResultBase=resultsBase[objectiveDim][0];
			for(int s=1; s<numStrat; s++) {
				if(objective==0) { //maximize
					if(resultsBase[objectiveDim][s]>bestResultBase) {
						bestResultBase=resultsBase[objectiveDim][s];
					}
				}
				else if(objective==1) { //minimize
					if(resultsBase[objectiveDim][s]<bestResultBase) {
						bestResultBase=resultsBase[objectiveDim][s];
					}
				}
			}
			//subgroups
			for(int g=0; g<numSubgroups; g++) {
				bestResultsBaseGroup[g]=resultsBaseGroup[g][objectiveDim][0];
				for(int s=1; s<numStrat; s++) {
					if(objective==0) { //maximize
						if(resultsBaseGroup[g][objectiveDim][s]>bestResultsBaseGroup[g]) {
							bestResultsBaseGroup[g]=resultsBaseGroup[g][objectiveDim][s];
						}
					}
					else if(objective==1) { //minimize
						if(resultsBaseGroup[g][objectiveDim][s]<bestResultsBaseGroup[g]) {
							bestResultsBaseGroup[g]=resultsBaseGroup[g][objectiveDim][s];
						}
					}
				}
			}
			
			
			for(int p=0; p<numParams; p++) {
				for(int i=0; i<=intervals; i++) {
					double bestVal=results[objectiveDim][0][p][i]; //initialize
					bestStrategy[p][i]=0;
					for(int s=1; s<numStrat; s++) {
						double curVal=results[objectiveDim][s][p][i];
						if(objective==0) { //maximize
							if(curVal>bestVal) {
								bestVal=curVal;
								bestStrategy[p][i]=s;
							}
						}
						else { //minimize
							if(curVal<bestVal) {
								bestVal=curVal;
								bestStrategy[p][i]=s;
							}
						}
					}
				
					//subgroups
					for(int g=0; g<numSubgroups; g++) {
						bestVal=resultsGroup[g][objectiveDim][0][p][i]; //initialize
						bestStrategyGroup[g][p][i]=0;
						for(int s=1; s<numStrat; s++) {
							double curVal=resultsGroup[g][objectiveDim][s][p][i];
							if(objective==0) { //maximize
								if(curVal>bestVal) {
									bestVal=curVal;
									bestStrategyGroup[g][p][i]=s;
								}
							}
							else { //minimize
								if(curVal<bestVal) {
									bestVal=curVal;
									bestStrategyGroup[g][p][i]=s;
								}
							}
						}
					}
				}
			}
		}
		else { //CEA or BCA
			int costDim=dimInfo.costDim;
			int effectDim=dimInfo.effectDim;
			int numDim=dimInfo.dimNames.length;
			double WTP=dimInfo.WTP;
			
			//overall
			bestResultBase=resultsBase[numDim][0];
			double bestNMB=(resultsBase[effectDim][0]*WTP)-resultsBase[costDim][0]; 
			for(int s=1; s<numStrat; s++) {
				double curNMB=(resultsBase[effectDim][s]*WTP)-resultsBase[costDim][s]; 
				if(curNMB>bestNMB) {
					bestNMB=curNMB;
					bestResultBase=resultsBase[numDim][s];
				}
			}
			//subgroups
			for(int g=0; g<numSubgroups; g++) {
				bestResultsBaseGroup[g]=resultsBaseGroup[g][numDim][0];
				bestNMB=(resultsBaseGroup[g][effectDim][0]*WTP)-resultsBaseGroup[g][costDim][0]; 
				for(int s=1; s<numStrat; s++) {
					double curNMB=(resultsBaseGroup[g][effectDim][s]*WTP)-resultsBaseGroup[g][costDim][s]; 
					if(curNMB>bestNMB) {
						bestNMB=curNMB;
						bestResultsBaseGroup[g]=resultsBaseGroup[g][numDim][s];
					}
				}
			}
			
			
			for(int p=0; p<numParams; p++) {
				for(int i=0; i<=intervals; i++) {
					bestNMB=(results[effectDim][0][p][i]*WTP)-results[costDim][0][p][i]; //initialize
					bestStrategy[p][i]=0;
					for(int s=1; s<numStrat; s++) {
						double curNMB=(results[effectDim][s][p][i]*WTP)-results[costDim][s][p][i]; 
						if(curNMB>bestNMB) {
							bestNMB=curNMB;
							bestStrategy[p][i]=s;
						}
					}
				
					//subgroups
					for(int g=0; g<numSubgroups; g++) {
						bestNMB=(resultsGroup[g][effectDim][0][p][i]*WTP)-resultsGroup[g][costDim][0][p][i]; //initialize
						bestStrategyGroup[g][p][i]=0;
						for(int s=1; s<numStrat; s++) {
							double curNMB=(resultsGroup[g][effectDim][s][p][i]*WTP)-resultsGroup[g][costDim][s][p][i]; 
							if(curNMB>bestNMB) {
								bestNMB=curNMB;
								bestStrategyGroup[g][p][i]=s;
							}
						}
					}
				}
			}
		}
	}
	
	
	private void updateChart() {
		int chartType=comboChartType.getSelectedIndex();
		int group=0;
		if(numSubgroups>0){
			group=comboGroup.getSelectedIndex();
		}
		
		double globalMin=Double.POSITIVE_INFINITY, globalMax=Double.NEGATIVE_INFINITY;
		
		XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
		XYIntervalSeries[] series = new XYIntervalSeries[numStrat];
		for(int s=0; s<numStrat; s++) {
			series[s]=new XYIntervalSeries(myModel.strategyNames[s]);
			dataset.addSeries(series[s]);
		}
		
		if(chartType==0) { //parameter values
			for(int p=0; p<numParams; p++) {
				for(int i=0; i<=intervals; i++) {
					int bestStrat=bestStrategy[p][i];
					if(group>0) {bestStrat=bestStrategyGroup[group-1][p][i];}
					series[bestStrat].add(p,p-0.2,p+0.2,i,i-0.5,i+0.5);
				}
			}
		}
		else if(chartType==1) { //outcomes
			DimInfo dimInfo=myModel.dimInfo;
			int curDim=dimInfo.objectiveDim;
			if(dimInfo.analysisType>0) { //CEA or BCA
				curDim=dimInfo.dimNames.length;
			}
			
			for(int p=0; p<numParams; p++) {
				//get best vals
				double bestVals[]=new double[intervals+1];
				for(int i=0; i<=intervals; i++) {
					int bestStrat=bestStrategy[p][i];
					bestVals[i]=results[curDim][bestStrat][p][i];
					if(group>0) { //subgroup
						bestStrat=bestStrategyGroup[group-1][p][i];
						bestVals[i]=resultsGroup[group-1][curDim][bestStrat][p][i];
					}
				}
								
				for(int i=0; i<=intervals; i++) {
					int bestStrat=bestStrategy[p][i];
					if(group>0) {bestStrat=bestStrategyGroup[group-1][p][i];}
					double result=bestVals[i];
					double left=result, right=result;
					if(i==0) { //first
						double mid=(result+bestVals[i+1])/2.0;
						left=Math.min(result, mid);
						right=Math.max(result, mid);
						series[bestStrat].add(p,p-0.2,p+0.2,result,left,right);
					}
					else if(i==intervals) { //last
						double mid=(result+bestVals[i-1])/2.0;
						left=Math.min(result, mid);
						right=Math.max(result, mid);
						series[bestStrat].add(p,p-0.2,p+0.2,result,left,right);
					}
					else { //inner
						double resultL=Math.min(bestVals[i-1], bestVals[i+1]);
						double resultR=Math.max(bestVals[i-1], bestVals[i+1]);
						left=(result+resultL)/2.0;
						right=(result+resultR)/2.0;
						series[bestStrat].add(p,p-0.2,p+0.2,result,left,right);
					}
					if(Double.isNaN(left)==false) {
						globalMin=Math.min(globalMin, left);
						globalMax=Math.max(globalMax, left); 
					}
					if(Double.isNaN(right)==false) {
						globalMin=Math.min(globalMin, right);
						globalMax=Math.max(globalMax, right);
					}
					
				}
			}
		}
		
		
		XYBarRenderer xyRend = new XYBarRenderer();
	    xyRend.setShadowVisible(false);
	    xyRend.setUseYInterval(true);
	    xyRend.setBarPainter(new StandardXYBarPainter());
	    xyRend.setDrawBarOutline(true);
	    DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
	    for(int s=0; s<numStrat; s++) {
	    	xyRend.setSeriesPaint(s, supplier.getNextPaint());
	    	xyRend.setSeriesOutlinePaint(s, Color.LIGHT_GRAY);
	    }
	   	    
	    SymbolAxis xAxis = new SymbolAxis("", paramNames);
	    ValueAxis yAxis = new NumberAxis();
	    if(chartType==0) {
	    	yAxis.setLabel("Parameter Value");
	    	yAxis.setTickLabelsVisible(false);
	    }
	    else if(chartType==1) {
	    	yAxis.setRange(globalMin*0.95,globalMax*1.05);
	    	DimInfo dimInfo=myModel.dimInfo;
	    	if(dimInfo.analysisType==0) {
	    		yAxis.setLabel(dimInfo.dimNames[dimInfo.objectiveDim]);
	    	}
	    	else if(dimInfo.analysisType==1) { //CEA
	    		yAxis.setLabel("ICER ("+dimInfo.dimNames[dimInfo.costDim]+"/"+dimInfo.dimNames[dimInfo.effectDim]+")");
	    	}
	    	else if(dimInfo.analysisType==2) { //BCA
	    		yAxis.setLabel("NMB ("+dimInfo.dimNames[dimInfo.effectDim]+"-"+dimInfo.dimNames[dimInfo.costDim]+")");
	    	}
	    }
	    
		XYPlot plot = new XYPlot(dataset,xAxis,yAxis,xyRend);
		plot.setOrientation(PlotOrientation.HORIZONTAL);
	    plot.setBackgroundPaint(new Color(1,1,1,1));    
	    
	    //parameter labels
	    int labels=comboParamVals.getSelectedIndex();
	    
	    if(chartType==0) { //parameters
	    	//baseline values
	    	for(int p=0; p<numParams; p++) {
	    		double baseVal=(paramValsBase[p]-paramVals[p][0])/(paramVals[p][1]-paramVals[p][0]);
	    		baseVal*=intervals;
	    		Stroke dash = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4.0f, 4.0f}, 0);
	    		XYLineAnnotation curVal=new XYLineAnnotation(p-0.2,baseVal,p+0.2,baseVal, dash, Color.black);
	    		plot.addAnnotation(curVal);
	    	}
	    	
	    	
	    	if(labels==1) { //range
	    		for(int p=0; p<numParams; p++) {
	    			XYTextAnnotation lblMin=new XYTextAnnotation(paramVals[p][0]+"",p+0.2,0); 
	    			lblMin.setTextAnchor(TextAnchor.TOP_CENTER);
	    			plot.addAnnotation(lblMin);

	    			XYTextAnnotation lblMax=new XYTextAnnotation(paramVals[p][1]+"",p+0.2,intervals); 
	    			lblMax.setTextAnchor(TextAnchor.TOP_CENTER);
	    			plot.addAnnotation(lblMax);
	    		}
	    	}
	    	else if(labels==2) { //all
	    		for(int p=0; p<numParams; p++) {
	    			double step=(paramVals[p][1]-paramVals[p][0])/(intervals*1.0);
	    			for(int i=0; i<=intervals; i++) {
	    				double val=paramVals[p][0]+(step*i);
	    				val=MathUtils.round(val, 6);
	    				XYTextAnnotation curLbl=new XYTextAnnotation(val+"",p+0.2,i); 
	    				curLbl.setTextAnchor(TextAnchor.TOP_CENTER);
		    			plot.addAnnotation(curLbl);
		    		}
	    		}
	    	}
	    }
	    else if(chartType==1) { //outcome
	    	//baseline
			Stroke fill = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
			if(group==0) {plot.addRangeMarker(new ValueMarker(bestResultBase, Color.BLACK, fill));}
			else {plot.addRangeMarker(new ValueMarker(bestResultsBaseGroup[group-1], Color.BLACK, fill));}
			
			//parameter labels
			DimInfo dimInfo=myModel.dimInfo;
			int curDim=dimInfo.objectiveDim;
			if(dimInfo.analysisType>0) { //CEA or BCA
				curDim=dimInfo.dimNames.length;
			}
			
			if(labels==1) { //range
				for(int p=0; p<numParams; p++) {
					//lower bound
					int bestStrat=bestStrategy[p][0];
					double val=results[curDim][bestStrat][p][0];
					if(group>0) { //subgroup
						bestStrat=bestStrategyGroup[group-1][p][0];
						val=resultsGroup[group-1][curDim][bestStrat][p][0];
					}
					XYTextAnnotation lblMin=new XYTextAnnotation(paramVals[p][0]+"",p+0.2,val); 
					lblMin.setTextAnchor(TextAnchor.TOP_CENTER);
					plot.addAnnotation(lblMin);

					//upper bound
					bestStrat=bestStrategy[p][intervals];
					val=results[curDim][bestStrat][p][intervals];
					if(group>0) { //subgroup
						bestStrat=bestStrategyGroup[group-1][p][intervals];
						val=resultsGroup[group-1][curDim][bestStrat][p][intervals];
					}
					XYTextAnnotation lblMax=new XYTextAnnotation(paramVals[p][1]+"",p+0.2,val); 
					lblMax.setTextAnchor(TextAnchor.TOP_CENTER);
					plot.addAnnotation(lblMax);
				}
			}
			else if(labels==2) { //all
	    		for(int p=0; p<numParams; p++) {
	    			double step=(paramVals[p][1]-paramVals[p][0])/(intervals*1.0);
	    			for(int i=0; i<=intervals; i++) {
	    				double paramVal=paramVals[p][0]+(step*i);
	    				paramVal=MathUtils.round(paramVal, 6);
	    				
	    				int bestStrat=bestStrategy[p][i];
						double val=results[curDim][bestStrat][p][i];
						if(group>0) { //subgroup
							bestStrat=bestStrategyGroup[group-1][p][i];
							val=resultsGroup[group-1][curDim][bestStrat][p][i];
						}
						
	    				XYTextAnnotation curLbl=new XYTextAnnotation(paramVal+"",p+0.2,val); 
	    				curLbl.setTextAnchor(TextAnchor.TOP_CENTER);
		    			plot.addAnnotation(curLbl);
		    		}
	    		}
	    	}
		}
		
		chart = new JFreeChart(plot);
		plot.setBackgroundPaint(new Color(1,1,1,1));
		plot.getDomainAxis().setInverted(true);
		
		TextTitle legendText = new TextTitle("Best Strategy");
		legendText.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legendText);
				
		panelChart.setChart(chart);
	}
	
}
