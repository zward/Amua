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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 */
public class frmChangeSeriesColors {
	
	/**
	 * JFrame for form
	 */
	public JDialog frmChangeSeriesColors;
	//AmuaModel myModel;
	
	/**
	 * 0=XY, 1=Interval (Tornado plot), 2=XYIntervalSeriesCollection (Stacked one-way), 3=Two-way, 4=PSA Strategies, 5=Batch Runs, 6=Trace Summary, 7=Trace Summary Multi
	 */
	int type;
	JFreeChart chart;
	DefaultXYDataset chartData_XY;
	DefaultIntervalCategoryDataset chartData_Interval;
	XYIntervalSeriesCollection chartData_XYSeries;
	XYSeriesCollection chartData_Series;
	
	frmTornadoDiagram frmPlot_Tornado;
	frmSensTwoWay frmPlot_TwoWay;
	frmPSA frmPlot_PSA;
	frmBatch frmPlot_Batch;
	frmTraceSummary frmPlot_TraceSummary;
	frmTraceSummaryMulti frmPlot_TraceSummaryMulti;
	
	Paint seriesPaints[];
	XYLineAndShapeRenderer renderer;
	XYDifferenceRenderer rendererDiff[];
	
	DefaultTableModel modelSeries;
	private JTable tableSeries;
	
	public frmChangeSeriesColors(JFreeChart chart, DefaultXYDataset chartData, Paint seriesPaints[]){
		this.chart=chart;
		this.chartData_XY=chartData;
		this.seriesPaints=seriesPaints;
		this.type=0;
		initialize();
	}
	
	public frmChangeSeriesColors(JFreeChart chart, DefaultIntervalCategoryDataset chartData, Paint seriesPaints[], frmTornadoDiagram frmPlot){
		this.frmPlot_Tornado=frmPlot;
		this.chart=chart;
		this.chartData_Interval=chartData;
		this.seriesPaints=seriesPaints;
		this.type=1;
		initialize();
	}
	
	public frmChangeSeriesColors(JFreeChart chart, XYIntervalSeriesCollection chartData, Paint seriesPaints[]){
		this.chart=chart;
		this.chartData_XYSeries=chartData;
		this.seriesPaints=seriesPaints;
		this.type=2;
		initialize();
	}
	
	public frmChangeSeriesColors(JFreeChart chart, DefaultXYDataset chartData, Paint seriesPaints[], frmSensTwoWay frmPlot){
		this.chart=chart;
		this.chartData_XY=chartData;
		this.seriesPaints=seriesPaints;
		frmPlot_TwoWay=frmPlot;
		this.type=3;
		initialize();
	}
	
	public frmChangeSeriesColors(JFreeChart chart, DefaultXYDataset chartData, Paint seriesPaints[], frmPSA frmPlot){
		this.chart=chart;
		this.chartData_XY=chartData;
		this.seriesPaints=seriesPaints;
		frmPlot_PSA=frmPlot;
		this.type=4;
		initialize();
	}
	
	public frmChangeSeriesColors(JFreeChart chart, DefaultXYDataset chartData, Paint seriesPaints[], frmBatch frmPlot){
		this.chart=chart;
		this.chartData_XY=chartData;
		this.seriesPaints=seriesPaints;
		frmPlot_Batch=frmPlot;
		this.type=5;
		initialize();
	}
	
	public frmChangeSeriesColors(frmTraceSummary frmPlot, Paint seriesPaints[]) {
		this.frmPlot_TraceSummary=frmPlot;
		this.seriesPaints=seriesPaints;
		this.type=6;
		initialize();
	}
	
	public frmChangeSeriesColors(frmTraceSummaryMulti frmPlot, Paint seriesPaints[]) {
		this.frmPlot_TraceSummaryMulti=frmPlot;
		this.seriesPaints=seriesPaints;
		this.type=7;
		initialize();
	}
	
	
	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmChangeSeriesColors = new JDialog();
			//frmChangeSeriesColors.setIconImage(Toolkit.getDefaultToolkit().getImage(frmChangeSeriesColors.class.getResource("/images/import_128.png")));
			frmChangeSeriesColors.setModalityType(ModalityType.APPLICATION_MODAL);
			frmChangeSeriesColors.setTitle("Amua - Change Series Colors");
			frmChangeSeriesColors.setResizable(false);
			frmChangeSeriesColors.setBounds(100, 100, 316, 356);
			frmChangeSeriesColors.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmChangeSeriesColors.getContentPane().setLayout(null);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 13, 286, 261);
			frmChangeSeriesColors.getContentPane().add(scrollPane);
			
			modelSeries=new DefaultTableModel(
					new Object[][] {
						{null, null},
					},
					new String[] {
						"Series", "Color"
					}
				) {
					boolean[] columnEditables = new boolean[] {
						false, false
					};
					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				};
			
			//add series
			modelSeries.setRowCount(0);
			if(type==0) { //XY
				int numSeries=chartData_XY.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_XY.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==1) { //Interval
				int numSeries=chartData_Interval.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_Interval.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==2) { //XYSeries
				int numSeries=chartData_XYSeries.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_XYSeries.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==3) { //XY, two-way
				int numSeries=chartData_XY.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_XY.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==4) { //XY, PSA
				int numSeries=chartData_XY.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_XY.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==5) { //XY, batch
				int numSeries=chartData_XY.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {chartData_XY.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==6) { //XY, trace summary
				int numSeries=frmPlot_TraceSummary.mean.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {frmPlot_TraceSummary.mean.getSeriesKey(s), seriesPaints[s]});
				}
			}
			else if(type==7) { //XY, trace summary multi
				int numSeries=frmPlot_TraceSummaryMulti.mean.getSeriesCount();
				for(int s=0; s<numSeries; s++) {
					modelSeries.addRow(new Object[] {frmPlot_TraceSummaryMulti.mean.getSeriesKey(s), seriesPaints[s]});
				}
			}
			
			tableSeries = new JTable();
			tableSeries.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) { //double-click
						selectColor();
					}
					
				}
			});
			tableSeries.setModel(modelSeries);
			tableSeries.setRowHeight(20);
			tableSeries.getColumnModel().getColumn(1).setCellRenderer(new ColorPatchRenderer());
			
			scrollPane.setViewportView(tableSeries);
			
			
			JButton btnCancel = new JButton("Close");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmChangeSeriesColors.dispose();
				}
			});
			btnCancel.setBounds(208, 286, 90, 28);
			frmChangeSeriesColors.getContentPane().add(btnCancel);
			
		
			JButton btnSelectColor = new JButton("Select Color");
			btnSelectColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectColor();
				}
			});
			btnSelectColor.setBounds(12, 286, 105, 28);
			frmChangeSeriesColors.getContentPane().add(btnSelectColor);
			
			
						
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void selectColor() {
		int selected=tableSeries.getSelectedRow();
		if(selected!=-1) {
			if(type==0) { //XY
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_XY.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
				}
			}
			else if(type==1) { //Interval (tornado diagram)
				IntervalBarRenderer renderer = (IntervalBarRenderer) chart.getCategoryPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_Interval.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
					frmPlot_Tornado.updateRangeMarkers();
				}
			}
			else if(type==2) { //XYSeries
				XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_XYSeries.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
				}
			}
			else if(type==3) { //XY, two-way
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_XY.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
					frmPlot_TwoWay.updatePlot(); //re-draw annotations
				}
			}
			else if(type==4) { //XY, PSA
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_XY.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
					frmPlot_PSA.updateStratSeriesColor(selected); //update all renderers
				}
			}
			else if(type==5) { //XY, Batch
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + chartData_XY.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					renderer.setSeriesPaint(selected, c);
					modelSeries.setValueAt(c, selected, 1); //update table
					frmPlot_Batch.updateStratSeriesColor(selected); //update all renderers
				}
			}
			else if(type==6) { //XY, Trace summary
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) frmPlot_TraceSummary.chartTrace.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + frmPlot_TraceSummary.mean.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					//update plot
					int type=frmPlot_TraceSummary.comboPlot.getSelectedIndex();
					frmPlot_TraceSummary.updateChart(type);
					modelSeries.setValueAt(c, selected, 1); //update table
				}
			}
			else if(type==7) { //XY, Trace summary multi
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) frmPlot_TraceSummaryMulti.chartTrace.getXYPlot().getRenderer();
				Color initColor = (Color) renderer.getSeriesPaint(selected);
				Color c = JColorChooser.showDialog(
						frmChangeSeriesColors,
						"Choose color for series: " + frmPlot_TraceSummaryMulti.mean.getSeriesKey(selected),
						initColor == null ? Color.BLACK : initColor
						);
				if (c != null) {
					seriesPaints[selected]=c;
					//update plot
					int type=frmPlot_TraceSummaryMulti.comboPlot.getSelectedIndex();
					frmPlot_TraceSummaryMulti.updateChart(type);
					modelSeries.setValueAt(c, selected, 1); //update table
				}
			}
		}
	}
	
	// Custom color renderer showing a color patch and the RGB code
    static class ColorPatchRenderer extends DefaultTableCellRenderer {
        private static final int PATCH_SIZE = 18;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            // superclass configures the text colors, etc.
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            panel.setOpaque(true);
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            Color color = (value instanceof Color) ? (Color) value : Color.BLACK;

            // Custom component for the color patch
            JLabel patch = new JLabel() {
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(color);
                    g.fillRect(2, 2, PATCH_SIZE - 4, PATCH_SIZE - 4);
                    g.setColor(Color.BLACK);
                    g.drawRect(2, 2, PATCH_SIZE - 4, PATCH_SIZE - 4);
                }
                public Dimension getPreferredSize() {
                    return new Dimension(PATCH_SIZE, PATCH_SIZE);
                }
            };

            // Show the [R,G,B] code
            String rgbText = String.format("[%d,%d,%d]",
                    color.getRed(), color.getGreen(), color.getBlue());
            JLabel text = new JLabel(rgbText);

            panel.add(patch);
            panel.add(text);

            return panel;
        }
    }
}


