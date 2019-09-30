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
import javax.swing.DefaultListModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
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
import org.jfree.chart.plot.DefaultDrawingSupplier;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JList;

/**
 *
 */
public class frmTornadoDiagram {

	public JFrame frmTornadoDiagram;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	ChartPanel panelChart;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboSubgroup;
	JLabel lblSubgroup, lblOutcome;

	JFreeChart chart;
	String paramNames[];
	
	/**
	 * [Subgroup][Strategy][Dimension]
	 */
	double baseOutcomes[][][];
	
	/**
	 * [Subgroup][Strategy][Dimension][Parameter][Min/Max]
	 */
	double results[][][][][];
	
	ArrayList<ParamResult> curResults;
	
	Parameter curParam;
	private JScrollPane scrollPane;
	JList<String> listStrategies;
	DefaultListModel<String> listModelStrategies;
	JLabel lblStrategies;
	
	int numStrategies;
	int numOutcomes;
	int numSubgroups=0;
	int numParams;
	int curOutcome=0, curGroup=0;
	JButton btnUpdatePlot;
	
	public frmTornadoDiagram(AmuaModel model){
		this.myModel=model;
		numStrategies=myModel.getStrategies();
		numOutcomes=myModel.dimInfo.dimNames.length;
		if(myModel.dimInfo.analysisType>0) {numOutcomes++;}
		if(myModel.simType==1 && myModel.reportSubgroups==true) {
			numSubgroups=myModel.subgroupNames.size();
		}
		
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTornadoDiagram = new JFrame();
			frmTornadoDiagram.setTitle("Amua - Tornado Diagram");
			frmTornadoDiagram.setIconImage(Toolkit.getDefaultToolkit().getImage(frmTornadoDiagram.class.getResource("/images/tornado_128.png")));
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
			gbl_panel_1.rowHeights = new int[]{466, 146, 0};
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
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(curParam.name, i, 0);
				modelParams.setValueAt(curParam.expression, i, 1);
				modelParams.setValueAt(curParam.sensMin, i, 2);
				modelParams.setValueAt(curParam.sensMax, i, 3);
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

			lblOutcome = new JLabel("Outcome:");
			lblOutcome.setEnabled(false);
			lblOutcome.setBounds(222, 51, 60, 16);
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
			comboDimensions.setEnabled(false);
			comboDimensions.setBounds(288, 46, 161, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(258, 6, 90, 28);
			panel_2.add(btnRun);

			lblStrategies = new JLabel("Strategies:");
			lblStrategies.setEnabled(false);
			lblStrategies.setBounds(6, 12, 81, 16);
			panel_2.add(lblStrategies);
			
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
							String outcome=chart.getCategoryPlot().getRangeAxis().getLabel();
							out.write("Parameter");
							for(int s=0; s<numStrategies; s++) {
								out.write(","+myModel.strategyNames[s]+" "+outcome+" - Low");
								out.write(","+myModel.strategyNames[s]+" "+outcome+" - High");
							}
							out.newLine();
							//Baseline
							out.write("[Baseline]");
							for(int s=0; s<numStrategies; s++) {
								out.write(","+baseOutcomes[curGroup][s][curOutcome]);
								out.write(","+baseOutcomes[curGroup][s][curOutcome]);
							}
							out.newLine();
							for(int p=0; p<numParams; p++) {
								ParamResult result=curResults.get(p);
								out.write(result.name);
								int pIndex=myModel.getParameterIndex(result.name);
								for(int s=0; s<numStrategies; s++) {
									out.write(","+results[curGroup][s][curOutcome][pIndex][0]);
									out.write(","+results[curGroup][s][curOutcome][pIndex][1]);
								}
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
			btnExport.setBounds(355, 6, 90, 28);
			panel_2.add(btnExport);
			
			lblSubgroup = new JLabel("Subgroup:");
			lblSubgroup.setEnabled(false);
			lblSubgroup.setBounds(222, 84, 60, 16);
			panel_2.add(lblSubgroup);
			
			comboSubgroup = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"Overall"}));
			comboSubgroup.setEnabled(false);
			comboSubgroup.setBounds(288, 79, 161, 26);
			panel_2.add(comboSubgroup);
			
			scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 31, 205, 109);
			panel_2.add(scrollPane);
			
			listModelStrategies=new DefaultListModel<String>();
			for(int s=0; s<numStrategies; s++){listModelStrategies.addElement(myModel.strategyNames[s]);}

			listStrategies = new JList(listModelStrategies);
			listStrategies.setEnabled(false);
			scrollPane.setViewportView(listStrategies);
			
			btnUpdatePlot = new JButton("Update Plot");
			btnUpdatePlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listStrategies.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(frmTornadoDiagram, "Please select at least one strategy!");
					}
					else {
						int strats[]=listStrategies.getSelectedIndices();
						int numStrat=strats.length;
						int dim=comboDimensions.getSelectedIndex();
						curOutcome=dim;
						curGroup=0; //overall
						if(comboSubgroup.isEnabled()){
							curGroup=comboSubgroup.getSelectedIndex();
						}

						double globalMin=Double.POSITIVE_INFINITY, globalMax=Double.NEGATIVE_INFINITY;

						int numParams=paramNames.length;

						curResults=new ArrayList<ParamResult>();
						for(int p=0; p<numParams; p++) {
							ParamResult result=new ParamResult();
							result.name=paramNames[p];
							result.minVal=new double[numStrat];
							result.maxVal=new double[numStrat];
							for(int s=0; s<numStrat; s++) {
								result.minVal[s]=Math.min(results[curGroup][strats[s]][dim][p][0],results[curGroup][strats[s]][dim][p][1]);
								result.maxVal[s]=Math.max(results[curGroup][strats[s]][dim][p][0],results[curGroup][strats[s]][dim][p][1]);
								globalMin=Math.min(globalMin, result.minVal[s]);
								globalMax=Math.max(globalMax, result.maxVal[s]);
							}
							result.calcMeanRange();
							curResults.add(result);
						}
						Collections.sort(curResults);

						String paramNamesChrt[]=new String[numParams];
						double[][] starts = new double[numStrat][numParams];   
						double[][] ends = new double[numStrat][numParams];  
						for(int p=0; p<numParams; p++){
							paramNamesChrt[p]=curResults.get(p).name;
							for(int s=0; s<numStrat; s++) {
								starts[s][p]=curResults.get(p).minVal[s];
								ends[s][p]=curResults.get(p).maxVal[s];
							}
						}
						double offset=Math.abs(globalMin*0.05);
						offset=Math.max(offset, Math.abs(globalMax*0.05));
						globalMin-=offset;
						globalMax+=offset;

						DefaultIntervalCategoryDataset dataset=new DefaultIntervalCategoryDataset(starts, ends);
						dataset.setCategoryKeys(paramNamesChrt);
						String seriesKeys[]=new String[numStrat];
						for(int s=0; s<numStrat; s++) {
							seriesKeys[s]=myModel.strategyNames[strats[s]];
						}
						dataset.setSeriesKeys(seriesKeys);
												
						CategoryAxis xAxis = new CategoryAxis("Parameters");
						ValueAxis yAxis = new NumberAxis();
						DimInfo info=myModel.dimInfo;
						if(myModel.dimInfo.analysisType==0 || dim<(numOutcomes-1)) {yAxis.setLabel("EV ("+info.dimSymbols[dim]+")");}
						else {
							if(myModel.dimInfo.analysisType==1) {yAxis.setLabel("ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")");}
							else if(myModel.dimInfo.analysisType==2){yAxis.setLabel("NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");}
						}
						yAxis.setRange(globalMin,globalMax);
						IntervalBarRenderer renderer = new IntervalBarRenderer();
						CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
						((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
						((BarRenderer) plot.getRenderer()).setShadowVisible(false);
						plot.setOrientation(PlotOrientation.HORIZONTAL);
						
						DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
						Paint paint2=supplier.getNextPaint(); //flip red and blue
						Paint paint1=supplier.getNextPaint();
						
						for(int s=0; s<numStrat; s++){
							Paint curPaint;
							if(s==0) {curPaint=paint1;}
							else if(s==1) {curPaint=paint2;}
							else {curPaint=supplier.getNextPaint();}
							plot.getRenderer().setSeriesPaint(s, curPaint);
							
							Stroke fill = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
							if(numStrat==1) {curPaint=Color.BLACK;} //only 1 strategy, draw black baseline
							plot.addRangeMarker(new ValueMarker(baseOutcomes[curGroup][strats[s]][dim], curPaint, fill));
							plot.addRangeMarker(new ValueMarker(baseOutcomes[curGroup][strats[s]][dim], Color.BLACK, new CompositeStroke(fill,new BasicStroke(0.75f))));
						}
						
						chart = new JFreeChart(plot);
						//chart.removeLegend();
						panelChart.setChart(chart);
					}
				}
			});
			btnUpdatePlot.setBounds(288, 108, 109, 28);
			panel_2.add(btnUpdatePlot);
			
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
								enablePlot(false);
								btnExport.setEnabled(false);
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmTornadoDiagram, "Errors in base case model!");
								}
								else{
									boolean proceed=true;

									int numRuns=1; //baseline
									ArrayList<Integer> paramIndices=new ArrayList<Integer>();
									for(int p=0; p<tableParams.getRowCount(); p++){
										String paramName=(String)tableParams.getValueAt(p, 0);
										String strMin=(String)tableParams.getValueAt(p, 2);
										String strMax=(String)tableParams.getValueAt(p, 3);
										if(strMin!=null && strMin.length()>0 && strMax!=null && strMax.length()>0){
											strMin=strMin.replaceAll(",",""); //Replace any commas
											strMax=strMax.replaceAll(",",""); //Replace any commas
											paramIndices.add(p);
											try {
												double min=Double.parseDouble(strMin);
												double max=Double.parseDouble(strMax);
											} catch(Exception err) {
												proceed=false;
												JOptionPane.showMessageDialog(frmTornadoDiagram,"Invalid entry: "+paramName);
												p=tableParams.getRowCount();
											}
											numRuns+=2;
										}
									}
									
									if(proceed==true) {

										boolean origShowTrace=false;
										if(myModel.type==1) {
											origShowTrace=myModel.markov.showTrace;
											myModel.markov.showTrace=false;
										}
										progress.setMaximum(numRuns+1);
										int curProg=1;
										progress.setProgress(curProg);

										//Get baseline
										myModel.runModel(null, false);
										curProg++; progress.setProgress(curProg);

										baseOutcomes=new double[1+numSubgroups][numStrategies][numOutcomes];

										int numDim=myModel.dimInfo.dimNames.length;
										for(int s=0; s<numStrategies; s++) {
											for(int d=0; d<numDim; d++) {
												baseOutcomes[0][s][d]=myModel.getStrategyEV(s, d); //overall
												//subgroups
												for(int g=0; g<numSubgroups; g++) {
													baseOutcomes[g+1][s][d]=myModel.getSubgroupEV(g, s, d);
												}
											}
										}
										if(myModel.dimInfo.analysisType>0) { // CEA/BCA
											//overall
											Object table[][]=null;
											if(myModel.dimInfo.analysisType==1) {table=new CEAHelper().calculateICERs(myModel,-1,true);}
											else if(myModel.dimInfo.analysisType==2) {table=new CEAHelper().calculateNMB(myModel,-1,true);}
											for(int s=0; s<table.length; s++){	
												int origStrat=(int) table[s][0];
												baseOutcomes[0][origStrat][numDim]=(double) table[s][4];
											}
											//subgroups
											for(int g=0; g<numSubgroups; g++) {
												table=new CEAHelper().calculateICERs(myModel,g,true);
												for(int s=0; s<table.length; s++){	
													int origStrat=(int) table[s][0];
													baseOutcomes[g+1][origStrat][numDim]=(double) table[s][4];
												}
											}
										}
										
										numParams=paramIndices.size();
										results=new double[1+numSubgroups][numStrategies][numOutcomes][numParams][2];
										paramNames=new String[numParams];
										
										for(int p=0; p<numParams; p++){
											int pIndex=paramIndices.get(p);
											paramNames[p]=(String)tableParams.getValueAt(pIndex, 0);
											String strMin=(String)tableParams.getValueAt(pIndex, 2);
											String strMax=(String)tableParams.getValueAt(pIndex, 3);
											strMin=strMin.replaceAll(",",""); //Replace any commas
											strMax=strMax.replaceAll(",",""); //Replace any commas
											double min=Double.parseDouble(strMin);
											double max=Double.parseDouble(strMax);
											curParam=myModel.parameters.get(pIndex);
											curParam.sensMin=strMin;
											curParam.sensMax=strMax;

											Numeric origValue=curParam.value.copy();

											//Min
											curParam.value.setDouble(min);
											curParam.locked=true;
											ArrayList<String> errorsMin=myModel.parseModel();
											if(errorsMin.size()>0){
												curParam.value=origValue;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+paramNames[p]+" - Min value");
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; progress.setProgress(curProg);

												//get results
												for(int s=0; s<numStrategies; s++) {
													for(int d=0; d<numDim; d++) {
														results[0][s][d][p][0]=myModel.getStrategyEV(s, d); //overall
														//subgroups
														for(int g=0; g<numSubgroups; g++) {
															results[g+1][s][d][p][0]=myModel.getSubgroupEV(g, s, d);
														}
													}
												}
												if(myModel.dimInfo.analysisType>0) { // CEA/BCA
													//overall
													Object table[][]=null;
													if(myModel.dimInfo.analysisType==1) {table=new CEAHelper().calculateICERs(myModel,-1,true);}
													else if(myModel.dimInfo.analysisType==2) {table=new CEAHelper().calculateNMB(myModel,-1,true);}
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														results[0][origStrat][numDim][p][0]=(double) table[s][4];
													}
													//subgroups
													for(int g=0; g<numSubgroups; g++) {
														table=new CEAHelper().calculateICERs(myModel,g,true);
														for(int s=0; s<table.length; s++){	
															int origStrat=(int) table[s][0];
															results[g+1][origStrat][numDim][p][0]=(double) table[s][4];
														}
													}
												}
											}

											//Max
											curParam.value.setDouble(max);
											curParam.locked=true;
											ArrayList<String> errorsMax=myModel.parseModel();
											if(errorsMax.size()>0){
												curParam.value=origValue;
												curParam.locked=false;
												myModel.validateModelObjects();
												JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+paramNames[p]+" - Max value");
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; progress.setProgress(curProg);

												//get results
												for(int s=0; s<numStrategies; s++) {
													for(int d=0; d<numDim; d++) {
														results[0][s][d][p][1]=myModel.getStrategyEV(s, d); //overall
														//subgroups
														for(int g=0; g<numSubgroups; g++) {
															results[g+1][s][d][p][1]=myModel.getSubgroupEV(g, s, d);
														}
													}
												}
												if(myModel.dimInfo.analysisType>0) { // CEA/BCA
													//overall
													Object table[][]=null;
													if(myModel.dimInfo.analysisType==1) {table=new CEAHelper().calculateICERs(myModel,-1,true);}
													else if(myModel.dimInfo.analysisType==2) {table=new CEAHelper().calculateNMB(myModel,-1,true);}
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														results[0][origStrat][numDim][p][1]=(double) table[s][4];
													}
													//subgroups
													for(int g=0; g<numSubgroups; g++) {
														table=new CEAHelper().calculateICERs(myModel,g,true);
														for(int s=0; s<table.length; s++){	
															int origStrat=(int) table[s][0];
															results[g+1][origStrat][numDim][p][1]=(double) table[s][4];
														}
													}
												}

												curParam.value=origValue;
												curParam.locked=false;
											}

										} //end param loop
										progress.close();

										myModel.validateModelObjects();

										enablePlot(true);
										btnExport.setEnabled(true);

										if(myModel.type==1) {
											myModel.markov.showTrace=origShowTrace;
										}

									} //end proceed check
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

			panelChart = new ChartPanel(null,false);
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

	private void enablePlot(boolean enabled) {
		lblStrategies.setEnabled(enabled);
		listStrategies.setEnabled(enabled);
		btnUpdatePlot.setEnabled(enabled);
		lblOutcome.setEnabled(enabled);
		comboDimensions.setEnabled(enabled);
		if(myModel.simType==1 && myModel.reportSubgroups) {
			lblSubgroup.setEnabled(enabled);
			comboSubgroup.setEnabled(enabled);
		}
	}
	
	class CompositeStroke implements Stroke {
		private Stroke stroke1, stroke2;

		public CompositeStroke( Stroke stroke1, Stroke stroke2 ) {
			this.stroke1 = stroke1;
			this.stroke2 = stroke2;
		}

		public Shape createStrokedShape( Shape shape ) {
			return stroke2.createStrokedShape( stroke1.createStrokedShape( shape ) );
		}
	}
	
	class ParamResult implements Comparable<ParamResult>{
		String name;
		double minVal[], maxVal[];
		double meanRange;
		
		public void calcMeanRange() {
			int numStrat=minVal.length;
			meanRange=0;
			for(int s=0; s<numStrat; s++) {
				double curRange=maxVal[s]-minVal[s];
				meanRange+=curRange;
			}
			meanRange/=(numStrat*1.0);
		}

		@Override
		public int compareTo(ParamResult result){
			if (this.meanRange < result.meanRange) return 1;
			else if (this.meanRange == result.meanRange) return 0;
			else return -1;
		}

	}
}
