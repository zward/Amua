/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2020 Zachary J. Ward
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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.SystemColor;
import java.awt.Toolkit;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import base.AmuaModel;
import base.RunReport;
import main.ErrorLog;
import main.ScaledIcon;

import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import javax.swing.JToolBar;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

/**
 *
 */
public class frmCEPlane {

	public JFrame frmCEPlane;
	AmuaModel myModel;
	RunReport runReport;

	int numSubgroups=0;
	String subgroupNames[];
	
	boolean flipOrientation=false;

	DefaultXYDataset dataXY;
	XYSeriesCollection colSeries;
	JFreeChart chartPlane;
	ChartPanel panelChart;
	XYLineAndShapeRenderer renderer;
	DefaultDrawingSupplier supplier;
	ErrorLog errorLog;
	private JComboBox<String> comboGroup;
	private JComboBox<String> comboRelative;
	
	int lblSize=10;
	int markerSize=6;
	private JLabel lblLabelSize;
	private JButton buttonLblDec, buttonLblInc;
	private JSeparator separator;
	private JTextField textLblSize;
	private JSeparator separator_1;
	private JLabel lblMarkerSize;
	private JTextField textMarkerSize;
	private JButton buttonMarkerDec;
	private JButton buttonMarkerInc;

	/**
	 *  Default Constructor
	 */
	public frmCEPlane(AmuaModel myModel, RunReport runReport) {
		this.myModel=myModel;
		this.errorLog=myModel.errorLog;
		this.runReport=runReport;

		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmCEPlane = new JFrame();
			frmCEPlane.setTitle("Amua - "+myModel.name+" - C/E Plane");
			frmCEPlane.setIconImage(Toolkit.getDefaultToolkit().getImage(frmCEPlane.class.getResource("/images/logo_128.png")));
			frmCEPlane.setBounds(100, 100, 500, 500);
			frmCEPlane.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{561, 0};
			gridBagLayout.rowHeights = new int[]{0, 514, 0};
			gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmCEPlane.getContentPane().setLayout(gridBagLayout);

			dataXY = new DefaultXYDataset();
			colSeries=new XYSeriesCollection();

			String lblEffect="∆ "+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim];
			String lblCost="∆ "+myModel.dimInfo.dimNames[myModel.dimInfo.costDim];

			chartPlane = ChartFactory.createScatterPlot(null, lblEffect, lblCost, dataXY, PlotOrientation.VERTICAL, false, false, false);
			chartPlane.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			//draw axes
			ValueMarker marker = new ValueMarker(0);  // position is the value on the axis
			marker.setPaint(Color.black);
			chartPlane.getXYPlot().addDomainMarker(marker);
			chartPlane.getXYPlot().addRangeMarker(marker);

			/*if(numSubgroups>0){
				String names[]=new String[numSubgroups+1];
				names[0]="Overall";
				for(int g=0; g<numSubgroups; g++){names[g+1]=subgroupNames[g];}
				comboGroup.setModel(new DefaultComboBoxModel(names));
				comboGroup.setVisible(true);
				lblGroup.setVisible(true);
			}*/
			
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.fill = GridBagConstraints.BOTH;
			gbc_toolBar.insets = new Insets(0, 0, 5, 0);
			gbc_toolBar.gridx = 0;
			gbc_toolBar.gridy = 0;
			frmCEPlane.getContentPane().add(toolBar, gbc_toolBar);
			
			JButton btnFlipAxes = new JButton("Flip Axes");
			btnFlipAxes.setIcon(new ScaledIcon("/images/flipAxes",16,16,16,true));
			btnFlipAxes.setFocusable(false);
			btnFlipAxes.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					flipOrientation=!flipOrientation;
					XYPlot plot = chartPlane.getXYPlot();
					if(flipOrientation) {
						plot.setOrientation(PlotOrientation.HORIZONTAL);
					}
					else {
						plot.setOrientation(PlotOrientation.VERTICAL);
					}
				}
			});
			toolBar.add(btnFlipAxes);
			
			comboRelative = new JComboBox(new DefaultComboBoxModel(new String[] {"Relative","Absolute"}));
			comboRelative.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			toolBar.add(comboRelative);
			
			comboGroup = new JComboBox<String>();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}
			});
			comboGroup.setVisible(false);
			if(myModel.simType==1 && myModel.reportSubgroups){
				numSubgroups=myModel.subgroupNames.size();
				String groups[]=new String[numSubgroups+1];
				groups[0]="Overall";
				for(int g=0; g<numSubgroups; g++){
					groups[g+1]=myModel.subgroupNames.get(g);
				}
				comboGroup.setModel(new DefaultComboBoxModel<String>(groups));
				comboGroup.setVisible(true);
			}
			toolBar.add(comboGroup);
			
			separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
			toolBar.add(separator);
			
			lblLabelSize = new JLabel(" Label size:");
			toolBar.add(lblLabelSize);
			
			textLblSize = new JTextField();
			textLblSize.setBackground(SystemColor.info);
			textLblSize.setText(lblSize+"");
			textLblSize.setEditable(false);
			textLblSize.setColumns(2);
			toolBar.add(textLblSize);
			
			
			buttonLblDec = new JButton(" – ");
			buttonLblDec.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					lblSize--;
					if(lblSize<0) {lblSize=0;}
					textLblSize.setText(lblSize+"");
					updateChart();
				}
			});
			buttonLblDec.setFont(new Font("SansSerif", Font.PLAIN, 14));
			toolBar.add(buttonLblDec);
			
			buttonLblInc = new JButton(" + ");
			buttonLblInc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					lblSize++;
					textLblSize.setText(lblSize+"");
					updateChart();
				}
			});
			buttonLblInc.setFont(new Font("SansSerif", Font.PLAIN, 14));
			toolBar.add(buttonLblInc);
			
			separator_1 = new JSeparator();
			separator_1.setOrientation(SwingConstants.VERTICAL);
			toolBar.add(separator_1);
			
			lblMarkerSize = new JLabel(" Marker size:");
			toolBar.add(lblMarkerSize);
			
			textMarkerSize = new JTextField();
			textMarkerSize.setBackground(SystemColor.info);
			textMarkerSize.setText(markerSize+"");
			textMarkerSize.setEditable(false);
			toolBar.add(textMarkerSize);
			textMarkerSize.setColumns(2);
			
			buttonMarkerDec = new JButton(" – ");
			buttonMarkerDec.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					markerSize--;
					if(markerSize<1) {markerSize=1;}
					textMarkerSize.setText(markerSize+"");
					updateChart();
				}
			});
			buttonMarkerDec.setFont(new Font("SansSerif", Font.PLAIN, 14));
			toolBar.add(buttonMarkerDec);
			
			buttonMarkerInc = new JButton(" + ");
			buttonMarkerInc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					markerSize++;
					textMarkerSize.setText(markerSize+"");
					updateChart();
				}
			});
			buttonMarkerInc.setFont(new Font("SansSerif", Font.PLAIN, 14));
			toolBar.add(buttonMarkerInc);

			panelChart = new ChartPanel(chartPlane,false);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 0;
			gbc_panelChart.gridy = 1;
			frmCEPlane.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));

			updateChart(); //show overall


		} catch (Exception ex){
			ex.printStackTrace();
		}
	}



	public void updateChart(){
		int relative=comboRelative.getSelectedIndex();
		int group=comboGroup.getSelectedIndex();
		
		//Clear series
		while(dataXY.getSeriesCount()>0){
			dataXY.removeSeries(dataXY.getSeriesKey(0));
		}

		XYPlot plot = chartPlane.getXYPlot();
		plot.clearAnnotations();
		
		renderer = new XYLineAndShapeRenderer(false,true);
		Shape dot=new Ellipse2D.Double(-(markerSize)/2.0,-(markerSize)/2.0,markerSize,markerSize);
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setSeriesShape(0, dot);
		renderer.setSeriesPaint(1, Color.RED);
		renderer.setSeriesShape(1, dot);
		
		plot.setRenderer(renderer);

		Object table[][]=runReport.table;
		if(group>0) {
			table=runReport.tableGroup[group-1];
		}
		
		int numStrat=runReport.numStrat;
		int base=-1;
		boolean viable[]=new boolean[numStrat];
		int numNonviable=0;
		for(int s=0; s<numStrat; s++) {
			viable[s]=true; //default to viable
			String note=(String) table[s][5];
			if(note==null || note.isEmpty()) {
				viable[s]=true;
			}
			else {
				if(note.contains("Base")) {
					base=s;
					viable[s]=true;
				}
				if(note.contains("Dominated")) {
					viable[s]=false;
					numNonviable++;
				}
			}
		}
		
		double baseCost=(double)table[base][2];
		double baseEffect=(double)table[base][3];
				
		int numViable=numStrat-numNonviable;
		
		double dataViable[][]=new double[2][numViable]; //[effect/cost][strat]
		double dataNonviable[][]=new double[2][numNonviable];
		int indexViable=0, indexNonviable=0;
		double minCost=Double.POSITIVE_INFINITY, maxCost=Double.NEGATIVE_INFINITY;
		double minEffect=Double.POSITIVE_INFINITY, maxEffect=Double.NEGATIVE_INFINITY;
		
		for(int i=0; i<numStrat; i++) {
			double cost=(double)table[i][2];
			double effect=(double)table[i][3];
			if(relative==0) {
				cost-=baseCost;
				effect-=baseEffect;
			}
			minCost=Math.min(minCost, cost); maxCost=Math.max(maxCost, cost);
			minEffect=Math.min(minEffect, effect); maxEffect=Math.max(maxEffect, effect);
						
			if(viable[i]) {
				dataViable[0][indexViable]=effect; dataViable[1][indexViable]=cost;
				indexViable++;
			}
			else {
				dataNonviable[0][indexNonviable]=effect; dataNonviable[1][indexNonviable]=cost;
				indexNonviable++;
			}
			
			XYTextAnnotation lbl=new XYTextAnnotation(" "+table[i][1],effect,cost); 
			lbl.setTextAnchor(TextAnchor.TOP_LEFT);
			Font curFont=lbl.getFont();
			lbl.setFont(new Font(curFont.getName(), Font.PLAIN, lblSize));
			plot.addAnnotation(lbl);
		}

		for(int i=0; i<numViable-1; i++) {
			double effect1=dataViable[0][i];
			double effect2=dataViable[0][i+1];
			double cost1=dataViable[1][i];
			double cost2=dataViable[1][i+1];
			
			XYLineAnnotation line=new XYLineAnnotation(effect1,cost1,effect2,cost2);
			plot.addAnnotation(line);
		}
		
		dataXY.addSeries("1",dataViable);
		dataXY.addSeries("2", dataNonviable);
		
		plot.getRangeAxis().setRange(minCost,maxCost);
		plot.getDomainAxis().setRange(minEffect,maxEffect);
		
		if(relative==0) {
			plot.getRangeAxis().setLabel("∆ "+myModel.dimInfo.dimNames[myModel.dimInfo.costDim]);
			plot.getDomainAxis().setLabel("∆ "+myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]);
		}
		else if(relative==1) {
			plot.getRangeAxis().setLabel(myModel.dimInfo.dimNames[myModel.dimInfo.costDim]);
			plot.getDomainAxis().setLabel(myModel.dimInfo.dimNames[myModel.dimInfo.effectDim]);
		}
		
		panelChart.zoomOutBoth(0, 0);
	}
	
}
