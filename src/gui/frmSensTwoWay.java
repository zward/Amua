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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.border.LineBorder;
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
import surface.SurfaceModel;
import surface.SurfacePanel;

/**
 *
 */
public class frmSensTwoWay {

	public JFrame frmSensTwoWay;
	AmuaModel myModel;
	DefaultTableModel modelParams;
	private JTable tableParams;

	DefaultXYDataset chartData;
	JFreeChart chart;
	SurfacePanel surfacePanel;
	JComboBox<String> comboDimensions;
	JComboBox<String> comboStrategy;
	JComboBox<String> comboGroup;
	
	double dataEV[][][];
	double dataSurface[][][];
	SurfaceModel surfaceModel;
	private JTextField textIntervals;
	JComboBox<String> comboMinMax;
	JLabel lblCEThresh;
	private JTextField textCEThresh;
	Parameter curParam1, curParam2;

	public frmSensTwoWay(AmuaModel model){
		this.myModel=model;
		myModel.getStrategies();
		initialize();
	}
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmSensTwoWay = new JFrame();
			frmSensTwoWay.setTitle("Amua - Two-way Sensitivity Analysis");
			frmSensTwoWay.setIconImage(Toolkit.getDefaultToolkit().getImage(frmSensTwoWay.class.getResource("/images/twoWay_128.png")));
			frmSensTwoWay.setBounds(100, 100, 1000, 500);
			frmSensTwoWay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmSensTwoWay.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmSensTwoWay.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{466, 160, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelParams=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Parameter","Expression","Min","Max"}) {
				boolean[] columnEditables = new boolean[] {false, false,true,true};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			String paramNames[]=new String[myModel.parameters.size()];

			for(int i=0; i<myModel.parameters.size(); i++){
				modelParams.addRow(new Object[]{null});
				Parameter curParam=myModel.parameters.get(i);
				modelParams.setValueAt(curParam.name, i, 0);
				modelParams.setValueAt(curParam.expression, i, 1);
				modelParams.setValueAt(curParam.sensMin, i, 2);
				modelParams.setValueAt(curParam.sensMax, i, 3);
				paramNames[i]=curParam.name;
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
			lblOutcome.setBounds(12, 69, 81, 16);
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
			comboDimensions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(myModel.dimInfo.analysisType==1){ //CEA
						if(comboDimensions.getSelectedIndex()==comboDimensions.getItemCount()-1){ //CEA or BCA
							comboMinMax.setEnabled(false);
							lblCEThresh.setEnabled(true);
							textCEThresh.setEnabled(true);
						}
						else{
							comboMinMax.setEnabled(true);
							lblCEThresh.setEnabled(false);
							textCEThresh.setEnabled(false);
						}
					}
				}
			});
			comboDimensions.setBounds(94, 64, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(353, 27, 90, 28);
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

						int returnVal = fc.showSaveDialog(frmSensTwoWay);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							//Headers
							out.write(chart.getXYPlot().getDomainAxis().getLabel()+","); //Param name
							String outcome=chart.getXYPlot().getRangeAxis().getLabel();
							int numStrat=myModel.strategyNames.length;
							for(int s=0; s<numStrat-1; s++){out.write(outcome+"-"+myModel.strategyNames[s]+",");}
							out.write(outcome+"-"+myModel.strategyNames[numStrat-1]); out.newLine();
							//Chart data
							int numPoints=dataEV[0][0].length;
							for(int i=0; i<numPoints; i++){
								out.write(dataEV[0][0][i]+","); //x-axis
								for(int s=0; s<numStrat-1; s++){out.write(dataEV[s][1][i]+",");} //y-axis
								out.write(dataEV[numStrat-1][1][i]+""); out.newLine();
							}
							out.close();

							JOptionPane.showMessageDialog(frmSensTwoWay, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmSensTwoWay, ex.getMessage());
						myModel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(353, 125, 90, 28);
			panel_2.add(btnExport);

			JLabel lblParameter = new JLabel("Parameter 1:");
			lblParameter.setBounds(12, 9, 81, 16);
			panel_2.add(lblParameter);

			final JComboBox<String> comboParam1 = new JComboBox<String>(new DefaultComboBoxModel<String>(paramNames));
			comboParam1.setBounds(94, 4, 227, 26);
			panel_2.add(comboParam1);
			if(paramNames.length>0){comboParam1.setSelectedIndex(0);}

			JLabel lblParameter_1 = new JLabel("Parameter 2:");
			lblParameter_1.setBounds(12, 39, 81, 16);
			panel_2.add(lblParameter_1);
			
			final JComboBox<String> comboParam2 = new JComboBox<String>(new DefaultComboBoxModel<String>(paramNames));
			comboParam2.setBounds(94, 34, 227, 26);
			panel_2.add(comboParam2);
			if(paramNames.length>1){comboParam2.setSelectedIndex(1);}

			comboMinMax = new JComboBox<String>();
			comboMinMax.setModel(new DefaultComboBoxModel<String>(new String[] {"Min", "Max"}));
			comboMinMax.setSelectedIndex(1);
			comboMinMax.setBounds(353, 64, 90, 26);
			panel_2.add(comboMinMax);
			
			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(333, 100, 55, 16);
			panel_2.add(lblIntervals);
			
			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("10");
			textIntervals.setBounds(388, 94, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);
			
			lblCEThresh = new JLabel("Cost-Effectiveness Threshold:");
			lblCEThresh.setEnabled(false);
			lblCEThresh.setBounds(12, 100, 171, 16);
			panel_2.add(lblCEThresh);
			
			textCEThresh = new JTextField();
			textCEThresh.setEnabled(false);
			textCEThresh.setBounds(181, 94, 122, 28);
			panel_2.add(textCEThresh);
			textCEThresh.setColumns(10);
			
			JLabel lblGroup = new JLabel("Group:");
			lblGroup.setEnabled(false);
			lblGroup.setBounds(12, 131, 55, 16);
			panel_2.add(lblGroup);
			
			comboGroup = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"Overall"}));
			comboGroup.setEnabled(false);
			comboGroup.setBounds(94, 126, 227, 26);
			panel_2.add(comboGroup);
			
			if(myModel.simType==1 && myModel.reportSubgroups){
				int numGroups=myModel.subgroupNames.size();
				String groups[]=new String[numGroups+1];
				groups[0]="Overall";
				for(int i=0; i<numGroups; i++){groups[i+1]=myModel.subgroupNames.get(i);}
				comboGroup.setModel(new DefaultComboBoxModel(groups));
				comboGroup.setEnabled(true);
				lblGroup.setEnabled(true);
			}

			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmSensTwoWay, "Two-way", "Analyzing...", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								int row1=comboParam1.getSelectedIndex();
								int row2=comboParam2.getSelectedIndex();
								if(row1==row2){
									JOptionPane.showMessageDialog(frmSensTwoWay, "Please select two different parameters!");
								}
								else if(myModel.parseModel().size()>0){ //Check model
									JOptionPane.showMessageDialog(frmSensTwoWay, "Errors in base case model!");
								}
								else{
									
									boolean error=false;
									//Get parameters
									int intervals=Integer.parseInt(textIntervals.getText());
									
									String strMin1=(String)tableParams.getValueAt(row1,2);
									String strMin2=(String)tableParams.getValueAt(row2,2);
									String strMax1=(String)tableParams.getValueAt(row1,3);
									String strMax2=(String)tableParams.getValueAt(row2,3);
									strMin1=strMin1.replaceAll(",", ""); //Replace any commas
									strMin2=strMin2.replaceAll(",", "");
									strMax1=strMax1.replaceAll(",", "");
									strMax2=strMax2.replaceAll(",", "");
									double min1=Double.parseDouble(strMin1);
									double min2=Double.parseDouble(strMin2);
									double max1=Double.parseDouble(strMax1);
									double max2=Double.parseDouble(strMax2);
									double step1=(max1-min1)/(intervals*1.0);
									double step2=(max2-min2)/(intervals*1.0);
									curParam1=myModel.parameters.get(row1);
									curParam2=myModel.parameters.get(row2);
									//record min/max
									curParam1.sensMin=strMin1;
									curParam1.sensMax=strMax1;
									curParam2.sensMin=strMin2;
									curParam2.sensMax=strMax2;
									
									Numeric origValue1=curParam1.value.copy();
									Numeric origValue2=curParam2.value.copy();

									int dim=comboDimensions.getSelectedIndex();
									DimInfo info=myModel.dimInfo;
									int analysisType=0; //Analysis type for current 2-way, default EV
									
									double ceThresh=0;
									String lblOutcome="";
									if(info.analysisType==0){ //EV
										lblOutcome = info.dimNames[dim];
									}
									else{	
										if(dim==comboDimensions.getItemCount()-1){ //ICER or NMB selected
											if(info.analysisType==1){
												lblOutcome="ICER ("+info.dimSymbols[info.costDim]+"/"+info.dimSymbols[info.effectDim]+")";
												ceThresh=Double.parseDouble(textCEThresh.getText().replaceAll(",", ""));
												analysisType=1;
											}
											else if(info.analysisType==2){
												lblOutcome="NMB ("+info.dimSymbols[info.effectDim]+"-"+info.dimSymbols[info.costDim]+")";
												analysisType=2;
											}
										}
										else{
											lblOutcome = info.dimNames[dim];
										}
									}
									
									boolean max=true;
									if(comboMinMax.getSelectedIndex()==0){max=false;}
									
									//Test parameters at min and max...
									curParam1.value.setDouble(min1); curParam2.value.setDouble(min2);
									curParam1.locked=true; curParam2.locked=true;
									ArrayList<String> errorsMin=myModel.parseModel();
									curParam1.value.setDouble(max1); curParam2.value.setDouble(max2);
									curParam1.locked=true; curParam2.locked=true;
									ArrayList<String> errorsMax=myModel.parseModel();
									curParam1.value=origValue1; curParam2.value=origValue2; //Reset
									if(errorsMin.size()>0){
										error=true;
										curParam1.locked=false; curParam2.locked=false;
										myModel.validateModelObjects();
										JOptionPane.showMessageDialog(frmSensTwoWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curParam1.locked=false; curParam2.locked=false;
										myModel.validateModelObjects();
										JOptionPane.showMessageDialog(frmSensTwoWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										
										boolean origShowTrace=false;
										if(myModel.type==1) {
											origShowTrace=myModel.markov.showTrace;
											myModel.markov.showTrace=false;
										}
										
										int group=-1;
										if(comboGroup.isEnabled()){group=comboGroup.getSelectedIndex()-1;}
										
										int numStrat=myModel.strategyNames.length;
										dataEV=new double[numStrat][2][(intervals+1)*(intervals+1)];
										dataSurface=new double[numStrat][intervals+1][intervals+1];
										int numRuns=(intervals+1)*(intervals+1);
										progress.setMaximum(numRuns);
										
										long startTime=System.currentTimeMillis();
										
										int count=0;
										for(int i=0; i<=intervals; i++){
											double curVal1=min1+(step1*i);
											curParam1.value.setDouble(curVal1);
											for(int j=0; j<=intervals; j++){
												double curVal2=min2+(step2*j);
												curParam2.value.setDouble(curVal2);
												curParam1.locked=true; curParam2.locked=true;
												myModel.parseModel();
												myModel.runModel(null, false);
												
												if(analysisType==0){ //EV
													double maxEV=Double.NEGATIVE_INFINITY;
													double minEV=Double.POSITIVE_INFINITY;
													int maxStrat=-1, minStrat=-1;
													for(int s=0; s<numStrat; s++){
														dataEV[s][0][count]=curVal1;
														dataEV[s][1][count]=Double.NaN;
														double curOutcome;
														if(group==-1){curOutcome=myModel.getStrategyEV(s, dim);}
														else{curOutcome=myModel.getSubgroupEV(group, s, dim);}
														if(curOutcome>maxEV){maxEV=curOutcome; maxStrat=s;}
														if(curOutcome<minEV){minEV=curOutcome; minStrat=s;}
														dataSurface[s][i][j]=curOutcome;
													}
													if(max){dataEV[maxStrat][1][count]=curVal2;}
													else{dataEV[minStrat][1][count]=curVal2;}
												}
												else if(analysisType==1){ //CEA
													Object table[][]=new CEAHelper().calculateICERs(myModel,group,true);
													double bestICER=Double.NEGATIVE_INFINITY;
													int bestStrat=-1;
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														if(origStrat!=-1){
															dataEV[origStrat][0][count]=curVal1;
															dataEV[origStrat][1][count]=Double.NaN;
															double curICER=(double)table[s][4];
															if(curICER>bestICER && curICER<=ceThresh){
																bestICER=curICER;
																bestStrat=origStrat;
															}
															dataSurface[origStrat][i][j]=curICER;
														}
													}
													if(bestStrat!=-1){
														dataEV[bestStrat][1][count]=curVal2;
													}
												}
												else if(analysisType==2){ //BCA
													Object table[][]=new CEAHelper().calculateNMB(myModel,group,true);
													double maxNMB=Double.NEGATIVE_INFINITY;
													int maxStrat=-1;
													for(int s=0; s<table.length; s++){	
														int origStrat=(int) table[s][0];
														dataEV[origStrat][0][count]=curVal1;
														dataEV[origStrat][1][count]=Double.NaN;
														double curNMB=(double)table[s][4];
														if(curNMB>maxNMB){
															maxNMB=curNMB;
															maxStrat=origStrat;
														}
														dataSurface[origStrat][i][j]=curNMB;
													}
													dataEV[maxStrat][1][count]=curVal2;
												}
												
												count++;
												//Update progress
												double prog=(count/(numRuns*1.0))*100;
												long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
												remTime=(long) (remTime*(100-prog));
												remTime=remTime/1000;
												String seconds = Integer.toString((int)(remTime % 60));
												String minutes = Integer.toString((int)(remTime/60));
												if(seconds.length()<2){seconds="0"+seconds;}
												if(minutes.length()<2){minutes="0"+minutes;}
												progress.setProgress(count);
												progress.setNote("Time left: "+minutes+":"+seconds);
																								
												if(progress.isCanceled()){ //End loop
													cancelled=true;
													j=intervals+1;
													i=intervals+1;
												}
											}
										}
										//Reset parameter values
										curParam1.value=origValue1; curParam2.value=origValue2;
										curParam1.locked=false; curParam2.locked=false;
										myModel.validateModelObjects();
										
										if(myModel.type==1) {
											myModel.markov.showTrace=origShowTrace;
										}
										
										if(cancelled==false){
											//Update chart
											chart.getXYPlot().getDomainAxis().setLabel(curParam1.name);
											chart.getXYPlot().getRangeAxis().setLabel(curParam2.name);
											if(chartData.getSeriesCount()>0){
												for(int s=0; s<numStrat; s++){
													chartData.removeSeries(myModel.strategyNames[s]);
												}
											}
											for(int s=0; s<numStrat; s++){
												chartData.addSeries(myModel.strategyNames[s],dataEV[s]);
											}
											XYPlot plot = chart.getXYPlot();

											XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(false,true);
											Shape square=new Rectangle(-3,-3,6,6);
											DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											for(int s=0; s<numStrat; s++){
												renderer1.setSeriesPaint(s, supplier.getNextPaint());
												renderer1.setSeriesShape(s, square);
											}
											plot.setRenderer(renderer1);
											
											//Update surface chart
											int strat=comboStrategy.getSelectedIndex();
											surfaceModel = new SurfaceModel(dataSurface,strat,intervals,min1,max1,min2,max2,curParam1.name,curParam2.name,lblOutcome);
											surfacePanel.setModel(surfaceModel);
											surfacePanel.repaint();
											
											btnExport.setEnabled(true);
											tabbedPane.setEnabledAt(1, true);
										}
										progress.close();
									}
								}
							} catch (Exception e) {
								curParam1.locked=false; curParam2.locked=false;
								myModel.validateModelObjects();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmSensTwoWay, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			chartData = new DefaultXYDataset();
			chart = ChartFactory.createScatterPlot(null, "Param 1", "Param 2", chartData, PlotOrientation.VERTICAL, true, false, false);
			chart.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chart.getXYPlot().addDomainMarker(marker);
			chart.getXYPlot().addRangeMarker(marker);
			
			
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 1;
			gbc_tabbedPane.gridy = 0;
			frmSensTwoWay.getContentPane().add(tabbedPane, gbc_tabbedPane);

			ChartPanel panelChart = new ChartPanel(chart,false);
			tabbedPane.addTab("Area Chart", null, panelChart, null);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));
			
			JPanel panelSurface = new JPanel();
			panelSurface.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			tabbedPane.addTab("Surface Chart", null, panelSurface, null);
			tabbedPane.setEnabledAt(1, false);
			GridBagLayout gbl_panelSurface = new GridBagLayout();
			gbl_panelSurface.columnWidths = new int[]{71, 196, 1, 0};
			gbl_panelSurface.rowHeights = new int[]{0, 0, 0};
			gbl_panelSurface.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_panelSurface.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panelSurface.setLayout(gbl_panelSurface);
			
			surfacePanel = new SurfacePanel();
			GridBagConstraints gbc_panel_3 = new GridBagConstraints();
			gbc_panel_3.gridwidth = 3;
			gbc_panel_3.insets = new Insets(0, 0, 5, 5);
			gbc_panel_3.fill = GridBagConstraints.BOTH;
			gbc_panel_3.gridx = 0;
			gbc_panel_3.gridy = 0;
			panelSurface.add(surfacePanel, gbc_panel_3);
			surfacePanel.setLayout(new BorderLayout(0, 0));
			
			
			JLabel lblStrategy = new JLabel("Strategy");
			GridBagConstraints gbc_lblStrategy = new GridBagConstraints();
			gbc_lblStrategy.anchor = GridBagConstraints.WEST;
			gbc_lblStrategy.insets = new Insets(0, 0, 0, 5);
			gbc_lblStrategy.gridx = 0;
			gbc_lblStrategy.gridy = 1;
			panelSurface.add(lblStrategy, gbc_lblStrategy);
		
			comboStrategy = new JComboBox<String>(new DefaultComboBoxModel<String>(myModel.strategyNames));
			comboStrategy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Update surface chart
					int strat=comboStrategy.getSelectedIndex();
					surfaceModel = new SurfaceModel(dataSurface,strat,surfaceModel.intervals,surfaceModel.xMin,
							surfaceModel.xMax,surfaceModel.yMin,surfaceModel.yMax,surfaceModel.xLabel,
							surfaceModel.yLabel,surfaceModel.zLabel);
					surfacePanel.setModel(surfaceModel);
					surfacePanel.repaint();
				}
			});
			comboStrategy.setLightWeightPopupEnabled(false); //Ensure drop-down appears over canvas
		
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.insets = new Insets(0, 0, 0, 5);
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 1;
			panelSurface.add(comboStrategy, gbc_comboBox);

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
}
