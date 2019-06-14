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
import java.util.Collections;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultIntervalCategoryDataset;

import base.AmuaModel;
import filters.CSVFilter;
import main.CEAHelper;
import main.DimInfo;
import main.Parameter;
import math.Numeric;

import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.ComboBoxModel;

/**
 *
 */
public class frmTornadoDiagram {

	public JFrame frmTornadoDiagram;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	ChartPanel panelChart;
	JComboBox<String> comboStrategy;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboSubgroup;
	JLabel lblSubgroup;

	JFreeChart chart;
	double baseOutcome;
	ArrayList<ParamResult> results;
	Parameter curParam;
	
	public frmTornadoDiagram(AmuaModel model){
		this.myModel=model;
		myModel.getStrategies();
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTornadoDiagram = new JFrame();
			frmTornadoDiagram.setTitle("Amua - Tornado Diagram");
			frmTornadoDiagram.setIconImage(Toolkit.getDefaultToolkit().getImage(frmTornadoDiagram.class.getResource("/images/tornado.png")));
			frmTornadoDiagram.setBounds(100, 100, 1000, 500);
			frmTornadoDiagram.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmTornadoDiagram.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmTornadoDiagram.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{466, 94, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelParams=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Parameter", "Expression","Low","High"}) {
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
			tableParams.setRowSelectionAllowed(false);
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

			final JLabel lblOutcome = new JLabel("Outcome:");
			lblOutcome.setBounds(6, 42, 81, 16);
			panel_2.add(lblOutcome);

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
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setBounds(88, 37, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(355, 5, 90, 28);
			panel_2.add(btnRun);

			JLabel lblStrategy = new JLabel("Strategy:");
			lblStrategy.setBounds(6, 11, 81, 16);
			panel_2.add(lblStrategy);

			comboStrategy = new JComboBox<String>(new DefaultComboBoxModel<String>(myModel.strategyNames));
			comboStrategy.setBounds(88, 6, 227, 26);
			panel_2.add(comboStrategy);
			
			final JButton btnExport = new JButton("Export");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter());

						fc.setDialogTitle("Export Graph Data");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmTornadoDiagram);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							//Headers
							out.write(chart.getCategoryPlot().getRangeAxis().getLabel()+",,"); out.newLine(); //Outcome
							out.write("Base case,"+baseOutcome+","); out.newLine();
							out.write("Parameter,Low,High"); out.newLine();
							int numParams=results.size();
							for(int v=0; v<numParams; v++){
								ParamResult result=results.get(v);
								out.write(result.name+","+result.minVal+","+result.maxVal);
								out.newLine();
							}
							out.close();
							
							JOptionPane.showMessageDialog(frmTornadoDiagram, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmTornadoDiagram, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setEnabled(false);
			btnExport.setBounds(355, 36, 90, 28);
			panel_2.add(btnExport);
			
			lblSubgroup = new JLabel("Subgroup:");
			lblSubgroup.setEnabled(false);
			lblSubgroup.setBounds(6, 71, 81, 16);
			panel_2.add(lblSubgroup);
			
			comboSubgroup = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"Overall"}));
			comboSubgroup.setEnabled(false);
			comboSubgroup.setBounds(88, 66, 227, 26);
			panel_2.add(comboSubgroup);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numSubgroups=myModel.subgroupNames.size();
				String groups[]=new String[numSubgroups+1];
				groups[0]="Overall";
				for(int i=0; i<numSubgroups; i++){groups[i+1]=myModel.subgroupNames.get(i);}
				comboSubgroup.setModel(new DefaultComboBoxModel(groups));
				lblSubgroup.setEnabled(true);
				comboSubgroup.setEnabled(true);
			}
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmTornadoDiagram, "Tornado diagram", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{

								int strat=comboStrategy.getSelectedIndex();
								int dim=comboDimensions.getSelectedIndex();
								int analysisType=0; //EV
								if(dim==comboDimensions.getItemCount()-1){ //ICER or NMB selected
									analysisType=myModel.dimInfo.analysisType;
								} 

								int group=-1; //overall
								if(comboSubgroup.isEnabled()){
									group=comboSubgroup.getSelectedIndex()-1;
								}

								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmTornadoDiagram, "Errors in base case model!");
								}
								else{

									int numRuns=1; //baseline
									int numParams=tableParams.getRowCount();
									for(int p=0; p<numParams; p++){
										String strMin=(String)tableParams.getValueAt(p, 2);
										String strMax=(String)tableParams.getValueAt(p, 3);
										if(strMin!=null && strMin.length()>0 && strMax!=null && strMax.length()>0){
											numRuns+=2;
										}
									}
									progress.setMaximum(numRuns+1);
									int curProg=1;
									progress.setProgress(curProg);

									//Get baseline
									myModel.runModel(null, false);
									curProg++; progress.setProgress(curProg);
									
									if(analysisType==0){ //EV
										if(group==-1){baseOutcome=myModel.getStrategyEV(strat, dim);} //overall
										else{baseOutcome=myModel.getSubgroupEV(group, strat, dim);}
									} 
									else if(analysisType==1){ //CEA
										Object table[][]=new CEAHelper().calculateICERs(myModel,group);
										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											if(origStrat==strat){
												baseOutcome=(double) table[s][4];
											}
										}
									}
									else if(analysisType==2){ //BCA
										Object table[][]=new CEAHelper().calculateNMB(myModel,group);
										for(int s=0; s<table.length; s++){	
											int origStrat=(int) table[s][0];
											if(origStrat==strat){
												baseOutcome=(double) table[s][4];
											}
										}
									}

									results=new ArrayList<ParamResult>();
									double globalMin=Double.POSITIVE_INFINITY, globalMax=Double.NEGATIVE_INFINITY;

									for(int v=0; v<numParams; v++){
										String paramName=(String)tableParams.getValueAt(v, 0);
										String strMin=(String)tableParams.getValueAt(v, 2);
										String strMax=(String)tableParams.getValueAt(v, 3);
										if(strMin!=null && strMin.length()>0 && strMax!=null && strMax.length()>0){
											ParamResult result=new ParamResult();
											result.name=paramName;
											strMin=strMin.replaceAll(",",""); //Replace any commas
											strMax=strMax.replaceAll(",",""); //Replace any commas
											double min=Double.parseDouble(strMin);
											double max=Double.parseDouble(strMax);
											curParam=myModel.parameters.get(v);

											Numeric origValue=curParam.value.copy();
											double minOutcome=baseOutcome, maxOutcome=baseOutcome;

											//Min
											curParam.value.setDouble(min);
											curParam.locked=true;
											ArrayList<String> errorsMin=myModel.parseModel();
											if(errorsMin.size()>0){
												curParam.value=origValue;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+paramName+" - Min value");
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; progress.setProgress(curProg);

												if(analysisType==0){ //EV
													if(group==-1){minOutcome=myModel.getStrategyEV(strat, dim);}
													else{minOutcome=myModel.getSubgroupEV(group, strat, dim);}
												}
												else if(analysisType==1){ //CEA
													Object table[][]=new CEAHelper().calculateICERs(myModel,group);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat==strat){
															minOutcome=(double) table[s][4];
														}
													}
												}
												else if(analysisType==2){ //BCA
													Object table[][]=new CEAHelper().calculateNMB(myModel,group);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat==strat){
															minOutcome=(double) table[s][4];
														}
													}
												}
											}
											result.minVal=minOutcome;

											//Max
											curParam.value.setDouble(max);
											curParam.locked=true;
											ArrayList<String> errorsMax=myModel.parseModel();
											if(errorsMax.size()>0){
												curParam.value=origValue;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+paramName+" - Max value");
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; progress.setProgress(curProg);

												if(analysisType==0){ //EV
													if(group==-1){maxOutcome=myModel.getStrategyEV(strat, dim);}
													else{maxOutcome=myModel.getSubgroupEV(group, strat, dim);}
												} 
												else if(analysisType==1){ //CEA
													Object table[][]=new CEAHelper().calculateICERs(myModel,group);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat==strat){
															maxOutcome=(double) table[s][4];
														}
													}
												}
												else if(analysisType==2){ //BCA
													Object table[][]=new CEAHelper().calculateNMB(myModel,group);
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat==strat){
															maxOutcome=(double) table[s][4];
														}
													}
												}
											}
											result.maxVal=maxOutcome;
											result.range=(Math.abs(result.maxVal-result.minVal));

											curParam.value=origValue;
											curParam.locked=false;
											results.add(result);
										}
									}
									progress.close();
									
									myModel.validateModelObjects();
									//Update chart
									Collections.sort(results);

									numParams=results.size();
									String paramNamesChrt[]=new String[numParams];
									double[][] starts = new double[1][numParams];   
									double[][] ends = new double[1][numParams];  
									for(int v=0; v<numParams; v++){
										paramNamesChrt[v]=results.get(v).name;
										starts[0][v]=results.get(v).minVal;
										ends[0][v]=results.get(v).maxVal;
										double curMin=Math.min(starts[0][v], ends[0][v]);
										double curMax=Math.max(starts[0][v], ends[0][v]);
										globalMin=Math.min(globalMin, curMin);
										globalMax=Math.max(globalMax, curMax);
									}
									double offset=Math.abs(globalMin*0.05);
									offset=Math.max(offset, Math.abs(globalMax*0.05));
									globalMin-=offset;
									globalMax+=offset;

									DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
									dataset.setCategoryKeys(paramNamesChrt);

									CategoryAxis xAxis = new CategoryAxis("Parameters");
									ValueAxis yAxis = new NumberAxis();
									DimInfo info=myModel.dimInfo;
									if(analysisType==0){yAxis.setLabel("EV ("+info.dimSymbols[dim]+")");}
									else if(analysisType==1){yAxis.setLabel("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");}
									else if(analysisType==2){yAxis.setLabel("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");}
									yAxis.setRange(globalMin,globalMax);
									IntervalBarRenderer renderer = new IntervalBarRenderer();
									CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
									((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
									((BarRenderer) plot.getRenderer()).setShadowVisible(false);
									plot.setOrientation(PlotOrientation.HORIZONTAL);
									plot.addRangeMarker(new ValueMarker(baseOutcome, Color.BLACK,new BasicStroke(3f)));
									plot.getRenderer().setSeriesPaint(0, Color.BLUE);

									chart = new JFreeChart(plot);
									chart.removeLegend();
									panelChart.setChart(chart);
									btnExport.setEnabled(true);
								}
								
							}catch(Exception e1){
								curParam.locked=false;
								myModel.validateModelObjects();
								JOptionPane.showMessageDialog(frmTornadoDiagram, e1.getMessage());
								e1.printStackTrace();
								myModel.errorLog.recordError(e1);
							}

						}
					};
					SimThread.start();
				}

			});

			panelChart = new ChartPanel(null);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 1;
			gbc_panelChart.gridy = 0;
			frmTornadoDiagram.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	class ParamResult implements Comparable<ParamResult>{
		String name;
		double minVal, maxVal;
		double range;

		@Override
		public int compareTo(ParamResult result){
			if (this.range < result.range) return 1;
			else if (this.range == result.range) return 0;
			else return -1;
		}

	}
}
