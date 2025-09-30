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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

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
import java.awt.Cursor;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmTornadoDiagram {

	public frmTornadoDiagram frmThis;
	public JFrame frmTornadoDiagram;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	ChartPanel panelChart;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboSubgroup;
	JLabel lblSubgroup, lblOutcome;

	JFreeChart chart;
	DefaultIntervalCategoryDataset dataset;
	Paint seriesPaints[];
	ArrayList<String> paramNames;
	
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
		this.frmThis=this;
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
			frmTornadoDiagram.setTitle("Amua - "+myModel.language.base.getString("menu.tornado_diagram")); //Tornado Diagram
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
					new String[] {
							myModel.language.base.getString("object.parameter"), //Parameter
							myModel.language.base.getString("object.expression"), //Expression
							myModel.language.analysis.getString("sens.low"), //Low
							myModel.language.analysis.getString("sens.high")}) { //High
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

			lblOutcome = new JLabel(myModel.language.analysis.getString("result.outcome")+":"); //Outcome
			lblOutcome.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOutcome.setEnabled(false);
			lblOutcome.setBounds(213, 51, 73, 16);
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
					outcomes[info.dimNames.length]=myModel.language.analysis.getString("cea.icer")+" ("+info.dimNames[info.costDim]+"/"+info.dimNames[info.effectDim]+")"; //ICER
				}
				else if(info.analysisType==2){ //BCA
					outcomes[info.dimNames.length]=myModel.language.analysis.getString("bca.nmb")+" ("+info.dimNames[info.effectDim]+"-"+info.dimNames[info.costDim]+")"; //NMB
				}
			}
			
			comboDimensions = new JComboBox<String>(new DefaultComboBoxModel<String>(outcomes));
			comboDimensions.setEnabled(false);
			comboDimensions.setBounds(288, 46, 161, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton(myModel.language.base.getString("menu.run")); //Run
			btnRun.setBounds(258, 6, 90, 28);
			panel_2.add(btnRun);

			lblStrategies = new JLabel(myModel.language.analysis.getString("gen.strategies")+":"); //Strategies
			lblStrategies.setEnabled(false);
			lblStrategies.setBounds(6, 12, 81, 16);
			panel_2.add(lblStrategies);
			
			final JButton btnExport = new JButton(myModel.language.base.getString("menu.export")); //Export
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(myModel.filepath);
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new CSVFilter(myModel.language));

						fc.setDialogTitle(myModel.language.base.getString("title.export_graph_data")); //Export Graph Data
						fc.setApproveButtonText(myModel.language.base.getString("menu.export")); //Export

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
							out.write(myModel.language.base.getString("object.parameter")); //Parmaeter
							for(int s=0; s<numStrategies; s++) {
								out.write(","+myModel.strategyNames[s]+" "+outcome+" - Low");
								out.write(","+myModel.strategyNames[s]+" "+outcome+" - High");
							}
							out.newLine();
							//Baseline
							out.write("["+myModel.language.analysis.getString("sens.baseline")+"]"); //Baseline
							for(int s=0; s<numStrategies; s++) {
								out.write(","+baseOutcomes[curGroup][s][curOutcome]);
								out.write(","+baseOutcomes[curGroup][s][curOutcome]);
							}
							out.newLine();
							for(int p=0; p<numParams; p++) {
								ParamResult result=curResults.get(p);
								out.write(result.name);
								int pIndex=paramNames.indexOf(result.name);
								for(int s=0; s<numStrategies; s++) {
									out.write(","+results[curGroup][s][curOutcome][pIndex][0]);
									out.write(","+results[curGroup][s][curOutcome][pIndex][1]);
								}
								out.newLine();
							}
							out.close();
							
							JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("info.exported")); //Exported!
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
			
			lblSubgroup = new JLabel(myModel.language.analysis.getString("result.subgroup")+":"); //Subgroup
			lblSubgroup.setHorizontalAlignment(SwingConstants.RIGHT);
			lblSubgroup.setEnabled(false);
			lblSubgroup.setBounds(213, 84, 73, 16);
			panel_2.add(lblSubgroup);
			
			comboSubgroup = new JComboBox<String>(new DefaultComboBoxModel(new String[]{myModel.language.analysis.getString("result.overall")})); //Overall
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
			
			//get default series colors
			seriesPaints=new Paint[numStrategies];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			if(numStrategies==1) { //get blue
				seriesPaints[0]=supplier.getNextPaint(); //red
				seriesPaints[0]=supplier.getNextPaint(); //skip to blue
			}
			else if(numStrategies>1){
				for(int s=0; s<numStrategies; s++) {
					seriesPaints[s]=supplier.getNextPaint();
				}
				Paint red=seriesPaints[0];
				Paint blue=seriesPaints[1];
				seriesPaints[0]=blue;
				seriesPaints[1]=red;
			}
			
			btnUpdatePlot = new JButton(myModel.language.base.getString("plot.update_plot")); //Update Plot
			btnUpdatePlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(listStrategies.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("err.select_one_strategy")); //Please select at least one strategy!
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

						int numParams=paramNames.size();
						boolean anyNaN=false;

						curResults=new ArrayList<ParamResult>();
						for(int p=0; p<numParams; p++) {
							ParamResult result=new ParamResult();
							result.name=paramNames.get(p);
							result.minVal=new double[numStrat];
							result.maxVal=new double[numStrat];
							for(int s=0; s<numStrat; s++) {
								result.minVal[s]=Math.min(results[curGroup][strats[s]][dim][p][0],results[curGroup][strats[s]][dim][p][1]);
								result.maxVal[s]=Math.max(results[curGroup][strats[s]][dim][p][0],results[curGroup][strats[s]][dim][p][1]);
								if(isNumber(result.minVal[s])) {
									globalMin=Math.min(globalMin, result.minVal[s]);
								}
								else {
									anyNaN=true;
								}
								
								if(isNumber(result.maxVal[s])) {
									globalMax=Math.max(globalMax, result.maxVal[s]);
								}
								else {
									anyNaN=true;
								}
							}
							result.calcMeanRange();
							if(isNumber(result.meanRange)==false) {
								anyNaN=true;
							}
							
							curResults.add(result);
						}
						Collections.sort(curResults);

						String paramNamesChrt[]=new String[numParams];
						double[][] starts = new double[numStrat][numParams];   
						double[][] ends = new double[numStrat][numParams];  
						for(int p=0; p<numParams; p++){
							paramNamesChrt[p]=curResults.get(p).name;
							if(isNumber(curResults.get(p).meanRange)==false) {
								paramNamesChrt[p]+=" ["+myModel.language.math.getString("sum.undefined")+"]"; //Undefined
							}
							for(int s=0; s<numStrat; s++) {
								starts[s][p]=curResults.get(p).minVal[s];
								ends[s][p]=curResults.get(p).maxVal[s];
							}
						}
						double offset=Math.abs(globalMin*0.05);
						offset=Math.max(offset, Math.abs(globalMax*0.05));
						globalMin-=offset;
						globalMax+=offset;

						dataset=new DefaultIntervalCategoryDataset(starts, ends);
						dataset.setCategoryKeys(paramNamesChrt);
						String seriesKeys[]=new String[numStrat];
						for(int s=0; s<numStrat; s++) {
							seriesKeys[s]=myModel.strategyNames[strats[s]];
						}
						dataset.setSeriesKeys(seriesKeys);
												
						CategoryAxis xAxis = new CategoryAxis(myModel.language.base.getString("object.parameters")); //Parameters
						ValueAxis yAxis = new NumberAxis();
						DimInfo info=myModel.dimInfo;
						if(myModel.dimInfo.analysisType==0 || dim<(numOutcomes-1)) {yAxis.setLabel(myModel.language.analysis.getString("result.ev")+" ("+info.dimSymbols[dim]+")");} //EV
						else {
							if(myModel.dimInfo.analysisType==1) { //CEA (ICERs)
								yAxis.setLabel(myModel.language.analysis.getString("cea.icer")+" ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")"); //ICER
								if(anyNaN) {
									//Warning: One or more ICERs were undefined! Consider plotting NMB instead.
									JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("warn.icer_undefined"));
								}
							}
							else if(myModel.dimInfo.analysisType==2){yAxis.setLabel(myModel.language.analysis.getString("bca.nmb")+" ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")");} //NMB
						}
						if(globalMin==globalMax) { //ensure positive range length
							globalMin-=0.01;
							globalMax+=0.01;
						}
						yAxis.setRange(globalMin,globalMax);
						IntervalBarRenderer renderer = new IntervalBarRenderer();
						CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
						((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
						((BarRenderer) plot.getRenderer()).setShadowVisible(false);
						plot.setOrientation(PlotOrientation.HORIZONTAL);
						
						for(int s=0; s<numStrat; s++){
							Paint curPaint=seriesPaints[s];
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
			btnUpdatePlot.setBounds(258, 108, 191, 28);
			panel_2.add(btnUpdatePlot);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numSubgroups=myModel.subgroupNames.size();
				String groups[]=new String[numSubgroups+1];
				groups[0]=myModel.language.analysis.getString("result.overall"); //Overall
				for(int i=0; i<numSubgroups; i++){groups[i+1]=myModel.subgroupNames.get(i);}
				comboSubgroup.setModel(new DefaultComboBoxModel(groups));
				lblSubgroup.setEnabled(true);
				comboSubgroup.setEnabled(true);
			}
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmTornadoDiagram, myModel.language.base.getString("menu.tornado_diagram"), myModel.language.message.getString("info.running"), 0, 100); //Tornado diagram, Running

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								enablePlot(false);
								btnExport.setEnabled(false);
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("err.base_case")); //Errors in base case model!
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
												//Invalid entry: [name]
												String msg = MessageFormat.format(myModel.language.message.getString("err.invalid_entry_name"), paramName);
												JOptionPane.showMessageDialog(frmTornadoDiagram, msg);
												p=tableParams.getRowCount();
											}
											numRuns+=2;
										}
									}
									
									progress.setMillisToDecideToPopup(0);
									progress.setMillisToPopup(0);
									
									long startTime=System.currentTimeMillis();
									
									if(proceed==true) {
										frmTornadoDiagram.setCursor(new Cursor(Cursor.WAIT_CURSOR));
										btnRun.setEnabled(false);

										boolean origShowTrace=false;
										if(myModel.type==1) {
											origShowTrace=myModel.markov.showTrace;
											myModel.markov.showTrace=false;
										}
										progress.setMaximum(numRuns+1);
										int curProg=0;
										progress.setProgress(curProg);

										//Get baseline
										myModel.runModel(null, false);
										curProg++; updateProgress(progress, curProg, numRuns+1, startTime);

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
										paramNames=new ArrayList<String>();
										
										for(int p=0; p<numParams; p++){
											int pIndex=paramIndices.get(p);
											paramNames.add((String)tableParams.getValueAt(pIndex, 0));
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
												JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("err.min_value")+": "+paramNames.get(p)); //Error: Min value
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; updateProgress(progress, curProg, numRuns+1, startTime);

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
												JOptionPane.showMessageDialog(frmTornadoDiagram, myModel.language.message.getString("err.max_value")+": "+paramNames.get(p)); //Error: Max value
												break;
											}
											else{
												myModel.parseModel();
												myModel.runModel(null, false);
												curProg++; updateProgress(progress, curProg, numRuns+1, startTime);

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
										
										frmTornadoDiagram.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
										btnRun.setEnabled(true);

									} //end proceed check
								}
								
							}catch(Exception e1){
								curParam.locked=false;
								myModel.validateModelObjects();
								JOptionPane.showMessageDialog(frmTornadoDiagram, e1.getMessage());
								e1.printStackTrace();
								myModel.errorLog.recordError(e1);
								frmTornadoDiagram.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								btnRun.setEnabled(true);
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
			
			//pop-up menu
			JPopupMenu popup = panelChart.getPopupMenu();
			JMenuItem mntmChangeColor = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chart, dataset, seriesPaints, frmThis, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);
			

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
		
	}

	private void updateProgress(ProgressMonitor progress, int curProg, int numRuns, long startTime) {
		double prog=(curProg/(numRuns*1.0))*100;
		long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
		remTime=(long) (remTime*(100-prog));
		remTime=remTime/1000;
		String seconds = Integer.toString((int)(remTime % 60));
		String minutes = Integer.toString((int)(remTime/60));
		if(seconds.length()<2){seconds="0"+seconds;}
		if(minutes.length()<2){minutes="0"+minutes;}
		progress.setProgress(curProg);
		if(curProg>0) {
			progress.setNote(myModel.language.message.getString("info.time_left")+": "+minutes+":"+seconds); //Time left
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
	
	public void updateRangeMarkers() {
		CategoryPlot plot = chart.getCategoryPlot();
		plot.clearRangeMarkers();
		
		int strats[]=listStrategies.getSelectedIndices();
		int numStrat=strats.length;
		int dim=comboDimensions.getSelectedIndex();
		
		Stroke fill = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
		for(int s=0; s<numStrat; s++) {
			Paint curPaint=seriesPaints[s];
			if(numStrat==1) {curPaint=Color.BLACK;} //only 1 strategy, draw black baseline
			plot.addRangeMarker(new ValueMarker(baseOutcomes[curGroup][strats[s]][dim], curPaint, fill));
			plot.addRangeMarker(new ValueMarker(baseOutcomes[curGroup][strats[s]][dim], Color.BLACK, new CompositeStroke(fill,new BasicStroke(0.75f))));
		}
	}
	
	private boolean isNumber(double val) {
		boolean valid=true;
		if(Double.isInfinite(val) || Double.isNaN(val)) {
			valid=false;
		}
		return(valid);
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
