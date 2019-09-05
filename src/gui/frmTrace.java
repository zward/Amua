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
import java.util.ArrayList;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import filters.CSVFilter;
import main.ErrorLog;
import main.ScaledIcon;
import markov.MarkovTrace;

import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
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
public class frmTrace {

	public JFrame frmTrace;
	MarkovTrace curTrace, traceOverall, traceGroup[];
	int numSubgroups=0;
	String subgroupNames[];
	JComboBox comboPlot, comboGroup;
	DefaultXYDataset dataTrace;
	XYSeriesCollection colSeries;
	JFreeChart chartTrace;
	XYLineAndShapeRenderer renderer;
	DefaultDrawingSupplier supplier;
	private JTable table;
	JFileChooser fc;
	ErrorLog errorLog;
	
	/**
	 *  Default Constructor
	 */
	public frmTrace(MarkovTrace traceOverall, ErrorLog errorLog1, MarkovTrace traceGroup[], String subgroupNames[]) {
		this.traceOverall=traceOverall;
		this.errorLog=errorLog1;
		this.traceGroup=traceGroup;
		curTrace=traceOverall;
		if(traceGroup!=null){
			numSubgroups=traceGroup.length;
			this.subgroupNames=subgroupNames;
		}
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmTrace = new JFrame();
			frmTrace.setTitle("Amua - Markov Trace: "+traceOverall.traceName);
			frmTrace.setIconImage(Toolkit.getDefaultToolkit().getImage(frmTrace.class.getResource("/images/logo_128.png")));
			frmTrace.setBounds(100, 100, 1000, 600);
			frmTrace.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{561, 557, 0};
			gridBagLayout.rowHeights = new int[]{32, 514, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			frmTrace.getContentPane().setLayout(gridBagLayout);

			dataTrace = new DefaultXYDataset();
			colSeries=new XYSeriesCollection();
			
			chartTrace = ChartFactory.createScatterPlot(null, "t", "Prev(t)", dataTrace, PlotOrientation.VERTICAL, true, false, false);
			chartTrace.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
			
			JPanel panel = new JPanel();
			panel.setLayout(null);
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			frmTrace.getContentPane().add(panel, gbc_panel);
			
			JLabel lblNewLabel = new JLabel("Plot:");
			lblNewLabel.setBounds(6, 6, 55, 16);
			panel.add(lblNewLabel);
			
			comboPlot = new JComboBox();
			comboPlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateChart(comboPlot.getSelectedIndex());
				}
			});
			comboPlot.setModel(new DefaultComboBoxModel(new String[] {"State Prevalence", "Rewards (Cycle)", "Rewards (Cum.)"}));
			if(traceOverall.numVariables>0){
				comboPlot.setModel(new DefaultComboBoxModel(new String[] {"State Prevalence", "Rewards (Cycle)", "Rewards (Cum.)","Variables (Cycle)"}));
			}
			comboPlot.setBounds(41, 1, 157, 26);
			panel.add(comboPlot);
			
			JLabel lblGroup = new JLabel("Group:");
			lblGroup.setVisible(false);
			lblGroup.setBounds(213, 6, 55, 16);
			panel.add(lblGroup);
			
			comboGroup = new JComboBox();
			comboGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int selected=comboGroup.getSelectedIndex();
					if(selected==0){curTrace=traceOverall;}
					else{curTrace=traceGroup[selected-1];}
					table.setModel(curTrace.modelTraceRounded);
					updateChart(comboPlot.getSelectedIndex());
				}
			});
			comboGroup.setVisible(false);
			comboGroup.setBounds(257, 1, 165, 26);
			panel.add(comboGroup);
			
			if(numSubgroups>0){
				String names[]=new String[numSubgroups+1];
				names[0]="Overall";
				for(int g=0; g<numSubgroups; g++){names[g+1]=subgroupNames[g];}
				comboGroup.setModel(new DefaultComboBoxModel(names));
				comboGroup.setVisible(true);
				lblGroup.setVisible(true);
			}
			
			ChartPanel panelChart = new ChartPanel(chartTrace,false);
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.insets = new Insets(0, 0, 0, 5);
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 0;
			gbc_panelChart.gridy = 1;
			frmTrace.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setBorder(new LineBorder(new Color(0, 0, 0)));
			
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
			GridBagConstraints gbc_toolBar = new GridBagConstraints();
			gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_toolBar.insets = new Insets(0, 0, 5, 0);
			gbc_toolBar.gridx = 1;
			gbc_toolBar.gridy = 0;
			frmTrace.getContentPane().add(toolBar, gbc_toolBar);
			
			JButton btnExport = new JButton("Export");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						fc=new JFileChooser();
						fc.setDialogTitle("Export Trace");
						fc.setApproveButtonText("Export");
						fc.setFileFilter(new CSVFilter());

						int returnVal = fc.showOpenDialog(frmTrace);
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
							
							JOptionPane.showMessageDialog(frmTrace, "Exported!");
						}

					}catch(Exception er){
						JOptionPane.showMessageDialog(frmTrace,er.getMessage());
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
			frmTrace.getContentPane().add(scrollPane, gbc_scrollPane);
			
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
		XYPlot plot = chartTrace.getXYPlot();
		renderer = new XYLineAndShapeRenderer(true,false);
		supplier = new DefaultDrawingSupplier();
		int numStates=curTrace.stateNames.length;
		//Clear series
		while(dataTrace.getSeriesCount()>0){
			dataTrace.removeSeries(dataTrace.getSeriesKey(0));
		}
		
		if(type==0){ //Prevalence
			for(int s=0; s<numStates; s++){
				renderer.setSeriesPaint(s, supplier.getNextPaint());
				dataTrace.addSeries(curTrace.stateNames[s],getSeriesData(curTrace.cycles,curTrace.prev[s]));
			}
			plot.setRenderer(renderer);
			plot.setDataset(dataTrace);
			plot.getRangeAxis().setLabel("Prev(t)");
		}
		else if(type==1){ //Rewards - Cycle
			for(int d=0; d<curTrace.numDim; d++){
				renderer.setSeriesPaint(d, supplier.getNextPaint());
				dataTrace.addSeries(curTrace.dimNames[d],getSeriesData(curTrace.cycles,curTrace.cycleRewards[d]));
			}
			if(curTrace.discounted==true){
				for(int d=0; d<curTrace.numDim; d++){
					renderer.setSeriesPaint(curTrace.numDim+d, supplier.getNextPaint());
					dataTrace.addSeries(curTrace.dimNames[d]+" (Discounted)",getSeriesData(curTrace.cycles,curTrace.cycleRewardsDis[d]));
				}
			}
			plot.setRenderer(renderer);
			plot.setDataset(dataTrace);
			plot.getRangeAxis().setLabel("Rewards(t)");
		}
		else if(type==2){ //Rewards - Cumulative
			for(int d=0; d<curTrace.numDim; d++){
				renderer.setSeriesPaint(d, supplier.getNextPaint());
				dataTrace.addSeries(curTrace.dimNames[d],getSeriesData(curTrace.cycles,curTrace.cumRewards[d]));
			}
			if(curTrace.discounted==true){
				for(int d=0; d<curTrace.numDim; d++){
					renderer.setSeriesPaint(curTrace.numDim+d, supplier.getNextPaint());
					dataTrace.addSeries(curTrace.dimNames[d]+" (Discounted)",getSeriesData(curTrace.cycles,curTrace.cumRewardsDis[d]));
				}
			}
			plot.setRenderer(renderer);
			plot.setDataset(dataTrace);
			plot.getRangeAxis().setLabel("Cum. Rewards(t)");
		}
		else if(type==3){ //Variables - Cycle
			for(int c=0; c<curTrace.numVariables; c++){
				renderer.setSeriesPaint(c, supplier.getNextPaint());
				dataTrace.addSeries(curTrace.varNames[c],getSeriesData(curTrace.cycles,curTrace.cycleVariables[c]));
			}
			plot.setRenderer(renderer);
			plot.setDataset(dataTrace);
			plot.getRangeAxis().setLabel("Variables(t)");
		}
	}
	
	private double [][] getSeriesData(ArrayList<Integer> cycle, ArrayList<Double> traceData){
		int numCycles=cycle.size();
		double data[][]=new double[2][numCycles];
		for(int i=0; i<numCycles; i++){
			data[0][i]=cycle.get(i);
			data[1][i]=traceData.get(i);
		}
		return(data);
	}
}
