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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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

	frmBatch frmThis;
	public JFrame frmBatch;
	AmuaModel myModel;

	DefaultXYDataset chartDataResults, chartDataScatter;
	JFreeChart chartResults, chartScatter;
	Paint seriesPaints[];

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
		this.frmThis=this;
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
			frmBatch.setTitle("Amua - "+myModel.language.base.getString("title.batch_runs")); //Batch Runs (1st-order uncertainty)
			frmBatch.setFont(myModel.language.font);
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
					outcomes[info.dimNames.length]=myModel.language.analysis.getString("cea.icer")+" ("+info.dimNames[info.costDim]+"/"+info.dimNames[info.effectDim]+")"; //ICER
				}
				else if(info.analysisType==2){ //BCA
					outcomes[info.dimNames.length]=myModel.language.analysis.getString("bca.nmb")+" ("+info.dimNames[info.effectDim]+"-"+info.dimNames[info.costDim]+")"; //NMB
				}
			}

			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 0;
			gbc_tabbedPane.gridy = 0;
			frmBatch.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelResults = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("result.results"), null, panelResults, null); //Results
			tabbedPane.setFont(myModel.language.font);
			GridBagLayout gbl_panelResults = new GridBagLayout();
			gbl_panelResults.columnWidths = new int[]{86, 50, 110, 207, 162, 0, 0};
			gbl_panelResults.rowHeights = new int[]{0, 0, 0, 0};
			gbl_panelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panelResults.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
			panelResults.setLayout(gbl_panelResults);

			chartDataResults = new DefaultXYDataset();
			chartResults = ChartFactory.createScatterPlot(null, myModel.language.analysis.getString("result.value"), myModel.language.base.getString("plot.density"), 
					chartDataResults, PlotOrientation.VERTICAL, true, false, false); //Value, Density
			chartResults.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//set font
			chartResults.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartResults.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartResults.getLegend().setItemFont(myModel.language.font);
			
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartResults.getXYPlot().addDomainMarker(marker);
			chartResults.getXYPlot().addRangeMarker(marker);

			numStrat=myModel.getStrategies();
			seriesPaints=new Paint[numStrat];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			for(int s=0; s<numStrat; s++) {
				seriesPaints[s]=supplier.getNextPaint();
			}

			ChartPanel panelChartResults = new ChartPanel(chartResults,false);
			GridBagConstraints gbc_panelChartResults = new GridBagConstraints();
			gbc_panelChartResults.gridwidth = 6;
			gbc_panelChartResults.insets = new Insets(0, 0, 5, 0);
			gbc_panelChartResults.fill = GridBagConstraints.BOTH;
			gbc_panelChartResults.gridx = 0;
			gbc_panelChartResults.gridy = 0;
			panelResults.add(panelChartResults, gbc_panelChartResults);

			//pop-up menu
			JPopupMenu popup = panelChartResults.getPopupMenu();
			JMenuItem mntmChangeColor = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartResults, chartDataResults, seriesPaints, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setFont(myModel.language.font);
			GridBagConstraints gbc_comboDimensions = new GridBagConstraints();
			gbc_comboDimensions.gridwidth = 3;
			gbc_comboDimensions.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboDimensions.insets = new Insets(0, 0, 5, 5);
			gbc_comboDimensions.gridx = 0;
			gbc_comboDimensions.gridy = 1;
			panelResults.add(comboDimensions, gbc_comboDimensions);

			comboDimensions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			
			String plotTypes[]=new String[5];
			plotTypes[0]=myModel.language.base.getString("plot.density"); //Density
			plotTypes[1]=myModel.language.base.getString("plot.histogram"); //Histogram
			plotTypes[2]=myModel.language.base.getString("plot.cumulative_distribution"); //Cumulative Distribution
			plotTypes[3]=myModel.language.base.getString("plot.quantiles"); //Quantiles
			plotTypes[4]=myModel.language.base.getString("plot.iteration"); //Iteration

			comboResults = new JComboBox<String>();
			GridBagConstraints gbc_comboResults = new GridBagConstraints();
			gbc_comboResults.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboResults.insets = new Insets(0, 0, 5, 5);
			gbc_comboResults.gridx = 3;
			gbc_comboResults.gridy = 1;
			panelResults.add(comboResults, gbc_comboResults);
			comboResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			comboResults.setModel(new DefaultComboBoxModel<String>(plotTypes));
			comboResults.setFont(myModel.language.font);


			comboGroup = new JComboBox<String>();
			comboGroup.setFont(myModel.language.font);
			GridBagConstraints gbc_comboGroup = new GridBagConstraints();
			gbc_comboGroup.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroup.insets = new Insets(0, 0, 5, 5);
			gbc_comboGroup.gridx = 4;
			gbc_comboGroup.gridy = 1;
			panelResults.add(comboGroup, gbc_comboGroup);
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
					updateScatter();
				}
			});
			comboGroup.setVisible(false);

			JLabel lblIterations = new JLabel(myModel.language.analysis.getString("sim.num_iterations")+":");
			lblIterations.setFont(myModel.language.font);
			GridBagConstraints gbc_lblIterations = new GridBagConstraints();
			gbc_lblIterations.anchor = GridBagConstraints.EAST;
			gbc_lblIterations.insets = new Insets(0, 0, 0, 5);
			gbc_lblIterations.gridx = 0;
			gbc_lblIterations.gridy = 2;
			panelResults.add(lblIterations, gbc_lblIterations);

			textIterations = new JTextField();
			GridBagConstraints gbc_textIterations = new GridBagConstraints();
			gbc_textIterations.insets = new Insets(0, 0, 0, 5);
			gbc_textIterations.gridx = 1;
			gbc_textIterations.gridy = 2;
			panelResults.add(textIterations, gbc_textIterations);
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			textIterations.setColumns(5);

			JButton btnRun = new JButton(myModel.language.base.getString("menu.run"));
			btnRun.setFont(myModel.language.font);
			GridBagConstraints gbc_btnRun = new GridBagConstraints();
			gbc_btnRun.insets = new Insets(0, 0, 0, 5);
			gbc_btnRun.gridx = 2;
			gbc_btnRun.gridy = 2;
			panelResults.add(btnRun, gbc_btnRun);
			
			
			final JButton btnExport = new JButton(myModel.language.base.getString("menu.export"));
			btnExport.setFont(myModel.language.font);

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//final ProgressMonitor progress=new ProgressMonitor(frmBatch, myModel.language.base.getString("menu.batch_runs"), myModel.language.message.getString("info.running"), 0, 100); //Batch Runs, Running
					final frmProgressMonitor progress=new frmProgressMonitor(frmBatch, myModel.language.base.getString("menu.batch_runs"), myModel.language.message.getString("info.running"), 0, 100, myModel.language); //Batch Runs, Running
					SwingUtilities.invokeLater(progress::show);  //dialog is created/shown on EDT
					
					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								tabbedPane.setEnabledAt(1, false);

								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();

								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmBatch, myModel.language.message.getString("err.base_case")); //Errors in base case model!
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
										progress.setNote(myModel.language.message.getString("info.time_left")+": "+minutes+":"+seconds); //Time left

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
											//DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											for(int s=0; s<numStrat; s++){
												rendererScatter.setSeriesPaint(s, seriesPaints[s]);
												rendererScatter.setSeriesShape(s, dot);
											}
											plotScatter.setRenderer(rendererScatter);
											updateScatter();


										}
										btnExport.setEnabled(true);

										//Get trace summary
										if(myModel.type==1 && myModel.markov.showTrace){
											if(myModel.markov.compileTraces==false) {
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
													frmTraceSummary showSummary=new frmTraceSummary(traceSummaries,myModel.errorLog,subgroupNames,myModel.language);
													showSummary.frmTraceSummary.setVisible(true);
												}
											}
											else {
												RunReportSummary reportSummary=new RunReportSummary(reports);
												frmTraceSummaryMulti window=new frmTraceSummaryMulti(reportSummary,myModel.errorLog,myModel.language);
												window.frmTraceSummaryMulti.setVisible(true);
											}
										}

										//Print results summary to console
										Console console=myModel.mainForm.console;
										myModel.printSimInfo(console);
										console.print(myModel.language.analysis.getString("sim.batch_iterations")+":\t"+numIterations+"\n\n"); //Batch Iterations
										boolean colTypes[]=new boolean[]{false,false,true,true,true}; //is column number (true), or text (false)
										ConsoleTable curTable=new ConsoleTable(console,colTypes);
										//String headers[]=new String[]{"Strategy","Outcome","Mean","95% LB","95% UB"};
										String headers[]=new String[5];
										headers[0]=myModel.language.analysis.getString("gen.strategy"); //Strategy
										headers[1]=myModel.language.analysis.getString("result.outcome"); //Outcome
										headers[2]=myModel.language.math.getString("sum.mean"); //Mean
										headers[3]=myModel.language.math.getString("sum.95_lb"); //95% LB
										headers[4]=myModel.language.math.getString("sum.95_ub"); //95% UB
										curTable.addRow(headers);
										//strategy results
										for(int s=0; s<numStrat; s++){
											String stratName=myModel.strategyNames[s];
											for(int d=0; d<numDim; d++){
												String dimName=myModel.dimInfo.dimNames[d];
												if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" "+myModel.language.analysis.getString("result.dis");} //(Dis)
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
											String curLbl=myModel.language.analysis.getString("result.subgroup_results");
											console.print("\n"+curLbl+": "+reports[0].subgroupNames[g]+"\n"); //Subgroup Results
											curTable=new ConsoleTable(console,colTypes);
											curTable.addRow(headers);
											for(int s=0; s<numStrat; s++){
												String stratName=myModel.strategyNames[s];
												for(int d=0; d<numDim; d++){
													String dimName=myModel.dimInfo.dimNames[d];
													if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" "+myModel.language.analysis.getString("result.dis");} //(Dis)
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
											console.print("\n"+myModel.language.analysis.getString("result.individual_level_results")+":\n"); //Individual-level Results
											RunReportSummary summary=new RunReportSummary(reports);
											for(int s=0; s<numStrat; s++){
												console.print(myModel.language.analysis.getString("gen.strategy")+": "+myModel.strategyNames[s]+"\n"); //Strategy
												summary.microStatsSummary[s].printSummary(console);
											}
											//subgroups
											for(int g=0; g<numSubgroups; g++){
												String curLbl=myModel.language.analysis.getString("result.subgroup_results");
												console.print("\n"+curLbl+": "+summary.subgroupNames[g]+"\n"); //Subgroup Results
												for(int s=0; s<numStrat; s++){
													console.print(myModel.language.analysis.getString("gen.strategy")+": "+myModel.strategyNames[s]+"\n"); //Strategy
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

			
			GridBagConstraints gbc_btnExport = new GridBagConstraints();
			gbc_btnExport.insets = new Insets(0, 0, 0, 5);
			gbc_btnExport.gridx = 3;
			gbc_btnExport.gridy = 2;
			panelResults.add(btnExport, gbc_btnExport);
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						myModel.language.setFontRecursively(fc); //set font
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter(myModel.language));

						fc.setDialogTitle(myModel.language.base.getString("title.export_batch_results")); //Export Batch Results
						fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export

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

							out.write(myModel.language.base.getString("plot.iteration")); //Iteration
							for(int d=0; d<numDim; d++){ //EVs
								out.write(","+info.dimNames[d]);
								for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
							}
							if(analysisType>0){ //CEA or BCA
								if(analysisType==1){out.write(","+myModel.language.analysis.getString("cea.icer")+" ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");} //ICER
								else if(analysisType==2){out.write(","+myModel.language.analysis.getString("bca.nmb")+" ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");} //NMB
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
								FileWriter fstream2 = new FileWriter(path+"_"+myModel.language.analysis.getString("result.IndResults")+".csv"); //Create new file (IndResults)
								BufferedWriter out2 = new BufferedWriter(fstream2);

								int numVars=myModel.variables.size();

								//Headers
								//String est[]=new String[]{"Mean","SD","Min","Q1","Median","Q3","Max"};
								String est[]=new String[7];
								est[0]=myModel.language.math.getString("sum.mean"); //Mean
								est[1]=myModel.language.math.getString("sum.SD"); //SD
								est[2]=myModel.language.math.getString("sum.min"); //Min
								est[3]=myModel.language.math.getString("sum.q1"); //Q1
								est[4]=myModel.language.math.getString("sum.median"); //Median
								est[5]=myModel.language.math.getString("sum.q3"); //Q3
								est[6]=myModel.language.math.getString("sum.max"); //Max								

								out2.write(myModel.language.base.getString("plot.iteration")+","+myModel.language.analysis.getString("gen.strategy")); //Iteration, Strategy
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

							JOptionPane.showMessageDialog(frmBatch, myModel.language.message.getString("info.exported")); //Exported!
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmBatch, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			//comboResults.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
			

			if(myModel.simType==1 && myModel.reportSubgroups){
				numSubgroups=myModel.subgroupNames.size();
				subgroupNames=new String[numSubgroups+1];
				subgroupNames[0]=myModel.language.analysis.getString("result.overall"); //Overall
				for(int i=0; i<numSubgroups; i++){
					subgroupNames[i+1]=myModel.subgroupNames.get(i);
				}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
			}

			JPanel panelScatter = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("plot.scatter"), null, panelScatter, null); //Scatter
			tabbedPane.setFont(myModel.language.font);
			tabbedPane.setEnabledAt(1, false);

			chartDataScatter = new DefaultXYDataset();
			chartScatter = ChartFactory.createScatterPlot(null, "x", "y", chartDataScatter, PlotOrientation.VERTICAL, true, false, false);
			chartScatter.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//set font
			chartScatter.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScatter.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScatter.getLegend().setItemFont(myModel.language.font);
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
			comboScatterType.setFont(myModel.language.font);
			comboScatterType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateScatter();
				}
			});
			//comboScatterType.setModel(new DefaultComboBoxModel(new String[] {"Absolute Magnitude", "Relative to Baseline"}));
			String plotType[]=new String[2];
			plotType[0]=myModel.language.base.getString("plot.absolute_magnitude"); //Absolute Magnitude
			plotType[1]=myModel.language.base.getString("plot.relative_baseline"); //Relative to Baseline

			GridBagConstraints gbc_comboScatterType = new GridBagConstraints();
			gbc_comboScatterType.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboScatterType.insets = new Insets(0, 0, 5, 5);
			gbc_comboScatterType.gridx = 0;
			gbc_comboScatterType.gridy = 0;
			panelScatter.add(comboScatterType, gbc_comboScatterType);

			ChartPanel panelChartScatter = new ChartPanel(chartScatter,false);
			GridBagConstraints gbc_panelChartScatter = new GridBagConstraints();
			gbc_panelChartScatter.gridwidth = 2;
			gbc_panelChartScatter.fill = GridBagConstraints.BOTH;
			gbc_panelChartScatter.gridx = 0;
			gbc_panelChartScatter.gridy = 1;
			panelScatter.add(panelChartScatter, gbc_panelChartScatter);
			panelChartScatter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

			//pop-up menu
			popup = panelChartScatter.getPopupMenu();
			JMenuItem mntmChangeColorScatter = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorScatter.setFont(myModel.language.font);
			mntmChangeColorScatter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartScatter, chartDataScatter, seriesPaints, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorScatter, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

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
		if(analysisType==0){outcome=myModel.language.analysis.getString("result.ev")+" ("+info.dimSymbols[dim]+")";} //EV
		else if(analysisType==1){outcome=myModel.language.analysis.getString("cea.icer")+" ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")";} //ICER
		else if(analysisType==2){outcome=myModel.language.analysis.getString("bca.nmb")+" ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")";} //NMB

		int group=0; //overall
		if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){
				chartDataResults.removeSeries(myModel.strategyNames[s]);
			}
		}
		XYPlot plotResults = chartResults.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, seriesPaints[s]);
		}
		plotResults.setRenderer(rendererResults);

		int selected=comboResults.getSelectedIndex();
		int numStrat=myModel.strategyNames.length;
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataResults.removeSeries(myModel.strategyNames[s]);}
		}

		if(selected==0){ //Density
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.density")); //Density
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.density(dataResultsIter[group][dim][s][1], 100);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==1){ //Histogram
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.frequency")); //Frequency
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.histogram(dataResultsIter[group][dim][s][1], 100, 10);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==2){ //CDF
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.cumulative_distribution")); //Cumulative Distribution
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],dataResultsCumDens[group][dim][s]);
			}

		}
		else if(selected==3){ //Quantile
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.quantile")); //Quantile
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],dataResultsVal[group][dim][s]);
			}
		}
		else if(selected==4){ //Iteration
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.iteration")); //Iteration
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
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

	public void updateStratSeriesColor(int s) {
		//results
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartResults.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints[s]);

		//scatter
		renderer = (XYLineAndShapeRenderer) chartScatter.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints[s]);
		//mean
		/*Color curCol=(Color) seriesPaints[s];
		int r=(int) (curCol.getRed()*0.6);
		int g=(int) (curCol.getGreen()*0.6);
		int b=(int) (curCol.getBlue()*0.6);
		Color newCol=new Color(r, g, b);
		renderer.setSeriesPaint(numStrat+s, newCol);
		 */

	}
}
