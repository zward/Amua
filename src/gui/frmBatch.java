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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import base.AmuaModel;
import base.MicroStats;
import base.RunReport;
import base.RunReportSummary;
import filters.CSVFilter;
import main.CEAHelper;
import main.Console;
import main.ConsoleTable;
import main.DimInfo;
import markov.MarkovTrace;
import markov.MarkovTraceSummary;
import math.KernelSmooth;
import math.MathUtils;

/**
 *
 */
public class frmBatch {

	public JFrame frmBatch;
	AmuaModel myModel;

	DefaultXYDataset chartDataResults, chartDataScatter;
	JFreeChart chartResults, chartScatter;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	JComboBox<String> comboScatterType;

	int numStrat;
	int numSubgroups=0;
	String subgroupNames[];
	/**
	 * [Group][Outcome][Strategy][x,y][Iteration]
	 */
	double dataResultsIter[][][][][], dataResultsVal[][][][][], dataResultsDens[][][][], dataResultsCumDens[][][][][];
	double dataScatterAbs[][][][], dataScatterRel[][][][];
	String CEAnotes[][][];
	private JTextField textIterations;
	int numIterations;
	String outcome;

	RunReport reports[];
	
	public frmBatch(AmuaModel myModel){
		this.myModel=myModel;
		numStrat=myModel.getStrategies();
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmBatch = new JFrame();
			frmBatch.setTitle("Amua - Batch Runs (1st-order uncertainty)");
			frmBatch.setIconImage(Toolkit.getDefaultToolkit().getImage(frmBatch.class.getResource("/images/runBatch_128.png")));
			frmBatch.setBounds(100, 100, 1000, 600);
			frmBatch.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmBatch.getContentPane().setLayout(gridBagLayout);

			DimInfo info=myModel.dimInfo;
			String outcomes[] = null;
			if(info.analysisType==0){ //EV
				outcomes=new String[info.dimNames.length];
				for(int d=0; d<info.dimNames.length; d++){
					outcomes[d]=info.dimNames[d];
				}
			}
			else{ //CEA or BCA
				outcomes=new String[info.dimNames.length+1];
				for(int d=0; d<info.dimNames.length; d++){
					outcomes[d]=info.dimNames[d];
				}
				if(info.analysisType==1){ //CEA
					outcomes[info.dimNames.length]="ICER ("+info.dimNames[info.costDim]+"/"+info.dimNames[info.effectDim]+")";
				}
				else if(info.analysisType==2){ //BCA
					outcomes[info.dimNames.length]="NMB ("+info.dimNames[info.effectDim]+"-"+info.dimNames[info.costDim]+")";
				}
			}

			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 0;
			gbc_tabbedPane.gridy = 0;
			frmBatch.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelResults = new JPanel();
			tabbedPane.addTab("Results", null, panelResults, null);
			GridBagLayout gbl_panelResults = new GridBagLayout();
			gbl_panelResults.columnWidths = new int[]{264, 371, 0, 0};
			gbl_panelResults.rowHeights = new int[]{0, 73, 0};
			gbl_panelResults.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panelResults.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panelResults.setLayout(gbl_panelResults);

			chartDataResults = new DefaultXYDataset();
			chartResults = ChartFactory.createScatterPlot(null, "Value", "Density", chartDataResults, PlotOrientation.VERTICAL, true, false, false);
			chartResults.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartResults.getXYPlot().addDomainMarker(marker);
			chartResults.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChartResults = new ChartPanel(chartResults,false);
			GridBagConstraints gbc_panelChartResults = new GridBagConstraints();
			gbc_panelChartResults.gridwidth = 3;
			gbc_panelChartResults.insets = new Insets(0, 0, 5, 0);
			gbc_panelChartResults.fill = GridBagConstraints.BOTH;
			gbc_panelChartResults.gridx = 0;
			gbc_panelChartResults.gridy = 0;
			panelResults.add(panelChartResults, gbc_panelChartResults);

			JPanel panel = new JPanel();
			panel.setLayout(null);
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.gridwidth = 2;
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;
			panelResults.add(panel, gbc_panel);

			JLabel lblIterations = new JLabel("# Iterations:");
			lblIterations.setBounds(10, 45, 64, 16);
			panel.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setBounds(78, 39, 69, 28);
			panel.add(textIterations);
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			textIterations.setColumns(10);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(167, 39, 90, 28);
			panel.add(btnRun);

			final JButton btnExport = new JButton("Export");
			btnExport.setBounds(294, 39, 90, 28);
			panel.add(btnExport);
			btnExport.setEnabled(false);

			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setBounds(6, 0, 257, 26);
			panel.add(comboDimensions);

			comboResults = new JComboBox<String>();
			comboResults.setBounds(281, 0, 158, 26);
			panel.add(comboResults);
			comboResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			comboResults.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
			
			comboGroup = new JComboBox<String>();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
					updateScatter();
				}
			});
			comboGroup.setVisible(false);
			comboGroup.setBounds(458, 0, 170, 26);
			panel.add(comboGroup);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				numSubgroups=myModel.subgroupNames.size();
				subgroupNames=new String[numSubgroups+1];
				subgroupNames[0]="Overall";
				for(int i=0; i<numSubgroups; i++){
					subgroupNames[i+1]=myModel.subgroupNames.get(i);
				}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
			}
			
			comboDimensions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter());

						fc.setDialogTitle("Export Batch Results");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmBatch);
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
							int analysisType=info.analysisType;
							int numStrat=myModel.strategyNames.length;
							int group=0; //overall
							if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}
							
							out.write("Iteration");
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
							int numPoints=dataResultsIter[group][0][0][0].length;
							for(int i=0; i<numPoints; i++){
								out.write((i+1)+""); //Iteration
								//Outcomes
								for(int d=0; d<numDim; d++){ //EVs
									out.write(",");
									for(int s=0; s<numStrat; s++){out.write(","+dataResultsIter[group][d][s][1][i]);}
								}
								if(analysisType>0){
									out.write(",");
									if(analysisType==1){ //CEA
										for(int s=0; s<numStrat; s++){
											double icer=dataResultsIter[group][numDim][s][1][i];
											if(!Double.isNaN(icer)){out.write(","+icer);} //valid ICER
											else{out.write(","+CEAnotes[group][s][i]);} //invalid ICER
										}
									}
									else if(analysisType==2){ //BCA
										for(int s=0; s<numStrat; s++){out.write(","+dataResultsIter[group][numDim][s][1][i]);}
									}
								}

								out.newLine();
							}
							out.close();

							
							//Individual results
							if(myModel.simType==1 && myModel.displayIndResults==true){
								FileWriter fstream2 = new FileWriter(path+"_IndResults.csv"); //Create new file
								BufferedWriter out2 = new BufferedWriter(fstream2);
								
								int numVars=myModel.variables.size();
								
								//Headers
								String est[]=new String[]{"Mean","SD","Min","Q1","Median","Q3","Max"};
								
								out2.write("Iteration,Strategy");
								for(int d=0; d<numDim; d++){
									for(int j=0; j<7; j++){
										out2.write(","+myModel.dimInfo.dimNames[d]+"_"+est[j]);
									}
								}
								for(int v=0; v<numVars; v++){
									for(int j=0; j<7; j++){
										out2.write(","+myModel.variables.get(v).name+"_"+est[j]);
									}
								}
								out2.newLine();
								
								for(int r=0; r<reports.length; r++){
									for(int s=0; s<numStrat; s++){
										out2.write(r+","+myModel.strategyNames[s]); 
										MicroStats curStats=reports[r].microStats.get(s);
										for(int d=0; d<numDim; d++){
											out2.write(","+curStats.outcomesMean[d]);
											out2.write(","+curStats.outcomesSD[d]);
											out2.write(","+curStats.outcomesMin[d]);
											out2.write(","+curStats.outcomesQ1[d]);
											out2.write(","+curStats.outcomesMed[d]);
											out2.write(","+curStats.outcomesQ3[d]);
											out2.write(","+curStats.outcomesMax[d]);
										}
										for(int v=0; v<numVars; v++){
											out2.write(","+curStats.varsMean[v]);
											out2.write(","+curStats.varsSD[v]);
											out2.write(","+curStats.varsMin[v]);
											out2.write(","+curStats.varsQ1[v]);
											out2.write(","+curStats.varsMed[v]);
											out2.write(","+curStats.varsQ3[v]);
											out2.write(","+curStats.varsMax[v]);
										}
										out2.newLine();
									}
								}
							
								out2.close();
							}
							
							JOptionPane.showMessageDialog(frmBatch, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmBatch, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmBatch, "Batch Runs", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								tabbedPane.setEnabledAt(1, false);

								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();

								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmBatch, "Errors in base case model!");
								}
								else{
									boolean cancelled=false;

									numIterations=Integer.parseInt(textIterations.getText().replaceAll(",", ""));
									progress.setMaximum(numIterations);

									int numOutcomes=comboDimensions.getItemCount();
									int numDim=myModel.dimInfo.dimNames.length;
									int analysisType=myModel.dimInfo.analysisType;
									if(analysisType==1){CEAnotes=new String[numSubgroups+1][numStrat][numIterations];} //CEA
									else{CEAnotes=null;}

									dataResultsIter=new double[numSubgroups+1][numOutcomes][numStrat][2][numIterations];
									dataResultsVal=new double[numSubgroups+1][numOutcomes][numStrat][2][numIterations];
									dataResultsCumDens=new double[numSubgroups+1][numOutcomes][numStrat][2][numIterations];

									dataScatterAbs=new double[numSubgroups+1][numStrat][2][numIterations];
									dataScatterRel=new double[numSubgroups+1][numStrat][2][numIterations];

									reports=new RunReport[numIterations];
								
									myModel.evaluateParameters(); //get parameters
									
									boolean origShowTrace = true;
									if(myModel.type==1){
										origShowTrace=myModel.markov.showTrace;
										myModel.markov.showTrace=false; //don't show individual trace
									}
									
									long startTime=System.currentTimeMillis();

									for(int n=0; n<numIterations; n++){
										//Update progress
										double prog=((n+1)/(numIterations*1.0))*100;
										long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
										remTime=(long) (remTime*(100-prog));
										remTime=remTime/1000;
										String seconds = Integer.toString((int)(remTime % 60));
										String minutes = Integer.toString((int)(remTime/60));
										if(seconds.length()<2){seconds="0"+seconds;}
										if(minutes.length()<2){minutes="0"+minutes;}
										progress.setProgress(n+1);
										progress.setNote("Time left: "+minutes+":"+seconds);

										//Run model
										reports[n]=myModel.runModel(null, false);
										

										//Get EVs
										for(int d=0; d<numDim; d++){
											for(int s=0; s<numStrat; s++){
												//overall
												dataResultsIter[0][d][s][0][n]=n; dataResultsVal[0][d][s][0][n]=n;
												double curOutcome=myModel.getStrategyEV(s, d);
												dataResultsIter[0][d][s][1][n]=curOutcome; dataResultsVal[0][d][s][1][n]=curOutcome;
												//subgroups
												for(int g=0; g<numSubgroups; g++){
													dataResultsIter[g+1][d][s][0][n]=n; dataResultsVal[g+1][d][s][0][n]=n;
													curOutcome=myModel.getSubgroupEV(g,s,d);
													dataResultsIter[g+1][d][s][1][n]=curOutcome; dataResultsVal[g+1][d][s][1][n]=curOutcome;
												}
											}
										}
										if(analysisType>0){ //CEA or BCA
											if(analysisType==1){ //CEA
												for(int g=0; g<numSubgroups+1; g++){
													Object table[][]=new CEAHelper().calculateICERs(myModel,g-1,true);
													//get baseline row
													int baseIndex=myModel.getStrategyIndex(myModel.dimInfo.baseScenario);
													int baseRow=-1,curRow=0;
													while(baseRow==-1 && curRow<table.length){
														if((int)table[curRow][0]==baseIndex){
															baseRow=curRow;
														}
														curRow++;
													}

													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat!=-1){
															dataResultsIter[g][numDim][origStrat][0][n]=n; dataResultsVal[g][numDim][origStrat][0][n]=n;
															double curOutcome=(double) table[s][4];
															dataResultsIter[g][numDim][origStrat][1][n]=curOutcome; dataResultsVal[g][numDim][origStrat][1][n]=curOutcome;
															CEAnotes[g][origStrat][n]=(String) table[s][5];
															double cost=(double) table[s][2];
															double benefit=(double) table[s][3];
															dataScatterAbs[g][origStrat][0][n]=benefit;
															dataScatterAbs[g][origStrat][1][n]=cost;
															double baseCost=(double) table[baseRow][2];
															double baseBenefit=(double) table[baseRow][3];
															dataScatterRel[g][origStrat][0][n]=benefit-baseBenefit;
															dataScatterRel[g][origStrat][1][n]=cost-baseCost;
														}
													}
												}
											}
											else if(analysisType==2){ //BCA
												for(int g=0; g<numSubgroups+1; g++){
													Object table[][]=new CEAHelper().calculateNMB(myModel,g-1,true);
													//get baseline row
													int baseIndex=myModel.getStrategyIndex(myModel.dimInfo.baseScenario);
													int baseRow=-1,curRow=0;
													while(baseRow==-1 && curRow<table.length){
														if((int)table[curRow][0]==baseIndex){
															baseRow=curRow;
														}
														curRow++;
													}
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														dataResultsIter[g][numDim][origStrat][0][n]=n;	dataResultsVal[g][numDim][origStrat][0][n]=n;
														double curOutcome=(double) table[s][4];
														dataResultsIter[g][numDim][origStrat][1][n]=curOutcome; dataResultsVal[g][numDim][origStrat][1][n]=curOutcome;
														double cost=(double) table[s][2];
														double benefit=(double) table[s][3];
														dataScatterAbs[g][origStrat][0][n]=benefit;
														dataScatterAbs[g][origStrat][1][n]=cost;
														double baseCost=(double) table[baseRow][2];
														double baseBenefit=(double) table[baseRow][3];
														dataScatterRel[g][origStrat][0][n]=benefit-baseBenefit;
														dataScatterRel[g][origStrat][1][n]=cost-baseCost;
													}
												}
											}
										}

										if(progress.isCanceled()){  //End loop
											n=numIterations;
											cancelled=true;
										}
									}
									myModel.unlockParams();
									if(myModel.type==1){
										myModel.markov.showTrace=origShowTrace; //reset
									}
																		
									if(cancelled==false){
										double meanResults[][][]=new double[numSubgroups+1][numOutcomes][numStrat];
										double lbResults[][][]=new double[numSubgroups+1][numOutcomes][numStrat];
										double ubResults[][][]=new double[numSubgroups+1][numOutcomes][numStrat];
										int bounds[]=MathUtils.getBoundIndices(numIterations);
										int indexLB=bounds[0], indexUB=bounds[1];

										//Sort ordered arrays
										for(int d=0; d<numOutcomes; d++){
											for(int s=0; s<numStrat; s++){
												for(int g=0; g<numSubgroups+1; g++){
													Arrays.sort(dataResultsVal[g][d][s][1]);
													for(int n=0; n<numIterations; n++){
														dataResultsVal[g][d][s][0][n]=n/(numIterations*1.0);
														dataResultsCumDens[g][d][s][0][n]=dataResultsVal[g][d][s][1][n];
														dataResultsCumDens[g][d][s][1][n]=dataResultsVal[g][d][s][0][n];
														meanResults[g][d][s]+=dataResultsVal[g][d][s][1][n];
													}
													meanResults[g][d][s]/=(numIterations*1.0);
													lbResults[g][d][s]=dataResultsVal[g][d][s][1][indexLB];
													ubResults[g][d][s]=dataResultsVal[g][d][s][1][indexUB];
												}
											}
										}

										//Update results chart
										updateResultsChart();

										//Update scatter chart
										if(analysisType>0){
											tabbedPane.setEnabledAt(1, true);
											XYPlot plotScatter = chartScatter.getXYPlot();
											XYLineAndShapeRenderer rendererScatter = new XYLineAndShapeRenderer(false,true);
											Shape dot=new Ellipse2D.Double(0,0,3,3);
											DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											for(int s=0; s<numStrat; s++){
												rendererScatter.setSeriesPaint(s, supplier.getNextPaint());
												rendererScatter.setSeriesShape(s, dot);
											}
											plotScatter.setRenderer(rendererScatter);
											updateScatter();


										}
										btnExport.setEnabled(true);

										//Get trace summary
										if(myModel.type==1){
											//get mean and bounds of results
											int numChains=reports[0].markovTraces.size();
											for(int c=0; c<numChains; c++){
												MarkovTrace curTraces[][]=new MarkovTrace[numSubgroups+1][numIterations];
												for(int i=0; i<numIterations; i++){
													curTraces[0][i]=reports[i].markovTraces.get(c);
													for(int g=0; g<numSubgroups; g++){
														curTraces[g+1][i]=reports[i].markovTracesGroup[g].get(c);
													}
												}
												MarkovTraceSummary traceSummaries[]=new MarkovTraceSummary[numSubgroups+1];
												traceSummaries[0]=new MarkovTraceSummary(curTraces[0]);
												for(int g=0; g<numSubgroups; g++){
													traceSummaries[g+1]=new MarkovTraceSummary(curTraces[g+1]);
												}
												frmTraceSummary showSummary=new frmTraceSummary(traceSummaries,myModel.errorLog,subgroupNames);
												showSummary.frmTraceSummary.setVisible(true);
											}
										}

										//Print results summary to console
										Console console=myModel.mainForm.console;
										myModel.printSimInfo(console);
										console.print("Batch Iterations:\t"+numIterations+"\n\n");
										boolean colTypes[]=new boolean[]{false,false,true,true,true}; //is column number (true), or text (false)
										ConsoleTable curTable=new ConsoleTable(console,colTypes);
										String headers[]=new String[]{"Strategy","Outcome","Mean","95% LB","95% UB"};
										curTable.addRow(headers);
										//strategy results
										for(int s=0; s<numStrat; s++){
											String stratName=myModel.strategyNames[s];
											for(int d=0; d<numDim; d++){
												String dimName=myModel.dimInfo.dimNames[d];
												if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" (Dis)";}
												double mean=MathUtils.round(meanResults[0][d][s],myModel.dimInfo.decimals[d]);
												double lb=MathUtils.round(lbResults[0][d][s],myModel.dimInfo.decimals[d]);
												double ub=MathUtils.round(ubResults[0][d][s],myModel.dimInfo.decimals[d]);
												String curRow[]=new String[]{stratName,dimName,mean+"",lb+"",ub+""};
												curTable.addRow(curRow);
											}
										}
										curTable.print();
										
										//subgroups
										for(int g=0; g<numSubgroups; g++){
											console.print("\nSubgroup Results: "+reports[0].subgroupNames[g]+"\n");
											curTable=new ConsoleTable(console,colTypes);
											curTable.addRow(headers);
											for(int s=0; s<numStrat; s++){
												String stratName=myModel.strategyNames[s];
												for(int d=0; d<numDim; d++){
													String dimName=myModel.dimInfo.dimNames[d];
													if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" (Dis)";}
													double mean=MathUtils.round(meanResults[g+1][d][s],myModel.dimInfo.decimals[d]);
													double lb=MathUtils.round(lbResults[g+1][d][s],myModel.dimInfo.decimals[d]);
													double ub=MathUtils.round(ubResults[g+1][d][s],myModel.dimInfo.decimals[d]);
													String curRow[]=new String[]{stratName,dimName,mean+"",lb+"",ub+""};
													curTable.addRow(curRow);
												}
											}
											curTable.print();
										}
										
										if(myModel.simType==1 && myModel.displayIndResults==true){
											console.print("\nIndividual-level Results:\n");
											RunReportSummary summary=new RunReportSummary(reports);
											for(int s=0; s<numStrat; s++){
												console.print("Strategy: "+myModel.strategyNames[s]+"\n");
												summary.microStatsSummary[s].printSummary(console);
											}
											//subgroups
											for(int g=0; g<numSubgroups; g++){
												console.print("\nSubgroup Results: "+summary.subgroupNames[g]+"\n");
												for(int s=0; s<numStrat; s++){
													console.print("Strategy: "+myModel.strategyNames[s]+"\n");
													summary.microStatsSummaryGroup[g][s].printSummary(console);
												}
											}
										}
										console.print("\n");
										console.newLine();


									}
									progress.close();
								}

							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmBatch, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			JPanel panelScatter = new JPanel();
			tabbedPane.addTab("Scatter", null, panelScatter, null); 
			tabbedPane.setEnabledAt(1, false);

			chartDataScatter = new DefaultXYDataset();
			chartScatter = ChartFactory.createScatterPlot(null, "x", "y", chartDataScatter, PlotOrientation.VERTICAL, true, false, false);
			chartScatter.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartScatter.getXYPlot().addDomainMarker(marker);
			chartScatter.getXYPlot().addRangeMarker(marker);

			GridBagLayout gbl_panelScatter = new GridBagLayout();
			gbl_panelScatter.columnWidths = new int[]{155, 680, 0};
			gbl_panelScatter.rowHeights = new int[]{0, 420, 0};
			gbl_panelScatter.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelScatter.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelScatter.setLayout(gbl_panelScatter);

			comboScatterType = new JComboBox<String>();
			comboScatterType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateScatter();
				}
			});
			comboScatterType.setModel(new DefaultComboBoxModel(new String[] {"Absolute Magnitude", "Relative to Baseline"}));
			GridBagConstraints gbc_comboScatterType = new GridBagConstraints();
			gbc_comboScatterType.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboScatterType.insets = new Insets(0, 0, 5, 5);
			gbc_comboScatterType.gridx = 0;
			gbc_comboScatterType.gridy = 0;
			panelScatter.add(comboScatterType, gbc_comboScatterType);

			ChartPanel panelChartScatter = new ChartPanel(chartScatter);
			GridBagConstraints gbc_panelChartScatter = new GridBagConstraints();
			gbc_panelChartScatter.gridwidth = 2;
			gbc_panelChartScatter.fill = GridBagConstraints.BOTH;
			gbc_panelChartScatter.gridx = 0;
			gbc_panelChartScatter.gridy = 1;
			panelScatter.add(panelChartScatter, gbc_panelChartScatter);
			panelChartScatter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	public void updateResultsChart(){
		DimInfo info=myModel.dimInfo;
		int dim=comboDimensions.getSelectedIndex();
		int analysisType=0; //EV
		if(myModel.dimInfo.analysisType>0){ //CEA or BCA
			if(dim==comboDimensions.getItemCount()-1){ //ICER or NMB selected
				analysisType=myModel.dimInfo.analysisType;
			}
		}
		if(analysisType==0){outcome="EV ("+info.dimSymbols[dim]+")";}
		else if(analysisType==1){outcome="ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")";}
		else if(analysisType==2){outcome="NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")";}
		
		int group=0; //overall
		if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}
		
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){
				chartDataResults.removeSeries(myModel.strategyNames[s]);
			}
		}
		XYPlot plotResults = chartResults.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, supplierResults.getNextPaint());
		}
		plotResults.setRenderer(rendererResults);

		int selected=comboResults.getSelectedIndex();
		int numStrat=myModel.strategyNames.length;
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataResults.removeSeries(myModel.strategyNames[s]);}
		}

		if(selected==0){ //Density
			chartResults.getXYPlot().getDomainAxis().setLabel("Value");
			chartResults.getXYPlot().getRangeAxis().setLabel("Density");
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.density(dataResultsIter[group][dim][s][1], 100);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==1){ //Histogram
			chartResults.getXYPlot().getDomainAxis().setLabel("Value");
			chartResults.getXYPlot().getRangeAxis().setLabel("Frequency");
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.histogram(dataResultsIter[group][dim][s][1], 100, 10);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==2){ //CDF
			chartResults.getXYPlot().getDomainAxis().setLabel("Value");
			chartResults.getXYPlot().getRangeAxis().setLabel("Cumulative Distribution");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],dataResultsCumDens[group][dim][s]);
			}

		}
		else if(selected==3){ //Quantile
			chartResults.getXYPlot().getDomainAxis().setLabel("Quantile");
			chartResults.getXYPlot().getRangeAxis().setLabel("Value");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],dataResultsVal[group][dim][s]);
			}
		}
		else if(selected==4){ //Iteration
			chartResults.getXYPlot().getDomainAxis().setLabel("Iteration");
			chartResults.getXYPlot().getRangeAxis().setLabel("Value");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],dataResultsIter[group][dim][s]);
			}
		}
	}

	private void updateScatter(){
		int type=comboScatterType.getSelectedIndex();
		int numStrat=myModel.strategyNames.length;
		int group=0; //overall
		if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

		DimInfo info=myModel.dimInfo;
		if(chartDataScatter.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataScatter.removeSeries(myModel.strategyNames[s]);}
		}		
		if(type==0){ //absolute
			chartScatter.getXYPlot().getRangeAxis().setLabel(info.dimNames[info.costDim]);
			chartScatter.getXYPlot().getDomainAxis().setLabel(info.dimNames[info.effectDim]);
			for(int s=0; s<numStrat; s++){
				chartDataScatter.addSeries(myModel.strategyNames[s],dataScatterAbs[group][s]);
			}
		}
		else if(type==1){ //relative to baseline
			chartScatter.getXYPlot().getRangeAxis().setLabel("∆ "+info.dimNames[info.costDim]);
			chartScatter.getXYPlot().getDomainAxis().setLabel("∆ "+info.dimNames[info.effectDim]);
			for(int s=0; s<numStrat; s++){
				chartDataScatter.addSeries(myModel.strategyNames[s],dataScatterRel[group][s]);
			}
		}
	}
}
