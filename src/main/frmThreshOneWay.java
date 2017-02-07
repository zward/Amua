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

import javax.swing.border.LineBorder;

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;
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
	PanelTree panel;
	DecisionTree tree;
	DefaultTableModel modelVars;
	private JTable tableVars;

	DefaultXYDataset chartData;
	JFreeChart chart;
	JCheckBox chckbxRatio;
	JComboBox comboDimensions;
	JComboBox comboDenom;
	int strategies[];
	String stratNames[];
	double dataEV[][][];
	private JTextField textThresh;
	private JTextField textIntervals;

	/**
	 *  Default Constructor
	 */
	public frmThreshOneWay(PanelTree panel) {
		this.panel=panel;
		this.tree=panel.tree;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmThreshOneWay = new JFrame();
			frmThreshOneWay.setTitle("Amua - Threshold Analysis");
			frmThreshOneWay.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmThreshOneWay.setBounds(100, 100, 1000, 600);
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
			gbl_panel_1.rowHeights = new int[]{401, 154, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelVars=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Variable", "Expression","Min","Max"}) {
				boolean[] columnEditables = new boolean[] {false, false,true,true};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			for(int i=0; i<tree.variables.size(); i++){
				modelVars.addRow(new Object[]{null});
				modelVars.setValueAt(tree.variables.get(i).name, i, 0);
				modelVars.setValueAt(tree.variables.get(i).expression, i, 1);
			}

			JScrollPane scrollPaneVars = new JScrollPane();
			GridBagConstraints gbc_scrollPaneVars = new GridBagConstraints();
			gbc_scrollPaneVars.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneVars.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneVars.gridx = 0;
			gbc_scrollPaneVars.gridy = 0;
			panel_1.add(scrollPaneVars, gbc_scrollPaneVars);
			tableVars = new JTable();
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
			lblOutcome.setBounds(6, 85, 81, 16);
			panel_2.add(lblOutcome);

			comboDimensions = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDimensions.setBounds(88, 80, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(348, 7, 90, 28);
			panel_2.add(btnRun);

			final JLabel lblDenominator = new JLabel("Denominator:");
			lblDenominator.setBounds(6, 120, 89, 16);
			lblDenominator.setVisible(false);
			panel_2.add(lblDenominator);

			comboDenom = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDenom.setVisible(false);
			comboDenom.setBounds(88, 115, 227, 26);
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
			chckbxRatio.setBounds(348, 123, 90, 18);
			panel_2.add(chckbxRatio);

			JLabel lblStrategy = new JLabel("Strategy 1:");
			lblStrategy.setBounds(6, 13, 67, 16);
			panel_2.add(lblStrategy);

			TreeNode root=tree.nodes.get(0);
			strategies=new int[root.childIndices.size()];
			int numStrat=strategies.length;
			stratNames=new String[numStrat];
			for(int i=0; i<numStrat; i++){
				strategies[i]=root.childIndices.get(i);
				stratNames[i]=tree.nodes.get(strategies[i]).name;
			}

			final JComboBox comboStrat1 = new JComboBox(new DefaultComboBoxModel(stratNames));
			comboStrat1.setBounds(88, 8, 227, 26);
			panel_2.add(comboStrat1);

			JLabel lblStrategy_1 = new JLabel("Strategy 2:");
			lblStrategy_1.setBounds(6, 51, 67, 16);
			panel_2.add(lblStrategy_1);

			final JComboBox comboStrat2 = new JComboBox(new DefaultComboBoxModel(stratNames));
			comboStrat2.setBounds(88, 46, 227, 26);
			panel_2.add(comboStrat2);

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
			lblIntervals.setBounds(327, 47, 55, 16);
			panel_2.add(lblIntervals);
			
			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("100");
			textIntervals.setBounds(383, 39, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);


			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmThreshOneWay, "Threshold analysis", "Analyzing...", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{

								int strat1=comboStrat1.getSelectedIndex();
								int strat2=comboStrat2.getSelectedIndex();
								//Check tree first
								ArrayList<String> errorsBase=tree.parseTree();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Errors in base case model!");
								}
								else if(strat1==strat2){
									JOptionPane.showMessageDialog(frmThreshOneWay, "Please select 2 different strategies!");
								}
								else{
									//Get variable
									int intervals=Integer.parseInt(textIntervals.getText());
									int row=tableVars.getSelectedRow();
									double min=Double.parseDouble((String)tableVars.getValueAt(row, 2));
									double max=Double.parseDouble((String)tableVars.getValueAt(row, 3));
									double step=(max-min)/(intervals*1.0);
									TreeVariable curVar=tree.variables.get(row);
									double origValue=curVar.value;
									curVar.fixed=true;

									boolean ratio=chckbxRatio.isSelected();
									int dim=comboDimensions.getSelectedIndex();
									int dim2=comboDenom.getSelectedIndex();

									boolean error=false;
									//Test variable at min and max...
									curVar.value=min;
									ArrayList<String> errorsMin=tree.parseTree();
									curVar.value=max;
									ArrayList<String> errorsMax=tree.parseTree();
									curVar.value=origValue; //Reset
									if(errorsMin.size()>0){
										error=true;
										curVar.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmThreshOneWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curVar.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmThreshOneWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										TreeNode root=tree.nodes.get(0);
										int numStrat=strategies.length;
										double minDist=Double.POSITIVE_INFINITY;
										int minIndex=-1;

										dataEV=new double[numStrat][2][intervals+1];
										progress.setMaximum(intervals+1);
										for(int i=0; i<=intervals; i++){
											double curVal=min+(step*i);
											curVar.value=curVal;
											tree.parseTree();
											tree.runTree(root, false);
											for(int s=0; s<numStrat; s++){
												dataEV[s][0][i]=curVal;
												double curOutcome;
												if(ratio==false){
													curOutcome=tree.nodes.get(strategies[s]).expectedValues[dim];
												}
												else{
													double num=tree.nodes.get(strategies[s]).expectedValues[dim];
													double denom=tree.nodes.get(strategies[s]).expectedValues[dim2];
													curOutcome=num/(denom*1.0);
												}
												dataEV[s][1][i]=curOutcome;
											}
											double curDist=Math.abs(dataEV[strat1][1][i]-dataEV[strat2][1][i]);
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
											if(minDist==0){ //Intersection found
												intersection=dataEV[0][0][minIndex];
											}
											else{
												if(minIndex==0 || minIndex==intervals){
													JOptionPane.showMessageDialog(frmThreshOneWay, "No intersection found in current range!");
												}
												else{ //Search neighbourhood for intersection
													double minVal=dataEV[0][0][minIndex];
													double thresh=Math.pow(10, -(tree.decimals+1));
													int i=0;
													while(minDist>thresh && i<1000){ //Binary search of neighbourhood until convergence
														//Left
														double valL=minVal-(step/2.0);
														curVar.value=valL;
														tree.parseTree();
														tree.runTree(root, false);
														double result1L=tree.nodes.get(strategies[strat1]).expectedValues[dim];
														double result2L=tree.nodes.get(strategies[strat2]).expectedValues[dim];
														if(ratio==true){
															double num1=tree.nodes.get(strategies[strat1]).expectedValues[dim];
															double denom1=tree.nodes.get(strategies[strat1]).expectedValues[dim2];
															result1L=num1/(denom1*1.0);
															double num2=tree.nodes.get(strategies[strat2]).expectedValues[dim];
															double denom2=tree.nodes.get(strategies[strat2]).expectedValues[dim2];
															result2L=num2/(denom2*1.0);
														}
														double distL=Math.abs(result1L-result2L);
														//Right
														double valR=minVal+(step/2.0);
														curVar.value=valR;
														tree.parseTree();
														tree.runTree(root, false);
														double result1R=tree.nodes.get(strategies[strat1]).expectedValues[dim];
														double result2R=tree.nodes.get(strategies[strat2]).expectedValues[dim];
														if(ratio==true){
															double num1=tree.nodes.get(strategies[strat1]).expectedValues[dim];
															double denom1=tree.nodes.get(strategies[strat1]).expectedValues[dim2];
															result1R=num1/(denom1*1.0);
															double num2=tree.nodes.get(strategies[strat2]).expectedValues[dim];
															double denom2=tree.nodes.get(strategies[strat2]).expectedValues[dim2];
															result2R=num2/(denom2*1.0);
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
													}
													if(minDist<thresh){ //Convergence achieved
														intersection=minVal;
													}
													else{
														JOptionPane.showMessageDialog(frmThreshOneWay, "No intersection found in current range!");
													}
												}
											}
											textThresh.setText(tree.round(intersection)+"");

											//Update chart
											chart.getXYPlot().getDomainAxis().setLabel(curVar.name);
											if(ratio==false){
												chart.getXYPlot().getRangeAxis().setLabel("EV ("+tree.dimSymbols[dim]+")");
											}
											else{
												chart.getXYPlot().getRangeAxis().setLabel("EV ("+tree.dimSymbols[dim]+"/"+tree.dimSymbols[dim2]+")");
											}
											if(chartData.getSeriesCount()>0){
												for(int s=0; s<numStrat; s++){
													chartData.removeSeries(stratNames[s]);
												}
											}
											for(int s=0; s<numStrat; s++){
												chartData.addSeries(stratNames[s],dataEV[s]);
											}
											XYPlot plot = chart.getXYPlot();

											XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true,false);
											DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
											for(int s=0; s<numStrat; s++){
												renderer1.setSeriesPaint(s, supplier.getNextPaint());
											}
											plot.setRenderer(renderer1);
										}

										//Reset var value
										curVar.value=origValue;
										curVar.fixed=false;
										tree.evalAllVars();

										progress.close();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmThreshOneWay, e.getMessage());
								panel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			chartData = new DefaultXYDataset();
			chart = ChartFactory.createScatterPlot(null, "x", "EV($)", chartData, PlotOrientation.VERTICAL, true, false, false);

			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chart.getXYPlot().addDomainMarker(marker);
			chart.getXYPlot().addRangeMarker(marker);

			ChartPanel panelChart = new ChartPanel(chart);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 1;
			gbc_panelChart.gridy = 0;
			frmThreshOneWay.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			panel.errorLog.recordError(ex);
		}
	}
}
