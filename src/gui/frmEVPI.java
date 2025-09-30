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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
import voi.EVPI;

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
	String paramNames[];
	int paramDims[];

	DefaultTableModel modelParams;
	private JTable tableParams;

	JTabbedPane tabbedPane;
	DefaultXYDataset chartDataResults, chartDataParams;
	JFreeChart chartResults, chartParams;
	Paint seriesPaints_Strat[], seriesPaints_Params[];
	int numSeries_Params;

	JComboBox<String> comboDimensions;
	JComboBox<String> comboResults;
	JComboBox<String> comboGroup;
	String subgroupNames[];

	JComboBox<String> comboParams;

	PSAResults psaResults;

	JTextPane textEVPI;

	boolean exportReady=false;

	int analysisType;

	JList<String> listParams;
	DefaultListModel<String> listModelParams;
	JComboBox comboSource;
	JLabel lblIterations;
	private JTextField textIterations;
	int numIterations;
	JCheckBox chckbxSeed;
	private JTextField textSeed;
	JButton btnRun;
	String outcome;

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
			frmEVPI.setTitle("Amua - "+myModel.language.analysis.getString("voi.evpi_full")); //Expected Value of Perfect Information (EVPI)
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
					new String[] {myModel.language.base.getString("object.parameter"), myModel.language.base.getString("object.expression")}){ //Parameter,  Expression
				boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
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
			seriesPaints_Params=new Paint[numParams];
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
				seriesPaints_Params[i]=supplierParams.getNextPaint();
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
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 0;
			gbc_panel_2.gridy = 1;
			panel_1.add(panel_2, gbc_panel_2);

			String outcomes[]=myModel.dimInfo.getOutcomes();
			GridBagLayout gbl_panel_2 = new GridBagLayout();
			gbl_panel_2.columnWidths = new int[]{75, 69, 81, 59, 51, 90, 0};
			gbl_panel_2.rowHeights = new int[]{28, 28, 0};
			gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			panel_2.setLayout(gbl_panel_2);

			
			String source[]=new String[2];
			source[0]=myModel.language.analysis.getString("voi.run_new_psa"); //Run New PSA
			source[1]=myModel.language.analysis.getString("voi.import_psa_results"); //Import PSA Results

			comboSource = new JComboBox();
			comboSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(comboSource.getSelectedIndex()==0) { //run new PSA
						lblIterations.setEnabled(true);
						textIterations.setEnabled(true);
						chckbxSeed.setEnabled(true);
						if(chckbxSeed.isSelected()) {
							textSeed.setEnabled(true);
						}
						btnRun.setText(myModel.language.base.getString("menu.run")); //Run
					}
					else { //import existing PSA
						lblIterations.setEnabled(false);
						textIterations.setEnabled(false);
						chckbxSeed.setEnabled(false);
						textSeed.setEnabled(false);
						btnRun.setText(myModel.language.base.getString("menu.import")); //Import
					}
				}
			});
			comboSource.setModel(new DefaultComboBoxModel(source));
			GridBagConstraints gbc_comboSource = new GridBagConstraints();
			gbc_comboSource.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboSource.insets = new Insets(0, 0, 5, 5);
			gbc_comboSource.gridwidth = 3;
			gbc_comboSource.gridx = 1;
			gbc_comboSource.gridy = 0;
			panel_2.add(comboSource, gbc_comboSource);

			JLabel lblSource = new JLabel(myModel.language.analysis.getString("gen.source")+":");
			GridBagConstraints gbc_lblSource = new GridBagConstraints();
			gbc_lblSource.anchor = GridBagConstraints.EAST;
			gbc_lblSource.insets = new Insets(0, 0, 5, 5);
			gbc_lblSource.gridx = 0;
			gbc_lblSource.gridy = 0;
			panel_2.add(lblSource, gbc_lblSource);
			
			
			final JButton btnExport = new JButton(myModel.language.base.getString("menu.export")); //Export
			JButton btnCopy = new JButton(myModel.language.base.getString("menu.copy")); //Copy

			btnRun = new JButton(myModel.language.base.getString("menu.run"));
			GridBagConstraints gbc_btnRun = new GridBagConstraints();
			gbc_btnRun.anchor = GridBagConstraints.NORTH;
			gbc_btnRun.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnRun.insets = new Insets(0, 0, 5, 0);
			gbc_btnRun.gridx = 5;
			gbc_btnRun.gridy = 0;
			panel_2.add(btnRun, gbc_btnRun);

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmEVPI, myModel.language.analysis.getString("gen.psa"), myModel.language.message.getString("info.running_PSA"), 0, 100); //PSA, Running PSA

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{

								frmEVPI.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								btnRun.setEnabled(false);

								int source=comboSource.getSelectedIndex();
								if(source==0) { //run PSA
									numIterations=Integer.parseInt(textIterations.getText().replaceAll(",", ""));
									psaResults=new PSAResults(myModel, numIterations);

									boolean useSeed=false;
									int seed=-1;
									if(chckbxSeed.isSelected()){
										useSeed=true;
										seed=Integer.parseInt(textSeed.getText());
									}

									psaResults.runPSA(myModel, frmEVPI, useSeed, seed, progress);

								}
								else if(source==1) { //import PSA
									//select file to import
									JFileChooser fc=new JFileChooser();
									fc.setDialogTitle(myModel.language.analysis.getString("voi.import_psa_results")); //Import PSA Results 
									fc.setApproveButtonText(myModel.language.base.getString("menu.import")); //Import
									fc.setFileFilter(new CSVFilter(myModel.language));

									int returnVal = fc.showOpenDialog(frmEVPI);
									if (returnVal == JFileChooser.APPROVE_OPTION) {
										File file = fc.getSelectedFile();
										String path=file.getAbsolutePath();

										numIterations=FileUtils.getLineCount(path, false);
										psaResults=new PSAResults(myModel, numIterations);

										psaResults.importResults(path, myModel, frmEVPI, progress);

									}
								}

								if(psaResults!=null && psaResults.valid==true) {
									psaResults.summarizeResults();
									EVPI evpi = new EVPI(psaResults);
									evpi.calculate();

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
									strReport+=("<html><body><b>"+myModel.language.analysis.getString("voi.evpi_report")+"</b><br>"); //EVPI Report

									//sim info
									strReport+=myModel.getSimInfoHTML();
									strReport+=(myModel.language.analysis.getString("voi.evpi_iterations")+":\t"+numIterations+"<br><br>"); //EVPI Iterations
									String lblObj=myModel.language.math.getString("sum.max").toLowerCase(myModel.language.locale); //max
									String lblOutcome=myModel.language.analysis.getString("bca.nmb"); //NMB
									int numDecimals=4;
									if(myModel.dimInfo.analysisType==0) { //EV
										lblOutcome=myModel.dimInfo.dimSymbols[myModel.dimInfo.objectiveDim];
										numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.objectiveDim];
										if(myModel.dimInfo.objective==1) {
											lblObj=myModel.language.math.getString("sum.min").toLowerCase(myModel.language.locale); //min
											evpi.bestOutcome=-evpi.bestOutcome; //flip signs
											evpi.bestMean=-evpi.bestMean;
										}
									}
									else { //CEA/BCA
										numDecimals=myModel.dimInfo.decimals[myModel.dimInfo.costDim];
										if(myModel.dimInfo.objective==1) {
											lblObj=myModel.language.math.getString("sum.min").toLowerCase(myModel.language.locale); //min
											evpi.bestOutcome=-evpi.bestOutcome; //flip signs
											evpi.bestMean=-evpi.bestMean;
										}
									}

									//evpi table
									strReport+=("<table>");
									strReport+=("<caption>"+myModel.language.analysis.getString("voi.evpi_full")+"</caption>"); //Expected Value of Perfect Information
									strReport+=("<tr><th>"+myModel.language.analysis.getString("voi.estimand")+"</th><th>"+lblOutcome+"</th></tr>"); //Estimand
									strReport+=("<tr><td>E["+lblObj+"(\u00B7)] ("+myModel.language.analysis.getString("voi.perfect_information")+")</td><td align=\"right\">"+MathUtils.round(evpi.bestOutcome,numDecimals)+"</td></tr>"); //Perfect Information
									strReport+=("<tr><td>"+lblObj+"(E[\u00B7]) ("+myModel.strategyNames[evpi.bestStrat]+")</td><td align=\"right\">"+MathUtils.round(evpi.bestMean,numDecimals)+"</td></tr>");
									strReport+=("<tr><td>"+myModel.language.analysis.getString("voi.evpi_difference")+"</td><td align=\"right\">"+MathUtils.round(evpi.evpi,numDecimals)+"</td></tr>"); //EVPI (Difference)
									strReport+=("</table>");
									strReport+=("<br><br>");

									//p(Best)|PI table
									strReport+=("<table>");
									strReport+=("<caption>"+myModel.language.analysis.getString("voi.pbest")+"</caption>"); //p(Best)
									strReport+=("<tr><th>Strategy</th><th>"+myModel.language.analysis.getString("voi.pbest")+"|"+myModel.language.analysis.getString("voi.perfect_information")+"</th></tr>"); //p(Best)|Perfect Information
									for(int s=0; s<evpi.numStrat; s++) {
										strReport+=("<tr><td>"+myModel.strategyNames[s]+"</td>");
										strReport+=("<td align=\"right\">"+(evpi.numBest[s]/(numIterations*1.0))+"</td></tr>");
									}
									strReport+=("</table>");
									strReport+=("<br><br>");

									//outcome summaries
									strReport+=("<table>");
									strReport+=("<caption>"+myModel.language.analysis.getString("result.outcome_summaries")+"</caption>"); //Outcome Summaries
									strReport+=("<tr><th>"+myModel.language.analysis.getString("gen.strategy")+"</th>" //Strategy
											+ "<th>"+myModel.language.analysis.getString("result.outcome")+"</th>" //Outcome
											+ "<th>"+myModel.language.math.getString("sum.mean")+"</th>" //Mean
											+ "<th>"+myModel.language.math.getString("sum.95_lb")+"</th>" //95% LB
											+ "<th>"+myModel.language.math.getString("sum.95_ub")+"</th></tr>"); //95% UB
									//strategy results - overall
									for(int s=0; s<evpi.numStrat; s++) {
										String stratName=myModel.strategyNames[s];
										if(myModel.dimInfo.analysisType>0) { //NMB
											String dimName=myModel.language.analysis.getString("bca.nmb"); //NMB
											double mean=MathUtils.round(evpi.meanOutcomes[s]*evpi.sign, numDecimals);
											Arrays.sort(psaResults.results[s]);
											double lb=MathUtils.round(psaResults.results[s][psaResults.indexLB],numDecimals);
											double ub=MathUtils.round(psaResults.results[s][psaResults.indexUB],numDecimals);
											strReport+=("<tr><td>"+stratName+"</td><td>"+dimName+"</td>");
											strReport+=("<td align=\"right\">"+mean+"</td>");
											strReport+=("<td align=\"right\">"+lb+"</td>");
											strReport+=("<td align=\"right\">"+ub+"</td></tr>");
										}
										for(int d=0; d<psaResults.numDim; d++){
											String dimName=myModel.dimInfo.dimNames[d];
											if(myModel.type==1 && myModel.markov.discountRewards){dimName+=" "+myModel.language.analysis.getString("result.dis");} //(Dis)
											double mean=MathUtils.round(psaResults.meanResults[0][d][s],myModel.dimInfo.decimals[d]);
											double lb=MathUtils.round(psaResults.lbResults[0][d][s],myModel.dimInfo.decimals[d]);
											double ub=MathUtils.round(psaResults.ubResults[0][d][s],myModel.dimInfo.decimals[d]);
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
								} //end check if valid

								btnRun.setEnabled(true);
								frmEVPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

							} catch (Exception e) {
								e.printStackTrace();
								btnRun.setEnabled(true);
								frmEVPI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								JOptionPane.showMessageDialog(frmEVPI, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			chckbxSeed = new JCheckBox(myModel.language.analysis.getString("sim.seed")); //Seed
			chckbxSeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxSeed.isSelected()){textSeed.setEnabled(true);}
					else{textSeed.setEnabled(false);}
				}
			});

			lblIterations = new JLabel(myModel.language.analysis.getString("sim.num_iterations")+":");
			GridBagConstraints gbc_lblIterations = new GridBagConstraints();
			gbc_lblIterations.anchor = GridBagConstraints.EAST;
			gbc_lblIterations.insets = new Insets(0, 0, 0, 5);
			gbc_lblIterations.gridx = 0;
			gbc_lblIterations.gridy = 1;
			panel_2.add(lblIterations, gbc_lblIterations);

			textIterations = new JTextField();
			textIterations.setHorizontalAlignment(SwingConstants.CENTER);
			textIterations.setText("1000");
			GridBagConstraints gbc_textIterations = new GridBagConstraints();
			gbc_textIterations.anchor = GridBagConstraints.NORTH;
			gbc_textIterations.fill = GridBagConstraints.HORIZONTAL;
			gbc_textIterations.insets = new Insets(0, 0, 0, 5);
			gbc_textIterations.gridx = 1;
			gbc_textIterations.gridy = 1;
			panel_2.add(textIterations, gbc_textIterations);
			textIterations.setColumns(10);
			GridBagConstraints gbc_chckbxSeed = new GridBagConstraints();
			gbc_chckbxSeed.anchor = GridBagConstraints.EAST;
			gbc_chckbxSeed.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxSeed.gridx = 2;
			gbc_chckbxSeed.gridy = 1;
			panel_2.add(chckbxSeed, gbc_chckbxSeed);

			
			btnExport.setEnabled(false);
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter(myModel.language));

						int tabIndex=tabbedPane.getSelectedIndex();
						if(tabIndex>=1) { //psa
							fc.setDialogTitle(myModel.language.base.getString("title.export_psa_results")); //Export PSA Results

							fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export

							int returnVal = fc.showSaveDialog(frmEVPI);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								String path=file.getAbsolutePath();
								path=path.replaceAll(".csv", "");
								//Open file for writing
								FileWriter fstream = new FileWriter(path+".csv"); //Create new file
								BufferedWriter out = new BufferedWriter(fstream);

								int group=0;
								if(comboGroup.isVisible()){group=comboGroup.getSelectedIndex();}

								psaResults.export(myModel, out, group);

								out.close();
								JOptionPane.showMessageDialog(frmEVPI, myModel.language.message.getString("info.exported")); //Exported!
							}
						}

					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmEVPI, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});

			textSeed = new JTextField();
			textSeed.setEnabled(false);
			GridBagConstraints gbc_textSeed = new GridBagConstraints();
			gbc_textSeed.anchor = GridBagConstraints.NORTH;
			gbc_textSeed.fill = GridBagConstraints.HORIZONTAL;
			gbc_textSeed.insets = new Insets(0, 0, 0, 5);
			gbc_textSeed.gridx = 3;
			gbc_textSeed.gridy = 1;
			panel_2.add(textSeed, gbc_textSeed);
			textSeed.setColumns(10);
			GridBagConstraints gbc_btnExport = new GridBagConstraints();
			gbc_btnExport.anchor = GridBagConstraints.NORTH;
			gbc_btnExport.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnExport.gridx = 5;
			gbc_btnExport.gridy = 1;
			panel_2.add(btnExport, gbc_btnExport);
			
			
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
			tabbedPane.addTab(myModel.language.analysis.getString("voi.evpi"), null, panelEVPI, null); //EVPI
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
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartResults, chartDataResults, seriesPaints_Strat, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);


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
			for(int s=0; s<psaResults.numStrat; s++){
				chartDataResults.removeSeries(myModel.strategyNames[s]);
			}
		}
		XYPlot plotResults = chartResults.getXYPlot();
		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
		//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
		for(int s=0; s<psaResults.numStrat; s++){
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
				double kde[][]=KernelSmooth.density(psaResults.dataResultsIter[group][dim][s][1], 100);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==1){ //Histogram
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.frequency")); //Frequency
			for(int s=0; s<numStrat; s++){
				double kde[][]=KernelSmooth.histogram(psaResults.dataResultsIter[group][dim][s][1], 100, 10);
				chartDataResults.addSeries(myModel.strategyNames[s],kde);
			}
		}
		else if(selected==2){ //CDF
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("plot.cumulative_distribution")); //Cumulative Distribution
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],psaResults.dataResultsCumDens[group][dim][s]);
			}

		}
		else if(selected==3){ //Quantile
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.quantile")); //Quantile
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],psaResults.dataResultsVal[group][dim][s]);
			}
		}
		else if(selected==4){ //Iteration
			chartResults.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("plot.iteration")); //Iteration
			chartResults.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("result.value")); //Value
			for(int s=0; s<numStrat; s++){
				chartDataResults.addSeries(myModel.strategyNames[s],psaResults.dataResultsIter[group][dim][s]);
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
						double kde[][]=KernelSmooth.density(psaResults.dataParamsIter[v][0][1], 100);
						chartDataParams.addSeries(paramNames[v],kde);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double kde[][]=KernelSmooth.density(psaResults.dataParamsIter[v][z][1], 100);
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
						double hist[][]=KernelSmooth.histogram(psaResults.dataParamsIter[v][0][1], 100, 10);
						chartDataParams.addSeries(paramNames[v],hist);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							double hist[][]=KernelSmooth.histogram(psaResults.dataParamsIter[v][z][1], 100, 10);
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
						chartDataParams.addSeries(paramNames[v],psaResults.dataParamsCumDens[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, psaResults.dataParamsCumDens[v][z]);
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
						chartDataParams.addSeries(paramNames[v],psaResults.dataParamsVal[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, psaResults.dataParamsVal[v][z]);
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
						chartDataParams.addSeries(paramNames[v],psaResults.dataParamsIter[v][0]);
					}
					else { //matrix
						Numeric val=myModel.parameters.get(v).value;
						for(int z=0; z<curDim; z++) {
							String curLbl=val.getDimLbl(z);
							chartDataParams.addSeries(paramNames[v]+curLbl, psaResults.dataParamsIter[v][z]);
						}
					}
				}
			}
		}
	}



}


