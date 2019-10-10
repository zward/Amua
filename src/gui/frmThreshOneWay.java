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
import java.util.ArrayList;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.Stroke;
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
import main.CEAHelper;
import main.DimInfo;
import main.Parameter;
import math.MathUtils;
import math.Numeric;

import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import java.awt.Font;

/**
 *
 */
public class frmThreshOneWay {

	public JFrame frmThreshOneWay;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	DefaultXYDataset chartData;
	JFreeChart chart;
	JComboBox<String> comboDimensions;
	double dataEV[][][];
	private JTextField textThresh;
	private JTextField textIntervals;
	String CEAnotes[][];
	Parameter curParam;
	private JTextField textTolerance;
	double baselineParamValue;
		
	public frmThreshOneWay(AmuaModel model){
		this.myModel=model;
		myModel.getStrategies();
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmThreshOneWay = new JFrame();
			frmThreshOneWay.setTitle("Amua - Threshold Analysis");
			frmThreshOneWay.setIconImage(Toolkit.getDefaultToolkit().getImage(frmThreshOneWay.class.getResource("/images/threshold_128.png")));
			frmThreshOneWay.setBounds(100, 100, 1000, 500);
			frmThreshOneWay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmThreshOneWay.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmThreshOneWay.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{401, 149, 0};
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

			final JLabel lblOutcome = new JLabel("Outcome:");
			lblOutcome.setBounds(6, 73, 81, 16);
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
			comboDimensions.setBounds(88, 68, 227, 26);
			panel_2.add(comboDimensions);
			
			JButton btnRun = new JButton("Run");
			btnRun.setBounds(345, 115, 90, 28);
			panel_2.add(btnRun);

			JLabel lblStrategy = new JLabel("Strategy 1:");
			lblStrategy.setBounds(6, 13, 67, 16);
			panel_2.add(lblStrategy);

			final JComboBox<String> comboStrat1 = new JComboBox<String>(new DefaultComboBoxModel<String>(myModel.strategyNames));
			comboStrat1.setBounds(88, 8, 227, 26);
			panel_2.add(comboStrat1);
			if(myModel.strategyNames.length>0){comboStrat1.setSelectedIndex(0);}

			JLabel lblStrategy_1 = new JLabel("Strategy 2:");
			lblStrategy_1.setBounds(6, 43, 67, 16);
			panel_2.add(lblStrategy_1);

			final JComboBox<String> comboStrat2 = new JComboBox<String>(new DefaultComboBoxModel<String>(myModel.strategyNames));
			comboStrat2.setBounds(88, 38, 227, 26);
			panel_2.add(comboStrat2);
			if(myModel.strategyNames.length>1){comboStrat2.setSelectedIndex(1);}

			JLabel lblThreshold = new JLabel("Threshold:");
			lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblThreshold.setHorizontalAlignment(SwingConstants.CENTER);
			lblThreshold.setBounds(327, 69, 117, 16);
			panel_2.add(lblThreshold);

			textThresh = new JTextField();
			textThresh.setFont(new Font("SansSerif", Font.BOLD, 12));
			textThresh.setEditable(false);
			textThresh.setBounds(327, 86, 122, 28);
			panel_2.add(textThresh);
			textThresh.setColumns(10);
			
			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(338, 16, 55, 16);
			panel_2.add(lblIntervals);
			
			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("10");
			textIntervals.setBounds(394, 8, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);
			
			JLabel lblGroup = new JLabel("Group:");
			lblGroup.setEnabled(false);
			lblGroup.setBounds(6, 103, 55, 16);
			panel_2.add(lblGroup);
			
			final JComboBox<String> comboGroup = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"Overall"}));
			comboGroup.setEnabled(false);
			comboGroup.setBounds(88, 98, 227, 26);
			panel_2.add(comboGroup);
			
			JLabel lblTolerance = new JLabel("Tolerance:");
			lblTolerance.setBounds(327, 44, 66, 16);
			panel_2.add(lblTolerance);
			
			textTolerance = new JTextField();
			textTolerance.setText("0.001");
			textTolerance.setBounds(394, 38, 55, 28);
			panel_2.add(textTolerance);
			textTolerance.setColumns(10);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				String groups[]=new String[numGroups+1];
				groups[0]="Overall";
				for(int i=0; i<numGroups; i++){groups[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(groups));
				comboGroup.setEnabled(true);
				lblGroup.setEnabled(true);
			}

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmThreshOneWay, "Threshold analysis", "Running intervals...", 0, 100);
					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								boolean proceed=true;
								
								int strat1=comboStrat1.getSelectedIndex();
								int strat2=comboStrat2.getSelectedIndex();
								double tol=0.001;
								try{
									tol=Double.parseDouble(textTolerance.getText());
								}catch(Exception err){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Invalid tolerance entered!");
									proceed=false;
								}
								if(tol<=0){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Invalid tolerance entered!");
									proceed=false;
								}
								
								//Check model first
								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Errors in base case model!");
									proceed=false;
								}
								else if(strat1==strat2){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Please select 2 different strategies!");
									proceed=false;
								}
								
								if(tableParams.getSelectedRow()==-1) {
									JOptionPane.showMessageDialog(frmThreshOneWay, "Please select a parameter!");
									proceed=false;
								}
								else { //validate entry
									int row=tableParams.getSelectedRow();
									String strMin=(String)tableParams.getValueAt(row, 2);
									String strMax=(String)tableParams.getValueAt(row, 3);
									double min=0, max=0;
									if(strMin==null) {
										proceed=false;
										JOptionPane.showMessageDialog(frmThreshOneWay, "Please enter a min value!");
									}
									else if(strMax==null) {
										proceed=false;
										JOptionPane.showMessageDialog(frmThreshOneWay, "Please enter a max value!");
									}
									if(proceed) {
										strMin=strMin.replaceAll(",", ""); //Replace any commas
										strMax=strMax.replaceAll(",", ""); 
										try {
											min=Double.parseDouble(strMin);
										} catch(Exception err) {
											proceed=false;
											JOptionPane.showMessageDialog(frmThreshOneWay, "Please enter a valid min!");
										}
										try {
											max=Double.parseDouble(strMax);
										} catch(Exception err) {
											if(proceed) {
												proceed=false;
												JOptionPane.showMessageDialog(frmThreshOneWay, "Please enter a valid max!");
											}
										}
									}
									if(proceed && min>=max) {
										proceed=false;
										JOptionPane.showMessageDialog(frmThreshOneWay, "Please ensure max is greater min!");
									}
								}
								
								
								if(proceed==true){
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
									curParam.sensMin=strMin; //record min/max
									curParam.sensMax=strMax;
									Numeric origValue=curParam.value.copy();
									baselineParamValue=origValue.getValue();
									
									int dim=comboDimensions.getSelectedIndex();
									int decimalDim=dim;
									int analysisType=0; //EV
									if(dim==comboDimensions.getItemCount()-1){ //ICER or NMB selected
										analysisType=myModel.dimInfo.analysisType;
										if(analysisType>0){
											decimalDim=myModel.dimInfo.costDim;
										}
									} 
									
									int group=-1;
									if(comboGroup.isEnabled()){group=comboGroup.getSelectedIndex()-1;}
									
									boolean error=false;
									//Test parameter at min and max...
									curParam.value.setDouble(min);
									curParam.locked=true;
									ArrayList<String> errorsMin=myModel.parseModel();
									curParam.value.setDouble(max);
									curParam.locked=true;
									ArrayList<String> errorsMax=myModel.parseModel();
									curParam.value=origValue; //Reset
									if(errorsMin.size()>0){
										error=true;
										curParam.locked=false;
										myModel.validateModelObjects();
										JOptionPane.showMessageDialog(frmThreshOneWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curParam.locked=false;
										myModel.validateModelObjects();
										JOptionPane.showMessageDialog(frmThreshOneWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										int numStrat=myModel.strategyNames.length;
										double minDist=Double.POSITIVE_INFINITY;
										int minIndex=-1;

										dataEV=new double[numStrat][2][intervals+1];
										progress.setMaximum(intervals+1);
										if(analysisType==1){CEAnotes=new String[numStrat][intervals+1];} //CEA
										else{CEAnotes=null;}
										
										boolean origShowTrace=false;
										if(myModel.type==1) {
											origShowTrace=myModel.markov.showTrace;
											myModel.markov.showTrace=false;
										}
										
										double diffs[]=new double[intervals+1];
										for(int i=0; i<=intervals; i++){
											double curVal=min+(step*i);
											curParam.value.setDouble(curVal);
											curParam.locked=true;
											myModel.parseModel();
											myModel.runModel(null, false);
											
											if(analysisType==0){ //EV
												for(int s=0; s<numStrat; s++){
													dataEV[s][0][i]=curVal;
													if(group==-1){dataEV[s][1][i]=myModel.getStrategyEV(s, dim);}
													else{dataEV[s][1][i]=myModel.getSubgroupEV(group, s, dim);}
												}
											}
											else if(analysisType==1){ //CEA
												Object table[][]=new CEAHelper().calculateICERs(myModel,group,true);
												for(int s=0; s<table.length; s++){	
													int origStrat=(int) table[s][0];
													if(origStrat!=-1){
														dataEV[origStrat][0][i]=curVal;
														dataEV[origStrat][1][i]=(double) table[s][4];
														CEAnotes[origStrat][i]=(String) table[s][5];
													}
												}
											}
											else if(analysisType==2){ //BCA
												Object table[][]=new CEAHelper().calculateNMB(myModel,group,true);
												for(int s=0; s<table.length; s++){	
													int origStrat=(int) table[s][0];
													dataEV[origStrat][0][i]=curVal;
													dataEV[origStrat][1][i]=(double) table[s][4];
												}
											}
											
											
											diffs[i]=dataEV[strat1][1][i]-dataEV[strat2][1][i];
											double curDist=Math.abs(diffs[i]);
											if(curDist<minDist){
												minDist=curDist;
												minIndex=i;
											}
											progress.setProgress(i);
											if(progress.isCanceled()){ //End loop
												cancelled=true;
												i=intervals+1;
											}
										}

										if(cancelled==false){
											//Find intersection
											double intersection=Double.NaN;
											if(minDist==0){ //Intersection coincides with interval
												intersection=dataEV[0][0][minIndex];
											}
											else{ //No exact intersection found
												//Check if lines cross
												boolean cross=false;
												for(int i=1; i<=intervals; i++) {
													if(Math.signum(diffs[i-1])!=Math.signum(diffs[i])) {
														cross=true;
													}
												}
												if(cross==false) {
													JOptionPane.showMessageDialog(frmThreshOneWay, "No intersection found in current range!");
												}
												else{ //Search neighbourhood for intersection
													frmThreshOneWay.setCursor(new Cursor(Cursor.WAIT_CURSOR));
													
													double minVal=dataEV[0][0][minIndex];
													//if min dist is at either extreme move to the midpoint of the interval
													//if(minIndex==0) {minVal+=(step/2.0);}
													//else if(minIndex==intervals) {minVal-=(step/2.0);}
													
													int dec=myModel.dimInfo.decimals[decimalDim]+1;
													int i=0;
													progress.setMaximum(100);
													while(minDist>tol && i<100){ //Binary search of neighbourhood until convergence
														progress.setProgress(i);
														progress.setNote("Distance: "+MathUtils.round(minDist, dec));
														
														//Left
														double valL=minVal-(step/2.0);
														valL=Math.max(valL, min); //floor of min
														curParam.value.setDouble(valL);
														curParam.locked=true;
														myModel.parseModel();
														myModel.runModel(null, false);
														
														double result1L=0, result2L=0;
														if(analysisType==0){ //EV
															if(group==-1){
																result1L=myModel.getStrategyEV(strat1, dim);
																result2L=myModel.getStrategyEV(strat2, dim);
															}
															else{
																result1L=myModel.getSubgroupEV(group,strat1, dim);
																result2L=myModel.getSubgroupEV(group,strat2, dim);
															}
														}
														else if(analysisType==1){ //CEA
															Object table[][]=new CEAHelper().calculateICERs(myModel,group,true);
															for(int s=0; s<table.length; s++){	
																int origStrat=(int) table[s][0];
																if(origStrat==strat1){result1L=(double) table[s][4];}
																if(origStrat==strat2){result2L=(double) table[s][4];}
															}
														}
														else if(analysisType==2){ //BCA
															Object table[][]=new CEAHelper().calculateNMB(myModel,group,true);
															for(int s=0; s<table.length; s++){	
																int origStrat=(int) table[s][0];
																if(origStrat==strat1){result1L=(double) table[s][4];}
																if(origStrat==strat2){result2L=(double) table[s][4];}
															}
														}
														double distL=Math.abs(result1L-result2L);
														
														//Right
														double valR=minVal+(step/2.0);
														valR=Math.min(valR, max); //ceiling of max
														curParam.value.setDouble(valR);
														curParam.locked=true;
														myModel.parseModel();
														myModel.runModel(null, false);
														
														double result1R=0, result2R=0;
														if(analysisType==0){ //EV
															if(group==-1){
																result1R=myModel.getStrategyEV(strat1, dim);
																result2R=myModel.getStrategyEV(strat2, dim);
															}
															else{
																result1R=myModel.getSubgroupEV(group,strat1, dim);
																result2R=myModel.getSubgroupEV(group,strat2, dim);
															}
														}
														else if(analysisType==1){ //CEA
															Object table[][]=new CEAHelper().calculateICERs(myModel,group,true);
															for(int s=0; s<table.length; s++){	
																int origStrat=(int) table[s][0];
																if(origStrat==strat1){result1R=(double) table[s][4];}
																if(origStrat==strat2){result2R=(double) table[s][4];}
															}
														}
														else if(analysisType==2){ //BCA
															Object table[][]=new CEAHelper().calculateNMB(myModel,group,true);
															for(int s=0; s<table.length; s++){	
																int origStrat=(int) table[s][0];
																if(origStrat==strat1){result1R=(double) table[s][4];}
																if(origStrat==strat2){result2R=(double) table[s][4];}
															}
														}
														double distR=Math.abs(result1R-result2R);
														
														//Move to lowest dist (or stay)
														if(distL<minDist){
															minVal=valL;
															minDist=distL;
														}
														if(distR<minDist){
															minVal=valR;
															minDist=distR;
														}
														step/=2.0;
														
														i++;
														if(progress.isCanceled()){ //End loop
															cancelled=true;
															i=1000; //end
														}
													}
													if(minDist<tol){ //Convergence achieved
														intersection=minVal;
													}
													else{
														JOptionPane.showMessageDialog(frmThreshOneWay, "No intersection found! Try increasing tolerance.");
													}
													frmThreshOneWay.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
												}
											}
											textThresh.setText(MathUtils.round(intersection,myModel.dimInfo.decimals[decimalDim])+"");

											//Update chart
											DimInfo info=myModel.dimInfo;
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
												chartData.addSeries(myModel.strategyNames[s],dataEV[s]);
											}
											XYPlot plot = chart.getXYPlot();

											XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true,false);
											DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											for(int s=0; s<numStrat; s++){
												renderer1.setSeriesPaint(s, supplier.getNextPaint());
											}
											plot.setRenderer(renderer1);
											
											//add baseline marker
											plot.clearDomainMarkers();
											Stroke fill = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10.0f, 10.0f}, 0);
											plot.addDomainMarker(new ValueMarker(baselineParamValue, Color.BLACK, fill));
										}

										//Reset param value
										curParam.value=origValue;
										curParam.locked=false;
										myModel.validateModelObjects();
										
										if(myModel.type==1) {
											myModel.markov.showTrace=origShowTrace;
										}
										
										progress.close();
									}
								}
							} catch (Exception e) {
								frmThreshOneWay.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
								curParam.locked=false;
								myModel.validateModelObjects();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmThreshOneWay, e.getMessage());
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

			ChartPanel panelChart = new ChartPanel(chart,false);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 1;
			gbc_panelChart.gridy = 0;
			frmThreshOneWay.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
}
