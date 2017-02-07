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
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmSensOneWay {

	public JFrame frmSensOneWay;
	PanelTree panel;
	DecisionTree tree;
	DefaultTableModel modelVars;
	private JTable tableVars;

	DefaultXYDataset chartData;
	JFreeChart chart;
	JCheckBox chckbxRatio;
	JComboBox comboDimensions;
	JComboBox comboDenom;
	String stratNames[];
	double dataEV[][][];
	private JTextField textIntervals;

	/**
	 *  Default Constructor
	 */
	public frmSensOneWay(PanelTree panel) {
		this.panel=panel;
		this.tree=panel.tree;
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
			frmSensOneWay.setBounds(100, 100, 1000, 600);
			frmSensOneWay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{460, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			frmSensOneWay.getContentPane().setLayout(gridBagLayout);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 0;
			frmSensOneWay.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{466, 100, 0};
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
			lblOutcome.setBounds(6, 40, 81, 16);
			panel_2.add(lblOutcome);

			comboDimensions = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDimensions.setBounds(88, 35, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(354, 34, 90, 28);
			panel_2.add(btnRun);

			final JLabel lblDenominator = new JLabel("Denominator:");
			lblDenominator.setBounds(6, 73, 89, 16);
			lblDenominator.setVisible(false);
			panel_2.add(lblDenominator);

			comboDenom = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDenom.setVisible(false);
			comboDenom.setBounds(88, 68, 227, 26);
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
			chckbxRatio.setBounds(6, 10, 104, 18);
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

						int returnVal = fc.showSaveDialog(frmSensOneWay);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							//Headers
							out.write(chart.getXYPlot().getDomainAxis().getLabel()+","); //Var name
							String outcome=chart.getXYPlot().getRangeAxis().getLabel();
							int numStrat=stratNames.length;
							for(int s=0; s<numStrat-1; s++){out.write(outcome+"-"+stratNames[s]+",");}
							out.write(outcome+"-"+stratNames[numStrat-1]); out.newLine();
							//Chart data
							int numPoints=dataEV[0][0].length;
							for(int i=0; i<numPoints; i++){
								out.write(dataEV[0][0][i]+","); //x-axis
								for(int s=0; s<numStrat-1; s++){out.write(dataEV[s][1][i]+",");} //y-axis
								out.write(dataEV[numStrat-1][1][i]+""); out.newLine();
							}
							out.close();

							JOptionPane.showMessageDialog(frmSensOneWay, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmSensOneWay, ex.getMessage());
						panel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(354, 67, 90, 28);
			panel_2.add(btnExport);
			
			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(204, 12, 55, 16);
			panel_2.add(lblIntervals);
			
			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("100");
			textIntervals.setBounds(260, 6, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);


			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmSensOneWay, "One-way sensitivity", "Running", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								//Check tree first
								ArrayList<String> errorsBase=tree.parseTree();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmSensOneWay, "Errors in base case model!");
								}
								else{
									//Get variable
									int intervals=Integer.parseInt(textIntervals.getText());
									int row=tableVars.getSelectedRow();
									double min=Double.parseDouble((String)tableVars.getValueAt(row, 2));
									double max=Double.parseDouble((String)tableVars.getValueAt(row, 3));
									double step=(max-min)/(intervals*1.0);
									TreeVariable curVar=tree.variables.get(row);
									curVar.fixed=true;
									double origValue=curVar.value;

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
										JOptionPane.showMessageDialog(frmSensOneWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curVar.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmSensOneWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										TreeNode root=tree.nodes.get(0);
										int strategies[]=new int[root.childIndices.size()];
										int numStrat=strategies.length;
										stratNames=new String[numStrat];
										for(int i=0; i<numStrat; i++){
											strategies[i]=root.childIndices.get(i);
											stratNames[i]=tree.nodes.get(strategies[i]).name;
										}

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
											progress.setProgress(i);
											if(progress.isCanceled()){ //End loop
												cancelled=true;
												i=intervals+1;
											}
										}
										//Reset var value
										curVar.value=origValue;
										curVar.fixed=false;
										tree.evalAllVars();

										if(cancelled==false){
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
											btnExport.setEnabled(true);
										}
										progress.close();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmSensOneWay, e.getMessage());
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
			frmSensOneWay.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

		} catch (Exception ex){
			ex.printStackTrace();
			panel.errorLog.recordError(ex);
		}
	}
}
