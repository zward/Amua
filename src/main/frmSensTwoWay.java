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
import java.awt.Rectangle;
import java.awt.Shape;
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
import org.sf.surfaceplot.SurfaceCanvas;

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

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.ComboBoxModel;
import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmSensTwoWay {

	public JFrame frmSensTwoWay;
	PanelTree panel;
	DecisionTree tree;
	DefaultTableModel modelVars;
	private JTable tableVars;

	DefaultXYDataset chartData;
	JFreeChart chart;
	SurfaceCanvas surfaceCanvas;
	JCheckBox chckbxRatio;
	JComboBox comboDimensions;
	JComboBox comboDenom;
	JComboBox comboStrategy;
	int numStrat;
	int strategies[];
	String stratNames[];
	double dataEV[][][];
	double dataSurface[][][];
	SurfaceModel surfaceModel;
	private JTextField textIntervals;

	/**
	 *  Default Constructor
	 */
	public frmSensTwoWay(PanelTree panel) {
		this.panel=panel;
		this.tree=panel.tree;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmSensTwoWay = new JFrame();
			frmSensTwoWay.setTitle("Amua - Two-way Sensitivity Analysis");
			frmSensTwoWay.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmSensTwoWay.setBounds(100, 100, 1000, 600);
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
			gbl_panel_1.rowHeights = new int[]{466, 157, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			modelVars=new DefaultTableModel(
					new Object[][] {,},
					new String[] {"Variable","Expression","Min","Max"}) {
				boolean[] columnEditables = new boolean[] {false, false,true,true};
				public boolean isCellEditable(int row, int column) {return columnEditables[column];}
			};

			String varNames[]=new String[tree.variables.size()];

			for(int i=0; i<tree.variables.size(); i++){
				modelVars.addRow(new Object[]{null});
				modelVars.setValueAt(tree.variables.get(i).name, i, 0);
				modelVars.setValueAt(tree.variables.get(i).expression, i, 1);
				varNames[i]=tree.variables.get(i).name;
			}

			JScrollPane scrollPaneVars = new JScrollPane();
			GridBagConstraints gbc_scrollPaneVars = new GridBagConstraints();
			gbc_scrollPaneVars.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneVars.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneVars.gridx = 0;
			gbc_scrollPaneVars.gridy = 0;
			panel_1.add(scrollPaneVars, gbc_scrollPaneVars);
			tableVars = new JTable();
			tableVars.setRowSelectionAllowed(false);
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
			lblOutcome.setBounds(12, 94, 81, 16);
			panel_2.add(lblOutcome);

			comboDimensions = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDimensions.setBounds(94, 89, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(353, 7, 90, 28);
			panel_2.add(btnRun);

			final JLabel lblDenominator = new JLabel("Denominator:");
			lblDenominator.setBounds(12, 123, 89, 16);
			lblDenominator.setVisible(false);
			panel_2.add(lblDenominator);

			comboDenom = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDenom.setVisible(false);
			comboDenom.setBounds(94, 119, 227, 26);
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
			chckbxRatio.setBounds(6, 70, 81, 18);
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

						int returnVal = fc.showSaveDialog(frmSensTwoWay);
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

							JOptionPane.showMessageDialog(frmSensTwoWay, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmSensTwoWay, ex.getMessage());
						panel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setBounds(353, 36, 90, 28);
			panel_2.add(btnExport);

			JLabel lblVariable = new JLabel("Variable 1:");
			lblVariable.setBounds(12, 13, 81, 16);
			panel_2.add(lblVariable);

			final JComboBox comboVar1 = new JComboBox(new DefaultComboBoxModel(varNames));
			comboVar1.setBounds(94, 8, 227, 26);
			panel_2.add(comboVar1);

			JLabel lblVariable_1 = new JLabel("Variable 2:");
			lblVariable_1.setBounds(12, 42, 81, 16);
			panel_2.add(lblVariable_1);

			final JComboBox comboVar2 = new JComboBox(new DefaultComboBoxModel(varNames));
			comboVar2.setBounds(94, 38, 227, 26);
			panel_2.add(comboVar2);

			final JComboBox comboMinMax = new JComboBox();
			comboMinMax.setModel(new DefaultComboBoxModel(new String[] {"Min", "Max"}));
			comboMinMax.setSelectedIndex(1);
			comboMinMax.setBounds(353, 89, 90, 26);
			panel_2.add(comboMinMax);
			
			JLabel lblIntervals = new JLabel("Intervals:");
			lblIntervals.setBounds(333, 123, 55, 16);
			panel_2.add(lblIntervals);
			
			textIntervals = new JTextField();
			textIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textIntervals.setText("100");
			textIntervals.setBounds(388, 117, 55, 28);
			panel_2.add(textIntervals);
			textIntervals.setColumns(10);

			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ProgressMonitor progress=new ProgressMonitor(frmSensTwoWay, "Two-way", "Analyzing...", 0, 100);

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								//Check tree first
								ArrayList<String> errorsBase=tree.parseTree();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmSensTwoWay, "Errors in base case model!");
								}
								else{
									//Get variables
									int intervals=Integer.parseInt(textIntervals.getText());
									int row1=comboVar1.getSelectedIndex();
									int row2=comboVar2.getSelectedIndex();
									double min1=Double.parseDouble((String)tableVars.getValueAt(row1, 2));
									double min2=Double.parseDouble((String)tableVars.getValueAt(row2, 2));
									double max1=Double.parseDouble((String)tableVars.getValueAt(row1, 3));
									double max2=Double.parseDouble((String)tableVars.getValueAt(row2, 3));
									double step1=(max1-min1)/(intervals*1.0);
									double step2=(max2-min2)/(intervals*1.0);
									TreeVariable curVar1=tree.variables.get(row1);
									TreeVariable curVar2=tree.variables.get(row2);
									curVar1.fixed=true;
									curVar2.fixed=true;
									double origValue1=curVar1.value;
									double origValue2=curVar2.value;

									boolean ratio=chckbxRatio.isSelected();
									int dim=comboDimensions.getSelectedIndex();
									int dim2=comboDenom.getSelectedIndex();
									String lblOutcome=tree.dimNames[dim];
									if(ratio){
										lblOutcome=tree.dimNames[dim]+"/"+tree.dimNames[dim2];
									}

									boolean max=true;
									if(comboMinMax.getSelectedIndex()==0){max=false;}

									boolean error=false;
									//Test variables at min and max...
									curVar1.value=min1; curVar2.value=min2;
									ArrayList<String> errorsMin=tree.parseTree();
									curVar1.value=max1; curVar2.value=max2;
									ArrayList<String> errorsMax=tree.parseTree();
									curVar1.value=origValue1; curVar2.value=origValue2; //Reset
									if(errorsMin.size()>0){
										error=true;
										curVar1.fixed=false; curVar2.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmSensTwoWay, "Error: Min value");
									}
									if(errorsMax.size()>0){
										error=true;
										curVar1.fixed=false; curVar2.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmSensTwoWay, "Error: Max value");
									}

									if(error==false){
										boolean cancelled=false;
										//Run model...
										TreeNode root=tree.nodes.get(0);
										
										dataEV=new double[numStrat][2][(intervals+1)*(intervals+1)];
										dataSurface=new double[numStrat][intervals+1][intervals+1];
										progress.setMaximum((intervals+1)*(intervals+1));
										int count=0;
										for(int i=0; i<=intervals; i++){
											double curVal1=min1+(step1*i);
											curVar1.value=curVal1;
											for(int j=0; j<=intervals; j++){
												double curVal2=min2+(step2*j);
												curVar2.value=curVal2;
												tree.parseTree();
												tree.runTree(root, false);
												double maxEV=Double.NEGATIVE_INFINITY;
												double minEV=Double.POSITIVE_INFINITY;
												int maxStrat=-1, minStrat=-1;
												for(int s=0; s<numStrat; s++){
													dataEV[s][0][count]=curVal1;
													dataEV[s][1][count]=Double.NaN;
													double curOutcome;
													if(ratio==false){
														curOutcome=tree.nodes.get(strategies[s]).expectedValues[dim];
													}
													else{
														double num=tree.nodes.get(strategies[s]).expectedValues[dim];
														double denom=tree.nodes.get(strategies[s]).expectedValues[dim2];
														curOutcome=num/(denom*1.0);
													}
													if(curOutcome>maxEV){maxEV=curOutcome; maxStrat=s;}
													if(curOutcome<minEV){minEV=curOutcome; minStrat=s;}
													dataSurface[s][i][j]=curOutcome;
												}
												if(max){dataEV[maxStrat][1][count]=curVal2;}
												else{dataEV[minStrat][1][count]=curVal2;}
												count++;
												progress.setProgress(count);
												if(progress.isCanceled()){ //End loop
													cancelled=true;
													j=intervals+1;
													i=intervals+1;
												}
											}
										}
										//Reset variable values
										curVar1.value=origValue1; curVar2.value=origValue2;
										curVar1.fixed=false; curVar2.fixed=false;
										tree.evalAllVars();

										if(cancelled==false){
											//Update chart
											chart.getXYPlot().getDomainAxis().setLabel(curVar1.name);
											chart.getXYPlot().getRangeAxis().setLabel(curVar2.name);
											if(chartData.getSeriesCount()>0){
												for(int s=0; s<numStrat; s++){
													chartData.removeSeries(stratNames[s]);
												}
											}
											for(int s=0; s<numStrat; s++){
												chartData.addSeries(stratNames[s],dataEV[s]);
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
											surfaceModel = new SurfaceModel(dataSurface,strat,intervals,min1,max1,min2,max2,curVar1.name,curVar2.name,lblOutcome);
											surfaceCanvas.setModel(surfaceModel);
											surfaceCanvas.repaint();
											
											btnExport.setEnabled(true);
											tabbedPane.setEnabledAt(1, true);
										}
										progress.close();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmSensTwoWay, e.getMessage());
								panel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			chartData = new DefaultXYDataset();
			chart = ChartFactory.createScatterPlot(null, "Var 1", "Var 2", chartData, PlotOrientation.VERTICAL, true, false, false);

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

			ChartPanel panelChart = new ChartPanel(chart);
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
			
			JPanel panel_3 = new JPanel();
			GridBagConstraints gbc_panel_3 = new GridBagConstraints();
			gbc_panel_3.gridwidth = 3;
			gbc_panel_3.insets = new Insets(0, 0, 5, 5);
			gbc_panel_3.fill = GridBagConstraints.BOTH;
			gbc_panel_3.gridx = 0;
			gbc_panel_3.gridy = 0;
			panelSurface.add(panel_3, gbc_panel_3);
			panel_3.setLayout(new BorderLayout(0, 0));
	
			surfaceCanvas = new SurfaceCanvas();
			panel_3.add(surfaceCanvas);
			
			JLabel lblStrategy = new JLabel("Strategy");
			GridBagConstraints gbc_lblStrategy = new GridBagConstraints();
			gbc_lblStrategy.anchor = GridBagConstraints.WEST;
			gbc_lblStrategy.insets = new Insets(0, 0, 0, 5);
			gbc_lblStrategy.gridx = 0;
			gbc_lblStrategy.gridy = 1;
			panelSurface.add(lblStrategy, gbc_lblStrategy);
		
			TreeNode root=tree.nodes.get(0);
			strategies=new int[root.childIndices.size()];
			numStrat=strategies.length;
			stratNames=new String[numStrat];
			for(int i=0; i<numStrat; i++){
				strategies[i]=root.childIndices.get(i);
				stratNames[i]=tree.nodes.get(strategies[i]).name;
			}
			
			comboStrategy = new JComboBox(new DefaultComboBoxModel(stratNames));
			comboStrategy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Update surface chart
					int strat=comboStrategy.getSelectedIndex();
					surfaceModel = new SurfaceModel(dataSurface,strat,surfaceModel.intervals,surfaceModel.xMin,
							surfaceModel.xMax,surfaceModel.yMin,surfaceModel.yMax,surfaceModel.xLabel,
							surfaceModel.yLabel,surfaceModel.zLabel);
					surfaceCanvas.setModel(surfaceModel);
					surfaceCanvas.repaint();
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
			panel.errorLog.recordError(ex);
		}
	}
}
