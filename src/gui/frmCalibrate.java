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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
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
import base.RunReport;
import main.Constraint;
import main.DimInfo;
import main.MersenneTwisterFast;
import main.Parameter;
import main.ParameterSet;
import main.ScaledIcon;
import main.StyledTextPane;
import markov.MarkovNode;
import math.Interpreter;
import math.KernelSmooth;
import math.Numeric;

/**
 *
 */
public class frmCalibrate {

	public JFrame frmCalibrate;
	AmuaModel myModel;
	int numParams;
	int numConst;
	Numeric origValues[];
	StyledTextPane textPaneExpression;
	ArrayList<MarkovNode> chainRoots;

	DefaultXYDataset chartData, chartDataScores;
	JFreeChart chart, chartScores;
	Paint seriesPaints_Scores[], seriesPaints_Params[];
	int numSeries_Params;

	JComboBox comboPlot;
	private JTextField textNumSets;
	String CEAnotes[][];
	DefaultTableModel modelParamSets;
	private JTable tableParamSets;
	int numSets;
	String paramNames[];
	ParameterSet params[];
	double paramVals[][]; //[param][set]
	DefaultTableModel modelCalibSettings;
	private JTable tableCalibSettings;
	DefaultTableModel modelParams;
	private JTable tableParams;

	public frmCalibrate(AmuaModel myModel){
		this.myModel=myModel;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmCalibrate = new JFrame();
			frmCalibrate.setTitle("Amua - "+myModel.language.base.getString("title.model_calibration")); //Model Calibration
			frmCalibrate.setFont(myModel.language.font);
			frmCalibrate.setIconImage(Toolkit.getDefaultToolkit().getImage(frmCalibrate.class.getResource("/images/calibrate_128.png")));
			frmCalibrate.setBounds(100, 100, 1020, 545);
			frmCalibrate.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 460, 0, 0};
			gridBagLayout.rowHeights = new int[]{24, 514, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmCalibrate.getContentPane().setLayout(gridBagLayout);

			//get default series colors
			seriesPaints_Scores=new Paint[1];
			DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
			seriesPaints_Scores[0]=supplier.getNextPaint();

			numParams=myModel.parameters.size();
			numConst=myModel.constraints.size();
			paramNames=new String[numParams];
			origValues=new Numeric[numParams];
			DefaultDrawingSupplier supplierParams = new DefaultDrawingSupplier();
			seriesPaints_Params=new Paint[numParams];
			numSeries_Params=numParams;
			for(int i=0; i<numParams; i++){
				paramNames[i]=myModel.parameters.get(i).name;
				origValues[i]=myModel.parameters.get(i).value.copy();
				seriesPaints_Params[i]=supplierParams.getNextPaint();
			}

			JToolBar toolBar = new JToolBar();
			toolBar.setBorderPainted(false);
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			toolBar.setBounds(1, 40, 48, 24);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.anchor = GridBagConstraints.SOUTHWEST;
			gbc_toolBar.insets = new Insets(0, 0, 5, 5);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			frmCalibrate.getContentPane().add(toolBar, gbc_toolBar);

			JButton btnFx = new JButton("");
			btnFx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmExpressionBuilder window=new frmExpressionBuilder(myModel,textPaneExpression,false);
					window.frmExpressionBuilder.setVisible(true);
				}
			});
			btnFx.setToolTipText(myModel.language.base.getString("button.build_expression")); //Build Expression
			btnFx.setFocusPainted(false);
			btnFx.setIcon(new ScaledIcon("/images/formula",24,24,24,true));
			toolBar.add(btnFx);

			JLabel lblLikelihooddistanceScore = new JLabel(myModel.language.analysis.getString("calib.calib_score_expression")+":"); //Calibration Score Expression
			lblLikelihooddistanceScore.setFont(myModel.language.font);
			GridBagConstraints gbc_lblLikelihooddistanceScore = new GridBagConstraints();
			gbc_lblLikelihooddistanceScore.insets = new Insets(0, 0, 5, 5);
			gbc_lblLikelihooddistanceScore.gridx = 1;
			gbc_lblLikelihooddistanceScore.gridy = 0;
			frmCalibrate.getContentPane().add(lblLikelihooddistanceScore, gbc_lblLikelihooddistanceScore);

			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
			gbc_tabbedPane.gridheight = 2;
			gbc_tabbedPane.insets = new Insets(0, 0, 0, 5);
			gbc_tabbedPane.fill = GridBagConstraints.BOTH;
			gbc_tabbedPane.gridx = 2;
			gbc_tabbedPane.gridy = 0;
			frmCalibrate.getContentPane().add(tabbedPane, gbc_tabbedPane);

			JPanel panel = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("object.sets"), null, panel, null); //Sets
			tabbedPane.setFont(myModel.language.font);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{522, 0};
			gbl_panel.rowHeights = new int[]{263, 180, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);


			modelParamSets=new DefaultTableModel(){
				@Override
				public Class getColumnClass(int column) {
					return Double.class;
				}
			};
			modelParamSets.addColumn(myModel.language.base.getString("object.set")); //Set
			modelParamSets.addColumn(myModel.language.base.getString("object.score")); //Score
			for(int i=0; i<numParams; i++){
				modelParamSets.addColumn(paramNames[i]);
			}
			
			JScrollPane scrollPane_1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.gridx = 0;
			gbc_scrollPane_1.gridy = 0;
			panel.add(scrollPane_1, gbc_scrollPane_1);

			tableParamSets = new JTable();
			tableParamSets.setEnabled(false);
			tableParamSets.setShowVerticalLines(true);
			tableParamSets.setModel(modelParamSets);
			tableParamSets.getTableHeader().setFont(myModel.language.font);
			tableParamSets.setFont(myModel.language.font);
			tableParamSets.getTableHeader().setReorderingAllowed(false);
			tableParamSets.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tableParamSets.setAutoCreateRowSorter(true);
			scrollPane_1.setViewportView(tableParamSets);

			JPanel panel_3 = new JPanel();
			tabbedPane.addTab(myModel.language.base.getString("plot.plots"), null, panel_3, null); //Plots
			tabbedPane.setFont(myModel.language.font);
			GridBagLayout gbl_panel_3 = new GridBagLayout();
			gbl_panel_3.columnWidths = new int[]{228, 0, 0};
			gbl_panel_3.rowHeights = new int[]{0, 0};
			gbl_panel_3.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panel_3.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			panel_3.setLayout(gbl_panel_3);

			JPanel panel_4 = new JPanel();
			GridBagConstraints gbc_panel_4 = new GridBagConstraints();
			gbc_panel_4.insets = new Insets(0, 0, 0, 5);
			gbc_panel_4.fill = GridBagConstraints.BOTH;
			gbc_panel_4.gridx = 0;
			gbc_panel_4.gridy = 0;
			panel_3.add(panel_4, gbc_panel_4);
			GridBagLayout gbl_panel_4 = new GridBagLayout();
			gbl_panel_4.columnWidths = new int[]{0, 0};
			gbl_panel_4.rowHeights = new int[]{0, 40, 0};
			gbl_panel_4.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel_4.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			panel_4.setLayout(gbl_panel_4);

			JScrollPane scrollPane_3 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
			gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_3.gridx = 0;
			gbc_scrollPane_3.gridy = 0;
			panel_4.add(scrollPane_3, gbc_scrollPane_3);

			modelParams=new DefaultTableModel(
					new Object[][] {},
					new String[] {
							myModel.language.base.getString("object.parameter"), myModel.language.base.getString("object.expression") //Parameter, Expression
					}
					) {
				boolean[] columnEditables = new boolean[] {
						false, false
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};

			tableParams = new JTable();
			tableParams.setShowVerticalLines(true);
			tableParams.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tableParams.setModel(modelParams);
			tableParams.getTableHeader().setFont(myModel.language.font);
			tableParams.setFont(myModel.language.font);
			scrollPane_3.setViewportView(tableParams);

			JPanel panel_6 = new JPanel();
			GridBagConstraints gbc_panel_6 = new GridBagConstraints();
			gbc_panel_6.fill = GridBagConstraints.BOTH;
			gbc_panel_6.gridx = 0;
			gbc_panel_6.gridy = 1;
			panel_4.add(panel_6, gbc_panel_6);
			GridBagLayout gbl_panel_6 = new GridBagLayout();
			gbl_panel_6.columnWidths = new int[]{151, 62, 0};
			gbl_panel_6.rowHeights = new int[]{28, 0};
			gbl_panel_6.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_6.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel_6.setLayout(gbl_panel_6);
			//comboPlot.setModel(new DefaultComboBoxModel<String>(new String[] {"Density","Histogram","Cumulative Distribution","Quantiles","By Set"}));
			String plotType[]=new String[5];
			plotType[0]=myModel.language.base.getString("plot.density"); //Density
			plotType[1]=myModel.language.base.getString("plot.histogram"); //Histogram
			plotType[2]=myModel.language.base.getString("plot.cumulative_distribution"); //Cumulative Distribution
			plotType[3]=myModel.language.base.getString("plot.quantiles"); //Quantiles
			plotType[4]=myModel.language.base.getString("plot.by_set"); //By Set

			JButton btnUpdatePlot = new JButton(myModel.language.base.getString("button.plot")); //Plot
			btnUpdatePlot.setFont(myModel.language.font);
			btnUpdatePlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					while(chartData.getSeriesCount()>0){ //clear chart
						chartData.removeSeries(chartData.getSeriesKey(0));
					}


					int plotType=comboPlot.getSelectedIndex();
					int selected[]=tableParams.getSelectedRows();
					int numSelected=selected.length;

					int numSeries=numSelected;
					if(numSeries>0 && numSeries!=numSeries_Params) { //reset series colours
						numSeries_Params=numSeries;
						seriesPaints_Params=new Paint[numSeries];
						DefaultDrawingSupplier supplier = new DefaultDrawingSupplier();
						XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
						for(int s=0; s<numSeries; s++) {
							seriesPaints_Params[s]=supplier.getNextPaint();
							renderer.setSeriesPaint(s, seriesPaints_Params[s]);
						}
					}

					if(plotType==0){ //Density
						chart.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("calib.parameter_value")); //Parameter Value
						chart.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("calib.posterior_density")); //Posterior Density
						for(int i=0; i<numSelected; i++){
							int curIndex=selected[i];
							double density[][]=KernelSmooth.density(paramVals[curIndex], 100);
							chartData.addSeries(paramNames[curIndex], density);
						}
					}
					else if(plotType==1){ //Histogram
						chart.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("calib.parameter_value")); //Parameter Value
						chart.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("calib.posterior_frequency")); //Posterior Frequency
						for(int i=0; i<numSelected; i++){
							int curIndex=selected[i];
							double hist[][]=KernelSmooth.histogram(paramVals[curIndex], 100, 10);
							chartData.addSeries(paramNames[curIndex], hist);
						}
					}
					else if(plotType==2){ //CDF
						chart.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("calib.parameter_value")); //Parameter Value
						chart.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("calib.posterior_cdf")); //Posterior CDF
						for(int i=0; i<numSelected; i++){
							int curIndex=selected[i];
							double cdf[][]=KernelSmooth.cdf(paramVals[curIndex]);
							chartData.addSeries(paramNames[curIndex], cdf);
						}
					}
					else if(plotType==3){ //Quantiles
						chart.getXYPlot().getDomainAxis().setLabel(myModel.language.analysis.getString("calib.posterior_quantile")); //Posterior Quantile
						chart.getXYPlot().getRangeAxis().setLabel(myModel.language.base.getString("object.parameter")); //Parameter
						for(int i=0; i<numSelected; i++){
							int curIndex=selected[i];
							double cdf[][]=KernelSmooth.quantiles(paramVals[curIndex]);
							chartData.addSeries(paramNames[curIndex], cdf);
						}
					}
					else if(plotType==4){ //By Set
						chart.getXYPlot().getDomainAxis().setLabel(myModel.language.base.getString("object.parameter_set")); //Parameter Set
						chart.getXYPlot().getRangeAxis().setLabel(myModel.language.analysis.getString("calib.parameter_value")); //Parameter Value
						for(int i=0; i<numSelected; i++){
							int curIndex=selected[i];
							double vals[][]=new double[2][numSets];
							for(int j=0; j<numSets; j++){
								vals[0][j]=(j+1); //set
								vals[1][j]=paramVals[curIndex][j];
							}
							chartData.addSeries(paramNames[curIndex], vals);
						}

					}

					XYPlot plotResults = chart.getXYPlot();
					XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
					//DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
					for(int i=0; i<numSelected; i++){
						rendererResults.setSeriesPaint(i, seriesPaints_Params[i]);
					}
					plotResults.setRenderer(rendererResults);


				}
			});

			comboPlot = new JComboBox();
			comboPlot.setModel(new DefaultComboBoxModel<String>(plotType));
			comboPlot.setFont(myModel.language.font);
			
			GridBagConstraints gbc_comboPlot = new GridBagConstraints();
			gbc_comboPlot.fill = GridBagConstraints.BOTH;
			gbc_comboPlot.insets = new Insets(0, 0, 0, 5);
			gbc_comboPlot.gridx = 0;
			gbc_comboPlot.gridy = 0;
			panel_6.add(comboPlot, gbc_comboPlot);
			GridBagConstraints gbc_btnUpdatePlot = new GridBagConstraints();
			gbc_btnUpdatePlot.fill = GridBagConstraints.BOTH;
			gbc_btnUpdatePlot.gridx = 1;
			gbc_btnUpdatePlot.gridy = 0;
			panel_6.add(btnUpdatePlot, gbc_btnUpdatePlot);

			chartData = new DefaultXYDataset();
			chart = ChartFactory.createScatterPlot(null, myModel.language.analysis.getString("result.value"), myModel.language.base.getString("plot.density"), //Value, Density 
					chartData, PlotOrientation.VERTICAL, true, false, false);
			chart.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chart.getXYPlot().addDomainMarker(marker);
			chart.getXYPlot().addRangeMarker(marker);
			//font
			chart.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chart.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chart.getLegend().setItemFont(myModel.language.font);

			chartDataScores = new DefaultXYDataset();
			chartScores = ChartFactory.createScatterPlot(null, myModel.language.base.getString("plot.order"), myModel.language.base.getString("object.score"), //Order, Score 
					chartDataScores, PlotOrientation.VERTICAL, true, false, false);
			chartScores.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//Draw axes
			ValueMarker markerScore = new ValueMarker(0);  // position is the value on the axis
			markerScore.setPaint(Color.black);
			chartScores.getXYPlot().addDomainMarker(markerScore);
			chartScores.getXYPlot().addRangeMarker(markerScore);
			//font
			chartScores.getXYPlot().getDomainAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScores.getXYPlot().getRangeAxis().setLabelFont(myModel.language.font.deriveFont(Font.BOLD, 14f));
			chartScores.getLegend().setItemFont(myModel.language.font);

			ChartPanel panelChart = new ChartPanel(chart,false);
			GridBagConstraints gbc_panel_chart = new GridBagConstraints();
			gbc_panel_chart.fill = GridBagConstraints.BOTH;
			gbc_panel_chart.gridx = 1;
			gbc_panel_chart.gridy = 0;
			panel_3.add(panelChart, gbc_panel_chart);

			//pop-up menu
			JPopupMenu popup = panelChart.getPopupMenu();
			JMenuItem mntmChangeColor = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chart, chartData, seriesPaints_Params, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColor, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			ChartPanel panelChartScores = new ChartPanel(chartScores,false);
			GridBagConstraints gbc_panel_scores = new GridBagConstraints();
			gbc_panel_scores.fill = GridBagConstraints.BOTH;
			gbc_panel_scores.gridx = 0;
			gbc_panel_scores.gridy = 1;
			panel.add(panelChartScores, gbc_panel_scores);

			//pop-up menu
			popup = panelChartScores.getPopupMenu();
			JMenuItem mntmChangeColorScores = new JMenuItem(myModel.language.base.getString("plot.change_series_colors")+"..."); //Change Series Colors
			mntmChangeColorScores.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					frmChangeSeriesColors window=new frmChangeSeriesColors(chartScores, chartDataScores, seriesPaints_Scores, myModel.language);
					window.frmChangeSeriesColors.setVisible(true);
				}
			});
			popup.insert(mntmChangeColorScores, 0);
			myModel.language.installMenuFontUpdater(popup); //set font
			myModel.language.setChartPropertiesFont(popup, 1);

			JPanel panel_1 = new JPanel();
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.gridwidth = 2;
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;
			frmCalibrate.getContentPane().add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{455, 0};
			gbl_panel_1.rowHeights = new int[]{253, 184, 0};
			gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 0;
			panel_1.add(scrollPane, gbc_scrollPane);

			textPaneExpression = new StyledTextPane(myModel, myModel.language);
			//textPaneExpression.setFont(new Font("Consolas", Font.PLAIN, 15));
			textPaneExpression.setFont(myModel.language.fontCode.deriveFont(Font.PLAIN, 15f));
			scrollPane.setViewportView(textPaneExpression);
			
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
					outcomes[dimInfo.dimNames.length]=myModel.language.analysis.getString("cea.icer")+" ("+dimInfo.dimNames[dimInfo.costDim]+"/"+dimInfo.dimNames[dimInfo.effectDim]+")"; //ICER
				}
				else if(dimInfo.analysisType==2){ //BCA
					outcomes[dimInfo.dimNames.length]=myModel.language.analysis.getString("bca.nmb")+" ("+dimInfo.dimNames[dimInfo.effectDim]+"-"+dimInfo.dimNames[dimInfo.costDim]+")"; //NMB
				}
			}
			//comboMethod.setModel(new DefaultComboBoxModel(new String[] {"Random", "Approximate Bayesian Computation"}));
			String methods[]=new String[2];
			methods[0]=myModel.language.analysis.getString("calib.random"); //Random
			methods[1]=myModel.language.analysis.getString("calib.abc"); //Approximate Bayesian Computation

			modelCalibSettings=new DefaultTableModel(
					new Object[][] {
					},
					new String[] {
							myModel.language.analysis.getString("gen.setting"), myModel.language.analysis.getString("result.value") //Setting, Value
					}
					) {
				boolean[] columnEditables = new boolean[] {
						false, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};

			chainRoots=new ArrayList<MarkovNode>();
			int numNodes=myModel.markov.nodes.size();
			for(int i=0; i<numNodes; i++){
				MarkovNode curNode=myModel.markov.nodes.get(i);
				if(curNode.type==1){
					chainRoots.add(curNode);
				}
			}
			String chainNames[]=new String[chainRoots.size()];
			for(int i=0; i<chainRoots.size(); i++){
				chainNames[i]=chainRoots.get(i).name;
			}

			JPanel panel_5 = new JPanel();
			GridBagConstraints gbc_panel_5 = new GridBagConstraints();
			gbc_panel_5.fill = GridBagConstraints.BOTH;
			gbc_panel_5.gridx = 0;
			gbc_panel_5.gridy = 1;
			panel_1.add(panel_5, gbc_panel_5);
			GridBagLayout gbl_panel_5 = new GridBagLayout();
			gbl_panel_5.columnWidths = new int[]{77, 315, 0, 51, 0};
			gbl_panel_5.rowHeights = new int[]{0, 0, 0, 72, 0};
			gbl_panel_5.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_5.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panel_5.setLayout(gbl_panel_5);

			JLabel lblChain = new JLabel(myModel.language.base.getString("node.chain")+":");
			lblChain.setFont(myModel.language.font);
			GridBagConstraints gbc_lblChain = new GridBagConstraints();
			gbc_lblChain.anchor = GridBagConstraints.EAST;
			gbc_lblChain.insets = new Insets(0, 0, 5, 5);
			gbc_lblChain.gridx = 0;
			gbc_lblChain.gridy = 0;
			panel_5.add(lblChain, gbc_lblChain);

			final JComboBox comboChain = new JComboBox(new DefaultComboBoxModel(chainNames));
			comboChain.setFont(myModel.language.font);
			GridBagConstraints gbc_comboChain = new GridBagConstraints();
			gbc_comboChain.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboChain.insets = new Insets(0, 0, 5, 5);
			gbc_comboChain.gridx = 1;
			gbc_comboChain.gridy = 0;
			panel_5.add(comboChain, gbc_comboChain);

			JButton btnRun = new JButton(myModel.language.base.getString("menu.run"));
			btnRun.setFont(myModel.language.font);
			GridBagConstraints gbc_btnRun = new GridBagConstraints();
			gbc_btnRun.gridwidth = 2;
			gbc_btnRun.insets = new Insets(0, 0, 5, 0);
			gbc_btnRun.gridx = 2;
			gbc_btnRun.gridy = 0;
			panel_5.add(btnRun, gbc_btnRun);
			
			final JComboBox comboMethod = new JComboBox();
			comboMethod.setFont(myModel.language.font);
			
			final JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.setFont(myModel.language.font);

			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//final ProgressMonitor progress=new ProgressMonitor(frmCalibrate, myModel.language.message.getString("info.calibrating"), myModel.language.message.getString("info.time_left")+": ??:??", 0, 100); //Calibrating, Time Left
					final frmProgressMonitor progress=new frmProgressMonitor(frmCalibrate, myModel.language.message.getString("info.calibrating"), myModel.language.message.getString("info.running"), 0, 100, myModel.language); 
					SwingUtilities.invokeLater(progress::show);  //dialog is created/shown on EDT

					Thread SimThread = new Thread(){ //Non-UI
						public void run(){
							try{
								//Check model first
								MarkovNode curChain=chainRoots.get(comboChain.getSelectedIndex());
								myModel.panelMarkov.curNode=curChain; //select current chain

								boolean origShowTrace=true;
								if(myModel.type==1){
									origShowTrace=myModel.markov.showTrace;
									myModel.markov.showTrace=false;
								}

								ArrayList<String> errorsBase=myModel.parseModel();
								if(errorsBase.size()>0){
									JOptionPane.showMessageDialog(frmCalibrate, myModel.language.message.getString("err.base_case")); //Errors in base case model!
								}
								else{
									//initial run to build trace
									RunReport curReport=new RunReport(myModel);
									myModel.markov.runModel(false,curReport,false);
								}

								//try evaluate score expression
								String strScore=textPaneExpression.getText();
								boolean go=true;
								try{
									Interpreter.evaluate(strScore, myModel, false, myModel.language);
								}
								catch(Exception e){
									go=false;
									JOptionPane.showMessageDialog(frmCalibrate,myModel.language.message.getString("err.score_expression")+": "+e.toString()); //Error in score expression
									if(myModel.type==1){
										myModel.markov.showTrace=origShowTrace;
									}
								}
								if(go){

									int method=comboMethod.getSelectedIndex();
									numSets=Integer.parseInt(textNumSets.getText());
									progress.setMaximum(numSets);
									params=new ParameterSet[numSets];

									modelParams.setRowCount(0);

									myModel.sampleParam=true;
									myModel.generatorParam=new MersenneTwisterFast();
									if(myModel.curGenerator==null){myModel.curGenerator=new MersenneTwisterFast[1];}
									myModel.curGenerator[0]=myModel.generatorParam;


									//Get orig values for all parameters
									Numeric origValues[]=new Numeric[numParams];
									for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters
										origValues[v]=myModel.parameters.get(v).value.copy();
										modelParams.addRow(new Object[]{null});
										modelParams.setValueAt(myModel.parameters.get(v).name, v, 0);
										modelParams.setValueAt(myModel.parameters.get(v).expression, v, 1);
									}
									//Initialize constraint expressions
									for(int c=0; c<numConst; c++){
										myModel.constraints.get(c).parseConstraints();
									}

									long startTime=System.currentTimeMillis();
									boolean wasCancelled=false;

									if(method==0){ //random
										for(int i=0; i<numSets; i++){
											progress.setProgress(i+1);
											sampleAll();
											RunReport curReport=new RunReport(myModel);
											myModel.markov.runModel(false,curReport,false);
											double curScore=Interpreter.evaluate(strScore, myModel, false, myModel.language).getDouble(myModel.language);
											params[i]=new ParameterSet(myModel);
											params[i].id=(i+1)+"";
											params[i].score=curScore;
											if(progress.isCanceled()){ //listen for cancel
												numSets=i; //end for loop
												wasCancelled=true;
											}
											//Update progress bar
											String remTime=estimateTime(startTime,i,numSets);
											progress.setNote(myModel.language.message.getString("info.time_left")+": "+remTime); //Time left
										}
									}
									else if(method==1){ //ABC
										double thresh=Double.parseDouble((String) tableCalibSettings.getValueAt(0,1));
										//progress.setMillisToPopup(0);
										//progress.setMillisToDecideToPopup(0);
										//progress.setProgress(0);
										//progress.setProgress(1);
										progress.setNote(myModel.language.message.getString("info.sampling")+"..."); //Sampling

										for(int i=0; i<numSets; i++){
											progress.setProgress(i+1);
											double curScore=thresh+1;
											while(curScore>thresh){
												sampleAll();
												RunReport curReport=new RunReport(myModel);
												myModel.markov.runModel(false,curReport,false);
												curScore=Interpreter.evaluate(strScore, myModel, false, myModel.language).getDouble(myModel.language);
												if(progress.isCanceled()){ //listen for cancel
													curScore=thresh-1; //end while loop
													numSets=i; //end for loop
													wasCancelled=true;
												}
											}
											if(wasCancelled==false){
												params[i]=new ParameterSet(myModel);
												params[i].id=(i+1)+"";
												params[i].score=curScore;
											}

											//Update progress bar
											String remTime=estimateTime(startTime,i,numSets);
											progress.setNote(myModel.language.message.getString("info.time_left")+": "+remTime); //Time left

										}
									}

									if(myModel.type==1){
										myModel.markov.showTrace=origShowTrace;
									}

									//Reset all parameters
									myModel.sampleParam=false;
									for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters and orig values
										Parameter curParam=myModel.parameters.get(v);
										curParam.locked=false;
										curParam.value=origValues[v];
									}
									myModel.validateModelObjects();

									//Get parameter values
									paramVals=new double[numParams][numSets];
									double scores[]=new double[numSets];
									modelParamSets.setRowCount(0);
									for(int i=0; i<numSets; i++){
										modelParamSets.addRow(new Object[]{null});
										modelParamSets.setValueAt(Double.parseDouble(params[i].id), i, 0);
										modelParamSets.setValueAt(params[i].score, i, 1);
										scores[i]=params[i].score;
										for(int j=0; j<numParams; j++){
											//modelParamSets.setValueAt(params[i].values[j].toString(),i,2+j);
											//update parameter ranges
											double val=Double.NaN;
											try{
												val=params[i].values[j].getDouble(myModel.language);
											} catch(Exception e){
												val=Double.NaN;
											}
											modelParamSets.setValueAt(val,i,2+j);

											paramVals[j][i]=val;
										}
									}

									progress.close();

									//plot scores
									while(chartDataScores.getSeriesCount()>0){ //clear chart
										chartDataScores.removeSeries(chartDataScores.getSeriesKey(0));
									}
									double scoreData[][]=new double[2][numSets];
									Arrays.sort(scores);
									for(int i=0; i<numSets; i++){
										scoreData[0][i]=i+1;
										scoreData[1][i]=scores[i];
									}
									chartDataScores.addSeries(myModel.language.base.getString("object.scores"), scoreData); //Scores
									chartScores.removeLegend();

									XYPlot plotScores = chartScores.getXYPlot();
									XYLineAndShapeRenderer rendererScores = new XYLineAndShapeRenderer(true,false);
									rendererScores.setSeriesPaint(0,Color.RED);
									plotScores.setRenderer(rendererScores);

									btnSave.setEnabled(true);
								}
							} catch (Exception e) {
								//Reset all parameters
								myModel.sampleParam=false;
								for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters and orig values
									Parameter curParam=myModel.parameters.get(v);
									curParam.locked=false;
									curParam.value=origValues[v];
								}
								myModel.validateModelObjects();
								e.printStackTrace();
								JOptionPane.showMessageDialog(frmCalibrate, e.getMessage());
								myModel.errorLog.recordError(e);
							}
						}
					};
					SimThread.start();
				}
			});

			JLabel lblMethod = new JLabel(myModel.language.analysis.getString("calib.method")+":");
			lblMethod.setFont(myModel.language.font);
			GridBagConstraints gbc_lblMethod = new GridBagConstraints();
			gbc_lblMethod.anchor = GridBagConstraints.EAST;
			gbc_lblMethod.insets = new Insets(0, 0, 5, 5);
			gbc_lblMethod.gridx = 0;
			gbc_lblMethod.gridy = 1;
			panel_5.add(lblMethod, gbc_lblMethod);

			GridBagConstraints gbc_comboMethod = new GridBagConstraints();
			gbc_comboMethod.insets = new Insets(0, 0, 5, 5);
			gbc_comboMethod.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboMethod.gridx = 1;
			gbc_comboMethod.gridy = 1;
			panel_5.add(comboMethod, gbc_comboMethod);
			comboMethod.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int method=comboMethod.getSelectedIndex();
					if(method==0){ //random
						modelCalibSettings.setRowCount(0);
						tableCalibSettings.setEnabled(false);
					}
					else if(method==1){ //abc
						modelCalibSettings.setRowCount(1);
						modelCalibSettings.setValueAt(myModel.language.analysis.getString("calib.score_threshold"), 0, 0); //Score Threshold
						tableCalibSettings.setEnabled(true);
					}
				}
			});
			comboMethod.setModel(new DefaultComboBoxModel(methods));

			
			GridBagConstraints gbc_btnSave = new GridBagConstraints();
			gbc_btnSave.gridwidth = 2;
			gbc_btnSave.insets = new Insets(0, 0, 5, 0);
			gbc_btnSave.gridx = 2;
			gbc_btnSave.gridy = 1;
			panel_5.add(btnSave, gbc_btnSave);
			btnSave.setEnabled(false);

			JLabel lblCalibrationSettings = new JLabel(myModel.language.analysis.getString("calib.calib_settings")); //Calibration Settings
			lblCalibrationSettings.setFont(myModel.language.font.deriveFont(Font.ITALIC, 12f));
			GridBagConstraints gbc_lblCalibrationSettings = new GridBagConstraints();
			gbc_lblCalibrationSettings.anchor = GridBagConstraints.SOUTHWEST;
			gbc_lblCalibrationSettings.gridwidth = 2;
			gbc_lblCalibrationSettings.insets = new Insets(0, 0, 5, 5);
			gbc_lblCalibrationSettings.gridx = 0;
			gbc_lblCalibrationSettings.gridy = 2;
			panel_5.add(lblCalibrationSettings, gbc_lblCalibrationSettings);
			
			JLabel lblIntervals = new JLabel(myModel.language.analysis.getString("calib.num_param_sets")+":");
			lblIntervals.setFont(myModel.language.font);
			GridBagConstraints gbc_lblIntervals = new GridBagConstraints();
			gbc_lblIntervals.insets = new Insets(0, 0, 5, 5);
			gbc_lblIntervals.gridx = 2;
			gbc_lblIntervals.gridy = 2;
			panel_5.add(lblIntervals, gbc_lblIntervals);

			textNumSets = new JTextField();
			GridBagConstraints gbc_textNumSets = new GridBagConstraints();
			gbc_textNumSets.fill = GridBagConstraints.HORIZONTAL;
			gbc_textNumSets.insets = new Insets(0, 0, 5, 0);
			gbc_textNumSets.gridx = 3;
			gbc_textNumSets.gridy = 2;
			panel_5.add(textNumSets, gbc_textNumSets);
			textNumSets.setHorizontalAlignment(SwingConstants.CENTER);
			textNumSets.setText("50");
			textNumSets.setColumns(5);

			JScrollPane scrollPane_2 = new JScrollPane();
			GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
			gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_2.gridwidth = 4;
			gbc_scrollPane_2.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane_2.gridx = 0;
			gbc_scrollPane_2.gridy = 3;
			panel_5.add(scrollPane_2, gbc_scrollPane_2);

			tableCalibSettings = new JTable();
			tableCalibSettings.setRowSelectionAllowed(false);
			tableCalibSettings.setShowVerticalLines(true);
			tableCalibSettings.getTableHeader().setReorderingAllowed(false);
			tableCalibSettings.getTableHeader().setFont(myModel.language.font);
			tableCalibSettings.setModel(modelCalibSettings);
			tableCalibSettings.setFont(myModel.language.font); //for cells
			tableCalibSettings.getColumnModel().getColumn(0).setPreferredWidth(121);
			tableCalibSettings.getColumnModel().getColumn(1).setPreferredWidth(121);
			scrollPane_2.setViewportView(tableCalibSettings);
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myModel.parameterNames=paramNames;
					myModel.parameterSets=params;
					myModel.buildParamSetsTable(myModel.mainForm.modelParamSets);
					myModel.mainForm.chckbxUseParamSets.setEnabled(true);
					myModel.saveModel();
					JOptionPane.showMessageDialog(frmCalibrate, myModel.language.message.getString("info.param_sets_saved")); //Parameter sets saved!
				}
			});

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private String estimateTime(long startTime,int i, int numSets){
		double prog=((i+1)/(numSets*1.0))*100;
		long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
		remTime=(long) (remTime*(100-prog));
		remTime=remTime/1000;
		String seconds = Integer.toString((int)(remTime % 60));
		String minutes = Integer.toString((int)(remTime/60));
		if(seconds.length()<2){seconds="0"+seconds;}
		if(minutes.length()<2){minutes="0"+minutes;}
		return(minutes+":"+seconds);
	}

	private void sampleAll(){
		try{
			//Sample parameters
			boolean validParams=false;
			while(validParams==false){
				for(int v=0; v<numParams; v++){ //Reset 'fixed' for all parameters and orig values
					Parameter curParam=myModel.parameters.get(v);
					curParam.locked=false;
					curParam.value=origValues[v];
				}

				for(int v=0; v<numParams; v++){ //sample all parameters
					Parameter curParam=myModel.parameters.get(v);
					if(curParam.locked==false) {
						curParam.value=Interpreter.evaluateTokens(curParam.parsedTokens, 0, true, myModel.language);
						curParam.locked=true;
					}
				}
				//check constraints
				validParams=true;
				int c=0;
				while(validParams==true && c<numConst){
					Constraint curConst=myModel.constraints.get(c);
					validParams=curConst.checkConstraints(myModel);
					c++;
				}
				if(validParams){ //check model for valid params
					if(myModel.parseModel().size()!=0){validParams=false;}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmCalibrate, e.getMessage());
			myModel.errorLog.recordError(e);
		}
	}
}
