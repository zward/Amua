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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

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
import filters.CSVFilter;
import main.CEAHelper;
import main.Constraint;
import main.DimInfo;
import main.HtmlSelection;
import main.MersenneTwisterFast;
import main.Parameter;
import main.ScaledIcon;
import math.Interpreter;
import math.KernelSmooth;
import math.MathUtils;
import math.Numeric;
import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.JToolBar;

/**
 *
 */
public class frmEVPI {

	public JFrame frmEVPI;
	AmuaModel myModel;
	int numParams;
	int numConstraints;
	DefaultTableModel modelParams;
	private JTable tableParams;

	JTabbedPane tabbedPane;
	DefaultXYDataset chartDataEVPPI, chartDataResults, chartDataParams;
	JFreeChart chartEVPPI, chartResults, chartParams;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	String subgroupNames[];
	
	JComboBox<String> comboParams;
	
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
	String CEAnotes[][][];
	
	int paramDims[];
	/**
	 * [Parameter][Parameter Dimension][Iteration][Value]
	 */
	double dataParamsIter[][][][], dataParamsVal[][][][], dataParamsDens[][][][], dataParamsCumDens[][][][];
	
	JTextPane textEVPI;
	
	/**
	 * [Strategy][Iteration]
	 */
	double results[][];
	
	ArrayList<SortedResult> sortedResults;
	
	/**
	 * [Parameter][Parameter Dimension][Value][Bins]
	 */
	double evppiBins[][][][];
	boolean exportReady=false;
	
	JList<String> listParams;
	DefaultListModel<String> listModelParams;
	private JTextField textIterations;
	int numIterations;
	JCheckBox chckbxSeed;
	private JTextField textSeed;
	String outcome;
	private JTextField textNumBins;
	
	//RunReport reports[];
	

	public frmEVPI(AmuaModel myModel){
		this.myModel=myModel;
		myModel.getStrategies();
		exportReady=false;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmEVPI = new JFrame();
			frmEVPI.setTitle("Amua - Expected Value of Perfect Information (EVPI/EVPPI)");
			frmEVPI.setIconImage(Toolkit.getDefaultToolkit().getImage(frmEVPI.class.getResource("/images/evpi_128.png")));
			frmEVPI.setBounds(100, 100, 1000, 600);
			frmEVPI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmEVPI.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmEVPI.getContentPane().add(panel_1, gbc_panel_1);
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
			paramDims=new int[numParams];
			for(int i=0; i<numParams; i++){
				modelParams.addRow(new Object[]{null});
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(curParam.name, i, 0);
				modelParams.setValueAt(curParam.expression, i, 1);
				//modelParams.setValueAt(Boolean.TRUE , i, 2);
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
						
						int tabIndex=tabbedPane.getSelectedIndex();
						if(tabIndex==0) {
							fc.setDialogTitle("Export EVPI Report");
						}
						else if(tabIndex==1) {
							fc.setDialogTitle("Export EVPPI Estimates");
						}
						else if(tabIndex>=2) {
							fc.setDialogTitle("Export PSA Results");
						}
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmEVPI);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							if(tabIndex==1) { //EVPPI estimates
								//Headers
								out.write("# Bins");
								for(int p=0; p<numParams; p++) {
									int curDim=paramDims[p];
									if(curDim==1) { //scalar
										out.write(",\""+paramNames[p]+"\"");
									}
									else { //matrix
										Numeric val=myModel.parameters.get(p).value;
										for(int z=0; z<curDim; z++) {
											out.write(",\""+paramNames[p]+getParamDimLbl(val,z)+"\"");
										}
									}
								}
								out.newLine();
								int numBins=evppiBins[0][0][0].length;
								for(int b=0; b<numBins; b++) {
									out.write(evppiBins[0][0][0][b]+"");
									for(int p=0; p<numParams; p++) {
										int curDim=paramDims[p];
										for(int z=0; z<curDim; z++) {
											out.write(","+evppiBins[p][z][1][b]);
										}
									}
									out.newLine();
								}
							}
							else if(tabIndex>=2) { //PSA results
								//Headers
								DimInfo info=myModel.dimInfo;
								int numDim=info.dimNames.length;
								int analysisType=info.analysisType;
								int numStrat=myModel.strategyNames.length;
								out.write("Iteration");
								out.write(",Parameters");
								for(int p=0; p<numParams; p++) {
									int curDim=paramDims[p];
									if(curDim==1) { //scalar
										out.write(",\""+paramNames[p]+"\"");
									}
									else { //matrix
										Numeric val=myModel.parameters.get(p).value;
										for(int z=0; z<curDim; z++) {
											out.write(",\""+paramNames[p]+getParamDimLbl(val,z)+"\"");
										}
									}
								}
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
							}

							out.close();
							JOptionPane.showMessageDialog(frmEVPI, "Exported!");
						}

					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmEVPI, ex.getMessage());
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
			
			JLabel lblEvppiBins = new JLabel("EVPPI # bins:");
			lblEvppiBins.setBounds(6, 42, 81, 16);
			panel_2.add(lblEvppiBins);
			
			textNumBins = new JTextField();
			textNumBins.setHorizontalAlignment(SwingConstants.CENTER);
			textNumBins.setText("100");
			textNumBins.setBounds(83, 36, 59, 28);
			panel_2.add(textNumBins);
			textNumBins.setColumns(10);

			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					int index=tabbedPane.getSelectedIndex();
					if(index==0) { //EVPI report
						btnExport.setEnabled(false);
					}
					else if(index>0 && exportReady==true) {
						btnExport.setEnabled(true);
					}
				}
			});
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 1;
			gbc_tabbedPane.gridy = 0;
			frmEVPI.getContentPane().add(tabbedPane, gbc_tabbedPane);
			
			JPanel panelEVPI = new JPanel();
			tabbedPane.addTab("EVPI", null, panelEVPI, null);
			GridBagLayout gbl_panelEVPI = new GridBagLayout();
			gbl_panelEVPI.columnWidths = new int[]{0, 0};
			gbl_panelEVPI.rowHeights = new int[]{0, 0, 0};
			gbl_panelEVPI.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelEVPI.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelEVPI.setLayout(gbl_panelEVPI);
			
			JToolBar toolBar = new JToolBar();
			toolBar.setRollover(true);
			toolBar.setFloatable(false);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.WEST;
			gbc_toolBar.insets = new Insets(0, 0, 5, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			panelEVPI.add(toolBar, gbc_toolBar);
			
			JButton btnCopy = new JButton("Copy");
			btnCopy.setEnabled(false);
			btnCopy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
					clip.setContents(new HtmlSelection(textEVPI.getText()), null);
				}
			});
			btnCopy.setDisabledIcon(new ScaledIcon("/images/copy",16,16,16,false));
			btnCopy.setIcon(new ScaledIcon("/images/copy",16,16,16,true));
			toolBar.add(btnCopy);
			
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			panelEVPI.add(scrollPane, gbc_scrollPane);
			
			textEVPI = new JTextPane();
			textEVPI.setFont(new Font("Consolas", Font.PLAIN, 14));
			textEVPI.setContentType("text/html");
			textEVPI.setEditable(false);
			scrollPane.setViewportView(textEVPI);
			
			JPanel panelEVPPI = new JPanel();
			tabbedPane.addTab("EVPPI", null, panelEVPPI, null);
			GridBagLayout gbl_panelEVPPI = new GridBagLayout();
			gbl_panelEVPPI.columnWidths = new int[]{0, 0};
			gbl_panelEVPPI.rowHeights = new int[]{0, 0};
			gbl_panelEVPPI.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelEVPPI.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panelEVPPI.setLayout(gbl_panelEVPPI);
			
			chartDataEVPPI = new DefaultXYDataset();
			chartEVPPI = ChartFactory.createScatterPlot(null, "# of Bins", "EVPPI", chartDataEVPPI, PlotOrientation.VERTICAL, true, false, false);
			chartEVPPI.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartEVPPI.getXYPlot().addDomainMarker(marker);
			chartEVPPI.getXYPlot().addRangeMarker(marker);
			
			ChartPanel panelChartEVPPI = new ChartPanel(chartEVPPI,false);
			GridBagConstraints gbc_panelChartEVPPI = new GridBagConstraints();
			gbc_panelChartEVPPI.fill = GridBagConstraints.BOTH;
			gbc_panelChartEVPPI.gridx = 0;
			gbc_panelChartEVPPI.gridy = 0;
			panelEVPPI.add(panelChartEVPPI, gbc_panelChartEVPPI);
			
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
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				subgroupNames=new String[numGroups+1];
				subgroupNames[0]="Overall";
				for(int i=0; i<numGroups; i++){subgroupNames[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
			}

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmEVPI, "EVPI", "Sampling", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								frmEVPI.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						
								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmEVPI, "Errors in base case model!");
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

									dataParamsIter=new double[numParams][][][];
									dataParamsVal=new double[numParams][][][];
									dataParamsCumDens=new double[numParams][][][];
									for(int p=0; p<numParams; p++) {
										int curDim=paramDims[p];
										dataParamsIter[p]=new double[curDim][2][numIterations];
										dataParamsVal[p]=new double[curDim][2][numIterations];
										dataParamsCumDens[p]=new double[curDim][2][numIterations];
									}
									
									results=new double[numStrat][numIterations];
									sortedResults=new ArrayList<SortedResult>();
									
									//Get orig values for all parameters
									Numeric origValues[]=new Numeric[numParams];
									for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters
										origValues[v]=myModel.parameters.get(v).value.copy();
									}
									
									//Parse constraints
									for(int c=0; c<numConstraints; c++){
										myModel.constraints.get(c).parseConstraints();
									}
									
									boolean origShowTrace=true;
									if(myModel.type==1){
										origShowTrace=myModel.markov.showTrace;
										myModel.markov.showTrace=false;
									}
									
									long startTime=System.currentTimeMillis();
									
									//reports=new RunReport[numIterations];
									
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
												if(curParam.locked==false) {
													curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true);
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
												if(myModel.parseModel().size()!=0){validParams=false;}
											}
										}

										for(int v=0; v<numParams; v++){ //Record value
											int curDim=paramDims[v];
											if(curDim==1) { //scalar
												dataParamsIter[v][0][0][n]=n; dataParamsVal[v][0][0][n]=n;
												try{
													dataParamsIter[v][0][1][n]=myModel.parameters.get(v).value.getDouble();
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
										//reports[n]=myModel.runModel(null, false);
										myModel.runModel(null, false);
													
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
													}
												}
											}
										}
										
										
										//Get EVPI results
										SortedResult curResult=new SortedResult(n,numStrat,numParams);
										for(int p=0; p<numParams; p++){ //Record parameter value
											int curDim=paramDims[p];
											curResult.paramVals[p]=new double[curDim];
											for(int z=0; z<curDim; z++) {
												curResult.paramVals[p][z]=dataParamsIter[p][z][1][n];
											}
										} 
										if(analysisType==0) { //EV
											for(int s=0; s<numStrat; s++) {
												results[s][n]=myModel.getStrategyEV(s, myModel.dimInfo.objectiveDim);
												curResult.outcomes[s]=results[s][n];
											}
										}
										else { //CEA or BCA
											for(int s=0; s<numStrat; s++) {
												double cost=myModel.getStrategyEV(s, myModel.dimInfo.costDim);
												double effect=myModel.getStrategyEV(s, myModel.dimInfo.effectDim);
												double NMB=myModel.dimInfo.WTP*effect-cost;
												results[s][n]=NMB;
												curResult.outcomes[s]=results[s][n];
											}
										}
										
										sortedResults.add(curResult);
																		
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

										//Calculate EVPI
										int sign=1; //objective is to maximize outcome
										if(analysisType==0 && myModel.dimInfo.objective==1) { //EV, minimize - change to maximize negative
											sign=-1;
										}
										
										double meanOutcomes[]=new double[numStrat];
										int numBest[]=new int[numStrat];
										double bestOutcome=0; //mean of max
										for(int n=0; n<numIterations; n++) {
											double curBest=Double.NEGATIVE_INFINITY;
											int bestS=-1;
											for(int s=0; s<numStrat; s++) {
												double curRes=sign*results[s][n];
												meanOutcomes[s]+=curRes;
												if(curRes>curBest) {
													curBest=curRes;
													bestS=s;
												}
											}
											bestOutcome+=curBest;
											numBest[bestS]++;
										}
										bestOutcome/=(numIterations*1.0);
										double bestMean=Double.NEGATIVE_INFINITY;
										int bestStrat=-1;
										for(int s=0; s<numStrat; s++) {
											double curMean=meanOutcomes[s]/=(numIterations*1.0);
											if(curMean>bestMean) {
												bestMean=curMean;
												bestStrat=s;
											}
										}
										
										//EVPI: E[max] - max E[]
										double evpi=bestOutcome-bestMean;
																				
										//Calculate EVPPI (for each parameter)
										double bestPPI[][]=new double[numParams][];
										double evppi[][]=new double[numParams][];
										
										int numBins=Integer.parseInt(textNumBins.getText());
										int reportNumBins=numBins;
										
										int numSamp=numIterations/numBins;
										for(int p=0; p<numParams; p++) {
											int curDim=paramDims[p];
											bestPPI[p]=new double[curDim];
											evppi[p]=new double[curDim];
											for(int z=0; z<curDim; z++) {
												Collections.sort(sortedResults, new ResultComparator(-1,-1)); //order by iteration (reset previous ordering)
												Collections.sort(sortedResults, new ResultComparator(p,z)); //re-sort ascending by parameter
												double curMax[]=new double[numBins];
												double avgMax=0;
												for(int k=0; k<numBins; k++) {
													int index0=k*numSamp;
													int index1=index0+numSamp;
													//calculate strategy mean within bin
													double binMeans[]=new double[numStrat];
													for(int i=index0; i<index1; i++) {
														for(int s=0; s<numStrat; s++) {
															binMeans[s]+=sortedResults.get(i).outcomes[s];
														}
													}
													//get best strategy among bin means
													double binMax=Double.NEGATIVE_INFINITY;
													for(int s=0; s<numStrat; s++) {
														binMeans[s]/=(numSamp*1.0);
														binMax=Math.max(binMax, sign*binMeans[s]);
													}
													curMax[k]=binMax;
													avgMax+=binMax;
												}
												avgMax/=(numBins*1.0);
												bestPPI[p][z]=avgMax;
												evppi[p][z]=avgMax-bestMean;
											}
										}
										
										//Calculate EVPPI for varying bin sizes
										ArrayList<Integer> binSizes=new ArrayList<Integer>();
										for(int b=1; b<numIterations/2; b++) {
											if(numIterations%b==0) { //divides evenly
												binSizes.add(b);
											}
										}
										
										evppiBins=new double[numParams][][][];

										for(int p=0; p<numParams; p++) {
											int curDim=paramDims[p];
											evppiBins[p]=new double[curDim][2][binSizes.size()];
											for(int z=0; z<curDim; z++) {
												Collections.sort(sortedResults, new ResultComparator(-1,-1)); //order by iteration (reset previous ordering)
												Collections.sort(sortedResults, new ResultComparator(p,z)); //re-sort ascending by parameter

												for(int b=0; b<binSizes.size(); b++) { //number of bins
													numBins=binSizes.get(b);
													evppiBins[p][z][0][b]=numBins;
													numSamp=numIterations/numBins;
													double curMax[]=new double[numBins];
													double avgMax=0;
													for(int k=0; k<numBins; k++) {
														int index0=k*numSamp;
														int index1=index0+numSamp;
														//calculate strategy mean within bin
														double binMeans[]=new double[numStrat];
														for(int i=index0; i<index1; i++) {
															for(int s=0; s<numStrat; s++) {
																binMeans[s]+=sortedResults.get(i).outcomes[s];
															}
														}
														//get best strategy among bin means
														double binMax=Double.NEGATIVE_INFINITY;
														for(int s=0; s<numStrat; s++) {
															binMeans[s]/=(numSamp*1.0);
															binMax=Math.max(binMax, sign*binMeans[s]);
														}
														curMax[k]=binMax;
														avgMax+=binMax;
													}
													avgMax/=(numBins*1.0);
													evppiBins[p][z][1][b]=avgMax-bestMean;
												}
											}
										}
										
										
										//Update EVPPI chart
										updateEVPPIChart();
										
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
										
										//Print results summary to textpane
										HTMLEditorKit kit = new HTMLEditorKit();
										textEVPI.setEditorKit(kit);
										
										//add html styles
								        StyleSheet styleSheet = kit.getStyleSheet();
								        styleSheet.addRule("th {border-bottom: 1px solid black}");
										
								        String strReport="";
								        strReport+=("<html><body><b>EVPI Report</b><br>");
								        
								        //sim info
								        strReport+=myModel.getSimInfoHTML();
								        strReport+=("EVPI Iterations:\t"+numIterations+"<br><br>");
										String lblObj="max";
										String lblOutcome="NMB";
										int numDecimals=4;
										if(myModel.dimInfo.analysisType==0) { //EV
											lblOutcome=myModel.dimInfo.dimSymbols[myModel.dimInfo.objectiveDim];
											numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.objectiveDim];
											if(myModel.dimInfo.objective==1) {
												lblObj="min";
												bestOutcome=-bestOutcome; //flip signs
												bestMean=-bestMean;
											}
										}
										else { //CEA/BCA
											numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.costDim];
										}
										
										//evpi table
										strReport+=("<table>");
										strReport+=("<caption>Expected Value of Perfect Information</caption>");
										strReport+=("<tr><th>Estimand</th><th>"+lblOutcome+"</th></tr>");
										strReport+=("<tr><td>E["+lblObj+"(\u00B7)] (Perfect Information)</td><td align=\"right\">"+MathUtils.round(bestOutcome,numDecimals)+"</td></tr>");
										strReport+=("<tr><td>"+lblObj+"(E[\u00B7]) ("+myModel.strategyNames[bestStrat]+")</td><td align=\"right\">"+MathUtils.round(bestMean,numDecimals)+"</td></tr>");
										strReport+=("<tr><td>EVPI (Difference)</td><td align=\"right\">"+MathUtils.round(evpi,numDecimals)+"</td></tr>");
										strReport+=("</table>");
										strReport+=("<br><br>");
										
										//p(Best)|PI table
										strReport+=("<table>");
										strReport+=("<caption>p(Best)</caption>");
										strReport+=("<tr><th>Strategy</th><th>p(Best)|Perfect Information</th></tr>");
										for(int s=0; s<numStrat; s++) {
											strReport+=("<tr><td>"+myModel.strategyNames[s]+"</td>");
											strReport+=("<td align=\"right\">"+(numBest[s]/(numIterations*1.0))+"</td></tr>");
										}
										strReport+=("</table>");
										strReport+=("<br><br>");
										
										//evppi table
										strReport+=("<table>");
										strReport+=("<caption>Expected Value of Partial Perfect Information ("+reportNumBins+" bins)</caption>");
										strReport+=("<tr><th>Parameter</th><th>Expression</th><th>EVPPI</th></tr>");
										for(int p=0; p<numParams; p++) {
											Parameter curParam=myModel.parameters.get(p);
											int curDim=paramDims[p];
											if(curDim==1) { //scalar
												strReport+=("<tr><td>"+curParam.name+"</td>");
												strReport+=("<td>"+curParam.expression+"</td>");
												strReport+=("<td align=\"right\">"+MathUtils.round(evppi[p][0],numDecimals)+"</td></tr>");
											}
											else { //matrix
												Numeric val=curParam.value;
												for(int z=0; z<curDim; z++) {
													strReport+=("<tr><td>"+curParam.name+getParamDimLbl(val,z)+"</td>");
													strReport+=("<td>"+curParam.expression+"</td>");
													strReport+=("<td align=\"right\">"+MathUtils.round(evppi[p][z],numDecimals)+"</td></tr>");
												}
											}
										}
										strReport+=("</table>");
										strReport+=("<br><br>");
										
										
										//outcome summaries
										strReport+=("<table>");
										strReport+=("<caption>Outcome Summaries</caption>");
										strReport+=("<tr><th>Strategy</th><th>Outcome</th><th>Mean</th><th>95% LB</th><th>95% UB</th></tr>");
										//strategy results - overall
										for(int s=0; s<numStrat; s++) {
											String stratName=myModel.strategyNames[s];
											if(myModel.dimInfo.analysisType>0) { //NMB
												String dimName="NMB";
												double mean=MathUtils.round(meanOutcomes[s]*sign, numDecimals);
												Arrays.sort(results[s]);
												double lb=MathUtils.round(results[s][indexLB],numDecimals);
												double ub=MathUtils.round(results[s][indexUB],numDecimals);
												strReport+=("<tr><td>"+stratName+"</td><td>"+dimName+"</td>");
												strReport+=("<td align=\"right\">"+mean+"</td>");
												strReport+=("<td align=\"right\">"+lb+"</td>");
												strReport+=("<td align=\"right\">"+ub+"</td></tr>");
											}
											for(int d=0; d<numDim; d++){
												String dimName=myModel.dimInfo.dimNames[d];
												if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" (Dis)";}
												double mean=MathUtils.round(meanResults[0][d][s],myModel.dimInfo.decimals[d]);
												double lb=MathUtils.round(lbResults[0][d][s],myModel.dimInfo.decimals[d]);
												double ub=MathUtils.round(ubResults[0][d][s],myModel.dimInfo.decimals[d]);
												strReport+=("<tr><td>"+stratName+"</td><td>"+dimName+"</td>");
												strReport+=("<td align=\"right\">"+mean+"</td>");
												strReport+=("<td align=\"right\">"+lb+"</td>");
												strReport+=("<td align=\"right\">"+ub+"</td></tr>");
											}
										}
										strReport+=("</table>");
										strReport+=("<br><br>");
										
										//end EVPI report
										strReport+=("</body></html>");
								        
										Document doc = kit.createDefaultDocument();
										textEVPI.setDocument(doc);
										textEVPI.setText(strReport);
										
										textEVPI.setCaretPosition(0); //go to top
										
										exportReady=true;
										btnCopy.setEnabled(true);
										
										int index=tabbedPane.getSelectedIndex();
										if(index==0) { //EVPI report
											btnExport.setEnabled(false);
										}
										else if(index>0 && exportReady==true) {
											btnExport.setEnabled(true);
										}
										
										//subgroups
										/*for(int g=0; g<numSubgroups; g++){
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
										}*/
										
									}
									progress.close();
								}
								frmEVPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								
							} catch (Exception e) {
								e.printStackTrace();
								frmEVPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								JOptionPane.showMessageDialog(frmEVPI, e.getMessage());
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

	public void updateEVPPIChart(){
		while(chartDataEVPPI.getSeriesCount()>0){
			chartDataEVPPI.removeSeries(chartDataEVPPI.getSeriesKey(0));
		}
		
		XYPlot plotResults = chartEVPPI.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,true);
		DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		Shape circle=new Ellipse2D.Double(-2.5,-2.5,5,5);
		int i=0;
		for(int p=0; p<numParams; p++){
			int curDim=paramDims[p];
			for(int z=0; z<curDim; z++) {
				rendererResults.setSeriesPaint(i, supplierResults.getNextPaint());
				rendererResults.setSeriesShape(i, circle);
				i++;
			}
		}
		plotResults.setRenderer(rendererResults);
		
		for(int p=0; p<numParams; p++){
			Parameter curParam=myModel.parameters.get(p);
			int curDim=paramDims[p];
			if(curDim==1) { //scalar
				chartDataEVPPI.addSeries(curParam.name, evppiBins[p][0]);
			}
			else { //matrix
				Numeric val=curParam.value;
				for(int z=0; z<curDim; z++) {
					chartDataEVPPI.addSeries(curParam.name+getParamDimLbl(val, z), evppiBins[p][z]);
				}
			}
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
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						double kde[][]=KernelSmooth.density(dataParamsIter[v][0][1], 100);
						chartDataParams.addSeries(paramNames[v],kde);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double kde[][]=KernelSmooth.density(dataParamsIter[v][z][1], 100);
							String curLbl=getParamDimLbl(val, z);
							chartDataParams.addSeries(paramNames[v]+curLbl, kde);
						}
					}
				}
			}
		}
		else if(selected==1){ //Histogram
			chartParams.getXYPlot().getDomainAxis().setLabel("Value");
			chartParams.getXYPlot().getRangeAxis().setLabel("Frequency");
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
							String curLbl=getParamDimLbl(val, z);
							chartDataParams.addSeries(paramNames[v]+curLbl, hist);
						}
					}
				}
			}
		}
		else if(selected==2){ //CDF
			chartParams.getXYPlot().getDomainAxis().setLabel("Value");
			chartParams.getXYPlot().getRangeAxis().setLabel("Cumulative Distribution");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsCumDens[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=getParamDimLbl(val, z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsCumDens[v][z]);
						}
					}
				}
			}
		}
		else if(selected==3){ //Quantile
			chartParams.getXYPlot().getDomainAxis().setLabel("Quantile");
			chartParams.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsVal[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=getParamDimLbl(val, z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsVal[v][z]);
						}
					}
				}
			}
		}
		else if(selected==4){ //Iteration
			chartParams.getXYPlot().getDomainAxis().setLabel("Iteration");
			chartParams.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numParams; v++){
				if(listParams.isSelectedIndex(v)){
					int curDim=paramDims[v];
					if(curDim==1) { //scalar
						chartDataParams.addSeries(paramNames[v],dataParamsIter[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=getParamDimLbl(val, z);
							chartDataParams.addSeries(paramNames[v]+curLbl, dataParamsIter[v][z]);
						}
					}
				}
			}
		}
	}
	
	//get dimension index label
	private String getParamDimLbl(Numeric val, int z) {
		String curLbl="";
		int curZ=-1;
		for(int i=0; i<val.nrow; i++) {
			for(int j=0; j<val.ncol; j++) {
				curZ++;
				if(z==curZ) {
					if(val.nrow>1 && val.ncol>1) { //matrix
						curLbl="["+i+","+j+"]";
					}
					else { //vector
						curLbl="["+Math.max(i, j)+"]";
					}
					return(curLbl);
				}
			}
		}
		return(curLbl);
	}
	
}

class SortedResult{
	int iteration;
	double outcomes[]; //strategy outcome of interest (e.g. NMB)
	double paramVals[][]; //[parameter][dimension]
	
	SortedResult(int i, int numStrat, int numParams){ //constructor
		iteration=i;
		outcomes=new double[numStrat];
		paramVals=new double[numParams][];
	}
}

class ResultComparator implements Comparator<SortedResult> {
	int pIndex; //parameter index to sort by
	int dIndex; //parameter dimension to sort by
	
	ResultComparator(int p, int d){ //Constructor
		this.pIndex=p;
		this.dIndex=d;
	}
	
	@Override public int compare(SortedResult o1, SortedResult o2) {
		if(pIndex==-1) { //sort by iteration
			return(Double.compare(o1.iteration, o2.iteration));
		}
		else { //sort by parameter value
			return(Double.compare(o1.paramVals[pIndex][dIndex], o2.paramVals[pIndex][dIndex]));
		}
	}
}
