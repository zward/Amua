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
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
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
import main.FileUtils;
import main.HtmlSelection;
import main.MersenneTwisterFast;
import main.PSAResults;
import main.Parameter;
import main.ScaledIcon;
import math.Interpreter;
import math.KernelSmooth;
import math.MathUtils;
import math.Numeric;
import voi.EVPPI;

import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.JToolBar;

/**
 *
 */
public class frmEVPPI {

	public JFrame frmEVPPI;
	AmuaModel myModel;
	int numParams;
	DefaultTableModel modelParams;
	private JTable tableParams;

	JComboBox comboMethod, comboSource;

	JLabel lblOuter, lblInner;
	private JTextField textOuter, textInner;
	JCheckBox chckbxSeed;
	private JTextField textSeed;

	EVPPI evppi;

	JTabbedPane tabbedPane;
	DefaultXYDataset chartDataEVPPI_Bins, chartDataResults, chartDataParams;
	JFreeChart chartEVPPI_Bins, chartResults, chartParams;
	Paint seriesPaints_Bins[], seriesPaints_Strat[], seriesPaints_Params[];
	int numSeries_Params;

	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	String subgroupNames[];

	JComboBox<String> comboParams;

	String paramNames[];
	int paramDims[];

	JTextPane textEVPI;

	boolean exportReady=false;

	JList<String> listParams;
	DefaultListModel<String> listModelParams;

	String outcome;

	public frmEVPPI(AmuaModel myModel){
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
			frmEVPPI = new JFrame();
			frmEVPPI.setTitle("Amua - "+myModel.language.analysis.getString("voi.evppi_full"));
			frmEVPPI.setIconImage(Toolkit.getDefaultToolkit().getImage(frmEVPPI.class.getResource("/images/evppi_128.png")));
			frmEVPPI.setBounds(100, 100, 1000, 600);
			frmEVPPI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmEVPPI.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmEVPPI.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{0, 460, 130, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelParams=new DefaultTableModel(
					new Object[][] {},
					new String[] {myModel.language.analysis.getString("result.estimate"), //Estimate
							myModel.language.base.getString("object.parameter"), //Parameter
							myModel.language.base.getString("object.expression")} //Expression
					) {
				Class[] columnTypes = new Class[] {Boolean.class, Object.class, Object.class};
				public Class getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				boolean[] columnEditables = new boolean[] {true, false, false};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};

			//get default series colors
			int numStrat=myModel.getStrategies();
			seriesPaints_Strat=new Paint[numStrat];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			for(int s=0; s<numStrat; s++) {
				seriesPaints_Strat[s]=supplier.getNextPaint();
			}


			numParams=myModel.parameters.size();
			paramNames=new String[numParams];
			paramDims=new int[numParams];
			DefaultDrawingSupplier supplierParams = new DefaultDrawingSupplier();
			seriesPaints_Bins=new Paint[numParams];
			seriesPaints_Params=new Paint[numParams];
			for(int i=0; i<numParams; i++){
				modelParams.addRow(new Object[]{null});
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(Boolean.FALSE , i, 0);
				modelParams.setValueAt(curParam.name, i, 1);
				modelParams.setValueAt(curParam.expression, i, 2);
				paramNames[i]=curParam.name;
				paramDims[i]=1; //default to single dimension (scalar)
				if(curParam.value.isMatrix()) {
					paramDims[i]=curParam.value.nrow*curParam.value.ncol;
				}
				seriesPaints_Bins[i]=supplierParams.getNextPaint();
				seriesPaints_Params[i]=seriesPaints_Bins[i];
			}

			JButton btnSelectAll = new JButton(myModel.language.base.getString("button.select_all")); //Select All
			btnSelectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(btnSelectAll.getText().matches(myModel.language.base.getString("button.deselect_all"))) { //De-Select All
						for(int p=0; p<numParams; p++) {
							tableParams.setValueAt(Boolean.FALSE, p, 0);
							btnSelectAll.setText(myModel.language.base.getString("button.select_all")); //Select All
						}
					}
					else {
						for(int p=0; p<numParams; p++) {
							tableParams.setValueAt(Boolean.TRUE, p, 0);
							btnSelectAll.setText(myModel.language.base.getString("button.deselect_all")); //De-Select All
						}
					}
				}
			});
			GridBagConstraints gbc_btnSelectAll = new GridBagConstraints();
			gbc_btnSelectAll.anchor = GridBagConstraints.WEST;
			gbc_btnSelectAll.insets = new Insets(0, 0, 5, 0);
			gbc_btnSelectAll.gridx = 0;
			gbc_btnSelectAll.gridy = 0;
			panel_1.add(btnSelectAll, gbc_btnSelectAll);

			JScrollPane scrollPaneParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneParams = new GridBagConstraints();
			gbc_scrollPaneParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneParams.gridx = 0;
			gbc_scrollPaneParams.gridy = 1;
			panel_1.add(scrollPaneParams, gbc_scrollPaneParams);

			tableParams=new JTable();
			tableParams.setRowSelectionAllowed(false);
			tableParams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableParams.setShowVerticalLines(true);
			tableParams.getTableHeader().setReorderingAllowed(false);
			//tableParams.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			tableParams.setModel(modelParams);
			tableParams.getColumnModel().getColumn(0).setPreferredWidth(10);
			tableParams.getColumnModel().getColumn(1).setPreferredWidth(120);
			tableParams.getColumnModel().getColumn(2).setPreferredWidth(150);
			scrollPaneParams.setViewportView(tableParams);

			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 2;
			panel_1.add(panel_2, gbc_panel_2);

			String outcomes[]=myModel.dimInfo.getOutcomes();
			GridBagLayout gbl_panel_2 = new GridBagLayout();
			gbl_panel_2.columnWidths = new int[]{52, 77, 52, 59, 59, 45, 90, 0};
			gbl_panel_2.rowHeights = new int[]{26, 28, 28, 5, 0};
			gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			panel_2.setLayout(gbl_panel_2);

			

			String methods[]=new String[2];
			methods[0]=myModel.language.analysis.getString("voi.two_level_monte_carlo"); //Two-level Monte Carlo
			methods[1]=myModel.language.analysis.getString("voi.bins"); //Bins
			
			JLabel lblNewLabel = new JLabel(myModel.language.analysis.getString("calib.method")+":");
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel_2.add(lblNewLabel, gbc_lblNewLabel);
			
			comboMethod = new JComboBox();
			comboMethod.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int method=comboMethod.getSelectedIndex();
					if(method==0) { //Two-level Monte Carlo
						lblOuter.setText(myModel.language.analysis.getString("voi.num_outer")+":"); //# Outer
						lblInner.setText(myModel.language.analysis.getString("voi.num_inner")+":"); //# Inner
					}
					else {
						lblOuter.setText(myModel.language.analysis.getString("sim.num_iterations")+":"); //# Iterations
						lblInner.setText(myModel.language.analysis.getString("voi.num_bins")+":"); //# Bins
					}

				}
			});
			
			comboMethod.setModel(new DefaultComboBoxModel(methods));
			
			GridBagConstraints gbc_comboMethod = new GridBagConstraints();
			gbc_comboMethod.anchor = GridBagConstraints.NORTH;
			gbc_comboMethod.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboMethod.insets = new Insets(0, 0, 5, 5);
			gbc_comboMethod.gridwidth = 4;
			gbc_comboMethod.gridx = 1;
			gbc_comboMethod.gridy = 0;
			panel_2.add(comboMethod, gbc_comboMethod);

			JLabel lblSource = new JLabel(myModel.language.analysis.getString("gen.source")+":");
			GridBagConstraints gbc_lblSource = new GridBagConstraints();
			gbc_lblSource.anchor = GridBagConstraints.EAST;
			gbc_lblSource.insets = new Insets(0, 0, 5, 5);
			gbc_lblSource.gridx = 0;
			gbc_lblSource.gridy = 1;
			panel_2.add(lblSource, gbc_lblSource);
			
			String source[]=new String[2];
			source[0]=myModel.language.analysis.getString("voi.run_new_psa"); //Run New PSA
			source[1]=myModel.language.analysis.getString("voi.import_psa_results"); //Import PSA Results
			
			comboSource = new JComboBox();
			comboSource.setModel(new DefaultComboBoxModel(source));
			GridBagConstraints gbc_comboSource = new GridBagConstraints();
			gbc_comboSource.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboSource.insets = new Insets(0, 0, 5, 5);
			gbc_comboSource.gridwidth = 4;
			gbc_comboSource.gridx = 1;
			gbc_comboSource.gridy = 1;
			panel_2.add(comboSource, gbc_comboSource);

			JButton btnRun = new JButton(myModel.language.base.getString("menu.run"));
			GridBagConstraints gbc_btnRun = new GridBagConstraints();
			gbc_btnRun.anchor = GridBagConstraints.NORTH;
			gbc_btnRun.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnRun.insets = new Insets(0, 0, 5, 0);
			gbc_btnRun.gridx = 6;
			gbc_btnRun.gridy = 1;
			panel_2.add(btnRun, gbc_btnRun);
			
			final JButton btnExport = new JButton(myModel.language.base.getString("menu.export")); //Export
			JButton btnCopy = new JButton(myModel.language.base.getString("menu.copy")); //Copy
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					boolean runVOI=true;

					//get selected parameters
					boolean paramSelected[]=new boolean[numParams];
					int countSelected=0;
					for(int p=0; p<numParams; p++) {
						paramSelected[p]=(boolean)tableParams.getValueAt(p, 0);
						if(paramSelected[p]==true) {
							countSelected++;
						}
					}

					if(countSelected==0) {
						JOptionPane.showMessageDialog(frmEVPPI, myModel.language.message.getString("err.select_one_param")); //Please select at least one parameter to estimate
						runVOI=false;
					}

					evppi=new EVPPI(myModel, countSelected, paramSelected);

					//get method + source
					final int method=comboMethod.getSelectedIndex();
					String strMethod=(String) comboMethod.getSelectedItem();
					final int source=comboSource.getSelectedIndex();

					if(runVOI) {
						final ProgressMonitor progress=new ProgressMonitor(frmEVPPI, myModel.language.analysis.getString("voi.evppi"), myModel.language.message.getString("info.sampling"), 0, 100); //EVPPI, Sampling

						Thread SimThread = new Thread(){ //Non-UI
							public void run(){
								try{
									frmEVPPI.setCursor(new Cursor(Cursor.WAIT_CURSOR));
									btnRun.setEnabled(false);
									tabbedPane.setEnabledAt(1, false);

									evppi.valid=true;
									evppi.method=method;

									if(method==0) { //two-level Monte Carlo
										int numOuter=Integer.parseInt(textOuter.getText().replaceAll(",", ""));
										int numInner=Integer.parseInt(textInner.getText().replaceAll(",", ""));

										if(source==0) { //run new PSA
											boolean useSeed=false;
											int seed=-1;
											if(chckbxSeed.isSelected()){
												useSeed=true;
												seed=Integer.parseInt(textSeed.getText());
											}
											evppi.runTwoLevelMonteCarlo(numOuter, numInner, useSeed, seed, progress, frmEVPPI);
										}
										else { //import PSA results
											JFileChooser fc=new JFileChooser();
											fc.setDialogTitle(myModel.language.analysis.getString("voi.import_psa_results")); //Import PSA Results
											fc.setApproveButtonText(myModel.language.base.getString("menu.import")); //Import
											fc.setFileFilter(new CSVFilter(myModel.language));

											int returnVal = fc.showOpenDialog(frmEVPPI);
											if (returnVal == JFileChooser.APPROVE_OPTION) {
												File file = fc.getSelectedFile();
												String path=file.getAbsolutePath();

												int numIterations=FileUtils.getLineCount(path, false);
												PSAResults psaResults=new PSAResults(myModel, numIterations);

												psaResults.importResults(path, myModel, frmEVPPI, progress);
												psaResults.summarizeResults();
												evppi.valid=psaResults.valid;
												evppi.getPSAResults(psaResults);
											}

											//check # iterations vs outer X inner
											int numTotal=numOuter*numInner*evppi.numSelected;
											if(numTotal != evppi.numIterations) {
												//Error: Expected [total] iterations ([num] parameters X [outer] X [inner]), but got [iterations]
												String msg = MessageFormat.format(myModel.language.message.getString("err.import_psa_iterations"), 
														numTotal, evppi.numSelected, numOuter, numInner, evppi.numIterations);
												JOptionPane.showMessageDialog(frmEVPPI, msg);
											}
											else {
												evppi.estimateTwoLevelMonteCarlo(numOuter, numInner);
											}
										}
									}
									else { //PSA-based method (Bins)
										int numBins=Integer.parseInt(textInner.getText().replaceAll(",", ""));

										if(source==0) { //run new PSA
											int numIterations=Integer.parseInt(textOuter.getText().replaceAll(",", ""));
											boolean useSeed=false;
											int seed=-1;
											if(chckbxSeed.isSelected()){
												useSeed=true;
												seed=Integer.parseInt(textSeed.getText());
											}

											PSAResults psaResults=new PSAResults(myModel, numIterations);
											psaResults.runPSA(myModel, frmEVPPI, useSeed, seed, progress);
											psaResults.summarizeResults();
											evppi.valid=psaResults.valid;
											evppi.getPSAResults(psaResults);
										}
										else { //import PSA results
											JFileChooser fc=new JFileChooser();
											fc.setDialogTitle(myModel.language.analysis.getString("voi.import_psa_results")); //Import PSA Results
											fc.setApproveButtonText(myModel.language.base.getString("menu.import")); //Import
											fc.setFileFilter(new CSVFilter(myModel.language));

											int returnVal = fc.showOpenDialog(frmEVPPI);
											if (returnVal == JFileChooser.APPROVE_OPTION) {
												File file = fc.getSelectedFile();
												String path=file.getAbsolutePath();

												int numIterations=FileUtils.getLineCount(path, false);
												PSAResults psaResults=new PSAResults(myModel, numIterations);

												psaResults.importResults(path, myModel, frmEVPPI, progress);
												psaResults.summarizeResults();
												evppi.valid=psaResults.valid;
												evppi.getPSAResults(psaResults);
											}
										}

										evppi.estimateBins(numBins);
									}

									//update display
									if(evppi.valid==true){

										//Update EVPPI bins chart
										if(method==1) {
											tabbedPane.setEnabledAt(1, true);
											updateEVPPIBinsChart();
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

										//Print results summary to textpane
										HTMLEditorKit kit = new HTMLEditorKit();
										textEVPI.setEditorKit(kit);

										//add html styles
										StyleSheet styleSheet = kit.getStyleSheet();
										styleSheet.addRule("th {border-bottom: 1px solid black}");

										String strReport="";
										strReport+=("<html><body><b>"+myModel.language.analysis.getString("voi.evppi_report")+"</b><br>"); //EVPPI Report

										//sim info
										strReport+=myModel.getSimInfoHTML();

										strReport+=(myModel.language.analysis.getString("voi.evppi_method")+":\t"+strMethod+"<br><br>"); //EVPPI Method

										int numDecimals=4;
										if(myModel.dimInfo.analysisType==0) { //EV
											numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.objectiveDim];
										}
										else { //CEA/BCA
											numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.costDim];
										}

										//evppi table
										strReport+=("<table>");
										strReport+=("<caption>"+myModel.language.analysis.getString("voi_evppi_full")+"</caption>"); //Expected Value of Partial Perfect Information
										strReport+=("<tr><th>"+myModel.language.base.getString("object.parameter")+"</th>" //Parameter
												+ "<th>"+myModel.language.base.getString("object.expression")+"</th>" //Expression
												+ "<th>"+myModel.language.analysis.getString("voi.evppi")+"</th></tr>"); //EVPPI
										for(int p=0; p<numParams; p++) {
											if(evppi.paramSelected[p]) {
												Parameter curParam=myModel.parameters.get(p);
												int curDim=paramDims[p];
												if(curDim==1) { //scalar
													strReport+=("<tr><td>"+curParam.name+"</td>");
													strReport+=("<td>"+curParam.expression+"</td>");
													strReport+=("<td align=\"right\">"+MathUtils.round(evppi.evppi[p][0],numDecimals)+"</td></tr>");
												}
												else { //matrix
													Numeric val=curParam.value;
													for(int z=0; z<curDim; z++) {
														strReport+=("<tr><td>"+curParam.name+val.getDimLbl(z)+"</td>");
														strReport+=("<td>"+curParam.expression+"</td>");
														strReport+=("<td align=\"right\">"+MathUtils.round(evppi.evppi[p][z],numDecimals)+"</td></tr>");
													}
												}
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

									frmEVPPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
									btnRun.setEnabled(true);

								} catch (Exception e) {
									e.printStackTrace();
									frmEVPPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
									btnRun.setEnabled(true);
									JOptionPane.showMessageDialog(frmEVPPI, e.getMessage());
									myModel.errorLog.recordError(e);
								}
							}
						};
						SimThread.start();
					} //end check if runVOI 
				} 
			});

			lblOuter = new JLabel(myModel.language.analysis.getString("voi.num_outer")+":");
			GridBagConstraints gbc_lblOuter = new GridBagConstraints();
			gbc_lblOuter.anchor = GridBagConstraints.EAST;
			gbc_lblOuter.insets = new Insets(0, 0, 5, 5);
			gbc_lblOuter.gridx = 0;
			gbc_lblOuter.gridy = 2;
			panel_2.add(lblOuter, gbc_lblOuter);

			textOuter = new JTextField();
			textOuter.setHorizontalAlignment(SwingConstants.CENTER);
			textOuter.setText("100");
			GridBagConstraints gbc_textOuter = new GridBagConstraints();
			gbc_textOuter.anchor = GridBagConstraints.NORTH;
			gbc_textOuter.fill = GridBagConstraints.HORIZONTAL;
			gbc_textOuter.insets = new Insets(0, 0, 5, 5);
			gbc_textOuter.gridx = 1;
			gbc_textOuter.gridy = 2;
			panel_2.add(textOuter, gbc_textOuter);
			textOuter.setColumns(10);

			
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter(myModel.language));

						int tabIndex=tabbedPane.getSelectedIndex();
						if(tabIndex==1) {
							fc.setDialogTitle(myModel.language.analysis.getString("voi.export_evppi_bin_results")); //Export EVPPI Bin Results
						}
						else if(tabIndex>=2) {
							fc.setDialogTitle(myModel.language.base.getString("title.export_psa_results")); //Export PSA Results
						}
						fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export

						int returnVal = fc.showSaveDialog(frmEVPPI);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);

							if(tabIndex==1) { //EVPPI Bin estimates
								//Headers
								out.write(myModel.language.analysis.getString("voi.num_bins")); //# Bins
								for(int p=0; p<numParams; p++) {
									int curDim=paramDims[p];
									if(curDim==1) { //scalar
										out.write(",\""+paramNames[p]+"\"");
									}
									else { //matrix
										Numeric val=myModel.parameters.get(p).value;
										for(int z=0; z<curDim; z++) {
											out.write(",\""+paramNames[p]+val.getDimLbl(z)+"\"");
										}
									}
								}
								out.newLine();
								int numBins=evppi.evppiBins[0][0][0].length;
								for(int b=0; b<numBins; b++) {
									out.write(evppi.evppiBins[0][0][0][b]+"");
									for(int p=0; p<numParams; p++) {
										int curDim=paramDims[p];
										for(int z=0; z<curDim; z++) {
											out.write(","+evppi.evppiBins[p][z][1][b]);
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
								out.write(myModel.language.base.getString("plot.iteration")); //Iteration
								out.write(","+myModel.language.base.getString("object.parameters")); //Parameters
								for(int p=0; p<numParams; p++) {
									int curDim=paramDims[p];
									if(curDim==1) { //scalar
										out.write(",\""+paramNames[p]+"\"");
									}
									else { //matrix
										Numeric val=myModel.parameters.get(p).value;
										for(int z=0; z<curDim; z++) {
											out.write(",\""+paramNames[p]+val.getDimLbl(z)+"\"");
										}
									}
								}
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

								int group=0;
								if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

								//Results
								int numPoints=evppi.dataResultsIter[group][0][0][0].length;
								for(int i=0; i<numPoints; i++){
									if(evppi.method==0) { //2-level Monte carlo
										int outer=i/evppi.numInner;
										int inner=i%evppi.numInner;
										out.write(outer+"-"+inner); //outer-inner
									}
									else { //PSA
										out.write((i+1)+""); //Iteration
									}
									//Parameters
									out.write(",");
									for(int p=0; p<numParams; p++){
										int curDim=paramDims[p];
										for(int z=0; z<curDim; z++) {
											out.write(","+evppi.dataParamsIter[p][z][1][i]);
										}
									}
									//Outcomes
									for(int d=0; d<numDim; d++){ //EVs
										out.write(",");
										for(int s=0; s<numStrat; s++){out.write(","+evppi.dataResultsIter[group][d][s][1][i]);}
									}
									if(analysisType>0){
										out.write(",");
										if(analysisType==1){ //CEA
											for(int s=0; s<numStrat; s++){
												double icer=evppi.dataResultsIter[group][numDim][s][1][i];
												if(!Double.isNaN(icer)){out.write(","+icer);} //valid ICER
												else{out.write(","+evppi.CEAnotes[group][s][i]);} //invalid ICER
											}
										}
										else if(analysisType==2){ //BCA
											for(int s=0; s<numStrat; s++){out.write(","+evppi.dataResultsIter[group][numDim][s][1][i]);}
										}
									}

									out.newLine();
								}


							}

							out.close();
							JOptionPane.showMessageDialog(frmEVPPI, myModel.language.message.getString("info.exported")); //exported
						}

					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmEVPPI, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});

			chckbxSeed = new JCheckBox(myModel.language.analysis.getString("sim.seed")); //Seed
			chckbxSeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxSeed.isSelected()){textSeed.setEnabled(true);}
					else{textSeed.setEnabled(false);}
				}
			});
			GridBagConstraints gbc_chckbxSeed = new GridBagConstraints();
			gbc_chckbxSeed.anchor = GridBagConstraints.EAST;
			gbc_chckbxSeed.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxSeed.gridx = 3;
			gbc_chckbxSeed.gridy = 2;
			panel_2.add(chckbxSeed, gbc_chckbxSeed);

			textSeed = new JTextField();
			textSeed.setEnabled(false);
			GridBagConstraints gbc_textSeed = new GridBagConstraints();
			gbc_textSeed.fill = GridBagConstraints.HORIZONTAL;
			gbc_textSeed.insets = new Insets(0, 0, 0, 5);
			gbc_textSeed.gridx = 4;
			gbc_textSeed.gridy = 2;
			panel_2.add(textSeed, gbc_textSeed);
			textSeed.setColumns(10);
			GridBagConstraints gbc_btnExport = new GridBagConstraints();
			gbc_btnExport.anchor = GridBagConstraints.NORTH;
			gbc_btnExport.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnExport.insets = new Insets(0, 0, 5, 0);
			gbc_btnExport.gridx = 6;
			gbc_btnExport.gridy = 2;
			panel_2.add(btnExport, gbc_btnExport);

			lblInner = new JLabel(myModel.language.analysis.getString("voi.num_inner")+":");
			GridBagConstraints gbc_lblInner = new GridBagConstraints();
			gbc_lblInner.anchor = GridBagConstraints.EAST;
			gbc_lblInner.insets = new Insets(0, 0, 0, 5);
			gbc_lblInner.gridx = 0;
			gbc_lblInner.gridy = 3;
			panel_2.add(lblInner, gbc_lblInner);

			textInner = new JTextField();
			textInner.setText("1000");
			textInner.setHorizontalAlignment(SwingConstants.CENTER);
			textInner.setColumns(10);
			GridBagConstraints gbc_textInner = new GridBagConstraints();
			gbc_textInner.anchor = GridBagConstraints.NORTH;
			gbc_textInner.fill = GridBagConstraints.HORIZONTAL;
			gbc_textInner.insets = new Insets(0, 0, 0, 5);
			gbc_textInner.gridx = 1;
			gbc_textInner.gridy = 3;
			panel_2.add(textInner, gbc_textInner);
			
			
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
			frmEVPPI.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panelEVPPI = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("voi.evppi"), null, panelEVPPI, null); //EVPPI
			GridBagLayout gbl_panelEVPPI = new GridBagLayout();
			gbl_panelEVPPI.columnWidths = new int[]{0, 0};
			gbl_panelEVPPI.rowHeights = new int[]{0, 0, 0};
			gbl_panelEVPPI.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelEVPPI.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelEVPPI.setLayout(gbl_panelEVPPI);

			JToolBar toolBar = new JToolBar();
			toolBar.setRollover(true);
			toolBar.setFloatable(false);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.WEST;
			gbc_toolBar.insets = new Insets(0, 0, 5, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			panelEVPPI.add(toolBar, gbc_toolBar);

			
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
			panelEVPPI.add(scrollPane, gbc_scrollPane);

			textEVPI = new JTextPane();
			textEVPI.setFont(new Font("Consolas", Font.PLAIN, 14));
			textEVPI.setContentType("text/html");
			textEVPI.setEditable(false);
			scrollPane.setViewportView(textEVPI);

			JPanel panelEVPPI_Bins = new JPanel();
			tabbedPane.addTab(myModel.language.analysis.getString("voi.evppi_bins"), null, panelEVPPI_Bins, null); //EVPPI Bins
			tabbedPane.setEnabledAt(1, false);
			GridBagLayout gbl_panelEVPPI_Bins = new GridBagLayout();
			gbl_panelEVPPI_Bins.columnWidths = new int[]{0, 0};
			gbl_panelEVPPI_Bins.rowHeights = new int[]{0, 0};
			gbl_panelEVPPI_Bins.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panelEVPPI_Bins.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panelEVPPI_Bins.setLayout(gbl_panelEVPPI_Bins);

			chartDataEVPPI_Bins = new DefaultXYDataset();
			chartEVPPI_Bins = ChartFactory.createScatterPlot(null, myModel.language.analysis.getString("voi.num_bins"), myModel.language.analysis.getString("voi.evppi"), //# of Bins, EVPPI 
					chartDataEVPPI_Bins, PlotOrientation.VERTICAL, true, false, false);
			chartEVPPI_Bins.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartEVPPI_Bins.getXYPlot().addDomainMarker(marker);
			chartEVPPI_Bins.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChartEVPPI_Bins = new ChartPanel(chartEVPPI_Bins,false);
			GridBagConstraints gbc_panelChartEVPPI_Bins = new GridBagConstraints();
			gbc_panelChartEVPPI_Bins.fill = GridBagConstraints.BOTH;
			gbc_panelChartEVPPI_Bins.gridx = 0;
			gbc_panelChartEVPPI_Bins.gridy = 0;
			panelEVPPI_Bins.add(panelChartEVPPI_Bins, gbc_panelChartEVPPI_Bins);

			//pop-up menu
			JPopupMenu popup = panelChartEVPPI_Bins.getPopupMenu();
			JMenuItem mntmChangeColor = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartEVPPI_Bins, chartDataEVPPI_Bins, seriesPaints_Bins, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);


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

			//pop-up menu
			popup = panelChartResults.getPopupMenu();
			JMenuItem mntmChangeColorStrat = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorStrat.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartResults, chartDataResults, seriesPaints_Strat, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorStrat, 0);


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
			scrollPaneSelectParams.setViewportView(listParams);
			int selectedIndices[]=new int[numParams];
			for(int v=0; v<numParams; v++){selectedIndices[v]=v;}
			listParams.setSelectedIndices(selectedIndices); //Select all

			JButton btnUpdateChart = new JButton(myModel.language.base.getString("plot.update_plot")); //Update Plot
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
			//comboParams.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","Iteration"}));
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

			ChartPanel panelChartParams = new ChartPanel(chartParams,false);
			GridBagConstraints gbc_panelChartParams = new GridBagConstraints();
			gbc_panelChartParams.fill = GridBagConstraints.BOTH;
			gbc_panelChartParams.gridx = 1;
			gbc_panelChartParams.gridy = 0;
			panelParams.add(panelChartParams, gbc_panelChartParams);

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

			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				subgroupNames=new String[numGroups+1];
				subgroupNames[0]=myModel.language.analysis.getString("result.overall"); //Overall
				for(int i=0; i<numGroups; i++){subgroupNames[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(subgroupNames));
				comboGroup.setVisible(true);
			}

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	public void updateEVPPIBinsChart(){
		while(chartDataEVPPI_Bins.getSeriesCount()>0){
			chartDataEVPPI_Bins.removeSeries(chartDataEVPPI_Bins.getSeriesKey(0));
		}

		XYPlot plotResults = chartEVPPI_Bins.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,true);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		Shape circle=new Ellipse2D.Double(-2.5,-2.5,5,5);
		int i=0;
		for(int p=0; p<numParams; p++){
			int curDim=paramDims[p];
			for(int z=0; z<curDim; z++) {
				rendererResults.setSeriesPaint(i, seriesPaints_Bins[p]);
				rendererResults.setSeriesShape(i, circle);
				i++;
			}
		}
		plotResults.setRenderer(rendererResults);

		for(int p=0; p<numParams; p++){
			Parameter curParam=myModel.parameters.get(p);
			int curDim=paramDims[p];
			if(curDim==1) { //scalar
				chartDataEVPPI_Bins.addSeries(curParam.name, evppi.evppiBins[p][0]);
			}
			else { //matrix
				Numeric val=curParam.value;
				for(int z=0; z<curDim; z++) {
					chartDataEVPPI_Bins.addSeries(curParam.name+val.getDimLbl(z), evppi.evppiBins[p][z]);
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
		if(analysisType==0){outcome=myModel.language.analysis.getString("result.ev")+" ("+info.dimSymbols[dim]+")";} //EV
		else if(analysisType==1){outcome=myModel.language.analysis.getString("cea.icer")+" ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")";} //ICER
		else if(analysisType==2){outcome=myModel.language.analysis.getString("bca.nmb")+" ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")";} //NMB
		if(chartDataResults.getSeriesCount()>0){
			for(int s=0; s<evppi.numStrat; s++){
				chartDataResults.removeSeries(myModel.strategyNames[s]);
			}
		}
		XYPlot plotResults = chartResults.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<evppi.numStrat; s++){
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
				double kde[][]=KernelSmooth.density(evppi.dataResultsIter[group][dim][s][1], 100);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==1){ //Histogram
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.frequency")); //Frequency
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.histogram(evppi.dataResultsIter[group][dim][s][1], 100, 10);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==2){ //CDF
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.cumulative_distribution")); //Cumulative Distribution
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],evppi.dataResultsCumDens[group][dim][s]);
			}

		}
		else if(selected==3){ //Quantile
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.quantile")); //Quantile
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],evppi.dataResultsVal[group][dim][s]);
			}
		}
		else if(selected==4){ //Iteration
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.iteration")); //Iteration
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],evppi.dataResultsIter[group][dim][s]);
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
						double kde[][]=KernelSmooth.density(evppi.dataParamsIter[v][0][1], 100);
						chartDataParams.addSeries(paramNames[v],kde);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double kde[][]=KernelSmooth.density(evppi.dataParamsIter[v][z][1], 100);
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
						double hist[][]=KernelSmooth.histogram(evppi.dataParamsIter[v][0][1], 100, 10);
						chartDataParams.addSeries(paramNames[v],hist);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double hist[][]=KernelSmooth.histogram(evppi.dataParamsIter[v][z][1], 100, 10);
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
						chartDataParams.addSeries(paramNames[v],evppi.dataParamsCumDens[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, evppi.dataParamsCumDens[v][z]);
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
						chartDataParams.addSeries(paramNames[v],evppi.dataParamsVal[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, evppi.dataParamsVal[v][z]);
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
						chartDataParams.addSeries(paramNames[v],evppi.dataParamsIter[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, evppi.dataParamsIter[v][z]);
						}
					}
				}
			}
		}
	}


}


