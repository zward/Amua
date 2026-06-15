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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import base.AmuaModel;
import base.RunReport;
import base.RunReportSummary;
import filters.CSVFilter;
import main.CEAHelper;
import main.Console;
import main.ConsoleTable;
import main.Constraint;
import main.DimInfo;
import main.MersenneTwisterFast;
import main.Parameter;
import markov.MarkovNode;
import markov.MarkovTrace;
import markov.MarkovTraceSummary;
import math.Interpreter;
import math.KernelSmooth;
import math.MathUtils;
import math.Numeric;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 *
 */
public class frmPSA {

	public frmPSA frmThis;
	public JFrame frmPSA;
	AmuaModel myModel;
	int numParams;
	int numConstraints;
	DefaultTableModel modelParams;
	private JTable tableParams;

	JTabbedPane tabbedPane;
	DefaultXYDataset chartDataResults, chartDataParams, chartDataScatter, chartDataCEAC, chartDataCEAF;
	JFreeChart chartResults, chartParams, chartScatter, chartCEAC, chartCEAF;
	Paint seriesPaints_Strat[], seriesPaints_Params[];
	int numSeries_Params;
	
	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	String subgroupNames[];
	
	JComboBox<String> comboParams;
	
	JComboBox<String> comboScatterType;
	JCheckBox chckbxScatterMeans;
	JComboBox<String> comboGroupScatter;
	
	String paramNames[];
	int numStrat;
	/**
	 * Excluding overall
	 */
	int numSubgroups=0;
	
	/**
	 * [Group][Outcome][Strategy][x,y][Iteration]
	 */
	double dataResultsIter[][][][][], dataResultsVal[][][][][], dataResultsCumDens[][][][][];
	double dataScatterAbs[][][][], dataScatterRel[][][][];
	/**
	 * [Group][Strategy][Outcome]
	 */
	double meanScatterAbs[][][][], meanScatterRel[][][][];
	
	String CEAnotes[][][];
	
	int paramDims[];
	/**
	 * [Parameter][Parameter Dimension][Iteration][Value]
	 */
	double dataParamsIter[][][][], dataParamsVal[][][][], dataParamsCumDens[][][][];
	
	
	/**
	 * [Strategy][x,y][WTP]
	 */
	double dataCEAC[][][], dataCEAF[][][];
	
	JList<String> listParams;
	DefaultListModel<String> listModelParams;
	private JTextField textIterations;
	int numIterations;
	JCheckBox chckbxSeed;
	private JTextField textSeed;
	JCheckBox chckbxSampleParameterSets;
	String outcome;
	
	
	//CEAC
	private JTextField textCEACMin;
	private JTextField textCEACMax;
	private JTextField textCEACIntervals;
	JComboBox comboCEACGroup;
	
	//CEAF
	private JTextField textCEAFMin;
	private JTextField textCEAFMax;
	private JTextField textCEAFIntervals;
	JComboBox comboCEAFGroup;
	
	RunReport reports[];
	

	public frmPSA(AmuaModel myModel){
		this.frmThis=this;
		this.myModel=myModel;
		myModel.getStrategies();
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmPSA = new JFrame();
			frmPSA.setTitle("Amua - "+myModel.language.analysis.getString("sens.prob_sens_analysis")); //Probabilistic Sensitivity Analysis
			frmPSA.setFont(myModel.language.font);
			frmPSA.setIconImage(Toolkit.getDefaultToolkit().getImage(frmPSA.class.getResource("/images/psa_128.png")));
			frmPSA.setBounds(100, 100, 1000, 600);
			frmPSA.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmPSA.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmPSA.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{466, 70, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelParams=new DefaultTableModel(
					new Object[][] {,},
					new String[] {myModel.language.base.getString("object.parameter"), //Parameter 
							myModel.language.base.getString("object.expression")}){ //Expression
					boolean[] columnEditables = new boolean[] {false, false};
					public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			numParams=myModel.parameters.size();
			numConstraints=myModel.constraints.size();
			paramNames=new String[numParams];
			paramDims=new int[numParams];
			for(int i=0; i<numParams; i++){
				modelParams.addRow(new Object[]{null});
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(curParam.name, i, 0);
				modelParams.setValueAt(curParam.expression, i, 1);
				paramNames[i]=curParam.name;
				paramDims[i]=1; //default to single dimension (scalar)
				if(curParam.value.isMatrix()) {
					paramDims[i]=curParam.value.nrow*curParam.value.ncol;
				}
			}

			JScrollPane scrollPaneParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneParams = new GridBagConstraints();
			gbc_scrollPaneParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneParams.gridx = 0;
			gbc_scrollPaneParams.gridy = 0;
			panel_1.add(scrollPaneParams, gbc_scrollPaneParams);
			tableParams=new JTable();
			tableParams.setRowSelectionAllowed(false);
			tableParams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableParams.setShowVerticalLines(true);
			tableParams.getTableHeader().setReorderingAllowed(false);
			tableParams.getTableHeader().setFont(myModel.language.font);
			tableParams.setFont(myModel.language.font);
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

			JButton btnRun = new JButton(myModel.language.base.getString("menu.run")); //Run
			btnRun.setFont(myModel.language.font);
			btnRun.setBounds(359, 6, 90, 28);
			panel_2.add(btnRun);

			final JButton btnExport = new JButton(myModel.language.base.getString("menu.export")); //Export
			btnExport.setFont(myModel.language.font);
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter(myModel.language));
						
						if(tabbedPane.getSelectedIndex()<3) { //Not CEAC or CEAF
							fc.setDialogTitle(myModel.language.base.getString("title.export_psa_results")); //Export PSA Results
						}
						else if(tabbedPane.getSelectedIndex()==3){
							fc.setDialogTitle(myModel.language.base.getString("title.export_ceac_results")); //Export CEAC Results
						}
						else if(tabbedPane.getSelectedIndex()==4){
							fc.setDialogTitle(myModel.language.base.getString("title.export_ceaf_results")); //Export CEAF Results
						}
						fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export
						myModel.language.setFontRecursively(fc); //set font

						int returnVal = fc.showSaveDialog(frmPSA);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							if(tabbedPane.getSelectedIndex()<3) { //Not CEAC or CEAF
								//Headers
								DimInfo info=myModel.dimInfo;
								int numDim=info.dimNames.length;
								int analysisType=info.analysisType;
								int numStrat=myModel.strategyNames.length;
								out.write("Iteration");
								out.write(",Parameters");
								for(int p=0; p<numParams; p++){
									int curDim=paramDims[p];
									if(curDim==1) { //scalar
										out.write(",\""+paramNames[p]+"\"");
									}
									else { //matrix
										Numeric val=myModel.parameters.get(p).value;
										for(int z=0; z<curDim; z++) {
											String curLbl=val.getDimLbl(z);
											out.write(",\""+(paramNames[p]+curLbl)+"\"");
										}
									}
								}
								for(int d=0; d<numDim; d++){ //EVs
									out.write(",\""+info.dimNames[d]+"\"");
									for(int s=0; s<numStrat; s++){out.write(",\""+myModel.strategyNames[s]+"\"");}
								}
								if(analysisType>0){ //CEA or BCA
									if(analysisType==1){out.write(",\""+myModel.language.analysis.getString("cea.icer")+" ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")\"");} //ICER
									else if(analysisType==2){out.write(",\""+myModel.language.analysis.getString("bca.nmb")+" ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")\"");} //NMB
									for(int s=0; s<numStrat; s++){out.write(",\""+myModel.strategyNames[s]+"\"");}
								}
								out.newLine();

								int group=0;
								if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

								//Results
								int numPoints=dataResultsIter[group][0][0][0].length;
								for(int i=0; i<numPoints; i++){
									out.write((i+1)+""); //Iteration
									//Parameters
									out.write(",");
									for(int p=0; p<numParams; p++){
										int curDim=paramDims[p];
										for(int z=0; z<curDim; z++) {
											out.write(","+dataParamsIter[p][z][1][i]);
										}
									}
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
							}
							else if(tabbedPane.getSelectedIndex()==3){ //CEAC
								//Headers
								int numStrat=myModel.strategyNames.length;
								out.write(myModel.language.analysis.getString("cea.wtp_abbr")); //WTP
								for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
								out.newLine();
								
								
								//Results
								int numPoints=dataCEAC[0][0].length;
								for(int i=0; i<numPoints; i++){
									out.write(dataCEAC[0][0][i]+""); //WTP
									//Strategies
									for(int s=0; s<numStrat; s++) {
										out.write(","+dataCEAC[s][1][i]);
									}
									out.newLine();
								}
								out.close();
							}
							else if(tabbedPane.getSelectedIndex()==4){ //CEAF
								//Headers
								int numStrat=myModel.strategyNames.length;
								out.write(myModel.language.analysis.getString("cea.wtp_abbr")); //WTP
								for(int s=0; s<numStrat; s++){out.write(","+myModel.strategyNames[s]);}
								out.newLine();
								
								
								//Results
								int numPoints=dataCEAF[0][0].length;
								for(int i=0; i<numPoints; i++){
									out.write(dataCEAF[0][0][i]+""); //WTP
									//Strategies
									for(int s=0; s<numStrat; s++) {
										out.write(","+dataCEAF[s][1][i]);
									}
									out.newLine();
								}
								out.close();
							}

							JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("info.exported")); //Exported
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmPSA, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(359, 36, 90, 28);
			panel_2.add(btnExport);

			JLabel lblIterations = new JLabel(myModel.language.analysis.getString("sim.num_iterations")+":"); //# Iterations
			lblIterations.setFont(myModel.language.font);
			lblIterations.setBounds(6, 12, 74, 16);
			panel_2.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			textIterations.setBounds(78, 6, 69, 28);
			panel_2.add(textIterations);
			textIterations.setColumns(10);

			chckbxSeed = new JCheckBox(myModel.language.analysis.getString("sim.seed")); //Seed
			chckbxSeed.setFont(myModel.language.font);
			chckbxSeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxSeed.isSelected()){textSeed.setEnabled(true);}
					else{textSeed.setEnabled(false);}
				}
			});
			chckbxSeed.setBounds(150, 11, 76, 18);
			panel_2.add(chckbxSeed);

			textSeed = new JTextField();
			textSeed.setEnabled(false);
			textSeed.setBounds(228, 6, 59, 28);
			panel_2.add(textSeed);
			textSeed.setColumns(10);
			
			chckbxSampleParameterSets = new JCheckBox(myModel.language.analysis.getString("sim.sample_parameter_sets")); //Sample parameter sets
			chckbxSampleParameterSets.setFont(myModel.language.font);
			chckbxSampleParameterSets.setEnabled(false);
			chckbxSampleParameterSets.setBounds(78, 41, 274, 18);
			panel_2.add(chckbxSampleParameterSets);
			
			if(myModel.parameterSets!=null) {
				chckbxSampleParameterSets.setEnabled(true);
			}
			

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setFont(myModel.language.font);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 1;
			gbc_tabbedPane.gridy = 0;
			frmPSA.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelResults = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("result.results"), null, panelResults, null); //Results
			GridBagLayout gbl_panelResults = new GridBagLayout();
			gbl_panelResults.columnWidths = new int[]{165, 165, 165, 0, 0};
			gbl_panelResults.rowHeights = new int[]{0, 0, 0};
			gbl_panelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panelResults.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panelResults.setLayout(gbl_panelResults);

			chartDataResults = new DefaultXYDataset();
			chartResults = ChartFactory.createScatterPlot(null, myModel.language.analysis.getString("result.value"), myModel.language.base.getString("plot.density"), //Value, Density
					chartDataResults, PlotOrientation.VERTICAL, true, false, false);
			chartResults.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartResults.getXYPlot().addDomainMarker(marker);
			chartResults.getXYPlot().addRangeMarker(marker);
			//font
			chartResults.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartResults.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartResults.getLegend().setItemFont(myModel.language.font);
			
			//get default series colors
			numStrat=myModel.getStrategies();
			seriesPaints_Strat=new Paint[numStrat];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			for(int s=0; s<numStrat; s++) {
				seriesPaints_Strat[s]=supplier.getNextPaint();
			}
			
			ChartPanel panelChartResults = new ChartPanel(chartResults,false);
			GridBagConstraints gbc_panelChartResults = new GridBagConstraints();
			gbc_panelChartResults.gridwidth = 4;
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
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartResults, chartDataResults, seriesPaints_Strat, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			comboResults = new JComboBox<String>();
			comboResults.setFont(myModel.language.font);
			comboResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setFont(myModel.language.font);
			comboDimensions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			GridBagConstraints gbc_comboDimensions = new GridBagConstraints();
			gbc_comboDimensions.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboDimensions.insets = new Insets(0, 0, 0, 5);
			gbc_comboDimensions.gridx = 0;
			gbc_comboDimensions.gridy = 1;
			panelResults.add(comboDimensions, gbc_comboDimensions);
			//comboResults.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
			String plotType[]=new String[5];
			plotType[0]=myModel.language.base.getString("plot.density"); //Density
			plotType[1]=myModel.language.base.getString("plot.histogram"); //Histogram
			plotType[2]=myModel.language.base.getString("plot.cumulative_distribution"); //Cumulative Distribution
			plotType[3]=myModel.language.base.getString("plot.quantiles"); //Quantiles
			plotType[4]=myModel.language.base.getString("plot.iteration"); //Iteration
			comboResults.setModel(new DefaultComboBoxModel<String>(plotType));
			
			GridBagConstraints gbc_comboResults = new GridBagConstraints();
			gbc_comboResults.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboResults.insets = new Insets(0, 0, 0, 5);
			gbc_comboResults.gridx = 1;
			gbc_comboResults.gridy = 1;
			panelResults.add(comboResults, gbc_comboResults);
			
			comboGroup = new JComboBox<String>();
			comboGroup.setFont(myModel.language.font);
			comboGroup.setVisible(false);
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			
			GridBagConstraints gbc_comboGroup = new GridBagConstraints();
			gbc_comboGroup.insets = new Insets(0, 0, 0, 5);
			gbc_comboGroup.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroup.gridx = 2;
			gbc_comboGroup.gridy = 1;
			panelResults.add(comboGroup, gbc_comboGroup);

			JPanel panelParams = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("object.parameters"), null, panelParams, null); //Parameters
			GridBagLayout gbl_panelParams = new GridBagLayout();
			gbl_panelParams.columnWidths = new int[]{225, 0, 0};
			gbl_panelParams.rowHeights = new int[]{0, 0};
			gbl_panelParams.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelParams.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panelParams.setLayout(gbl_panelParams);

			JPanel panel_3 = new JPanel();
			GridBagConstraints gbc_panel_3 = new GridBagConstraints();
			gbc_panel_3.insets = new Insets(0, 0, 0, 5);
			gbc_panel_3.fill = GridBagConstraints.BOTH;
			gbc_panel_3.gridx = 0;
			gbc_panel_3.gridy = 0;
			panelParams.add(panel_3, gbc_panel_3);
			GridBagLayout gbl_panel_3 = new GridBagLayout();
			gbl_panel_3.columnWidths = new int[]{0, 0, 0};
			gbl_panel_3.rowHeights = new int[]{0, 0, 0};
			gbl_panel_3.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			gbl_panel_3.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_3.setLayout(gbl_panel_3);

			JScrollPane scrollPaneSelectParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneSelectParams = new GridBagConstraints();
			gbc_scrollPaneSelectParams.gridwidth = 2;
			gbc_scrollPaneSelectParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneSelectParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneSelectParams.gridx = 0;
			gbc_scrollPaneSelectParams.gridy = 0;
			panel_3.add(scrollPaneSelectParams, gbc_scrollPaneSelectParams);

			listModelParams=new DefaultListModel<String>();
			for(int v=0; v<numParams; v++){listModelParams.addElement(paramNames[v]);}

			listParams = new JList<String>(listModelParams);
			listParams.setFont(myModel.language.font);
			scrollPaneSelectParams.setViewportView(listParams);
			int selectedIndices[]=new int[numParams];
			for(int v=0; v<numParams; v++){selectedIndices[v]=v;}
			listParams.setSelectedIndices(selectedIndices); //Select all

			JButton btnUpdateChart = new JButton(myModel.language.base.getString("plot.update_plot")); //Update Plot
			btnUpdateChart.setFont(myModel.language.font);
			btnUpdateChart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateParamChart();
				}
			});
			GridBagConstraints gbc_btnUpdateChart = new GridBagConstraints();
			gbc_btnUpdateChart.insets = new Insets(0, 0, 0, 5);
			gbc_btnUpdateChart.gridx = 0;
			gbc_btnUpdateChart.gridy = 1;
			panel_3.add(btnUpdateChart, gbc_btnUpdateChart);

			comboParams = new JComboBox<String>();
			comboParams.setFont(myModel.language.font);
			comboParams.setModel(new DefaultComboBoxModel<String>(plotType));
			GridBagConstraints gbc_comboParams = new GridBagConstraints();
			gbc_comboParams.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboParams.gridx = 1;
			gbc_comboParams.gridy = 1;
			panel_3.add(comboParams, gbc_comboParams);

			chartDataParams = new DefaultXYDataset();
			chartParams = ChartFactory.createScatterPlot(null, myModel.language.analysis.getString("result.value"), myModel.language.base.getString("plot.density"), //Value, Density
					chartDataParams, PlotOrientation.VERTICAL, true, false, false);
			chartParams.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartParams.getXYPlot().addDomainMarker(marker);
			chartParams.getXYPlot().addRangeMarker(marker);
			//font
			chartParams.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartParams.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartParams.getLegend().setItemFont(myModel.language.font);

			ChartPanel panelChartParams = new ChartPanel(chartParams,false);
			GridBagConstraints gbc_panelChartParams = new GridBagConstraints();
			gbc_panelChartParams.fill = GridBagConstraints.BOTH;
			gbc_panelChartParams.gridx = 1;
			gbc_panelChartParams.gridy = 0;
			panelParams.add(panelChartParams, gbc_panelChartParams);

			DefaultDrawingSupplier supplierParams = new DefaultDrawingSupplier();
			seriesPaints_Params=new Paint[numParams];
			for(int v=0; v<numParams; v++){
				seriesPaints_Params[v]=supplierParams.getNextPaint();
			}
			
			//pop-up menu
			popup = panelChartParams.getPopupMenu();
			JMenuItem mntmChangeColorParams = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorParams.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartParams, chartDataParams, seriesPaints_Params, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorParams, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);
			
			JPanel panelScatter = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("plot.scatter"), null, panelScatter, null); //Scatter
			tabbedPane.setEnabledAt(2, false);

			chartDataScatter = new DefaultXYDataset();
			chartScatter = ChartFactory.createScatterPlot(null, "x", "y", chartDataScatter, PlotOrientation.VERTICAL, true, false, false);
			chartScatter.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartScatter.getXYPlot().addDomainMarker(marker);
			chartScatter.getXYPlot().addRangeMarker(marker);
			//font
			chartScatter.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScatter.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScatter.getLegend().setItemFont(myModel.language.font);

			GridBagLayout gbl_panelScatter = new GridBagLayout();
			gbl_panelScatter.columnWidths = new int[]{155, 111, 156, 680, 0};
			gbl_panelScatter.rowHeights = new int[]{0, 420, 0};
			gbl_panelScatter.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panelScatter.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelScatter.setLayout(gbl_panelScatter);
			
			comboScatterType = new JComboBox<String>();
			comboScatterType.setFont(myModel.language.font);
			comboScatterType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateScatter();
				}
			});
			comboScatterType.setModel(new DefaultComboBoxModel(new String[] {
					myModel.language.base.getString("plot.absolute_magnitude"), // Absolute Magnitude
					myModel.language.base.getString("plot.relative_baseline")})); //Relative to Baseline
			GridBagConstraints gbc_comboScatterType = new GridBagConstraints();
			gbc_comboScatterType.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboScatterType.insets = new Insets(0, 0, 5, 5);
			gbc_comboScatterType.gridx = 0;
			gbc_comboScatterType.gridy = 0;
			panelScatter.add(comboScatterType, gbc_comboScatterType);
			
			comboGroupScatter = new JComboBox<String>();
			comboGroupScatter.setFont(myModel.language.font);
			comboGroupScatter.setVisible(false);
			comboGroupScatter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateScatter();
				}
			});
			
			chckbxScatterMeans = new JCheckBox(myModel.language.base.getString("plot.display_means")); //Display Means
			chckbxScatterMeans.setFont(myModel.language.font);
			chckbxScatterMeans.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateScatter();
				}
			});
			chckbxScatterMeans.setSelected(true);
			GridBagConstraints gbc_chckbxScatterMeans = new GridBagConstraints();
			gbc_chckbxScatterMeans.anchor = GridBagConstraints.WEST;
			gbc_chckbxScatterMeans.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxScatterMeans.gridx = 1;
			gbc_chckbxScatterMeans.gridy = 0;
			panelScatter.add(chckbxScatterMeans, gbc_chckbxScatterMeans);
			
			GridBagConstraints gbc_comboGroupScatter = new GridBagConstraints();
			gbc_comboGroupScatter.insets = new Insets(0, 0, 5, 5);
			gbc_comboGroupScatter.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroupScatter.gridx = 2;
			gbc_comboGroupScatter.gridy = 0;
			panelScatter.add(comboGroupScatter, gbc_comboGroupScatter);

			ChartPanel panelChartScatter = new ChartPanel(chartScatter,false);
			GridBagConstraints gbc_panelChartScatter = new GridBagConstraints();
			gbc_panelChartScatter.gridwidth = 4;
			gbc_panelChartScatter.fill = GridBagConstraints.BOTH;
			gbc_panelChartScatter.gridx = 0;
			gbc_panelChartScatter.gridy = 1;
			panelScatter.add(panelChartScatter, gbc_panelChartScatter);
			panelChartScatter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
			//pop-up menu
			popup = panelChartScatter.getPopupMenu();
			JMenuItem mntmChangeColorScatter = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorScatter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//pass results chart, not scatter (extra series for means)
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartResults, chartDataResults, seriesPaints_Strat, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorScatter, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			//CEAC *********************************
			JPanel panelCEAC = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("cea.ceac"), null, panelCEAC, myModel.language.analysis.getString("cea.ceac_full")); //CEAC, "Cost-Effectiveness Acceptability Curve"
			tabbedPane.setEnabledAt(3, false);

			chartDataCEAC = new DefaultXYDataset();
			String costDim=myModel.language.analysis.getString("cea.cost"), effectDim=myModel.language.analysis.getString("cea.effect"); //Cost, Effect
			if(myModel.dimInfo.analysisType>0) {
				costDim=myModel.dimInfo.dimNames[myModel.dimInfo.costDim];
				effectDim=myModel.dimInfo.dimNames[myModel.dimInfo.effectDim];
			}
			//Willingness-to-pay ([cost] per [effect])
			String msg = MessageFormat.format(myModel.language.analysis.getString("cea.wtp_per"), costDim, effectDim);
			chartCEAC = ChartFactory.createScatterPlot(null, msg, 
					myModel.language.analysis.getString("sens.pOptimal"), chartDataCEAC, PlotOrientation.VERTICAL, true, false, false); //p(Optimal)
			chartCEAC.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartCEAC.getXYPlot().addDomainMarker(marker);
			chartCEAC.getXYPlot().addRangeMarker(marker);
			//Add baseline WTP
			if(myModel.dimInfo.analysisType>0) {
				Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
				chartCEAC.getXYPlot().addDomainMarker(new ValueMarker(myModel.dimInfo.WTP, Color.BLACK,dashed));
			}
			//font
			chartCEAC.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartCEAC.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartCEAC.getLegend().setItemFont(myModel.language.font);

			GridBagLayout gbl_panelCEAC = new GridBagLayout();
			gbl_panelCEAC.columnWidths = new int[]{0, 0};
			gbl_panelCEAC.rowHeights = new int[]{35, 41, 0};
			gbl_panelCEAC.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelCEAC.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelCEAC.setLayout(gbl_panelCEAC);

			JPanel panelCEACHeader = new JPanel();
			panelCEACHeader.setLayout(null);
			GridBagConstraints gbc_panelCEACHeader = new GridBagConstraints();
			gbc_panelCEACHeader.insets = new Insets(0, 0, 5, 0);
			gbc_panelCEACHeader.fill = GridBagConstraints.BOTH;
			gbc_panelCEACHeader.gridx = 0;
			gbc_panelCEACHeader.gridy = 0;
			panelCEAC.add(panelCEACHeader, gbc_panelCEACHeader);

			JLabel lblCEACMin = new JLabel(myModel.language.math.getString("sum.min")+":"); //Min
			lblCEACMin.setFont(myModel.language.font.deriveFont(11f));
			lblCEACMin.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCEACMin.setBounds(0, 6, 43, 16);
			panelCEACHeader.add(lblCEACMin);

			textCEACMin = new JTextField();
			textCEACMin.setText("0");
			textCEACMin.setBounds(44, 0, 58, 28);
			panelCEACHeader.add(textCEACMin);
			textCEACMin.setColumns(10);

			JLabel lblCEACMax = new JLabel(myModel.language.math.getString("sum.max")+":"); //Max
			lblCEACMax.setFont(myModel.language.font.deriveFont(11f));
			lblCEACMax.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCEACMax.setBounds(100, 6, 43, 16);
			panelCEACHeader.add(lblCEACMax);

			textCEACMax = new JTextField();
			textCEACMax.setBounds(144, 0, 58, 28);
			panelCEACHeader.add(textCEACMax);
			textCEACMax.setColumns(10);
			textCEACMax.setText((myModel.dimInfo.WTP*3)+"");

			JLabel lblIntervals = new JLabel(myModel.language.base.getString("plot.intervals")+":"); //Intervals
			lblIntervals.setFont(myModel.language.font.deriveFont(11f));
			lblIntervals.setHorizontalAlignment(SwingConstants.RIGHT);
			lblIntervals.setBounds(200, 6, 70, 16);
			panelCEACHeader.add(lblIntervals);

			textCEACIntervals = new JTextField();
			textCEACIntervals.setText("100");
			textCEACIntervals.setBounds(270, 0, 50, 28);
			panelCEACHeader.add(textCEACIntervals);
			textCEACIntervals.setColumns(10);

			JButton btnUpdateCEAC = new JButton(myModel.language.base.getString("button.update")); //Update
			btnUpdateCEAC.setFont(myModel.language.font);
			btnUpdateCEAC.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int group=0;
					if(comboCEACGroup.isVisible()){group=comboCEACGroup.getSelectedIndex();}
					updateCEAC(group);
				}
			});
			btnUpdateCEAC.setBounds(325, 0, 100, 28);
			panelCEACHeader.add(btnUpdateCEAC);

			comboCEACGroup = new JComboBox();
			comboCEACGroup.setFont(myModel.language.font);
			comboCEACGroup.setVisible(false);
			comboCEACGroup.setBounds(428, 1, 100, 26);
			panelCEACHeader.add(comboCEACGroup);

			ChartPanel panelChartCEAC = new ChartPanel(chartCEAC,false);
			GridBagConstraints gbc_panelChartCEAC = new GridBagConstraints();
			gbc_panelChartCEAC.fill = GridBagConstraints.BOTH;
			gbc_panelChartCEAC.gridx = 0;
			gbc_panelChartCEAC.gridy = 1;
			panelCEAC.add(panelChartCEAC, gbc_panelChartCEAC);

			//pop-up menu
			popup = panelChartCEAC.getPopupMenu();
			JMenuItem mntmChangeColorCEAC = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorCEAC.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartCEAC, chartDataCEAC, seriesPaints_Strat, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorCEAC, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			// CEAF ******************************************************
			JPanel panelCEAF = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("cea.ceaf"), null, panelCEAF, myModel.language.analysis.getString("cea.ceaf_full")); //CEAF, Cost-Effectiveness Acceptability Frontier
			tabbedPane.setEnabledAt(4, false);

			chartDataCEAF = new DefaultXYDataset();
			costDim=myModel.language.analysis.getString("cea.cost"); effectDim=myModel.language.analysis.getString("cea.effect"); //Cost, Effect
			if(myModel.dimInfo.analysisType>0) {
				costDim=myModel.dimInfo.dimNames[myModel.dimInfo.costDim];
				effectDim=myModel.dimInfo.dimNames[myModel.dimInfo.effectDim];
			}
			//Willingness-to-pay ([cost] per [effect])
			msg = MessageFormat.format(myModel.language.analysis.getString("cea.wtp_per"), costDim, effectDim);
			chartCEAF = ChartFactory.createScatterPlot(null, msg, 
					myModel.language.analysis.getString("sens.pOptimal"), chartDataCEAF, PlotOrientation.VERTICAL, true, false, false); //p(Optimal)
			chartCEAF.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartCEAF.getXYPlot().addDomainMarker(marker);
			chartCEAF.getXYPlot().addRangeMarker(marker);
			//Add baseline WTP
			if(myModel.dimInfo.analysisType>0) {
				Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
				chartCEAF.getXYPlot().addDomainMarker(new ValueMarker(myModel.dimInfo.WTP, Color.BLACK,dashed));
			}
			//font
			chartCEAF.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartCEAF.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartCEAF.getLegend().setItemFont(myModel.language.font);

			GridBagLayout gbl_panelCEAF = new GridBagLayout();
			gbl_panelCEAF.columnWidths = new int[]{0, 0};
			gbl_panelCEAF.rowHeights = new int[]{35, 41, 0};
			gbl_panelCEAF.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelCEAF.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelCEAF.setLayout(gbl_panelCEAF);

			JPanel panelCEAFHeader = new JPanel();
			panelCEAFHeader.setLayout(null);
			GridBagConstraints gbc_panelCEAFHeader = new GridBagConstraints();
			gbc_panelCEAFHeader.insets = new Insets(0, 0, 5, 0);
			gbc_panelCEAFHeader.fill = GridBagConstraints.BOTH;
			gbc_panelCEAFHeader.gridx = 0;
			gbc_panelCEAFHeader.gridy = 0;
			panelCEAF.add(panelCEAFHeader, gbc_panelCEAFHeader);

			JLabel lblCEAFMin = new JLabel(myModel.language.math.getString("sum.min")+":"); //Min
			lblCEAFMin.setFont(myModel.language.font.deriveFont(11f));
			lblCEAFMin.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCEAFMin.setBounds(0, 6, 43, 16);
			panelCEAFHeader.add(lblCEAFMin);

			textCEAFMin = new JTextField();
			textCEAFMin.setText("0");
			textCEAFMin.setBounds(44, 0, 58, 28);
			panelCEAFHeader.add(textCEAFMin);
			textCEAFMin.setColumns(10);

			JLabel lblCEAFMax = new JLabel(myModel.language.math.getString("sum.max")+":"); //Max
			lblCEAFMax.setFont(myModel.language.font.deriveFont(11f));
			lblCEAFMax.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCEAFMax.setBounds(100, 6, 43, 16);
			panelCEAFHeader.add(lblCEAFMax);

			textCEAFMax = new JTextField();
			textCEAFMax.setBounds(144, 0, 58, 28);
			panelCEAFHeader.add(textCEAFMax);
			textCEAFMax.setColumns(10);
			textCEAFMax.setText((myModel.dimInfo.WTP*3)+"");

			JLabel lblIntervalsCEAF = new JLabel(myModel.language.base.getString("plot.intervals")+":"); //Intervals
			lblIntervalsCEAF.setFont(myModel.language.font.deriveFont(11f));
			lblIntervalsCEAF.setHorizontalAlignment(SwingConstants.RIGHT);
			lblIntervalsCEAF.setBounds(200, 6, 70, 16);
			panelCEAFHeader.add(lblIntervalsCEAF);

			textCEAFIntervals = new JTextField();
			textCEAFIntervals.setText("100");
			textCEAFIntervals.setBounds(270, 0, 50, 28);
			panelCEAFHeader.add(textCEAFIntervals);
			textCEAFIntervals.setColumns(10);

			JButton btnUpdateCEAF = new JButton(myModel.language.base.getString("button.update")); //Update
			btnUpdateCEAF.setFont(myModel.language.font);
			btnUpdateCEAF.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int group=0;
					if(comboCEAFGroup.isVisible()){group=comboCEAFGroup.getSelectedIndex();}
					updateCEAF(group);
				}
			});
			btnUpdateCEAF.setBounds(325, 0, 100, 28);
			panelCEAFHeader.add(btnUpdateCEAF);

			comboCEAFGroup = new JComboBox();
			comboCEAFGroup.setFont(myModel.language.font);
			comboCEAFGroup.setVisible(false);
			comboCEAFGroup.setBounds(428, 1, 100, 26);
			panelCEAFHeader.add(comboCEAFGroup);

			ChartPanel panelChartCEAF = new ChartPanel(chartCEAF,false);
			GridBagConstraints gbc_panelChartCEAF = new GridBagConstraints();
			gbc_panelChartCEAF.fill = GridBagConstraints.BOTH;
			gbc_panelChartCEAF.gridx = 0;
			gbc_panelChartCEAF.gridy = 1;
			panelCEAF.add(panelChartCEAF, gbc_panelChartCEAF);

			//pop-up menu
			popup = panelChartCEAF.getPopupMenu();
			JMenuItem mntmChangeColorCEAF = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorCEAF.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartCEAF, chartDataCEAF, seriesPaints_Strat, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorCEAF, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			//subgroup combos **************
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				subgroupNames=new String[numGroups+1];
				subgroupNames[0]=myModel.language.analysis.getString("result.overall"); //Overall
				for(int i=0; i<numGroups; i++){subgroupNames[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
				comboGroupScatter.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroupScatter.setVisible(true);
				comboCEACGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboCEACGroup.setVisible(true);
				comboCEAFGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboCEAFGroup.setVisible(true);
			}


			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//final ProgressMonitor progress=new ProgressMonitor(frmPSA, myModel.language.analysis.getString("gen.psa"), myModel.language.message.getString("info.sampling"), 0, 100); //PSA, Sampling
					final frmProgressMonitor progress=new frmProgressMonitor(frmPSA, myModel.language.analysis.getString("gen.psa"), myModel.language.message.getString("info.sampling"), 0, 100, myModel.language); ///PSA, Sampling
					SwingUtilities.invokeLater(progress::show);  //dialog is created/shown on EDT

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								btnRun.setEnabled(false);
								frmPSA.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								tabbedPane.setEnabledAt(2, false);
								tabbedPane.setEnabledAt(3, false);
								tabbedPane.setEnabledAt(4, false);

								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();

								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.base_case")); //Errors in base case model!
								}
								else{
									boolean cancelled=false;

									myModel.sampleParam=true;
									myModel.generatorParam=new MersenneTwisterFast();
									if(myModel.curGenerator==null){
										myModel.curGenerator=new MersenneTwisterFast[1];
									}
									myModel.curGenerator[0]=myModel.generatorParam;
									if(chckbxSeed.isSelected()){
										int seed=Integer.parseInt(textSeed.getText());
										myModel.generatorParam.setSeed(seed);
									}
									myModel.simParamSets=false; //turn off use parameter sets (no looping through)
									boolean sampleParamSets=chckbxSampleParameterSets.isSelected();
									int numSets=-1;
									if(sampleParamSets) {
										numSets=myModel.parameterSets.length;
									}

									numIterations=Integer.parseInt(textIterations.getText().replaceAll(",", ""));
									progress.setMaximum(numIterations);
									//progress.setMillisToDecideToPopup(0);
									//progress.setMillisToPopup(0);

									numStrat=myModel.getStrategies();
									int numOutcomes=comboDimensions.getItemCount();
									int numDim=myModel.dimInfo.dimNames.length;

									if(myModel.simType==1 && myModel.reportSubgroups){numSubgroups=myModel.subgroupNames.size();}

									int analysisType=myModel.dimInfo.analysisType;
									if(analysisType==1){CEAnotes=new String[1+numSubgroups][numStrat][numIterations];} //CEA
									else{CEAnotes=null;}

									dataResultsIter=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];
									dataResultsVal=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];
									dataResultsCumDens=new double[1+numSubgroups][numOutcomes][numStrat][2][numIterations];

									dataParamsIter=new double[numParams][][][];
									dataParamsVal=new double[numParams][][][];
									dataParamsCumDens=new double[numParams][][][];
									for(int p=0; p<numParams; p++) {
										int curDim=paramDims[p];
										dataParamsIter[p]=new double[curDim][2][numIterations];
										dataParamsVal[p]=new double[curDim][2][numIterations];
										dataParamsCumDens[p]=new double[curDim][2][numIterations];
									}

									dataScatterAbs=new double[1+numSubgroups][numStrat][2][numIterations];
									dataScatterRel=new double[1+numSubgroups][numStrat][2][numIterations];
									meanScatterAbs=new double[1+numSubgroups][numStrat][2][1];
									meanScatterRel=new double[1+numSubgroups][numStrat][2][1];

									dataCEAC=new double[numStrat][][];
									dataCEAF=new double[numStrat][][];

									//Get orig values for all parameters
									Numeric origValues[]=new Numeric[numParams];
									for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters
										origValues[v]=myModel.parameters.get(v).value.copy();
									}

									//Parse constraints
									for(int c=0; c<numConstraints; c++){
										myModel.constraints.get(c).parseConstraints();
									}

									MarkovTrace traces[][][]=null;
									ArrayList<MarkovNode> chainRoots=null;
									int numChains = 0;
									boolean origShowTrace=true;
									if(myModel.type==1){
										//get number of chains
										chainRoots=new ArrayList<MarkovNode>();
										for(int n=0; n<myModel.markov.nodes.size(); n++){
											MarkovNode curNode=myModel.markov.nodes.get(n);
											if(curNode.type==1){chainRoots.add(curNode);}
										}
										numChains=chainRoots.size();
										traces=new MarkovTrace[numChains][numSubgroups+1][numIterations];
										origShowTrace=myModel.markov.showTrace;
										myModel.markov.showTrace=false;
									}

									long startTime=System.currentTimeMillis();

									reports=new RunReport[numIterations];

									for(int n=0; n<numIterations; n++){
										//Update progress
										double prog=((n)/(numIterations*1.0))*100;
										long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
										remTime=(long) (remTime*(100-prog));
										remTime=remTime/1000;
										String seconds = Integer.toString((int)(remTime % 60));
										String minutes = Integer.toString((int)(remTime/60));
										if(seconds.length()<2){seconds="0"+seconds;}
										if(minutes.length()<2){minutes="0"+minutes;}
										progress.setProgress(n+1);
										if(n>0) {
											progress.setNote(myModel.language.message.getString("info.time_left")+": "+minutes+":"+seconds); //Time left
										}

										//Sample parameters
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
												if(curParam.locked==false) {
													curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true, myModel.language);
													curParam.locked=true;
												}
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
												ArrayList<String> errors=myModel.parseModel();
												if(errors.size()!=0) {
													validParams=false;
												}
											}
										} //end sample params
										if(sampleParamSets) {
											int curSet=myModel.generatorParam.nextInt(numSets);
											myModel.parameterSets[curSet].setParameters(myModel);
										}

										for(int v=0; v<numParams; v++){ //Record value
											int curDim=paramDims[v];
											if(curDim==1) { //scalar
												dataParamsIter[v][0][0][n]=n; dataParamsVal[v][0][0][n]=n;
												try{
													dataParamsIter[v][0][1][n]=myModel.parameters.get(v).value.getDouble(myModel.language);
												} catch(Exception e){
													dataParamsIter[v][0][1][n]=Double.NaN;
												}
												dataParamsVal[v][0][1][n]=dataParamsIter[v][0][1][n];
											}
											else { //matrix
												Numeric val=myModel.parameters.get(v).value;
												int z=0;
												for(int i=0; i<val.nrow; i++) {
													for(int j=0; j<val.ncol; j++) {
														dataParamsIter[v][z][0][n]=n; dataParamsVal[v][z][0][n]=n;
														dataParamsIter[v][z][1][n]=val.matrix[i][j];
														dataParamsVal[v][z][1][n]=val.matrix[i][j];
														z++;
													}
												}
											}
										} 

										//Run model
										myModel.curGenerator=myModel.generatorVar;
										reports[n]=myModel.runModel(null, false);

										if(myModel.type==1){ //Markov model
											for(int c=0; c<numChains; c++){
												traces[c][0][n]=reports[n].markovTraces.get(c); //overall
												for(int g=0; g<numSubgroups; g++){
													traces[c][g+1][n]=reports[n].markovTracesGroup[g].get(c);
												}
											}
										}

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
													curOutcome=myModel.getSubgroupEV(g, s, d);
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
															if(myModel.dimInfo.objective==1) { //minimize
																dataScatterRel[g][origStrat][0][n]*=-1; //flip sign
															}
															dataScatterRel[g][origStrat][1][n]=cost-baseCost;
															//update means
															meanScatterAbs[g][origStrat][0][0]+=dataScatterAbs[g][origStrat][0][n];
															meanScatterAbs[g][origStrat][1][0]+=dataScatterAbs[g][origStrat][1][n];
															meanScatterRel[g][origStrat][0][0]+=dataScatterRel[g][origStrat][0][n];
															meanScatterRel[g][origStrat][1][0]+=dataScatterRel[g][origStrat][1][n];
														}
													}
												}
											}
											else if(analysisType==2){ //BCA
												for(int g=0; g<numSubgroups+1; g++){
													Object table[][]=new CEAHelper().calculateNMB(myModel,g-1,true);
													//use first row as baseline
													//int baseIndex=myModel.getStrategyIndex(myModel.dimInfo.baseScenario);
													int baseIndex=0;
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
														double benefit=(double) table[s][2];
														double cost=(double) table[s][3];
														dataScatterAbs[g][origStrat][0][n]=benefit;
														dataScatterAbs[g][origStrat][1][n]=cost;
														double baseBenefit=(double) table[baseRow][2]; 
														double baseCost=(double) table[baseRow][3];
														dataScatterRel[g][origStrat][0][n]=benefit-baseBenefit;
														if(myModel.dimInfo.objective==1) { //minimize
															dataScatterRel[g][origStrat][0][n]*=-1; //flip sign
														}
														dataScatterRel[g][origStrat][1][n]=cost-baseCost;
														//update means
														meanScatterAbs[g][origStrat][0][0]+=dataScatterAbs[g][origStrat][0][n];
														meanScatterAbs[g][origStrat][1][0]+=dataScatterAbs[g][origStrat][1][n];
														meanScatterRel[g][origStrat][0][0]+=dataScatterRel[g][origStrat][0][n];
														meanScatterRel[g][origStrat][1][0]+=dataScatterRel[g][origStrat][1][n];
													}
												}
											}
										}

										if(progress.isCanceled()){  //End loop
											n=numIterations;
											cancelled=true;
										}
									}

									//Reset all parameters
									myModel.sampleParam=false;
									for(int v=0; v<numParams; v++){ //Reset 'locked' for all parameter and orig values
										Parameter curParam=myModel.parameters.get(v);
										curParam.locked=false;
										curParam.value=origValues[v];
									}
									myModel.validateModelObjects();

									if(myModel.type==1){
										myModel.markov.showTrace=origShowTrace;
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
										for(int v=0; v<numParams; v++){
											int curDim=paramDims[v];
											for(int z=0; z<curDim; z++) {
												Arrays.sort(dataParamsVal[v][z][1]);
												for(int n=0; n<numIterations; n++){
													dataParamsVal[v][z][0][n]=n/(numIterations*1.0);
													dataParamsCumDens[v][z][0][n]=dataParamsVal[v][z][1][n];
													dataParamsCumDens[v][z][1][n]=dataParamsVal[v][z][0][n];
												}
											}
										}

										//Update results chart
										updateResultsChart();

										//Update param chart
										XYPlot plotParams = chartParams.getXYPlot();
										XYLineAndShapeRenderer rendererParams = new XYLineAndShapeRenderer(true,false);
										//DefaultDrawingSupplier supplierParams = new DefaultDrawingSupplier();
										for(int v=0; v<numParams; v++){
											rendererParams.setSeriesPaint(v, seriesPaints_Params[v]);
										}
										plotParams.setRenderer(rendererParams);
										updateParamChart();

										//Update scatter chart and CEAC
										if(analysisType>0){
											//calc mean
											for(int g=0; g<(1+numSubgroups); g++) {
												for(int s=0; s<numStrat; s++) {
													for(int i=0; i<2; i++) { //outcome
														meanScatterAbs[g][s][i][0]/=(numIterations*1.0);
														meanScatterRel[g][s][i][0]/=(numIterations*1.0);
													}
												}
											}

											tabbedPane.setEnabledAt(2, true);
											XYPlot plotScatter = chartScatter.getXYPlot();
											XYLineAndShapeRenderer rendererScatter = new XYLineAndShapeRenderer(false,true);
											rendererScatter.setDrawOutlines(true);
											rendererScatter.setUseOutlinePaint(true);
											rendererScatter.setBaseShapesFilled(true);
											//DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											//Paint colours[]=new Paint[numStrat];
											//iterations
											Shape dot=new Ellipse2D.Double(-2,-2,4,4);
											for(int s=0; s<numStrat; s++){
												//colours[s]=supplier.getNextPaint();
												rendererScatter.setSeriesPaint(s, seriesPaints_Strat[s]);
												rendererScatter.setSeriesOutlinePaint(s, null);
												rendererScatter.setSeriesOutlineStroke(s, new BasicStroke(0.0f));
												rendererScatter.setSeriesShape(s, dot);
											}
											//means
											//Shape mean=new Rectangle2D.Double(-5,-5,10,10);
											Shape mean=new Ellipse2D.Double(-5,-5,10,10);
											for(int s=0; s<numStrat; s++){
												Color curCol=(Color) seriesPaints_Strat[s];
												int r=(int) (curCol.getRed()*0.6);
												int g=(int) (curCol.getGreen()*0.6);
												int b=(int) (curCol.getBlue()*0.6);
												Color newCol=new Color(r, g, b);
												//rendererScatter.setSeriesPaint(numStrat+s, colours[s]);
												rendererScatter.setSeriesPaint(numStrat+s, newCol);
												rendererScatter.setSeriesShape(numStrat+s, mean);
												rendererScatter.setSeriesOutlinePaint(numStrat+s, Color.LIGHT_GRAY);
												rendererScatter.setSeriesOutlineStroke(numStrat+s, new BasicStroke(1.5f));
												rendererScatter.setSeriesVisibleInLegend(numStrat+s, Boolean.FALSE);
											}

											plotScatter.setRenderer(rendererScatter);
											plotScatter.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
											updateScatter();

											//CEAC
											tabbedPane.setEnabledAt(3, true);
											if(comboCEACGroup.isVisible()){comboCEACGroup.setSelectedIndex(0);}
											updateCEAC(0);

											//CEAF
											tabbedPane.setEnabledAt(4, true);
											if(comboCEAFGroup.isVisible()){comboCEAFGroup.setSelectedIndex(0);}
											updateCEAF(0);

										}
										btnExport.setEnabled(true);

										//Get trace summary
										if(myModel.type==1 && myModel.markov.showTrace){
											if(myModel.markov.compileTraces==false) {
												//get mean and bounds of results
												for(int c=0; c<numChains; c++){
													MarkovTraceSummary traceSummaries[]=new MarkovTraceSummary[numSubgroups+1];
													for(int g=0; g<numSubgroups+1; g++){
														traceSummaries[g]=new MarkovTraceSummary(traces[c][g]);
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
										console.print(myModel.language.analysis.getString("sens.psa_iterations")+":\t"+numIterations+"\n\n"); //PSA Iterations
										boolean colTypes[]=new boolean[]{false,false,true,true,true}; //is column number (true), or text (false)
										ConsoleTable curTable=new ConsoleTable(console,colTypes);
										String headers[]=new String[]{
												myModel.language.analysis.getString("gen.strategy"), //Strategy
												myModel.language.analysis.getString("result.outcome"), //Outcome
												myModel.language.math.getString("sum.mean"), //Mean
												myModel.language.math.getString("sum.95_lb"), //95% LB
												myModel.language.math.getString("sum.95_ub")}; //95% UB
										curTable.addRow(headers);
										//strategy results - overall
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

										if(myModel.dimInfo.analysisType==1) { //CEA - get mean ICERS
											printCEAResults(console, 0);
										}
										else if(myModel.dimInfo.analysisType==2) { //BCA - get mean NMB
											printBCAResults(console, 0);
										}

										//subgroups
										for(int g=0; g<numSubgroups; g++){
											console.print("\n"+myModel.language.analysis.getString("result.subgroup_results")+": "+myModel.subgroupNames.get(g)+"\n"); //Subgroup Results
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

											if(myModel.dimInfo.analysisType==1) { //CEA - get mean ICERS
												printCEAResults(console, g+1);
											}
											else if(myModel.dimInfo.analysisType==2) { //BCA - get mean NMB
												printBCAResults(console, g+1);
											}


										}

										if(myModel.simType==1 && myModel.displayIndResults==true){
											console.print("\n"+myModel.language.analysis.getString("result.individual_level_results")+":\n"); //Individual-level Results
											RunReportSummary summary=new RunReportSummary(reports);
											for(int s=0; s<numStrat; s++){
												console.print(myModel.language.analysis.getString("gen.strategy")+": "+myModel.strategyNames[s]+"\n"); //Strategy
												summary.microStatsSummary[s].printSummary(console);
											}

											//subgroups



										}
										console.newLine();
									}
									progress.close();
								}
								btnRun.setEnabled(true);
								frmPSA.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

							} catch (Exception e) {
								e.printStackTrace();
								btnRun.setEnabled(true);
								frmPSA.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								JOptionPane.showMessageDialog(frmPSA, e.getMessage());
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
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){
				chartDataResults.removeSeries(myModel.strategyNames[s]);
			}
		}
		XYPlot plotResults = chartResults.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, seriesPaints_Strat[s]);
		}
		plotResults.setRenderer(rendererResults);
		
		int selected=comboResults.getSelectedIndex();
		int numStrat=myModel.strategyNames.length;
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataResults.removeSeries(myModel.strategyNames[s]);}
		}
		
		int group=0; //overall
		if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}
				
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
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); // Value
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

	public void updateParamChart(){
		int selected=comboParams.getSelectedIndex();
		while(chartDataParams.getSeriesCount()>0){
			chartDataParams.removeSeries(chartDataParams.getSeriesKey(0));
		}
		
		int numSeries=listParams.getSelectedIndices().length;
		if(numSeries>0 && numSeries!=numSeries_Params) { //reset series colours
			numSeries_Params=numSeries;
			seriesPaints_Params=new Paint[numSeries];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartParams.getXYPlot().getRenderer();
			for(int s=0; s<numSeries; s++) {
				seriesPaints_Params[s]=supplier.getNextPaint();
				renderer.setSeriesPaint(s, seriesPaints_Params[s]);
			}
		}
		
		if(selected==0){ //Density
			chartParams.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartParams.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.density")); //Density
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						double kde[][]=KernelSmooth.density(dataParamsIter[v][0][1], 100);
						chartDataParams.addSeries(paramNames[v],kde);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double kde[][]=KernelSmooth.density(dataParamsIter[v][z][1], 100);
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, kde);
						}
					}
				}
			}
		}
		else if(selected==1){ //Histogram
			chartParams.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartParams.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.frequency")); //Frequency
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						double hist[][]=KernelSmooth.histogram(dataParamsIter[v][0][1], 100, 10);
						chartDataParams.addSeries(paramNames[v],hist);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double hist[][]=KernelSmooth.histogram(dataParamsIter[v][z][1], 100, 10);
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, hist);
						}
					}
				}
			}
		}
		else if(selected==2){ //CDF
			chartParams.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartParams.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.cumulative_distribution")); //Cumulative Distribution
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsCumDens[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsCumDens[v][z]);
						}
					}
				}
			}
		}
		else if(selected==3){ //Quantile
			chartParams.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.quantile")); //Quantile
			chartParams.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsVal[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsVal[v][z]);
						}
					}
				}
			}
		}
		else if(selected==4){ //Iteration
			chartParams.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.iteration")); //Iteration
			chartParams.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsIter[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsIter[v][z]);
						}
					}
				}
			}
		}
	}

	private void updateScatter(){
		int type=comboScatterType.getSelectedIndex();
		boolean showMeans=chckbxScatterMeans.isSelected();
		int numStrat=myModel.strategyNames.length;
		int group=0; //overall
		if(comboGroupScatter.isVisible()){group=comboGroupScatter.getSelectedIndex();}
				
		if(dataScatterAbs!=null) { //dataset has been created

			DimInfo info=myModel.dimInfo;
			while(chartDataScatter.getSeriesCount()>0) {
				chartDataScatter.removeSeries(chartDataScatter.getSeriesKey(0));
			}
			if(type==0){ //absolute
				chartScatter.getXYPlot().getRangeAxis().setLabel(info.dimNames[info.costDim]);
				chartScatter.getXYPlot().getDomainAxis().setLabel(info.dimNames[info.effectDim]);
				//iterations
				for(int s=0; s<numStrat; s++){
					chartDataScatter.addSeries(myModel.strategyNames[s],dataScatterAbs[group][s]);
				}
				if(showMeans) {
					//means
					for(int s=0; s<numStrat; s++){
						chartDataScatter.addSeries("Mean-"+myModel.strategyNames[s],meanScatterAbs[group][s]);
					}
				}
			}
			else if(type==1){ //relative to baseline
				chartScatter.getXYPlot().getRangeAxis().setLabel("∆ "+info.dimNames[info.costDim]);
				chartScatter.getXYPlot().getDomainAxis().setLabel("∆ "+info.dimNames[info.effectDim]);
				//iterations
				for(int s=0; s<numStrat; s++){
					chartDataScatter.addSeries(myModel.strategyNames[s],dataScatterRel[group][s]);
				}
				if(showMeans) {
					//means
					for(int s=0; s<numStrat; s++){
						chartDataScatter.addSeries("Mean-"+myModel.strategyNames[s],meanScatterRel[group][s]);
					}
				}
			}
		}
	}
	
	private void updateCEAC(int g) {
		double minWTP=0, maxWTP=myModel.dimInfo.WTP*3;
		try {
			minWTP=Double.parseDouble(textCEACMin.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_min")); //Please enter a valid min!
		}
		try {
			maxWTP=Double.parseDouble(textCEACMax.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_max")); //Please enter a valid max!
		}
		int numIntervals=100;
		try {
			numIntervals=Integer.parseInt(textCEACIntervals.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_intervals")); //Please enter a valid number of intervals!
		}
		double step=(maxWTP-minWTP)/(numIntervals*1.0);
		
		int costDim=myModel.dimInfo.costDim;
		int effectDim=myModel.dimInfo.effectDim;
		int objSign=1;
		if(myModel.dimInfo.objective==1) { //minimize
			objSign=-1;
		}
				
		//calculate 
		for(int s=0; s<numStrat; s++) {
			dataCEAC[s]=new double[2][numIntervals+1];
		}
		for(int w=0; w<=numIntervals; w++) {
			double curWTP=minWTP+(step*w);
			int numBest[]=new int[numStrat]; //# of times strategy is best
			for(int i=0; i<numIterations; i++) {
				double maxNMB=Double.NEGATIVE_INFINITY;
				int maxIndex=-1;
				for(int s=0; s<numStrat; s++) {
					double cost=dataResultsIter[g][costDim][s][1][i];
					double effect=dataResultsIter[g][effectDim][s][1][i]*objSign;
					double curNMB=(effect*curWTP)-cost;
					if(curNMB>maxNMB) {
						maxNMB=curNMB;
						maxIndex=s;
					}
				}
				numBest[maxIndex]++;
			}
			for(int s=0; s<numStrat; s++) {
				dataCEAC[s][0][w]=curWTP;
				dataCEAC[s][1][w]=numBest[s]/(numIterations*1.0);
			}
		}
		
		//plot
		if(chartDataCEAC.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataCEAC.removeSeries(myModel.strategyNames[s]);}
		}	
		XYPlot plotResults = chartCEAC.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, seriesPaints_Strat[s]);
		}
		plotResults.setRenderer(rendererResults);
		
		for(int s=0; s<numStrat; s++){
			chartDataCEAC.addSeries(myModel.strategyNames[s],dataCEAC[s]);
		}
	}
	
	private void updateCEAF(int g) {
		double minWTP=0, maxWTP=myModel.dimInfo.WTP*3;
		try {
			minWTP=Double.parseDouble(textCEAFMin.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_min")); //Please enter a valid min!
		}
		try {
			maxWTP=Double.parseDouble(textCEAFMax.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_max")); //Please enter a valid max!
		}
		int numIntervals=100;
		try {
			numIntervals=Integer.parseInt(textCEAFIntervals.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, myModel.language.message.getString("err.valid_intervals")); //Please enter a valid number of intervals!
		}
		double step=(maxWTP-minWTP)/(numIntervals*1.0);
		
		int costDim=myModel.dimInfo.costDim;
		int effectDim=myModel.dimInfo.effectDim;
		int objSign=1;
		if(myModel.dimInfo.objective==1) { //minimize
			objSign=-1;
		}
				
		//calculate 
		for(int s=0; s<numStrat; s++) {
			dataCEAF[s]=new double[2][numIntervals+1];
		}
		for(int w=0; w<=numIntervals; w++) {
			double curWTP=minWTP+(step*w);
			
			double meanNMB[]=new double[numStrat];
			int numBest[]=new int[numStrat]; //# of times strategy is the best
			for(int i=0; i<numIterations; i++) {
				double maxNMB=Double.NEGATIVE_INFINITY;
				int maxIndex=-1;
				for(int s=0; s<numStrat; s++) {
					double cost=dataResultsIter[g][costDim][s][1][i];
					double effect=dataResultsIter[g][effectDim][s][1][i]*objSign;
					double curNMB=(effect*curWTP)-cost;
					meanNMB[s]+=curNMB;
					if(curNMB>maxNMB) {
						maxNMB=curNMB;
						maxIndex=s;
					}
				}
				numBest[maxIndex]++;
			}
			//find optimal strategy (E[NMB]) at current WTP
			double maxMeanNMB=Double.NEGATIVE_INFINITY;
			int bestStrat=-1;
			for(int s=0; s<numStrat; s++) {
				double curMean=meanNMB[s]/(numIterations*1.0);
				if(curMean>maxMeanNMB) {
					maxMeanNMB=curMean;
					bestStrat=s;
				}
			}
			//update display prob
			for(int s=0; s<numStrat; s++) {
				dataCEAF[s][0][w]=curWTP;
				if(s==bestStrat) {
					dataCEAF[s][1][w]=numBest[s]/(numIterations*1.0);
				}
				else {
					dataCEAF[s][1][w]=Double.NaN; //don't display
				}
			}
		}
		
		//plot
		if(chartDataCEAF.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataCEAF.removeSeries(myModel.strategyNames[s]);}
		}	
		XYPlot plotResults = chartCEAF.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, seriesPaints_Strat[s]);
		}
		plotResults.setRenderer(rendererResults);
		
		for(int s=0; s<numStrat; s++){
			chartDataCEAF.addSeries(myModel.strategyNames[s],dataCEAF[s]);
		}
	}
	
	public void updateStratSeriesColor(int s) {
		//results
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartResults.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints_Strat[s]);
		
		//scatter
		renderer = (XYLineAndShapeRenderer) chartScatter.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints_Strat[s]);
		//mean
		Color curCol=(Color) seriesPaints_Strat[s];
		int r=(int) (curCol.getRed()*0.6);
		int g=(int) (curCol.getGreen()*0.6);
		int b=(int) (curCol.getBlue()*0.6);
		Color newCol=new Color(r, g, b);
		renderer.setSeriesPaint(numStrat+s, newCol);
		
		//ceac
		renderer = (XYLineAndShapeRenderer) chartCEAC.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints_Strat[s]);
		
		//ceaf
		renderer = (XYLineAndShapeRenderer) chartCEAF.getXYPlot().getRenderer();
		renderer.setSeriesPaint(s, seriesPaints_Strat[s]);
	}
	
	private void printCEAResults(Console console, int group) {
		CEAHelper cea=new CEAHelper();
		//get mean results
		cea.numStrat=numStrat;
		cea.costs=new double[numStrat]; cea.effects=new double[numStrat];
		for(int s=0; s<numStrat; s++) {
			for(int n=0; n<numIterations; n++) {
				cea.costs[s]+=dataScatterAbs[group][s][1][n];
				cea.effects[s]+=dataScatterAbs[group][s][0][n];
			}
			cea.costs[s]/=(numIterations*1.0);
			cea.effects[s]/=(numIterations*1.0);
		}
		Object table[][]=cea.calculateICERs(myModel, -1, false);
		
		//Print results
		DimInfo dimInfo=myModel.dimInfo;
		console.print("\n"+myModel.language.analysis.getString("cea.cea_results")+" ("+myModel.language.analysis.getString("cea.ratio_of_means")+"):\n"); //CEA Results (Ratio of Means)
		boolean colTypes[]=new boolean[]{false,true,true,true,false}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{
				myModel.language.analysis.getString("gen.strategy"), //Strategy
				dimInfo.dimNames[dimInfo.costDim],
				dimInfo.dimNames[dimInfo.effectDim],
				myModel.language.analysis.getString("cea.icer"), //ICER
				myModel.language.base.getString("menu.notes")}; //Notes
		curTable.addRow(headers);
		for(int s=0; s<numStrat; s++){
			String cost=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.costDim])+"";
			String effect=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.effectDim])+"";
			String icer="";
			double nIcer=(double)table[s][4];
			if(Double.isNaN(nIcer)) {icer="---";}
			else {
				icer=MathUtils.round(nIcer,dimInfo.decimals[dimInfo.costDim])+"";
			}
			String row[]=new String[]{table[s][1]+"",cost,effect,icer,table[s][5]+""};
			curTable.addRow(row);
		}
		curTable.print();
	}
	
	private void printBCAResults(Console console, int group) {
		CEAHelper cea=new CEAHelper();
		//get mean results
		cea.numStrat=numStrat;
		cea.costs=new double[numStrat]; cea.effects=new double[numStrat];
		for(int s=0; s<numStrat; s++) {
			for(int n=0; n<numIterations; n++) {
				cea.costs[s]+=dataScatterAbs[group][s][1][n];
				cea.effects[s]+=dataScatterAbs[group][s][0][n];
			}
			cea.costs[s]/=(numIterations*1.0);
			cea.effects[s]/=(numIterations*1.0);
		}
		Object table[][]=cea.calculateNMB(myModel, -1, false);
		
		//Print results
		DimInfo dimInfo=myModel.dimInfo;
		console.print("\n"+myModel.language.analysis.getString("bca.bca_results")+" ("+myModel.language.math.getString("sum.mean")+"):\n"); //BCA Results (Mean)
		boolean colTypes[]=new boolean[]{false,true,true,true}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{
				myModel.language.analysis.getString("gen.strategy"), //Strategy
				dimInfo.dimNames[dimInfo.effectDim],
				dimInfo.dimNames[dimInfo.costDim],
				myModel.language.analysis.getString("bca.nmb")}; //NMB
		curTable.addRow(headers);
		for(int s=0; s<numStrat; s++){
			String effect=MathUtils.round((double)table[s][2],dimInfo.decimals[dimInfo.effectDim])+"";
			String cost=MathUtils.round((double)table[s][3],dimInfo.decimals[dimInfo.costDim])+"";
			String nmb=MathUtils.round((double)table[s][4],dimInfo.decimals[dimInfo.costDim])+"";
			String row[]=new String[]{table[s][1]+"",effect,cost,nmb};
			curTable.addRow(row);
		}
		curTable.print();
	}
}
