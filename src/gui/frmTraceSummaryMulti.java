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
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import base.RunReportSummary;
import filters.CSVFilter;
import main.ErrorLog;
import main.ScaledIcon;
import markov.MarkovTraceSummary;

import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 *
 */
public class frmTraceSummaryMulti {

	public JFrame frmTraceSummaryMulti;
	RunReportSummary runReport;
	MarkovTraceSummary curTrace;
	int numSubgroups=0;
	String groupNames[];
	JComboBox comboChain, comboPlot, comboGroup;
	XYSeriesCollection mean, bounds[];
	JFreeChart chartTrace;
	XYLineAndShapeRenderer renderer;
	DefaultDrawingSupplier supplier;
	private JTable table;
	JFileChooser fc;
	ErrorLog errorLog;
	
	/**
	 *  Default Constructor
	 */
	public frmTraceSummaryMulti (RunReportSummary runReport, ErrorLog errorLog1) {
		this.runReport=runReport;
		this.errorLog=errorLog1;
		curTrace=runReport.markovTraceSummary[0];
		if(runReport.markovTraceSummaryGroup!=null){
			numSubgroups=runReport.subgroupNames.length;
			groupNames=new String[numSubgroups+1];
			groupNames[0]="Overall";
			for(int g=0; g<numSubgroups; g++) {
				groupNames[g+1]=runReport.subgroupNames[g];
			}
		}
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTraceSummaryMulti = new JFrame();
			frmTraceSummaryMulti.setTitle("Amua - Markov Trace Summary");
			frmTraceSummaryMulti.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_128.png")));
			frmTraceSummaryMulti.setBounds(100, 100, 1200, 600);
			frmTraceSummaryMulti.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{561, 557, 0};
			gridBagLayout.rowHeights = new int[]{32, 514, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmTraceSummaryMulti.getContentPane().setLayout(gridBagLayout);

			mean=new XYSeriesCollection();
			
			chartTrace = ChartFactory.createScatterPlot(null, "t", "Prev(t)", mean, PlotOrientation.VERTICAL, true, false, false);
			chartTrace.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			
			JPanel panel = new JPanel();
			panel.setLayout(null);
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			frmTraceSummaryMulti.getContentPane().add(panel, gbc_panel);
			
			JLabel lblChain = new JLabel("Chain:");
			lblChain.setBounds(2, 5, 42, 16);
			panel.add(lblChain);
			
			comboChain = new JComboBox();
			comboChain.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//get chain
					int chainIndex=comboChain.getSelectedIndex();
					int selected=comboGroup.getSelectedIndex();
					if(selected<=0){curTrace=runReport.markovTraceSummary[chainIndex];}
					else{curTrace=runReport.markovTraceSummaryGroup[selected-1][chainIndex];}
					table.setModel(curTrace.modelTraceRounded);
					updateChart(comboPlot.getSelectedIndex());
				}
			});
			comboChain.setModel(new DefaultComboBoxModel(runReport.markovChainNames));
			comboChain.setBounds(41, 0, 150, 26);
			panel.add(comboChain);
			
			JLabel lblNewLabel = new JLabel("Plot:");
			lblNewLabel.setBounds(207, 5, 34, 16);
			panel.add(lblNewLabel);
			
			comboPlot = new JComboBox();
			comboPlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateChart(comboPlot.getSelectedIndex());
				}
			});
			comboPlot.setModel(new DefaultComboBoxModel(new String[] {"State Prevalence", "Rewards (Cycle)", "Rewards (Cum.)"}));
			if(curTrace.numVariables>0){
				comboPlot.setModel(new DefaultComboBoxModel(new String[] {"State Prevalence", "Rewards (Cycle)", "Rewards (Cum.)","Variables (Cycle)"}));
			}
			comboPlot.setBounds(235, 0, 150, 26);
			panel.add(comboPlot);
			
			JLabel lblGroup = new JLabel("Group:");
			lblGroup.setVisible(false);
			lblGroup.setBounds(397, 5, 55, 16);
			panel.add(lblGroup);
			
			comboGroup = new JComboBox();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int chainIndex=comboChain.getSelectedIndex();
					int selected=comboGroup.getSelectedIndex();
					if(selected<=0){curTrace=runReport.markovTraceSummary[chainIndex];}
					else{curTrace=runReport.markovTraceSummaryGroup[selected-1][chainIndex];}
					table.setModel(curTrace.modelTraceRounded);
					updateChart(comboPlot.getSelectedIndex());
				}
			});
			comboGroup.setVisible(false);
			comboGroup.setBounds(441, 0, 150, 26);
			panel.add(comboGroup);
			
			if(numSubgroups>0){
				comboGroup.setModel(new DefaultComboBoxModel(groupNames));
				lblGroup.setVisible(true);
				comboGroup.setVisible(true);
			}
			
			
			ChartPanel panelChart = new ChartPanel(chartTrace,false);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.insets = new Insets(0, 0, 0, 5);
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 0;
			gbc_panelChart.gridy = 1;
			frmTraceSummaryMulti.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));
			
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_toolBar.insets = new Insets(0, 0, 5, 0);
			gbc_toolBar.gridx = 1;
			gbc_toolBar.gridy = 0;
			frmTraceSummaryMulti.getContentPane().add(toolBar, gbc_toolBar);
			
			JButton btnExport = new JButton("Export");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						fc=new JFileChooser();
						fc.setDialogTitle("Export Trace");
						fc.setApproveButtonText("Export");
						fc.setFileFilter(new CSVFilter());

						int returnVal = fc.showOpenDialog(frmTraceSummaryMulti);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String filepath=file.getAbsolutePath().replaceAll(".csv","");
							FileWriter fstream = new FileWriter(filepath+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							//Write headers
							int numCol=curTrace.modelTraceRounded.getColumnCount();
							int numRow=curTrace.modelTraceRounded.getRowCount();
							for(int c=0; c<numCol-1; c++){
								out.write(curTrace.modelTraceRounded.getColumnName(c)+",");
							}
							out.write(curTrace.modelTraceRounded.getColumnName(numCol-1)); out.newLine();
							
							//Write trace rows
							for(int r=0; r<numRow; r++){
								for(int c=0; c<numCol-1; c++){
									out.write(curTrace.modelTraceRounded.getValueAt(r, c)+",");
								}
								out.write(curTrace.modelTraceRounded.getValueAt(r, numCol-1)+""); out.newLine();
							}
							
							out.close();
							
							JOptionPane.showMessageDialog(frmTraceSummaryMulti, "Exported!");
						}

					}catch(Exception er){
						JOptionPane.showMessageDialog(frmTraceSummaryMulti,er.getMessage());
						errorLog.recordError(er);
					}
				}
			});
			btnExport.setIcon(new ScaledIcon("/images/export",16,16,16,true));
			btnExport.setToolTipText("Export");
			toolBar.add(btnExport);
			
			JButton btnCopy = new JButton("Copy");
			btnCopy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int numCol=curTrace.modelTraceRounded.getColumnCount();
					int numRow=curTrace.modelTraceRounded.getRowCount();
					String data[][]=new String[numRow+1][numCol];
					//Get headers
					for(int c=0; c<numCol; c++){
						data[0][c]=curTrace.modelTraceRounded.getColumnName(c);
					}
					//Get row
					for(int r=0; r<numRow; r++){
						for(int c=0; c<numCol; c++){
							data[r+1][c]=curTrace.modelTraceRounded.getValueAt(r, c)+"";
						}
					}
					
					Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
					clip.setContents(new DataTransferable(data), null);
					
				}
			});
			btnCopy.setIcon(new ScaledIcon("/images/copy",16,16,16,true));
			btnCopy.setToolTipText("Copy");
			toolBar.add(btnCopy);
			
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 1;
			frmTraceSummaryMulti.getContentPane().add(scrollPane, gbc_scrollPane);
			
			updateChart(0); //show prevalence
			
			table = new JTable();
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setEnabled(false);
			table.setModel(curTrace.modelTraceRounded);
			table.setShowVerticalLines(true);
			table.getTableHeader().setReorderingAllowed(false);
			scrollPane.setViewportView(table);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	class DataTransferable implements Transferable
	{
	   public DataTransferable(String data[][]){
		   //Build string
		   strData=""; //empty string
		   int numRow=data.length;
		   int numCol=data[0].length;
		   for(int r=0; r<numRow; r++){
			   for(int c=0; c<numCol; c++){
				   strData+=data[r][c]+"\t";
			   }
			   strData+="\n";
		   }
	   }
	   public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[] { DataFlavor.stringFlavor };}
	   public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.stringFlavor);}
	   public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{
	      if (flavor.equals(DataFlavor.stringFlavor)){return strData;}
	      else{throw new UnsupportedFlavorException(flavor);}
	   }
	   private String strData;
	}
	
	public void updateChart(int type){
		int alpha=50;
		XYPlot plot = chartTrace.getXYPlot();
		plot.setFixedLegendItems(null); //dynamic legend
		renderer = new XYLineAndShapeRenderer(true,false);
		supplier = new DefaultDrawingSupplier();
				
		//Clear series
		mean.removeAllSeries();
		if(bounds!=null){
			for(int b=0; b<bounds.length; b++){
				bounds[b].removeAllSeries();
			}
		}
				
		if(type==0){ //Prevalence
			int numStates=curTrace.stateNames.length;
			XYDifferenceRenderer rendererDiff[]=new XYDifferenceRenderer[numStates];
			bounds=new XYSeriesCollection[numStates];
			for(int s=0; s<numStates; s++){
				mean.addSeries(getSeries(curTrace.stateNames[s],curTrace.prev[s],0));
				bounds[s]=new XYSeriesCollection();
				bounds[s].addSeries(getSeries(curTrace.stateNames[s],curTrace.prev[s],1)); //lb
				bounds[s].addSeries(getSeries(curTrace.stateNames[s],curTrace.prev[s],2)); //ub
				
				Paint curPaint=supplier.getNextPaint();
				renderer.setSeriesPaint(s, curPaint);
				Color curColor=(Color) curPaint;
				Color fill=new Color(curColor.getRed(),curColor.getGreen(),curColor.getBlue(),alpha);
				rendererDiff[s]=new XYDifferenceRenderer(fill,fill,false);
	        	rendererDiff[s].setSeriesPaint(0, new Color(0,0,0,0));
	            rendererDiff[s].setSeriesPaint(1, new Color(0,0,0,0));
			}
			plot.setDataset(0,mean);
			plot.setRenderer(0,renderer);
			for(int s=0; s<numStates; s++){
				plot.setDataset(s+1,bounds[s]);
				plot.setRenderer(s+1,rendererDiff[s]);
			}
			plot.getRangeAxis().setLabel("Prev(t)");
			//update legend
			LegendItemCollection legendItemsOld=plot.getLegendItems();
	        final LegendItemCollection legendItemsNew=new LegendItemCollection();
	        for(int i=0; i<numStates; i++){
	        	legendItemsNew.add(legendItemsOld.get(i));
	        } 
	        plot.setFixedLegendItems(legendItemsNew);
		}
		else if(type==1){ //Rewards - Cycle
			int numLines=curTrace.numDim;
			if(curTrace.discounted){numLines*=2;}
			XYDifferenceRenderer rendererDiff[]=new XYDifferenceRenderer[numLines];
			bounds=new XYSeriesCollection[numLines];
			for(int d=0; d<curTrace.numDim; d++){
				mean.addSeries(getSeries(curTrace.dimNames[d],curTrace.cycleRewards[d],0));
				bounds[d]=new XYSeriesCollection();
				bounds[d].addSeries(getSeries(curTrace.dimNames[d],curTrace.cycleRewards[d],1)); //lb
				bounds[d].addSeries(getSeries(curTrace.dimNames[d],curTrace.cycleRewards[d],2)); //ub
			}
			if(curTrace.discounted){
				for(int d=0; d<curTrace.numDim; d++){
					mean.addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cycleRewardsDis[d],0));
					bounds[curTrace.numDim+d]=new XYSeriesCollection();
					bounds[curTrace.numDim+d].addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cycleRewardsDis[d],1)); //lb
					bounds[curTrace.numDim+d].addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cycleRewardsDis[d],2)); //ub
				}
			}
			for(int d=0; d<numLines; d++){
				Paint curPaint=supplier.getNextPaint();
				renderer.setSeriesPaint(d, curPaint);
				Color curColor=(Color) curPaint;
				Color fill=new Color(curColor.getRed(),curColor.getGreen(),curColor.getBlue(),alpha);
				rendererDiff[d]=new XYDifferenceRenderer(fill,fill,false);
	        	rendererDiff[d].setSeriesPaint(0, new Color(0,0,0,0));
	            rendererDiff[d].setSeriesPaint(1, new Color(0,0,0,0));
			}
			
			plot.setDataset(0,mean);
			plot.setRenderer(0,renderer);
			for(int d=0; d<numLines; d++){
				plot.setDataset(d+1,bounds[d]);
				plot.setRenderer(d+1,rendererDiff[d]);
			}
			plot.getRangeAxis().setLabel("Rewards(t)");
			//legend
			LegendItemCollection legendItemsOld=plot.getLegendItems();
	        final LegendItemCollection legendItemsNew=new LegendItemCollection();
	        for(int i=0; i<numLines; i++){
	        	legendItemsNew.add(legendItemsOld.get(i));
	        } 
	        plot.setFixedLegendItems(legendItemsNew);
		}
		else if(type==2){ //Rewards - Cumulative
			int numLines=curTrace.numDim;
			if(curTrace.discounted){numLines*=2;}
			XYDifferenceRenderer rendererDiff[]=new XYDifferenceRenderer[numLines];
			bounds=new XYSeriesCollection[numLines];
			for(int d=0; d<curTrace.numDim; d++){
				mean.addSeries(getSeries(curTrace.dimNames[d],curTrace.cumRewards[d],0));
				bounds[d]=new XYSeriesCollection();
				bounds[d].addSeries(getSeries(curTrace.dimNames[d],curTrace.cumRewards[d],1)); //lb
				bounds[d].addSeries(getSeries(curTrace.dimNames[d],curTrace.cumRewards[d],2)); //ub
			}
			if(curTrace.discounted){
				for(int d=0; d<curTrace.numDim; d++){
					mean.addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cumRewardsDis[d],0));
					bounds[curTrace.numDim+d]=new XYSeriesCollection();
					bounds[curTrace.numDim+d].addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cumRewardsDis[d],1)); //lb
					bounds[curTrace.numDim+d].addSeries(getSeries(curTrace.dimNames[d]+" (Discounted)",curTrace.cumRewardsDis[d],2)); //ub
				}
			}
			for(int d=0; d<numLines; d++){
				Paint curPaint=supplier.getNextPaint();
				renderer.setSeriesPaint(d, curPaint);
				Color curColor=(Color) curPaint;
				Color fill=new Color(curColor.getRed(),curColor.getGreen(),curColor.getBlue(),alpha);
				rendererDiff[d]=new XYDifferenceRenderer(fill,fill,false);
	        	rendererDiff[d].setSeriesPaint(0, new Color(0,0,0,0));
	            rendererDiff[d].setSeriesPaint(1, new Color(0,0,0,0));
			}
			plot.setDataset(0,mean);
			plot.setRenderer(0,renderer);
			for(int d=0; d<numLines; d++){
				plot.setDataset(d+1,bounds[d]);
				plot.setRenderer(d+1,rendererDiff[d]);
			}
			plot.getRangeAxis().setLabel("Cum. Rewards(t)");
			//legend
			LegendItemCollection legendItemsOld=plot.getLegendItems();
	        final LegendItemCollection legendItemsNew=new LegendItemCollection();
	        for(int i=0; i<numLines; i++){
	        	legendItemsNew.add(legendItemsOld.get(i));
	        } 
	        plot.setFixedLegendItems(legendItemsNew);
		}
		else if(type==3) { //Variables
			int numLines=curTrace.numVariables;
			XYDifferenceRenderer rendererDiff[]=new XYDifferenceRenderer[numLines];
			bounds=new XYSeriesCollection[numLines];
			for(int v=0; v<curTrace.numVariables; v++){
				mean.addSeries(getSeries(curTrace.varNames[v],curTrace.cycleVars[v],0));
				bounds[v]=new XYSeriesCollection();
				bounds[v].addSeries(getSeries(curTrace.varNames[v],curTrace.cycleVars[v],1)); //lb
				bounds[v].addSeries(getSeries(curTrace.varNames[v],curTrace.cycleVars[v],2)); //ub
			}
			for(int d=0; d<numLines; d++){
				Paint curPaint=supplier.getNextPaint();
				renderer.setSeriesPaint(d, curPaint);
				Color curColor=(Color) curPaint;
				Color fill=new Color(curColor.getRed(),curColor.getGreen(),curColor.getBlue(),alpha);
				rendererDiff[d]=new XYDifferenceRenderer(fill,fill,false);
	        	rendererDiff[d].setSeriesPaint(0, new Color(0,0,0,0));
	            rendererDiff[d].setSeriesPaint(1, new Color(0,0,0,0));
			}
			plot.setDataset(0,mean);
			plot.setRenderer(0,renderer);
			for(int d=0; d<numLines; d++){
				plot.setDataset(d+1,bounds[d]);
				plot.setRenderer(d+1,rendererDiff[d]);
			}
			plot.getRangeAxis().setLabel("Variables(t)");
			//legend
			LegendItemCollection legendItemsOld=plot.getLegendItems();
	        final LegendItemCollection legendItemsNew=new LegendItemCollection();
	        for(int i=0; i<numLines; i++){
	        	legendItemsNew.add(legendItemsOld.get(i));
	        } 
	        plot.setFixedLegendItems(legendItemsNew);
		}
	}
	
	
	private XYSeries getSeries(String name,double traceData[][], int sumIndex){
		String seriesName=name;
		if(sumIndex>0){seriesName+=sumIndex;}
		XYSeries curSeries=new XYSeries(seriesName);
		int numCycles=traceData[0].length;
		for(int i=0; i<numCycles; i++){
			double x=(i+1);
			double y=traceData[sumIndex][i];
			curSeries.add(x,y);
		}
		return(curSeries);
	}
}
