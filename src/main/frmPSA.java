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

package main;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import export.CppTree;
import export.ExcelTree;
import export.JavaTree;
import export.PythonTree;
import export.RTree;
import filters.CSVFilter;
import filters.CppFilter;
import filters.JavaFilter;
import filters.PyFilter;
import filters.RFilter;
import filters.XMLFilter;

import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.JTabbedPane;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 *
 */
public class frmPSA {

	public JFrame frmPSA;
	PanelTree panel;
	DecisionTree tree;
	VarHelper varHelper;
	int numVars;
	DefaultTableModel modelVars;
	private JTable tableVars;

	DefaultXYDataset chartDataResults, chartDataVars, chartDataScatter;
	JFreeChart chartResults, chartVars, chartScatter;
	JCheckBox chckbxRatio;
	JComboBox comboDimensions;
	JComboBox comboDenom;
	JComboBox comboResults;
	JComboBox comboVars;
	JComboBox comboSampling;
	int numStrat;
	String stratNames[], varNames[];
	double dataResultsIter[][][], dataResultsVal[][][], dataResultsDens[][][], dataResultsCumDens[][][];
	double dataVarsIter[][][], dataVarsVal[][][], dataVarsDens[][][], dataVarsCumDens[][][];
	JList listVars;
	DefaultListModel listModelVars;
	private JTextField textIterations;
	int numIterations;
	JCheckBox chckbxSeed;
	private JTextField textSeed;
	private JTextField textBins;
	String outcome;

	/**
	 *  Default Constructor
	 */
	public frmPSA(PanelTree panel) {
		this.panel=panel;
		this.tree=panel.tree;
		this.varHelper=panel.varHelper;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmPSA = new JFrame();
			frmPSA.setTitle("Amua - Probabilistic Sensitivity Analysis");
			frmPSA.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
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
			gbl_panel_1.rowHeights = new int[]{466, 175, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelVars=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Variable", "Expression"}) {
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			numVars=tree.variables.size();
			varNames=new String[numVars];
			for(int i=0; i<numVars; i++){
				modelVars.addRow(new Object[]{null});
				modelVars.setValueAt(tree.variables.get(i).name, i, 0);
				modelVars.setValueAt(tree.variables.get(i).expression, i, 1);
				varNames[i]=tree.variables.get(i).name;
			}

			JScrollPane scrollPaneVars = new JScrollPane();
			GridBagConstraints gbc_scrollPaneVars = new GridBagConstraints();
			gbc_scrollPaneVars.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneVars.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneVars.gridx = 0;
			gbc_scrollPaneVars.gridy = 0;
			panel_1.add(scrollPaneVars, gbc_scrollPaneVars);
			tableVars = new JTable();
			tableVars.setRowSelectionAllowed(false);
			tableVars.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableVars.setShowVerticalLines(true);
			tableVars.setModel(modelVars);
			scrollPaneVars.setViewportView(tableVars);

			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_2.setLayout(null);
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 1;
			panel_1.add(panel_2, gbc_panel_2);

			final JLabel lblOutcome = new JLabel("Outcome:");
			lblOutcome.setBounds(6, 11, 81, 16);
			panel_2.add(lblOutcome);

			comboDimensions = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDimensions.setBounds(88, 6, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(6, 136, 90, 28);
			panel_2.add(btnRun);

			final JLabel lblDenominator = new JLabel("Denominator:");
			lblDenominator.setBounds(6, 44, 89, 16);
			lblDenominator.setVisible(false);
			panel_2.add(lblDenominator);

			comboDenom = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDenom.setVisible(false);
			comboDenom.setBounds(88, 39, 227, 26);
			panel_2.add(comboDenom);

			chckbxRatio = new JCheckBox("Ratio");
			chckbxRatio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxRatio.isSelected()){
						lblDenominator.setVisible(true);
						lblOutcome.setText("Numerator:");
						comboDenom.setVisible(true);
					}
					else{
						lblDenominator.setVisible(false);
						lblOutcome.setText("Outcome:");
						comboDenom.setVisible(false);
					}
				}
			});
			chckbxRatio.setBounds(345, 10, 104, 18);
			panel_2.add(chckbxRatio);

			final JButton btnExport = new JButton("Export");
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(panel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter());

						fc.setDialogTitle("Export Graph Data");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmPSA);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							//Headers
							out.write(chartResults.getXYPlot().getDomainAxis().getLabel()+","); //Var name
							String outcome=chartResults.getXYPlot().getRangeAxis().getLabel();
							int numStrat=stratNames.length;
							for(int s=0; s<numStrat-1; s++){out.write(outcome+"-"+stratNames[s]+",");}
							out.write(outcome+"-"+stratNames[numStrat-1]); out.newLine();
							//Chart data
							int numPoints=dataResultsIter[0][0].length;
							for(int i=0; i<numPoints; i++){
								out.write(dataResultsIter[0][0][i]+","); //x-axis
								for(int s=0; s<numStrat-1; s++){out.write(dataResultsIter[s][1][i]+",");} //y-axis
								out.write(dataResultsIter[numStrat-1][1][i]+""); out.newLine();
							}
							out.close();

							JOptionPane.showMessageDialog(frmPSA, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmPSA, ex.getMessage());
						panel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(109, 136, 90, 28);
			panel_2.add(btnExport);

			JLabel lblIterations = new JLabel("# Iterations:");
			lblIterations.setBounds(6, 78, 69, 16);
			panel_2.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			textIterations.setBounds(88, 72, 90, 28);
			panel_2.add(textIterations);
			textIterations.setColumns(10);

			chckbxSeed = new JCheckBox("Seed");
			chckbxSeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxSeed.isSelected()){textSeed.setEnabled(true);}
					else{textSeed.setEnabled(false);}
				}
			});
			chckbxSeed.setBounds(339, 106, 59, 18);
			panel_2.add(chckbxSeed);

			textSeed = new JTextField();
			textSeed.setEnabled(false);
			textSeed.setBounds(395, 101, 54, 28);
			panel_2.add(textSeed);
			textSeed.setColumns(10);

			JLabel lblSampling = new JLabel("Sampling:");
			lblSampling.setBounds(201, 78, 69, 16);
			panel_2.add(lblSampling);

			comboSampling = new JComboBox();
			comboSampling.setModel(new DefaultComboBoxModel(new String[] {"Independent", "Linked (Across-Variable)"}));
			comboSampling.setBounds(276, 73, 173, 26);
			panel_2.add(comboSampling);

			JLabel lblDensityBins = new JLabel("Density Bins:");
			lblDensityBins.setBounds(307, 142, 81, 16);
			panel_2.add(lblDensityBins);

			textBins = new JTextField();
			textBins.setHorizontalAlignment(SwingConstants.CENTER);
			textBins.setText("100");
			textBins.setBounds(390, 136, 59, 28);
			panel_2.add(textBins);
			textBins.setColumns(10);

			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 1;
			gbc_tabbedPane.gridy = 0;
			frmPSA.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelResults = new JPanel();
			tabbedPane.addTab("Results", null, panelResults, null);
			GridBagLayout gbl_panelResults = new GridBagLayout();
			gbl_panelResults.columnWidths = new int[]{157, 0, 0};
			gbl_panelResults.rowHeights = new int[]{0, 0, 0};
			gbl_panelResults.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelResults.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panelResults.setLayout(gbl_panelResults);

			chartDataResults = new DefaultXYDataset();
			chartResults = ChartFactory.createScatterPlot(null, "Iteration", "EV($)", chartDataResults, PlotOrientation.VERTICAL, true, false, false);
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartResults.getXYPlot().addDomainMarker(marker);
			chartResults.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChartResults = new ChartPanel(chartResults);
			GridBagConstraints gbc_panelChartResults = new GridBagConstraints();
			gbc_panelChartResults.gridwidth = 2;
			gbc_panelChartResults.insets = new Insets(0, 0, 5, 0);
			gbc_panelChartResults.fill = GridBagConstraints.BOTH;
			gbc_panelChartResults.gridx = 0;
			gbc_panelChartResults.gridy = 0;
			panelResults.add(panelChartResults, gbc_panelChartResults);

			comboResults = new JComboBox();
			comboResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResultsChart();
				}
			});
			comboResults.setModel(new DefaultComboBoxModel(new String[] {"Sort by: Iteration", "Sort by: Quantile", "Probability", "Cumulative Probability"}));
			GridBagConstraints gbc_comboResults = new GridBagConstraints();
			gbc_comboResults.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboResults.insets = new Insets(0, 0, 0, 5);
			gbc_comboResults.gridx = 0;
			gbc_comboResults.gridy = 1;
			panelResults.add(comboResults, gbc_comboResults);

			JPanel panelVars = new JPanel();
			tabbedPane.addTab("Variables", null, panelVars, null);
			GridBagLayout gbl_panelVars = new GridBagLayout();
			gbl_panelVars.columnWidths = new int[]{225, 0, 0};
			gbl_panelVars.rowHeights = new int[]{0, 0};
			gbl_panelVars.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelVars.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panelVars.setLayout(gbl_panelVars);

			JPanel panel_3 = new JPanel();
			GridBagConstraints gbc_panel_3 = new GridBagConstraints();
			gbc_panel_3.insets = new Insets(0, 0, 0, 5);
			gbc_panel_3.fill = GridBagConstraints.BOTH;
			gbc_panel_3.gridx = 0;
			gbc_panel_3.gridy = 0;
			panelVars.add(panel_3, gbc_panel_3);
			GridBagLayout gbl_panel_3 = new GridBagLayout();
			gbl_panel_3.columnWidths = new int[]{0, 0, 0};
			gbl_panel_3.rowHeights = new int[]{0, 0, 0};
			gbl_panel_3.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			gbl_panel_3.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_3.setLayout(gbl_panel_3);

			JScrollPane scrollPaneSelectVars = new JScrollPane();
			GridBagConstraints gbc_scrollPaneSelectVars = new GridBagConstraints();
			gbc_scrollPaneSelectVars.gridwidth = 2;
			gbc_scrollPaneSelectVars.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneSelectVars.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneSelectVars.gridx = 0;
			gbc_scrollPaneSelectVars.gridy = 0;
			panel_3.add(scrollPaneSelectVars, gbc_scrollPaneSelectVars);

			listModelVars=new DefaultListModel();
			for(int v=0; v<numVars; v++){listModelVars.addElement(varNames[v]);}

			listVars = new JList(listModelVars);
			scrollPaneSelectVars.setViewportView(listVars);
			int selectedIndices[]=new int[numVars];
			for(int v=0; v<numVars; v++){selectedIndices[v]=v;}
			listVars.setSelectedIndices(selectedIndices); //Select all

			JButton btnUpdateChart = new JButton("Update Chart");
			btnUpdateChart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateVarChart();
				}
			});
			GridBagConstraints gbc_btnUpdateChart = new GridBagConstraints();
			gbc_btnUpdateChart.insets = new Insets(0, 0, 0, 5);
			gbc_btnUpdateChart.gridx = 0;
			gbc_btnUpdateChart.gridy = 1;
			panel_3.add(btnUpdateChart, gbc_btnUpdateChart);

			comboVars = new JComboBox();
			comboVars.setModel(new DefaultComboBoxModel(new String[] {"Sort by: Iteration", "Sort by: Quantile", "Probability", "Cumulative Probability"}));
			GridBagConstraints gbc_comboVars = new GridBagConstraints();
			gbc_comboVars.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboVars.gridx = 1;
			gbc_comboVars.gridy = 1;
			panel_3.add(comboVars, gbc_comboVars);

			chartDataVars = new DefaultXYDataset();
			chartVars = ChartFactory.createScatterPlot(null, "Iteration", "Value", chartDataVars, PlotOrientation.VERTICAL, true, false, false);
			//Draw axes
			chartVars.getXYPlot().addDomainMarker(marker);
			chartVars.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChartVars = new ChartPanel(chartVars);
			GridBagConstraints gbc_panelChartVars = new GridBagConstraints();
			gbc_panelChartVars.fill = GridBagConstraints.BOTH;
			gbc_panelChartVars.gridx = 1;
			gbc_panelChartVars.gridy = 0;
			panelVars.add(panelChartVars, gbc_panelChartVars);

			JPanel panelScatter = new JPanel();
			tabbedPane.addTab("Scatter", null, panelScatter, null);
			tabbedPane.setEnabledAt(2, false);

			chartDataScatter = new DefaultXYDataset();
			chartScatter = ChartFactory.createScatterPlot(null, "x", "y", chartDataScatter, PlotOrientation.VERTICAL, true, false, false);
			//Draw axes
			chartScatter.getXYPlot().addDomainMarker(marker);
			chartScatter.getXYPlot().addRangeMarker(marker);

			GridBagLayout gbl_panelScatter = new GridBagLayout();
			gbl_panelScatter.columnWidths = new int[]{680, 0};
			gbl_panelScatter.rowHeights = new int[]{420, 0};
			gbl_panelScatter.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelScatter.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panelScatter.setLayout(gbl_panelScatter);

			ChartPanel panelChartScatter = new ChartPanel(chartScatter);
			GridBagConstraints gbc_panelChartScatter = new GridBagConstraints();
			gbc_panelChartScatter.fill = GridBagConstraints.BOTH;
			gbc_panelChartScatter.gridx = 0;
			gbc_panelChartScatter.gridy = 0;
			panelScatter.add(panelChartScatter, gbc_panelChartScatter);
			panelChartScatter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmPSA, "PSA", "Sampling", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								tabbedPane.setEnabledAt(2, false);

								//Check tree first
								ArrayList<String> errorsBase=tree.parseTree();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmPSA, "Errors in base case model!");
								}
								else{
									boolean cancelled=false;
									boolean ratio=chckbxRatio.isSelected();
									int dim=comboDimensions.getSelectedIndex();
									int dim2=comboDenom.getSelectedIndex();
									panel.varHelper.generator=new MersenneTwisterFast();
									if(chckbxSeed.isSelected()){
										int seed=Integer.parseInt(textSeed.getText());
										panel.varHelper.generator.setSeed(seed);
									}
									int sampleStrategy=comboSampling.getSelectedIndex();
									panel.varHelper.samplingStrategy=sampleStrategy;

									TreeNode root=tree.nodes.get(0);
									int strategies[]=new int[root.childIndices.size()];
									numStrat=strategies.length;
									stratNames=new String[numStrat];
									for(int i=0; i<numStrat; i++){
										strategies[i]=root.childIndices.get(i);
										stratNames[i]=tree.nodes.get(strategies[i]).name;
									}
									numIterations=Integer.parseInt(textIterations.getText().replaceAll(",", ""));
									progress.setMaximum(numIterations);

									dataResultsIter=new double[numStrat][2][numIterations];
									dataResultsVal=new double[numStrat][2][numIterations];
									dataResultsCumDens=new double[numStrat][2][numIterations];

									dataVarsIter=new double[numVars][2][numIterations];
									dataVarsVal=new double[numVars][2][numIterations];
									dataVarsCumDens=new double[numVars][2][numIterations];

									double dataScatter[][][]=new double[numStrat][2][numIterations];

									//Get orig values for all variables
									double origValues[]=new double[numVars];
									for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables
										origValues[v]=tree.variables.get(v).value;
									}

									for(int n=0; n<numIterations; n++){
										progress.setProgress(n);
										for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
											TreeVariable curVar=tree.variables.get(v);
											curVar.fixed=false;
											curVar.value=origValues[v];
										}
										if(sampleStrategy==1){ //All linked
											panel.varHelper.curRand=panel.varHelper.generator.nextDouble();
										}
										for(int v=0; v<numVars; v++){tree.variables.get(v).sample(varHelper);} //Sample
										for(int v=0; v<numVars; v++){ //Record value
											dataVarsIter[v][0][n]=n; dataVarsVal[v][0][n]=n;
											dataVarsIter[v][1][n]=tree.variables.get(v).value;
											dataVarsVal[v][1][n]=dataVarsIter[v][1][n];
										} 
										//Run tree
										tree.parseTree();
										tree.runTree(root, false);
										for(int s=0; s<numStrat; s++){
											dataResultsIter[s][0][n]=n;
											dataResultsVal[s][0][n]=n;
											double curOutcome;
											if(ratio==false){
												curOutcome=tree.nodes.get(strategies[s]).expectedValues[dim];
											}
											else{
												double num=tree.nodes.get(strategies[s]).expectedValues[dim];
												double denom=tree.nodes.get(strategies[s]).expectedValues[dim2];
												curOutcome=num/(denom*1.0);
												dataScatter[s][0][n]=denom;
												dataScatter[s][1][n]=num;
											}
											dataResultsIter[s][1][n]=curOutcome;
											dataResultsVal[s][1][n]=curOutcome;
										}
										if(progress.isCanceled()){  //End loop
											n=numIterations;
											cancelled=true;
										}
									}

									//Reset all variables
									for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
										TreeVariable curVar=tree.variables.get(v);
										curVar.fixed=false;
										curVar.value=origValues[v];
									}
									tree.evalAllVars();

									if(cancelled==false){
										//Sort ordered arrays
										for(int s=0; s<numStrat; s++){
											Arrays.sort(dataResultsVal[s][1]);
											for(int n=0; n<numIterations; n++){
												dataResultsVal[s][0][n]=n/(numIterations*1.0);
												dataResultsCumDens[s][0][n]=dataResultsVal[s][1][n];
												dataResultsCumDens[s][1][n]=dataResultsVal[s][0][n];
											}
										}
										for(int v=0; v<numVars; v++){
											Arrays.sort(dataVarsVal[v][1]);
											for(int n=0; n<numIterations; n++){
												dataVarsVal[v][0][n]=n/(numIterations*1.0);
												dataVarsCumDens[v][0][n]=dataVarsVal[v][1][n];
												dataVarsCumDens[v][1][n]=dataVarsVal[v][0][n];
											}
										}

										//Update results chart
										if(ratio==false){outcome="EV ("+tree.dimSymbols[dim]+")";}
										else{outcome="EV ("+tree.dimSymbols[dim]+"/"+tree.dimSymbols[dim2]+")";}
										if(chartDataResults.getSeriesCount()>0){
											for(int s=0; s<numStrat; s++){
												chartDataResults.removeSeries(stratNames[s]);
											}
										}
										XYPlot plotResults = chartResults.getXYPlot();
										XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
										DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
										for(int s=0; s<numStrat; s++){
											rendererResults.setSeriesPaint(s, supplierResults.getNextPaint());
										}
										plotResults.setRenderer(rendererResults);
										updateResultsChart();

										//Update var chart
										XYPlot plotVars = chartVars.getXYPlot();
										XYLineAndShapeRenderer rendererVars = new XYLineAndShapeRenderer(true,false);
										DefaultDrawingSupplier supplierVars = new DefaultDrawingSupplier();
										for(int v=0; v<numVars; v++){
											rendererVars.setSeriesPaint(v, supplierVars.getNextPaint());
										}
										plotVars.setRenderer(rendererVars);
										updateVarChart();

										//Update scatter chart
										if(ratio){
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
											if(chartDataScatter.getSeriesCount()>0){
												for(int s=0; s<numStrat; s++){chartDataResults.removeSeries(stratNames[s]);}
											}
											chartScatter.getXYPlot().getDomainAxis().setLabel(tree.dimNames[dim2]);
											chartScatter.getXYPlot().getRangeAxis().setLabel(tree.dimNames[dim]);
											for(int s=0; s<numStrat; s++){
												chartDataScatter.addSeries(stratNames[s],dataScatter[s]);
											}
										}
										btnExport.setEnabled(true);
									}
									progress.close();
								}

							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmPSA, e.getMessage());
								panel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

		} catch (Exception ex){
			ex.printStackTrace();
			panel.errorLog.recordError(ex);
		}
	}

	public void updateResultsChart(){
		int selected=comboResults.getSelectedIndex();
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){chartDataResults.removeSeries(stratNames[s]);}
		}
		if(selected==0){ //Iteration
			chartResults.getXYPlot().getDomainAxis().setLabel("Iteration");
			chartResults.getXYPlot().getRangeAxis().setLabel("Value");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(stratNames[s],dataResultsIter[s]);
			}
		}
		else if(selected==1){ //Value
			chartResults.getXYPlot().getDomainAxis().setLabel("Quantile");
			chartResults.getXYPlot().getRangeAxis().setLabel("Value");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(stratNames[s],dataResultsVal[s]);
			}
		}
		else if(selected==2){ //Estimate density
			chartResults.getXYPlot().getDomainAxis().setLabel("Value");
			chartResults.getXYPlot().getRangeAxis().setLabel("Probability");
			int numBins=Integer.parseInt(textBins.getText());
			dataResultsDens=new double[numStrat][2][numBins+1];
			for(int s=0; s<numStrat; s++){
				double min=dataResultsVal[s][1][0];
				double max=dataResultsVal[s][1][numIterations-1];
				double width=(max-min)/(numBins*1.0);
				for(int n=0; n<numIterations; n++){
					int bin=(int) ((dataResultsVal[s][1][n]-min)/width);
					dataResultsDens[s][1][bin]++;
				}
				for(int b=0; b<=numBins; b++){
					dataResultsDens[s][0][b]=min+(b*width);
					dataResultsDens[s][1][b]/=(numIterations*1.0);
				}
				chartDataResults.addSeries(stratNames[s], dataResultsDens[s]);
			}
		}
		else if(selected==3){ //Cum. density
			chartResults.getXYPlot().getDomainAxis().setLabel("Value");
			chartResults.getXYPlot().getRangeAxis().setLabel("Cumulative Probability");
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(stratNames[s],dataResultsCumDens[s]);
			}
		}
	}

	public void updateVarChart(){
		int selected=comboVars.getSelectedIndex();
		while(chartDataVars.getSeriesCount()>0){
			chartDataVars.removeSeries(chartDataVars.getSeriesKey(0));
		}
		if(selected==0){ //Iteration
			chartVars.getXYPlot().getDomainAxis().setLabel("Iteration");
			chartVars.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numVars; v++){
				if(listVars.isSelectedIndex(v)){chartDataVars.addSeries(varNames[v],dataVarsIter[v]);}
			}
		}
		else if(selected==1){ //Quantile
			chartVars.getXYPlot().getDomainAxis().setLabel("Quantile");
			chartVars.getXYPlot().getRangeAxis().setLabel("Value");
			for(int v=0; v<numVars; v++){
				if(listVars.isSelectedIndex(v)){chartDataVars.addSeries(varNames[v],dataVarsVal[v]);}
			}
		}
		else if(selected==2){ //Estimate density
			chartVars.getXYPlot().getDomainAxis().setLabel("Value");
			chartVars.getXYPlot().getRangeAxis().setLabel("Probability");
			int numBins=Integer.parseInt(textBins.getText());
			dataVarsDens=new double[numVars][2][numBins+1];
			for(int v=0; v<numVars; v++){
				if(listVars.isSelectedIndex(v)){
					double min=dataVarsVal[v][1][0];
					double max=dataVarsVal[v][1][numIterations-1];
					double width=(max-min)/(numBins*1.0);
					for(int n=0; n<numIterations; n++){
						int bin=(int) ((dataVarsVal[v][1][n]-min)/width);
						dataVarsDens[v][1][bin]++;
					}
					for(int b=0; b<=numBins; b++){
						dataVarsDens[v][0][b]=min+(b*width);
						dataVarsDens[v][1][b]/=(numIterations*1.0);
					}
					chartDataVars.addSeries(varNames[v], dataVarsDens[v]);
				}
			}
		}
		else if(selected==3){ //Cumulative density
			chartVars.getXYPlot().getDomainAxis().setLabel("Value");
			chartVars.getXYPlot().getRangeAxis().setLabel("Cumulative Probability");
			for(int v=0; v<numVars; v++){
				if(listVars.isSelectedIndex(v)){chartDataVars.addSeries(varNames[v],dataVarsCumDens[v]);}
			}
		}
	}
}
