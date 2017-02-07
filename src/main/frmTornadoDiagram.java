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
import java.util.Collections;
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;

import filters.CSVFilter;

import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmTornadoDiagram {

	public JFrame frmTornadoDiagram;
	PanelTree panel;
	DecisionTree tree;
	DefaultTableModel modelVars;
	private JTable tableVars;

	ChartPanel panelChart;
	JCheckBox chckbxRatio;
	JComboBox comboStrategy;
	JComboBox comboDimensions;
	JComboBox comboDenom;
	int strategies[];
	String stratNames[];
	JFreeChart chart;
	double baseOutcome;
	ArrayList<VarResult> results;

	/**
	 *  Default Constructor
	 */
	public frmTornadoDiagram(PanelTree panel) {
		this.panel=panel;
		this.tree=panel.tree;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTornadoDiagram = new JFrame();
			frmTornadoDiagram.setTitle("Amua - Tornado Diagram");
			frmTornadoDiagram.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmTornadoDiagram.setBounds(100, 100, 1000, 600);
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
			tableVars.setRowSelectionAllowed(false);
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
			lblOutcome.setBounds(6, 42, 81, 16);
			panel_2.add(lblOutcome);

			comboDimensions = new JComboBox(new DefaultComboBoxModel(tree.dimNames));
			comboDimensions.setBounds(88, 37, 227, 26);
			panel_2.add(comboDimensions);

			JButton btnRun = new JButton("Run");
			btnRun.setBounds(355, 5, 90, 28);
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
			chckbxRatio.setHorizontalAlignment(SwingConstants.LEFT);
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
			chckbxRatio.setBounds(355, 41, 90, 18);
			panel_2.add(chckbxRatio);

			TreeNode root=tree.nodes.get(0);
			strategies=new int[root.childIndices.size()];
			int numStrat=strategies.length;
			stratNames=new String[numStrat];
			for(int i=0; i<numStrat; i++){
				strategies[i]=root.childIndices.get(i);
				stratNames[i]=tree.nodes.get(strategies[i]).name;
			}

			JLabel lblStrategy = new JLabel("Strategy:");
			lblStrategy.setBounds(6, 11, 81, 16);
			panel_2.add(lblStrategy);

			comboStrategy = new JComboBox(new DefaultComboBoxModel(stratNames));
			comboStrategy.setBounds(88, 6, 227, 26);
			panel_2.add(comboStrategy);
			
			final JButton btnExport = new JButton("Export");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						//Show save as dialog
						JFileChooser fc=new JFileChooser(panel.filepath);
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
							out.write("Variable,Low,High"); out.newLine();
							int numVars=results.size();
							for(int v=0; v<numVars; v++){
								VarResult result=results.get(v);
								out.write(result.name+","+result.minVal+","+result.maxVal);
								out.newLine();
							}
							out.close();
							
							JOptionPane.showMessageDialog(frmTornadoDiagram, "Exported!");
						}


					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmTornadoDiagram, ex.getMessage());
						panel.errorLog.recordError(ex);
					}
				}
			});
			btnExport.setEnabled(false);
			btnExport.setBounds(355, 67, 90, 28);
			panel_2.add(btnExport);


			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
						int strat=strategies[comboStrategy.getSelectedIndex()];
						String curStrat=stratNames[comboStrategy.getSelectedIndex()];
						boolean ratio=chckbxRatio.isSelected();
						int dim=comboDimensions.getSelectedIndex();
						int dim2=comboDenom.getSelectedIndex();


						ArrayList<String> errorsBase=tree.parseTree();
						if(errorsBase.size()>0){
							JOptionPane.showMessageDialog(frmTornadoDiagram, "Errors in base case model!");
						}
						else{
							TreeNode root=tree.nodes.get(0);
							tree.runTree(root, false); //Get baseline
							if(ratio==false){baseOutcome=tree.nodes.get(strat).expectedValues[dim];}
							else{
								double num=tree.nodes.get(strat).expectedValues[dim];
								double denom=tree.nodes.get(strat).expectedValues[dim2];
								baseOutcome=num/(denom*1.0);
							}

							results=new ArrayList<VarResult>();
							double globalMin=Double.POSITIVE_INFINITY, globalMax=Double.NEGATIVE_INFINITY;

							int numVars=tableVars.getRowCount();
							for(int v=0; v<numVars; v++){
								String varName=(String)tableVars.getValueAt(v, 0);
								String strMin=(String)tableVars.getValueAt(v, 2);
								String strMax=(String)tableVars.getValueAt(v, 3);
								if(strMin!=null && strMin.length()>0 && strMax!=null && strMax.length()>0){
									VarResult result=new VarResult();
									result.name=varName;
									double min=Double.parseDouble(strMin);
									double max=Double.parseDouble(strMax);
									TreeVariable curVar=tree.variables.get(v);
									curVar.fixed=true;
									double origValue=curVar.value;
									double minOutcome, maxOutcome;

									//Min
									curVar.value=min;
									ArrayList<String> errorsMin=tree.parseTree();
									if(errorsMin.size()>0){
										curVar.value=origValue;
										curVar.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+varName+" - Min value");
										break;
									}
									else{
										tree.parseTree();
										tree.runTree(root, false);
										if(ratio==false){minOutcome=tree.nodes.get(strat).expectedValues[dim];}
										else{
											double num=tree.nodes.get(strat).expectedValues[dim];
											double denom=tree.nodes.get(strat).expectedValues[dim2];
											minOutcome=num/(denom*1.0);
										}
									}
									result.minVal=minOutcome;

									//Max
									curVar.value=max;
									ArrayList<String> errorsMax=tree.parseTree();
									if(errorsMax.size()>0){
										curVar.value=origValue;
										curVar.fixed=false;
										tree.evalAllVars();
										JOptionPane.showMessageDialog(frmTornadoDiagram, "Error: "+varName+" - Max value");
										break;
									}
									else{
										tree.parseTree();
										tree.runTree(root, false);
										if(ratio==false){maxOutcome=tree.nodes.get(strat).expectedValues[dim];}
										else{
											double num=tree.nodes.get(strat).expectedValues[dim];
											double denom=tree.nodes.get(strat).expectedValues[dim2];
											maxOutcome=num/(denom*1.0);
										}
									}
									result.maxVal=maxOutcome;
									result.range=(Math.abs(result.maxVal-result.minVal));

									curVar.value=origValue;
									curVar.fixed=false;
									results.add(result);
								}
							}
							tree.evalAllVars();
							//Update chart
							Collections.sort(results);

							numVars=results.size();
							String varNamesChrt[]=new String[numVars];
							double[][] starts = new double[1][numVars];   
							double[][] ends = new double[1][numVars];  
							for(int v=0; v<numVars; v++){
								varNamesChrt[v]=results.get(v).name;
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
							dataset.setCategoryKeys(varNamesChrt);

							CategoryAxis xAxis = new CategoryAxis("Variables");
							ValueAxis yAxis = new NumberAxis("EV ("+tree.dimSymbols[dim]+"): "+curStrat);
							yAxis.setRange(globalMin*1.1,globalMax*1.1);
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
						JOptionPane.showMessageDialog(frmTornadoDiagram, e1.getMessage());
						e1.printStackTrace();
						panel.errorLog.recordError(e1);
					}
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
			panel.errorLog.recordError(ex);
		}
	}

	class VarResult implements Comparable<VarResult>{
		String name;
		double minVal, maxVal;
		double range;

		@Override
		public int compareTo(VarResult result){
			if (this.range < result.range) return 1;
			else if (this.range == result.range) return 0;
			else return -1;
		}

	}
}
