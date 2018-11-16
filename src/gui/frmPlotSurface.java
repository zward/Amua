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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.sf.surfaceplot.SurfaceCanvas;

import base.AmuaModel;
import main.Parameter;
import main.StyledTextPane;
import main.SurfaceModel;
import main.Table;
import main.Variable;
import math.Interpreter;
import math.Numeric;

/**
 *
 */
public class frmPlotSurface {

	public JFrame frmPlotSurface;
	AmuaModel myModel;
	StyledTextPane paneFunction;

	private JTable table;
	SurfaceModel surfaceModel;
	SurfaceCanvas surfaceCanvas;
	private JTextField textNumIntervals;

	/**
	 *  Default Constructor
	 */
	public frmPlotSurface(AmuaModel myModel) {
		this.myModel=myModel;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmPlotSurface = new JFrame();
			frmPlotSurface.setTitle("Amua - Plot Surface");
			frmPlotSurface.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
			frmPlotSurface.setBounds(100, 100, 1000, 600);
			frmPlotSurface.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{316, 0, 0};
			gridBagLayout.rowHeights = new int[]{514, 86, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			frmPlotSurface.getContentPane().setLayout(gridBagLayout);

			JPanel panelChart = new JPanel();
			GridBagConstraints gbc_panelChart = new GridBagConstraints();
			gbc_panelChart.gridwidth = 2;
			gbc_panelChart.insets = new Insets(0, 0, 5, 5);
			gbc_panelChart.fill = GridBagConstraints.BOTH;
			gbc_panelChart.gridx = 0;
			gbc_panelChart.gridy = 0;
			frmPlotSurface.getContentPane().add(panelChart, gbc_panelChart);
			panelChart.setLayout(new BorderLayout(0, 0));
			
			surfaceCanvas = new SurfaceCanvas();
			panelChart.add(surfaceCanvas);

			JPanel panel_1 = new JPanel();
			panel_1.setLayout(null);
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;
			frmPlotSurface.getContentPane().add(panel_1, gbc_panel_1);

			JButton btnPlot = new JButton("Plot");
			btnPlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plot();
				}
			});
			btnPlot.setBounds(225, 26, 86, 28);
			panel_1.add(btnPlot);

			JLabel lblFunction = new JLabel("f(x,y):");
			lblFunction.setHorizontalAlignment(SwingConstants.RIGHT);
			lblFunction.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblFunction.setBounds(260, 6, 45, 16);
			panel_1.add(lblFunction);

			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 0, 207, 61);
			panel_1.add(scrollPane_1);

			table = new JTable();
			scrollPane_1.setViewportView(table);
			table.setShowVerticalLines(true);
			table.setRowSelectionAllowed(false);
			table.getTableHeader().setReorderingAllowed(false);
			table.setModel(new DefaultTableModel(
				new Object[][] {
					{"Min", "0", "0"},
					{"Max", "1", "1"},
				},
				new String[] {
					"", "x", "y"
				}
			) {
				boolean[] columnEditables = new boolean[] {
					false, true, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
			
			JLabel lblIntervals = new JLabel("# intervals:");
			lblIntervals.setBounds(172, 61, 65, 16);
			panel_1.add(lblIntervals);
			
			textNumIntervals = new JTextField();
			textNumIntervals.setHorizontalAlignment(SwingConstants.CENTER);
			textNumIntervals.setText("100");
			textNumIntervals.setBounds(235, 55, 65, 28);
			panel_1.add(textNumIntervals);
			textNumIntervals.setColumns(10);


			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 1;
			frmPlotSurface.getContentPane().add(scrollPane, gbc_scrollPane);

			paneFunction = new StyledTextPane(myModel);
			paneFunction.setFont(new Font("Consolas", Font.PLAIN, 15));
			scrollPane.setViewportView(paneFunction);
			paneFunction.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						plot();
					}
				}
			});


		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private void plot(){
		final ProgressMonitor progress=new ProgressMonitor(frmPlotSurface, "Plot Function", "Calculating", 0, 100);

		Thread SimThread = new Thread(){ //Non-UI
			public void run(){
				try{
					//Validate domain
					boolean ok=true;
					double minX=0, maxX=1, minY=0, maxY=1;
					int numIntervals=100;
					try{
						minX=Double.parseDouble((String)table.getValueAt(0, 1));
						maxX=Double.parseDouble((String)table.getValueAt(1, 1));
						minY=Double.parseDouble((String)table.getValueAt(0, 2));
						maxY=Double.parseDouble((String)table.getValueAt(1, 2));
						numIntervals=Integer.parseInt(textNumIntervals.getText());
					}catch(Exception n){
						JOptionPane.showMessageDialog(frmPlotSurface,"Invalid plot specification!");
						ok=false;
					}

					if(ok){
						progress.setMaximum(numIntervals*numIntervals);
						double stepX=(maxX-minX)/(numIntervals*1.0);
						double stepY=(maxY-minY)/(numIntervals*1.0);

						String fx=paneFunction.getText();

						if(myModel==null){
							myModel=new AmuaModel(); //temp model
							myModel.parameters=new ArrayList<Parameter>();
							myModel.variables=new ArrayList<Variable>();
							myModel.tables=new ArrayList<Table>();
						}
						if(myModel.innateVariables==null){
							myModel.innateVariables=new ArrayList<Variable>();
						}
						
						//Get orig values for all variables
						/*int numVars=myModel.variables.size();
						double origValues[]=new double[numVars];
						for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables
							origValues[v]=myModel.variables.get(v).value;
						}*/
						
						//Create innate variable x
						Variable curX=new Variable();
						curX.name="x";
						int index=myModel.getInnateVariableIndex("x");
						if(index!=-1){myModel.innateVariables.set(index, curX);}
						else{myModel.innateVariables.add(curX);}

						Variable curY=new Variable();
						curY.name="y";
						index=myModel.getInnateVariableIndex("y");
						if(index!=-1){myModel.innateVariables.set(index, curY);}
						else{myModel.innateVariables.add(curY);}
						
						double dataSurface[][][]=new double[1][numIntervals+1][numIntervals+1];
						int prog=0;
						for(int i=0; i<=numIntervals; i++){
							for(int j=0; j<=numIntervals; j++){
								/*for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
									Variable curVar=myModel.variables.get(v);
									curVar.fixed=false;
									curVar.value=origValues[v];
								}*/

								double x=minX+stepX*i;
								curX.value=new Numeric(x);
								
								double y=minY+stepY*j;
								curY.value=new Numeric(y);
								
								double curVal;
								try{
									curVal=Interpreter.evaluate(fx, myModel,false).getDouble();
								}catch(Exception e1){
									curVal=Double.NaN;
									e1.printStackTrace();
									JOptionPane.showMessageDialog(frmPlotSurface, e1.getMessage());
									i=numIntervals; //end loop
									j=numIntervals;
								}
								
								dataSurface[0][i][j]=curVal;

								prog++;
								progress.setProgress(prog);
								
							}
						}

						//Remove vars
						index=myModel.getInnateVariableIndex("x");
						if(index!=-1){myModel.innateVariables.remove(index);}
						index=myModel.getInnateVariableIndex("y");
						if(index!=-1){myModel.innateVariables.remove(index);}

						//Reset all variables
						/*for(int v=0; v<numVars; v++){ //Reset 'fixed' for all variables and orig values
							Variable curVar=myModel.variables.get(v);
							curVar.fixed=false;
							curVar.value=origValues[v];
						}
						for(int v=0; v<numVars; v++){ //Evaluate all variables
							myModel.variables.get(v).evaluate(myModel);
						}*/

						//Update chart
						surfaceModel = new SurfaceModel(dataSurface,0,numIntervals,minX,maxX,minY,maxY,"x","y","f(x,y)");
						surfaceCanvas.setModel(surfaceModel);
						surfaceCanvas.repaint();

						progress.close();
					}

				} catch (Exception e) {
					int index=myModel.getInnateVariableIndex("x");
					if(index!=-1){myModel.innateVariables.remove(index);}
					index=myModel.getInnateVariableIndex("y");
					if(index!=-1){myModel.innateVariables.remove(index);}

					e.printStackTrace();
					progress.close();
					JOptionPane.showMessageDialog(frmPlotSurface, e.getMessage());
				}
			}
		};
		SimThread.start();
	}
}
