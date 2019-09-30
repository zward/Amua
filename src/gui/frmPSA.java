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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

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

/**
 *
 */
public class frmPSA {

	public JFrame frmPSA;
	AmuaModel myModel;
	int numParams;
	int numConstraints;
	DefaultTableModel modelParams;
	private JTable tableParams;

	JTabbedPane tabbedPane;
	DefaultXYDataset chartDataResults, chartDataParams, chartDataScatter, chartDataCEAC;
	JFreeChart chartResults, chartParams, chartScatter, chartCEAC;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	String subgroupNames[];
	
	JComboBox<String> comboParams;
	
	JComboBox<String> comboScatterType;
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
	double dataResultsIter[][][][][], dataResultsVal[][][][][], dataResultsDens[][][], dataResultsCumDens[][][][][];
	double dataParamsIter[][][], dataParamsVal[][][], dataParamsDens[][][], dataParamsCumDens[][][];
	double dataScatterAbs[][][][], dataScatterRel[][][][];
	String CEAnotes[][][];
	/**
	 * [Strategy][x,y][WTP]
	 */
	double dataCEAC[][][];
	
	JList<String> listParams;
	DefaultListModel<String> listModelParams;
	private JTextField textIterations;
	int numIterations;
	JCheckBox chckbxSeed;
	private JTextField textSeed;
	String outcome;
	
	
	//CEAC
	private JTextField textCEACMin;
	private JTextField textCEACMax;
	private JTextField textCEACIntervals;
	JComboBox comboCEACGroup;
	
	RunReport reports[];
	

	public frmPSA(AmuaModel myModel){
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
			frmPSA.setTitle("Amua - Probabilistic Sensitivity Analysis");
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
					//new String[] {"Parameter", "Expression","Sample"}) {
					new String[] {"Parameter", "Expression"}){
					//boolean[] columnEditables = new boolean[] {false, false,true};
					boolean[] columnEditables = new boolean[] {false, false};
					public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			numParams=myModel.parameters.size();
			numConstraints=myModel.constraints.size();
			paramNames=new String[numParams];
			for(int i=0; i<numParams; i++){
				modelParams.addRow(new Object[]{null});
				modelParams.setValueAt(myModel.parameters.get(i).name, i, 0);
				modelParams.setValueAt(myModel.parameters.get(i).expression, i, 1);
				//modelParams.setValueAt(Boolean.TRUE , i, 2);
				paramNames[i]=myModel.parameters.get(i).name;
			}

			JScrollPane scrollPaneParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneParams = new GridBagConstraints();
			gbc_scrollPaneParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneParams.gridx = 0;
			gbc_scrollPaneParams.gridy = 0;
			panel_1.add(scrollPaneParams, gbc_scrollPaneParams);
			tableParams=new JTable();
			/*tableParams = new JTable(){
				@Override
		        public Class<?> getColumnClass(int columnIndex) {
					return(modelParams.getValueAt(0, columnIndex).getClass());
		        }
			};*/
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

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(359, 6, 90, 28);
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

						if(tabbedPane.getSelectedIndex()!=3) { //Not CEAC
							fc.setDialogTitle("Export PSA Results");
						}
						else {
							fc.setDialogTitle("Export CEAC Results");
						}
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmPSA);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							if(tabbedPane.getSelectedIndex()!=3) { //Not CEAC
								//Headers
								DimInfo info=myModel.dimInfo;
								int numDim=info.dimNames.length;
								int analysisType=info.analysisType;
								int numStrat=myModel.strategyNames.length;
								out.write("Iteration");
								out.write(",Parameters");
								for(int p=0; p<numParams; p++){out.write(","+paramNames[p]);}
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

								int group=0;
								if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

								//Results
								int numPoints=dataResultsIter[group][0][0][0].length;
								for(int i=0; i<numPoints; i++){
									out.write((i+1)+""); //Iteration
									//Parameters
									out.write(",");
									for(int p=0; p<numParams; p++){out.write(","+dataParamsIter[p][1][i]);}
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
							else { //CEAC
								//Headers
								int numStrat=myModel.strategyNames.length;
								out.write("WTP");
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

							JOptionPane.showMessageDialog(frmPSA, "Exported!");
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

			JLabel lblIterations = new JLabel("# Iterations:");
			lblIterations.setBounds(6, 12, 69, 16);
			panel_2.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			textIterations.setBounds(73, 6, 69, 28);
			panel_2.add(textIterations);
			textIterations.setColumns(10);

			chckbxSeed = new JCheckBox("Seed");
			chckbxSeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxSeed.isSelected()){textSeed.setEnabled(true);}
					else{textSeed.setEnabled(false);}
				}
			});
			chckbxSeed.setBounds(162, 11, 59, 18);
			panel_2.add(chckbxSeed);

			textSeed = new JTextField();
			textSeed.setEnabled(false);
			textSeed.setBounds(223, 6, 59, 28);
			panel_2.add(textSeed);
			textSeed.setColumns(10);

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 1;
			gbc_tabbedPane.gridy = 0;
			frmPSA.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelResults = new JPanel();
			tabbedPane.addTab("Results", null, panelResults, null);
			GridBagLayout gbl_panelResults = new GridBagLayout();
			gbl_panelResults.columnWidths = new int[]{165, 165, 165, 0, 0};
			gbl_panelResults.rowHeights = new int[]{0, 0, 0};
			gbl_panelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
			gbc_panelChartResults.gridwidth = 4;
			gbc_panelChartResults.insets = new Insets(0, 0, 5, 0);
			gbc_panelChartResults.fill = GridBagConstraints.BOTH;
			gbc_panelChartResults.gridx = 0;
			gbc_panelChartResults.gridy = 0;
			panelResults.add(panelChartResults, gbc_panelChartResults);

			comboResults = new JComboBox<String>();
			comboResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
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
			comboResults.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
			GridBagConstraints gbc_comboResults = new GridBagConstraints();
			gbc_comboResults.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboResults.insets = new Insets(0, 0, 0, 5);
			gbc_comboResults.gridx = 1;
			gbc_comboResults.gridy = 1;
			panelResults.add(comboResults, gbc_comboResults);
			
			comboGroup = new JComboBox<String>();
			comboGroup.setVisible(false);
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			
			comboGroupScatter = new JComboBox<String>();
			comboGroupScatter.setVisible(false);
			comboGroupScatter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateScatter();
				}
			});
			
			GridBagConstraints gbc_comboGroup = new GridBagConstraints();
			gbc_comboGroup.insets = new Insets(0, 0, 0, 5);
			gbc_comboGroup.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroup.gridx = 2;
			gbc_comboGroup.gridy = 1;
			panelResults.add(comboGroup, gbc_comboGroup);

			JPanel panelParams = new JPanel();
			tabbedPane.addTab("Parameters", null, panelParams, null);
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
			scrollPaneSelectParams.setViewportView(listParams);
			int selectedIndices[]=new int[numParams];
			for(int v=0; v<numParams; v++){selectedIndices[v]=v;}
			listParams.setSelectedIndices(selectedIndices); //Select all

			JButton btnUpdateChart = new JButton("Update Chart");
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
			comboParams.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
			GridBagConstraints gbc_comboParams = new GridBagConstraints();
			gbc_comboParams.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboParams.gridx = 1;
			gbc_comboParams.gridy = 1;
			panel_3.add(comboParams, gbc_comboParams);

			chartDataParams = new DefaultXYDataset();
			chartParams = ChartFactory.createScatterPlot(null, "Value", "Density", chartDataParams, PlotOrientation.VERTICAL, true, false, false);
			chartParams.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartParams.getXYPlot().addDomainMarker(marker);
			chartParams.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChartParams = new ChartPanel(chartParams,false);
			GridBagConstraints gbc_panelChartParams = new GridBagConstraints();
			gbc_panelChartParams.fill = GridBagConstraints.BOTH;
			gbc_panelChartParams.gridx = 1;
			gbc_panelChartParams.gridy = 0;
			panelParams.add(panelChartParams, gbc_panelChartParams);

			JPanel panelScatter = new JPanel();
			tabbedPane.addTab("Scatter", null, panelScatter, null); 
			tabbedPane.setEnabledAt(2, false);

			chartDataScatter = new DefaultXYDataset();
			chartScatter = ChartFactory.createScatterPlot(null, "x", "y", chartDataScatter, PlotOrientation.VERTICAL, true, false, false);
			chartScatter.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartScatter.getXYPlot().addDomainMarker(marker);
			chartScatter.getXYPlot().addRangeMarker(marker);

			GridBagLayout gbl_panelScatter = new GridBagLayout();
			gbl_panelScatter.columnWidths = new int[]{155, 165, 680, 0};
			gbl_panelScatter.rowHeights = new int[]{0, 420, 0};
			gbl_panelScatter.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
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
			
			GridBagConstraints gbc_comboGroupScatter = new GridBagConstraints();
			gbc_comboGroupScatter.insets = new Insets(0, 0, 5, 5);
			gbc_comboGroupScatter.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboGroupScatter.gridx = 1;
			gbc_comboGroupScatter.gridy = 0;
			panelScatter.add(comboGroupScatter, gbc_comboGroupScatter);

			ChartPanel panelChartScatter = new ChartPanel(chartScatter,false);
			GridBagConstraints gbc_panelChartScatter = new GridBagConstraints();
			gbc_panelChartScatter.gridwidth = 3;
			gbc_panelChartScatter.fill = GridBagConstraints.BOTH;
			gbc_panelChartScatter.gridx = 0;
			gbc_panelChartScatter.gridy = 1;
			panelScatter.add(panelChartScatter, gbc_panelChartScatter);
			panelChartScatter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
			JPanel panelCEAC = new JPanel();
			tabbedPane.addTab("CEAC", null, panelCEAC, "Cost-Effectiveness Acceptability Curve");
			tabbedPane.setEnabledAt(3, false);
			
			chartDataCEAC = new DefaultXYDataset();
			String costDim="Cost", effectDim="Effect";
			if(myModel.dimInfo.analysisType>0) {
				costDim=myModel.dimInfo.dimNames[myModel.dimInfo.costDim];
				effectDim=myModel.dimInfo.dimNames[myModel.dimInfo.effectDim];
			}
			chartCEAC = ChartFactory.createScatterPlot(null, "Willingness-to-pay ("+costDim+" per "+effectDim+")", 
					"p(Optimal)", chartDataCEAC, PlotOrientation.VERTICAL, true, false, false);
			chartCEAC.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			chartCEAC.getXYPlot().addDomainMarker(marker);
			chartCEAC.getXYPlot().addRangeMarker(marker);
			//Add baseline WTP
			if(myModel.dimInfo.analysisType>0) {
				Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
				chartCEAC.getXYPlot().addDomainMarker(new ValueMarker(myModel.dimInfo.WTP, Color.BLACK,dashed));
			}
			
			
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
			
			JLabel lblCEACMin = new JLabel("Min:");
			lblCEACMin.setBounds(0, 6, 23, 16);
			panelCEACHeader.add(lblCEACMin);
			
			textCEACMin = new JTextField();
			textCEACMin.setText("0");
			textCEACMin.setBounds(25, 0, 70, 28);
			panelCEACHeader.add(textCEACMin);
			textCEACMin.setColumns(10);
			
			JLabel lblCEACMax = new JLabel("Max:");
			lblCEACMax.setBounds(97, 6, 36, 16);
			panelCEACHeader.add(lblCEACMax);
			
			textCEACMax = new JTextField();
			textCEACMax.setBounds(127, 0, 70, 28);
			panelCEACHeader.add(textCEACMax);
			textCEACMax.setColumns(10);
			textCEACMax.setText((myModel.dimInfo.WTP*3)+"");
			
			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(202, 6, 54, 16);
			panelCEACHeader.add(lblIntervals);
			
			textCEACIntervals = new JTextField();
			textCEACIntervals.setText("100");
			textCEACIntervals.setBounds(258, 0, 50, 28);
			panelCEACHeader.add(textCEACIntervals);
			textCEACIntervals.setColumns(10);
			
			JButton btnUpdateCEAC = new JButton("Update");
			btnUpdateCEAC.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int group=0;
					if(comboCEACGroup.isVisible()){group=comboCEACGroup.getSelectedIndex();}
					updateCEAC(group);
				}
			});
			btnUpdateCEAC.setBounds(315, 0, 70, 28);
			panelCEACHeader.add(btnUpdateCEAC);
			
			comboCEACGroup = new JComboBox();
			comboCEACGroup.setVisible(false);
			comboCEACGroup.setBounds(389, 1, 139, 26);
			panelCEACHeader.add(comboCEACGroup);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				subgroupNames=new String[numGroups+1];
				subgroupNames[0]="Overall";
				for(int i=0; i<numGroups; i++){subgroupNames[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
				comboGroupScatter.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroupScatter.setVisible(true);
				comboCEACGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboCEACGroup.setVisible(true);
			}
			
			ChartPanel panelChartCEAC = new ChartPanel(chartCEAC,false);
			GridBagConstraints gbc_panelChartCEAC = new GridBagConstraints();
			gbc_panelChartCEAC.fill = GridBagConstraints.BOTH;
			gbc_panelChartCEAC.gridx = 0;
			gbc_panelChartCEAC.gridy = 1;
			panelCEAC.add(panelChartCEAC, gbc_panelChartCEAC);

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmPSA, "PSA", "Sampling", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								frmPSA.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								tabbedPane.setEnabledAt(2, false);
								tabbedPane.setEnabledAt(3, false);

								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmPSA, "Errors in base case model!");
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
																		
									numIterations=Integer.parseInt(textIterations.getText().replaceAll(",", ""));
									progress.setMaximum(numIterations);

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

									dataParamsIter=new double[numParams][2][numIterations];
									dataParamsVal=new double[numParams][2][numIterations];
									dataParamsCumDens=new double[numParams][2][numIterations];

									dataScatterAbs=new double[1+numSubgroups][numStrat][2][numIterations];
									dataScatterRel=new double[1+numSubgroups][numStrat][2][numIterations];
									
									dataCEAC=new double[numStrat][][];
									
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
												curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true);
												curParam.locked=true;
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

										for(int v=0; v<numParams; v++){ //Record value
											dataParamsIter[v][0][n]=n; dataParamsVal[v][0][n]=n;
											try{
												dataParamsIter[v][1][n]=myModel.parameters.get(v).value.getDouble();
											} catch(Exception e){
												dataParamsIter[v][1][n]=Double.NaN;
											}
											dataParamsVal[v][1][n]=dataParamsIter[v][1][n];
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
															dataScatterRel[g][origStrat][1][n]=cost-baseCost;
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
											Arrays.sort(dataParamsVal[v][1]);
											for(int n=0; n<numIterations; n++){
												dataParamsVal[v][0][n]=n/(numIterations*1.0);
												dataParamsCumDens[v][0][n]=dataParamsVal[v][1][n];
												dataParamsCumDens[v][1][n]=dataParamsVal[v][0][n];
											}
										}

										//Update results chart
										updateResultsChart();

										//Update param chart
										XYPlot plotParams = chartParams.getXYPlot();
										XYLineAndShapeRenderer rendererParams = new XYLineAndShapeRenderer(true,false);
										DefaultDrawingSupplier supplierParams = new DefaultDrawingSupplier();
										for(int v=0; v<numParams; v++){
											rendererParams.setSeriesPaint(v, supplierParams.getNextPaint());
										}
										plotParams.setRenderer(rendererParams);
										updateParamChart();

										//Update scatter chart and CEAC
										if(analysisType>0){
											tabbedPane.setEnabledAt(2, true);
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
											
											tabbedPane.setEnabledAt(3, true);
											if(comboCEACGroup.isVisible()){comboCEACGroup.setSelectedIndex(0);}
											updateCEAC(0);
											
										}
										btnExport.setEnabled(true);
										
										//Get trace summary
										if(myModel.type==1){
											//get mean and bounds of results
											for(int c=0; c<numChains; c++){
												MarkovTraceSummary traceSummaries[]=new MarkovTraceSummary[numSubgroups+1];
												for(int g=0; g<numSubgroups+1; g++){
													traceSummaries[g]=new MarkovTraceSummary(traces[c][g]);
												}
												frmTraceSummary showSummary=new frmTraceSummary(traceSummaries,myModel.errorLog,subgroupNames);
												showSummary.frmTraceSummary.setVisible(true);
											}
										}
										
										//Print results summary to console
										Console console=myModel.mainForm.console;
										myModel.printSimInfo(console);
										console.print("PSA Iterations:\t"+numIterations+"\n\n");
										boolean colTypes[]=new boolean[]{false,false,true,true,true}; //is column number (true), or text (false)
										ConsoleTable curTable=new ConsoleTable(console,colTypes);
										String headers[]=new String[]{"Strategy","Outcome","Mean","95% LB","95% UB"};
										curTable.addRow(headers);
										//strategy results - overall
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
										
										if(myModel.dimInfo.analysisType==1) { //CEA - get mean ICERS
											printCEAResults(console, 0);
										}
										else if(myModel.dimInfo.analysisType==2) { //BCA - get mean NMB
											printBCAResults(console, 0);
										}
										
										//subgroups
										for(int g=0; g<numSubgroups; g++){
											console.print("\nSubgroup Results: "+myModel.subgroupNames.get(g)+"\n");
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
											
											if(myModel.dimInfo.analysisType==1) { //CEA - get mean ICERS
												printCEAResults(console, g+1);
											}
											else if(myModel.dimInfo.analysisType==2) { //BCA - get mean NMB
												printBCAResults(console, g+1);
											}
											
											
										}
										
										if(myModel.simType==1 && myModel.displayIndResults==true){
											console.print("\nIndividual-level Results:\n");
											RunReportSummary summary=new RunReportSummary(reports);
											for(int s=0; s<numStrat; s++){
												console.print("Strategy: "+myModel.strategyNames[s]+"\n");
												summary.microStatsSummary[s].printSummary(console);
											}
											
											//subgroups
											
											
											
										}
										console.newLine();
									}
									progress.close();
								}
								frmPSA.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								
							} catch (Exception e) {
								e.printStackTrace();
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
		if(analysisType==0){outcome="EV ("+info.dimSymbols[dim]+")";}
		else if(analysisType==1){outcome="ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")";}
		else if(analysisType==2){outcome="NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")";}
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
		
		int group=0; //overall
		if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}
				
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

	public void updateParamChart(){
		int selected=comboParams.getSelectedIndex();
		while(chartDataParams.getSeriesCount()>0){
			chartDataParams.removeSeries(chartDataParams.getSeriesKey(0));
		}
		
		if(selected==0){ //Density
			chartParams.getXYPlot().getDomainAxis().setLabel("Value");
			chartParams.getXYPlot().getRangeAxis().setLabel("Density");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					double kde[][]=KernelSmooth.density(dataParamsIter[v][1], 100);
					chartDataParams.addSeries(paramNames[v],kde);
				}
			}
		}
		else if(selected==1){ //Histogram
			chartParams.getXYPlot().getDomainAxis().setLabel("Value");
			chartParams.getXYPlot().getRangeAxis().setLabel("Frequency");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					double hist[][]=KernelSmooth.histogram(dataParamsIter[v][1], 100, 10);
					chartDataParams.addSeries(paramNames[v],hist);
				}
			}
		}
		else if(selected==2){ //CDF
			chartParams.getXYPlot().getDomainAxis().setLabel("Value");
			chartParams.getXYPlot().getRangeAxis().setLabel("Cumulative Distribution");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){chartDataParams.addSeries(paramNames[v],dataParamsCumDens[v]);}
			}
		}
		else if(selected==3){ //Quantile
			chartParams.getXYPlot().getDomainAxis().setLabel("Quantile");
			chartParams.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){chartDataParams.addSeries(paramNames[v],dataParamsVal[v]);}
			}
		}
		else if(selected==4){ //Iteration
			chartParams.getXYPlot().getDomainAxis().setLabel("Iteration");
			chartParams.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){chartDataParams.addSeries(paramNames[v],dataParamsIter[v]);}
			}
		}
	}

	private void updateScatter(){
		int type=comboScatterType.getSelectedIndex();
		int numStrat=myModel.strategyNames.length;
		int group=0; //overall
		if(comboGroupScatter.isVisible()){group=comboGroupScatter.getSelectedIndex();}
				
		
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
	
	private void updateCEAC(int g) {
		double minWTP=0, maxWTP=myModel.dimInfo.WTP*3;
		try {
			minWTP=Double.parseDouble(textCEACMin.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, "CEAC: Please enter a valid min!");
		}
		try {
			maxWTP=Double.parseDouble(textCEACMax.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, "CEAC: Please enter a valid max!");
		}
		int numIntervals=100;
		try {
			numIntervals=Integer.parseInt(textCEACIntervals.getText().replaceAll(",",""));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frmPSA, "CEAC: Please enter a valid number of intervals!");
		}
		double step=(maxWTP-minWTP)/(numIntervals*1.0);
		
		int costDim=myModel.dimInfo.costDim;
		int effectDim=myModel.dimInfo.effectDim;
				
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
					double effect=dataResultsIter[g][effectDim][s][1][i];
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
		DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			rendererResults.setSeriesPaint(s, supplierResults.getNextPaint());
		}
		plotResults.setRenderer(rendererResults);
		
		for(int s=0; s<numStrat; s++){
			chartDataCEAC.addSeries(myModel.strategyNames[s],dataCEAC[s]);
		}
		
		
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
		console.print("\nCEA Results (Ratio of Means):\n");
		boolean colTypes[]=new boolean[]{false,true,true,true,false}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.costDim],dimInfo.dimNames[dimInfo.effectDim],"ICER","Notes"};
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
		console.print("\nBCA Results (Mean):\n");
		boolean colTypes[]=new boolean[]{false,true,true,true}; //is column number (true), or text (false)
		ConsoleTable curTable=new ConsoleTable(console,colTypes);
		String headers[]=new String[]{"Strategy",dimInfo.dimNames[dimInfo.effectDim],dimInfo.dimNames[dimInfo.costDim],"NMB"};
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
