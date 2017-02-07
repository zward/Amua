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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.SwingConstants;

/**
 *
 */
public class frmPlotFx {

	public JFrame frmPlotFx;
	VarHelper varHelper;
	StyledTextPane paneFunction;
	DefaultXYDataset chartDataCont, chartDataDis;
	XYSeriesCollection colCont, colDis; //Continuous/Discrete
	//XYSeries serCont, serDis;
	JFreeChart chart;
	String stratNames[];
	double dataEV[][][];
	private JTable table;
	JCheckBox chckbxMultiplot, chckbxDiscrete;
	XYLineAndShapeRenderer rendererCont, rendererDis;
	DefaultDrawingSupplier supplierVars;
	int curSeriesCont, curSeriesDis=0;
	boolean discrete=false;
	
	/**
	 *  Default Constructor
	 */
	public frmPlotFx(VarHelper varHelper) {
		this.varHelper=varHelper;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmPlotFx = new JFrame();
			frmPlotFx.setTitle("Amua - Plot Function");
			frmPlotFx.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmPlotFx.setBounds(100, 100, 1000, 600);
			frmPlotFx.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{316, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 61, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			frmPlotFx.getContentPane().setLayout(gridBagLayout);

			chartDataCont = new DefaultXYDataset();
			chartDataDis = new DefaultXYDataset();
			colCont=new XYSeriesCollection();
			colDis=new XYSeriesCollection();
			
			chart = ChartFactory.createScatterPlot(null, "x", "f(x)", chartDataCont, PlotOrientation.VERTICAL, false, false, false);
			XYPlot plot = chart.getXYPlot();
			plot.setDataset(0,chartDataCont);
			plot.setDataset(1,chartDataDis);
			
			rendererCont = new XYLineAndShapeRenderer(true,false);
			rendererDis = new XYLineAndShapeRenderer(true,true);
			plot.setRenderer(0,rendererCont);
			plot.setRenderer(1,rendererDis);
		
			ChartPanel panelChart = new ChartPanel(chart);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.gridwidth = 2;
			gbc_panelChart.insets = new Insets(0, 0, 5, 0);
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 0;
			gbc_panelChart.gridy = 0;
			frmPlotFx.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

			JPanel panel_1 = new JPanel();
			panel_1.setLayout(null);
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;
			frmPlotFx.getContentPane().add(panel_1, gbc_panel_1);

			JButton btnPlot = new JButton("Plot");
			btnPlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plot();
				}
			});
			btnPlot.setBounds(225, 26, 86, 28);
			panel_1.add(btnPlot);

			JLabel lblFunction = new JLabel("f(x):");
			lblFunction.setHorizontalAlignment(SwingConstants.RIGHT);
			lblFunction.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblFunction.setBounds(275, 6, 30, 16);
			panel_1.add(lblFunction);

			table = new JTable();
			table.setShowVerticalLines(true);
			table.setRowSelectionAllowed(false);
			table.setModel(new DefaultTableModel(
				new Object[][] {
					{"Min x", "0"},
					{"Max x", "1"},
					{"Intervals", "1000"},
				},
				new String[] {
					"New column", "New column"
				}
			) {
				boolean[] columnEditables = new boolean[] {
					false, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
			table.setBounds(6, 6, 120, 48);
			panel_1.add(table);
			
			chckbxMultiplot = new JCheckBox("Multi-plot");
			chckbxMultiplot.setBounds(203, 5, 81, 18);
			panel_1.add(chckbxMultiplot);
			
			JButton btnClear = new JButton("Clear");
			btnClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearPlot();
				}
			});
			btnClear.setBounds(127, 26, 86, 28);
			panel_1.add(btnClear);
			
			chckbxDiscrete = new JCheckBox("Discrete");
			chckbxDiscrete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxDiscrete.isSelected()){
						discrete=true;
						table.setValueAt("---", 2, 1);
					}
					else{
						discrete=false;
						table.setValueAt("1000", 2, 1);
					}
				}
			});
			chckbxDiscrete.setBounds(127, 5, 104, 18);
			panel_1.add(chckbxDiscrete);
			
			supplierVars = new DefaultDrawingSupplier();
			curSeriesCont=0;
			curSeriesDis=0;
		
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 1;
			frmPlotFx.getContentPane().add(scrollPane, gbc_scrollPane);

			paneFunction = new StyledTextPane(varHelper);
			scrollPane.setViewportView(paneFunction);
			paneFunction.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						plot();
					}
				}
			});
			
			//Draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chart.getXYPlot().addDomainMarker(marker);
			chart.getXYPlot().addRangeMarker(marker);

		} catch (Exception ex){
			ex.printStackTrace();
			varHelper.errorLog.recordError(ex);
		}
	}
	
	private void clearPlot(){
		while(chartDataCont.getSeriesCount()>0){
			chartDataCont.removeSeries(chartDataCont.getSeriesKey(0)+"");
		}
		while(chartDataDis.getSeriesCount()>0){
			chartDataDis.removeSeries(chartDataDis.getSeriesKey(0)+"");
		}
		curSeriesCont=0;
		curSeriesDis=0;
	}

	private void plot(){
		final ProgressMonitor progress=new ProgressMonitor(frmPlotFx, "Plot Function", "Calculating", 0, 100);

		Thread SimThread = new Thread(){ //Non-UI
			public void run(){
				try{
					//Validate domain
					boolean ok=true;
					double minX=0, maxX=0;
					int numIntervals=1000;
					try{
						minX=Double.parseDouble((String)table.getValueAt(0, 1));
						maxX=Double.parseDouble((String)table.getValueAt(1, 1));
						if(discrete==false){numIntervals=Integer.parseInt((String)table.getValueAt(2, 1));}
						else{numIntervals=(int)maxX-(int)minX;}
					}catch(Exception n){
						JOptionPane.showMessageDialog(frmPlotFx,"Invalid plot specification!");
						ok=false;
					}

					if(ok){
						progress.setMaximum(numIntervals);
						double step=(maxX-minX)/(numIntervals); //+1 for continuous????

						String fx=paneFunction.getText();

						//Get orig values for all variables
						int numVars=varHelper.variables.size();
						double origValues[]=new double[numVars];
						for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables
							origValues[v]=varHelper.variables.get(v).value;
						}

						double curData[][]=new double[2][numIntervals+1];
						for(int i=0; i<=numIntervals; i++){
							for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
								TreeVariable curVar=varHelper.variables.get(v);
								curVar.fixed=false;
								curVar.value=origValues[v];
							}

							double x=minX+step*i;
							TreeVariable curX=new TreeVariable();
							curX.name="x";
							curX.expression=x+"";
							curX.value=x;
							int index=varHelper.getVariableIndex("x");
							if(index!=-1){varHelper.variables.set(index, curX);}
							else{varHelper.variables.add(curX);}

							double curY;
							try{
								curY=varHelper.evaluateExpression(fx);
							}catch(Exception e1){
								curY=Double.NaN;
								e1.printStackTrace();
								JOptionPane.showMessageDialog(frmPlotFx, e1.getMessage());
								i=numIntervals; //end loop
							}
							curData[0][i]=x;
							curData[1][i]=curY;

							progress.setProgress(i);
						}

						//Remove var
						int index=varHelper.getVariableIndex("x");
						if(index!=-1){varHelper.variables.remove(index);}

						//Reset all variables
						for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
							TreeVariable curVar=varHelper.variables.get(v);
							curVar.fixed=false;
							curVar.value=origValues[v];
						}
						for(int v=0; v<numVars; v++){ //Evaluate all variables
							varHelper.variables.get(v).evaluate(varHelper);
						}

						//Update chart
						XYPlot plot = chart.getXYPlot();
						if(!chckbxMultiplot.isSelected()){
							supplierVars = new DefaultDrawingSupplier();
							clearPlot();
						}
						else{
							if(discrete==false){curSeriesCont++;}
							else{curSeriesDis++;}
						}
						
						if(discrete==false){
							rendererCont.setSeriesPaint(curSeriesCont, supplierVars.getNextPaint());
						}
						else{
							rendererDis.setSeriesPaint(curSeriesDis, supplierVars.getNextPaint());
							Shape circle=new Ellipse2D.Double(-3,-3,6,6);
							rendererDis.setSeriesShape(curSeriesDis, circle);
						}
						plot.setRenderer(0,rendererCont);
						plot.setRenderer(1,rendererDis);
						
						if(discrete==false){chartDataCont.addSeries(curSeriesCont+"", curData);}
						else{chartDataDis.addSeries(curSeriesDis+"", curData);}
					}

				} catch (Exception e) {
					int index=varHelper.getVariableIndex("x");
					if(index!=-1){varHelper.variables.remove(index);}
					e.printStackTrace();
					JOptionPane.showMessageDialog(frmPlotFx, e.getMessage());
				}
			}
		};
		SimThread.start();
	}
}
