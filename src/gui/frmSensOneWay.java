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
import java.awt.Toolkit;

import javax.swing.JTable;
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
import filters.CSVFilter;
import main.CEAHelper;
import main.DimInfo;
import main.Parameter;
import math.Numeric;

import javax.swing.border.LineBorder;
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
public class frmSensOneWay {

	public JFrame frmSensOneWay;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	DefaultXYDataset chartData;
	JFreeChart chart;
	JComboBox<String> comboDimensions;
	int numStrat;
	/**
	 * [Outcome][Strategy][x,y][Iteration]
	 */
	double results[][][][];
	private JTextField textIntervals;
	String CEAnotes[][];
	Parameter curParam;
	
	public frmSensOneWay(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmSensOneWay = new JFrame();
			frmSensOneWay.setTitle("Amua - One-way Sensitivity Analysis");
			frmSensOneWay.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmSensOneWay.setBounds(100, 100, 1000, 499);
			frmSensOneWay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 180, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmSensOneWay.getContentPane().setLayout(gridBagLayout);
			
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
			
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setEnabled(false);
			comboDimensions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			GridBagConstraints gbc_comboDimensions = new GridBagConstraints();
			gbc_comboDimensions.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboDimensions.insets = new Insets(0, 0, 5, 5);
			gbc_comboDimensions.gridx = 1;
			gbc_comboDimensions.gridy = 0;
			frmSensOneWay.getContentPane().add(comboDimensions, gbc_comboDimensions);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.gridheight = 2;
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmSensOneWay.getContentPane().add(panel_1, gbc_panel_1);
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
				modelParams.setValueAt(myModel.parameters.get(i).name, i, 0);
				modelParams.setValueAt(myModel.parameters.get(i).expression, i, 1);
			}

			JScrollPane scrollPaneParams = new JScrollPane();
			GridBagConstraints gbc_scrollPaneParams = new GridBagConstraints();
			gbc_scrollPaneParams.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneParams.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneParams.gridx = 0;
			gbc_scrollPaneParams.gridy = 0;
			panel_1.add(scrollPaneParams, gbc_scrollPaneParams);
			tableParams = new JTable();
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

						fc.setDialogTitle("Export Graph Data");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmSensOneWay);
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
							out.write(chart.getXYPlot().getDomainAxis().getLabel()); //Param name
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
							int numPoints=results[0][0][0].length;
							for(int i=0; i<numPoints; i++){
								out.write(results[0][0][0][i]+""); //Param value
								for(int d=0; d<numDim; d++){ //EVs
									out.write(",");
									for(int s=0; s<numStrat; s++){out.write(","+results[d][s][1][i]);}
								}
								if(analysisType>0){
									out.write(",");
									if(analysisType==1){ //CEA
										for(int s=0; s<numStrat; s++){
											double icer=results[numDim][s][1][i];
											if(!Double.isNaN(icer)){out.write(","+icer);} //valid ICER
											else{out.write(","+CEAnotes[s][i]);} //invalid ICER
										}
									}
									else if(analysisType==2){ //BCA
										for(int s=0; s<numStrat; s++){out.write(","+results[numDim][s][1][i]);}
									}
								}
								out.newLine();
							}
							out.close();

							JOptionPane.showMessageDialog(frmSensOneWay, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmSensOneWay, ex.getMessage());
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
					final ProgressMonitor progress=new ProgressMonitor(frmSensOneWay, "One-way sensitivity", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmSensOneWay, "Errors in base case model!");
								}
								else{
									//Get parameter
									int intervals=Integer.parseInt(textIntervals.getText());
									int row=tableParams.getSelectedRow();
									String strMin=(String)tableParams.getValueAt(row, 2);
									String strMax=(String)tableParams.getValueAt(row, 3);
									strMin=strMin.replaceAll(",", ""); //Replace any commas
									strMax=strMax.replaceAll(",", "");
									double min=Double.parseDouble(strMin);
									double max=Double.parseDouble(strMax);
									double step=(max-min)/(intervals*1.0);
									curParam=myModel.parameters.get(row);
									curParam.locked=true;
									Numeric origValue=curParam.value.copy();
									
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
										myModel.validateParamsVars();
										JOptionPane.showMessageDialog(frmSensOneWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curParam.locked=false;
										myModel.validateParamsVars();
										JOptionPane.showMessageDialog(frmSensOneWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										numStrat=myModel.getStrategies();
										int numOutcomes=comboDimensions.getItemCount();
										int numDim=myModel.dimInfo.dimNames.length;
										results=new double[numOutcomes][numStrat][2][intervals+1];
										progress.setMaximum(intervals+1);
										int analysisType=myModel.dimInfo.analysisType;
										if(analysisType==1){CEAnotes=new String[numStrat][intervals+1];} //CEA
										else{CEAnotes=null;}
										
										for(int i=0; i<=intervals; i++){
											double curVal=min+(step*i);
											curParam.value.setDouble(curVal);
											curParam.locked=true;
											myModel.parseModel();
											myModel.runModel(null, false);
											
											//Get EVs
											for(int d=0; d<numDim; d++){
												for(int s=0; s<numStrat; s++){
													results[d][s][0][i]=curVal;
													results[d][s][1][i]=myModel.getStrategyEV(s, d);
												}
											}
											if(analysisType>0){ //CEA or BCA
												if(analysisType==1){ //CEA
													Object table[][]=new CEAHelper().calculateICERs(myModel);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat!=-1){
															results[numDim][origStrat][0][i]=curVal;
															results[numDim][origStrat][1][i]=(double) table[s][4];
															CEAnotes[origStrat][i]=(String) table[s][5];
														}
													}
												}
												else if(analysisType==2){
													Object table[][]=new CEAHelper().calculateNMB(myModel);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														results[numDim][origStrat][0][i]=curVal;
														results[numDim][origStrat][1][i]=(double) table[s][4];
													}
												}
											}
											
											progress.setProgress(i);
											if(progress.isCanceled()){ //End loop
												cancelled=true;
												i=intervals+1;
											}
										}
										//Reset param value
										curParam.value=origValue;
										curParam.locked=false;
										myModel.validateParamsVars();
										
										if(cancelled==false){
											updateChart();
											if(numOutcomes>1){
												comboDimensions.setEnabled(true);
											}
											btnExport.setEnabled(true);
										}
										progress.close();
									}
								}
							} catch (Exception e) {
								curParam.locked=false;
								myModel.validateParamsVars();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmSensOneWay, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			chartData = new DefaultXYDataset();
			chart = ChartFactory.createScatterPlot(null, "x", "EV($)", chartData, PlotOrientation.VERTICAL, true, false, false);
			chart.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chart.getXYPlot().addDomainMarker(marker);
			chart.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChart = new ChartPanel(chart);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.gridwidth = 2;
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 1;
			gbc_panelChart.gridy = 1;
			frmSensOneWay.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
	
	private void updateChart(){
		DimInfo info=myModel.dimInfo;
		
		int dim=comboDimensions.getSelectedIndex();
		int analysisType=0; //EV
		if(myModel.dimInfo.analysisType>0){ //CEA or BCA
			if(dim==comboDimensions.getItemCount()-1){ //ICER or NMB selected
				analysisType=myModel.dimInfo.analysisType;
			}
		}
		
		//Update chart
		chart.getXYPlot().getDomainAxis().setLabel(curParam.name);
		if(analysisType==0){chart.getXYPlot().getRangeAxis().setLabel("EV ("+info.dimSymbols[dim]+")");}
		else if(analysisType==1){chart.getXYPlot().getRangeAxis().setLabel("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");}
		else if(analysisType==2){chart.getXYPlot().getRangeAxis().setLabel("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");}
		
		if(chartData.getSeriesCount()>0){
			for(int s=0; s<numStrat; s++){
				chartData.removeSeries(myModel.strategyNames[s]);
			}
		}
		for(int s=0; s<numStrat; s++){
			chartData.addSeries(myModel.strategyNames[s],results[dim][s]);
		}
		XYPlot plot = chart.getXYPlot();

		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true,false);
		DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
		for(int s=0; s<numStrat; s++){
			renderer1.setSeriesPaint(s, supplier.getNextPaint());
		}
		plot.setRenderer(renderer1);
	}
}
